package com.sonicether.soundphysics;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;

import java.nio.file.Path;

@Mod.EventBusSubscriber
public class Config {

	// general
	public static ForgeConfigSpec.DoubleValue rolloffFactor;
	public static ForgeConfigSpec.DoubleValue globalVolumeMultiplier;
	public static ForgeConfigSpec.DoubleValue globalReverbGain;
	public static ForgeConfigSpec.DoubleValue globalReverbBrightness;
	public static ForgeConfigSpec.DoubleValue soundDistanceAllowance;
	public static ForgeConfigSpec.DoubleValue globalBlockAbsorption;
	public static ForgeConfigSpec.DoubleValue globalBlockReflectance;
	public static ForgeConfigSpec.DoubleValue airAbsorption;
	public static ForgeConfigSpec.DoubleValue snowAirAbsorptionFactor;
	public static ForgeConfigSpec.DoubleValue underwaterFilter;
	public static ForgeConfigSpec.BooleanValue noteBlockDisable;
	public static ForgeConfigSpec.DoubleValue maxRayDistance;
	//public static ForgeConfigSpec.BooleanValue dopplerEnabled;

	// performance
	public static ForgeConfigSpec.BooleanValue skipRainOcclusionTracing;
	public static ForgeConfigSpec.IntValue environmentEvaluationRays;
	public static ForgeConfigSpec.BooleanValue simplerSharedAirspaceSimulation;
	public static ForgeConfigSpec.BooleanValue dynamicEnvironementEvalutaion;
	public static ForgeConfigSpec.IntValue dynamicEnvironementEvalutaionFrequency;

	// block properties
	public static ForgeConfigSpec.DoubleValue stoneReflectivity;
	public static ForgeConfigSpec.DoubleValue woodReflectivity;
	public static ForgeConfigSpec.DoubleValue groundReflectivity;
	public static ForgeConfigSpec.DoubleValue plantReflectivity;
	public static ForgeConfigSpec.DoubleValue metalReflectivity;
	public static ForgeConfigSpec.DoubleValue glassReflectivity;
	public static ForgeConfigSpec.DoubleValue clothReflectivity;
	public static ForgeConfigSpec.DoubleValue sandReflectivity;
	public static ForgeConfigSpec.DoubleValue snowReflectivity;

	// compatibility
	public static ForgeConfigSpec.BooleanValue computronicsPatching;
	public static ForgeConfigSpec.BooleanValue irPatching;
	public static ForgeConfigSpec.BooleanValue dsPatching;
	public static ForgeConfigSpec.BooleanValue autoSteroDownmix;

	// misc
	public static ForgeConfigSpec.BooleanValue autoSteroDownmixLogging;
	public static ForgeConfigSpec.BooleanValue debugInfoShow;
	public static ForgeConfigSpec.BooleanValue injectorLogging;

	private static final String CATEGORY_GENERAL = "General";
	private static final String CATEGORY_PERFORMANCE = "Performance";
	private static final String CATEGORY_MATERIAL = "Materials";
	private static final String CATEGORY_COMPAT = "Compatibility";
	private static final String CATEGORY_MISC = "Misc";

	private static final ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();
	private static final ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();

	public static final ForgeConfigSpec COMMON_CONFIG;
	public static final ForgeConfigSpec CLIENT_CONFIG;

	static {
		COMMON_BUILDER.comment("General settings").push(CATEGORY_GENERAL);
		setupGeneral();
		COMMON_BUILDER.pop();

		COMMON_BUILDER.comment("Performance settings").push(CATEGORY_PERFORMANCE);
		setupPerformance();
		COMMON_BUILDER.pop();

		COMMON_BUILDER.comment("Material properties").push(CATEGORY_MATERIAL);
		setupMaterial();
		COMMON_BUILDER.pop();

		COMMON_BUILDER.comment("Compatibility settings").push(CATEGORY_COMPAT);
		setupCompatibility();
		COMMON_BUILDER.pop();

		COMMON_BUILDER.comment("Misc settings").push(CATEGORY_MISC);
		setupMisc();
		COMMON_BUILDER.pop();

		COMMON_CONFIG = COMMON_BUILDER.build();
		CLIENT_CONFIG = CLIENT_BUILDER.build();
	}

	static void loadConfig(ForgeConfigSpec spec, Path path) {
		final CommentedFileConfig configData = CommentedFileConfig.builder(path)
				.sync()
				.autosave()
				.preserveInsertionOrder()
				.writingMode(WritingMode.REPLACE)
				.build();

		configData.load();
		spec.setConfig(configData);
	}

	private static void setupGeneral() {
		// General
		rolloffFactor = COMMON_BUILDER.comment("Affects how quiet a sound gets based on distance. Lower values mean distant sounds are louder. 1.0 is the physically correct value.")
				.defineInRange("AttenuationFactor", 1.0D, 0.2D, 1.0D);
		globalVolumeMultiplier = COMMON_BUILDER.comment("The global volume multiplier made to sounds")
				.defineInRange("GlobalVolumeMultiplier", 4.0D, 0.1D, 10.0D);
		globalReverbGain = COMMON_BUILDER.comment("The global volume of simulated reverberations.")
				.defineInRange("GlobalReverbGain", 1.0D, 0.1D, 2.0D);
		globalReverbBrightness = COMMON_BUILDER.comment("The brightness of reverberation. Higher values result in more high frequencies in reverberation. Lower values give a more muffled sound to the reverb.")
				.defineInRange("GlobalReverbBrightness", 1.0D, 0.1D, 2.0D);
		globalBlockAbsorption = COMMON_BUILDER.comment("The global amount of sound that will be absorbed when traveling through blocks.")
				.defineInRange("GlobalBlockAbsorption", 1.0D, 0.1D, 4.0D);
		globalBlockReflectance = COMMON_BUILDER.comment("The global amount of sound reflectance energy of all blocks. Lower values result in more conservative reverb simulation with shorter reverb tails. Higher values result in more generous reverb simulation with higher reverb tails.")
				.defineInRange("GlobalBlockReflectance", 1.0D, 0.1D, 4.0D);
		soundDistanceAllowance = COMMON_BUILDER.comment("Minecraft won't allow sounds to play past a certain distance. This parameter is a multiplier for how far away a sound source is allowed to be in order for it to actually play. Values too high can cause polyphony issues.")
				.defineInRange("SoundDistanceAllowance", 4.0D, 1.0D, 6.0D);
		airAbsorption = COMMON_BUILDER.comment("A value controlling the amount that air absorbs high frequencies with distance. A value of 1.0 is physically correct for air with normal humidity and temperature. Higher values mean air will absorb more high frequencies with distance. 0 disables this effect.")
				.defineInRange("AirAbsorption", 1.0D, 0.0D, 5.0D);
		snowAirAbsorptionFactor = COMMON_BUILDER.comment("The maximum air absorption factor when it's snowing. The real absorption factor will depend on the snow's intensity. Set to 1 or lower to disable")
				.defineInRange("AirSnowMaxAbsorptionFactor", 5.0D, 0.0D, 10.0D);
		underwaterFilter = COMMON_BUILDER.comment("How much sound is filtered when the player is underwater. 0.0 means no filter. 1.0 means fully filtered.")
				.defineInRange("UnderwaterFilter", 0.8D, 0.0D, 1.0D);
		noteBlockDisable = COMMON_BUILDER.comment("If true, note blocks will not be processed.")
				.define("NoteBlocksDisable", false);
		maxRayDistance = COMMON_BUILDER.comment("How far the rays should be traced.")
				.defineInRange("MaxRayDistance", 256.0D, 1.0D, 8192.0D);
		/*dopplerEnabled = COMMON_BUILDER.comment("REQUIRES RESTART. If true, the doppler effect will be enabled.")
				.define("Enable doppler effect", true);*/
	}

	private static void setupPerformance() {
		// performance
		skipRainOcclusionTracing = COMMON_BUILDER.comment("If true, rain sound sources won't trace for sound occlusion. This can help performance during rain.")
				.define("SkipRainOcclusionTracing", true);
		environmentEvaluationRays = COMMON_BUILDER.comment("The number of rays to trace to determine reverberation for each sound source. More rays provides more consistent tracing results but takes more time to calculate. Decrease this value if you experience lag spikes when sounds play.")
				.defineInRange("EnvironmentEvaluationRays", 32, 8, 64);
		simplerSharedAirspaceSimulation = COMMON_BUILDER.comment("If true, enables a simpler technique for determining when the player and a sound source share airspace. Might sometimes miss recognizing shared airspace, but it's faster to calculate.")
				.define("SimplerSharedAirspaceSimulation", false);
		dynamicEnvironementEvalutaion = COMMON_BUILDER.comment("WARNING it's implemented really badly so i'd recommend not always using it.If true, the environment will keep getting evaluated for every sound that is currently playing. This may affect performance")
				.define("EnvironmentEvaluationDynamic", false);
		dynamicEnvironementEvalutaionFrequency = COMMON_BUILDER.comment("The frequency at witch to update environment of sounds if dynamic environment evaluation is enabled")
				.defineInRange("EnvironmentEvaluationFrequency", 30, 1, 60);
	}

	private static void setupMaterial() {
		// material properties
		stoneReflectivity = COMMON_BUILDER.comment("Sound reflectivity for stone blocks.")
				.defineInRange("StoneReflectivity", 0.95D, 0.0D, 1.0D);
		woodReflectivity = COMMON_BUILDER.comment("Sound reflectivity for wooden blocks.")
				.defineInRange("WoodReflectivity", 0.7D, 0.D, 1.0D);
		groundReflectivity = COMMON_BUILDER.comment("Sound reflectivity for ground blocks (dirt, gravel, etc).")
				.defineInRange("GroundReflectivity", 0.3D, 0.0D, 1.0D);
		plantReflectivity = COMMON_BUILDER.comment("Sound reflectivity for foliage blocks (leaves, grass, etc.).")
				.defineInRange("FoliageReflectivity", 0.2D, 0.0D, 1.0D);
		metalReflectivity = COMMON_BUILDER.comment("Sound reflectivity for metal blocks.")
				.defineInRange("MetalReflectivity", 0.97D, 0.0D, 1.0D);
		glassReflectivity = COMMON_BUILDER.comment("Sound reflectivity for glass blocks.")
				.defineInRange("GlassReflectivity", 0.5D, 0.0D, 1.0D);
		clothReflectivity = COMMON_BUILDER.comment("Sound reflectivity for cloth blocks (carpet, wool, etc).")
				.defineInRange("ClothReflectivity", 0.25D, 0.0D, 1.0D);
		sandReflectivity = COMMON_BUILDER.comment("Sound reflectivity for sand blocks.")
				.defineInRange("SandReflectivity", 0.2D, 0.0D, 1.0D);
		snowReflectivity = COMMON_BUILDER.comment("Sound reflectivity for snow blocks.")
				.defineInRange("SnowReflectivity", 0.2D, 0.0D, 1.0D);
	}

	private static void setupCompatibility() {
		// compatibility
		computronicsPatching = COMMON_BUILDER.comment("REQUIRES RESTART. If true, patches the Computronics sound sources so it works with Sound Physics.")
				.define("PatchComputronics", false);
		irPatching = COMMON_BUILDER.comment("REQUIRES RESTART. If true, patches the Immersive Railroading sound sources so it works with Sound Physics.")
				.define("PatchImmersiveRailroading", false);
		dsPatching = COMMON_BUILDER.comment("REQUIRES RESTART. If true, patches Dynamic Surroundings to fix some bugs with Sound Physics.")
				.define("PatchDynamicSurroundings", false);
		autoSteroDownmix = COMMON_BUILDER.comment("REQUIRES RESTART. If true, Automatically downmix stereo sounds that are loaded to mono")
				.define("StereoDownmixAuto", false);
	}

	private static void setupMisc() {
		// misc
		autoSteroDownmixLogging = COMMON_BUILDER.comment("If true, Prints sound name and format of the sounds that get converted")
				.define("StereoDownmixLogging", false);
		debugInfoShow = COMMON_BUILDER.comment("If true, Shows sources currently playing in the F3 debug info")
				.define("DynamicEnvironmentInfo", false);
		injectorLogging = COMMON_BUILDER.comment("If true, Logs debug info about the injector")
				.define("InjectorLogging", false);

	}

	private Config() {
		throw new IllegalStateException("Utility class");
	}

}
