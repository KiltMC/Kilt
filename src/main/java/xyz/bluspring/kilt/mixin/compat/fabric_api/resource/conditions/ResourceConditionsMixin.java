package xyz.bluspring.kilt.mixin.compat.fabric_api.resource.conditions;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.workarounds.resource.WrappedNeoCondition;

import net.minecraft.resources.ResourceLocation;

import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;

@Mixin(ResourceConditions.class)
public abstract class ResourceConditionsMixin {
    @ModifyReturnValue(method = "getConditionType", at = @At("RETURN"))
    private static ResourceConditionType<?> kilt$fabric_api$detectWithNeoConditions(ResourceConditionType<?> original, @Local(argsOnly = true, name = "id") ResourceLocation id) {
        if (original == null) {
            var condition = NeoForgeRegistries.CONDITION_SERIALIZERS.get(id);
            if (condition != null)
                return WrappedNeoCondition.getOrRegister(condition, id);
        }

        return original;
    }
}
