package net.isaiah.deathstatues.networking.packet;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.isaiah.deathstatues.DeathStatues;
import net.isaiah.deathstatues.entity.deathstatue.DeathStatueEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.UUID;

public class C2SPacket {
    //Everything here only happens on the server
    public static void serverSpawnStatue(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        //DeathStatues.spawnDeathStatue(player); // Old armor stand method
        //DeathStatues.spawnPlayerDeathStatue(player);
    }
    public static void serverSpawnDeathStatue(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        DeathStatues.spawnPlayerDeathStatue(player, player.getPos());
    }
    public static void serverReceivedStatueClient(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        DeathStatues.receivedStatueClient(handler);
    }
    public static void serverReceivedPlayer(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        DeathStatueEntity.receivedCurrentPlayer(player);
    }
    public static void serverReceivedTexture(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        DeathStatueEntity.updateSkinTexture(buf.readIdentifier());
    }
}
