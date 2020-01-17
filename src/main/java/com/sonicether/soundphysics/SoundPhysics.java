package com.sonicether.soundphysics;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.*;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.Heightmap;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.EXTEfx;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Pattern;

@Mod(SoundPhysics.modid)
public class SoundPhysics {

	public static final String modid = "soundphysics";
	public static final String version = "1.0.8-1";
	public static final String mcVersion = "1.12.2";
	public static final String deps = "";

	public static final Logger logger = LogManager.getLogger(modid);

	public static boolean onServer = false;

	protected static final Pattern rainPattern = Pattern.compile(".*rain.*");
	protected static final Pattern stepPattern = Pattern.compile(".*step.*");
	private static final Pattern blockPattern = Pattern.compile(".*block.*");
	protected static final Pattern uiPattern = Pattern.compile(".*\\/ui\\/.*");
	protected static final Pattern clickPattern = Pattern.compile(".*random.click.*");
	protected static final Pattern noteBlockPattern = Pattern.compile(".*block.note.*");

	public SoundPhysics() {
		// Register the setup method for modloading
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
		// Register the enqueueIMC method for modloading
		//FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
		// Register the processIMC method for modloading
		//FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);
		// Register the doClientStuff method for modloading
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);

		// Register ourselves for server and other game events we are interested in
		MinecraftForge.EVENT_BUS.register(this);
	}

	public void setup(final FMLCommonSetupEvent event) {
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.COMMON_CONFIG);
		Config.loadConfig(Config.COMMON_CONFIG, FMLPaths.CONFIGDIR.get().resolve("soundphysics-common.toml"));
	}

	public void doClientStuff(final FMLClientSetupEvent event) {
		ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.CLIENT_CONFIG);
		Config.loadConfig(Config.CLIENT_CONFIG, FMLPaths.CONFIGDIR.get().resolve("soundphysics-client.toml"));
	}

	public void doServerStuff(final FMLClientSetupEvent event) {
		onServer = true;
	}

	private static int auxFXSlot0;
	private static int auxFXSlot1;
	private static int auxFXSlot2;
	private static int auxFXSlot3;
	private static int reverb0;
	private static int reverb1;
	private static int reverb2;
	private static int reverb3;
	private static int directFilter0;
	private static int sendFilter0;
	private static int sendFilter1;
	private static int sendFilter2;
	private static int sendFilter3;

	private static ProcThread proc_thread;
	private static volatile boolean thread_alive;
	@SuppressWarnings("unused")
	private static volatile boolean thread_signal_death;
	private static volatile List<Source> source_list;

	public static class Source {
		public int sourceID;
		public float posX;
		public float posY;
		public float posZ;
		public SoundCategory category;
		public String name;
		public int frequency;
		public int size;
		public int bufferID;

		public Source(int sid, float px, float py, float pz, SoundCategory cat, String n) {
			this.sourceID = sid;
			this.posX = px;
			this.posY = py;
			this.posZ = pz;
			this.category = cat;
			this.name = n;
			bufferID = AL10.alGetSourcei(sid, AL10.AL_BUFFER);
			size = AL10.alGetBufferi(bufferID, AL10.AL_SIZE);
			frequency = AL10.alGetBufferi(bufferID, AL10.AL_FREQUENCY);
		}
	}

	public static class ProcThread extends Thread {
		@Override
		public synchronized void run() {
			while (thread_alive) {
				while (!Config.dynamicEnvironementEvalutaion.get()) {
					try {
						Thread.sleep(1000);
					} catch (Exception e) {
						logError(String.valueOf(e));
					}
				}
				synchronized (source_list) {
					//log("Updating env " + String.valueOf(source_list.size()));
					ListIterator<Source> iter = source_list.listIterator();
					while (iter.hasNext()) {
						Source source = iter.next();
						//log("Updating sound '" + source.name + "' SourceID:" + String.valueOf(source.sourceID));
						//boolean pl = sndHandler.isSoundPlaying(source.sound);
						//FloatBuffer pos = BufferUtils.createFloatBuffer(3);
						//AL10.alGetSource(source.sourceID,AL10.AL_POSITION,pos);
						//To try ^
						int state = AL10.alGetSourcei(source.sourceID, AL10.AL_SOURCE_STATE);
						//int byteoff = AL10.alGetSourcei(source.sourceID, AL11.AL_BYTE_OFFSET);
						//boolean finished = source.size == byteoff;
						if (state == AL10.AL_PLAYING) {
							FloatBuffer pos = BufferUtils.createFloatBuffer(3);
							AL10.alGetSourcef(source.sourceID, AL10.AL_POSITION, pos);
							source.posX = pos.get(0);
							source.posY = pos.get(1);
							source.posZ = pos.get(2);
							evaluateEnvironment(source.sourceID, source.posX, source.posY, source.posZ, source.category, source.name);
						} else /*if (state == AL10.AL_STOPPED)*/ {
							iter.remove();
						}
					}
				}
				try {
					Thread.sleep(1000 / Config.dynamicEnvironementEvalutaionFrequency.get());
				} catch (Exception e) {
					logError(String.valueOf(e));
				}
			}
			thread_signal_death = true;
		}
	}

	public static void source_check_add(Source s) {
		synchronized (source_list) {
			ListIterator<Source> iter = source_list.listIterator();
			while (iter.hasNext()) {
				Source sn = iter.next();
				if (sn.sourceID == s.sourceID) {
					sn.posX = s.posX;
					sn.posY = s.posY;
					sn.posZ = s.posZ;
					sn.category = s.category;
					sn.name = s.name;
					return;
				}
			}
			source_list.add(s);
		}
	}

	@Mod.EventBusSubscriber
	public static class DebugDisplayEventHandler {
		@SubscribeEvent
		public static void onDebugOverlay(RenderGameOverlayEvent.Text event) {
			if (AsmHooks.mc != null && AsmHooks.mc.gameSettings.showDebugInfo && Config.dynamicEnvironementEvalutaion.get() && Config.debugInfoShow.get()) {
				event.getLeft().add("");
				event.getLeft().add("[SoundPhysics] " + String.valueOf(source_list.size()) + " Sources");
				event.getLeft().add("[SoundPhysics] Source list :");
				synchronized (source_list) {
					ListIterator<Source> iter = source_list.listIterator();
					while (iter.hasNext()) {
						Source s = iter.next();
						Vec3d tmp = new Vec3d(s.posX, s.posY, s.posZ);
						event.getLeft().add(String.valueOf(s.sourceID) + "-" + s.category.toString() + "-" + s.name + "-" + tmp.toString());
						/*int buffq = AL10.alGetSourcei(s.sourceID, AL10.AL_BUFFERS_QUEUED);
						int buffp = AL10.alGetSourcei(s.sourceID, AL10.AL_BUFFERS_PROCESSED);
						int sampoff = AL10.alGetSourcei(s.sourceID, AL11.AL_SAMPLE_OFFSET);
						int byteoff = AL10.alGetSourcei(s.sourceID, AL11.AL_BYTE_OFFSET);
						String k = "";
						if (sampoff!=0) {
							//k = String.valueOf(sampoff)+"/"+String.valueOf((byteoff/sampoff)*size)+" ";
							k = String.valueOf((float)sampoff/(float)s.frequency)+"/"+String.valueOf((float)((byteoff/sampoff)*s.size)/(float)s.frequency)+" ";
						} else {
							k = "0/? ";
						}
						event.getLeft().add(k+String.valueOf(buffp)+"/"+String.valueOf(buffq)+" "+String.valueOf(s.bufferID));
						event.getLeft().add("----");*/
					}
				}
			}
		}
	}

	public static synchronized void setupThread() {
		if (source_list == null) source_list = Collections.synchronizedList(new ArrayList<Source>());
		else source_list.clear();

		/*if (proc_thread != null) {
			thread_signal_death = false;
			thread_alive = false;
			while (!thread_signal_death);
		}*/
		if (proc_thread == null) {
			proc_thread = new ProcThread();
			thread_alive = true;
			proc_thread.start();
		}
	}

	public static void applyConfigChanges() {
		AsmHooks.globalRolloffFactor = Config.rolloffFactor.get();
		AsmHooks.globalReverbMultiplier = (float) (0.7f * Config.globalReverbGain.get());
		AsmHooks.soundDistanceAllowance = Config.soundDistanceAllowance.get();

		if (auxFXSlot0 != 0) {
			// Set the global reverb parameters and apply them to the effect and
			// effectslot
			setReverbParams(ReverbParams.getReverb0(), auxFXSlot0, reverb0);
			setReverbParams(ReverbParams.getReverb1(), auxFXSlot1, reverb1);
			setReverbParams(ReverbParams.getReverb2(), auxFXSlot2, reverb2);
			setReverbParams(ReverbParams.getReverb3(), auxFXSlot3, reverb3);
		}
	}

	public static void setupEFX() {
		// Get current context and device
		final long currentContext = ALC10.alcGetCurrentContext();
		log("Context: " + currentContext);
		final long currentDevice = ALC10.alcGetContextsDevice(currentContext);
		log("Device: " + currentDevice);

		if (ALC10.alcIsExtensionPresent(currentDevice, "ALC_EXT_EFX")) {
			log("EFX Extension recognized.");
		} else {
			logError("EFX Extension not found on current device. Aborting.");
			return;
		}

		// Create auxiliary effect slots
		auxFXSlot0 = EXTEfx.alGenAuxiliaryEffectSlots();
		log("Aux slot " + auxFXSlot0 + " created");
		EXTEfx.alAuxiliaryEffectSloti(auxFXSlot0, EXTEfx.AL_EFFECTSLOT_AUXILIARY_SEND_AUTO, AL10.AL_TRUE);

		auxFXSlot1 = EXTEfx.alGenAuxiliaryEffectSlots();
		log("Aux slot " + auxFXSlot1 + " created");
		EXTEfx.alAuxiliaryEffectSloti(auxFXSlot1, EXTEfx.AL_EFFECTSLOT_AUXILIARY_SEND_AUTO, AL10.AL_TRUE);

		auxFXSlot2 = EXTEfx.alGenAuxiliaryEffectSlots();
		log("Aux slot " + auxFXSlot2 + " created");
		EXTEfx.alAuxiliaryEffectSloti(auxFXSlot2, EXTEfx.AL_EFFECTSLOT_AUXILIARY_SEND_AUTO, AL10.AL_TRUE);

		auxFXSlot3 = EXTEfx.alGenAuxiliaryEffectSlots();
		log("Aux slot " + auxFXSlot3 + " created");
		EXTEfx.alAuxiliaryEffectSloti(auxFXSlot3, EXTEfx.AL_EFFECTSLOT_AUXILIARY_SEND_AUTO, AL10.AL_TRUE);
		checkErrorLog("Failed creating auxiliary effect slots!");

		reverb0 = EXTEfx.alGenEffects();
		EXTEfx.alEffecti(reverb0, EXTEfx.AL_EFFECT_TYPE, EXTEfx.AL_EFFECT_EAXREVERB);
		checkErrorLog("Failed creating reverb effect slot 0!");
		reverb1 = EXTEfx.alGenEffects();
		EXTEfx.alEffecti(reverb1, EXTEfx.AL_EFFECT_TYPE, EXTEfx.AL_EFFECT_EAXREVERB);
		checkErrorLog("Failed creating reverb effect slot 1!");
		reverb2 = EXTEfx.alGenEffects();
		EXTEfx.alEffecti(reverb2, EXTEfx.AL_EFFECT_TYPE, EXTEfx.AL_EFFECT_EAXREVERB);
		checkErrorLog("Failed creating reverb effect slot 2!");
		reverb3 = EXTEfx.alGenEffects();
		EXTEfx.alEffecti(reverb3, EXTEfx.AL_EFFECT_TYPE, EXTEfx.AL_EFFECT_EAXREVERB);
		checkErrorLog("Failed creating reverb effect slot 3!");

		// Create filters
		directFilter0 = EXTEfx.alGenFilters();
		EXTEfx.alFilteri(directFilter0, EXTEfx.AL_FILTER_TYPE, EXTEfx.AL_FILTER_LOWPASS);

		sendFilter0 = EXTEfx.alGenFilters();
		EXTEfx.alFilteri(sendFilter0, EXTEfx.AL_FILTER_TYPE, EXTEfx.AL_FILTER_LOWPASS);

		sendFilter1 = EXTEfx.alGenFilters();
		EXTEfx.alFilteri(sendFilter1, EXTEfx.AL_FILTER_TYPE, EXTEfx.AL_FILTER_LOWPASS);

		sendFilter2 = EXTEfx.alGenFilters();
		EXTEfx.alFilteri(sendFilter2, EXTEfx.AL_FILTER_TYPE, EXTEfx.AL_FILTER_LOWPASS);

		sendFilter3 = EXTEfx.alGenFilters();
		EXTEfx.alFilteri(sendFilter3, EXTEfx.AL_FILTER_TYPE, EXTEfx.AL_FILTER_LOWPASS);
		checkErrorLog("Error creating lowpass filters!");

		applyConfigChanges();
	}

	// Unused
	@SuppressWarnings("unused")
	private static boolean isSnowingAt(BlockPos position) {
		return isSnowingAt(position, true);
	}

	// Copy of isRainingAt
	private static boolean isSnowingAt(BlockPos position, boolean check_rain) {
		if (check_rain && !AsmHooks.mc.world.isRaining()) {
			return false;
		} else if (!AsmHooks.mc.world.isSkyVisible(position)) {
			return false;
		} else if (AsmHooks.mc.world.getHeight(Heightmap.Type.MOTION_BLOCKING, position).getY() > position.getY()) {
			return false;
		} else {
			/*boolean cansnow = mc.world.canSnowAt(position, false);
			if (mc.world.getBiome(position).getEnableSnow() && cansnow) return true;
			else if (cansnow) return true;
			else return false;*/
			return AsmHooks.mc.world.getBiome(position).getPrecipitation() == Biome.RainType.SNOW;
		}
	}

	@SuppressWarnings("deprecation")
	private static Double getBlockReflectivity(final BlockPos blockPos) {
		final BlockState blockState = AsmHooks.mc.world.getBlockState(blockPos);
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

	private static Vec3d reflect(final Vec3d dir, final Vec3d normal) {
		final double dot2 = dir.dotProduct(normal) * 2;

		final double x = dir.x - dot2 * normal.x;
		final double y = dir.y - dot2 * normal.y;
		final double z = dir.z - dot2 * normal.z;

		return new Vec3d(x, y, z);
	}

	private static Vec3d offsetSoundByName(final double soundX, final double soundY, final double soundZ,
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
				(name == "openal" && !AsmHooks.mc.world.isAirBlock(new BlockPos(soundX, soundY, soundZ)))) {
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
	static void evaluateEnvironment(final int sourceID, final float posX, final float posY, final float posZ, final SoundCategory category, final String name) {
		try {
			if (AsmHooks.mc.player == null || AsmHooks.mc.world == null || posY <= 0 || category == SoundCategory.RECORDS
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

			final BlockPos mcplayerpos = AsmHooks.mc.player.getPosition();
			final Vec3d playerPos = new Vec3d(mcplayerpos.getX(), mcplayerpos.getY() + AsmHooks.mc.player.getEyeHeight(), mcplayerpos.getZ());
			final Vec3d soundPos = offsetSoundByName(posX, posY, posZ, playerPos, name, category);
			final Vec3d normalToPlayer = playerPos.subtract(soundPos).normalize();

			float airAbsorptionFactor = 1.0f;

			if (Config.snowAirAbsorptionFactor.get() > 1.0f && AsmHooks.mc.world.isRaining()) {
				final Vec3d middlePos = playerPos.add(soundPos).scale(0.5);
				final BlockPos playerPosBlock = new BlockPos(playerPos);
				final BlockPos soundPosBlock = new BlockPos(soundPos);
				final BlockPos middlePosBlock = new BlockPos(middlePos);
				final int snowingPlayer = isSnowingAt(playerPosBlock, false) ? 1 : 0;
				final int snowingSound = isSnowingAt(soundPosBlock, false) ? 1 : 0;
				final int snowingMiddle = isSnowingAt(middlePosBlock, false) ? 1 : 0;
				final float snowFactor = snowingPlayer * 0.25f + snowingMiddle * 0.5f + snowingSound * 0.25f;
				airAbsorptionFactor = (float) Math.max(Config.snowAirAbsorptionFactor.get() * AsmHooks.mc.world.getRainStrength(1.0f) * snowFactor, airAbsorptionFactor);
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
				final RayTraceContext rayTraceContext = new RayTraceContext(rayOrigin, playerPos, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, AsmHooks.mc.player);
				final BlockRayTraceResult rayHit = AsmHooks.mc.world.rayTraceBlocks(rayTraceContext);

				if (rayHit == null) {
					break;
				}

				final Block blockHit = AsmHooks.mc.world.getBlockState(rayHit.getPos()).getBlock();

				float blockOcclusion = 1.0f;

				if (!blockHit.isOpaqueCube(blockHit.getDefaultState(), AsmHooks.mc.world.getWorld(), rayHit.getPos())) {
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

			if (AsmHooks.mc.player.isInWater()) {
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

				final BlockRayTraceResult rayHit = AsmHooks.mc.world.rayTraceBlocks(rayTraceContext);

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

						final BlockRayTraceResult newRayHit = AsmHooks.mc.world.rayTraceBlocks(rayTraceSecondaryContext);

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

								final RayTraceResult finalRayHit = AsmHooks.mc.world.rayTraceBlocks(rayTraceFinalContext);

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

			if (AsmHooks.mc.player.isInWater()) {
				sendCutoff0 *= 0.4f;
				sendCutoff1 *= 0.4f;
				sendCutoff2 *= 0.4f;
				sendCutoff3 *= 0.4f;
			}

			setEnvironment(sourceID, sendGain0, sendGain1, sendGain2, sendGain3, sendCutoff0, sendCutoff1, sendCutoff2,
					sendCutoff3, directCutoff, directGain, airAbsorptionFactor);
		} catch (Exception e) {
			logError("Error while evaluation environment:");
			e.printStackTrace();
			setEnvironment(sourceID, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f);
		}
	}

	private static void setEnvironment(final int sourceID, final float sendGain0, final float sendGain1,
									   final float sendGain2, final float sendGain3, final float sendCutoff0, final float sendCutoff1,
									   final float sendCutoff2, final float sendCutoff3, final float directCutoff, final float directGain,
									   final float airAbsorptionFactor) {
		// Set reverb send filter values and set source to send to all reverb fx
		// slots
		EXTEfx.alFilterf(sendFilter0, EXTEfx.AL_LOWPASS_GAIN, sendGain0);
		EXTEfx.alFilterf(sendFilter0, EXTEfx.AL_LOWPASS_GAINHF, sendCutoff0);
		AL11.alSource3i(sourceID, EXTEfx.AL_AUXILIARY_SEND_FILTER, auxFXSlot0, 0, sendFilter0);

		EXTEfx.alFilterf(sendFilter1, EXTEfx.AL_LOWPASS_GAIN, sendGain1);
		EXTEfx.alFilterf(sendFilter1, EXTEfx.AL_LOWPASS_GAINHF, sendCutoff1);
		AL11.alSource3i(sourceID, EXTEfx.AL_AUXILIARY_SEND_FILTER, auxFXSlot1, 1, sendFilter1);

		EXTEfx.alFilterf(sendFilter2, EXTEfx.AL_LOWPASS_GAIN, sendGain2);
		EXTEfx.alFilterf(sendFilter2, EXTEfx.AL_LOWPASS_GAINHF, sendCutoff2);
		AL11.alSource3i(sourceID, EXTEfx.AL_AUXILIARY_SEND_FILTER, auxFXSlot2, 2, sendFilter2);

		EXTEfx.alFilterf(sendFilter3, EXTEfx.AL_LOWPASS_GAIN, sendGain3);
		EXTEfx.alFilterf(sendFilter3, EXTEfx.AL_LOWPASS_GAINHF, sendCutoff3);
		AL11.alSource3i(sourceID, EXTEfx.AL_AUXILIARY_SEND_FILTER, auxFXSlot3, 3, sendFilter3);

		EXTEfx.alFilterf(directFilter0, EXTEfx.AL_LOWPASS_GAIN, directGain);
		EXTEfx.alFilterf(directFilter0, EXTEfx.AL_LOWPASS_GAINHF, directCutoff);
		AL10.alSourcei(sourceID, EXTEfx.AL_DIRECT_FILTER, directFilter0);

		AL10.alSourcef(sourceID, EXTEfx.AL_AIR_ABSORPTION_FACTOR, (float) MathHelper.clamp(Config.airAbsorption.get() * airAbsorptionFactor, 0.0f, 10.0f));
	}

	/**
	 * Applies the parameters in the enum ReverbParams to the main reverb
	 * effect.
	 */
	protected static void setReverbParams(final ReverbParams r, final int auxFXSlot, final int reverbSlot) {
		EXTEfx.alEffectf(reverbSlot, EXTEfx.AL_EAXREVERB_DENSITY, r.density);
		checkErrorLog("Error while assigning reverb density: " + r.density);

		EXTEfx.alEffectf(reverbSlot, EXTEfx.AL_EAXREVERB_DIFFUSION, r.diffusion);
		checkErrorLog("Error while assigning reverb diffusion: " + r.diffusion);

		EXTEfx.alEffectf(reverbSlot, EXTEfx.AL_EAXREVERB_GAIN, r.gain);
		checkErrorLog("Error while assigning reverb gain: " + r.gain);

		EXTEfx.alEffectf(reverbSlot, EXTEfx.AL_EAXREVERB_GAINHF, r.gainHF);
		checkErrorLog("Error while assigning reverb gainHF: " + r.gainHF);

		EXTEfx.alEffectf(reverbSlot, EXTEfx.AL_EAXREVERB_DECAY_TIME, r.decayTime);
		checkErrorLog("Error while assigning reverb decayTime: " + r.decayTime);

		EXTEfx.alEffectf(reverbSlot, EXTEfx.AL_EAXREVERB_DECAY_HFRATIO, r.decayHFRatio);
		checkErrorLog("Error while assigning reverb decayHFRatio: " + r.decayHFRatio);

		EXTEfx.alEffectf(reverbSlot, EXTEfx.AL_EAXREVERB_REFLECTIONS_GAIN, r.reflectionsGain);
		checkErrorLog("Error while assigning reverb reflectionsGain: " + r.reflectionsGain);

		EXTEfx.alEffectf(reverbSlot, EXTEfx.AL_EAXREVERB_LATE_REVERB_GAIN, r.lateReverbGain);
		checkErrorLog("Error while assigning reverb lateReverbGain: " + r.lateReverbGain);

		EXTEfx.alEffectf(reverbSlot, EXTEfx.AL_EAXREVERB_LATE_REVERB_DELAY, r.lateReverbDelay);
		checkErrorLog("Error while assigning reverb lateReverbDelay: " + r.lateReverbDelay);

		EXTEfx.alEffectf(reverbSlot, EXTEfx.AL_EAXREVERB_AIR_ABSORPTION_GAINHF, r.airAbsorptionGainHF);
		checkErrorLog("Error while assigning reverb airAbsorptionGainHF: " + r.airAbsorptionGainHF);

		EXTEfx.alEffectf(reverbSlot, EXTEfx.AL_EAXREVERB_ROOM_ROLLOFF_FACTOR, r.roomRolloffFactor);
		checkErrorLog("Error while assigning reverb roomRolloffFactor: " + r.roomRolloffFactor);

		// Attach updated effect object
		EXTEfx.alAuxiliaryEffectSloti(auxFXSlot, EXTEfx.AL_EFFECTSLOT_EFFECT, reverbSlot);
	}

	public static void log(final String message) {
		logger.info(message);
	}

	public static void logError(final String errorMessage) {
		logger.error(errorMessage);
	}

	protected static boolean checkErrorLog(final String errorMessage) {
		final int error = AL10.alGetError();
		if (error == AL10.AL_NO_ERROR) {
			return false;
		}

		String errorName;

		switch (error) {
			case AL10.AL_INVALID_NAME:
				errorName = "AL_INVALID_NAME";
				break;
			case AL10.AL_INVALID_ENUM:
				errorName = "AL_INVALID_ENUM";
				break;
			case AL10.AL_INVALID_VALUE:
				errorName = "AL_INVALID_VALUE";
				break;
			case AL10.AL_INVALID_OPERATION:
				errorName = "AL_INVALID_OPERATION";
				break;
			case AL10.AL_OUT_OF_MEMORY:
				errorName = "AL_OUT_OF_MEMORY";
				break;
			default:
				errorName = Integer.toString(error);
				break;
		}

		logError(errorMessage + " OpenAL error " + errorName);
		return true;
	}

}
