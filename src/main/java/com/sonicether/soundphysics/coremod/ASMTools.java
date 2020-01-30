package com.sonicether.soundphysics.coremod;

import com.sonicether.soundphysics.Config;
import com.sonicether.soundphysics.ReverbParams;
import com.sonicether.soundphysics.SoundPhysics;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.Heightmap;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.EXTEfx;

import java.util.regex.Pattern;

import static com.sonicether.soundphysics.coremod.ASMHooks.globalReverbMultiplier;
import static com.sonicether.soundphysics.coremod.ASMHooks.globalRolloffFactor;
import static com.sonicether.soundphysics.coremod.ASMHooks.soundDistanceAllowance;

public class ASMTools {
	protected static final Pattern rainPattern = Pattern.compile(".*rain.*");
	protected static final Pattern stepPattern = Pattern.compile(".*step.*");
	protected static final Pattern blockPattern = Pattern.compile(".*block.*");
	protected static final Pattern uiPattern = Pattern.compile(".*\\/ui\\/.*");
	protected static final Pattern clickPattern = Pattern.compile(".*random.click.*");
	protected static final Pattern noteBlockPattern = Pattern.compile(".*block.note.*");

	public static Minecraft mc;

	private static int auxFXSlot1;
	private static int auxFXSlot2;
	private static int auxFXSlot3;
	private static int auxFXSlot4;
	private static int reverb1;
	private static int reverb2;
	private static int reverb3;
	private static int reverb4;
	private static int directFilter1;
	private static int sendFilter1;
	private static int sendFilter2;
	private static int sendFilter3;
	private static int sendFilter4;

	public static void applyConfigChanges() {
		globalRolloffFactor = Config.rolloffFactor.get();
		globalReverbMultiplier = (float) (0.7f * Config.globalReverbGain.get());
		soundDistanceAllowance = Config.soundDistanceAllowance.get();

		if (auxFXSlot1 != 0) {
			// Set the global reverb parameters and apply them to the effect and
			// effectslot
			setReverbParams(ReverbParams.getReverb1(), auxFXSlot1, reverb1);
			setReverbParams(ReverbParams.getReverb2(), auxFXSlot2, reverb2);
			setReverbParams(ReverbParams.getReverb3(), auxFXSlot3, reverb3);
			setReverbParams(ReverbParams.getReverb4(), auxFXSlot4, reverb4);
		}
	}

	private static int generateEFXSlots(int slot, EFXtype type) {
		switch (type) {
			case AUX:
				slot = EXTEfx.alGenAuxiliaryEffectSlots();
				EXTEfx.alAuxiliaryEffectSloti(slot, EXTEfx.AL_EFFECTSLOT_AUXILIARY_SEND_AUTO, AL10.AL_TRUE);
				break;
			case EFFECT:
				slot = EXTEfx.alGenEffects();
				EXTEfx.alEffecti(slot, EXTEfx.AL_EFFECT_TYPE, EXTEfx.AL_EFFECT_EAXREVERB);
				break;
			case FILTER:
				slot = EXTEfx.alGenFilters();
				EXTEfx.alFilteri(slot, EXTEfx.AL_FILTER_TYPE, EXTEfx.AL_FILTER_LOWPASS);
				break;
		}
		SoundPhysics.log(type + " slot " + slot + " created");
		SoundPhysics.checkErrorLog("Failed creating " + type + " slots!");
		return slot;
	}

	public static void setupEFX() {
		// Get current context and device
		final long currentContext = ALC10.alcGetCurrentContext();
		SoundPhysics.log("Context: " + currentContext);
		final long currentDevice = ALC10.alcGetContextsDevice(currentContext);
		SoundPhysics.log("Device: " + currentDevice);

		if (ALC10.alcIsExtensionPresent(currentDevice, "ALC_EXT_EFX")) {
			SoundPhysics.log("EFX Extension recognized.");
		} else {
			SoundPhysics.logError("EFX Extension not found on current device. Aborting.");
			return;
		}

		// Create auxiliary effect slots
		auxFXSlot1 = generateEFXSlots(auxFXSlot1, EFXtype.AUX);
		auxFXSlot2 = generateEFXSlots(auxFXSlot2, EFXtype.AUX);
		auxFXSlot3 = generateEFXSlots(auxFXSlot3, EFXtype.AUX);
		auxFXSlot4 = generateEFXSlots(auxFXSlot4, EFXtype.AUX);

		// Create reverbs
		reverb1 = generateEFXSlots(reverb1, EFXtype.EFFECT);
		reverb2 = generateEFXSlots(reverb2, EFXtype.EFFECT);
		reverb3 = generateEFXSlots(reverb3, EFXtype.EFFECT);
		reverb4 = generateEFXSlots(reverb4, EFXtype.EFFECT);

		// Create filters
		sendFilter1 = generateEFXSlots(sendFilter1, EFXtype.FILTER);
		sendFilter2 = generateEFXSlots(sendFilter2, EFXtype.FILTER);
		sendFilter3 = generateEFXSlots(sendFilter3, EFXtype.FILTER);
		sendFilter4 = generateEFXSlots(sendFilter4, EFXtype.FILTER);
		directFilter1 = generateEFXSlots(directFilter1, EFXtype.FILTER);

		applyConfigChanges();
	}

	// Unused
	@SuppressWarnings("unused")
	private static boolean isSnowingAt(BlockPos position) {
		return isSnowingAt(position, true);
	}

	// Copy of isRainingAt
	public static boolean isSnowingAt(BlockPos position, boolean check_rain) {
		if (check_rain && !mc.world.isRaining()) {
			return false;
		} else if (!mc.world.isSkyVisible(position)) {
			return false;
		} else if (mc.world.getHeight(Heightmap.Type.MOTION_BLOCKING, position).getY() > position.getY()) {
			return false;
		} else {
			/*boolean cansnow = mc.world.canSnowAt(position, false);
			if (mc.world.getBiome(position).getEnableSnow() && cansnow) return true;
			else if (cansnow) return true;
			else return false;*/
			return mc.world.getBiome(position).getPrecipitation() == Biome.RainType.SNOW;
		}
	}

	@SuppressWarnings("deprecation")
	public static Double getBlockReflectivity(final BlockPos blockPos) {
		final BlockState blockState = mc.world.getBlockState(blockPos);
		final Block block = blockState.getBlock();
		final SoundType soundType = block.getSoundType(blockState);

		Double reflectivity = 0.5D;

		if (soundType == SoundType.STONE) {
			reflectivity = Config.stoneReflectivity.get();
		} else if (soundType == SoundType.WOOD) {
			reflectivity = Config.woodReflectivity.get();
		} else if (soundType == SoundType.GROUND) {
			reflectivity = Config.groundReflectivity.get();
		} else if (soundType == SoundType.PLANT) {
			reflectivity = Config.plantReflectivity.get();
		} else if (soundType == SoundType.METAL) {
			reflectivity = Config.metalReflectivity.get();
		} else if (soundType == SoundType.GLASS) {
			reflectivity = Config.glassReflectivity.get();
		} else if (soundType == SoundType.CLOTH) {
			reflectivity = Config.clothReflectivity.get();
		} else if (soundType == SoundType.SAND) {
			reflectivity = Config.sandReflectivity.get();
		} else if (soundType == SoundType.SNOW) {
			reflectivity = Config.snowReflectivity.get();
		} else if (soundType == SoundType.LADDER) {
			reflectivity = Config.woodReflectivity.get();
		} else if (soundType == SoundType.ANVIL) {
			reflectivity = Config.metalReflectivity.get();
		}

		reflectivity *= Config.globalBlockReflectance.get();

		return reflectivity;
	}

	static Vec3d getNormalFromFacing(final Direction sideHit) {
		return new Vec3d(sideHit.getDirectionVec());
	}

	public static Vec3d reflect(final Vec3d dir, final Vec3d normal) {
		final double dot2 = dir.dotProduct(normal) * 2;

		final double x = dir.x - dot2 * normal.x;
		final double y = dir.y - dot2 * normal.y;
		final double z = dir.z - dot2 * normal.z;

		return new Vec3d(x, y, z);
	}

	public static Vec3d offsetSoundByName(final double soundX, final double soundY, final double soundZ,
										   final Vec3d playerPos, final String name, final SoundCategory category) {
		double offsetX = 0.0;
		double offsetY = 0.0;
		double offsetZ = 0.0;
		double offsetTowardsPlayer = 0.0;

		double tempNormX = 0;
		double tempNormY = 0;
		double tempNormZ = 0;

		if (soundY % 1.0 < 0.001 || stepPattern.matcher(name).matches()) {
			offsetY = 0.225;
		}

		if (category == SoundCategory.BLOCKS || blockPattern.matcher(name).matches() ||
				(name.equals("openal") && !mc.world.isAirBlock(new BlockPos(soundX, soundY, soundZ)))) {
			// The ray will probably hit the block that it's emitting from
			// before
			// escaping. Offset the ray start position towards the player by the
			// diagonal half length of a cube

			tempNormX = playerPos.x - soundX;
			tempNormY = playerPos.y - soundY;
			tempNormZ = playerPos.z - soundZ;
			final double length = Math.sqrt(tempNormX * tempNormX + tempNormY * tempNormY + tempNormZ * tempNormZ);
			tempNormX /= length;
			tempNormY /= length;
			tempNormZ /= length;
			// 0.867 > square root of 0.5^2 * 3
			offsetTowardsPlayer = 0.867;
			offsetX += tempNormX * offsetTowardsPlayer;
			offsetY += tempNormY * offsetTowardsPlayer;
			offsetZ += tempNormZ * offsetTowardsPlayer;
		}

		return new Vec3d(soundX + offsetX, soundY + offsetY, soundZ + offsetZ);
	}

	@SuppressWarnings("deprecation")
	public static void evaluateEnvironment(final int sourceID, final float posX, final float posY, final float posZ, final SoundCategory category, final String name) {
		try {
			if (mc.player == null || mc.world == null || posY <= 0 || category == SoundCategory.RECORDS
					|| category == SoundCategory.MUSIC) {
				// posY <= 0 as a condition has to be there: Ingame
				// menu clicks do have a player and world present
				setEnvironment(sourceID, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f);
				return;
			}

			final boolean isRain = rainPattern.matcher(name).matches();

			if (Config.skipRainOcclusionTracing.get() && isRain) {
				setEnvironment(sourceID, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f);
				return;
			}

			float directCutoff = 1.0f;
			final float absorptionCoeff = (float) (Config.globalBlockAbsorption.get() * 3.0f);

			final BlockPos mcplayerpos = mc.player.getPosition();
			final Vec3d playerPos = new Vec3d(mcplayerpos.getX(), mcplayerpos.getY() + mc.player.getEyeHeight(), mcplayerpos.getZ());
			final Vec3d soundPos = offsetSoundByName(posX, posY, posZ, playerPos, name, category);
			final Vec3d normalToPlayer = playerPos.subtract(soundPos).normalize();

			float airAbsorptionFactor = 1.0f;

			if (Config.snowAirAbsorptionFactor.get() > 1.0f && mc.world.isRaining()) {
				final Vec3d middlePos = playerPos.add(soundPos).scale(0.5);
				final BlockPos playerPosBlock = new BlockPos(playerPos);
				final BlockPos soundPosBlock = new BlockPos(soundPos);
				final BlockPos middlePosBlock = new BlockPos(middlePos);
				final int snowingPlayer = isSnowingAt(playerPosBlock, false) ? 1 : 0;
				final int snowingSound = isSnowingAt(soundPosBlock, false) ? 1 : 0;
				final int snowingMiddle = isSnowingAt(middlePosBlock, false) ? 1 : 0;
				final float snowFactor = snowingPlayer * 0.25f + snowingMiddle * 0.5f + snowingSound * 0.25f;
				airAbsorptionFactor = (float) Math.max(Config.snowAirAbsorptionFactor.get() * mc.world.getRainStrength(1.0f) * snowFactor, airAbsorptionFactor);
			}

			/*final double distance = playerPos.distanceTo(soundPos);
			final double time = (distance/343.3)*1000;
			AL10.alSourcePause(sourceID);
			log("paused, time "+String.valueOf(time));

			new java.util.Timer().schedule(
				new java.util.TimerTask() {
					@Override
					public void run() {
						log("play, time "+String.valueOf(time));
						AL10.alSourcePlay(sourceID);
					}
				},
				(long)time
			);*/

			Vec3d rayOrigin = soundPos;

			float occlusionAccumulation = 0.0f;

			for (int i = 0; i < 10; i++) {
				final RayTraceContext rayTraceContext = new RayTraceContext(rayOrigin, playerPos, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, mc.player);
				final BlockRayTraceResult rayHit = mc.world.rayTraceBlocks(rayTraceContext);

				if (rayHit == null) {
					break;
				}

				final Block blockHit = mc.world.getBlockState(rayHit.getPos()).getBlock();

				float blockOcclusion = 1.0f;

				if (!blockHit.isOpaqueCube(blockHit.getDefaultState(), mc.world.getWorld(), rayHit.getPos())) {
					// log("not a solid block!");
					blockOcclusion *= 0.15f;
				}

				occlusionAccumulation += blockOcclusion;

				rayOrigin = new Vec3d(rayHit.getHitVec().x + normalToPlayer.x * 0.1, rayHit.getHitVec().y + normalToPlayer.y * 0.1,
						rayHit.getHitVec().z + normalToPlayer.z * 0.1);
			}

			directCutoff = (float) Math.exp(-occlusionAccumulation * absorptionCoeff);
			float directGain = (float) Math.pow(directCutoff, 0.1);

			// Calculate reverb parameters for this sound
			float sendGain0 = 0.0f;
			float sendGain1 = 0.0f;
			float sendGain2 = 0.0f;
			float sendGain3 = 0.0f;

			float sendCutoff0 = 1.0f;
			float sendCutoff1 = 1.0f;
			float sendCutoff2 = 1.0f;
			float sendCutoff3 = 1.0f;

			if (mc.player.isInWater()) {
				directCutoff *= 1.0f - Config.underwaterFilter.get();
			}

			if (isRain) {
				setEnvironment(sourceID, sendGain0, sendGain1, sendGain2, sendGain3, sendCutoff0, sendCutoff1, sendCutoff2,
						sendCutoff3, directCutoff, directGain, airAbsorptionFactor);
				return;
			}

			// Shoot rays around sound
			final float phi = 1.618033988f;
			final float gAngle = phi * (float) Math.PI * 2.0f;
			final float maxDistance = (float) (Config.maxDistance.get() * 1.0f);

			final int numRays = Config.environmentEvaluationRays.get();
			final int rayBounces = 4;

			final float[] bounceReflectivityRatio = new float[rayBounces];

			float sharedAirspace = 0.0f;

			final float rcpTotalRays = 1.0f / (numRays * rayBounces);
			final float rcpPrimaryRays = 1.0f / numRays;

			for (int i = 0; i < numRays; i++) {
				final float fi = i;
				final float fiN = fi / numRays;
				final float longitude = gAngle * fi;
				final float latitude = (float) Math.asin(fiN * 2.0f - 1.0f);

				final Vec3d rayDir = new Vec3d(Math.cos(latitude) * Math.cos(longitude),
						Math.cos(latitude) * Math.sin(longitude), Math.sin(latitude));

				final Vec3d rayStart = new Vec3d(soundPos.x, soundPos.y, soundPos.z);

				final Vec3d rayEnd = new Vec3d(rayStart.x + rayDir.x * maxDistance, rayStart.y + rayDir.y * maxDistance,
						rayStart.z + rayDir.z * maxDistance);

				final RayTraceContext rayTraceContext = new RayTraceContext(rayStart, rayEnd, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, null);

				final BlockRayTraceResult rayHit = mc.world.rayTraceBlocks(rayTraceContext);

				if (rayHit != null) {
					final double rayLength = soundPos.distanceTo(rayHit.getHitVec());

					// Additional bounces
					BlockPos lastHitBlock = rayHit.getPos();
					Vec3d lastHitPos = rayHit.getHitVec();
					Vec3d lastHitNormal = getNormalFromFacing(rayHit.getFace());
					Vec3d lastRayDir = rayDir;

					float totalRayDistance = (float) rayLength;

					// Secondary ray bounces
					for (int j = 0; j < rayBounces; j++) {
						final Vec3d newRayDir = reflect(lastRayDir, lastHitNormal);
						// Vec3d newRayDir = lastHitNormal;
						final Vec3d newRayStart = new Vec3d(lastHitPos.x + lastHitNormal.x * 0.01,
								lastHitPos.y + lastHitNormal.y * 0.01, lastHitPos.z + lastHitNormal.z * 0.01);
						final Vec3d newRayEnd = new Vec3d(newRayStart.x + newRayDir.x * maxDistance,
								newRayStart.y + newRayDir.y * maxDistance, newRayStart.z + newRayDir.z * maxDistance);

						final RayTraceContext rayTraceSecondaryContext = new RayTraceContext(newRayStart, newRayEnd, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, null);

						final BlockRayTraceResult newRayHit = mc.world.rayTraceBlocks(rayTraceSecondaryContext);

						float energyTowardsPlayer = 0.25f;
						final Double blockReflectivity = getBlockReflectivity(lastHitBlock);
						energyTowardsPlayer *= blockReflectivity * 0.75f + 0.25f;

						if (newRayHit == null) {
							totalRayDistance += lastHitPos.distanceTo(playerPos);
						} else {
							final double newRayLength = lastHitPos.distanceTo(newRayHit.getHitVec());

							bounceReflectivityRatio[j] += blockReflectivity;

							totalRayDistance += newRayLength;

							lastHitPos = newRayHit.getHitVec();
							lastHitNormal = getNormalFromFacing(newRayHit.getFace());
							lastRayDir = newRayDir;
							lastHitBlock = newRayHit.getPos();

							// Cast one final ray towards the player. If it's
							// unobstructed, then the sound source and the player
							// share airspace.
							if (Config.simplerSharedAirspaceSimulation.get() && j == rayBounces - 1
									|| !Config.simplerSharedAirspaceSimulation.get()) {
								final Vec3d finalRayStart = new Vec3d(lastHitPos.x + lastHitNormal.x * 0.01,
										lastHitPos.y + lastHitNormal.y * 0.01, lastHitPos.z + lastHitNormal.z * 0.01);

								final RayTraceContext rayTraceFinalContext = new RayTraceContext(finalRayStart, playerPos, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, null);

								final RayTraceResult finalRayHit = mc.world.rayTraceBlocks(rayTraceFinalContext);

								if (finalRayHit == null) {
									// log("Secondary ray hit the player!");
									sharedAirspace += 1.0f;
								}
							}
						}

						final double reflectionDelay = (Math.max(totalRayDistance, 0.0) * 0.12f * blockReflectivity);

						final double cross0 = 1.0f - MathHelper.clamp(Math.abs(reflectionDelay - 0.0f), 0.0f, 1.0f);
						final double cross1 = 1.0f - MathHelper.clamp(Math.abs(reflectionDelay - 1.0f), 0.0f, 1.0f);
						final double cross2 = 1.0f - MathHelper.clamp(Math.abs(reflectionDelay - 2.0f), 0.0f, 1.0f);
						final double cross3 = MathHelper.clamp(reflectionDelay - 2.0f, 0.0f, 1.0f);

						sendGain0 += cross0 * energyTowardsPlayer * 6.4f * rcpTotalRays;
						sendGain1 += cross1 * energyTowardsPlayer * 12.8f * rcpTotalRays;
						sendGain2 += cross2 * energyTowardsPlayer * 12.8f * rcpTotalRays;
						sendGain3 += cross3 * energyTowardsPlayer * 12.8f * rcpTotalRays;

						// Nowhere to bounce off of, stop bouncing!
						if (newRayHit == null) {
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

			final float sharedAirspaceWeight0 = MathHelper.clamp(sharedAirspace / 20.0f, 0.0f, 1.0f);
			final float sharedAirspaceWeight1 = MathHelper.clamp(sharedAirspace / 15.0f, 0.0f, 1.0f);
			final float sharedAirspaceWeight2 = MathHelper.clamp(sharedAirspace / 10.0f, 0.0f, 1.0f);
			final float sharedAirspaceWeight3 = MathHelper.clamp(sharedAirspace / 10.0f, 0.0f, 1.0f);

			sendCutoff0 = (float) Math.exp(-occlusionAccumulation * absorptionCoeff * 1.0f) * (1.0f - sharedAirspaceWeight0)
					+ sharedAirspaceWeight0;
			sendCutoff1 = (float) Math.exp(-occlusionAccumulation * absorptionCoeff * 1.0f) * (1.0f - sharedAirspaceWeight1)
					+ sharedAirspaceWeight1;
			sendCutoff2 = (float) Math.exp(-occlusionAccumulation * absorptionCoeff * 1.5f) * (1.0f - sharedAirspaceWeight2)
					+ sharedAirspaceWeight2;
			sendCutoff3 = (float) Math.exp(-occlusionAccumulation * absorptionCoeff * 1.5f) * (1.0f - sharedAirspaceWeight3)
					+ sharedAirspaceWeight3;

			// attempt to preserve directionality when airspace is shared by
			// allowing some of the dry signal through but filtered
			final float averageSharedAirspace = (sharedAirspaceWeight0 + sharedAirspaceWeight1 + sharedAirspaceWeight2
					+ sharedAirspaceWeight3) * 0.25f;
			directCutoff = Math.max((float) Math.pow(averageSharedAirspace, 0.5) * 0.2f, directCutoff);

			directGain = (float) Math.pow(directCutoff, 0.1);

			sendGain1 *= bounceReflectivityRatio[1];
			sendGain2 *= (float) Math.pow(bounceReflectivityRatio[2], 3.0);
			sendGain3 *= (float) Math.pow(bounceReflectivityRatio[3], 4.0);

			sendGain0 = MathHelper.clamp(sendGain0, 0.0f, 1.0f);
			sendGain1 = MathHelper.clamp(sendGain1, 0.0f, 1.0f);
			sendGain2 = MathHelper.clamp(sendGain2 * 1.05f - 0.05f, 0.0f, 1.0f);
			sendGain3 = MathHelper.clamp(sendGain3 * 1.05f - 0.05f, 0.0f, 1.0f);

			sendGain0 *= (float) Math.pow(sendCutoff0, 0.1);
			sendGain1 *= (float) Math.pow(sendCutoff1, 0.1);
			sendGain2 *= (float) Math.pow(sendCutoff2, 0.1);
			sendGain3 *= (float) Math.pow(sendCutoff3, 0.1);

			if (mc.player.isInWater()) {
				sendCutoff0 *= 0.4f;
				sendCutoff1 *= 0.4f;
				sendCutoff2 *= 0.4f;
				sendCutoff3 *= 0.4f;
			}

			setEnvironment(sourceID, sendGain0, sendGain1, sendGain2, sendGain3, sendCutoff0, sendCutoff1, sendCutoff2,
					sendCutoff3, directCutoff, directGain, airAbsorptionFactor);
		} catch (Exception e) {
			SoundPhysics.logError("Error while evaluation environment:");
			e.printStackTrace();
			setEnvironment(sourceID, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f);
		}
	}

	private static void setEnvironment(final int sourceID, final float sendGain1, final float sendGain2,
									   final float sendGain3, final float sendGain4, final float sendCutoff1, final float sendCutoff2,
									   final float sendCutoff3, final float sendCutoff4, final float directCutoff, final float directGain,
									   final float airAbsorptionFactor) {
		// Set reverb send filter values and set source to send to all reverb fx
		// slots
		EXTEfx.alFilterf(sendFilter1, EXTEfx.AL_LOWPASS_GAIN, sendGain1);
		EXTEfx.alFilterf(sendFilter1, EXTEfx.AL_LOWPASS_GAINHF, sendCutoff1);
		AL11.alSource3i(sourceID, EXTEfx.AL_AUXILIARY_SEND_FILTER, auxFXSlot1, 1, sendFilter1);

		EXTEfx.alFilterf(sendFilter2, EXTEfx.AL_LOWPASS_GAIN, sendGain2);
		EXTEfx.alFilterf(sendFilter2, EXTEfx.AL_LOWPASS_GAINHF, sendCutoff2);
		AL11.alSource3i(sourceID, EXTEfx.AL_AUXILIARY_SEND_FILTER, auxFXSlot2, 2, sendFilter2);

		EXTEfx.alFilterf(sendFilter3, EXTEfx.AL_LOWPASS_GAIN, sendGain3);
		EXTEfx.alFilterf(sendFilter3, EXTEfx.AL_LOWPASS_GAINHF, sendCutoff3);
		AL11.alSource3i(sourceID, EXTEfx.AL_AUXILIARY_SEND_FILTER, auxFXSlot3, 3, sendFilter3);

		EXTEfx.alFilterf(sendFilter4, EXTEfx.AL_LOWPASS_GAIN, sendGain4);
		EXTEfx.alFilterf(sendFilter4, EXTEfx.AL_LOWPASS_GAINHF, sendCutoff4);
		AL11.alSource3i(sourceID, EXTEfx.AL_AUXILIARY_SEND_FILTER, auxFXSlot4, 4, sendFilter4);

		EXTEfx.alFilterf(directFilter1, EXTEfx.AL_LOWPASS_GAIN, directGain);
		EXTEfx.alFilterf(directFilter1, EXTEfx.AL_LOWPASS_GAINHF, directCutoff);
		AL10.alSourcei(sourceID, EXTEfx.AL_DIRECT_FILTER, directFilter1);

		AL10.alSourcef(sourceID, EXTEfx.AL_AIR_ABSORPTION_FACTOR, (float) MathHelper.clamp(Config.airAbsorption.get() * airAbsorptionFactor, 0.0f, 10.0f));
	}

	/**
	 * Applies the parameters in the enum ReverbParams to the main reverb
	 * effect.
	 */
	protected static void setReverbParams(final ReverbParams r, final int auxFXSlot, final int reverbSlot) {
		EXTEfx.alEffectf(reverbSlot, EXTEfx.AL_EAXREVERB_DENSITY, r.density);
		SoundPhysics.checkErrorLog("Error while assigning reverb density: " + r.density);

		EXTEfx.alEffectf(reverbSlot, EXTEfx.AL_EAXREVERB_DIFFUSION, r.diffusion);
		SoundPhysics.checkErrorLog("Error while assigning reverb diffusion: " + r.diffusion);

		EXTEfx.alEffectf(reverbSlot, EXTEfx.AL_EAXREVERB_GAIN, r.gain);
		SoundPhysics.checkErrorLog("Error while assigning reverb gain: " + r.gain);

		EXTEfx.alEffectf(reverbSlot, EXTEfx.AL_EAXREVERB_GAINHF, r.gainHF);
		SoundPhysics.checkErrorLog("Error while assigning reverb gainHF: " + r.gainHF);

		EXTEfx.alEffectf(reverbSlot, EXTEfx.AL_EAXREVERB_DECAY_TIME, r.decayTime);
		SoundPhysics.checkErrorLog("Error while assigning reverb decayTime: " + r.decayTime);

		EXTEfx.alEffectf(reverbSlot, EXTEfx.AL_EAXREVERB_DECAY_HFRATIO, r.decayHFRatio);
		SoundPhysics.checkErrorLog("Error while assigning reverb decayHFRatio: " + r.decayHFRatio);

		EXTEfx.alEffectf(reverbSlot, EXTEfx.AL_EAXREVERB_REFLECTIONS_GAIN, r.reflectionsGain);
		SoundPhysics.checkErrorLog("Error while assigning reverb reflectionsGain: " + r.reflectionsGain);

		EXTEfx.alEffectf(reverbSlot, EXTEfx.AL_EAXREVERB_LATE_REVERB_GAIN, r.lateReverbGain);
		SoundPhysics.checkErrorLog("Error while assigning reverb lateReverbGain: " + r.lateReverbGain);

		EXTEfx.alEffectf(reverbSlot, EXTEfx.AL_EAXREVERB_LATE_REVERB_DELAY, r.lateReverbDelay);
		SoundPhysics.checkErrorLog("Error while assigning reverb lateReverbDelay: " + r.lateReverbDelay);

		EXTEfx.alEffectf(reverbSlot, EXTEfx.AL_EAXREVERB_AIR_ABSORPTION_GAINHF, r.airAbsorptionGainHF);
		SoundPhysics.checkErrorLog("Error while assigning reverb airAbsorptionGainHF: " + r.airAbsorptionGainHF);

		EXTEfx.alEffectf(reverbSlot, EXTEfx.AL_EAXREVERB_ROOM_ROLLOFF_FACTOR, r.roomRolloffFactor);
		SoundPhysics.checkErrorLog("Error while assigning reverb roomRolloffFactor: " + r.roomRolloffFactor);

		// Attach updated effect object
		EXTEfx.alAuxiliaryEffectSloti(auxFXSlot, EXTEfx.AL_EFFECTSLOT_EFFECT, reverbSlot);
	}

	private enum EFXtype {AUX, EFFECT, FILTER}
}
