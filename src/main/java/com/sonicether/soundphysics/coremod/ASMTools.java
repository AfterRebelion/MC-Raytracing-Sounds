package com.sonicether.soundphysics.coremod;

import com.sonicether.soundphysics.soundsystem.ReverbParams;
import com.sonicether.soundphysics.SoundPhysics;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.EXTEfx;

public class ASMTools {
	public static int auxFXSlot1;
	public static int auxFXSlot2;
	public static int auxFXSlot3;
	public static int auxFXSlot4;
	private static int reverb1;
	private static int reverb2;
	private static int reverb3;
	private static int reverb4;
	public static int directFilter1;
	public static int sendFilter1;
	public static int sendFilter2;
	public static int sendFilter3;
	public static int sendFilter4;

	public static void applyReverbParams() {
		if (EXTEfx.alIsAuxiliaryEffectSlot(auxFXSlot1)) {
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
			case REVERB:
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
		reverb1 = generateEFXSlots(reverb1, EFXtype.REVERB);
		reverb2 = generateEFXSlots(reverb2, EFXtype.REVERB);
		reverb3 = generateEFXSlots(reverb3, EFXtype.REVERB);
		reverb4 = generateEFXSlots(reverb4, EFXtype.REVERB);

		// Create filters
		sendFilter1 = generateEFXSlots(sendFilter1, EFXtype.FILTER);
		sendFilter2 = generateEFXSlots(sendFilter2, EFXtype.FILTER);
		sendFilter3 = generateEFXSlots(sendFilter3, EFXtype.FILTER);
		sendFilter4 = generateEFXSlots(sendFilter4, EFXtype.FILTER);
		directFilter1 = generateEFXSlots(directFilter1, EFXtype.FILTER);

		applyReverbParams();
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

	private enum EFXtype {AUX, REVERB, FILTER}
}
