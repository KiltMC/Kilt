package xyz.bluspring.kilt.forgeinjects.core;

import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.core.DirectionInjection;

@Mixin(Direction.class)
public class DirectionInject implements DirectionInjection {
    @CreateStatic
    private static Direction getNearestStable(float x, float y, float z) {
        return DirectionInjection.getNearestStable(x, y, z);
    }
}
