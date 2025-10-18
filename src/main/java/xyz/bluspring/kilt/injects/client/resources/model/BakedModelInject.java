// TRACKED HASH: 38803da8adefb294d6d64e0841a02273caf2b283
package xyz.bluspring.kilt.injects.client.resources.model;

import net.minecraft.client.resources.model.BakedModel;
import net.neoforged.neoforge.client.extensions.IBakedModelExtension;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BakedModel.class)
public interface BakedModelInject extends IBakedModelExtension {
}