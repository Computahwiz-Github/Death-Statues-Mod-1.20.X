package net.isaiah.deathstatues;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeathStatues implements ModInitializer {
    public static final String MOD_ID = "death-statues";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private boolean worldLoaded = false;
    private boolean welcomeToastDisplayed = false;
    private static KeyBinding keyBinding;
    String playerName;

    @Override
    public void onInitialize() {
        ServerTickEvents.START_SERVER_TICK.register(this::onServerTick);
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (entity instanceof PlayerEntity player){
                playerName = player.getName().getString();
            }
        });

        keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.deathstatues.toast", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_R, "category.deathstatues.toast"));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (keyBinding.wasPressed()) {
                LOGGER.info("Key [" + keyBinding.toString() + "] is pressed");
                assert playerName != null;
                client.inGameHud.getChatHud().addMessage(Text.translatable("chat.deathstatues.toast.keybind", playerName));
                DeathStatuesToast.add(MinecraftClient.getInstance().getToastManager(), DeathStatuesToast.Type.PERIODIC_NOTIFICATION, Text.translatable("deathstatues.toast.title"), Text.translatable("deathstatues.toast.welcome", playerName));
            }
        });
    }

    private void onServerTick(MinecraftServer server) {
        if (server.getSaveProperties().getLevelName() != null && playerName != null && !worldLoaded) {
            worldLoaded = true;
        }
        if (worldLoaded && !welcomeToastDisplayed) {
            LOGGER.info("World Loaded");
            DeathStatuesToast.add(MinecraftClient.getInstance().getToastManager(), DeathStatuesToast.Type.PERIODIC_NOTIFICATION, Text.translatable("deathstatues.toast.title"), Text.translatable("deathstatues.toast.welcome", playerName));
            welcomeToastDisplayed = true;
        }
    }
}
