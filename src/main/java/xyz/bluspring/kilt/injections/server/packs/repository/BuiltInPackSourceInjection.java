package xyz.bluspring.kilt.injections.server.packs.repository;

import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.Pack;

import java.util.function.Function;

public interface BuiltInPackSourceInjection {
    static Pack.ResourcesSupplier fromName(Function<PackLocationInfo, PackResources> onName) {
        return new Pack.ResourcesSupplier() {
            @Override
            public PackResources openPrimary(PackLocationInfo location) {
                return onName.apply(location);
            }

            @Override
            public PackResources openFull(PackLocationInfo location, Pack.Metadata metadata) {
                return onName.apply(location);
            }
        };
    }
}
