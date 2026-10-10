package xyz.bluspring.kilt.injects.world.level.saveddata;

import java.util.function.Supplier;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.serialization.Codec;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.helpers.RecordMixinRefMaps;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.injections.world.level.saveddata.SavedDataTypeInjection;

import net.minecraft.resources.Identifier;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

@Mixin(SavedDataType.class)
public abstract class SavedDataTypeInject<T extends SavedData> implements SavedDataTypeInjection<T> {
    @Shadow
    public abstract Supplier<T> constructor();

    @Shadow
    @Final
    private Codec<T> codec;

    public SavedDataTypeInject(Identifier id, Supplier<T> constructor, Codec<T> codec, @Nullable DataFixTypes dataFixType) {
    }

    @CreateInitializer
    public SavedDataTypeInject(Identifier id, Supplier<T> constructor, Codec<T> codec) {
        this(id, constructor, codec, null);
    }

    @CreateInitializer
    public SavedDataTypeInject(Identifier id, Factory<T> factory, Factory<Codec<T>> codecFactory, @Nullable DataFixTypes dataFixType) {
        this(id, () -> factory.create(null), null, dataFixType);
        RecordMixinRefMaps.EXTENDED_SAVED_DATA_TYPE.put((SavedDataType<?>) (Object) this, new RecordMixinRefMaps.ExtendedSavedDataType<>(factory, codecFactory));
    }

    @CreateInitializer
    public SavedDataTypeInject(Identifier id, Factory<T> factory, Factory<Codec<T>> codecFactory) {
        this(id, factory, codecFactory, null);
    }

    @ModifyReturnValue(method = "codec", at = @At("RETURN"))
    private Codec<T> kilt$useCodecFactory(Codec<T> original) {
        var extended = RecordMixinRefMaps.EXTENDED_SAVED_DATA_TYPE.get((SavedDataType<?>) (Object) this);
        if (original == null && extended != null) {
            return (Codec<T>) extended.codecFactory().create(null);
        }

        return original;
    }

    @Override
    public Factory<T> factory() {
        var extended = RecordMixinRefMaps.EXTENDED_SAVED_DATA_TYPE.get((SavedDataType<?>) (Object) this);

        if (extended != null) {
            return (Factory<T>) extended.factory();
        }

        return _ -> this.constructor().get();
    }

    @Override
    public Factory<Codec<T>> codecFactory() {
        var extended = RecordMixinRefMaps.EXTENDED_SAVED_DATA_TYPE.get((SavedDataType<?>) (Object) this);

        if (extended != null) {
            return (Factory<Codec<T>>) (Object) extended.codecFactory();
        }

        return _ -> this.codec;
    }
}
