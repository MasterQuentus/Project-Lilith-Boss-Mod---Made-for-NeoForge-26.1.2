package com.masterquentus.projectlilith.block;

import com.masterquentus.projectlilith.ProjectLilith;
import com.masterquentus.projectlilith.block.custom.HellfireBlock;
import com.masterquentus.projectlilith.block.custom.LilithTrophyBlock;
import com.masterquentus.projectlilith.item.ModItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(ProjectLilith.MOD_ID);

    public static final DeferredBlock<Block> HELLFIRE = registerBlock("hell_fire",
            properties -> new HellfireBlock(properties.strength(4f)
                    .instabreak().noOcclusion().randomTicks().lightLevel((state) -> 10).noLootTable().noCollision().instabreak()));

    public static final DeferredBlock<Block> LILITH_TROPHY = registerBlock("lilith_trophy",
            properties -> new LilithTrophyBlock(properties.strength(2.0f).noOcclusion())
    );

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Function<BlockBehaviour.Properties, T> function) {
        DeferredBlock<T> toReturn = BLOCKS.registerBlock(name, function);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        ModItems.ITEMS.registerItem(name, properties -> new BlockItem(block.get(), properties.useBlockDescriptionPrefix()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}