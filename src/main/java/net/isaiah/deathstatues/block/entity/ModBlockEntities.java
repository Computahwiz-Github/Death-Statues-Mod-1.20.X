package net.isaiah.deathstatues.block.entity;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.isaiah.deathstatues.DeathStatues;
import net.isaiah.deathstatues.block.ModBlocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModBlockEntities {
    public static final BlockEntityType<DeathStatueBlockEntity> DEATH_STATUE_BASE =
            Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier(DeathStatues.MOD_ID, "death_statue_base"),
                    FabricBlockEntityTypeBuilder.create(DeathStatueBlockEntity::new,
                            ModBlocks.DEATH_STATUE_BASE_BLOCK).build(null));

    public static void registerBlockEntities() {
        DeathStatues.LOGGER.info("Registering Block Entities for: " + DeathStatues.MOD_ID);
    }
}
