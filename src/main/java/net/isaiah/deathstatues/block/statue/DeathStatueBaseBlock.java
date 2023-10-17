package net.isaiah.deathstatues.block.statue;

import net.isaiah.deathstatues.DeathStatues;
import net.isaiah.deathstatues.block.ModBlocks;
import net.isaiah.deathstatues.block.entity.DeathStatueBlockEntity;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.*;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class DeathStatueBaseBlock extends BlockWithEntity implements BlockEntityProvider {
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
    private static final VoxelShape SHAPE = Block.createCuboidShape(0, 0, 0, 16, 16, 16);
    private static boolean BASE_PLACES_BLOCK = false;

    private static boolean BASE_PLACES_ENTITY = false;

    public DeathStatueBaseBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getDefaultState().with(FACING, Direction.NORTH));
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return super.getPlacementState(ctx).with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }
    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        if (world.getBlockState(pos.up()).isAir()) {
            //Spawns player model block if the config option for placing the block is true and placing the entity is false
            if (BASE_PLACES_BLOCK && !BASE_PLACES_ENTITY) {
                world.setBlockState(pos.up(), ModBlocks.DEATH_STATUE_BLOCK.getDefaultState().with(FACING, state.get(FACING)).with(DeathStatueBlock.HALF, DoubleBlockHalf.LOWER));
                world.setBlockState(pos.up().add(0, 1, 0), ModBlocks.DEATH_STATUE_BLOCK.getDefaultState().with(FACING, state.get(FACING)).with(DeathStatueBlock.HALF, DoubleBlockHalf.UPPER));
                world.playSound(null, pos, SoundEvents.BLOCK_AMETHYST_BLOCK_BREAK, SoundCategory.BLOCKS, 0.5f, world.random.nextFloat() * 0.1f + 0.9f);
            }
            //Spawns player model entity if the config option for placing the block is false and placing the entity is true
            else if (BASE_PLACES_ENTITY && !BASE_PLACES_BLOCK) {
                assert placer != null;
                if (placer.isHolding(ModBlocks.DEATH_STATUE_BASE_BLOCK.asItem())) {
                    int currentSlot = ((PlayerEntity) placer).getInventory().selectedSlot;
                    switchToWeaponSlot((PlayerEntity) placer);
                    DeathStatues.spawnDeathStatueEntity((PlayerEntity) placer, pos.up().toCenterPos());
                    ((PlayerEntity) placer).getInventory().selectedSlot = currentSlot;
                }
            }
        }
        super.onPlaced(world, pos, state, placer, itemStack);
    }

    public static void determineBasePlacesEntityBasedOnConfig(PacketByteBuf buf) {
        BASE_PLACES_ENTITY = buf.readBoolean();
    }

    public static void determineBasePlacesBlockBasedOnConfig(PacketByteBuf buf) {
        BASE_PLACES_BLOCK = buf.readBoolean();
    }

    public void switchToWeaponSlot(PlayerEntity player) {
        for (int i = 0; i < PlayerInventory.getHotbarSize(); i++) {
            ItemStack stackInSlot = player.getInventory().getStack(i);
            Item item = stackInSlot.getItem();
            if (item instanceof SwordItem) {
                player.getInventory().selectedSlot = i;
                return;
            }
            else if (item instanceof AxeItem) {
                player.getInventory().selectedSlot = i;
                return;
            }
        }
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (world.getBlockState(pos.up()).isOf(ModBlocks.DEATH_STATUE_BLOCK) && world.getBlockState(pos.up().add(0, 1, 0)).isOf(ModBlocks.DEATH_STATUE_BLOCK)) {
            world.removeBlock(pos.up(), false);
            world.removeBlock(pos.up().add(0, 1, 0), false);
            world.playSound(null, pos, SoundEvents.BLOCK_AMETHYST_BLOCK_BREAK, SoundCategory.BLOCKS, 0.5f, world.random.nextFloat() * 0.1f + 0.9f);
        }
        super.onBreak(world, pos, state, player);
    }

    @Override
    public boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        return ScreenHandler.calculateComparatorOutput(world.getBlockEntity(pos));
    }
    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new DeathStatueBlockEntity(pos, state);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }
    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);

            if (blockEntity instanceof DeathStatueBlockEntity) {
                ItemScatterer.spawn(world, pos, (DeathStatueBlockEntity)blockEntity);

                world.updateComparators(pos, this);
            }
            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }
    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient()) {

            NamedScreenHandlerFactory screenHandlerFactory = state.createScreenHandlerFactory(world, pos);

            if (screenHandlerFactory != null) {
                world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.BLOCK_ENDER_CHEST_OPEN, SoundCategory.BLOCKS, 0.5f, world.random.nextFloat() * 0.1f + 0.9f);
                player.openHandledScreen(screenHandlerFactory);
            }
        }
        return ActionResult.SUCCESS;
    }
}
