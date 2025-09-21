package xyz.bluspring.kilt.injections.resources;

import io.github.fabricators_of_create.porting_lib.extensions.common.ResourceLocationExtension;
import net.minecraft.resources.ResourceLocation;
import xyz.bluspring.kilt.processor.FabricInjectedInterface;

@FabricInjectedInterface(ResourceLocation.class)
public interface ResourceLocationInjection extends ResourceLocationExtension {
    default ResourceLocation self() {
        return (ResourceLocation) this;
    }

    default int compareNamespaced(ResourceLocation o) {
        var ret = self().getNamespace().compareTo(o.getNamespace());
        return ret != 0 ? ret : self().getPath().compareTo(o.getPath());
    }
}
