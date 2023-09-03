package net.isaiah.deathstatues;

import net.fabricmc.api.ModInitializer;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeathStatues implements ModInitializer {
    public static final String MOD_ID = "death-statues";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        //DeathStatuesToast.registerModToasts();
        LOGGER.info("Hello Fabric world!");
    }
}
