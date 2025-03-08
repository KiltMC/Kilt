package xyz.bluspring.kilt.forgeinjects.world.level.biome;

import com.google.common.base.Suppliers;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

@Mixin(BiomeSource.class)
public abstract class BiomeSourceInject {
    @Shadow @Final @Mutable
    private Set<Holder<Biome>> possibleBiomes;
    private Supplier<Set<Holder<Biome>>> lazyPossibleBiomes;

    @Inject(method = "<init>(Ljava/util/List;)V", at = @At("TAIL"))
    private void kilt$storeLazyConstructor(List<Holder<Biome>> possibleBiomes, CallbackInfo ci) {
        var biomes = this.possibleBiomes;
        this.lazyPossibleBiomes = Suppliers.memoize(() -> biomes);
    }

    @CreateInitializer
    protected BiomeSourceInject(Supplier<List<Holder<Biome>>> biomes) {
        this.possibleBiomes = new ObjectLinkedOpenHashSet<>();
        var oldBiomes = this.possibleBiomes;
        this.lazyPossibleBiomes = Suppliers.memoize(() -> {
            oldBiomes.addAll(biomes.get());
            return oldBiomes;
        });
    }

    @Inject(method = "possibleBiomes", at = @At("HEAD"))
    private void kilt$initPossibleBiomes(CallbackInfoReturnable<Set<Holder<Biome>>> cir) {
        this.lazyPossibleBiomes.get();
    }
}
