package xyz.bluspring.kilt.injections.resources;

import xyz.bluspring.kilt.processor.FabricInjectedInterface;

import net.minecraft.resources.Identifier;

@FabricInjectedInterface(Identifier.class)
public interface IdentifierInjection {
    private Identifier self() { // Kilt: make private cuz otherwise things panic
        return (Identifier) (Object) this;
    }

    default int compareNamespaced(Identifier o) {
        var ret = self().getNamespace().compareTo(o.getNamespace());
        return ret != 0 ? ret : self().getPath().compareTo(o.getPath());
    }
}
