package xyz.bluspring.kilt.compat.create.mixin;

import com.tterrag.registrate.fabric.RegistryObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.RegisterEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.compat.create.extensions.RegistryObjectForgeExtension;

@Mixin(RegistryObject.class)
public abstract class RegistryObjectMixin<T> implements RegistryObjectForgeExtension {
    @Shadow private @Nullable T object;

    @Shadow public abstract ResourceLocation getId();

    @Override
    public void updateReference(@NotNull RegisterEvent event) {
        this.object = (T) event.getForgeRegistry().getValue(this.getId());
    }
}
