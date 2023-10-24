package net.isaiah.deathstatues;


import io.netty.buffer.Unpooled;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.isaiah.deathstatues.block.ModBlocks;
import net.isaiah.deathstatues.client.render.entity.model.DeathStatueEntityModel;
import net.isaiah.deathstatues.client.render.entity.DeathStatueEntityRenderer;
import net.isaiah.deathstatues.config.DeathStatueConfig;
import net.isaiah.deathstatues.config.DeathStatueConfigManager;
import net.isaiah.deathstatues.entity.ModEntities;
import net.isaiah.deathstatues.entity.deathstatue.DeathStatueEntity;
import net.isaiah.deathstatues.networking.ModMessages;
import net.isaiah.deathstatues.screen.DeathStatuesScreen;
import net.isaiah.deathstatues.screen.ModScreenHandlers;
import net.isaiah.deathstatues.util.ModModelPredicateProvider;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;

@Environment(EnvType.CLIENT)
public class DeathStatuesClient implements ClientModInitializer {
    public static final String MOD_ID = "deathstatues";
    private static KeyBinding spawnKeyBinding;
    public static final EntityModelLayer MODEL_STATUE_LAYER = new EntityModelLayer(new Identifier(MOD_ID, "statue"), "main");
    private PlayerEntity currentPlayer;

    @Override
    public void onInitializeClient() {
        DeathStatueConfigManager.initializeConfig();

        //Register Server-To-Client Packets
        ModMessages.registerS2CPackets();

        ModModelPredicateProvider.registerModModels();
        HandledScreens.register(ModScreenHandlers.DEATH_STATUE_SCREEN_HANDLER, DeathStatuesScreen::new);
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.DEATH_STATUE_BLOCK, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.DEATH_STATUE_BASE_BLOCK, RenderLayer.getCutout());

        EntityModelLayerRegistry.registerModelLayer(MODEL_STATUE_LAYER, () -> DeathStatueEntityModel.getTexturedModelData(Dilation.NONE, false));

        EntityRendererRegistry.register(ModEntities.DEATH_STATUE, DeathStatueEntityRenderer::new);

        //This code executes when the player loads into a world.
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            ClientPlayNetworking.send(ModMessages.HAS_STATUE_CLIENT_ID, PacketByteBufs.create());
            displayWelcomeMessage(client);

            this.currentPlayer = client.player;
            PacketByteBuf buf = new PacketByteBuf(PacketByteBufs.create());
            buf.writeUuid(this.currentPlayer.getUuid());
            ClientPlayNetworking.send(ModMessages.CURRENT_PLAYER_ID, buf);

            ClientPlayNetworking.send(ModMessages.BASE_PLACES_ENTITY_CONFIG_ID, new PacketByteBuf(Unpooled.buffer().writeBoolean(DeathStatueConfig.BASE_PLACES_ENTITY.getValue())));
            ClientPlayNetworking.send(ModMessages.BASE_PLACES_BLOCK_CONFIG_ID, new PacketByteBuf(Unpooled.buffer().writeBoolean(DeathStatueConfig.BASE_PLACES_BLOCK.getValue())));
        });

        //This code executes when you press [R] on the keyboard.
        spawnKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.deathstatues.toast", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_R, "category.deathstatues.toast"));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (spawnKeyBinding.wasPressed()) {
                displayKeyBindMessage(client);
                //ClientPlayNetworking.send(ModMessages.UPDATE_STATUE_TEXTURE, new PacketByteBuf(Unpooled.buffer()).writeIdentifier(getSkinTexture()));
                assert client.player != null;
                String statueTexture = Objects.requireNonNull(Objects.requireNonNull(client.player.networkHandler).getPlayerListEntry(client.player.getUuid())).getSkinTexture().toTranslationKey().replace("minecraft.", "");
                ClientPlayNetworking.send(ModMessages.SPAWN_FAKE_DEATH_STATUE_ID, PacketByteBufs.create().writeString(statueTexture));
                System.out.println("StatueTexture: " + statueTexture);
                //displayStatueSpawned(client);
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (Screen.hasControlDown()) {
                assert client.crosshairTarget != null;
                if (client.crosshairTarget.getType().equals(HitResult.Type.ENTITY)) {
                    if (((EntityHitResult) client.crosshairTarget).getEntity() instanceof DeathStatueEntity) {
                        boolean hasControlDown = true;
                        ClientPlayNetworking.send(ModMessages.SERVER_PLAYER_HAS_CONTROL_DOWN_ID, new PacketByteBuf(Unpooled.buffer().writeBoolean(hasControlDown)));
                    }
                }
            }
        });
    }

    public static void sendStatueTexture(MinecraftClient client) {
        assert client.player != null;
        String statueTexture = Objects.requireNonNull(Objects.requireNonNull(client.player.networkHandler).getPlayerListEntry(client.player.getUuid())).getSkinTexture().toTranslationKey().replace("minecraft.", "");
        ClientPlayNetworking.send(ModMessages.SERVER_RECEIVED_STATUE_TEXTURE_ID, PacketByteBufs.create().writeString(statueTexture));
    }

    public static void displayStatueSpawned(MinecraftClient client) {
        assert client.player != null;
        String statueLocation = client.player.getBlockX() + ", " + client.player.getBlockY() + ", " + client.player.getBlockZ();
        DeathStatuesToast.add(MinecraftClient.getInstance().getToastManager(), DeathStatuesToast.Type.STATUE_NOTIFICATION, Text.translatable("deathstatues.toast.title"), Text.translatable("deathstatues.toast.spawned").append(statueLocation).formatted(Formatting.DARK_PURPLE).append("§A)"));

        MutableText tooltipText = Text.translatable("deathstatues.toast.spawned");
        MutableText message = Text.translatable(statueLocation).formatted(Formatting.GOLD).setStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("Click to get Location in chat!"))).withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tp @s " + statueLocation.replace(",", ""))).withFormatting(Formatting.DARK_PURPLE));
        tooltipText.append(message).append("§A)");
        MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(tooltipText);
    }

    public static void displayStatueDestroyed() {
        MinecraftClient.getInstance().inGameHud.setOverlayMessage(Text.translatable("deathstatues.toast.destroyed").formatted(Formatting.RED), false);
        DeathStatuesToast.add(MinecraftClient.getInstance().getToastManager(), DeathStatuesToast.Type.STATUE_NOTIFICATION, Text.translatable("deathstatues.toast.title"), Text.translatable("deathstatues.toast.destroyed"));
    }

    public static void displayWelcomeMessage(MinecraftClient client) {
        assert client.player != null;
        String playerName = client.player.getName().getString();
        client.inGameHud.getChatHud().addMessage(Text.translatable("chat.deathstatues.welcome", ("§6" + playerName + "§r")));
        DeathStatuesToast.add(MinecraftClient.getInstance().getToastManager(), DeathStatuesToast.Type.STATUE_NOTIFICATION, Text.translatable("deathstatues.toast.title"), Text.translatable("deathstatues.toast.welcome", ("§6" + playerName + "§r")));
    }

    public static void displayKeyBindMessage(MinecraftClient client) {
        assert client.player != null;
        String playerName = client.player.getName().getString();
        client.inGameHud.getChatHud().addMessage(Text.translatable("chat.deathstatues.keybind", ("§6" + playerName + "§r"))
                .append(Text.translatable("chat.deathstatues.keybind.action"))
                .append(Text.translatable("chat.deathstatues.keybind.message"))
                .setStyle(Style.EMPTY.withHoverEvent(HoverEvent.Action.SHOW_TEXT.buildHoverEvent(Text.translatable("chat.deathstatues.keybind.tooltip")))));
    }

    public static void displayWhisperMessage(MinecraftClient client, PacketByteBuf buf) {
        assert client.player != null;
        String bufferReader = buf.readString();
        String playerName = bufferReader.substring(bufferReader.indexOf(",")+1);
        String whisperMessage = bufferReader.substring(0, bufferReader.indexOf(","));
        DeathStatuesToast.add(MinecraftClient.getInstance().getToastManager(), DeathStatuesToast.Type.WHISPER_NOTIFICATION, Text.translatable("deathstatues.toast.title"), Text.translatable("deathstatues.toast.whisper",("§d" + playerName + "§r"), ("§b§o" + whisperMessage + "§r")));
    }
}
