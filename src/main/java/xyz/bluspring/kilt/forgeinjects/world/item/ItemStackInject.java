// TRACKED HASH: debf6874a4415fcbb527c106a281c5bd27a0b454
package xyz.bluspring.kilt.forgeinjects.world.item;

import com.bawnorton.mixinsquared.TargetHandler;
import com.google.common.collect.Multimap;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.capabilities.CapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilityProviderImpl;
import net.minecraftforge.common.extensions.IForgeItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.helpers.mixin.Extends;
import xyz.bluspring.kilt.injections.CapabilityProviderInjection;
import xyz.bluspring.kilt.injections.item.ItemStackInjection;
import xyz.bluspring.kilt.util.KiltHelper;

import java.util.Objects;
import java.util.function.Function;

@Mixin(value = ItemStack.class, priority = 1050)
@Extends(CapabilityProvider.class)
public abstract class ItemStackInject implements IForgeItemStack, CapabilityProviderInjection, ICapabilityProviderImpl<ItemStack>, ItemStackInjection {
    private CompoundTag capNBT;

    @Unique @Nullable private Holder.Reference<Item> delegate;

    @Override
    public CompoundTag getCapNBT() {
        return capNBT;
    }

    @Shadow public abstract void setTag(@Nullable CompoundTag compoundTag);
    @Shadow @Final @Deprecated @Mutable private Item item;
    @Shadow private int count;
    @Shadow public abstract Item getItem();
    @Shadow public abstract InteractionResult useOn(UseOnContext context);

    @Unique private boolean kilt$hasRunForgeInit = false;

    public ItemStackInject(ItemLike item, int count) {}

    @CreateInitializer
    public ItemStackInject(ItemLike item, int count, CompoundTag tag) {
        this(item, count);
        this.kilt$setLazy(true);
        this.kilt$getCapabilityWorkaround().kilt$setLazy(true);
        this.delegate = getDelegate(item.asItem());
        this.capNBT = tag;
        this.forgeInit();
    }

    @Inject(at = @At("TAIL"), method = "<init>(Lnet/minecraft/nbt/CompoundTag;)V")
    public void kilt$registerCapabilities(CompoundTag compoundTag, CallbackInfo ci) {
        this.capNBT = compoundTag.contains("ForgeCaps") ? compoundTag.getCompound("ForgeCaps") : null;
        this.delegate = getDelegate(this.item.asItem());
        this.kilt$setLazy(true);
        this.kilt$getCapabilityWorkaround().kilt$setLazy(true);

        this.forgeInit();
    }

    @Inject(at = @At("TAIL"), method = "<init>(Lnet/minecraft/world/level/ItemLike;I)V")
    public void kilt$initForgeItemStack(ItemLike itemLike, int i, CallbackInfo ci) {
        this.delegate = getDelegate(itemLike.asItem());
        this.kilt$setLazy(true);
        this.kilt$getCapabilityWorkaround().kilt$setLazy(true);

        this.forgeInit();
    }

    @Inject(at = @At("TAIL"), method = "save")
    public void kilt$saveForgeCaps(CompoundTag compoundTag, CallbackInfoReturnable<CompoundTag> cir) {
        var capNbt = this.serializeCaps();
        if (capNbt != null && !capNbt.isEmpty()) {
            compoundTag.put("ForgeCaps", capNbt);
        }
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        var itemStack = ItemStack.of(nbt);
        this.setTag(nbt);

        if (itemStack.getCapNBT() != null)
            deserializeCaps(itemStack.getCapNBT());
    }

    @WrapOperation(method = "getMaxStackSize", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;getMaxStackSize()I"))
    private int kilt$tryUseForgeMaxStackSize(Item instance, Operation<Integer> original) {
        if (KiltHelper.INSTANCE.hasMethodOverride(instance.getClass(), Item.class, "getMaxStackSize", ItemStack.class)) {
            return instance.getMaxStackSize((ItemStack) (Object) this);
        }

        return original.call(instance);
    }

    /*@WrapOperation(method = "isEmpty", at = @At(value = "FIELD", target = "Lnet/minecraft/world/item/ItemStack;item:Lnet/minecraft/world/item/Item;"))
    private Item kilt$useDelegateCheckOnEmptyCheck(ItemStack instance, Operation<Item> original) {
        if (this.delegate == null)
            return original.call(instance);

        return this.delegate.value();
    }*/

    @WrapOperation(method = "getItem", at = @At(value = "FIELD", target = "Lnet/minecraft/world/item/ItemStack;item:Lnet/minecraft/world/item/Item;"))
    private Item kilt$useDelegateCheckOnItemGet(ItemStack instance, Operation<Item> original) {
        if (this.delegate == null)
            return original.call(instance);

        return this.delegate.value();
    }

    @Unique private Function<UseOnContext, InteractionResult> kilt$callback;

    @Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
    private void kilt$tryPlaceItemInWorld(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        if (!context.getLevel().isClientSide() && this.kilt$callback == null) {
            cir.setReturnValue(ForgeHooks.onPlaceItemIntoWorld(context));
        }
    }

    @Override
    public InteractionResult onItemUseFirst(UseOnContext context) {
        this.kilt$callback = c -> this.getItem().onItemUseFirst((ItemStack) (Object) this, c);
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

    @Inject(method = "save", at = @At("TAIL"))
    private void kilt$addForgeCapData(CompoundTag compoundTag, CallbackInfoReturnable<CompoundTag> cir) {
        var capNbt = this.serializeCaps();

        if (capNbt != null && !capNbt.isEmpty()) {
            compoundTag.put("ForgeCaps", capNbt);
        }
    }

    @Unique
    private void forgeInit() {
        // Avoid double running
        if (this.kilt$hasRunForgeInit)
            return;

        if (this.delegate != null) {
            this.gatherCapabilities(() -> Objects.requireNonNullElseGet(this.item, () -> this.delegate.value()).initCapabilities((ItemStack) (Object) this, this.capNBT));
            if (this.capNBT != null)
                this.deserializeCaps(this.capNBT);

            this.kilt$hasRunForgeInit = true;
        }
    }

    @Unique
    private static Holder.Reference<Item> getDelegate(Item item) {
        var forgeDelegate = ForgeRegistries.ITEMS.getDelegate(item);

        if (forgeDelegate.isEmpty()) {
            var key = BuiltInRegistries.ITEM.getResourceKey(item);

            if (key.isPresent()) {
                return BuiltInRegistries.ITEM.getHolderOrThrow(key.orElseThrow());
            }
        } else {
            return forgeDelegate.get();
        }

        return null;
    }

    @Inject(method = "isCorrectToolForDrops", at = @At("HEAD"), cancellable = true)
    private void kilt$isCorrectToolForDrops(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        Item item = getItem();
        if (KiltHelper.INSTANCE.hasMethodOverride(item.getClass(), Item.class, "isCorrectToolForDrops", ItemStack.class, BlockState.class)) {
            cir.setReturnValue(item.isCorrectToolForDrops((ItemStack) (Object) this, state));
        }
    }

    @WrapOperation(method = "getAttributeModifiers", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;getDefaultAttributeModifiers(Lnet/minecraft/world/entity/EquipmentSlot;)Lcom/google/common/collect/Multimap;"))
    private Multimap<Attribute, AttributeModifier> kilt$getModdedAttributeModifiers(Item instance, EquipmentSlot slot, Operation<Multimap<Attribute, AttributeModifier>> original) {
        if (KiltHelper.INSTANCE.hasMethodOverride(instance.getClass(), Item.class, "getAttributeModifiers", EquipmentSlot.class, ItemStack.class)) {
            return instance.getAttributeModifiers(slot,  (ItemStack) (Object) this);
        }
        return original.call(instance, slot);
    }

    @ModifyReturnValue(method = "getAttributeModifiers", at = @At("RETURN"))
    private Multimap<Attribute, AttributeModifier> kilt$invokeAttributeModifiersEvent(Multimap<Attribute, AttributeModifier> original, EquipmentSlot slot) {
        return ForgeHooks.getAttributeModifiers((ItemStack) (Object) this, slot, original);
    }

    @TargetHandler(mixin = "io.github.fabricators_of_create.porting_lib.tool.mixin.ItemStackMixin", name = "canPerformAction")
    @Inject(method = "@MixinSquared:Handler", at = @At("HEAD"), cancellable = true)
    private void kilt$checkCanPerformActionForge(io.github.fabricators_of_create.porting_lib.tool.ToolAction toolAction, CallbackInfoReturnable<Boolean> cir) {
        var forgeToolAction = ToolAction.kilt$getNullable(toolAction.name());
        if (forgeToolAction != null && this.canPerformAction(forgeToolAction)) {
            cir.setReturnValue(true);
        }
    }
}