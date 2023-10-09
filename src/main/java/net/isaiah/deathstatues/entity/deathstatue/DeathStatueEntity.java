package net.isaiah.deathstatues.entity.deathstatue;

import com.google.common.collect.Lists;
import net.isaiah.deathstatues.block.statue.DeathStatueBaseBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;


public class DeathStatueEntity extends LivingEntity {
    private final DeathStatueInventory inventory = new DeathStatueInventory(this);
    private static ServerPlayerEntity currentPlayer;
    private static boolean receivedCurrentPlayer = false;
    public static final TrackedData<Byte> PLAYER_MODEL_PARTS = DataTracker.registerData(DeathStatueEntity.class, TrackedDataHandlerRegistry.BYTE);
    protected Vec3d lastVelocity = Vec3d.ZERO;
    @Nullable
    private PlayerListEntry playerListEntry;
    private static Identifier skinTexture;

    public DeathStatueEntity(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
        //this.currentPlayer = MinecraftClient.getInstance().player;
    }

    public static void receivedCurrentPlayer(ServerPlayerEntity player, PacketByteBuf buf) {
        //currentPlayer = player;
        if (player != null) {
            receivedCurrentPlayer = true;
            setCurrentPlayer(player);
        }
        //player.sendMessage(Text.of("Receiving player packet for: " + currentPlayer.getName().getString()));
        //currentPlayer.setUuid(buf.readUuid());
    }

    public static void setCurrentPlayer(ServerPlayerEntity player) {
        if (receivedCurrentPlayer) {
            currentPlayer = player;
        }
    }

    public static UUID getCurrentPlayerUUID() {
        return currentPlayer.getUuid();
    }

    @Override
    public UUID getUuid() {
        return super.getUuid();
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(PLAYER_MODEL_PARTS, (byte)0);
    }

    public static DefaultAttributeContainer.Builder createStatueAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 1.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.1f)
                .add(EntityAttributes.GENERIC_ATTACK_SPEED)
                .add(EntityAttributes.GENERIC_LUCK)
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 20);
    }

    public static void updateSkinTexture(Identifier skinTexturePath) {
        if (skinTexturePath != null) {
            DeathStatueEntity.skinTexture = skinTexturePath;
        }
    }

    @Nullable
    protected PlayerListEntry getPlayerListEntry() {
        if (this.playerListEntry == null) {
            this.playerListEntry = Objects.requireNonNull(MinecraftClient.getInstance().getNetworkHandler()).getPlayerListEntry(DeathStatueEntity.UUID_KEY);
        }
        return this.playerListEntry;
    }
    public Identifier getSkinTexture() {
        PlayerListEntry playerListEntry = this.getPlayerListEntry();
        if (playerListEntry == null) {
            assert MinecraftClient.getInstance().player != null;
            return DefaultSkinHelper.getTexture(MinecraftClient.getInstance().player.getUuid());
        } else {
            return playerListEntry.getSkinTexture();
        }
    }

    @Override
    public Iterable<ItemStack> getArmorItems() {
        return this.inventory.armor;
    }

    @Override
    public Iterable<ItemStack> getHandItems() {
        return Lists.newArrayList(this.inventory.getMainHandStack(), this.getOffHandStack());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        NbtList nbtList = nbt.getList("Inventory", NbtElement.COMPOUND_TYPE);
        this.inventory.readNbt(nbtList);
        this.inventory.selectedSlot = nbt.getInt("SelectedItemSlot");
        }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        NbtHelper.putDataVersion(nbt);
        nbt.put("Inventory", this.inventory.writeNbt(new NbtList()));
        nbt.putInt("SelectedItemSlot", this.inventory.selectedSlot);
    }

    @Override
    public ItemStack getEquippedStack(EquipmentSlot slot) {
        if (slot == EquipmentSlot.MAINHAND) {
            return this.inventory.getMainHandStack();
        }
        if (slot == EquipmentSlot.OFFHAND) {
            return this.inventory.offHand.get(0);
        }
        if (slot.getType() == EquipmentSlot.Type.ARMOR) {
            return this.inventory.armor.get(slot.getEntitySlotId());
        }
        return ItemStack.EMPTY;
    }

    public Scoreboard getScoreboard() {
        return this.getWorld().getScoreboard();
    }

    /*public boolean isPartVisible(PlayerModelPart modelPart) {
        return (this.getDataTracker().get(PLAYER_MODEL_PARTS) & modelPart.getBitFlag()) == modelPart.getBitFlag();
    }*/

    @Override
    public void equipStack(EquipmentSlot slot, ItemStack stack) {
        this.processEquippedStack(stack);
        if (slot == EquipmentSlot.MAINHAND) {
            this.onEquipStack(slot, this.inventory.main.set(this.inventory.selectedSlot, stack), stack);
        } else if (slot == EquipmentSlot.OFFHAND) {
            this.onEquipStack(slot, this.inventory.offHand.set(0, stack), stack);
        } else if (slot.getType() == EquipmentSlot.Type.ARMOR) {
            this.onEquipStack(slot, this.inventory.armor.set(slot.getEntitySlotId(), stack), stack);
        }
    }

    @Override
    public Arm getMainArm() {
        return Arm.RIGHT;
    }

    public Vec3d lerpVelocity(float tickDelta) {
        return this.lastVelocity.lerp(this.getVelocity(), tickDelta);
    }

    /*public Identifier getSkinTexture() {
        return this.skinTexture;
    }*/

    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        BlockPos bottomPos = this.getBlockPos();
        BlockState bottomBlockState = player.getWorld().getBlockState(bottomPos);
        Block bottomBlock = bottomBlockState.getBlock();


        if (bottomBlock instanceof DeathStatueBaseBlock) {
            return bottomBlock.onUse(bottomBlockState, player.getWorld(), bottomPos, player, hand, new BlockHitResult(this.getPos(), this.getHorizontalFacing(), this.getBlockPos(), false));
        }
        return super.interact(player, hand);
    }
}
