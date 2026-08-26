package com.masterquentus.projectlilith.item.custom;

import com.masterquentus.projectlilith.ProjectLilith;
import com.masterquentus.projectlilith.block.ModBlocks;
import com.masterquentus.projectlilith.entity.ModEntities;
import com.masterquentus.projectlilith.item.entity.LilithEntity;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
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
            BlockPos spawnPos = pos.above();

            // Spawn the visual lightning bolt
            LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(serverLevel, EntitySpawnReason.COMMAND);
            if (lightning != null) {
                lightning.setPos(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D);
                serverLevel.addFreshEntity(lightning);
            }

            // Spawn Lilith
            LilithEntity lilith = new LilithEntity(ModEntities.LILITH.get(), serverLevel);
            lilith.setPos(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D);
            serverLevel.addFreshEntity(lilith);

            // --- GRANT AWAKENING THE DARKNESS ADVANCEMENT ---
            if (player instanceof ServerPlayer serverPlayer) {
                MinecraftServer server = serverLevel.getServer();
                AdvancementHolder advancement = server.getAdvancements().get(Identifier.fromNamespaceAndPath(ProjectLilith.MOD_ID, "awakening_darkness"));
                if (advancement != null) {
                    PlayerAdvancements playerAdvancements = server.getPlayerList().getPlayerAdvancements(serverPlayer);
                    for (String criterion : playerAdvancements.getOrStartProgress(advancement).getRemainingCriteria()) {
                        playerAdvancements.award(advancement, criterion);
                    }
                }
            }
            // ------------------------------------------------

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