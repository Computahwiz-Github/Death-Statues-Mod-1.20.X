package net.isaiah.deathstatues.block.statue;

import net.isaiah.deathstatues.block.ModBlocks;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class DeathStatueBlock extends HorizontalFacingBlock {
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
    public static final BooleanProperty CHARGED = BooleanProperty.of("charged");
    public static final EnumProperty<DoubleBlockHalf> HALF = Properties.DOUBLE_BLOCK_HALF;

    public DeathStatueBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getDefaultState().with(FACING, Direction.SOUTH).with(HALF, DoubleBlockHalf.LOWER).with(CHARGED, false));
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return super.getPlacementState(ctx).with(FACING, ctx.getHorizontalPlayerFacing().getOpposite()).with(HALF, DoubleBlockHalf.LOWER);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        setChargedProperty(state, world, pos);
        return openBaseStorage(state, world, pos, player, hand, hit);
    }

    public ActionResult openBaseStorage(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        BlockPos bottomPos = pos.down();
        BlockState bottomBlockState = world.getBlockState(bottomPos);
        Block bottomBlock = bottomBlockState.getBlock();

        BlockPos belowBottomPos = pos.down(2);
        BlockState belowBottomBlockState = world.getBlockState(belowBottomPos);
        Block belowBottomBlock = belowBottomBlockState.getBlock();


        if (bottomBlock instanceof DeathStatueBaseBlock) {
            return bottomBlock.onUse(bottomBlockState, world, bottomPos, player, hand, hit);
        }
        if (bottomBlock instanceof DeathStatueBlock && belowBottomBlock instanceof DeathStatueBaseBlock) {
            return belowBottomBlock.onUse(belowBottomBlockState, world, belowBottomPos, player, hand, hit);
        }
        return super.onUse(state, world, pos, player, hand, hit);
    }

    public void setChargedProperty(BlockState state, World world, BlockPos pos) {
        if (world.getBlockState(pos.up()).isOf(ModBlocks.DEATH_STATUE_BLOCK) && world.getBlockState(state.get(HALF) == DoubleBlockHalf.LOWER ? pos.up() : pos.down()).getBlock() == this) {
            if (!world.getBlockState(pos).get(CHARGED) && !world.getBlockState(pos.up()).get(CHARGED)) {
                world.setBlockState(pos, state.with(CHARGED, true));
                world.setBlockState(pos.up(), state.with(CHARGED, true).with(HALF, DoubleBlockHalf.UPPER));
            }
            else if (world.getBlockState(pos).get(CHARGED) && !world.getBlockState(pos.up()).get(CHARGED)) {
                world.setBlockState(pos.up(), state.with(HALF, DoubleBlockHalf.UPPER).with(CHARGED, true));
            }
            else {
                world.setBlockState(pos, state.with(CHARGED, false));
                world.setBlockState(pos.up(), state.with(CHARGED, false).with(HALF, DoubleBlockHalf.UPPER));
            }
        }

        if (world.getBlockState(pos.down()).isOf(ModBlocks.DEATH_STATUE_BLOCK) && world.getBlockState(state.get(HALF) == DoubleBlockHalf.UPPER ? pos.down() : pos.up()).getBlock() == this) {
            if (!world.getBlockState(pos).get(CHARGED) && !world.getBlockState(pos.down()).get(CHARGED)) {
                world.setBlockState(pos, state.with(CHARGED, true));
                world.setBlockState(pos.down(), state.with(CHARGED, true).with(HALF, DoubleBlockHalf.LOWER));
            }
            else if (world.getBlockState(pos).get(CHARGED) && !world.getBlockState(pos.down()).get(CHARGED)) {
                world.setBlockState(pos.down(), state.with(HALF, DoubleBlockHalf.LOWER).with(CHARGED, true));
            }
            else {
                world.setBlockState(pos, state.with(CHARGED, false));
                world.setBlockState(pos.down(), state.with(CHARGED, false).with(HALF, DoubleBlockHalf.LOWER));
            }
        }
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (world.getBlockState(pos.up()).isOf(ModBlocks.DEATH_STATUE_BLOCK) && world.getBlockState(state.get(HALF) == DoubleBlockHalf.LOWER ? pos.up() : pos.down()).getBlock() == this) {
            world.removeBlock(state.get(HALF) == DoubleBlockHalf.LOWER ? pos.up() : pos.down(), false);
        }
        if (world.getBlockState(pos.down()).isOf(ModBlocks.DEATH_STATUE_BLOCK) && world.getBlockState(state.get(HALF) == DoubleBlockHalf.UPPER ? pos.down() : pos.up()).getBlock() == this) {
            world.removeBlock(state.get(HALF) == DoubleBlockHalf.UPPER ? pos.down() : pos.up(), false);
        }
        super.onBreak(world, pos, state, player);
    }

    @Override
    public void afterBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        if (world.getBlockState(pos.up()).isOf(ModBlocks.DEATH_STATUE_BLOCK) && world.getBlockState(state.get(HALF) == DoubleBlockHalf.LOWER ? pos.up() : pos.down()).getBlock() == this) {
            world.removeBlock(state.get(HALF) == DoubleBlockHalf.LOWER ? pos.up() : pos.down(), false);
        }
        if (world.getBlockState(pos.down()).isOf(ModBlocks.DEATH_STATUE_BLOCK) && world.getBlockState(state.get(HALF) == DoubleBlockHalf.UPPER ? pos.down() : pos.up()).getBlock() == this) {
            world.removeBlock(state.get(HALF) == DoubleBlockHalf.UPPER ? pos.down() : pos.up(), false);
        }
        super.afterBreak(world, player, pos, state, blockEntity, tool);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
            world.setBlockState(pos.up(), ModBlocks.DEATH_STATUE_BLOCK.getDefaultState().with(FACING, world.getBlockState(pos).get(FACING)).with(HALF, DoubleBlockHalf.UPPER));
        super.onPlaced(world, pos, state, placer, itemStack);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        Direction dir = state.get(FACING);
        switch (dir) {
            case NORTH, SOUTH:
                return VoxelShapes.cuboid(0,0,0.25,1,1,0.75); // Changed maxY from 2 to 1
            case EAST, WEST:
                return  VoxelShapes.cuboid(0.25,0,0,0.75,1,1); // Changed maxY from 2 to 1
            default:
                return VoxelShapes.fullCube();
        }
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        Direction dir = state.get(FACING);
        switch (dir) {
            case NORTH, SOUTH:
                return VoxelShapes.cuboid(0,0,0.25,1,2,0.75);
            case EAST, WEST:
                return  VoxelShapes.cuboid(0.25,0,0,0.75,2,1);
            default:
                return VoxelShapes.fullCube();
        }
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, HALF, CHARGED);
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        BlockPos blockPos = pos.down();
        BlockState blockState = world.getBlockState(blockPos);
        if (state.get(HALF) == DoubleBlockHalf.LOWER) {
            return blockState.isSideSolidFullSquare(world, blockPos, Direction.UP);
        }
        return blockState.isOf(this);
    }
}
