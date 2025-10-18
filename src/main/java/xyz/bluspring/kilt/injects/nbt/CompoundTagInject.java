package xyz.bluspring.kilt.injects.nbt;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.DataInput;
import java.io.IOException;

@Mixin(CompoundTag.class)
public abstract class CompoundTagInject {
    @Mixin(targets = "net.minecraft.nbt.CompoundTag$1")
    public abstract static class CompoundTagAnonymous1Inject {
        private static byte readNamedTagType(DataInput input, NbtAccounter accounter) throws IOException {
            accounter.accountBytes(2);
            return input.readByte();
        }

        // Kilt TODO: are these needed?
//        @WrapOperation(method = "loadCompound", at = @At(value = "INVOKE", target = "Ljava/io/DataInput;readByte()B"))
//        private static byte kilt$accountReadByte(DataInput instance, Operation<Byte> original, @Local(argsOnly = true) NbtAccounter accounter) {
//            accounter.accountBytes(2);
//            return original.call(instance);
//        }

//        @Inject(method = "loadCompound", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/CompoundTag$1;readString(Ljava/io/DataInput;Lnet/minecraft/nbt/NbtAccounter;)Ljava/lang/String;"))
//        private static void kilt$accountReadString(DataInput dataInput, NbtAccounter nbtAccounter, CallbackInfoReturnable<CompoundTag> cir) {
//            nbtAccounter.accountBytes(4);
//            nbtAccounter.readUTF()
//        }
    }
}
