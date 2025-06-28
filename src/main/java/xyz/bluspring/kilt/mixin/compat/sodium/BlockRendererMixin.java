package xyz.bluspring.kilt.mixin.compat.sodium;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import me.jellysquid.mods.sodium.client.model.color.ColorProvider;
import me.jellysquid.mods.sodium.client.model.color.ColorProviderRegistry;
import me.jellysquid.mods.sodium.client.model.light.LightMode;
import me.jellysquid.mods.sodium.client.model.light.LightPipeline;
import me.jellysquid.mods.sodium.client.model.light.LightPipelineProvider;
import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuildBuffers;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderContext;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.material.DefaultMaterials;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.material.Material;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.ModelData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.injections.sodium.BlockRenderContextInjection;

import java.util.List;
import java.util.Objects;

@IfModLoaded("sodium")
@Mixin(BlockRenderer.class)
public abstract class BlockRendererMixin {
    @Unique private static final Object kilt$lock = new Object();

    @Shadow @Final private RandomSource random;

    @WrapMethod(method = "renderModel", remap = false)
    private void kilt$renderModelWithRenderTypes(
        BlockRenderContext ctx, ChunkBuildBuffers buffers, Operation<Void> original,
        // Kilt: These aren't used in this method, but they're here so I remember what is used.
        //       They're largely for caching, as otherwise it may become more inefficient for avoiding an @Overwrite.
        @Share("colorizer") LocalRef<ColorProvider<BlockState>> colorizerRef,
        @Share("lighter") LocalRef<LightPipeline> lighterRef,
        @Share("renderOffset") LocalRef<Vec3> renderOffsetRef,

        @Share("data") LocalRef<ModelData> modelDataRef,
        @Share("renderType") LocalRef<RenderType> renderTypeRef
    ) {
        ModelData data;
        synchronized (kilt$lock) {
            data = ctx.model().getModelData(ctx.world(), ctx.pos(), ctx.state(), ((BlockRenderContextInjection) ctx).kilt$getModelData(ctx.pos()));
        }

        modelDataRef.set(data);
        random.setSeed(ctx.seed());

        var renderTypes = ctx.model().getRenderTypes(ctx.state(), random, data);

        // Kilt: Somehow, the render types are empty.
        //       But rather than simply stopping it, we should go through the
        //       original pipeline just in case someone made it empty on purpose.
        if (renderTypes.isEmpty()) {
            original.call(ctx, buffers);
            return;
        }

        for (RenderType renderType : renderTypes) {
            renderTypeRef.set(renderType);
            original.call(ctx, buffers);
        }
    }

    @WrapOperation(method = "renderModel", at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/render/chunk/terrain/material/DefaultMaterials;forBlockState(Lnet/minecraft/world/level/block/state/BlockState;)Lme/jellysquid/mods/sodium/client/render/chunk/terrain/material/Material;"))
    private Material kilt$useCurrentRenderTypeForMaterial(BlockState state, Operation<Material> original, @Share("renderType") LocalRef<RenderType> renderTypeRef) {
        if (renderTypeRef.get() == null)
            return original.call(state);

        return DefaultMaterials.forRenderLayer(renderTypeRef.get());
    }

    @WrapOperation(method = "renderModel", at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/model/color/ColorProviderRegistry;getColorProvider(Lnet/minecraft/world/level/block/Block;)Lme/jellysquid/mods/sodium/client/model/color/ColorProvider;"))
    private ColorProvider<BlockState> kilt$tryReuseColorProvider(ColorProviderRegistry instance, Block block, Operation<ColorProvider<BlockState>> original, @Share("colorizer") LocalRef<ColorProvider<BlockState>> colorizerRef) {
        var existing = colorizerRef.get();
        if (existing != null)
            return existing;

        var current = original.call(instance, block);
        colorizerRef.set(current);

        return current;
    }

    @WrapOperation(method = "renderModel", at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/model/light/LightPipelineProvider;getLighter(Lme/jellysquid/mods/sodium/client/model/light/LightMode;)Lme/jellysquid/mods/sodium/client/model/light/LightPipeline;"), remap = false)
    private LightPipeline kilt$tryReuseLighter(LightPipelineProvider instance, LightMode type, Operation<LightPipeline> original, @Share("lighter") LocalRef<LightPipeline> lighterRef) {
        var existing = lighterRef.get();
        if (existing != null)
            return existing;

        var current = original.call(instance, type);
        lighterRef.set(current);

        return current;
    }

    @WrapOperation(method = "renderModel", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getOffset(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/phys/Vec3;"))
    private Vec3 kilt$tryReuseOffset(BlockState instance, BlockGetter blockGetter, BlockPos blockPos, Operation<Vec3> original, @Share("renderOffset") LocalRef<Vec3> renderOffsetRef) {
        var existing = renderOffsetRef.get();
        if (existing != null)
            return existing;

        var current = original.call(instance, blockGetter, blockPos);
        renderOffsetRef.set(current);

        return current;
    }

    @WrapOperation(method = "renderModel", at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/render/chunk/compile/pipeline/BlockRenderer;getGeometry(Lme/jellysquid/mods/sodium/client/render/chunk/compile/pipeline/BlockRenderContext;Lnet/minecraft/core/Direction;)Ljava/util/List;"))
    private List<BakedQuad> kilt$tryGetRenderTypeGeometry(BlockRenderer instance, BlockRenderContext ctx, Direction face, Operation<List<BakedQuad>> original, @Share("data") LocalRef<ModelData> modelDataRef, @Share("renderType") LocalRef<RenderType> renderTypeRef) {
        if (renderTypeRef.get() == null)
            return original.call(instance, ctx, face);

        return this.kilt$getGeometry(ctx, face, modelDataRef.get(), renderTypeRef.get());
    }

    @Unique
    private List<BakedQuad> kilt$getGeometry(BlockRenderContext ctx, Direction face, ModelData data, RenderType renderType) {
        var random = this.random;
        random.setSeed(ctx.seed());

        return ctx.model().getQuads(ctx.state(), face, random, data, renderType);
    }
}
