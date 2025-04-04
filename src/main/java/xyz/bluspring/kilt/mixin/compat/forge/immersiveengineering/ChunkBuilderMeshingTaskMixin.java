package xyz.bluspring.kilt.mixin.compat.forge.immersiveengineering;

import com.llamalad7.mixinextras.sugar.Local;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import me.jellysquid.mods.sodium.client.model.quad.properties.ModelQuadFacing;
import me.jellysquid.mods.sodium.client.render.SodiumWorldRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSection;
import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuildBuffers;
import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuildContext;
import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuildOutput;
import me.jellysquid.mods.sodium.client.render.chunk.compile.tasks.ChunkBuilderMeshingTask;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.material.DefaultMaterials;
import me.jellysquid.mods.sodium.client.util.task.CancellationToken;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.compat.immersive_engineering.SodiumIEVertexConsumer;

@IfModLoaded(value = "sodium", maxVersion = "0.6.0")
@Pseudo
@Mixin(ChunkBuilderMeshingTask.class)
public abstract class ChunkBuilderMeshingTaskMixin {
    @Shadow @Final private RenderSection render;

    @Inject(method = "execute(Lme/jellysquid/mods/sodium/client/render/chunk/compile/ChunkBuildContext;Lme/jellysquid/mods/sodium/client/util/task/CancellationToken;)Lme/jellysquid/mods/sodium/client/render/chunk/compile/ChunkBuildOutput;", at = @At(value = "FIELD", target = "Lme/jellysquid/mods/sodium/client/render/chunk/terrain/DefaultTerrainRenderPasses;ALL:[Lme/jellysquid/mods/sodium/client/render/chunk/terrain/TerrainRenderPass;", shift = At.Shift.AFTER, remap = false), remap = false)
    private void kilt$tryBuildImmersiveConnections(ChunkBuildContext buildContext, CancellationToken cancellationToken, CallbackInfoReturnable<ChunkBuildOutput> cir, @Local(ordinal = 0) BlockPos.MutableBlockPos blockPos) {
        ChunkBuildBuffers buffers = buildContext.buffers;
        blockPos.set(this.render.getOriginX(), this.render.getOriginY(), this.render.getOriginZ());

        // Kilt: Create an Immersive Engineering handler for Sodium compatibility within Kilt
        SodiumIEVertexConsumer.Companion.renderConnectionsInSection(renderType -> {
            var material = DefaultMaterials.forRenderLayer(renderType);
            var builder = buffers.get(material).getVertexBuffer(ModelQuadFacing.UNASSIGNED);
            return new SodiumIEVertexConsumer(builder, material);
        }, ((SodiumWorldRendererAccessor) SodiumWorldRenderer.instance()).getWorld(), blockPos);
    }
}
