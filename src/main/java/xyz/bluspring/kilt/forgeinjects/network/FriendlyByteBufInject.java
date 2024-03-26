// TRACKED HASH: 65eab7af6923cfe40b811ec9f2b77f27d0284455
package xyz.bluspring.kilt.forgeinjects.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.IdMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.extensions.IForgeFriendlyByteBuf;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FriendlyByteBuf.class)
public abstract class FriendlyByteBufInject implements IForgeFriendlyByteBuf {
    @Shadow public abstract ByteBuf writeBoolean(boolean bl);

    @Shadow public abstract <T> void writeId(IdMap<T> idMap, T value);

    @Shadow public abstract ByteBuf writeByte(int i);

    @Shadow public abstract FriendlyByteBuf writeNbt(@Nullable CompoundTag nbt);

    public FriendlyByteBuf writeItemStack(ItemStack stack, boolean limitedTag) {
        if (stack.isEmpty()) {
            this.writeBoolean(false);
        } else {
            this.writeBoolean(true);
            Item item = stack.getItem();
            this.writeId(BuiltInRegistries.ITEM, item);
            this.writeByte(stack.getCount());
            CompoundTag compoundTag = null;
            if (item.isDamageable(stack) || item.shouldOverrideMultiplayerNbt()) {
                compoundTag = limitedTag ? stack.getShareTag() : stack.getTag();
            }

            this.writeNbt(compoundTag);
        }

        return (FriendlyByteBuf) (Object) this;
    }

    @Redirect(method = "readItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;setTag(Lnet/minecraft/nbt/CompoundTag;)V"))
    private void kilt$readShareTagForStack(ItemStack instance, CompoundTag compoundTag) {
        instance.readShareTag(compoundTag);
    }
}