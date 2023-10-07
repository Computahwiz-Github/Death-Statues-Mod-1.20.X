package net.isaiah.deathstatues.util;

import net.isaiah.deathstatues.DeathStatues;
import net.isaiah.deathstatues.item.ModItems;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.util.Identifier;

public class ModModelPredicateProvider {
    public static void registerModModels() {
        //ModelPredicateProviderRegistry.register(ModItems.DEATH_STATUE, new Identifier(DeathStatues.MOD_ID, "on"), (stack, world, entity, seed) -> stack.hasNbt() ? 1f : 0f);
    }
}
