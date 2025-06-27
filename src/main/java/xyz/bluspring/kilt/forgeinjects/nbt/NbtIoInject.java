package xyz.bluspring.kilt.forgeinjects.nbt;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.injections.nbt.NbtAccounterInjection;

import java.io.DataInput;
import java.io.IOException;

@Mixin(NbtIo.class)
public abstract class NbtIoInject {
    @Inject(method = "readUnnamedTag", at = @At(value = "INVOKE", target = "Ljava/io/DataInput;readByte()B", ordinal = 0, shift = At.Shift.AFTER))
    private static void kilt$accountByteTag(DataInput input, int depth, NbtAccounter accounter, CallbackInfoReturnable<Tag> cir) {
        accounter.accountBits(8);
    }

    @Redirect(method = "readUnnamedTag", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/StringTag;skipString(Ljava/io/DataInput;)V"))
    private static void kilt$useImprovedStringAccounter(DataInput input, @Local(argsOnly = true) NbtAccounter accounter) throws IOException {
        ((NbtAccounterInjection) accounter).readUTF(input.readUTF()); // Forge: Count this string.
        accounter.accountBits(32); // Forge: 4 extra bytes for the object allocation.
    }
}
