package com.masterquentus.projectlilith.item.custom;

import com.masterquentus.projectlilith.block.ModBlocks;
import com.masterquentus.projectlilith.entity.ModEntities;
import com.masterquentus.projectlilith.item.entity.LilithEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class FlintAndHellfireItem extends Item {

    public FlintAndHellfireItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        ServerLevel serverLevel = (ServerLevel) level;
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();

        // 1. Summon Lilith if clicked directly on Netherrack
        if (serverLevel.getBlockState(pos).is(Blocks.NETHERRACK)) {
            LilithEntity lilith = new LilithEntity(ModEntities.LILITH.get(), serverLevel);
            lilith.setPos(pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D);
            serverLevel.addFreshEntity(lilith);

            serverLevel.playSound(null, pos, SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.BLOCKS, 1.0F, 0.8F);
            damageItem(stack, player, context);
            return InteractionResult.SUCCESS;
        }

        // 2. Existing Hellfire placement logic for other blocks
        BlockPos targetPos = pos.relative(context.getClickedFace());
        if (serverLevel.getBlockState(targetPos).isAir() && ModBlocks.HELLFIRE.get().defaultBlockState().canSurvive(serverLevel, targetPos)) {
            serverLevel.setBlock(targetPos, ModBlocks.HELLFIRE.get().defaultBlockState(), 11);
            serverLevel.playSound(null, targetPos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
            damageItem(stack, player, context);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    private void damageItem(ItemStack stack, Player player, UseOnContext context) {
        if (player != null) {
            stack.hurtAndBreak(1, player, context.getHand());
        }
    }
}