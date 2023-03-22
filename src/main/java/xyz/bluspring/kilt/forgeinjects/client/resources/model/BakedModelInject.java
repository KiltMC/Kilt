package xyz.bluspring.kilt.forgeinjects.client.resources.model;

import net.minecraftforge.client.extensions.IForgeBakedModel;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(net.minecraft.client.resources.model.BakedModel.class)
public interface BakedModelInject extends IForgeBakedModel {
}
