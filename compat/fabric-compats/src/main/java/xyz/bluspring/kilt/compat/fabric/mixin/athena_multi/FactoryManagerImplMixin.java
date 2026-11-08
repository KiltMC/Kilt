package xyz.bluspring.kilt.compat.fabric.mixin.athena_multi;

import java.util.Collection;

import earth.terrarium.athena.api.client.models.AthenaModelFactory;
import earth.terrarium.athena.api.client.models.fabric.FactoryManagerImpl;
import earth.terrarium.athena.api.client.utils.AthenaUnbakedModelLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Pseudo;

import net.minecraft.resources.ResourceLocation;

@Pseudo
@Mixin(value = earth.terrarium.athena.api.client.models.neoforge.FactoryManagerImpl.class, priority = 900)
public abstract class FactoryManagerImplMixin {
    /**
     * @author BluSpring
     * @reason Point back at the Athena Fabric registry.
     */
    @Overwrite
    public static void register(ResourceLocation type, AthenaModelFactory factory) {
        FactoryManagerImpl.register(type, factory);
    }

    /**
     * @author BluSpring
     * @reason Point back at Athena Fabric.
     */
    @Overwrite
    public static AthenaUnbakedModelLoader get(ResourceLocation type) {
        return FactoryManagerImpl.LOADERS.get(type);
    }

    /**
     * @author BluSpring
     * @reason Point back at Athena Fabric.
     */
    @Overwrite
    public static Collection<ResourceLocation> getTypes() {
        return FactoryManagerImpl.LOADERS.keySet();
    }
}
