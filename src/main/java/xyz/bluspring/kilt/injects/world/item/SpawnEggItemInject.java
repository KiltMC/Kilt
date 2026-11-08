// TRACKED HASH: 47476edb59fcfcf846692ab9ec3986cad3c5d6c4
package xyz.bluspring.kilt.injects.world.item;

import java.util.Map;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.injections.world.item.SpawnEggItemInjection;
import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.SpawnEggItem;

@Mixin(SpawnEggItem.class)
public class SpawnEggItemInject implements SpawnEggItemInjection {
    @Shadow @Final private EntityType<?> defaultType;

    @Definition(id = "BY_ID", field = "Lnet/minecraft/world/item/SpawnEggItem;BY_ID:Ljava/util/Map;")
    @Definition(id = "put", method = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;")
    @Expression("BY_ID.put(?, ?)")
    @WrapOperation(method = "<init>", at = @At("MIXINEXTRAS:EXPRESSION"))
    private <K, V> Object kilt$ensureTypeNotNull(Map<K, V> instance, K k, V v, Operation<Object> original) {
        if (v == null)
            return null;

        return original.call(instance, k, v);
    }

    @Inject(method = "byId", at = @At("HEAD"), cancellable = true)
    private static void kilt$tryResolveDeferredSpawnEggItem(EntityType<?> type, CallbackInfoReturnable<SpawnEggItem> cir) {
        var ret = DeferredSpawnEggItem.deferredOnlyById(type);
        if (ret != null)
            cir.setReturnValue(ret);
    }

    @WrapOperation(method = "getType", at = @At(value = "FIELD", target = "Lnet/minecraft/world/item/SpawnEggItem;defaultType:Lnet/minecraft/world/entity/EntityType;", opcode = 0))
    private EntityType<?> kilt$useForgeDefaultType(SpawnEggItem instance, Operation<EntityType<?>> original) {
        if (KiltHelper.INSTANCE.hasMethodOverride(this.getClass(), SpawnEggItem.class, "getDefaultType"))
            return this.getDefaultType();

        return original.call(instance);
    }

    @ModifyReturnValue(method = "getType", at = @At("RETURN"))
    private EntityType<?> kilt$returnForgeDefaultType(EntityType<?> original) {
        if (KiltHelper.INSTANCE.hasMethodOverride(this.getClass(), SpawnEggItem.class, "getDefaultType"))
            return this.getDefaultType();

        return original;
    }

    @ModifyReceiver(method = "requiredFeatures", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/EntityType;requiredFeatures()Lnet/minecraft/world/flag/FeatureFlagSet;"))
    private EntityType<?> kilt$useForgeDefaultTypeForFeatures(EntityType<?> instance) {
        if (KiltHelper.INSTANCE.hasMethodOverride(this.getClass(), SpawnEggItem.class, "getDefaultType"))
            return this.getDefaultType();

        return instance;
    }

    @Override
    public EntityType<?> getDefaultType() {
        return this.defaultType;
    }
}
