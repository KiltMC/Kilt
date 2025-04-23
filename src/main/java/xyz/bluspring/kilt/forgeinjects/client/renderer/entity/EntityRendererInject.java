package xyz.bluspring.kilt.forgeinjects.client.renderer.entity;

import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.RenderNameTagEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererInject<T extends Entity> {
    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;shouldShowName(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean kilt$checkShouldShowName(EntityRenderer<T> instance, T entity, Operation<Boolean> original, @Share("event") LocalRef<RenderNameTagEvent> eventRef,
                                             @Local(argsOnly = true) PoseStack poseStack, @Local(argsOnly = true) MultiBufferSource bufferSource,
                                             @Local(argsOnly = true) int packedLight, @Local(argsOnly = true, ordinal = 1) float partialTick) {
        var event = new RenderNameTagEvent(entity, entity.getDisplayName(), (EntityRenderer<?>) (Object) this, poseStack, bufferSource, packedLight, partialTick);
        MinecraftForge.EVENT_BUS.post(event);
        eventRef.set(event);

        //noinspection MixinExtrasOperationParameters
        return event.getResult() != Event.Result.DENY && (event.getResult() == Event.Result.ALLOW || original.call(instance, entity));
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getDisplayName()Lnet/minecraft/network/chat/Component;"))
    private Component kilt$useEventDisplayName(Entity instance, Operation<Component> original, @Share("event") LocalRef<RenderNameTagEvent> eventRef) {
        var event = eventRef.get();

        if (event != null) {
            if (event.getOriginalContent() != event.getContent()) {
                return event.getContent();
            }
        }

        return original.call(instance);
    }

    @Expression("? > 4096.0")
    @ModifyExpressionValue(method = "renderNameTag", at = @At(value = "MIXINEXTRAS:EXPRESSION", ordinal = 0))
    private boolean kilt$checkInNameplateDistance(boolean original, @Local double distance, @Local(argsOnly = true) T entity) {
        return original || !ForgeHooksClient.isNameplateInRenderDistance(entity, distance);
    }
}
