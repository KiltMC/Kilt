// TRACKED HASH: d44fab80835e8d0f8ac7b6fb2cc81aacbbcf8bc9
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