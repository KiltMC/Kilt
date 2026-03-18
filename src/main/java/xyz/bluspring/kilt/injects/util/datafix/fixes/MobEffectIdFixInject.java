package xyz.bluspring.kilt.injects.util.datafix.fixes;

import java.util.Optional;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.serialization.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.util.datafix.fixes.MobEffectIdFix;

@Mixin(MobEffectIdFix.class)
public abstract class MobEffectIdFixInject {
    @Unique
    private static <T> Dynamic<T> setFieldIfPresent(Dynamic<T> dynamic, String key, Optional<Dynamic<T>> value) {
        return value.isEmpty() ? dynamic : dynamic.set(key, value.get());
    }

    @WrapOperation(method = "updateMobEffectInstance", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/datafix/fixes/MobEffectIdFix;updateMobEffectIdField(Lcom/mojang/serialization/Dynamic;Ljava/lang/String;Ljava/lang/String;)Lcom/mojang/serialization/Dynamic;"))
    private static <T> Dynamic<T> kilt$updateMobEffectIdConsideringForge(Dynamic<T> dynamic, String oldName, String newName, Operation<Dynamic<T>> original) {
        var forgeField = dynamic.get("forge:id").result();

        if (forgeField.isPresent()) {
            return setFieldIfPresent(dynamic.remove("forge:id"), newName, forgeField);
        }

        return original.call(dynamic, oldName, newName);
    }

    @WrapOperation(method = "updateSuspiciousStewEntry(Lcom/mojang/serialization/Dynamic;Lcom/mojang/serialization/Dynamic;)Lcom/mojang/serialization/Dynamic;", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/datafix/fixes/MobEffectIdFix;updateMobEffectIdField(Lcom/mojang/serialization/Dynamic;Ljava/lang/String;Lcom/mojang/serialization/Dynamic;Ljava/lang/String;)Lcom/mojang/serialization/Dynamic;"))
    private static <T> Dynamic<T> kilt$updateSuspiciousStewMobEffectIdConsideringForge(Dynamic<T> oldDynamic, String oldName, Dynamic<T> newDynamic, String newName, Operation<Dynamic<T>> original) {
        var forgeField = oldDynamic.get("forge:id").result();

        if (forgeField.isPresent()) {
            return setFieldIfPresent(newDynamic, newName, forgeField);
        }

        return original.call(oldDynamic, oldName, newDynamic, newName);
    }
}
