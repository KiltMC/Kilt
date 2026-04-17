package xyz.bluspring.kilt.injects.client.renderer.chunk;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.injections.client.renderer.chunk.RenderRegionCacheInjection;

import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.client.renderer.chunk.RenderRegionCache;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

@Mixin(RenderRegionCache.class)
public abstract class RenderRegionCacheInject implements RenderRegionCacheInjection {
    @Shadow
    @Nullable
    public abstract RenderChunkRegion createRegion(Level level, SectionPos sectionPos);

    @Unique private final ThreadLocal<Boolean> kilt$nullForEmpty = ThreadLocal.withInitial(() -> true);

    @Override
    public RenderChunkRegion createRegion(Level level, SectionPos pos, boolean nullForEmpty) {
        try {
            this.kilt$nullForEmpty.set(nullForEmpty);
            return this.createRegion(level, pos);
        } finally {
            this.kilt$nullForEmpty.set(true);
        }
    }

    @WrapOperation(method = "createRegion", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/LevelChunk;isSectionEmpty(I)Z"))
    private boolean kilt$checkReturnNullForEmpty(LevelChunk instance, int i, Operation<Boolean> original) {
        return this.kilt$nullForEmpty.get() && original.call(instance, i);
    }

    @WrapOperation(method = "createRegion", at = @At(value = "NEW", target = "(Lnet/minecraft/world/level/Level;II[Lnet/minecraft/client/renderer/chunk/RenderChunk;)Lnet/minecraft/client/renderer/chunk/RenderChunkRegion;"))
    private RenderChunkRegion kilt$attachModelDataManager(Level level, int minChunkX, int minChunkZ, RenderChunk[] chunks, Operation<RenderChunkRegion> original, @Local(argsOnly = true) SectionPos pos) {
        RenderChunkRegion region = original.call(level, minChunkX, minChunkZ, chunks);
        int sectionMinY = pos.getY() - RenderChunkRegion.RADIUS;
        int sectionMaxY = pos.getY() + RenderChunkRegion.RADIUS;
        var modelDataManager = level.getModelDataManager().snapshotSectionRegion(minChunkX, sectionMinY, minChunkZ, pos.getX() + RenderChunkRegion.RADIUS, sectionMaxY, pos.getZ() + RenderChunkRegion.RADIUS);
        region.kilt$setModelDataSnapshot(modelDataManager);

        return region;
    }

    @Override
    public void kilt$setNullForEmpty(boolean value) {
        this.kilt$nullForEmpty.set(value);
    }
}
