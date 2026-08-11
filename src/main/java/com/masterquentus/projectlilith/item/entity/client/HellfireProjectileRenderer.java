package com.masterquentus.projectlilith.item.entity.client;

import com.masterquentus.projectlilith.item.ModItems;
import com.masterquentus.projectlilith.item.entity.HellfireProjectile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class HellfireProjectileRenderer<T extends HellfireProjectile> extends EntityRenderer<T, HellfireProjectileRenderer.HellfireRenderState> {
    private static final Identifier FIRE_TEXTURE = Identifier.fromNamespaceAndPath("projectlilith", "textures/entity/projectiles/hellfire.png");
    private final ItemModelResolver itemModelResolver;

    public HellfireProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemModelResolver = context.getItemModelResolver();
    }

    @Override
    public HellfireRenderState createRenderState() {
        return new HellfireRenderState();
    }

    @Override
    public void extractRenderState(T entity, HellfireRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.tickCount = entity.tickCount;
        state.partialTicks = partialTicks;

        ItemStack stack = entity.getItem();
        if (stack.isEmpty()) {
            stack = new ItemStack(ModItems.HELLFIRE_CHARGE.get());
        }

        if (!stack.isEmpty()) {
            this.itemModelResolver.updateForTopItem(
                    state.itemRenderState,
                    stack,
                    ItemDisplayContext.GROUND,
                    entity.level(),
                    entity,
                    entity.getId()
            );
        } else {
            state.itemRenderState.clear();
        }
    }

    @Override
    public void submit(HellfireRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
        super.submit(state, poseStack, collector, cameraState);

        poseStack.pushPose();
        poseStack.scale(1.5F, 1.5F, 1.5F);
        poseStack.mulPose(cameraState.orientation);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));

        // LAYER 1: The Core Charge (Using GROUND display context from your old code)
        if (!state.itemRenderState.isEmpty()) {
            state.itemRenderState.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        }

        // LAYER 2: The Animated Fire
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.2D, -0.05D);
        poseStack.scale(1.2F, 1.2F, 1.2F);

        RenderType renderType = RenderTypes.entityTranslucentEmissive(FIRE_TEXTURE);

        collector.submitCustomGeometry(poseStack, renderType, (pose, fireBuffer) -> {
            float frameHeight = 1.0F / 16.0F;
            int frame = (int)(((state.tickCount + state.partialTicks) / 2.0F) % 16);
            float v0 = frame * frameHeight;
            float v1 = v0 + frameHeight;

            Matrix4f matrix4f = pose.pose();
            Matrix3f matrix3f = pose.normal();

            // Exact vertex layout from your old 1.20.1 implementation
            vertex(fireBuffer, matrix4f, matrix3f, -0.4F, -0.2F, 0.0F, v1);
            vertex(fireBuffer, matrix4f, matrix3f,  0.4F, -0.2F, 1.0F, v1);
            vertex(fireBuffer, matrix4f, matrix3f,  0.4F,  0.6F, 1.0F, v0);
            vertex(fireBuffer, matrix4f, matrix3f, -0.4F,  0.6F, 0.0F, v0);
        });

        poseStack.popPose();
        poseStack.popPose();
    }

    private static void vertex(VertexConsumer consumer, Matrix4f matrix4f, Matrix3f matrix3f, float x, float y, float u, float v) {
        consumer.addVertex(matrix4f, x, y, 0.0F)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(15728880)
                .setNormal(0.0F, 1.0F, 0.0F);
    }

    public static class HellfireRenderState extends EntityRenderState {
        public final ItemStackRenderState itemRenderState = new ItemStackRenderState();
        public int tickCount;
        public float partialTicks;
    }
}