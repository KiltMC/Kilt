// TRACKED HASH: 0b3087cc25c2d2fd27ea17dbdbb73d5d1d3d7c79
package xyz.bluspring.kilt.injects.client.renderer.texture;

import net.minecraft.client.renderer.texture.Stitcher;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Stitcher.class)
public abstract class StitcherInject {
    // TODO: is there much point for Kilt to implement this?
}