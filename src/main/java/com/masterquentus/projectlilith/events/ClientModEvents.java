package com.masterquentus.projectlilith.events;

import com.masterquentus.projectlilith.ProjectLilith;
import com.masterquentus.projectlilith.entity.ModEntities;
import com.masterquentus.projectlilith.item.entity.client.HellfireProjectileRenderer;
import com.masterquentus.projectlilith.item.entity.client.LilithRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = ProjectLilith.MOD_ID, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.LILITH.get(), LilithRenderer::new);
        event.registerEntityRenderer(ModEntities.HELLFIRE_PROJECTILE.get(), HellfireProjectileRenderer::new);

    }
}