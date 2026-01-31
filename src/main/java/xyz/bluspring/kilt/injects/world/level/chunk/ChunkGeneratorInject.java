package xyz.bluspring.kilt.injects.world.level.chunk;

import com.google.common.base.Supplier;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.level.biome.FeatureSorter;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.neoforged.neoforge.common.util.Lazy;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.injections.world.level.chunk.ChunkGeneratorInjection;

import java.util.List;

@Mixin(value = ChunkGenerator.class, priority = 10000) // We want our mixin to come in as late as possible to capture everyone.
public abstract class ChunkGeneratorInject implements ChunkGeneratorInjection {
    @Shadow @Final @Mutable private java.util.function.Supplier<List<FeatureSorter.StepFeatureData>> featuresPerStep;
    @Unique private Lazy<List<FeatureSorter.StepFeatureData>> kilt$lazyFeaturesPerStep;

    @WrapOperation(method = "<init>(Lnet/minecraft/world/level/biome/BiomeSource;Ljava/util/function/Function;)V", at = @At(value = "INVOKE", target = "Lcom/google/common/base/Suppliers;memoize(Lcom/google/common/base/Supplier;)Lcom/google/common/base/Supplier;"))
    private <T> Supplier<T> kilt$storeLazyFeatures(Supplier<T> delegate, Operation<Supplier<T>> original) {
        Supplier<T> memoized = original.call(delegate);
        this.kilt$lazyFeaturesPerStep = Lazy.of(() -> ((Supplier<List<FeatureSorter.StepFeatureData>>) delegate).get());
        return memoized;
    }

    @Override
    public void refreshFeaturesPerStep() {
        this.kilt$lazyFeaturesPerStep.invalidate();
        this.featuresPerStep = this.kilt$lazyFeaturesPerStep;
    }
}
