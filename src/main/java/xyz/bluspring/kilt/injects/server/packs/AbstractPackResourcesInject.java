package xyz.bluspring.kilt.injects.server.packs;

import net.minecraft.server.packs.AbstractPackResources;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Locale;

@Mixin(AbstractPackResources.class)
public abstract class AbstractPackResourcesInject {
    @Shadow @Final private String name;

    @Override
    public String toString() {
        return String.format(Locale.ROOT, "%s: %s", this.getClass().getName(), this.name);
    }
}
