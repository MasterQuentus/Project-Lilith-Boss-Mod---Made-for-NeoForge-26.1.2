package com.masterquentus.projectlilith.events;

import com.masterquentus.projectlilith.ProjectLilith;
import com.masterquentus.projectlilith.entity.ModEntities;
import com.masterquentus.projectlilith.item.entity.LilithEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

@EventBusSubscriber(modid = ProjectLilith.MOD_ID)
public class ModEvents {

    @SubscribeEvent
    public static void entityAttributeEvent(EntityAttributeCreationEvent event) {
        event.put(ModEntities.LILITH.get(), LilithEntity.setAttributes());
    }
}
