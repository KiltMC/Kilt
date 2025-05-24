package xyz.bluspring.kilt.forgeinjects.world.level.storage;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraftforge.common.util.DummySavedData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.function.Function;

@Mixin(DimensionDataStorage.class)
public abstract class DimensionDataStorageInject {
    @Shadow @Final private Map<String, SavedData> cache;

    @Inject(method = "get", at = @At(value = "INVOKE", target = "Ljava/util/Map;containsKey(Ljava/lang/Object;)Z"), cancellable = true)
    private <T> void kilt$checkIsSavedDataDummy(Function<CompoundTag, T> loadFunction, String name, CallbackInfoReturnable<T> cir, @Local SavedData data) {
        if (data == DummySavedData.DUMMY)
            cir.setReturnValue(null);
    }

    @Inject(method = "get", at = @At("TAIL"), cancellable = true)
    private <T> void kilt$storeDummyIntoCache(Function<CompoundTag, T> loadFunction, String name, CallbackInfoReturnable<T> cir, @Local SavedData data) {
        if (data == null && this.cache.containsKey(name)) {
            this.cache.put(name, DummySavedData.DUMMY);
            cir.setReturnValue(null);
        }
    }
}
