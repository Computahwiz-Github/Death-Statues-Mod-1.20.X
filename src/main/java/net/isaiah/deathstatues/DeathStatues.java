package net.isaiah.deathstatues;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.isaiah.deathstatues.block.ModBlocks;
import net.isaiah.deathstatues.block.entity.ModBlockEntities;
import net.isaiah.deathstatues.entity.ModEntities;
import net.isaiah.deathstatues.entity.deathstatue.DeathStatueEntity;
import net.isaiah.deathstatues.item.ModItemGroups;
import net.isaiah.deathstatues.item.ModItems;
import net.isaiah.deathstatues.networking.ModMessages;
import net.isaiah.deathstatues.screen.ModScreenHandlers;
import net.minecraft.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DeathStatues implements ModInitializer {
    public static final String MOD_ID = "deathstatues";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static boolean hasStatuesClient = false;

    @Override
    public void onInitialize() {
        //Register Client-To-Server Packets
        ModMessages.registerC2SPackets();

        ModItemGroups.registerItemGroups();
        ModItems.registerModItems();
        ModBlocks.registerModBlocks();
        ModScreenHandlers.registerScreenHandlers();
        ModBlockEntities.registerBlockEntities();

        FabricDefaultAttributeRegistry.register(ModEntities.DEATH_STATUE, DeathStatueEntity.createStatueAttributes());

        //This Event triggers when an entity dies and I check if it's a player then spawn the statue
        ServerLivingEntityEvents.ALLOW_DEATH.register((entity, damageSource, damageAmount) -> {
            if (entity instanceof PlayerEntity player) {
                //LOGGER.info("Event: Player (" + player.getName().getString() + "): [" + player.getUuidAsString() + "] Died");
                World world = player.getWorld();
                BlockPos playerBlockPos = player.getBlockPos();
                world.setBlockState(playerBlockPos, ModBlocks.DEATH_STATUE_BASE_BLOCK.getDefaultState());
                ServerPlayNetworking.send(((ServerPlayerEntity) player), ModMessages.SERVER_NEEDS_STATUE_TEXTURE_ID, PacketByteBufs.create());
                ServerPlayNetworking.registerGlobalReceiver(ModMessages.SERVER_RECEIVED_STATUE_TEXTURE_ID, (server, player1, handler, buf, responseSender) -> {
                    System.out.println("StatueTexture buffer: " + Identifier.tryParse(buf.readString()));
                    ModEntities.spawnDeathStatueEntity(player, player.getPos().add(0, 1, 0), player.getHeadYaw(), player.getBodyYaw(), player.getYaw(), buf.readString());
                });
                //ModEntities.spawnDeathStatueEntity(player, player.getPos().add(0, 1, 0), player.getHeadYaw(), player.getBodyYaw(), player.getYaw(), ModEntities.getStatueTextureString());
                ServerPlayNetworking.send((ServerPlayerEntity) player, ModMessages.PLAYER_DIED_ID, PacketByteBufs.create());
            }
            return true;
        });

        //This Event triggers when an entity is attacked. I check if it's a player attacking an armor stand of the same name (So you don't destroy other people's statues)
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClient()) {
                return ActionResult.PASS;
            }
            //This is called when the Death Statue Entity has the same name as the attacking player
            if (entity instanceof DeathStatueEntity deathStatueEntity && getPlayerNameFromStatueName(entity.getName().getString()).equals(player.getName())) {
                //LOGGER.info("Event: Attacking Death Statue: [" + entity.getUuidAsString() + "], at: (" + entity.getBlockX() + ", " + entity.getBlockY() + ", " + entity.getBlockZ() + ")");
                NbtCompound nbtCompound = new NbtCompound();
                deathStatueEntity.writeCustomDataToNbt(nbtCompound);
                boolean isFake = nbtCompound.getBoolean("Fake");
                if (isFake) {
                    //System.out.println("Death Statue Entity Is Fake");
                    deathStatueEntity.remove(Entity.RemovalReason.DISCARDED);
                    return ActionResult.SUCCESS;
                }
                else {
                    //System.out.println("Death Statue Entity Is Real");
                    deathStatueEntity.kill();
                    ServerPlayNetworking.send((ServerPlayerEntity) player, ModMessages.DESTROY_STATUE_ID, PacketByteBufs.create());
                    return ActionResult.PASS;
                }
            }
            //This is called when the Death Statue Entity doesn't have the same name as the attacking player
            else if (entity instanceof DeathStatueEntity && !getPlayerNameFromStatueName(entity.getName().getString()).equals(player.getName())) {
                //If you're creative, you can destroy any statue
                if (player.isCreative()) {
                    entity.remove(Entity.RemovalReason.DISCARDED);
                    return ActionResult.SUCCESS;
                }
                else {
                    player.sendMessage(Text.of("This statue isn't yours!"));
                    return ActionResult.FAIL;
                }
            }

            return ActionResult.PASS;
        });
    }
    public Text getPlayerNameFromStatueName(String entityName) {
        //Gets characters from between two square brackets, "[ ]"
        Pattern pattern = Pattern.compile("\\[(.*?)]");
        Matcher matcher = pattern.matcher(entityName);
        // Find the first matching pattern (if any)
        if (matcher.find()) {
            try {
                return Text.of(matcher.group(1));
            } catch (IllegalArgumentException e) {
                // UUID parsing failed
                e.printStackTrace();
            }
        }
        return Text.of(entityName);
    }
    //This is where I will eventually grab the player's skin texture for the statue block model
    public static void receivedStatueClient(ServerPlayNetworkHandler handler) {
        hasStatuesClient = true;
        //handler.getPlayer().sendMessage(Text.of("Has Statue Client: " + hasStatuesClient));
    }
}
