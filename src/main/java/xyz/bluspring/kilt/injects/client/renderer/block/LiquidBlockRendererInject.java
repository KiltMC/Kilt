// TRACKED HASH: 76d201a1a6836dbb6ce5a521f6f40e36bff0dc05
package xyz.bluspring.kilt.injects.client.renderer.block;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.block.LiquidBlockRenderer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LiquidBlockRenderer.class)
public class LiquidBlockRendererInject {
    // Kilt: forge added method
    private void vertex(VertexConsumer pConsumer, float pX, float pY, float pZ, float pRed, float pGreen, float pBlue, float alpha, float pU, float pV, int pPackedLight) {
        pConsumer.addVertex(pX, pY, pZ).setColor(pRed, pGreen, pBlue, alpha).setUv(pU, pV).setLight(pPackedLight).setNormal(0.0F, 1.0F, 0.0F);
    }
}