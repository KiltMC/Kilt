package xyz.bluspring.kilt.injects.client.renderer.entity;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.vertex.PoseStack;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererInject<T extends Entity> {
    @ModifyExpressionValue(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;shouldShowName(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean kilt$callRenderNameTagEvent(boolean original, T entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, @Share("event") LocalRef<RenderNameTagEvent> eventRef) {
        eventRef.set(new RenderNameTagEvent(entity, entity.getDisplayName(), (EntityRenderer<?>) (Object) this, poseStack, bufferSource, packedLight, partialTick));
        NeoForge.EVENT_BUS.post(eventRef.get());
        return eventRef.get().canRender().isTrue() || eventRef.get().canRender().isDefault() && original;
    }

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;renderNameTag(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/network/chat/Component;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IF)V"))
    private Component kilt$useEventContent(Component component, @Share("event") LocalRef<RenderNameTagEvent> eventRef) {
        if (eventRef.get() != null) {
            if (eventRef.get().getContent() != eventRef.get().getOriginalContent()) {
                return eventRef.get().getContent();
            }
        }

        return component;
    }

    @Definition(id = "d", local = @Local(type = double.class))
    @Expression("d > 4096.0")
    @WrapOperation(method = "renderNameTag", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$callNameplateInRenderDistance(double left, double right, Operation<Boolean> original, @Local(argsOnly = true) T entity) {
        return !ClientHooks.isNameplateInRenderDistance(entity, left) || original.call(left, right);
    }
}
