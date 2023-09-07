package net.isaiah.deathstatues;

import com.mojang.authlib.GameProfile;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.isaiah.deathstatues.networking.DeathStatuesMessages;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeathStatues implements ModInitializer {
    public static final String MOD_ID = "death-statues";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        //Register Client-To-Server Packets
        DeathStatuesMessages.registerC2SPackets();

        //This Event triggers when an entity dies and I check if it's a player then spawn the statue
        ServerLivingEntityEvents.ALLOW_DEATH.register((entity, damageSource, damageAmount) -> {
            if (entity instanceof PlayerEntity player){
                //LOGGER.info("Event: Player (" + player.getName().getString() + "): [" + player.getUuidAsString() + "] Died");
                ServerPlayNetworking.send((ServerPlayerEntity) player, DeathStatuesMessages.PLAYER_DIED_ID, PacketByteBufs.create());
                spawnDeathStatue(player);
            }
            return true;
        });

        //This Event triggers when an entity is attacked. I check if it's a player attacking an armor stand of the same name (So you don't destroy other people's statues)
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (entity instanceof ArmorStandEntity && entity.getName().equals(player.getName())) {
                //LOGGER.info("Event: Attacking Armor Stand: [" + entity.getUuidAsString() + "], at: (" + entity.getBlockX() + ", " + entity.getBlockY() + ", " + entity.getBlockZ() + ")");
                ServerPlayNetworking.send((ServerPlayerEntity) player, DeathStatuesMessages.DESTROY_STATUE_ID, PacketByteBufs.create());
                entity.kill();
            }

            return ActionResult.PASS;
        });
    }
    public static void spawnDeathStatue(PlayerEntity serverPlayer) {
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
        armorStand.setNoGravity(true);
        armorStand.setPosition(playerPosition);
        armorStand.setCustomNameVisible(true);
        armorStand.setCustomName(serverPlayer.getName());

        world.spawnEntity(armorStand);
        //armorStand.refreshPositionAndAngles(serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), serverPlayer.getYaw(), serverPlayer.getPitch());

        armorStand.equipStack(EquipmentSlot.HEAD, HELMET);
        armorStand.equipStack(EquipmentSlot.CHEST, BREASTPLATE);
        armorStand.equipStack(EquipmentSlot.LEGS, LEGGINGS);
        armorStand.equipStack(EquipmentSlot.FEET, BOOTS);
        armorStand.equipStack(EquipmentSlot.MAINHAND, MAINHAND);
        armorStand.equipStack(EquipmentSlot.OFFHAND, OFFHAND);

        //armorStand.equipStack(EquipmentSlot.HEAD, ItemStack.EMPTY);

        String statueLocation = armorStand.getBlockX() + ", " + armorStand.getBlockY() + ", " + armorStand.getBlockZ();
        LOGGER.info("SPAWNED ARMOR STAND: " + armorStand.getUuidAsString() + " at: " + statueLocation);
    }
}
