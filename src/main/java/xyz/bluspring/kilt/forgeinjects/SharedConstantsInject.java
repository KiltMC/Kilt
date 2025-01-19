package xyz.bluspring.kilt.forgeinjects;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import io.netty.util.ResourceLeakDetector;
import net.minecraft.SharedConstants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SharedConstants.class)
public abstract class SharedConstantsInject {
    @WrapWithCondition(method = "<clinit>", at = @At(value = "INVOKE", target = "Lio/netty/util/ResourceLeakDetector;setLevel(Lio/netty/util/ResourceLeakDetector$Level;)V"))
    private static boolean kilt$allowManualLevelSetting(ResourceLeakDetector.Level level) {
        return System.getProperty("io.netty.leakDetection.level") == null;
    }
}
