package xyz.bluspring.kilt.compat.create.mixin.registrate_fabric;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import com.tterrag.registrate.fabric.RegistryObject;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.compat.create.extensions.RegistryObjectForgeExtension;

@IfModLoaded("registrate-fabric")
@Mixin(RegistryObject.class)
public abstract class RegistryObjectMixin<T> implements RegistryObjectForgeExtension {
    @Shadow private @Nullable T object;

    @Shadow public abstract ResourceLocation getId();

    @Override
    public void updateReference(@NotNull RegisterEvent event) {
        this.object = (T) event.getRegistry().get(this.getId());
    }
}
