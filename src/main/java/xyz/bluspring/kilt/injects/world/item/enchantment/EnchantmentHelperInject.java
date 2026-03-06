package xyz.bluspring.kilt.injects.world.item.enchantment;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.event.EventHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.world.item.enchantment.EnchantmentHelperInjection;
import xyz.bluspring.kilt.util.KiltHelper;

@Mixin(EnchantmentHelper.class)
public abstract class EnchantmentHelperInject implements EnchantmentHelperInjection {
    @Inject(method = "getItemEnchantmentLevel", at = @At("HEAD"), cancellable = true)
    private static void kilt$tryHandleDirectEnchantmentLevel(Holder<Enchantment> enchantment, ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (!EnchantmentHelperInjection.kilt$shouldUseTagEnchantment.get() && KiltHelper.INSTANCE.hasMethodOverride(stack.getItem().getClass(), Item.class, "getEnchantmentLevel", ItemStack.class, Holder.class)) {
            cir.setReturnValue(stack.getEnchantmentLevel(enchantment));
        }
    }

    @CreateStatic
    private static int getTagEnchantmentLevel(Holder<Enchantment> enchantment, ItemStack stack) {
        EnchantmentHelperInjection.kilt$shouldUseTagEnchantment.set(true);
        var value = EnchantmentHelperInjection.getTagEnchantmentLevel(enchantment, stack);
        EnchantmentHelperInjection.kilt$shouldUseTagEnchantment.remove();
        return value;
    }

    @ModifyVariable(method = "runIterationOnItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/enchantment/EnchantmentHelper$EnchantmentVisitor;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/ItemEnchantments;entrySet()Ljava/util/Set;"))
    private static ItemEnchantments kilt$tryGetAllEnchantmentsFromLookup(ItemEnchantments value, @Local(argsOnly = true) ItemStack stack) {
        var lookup = CommonHooks.resolveLookup(Registries.ENCHANTMENT);
        if (lookup != null) {
            if (KiltHelper.INSTANCE.hasMethodOverride(stack.getItem().getClass(), Item.class, "getAllEnchantments", ItemStack.class, HolderLookup.RegistryLookup.class)) {
                return stack.getAllEnchantments(lookup);
            } else {
                return EventHooks.getAllEnchantmentLevels(value, stack, lookup); // Kilt: Make sure to still call the event.
            }
        }

        return value;
    }

    @Definition(id = "itemEnchantments", local = @Local(type = ItemEnchantments.class))
    @Expression("itemEnchantments != null")
    @ModifyVariable(method = "runIterationOnItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/EquipmentSlot;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/enchantment/EnchantmentHelper$EnchantmentInSlotVisitor;)V", at = @At("MIXINEXTRAS:EXPRESSION"))
    private static ItemEnchantments kilt$tryGetAllEnchantments(ItemEnchantments value, @Local(argsOnly = true) ItemStack stack, @Local(argsOnly = true) LivingEntity entity) {
        var lookup = entity.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);

        if (KiltHelper.INSTANCE.hasMethodOverride(stack.getItem().getClass(), Item.class, "getAllEnchantments", ItemStack.class, HolderLookup.RegistryLookup.class)) {
            return stack.getAllEnchantments(lookup);
        } else {
            return EventHooks.getAllEnchantmentLevels(value, stack, lookup); // Kilt: Make sure to still call the event.
        }
    }

    @WrapOperation(method = {"getEnchantmentCost", "selectEnchantment"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;getEnchantmentValue()I"))
    private static int kilt$tryGetEnchantmentValueFromStack(Item instance, Operation<Integer> original, @Local(argsOnly = true) ItemStack stack) {
        if (KiltHelper.INSTANCE.hasMethodOverride(stack.getItem().getClass(), Item.class, "getEnchantmentValue", ItemStack.class)) {
            return stack.getEnchantmentValue();
        }

        return original.call(instance);
    }

    @Inject(method = "method_60143", at = @At("HEAD"), cancellable = true)
    private static void kilt$tryCheckIsPrimaryItem(ItemStack itemStack, boolean bl, Holder<Enchantment> holder, CallbackInfoReturnable<Boolean> cir) {
        if (KiltHelper.INSTANCE.hasMethodOverride(itemStack.getItem().getClass(), Item.class, "isPrimaryItemFor", ItemStack.class, Holder.class)) {
            cir.setReturnValue(itemStack.isPrimaryItemFor(holder));
        }
    }
}
