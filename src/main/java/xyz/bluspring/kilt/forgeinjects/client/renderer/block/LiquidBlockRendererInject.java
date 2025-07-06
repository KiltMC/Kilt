// TRACKED HASH: 76d201a1a6836dbb6ce5a521f6f40e36bff0dc05
package xyz.bluspring.kilt.forgeinjects.client.renderer.block;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.block.LiquidBlockRenderer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LiquidBlockRenderer.class)
public class LiquidBlockRendererInject {
    // Kilt: forge added method
    private void vertex(VertexConsumer pConsumer, double pX, double pY, double pZ, float pRed, float pGreen, float pBlue, float alpha, float pU, float pV, int pPackedLight) {
        pConsumer.vertex(pX, pY, pZ).color(pRed, pGreen, pBlue, alpha).uv(pU, pV).uv2(pPackedLight).normal(0.0F, 1.0F, 0.0F).endVertex();
    }
}