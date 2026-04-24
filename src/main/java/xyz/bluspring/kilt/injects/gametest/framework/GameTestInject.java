package xyz.bluspring.kilt.injects.gametest.framework;

import java.lang.annotation.Annotation;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.gametest.framework.GameTest;

@Mixin(GameTest.class)
public interface GameTestInject extends Annotation {
    default String templateNamespace() {
        return "";
    }
}
