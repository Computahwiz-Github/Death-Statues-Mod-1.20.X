package net.isaiah.deathstatues.entity;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.isaiah.deathstatues.DeathStatues;
import net.isaiah.deathstatues.entity.deathstatue.DeathStatueEntity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModEntities {
    public static final EntityType<DeathStatueEntity> DEATH_STATUE_ENTITY = Registry.register(
            Registries.ENTITY_TYPE, new Identifier(DeathStatues.MOD_ID, "death_statue"),
            FabricEntityTypeBuilder.create(SpawnGroup.MISC, DeathStatueEntity::new)
                    .dimensions(EntityDimensions.fixed(0.6F, 1.8F)).build());
}
