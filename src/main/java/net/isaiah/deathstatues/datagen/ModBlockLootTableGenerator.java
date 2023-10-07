package net.isaiah.deathstatues.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.isaiah.deathstatues.block.ModBlocks;

public class ModBlockLootTableGenerator extends FabricBlockLootTableProvider {
    public ModBlockLootTableGenerator(FabricDataOutput dataOutput) {
        super(dataOutput);
    }
    @Override
    public void generate() {
        addDrop(ModBlocks.DEATH_STATUE_BLOCK); //This is where you change what the item drop is from the block
        addDrop(ModBlocks.DEATH_STATUE_BASE_BLOCK);
    }
}
