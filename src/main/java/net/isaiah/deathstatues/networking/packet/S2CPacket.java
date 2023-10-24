package net.isaiah.deathstatues.networking.packet;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.isaiah.deathstatues.DeathStatuesClient;
import net.isaiah.deathstatues.screen.DeathStatuesScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;

public class S2CPacket {

    //Everything here happens on the Client
    public static void clientDestroyedStatue(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        DeathStatuesClient.displayStatueDestroyed();
    }

    public static void clientPlayerDied(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        DeathStatuesClient.displayStatueSpawned(client);
    }

    public static void clientWhisperCommand(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        DeathStatuesClient.displayWhisperMessage(client, buf);
    }

    public static void clientSendStatueTexture(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        DeathStatuesClient.sendStatueTexture(client);
    }

    public static void clientOpenStatueBaseScreen(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        DeathStatuesScreen.setDeathStatueEntityID(buf.readInt());
    }
}
