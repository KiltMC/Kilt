package xyz.bluspring.kilt.forgeinjects.util.datafix.fixes;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.util.datafix.fixes.StructuresBecomeConfiguredFix;
import net.minecraftforge.common.ForgeHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;

@Mixin(StructuresBecomeConfiguredFix.class)
public abstract class StructuresBecomeConfiguredFixInject {
    @WrapOperation(method = "findUpdatedStructureType", at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;", ordinal = 0))
    private <K, V> V kilt$getForgeStructureConversion(Map<K, V> instance, Object o, Operation<V> original) {
        var value = original.call(instance, o);
        if (value == null) {
            return (V) ForgeHooks.getStructureConversion((String) o);
        }

        return value;
    }
}
