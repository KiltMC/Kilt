package xyz.bluspring.kilt.compat.fabric.mixin.jade;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.neoforged.fml.ModList;
import net.neoforged.neoforgespi.language.ModFileScanData;
import org.objectweb.asm.Type;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.util.CommonProxy;
import xyz.bluspring.kilt.Kilt;
import xyz.bluspring.kilt.loader.KiltLoader;
import xyz.bluspring.kilt.loader.mod.NeoForgeMod;

import java.util.List;

@IfModLoaded("jade")
@Pseudo
@Mixin(value = CommonProxy.class, remap = false)
public abstract class CommonProxyMixin {
    @Unique
    private static final Type kilt$pluginAnnotationType = Type.getType(WailaPlugin.class);

    @ModifyExpressionValue(method = "loadComplete", at = @At(value = "INVOKE", target = "Lnet/fabricmc/loader/api/FabricLoader;getEntrypointContainers(Ljava/lang/String;Ljava/lang/Class;)Ljava/util/List;"))
    private static List<EntrypointContainer<IWailaPlugin>> kilt$jade$loadNeoForgeJadePlugins(List<EntrypointContainer<IWailaPlugin>> original) {
        for (NeoForgeMod mod : KiltLoader.Companion.getInstance().getMods()) {
            for (ModFileScanData.AnnotationData annotation : mod.getScanData().getAnnotations()) {
                if (annotation.annotationType().equals(kilt$pluginAnnotationType)) {
                    String modid = ((String) annotation.annotationData().get("value"));
                    if (modid == null || FabricLoader.getInstance().isModLoaded(modid)) {
                        try {
                            var className = annotation.memberName();
                            Class<?> clazz = Class.forName(className);
                            IWailaPlugin plugin = (IWailaPlugin) clazz.getDeclaredConstructor().newInstance();
                            ModContainer fabricMod = FabricLoader.getInstance().getModContainer(mod.getModId()).orElseThrow();
                            original.add(new EntrypointContainer<>() {
                                @Override
                                public IWailaPlugin getEntrypoint() {
                                    return plugin;
                                }

                                @Override
                                public ModContainer getProvider() {
                                    return fabricMod;
                                }

                                @Override
                                public String getDefinition() {
                                    return className + " (kilt)";
                                }
                            });
                        } catch (Exception e) {
                            Kilt.Companion.getLogger().error("Failed to register NeoForge Jade entrypoint {} for mod {} ({})!", annotation.clazz().getClassName(), mod.getDisplayName(), mod.getModId(), e);
                        }
                    }
                }
            }
        }
        return original;
    }
}
