package net.isaiah.deathstatues;

import com.mojang.authlib.GameProfile;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.isaiah.deathstatues.entity.deathstatue.DeathStatueEntity;
import net.isaiah.deathstatues.networking.DeathStatuesMessages;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.decoration.ArmorStandEntity;
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
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class DeathStatues implements ModInitializer {
    public static final String MOD_ID = "death-statues";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static boolean hasStatuesClient = false;
    private static final Identifier DEATH_STATUE_ENTITY_ID = new Identifier("deathstatues", "death_statue_entity");
    public static final EntityType<DeathStatueEntity> DEATH_STATUE = Registry.register(Registries.ENTITY_TYPE, DEATH_STATUE_ENTITY_ID, FabricEntityTypeBuilder.create(SpawnGroup.MISC, DeathStatueEntity::new).dimensions(EntityDimensions.fixed(0.6F, 1.8F)).build());

    @Override
    public void onInitialize() {
        //Register Client-To-Server Packets
        DeathStatuesMessages.registerC2SPackets();

        FabricDefaultAttributeRegistry.register(DEATH_STATUE, DeathStatueEntity.createStatueAttributes());

        //This Event triggers when an entity dies and I check if it's a player then spawn the statue
        ServerLivingEntityEvents.ALLOW_DEATH.register((entity, damageSource, damageAmount) -> {
            if (entity instanceof PlayerEntity player){
                //LOGGER.info("Event: Player (" + player.getName().getString() + "): [" + player.getUuidAsString() + "] Died");
                ServerPlayNetworking.send((ServerPlayerEntity) player, DeathStatuesMessages.PLAYER_DIED_ID, PacketByteBufs.create());
                //spawnDeathStatue(player); // Old method that spawned armor stand
                spawnPlayerDeathStatue(player);
            }
            return true;
        });

        //This Event triggers when an entity is attacked. I check if it's a player attacking an armor stand of the same name (So you don't destroy other people's statues)
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClient()){
                return ActionResult.PASS;
            }
            if (entity instanceof ArmorStandEntity && entity.getName().equals(player.getName())) {
                //LOGGER.info("Event: Attacking Armor Stand: [" + entity.getUuidAsString() + "], at: (" + entity.getBlockX() + ", " + entity.getBlockY() + ", " + entity.getBlockZ() + ")");
                ServerPlayNetworking.send((ServerPlayerEntity) player, DeathStatuesMessages.DESTROY_STATUE_ID, PacketByteBufs.create());
                entity.kill();
                return ActionResult.PASS;
            }

            return ActionResult.PASS;
        });
    }
    // I will be replacing this method as it is old and spawned an armor stand with gear on instead of my custom player/statue
    /*public static void spawnDeathStatue(PlayerEntity serverPlayer) {
        Vec3d playerPosition = serverPlayer.getPos();
        World world = serverPlayer.getWorld();

        ItemStack HELMET;
        ItemStack BREASTPLATE = serverPlayer.getInventory().getArmorStack(2);
        ItemStack LEGGINGS = serverPlayer.getInventory().getArmorStack(1);
        ItemStack BOOTS = serverPlayer.getInventory().getArmorStack(0);
        ItemStack MAINHAND = serverPlayer.getMainHandStack();
        ItemStack OFFHAND = serverPlayer.getOffHandStack();

        if (serverPlayer.getInventory().getArmorStack(3).isEmpty()) {
            HELMET = new ItemStack(Items.PLAYER_HEAD);
            GameProfile gameProfile = serverPlayer.getGameProfile();
            HELMET.getOrCreateNbt().put("SkullOwner", NbtHelper.writeGameProfile(new NbtCompound(), gameProfile));
        }
        else {
            HELMET = serverPlayer.getInventory().getArmorStack(3);
        }

        ArmorStandEntity armorStand = new ArmorStandEntity(EntityType.ARMOR_STAND, world);

        armorStand.setShowArms(true);
        armorStand.setInvulnerable(true);
        //armorStand.setNoGravity(true);
        armorStand.setPosition(playerPosition);
        armorStand.setCustomNameVisible(true);
        armorStand.setCustomName(serverPlayer.getName());

        world.spawnEntity(armorStand);
        armorStand.refreshPositionAndAngles(serverPlayer.getBlockPos(), serverPlayer.getYaw(), serverPlayer.getPitch());

        armorStand.equipStack(EquipmentSlot.HEAD, HELMET);
        armorStand.equipStack(EquipmentSlot.CHEST, BREASTPLATE);
        armorStand.equipStack(EquipmentSlot.LEGS, LEGGINGS);
        armorStand.equipStack(EquipmentSlot.FEET, BOOTS);
        armorStand.equipStack(EquipmentSlot.MAINHAND, MAINHAND);
        armorStand.equipStack(EquipmentSlot.OFFHAND, OFFHAND);

        //armorStand.equipStack(EquipmentSlot.HEAD, ItemStack.EMPTY);

        String statueLocation = armorStand.getBlockX() + ", " + armorStand.getBlockY() + ", " + armorStand.getBlockZ();
        LOGGER.info("SPAWNED ARMOR STAND: " + armorStand.getUuidAsString() + " at: " + statueLocation);
    }*/

    //Here is the current method that spawns the statue. Will be renamed to old method name.
    public static void spawnPlayerDeathStatue(PlayerEntity serverPlayer) {
        Vec3d playerPosition = serverPlayer.getPos();
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
        deathStatue.refreshPositionAndAngles(serverPlayer.getBlockPos(), serverPlayer.getYaw(), serverPlayer.getPitch());

        deathStatue.equipStack(EquipmentSlot.HEAD, HELMET);
        deathStatue.equipStack(EquipmentSlot.CHEST, BREASTPLATE);
        deathStatue.equipStack(EquipmentSlot.LEGS, LEGGINGS);
        deathStatue.equipStack(EquipmentSlot.FEET, BOOTS);
        deathStatue.equipStack(EquipmentSlot.MAINHAND, MAINHAND);
        deathStatue.equipStack(EquipmentSlot.OFFHAND, OFFHAND);

        String statueLocation = deathStatue.getBlockX() + ", " + deathStatue.getBlockY() + ", " + deathStatue.getBlockZ();
        LOGGER.info("SPAWNED DEATH STATUE: [" + deathStatue.getUuidAsString() + "] at: (" + statueLocation + ")");
    }

    public static void receivedStatueClient(ServerPlayNetworkHandler handler) {
        hasStatuesClient = true;
        handler.getPlayer().sendMessage(Text.of("Has Statue Client: " + hasStatuesClient));
    }
}
