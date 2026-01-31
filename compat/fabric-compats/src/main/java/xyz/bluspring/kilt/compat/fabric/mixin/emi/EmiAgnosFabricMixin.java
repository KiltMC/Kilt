package xyz.bluspring.kilt.compat.fabric.mixin.emi;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.platform.fabric.EmiAgnosFabric;
import dev.emi.emi.registry.EmiPluginContainer;
import net.fabricmc.loader.impl.launch.FabricLauncherBase;
import net.neoforged.neoforgespi.language.ModFileScanData;
import org.objectweb.asm.Type;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.Kilt;
import xyz.bluspring.kilt.loader.KiltLoader;
import xyz.bluspring.kilt.loader.mod.NeoForgeMod;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Pseudo
@Mixin(value = EmiAgnosFabric.class, remap = false)
public abstract class EmiAgnosFabricMixin {
    @Unique private static final Type kilt$entrypointType = Type.getType(EmiEntrypoint.class);
    @Unique private static final Map<String, EmiPlugin> kilt$loadedPluginInstances = new HashMap<>();

    @ModifyReturnValue(method = "getModsWithPluginsAgnos", at = @At("RETURN"))
    private List<String> kilt$emi$appendForgeEMIPluginIds(List<String> original) {
        for (NeoForgeMod mod : KiltLoader.Companion.getInstance().getMods()) {
            for (ModFileScanData.AnnotationData annotation : mod.getScanData().getAnnotations()) {
                if (annotation.annotationType().equals(kilt$entrypointType)) {
                    original.add(mod.getModId());

                    break;
                }
            }
        }

        return original;
    }

    @ModifyReturnValue(method = "getPluginsAgnos", at = @At("RETURN"))
    private List<EmiPluginContainer> kilt$emi$appendForgeEMIPlugins(List<EmiPluginContainer> original) {
        var launcher = FabricLauncherBase.getLauncher();

        for (NeoForgeMod mod : KiltLoader.Companion.getInstance().getMods()) {
            for (ModFileScanData.AnnotationData annotation : mod.getScanData().getAnnotations()) {
                if (annotation.annotationType().equals(kilt$entrypointType)) {
                    try {
                        if (kilt$loadedPluginInstances.containsKey(annotation.clazz().getClassName())) {
                            original.add(new EmiPluginContainer(kilt$loadedPluginInstances.get(annotation.clazz().getClassName()), mod.getModId()));
                        } else {
                            var clazz = launcher.loadIntoTarget(annotation.clazz().getClassName());
                            var constructor = clazz.getDeclaredConstructor();
                            var value = (EmiPlugin) constructor.newInstance();

                            kilt$loadedPluginInstances.put(annotation.clazz().getClassName(), value);

                            original.add(new EmiPluginContainer(value, mod.getModId()));
                        }
                    } catch (Throwable e) {
                        Kilt.Companion.getLogger().error("Failed to register Forge EMI entrypoint {} for mod {} ({})!", annotation.clazz().getClassName(), mod.getDisplayName(), mod.getModId());
                        e.printStackTrace();
                    }
                }
            }
        }

        return original;
    }
}
