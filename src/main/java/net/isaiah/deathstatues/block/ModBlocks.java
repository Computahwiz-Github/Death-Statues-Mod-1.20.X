package net.isaiah.deathstatues.block;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.isaiah.deathstatues.DeathStatues;
import net.isaiah.deathstatues.block.statue.DeathStatueBaseBlock;
import net.isaiah.deathstatues.block.statue.DeathStatueBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

public class ModBlocks {
    public static final Block DEATH_STATUE_BLOCK = registerBlock("death_statue_block", new DeathStatueBlock(FabricBlockSettings.create().strength(4.0f, 0.6f).sounds(BlockSoundGroup.AMETHYST_BLOCK)));
    public static final Block DEATH_STATUE_BASE_BLOCK = registerBlock("death_statue_base", new DeathStatueBaseBlock(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK).sounds(BlockSoundGroup.COPPER).nonOpaque()));

    private static Block registerBlock(String name, Block block) {
        registerBlockItem(name, block);
        return Registry.register(Registries.BLOCK, new Identifier(DeathStatues.MOD_ID, name), block);
    }

    private static Item registerBlockItem(String name, Block block) {
        return Registry.register(Registries.ITEM, new Identifier(DeathStatues.MOD_ID, name), new BlockItem(block, new FabricItemSettings()));
    }

    public static void registerModBlocks() {
        DeathStatues.LOGGER.info("Registering ModBlocks for: " + DeathStatues.MOD_ID);
    }
}
