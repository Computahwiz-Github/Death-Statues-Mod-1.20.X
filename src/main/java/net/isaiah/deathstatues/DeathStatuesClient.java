package net.isaiah.deathstatues;


import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.isaiah.deathstatues.client.network.AbstractClientDeathStatueEntity;
import net.isaiah.deathstatues.client.render.entity.model.DeathStatueEntityModel;
import net.isaiah.deathstatues.client.render.entity.DeathStatueEntityRenderer;
import net.isaiah.deathstatues.entity.ModEntities;
import net.isaiah.deathstatues.entity.deathstatue.DeathStatueEntity;
import net.isaiah.deathstatues.networking.DeathStatuesMessages;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public class DeathStatuesClient implements ClientModInitializer {
    public static final String MOD_ID = "death-statues";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static KeyBinding keyBinding;
    public static final EntityModelLayer MODEL_STATUE_LAYER = new EntityModelLayer(new Identifier("deathstatues", "statue"), "main");

    @Override
    public void onInitializeClient() {

        //Register Server-To-Client Packets
        DeathStatuesMessages.registerS2CPackets();

        EntityModelLayerRegistry.registerModelLayer(MODEL_STATUE_LAYER, () -> DeathStatueEntityModel.getTexturedModelData(Dilation.NONE, false));

        /*I used this lambda before and everything rendered just fine. It started giving me parameter type errors when I added in armor and held item rendering logic as DeathStatueEntity is a parameterized class.
          It now calls for AbstractClientDeathStatueEntity to be passed. I'm not sure where I messed up as the only error that shows up is here.
          I've paid attention to class hierarchy and parameter types, but yet no luck.
          Then, I had to make an AbstractClientDeathStatueEntity class to be able to retrieve the calling/current player's skin and draw it on the statue entity, which extends DeathStatueEntity. So it's a bit of a mess.
          I plan on making a ModRegistries class to get this garbage out of here, but until then, I'm stumped.
        */
        /*EntityRendererRegistry.register(DeathStatues.DEATH_STATUE, (ctx) -> {
            return new DeathStatueEntityRenderer(ctx, new DeathStatueEntityModel<AbstractClientDeathStatueEntity>(ctx.getPart(MODEL_STATUE_LAYER), false), false);
        });*/

        EntityRendererRegistry.register(ModEntities.DEATH_STATUE_ENTITY, DeathStatueEntityRenderer::new); //<- Problem lies here. I get an error: Incompatible parameter types in method reference expression

        //This code executes when the player loads into a world.
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            ClientPlayNetworking.send(DeathStatuesMessages.HAS_STATUE_CLIENT_ID, PacketByteBufs.create());
            displayWelcomeMessage(client);
        });

        //This code executes when you press [R] on the keyboard.
        keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.deathstatues.toast", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_R, "category.deathstatues.toast"));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (keyBinding.wasPressed()) {
                //LOGGER.info("Key [" + keyBinding.toString() + "] is pressed");
                displayKeyBindMessage(client);
                //ClientPlayNetworking.send(DeathStatuesMessages.SPAWN_STATUE_ID, PacketByteBufs.create());
                //displayStatueSpawned(client);
                ClientPlayNetworking.send(DeathStatuesMessages.SPAWN_DEATH_STATUE_ID, PacketByteBufs.create());
                displayStatueSpawned(client);
            }
        });
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
        MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.translatable("deathstatues.toast.destroyed").formatted(Formatting.RED));
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
        client.inGameHud.getChatHud().addMessage(Text.translatable("chat.deathstatues.toast.keybind", ("§6" + playerName + "§r")));
    }

    public static void displayWhisperMessage(MinecraftClient client, PacketByteBuf buf) {
        assert client.player != null;
        String bufferReader = buf.readString();
        String playerName = bufferReader.substring(bufferReader.indexOf(",")+1);
        String whisperMessage = bufferReader.substring(0, bufferReader.indexOf(","));
        DeathStatuesToast.add(MinecraftClient.getInstance().getToastManager(), DeathStatuesToast.Type.WHISPER_NOTIFICATION, Text.translatable("deathstatues.toast.title"), Text.translatable("deathstatues.toast.whisper",("§d" + playerName + "§r"), ("§b§o" + whisperMessage + "§r")));
    }
}
