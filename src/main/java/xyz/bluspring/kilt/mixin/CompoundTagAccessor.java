package xyz.bluspring.kilt.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;

@Mixin(CompoundTag.class)
public interface CompoundTagAccessor {
    @Invoker("<init>")
    static CompoundTag createCompoundTag(Map<String, Tag> tags) {
        throw new UnsupportedOperationException();
    }
}
