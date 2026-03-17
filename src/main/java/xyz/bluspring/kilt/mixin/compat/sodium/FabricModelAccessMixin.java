package xyz.bluspring.kilt.mixin.compat.sodium;

import java.util.List;
import java.util.Set;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.caffeinemc.mods.sodium.client.services.SodiumModelData;
import net.caffeinemc.mods.sodium.client.services.SodiumModelDataContainer;
import net.caffeinemc.mods.sodium.client.world.LevelSlice;
import net.caffeinemc.mods.sodium.fabric.model.FabricModelAccess;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

@IfModLoaded("sodium")
@Mixin(FabricModelAccess.class)
public abstract class FabricModelAccessMixin {
    @Inject(method = "getModelRenderTypes", at = @At("HEAD"), cancellable = true)
    private void kilt$tryGetNeoRenderTypes(BlockAndTintGetter level, BakedModel model, BlockState state, BlockPos pos, RandomSource random, SodiumModelData modelData, CallbackInfoReturnable<Iterable<RenderType>> cir) {
        if (KiltHelper.INSTANCE.hasMethodOverride(model.getClass(), BakedModel.class, "getRenderTypes", BlockState.class, RandomSource.class, ModelData.class)) {
            cir.setReturnValue(model.getRenderTypes(state, random, (ModelData) (Object) modelData));
        }
    }

    @Inject(method = "getQuads", at = @At("HEAD"), cancellable = true)
    private void kilt$tryGetNeoQuads(BlockAndTintGetter level, BlockPos pos, BakedModel model, BlockState state, Direction face, RandomSource random, RenderType renderType, SodiumModelData modelData, CallbackInfoReturnable<List<BakedQuad>> cir) {
        if (KiltHelper.INSTANCE.hasMethodOverride(model.getClass(), BakedModel.class, "getQuads", BlockState.class, Direction.class, RandomSource.class, ModelData.class, RenderType.class)) {
            cir.setReturnValue(model.getQuads(state, face, random, (ModelData) (Object) modelData, renderType));
        }
    }

    /**
     * @author BluSpring
     * @reason We have model data with Kilt.
     */
    @Overwrite
    public SodiumModelDataContainer getModelDataContainer(Level level, SectionPos sectionPos) {
        Set<Long2ObjectMap.Entry<ModelData>> entrySet = level.getModelDataManager().getAt(sectionPos).long2ObjectEntrySet();
        Long2ObjectMap<SodiumModelData> modelDataMap = new Long2ObjectOpenHashMap<>(entrySet.size());

        for (Long2ObjectMap.Entry<ModelData> entry : entrySet) {
            modelDataMap.put(entry.getLongKey(), (SodiumModelData) (Object) entry.getValue());
        }

        return new SodiumModelDataContainer(modelDataMap);
    }

    @Inject(method = "getModelData", at = @At("HEAD"), cancellable = true)
    private void kilt$tryGetNeoModelData(LevelSlice slice, BakedModel model, BlockState state, BlockPos pos, SodiumModelData originalData, CallbackInfoReturnable<SodiumModelData> cir) {
        if (KiltHelper.INSTANCE.hasMethodOverride(model.getClass(), BakedModel.class, "getModelData", BlockAndTintGetter.class, BlockPos.class, BlockState.class, ModelData.class)) {
            cir.setReturnValue((SodiumModelData) (Object) model.getModelData(slice, pos, state, (ModelData) (Object) originalData));
        }
    }

    /**
     * @author BluSpring
     * @reason We have model data with Kilt.
     */
    @Overwrite
    public SodiumModelData getEmptyModelData() {
        return (SodiumModelData) (Object) ModelData.EMPTY;
    }
}
