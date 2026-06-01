package xyz.bluspring.kilt.mixin.workarounds.mapped_registry_aliases;

import net.fabricmc.fabric.api.event.registry.FabricRegistry;
import net.minecraft.core.MappedRegistry;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.BaseMappedRegistry;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.loader.KiltLoader;
import xyz.bluspring.kilt.workarounds.MappedRegistryWorkaround;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

@SuppressWarnings("NonExtendableApiUsage")
@Mixin(value = MappedRegistry.class, priority = 1050)
@Implements(@Interface(iface = MappedRegistryWorkaround.class, prefix = "kilt$i$"))
public class MappedRegistryMixin implements FabricRegistry {

    @Intrinsic(displace = true)
    public void kilt$i$addAlias(ResourceLocation old, ResourceLocation newId) {
        if (KiltLoader.Companion.getInstance().hasMod(old.getNamespace())) {
            try {
                // https://stackoverflow.com/a/15674467
                @SuppressWarnings("JavaLangInvokeHandleSignature")
                MethodHandle super$addAlias = MethodHandles.lookup().findSpecial(
                    BaseMappedRegistry.class, "addAlias",
                    MethodType.methodType(Void.TYPE, ResourceLocation.class, ResourceLocation.class),
                    MappedRegistry.class
                );
                //noinspection JavaLangInvokeHandleSignature
                super$addAlias.invoke(this, old, newId);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        } else {
            addAlias(old, newId);
        }
    }

}
