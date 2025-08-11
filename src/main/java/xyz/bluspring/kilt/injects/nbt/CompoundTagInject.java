package xyz.bluspring.kilt.injects.nbt;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.Tag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.injections.nbt.NbtAccounterInjection;

import java.io.DataInput;

@Mixin(CompoundTag.class)
public abstract class CompoundTagInject {
    @Inject(method = "put", at = @At("HEAD"))
    private void kilt$throwExceptionIfNullValue(String key, Tag value, CallbackInfoReturnable<Tag> cir) {
        if (value == null)
            throw new IllegalArgumentException("Invalid null NBT value with key " + key);
    }

    @Inject(method = "readNamedTagType", at = @At("HEAD"))
    private static void kilt$accountNamedTypeTag(DataInput input, NbtAccounter accounter, CallbackInfoReturnable<Byte> cir) {
        accounter.accountBytes(2);
    }

    @WrapOperation(method = "readNamedTagName", at = @At(value = "INVOKE", target = "Ljava/io/DataInput;readUTF()Ljava/lang/String;"))
    private static String kilt$accountTagNameUTF(DataInput instance, Operation<String> original, @Local(argsOnly = true) NbtAccounter accounter) {
        return ((NbtAccounterInjection) accounter).readUTF(original.call(instance));
    }

    @Mixin(targets = "net/minecraft/nbt/CompoundTag$1")
    public static abstract class Inner1Inject {
        @Inject(method = "load(Ljava/io/DataInput;ILnet/minecraft/nbt/NbtAccounter;)Lnet/minecraft/nbt/CompoundTag;", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/NbtAccounter;accountBytes(J)V", ordinal = 1, shift = At.Shift.AFTER))
        private void kilt$accountBytesForObjAlloc(DataInput dataInput, int i, NbtAccounter nbtAccounter, CallbackInfoReturnable<CompoundTag> cir) {
            nbtAccounter.accountBytes(4);
        }
    }
}
