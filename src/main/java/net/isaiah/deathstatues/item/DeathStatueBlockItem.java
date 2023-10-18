package net.isaiah.deathstatues.item;

import com.mojang.authlib.GameProfile;
import net.isaiah.deathstatues.DeathStatues;
import net.isaiah.deathstatues.entity.deathstatue.DeathStatueEntity;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class DeathStatueBlockItem extends Item {
    public DeathStatueBlockItem(Item.Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        ItemStack itemStack = context.getStack();
        World world = context.getWorld();
        NbtCompound nbtData;
        if (!(world instanceof ServerWorld)) {
            return ActionResult.SUCCESS;
        }

        //DeathStatues.spawnDeathStatueEntity(Objects.requireNonNull(context.getPlayer()), context.getHitPos());

        PlayerEntity player = context.getPlayer();
        Vec3d playerPosition = context.getHitPos();
        BlockPos playerBlockPos = BlockPos.ofFloored(playerPosition);
        String playerName = player.getName().getString();
        GameProfile gameProfile = new GameProfile(UUID.randomUUID(), "Death Statue of [" + playerName + "]");

        DeathStatueEntity deathStatue = new DeathStatueEntity(DeathStatues.DEATH_STATUE, world);

        deathStatue.setPosition(playerPosition);
        deathStatue.setUuid(Uuids.getUuidFromProfile(gameProfile));
        deathStatue.setCustomName(Text.of(gameProfile.getName()));
        deathStatue.setHeadYaw(player.getHeadYaw());
        if (itemStack.hasNbt()) {
            assert itemStack.getNbt() != null;
            nbtData = itemStack.getNbt();
            //System.out.println("Item NBT Data: " + nbtData);
            deathStatue.readCustomDataFromNbt(nbtData);
        }

        world.spawnEntity(deathStatue);
        deathStatue.refreshPositionAndAngles(playerBlockPos, player.getYaw(), player.getPitch());

        String statueLocation = deathStatue.getBlockX() + ", " + deathStatue.getBlockY() + ", " + deathStatue.getBlockZ();
        DeathStatues.LOGGER.info("SPAWNED DEATH STATUE: [" + deathStatue.getUuidAsString() + "] at: (" + statueLocation + ")");
        if (!player.isCreative()) {
            itemStack.decrement(1);
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        if (stack.hasNbt()) {
            assert stack.getNbt() != null;
            if (stack.getNbt().contains("Inventory")) {
                tooltip.addAll(List.of(
                        Text.translatable("item.deathstatues.death_statue_item.tooltip.1"),
                        Text.translatable("item.deathstatues.death_statue_item.tooltip.2")));
            }
        }
        if (!Screen.hasShiftDown()) {
            tooltip.add(Text.translatable("item.deathstatues.death_statue_item.tooltip.hint"));
        }
        else {
            tooltip.addAll(List.of(
                    Text.translatable("item.deathstatues.death_statue_item.tooltip.shift.1"),
                    Text.translatable("item.deathstatues.death_statue_item.tooltip.shift.2")));
        }
    }
}
