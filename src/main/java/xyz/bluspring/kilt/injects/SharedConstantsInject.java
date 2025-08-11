package xyz.bluspring.kilt.injects;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import io.netty.util.ResourceLeakDetector;
import net.minecraft.SharedConstants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;

import java.lang.management.ManagementFactory;

@Mixin(SharedConstants.class)
public abstract class SharedConstantsInject {
    @CreateStatic
    private static final boolean IS_RUNNING_WITH_JDWP = ManagementFactory.getRuntimeMXBean().getInputArguments().stream().anyMatch(str -> str.startsWith("-agentlib:jdwp"));

    @WrapWithCondition(method = "<clinit>", at = @At(value = "INVOKE", target = "Lio/netty/util/ResourceLeakDetector;setLevel(Lio/netty/util/ResourceLeakDetector$Level;)V"))
    private static boolean kilt$allowManualLevelSetting(ResourceLeakDetector.Level level) {
        return System.getProperty("io.netty.leakDetection.level") == null;
    }
}
