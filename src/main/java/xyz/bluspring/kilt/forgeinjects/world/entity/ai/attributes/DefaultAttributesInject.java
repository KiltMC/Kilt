// TRACKED HASH: 04ed1ed8c5f75415ffb7509faccf5a3e9d757b25
package xyz.bluspring.kilt.forgeinjects.world.entity.ai.attributes;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.minecraftforge.common.ForgeHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;

@Mixin(DefaultAttributes.class)
public class DefaultAttributesInject {
    @WrapOperation(at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;"), method = "getSupplier")
    private static <K, V> V kilt$useForgeSupplier(Map<K, V> instance, Object o, Operation<V> original, @Local(argsOnly = true) EntityType<? extends LivingEntity> entityType) {
        var supplier = ForgeHooks.getAttributesView().get(entityType);
        return supplier != null ? (V) supplier : original.call(instance, o);
    }

    @ModifyReturnValue(at = @At("RETURN"), method = "hasSupplier")
    private static boolean kilt$hasForgeSupplier(boolean original, @Local(argsOnly = true) EntityType<?> type) {
        return original || ForgeHooks.getAttributesView().containsKey(type);
    }
}