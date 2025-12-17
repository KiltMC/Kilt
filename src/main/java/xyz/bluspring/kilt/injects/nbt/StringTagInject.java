package xyz.bluspring.kilt.injects.nbt;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.StringTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.bluspring.kilt.injections.nbt.NbtAccounterInjection;

@Mixin(StringTag.class)
public abstract class StringTagInject {
    @Mixin(targets = "net/minecraft/nbt/StringTag$1")
    public static abstract class Inner1Inject {
        /*@Redirect(method = "load(Ljava/io/DataInput;ILnet/minecraft/nbt/NbtAccounter;)Lnet/minecraft/nbt/StringTag;", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/NbtAccounter;accountBytes(J)V", ordinal = 1))
        private void kilt$useImprovedUTFAccounter(NbtAccounter instance, long bytes, @Local String string) {
            ((NbtAccounterInjection) instance).readUTF(string);
        }*/
    }
}
