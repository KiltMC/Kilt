package xyz.bluspring.kilt.compat.fabric.mixin.jade_forge;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import snownee.jade.api.fluid.JadeFluidObject;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;

import java.util.Objects;

@Pseudo
@Mixin(value = JadeFluidObject.class, remap = false)
public abstract class JadeFluidObjectMixin {
    @CreateStatic
    private static boolean isSameFluidSameComponents(JadeFluidObject first, JadeFluidObject second) {
        if (first.getType() != second.getType()) {
            return false;
        } else {
            return first.isEmpty() && second.isEmpty() || Objects.equals(first.getComponents(), second.getComponents());
        }
    }
}
