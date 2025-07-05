package xyz.bluspring.kilt.compat.create.mixin.registrate_fabric;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import com.tterrag.registrate.fabric.RegistryObject;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.minecraftforge.registries.RegisterEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.compat.create.extensions.RegistryEntryForgeExtension;
import xyz.bluspring.kilt.compat.create.extensions.RegistryObjectForgeExtension;

import java.util.Objects;

@IfModLoaded("registrate-fabric")
@Mixin(RegistryEntry.class)
public abstract class RegistryEntryMixin<T> implements RegistryEntryForgeExtension {
    @Shadow @Final private @Nullable RegistryObject<T> delegate;

    @Override
    public void updateReference(@NotNull RegisterEvent event) {
        var delegate = this.delegate;
        ((RegistryObjectForgeExtension) Objects.requireNonNull(delegate)).updateReference(event);
    }
}
