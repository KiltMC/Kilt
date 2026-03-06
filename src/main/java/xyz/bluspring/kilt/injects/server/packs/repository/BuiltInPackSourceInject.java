package xyz.bluspring.kilt.injects.server.packs.repository;

import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.BuiltInPackSource;
import net.minecraft.server.packs.repository.Pack;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.server.packs.repository.BuiltInPackSourceInjection;

import java.util.function.Function;

@Mixin(BuiltInPackSource.class)
public abstract class BuiltInPackSourceInject implements BuiltInPackSourceInjection {
    @CreateStatic
    private static Pack.ResourcesSupplier fromName(final Function<PackLocationInfo, PackResources> onName) {
        return BuiltInPackSourceInjection.fromName(onName);
    }
}
