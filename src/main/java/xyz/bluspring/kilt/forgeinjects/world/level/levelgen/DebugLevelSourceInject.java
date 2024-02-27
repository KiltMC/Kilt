// TRACKED HASH: 548c719285d4e2bcf4ee2fbb1ec72b4e0c47a89d
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