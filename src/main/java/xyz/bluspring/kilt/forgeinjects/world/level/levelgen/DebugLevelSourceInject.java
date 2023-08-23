package xyz.bluspring.kilt.forgeinjects.world.level.levelgen;

import net.minecraft.world.level.levelgen.DebugLevelSource;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.world.level.levelgen.DebugLevelSourceInjection;

@Mixin(DebugLevelSource.class)
public class DebugLevelSourceInject implements DebugLevelSourceInjection {
    @CreateStatic
    private static void initValidStates() {
        DebugLevelSourceInjection.initValidStates();
    }
}
