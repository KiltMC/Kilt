package xyz.bluspring.kilt.forgeinjects.world.level.storage.loot;

import com.google.gson.JsonElement;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.storage.loot.LootDataManager;
import net.minecraft.world.level.storage.loot.LootDataType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.injections.world.level.storage.loot.LootDataTypeInjection;

import java.util.Map;
import java.util.Optional;

@Mixin(LootDataManager.class)
public abstract class LootDataManagerInject {
    @Unique private static final ThreadLocal<ResourceManager> kilt$resourceManager = new ThreadLocal<>();

    @Inject(method = "method_51189", at = @At("HEAD"))
    private static void kilt$storeResourceManager(ResourceManager resourceManager, LootDataType lootDataType, Map map, CallbackInfo ci) {
        kilt$resourceManager.set(resourceManager);
    }

    @WrapOperation(method = "method_51195", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/loot/LootDataType;deserialize(Lnet/minecraft/resources/ResourceLocation;Lcom/google/gson/JsonElement;)Ljava/util/Optional;"))
    private static <T> Optional<T> kilt$useForgeDeserialize(LootDataType<T> instance, ResourceLocation location, JsonElement json, Operation<Optional<T>> original) {
        return ((LootDataTypeInjection<T>) instance).kilt$tryDeserialize(location, json, kilt$resourceManager.get(), () -> original.call(instance, location, json));
    }
}
