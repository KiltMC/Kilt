package xyz.bluspring.kilt.forgeinjects.client.renderer;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.injections.client.render.ItemBlockRenderTypesInjection;

@Mixin(ItemBlockRenderTypes.class)
public class ItemBlockRenderTypesInject implements ItemBlockRenderTypesInjection {
}
