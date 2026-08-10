package com.masterquentus.projectlilith.creativemodtab;

import com.masterquentus.projectlilith.ProjectLilith;
import com.masterquentus.projectlilith.item.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCreativeModTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ProjectLilith.MOD_ID);


    public static final Supplier<CreativeModeTab> PROJECT_LILITH_ITEMS_TAB = CREATIVE_MODE_TABS.register("project_lilith_items_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.LILITH_CONTRACT.get()))
                    .title(Component.translatable("creativetab.projectlilith_items"))
                    .withTabsBefore(CreativeModeTabs.INGREDIENTS)
                    .withTabsAfter(Identifier.fromNamespaceAndPath(ProjectLilith.MOD_ID, "project_lilith_blocks_tab"))
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(ModItems.LILITH_CONTRACT);
                        output.accept(ModItems.LILITH_SOUL);
                        output.accept(ModItems.FLINT_AND_HELLFIRE);
                        output.accept(ModItems.CRIMSON_FANG);


                    }).build());


    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}