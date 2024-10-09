// TRACKED HASH: debf6874a4415fcbb527c106a281c5bd27a0b454
package xyz.bluspring.kilt.forgeinjects.world.item;

import com.bawnorton.mixinsquared.TargetHandler;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
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

import java.util.Objects;

@Mixin(ItemStack.class)
@Extends(CapabilityProvider.class)
public abstract class ItemStackInject implements IForgeItemStack, CapabilityProviderInjection, ICapabilityProviderImpl<ItemStack>, ItemStackInjection {
    private CompoundTag capNBT;

    @Unique @Nullable private Holder.Reference<Item> delegate;

    @Override
    public CompoundTag getCapNBT() {
        return capNBT;
    }

    @Shadow public abstract void setTag(@Nullable CompoundTag compoundTag);

    @Shadow @Final @Deprecated @Mutable
    private Item item;
    @Shadow private int count;

    @Shadow public abstract Item getItem();

    public ItemStackInject(ItemLike item, int count) {}

    @CreateInitializer
    public ItemStackInject(ItemLike item, int count, CompoundTag tag) {
        this(item, count);
        this.delegate = getDelegate(item.asItem());
        this.capNBT = tag;
        this.forgeInit();
    }

    @Inject(at = @At("TAIL"), method = "<init>(Lnet/minecraft/nbt/CompoundTag;)V")
    public void kilt$registerCapabilities(CompoundTag compoundTag, CallbackInfo ci) {
        this.capNBT = compoundTag.contains("ForgeCaps") ? compoundTag.getCompound("ForgeCaps") : null;
        this.delegate = getDelegate(this.item.asItem());
        this.forgeInit();
    }

    @Inject(at = @At("TAIL"), method = "<init>(Lnet/minecraft/world/level/ItemLike;I)V")
    public void kilt$initForgeItemStack(ItemLike itemLike, int i, CallbackInfo ci) {
        this.delegate = getDelegate(itemLike.asItem());

        // this might run twice.
        // TODO: figure out how to avoid double-running
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

    @WrapOperation(method = "isEmpty", at = @At(value = "FIELD", target = "Lnet/minecraft/world/item/ItemStack;item:Lnet/minecraft/world/item/Item;"))
    private Item kilt$useDelegateCheckOnEmptyCheck(ItemStack instance, Operation<Item> original) {
        if (this.delegate == null)
            return original.call(instance);

        return this.delegate.value();
    }

    @WrapOperation(method = "getItem", at = @At(value = "FIELD", target = "Lnet/minecraft/world/item/ItemStack;item:Lnet/minecraft/world/item/Item;"))
    private Item kilt$useDelegateCheckOnItemGet(ItemStack instance, Operation<Item> original) {
        if (this.delegate == null)
            return original.call(instance);

        return this.delegate.value();
    }

    private void forgeInit() {
        if (this.delegate != null || this.item != null) {
            this.gatherCapabilities(() -> Objects.requireNonNullElseGet(this.item, () -> this.delegate.value()).initCapabilities((ItemStack) (Object) this, this.capNBT));
            if (this.capNBT != null)
                this.deserializeCaps(this.capNBT);
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

    @TargetHandler(mixin = "io.github.fabricators_of_create.porting_lib.tool.mixin", name = "canPerformAction")
    @Inject(method = "@MixinSquared:Handler", at = @At("HEAD"), cancellable = true)
    private void kilt$checkCanPerformActionForge(io.github.fabricators_of_create.porting_lib.tool.ToolAction toolAction, CallbackInfoReturnable<Boolean> cir) {
        var forgeToolAction = ToolAction.kilt$getNullable(toolAction.name());
        if (forgeToolAction != null && this.canPerformAction(forgeToolAction)) {
            cir.setReturnValue(true);
        }
    }
}