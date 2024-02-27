// TRACKED HASH: 38803da8adefb294d6d64e0841a02273caf2b283
package xyz.bluspring.kilt.forgeinjects.client.resources.model;

import net.minecraftforge.client.extensions.IForgeBakedModel;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(net.minecraft.client.resources.model.BakedModel.class)
public interface BakedModelInject extends IForgeBakedModel {
}