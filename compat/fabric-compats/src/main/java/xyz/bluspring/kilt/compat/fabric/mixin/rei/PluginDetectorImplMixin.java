package xyz.bluspring.kilt.compat.fabric.mixin.rei;

import me.shedaniel.rei.api.common.plugins.REIPlugin;
import me.shedaniel.rei.api.common.plugins.REIPluginProvider;
import me.shedaniel.rei.api.common.plugins.REIServerPlugin;
import me.shedaniel.rei.fabric.PluginDetectorImpl;
import me.shedaniel.rei.forge.REIPluginClient;
import me.shedaniel.rei.forge.REIPluginCommon;
import me.shedaniel.rei.forge.REIPluginDedicatedServer;
import net.fabricmc.loader.impl.launch.FabricLauncherBase;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.objectweb.asm.Type;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.Kilt;
import xyz.bluspring.kilt.compat.fabric.rei.KiltREIPluginProvider;
import xyz.bluspring.kilt.loader.KiltLoader;
import xyz.bluspring.kilt.loader.mod.ForgeMod;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Pseudo
@Mixin(value = PluginDetectorImpl.class, remap = false)
public abstract class PluginDetectorImplMixin {
    @Unique private static final Type kilt$clientEntrypointType = Type.getType(REIPluginClient.class);
    @Unique private static final Type kilt$serverEntrypointType = Type.getType(REIPluginDedicatedServer.class);
    @Unique private static final Type kilt$commonEntrypointType = Type.getType(REIPluginCommon.class);
    @Unique private static final Map<String, REIPluginProvider<?>> kilt$loadedPluginInstances = Collections.synchronizedMap(new HashMap<>());

    @Inject(method = "loadPlugin", at = @At("TAIL"))
    private static <P extends REIPlugin<?>> void kilt$rei$loadForgeREIPlugins(Class<? extends P> pluginClass, Consumer<? super REIPluginProvider<P>> consumer, CallbackInfo ci) {
        Type entrypointType;

        if (pluginClass == REIServerPlugin.class)
            entrypointType = kilt$serverEntrypointType;
        else if (pluginClass == (Class<? extends REIPlugin<?>>) (Class) REIPlugin.class)
            entrypointType = kilt$commonEntrypointType;
        else if (pluginClass.getSimpleName().equals("REIClientPlugin")) // If we try to load this, it'll cause a crash on servers.
            entrypointType = kilt$clientEntrypointType;
        else return;

        var launcher = FabricLauncherBase.getLauncher();

        for (ForgeMod mod : KiltLoader.Companion.getInstance().getMods()) {
            for (ModFileScanData.AnnotationData annotation : mod.getScanData().getAnnotations()) {
                if (annotation.annotationType().equals(entrypointType)) {
                    try {
                        REIPluginProvider<P> plugin;

                        var clazz = launcher.loadIntoTarget(annotation.clazz().getClassName());
                        if (kilt$loadedPluginInstances.containsKey(annotation.clazz().getClassName())) {
                            var constructor = clazz.getDeclaredConstructor();
                            plugin = (REIPluginProvider<P>) constructor.newInstance();
                        } else {
                            plugin = (REIPluginProvider<P>) kilt$loadedPluginInstances.get(annotation.clazz().getClassName());
                        }

                        if (pluginClass.isAssignableFrom(plugin.getPluginProviderClass())) {
                            consumer.accept(new KiltREIPluginProvider<>(plugin, mod));
                        }
                    } catch (Throwable e) {
                        Kilt.Companion.getLogger().error("Failed to register Forge REI entrypoint {} for mod {} ({})!", annotation.clazz().getClassName(), mod.getDisplayName(), mod.getModId());
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
