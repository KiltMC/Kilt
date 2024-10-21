package xyz.bluspring.kilt.forgeinjects.client.renderer.entity.layers;

import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.injections.client.renderer.entity.layer.ElytraLayerInjection;

@Mixin(ElytraLayer.class)
public abstract class ElytraLayerInject<T extends LivingEntity> implements ElytraLayerInjection<T> {
    @Shadow @Final private static ResourceLocation WINGS_LOCATION;

    @Override
    public boolean shouldRender(ItemStack stack, T entity) {
        return stack.getItem() == Items.ELYTRA;
    }

    @Override
    public ResourceLocation getElytraTexture(ItemStack stack, T entity) {
        return WINGS_LOCATION;
    }
}
