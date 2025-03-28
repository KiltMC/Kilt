package xyz.bluspring.kilt.forgeinjects.world.level.storage.loot;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraftforge.common.ForgeHooks;
import org.apache.commons.lang3.function.TriFunction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.injections.world.level.storage.loot.LootDataTypeInjection;

import java.util.Optional;
import java.util.function.BiFunction;

@Mixin(LootDataType.class)
public abstract class LootDataTypeInject<T> implements LootDataTypeInjection<T> {
    @Shadow @Final private Gson parser;

    @Shadow @Final private String directory;

    @WrapOperation(method = "<clinit>", at = @At(value = "NEW", target = "(Lcom/google/gson/Gson;Ljava/util/function/BiFunction;Ljava/lang/String;Lnet/minecraft/world/level/storage/loot/LootDataType$Validator;)Lnet/minecraft/world/level/storage/loot/LootDataType;", ordinal = 2))
    private static <T extends LootTable> LootDataType<T> kilt$useForgeLootDeserializer(Gson parser, BiFunction topDeserializerGetter, String directory, LootDataType.Validator validator, Operation<LootDataType<T>> original) {
        var dataType = original.call(parser, topDeserializerGetter, directory, validator);
        ((LootDataTypeInjection<LootTable>) dataType).kilt$setTriTopDeserializerGetter(ForgeHooks::getLootTableDeserializer);

        return dataType;
    }

    @Unique private TriFunction<ResourceLocation, JsonElement, ResourceManager, Optional<T>> kilt$deserializer;

    @Override
    public void kilt$setTriTopDeserializerGetter(BiFunction<Gson, String, TriFunction<ResourceLocation, JsonElement, ResourceManager, Optional<T>>> deserializerGetter) {
        this.kilt$deserializer = deserializerGetter.apply(this.parser, this.directory);
    }

    @Override
    public Optional<T> deserialize(ResourceLocation id, JsonElement json, ResourceManager resourceManager) {
        return this.kilt$deserializer.apply(id, json, resourceManager);
    }
}
