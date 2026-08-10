package com.masterquentus.projectlilith.entity;

import com.masterquentus.projectlilith.ProjectLilith;
import com.masterquentus.projectlilith.item.entity.HellfireProjectile;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, ProjectLilith.MOD_ID);

    public static final Supplier<EntityType<HellfireProjectile>> HELLFIRE_PROJECTILE =
            ENTITIES.register("hellfire_projectile", (name) -> EntityType.Builder.<HellfireProjectile>of(HellfireProjectile::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, name)));


    public static void register(IEventBus eventBus) {
        ENTITIES.register(eventBus);
    }
}