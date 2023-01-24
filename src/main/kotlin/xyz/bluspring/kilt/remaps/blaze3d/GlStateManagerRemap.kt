package xyz.bluspring.kilt.remaps.blaze3d

import com.mojang.blaze3d.platform.GlStateManager

object GlStateManagerRemap : GlStateManager() {
    @JvmField var lastBrightnessX = 0F
    @JvmField var lastBrightnessY = 0F
}