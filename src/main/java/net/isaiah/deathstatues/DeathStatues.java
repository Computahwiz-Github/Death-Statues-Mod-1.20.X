package net.isaiah.deathstatues;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeathStatues implements ModInitializer {
    public static final String MOD_ID = "death-statues";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private boolean worldLoaded = false;
    private boolean welcomeToastDisplayed = false;

    @Override
    public void onInitialize() {
        ServerTickEvents.START_SERVER_TICK.register(this::onServerTick);
    }

    private void onServerTick(MinecraftServer server){
        if (server.getSaveProperties().getLevelName() != null) {
            worldLoaded = true;
        }
        if (worldLoaded && !welcomeToastDisplayed){
            DeathStatuesToast.showLoadedClientToast("deathstatues.toast.title","deathstatues.toast.loaded");

            //DeathStatuesToast.showCustomToast("My Custom Toast", new String[] {"This is a custom toast", "using Fabric for Minecraft 1.20!", "It has multiple lines of text."});
            LOGGER.info("World has loaded!");
            welcomeToastDisplayed = true;
        }
    }
}
