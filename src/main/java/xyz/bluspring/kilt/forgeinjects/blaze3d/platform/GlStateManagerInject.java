package xyz.bluspring.kilt.forgeinjects.blaze3d.platform;

import com.mojang.blaze3d.platform.GlStateManager;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.injections.blaze3d.GlStateManagerInjection;

@Mixin(GlStateManager.class)
public class GlStateManagerInject implements GlStateManagerInjection {
}
