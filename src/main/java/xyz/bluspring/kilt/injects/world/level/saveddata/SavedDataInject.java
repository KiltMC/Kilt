package xyz.bluspring.kilt.injects.world.level.saveddata;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.injections.world.level.saveddata.SavedDataInjection;

import java.util.function.BiFunction;
import java.util.function.Supplier;

@Mixin(SavedData.class)
public abstract class SavedDataInject implements SavedDataInjection {
    // Kilt TODO: do we need to use the IO worker??

    @ModifyArg(method = "save(Ljava/io/File;Lnet/minecraft/core/HolderLookup$Provider;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/NbtIo;writeCompressed(Lnet/minecraft/nbt/CompoundTag;Ljava/nio/file/Path;)V"))
    private CompoundTag kilt$copyNbtTag(CompoundTag compoundTag) {
        return compoundTag.copy();
    }

    @Mixin(SavedData.Factory.class)
    public abstract static class FactoryInject<T extends SavedData> implements SavedDataInjection.FactoryInjection {
        public FactoryInject(Supplier<T> constructor, BiFunction<CompoundTag, HolderLookup.Provider, T> deserializer, DataFixTypes type) {
        }

        @CreateInitializer
        public FactoryInject(Supplier<T> constructor, BiFunction<CompoundTag, HolderLookup.Provider, T> deserializer) {
            this(constructor, deserializer, null);
        }
    }
}
