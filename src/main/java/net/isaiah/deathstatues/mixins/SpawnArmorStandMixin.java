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
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
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
public abstract class SpawnArmorStandMixin extends PlayerEntity {
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
        String statueLocation = armorStand.getBlockX() + ", " + armorStand.getBlockY() + ", " + armorStand.getBlockZ();
        LOGGER.info("SPAWNED ARMOR STAND: " + armorStand.getUuidAsString() + " at: " + statueLocation);
        DeathStatuesToast.add(MinecraftClient.getInstance().getToastManager(), DeathStatuesToast.Type.PERIODIC_NOTIFICATION, Text.translatable("deathstatues.toast.title"), Text.translatable("deathstatues.toast.spawned").append(statueLocation).formatted(Formatting.DARK_PURPLE).append("§A)"));
        MutableText tooltipText = Text.translatable("deathstatues.toast.spawned");
        MutableText message = Text.translatable(statueLocation).formatted(Formatting.GOLD).setStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("Click to get Location in chat!"))).withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tp @s " + statueLocation.replace(",", ""))).withFormatting(Formatting.DARK_PURPLE));
        tooltipText.append(message).append("§A)");
        MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(tooltipText);
    }

    @Inject(at = @At("HEAD"), method = "attack")
    private void stopItemDrops(Entity target, CallbackInfo ci){
        if (target instanceof ArmorStandEntity && target.getName().equals(this.getName())){
            target.kill();
            MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.translatable("deathstatues.toast.destroyed").formatted(Formatting.RED));
            LOGGER.info("Attacking Armor Stand: " + target.getUuidAsString() + target.getBlockX() + ", " + target.getBlockY() + ", " + target.getBlockZ());
            DeathStatuesToast.add(MinecraftClient.getInstance().getToastManager(), DeathStatuesToast.Type.PERIODIC_NOTIFICATION, Text.translatable("deathstatues.toast.title"), Text.translatable("deathstatues.toast.destroyed"));
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
