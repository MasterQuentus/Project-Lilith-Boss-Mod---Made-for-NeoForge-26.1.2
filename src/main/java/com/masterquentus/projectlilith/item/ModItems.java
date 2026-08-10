package com.masterquentus.projectlilith.item;

import com.masterquentus.projectlilith.ProjectLilith;
import com.masterquentus.projectlilith.item.custom.CrimsonFang;
import com.masterquentus.projectlilith.item.custom.FlintAndHellfireItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ToolMaterial;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ProjectLilith.MOD_ID);

    public static final DeferredItem<Item> LILITH_CONTRACT = ITEMS.registerSimpleItem("lilith_contract");

    public static final DeferredItem<Item> LILITH_SOUL = ITEMS.registerSimpleItem("lilith_soul");

    public static final DeferredItem<FlintAndHellfireItem> FLINT_AND_HELLFIRE =
            ITEMS.registerItem("flint_and_hellfire",
                    props -> new FlintAndHellfireItem(props.durability(64)));

    public static final DeferredItem<CrimsonFang> CRIMSON_FANG =
            ITEMS.registerItem("crimson_fang",
                    props -> new CrimsonFang(
                            props.sword(ToolMaterial.NETHERITE, 3, -2.4F)
                                    .repairable(Items.NETHERITE_INGOT)
                    ));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}