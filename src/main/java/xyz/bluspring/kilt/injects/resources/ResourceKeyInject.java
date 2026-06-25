package xyz.bluspring.kilt.injects.resources;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

@Mixin(ResourceKey.class)
public abstract class ResourceKeyInject implements Comparable<ResourceKey<?>> {
    @Shadow public abstract Identifier identifier();
    @Shadow public abstract Identifier registry();

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (obj == null || this.getClass() != obj.getClass())
            return false;

        return this.identifier().equals(((ResourceKey<?>) obj).identifier()) && this.registry().equals(((ResourceKey<?>) obj).registry());
    }

    @Override
    public int compareTo(@NotNull ResourceKey<?> o) {
        int ret = this.registry().compareTo(o.registry());
        if (ret == 0)
            ret = this.identifier().compareTo(o.identifier());

        return ret;
    }
}
