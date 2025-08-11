package xyz.bluspring.kilt.injects.client.renderer;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.fml.ModLoader;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.injections.client.renderer.LevelRendererInjection;
import xyz.bluspring.kilt.mixin.LevelRendererAccessor;

import java.util.List;
import java.util.function.Consumer;

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

    // Kilt: we don't need to implement the reach checks, someone else can handle that.

    @ModifyReturnValue(method = "getFov", at = @At(value = "RETURN", ordinal = 1))
    private double kilt$getForgeFieldOfView(double original, Camera activeRenderInfo, float partialTicks, boolean useFOVSetting) {
        return ClientHooks.getFieldOfView((GameRenderer) (Object) this, activeRenderInfo, partialTicks, original, useFOVSetting);
    }

    @ModifyExpressionValue(method = "render", at = @At(value = "CONSTANT", args = "floatValue=21000.0"))
    private float kilt$tryGetGuiFarPlane(float original) {
        if (original != 21000)
            return original;

        return ClientHooks.getGuiFarPlane();
    }

    @ModifyExpressionValue(method = "render", at = @At(value = "CONSTANT", args = "floatValue=-11000.0"))
    private float kilt$tryMoveGuiFarPlane(float original) {
        if (original != -11000)
            return original;

        return 1000f - ClientHooks.getGuiFarPlane();
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;renderWithTooltip(Lnet/minecraft/client/gui/GuiGraphics;IIF)V"))
    private void kilt$callForgeDrawScreenEvent(Screen instance, GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, Operation<Void> original) {
        ClientHooks.kilt$drawScreen(instance, guiGraphics, mouseX, mouseY, partialTick, original);
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setup(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/world/entity/Entity;ZZF)V", shift = At.Shift.AFTER))
    private void kilt$setupCamera(float partialTicks, long finishTimeNano, PoseStack poseStack, CallbackInfo ci, @Local Camera camera) {
        var cameraSetup = ClientHooks.onCameraSetup((GameRenderer) (Object) this, camera, partialTicks);
        camera.setAnglesInternal(cameraSetup.getYaw(), cameraSetup.getPitch());

        poseStack.mulPose(Axis.ZP.rotationDegrees(cameraSetup.getRoll()));
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;renderLevel(Lcom/mojang/blaze3d/vertex/PoseStack;FJZLnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/GameRenderer;Lnet/minecraft/client/renderer/LightTexture;Lorg/joml/Matrix4f;)V", shift = At.Shift.AFTER))
    private void kilt$handleForgeRenderLastEvent(float partialTicks, long finishTimeNano, PoseStack poseStack, CallbackInfo ci, @Local(ordinal = 1) PoseStack poseStack2, @Local Matrix4f matrix, @Local Camera camera) {
        this.minecraft.getProfiler().popPush("forge_render_last");
        ClientHooks.dispatchRenderStage(RenderLevelStageEvent.Stage.AFTER_LEVEL, this.minecraft.levelRenderer, poseStack2, matrix, ((LevelRendererAccessor) this.minecraft.levelRenderer).getTicks(), camera, ((LevelRendererInjection) this.minecraft.levelRenderer).getFrustum());
    }
}
