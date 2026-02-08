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
    }
}
