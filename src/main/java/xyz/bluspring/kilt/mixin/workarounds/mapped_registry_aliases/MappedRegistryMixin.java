package xyz.bluspring.kilt.mixin.workarounds.mapped_registry_aliases;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import net.neoforged.neoforge.registries.BaseMappedRegistry;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import xyz.bluspring.kilt.loader.KiltLoader;
import xyz.bluspring.kilt.workarounds.MappedRegistryWorkaround;

import net.minecraft.core.MappedRegistry;
import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.api.event.registry.FabricRegistry;

@SuppressWarnings("NonExtendableApiUsage")
@Mixin(value = MappedRegistry.class, priority = 1050)
@Implements(@Interface(iface = MappedRegistryWorkaround.class, prefix = "kilt$i$"))
public abstract class MappedRegistryMixin implements FabricRegistry {

    @Unique
    private static final MethodHandle kilt$super$addAlias;

    static {
        try {
            // https://stackoverflow.com/a/15674467
            //noinspection JavaLangInvokeHandleSignature
            kilt$super$addAlias = MethodHandles.lookup().findSpecial(
                BaseMappedRegistry.class, "addAlias",
                MethodType.methodType(Void.TYPE, Identifier.class, Identifier.class),
                MappedRegistry.class
            );
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Intrinsic(displace = true)
    public void kilt$i$addAlias(Identifier old, Identifier newId) {
        if (KiltLoader.Companion.getInstance().hasMod(old.getNamespace())) {
            try {
                //noinspection JavaLangInvokeHandleSignature
                kilt$super$addAlias.invoke(this, old, newId);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        } else {
            addAlias(old, newId);
        }
    }

}
