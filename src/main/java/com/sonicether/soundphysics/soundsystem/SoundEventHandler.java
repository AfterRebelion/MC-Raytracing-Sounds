package com.sonicether.soundphysics.soundsystem;

import com.sonicether.soundphysics.Config;
import com.sonicether.soundphysics.SoundPhysics;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.sound.PlaySoundSourceEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.regex.Pattern;

@OnlyIn(Dist.CLIENT)
public class SoundEventHandler {
	private static final Pattern noteBlockPattern = Pattern.compile(".*block.note.*");

	@SubscribeEvent(priority= EventPriority.LOWEST)
	public void onEvent(PlaySoundSourceEvent event) {
		if (!event.getSound().isGlobal()) {
			if (!Config.noteBlockEnable.get() && event.getSound().getCategory() == SoundCategory.RECORDS
					&& noteBlockPattern.matcher(event.getName()).matches()) return;
			if (event.getName() == null) return;
			SoundPhysics.log(event.getSound().getSoundLocation().toString());
			Enviroment.evaluateEnvironment(event);
		}
	}
}
