package xyz.bluspring.kilt.forgeinjects.gametest.framework;

import net.minecraft.gametest.framework.GameTest;
import org.spongepowered.asm.mixin.Mixin;

import java.lang.annotation.Annotation;

@Mixin(GameTest.class)
public interface GameTestInject extends Annotation {
    default String templateNamespace() {
        return "";
    }
}
