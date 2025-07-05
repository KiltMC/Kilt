package xyz.bluspring.kilt.mixin.compat.modmenu;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.terraformersmc.modmenu.gui.widget.entries.ModListEntry;
import com.terraformersmc.modmenu.util.mod.Mod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ModListEntry.class)
public abstract class ModListEntryMixin {
    @Shadow @Final public Mod mod;

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIFFIIII)V", ordinal = 0))
    private void kilt$tryRenderCroppedIcon(GuiGraphics instance, ResourceLocation resourceLocation, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight, Operation<Void> original) {
        if (this.mod.getBadges().contains(Mod.Badge.PATCHWORK_FORGE)) {
            var texture = Minecraft.getInstance().textureManager.getTexture(resourceLocation);

            if (texture instanceof DynamicTexture dynamicTexture) {
                var nativeImage = dynamicTexture.getPixels();

                if (nativeImage != null) {
                    var imageWidth = nativeImage.getWidth();
                    var imageHeight = nativeImage.getHeight();

                    if (imageWidth != imageHeight) {
                        if (imageHeight > imageWidth) {
                            instance.blit(resourceLocation, x, y, width, height, u, v, imageWidth, imageWidth, imageWidth, imageHeight);
                        } else {
                            instance.blit(resourceLocation, x, y, width, height, u, v, imageHeight, imageHeight, imageWidth, imageHeight);
                        }

                        return;
                    }
                }
            }
        }

        original.call(instance, resourceLocation, x, y, u, v, width, height, textureWidth, textureHeight);
    }
}
