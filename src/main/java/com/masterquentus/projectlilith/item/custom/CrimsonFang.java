package com.masterquentus.projectlilith.item.custom;

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
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import java.util.List;

public class CrimsonFang extends Item {
    private static final String UPGRADED_TAG = "upgradedCrimsonFang";

    public CrimsonFang(Properties properties) {
        super(properties);
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        // Base lifesteal: 1 heart per hit
        attacker.heal(2.0F);

        if (isUpgraded(stack)) {
            if (attacker instanceof Player player && player.getHealth() >= player.getMaxHealth()) {
                // Strength effect (old DAMAGE_BOOST)
                player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 60, 1));

                if (player.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.SOUL,
                            target.getX(), target.getY() + 1, target.getZ(),
                            5, 0.2, 0.2, 0.2, 0.05);
                }
            }
        }

        super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // Only allow Shadow Dash when upgraded
        if (!isUpgraded(stack)) {
            return InteractionResult.PASS;
        }

        // your dash logic here...

        return InteractionResult.SUCCESS;
    }

    // Remove @Override if the IDE still complains
    public void appendHoverText(ItemStack stack, Item.TooltipContext context,
                                List<Component> tooltip, TooltipFlag flag) {
        if (isUpgraded(stack)) {
            tooltip.add(Component.literal("Contract Signed").withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC));
            tooltip.add(Component.literal("Ability: Shadow Dash [Right-Click]").withStyle(ChatFormatting.GOLD));
            tooltip.add(Component.literal("Passive: Vampiric Wrath").withStyle(ChatFormatting.RED));
        } else {
            tooltip.add(Component.literal("Requires Lilith's Soul & Contract").withStyle(ChatFormatting.GRAY));
        }
    }

    private boolean isUpgraded(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return false;
        return data.copyTag().getBoolean(UPGRADED_TAG).orElse(false);
    }

    public static void applyUpgrade(ItemStack stack) {
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = data.copyTag();
        tag.putBoolean(UPGRADED_TAG, true);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }
}