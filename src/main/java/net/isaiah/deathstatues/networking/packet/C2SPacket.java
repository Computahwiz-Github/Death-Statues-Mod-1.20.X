package net.isaiah.deathstatues.networking.packet;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.isaiah.deathstatues.DeathStatues;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

public class C2SPacket {
    //Everything here only happens on the server
    public static void serverSpawnStatue(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        DeathStatues.spawnDeathStatue(player);
    }
}
