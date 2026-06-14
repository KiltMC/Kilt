package xyz.bluspring.kilt.injects.client.renderer;

import java.util.List;
import java.util.function.Consumer;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

@Mixin(GameRenderer.class)
public abstract class GameRendererInject {
    @Shadow private @Nullable PostChain postEffect;

    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "checkEntityPostEffect", at = @At("TAIL"))
    private void kilt$callLoadEntityShaderEvent(Entity entity, CallbackInfo ci) {
        if (this.postEffect == null) {
            ClientHooks.loadEntityShader(entity, (GameRenderer) (Object) this);
        }
    }

    @Definition(id = "list2", local = @Local(type = List.class, ordinal = 1))
    @Definition(id = "add", method = "Ljava/util/List;add(Ljava/lang/Object;)Z")
    @Definition(id = "ShaderInstance", type = ShaderInstance.class)
    @Definition(id = "of", method = "Lcom/mojang/datafixers/util/Pair;of(Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;")
    @Expression("list2.add(of(new ShaderInstance(?, 'rendertype_gui_ghost_recipe_overlay', ?), ?))")
    @Inject(
        method = "reloadShaders",
        at = @At(value = "MIXINEXTRAS:EXPRESSION", shift = At.Shift.AFTER)
    )
    private void kilt$registerShaders(ResourceProvider resourceProvider, CallbackInfo ci, @Local(ordinal = 1) List<Pair<ShaderInstance, Consumer<ShaderInstance>>> list) {
        ModLoader.postEvent(new RegisterShadersEvent(resourceProvider, list));
    }

    @ModifyReturnValue(method = "getFov", at = @At(value = "RETURN", ordinal = 1))
    private double kilt$getForgeFieldOfView(double original, Camera activeRenderInfo, float partialTicks, boolean useFOVSetting) {
        return ClientHooks.getFieldOfView((GameRenderer) (Object) this, activeRenderInfo, partialTicks, original, useFOVSetting);
    }

    @Definition(id = "livingEntity", local = @Local(type = LivingEntity.class))
    @Definition(id = "hurtDuration", field = "Lnet/minecraft/world/entity/LivingEntity;hurtDuration:I")
    @Expression("livingEntity.hurtDuration")
    @Inject(method = "bobHurt", at = @At("MIXINEXTRAS:EXPRESSION"), cancellable = true)
    private void kilt$avoidScreenShakeIfNoFlinch(PoseStack poseStack, float partialTicks, CallbackInfo ci, @Local LivingEntity entity) {
        var lastSrc = entity.getLastDamageSource();
        if (lastSrc != null && lastSrc.is(Tags.DamageTypes.NO_FLINCH))
            ci.cancel();
    }

    @ModifyExpressionValue(method = "render", at = @At(value = "CONSTANT", args = "floatValue=21000.0"))
    private float kilt$tryGetGuiFarPlane(float original) {
        float diff = 21000 - original; // Kilt: compatibility :D
        return diff + ClientHooks.getGuiFarPlane();
    }

    @ModifyExpressionValue(method = "render", at = @At(value = "CONSTANT", args = "floatValue=-11000.0"))
    private float kilt$tryMoveGuiFarPlane(float original) {
        float diff = -11000 - original; // Kilt: compatibility :D
        return diff + (10000f - ClientHooks.getGuiFarPlane());
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;renderWithTooltip(Lnet/minecraft/client/gui/GuiGraphics;IIF)V"))
    private void kilt$callForgeDrawScreenEvent(Screen instance, GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, Operation<Void> original) {
        ClientHooks.kilt$drawScreen(instance, guiGraphics, mouseX, mouseY, partialTick, original);
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;renderLevel(Lnet/minecraft/client/DeltaTracker;ZLnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/GameRenderer;Lnet/minecraft/client/renderer/LightTexture;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;)V", shift = At.Shift.AFTER))
    private void kilt$handleForgeRenderLastEvent(DeltaTracker deltaTracker, CallbackInfo ci, @Local(ordinal = 1) Matrix4f modelViewMatrix, @Local(ordinal = 0) Matrix4f projMatrix, @Local Camera camera) {
        this.minecraft.getProfiler().popPush("neoforge_render_last");
        ClientHooks.dispatchRenderStage(RenderLevelStageEvent.Stage.AFTER_LEVEL, this.minecraft.levelRenderer, null, modelViewMatrix, projMatrix, this.minecraft.levelRenderer.getTicks(), camera, this.minecraft.levelRenderer.getFrustum());
    }
}
