// TRACKED HASH: 7806cbd7ecf0842aa5db2c08ecd295f2b0b0f3ed
package xyz.bluspring.kilt.injects.client.renderer.entity;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.fabricators_of_create.porting_lib.entity.MultiPartEntity;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.fml.ModLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.injections.client.renderer.entity.EntityRenderDispatcherInjection;

import java.util.Collections;
import java.util.Map;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherInject implements EntityRenderDispatcherInjection {
    @Shadow public Map<EntityType<?>, EntityRenderer<?>> renderers;

    @Shadow
    private Map<PlayerSkin.Model, EntityRenderer<? extends Player>> playerRenderers;

    @Override
    public Map<PlayerSkin.Model, EntityRenderer<? extends Player>> getSkinMap() {
        return Collections.unmodifiableMap(this.playerRenderers);
    }

    @Inject(method = "onResourceManagerReload", at = @At("TAIL"))
    private void kilt$addEntityRenderLayers(ResourceManager resourceManager, CallbackInfo ci, @Local EntityRendererProvider.Context context) {
        ModLoader.postEvent(new EntityRenderersEvent.AddLayers(this.renderers, this.playerRenderers, context));
    }

    @Definition(id = "entity", local = @Local(type = Entity.class, argsOnly = true))
    @Definition(id = "EnderDragon", type = EnderDragon.class)
    @Expression("entity instanceof EnderDragon")
    @WrapOperation(method = "renderHitbox", at = @At("MIXINEXTRAS:EXPRESSION"))
    private static boolean kilt$renderHitboxIfMultipart(Object object, Operation<Boolean> original, @Local(argsOnly = true) PoseStack poseStack, @Local(argsOnly = true) VertexConsumer buffer, @Local(argsOnly = true, ordinal = 0) float partialTick, @Local(argsOnly = true) Entity entity) {
        if (original.call(object))
            return true;

        if (((Entity) object).isMultipartEntity() && !(object instanceof MultiPartEntity)) {
            double d = -Mth.lerp(partialTick, entity.xOld, entity.getX());
            double e = -Mth.lerp(partialTick, entity.yOld, entity.getY());
            double f = -Mth.lerp(partialTick, entity.zOld, entity.getZ());

            for (EnderDragonPart enderDragonPart : ((EnderDragon)entity).getSubEntities()) {
                poseStack.pushPose();
                double g = d + Mth.lerp(partialTick, enderDragonPart.xOld, enderDragonPart.getX());
                double h = e + Mth.lerp(partialTick, enderDragonPart.yOld, enderDragonPart.getY());
                double i = f + Mth.lerp(partialTick, enderDragonPart.zOld, enderDragonPart.getZ());
                poseStack.translate(g, h, i);
                LevelRenderer.renderLineBox(poseStack, buffer, enderDragonPart.getBoundingBox().move(-enderDragonPart.getX(), -enderDragonPart.getY(), -enderDragonPart.getZ()), 0.25F, 1.0F, 0.0F, 1.0F);
                poseStack.popPose();
            }
        }

        return false;
    }
}