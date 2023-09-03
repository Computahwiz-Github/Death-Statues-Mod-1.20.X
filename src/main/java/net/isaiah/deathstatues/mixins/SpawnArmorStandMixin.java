package net.isaiah.deathstatues.mixins;

import com.mojang.authlib.GameProfile;
import net.isaiah.deathstatues.DeathStatuesToast;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)

public abstract class SpawnArmorStandMixin extends PlayerEntity{
    @Shadow @Final private static Logger LOGGER;

    public SpawnArmorStandMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Inject(at = @At("TAIL"), method = "onDeath")
    private void spawnArmorStand(DamageSource damageSource, CallbackInfo info){
        Logger LOGGER = LoggerFactory.getLogger("deathstatues");
        LOGGER.info("Died");

        Vec3d playerPosition = this.getPos();
        World world = this.getWorld();

        ItemStack HELMET;
        ItemStack BREASTPLATE = this.getInventory().getArmorStack(2);
        ItemStack LEGGINGS = this.getInventory().getArmorStack(1);
        ItemStack BOOTS = this.getInventory().getArmorStack(0);
        ItemStack MAINHAND = this.getMainHandStack();
        ItemStack OFFHAND = this.getOffHandStack();

        if (this.getInventory().getArmorStack(3).isEmpty()){
            HELMET = new ItemStack(Items.PLAYER_HEAD);
            GameProfile gameProfile = this.getGameProfile();
            HELMET.getOrCreateNbt().put("SkullOwner", NbtHelper.writeGameProfile(new NbtCompound(), gameProfile));
        }
        else{
            HELMET = this.getInventory().getArmorStack(3);
        }

        ArmorStandEntity armorStand = new ArmorStandEntity(EntityType.ARMOR_STAND, world);
        armorStand.setShowArms(true);
        armorStand.setInvulnerable(true);
        armorStand.setPosition(playerPosition);

        armorStand.equipStack(EquipmentSlot.HEAD, HELMET);
        armorStand.equipStack(EquipmentSlot.CHEST, BREASTPLATE);
        armorStand.equipStack(EquipmentSlot.LEGS, LEGGINGS);
        armorStand.equipStack(EquipmentSlot.FEET, BOOTS);
        armorStand.equipStack(EquipmentSlot.MAINHAND, MAINHAND);
        armorStand.equipStack(EquipmentSlot.OFFHAND, OFFHAND);

        //armorStand.equipStack(EquipmentSlot.HEAD, ItemStack.EMPTY);

        armorStand.setCustomNameVisible(true);
        armorStand.setCustomName(this.getName());

        world.spawnEntity(armorStand);
        LOGGER.info("SPAWNED ARMOR STAND");

        DeathStatuesToast.addSpawnedStatueToast(MinecraftClient.getInstance());
    }

    @Inject(at = @At("HEAD"), method = "attack")
    private void stopItemDrops(Entity target, CallbackInfo ci){
        MinecraftClient mc = MinecraftClient.getInstance();
        if (target instanceof ArmorStandEntity && target.getName().equals(this.getName()) && mc.player!=null){
            target.kill();
            mc.player.sendMessage(Text.of("Your Death Statue has been destroyed"));
            LOGGER.info("Attacking Armor Stand");
            DeathStatuesToast.addDestroyedStatueToast(mc);
        }
    }

    @Override
    public boolean isSpectator() {
        return false;
    }

    @Override
    public boolean isCreative() {
        return false;
    }
}
