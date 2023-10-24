package net.isaiah.deathstatues.item;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.isaiah.deathstatues.DeathStatues;
import net.isaiah.deathstatues.block.ModBlocks;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ModItemGroups {
    public static final ItemGroup STATUE_GROUP = Registry.register(Registries.ITEM_GROUP,
            new Identifier(DeathStatues.MOD_ID, "statue"),
            FabricItemGroup.builder().displayName(Text.translatable("itemgroup.statue"))
                    .icon(() -> new ItemStack(ModItems.DEATH_STATUE_ITEM)).entries(((displayContext, entries) -> {
                        entries.add(ModItems.DEATH_STATUE_ITEM);
                        entries.add(ModBlocks.DEATH_STATUE_BASE_BLOCK);
                        entries.add(ModBlocks.DEATH_STATUE_BLOCK);

                    })).build());

    public static void registerItemGroups() {
        DeathStatues.LOGGER.info("Registering Item Groups for: " + DeathStatues.MOD_ID);
    }
}
