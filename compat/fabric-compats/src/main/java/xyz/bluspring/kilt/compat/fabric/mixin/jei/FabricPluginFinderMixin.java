package xyz.bluspring.kilt.compat.fabric.mixin.jei;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.fabric.startup.FabricPluginFinder;
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

import java.util.*;

@Pseudo
@Mixin(value = FabricPluginFinder.class, remap = false)
public abstract class FabricPluginFinderMixin {
    @Unique private static final Type kilt$entrypointType = Type.getType(JeiPlugin.class);
    @Unique private static final Map<String, IModPlugin> kilt$loadedPluginInstances = Collections.synchronizedMap(new HashMap<>());

    @ModifyReturnValue(method = "getModPlugins", at = @At("RETURN"))
    private static List<IModPlugin> kilt$jei$appendForgeJEIPlugins(List<IModPlugin> original) {
        var launcher = FabricLauncherBase.getLauncher();

        for (NeoForgeMod mod : KiltLoader.Companion.getInstance().getMods()) {
            for (ModFileScanData.AnnotationData annotation : mod.getScanData().getAnnotations()) {
                if (annotation.annotationType().equals(kilt$entrypointType)) {
                    try {
                        if (kilt$loadedPluginInstances.containsKey(annotation.clazz().getClassName())) {
                            original.add(kilt$loadedPluginInstances.get(annotation.clazz().getClassName()));
                        } else {
                            var clazz = launcher.loadIntoTarget(annotation.clazz().getClassName());
                            var constructor = clazz.getDeclaredConstructor();
                            var value = (IModPlugin) constructor.newInstance();

                            kilt$loadedPluginInstances.put(annotation.clazz().getClassName(), value);

                            original.add(value);
                        }
                    } catch (Throwable e) {
                        Kilt.Companion.getLogger().error("Failed to register Forge JEI entrypoint {} for mod {} ({})!", annotation.clazz().getClassName(), mod.getDisplayName(), mod.getModId());
                        e.printStackTrace();
                    }
                }
            }
        }

        return original;
    }
}
