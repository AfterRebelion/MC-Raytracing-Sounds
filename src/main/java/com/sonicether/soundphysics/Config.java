package com.sonicether.soundphysics;

import java.nio.file.Path;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Mod.EventBusSubscriber
public class Config {

	// general
	public static ForgeConfigSpec.DoubleValue rolloffFactor;
	public static ForgeConfigSpec.DoubleValue globalReverbGain;
	public static ForgeConfigSpec.DoubleValue globalReverbBrightness;
	public static ForgeConfigSpec.DoubleValue soundDistanceAllowance;
	public static ForgeConfigSpec.DoubleValue globalBlockAbsorption;
	public static ForgeConfigSpec.DoubleValue globalBlockReflectance;
	public static ForgeConfigSpec.DoubleValue airAbsorption;
	public static ForgeConfigSpec.DoubleValue snowAirAbsorptionFactor;
	public static ForgeConfigSpec.DoubleValue underwaterFilter;
	public static ForgeConfigSpec.BooleanValue noteBlockEnable;
	public static ForgeConfigSpec.DoubleValue maxDistance;
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

	private static final String categoryGeneral = "General";
	private static final String categoryPerformance = "Performance";
	private static final String categoryMaterialProperties = "Materials";
	private static final String categoryCompatibility = "Compatibility";
	private static final String categoryMisc = "Misc";

	private static final ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();
	private static final ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();

	public static ForgeConfigSpec COMMON_CONFIG;
	public static ForgeConfigSpec CLIENT_CONFIG;

	static {
		COMMON_BUILDER.comment("General settings").push(categoryGeneral);
		setupGeneral();
		COMMON_BUILDER.pop();

		COMMON_BUILDER.comment("Performance").push(categoryPerformance);
		setupPerformance();
		COMMON_BUILDER.pop();

		COMMON_BUILDER.comment("Material properties").push(categoryMaterialProperties);
		setupMaterial();
		COMMON_BUILDER.pop();

		COMMON_BUILDER.comment("Compatibility").push(categoryCompatibility);
		setupCompatibility();
		COMMON_BUILDER.pop();

		COMMON_BUILDER.comment("Misc").push(categoryMisc);
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

	@SubscribeEvent
	public void onConfigChanged(final ModConfig.ConfigReloading eventArgs) {

	}

	private static void setupGeneral() {
		// General
		rolloffFactor = COMMON_BUILDER.comment("Affects how quiet a sound gets based on distance. Lower values mean distant sounds are louder. 1.0 is the physically correct value.")
				.defineInRange("Attenuation Factor", 1.0D, 0.2D, 1.0D);
		globalReverbGain = COMMON_BUILDER.comment("The global volume of simulated reverberations.")
				.defineInRange("Global Reverb Gain", 1.0D, 0.1D, 2.0D);
		globalReverbBrightness = COMMON_BUILDER.comment("The brightness of reverberation. Higher values result in more high frequencies in reverberation. Lower values give a more muffled sound to the reverb.")
				.defineInRange("Global Reverb Brightness", 1.0D, 0.1D, 2.0D);
		globalBlockAbsorption = COMMON_BUILDER.comment("The global amount of sound that will be absorbed when traveling through blocks.")
				.defineInRange("Global Block Absorption", 1.0D, 0.1D, 4.0D);
		globalBlockReflectance = COMMON_BUILDER.comment("The global amount of sound reflectance energy of all blocks. Lower values result in more conservative reverb simulation with shorter reverb tails. Higher values result in more generous reverb simulation with higher reverb tails.")
				.defineInRange("Global Block Reflectance", 1.0D, 0.1D, 4.0D);
		soundDistanceAllowance = COMMON_BUILDER.comment("Minecraft won't allow sounds to play past a certain distance. This parameter is a multiplier for how far away a sound source is allowed to be in order for it to actually play. Values too high can cause polyphony issues.")
				.defineInRange("Sound Distance Allowance", 4.0D, 1.0D, 6.0D);
		airAbsorption = COMMON_BUILDER.comment("A value controlling the amount that air absorbs high frequencies with distance. A value of 1.0 is physically correct for air with normal humidity and temperature. Higher values mean air will absorb more high frequencies with distance. 0 disables this effect.")
				.defineInRange("Air Absorption", 1.0D, 0.0D, 5.0D);
		snowAirAbsorptionFactor = COMMON_BUILDER.comment("The maximum air absorption factor when it's snowing. The real absorption factor will depend on the snow's intensity. Set to 1 or lower to disable")
				.defineInRange("Max Snow Air Absorption Factor", 5.0D, 0.0D, 10.0D);
		underwaterFilter	= COMMON_BUILDER.comment("How much sound is filtered when the player is underwater. 0.0 means no filter. 1.0 means fully filtered.")
				.defineInRange("Underwater Filter", 0.8D, 0.0D, 1.0D);
		noteBlockEnable = COMMON_BUILDER.comment("If true, note blocks will be processed.")
				.define("Affect Note Blocks", true);
		maxDistance = COMMON_BUILDER.comment("How far the rays should be traced.")
				.defineInRange("Max ray distance", 256.0D, 1.0D, 8192.0D);
		/*dopplerEnabled = COMMON_BUILDER.comment("REQUIRES RESTART. If true, the doppler effect will be enabled.")
				.define("Enable doppler effect", true);*/
	}

	private static void setupPerformance() {
		// performance
		skipRainOcclusionTracing = COMMON_BUILDER.comment("If true, rain sound sources won't trace for sound occlusion. This can help performance during rain.")
			.define("Skip Rain Occlusion Tracing", true);
		environmentEvaluationRays = COMMON_BUILDER.comment("The number of rays to trace to determine reverberation for each sound source. More rays provides more consistent tracing results but takes more time to calculate. Decrease this value if you experience lag spikes when sounds play.")
			.defineInRange("Environment Evaluation Rays", 32, 8, 64);
		simplerSharedAirspaceSimulation = COMMON_BUILDER.comment("If true, enables a simpler technique for determining when the player and a sound source share airspace. Might sometimes miss recognizing shared airspace, but it's faster to calculate.")
			.define("Simpler Shared Airspace Simulation", false);
		dynamicEnvironementEvalutaion = COMMON_BUILDER.comment("WARNING it's implemented really badly so i'd recommend not always using it.If true, the environment will keep getting evaluated for every sound that is currently playing. This may affect performance")
			.define("Dynamic environment evaluation", false);
		dynamicEnvironementEvalutaionFrequency = COMMON_BUILDER.comment("The frequency at witch to update environment of sounds if dynamic environment evaluation is enabled")
			.defineInRange("Frequency of environment evaluation", 30, 1, 60);
	}

	private static void setupMaterial() {
		// material properties
		stoneReflectivity = COMMON_BUILDER.comment("Sound reflectivity for stone blocks.")
			.defineInRange("Stone Reflectivity", 0.95D, 0.0D, 1.0D);
		woodReflectivity = COMMON_BUILDER.comment("Sound reflectivity for wooden blocks.")
			.defineInRange("Wood Reflectivity", 0.7D, 0.D, 1.0D);
		groundReflectivity = COMMON_BUILDER.comment("Sound reflectivity for ground blocks (dirt, gravel, etc).")
			.defineInRange("Ground Reflectivity", 0.3D, 0.0D, 1.0D);
		plantReflectivity = COMMON_BUILDER.comment("Sound reflectivity for foliage blocks (leaves, grass, etc.).")
			.defineInRange("Foliage Reflectivity", 0.2D, 0.0D, 1.0D);
		metalReflectivity = COMMON_BUILDER.comment("Sound reflectivity for metal blocks.")
			.defineInRange("Metal Reflectivity", 0.97D, 0.0D, 1.0D);
		glassReflectivity = COMMON_BUILDER.comment("Sound reflectivity for glass blocks.")
			.defineInRange("Glass Reflectivity", 0.5D, 0.0D, 1.0D);
		clothReflectivity = COMMON_BUILDER.comment("Sound reflectivity for cloth blocks (carpet, wool, etc).")
			.defineInRange("Cloth Reflectivity", 0.25D, 0.0D, 1.0D);
		sandReflectivity = COMMON_BUILDER.comment("Sound reflectivity for sand blocks.")
			.defineInRange("Sand Reflectivity", 0.2D, 0.0D, 1.0D);
		snowReflectivity = COMMON_BUILDER.comment("Sound reflectivity for snow blocks.")
			.defineInRange("Snow Reflectivity", 0.2D, 0.0D, 1.0D);
	}

	private static void setupCompatibility() {
		// compatibility
		computronicsPatching = COMMON_BUILDER.comment("REQUIRES RESTART. If true, patches the Computronics sound sources so it works with Sound Physics.")
			.define("Patch Computronics", true);
		irPatching = COMMON_BUILDER.comment("REQUIRES RESTART. If true, patches the Immersive Railroading sound sources so it works with Sound Physics.")
			.define("Patch Immersive Railroading", true);
		dsPatching = COMMON_BUILDER.comment("REQUIRES RESTART. If true, patches Dynamic Surroundings to fix some bugs with Sound Physics.")
			.define("Patch Dynamic Surroundings", true);
		autoSteroDownmix = COMMON_BUILDER.comment("REQUIRES RESTART. If true, Automatically downmix stereo sounds that are loaded to mono")
			.define("Auto stereo downmix", true);
	}

	private static void setupMisc() {
		// misc
		autoSteroDownmixLogging = COMMON_BUILDER.comment("If true, Prints sound name and format of the sounds that get converted")
		.define("Stereo downmix Logging", false);
		debugInfoShow = COMMON_BUILDER.comment("If true, Shows sources currently playing in the F3 debug info")
		.define("Dynamic environment info in F3", false);
		injectorLogging= COMMON_BUILDER.comment("If true, Logs debug info about the injector")
		.define("Injector Logging", false);
		
	}

}
