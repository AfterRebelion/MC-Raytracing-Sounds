package net.afterrebelion.raytracingsounds.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ClientModInitializer;

public class ModConfigInit implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		System.out.println("Init Config!");
		ConfigHolder<ModConfig> holder = AutoConfig.register(ModConfig.class, Toml4jConfigSerializer::new);
		ModConfigAccessor.updateConfig(holder);
	}
}
