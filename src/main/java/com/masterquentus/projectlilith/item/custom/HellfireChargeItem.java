package com.masterquentus.projectlilith.item.custom;

import com.masterquentus.projectlilith.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class HellfireChargeItem extends Item {
    public HellfireChargeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos().relative(context.getClickedFace());

        if (!level.isClientSide()) {
            // Set the block to fire or your custom hellfire block
            level.setBlock(pos, ModBlocks.HELLFIRE.get().defaultBlockState(), 3);
            context.getPlayer().playSound(SoundEvents.FIRECHARGE_USE, 1.0F, 1.2F);

            if (!context.getPlayer().getAbilities().instabuild) {
                context.getItemInHand().shrink(1);
            }
        }

        return InteractionResult.SUCCESS;
    }
}