package net.isaiah.deathstatues.client.render.entity;

import net.isaiah.deathstatues.DeathStatuesClient;
import net.isaiah.deathstatues.client.network.AbstractClientDeathStatueEntity;
import net.isaiah.deathstatues.client.render.entity.model.DeathStatueEntityModel;
import net.isaiah.deathstatues.client.render.entity.feature.DeathStatueHeldItemFeatureRenderer;
import net.isaiah.deathstatues.entity.deathstatue.DeathStatueEntity;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerModelPart;
import net.minecraft.client.render.entity.feature.*;
import net.minecraft.client.render.entity.model.ArmorEntityModel;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

import java.util.Objects;

public class DeathStatueEntityRenderer extends LivingEntityRenderer<DeathStatueEntity, DeathStatueEntityModel<DeathStatueEntity>> {
    public DeathStatueEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new DeathStatueEntityModel<>(ctx.getPart(DeathStatuesClient.MODEL_STATUE_LAYER), false), 0.5F);
        this.addFeature(new ArmorFeatureRenderer<>(this, new ArmorEntityModel<>(ctx.getPart(EntityModelLayers.PLAYER_INNER_ARMOR)), new ArmorEntityModel<>(ctx.getPart(EntityModelLayers.PLAYER_OUTER_ARMOR)), ctx.getModelManager()));
        this.addFeature(new DeathStatueHeldItemFeatureRenderer<>(this, ctx.getHeldItemRenderer()));
        //this.addFeature(new StuckArrowsFeatureRenderer<AbstractClientDeathStatueEntity, DeathStatueEntityModel<AbstractClientDeathStatueEntity>>(ctx, this));
        //this.addFeature(new CapeFeatureRenderer(this));
        this.addFeature(new HeadFeatureRenderer<>(this, ctx.getModelLoader(), ctx.getHeldItemRenderer()));
        //this.addFeature(new ElytraFeatureRenderer<AbstractClientDeathStatueEntity, DeathStatueEntityModel<AbstractClientPlayerEntity>>(this, ctx.getModelLoader()));
        //this.addFeature(new StuckStingersFeatureRenderer<AbstractClientDeathStatueEntity, DeathStatueEntityModel<AbstractClientDeathStatueEntity>>(this));
    }

    @Override
    public void render(DeathStatueEntity deathStatueEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        this.setModelPose(deathStatueEntity);
        super.render(deathStatueEntity, f, g, matrixStack, vertexConsumerProvider, i);
    }

    @Override
    public Vec3d getPositionOffset(DeathStatueEntity deathStatueEntity, float f) {
        if (deathStatueEntity.isInSneakingPose()) {
            return new Vec3d(0.0, -0.125, 0.0);
        }
        return super.getPositionOffset(deathStatueEntity, f);
    }

    private void setModelPose(DeathStatueEntity deathStatueEntity) {
        DeathStatueEntityModel<DeathStatueEntity> deathStatueEntityModel = this.getModel();
        if (deathStatueEntity.isSpectator()) {
            deathStatueEntityModel.setVisible(false);
            deathStatueEntityModel.head.visible = true;
            deathStatueEntityModel.hat.visible = true;
        } else {
            deathStatueEntityModel.setVisible(true);
            /*deathStatueEntityModel.hat.visible = deathStatueEntity.isPartVisible(PlayerModelPart.HAT);
            deathStatueEntityModel.jacket.visible = deathStatueEntity.isPartVisible(PlayerModelPart.JACKET);
            deathStatueEntityModel.leftPants.visible = deathStatueEntity.isPartVisible(PlayerModelPart.LEFT_PANTS_LEG);
            deathStatueEntityModel.rightPants.visible = deathStatueEntity.isPartVisible(PlayerModelPart.RIGHT_PANTS_LEG);
            deathStatueEntityModel.leftSleeve.visible = deathStatueEntity.isPartVisible(PlayerModelPart.LEFT_SLEEVE);
            deathStatueEntityModel.rightSleeve.visible = deathStatueEntity.isPartVisible(PlayerModelPart.RIGHT_SLEEVE);*/
            deathStatueEntityModel.sneaking = deathStatueEntity.isInSneakingPose();
            BipedEntityModel.ArmPose armPose = DeathStatueEntityRenderer.getArmPose(deathStatueEntity, Hand.MAIN_HAND);
            BipedEntityModel.ArmPose armPose2 = DeathStatueEntityRenderer.getArmPose(deathStatueEntity, Hand.OFF_HAND);
            if (armPose.isTwoHanded()) {
                BipedEntityModel.ArmPose armPose3 = armPose2 = deathStatueEntity.getOffHandStack().isEmpty() ? BipedEntityModel.ArmPose.EMPTY : BipedEntityModel.ArmPose.ITEM;
            }
            if (deathStatueEntity.getMainArm() == Arm.RIGHT) {
                deathStatueEntityModel.rightArmPose = armPose;
                deathStatueEntityModel.leftArmPose = armPose2;
            } else {
                deathStatueEntityModel.rightArmPose = armPose2;
                deathStatueEntityModel.leftArmPose = armPose;
            }
        }
    }

    private static BipedEntityModel.ArmPose getArmPose(DeathStatueEntity deathStatueEntity, Hand hand) {
        ItemStack itemStack = deathStatueEntity.getStackInHand(hand);
        if (itemStack.isEmpty()) {
            return BipedEntityModel.ArmPose.EMPTY;
        }
        if (deathStatueEntity.getActiveHand() == hand && deathStatueEntity.getItemUseTimeLeft() > 0) {
            UseAction useAction = itemStack.getUseAction();
            if (useAction == UseAction.BLOCK) {
                return BipedEntityModel.ArmPose.BLOCK;
            }
            if (useAction == UseAction.BOW) {
                return BipedEntityModel.ArmPose.BOW_AND_ARROW;
            }
            if (useAction == UseAction.SPEAR) {
                return BipedEntityModel.ArmPose.THROW_SPEAR;
            }
            if (useAction == UseAction.CROSSBOW && hand == deathStatueEntity.getActiveHand()) {
                return BipedEntityModel.ArmPose.CROSSBOW_CHARGE;
            }
            if (useAction == UseAction.SPYGLASS) {
                return BipedEntityModel.ArmPose.SPYGLASS;
            }
            if (useAction == UseAction.TOOT_HORN) {
                return BipedEntityModel.ArmPose.TOOT_HORN;
            }
            if (useAction == UseAction.BRUSH) {
                return BipedEntityModel.ArmPose.BRUSH;
            }
        } else if (!deathStatueEntity.handSwinging && itemStack.isOf(Items.CROSSBOW) && CrossbowItem.isCharged(itemStack)) {
            return BipedEntityModel.ArmPose.CROSSBOW_HOLD;
        }
        return BipedEntityModel.ArmPose.ITEM;
    }

    @Override
    public Identifier getTexture(DeathStatueEntity entity) {
        return entity.getSkinTexture();
    }

    @Override
    protected void scale(DeathStatueEntity entity, MatrixStack matrices, float amount) {
        super.scale(entity, matrices, 0.9375f);
    }

    @Override
    protected void renderLabelIfPresent(DeathStatueEntity deathStatueEntity, Text text, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        Scoreboard scoreboard;
        ScoreboardObjective scoreboardObjective;
        double d = this.dispatcher.getSquaredDistanceToCamera(deathStatueEntity);
        matrixStack.push();
        if (d < 100.0 && (scoreboardObjective = (scoreboard = deathStatueEntity.getScoreboard()).getObjectiveForSlot(2)) != null) {
            ScoreboardPlayerScore scoreboardPlayerScore = scoreboard.getPlayerScore(deathStatueEntity.getEntityName(), scoreboardObjective);
            super.renderLabelIfPresent(deathStatueEntity, Text.literal(Integer.toString(scoreboardPlayerScore.getScore())).append(ScreenTexts.SPACE).append(scoreboardObjective.getDisplayName()), matrixStack, vertexConsumerProvider, i);
            Objects.requireNonNull(this.getTextRenderer());
            matrixStack.translate(0.0f, 9.0f * 1.15f * 0.025f, 0.0f);
        }
        super.renderLabelIfPresent(deathStatueEntity, text, matrixStack, vertexConsumerProvider, i);
        matrixStack.pop();
    }

    public void renderRightArm(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, DeathStatueEntity player) {
        this.renderArm(matrices, vertexConsumers, light, player, this.model.rightArm, this.model.rightSleeve);
    }

    public void renderLeftArm(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, DeathStatueEntity player) {
        this.renderArm(matrices, vertexConsumers, light, player, this.model.leftArm, this.model.leftSleeve);
    }

    private void renderArm(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, DeathStatueEntity player, ModelPart arm, ModelPart sleeve) {
        DeathStatueEntityModel<DeathStatueEntity> playerEntityModel = this.getModel();
        this.setModelPose(player);
        playerEntityModel.handSwingProgress = 0.0f;
        playerEntityModel.sneaking = false;
        playerEntityModel.leaningPitch = 0.0f;
        playerEntityModel.setAngles(player, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
        arm.pitch = 0.0f;
        arm.render(matrices, vertexConsumers.getBuffer(RenderLayer.getEntitySolid(player.getSkinTexture())), light, OverlayTexture.DEFAULT_UV);
        sleeve.pitch = 0.0f;
        sleeve.render(matrices, vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(player.getSkinTexture())), light, OverlayTexture.DEFAULT_UV);
    }

    @Override
    protected void setupTransforms(DeathStatueEntity deathStatueEntity, MatrixStack matrixStack, float f, float g, float h) {
        float i = deathStatueEntity.getLeaningPitch(h);
        if (deathStatueEntity.isFallFlying()) {
            super.setupTransforms(deathStatueEntity, matrixStack, f, g, h);
            float j = (float) deathStatueEntity.getRoll() + h;
            float k = MathHelper.clamp(j * j / 100.0f, 0.0f, 1.0f);
            if (!deathStatueEntity.isUsingRiptide()) {
                matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(k * (-90.0f - deathStatueEntity.getPitch())));
            }
            Vec3d vec3d = deathStatueEntity.getRotationVec(h);
            Vec3d vec3d2 = deathStatueEntity.lerpVelocity(h);
            double d = vec3d2.horizontalLengthSquared();
            double e = vec3d.horizontalLengthSquared();
            if (d > 0.0 && e > 0.0) {
                double l = (vec3d2.x * vec3d.x + vec3d2.z * vec3d.z) / Math.sqrt(d * e);
                double m = vec3d2.x * vec3d.z - vec3d2.z * vec3d.x;
                matrixStack.multiply(RotationAxis.POSITIVE_Y.rotation((float)(Math.signum(m) * Math.acos(l))));
            }
        } else if (i > 0.0f) {
            super.setupTransforms(deathStatueEntity, matrixStack, f, g, h);
            float j = deathStatueEntity.isTouchingWater() ? -90.0f - deathStatueEntity.getPitch() : -90.0f;
            float k = MathHelper.lerp(i, 0.0f, j);
            matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(k));
            if (deathStatueEntity.isInSwimmingPose()) {
                matrixStack.translate(0.0f, -1.0f, 0.3f);
            }
        } else {
            super.setupTransforms(deathStatueEntity, matrixStack, f, g, h);
        }
    }
}
