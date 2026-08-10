package com.masterquentus.projectlilith.item.custom;

import com.masterquentus.projectlilith.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class FlintAndHellfireItem extends Item {

    public FlintAndHellfireItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();

        // If clicking on the side of a block, shift position to air space
        BlockPos targetPos = pos.relative(context.getClickedFace());

        // If not a portal, place Hellfire
        if (level.getBlockState(targetPos).isAir() && ModBlocks.HELLFIRE.get().defaultBlockState().canSurvive(level, targetPos)) {
            level.setBlock(targetPos, ModBlocks.HELLFIRE.get().defaultBlockState(), 11);
            level.playSound(null, targetPos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
            damageItem(stack, player, context);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    private void playPortalSound(Level level, BlockPos pos) {
        level.playSound(null, pos, SoundEvents.PORTAL_TRIGGER, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    /**
     * Damages the item and handles break animation.
     */
    private void damageItem(ItemStack stack, Player player, UseOnContext context) {
        if (player != null) {
            stack.hurtAndBreak(1, player, context.getHand());
        }
    }
}