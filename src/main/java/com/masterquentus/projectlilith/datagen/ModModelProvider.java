package com.masterquentus.projectlilith.datagen;

import com.masterquentus.projectlilith.ProjectLilith;
import com.masterquentus.projectlilith.block.ModBlocks;
import com.masterquentus.projectlilith.item.ModItems;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiPartGenerator;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;



public class ModModelProvider extends ModelProvider {
    public ModModelProvider(PackOutput output) {
        super(output, ProjectLilith.MOD_ID);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        itemModels.generateFlatItem(ModItems.LILITH_CONTRACT.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.LILITH_SOUL.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.FLINT_AND_HELLFIRE.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.HELLFIRE_CHARGE.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.CRIMSON_FANG.get(), ModelTemplates.FLAT_HANDHELD_ITEM);


        Block hellfire = ModBlocks.HELLFIRE.get();

        MultiVariant floor = blockModels.createFloorFireModels(hellfire);
        MultiVariant side  = blockModels.createSideFireModels(hellfire);
        MultiVariant top   = blockModels.createTopFireModels(hellfire);

        blockModels.createNonTemplateModelBlock(ModBlocks.LILITH_TROPHY.get());

        blockModels.blockStateOutput.accept(
                MultiPartGenerator.multiPart(hellfire)
                        .with(floor)   // always present
                        .with(BlockModelGenerators.condition().term(BlockStateProperties.NORTH, true), side)
                        .with(BlockModelGenerators.condition().term(BlockStateProperties.EAST,  true), side)
                        .with(BlockModelGenerators.condition().term(BlockStateProperties.SOUTH, true), side)
                        .with(BlockModelGenerators.condition().term(BlockStateProperties.WEST,  true), side)
                        .with(BlockModelGenerators.condition().term(BlockStateProperties.UP,    true), top)
        );

    }
}