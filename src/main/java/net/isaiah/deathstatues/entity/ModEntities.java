package net.isaiah.deathstatues.entity;

import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.isaiah.deathstatues.DeathStatues;
import net.isaiah.deathstatues.entity.deathstatue.DeathStatueEntity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.UUID;

public class ModEntities {
    private static final Identifier DEATH_STATUE_ENTITY_ID = new Identifier("deathstatues", "death_statue_entity");
    public static final EntityType<DeathStatueEntity> DEATH_STATUE = Registry.register(Registries.ENTITY_TYPE, DEATH_STATUE_ENTITY_ID,
            FabricEntityTypeBuilder.create(SpawnGroup.MISC, DeathStatueEntity::new)
                    .dimensions(EntityDimensions.fixed(0.6F, 1.8F)).build());

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


        if (!serverPlayer.isCreative()) {
            serverPlayer.getInventory().removeOne(HELMET);
            serverPlayer.getInventory().removeOne(BREASTPLATE);
            serverPlayer.getInventory().removeOne(LEGGINGS);
            serverPlayer.getInventory().removeOne(BOOTS);
            serverPlayer.getInventory().removeOne(MAINHAND);
            serverPlayer.getInventory().removeOne(OFFHAND);
        }

        String statueLocation = deathStatue.getBlockX() + ", " + deathStatue.getBlockY() + ", " + deathStatue.getBlockZ();
        DeathStatues.LOGGER.info("SPAWNED DEATH STATUE: [" + deathStatue.getUuidAsString() + "] at: (" + statueLocation + ")");
    }

    public static void spawnFakeDeathStatueEntities(PlayerEntity serverPlayer, Vec3d playerPosition) {
        BlockPos playerBlockPos = BlockPos.ofFloored(playerPosition);
        World world = serverPlayer.getWorld();
        String playerName = serverPlayer.getName().getString();
        GameProfile gameProfile = new GameProfile(UUID.randomUUID(), playerName);

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

        NbtCompound nbtCompound = new NbtCompound();
        nbtCompound.putBoolean("Fake", true);
        deathStatue.readCustomDataFromNbt(nbtCompound);

        world.spawnEntity(deathStatue);
        System.out.println("Player Yaw: " + serverPlayer.getYaw());
        if ((-45 <= serverPlayer.getYaw() && serverPlayer.getYaw() <= 45) || (135 <= serverPlayer.getYaw() ^ serverPlayer.getYaw() <= -135)) {
            deathStatue.updatePosition(1, 0, 0);
            deathStatue.refreshPositionAndAngles(playerBlockPos.add(1, 0, 0), serverPlayer.getYaw(), serverPlayer.getPitch());
        }
        else if ((45 < serverPlayer.getYaw() && serverPlayer.getYaw() < 135) || (-135 < serverPlayer.getYaw() && serverPlayer.getYaw() < -45)) {
            deathStatue.updatePosition(0, 0, 1);
            deathStatue.refreshPositionAndAngles(playerBlockPos.add(0, 0, 1), serverPlayer.getYaw(), serverPlayer.getPitch());
        }

        deathStatue.equipStack(EquipmentSlot.HEAD, HELMET);
        deathStatue.equipStack(EquipmentSlot.CHEST, BREASTPLATE);
        deathStatue.equipStack(EquipmentSlot.LEGS, LEGGINGS);
        deathStatue.equipStack(EquipmentSlot.FEET, BOOTS);
        deathStatue.equipStack(EquipmentSlot.MAINHAND, MAINHAND);
        deathStatue.equipStack(EquipmentSlot.OFFHAND, OFFHAND);

        String statueLocation = deathStatue.getBlockX() + ", " + deathStatue.getBlockY() + ", " + deathStatue.getBlockZ();
        DeathStatues.LOGGER.info("SPAWNED FAKE DEATH STATUE 1: [" + deathStatue.getUuidAsString() + "] at: (" + statueLocation + ")");

        //Second statue
        DeathStatueEntity deathStatue2 = new DeathStatueEntity(DEATH_STATUE, world);
        GameProfile gameProfile2 = new GameProfile(UUID.randomUUID(), playerName);

        deathStatue2.setPosition(playerPosition);
        deathStatue2.setUuid(Uuids.getUuidFromProfile(gameProfile2));
        deathStatue2.setCustomName(Text.of(gameProfile2.getName()));
        deathStatue2.setHeadYaw(serverPlayer.getHeadYaw());
        deathStatue2.readCustomDataFromNbt(nbtCompound);

        world.spawnEntity(deathStatue2);
        if ((-45 <= serverPlayer.getYaw() && serverPlayer.getYaw() <= 45) || (135 <= serverPlayer.getYaw() ^ serverPlayer.getYaw() <= -135)) {
            deathStatue2.updatePosition(-1, 0, 0);
            deathStatue2.refreshPositionAndAngles(playerBlockPos.add(-1, 0, 0), serverPlayer.getYaw(), serverPlayer.getPitch());
        }
        else if ((45 < serverPlayer.getYaw() && serverPlayer.getYaw() < 135) || (-135 < serverPlayer.getYaw() && serverPlayer.getYaw() < -45)) {
            deathStatue2.updatePosition(0, 0, -1);
            deathStatue2.refreshPositionAndAngles(playerBlockPos.add(0, 0, -1), serverPlayer.getYaw(), serverPlayer.getPitch());
        }

        deathStatue2.equipStack(EquipmentSlot.HEAD, HELMET);
        deathStatue2.equipStack(EquipmentSlot.CHEST, BREASTPLATE);
        deathStatue2.equipStack(EquipmentSlot.LEGS, LEGGINGS);
        deathStatue2.equipStack(EquipmentSlot.FEET, BOOTS);
        deathStatue2.equipStack(EquipmentSlot.MAINHAND, MAINHAND);
        deathStatue2.equipStack(EquipmentSlot.OFFHAND, OFFHAND);

        String statueLocation2 = deathStatue2.getBlockX() + ", " + deathStatue2.getBlockY() + ", " + deathStatue2.getBlockZ();
        DeathStatues.LOGGER.info("SPAWNED FAKE DEATH STATUE 2: [" + deathStatue2.getUuidAsString() + "] at: (" + statueLocation2 + ")");

        //Third statue
        DeathStatueEntity deathStatue3 = new DeathStatueEntity(DEATH_STATUE, world);
        GameProfile gameProfile3 = new GameProfile(UUID.randomUUID(), playerName);

        deathStatue3.setPosition(playerPosition);
        deathStatue3.setUuid(Uuids.getUuidFromProfile(gameProfile3));
        deathStatue3.setCustomName(Text.of(gameProfile3.getName()));
        deathStatue3.setHeadYaw(serverPlayer.getHeadYaw());
        deathStatue3.readCustomDataFromNbt(nbtCompound);

        world.spawnEntity(deathStatue3);
        if (-45 <= serverPlayer.getYaw() && serverPlayer.getYaw() <= 45) {
            deathStatue3.updatePosition(2, 0, 1);
            deathStatue3.refreshPositionAndAngles(playerBlockPos.add(2, 0, 1), serverPlayer.getYaw(), serverPlayer.getPitch());
        }
        else if ((135 <= serverPlayer.getYaw() && serverPlayer.getYaw() <= 180) ^ (-180 <= serverPlayer.getYaw() && serverPlayer.getYaw() <= -135)) {
            deathStatue3.updatePosition(2, 0, -1);
            deathStatue3.refreshPositionAndAngles(playerBlockPos.add(2, 0, -1), serverPlayer.getYaw(), serverPlayer.getPitch());
        }
        else if (45 < serverPlayer.getYaw() && serverPlayer.getYaw() < 135) {
            deathStatue3.updatePosition(-1, 0, -2);
            deathStatue3.refreshPositionAndAngles(playerBlockPos.add(-1, 0, -2), serverPlayer.getYaw(), serverPlayer.getPitch());
        }
        else if (-135 < serverPlayer.getYaw() && serverPlayer.getYaw() < -45) {
            deathStatue3.updatePosition(1, 0, -2);
            deathStatue3.refreshPositionAndAngles(playerBlockPos.add(1, 0, -2), serverPlayer.getYaw(), serverPlayer.getPitch());
        }

        deathStatue3.equipStack(EquipmentSlot.HEAD, HELMET);
        deathStatue3.equipStack(EquipmentSlot.CHEST, BREASTPLATE);
        deathStatue3.equipStack(EquipmentSlot.LEGS, LEGGINGS);
        deathStatue3.equipStack(EquipmentSlot.FEET, BOOTS);
        deathStatue3.equipStack(EquipmentSlot.MAINHAND, MAINHAND);
        deathStatue3.equipStack(EquipmentSlot.OFFHAND, OFFHAND);

        String statueLocation3 = deathStatue3.getBlockX() + ", " + deathStatue3.getBlockY() + ", " + deathStatue3.getBlockZ();
        DeathStatues.LOGGER.info("SPAWNED FAKE DEATH STATUE 3: [" + deathStatue3.getUuidAsString() + "] at: (" + statueLocation3 + ")");

        //Fourth statue
        DeathStatueEntity deathStatue4 = new DeathStatueEntity(DEATH_STATUE, world);
        GameProfile gameProfile4 = new GameProfile(UUID.randomUUID(), playerName);

        deathStatue4.setPosition(playerPosition);
        deathStatue4.setUuid(Uuids.getUuidFromProfile(gameProfile4));
        deathStatue4.setCustomName(Text.of(gameProfile4.getName()));
        deathStatue4.setHeadYaw(serverPlayer.getHeadYaw());
        deathStatue4.readCustomDataFromNbt(nbtCompound);

        world.spawnEntity(deathStatue4);
        if (-45 <= serverPlayer.getYaw() && serverPlayer.getYaw() <= 45) {
            deathStatue4.updatePosition(-2, 0, 1);
            deathStatue4.refreshPositionAndAngles(playerBlockPos.add(-2, 0, 1), serverPlayer.getYaw(), serverPlayer.getPitch());
        }
        else if ((135 <= serverPlayer.getYaw() && serverPlayer.getYaw() <= 180) ^ (-180 <= serverPlayer.getYaw() && serverPlayer.getYaw() <= -135)) {
            deathStatue4.updatePosition(-2, 0, -1);
            deathStatue4.refreshPositionAndAngles(playerBlockPos.add(-2, 0, -1), serverPlayer.getYaw(), serverPlayer.getPitch());
        }
        else if (45 < serverPlayer.getYaw() && serverPlayer.getYaw() < 135) {
            deathStatue4.updatePosition(-1, 0, 2);
            deathStatue4.refreshPositionAndAngles(playerBlockPos.add(-1, 0, 2), serverPlayer.getYaw(), serverPlayer.getPitch());
        }
        else if (-135 < serverPlayer.getYaw() && serverPlayer.getYaw() < -45) {
            deathStatue4.updatePosition(1, 0, 2);
            deathStatue4.refreshPositionAndAngles(playerBlockPos.add(1, 0, 2), serverPlayer.getYaw(), serverPlayer.getPitch());
        }

        deathStatue4.equipStack(EquipmentSlot.HEAD, HELMET);
        deathStatue4.equipStack(EquipmentSlot.CHEST, BREASTPLATE);
        deathStatue4.equipStack(EquipmentSlot.LEGS, LEGGINGS);
        deathStatue4.equipStack(EquipmentSlot.FEET, BOOTS);
        deathStatue4.equipStack(EquipmentSlot.MAINHAND, MAINHAND);
        deathStatue4.equipStack(EquipmentSlot.OFFHAND, OFFHAND);

        String statueLocation4 = deathStatue4.getBlockX() + ", " + deathStatue4.getBlockY() + ", " + deathStatue4.getBlockZ();
        DeathStatues.LOGGER.info("SPAWNED FAKE DEATH STATUE 4: [" + deathStatue4.getUuidAsString() + "] at: (" + statueLocation4 + ")");

        //Fifth statue
        DeathStatueEntity deathStatue5 = new DeathStatueEntity(DEATH_STATUE, world);
        GameProfile gameProfile5 = new GameProfile(UUID.randomUUID(), playerName);

        deathStatue5.setPosition(playerPosition);
        deathStatue5.setUuid(Uuids.getUuidFromProfile(gameProfile5));
        deathStatue5.setCustomName(Text.of(gameProfile5.getName()));
        deathStatue5.setHeadYaw(serverPlayer.getHeadYaw());
        deathStatue5.readCustomDataFromNbt(nbtCompound);

        world.spawnEntity(deathStatue5);
        //South
        if (-45 <= serverPlayer.getYaw() && serverPlayer.getYaw() <= 45) {
            deathStatue5.updatePosition(2, 0, 2);
            deathStatue5.refreshPositionAndAngles(playerBlockPos.add(2, 0, 2), serverPlayer.getYaw(), serverPlayer.getPitch());
        }
        //North
        else if ((135 <= serverPlayer.getYaw() && serverPlayer.getYaw() <= 180) ^ (-180 <= serverPlayer.getYaw() && serverPlayer.getYaw() <= -135)) {
            deathStatue5.updatePosition(2, 0, -2);
            deathStatue5.refreshPositionAndAngles(playerBlockPos.add(2, 0, -2), serverPlayer.getYaw(), serverPlayer.getPitch());
        }
        //West
        else if (45 < serverPlayer.getYaw() && serverPlayer.getYaw() < 135) {
            deathStatue5.updatePosition(-2, 0, 2);
            deathStatue5.refreshPositionAndAngles(playerBlockPos.add(-2, 0, 2), serverPlayer.getYaw(), serverPlayer.getPitch());
        }
        //East
        else if (-135 < serverPlayer.getYaw() && serverPlayer.getYaw() < -45) {
            deathStatue5.updatePosition(2, 0, -2);
            deathStatue5.refreshPositionAndAngles(playerBlockPos.add(2, 0, -2), serverPlayer.getYaw(), serverPlayer.getPitch());
        }

        deathStatue5.equipStack(EquipmentSlot.HEAD, HELMET);
        deathStatue5.equipStack(EquipmentSlot.CHEST, BREASTPLATE);
        deathStatue5.equipStack(EquipmentSlot.LEGS, LEGGINGS);
        deathStatue5.equipStack(EquipmentSlot.FEET, BOOTS);
        deathStatue5.equipStack(EquipmentSlot.MAINHAND, MAINHAND);
        deathStatue5.equipStack(EquipmentSlot.OFFHAND, OFFHAND);

        String statueLocation5 = deathStatue5.getBlockX() + ", " + deathStatue5.getBlockY() + ", " + deathStatue5.getBlockZ();
        DeathStatues.LOGGER.info("SPAWNED FAKE DEATH STATUE 5: [" + deathStatue5.getUuidAsString() + "] at: (" + statueLocation5 + ")");

        //Sixth statue
        DeathStatueEntity deathStatue6 = new DeathStatueEntity(DEATH_STATUE, world);
        GameProfile gameProfile6 = new GameProfile(UUID.randomUUID(), playerName);

        deathStatue6.setPosition(playerPosition);
        deathStatue6.setUuid(Uuids.getUuidFromProfile(gameProfile6));
        deathStatue6.setCustomName(Text.of(gameProfile6.getName()));
        deathStatue6.setHeadYaw(serverPlayer.getHeadYaw());
        deathStatue6.readCustomDataFromNbt(nbtCompound);

        world.spawnEntity(deathStatue6);
        //South
        if (-45 <= serverPlayer.getYaw() && serverPlayer.getYaw() <= 45) {
            deathStatue6.updatePosition(-2, 0, 2);
            deathStatue6.refreshPositionAndAngles(playerBlockPos.add(-2, 0, 2), serverPlayer.getYaw(), serverPlayer.getPitch());
        }
        //North
        else if ((135 <= serverPlayer.getYaw() && serverPlayer.getYaw() <= 180) ^ (-180 <= serverPlayer.getYaw() && serverPlayer.getYaw() <= -135)) {
            deathStatue6.updatePosition(-2, 0, -2);
            deathStatue6.refreshPositionAndAngles(playerBlockPos.add(-2, 0, -2), serverPlayer.getYaw(), serverPlayer.getPitch());
        }
        //West
        else if (45 < serverPlayer.getYaw() && serverPlayer.getYaw() < 135) {
            deathStatue6.updatePosition(-2, 0, -2);
            deathStatue6.refreshPositionAndAngles(playerBlockPos.add(-2, 0, -2), serverPlayer.getYaw(), serverPlayer.getPitch());
        }
        //East
        else if (-135 < serverPlayer.getYaw() && serverPlayer.getYaw() < -45) {
            deathStatue6.updatePosition(2, 0, 2);
            deathStatue6.refreshPositionAndAngles(playerBlockPos.add(2, 0, 2), serverPlayer.getYaw(), serverPlayer.getPitch());
        }

        deathStatue6.equipStack(EquipmentSlot.HEAD, HELMET);
        deathStatue6.equipStack(EquipmentSlot.CHEST, BREASTPLATE);
        deathStatue6.equipStack(EquipmentSlot.LEGS, LEGGINGS);
        deathStatue6.equipStack(EquipmentSlot.FEET, BOOTS);
        deathStatue6.equipStack(EquipmentSlot.MAINHAND, MAINHAND);
        deathStatue6.equipStack(EquipmentSlot.OFFHAND, OFFHAND);

        String statueLocation6 = deathStatue6.getBlockX() + ", " + deathStatue6.getBlockY() + ", " + deathStatue6.getBlockZ();
        DeathStatues.LOGGER.info("SPAWNED FAKE DEATH STATUE 6: [" + deathStatue6.getUuidAsString() + "] at: (" + statueLocation6 + ")");

        //Seventh statue
        DeathStatueEntity deathStatue7 = new DeathStatueEntity(DEATH_STATUE, world);
        GameProfile gameProfile7 = new GameProfile(UUID.randomUUID(), playerName);

        deathStatue7.setPosition(playerPosition);
        deathStatue7.setUuid(Uuids.getUuidFromProfile(gameProfile7));
        deathStatue7.setCustomName(Text.of(gameProfile7.getName()));
        deathStatue7.setHeadYaw(serverPlayer.getHeadYaw());
        deathStatue7.readCustomDataFromNbt(nbtCompound);

        world.spawnEntity(deathStatue7);
        deathStatue7.refreshPositionAndAngles(playerBlockPos, serverPlayer.getYaw(), serverPlayer.getPitch());

        deathStatue7.equipStack(EquipmentSlot.HEAD, HELMET);
        deathStatue7.equipStack(EquipmentSlot.CHEST, BREASTPLATE);
        deathStatue7.equipStack(EquipmentSlot.LEGS, LEGGINGS);
        deathStatue7.equipStack(EquipmentSlot.FEET, BOOTS);
        deathStatue7.equipStack(EquipmentSlot.MAINHAND, MAINHAND);
        deathStatue7.equipStack(EquipmentSlot.OFFHAND, OFFHAND);

        String statueLocation7 = deathStatue7.getBlockX() + ", " + deathStatue7.getBlockY() + ", " + deathStatue7.getBlockZ();
        DeathStatues.LOGGER.info("SPAWNED FAKE DEATH STATUE 7: [" + deathStatue7.getUuidAsString() + "] at: (" + statueLocation7 + ")");
    }
}
