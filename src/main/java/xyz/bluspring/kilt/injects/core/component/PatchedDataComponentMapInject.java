package xyz.bluspring.kilt.injects.core.component;

import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.PatchedDataComponentMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.injections.core.component.PatchedDataComponentMapInjection;

import java.util.Optional;

@Mixin(PatchedDataComponentMap.class)
public abstract class PatchedDataComponentMapInject implements PatchedDataComponentMapInjection {
    @Shadow private Reference2ObjectMap<DataComponentType<?>, Optional<?>> patch;

    @Override
    public boolean isPatchEmpty() {
        return this.patch.isEmpty();
    }
}
