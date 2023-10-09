package net.isaiah.deathstatues.item;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.isaiah.deathstatues.DeathStatues;
import net.isaiah.deathstatues.block.ModBlocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {
    //public static final Item DEATH_STATUE = registerItem("death_statue", new Item(new Item.Settings().maxCount(1)));
    //public static final Item DEATH_STATUE_BASE_ITEM = registerItem("death_statue_base_item", new Item(new Item.Settings().maxCount(1)));

    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, new Identifier(DeathStatues.MOD_ID, name), item);
    }
    private static void itemGroupFunctional(FabricItemGroupEntries entries) {
        //entries.add(DEATH_STATUE);
        //entries.add(DEATH_STATUE_BASE_ITEM);

        entries.add(ModBlocks.DEATH_STATUE_BLOCK);
        entries.add(ModBlocks.DEATH_STATUE_BASE_BLOCK);
    }
    public static void registerModItems() {
        DeathStatues.LOGGER.info("Registering Mod Items for: " + DeathStatues.MOD_ID);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(ModItems::itemGroupFunctional);
    }
}
