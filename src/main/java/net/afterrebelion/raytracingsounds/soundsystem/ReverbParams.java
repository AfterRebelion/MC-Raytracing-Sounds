package net.afterrebelion.raytracingsounds.soundsystem;

import net.afterrebelion.raytracingsounds.config.ModConfigAccessor;

public class ReverbParams {
	private static final float GLOBAL_REVERB_MUL = 0.7f * ModConfigAccessor.GeneralConfig.getRolloffFactor();
	public float decayTime;				// min: 0.1f	max: 10.0f
	public float density;				// min: 0.0f	max: 1.0f
	public float diffusion;				// min: 0.0f	max: 1.0f
	public float gain;					// min: 0.0f	max: 1.0f
	public float gainHF;				// min: 0.0f	max: 1.0f
	public float decayHFRatio;			// min: 0.1f	max: 2.0f
	public float reflectionsGain;		// min: 0.1f	max: 3.16f
	public float reflectionsDelay;		// min: 0.0f	max: 0.3f
	public float lateReverbGain;		// min: 0.0f	max: 10.0f
	public float lateReverbDelay;		// min: 0.0f	max: 0.1f
	public float airAbsorptionGainHF;	// min: 0.892f	max: 1.0f
	public float roomRolloffFactor;		// min: 0.0f	max: 10.0f
	
	public static ReverbParams getReverb1() {
		final ReverbParams r = new ReverbParams();
		r.decayTime =			0.15f;
		r.density =				0.0f;
		r.diffusion =			1.0f;
		r.gain =				0.2f * GLOBAL_REVERB_MUL * 0.85f;
		r.gainHF =				0.99f;
		r.decayHFRatio =		0.6f * ModConfigAccessor.GeneralConfig.getGlobalReverbBrightness();
		r.reflectionsGain =		2.5f;
		r.reflectionsDelay =	0.001f;
		r.lateReverbGain =		1.26f;
		r.lateReverbDelay =		0.011f;
		r.airAbsorptionGainHF =	0.994f;
		r.roomRolloffFactor =	0.16f * ModConfigAccessor.GeneralConfig.getRolloffFactor();
		
		return r;
	}

	public static ReverbParams getReverb2() {
		final ReverbParams r = new ReverbParams();
		r.decayTime =			0.55f;
		r.density =				0.0f;
		r.diffusion =			1.0f;
		r.gain =				0.3f * GLOBAL_REVERB_MUL * 0.85f;
		r.gainHF =				0.99f;
		r.decayHFRatio =		0.7f * ModConfigAccessor.GeneralConfig.getGlobalReverbBrightness();
		r.reflectionsGain =		0.2f;
		r.reflectionsDelay =	0.015f;
		r.lateReverbGain =		1.26f;
		r.lateReverbDelay =		0.011f;
		r.airAbsorptionGainHF =	0.994f;
		r.roomRolloffFactor =	0.15f * ModConfigAccessor.GeneralConfig.getRolloffFactor();
		
		return r;
	}

	public static ReverbParams getReverb3() {
		final ReverbParams r = new ReverbParams();
		r.decayTime =			1.68f;
		r.density =				0.1f;
		r.diffusion =			1.0f;
		r.gain = 				0.5f * GLOBAL_REVERB_MUL * 0.85f;
		r.gainHF =				0.99f;
		r.decayHFRatio =		0.7f * ModConfigAccessor.GeneralConfig.getGlobalReverbBrightness();
		r.reflectionsGain =		0.0f;
		r.reflectionsDelay =	0.021f;
		r.lateReverbGain =		1.26f;
		r.lateReverbDelay =		0.021f;
		r.airAbsorptionGainHF =	0.994f;
		r.roomRolloffFactor =	0.13f * ModConfigAccessor.GeneralConfig.getRolloffFactor();
		
		return r;
	}

	public static ReverbParams getReverb4() {
		final ReverbParams r = new ReverbParams();
		r.decayTime =			4.142f;
		r.density =				0.5f;
		r.diffusion =			1.0f;
		r.gain =				0.4f * GLOBAL_REVERB_MUL * 0.85f;
		r.gainHF =				0.89f;
		r.decayHFRatio =		0.7f * ModConfigAccessor.GeneralConfig.getGlobalReverbBrightness();
		r.reflectionsGain =		0.0f;
		r.reflectionsDelay =	0.025f;
		r.lateReverbGain =		1.26f;
		r.lateReverbDelay =		0.021f;
		r.airAbsorptionGainHF =	0.994f;
		r.roomRolloffFactor =	0.11f * ModConfigAccessor.GeneralConfig.getRolloffFactor();
		
		return r;
	}
}
