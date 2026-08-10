package com.masterquentus.projectlilith.item.entity;

import com.masterquentus.projectlilith.block.ModBlocks;
import com.masterquentus.projectlilith.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.HitResult;

public class HellfireProjectile extends ThrowableItemProjectile {
    private int explosionPower = 1;

    public HellfireProjectile(EntityType<? extends HellfireProjectile> type, Level level) {
        super(type, level);
    }

    // This is required for the renderer to see the hellfire_charge
    @Override
    protected Item getDefaultItem() {
        return ModItems.HELLFIRE_CHARGE.get();
    }

    public boolean isInvulnerable(DamageSource source) {
        if (source.getDirectEntity() instanceof LivingEntity puncher && !this.level().isClientSide()) {
            this.setDeltaMovement(puncher.getLookAngle().scale(1.5));
            this.setOwner(puncher);
            this.playSound(SoundEvents.FIRECHARGE_USE, 1.0F, 1.2F);
            return true;
        }
        return super.isInvulnerable();
    }

    @Override
    public void tick() {
        super.tick();

        // If the fireball is NOT moving (orbiting) and has no owner, or owner is dead, kill it
        if (!this.level().isClientSide() && this.getDeltaMovement().lengthSqr() <= 0.01) {
            if (this.getOwner() == null || !this.getOwner().isAlive()) {
                this.discard();
            }
        }
    }

    @Override
    protected void onHit(HitResult pResult) {
        if (!this.level().isClientSide()) {
            BlockPos centerPos = BlockPos.containing(pResult.getLocation());

            // 1. DAMAGE & SOUND (No vanilla explosion to prevent vanilla fire)
            this.level().explode(this, null, null, this.getX(), this.getY(), this.getZ(),
                    (float)this.explosionPower, false, Level.ExplosionInteraction.NONE);

            // 2. PLACE HELLFIRE CLUSTER (Random Spread)
            // We do 5-8 attempts to place fire in a 4-block radius
            for(int i = 0; i < 8; ++i) {
                BlockPos spawnPos = centerPos.offset(
                        this.random.nextInt(5) - 2,
                        this.random.nextInt(2),
                        this.random.nextInt(5) - 2
                );

                // If it's air or normal fire, REPLACE it with Hellfire
                if (this.level().getBlockState(spawnPos).isAir() || this.level().getBlockState(spawnPos).is(Blocks.FIRE)) {
                    if (this.level().getBlockState(spawnPos.below()).isSolid()) {
                        this.level().setBlock(spawnPos, ModBlocks.HELLFIRE.get().defaultBlockState(), 3);
                    }
                }
            }

            this.discard();
        }
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        // 1. Don't hit yourself or your owner (Lilith) while orbiting or just starting
        if (target == this.getOwner() && this.tickCount < 10) {
            return false;
        }
        // 2. Prevent hitting other spinning fireballs
        if (target instanceof HellfireProjectile) {
            return false;
        }
        return super.canHitEntity(target);
    }

    @Override
    public boolean isPickable() {
        return !this.isRemoved(); // This allows the player to click/punch the entity
    }

    @Override
    public boolean isNoGravity() { return true; }
}