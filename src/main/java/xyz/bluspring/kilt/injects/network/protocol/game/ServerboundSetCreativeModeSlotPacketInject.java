package xyz.bluspring.kilt.injects.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.bluspring.kilt.injections.network.FriendlyByteBufInjection;

@Mixin(ServerboundSetCreativeModeSlotPacket.class)
public abstract class ServerboundSetCreativeModeSlotPacketInject {
    @Redirect(method = "write", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/FriendlyByteBuf;writeItem(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/network/FriendlyByteBuf;"))
    private FriendlyByteBuf kilt$includeFullItemStack(FriendlyByteBuf instance, ItemStack stack) {
        return ((FriendlyByteBufInjection) instance).writeItemStack(stack, false);
    }
}
