// TRACKED HASH: debf6874a4415fcbb527c106a281c5bd27a0b454
package xyz.bluspring.kilt.injects.world.item;

import java.util.function.BiConsumer;
import java.util.function.Function;

import com.bawnorton.mixinsquared.TargetHandler;
import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.common.MutableDataComponentHolder;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.extensions.IItemStackExtension;
import net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.injections.world.item.ItemStackInjection;
import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.ItemLike;

@Mixin(value = ItemStack.class, priority = 1050)
public abstract class ItemStackInject implements MutableDataComponentHolder, IItemStackExtension, ItemStackInjection {

    @Shadow public abstract Item getItem();
    @Shadow public abstract InteractionResult useOn(UseOnContext context);
    @Shadow public abstract boolean isEmpty();
    @Shadow @Final private PatchedDataComponentMap components;

    @Shadow
    public abstract ItemEnchantments getEnchantments();

    @Shadow
    public abstract boolean is(Item item);

    public ItemStackInject(ItemLike item, int count) {}

    @Override
    public boolean isComponentsPatchEmpty() {
        return !this.isEmpty() ? this.components.isPatchEmpty() : true;
    }

    @ModifyReturnValue(method = "is(Lnet/minecraft/core/Holder;)Z", at = @At("RETURN"))
    private boolean kilt$checkMatchesDeferredHolder(boolean original, @Local(argsOnly = true) Holder<Item> holder) {
        return original || this.is(holder.value());
    }

    @Unique
    private Function<UseOnContext, InteractionResult> kilt$callback;

    @Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
    private void kilt$tryPlaceItemInWorld(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        if (this.kilt$callback == null) {
            var e = NeoForge.EVENT_BUS.post(new UseItemOnBlockEvent(context, UseItemOnBlockEvent.UsePhase.ITEM_AFTER_BLOCK));
            if (e.isCanceled()) {
                cir.setReturnValue(e.getCancellationResult().result());
            } else if (!context.getLevel().isClientSide()) {
                cir.setReturnValue(CommonHooks.onPlaceItemIntoWorld(context));
            }
        }
    }

    @Override
    public InteractionResult onItemUseFirst(UseOnContext context) {
        this.kilt$callback = c -> this.getItem().onItemUseFirst((ItemStack) (Object) this, c);
        var e = NeoForge.EVENT_BUS.post(new UseItemOnBlockEvent(context, UseItemOnBlockEvent.UsePhase.ITEM_BEFORE_BLOCK));
        if (e.isCanceled()) return e.getCancellationResult().result();
        var result = this.useOn(context);
        this.kilt$callback = null;
        return result;
    }

    @WrapOperation(method = "useOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;useOn(Lnet/minecraft/world/item/context/UseOnContext;)Lnet/minecraft/world/InteractionResult;"))
    private InteractionResult kilt$tryUseOnCallback(Item instance, UseOnContext context, Operation<InteractionResult> original) {
        if (kilt$callback != null) {
            var value = kilt$callback.apply(context);
            kilt$callback = null;

            return value;
        }

        return original.call(instance, context);
    }

    @WrapOperation(method = "getMaxStackSize", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getOrDefault(Lnet/minecraft/core/component/DataComponentType;Ljava/lang/Object;)Ljava/lang/Object;"))
    private Object tryUseForgeMaxStackSize(ItemStack instance, DataComponentType<Integer> type, Object fallback, Operation<Object> original) {
        if (KiltHelper.INSTANCE.hasMethodOverride(instance.getItem().getClass(), Item.class, "getMaxStackSize", ItemStack.class)) {
            return instance.getItem().getMaxStackSize((ItemStack) (Object) this);
        }

        return original.call(instance, type, fallback);
    }

    @Definition(id = "getDamageValue", method = "Lnet/minecraft/world/item/ItemStack;getDamageValue()I")
    @Expression("this.getDamageValue() > 0")
    @ModifyExpressionValue(method = "isDamaged", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean tryUseIsDamaged(boolean original) {
        if (KiltHelper.INSTANCE.hasMethodOverride(getItem().getClass(), Item.class, "isDamaged", ItemStack.class)) {
            return getItem().isDamaged((ItemStack) (Object) this);
        }
        return original;
    }

    @ModifyReturnValue(method = "getDamageValue", at = @At("RETURN"))
    private int tryUseItemGetDamage(int original) {
        if (KiltHelper.INSTANCE.hasMethodOverride(getItem().getClass(), Item.class, "getDamage", ItemStack.class)) {
            return getItem().getDamage((ItemStack) (Object) this);
        }
        return original;
    }

    @WrapOperation(method = "setDamageValue", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;set(Lnet/minecraft/core/component/DataComponentType;Ljava/lang/Object;)Ljava/lang/Object;"))
    private <T> Object trySetItemDamage(ItemStack instance, DataComponentType<? super T> component, T value, Operation<Object> original) {
        if (KiltHelper.INSTANCE.hasMethodOverride(getItem().getClass(), Item.class, "setDamage", ItemStack.class, int.class)) {
            getItem().setDamage((ItemStack) (Object) this, (Integer) value);
            return null;
        }

        return original.call(instance, component, value);
    }

    @ModifyReturnValue(method = "getMaxDamage", at = @At("RETURN"))
    private int tryItemGetMaxDamage(int original) {
        if (KiltHelper.INSTANCE.hasMethodOverride(getItem().getClass(), Item.class, "getMaxDamage", ItemStack.class)) {
            return getItem().getMaxDamage((ItemStack) (Object) this);
        }
        return original;
    }

//    @ModifyReturnValue(method = "getTooltipLines", at = @At(value = "RETURN", ordinal = 1)) Handled by fabric oops
//    private List<Component> onItemTooltip(List<Component> original, Item.TooltipContext tooltipContext, @Nullable Player player, TooltipFlag tooltipFlag) {
//        EventHooks.onItemTooltip((ItemStack) (Object) this, player, original, tooltipFlag, tooltipContext);
//        return original;
//    }

    // Kilt: okay seriously what's the point of this?
    @Override
    public ItemEnchantments getTagEnchantments() {
        return this.getEnchantments();
    }

    @ModifyReceiver(method = "forEachModifier(Lnet/minecraft/world/entity/EquipmentSlotGroup;Ljava/util/function/BiConsumer;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/component/ItemAttributeModifiers;forEach(Lnet/minecraft/world/entity/EquipmentSlotGroup;Ljava/util/function/BiConsumer;)V", ordinal = 0))
    private ItemAttributeModifiers computeModifiedAttributesGroup(ItemAttributeModifiers instance, EquipmentSlotGroup slotGroup, BiConsumer<Holder<Attribute>, AttributeModifier> action) {
        return CommonHooks.computeModifiedAttributes((ItemStack) (Object) this, instance);
    }

    @WrapOperation(method = "forEachModifier(Lnet/minecraft/world/entity/EquipmentSlotGroup;Ljava/util/function/BiConsumer;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;getDefaultAttributeModifiers()Lnet/minecraft/world/item/component/ItemAttributeModifiers;"))
    private ItemAttributeModifiers getDefaultModifiersAndComputeModifiedAttributesGroup(Item instance, Operation<ItemAttributeModifiers> original) {
        if (KiltHelper.INSTANCE.hasMethodOverride(instance.getClass(), Item.class, "getDefaultAttributeModifiers", ItemStack.class)) {
            return CommonHooks.computeModifiedAttributes((ItemStack) (Object) this, getItem().getDefaultAttributeModifiers((ItemStack) (Object) this));
        }
        return CommonHooks.computeModifiedAttributes((ItemStack) (Object) this, original.call(instance));
    }

    @ModifyReceiver(method = "forEachModifier(Lnet/minecraft/world/entity/EquipmentSlot;Ljava/util/function/BiConsumer;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/component/ItemAttributeModifiers;forEach(Lnet/minecraft/world/entity/EquipmentSlot;Ljava/util/function/BiConsumer;)V", ordinal = 0))
    private ItemAttributeModifiers computeModifiedAttributes(ItemAttributeModifiers instance, EquipmentSlot equipmentSlot, BiConsumer<Holder<Attribute>, AttributeModifier> action) {
        return CommonHooks.computeModifiedAttributes((ItemStack) (Object) this, instance);
    }

    @WrapOperation(method = "forEachModifier(Lnet/minecraft/world/entity/EquipmentSlot;Ljava/util/function/BiConsumer;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;getDefaultAttributeModifiers()Lnet/minecraft/world/item/component/ItemAttributeModifiers;"))
    private ItemAttributeModifiers getDefaultModifiersAndComputeModifiedAttributes(Item instance, Operation<ItemAttributeModifiers> original) {
        if (KiltHelper.INSTANCE.hasMethodOverride(instance.getClass(), Item.class, "getDefaultAttributeModifiers", ItemStack.class)) {
            return CommonHooks.computeModifiedAttributes((ItemStack) (Object) this, getItem().getDefaultAttributeModifiers((ItemStack) (Object) this));
        }
        return CommonHooks.computeModifiedAttributes((ItemStack) (Object) this, original.call(instance));
    }

    @TargetHandler(mixin = "io.github.fabricators_of_create.porting_lib.tool.mixin.ItemStackMixin", name = "canPerformAction")
    @Inject(method = "@MixinSquared:Handler", at = @At("HEAD"), cancellable = true)
    private void kilt$checkCanPerformActionForge(io.github.fabricators_of_create.porting_lib.tool.ItemAbility toolAction, CallbackInfoReturnable<Boolean> cir) {
        var forgeToolAction = ItemAbility.kilt$getNullable(toolAction.name());
        if (forgeToolAction != null && this.canPerformAction(forgeToolAction)) {
            cir.setReturnValue(true);
        }
    }
}
