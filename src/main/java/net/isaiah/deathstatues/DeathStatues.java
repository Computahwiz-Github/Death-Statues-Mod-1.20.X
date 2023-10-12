package net.isaiah.deathstatues;

import com.mojang.authlib.GameProfile;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.isaiah.deathstatues.block.ModBlocks;
import net.isaiah.deathstatues.block.entity.ModBlockEntities;
import net.isaiah.deathstatues.entity.deathstatue.DeathStatueEntity;
import net.isaiah.deathstatues.item.ModItemGroups;
import net.isaiah.deathstatues.item.ModItems;
import net.isaiah.deathstatues.networking.ModMessages;
import net.isaiah.deathstatues.screen.ModScreenHandlers;
import net.minecraft.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DeathStatues implements ModInitializer {
    public static final String MOD_ID = "deathstatues";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static boolean hasStatuesClient = false;
    private static final Identifier DEATH_STATUE_ENTITY_ID = new Identifier("deathstatues", "death_statue_entity");
    public static final EntityType<DeathStatueEntity> DEATH_STATUE = Registry.register(Registries.ENTITY_TYPE, DEATH_STATUE_ENTITY_ID,
            FabricEntityTypeBuilder.create(SpawnGroup.MISC, DeathStatueEntity::new)
                    .dimensions(EntityDimensions.fixed(0.6F, 1.8F)).build());

    @Override
    public void onInitialize() {
        //Register Client-To-Server Packets
        ModMessages.registerC2SPackets();

        ModItemGroups.registerItemGroups();
        ModItems.registerModItems();
        ModBlocks.registerModBlocks();
        ModScreenHandlers.registerScreenHandlers();
        ModBlockEntities.registerBlockEntities();

        FabricDefaultAttributeRegistry.register(DEATH_STATUE, DeathStatueEntity.createStatueAttributes());

        //This Event triggers when an entity dies and I check if it's a player then spawn the statue
        ServerLivingEntityEvents.ALLOW_DEATH.register((entity, damageSource, damageAmount) -> {
            if (entity instanceof PlayerEntity player){
                //LOGGER.info("Event: Player (" + player.getName().getString() + "): [" + player.getUuidAsString() + "] Died");
                ServerPlayNetworking.send((ServerPlayerEntity) player, ModMessages.PLAYER_DIED_ID, PacketByteBufs.create());
                spawnDeathStatueEntity(player, player.getPos());
            }
            return true;
        });

        //This Event triggers when an entity is attacked. I check if it's a player attacking an armor stand of the same name (So you don't destroy other people's statues)
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClient()){
                return ActionResult.PASS;
            }
            //This is called when the Death Statue Entity has the same name as the attacking player
            if (entity instanceof DeathStatueEntity && getPlayerNameFromStatueName(entity.getName().getString()).equals(player.getName())) {
                //LOGGER.info("Event: Attacking Death Statue: [" + entity.getUuidAsString() + "], at: (" + entity.getBlockX() + ", " + entity.getBlockY() + ", " + entity.getBlockZ() + ")");
                ServerPlayNetworking.send((ServerPlayerEntity) player, ModMessages.DESTROY_STATUE_ID, PacketByteBufs.create());
                entity.kill();
                return ActionResult.PASS;
            }
            //This is called when the Death Statue Entity doesn't have the same name as the attacking player
            else if (entity instanceof DeathStatueEntity && !getPlayerNameFromStatueName(entity.getName().getString()).equals(player.getName())) {
                //If you're creative, you can destroy any statue
                if (player.isCreative()) {
                    entity.kill();
                    return ActionResult.PASS;
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
                //return Uuids.getOfflinePlayerUuid(matcher.group(1));
                return Text.of(matcher.group(1));
            } catch (IllegalArgumentException e) {
                // UUID parsing failed
                e.printStackTrace();
            }
        }
        return null;
    }
    //This is the method that spawns the statue entity
    public static void spawnDeathStatueEntity(PlayerEntity serverPlayer, Vec3d playerPosition) {
        BlockPos playerBlockPos = BlockPos.ofFloored(playerPosition);
        World world = serverPlayer.getWorld();
        String playerName = serverPlayer.getName().getString();
        GameProfile gameProfile = new GameProfile(UUID.randomUUID(), "Death Statue of [" + playerName + "]");

        ItemStack HELMET = serverPlayer.getInventory().getArmorStack(3);
        ItemStack BREASTPLATE = serverPlayer.getInventory().getArmorStack(2);
        ItemStack LEGGINGS = serverPlayer.getInventory().getArmorStack(1);
        ItemStack BOOTS = serverPlayer.getInventory().getArmorStack(0);
        ItemStack MAINHAND = serverPlayer.getMainHandStack();
        ItemStack OFFHAND = serverPlayer.getOffHandStack();

        DeathStatueEntity deathStatue = new DeathStatueEntity(DEATH_STATUE, world);

        deathStatue.setPosition(playerPosition);
        deathStatue.setUuid(Uuids.getUuidFromProfile(gameProfile));
        deathStatue.setCustomName(Text.of(gameProfile.getName()));
        deathStatue.setHeadYaw(serverPlayer.getHeadYaw());

        world.spawnEntity(deathStatue);
        deathStatue.refreshPositionAndAngles(playerBlockPos, serverPlayer.getYaw(), serverPlayer.getPitch());

        deathStatue.equipStack(EquipmentSlot.HEAD, HELMET);
        deathStatue.equipStack(EquipmentSlot.CHEST, BREASTPLATE);
        deathStatue.equipStack(EquipmentSlot.LEGS, LEGGINGS);
        deathStatue.equipStack(EquipmentSlot.FEET, BOOTS);
        deathStatue.equipStack(EquipmentSlot.MAINHAND, MAINHAND);
        deathStatue.equipStack(EquipmentSlot.OFFHAND, OFFHAND);

        String statueLocation = deathStatue.getBlockX() + ", " + deathStatue.getBlockY() + ", " + deathStatue.getBlockZ();
        LOGGER.info("SPAWNED DEATH STATUE: [" + deathStatue.getUuidAsString() + "] at: (" + statueLocation + ")");
    }
    //This is where I will grab the player's skin texture for the statue block model
    public static void receivedStatueClient(ServerPlayNetworkHandler handler) {
        hasStatuesClient = true;
        //handler.getPlayer().sendMessage(Text.of("Has Statue Client: " + hasStatuesClient));
    }
}
