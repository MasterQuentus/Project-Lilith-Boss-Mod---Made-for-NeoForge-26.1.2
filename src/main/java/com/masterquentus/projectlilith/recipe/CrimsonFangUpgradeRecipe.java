package com.masterquentus.projectlilith.recipe;

import com.masterquentus.projectlilith.item.ModItems;
import com.masterquentus.projectlilith.item.custom.CrimsonFang;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.level.Level;

import java.util.List;

public class CrimsonFangUpgradeRecipe extends CustomRecipe {

    public CrimsonFangUpgradeRecipe() {
        super();   // no arguments
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        boolean hasSword = false;
        boolean hasSoul = false;
        boolean hasContract = false;
        int count = 0;

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (!stack.isEmpty()) {
                count++;
                if (stack.getItem() instanceof CrimsonFang && !hasSword) {
                    hasSword = true;
                } else if (stack.is(ModItems.LILITH_SOUL.get()) && !hasSoul) {
                    hasSoul = true;
                } else if (stack.is(ModItems.LILITH_CONTRACT.get()) && !hasContract) {
                    hasContract = true;
                }
            }
        }
        return hasSword && hasSoul && hasContract && count == 3;
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        ItemStack result = new ItemStack(ModItems.CRIMSON_FANG.get());
        CrimsonFang.applyUpgrade(result);
        return result;
    }

    @Override
    public boolean isSpecial() {
        return true;          // important for special crafting recipes
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;   // or PlacementInfo.create(...) if you want recipe book support
    }

    @Override
    public RecipeSerializer<? extends CustomRecipe> getSerializer() {
        return ModRecipes.CRIMSON_FANG_UPGRADE_SERIALIZER.get();
    }

    @Override
    public RecipeType<CraftingRecipe> getType() {
        return RecipeType.CRAFTING;
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of();
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        return NonNullList.withSize(input.size(), ItemStack.EMPTY);
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }
}