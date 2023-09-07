package net.isaiah.deathstatues.networking.packet;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.isaiah.deathstatues.DeathStatuesClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;

public class S2CPacket {

    //Everything here happens on the Client
    public static void clientDestroyedStatue(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        DeathStatuesClient.displayStatueDestroyed();
    }

    public static void clientPlayerDied(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender response_sender) {
        DeathStatuesClient.displayStatueSpawned(client);
    }
}
