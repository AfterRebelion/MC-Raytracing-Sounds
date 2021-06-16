package net.afterrebelion.raytracingsounds.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;
import net.afterrebelion.raytracingsounds.RaytracingsoundsMain;

@Config(name = RaytracingsoundsMain.MODID)
public class ModConfig implements ConfigData {
	@ConfigEntry.Category("general")
	@ConfigEntry.Gui.TransitiveObject
	SetupGeneral generalConfig = new SetupGeneral();

	@ConfigEntry.Category("performance")
	@ConfigEntry.Gui.TransitiveObject
	SetupPerformance performanceConfig = new SetupPerformance();

	@ConfigEntry.Category("material")
	@ConfigEntry.Gui.TransitiveObject
	SetupMaterial materialConfig = new SetupMaterial();

	@ConfigEntry.Category("compatibility")
	@ConfigEntry.Gui.TransitiveObject
	SetupCompatibility compatibilityConfig = new SetupCompatibility();

	@ConfigEntry.Category("misc")
	@ConfigEntry.Gui.TransitiveObject
	SetupMisc miscConfig = new SetupMisc();

	static class SetupGeneral {
		// General
		@Comment("Affects how quiet a sound gets based on distance. Lower values mean distant sounds are louder. 1.0 is the physically correct value.")
		@ConfigEntry.Gui.Tooltip(count = 3)
		float rolloffFactor = 1.0f;

		@Comment("The global volume multiplier made to sounds.")
		@ConfigEntry.Gui.Tooltip
		float globalVolumeMultiplier = 4.0f;

		@Comment("The global volume of simulated reverberations.")
		@ConfigEntry.Gui.Tooltip
		float globalReverbGain = 1.0f;

		@Comment("The brightness of reverberation. Higher values result in more high frequencies in reverberation. Lower values give a more muffled sound to the reverb.")
		@ConfigEntry.Gui.Tooltip(count = 3)
		float globalReverbBrightness = 1.0f;

		@Comment("Minecraft won't allow sounds to play past a certain distance. This parameter is a multiplier for how far away a sound source is allowed to be in order for it to actually play. Values too high can cause polyphony issues.")
		@ConfigEntry.Gui.Tooltip(count = 4)
		float soundDistanceAllowance = 4.0f;

		@Comment("The global amount of sound that will be absorbed when traveling through blocks.")
		@ConfigEntry.Gui.Tooltip
		float globalBlockAbsorption = 1.0f;

		@Comment("The global amount of sound reflectance energy of all blocks. Lower values result in more conservative reverb simulation with shorter reverb tails. Higher values result in more generous reverb simulation with higher reverb tails.")
		@ConfigEntry.Gui.Tooltip(count = 3)
		float globalBlockReflectance = 1.0f;

		@Comment("A value controlling the amount that air absorbs high frequencies with distance. A value of 1.0 is physically correct for air with normal humidity and temperature. Higher values mean air will absorb more high frequencies with distance. 0 disables this effect.")
		@ConfigEntry.Gui.Tooltip(count = 3)
		float airAbsorption = 1.0f;

		@Comment("The maximum air absorption factor when it's snowing. The real absorption factor will depend on the snow's intensity. Set to 1 or lower to disable.")
		@ConfigEntry.Gui.Tooltip(count = 2)
		float snowAirAbsorptionFactor = 5.0f;

		@Comment("How much sound is filtered when the player is underwater. 0.0 means no filter. 1.0 means fully filtered.")
		@ConfigEntry.Gui.Tooltip(count = 2)
		float underwaterFilter = 0.8f;

		@Comment("If true, note blocks will not be processed.")
		@ConfigEntry.Gui.Tooltip
		boolean noteBlockDisable = false;

		@Comment("How far the rays should be traced.")
		@ConfigEntry.Gui.Tooltip
		float maxRayDistance = 256.0f;

		/*@Comment("REQUIRES RESTART. If true, the doppler effect will be enabled.")
		@ConfigEntry.Gui.Tooltip
		@ConfigEntry.Gui.RequiresRestart
		boolean dopplerEnabled;*/
	}

	static class SetupPerformance {
		// performance
		@Comment("If true, rain sound sources won't trace for sound occlusion. This can help performance during rain.")
		@ConfigEntry.Gui.Tooltip(count = 2)
		boolean skipRainOcclusionTracing = true;

		@Comment("The number of rays to trace to determine reverberation for each sound source. More rays provides more consistent tracing results but takes more time to calculate. Decrease this value if you experience lag spikes when sounds play.")
		@ConfigEntry.Gui.Tooltip(count = 3)
		@ConfigEntry.BoundedDiscrete(min = 8, max = 64)
		int environmentEvaluationRays = 32;

		@Comment("If true, enables a simpler technique for determining when the player and a sound source share airspace. Might sometimes miss recognizing shared airspace, but it's faster to calculate.")
		@ConfigEntry.Gui.Tooltip(count = 2)
		boolean simplerSharedAirspaceSimulation = false;

		@Comment("WARNING it's implemented really badly so i'd recommend not always using it.If true, the environment will keep getting evaluated for every sound that is currently playing. This may affect performance")
		@ConfigEntry.Gui.Tooltip(count = 3)
		boolean dynamicEnvironmentEvaluation = false;

		@Comment("The frequency at witch to update environment of sounds if dynamic environment evaluation is enabled")
		@ConfigEntry.Gui.Tooltip
		@ConfigEntry.BoundedDiscrete(min = 1, max = 60)
		int dynamicEnvironmentEvaluationFrequency = 30;
	}

	static class SetupMaterial {
		// material properties
		@Comment("Sound reflectivity for stone blocks.")
		@ConfigEntry.Gui.Tooltip
		float stoneReflectivity = 0.95f;

		@Comment("Sound reflectivity for wooden blocks.")
		@ConfigEntry.Gui.Tooltip
		float woodReflectivity = 0.7f;

		@Comment("Sound reflectivity for ground blocks (dirt, gravel, etc).")
		@ConfigEntry.Gui.Tooltip
		float groundReflectivity = 0.3f;

		@Comment("Sound reflectivity for foliage blocks (leaves, grass, etc.).")
		@ConfigEntry.Gui.Tooltip
		float plantReflectivity = 0.2f;

		@Comment("Sound reflectivity for metal blocks.")
		@ConfigEntry.Gui.Tooltip
		float metalReflectivity = 0.97f;

		@Comment("Sound reflectivity for glass blocks.")
		@ConfigEntry.Gui.Tooltip
		float glassReflectivity = 0.5f;

		@Comment("Sound reflectivity for cloth blocks (carpet, wool, etc).")
		@ConfigEntry.Gui.Tooltip
		float clothReflectivity = 0.25f;

		@Comment("Sound reflectivity for sand blocks.")
		@ConfigEntry.Gui.Tooltip
		float sandReflectivity = 0.2f;

		@Comment("Sound reflectivity for snow blocks.")
		@ConfigEntry.Gui.Tooltip
		float snowReflectivity = 0.2f;
	}

	static class SetupCompatibility {
		// compatibility
		@Comment("REQUIRES RESTART. If true, patches the Computronics sound sources so it works with Sound Physics.")
		@ConfigEntry.Gui.Tooltip(count = 2)
		@ConfigEntry.Gui.RequiresRestart
		boolean computronicsPatching = false;

		@Comment("REQUIRES RESTART. If true, patches the Immersive Railroading sound sources so it works with Sound Physics.")
		@ConfigEntry.Gui.Tooltip(count = 2)
		@ConfigEntry.Gui.RequiresRestart
		boolean irPatching = false;

		@Comment("REQUIRES RESTART. If true, patches Dynamic Surroundings to fix some bugs with Sound Physics.")
		@ConfigEntry.Gui.Tooltip(count = 2)
		@ConfigEntry.Gui.RequiresRestart
		boolean dsPatching = false;

		@Comment("REQUIRES RESTART. If true, Automatically downmix stereo sounds that are loaded to mono")
		@ConfigEntry.Gui.Tooltip(count = 2)
		@ConfigEntry.Gui.RequiresRestart
		boolean autoStereoDownmix = false;
	}

	static class SetupMisc {
		// misc
		@Comment("If true, Prints sound name and format of the sounds that get converted.")
		@ConfigEntry.Gui.Tooltip
		boolean autoStereoDownmixLogging = false;

		@Comment("If true, Shows sources currently playing in the F3 debug info.")
		@ConfigEntry.Gui.Tooltip
		boolean debugInfoShow = false;

		@Comment("If true, Logs debug info about the injector.")
		@ConfigEntry.Gui.Tooltip
		boolean injectorLogging = false;
	}
}
