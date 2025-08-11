// TRACKED HASH: e63986ae2aac1456fe256ecfa6d01712dbfbbc8f
package xyz.bluspring.kilt.injects.client.resources.model;

import net.minecraft.client.resources.model.ModelBaker;
import net.neoforged.neoforge.client.extensions.IForgeModelBaker;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ModelBaker.class)
public interface ModelBakerInject extends IForgeModelBaker {
}