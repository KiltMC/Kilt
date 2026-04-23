package xyz.bluspring.kilt.injects.world.item.crafting;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.serialization.Codec;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SimpleCookingSerializer;

@Mixin(SimpleCookingSerializer.class)
public abstract class SimpleCookingSerializerInject {
    @ModifyExpressionValue(method = "method_53766", at = @At(value = "FIELD", target = "Lnet/minecraft/world/item/ItemStack;STRICT_SINGLE_ITEM_CODEC:Lcom/mojang/serialization/Codec;", opcode = Opcodes.GETSTATIC))
    private static Codec<ItemStack> kilt$useNonStrictItemCodec(Codec<ItemStack> original) {
        return ItemStack.CODEC;
    }
}
