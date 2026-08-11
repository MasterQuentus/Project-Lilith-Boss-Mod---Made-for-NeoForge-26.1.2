package com.masterquentus.projectlilith.item.entity;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.util.GeckoLibUtil;
import com.masterquentus.projectlilith.entity.ModEntities;
import com.masterquentus.projectlilith.item.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.BossEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LilithEntity extends Monster implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private final List<UUID> spinningFireballUUIDs = new ArrayList<>();
    private int bloodMoonCooldown = 0;
    private int darkVeilCooldown = 0;
    private int shadowDashCooldown = 0;
    private int darkDominionCooldown = 0; // Added cooldown
    private boolean isUsingDarkDominion = false;
    private int abilityPhaseTimer = 0;
    private static final double MAX_FLOAT_HEIGHT = 12.0;
    private double startingY = -1; // to store the Y where boss started floating
    private int darkDominionRounds = 0; // count how many waves summoned
    private static final int MAX_DOMINION_ROUNDS = 5; // how many waves before phase ends
    private int hellfireCooldown = 0;
    private int hellfireSpinTick = 0;
    private final List<Entity> spinningFireballs = new ArrayList<>();

    private boolean isAttacking = false;
    private int attackTimer = 0;
    private int attackType = 1;

    // Transformation fields
    private boolean isTransformed = false;
    private int transformTimer = 0; // ticks remaining until Lilith returns
    private final List<LilithBat> activeBats = new ArrayList<>();
    private final List<Monster> darkDominionMinions = new ArrayList<>();

    // === Animations ===
    private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("animation.lilith.walk");
    private static final RawAnimation ATTACK_ANIM = RawAnimation.begin().thenPlay("animation.lilith.attack");
    private static final RawAnimation ATTACK2_ANIM = RawAnimation.begin().thenPlay("animation.lilith.attack2");
    private static final RawAnimation ATTACK3_ANIM = RawAnimation.begin().thenPlay("animation.lilith.attack3");
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("animation.lilith.idle");

    public LilithEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 500;
    }

    public enum BossPhase {
        AWAKENED(1.0f),      // Phase 1: Normal combat
        SHADOW_VEIL(0.75f),  // Phase 2: Darkness & Speed
        BLOOD_QUEEN(0.50f),  // Phase 3: Flight & Wave Summoning
        VOID_EMPRESS(0.25f); // Phase 4: Final Stand (Projectiles & Bat Swarms)

        final float healthThreshold;
        BossPhase(float threshold) { this.healthThreshold = threshold; }
    }

    private BossPhase currentPhase = BossPhase.AWAKENED;
    private boolean isTransitioning = false;
    private int waveMinionsRemaining = 0;
    private int phaseTickCounter = 0;

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);

        // Example for your spinning fireballs list
        ValueOutput.ValueOutputList list = output.childrenList("SpinningFireballs");
        for (Entity fireball : this.spinningFireballs) {
            list.addChild().putString("UUID", fireball.getUUID().toString());
        }

        // Or simpler if you just store UUIDs as strings:
        // output.putString("SomeKey", "value");
        // output.putInt("Phase", this.currentPhase.ordinal());
        // output.putBoolean("Transformed", this.isTransformed);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);

        // Example reading
        // this.currentPhase = BossPhase.values()[input.getIntOr("Phase", 0)];
        // this.isTransformed = input.getBooleanOr("Transformed", false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new MeleeAttackGoal(this, 1.2, true));
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(2, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 1.2D));
        this.goalSelector.addGoal(4, new LeapAtTargetGoal(this, 0.5F));

        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Villager.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Animal.class, true));
    }

    private final ServerBossEvent bossEvent = new ServerBossEvent(
            UUID.randomUUID(),
            Component.translatable("boss.projectlilith.lilith"),   // update the key if needed
            BossEvent.BossBarColor.RED,
            BossEvent.BossBarOverlay.PROGRESS
    );

    public static AttributeSupplier setAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 450.0D)         // Buffed: 300 is low for a multi-phase boss
                .add(Attributes.MOVEMENT_SPEED, 0.3D)       // Nerfed: 0.35 is "Baby Zombie" fast. 0.3 is better.
                .add(Attributes.ATTACK_DAMAGE, 10.0D)       // Buffed: 5 hearts base. Feels like a real hit.
                .add(Attributes.ATTACK_KNOCKBACK, 0.5D)     // Nerfed: 1.5 sends players too far away for her to hit.
                .add(Attributes.ARMOR, 12.0D)               // Buffed: Slightly higher base protection.
                .add(Attributes.ARMOR_TOUGHNESS, 10.0D)     // Buffed: Helps her ignore high-damage crit hits.
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D) // Buffed: Bosses shouldn't be "ping-ponged" by players.
                .add(Attributes.FOLLOW_RANGE, 64.0D).build(); // Buffed: She shouldn't lose track of you in large arenas.
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) {
            // handleBossMusic(); // implement later if needed
            return;
        }

        float healthPercent = this.getHealth() / this.getMaxHealth();
        bossEvent.setProgress(healthPercent);

        // 1. PHASE CHECKS
        if (healthPercent <= BossPhase.VOID_EMPRESS.healthThreshold) currentPhase = BossPhase.VOID_EMPRESS;
        else if (healthPercent <= BossPhase.BLOOD_QUEEN.healthThreshold) currentPhase = BossPhase.BLOOD_QUEEN;
        else if (healthPercent <= BossPhase.SHADOW_VEIL.healthThreshold) currentPhase = BossPhase.SHADOW_VEIL;

        // 2. HELLFIRE LOGIC
        hellfireSpinTick++;
        if (!spinningFireballUUIDs.isEmpty() && spinningFireballs.isEmpty()) {
            for (UUID uuid : spinningFireballUUIDs) {
                Entity found = ((ServerLevel) level()).getEntity(uuid);
                if (found != null) spinningFireballs.add(found);
            }
            spinningFireballUUIDs.clear();
        }
        spinningFireballs.removeIf(f -> f == null || f.isRemoved() || !f.isAlive());

        if (getTarget() != null && hellfireCooldown <= 0 && spinningFireballs.isEmpty()) {
            for (int i = 0; i < 3; i++) {
                HellfireProjectile hellfire = new HellfireProjectile(ModEntities.HELLFIRE_PROJECTILE.get(), level());
                hellfire.setOwner(this);
                hellfire.setPos(getX(), getY() + 2.5, getZ());
                hellfire.setDeltaMovement(Vec3.ZERO);
                level().addFreshEntity(hellfire);
                spinningFireballs.add(hellfire);
            }
            hellfireCooldown = 600;
        }

        for (int i = 0; i < spinningFireballs.size(); i++) {
            Entity fireball = spinningFireballs.get(i);
            if (fireball.getDeltaMovement().lengthSqr() <= 0.01) {
                double angle = (hellfireSpinTick * 0.15) + (i * (Math.PI * 2 / 3));
                double targetX = getX() + Math.cos(angle) * 3.5;
                double targetY = getY() + 2.2;
                double targetZ = getZ() + Math.sin(angle) * 3.5;
                fireball.setPos(targetX, targetY, targetZ);
                fireball.xo = targetX;
                fireball.yo = targetY;
                fireball.zo = targetZ;
                // hasImpulse removed in 26.1
            }
        }

        if (getTarget() != null && hellfireSpinTick % 40 == 0 && !spinningFireballs.isEmpty()) {
            spinningFireballs.stream()
                    .filter(f -> f.getDeltaMovement().lengthSqr() <= 0.01)
                    .findFirst()
                    .ifPresent(f -> {
                        Vec3 direction = getTarget().position().add(0, 1, 0).subtract(f.position()).normalize().scale(1.2);
                        f.setDeltaMovement(direction);
                        this.swing(InteractionHand.MAIN_HAND);
                    });
        }

        // 3. SPECIAL PHASE LOCKS
        if (isUsingDarkDominion) {
            abilityPhaseTimer--;
            darkDominionMinions.removeIf(mob -> mob.isRemoved() || !mob.isAlive());
            double targetY = startingY + MAX_FLOAT_HEIGHT;
            if (this.getY() < targetY) {
                setDeltaMovement(0, 0.1, 0);
            } else {
                setDeltaMovement(0, 0, 0);
                setPos(getX(), targetY, getZ());
            }

            if (abilityPhaseTimer <= 0 && darkDominionMinions.isEmpty()) {
                darkDominionRounds++;
                if (darkDominionRounds >= MAX_DOMINION_ROUNDS) {
                    isUsingDarkDominion = false;
                    this.setNoAi(false);
                    this.setInvulnerable(false);
                    darkDominionRounds = 0;
                    darkDominionCooldown = 2000;
                    Player p = level().getNearestPlayer(this, 32);
                    if (p != null) this.setTarget(p);
                } else {
                    spawnDarkDominionWave();
                    abilityPhaseTimer = 100 + random.nextInt(60);
                }
            }
            return;
        }

        if (isTransformed) {
            transformTimer--;
            if (transformTimer <= 0 || activeBats.isEmpty()) revertFromBatForm();
            return;
        }

        // 4. COOLDOWN MANAGEMENT
        if (bloodMoonCooldown > 0) bloodMoonCooldown--;
        if (darkVeilCooldown > 0) darkVeilCooldown--;
        if (shadowDashCooldown > 0) shadowDashCooldown--;
        if (darkDominionCooldown > 0) darkDominionCooldown--;
        if (hellfireCooldown > 0) hellfireCooldown--;
        if (attackTimer > 0) attackTimer--;
        else isAttacking = false;

        // 5. ABILITY TRIGGERS
        if (this.getTarget() != null && !isAttacking) {

            // PRIORITY 1: Dark Dominion
            if (darkDominionCooldown <= 0 && healthPercent < 0.7) {
                applyDarkDominion(this);
                return;
            }

            // PRIORITY 2: Bat Swarm
            if (healthPercent < 0.4 && random.nextInt(100) == 0) {
                triggerBatSwarm(this);
                broadcastAbility("Bat Swarm");
                return;
            }

            // PRIORITY 3: Shadow Dash
            if (shadowDashCooldown <= 0 && healthPercent < 0.3 && this.distanceTo(getTarget()) > 5) {
                shadowDash(this);
                broadcastAbility("Shadow Dash");
                return;
            }

            // PRIORITY 4: Blood Moon
            if (bloodMoonCooldown <= 0 && healthPercent < 0.9) {
                applyBloodMoonCurse(this);
                broadcastAbility("Blood Moon Curse");
                bloodMoonCooldown = 1200;
                return;
            }

            // PRIORITY 5: Dark Veil
            if (darkVeilCooldown <= 0 && healthPercent < 0.8) {
                triggerDarkVeil(this);
                broadcastAbility("Dark Veil");
                darkVeilCooldown = 1800;
                return;
            }
        }
    }

    @Override
    public boolean isInvulnerableTo(ServerLevel level, DamageSource source) {
        // Invulnerable while transformed or using Dark Dominion
        if (isTransformed || isUsingDarkDominion) {
            return true;
        }

        // Immunity to her own projectiles
        if (source.getDirectEntity() != null && spinningFireballs.contains(source.getDirectEntity())) {
            return true;
        }

        // Immunity to fire damage types
        if (source.is(DamageTypes.IN_FIRE)
                || source.is(DamageTypes.ON_FIRE)
                || source.is(DamageTypes.FIREBALL)
                || source.is(DamageTypes.UNATTRIBUTED_FIREBALL)
                || source.is(DamageTypes.LAVA)
                || source.is(DamageTypes.HOT_FLOOR)) {
            return true;
        }

        return super.isInvulnerableTo(level, source);
    }

    private void broadcastAbility(String abilityName) {
        if (!level().isClientSide()) {
            // Fancy colored chat message
            Component msg = Component.empty()
                    .append(Component.literal("[Lilith] ").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD))
                    .append(Component.literal("uses ").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal(abilityName).withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                    .append(Component.literal("!").withStyle(ChatFormatting.GRAY));
            for (Player player : level().players()) {
                player.sendSystemMessage(msg);
            }
        }
    }

    private Player getNearestPlayer() {
        return level().getNearestPlayer(this, 10.0D);
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        bossEvent.removePlayer(player);
    }

    private void applyBloodMoonCurse(LivingEntity entity) {
        if (!level().isClientSide() && bloodMoonCooldown == 0) {
            for (Player player : level().players()) {
                player.addEffect(new MobEffectInstance(MobEffects.WITHER, 400, 1));
                player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 300, 1));
            }
            bloodMoonCooldown = 1200;
        }
    }

    private void triggerBatSwarm(LivingEntity entity) {
        if (!level().isClientSide() && !isTransformed) {
            for (int i = 0; i < 5; i++) {
                LilithBat bat = new LilithBat(level(), this);
                bat.setPos(
                        getX() + random.nextDouble() - 0.5,
                        getY() + random.nextDouble() * 0.5,
                        getZ() + random.nextDouble() - 0.5
                );
                level().addFreshEntity(bat);
                activeBats.add(bat);
            }

            isTransformed = true;
            transformTimer = 200;
            this.setInvisible(true);
            this.setNoAi(true);
            this.setSilent(true);
            this.setInvulnerable(true);
            this.setDeltaMovement(0, 0, 0);
            this.setTarget(null);  // clear target to prevent attacks

            playSound(SoundEvents.BAT_TAKEOFF, 1.0F, 1.0F);
            startAttackAnimation(3);
            playAttackSound(3);
        }
    }

    private void revertFromBatForm() {
        for (LilithBat bat : activeBats) {
            if (bat != null && !bat.isRemoved()) {
                bat.discard();
            }
        }
        activeBats.clear();

        isTransformed = false;
        this.setInvisible(false);
        this.setNoAi(false);
        this.setSilent(false);
        this.setInvulnerable(false);

        playSound(SoundEvents.BAT_AMBIENT, 1.0F, 1.0F);
    }

    public void notifyBatDeath(LilithBat bat) {
        activeBats.remove(bat);
        if (isTransformed && activeBats.isEmpty()) {
            revertFromBatForm();
        }
    }

    public static class LilithBat extends Bat {
        private int life = 200;
        private final LilithEntity owner;

        public LilithBat(Level level, LilithEntity owner) {
            super(EntityType.BAT, level);
            this.owner = owner;
        }

        @Override
        public boolean requiresCustomPersistence() {
            return false;
        }

        @Override
        public void tick() {
            super.tick();
            if (!level().isClientSide()) {
                life--;
                if (life <= 0) discard();
            }
        }

        @Override
        public void die(DamageSource cause) {
            super.die(cause);
            if (!level().isClientSide() && owner != null) {
                owner.notifyBatDeath(this);
            }
        }
    }

    // Dark Dominion spawns hostile skeleton minions with bows & targets players
    private void applyDarkDominion(LivingEntity entity) {
        if (!level().isClientSide() && !isUsingDarkDominion) {
            isUsingDarkDominion = true;
            startingY = this.getY();

            this.setNoAi(true);
            this.setInvulnerable(true);
            this.setTarget(null);
            setDeltaMovement(0, 0.2, 0); // float up slightly
            broadcastAbility("Dark Dominion");

            darkDominionMinions.clear();

            spawnDarkDominionWave();

            startAttackAnimation(2);
            playAttackSound(2);
        }
    }


    private void spawnDarkDominionWave() {
        AABB area = new AABB(blockPosition()).inflate(12);
        List<Player> nearbyPlayers = level().getEntitiesOfClass(Player.class, area);

        for (int i = 0; i < 6; i++) {
            EntityType<? extends Monster> type = switch (random.nextInt(6)) {
                case 0 -> EntityType.SKELETON;
                case 1 -> EntityType.WITHER_SKELETON;
                case 2 -> EntityType.ZOMBIE;
                case 3 -> EntityType.HUSK;
                case 4 -> EntityType.DROWNED;
                default -> EntityType.WITCH;
            };

            // create now requires EntitySpawnReason
            Monster mob = type.create(level(), EntitySpawnReason.MOB_SUMMONED);
            if (mob != null) {
                double xOff = random.nextDouble() * 6 - 3;
                double zOff = random.nextDouble() * 6 - 3;

                // use setPos instead of moveTo
                mob.setPos(getX() + xOff, getY(), getZ() + zOff);

                if (!nearbyPlayers.isEmpty()) {
                    mob.setTarget(nearbyPlayers.get(random.nextInt(nearbyPlayers.size())));
                }

                level().addFreshEntity(mob);
                darkDominionMinions.add(mob);
            }
        }
    }

    private void shadowDash(LivingEntity entity) {
        if (!level().isClientSide() && shadowDashCooldown == 0) {
            Player target = getNearestPlayer();
            if (target != null) {
                setDeltaMovement(target.getX() - getX(), 0.2, target.getZ() - getZ());
                target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
                target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 1));
                startAttackAnimation(1);
                playAttackSound(1);
            }
            shadowDashCooldown = 600;
        }
    }

    private void triggerDarkVeil(LivingEntity entity) {
        if (!level().isClientSide() && darkVeilCooldown == 0) {
            // Day-time change removed in 26.1 (world clocks)
            // If you really need night later, run the /time command via the server

            for (Player player : level().players()) {
                player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 200, 0));
                player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 300, 0));
            }
            darkVeilCooldown = 1800;
            startAttackAnimation(2);
            playAttackSound(2);
        }
    }

    private void startAttackAnimation(int type) {
        isAttacking = true;
        attackTimer = 20;
        attackType = type;
    }

    private void playAttackSound(int type) {
        SoundEvent sound = switch (type) {
            case 2 -> SoundEvents.WITCH_AMBIENT;
            case 3 -> SoundEvents.BAT_TAKEOFF;
            default -> SoundEvents.SPIDER_AMBIENT;
        };
        playSound(sound, 1.0F, 1.0F);
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        playSound(SoundEvents.ELDER_GUARDIAN_DEATH, 1.0F, 1.0F);

        if (!level().isClientSide()) {
            ServerLevel serverLevel = (ServerLevel) level();

            // 1. Guaranteed Soul
            this.spawnAtLocation(serverLevel, new ItemStack(ModItems.LILITH_SOUL.get()));

            // 2. High chance for Contract
            if (random.nextFloat() < 0.75f) {
                this.spawnAtLocation(serverLevel, new ItemStack(ModItems.LILITH_CONTRACT.get()));
            }

            // 3. Jackpot - Crimson Fang
            if (random.nextFloat() < 0.15f) {
                this.spawnAtLocation(serverLevel, new ItemStack(ModItems.CRIMSON_FANG.get()));
            }
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(state -> {
            if (isAttacking) {
                return switch (attackType) {
                    case 2 -> state.setAndContinue(ATTACK2_ANIM);
                    case 3 -> state.setAndContinue(ATTACK3_ANIM);
                    default -> state.setAndContinue(ATTACK_ANIM);
                };
            } else if (state.isMoving()) {
                return state.setAndContinue(WALK_ANIM);
            } else {
                return state.setAndContinue(IDLE_ANIM);
            }
        }));
    }

    @Override
    public boolean causeFallDamage(double fallDistance, float multiplier, DamageSource source) {
        return false;   // no fall damage for Lilith
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ELDER_GUARDIAN_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.ELDER_GUARDIAN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ELDER_GUARDIAN_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.ZOMBIE_STEP, 0.15F, 1.0F);
    }

    @Override
    public boolean isNoGravity() {
        if (isUsingDarkDominion) {
            return true; // disable gravity while using Dark Dominion
        }
        return super.isNoGravity();
    }

    // Override getTarget to prevent targeting while transformed (bat swarm)
    @Override
    public LivingEntity getTarget() {
        return isTransformed ? null : super.getTarget();
    }
}