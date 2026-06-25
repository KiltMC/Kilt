package xyz.bluspring.kilt.injections.core.registries;

import java.util.Collections;
import java.util.Set;

import xyz.bluspring.kilt.mixin.core.BuiltInRegistriesAccessor;

import net.minecraft.resources.Identifier;

public interface BuiltInRegistriesInjection {
    static Set<Identifier> getVanillaRegistrationOrder() {
        return Collections.unmodifiableSet(BuiltInRegistriesAccessor.getLoaders().keySet());
    }
}
