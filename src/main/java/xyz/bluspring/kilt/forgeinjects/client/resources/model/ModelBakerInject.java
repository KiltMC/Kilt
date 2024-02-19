package xyz.bluspring.kilt.forgeinjects.client.resources.model;

import net.minecraft.client.resources.model.ModelBaker;
import net.minecraftforge.client.extensions.IForgeModelBaker;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ModelBaker.class)
public interface ModelBakerInject extends IForgeModelBaker {
}
