package com.sonicether.soundphysics.utils;

import com.sonicether.soundphysics.Config;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.regex.Pattern;

public class SoundEventHandler {
	ModSoundSourceEvent.ModSound newModSound;
	private static final Pattern noteBlockPattern = Pattern.compile(".*block.note.*");

	@SubscribeEvent(priority= EventPriority.LOWEST)
	public void onEvent(PlaySoundEvent event) {
		if (!event.getSound().isGlobal()) {
			if (!Config.noteBlockEnable.get() && event.getSound().getCategory() == SoundCategory.RECORDS && noteBlockPattern.matcher(event.getName()).matches()) {
				return;
			}
			if (event.getName() == null) return;
			newModSound = new ModSoundSourceEvent.ModSound(event.getResultSound());
			event.setResultSound(null);
		}
	}
}
