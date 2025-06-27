package xyz.bluspring.kilt.forgeinjects.nbt;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.StringTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.bluspring.kilt.injections.nbt.NbtAccounterInjection;

@Mixin(targets = "net/minecraft/nbt/StringTag$0")
public abstract class StringTagInject {
    @Redirect(method = "load(Ljava/io/DataInput;ILnet/minecraft/nbt/NbtAccounter;)Lnet/minecraft/nbt/StringTag;", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/NbtAccounter;accountBits(J)V", ordinal = 1))
    private void kilt$useDirectReadUTF(NbtAccounter instance, long l, @Local String s) {
        ((NbtAccounterInjection) instance).readUTF(s);
    }
}
