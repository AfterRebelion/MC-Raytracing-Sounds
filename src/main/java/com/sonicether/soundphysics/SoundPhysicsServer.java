package com.sonicether.soundphysics;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

//Server side mod to load the config
@Mod(SoundPhysics.modid)
public class SoundPhysicsServer {
	@Mod.EventHandler
	public void preInit(final FMLPreInitializationEvent event) {
		SoundPhysics.onServer = true;
		Config.instance.preInit(event);
	}

	@Mod.EventHandler
	public void init(final FMLInitializationEvent event) {
		Config.instance.init(event);
	}
}