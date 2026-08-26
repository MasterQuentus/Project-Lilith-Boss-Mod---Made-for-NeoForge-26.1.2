package com.masterquentus.projectlilith.datagen;

import com.masterquentus.projectlilith.ProjectLilith;
import com.masterquentus.projectlilith.block.ModBlocks;
import com.masterquentus.projectlilith.entity.ModEntities;
import com.masterquentus.projectlilith.item.ModItems;
import net.minecraft.advancements.criterion.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.resources.Identifier;

import java.util.function.Consumer;

public class ModAdvancementProvider implements AdvancementSubProvider {

    @Override
    public void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> saver) {

        // 1. Root: Craft and Use the Flint and Hellfire
        AdvancementHolder useFlintAndHellfire = Advancement.Builder.advancement()
                .display(
                        ModItems.FLINT_AND_HELLFIRE.get(),
                        Component.translatable("advancements.projectlilith.use_flint_and_hellfire.title"),
                        Component.translatable("advancements.projectlilith.use_flint_and_hellfire.description"),
                        Identifier.fromNamespaceAndPath("minecraft", "gui/advancements/backgrounds/nether"),
                        AdvancementType.TASK,
                        true, true, false
                )
                .addCriterion("has_flint_and_hellfire", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.FLINT_AND_HELLFIRE.get()))
                .save(saver, Identifier.fromNamespaceAndPath(ProjectLilith.MOD_ID, "use_flint_and_hellfire").toString());

        // 2. Summoning Lilith (Unlocks via manual Java trigger when spawned on Netherrack)
        AdvancementHolder summonLilith = Advancement.Builder.advancement()
                .parent(useFlintAndHellfire)
                .display(
                        ModItems.LILITH_CONTRACT.get(),
                        Component.translatable("advancements.projectlilith.awakening_darkness.title"),
                        Component.translatable("advancements.projectlilith.awakening_darkness.description"),
                        null,
                        AdvancementType.TASK,
                        true, true, false
                )
                .addCriterion("summoned_lilith", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.LILITH_CONTRACT.get()))
                .save(saver, Identifier.fromNamespaceAndPath(ProjectLilith.MOD_ID, "awakening_darkness").toString());

        // 3. Queen of the Abyss (Goal: Defeating Lilith)
        AdvancementHolder queenOfTheAbyss = Advancement.Builder.advancement()
                .parent(summonLilith)
                .display(
                        ModItems.FLINT_AND_HELLFIRE.get(),
                        Component.translatable("advancements.projectlilith.queen_of_the_abyss.title"),
                        Component.translatable("advancements.projectlilith.queen_of_the_abyss.description"),
                        null,
                        AdvancementType.GOAL,
                        true, true, false
                )
                .addCriterion("killed_lilith", KilledTrigger.TriggerInstance.playerKilledEntity(
                        EntityPredicate.Builder.entity()
                                .entityType(EntityTypePredicate.of(registries.lookupOrThrow(Registries.ENTITY_TYPE), ModEntities.LILITH.get()))
                ))
                .save(saver, Identifier.fromNamespaceAndPath(ProjectLilith.MOD_ID, "queen_of_the_abyss").toString());

        // 4. Relics of the Dark Queen (Challenge: Claiming the Trophy)
        AdvancementHolder relicsOfDarkness = Advancement.Builder.advancement()
                .parent(queenOfTheAbyss)
                .display(
                        ModBlocks.LILITH_TROPHY.get(),
                        Component.translatable("advancements.projectlilith.relics_of_darkness.title"),
                        Component.translatable("advancements.projectlilith.relics_of_darkness.description"),
                        null,
                        AdvancementType.CHALLENGE,
                        true, true, true
                )
                .addCriterion("has_trophy", InventoryChangeTrigger.TriggerInstance.hasItems(ModBlocks.LILITH_TROPHY.get()))
                .save(saver, Identifier.fromNamespaceAndPath(ProjectLilith.MOD_ID, "relics_of_darkness").toString());
    }
}