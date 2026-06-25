// TRACKED HASH: ec6c3c6702bb8f7588d211003582f0637ede7a14
package xyz.bluspring.kilt.injects.resources;

import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.injections.resources.IdentifierInjection;

import net.minecraft.resources.Identifier;

@Mixin(Identifier.class)
public abstract class IdentifierInject implements IdentifierInjection {
    @Override
    public int compareNamespaced(Identifier o) {
        return IdentifierInjection.super.compareNamespaced(o);
    }
}
