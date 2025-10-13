package xyz.bluspring.kilt.mixin.compat.sodium;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import net.caffeinemc.mods.sodium.client.world.LevelSlice;
import net.caffeinemc.mods.sodium.fabric.level.FabricLevelRenderHooks;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.event.AddSectionGeometryEvent;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.Function;

@IfModLoaded("sodium")
@Mixin(FabricLevelRenderHooks.class)
public class FabricLevelRenderHooksMixin {
    @Inject(method = "runChunkLayerEvents", at = @At("HEAD"))
    private void dispatchNeoRenderStageEvent(RenderType renderType, LevelRenderer levelRenderer, Matrix4f modelMatrix, Matrix4f projectionMatrix, int renderTick, Camera mainCamera, Frustum cullingFrustum, CallbackInfo ci) {
        ClientHooks.dispatchRenderStage(renderType, levelRenderer, modelMatrix, projectionMatrix, renderTick, mainCamera, cullingFrustum);
    }

    @ModifyReturnValue(method = "retrieveChunkMeshAppenders", at = @At("RETURN"))
    private List<?> gatherAdditionalRenderers(List<?> original, Level level, BlockPos origin) {
        return ClientHooks.gatherAdditionalRenderers(origin, level); // We can't use the original list so lets hope no one modifies this before us
    }

    @Inject(method = "runChunkMeshAppenders", at = @At("HEAD"), remap = false)
    private void addSectionGeometryEvent(List<?> renderers, Function<RenderType, VertexConsumer> typeToConsumer, LevelSlice slice, CallbackInfo ci) {
        AddSectionGeometryEvent.SectionRenderingContext context = new AddSectionGeometryEvent.SectionRenderingContext(typeToConsumer, slice, new PoseStack());
        for (Object o : renderers) {
            if (o instanceof AddSectionGeometryEvent.AdditionalSectionRenderer renderer) {
                renderer.render(context);
            }
        }
    }
}
