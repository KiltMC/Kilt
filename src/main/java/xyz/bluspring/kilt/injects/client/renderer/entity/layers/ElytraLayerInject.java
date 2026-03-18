// TRACKED HASH: 5e7b81ffda346de6ce0bba263ac722b16acb197b
package xyz.bluspring.kilt.injects.client.renderer.entity.layers;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.injections.client.renderer.entity.layers.ElytraLayerInjection;
import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

@Mixin(ElytraLayer.class)
public abstract class ElytraLayerInject<T extends LivingEntity> implements ElytraLayerInjection<T> {
    @Shadow @Final private static ResourceLocation WINGS_LOCATION;

    @Definition(id = "itemStack", local = @Local(type = ItemStack.class))
    @Definition(id = "is", method = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z")
    @Definition(id = "ELYTRA", field = "Lnet/minecraft/world/item/Items;ELYTRA:Lnet/minecraft/world/item/Item;")
    @Expression("itemStack.is(ELYTRA)")
    @WrapOperation(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$checkShouldElytraRender(ItemStack instance, Item item, Operation<Boolean> original, @Local(argsOnly = true) T entity) {
        if (KiltHelper.INSTANCE.hasMethodOverride(this.getClass(), ElytraLayer.class, "shouldRender", ItemStack.class, LivingEntity.class)) {
            return this.shouldRender(instance, entity);
        }

        return original.call(instance, item);
    }

    @ModifyExpressionValue(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/entity/layers/ElytraLayer;WINGS_LOCATION:Lnet/minecraft/resources/ResourceLocation;"))
    private ResourceLocation kilt$tryUseCustomElytraTexture(ResourceLocation original, @Local ItemStack stack, @Local(argsOnly = true) T entity) {
        if (KiltHelper.INSTANCE.hasMethodOverride(this.getClass(), ElytraLayer.class, "getElytraTexture", ItemStack.class, LivingEntity.class)) {
            return this.getElytraTexture(stack, entity);
        }

        return original;
    }

    @Override
    public boolean shouldRender(ItemStack stack, T entity) {
        return stack.getItem() == Items.ELYTRA;
    }

    @Override
    public ResourceLocation getElytraTexture(ItemStack stack, T entity) {
        return WINGS_LOCATION;
    }
}