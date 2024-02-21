package xyz.bluspring.kilt.injections.resources;

import io.github.fabricators_of_create.porting_lib.extensions.extensions.ResourceLocationExtensions;
import net.minecraft.resources.ResourceLocation;

public interface ResourceLocationInjection extends ResourceLocationExtensions {
    default ResourceLocation self() {
        return (ResourceLocation) this;
    }

    @Override
    default int compareNamespaced(ResourceLocation o) {
        var ret = self().getNamespace().compareTo(o.getNamespace());
        return ret != 0 ? ret : self().getPath().compareTo(o.getPath());
    }
}
