package xyz.bluspring.kilt;

import com.bawnorton.mixinsquared.ext.ExtensionRegistrar;
import com.llamalad7.mixinextras.MixinExtrasBootstrap;
import com.moulberry.mixinconstraints.MixinConstraints;
import com.moulberry.mixinconstraints.mixin.MixinConstraintsBootstrap;
import cpw.mods.niofs.union.KiltUnionFileSystemHelper;
import kotlin.text.StringsKt;
import net.fabricmc.loader.impl.launch.FabricLauncherBase;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import xyz.bluspring.kilt.helpers.mixin.MixinExtensionHelper;
import xyz.bluspring.kilt.loader.KiltLoader;
import xyz.bluspring.kilt.loader.mixin.modifier.KiltMixinModifier;

import java.util.List;
import java.util.Set;

public class KiltMixinPlugin implements IMixinConfigPlugin {
    private String mixinPackage;

    @Override
    public void onLoad(String mixinPackage) {
        this.mixinPackage = mixinPackage;

        try {
            KiltUnionFileSystemHelper.directlyLoadIntoClassLoader(FabricLauncherBase.getLauncher().getTargetClassLoader());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

        MixinExtrasBootstrap.init();
        MixinConstraintsBootstrap.init(mixinPackage);

        ExtensionRegistrar.register(new KiltMixinModifier());
        KiltLoader.INSTANCE.injectMods();
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (!mixinClassName.startsWith(mixinPackage)) {
            return true;
        }

        if (mixinClassName.contains("compat.forge.")) {
            var modId = StringsKt.removePrefix(mixinClassName, "xyz.bluspring.kilt.mixin.compat.forge.").split("\\.")[0];
            return Kilt.Companion.getLoader().hasMod(modId) && MixinConstraints.shouldApplyMixin(mixinClassName);
        }

        return MixinConstraints.shouldApplyMixin(mixinClassName);
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        MixinExtensionHelper.apply(targetClassName, targetClass, mixinClassName, mixinInfo);
    }
}
