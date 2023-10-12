package net.isaiah.deathstatues.networking;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.isaiah.deathstatues.DeathStatues;
import net.isaiah.deathstatues.networking.packet.C2SPacket;
import net.isaiah.deathstatues.networking.packet.S2CPacket;
import net.minecraft.util.Identifier;

public class ModMessages {
    public static final Identifier SPAWN_DEATH_STATUE_ID = new Identifier(DeathStatues.MOD_ID, "spawn_death_statue");
    public static final Identifier DESTROY_STATUE_ID = new Identifier(DeathStatues.MOD_ID, "destroy_statue");
    public static final Identifier PLAYER_DIED_ID = new Identifier(DeathStatues.MOD_ID, "player_died");
    public static final Identifier WHISPER_COMMAND_ID = new Identifier(DeathStatues.MOD_ID, "whisper_command");
    public static final Identifier HAS_STATUE_CLIENT_ID = new Identifier(DeathStatues.MOD_ID, "has_statue_client");
    public static final Identifier CURRENT_PLAYER_ID = new Identifier(DeathStatues.MOD_ID, "current_player");
    public static final Identifier BASE_PLACES_ENTITY_CONFIG_ID = new Identifier(DeathStatues.MOD_ID, "base_places_entity_config");
    public static final Identifier BASE_PLACES_BLOCK_CONFIG_ID = new Identifier(DeathStatues.MOD_ID, "base_places_block_config");

    public static void registerC2SPackets() {
        ServerPlayNetworking.registerGlobalReceiver(HAS_STATUE_CLIENT_ID, C2SPacket::serverReceivedStatueClient);
        ServerPlayNetworking.registerGlobalReceiver(SPAWN_DEATH_STATUE_ID, C2SPacket::serverSpawnDeathStatue);
        ServerPlayNetworking.registerGlobalReceiver(BASE_PLACES_ENTITY_CONFIG_ID, C2SPacket::serverReceiveBasePlacesEntityConfig);
        ServerPlayNetworking.registerGlobalReceiver(BASE_PLACES_BLOCK_CONFIG_ID, C2SPacket::serverReceiveBasePlacesBlockConfig);
    }

    public static void registerS2CPackets() {
        ClientPlayNetworking.registerGlobalReceiver(DESTROY_STATUE_ID, S2CPacket::clientDestroyedStatue);
        ClientPlayNetworking.registerGlobalReceiver(PLAYER_DIED_ID, S2CPacket::clientPlayerDied);
        ClientPlayNetworking.registerGlobalReceiver(WHISPER_COMMAND_ID, S2CPacket::clientWhisperCommand);
    }
}
