package com.masterquentus.projectlilith.item.entity.client;

import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import com.masterquentus.projectlilith.ProjectLilith;
import com.masterquentus.projectlilith.item.entity.LilithEntity;
import net.minecraft.resources.Identifier;

public class LilithModel extends GeoModel<LilithEntity> {

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(ProjectLilith.MOD_ID, "lilith");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(ProjectLilith.MOD_ID, "textures/entity/lilith.png");
    }

    @Override
    public Identifier getAnimationResource(LilithEntity animatable) {
        return Identifier.fromNamespaceAndPath(ProjectLilith.MOD_ID, "lilith");
    }
}