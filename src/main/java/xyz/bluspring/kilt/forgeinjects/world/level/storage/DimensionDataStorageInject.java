package xyz.bluspring.kilt.forgeinjects.world.level.storage;

import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(DimensionDataStorage.class)
public abstract class DimensionDataStorageInject {
    @Shadow @Final private Map<String, SavedData> cache;

    // TODO: do we actually need to implement this?
    /*@Inject(method = "get", at = @At(value = "INVOKE", target = "Ljava/util/Map;containsKey(Ljava/lang/Object;)Z"), cancellable = true)
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
    }*/
}
