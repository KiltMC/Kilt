package xyz.bluspring.kilt.injects.world.level.storage.loot;

import java.util.Optional;
import java.util.function.BiConsumer;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.conditions.ConditionalOps;
import net.neoforged.neoforge.event.EventHooks;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.helpers.RecordMixinRefMaps;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.injections.world.level.storage.loot.LootDataTypeInjection;

import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.LootTable;

@Mixin(LootDataType.class)
public abstract class LootDataTypeInject<T> implements LootDataTypeInjection<T> {
    @Shadow
    public abstract Codec<T> codec();

    @ModifyExpressionValue(method = "<clinit>", at = @At(value = "NEW", target = "(Lnet/minecraft/resources/ResourceKey;Lcom/mojang/serialization/Codec;Lnet/minecraft/world/level/storage/loot/LootDataType$Validator;)Lnet/minecraft/world/level/storage/loot/LootDataType;"))
    private static LootDataType<LootTable> kilt$attachExtendedLootTableData(LootDataType<LootTable> original) {
        RecordMixinRefMaps.EXTENDED_LOOT_DATA_TYPE.put(original, new RecordMixinRefMaps.ExtendedLootDataType<>(LootTable.EMPTY, ConditionalOps.createConditionalCodec(original.codec()), (table, id) -> {
            try {
                table.setLootTableId(id);
            } catch (Throwable ignored) {}
        }));
        return original;
    }

    public LootDataTypeInject(ResourceKey<Registry<T>> registryKey, Codec<T> codec, LootDataType.Validator<T> validator) {}

    @CreateInitializer
    public LootDataTypeInject(ResourceKey<Registry<T>> registryKey, Codec<T> codec, LootDataType.Validator<T> validator, @Nullable T defaultValue, BiConsumer<T, ResourceLocation> idSetter) {
        this(registryKey, codec, validator, defaultValue, ConditionalOps.createConditionalCodec(codec), idSetter);
    }

    @CreateInitializer
    public LootDataTypeInject(ResourceKey<Registry<T>> registryKey, Codec<T> codec, LootDataType.Validator<T> validator, @Nullable T defaultValue, Codec<Optional<T>> conditionalCodec, BiConsumer<T, ResourceLocation> idSetter) {
        this(registryKey, codec, validator);
        RecordMixinRefMaps.EXTENDED_LOOT_DATA_TYPE.put((LootDataType<?>) (Object) this, new RecordMixinRefMaps.ExtendedLootDataType<>(defaultValue, conditionalCodec, idSetter));
    }

    @Unique
    private RecordMixinRefMaps.ExtendedLootDataType<T> kilt$getExtended() {
        var codec = this.codec();
        return (RecordMixinRefMaps.ExtendedLootDataType<T>) RecordMixinRefMaps.EXTENDED_LOOT_DATA_TYPE.computeIfAbsent((LootDataType<?>) (Object) this, $ -> new RecordMixinRefMaps.ExtendedLootDataType<>(null, ConditionalOps.createConditionalCodec(codec), (it, id) -> {}));
    }

    @Override
    public @Nullable T defaultValue() {
        return this.kilt$getExtended().defaultValue();
    }

    @Override
    public Codec<Optional<T>> conditionalCodec() {
        return this.kilt$getExtended().conditionalCodec();
    }

    @Override
    public BiConsumer<T, ResourceLocation> idSetter() {
        return this.kilt$getExtended().idSetter();
    }

    @WrapOperation(method = "deserialize", at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/Codec;parse(Lcom/mojang/serialization/DynamicOps;Ljava/lang/Object;)Lcom/mojang/serialization/DataResult;"))
    private <V> DataResult<T> kilt$tryUseConditionalCodec(Codec<T> instance, DynamicOps<V> dynamicOps, V o, Operation<DataResult<T>> original) {
        if (dynamicOps instanceof ConditionalOps<V> conditionalOps) {
            var decoded = this.conditionalCodec().parse(conditionalOps, o);
            if (decoded.isSuccess()) {
                var value = decoded.getOrThrow();
                if (value.isPresent()) {
                    return DataResult.success(value.orElseThrow());
                }

                if (this.defaultValue() != null) {
                    return DataResult.success(this.defaultValue());
                }
            }
        }

        return original.call(instance, dynamicOps, o);
    }

    @ModifyReturnValue(method = "deserialize", at = @At("RETURN"))
    private <V> Optional<T> kilt$tryHandleLoading(Optional<T> original, @Local(argsOnly = true) ResourceLocation id, @Local(argsOnly = true) DynamicOps<V> ops) {
        var value = original.orElse(this.defaultValue());
        if (value != null && value != this.defaultValue()) {
            this.idSetter().accept(value, id);
        }

        if (value instanceof LootTable lootTable && ops instanceof RegistryOps<V> registryOps) {
            var provider = CommonHooks.extractLookupProvider(registryOps);
            value = (T) EventHooks.loadLootTable(provider, id, lootTable);
        }

        return Optional.ofNullable(value);
    }

    // Kilt: it doesn't look like anything changed here???
}
