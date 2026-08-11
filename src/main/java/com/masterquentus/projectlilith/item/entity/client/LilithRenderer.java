package com.masterquentus.projectlilith.item.entity.client;

import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.masterquentus.projectlilith.entity.ModEntities;
import com.masterquentus.projectlilith.item.entity.LilithEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

public class LilithRenderer<R extends LivingEntityRenderState & GeoRenderState>
        extends GeoEntityRenderer<LilithEntity, R> {

    public LilithRenderer(EntityRendererProvider.Context context) {
        super(context, ModEntities.LILITH.get());
    }
}