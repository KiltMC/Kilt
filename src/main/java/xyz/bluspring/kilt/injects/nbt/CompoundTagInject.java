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
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;

import java.io.DataInput;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Mixin(CompoundTag.class)
public abstract class CompoundTagInject {
    protected CompoundTagInject(Map<String, Tag> tags) {}

    @Mixin(targets = "net.minecraft.nbt.CompoundTag$1")
    public abstract static class CompoundTagAnonymous1Inject {
    }

    @CreateInitializer
    public CompoundTagInject(int expectedEntries) {
        this(HashMap.newHashMap(expectedEntries));
    }
}
