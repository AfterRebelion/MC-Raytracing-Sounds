package com.sonicether.soundphysics;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

//Server side mod to load the config
@Mod(SoundPhysics.modid)
public class SoundPhysicsServer {
	public void setup(final FMLCommonSetupEvent event) {
		SoundPhysics.onServer = true;
		Config.instance.preInit(event);
	}

	public void doClientStuff(final FMLClientSetupEvent event) {
		Config.instance.init(event);
	}
}