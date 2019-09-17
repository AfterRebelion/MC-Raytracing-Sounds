package com.sonicether.soundphysics;

import net.minecraft.client.gui.screen.Screen;
import net.minecraftforge.fml.client.config.GuiConfig;

public class SPGuiConfig extends GuiConfig {

	public SPGuiConfig(final Screen parent) {
		super(parent, Config.instance.getConfigElements(), SoundPhysics.modid, false, false,
				"Sound Physics Configuration");
	}

}