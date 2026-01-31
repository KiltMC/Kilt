package xyz.bluspring.kilt.injections.client.renderer.entity.layers;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public interface ElytraLayerInjection<T extends LivingEntity> {
    boolean shouldRender(ItemStack stack, T entity);
    ResourceLocation getElytraTexture(ItemStack stack, T entity);
}
