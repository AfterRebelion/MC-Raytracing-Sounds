package com.sonicether.soundphysics.coremod;

import com.sonicether.soundphysics.Config;
import com.sonicether.soundphysics.SoundPhysics;
import net.minecraft.entity.Entity;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.regex.Pattern;

@SuppressWarnings("unused")
public class ASMHooks {
	// THESE VARIABLES ARE CONSTANTLY ACCESSED AND USED BY ASM INJECTED CODE! DO
	// NOT REMOVE!
	public static final double SOUND_DISTANCE_ALLOWANCE = Config.soundDistanceAllowance.get(); /** {@link net.minecraft.server.management.PlayerList#sendToAllNearExcept radius}*/

	protected static final Pattern stepPattern = Pattern.compile(".*step.*");
	/**
	 * CALLED BY ASM INJECTED CODE!
	 */
	@SubscribeEvent
	public static void init() {
		try {
			/*if (Config.dopplerEnabled) {
				sndSystem.changeDopplerFactor(1.0f);
				AL11.alSpeedOfSound(343.3f); // Should already be 343.3 but just in case
			}*/
			ASMTools.setupEFX();
			//ProcThread.setupThread();
		} catch (Exception e) {
			SoundPhysics.logError("Failed to init EFX or thread");
			SoundPhysics.logError(e.toString());
		}
	}

	/**
	 * CALLED BY ASM INJECTED CODE!
	 */
	public static double calculateEntitySoundOffset(final Entity entity, final SoundEvent sound) {
		if (sound == null) return entity.getEyeHeight();
		if (stepPattern.matcher(sound.getName().getPath()).matches()) {
			return 0;
		}

		return entity.getEyeHeight();
	}

	private ASMHooks() {
		throw new IllegalStateException("Utility class");
	}
}
