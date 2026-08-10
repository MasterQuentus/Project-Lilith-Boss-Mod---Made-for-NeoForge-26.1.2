package com.masterquentus.projectlilith.item.custom;

import com.masterquentus.projectlilith.item.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.Consumer;

public class CrimsonFang extends Item {

    private static final String UPGRADED_TAG = "upgradedCrimsonFang";

    public CrimsonFang(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return isUpgraded(stack);
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        // Heal the attacker (lifesteal) - e.g., 1 full heart (2.0F) or half a heart
        attacker.heal(2.0F);

        if (isUpgraded(stack)) {
            // Optional: Extra healing or effects for the upgraded version
            attacker.heal(1.0F); // Total 1.5 hearts when upgraded, for example

            if (attacker instanceof Player player &&
                    player.getHealth() >= player.getMaxHealth()) {

                player.addEffect(
                        new MobEffectInstance(
                                MobEffects.STRENGTH,
                                60,
                                1
                        )
                );

                if (player.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(
                            ParticleTypes.SOUL,
                            target.getX(),
                            target.getY() + 1,
                            target.getZ(),
                            5,
                            0.2,
                            0.2,
                            0.2,
                            0.05
                    );
                }
            }
        }

        super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public InteractionResult use(
            Level level,
            Player player,
            InteractionHand hand) {

        ItemStack stack = player.getItemInHand(hand);

        if (!isUpgraded(stack)) {
            return InteractionResult.PASS;
        }

        // Only run on the server side to handle motion and cooldowns properly
        if (!level.isClientSide()) {
            // Apply dash velocity (forward look vector multiplied by speed factor)
            Vec3 look = player.getLookAngle();
            player.setDeltaMovement(look.x * 1.5, 0.4, look.z * 1.5);
            player.hurtMarked = true; // Forces client to accept the velocity packet immediately

            // Optional: Play a sound or particles at start position
            level.playSound(
                    null,
                    player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENDERMAN_TELEPORT,
                    SoundSource.PLAYERS,
                    0.5F, 1.2F
            );

            // Add a short cooldown (e.g., 20 ticks / 1 second) so it can't be spammed
            player.getCooldowns().addCooldown(stack, 20);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            Item.TooltipContext context,
            TooltipDisplay display,
            Consumer<Component> builder,
            TooltipFlag flag) {

        if (isUpgraded(stack)) {
            builder.accept(
                    Component.literal("Contract Signed")
                            .withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC)
            );

            builder.accept(
                    Component.literal("Ability: Shadow Dash [Right-Click]")
                            .withStyle(ChatFormatting.GOLD)
            );

            builder.accept(
                    Component.literal("Passive: Lifesteal (Heals on Hit)")
                            .withStyle(ChatFormatting.RED)
            );

            builder.accept(
                    Component.literal("Passive: Vampiric Wrath")
                            .withStyle(ChatFormatting.RED)
            );
        } else {
            builder.accept(
                    Component.literal("Requires Lilith's Soul & Contract")
                            .withStyle(ChatFormatting.GRAY)
            );
        }
    }

    private boolean isUpgraded(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);

        if (data == null) {
            return false;
        }

        return data.copyTag()
                .getBoolean(UPGRADED_TAG)
                .orElse(false);
    }

    public static void applyUpgrade(ItemStack stack) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(UPGRADED_TAG, true);

        stack.set(
                DataComponents.CUSTOM_DATA,
                CustomData.of(tag)
        );
    }
}