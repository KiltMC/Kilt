package xyz.bluspring.kilt.mixin;

import java.util.Optional;

import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;

@Mixin(DataComponentPatch.class)
public interface DataComponentPatchAccessor {
    @Accessor("map")
    Reference2ObjectMap<DataComponentType<?>, Optional<?>> kilt$getMap();
}
