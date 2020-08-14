package com.sonicether.soundphysics.soundsystem;

import com.sonicether.soundphysics.Config;
import com.sonicether.soundphysics.SoundPhysics;
import com.sonicether.soundphysics.coremod.ASMTools;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.sound.PlaySoundSourceEvent;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;
import org.lwjgl.openal.EXTEfx;

public class Enviroment {
	public static void evaluateEnvironment(final PlaySoundSourceEvent SoundSourceEvent) {
		final Minecraft mc = Minecraft.getInstance();
		try {
			ISound sourceSound = SoundSourceEvent.getSound();
			if (mc.player == null || mc.world == null || sourceSound.getY() <= 0) {
				// posY <= 0 as a condition has to be there: Ingame
				// menu clicks do have a player and world present
				setEnvironment(SoundSourceEvent, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f);
				return;
			}

			final boolean isWeather = (SoundSourceEvent.getSound().getCategory() == SoundCategory.WEATHER);

			if (Config.skipRainOcclusionTracing.get() && isWeather) {
				setEnvironment(SoundSourceEvent, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f);
				return;
			}

			float directCutoff = 1.0f;
			final float absorptionCoeff = Config.globalBlockAbsorption.get().floatValue() * 3.0f;

			final Vec3d playerPos = new Vec3d(mc.player.getPosX(), mc.player.getPosY() + mc.player.getEyeHeight(), mc.player.getPosZ());
			final Vec3d soundPos = Utils.offsetSound(SoundSourceEvent, playerPos);
			final Vec3d normalToPlayer = playerPos.subtract(soundPos).normalize();

			float airAbsorptionFactor = 1.0f;

			if (Config.snowAirAbsorptionFactor.get() > 1.0f && mc.world.isRaining()) {
				final Vec3d middlePos = playerPos.add(soundPos).scale(0.5);
				final BlockPos playerPosBlock = new BlockPos(playerPos);
				final BlockPos soundPosBlock = new BlockPos(soundPos);
				final BlockPos middlePosBlock = new BlockPos(middlePos);
				final int snowingPlayer = Utils.isSnowingAt(playerPosBlock, false) ? 1 : 0;
				final int snowingSound = Utils.isSnowingAt(soundPosBlock, false) ? 1 : 0;
				final int snowingMiddle = Utils.isSnowingAt(middlePosBlock, false) ? 1 : 0;
				final float snowFactor = snowingPlayer * 0.25f + snowingMiddle * 0.5f + snowingSound * 0.25f;
				airAbsorptionFactor = Math.max(Config.snowAirAbsorptionFactor.get().floatValue() * mc.world.getRainStrength(1.0f) * snowFactor, airAbsorptionFactor);
			}

			Vec3d rayOrigin = soundPos;
			float occlusionAccumulation = 0.0f;

			for (int i = 0; i < 10; i++) {
				final RayTraceContext rayTraceContext = new RayTraceContext(rayOrigin, playerPos, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.ANY, mc.player);
				final BlockRayTraceResult rayHit = mc.world.rayTraceBlocks(rayTraceContext);

				if (rayHit.getType() == RayTraceResult.Type.MISS) {
					break;
				}

				final BlockState blockHit = mc.world.getBlockState(rayHit.getPos());

				float blockOcclusion = 1.0f;

				if (!blockHit.isOpaqueCube(mc.world, rayHit.getPos())) {
					blockOcclusion *= 0.15f;
				}

				occlusionAccumulation += blockOcclusion;

				rayOrigin = new Vec3d(rayHit.getHitVec().x + normalToPlayer.x * 0.1, rayHit.getHitVec().y + normalToPlayer.y * 0.1,
						rayHit.getHitVec().z + normalToPlayer.z * 0.1);
			}

			directCutoff = (float) Math.exp(-occlusionAccumulation * absorptionCoeff);

			float directGain = (float) Math.pow(directCutoff, 0.1f);

			// Calculate reverb parameters for this sound
			float sendGain1 = 0.0f;
			float sendGain2 = 0.0f;
			float sendGain3 = 0.0f;
			float sendGain4 = 0.0f;

			float sendCutoff1 = 1.0f;
			float sendCutoff2 = 1.0f;
			float sendCutoff3 = 1.0f;
			float sendCutoff4 = 1.0f;

			if (mc.player.isInWater()) {
				directCutoff *= 1.0f - Config.underwaterFilter.get();
			}

			if (isWeather) {
				setEnvironment(SoundSourceEvent, sendGain1, sendGain2, sendGain3, sendGain4, sendCutoff1, sendCutoff2, sendCutoff3,
						sendCutoff4, directCutoff, directGain, airAbsorptionFactor);
				return;
			}

			// Shoot rays around sound
			final float phi = 1.618033988f;
			final float gAngle = phi * (float) Math.PI * 2.0f;
			final float maxDistance = Config.maxRayDistance.get().floatValue();

			final int numRays = Config.environmentEvaluationRays.get();
			final int rayBounces = 4;

			final float[] bounceReflectivityRatio = new float[rayBounces];

			float sharedAirspace = 0.0f;

			final float rcpTotalRays = 1.0f / (numRays * rayBounces);
			final float rcpPrimaryRays = 1.0f / numRays;

			for (int i = 0; i < numRays; i++) {
				final float fiN = (float) i / numRays;
				final float longitude = gAngle * (float) i;
				final float latitude = (float) Math.asin(fiN * 2.0f - 1.0f);

				final Vec3d rayDir = new Vec3d(Math.cos(latitude) * Math.cos(longitude),
						Math.cos(latitude) * Math.sin(longitude), Math.sin(latitude));

				final Vec3d rayStart = new Vec3d(soundPos.x, soundPos.y, soundPos.z);

				final Vec3d rayEnd = new Vec3d(rayStart.x + rayDir.x * maxDistance, rayStart.y + rayDir.y * maxDistance,
						rayStart.z + rayDir.z * maxDistance);

				final RayTraceContext rayTraceContext = new RayTraceContext(rayStart, rayEnd, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.ANY, mc.player);
				final BlockRayTraceResult rayHit = mc.world.rayTraceBlocks(rayTraceContext);

				if (rayHit.getType() != RayTraceResult.Type.MISS) {
					final float rayLength = (float) soundPos.distanceTo(rayHit.getHitVec());

					// Additional bounces
					BlockPos lastHitBlock = rayHit.getPos();
					Vec3d lastHitPos = rayHit.getHitVec();
					Vec3d lastHitNormal = new Vec3d(rayHit.getFace().getDirectionVec());
					Vec3d lastRayDir = rayDir;

					float totalRayDistance = rayLength;

					// Secondary ray bounces
					for (int j = 0; j < rayBounces; j++) {
						final Vec3d newRayDir = Utils.reflect(lastRayDir, lastHitNormal);
						final Vec3d newRayStart = new Vec3d(lastHitPos.getX() + lastHitNormal.getX() * 0.01,
								lastHitPos.getY() + lastHitNormal.getY() * 0.01, lastHitPos.getZ() + lastHitNormal.getZ() * 0.01);
						final Vec3d newRayEnd = new Vec3d(newRayStart.x + newRayDir.x * maxDistance,
								newRayStart.y + newRayDir.y * maxDistance, newRayStart.z + newRayDir.z * maxDistance);

						final RayTraceContext rayTraceSecondaryContext = new RayTraceContext(newRayStart, newRayEnd, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.ANY, mc.player);
						final BlockRayTraceResult newRayHit = mc.world.rayTraceBlocks(rayTraceSecondaryContext);

						float energyTowardsPlayer = 0.25f;
						final float blockReflectivity = Utils.getBlockReflectivity(lastHitBlock);
						energyTowardsPlayer *= blockReflectivity * 0.75f + 0.25f;

						if (newRayHit.getType() == RayTraceResult.Type.MISS) {
							totalRayDistance += lastHitPos.distanceTo(playerPos);
						} else {
							final double newRayLength = lastHitPos.distanceTo(newRayHit.getHitVec());

							bounceReflectivityRatio[j] += blockReflectivity;

							totalRayDistance += newRayLength;

							lastHitPos = newRayHit.getHitVec();
							lastHitNormal = new Vec3d(newRayHit.getFace().getDirectionVec());
							lastRayDir = newRayDir;
							lastHitBlock = newRayHit.getPos();

							// Cast one final ray towards the player. If it's
							// unobstructed, then the sound source and the player
							// share airspace.
							if (Config.simplerSharedAirspaceSimulation.get() && j == rayBounces - 1
									|| !Config.simplerSharedAirspaceSimulation.get()) {
								final Vec3d finalRayStart = new Vec3d(lastHitPos.getX() + lastHitNormal.getX() * 0.01,
										lastHitPos.getY() + lastHitNormal.getY() * 0.01, lastHitPos.getZ() + lastHitNormal.getZ() * 0.01);

								final RayTraceContext rayTraceFinalContext = new RayTraceContext(finalRayStart, playerPos, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.ANY, mc.player);
								final RayTraceResult finalRayHit = mc.world.rayTraceBlocks(rayTraceFinalContext);

								if (finalRayHit.getType() == RayTraceResult.Type.MISS) {
									sharedAirspace += 1.0f;
								}
							}
						}

						final float reflectionDelay = Math.max(totalRayDistance, 0.0f) * 0.12f * blockReflectivity;

						final float cross1 = 1.0f - MathHelper.clamp(Math.abs(reflectionDelay - 0.0f), 0.0f, 1.0f);
						final float cross2 = 1.0f - MathHelper.clamp(Math.abs(reflectionDelay - 1.0f), 0.0f, 1.0f);
						final float cross3 = 1.0f - MathHelper.clamp(Math.abs(reflectionDelay - 2.0f), 0.0f, 1.0f);
						final float cross4 = MathHelper.clamp(reflectionDelay - 2.0f, 0.0f, 1.0f);

						sendGain1 += cross1 * energyTowardsPlayer * 6.4f * rcpTotalRays;
						sendGain2 += cross2 * energyTowardsPlayer * 12.8f * rcpTotalRays;
						sendGain3 += cross3 * energyTowardsPlayer * 12.8f * rcpTotalRays;
						sendGain4 += cross4 * energyTowardsPlayer * 12.8f * rcpTotalRays;

						// Nowhere to bounce off of, stop bouncing!
						if (newRayHit.getType() == RayTraceResult.Type.MISS) {
							break;
						}
					}
				}
			}

			// log("total reflectivity ratio: " + totalReflectivityRatio);

			bounceReflectivityRatio[0] = bounceReflectivityRatio[0] / numRays;
			bounceReflectivityRatio[1] = bounceReflectivityRatio[1] / numRays;
			bounceReflectivityRatio[2] = bounceReflectivityRatio[2] / numRays;
			bounceReflectivityRatio[3] = bounceReflectivityRatio[3] / numRays;

			sharedAirspace *= 64.0f;

			if (Config.simplerSharedAirspaceSimulation.get()) {
				sharedAirspace *= rcpPrimaryRays;
			} else {
				sharedAirspace *= rcpTotalRays;
			}

			final float sharedAirspaceWeight1 = MathHelper.clamp(sharedAirspace / 20.0f, 0.0f, 1.0f);
			final float sharedAirspaceWeight2 = MathHelper.clamp(sharedAirspace / 15.0f, 0.0f, 1.0f);
			final float sharedAirspaceWeight3 = MathHelper.clamp(sharedAirspace / 10.0f, 0.0f, 1.0f);
			final float sharedAirspaceWeight4 = MathHelper.clamp(sharedAirspace / 10.0f, 0.0f, 1.0f);

			sendCutoff1 = (float) Math.exp(-occlusionAccumulation * absorptionCoeff * 1.0f) * (1.0f - sharedAirspaceWeight1)
					+ sharedAirspaceWeight1;
			sendCutoff2 = (float) Math.exp(-occlusionAccumulation * absorptionCoeff * 1.0f) * (1.0f - sharedAirspaceWeight2)
					+ sharedAirspaceWeight2;
			sendCutoff3 = (float) Math.exp(-occlusionAccumulation * absorptionCoeff * 1.5f) * (1.0f - sharedAirspaceWeight3)
					+ sharedAirspaceWeight3;
			sendCutoff4 = (float) Math.exp(-occlusionAccumulation * absorptionCoeff * 1.5f) * (1.0f - sharedAirspaceWeight4)
					+ sharedAirspaceWeight4;

			// attempt to preserve directionality when airspace is shared by
			// allowing some of the dry signal through but filtered
			final float averageSharedAirspace = (sharedAirspaceWeight1 + sharedAirspaceWeight2 + sharedAirspaceWeight3
					+ sharedAirspaceWeight4) * 0.25f;
			directCutoff = Math.max((float) Math.pow(averageSharedAirspace, 0.5) * 0.2f, directCutoff);

			directGain = (float) Math.pow(directCutoff, 0.1);

			sendGain2 *= bounceReflectivityRatio[1];
			sendGain3 *= (float) Math.pow(bounceReflectivityRatio[2], 3.0);
			sendGain4 *= (float) Math.pow(bounceReflectivityRatio[3], 4.0);

			sendGain1 = MathHelper.clamp(sendGain1, 0.0f, 1.0f);
			sendGain2 = MathHelper.clamp(sendGain2, 0.0f, 1.0f);
			sendGain3 = MathHelper.clamp(sendGain3 * 1.05f - 0.05f, 0.0f, 1.0f);
			sendGain4 = MathHelper.clamp(sendGain4 * 1.05f - 0.05f, 0.0f, 1.0f);

			sendGain1 *= (float) Math.pow(sendCutoff1, 0.1);
			sendGain2 *= (float) Math.pow(sendCutoff2, 0.1);
			sendGain3 *= (float) Math.pow(sendCutoff3, 0.1);
			sendGain4 *= (float) Math.pow(sendCutoff4, 0.1);

			if (mc.player.isInWater()) {
				sendCutoff1 *= 0.4f;
				sendCutoff2 *= 0.4f;
				sendCutoff3 *= 0.4f;
				sendCutoff4 *= 0.4f;
			}

			setEnvironment(SoundSourceEvent, sendGain1, sendGain2, sendGain3, sendGain4, sendCutoff1, sendCutoff2, sendCutoff3,
					sendCutoff4, directCutoff, directGain, airAbsorptionFactor);
		} catch (Exception e) {
			SoundPhysics.logError("Error while evaluation environment:");
			e.printStackTrace();
			setEnvironment(SoundSourceEvent, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f);
		}
	}

	public static void setEnvironment(final PlaySoundSourceEvent source, final float sendGain1, final float sendGain2,
									  final float sendGain3, final float sendGain4, final float sendCutoff1, final float sendCutoff2,
									  final float sendCutoff3, final float sendCutoff4, final float directCutoff, final float directGain,
									  final float airAbsorptionFactor) {
		int alSource = source.getSource().id;
		// Set reverb send filter values and set source to send to all reverb fx
		// slots
		if (sendGain1 != 0) {
			EXTEfx.alFilterf(ASMTools.sendFilter1, EXTEfx.AL_LOWPASS_GAIN, sendGain1);
			EXTEfx.alFilterf(ASMTools.sendFilter1, EXTEfx.AL_LOWPASS_GAINHF, sendCutoff1);
			AL11.alSource3i(alSource, EXTEfx.AL_AUXILIARY_SEND_FILTER, ASMTools.auxFXSlot1, 0, ASMTools.sendFilter1);
			SoundPhysics.checkErrorLog("Error setting enviroment first filter: ");
		}

		if (sendGain2 != 0) {
			EXTEfx.alFilterf(ASMTools.sendFilter2, EXTEfx.AL_LOWPASS_GAIN, sendGain2);
			EXTEfx.alFilterf(ASMTools.sendFilter2, EXTEfx.AL_LOWPASS_GAINHF, sendCutoff2);
			AL11.alSource3i(alSource, EXTEfx.AL_AUXILIARY_SEND_FILTER, ASMTools.auxFXSlot2, 1, ASMTools.sendFilter2);
			SoundPhysics.checkErrorLog("Error setting enviroment second filter: ");
		}

		if (sendGain3 != 0) {
			EXTEfx.alFilterf(ASMTools.sendFilter3, EXTEfx.AL_LOWPASS_GAIN, sendGain3);
			EXTEfx.alFilterf(ASMTools.sendFilter3, EXTEfx.AL_LOWPASS_GAINHF, sendCutoff3);
			AL11.alSource3i(alSource, EXTEfx.AL_AUXILIARY_SEND_FILTER, ASMTools.auxFXSlot3, 2, ASMTools.sendFilter3);
			SoundPhysics.checkErrorLog("Error setting enviroment third filter: ");
		}

		if (sendGain4 != 0) {
			EXTEfx.alFilterf(ASMTools.sendFilter4, EXTEfx.AL_LOWPASS_GAIN, sendGain4);
			EXTEfx.alFilterf(ASMTools.sendFilter4, EXTEfx.AL_LOWPASS_GAINHF, sendCutoff4);
			AL11.alSource3i(alSource, EXTEfx.AL_AUXILIARY_SEND_FILTER, ASMTools.auxFXSlot4, 3, ASMTools.sendFilter4);
			SoundPhysics.checkErrorLog("Error setting enviroment fourth filter: ");
		}

		if (directGain != 0) {
			EXTEfx.alFilterf(ASMTools.directFilter1, EXTEfx.AL_LOWPASS_GAIN, directGain);
			EXTEfx.alFilterf(ASMTools.directFilter1, EXTEfx.AL_LOWPASS_GAINHF, directCutoff);
			AL10.alSourcei(alSource, EXTEfx.AL_DIRECT_FILTER, ASMTools.directFilter1);
			SoundPhysics.checkErrorLog("Error setting enviroment direct filter: ");
		}

		AL10.alSourcef(alSource, EXTEfx.AL_AIR_ABSORPTION_FACTOR, MathHelper.clamp(Config.airAbsorption.get().floatValue() * airAbsorptionFactor, 0.0f, 10.0f));
		SoundPhysics.checkErrorLog("Error setting enviroment air absortion: ");
	}

	private Enviroment() {
		throw new IllegalStateException("Utility class");
	}
}
