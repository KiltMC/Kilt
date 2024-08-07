package xyz.bluspring.kilt.mixin.compat.sodium;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import me.jellysquid.mods.sodium.client.model.color.ColorProvider;
import me.jellysquid.mods.sodium.client.model.color.ColorProviderRegistry;
import me.jellysquid.mods.sodium.client.model.light.LightMode;
import me.jellysquid.mods.sodium.client.model.light.LightPipeline;
import me.jellysquid.mods.sodium.client.model.light.LightPipelineProvider;
import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuildBuffers;
import me.jellysquid.mods.sodium.client.render.chunk.compile.buffers.ChunkModelBuilder;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderContext;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.material.DefaultMaterials;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.material.Material;
import me.jellysquid.mods.sodium.client.util.DirectionUtil;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.ModelData;
import org.spongepowered.asm.mixin.*;
import xyz.bluspring.kilt.injections.sodium.BlockRenderContextInjection;

import java.util.List;

@Mixin(value = BlockRenderer.class, remap = false)
public abstract class BlockRendererMixin {

    @Shadow @Final private ColorProviderRegistry colorProviderRegistry;

    @Shadow @Final private LightPipelineProvider lighters;

    @Shadow protected abstract LightMode getLightingMode(BlockState state, BakedModel model);

    @Shadow protected abstract List<BakedQuad> getGeometry(BlockRenderContext ctx, Direction face);

    @Shadow protected abstract boolean isFaceVisible(BlockRenderContext ctx, Direction face);

    @Shadow protected abstract void renderQuadList(BlockRenderContext ctx, Material material, LightPipeline lighter, ColorProvider<BlockState> colorizer, Vec3 offset, ChunkModelBuilder builder, List<BakedQuad> quads, Direction cullFace);

    @Shadow @Final private RandomSource random;

    /**
     * @author AlphaMode
     * @reason Temp overwrite for custom model support
     */
    @Overwrite
    public void renderModel(BlockRenderContext ctx, ChunkBuildBuffers buffers) {
        var data = ((BlockRenderContextInjection) ctx).klit$data();

        ColorProvider<BlockState> colorizer = this.colorProviderRegistry.getColorProvider(ctx.state().getBlock());

        LightPipeline lighter = this.lighters.getLighter(this.getLightingMode(ctx.state(), ctx.model()));
        Vec3 renderOffset;

        if (ctx.state().hasOffsetFunction()) {
            renderOffset = ctx.state().getOffset(ctx.world(), ctx.pos());
        } else {
            renderOffset = Vec3.ZERO;
        }

        random.setSeed(ctx.seed());
        for (RenderType renderType : ctx.model().getRenderTypes(ctx.state(), random, data)) {
            var material = DefaultMaterials.forRenderLayer(renderType);
            var meshBuilder = buffers.get(material);

            for (Direction face : DirectionUtil.ALL_DIRECTIONS) {
                List<BakedQuad> quads = this.getGeometry(ctx, face, data, renderType);

                if (!quads.isEmpty() && this.isFaceVisible(ctx, face)) {
                    this.renderQuadList(ctx, material, lighter, colorizer, renderOffset, meshBuilder, quads, face);
                }
            }

            List<BakedQuad> all = this.getGeometry(ctx, null, data, renderType);

            if (!all.isEmpty()) {
                this.renderQuadList(ctx, material, lighter, colorizer, renderOffset, meshBuilder, all, null);
            }
        }
    }

    private List<BakedQuad> getGeometry(BlockRenderContext ctx, Direction face, ModelData data, RenderType renderType) {
        var random = this.random;
        random.setSeed(ctx.seed());

        return ctx.model().getQuads(ctx.state(), face, random, data, renderType);
    }
}
