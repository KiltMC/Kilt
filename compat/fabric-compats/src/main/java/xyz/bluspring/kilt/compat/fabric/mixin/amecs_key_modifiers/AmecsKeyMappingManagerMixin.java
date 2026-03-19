package xyz.bluspring.kilt.compat.fabric.mixin.amecs_key_modifiers;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import de.siphalor.amecs.key_modifiers.impl.AmecsKeyMappingManager;
import de.siphalor.amecs.key_modifiers.impl.AmecsKeyMappingManagerLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Pseudo
@IfModLoaded(value = "amecs_key_modifiers")
@Mixin(AmecsKeyMappingManager.class)
public class AmecsKeyMappingManagerMixin {

    @WrapOperation(
            method = "<clinit>",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/List;add(Ljava/lang/Object;)Z"
            )
    )
    private static <E> boolean kilt$removeDefaultLayer(List<E> instance, E e, Operation<Boolean> original) {
        if (e instanceof AmecsKeyMappingManagerLayer) {
            return false;
        }
        return original.call(instance, e);
    }

}
