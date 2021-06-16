package net.afterrebelion.raytracingsounds.config;

import me.shedaniel.autoconfig.ConfigHolder;

public class ModConfigAccessor {
	private static ModConfig config;

	static void updateConfig(ConfigHolder<ModConfig> holder) {
		config = holder.getConfig();
	}

	public static class GeneralConfig {
		public static float getRolloffFactor() {
			return config.generalConfig.rolloffFactor;
		}

		public static float getGlobalVolumeMultiplier() {
			return config.generalConfig.globalVolumeMultiplier;
		}

		public static float getGlobalReverbGain() {
			return config.generalConfig.globalReverbGain;
		}

		public static float getGlobalReverbBrightness() {
			return config.generalConfig.globalReverbBrightness;
		}

		public static float getSoundDistanceAllowance() {
			return config.generalConfig.soundDistanceAllowance;
		}

		public static float getGlobalBlockAbsorption() {
			return config.generalConfig.globalBlockAbsorption;
		}

		public static float getGlobalBlockReflectance() {
			return config.generalConfig.globalBlockReflectance;
		}

		public static float getAirAbsorption() {
			return config.generalConfig.airAbsorption;
		}

		public static float getSnowAirAbsorptionFactor() {
			return config.generalConfig.snowAirAbsorptionFactor;
		}

		public static float getUnderwaterFilter() {
			return config.generalConfig.underwaterFilter;
		}

		public static boolean isNoteBlockDisabled() {
			return config.generalConfig.noteBlockDisable;
		}

		public static float getMaxRayDistance() {
			return config.generalConfig.maxRayDistance;
		}

		private GeneralConfig() { throw new IllegalStateException("Utility class"); }
	}

	public static class PerformanceConfig {
		public static boolean getSkipRainOcclusionTracing() {
			return config.performanceConfig.skipRainOcclusionTracing;
		}

		public static int getEnvironmentEvaluationRays() {
			return config.performanceConfig.environmentEvaluationRays;
		}

		public static boolean isSimplerSharedAirspaceSimulationEnabled () {
			return config.performanceConfig.simplerSharedAirspaceSimulation;
		}

		public static boolean isDynamicEnvironmentEvaluationEnabled () {
			return config.performanceConfig.dynamicEnvironmentEvaluation;
		}

		public static int getDynamicEnvironmentEvaluationFrequency () {
			return config.performanceConfig.dynamicEnvironmentEvaluationFrequency;
		}

		private PerformanceConfig() { throw new IllegalStateException("Utility class"); }
	}

	public static class MaterialConfig {
		public static float getStone() {
			return config.materialConfig.stoneReflectivity;
		}

		public static float getWood() {
			return config.materialConfig.woodReflectivity;
		}

		public static float getGround() {
			return config.materialConfig.groundReflectivity;
		}

		public static float getPlant() {
			return config.materialConfig.plantReflectivity;
		}

		public static float getMetal() {
			return config.materialConfig.metalReflectivity;
		}

		public static float getGlass() {
			return config.materialConfig.glassReflectivity;
		}

		public static float getCloth() {
			return config.materialConfig.clothReflectivity;
		}

		public static float getSand() {
			return config.materialConfig.sandReflectivity;
		}

		public static float getSnow() {
			return config.materialConfig.snowReflectivity;
		}

		private MaterialConfig() { throw new IllegalStateException("Utility class"); }
	}

	public static class CompatibilityConfig {
		public static boolean isComputronicsEnabled() {
			return config.compatibilityConfig.computronicsPatching;
		}

		public static boolean isImmersiveRailroadingEnabled() {
			return config.compatibilityConfig.irPatching;
		}

		public static boolean isDynamicSurroundingsEnabled() {
			return config.compatibilityConfig.dsPatching;
		}

		public static boolean isAutoStereoDownmixEnabled() {
			return config.compatibilityConfig.autoStereoDownmix;
		}

		private CompatibilityConfig() { throw new IllegalStateException("Utility class"); }
	}

	public static class MiscConfig {
		public static boolean isAutoStereoDownmixLoggingEnabled() {
			return config.miscConfig.autoStereoDownmixLogging;
		}

		public static boolean isDebugInfoEnabled() {
			return config.miscConfig.debugInfoShow;
		}

		public static boolean isInjectorLoggingEnabled() {
			return config.miscConfig.injectorLogging;
		}

		private MiscConfig() { throw new IllegalStateException("Utility class"); }
	}

	private ModConfigAccessor() { throw new IllegalStateException("Utility class"); }
}
