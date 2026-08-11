package com.masterquentus.projectlilith;

import com.masterquentus.projectlilith.block.ModBlocks;
import com.masterquentus.projectlilith.creativemodtab.ModCreativeModTabs;
import com.masterquentus.projectlilith.entity.ModEntities;
import com.masterquentus.projectlilith.events.ClientModEvents;
import com.masterquentus.projectlilith.item.ModItems;
import com.masterquentus.projectlilith.recipe.ModRecipes;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

@Mod(ProjectLilith.MOD_ID)
public class ProjectLilith {
    public static final String MOD_ID = "projectlilith";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ProjectLilith(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        ModCreativeModTabs.register(modEventBus);

        System.out.println("MOD CONSTRUCTOR RUNNING");

        ModRecipes.register(modEventBus);

        System.out.println("MOD RECIPES REGISTERED");

        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModEntities.register(modEventBus);

        NeoForge.EVENT_BUS.register(this);

        modEventBus.addListener(this::addCreative);
        modEventBus.register(ClientModEvents.class);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {

    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {

    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {

    }
}
