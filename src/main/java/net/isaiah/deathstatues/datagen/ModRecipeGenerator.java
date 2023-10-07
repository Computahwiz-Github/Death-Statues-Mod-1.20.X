package net.isaiah.deathstatues.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.isaiah.deathstatues.block.ModBlocks;
import net.isaiah.deathstatues.item.ModItems;
import net.minecraft.data.server.recipe.RecipeJsonProvider;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.util.Identifier;

import java.util.function.Consumer;

public class ModRecipeGenerator extends FabricRecipeProvider {
    public ModRecipeGenerator(FabricDataOutput output) {
        super(output);
    }
    @Override
    public void generate(Consumer<RecipeJsonProvider> exporter) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModItems.DEATH_STATUE)
                .pattern("FFF")
                .pattern("FNF")
                .pattern("FFF")
                .input('N', Items.NAME_TAG)
                .input('F', Items.ITEM_FRAME)
                .criterion(hasItem(Items.NAME_TAG), conditionsFromItem(Items.NAME_TAG))
                .criterion(hasItem(Items.ITEM_FRAME), conditionsFromItem(Items.ITEM_FRAME))
                .offerTo(exporter, new Identifier(getRecipeName(ModItems.DEATH_STATUE) + "_"));

        offerReversibleCompactingRecipes(exporter, RecipeCategory.MISC, ModItems.DEATH_STATUE, RecipeCategory.MISC, ModBlocks.DEATH_STATUE_BLOCK);
    }
}
