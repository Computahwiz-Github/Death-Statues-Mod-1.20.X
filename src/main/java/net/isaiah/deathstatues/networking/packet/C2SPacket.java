package net.isaiah.deathstatues.networking.packet;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.isaiah.deathstatues.DeathStatues;
import net.isaiah.deathstatues.block.statue.DeathStatueBaseBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

public class C2SPacket {
    //Everything here only happens on the server
    public static void serverSpawnDeathStatue(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        DeathStatues.spawnDeathStatueEntity(((PlayerEntity) player), player.getPos());
    }
    public static void serverReceivedStatueClient(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        DeathStatues.receivedStatueClient(handler);
    }
    public static void serverReceiveBasePlacesEntityConfig(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        DeathStatueBaseBlock.determineBasePlacesEntityBasedOnConfig(buf);
    }
    public static void serverReceiveBasePlacesBlockConfig(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        DeathStatueBaseBlock.determineBasePlacesBlockBasedOnConfig(buf);
    }
}
