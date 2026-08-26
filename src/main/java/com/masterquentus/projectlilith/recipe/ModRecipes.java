package com.masterquentus.projectlilith.recipe;

import com.masterquentus.projectlilith.ProjectLilith;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModRecipes {

    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, ProjectLilith.MOD_ID);

    public static final DeferredHolder<
            RecipeSerializer<?>,
            RecipeSerializer<CrimsonFangUpgradeRecipe>> CRIMSON_FANG_UPGRADE_SERIALIZER =
            SERIALIZERS.register("crimson_fang_upgrade", () ->
                    new RecipeSerializer<>(
                            MapCodec.unit(CrimsonFangUpgradeRecipe::new),
                            StreamCodec.of(
                                    (buf, recipe) -> {}, // Writer: writes nothing since it's stateless
                                    buf -> new CrimsonFangUpgradeRecipe() // Reader: safely instantiates a new recipe
                            )
                    )
            );

    public static void register(IEventBus eventBus) {
        SERIALIZERS.register(eventBus);
    }
}