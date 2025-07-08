package xyz.bluspring.kilt.mixin.compat.sodium;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderContext;
import me.jellysquid.mods.sodium.client.world.WorldSlice;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.client.model.data.ModelData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import xyz.bluspring.kilt.injections.sodium.BlockRenderContextInjection;

import java.util.Map;

@IfModLoaded("sodium")
@Mixin(BlockRenderContext.class)
public abstract class BlockRenderContextMixin implements BlockRenderContextInjection {
    @Shadow public abstract WorldSlice world();

    @Override
    public ModelData kilt$getModelData(BlockPos pos) {
        var modelDataManager = this.world().getModelDataManager();

        if (modelDataManager == null)
            return ModelData.EMPTY;

        return modelDataManager.getAt(pos);
    }
}
