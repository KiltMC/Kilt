package xyz.bluspring.kilt;

import com.bawnorton.mixinsquared.MixinSquaredBootstrap;
import com.bawnorton.mixinsquared.adjuster.MixinAnnotationAdjusterRegistrar;
import com.bawnorton.mixinsquared.api.MixinAnnotationAdjuster;
import com.bawnorton.mixinsquared.api.MixinCanceller;
import com.bawnorton.mixinsquared.canceller.MixinCancellerRegistrar;
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
import xyz.bluspring.kilt.loader.mixin.modifications.KiltMixinModifier;

import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;

public class KiltMixinPlugin implements IMixinConfigPlugin {
    private String mixinPackage;

    @Override
    public void onLoad(String mixinPackage) {
        this.mixinPackage = mixinPackage;

        MixinExtrasBootstrap.init();
        MixinConstraintsBootstrap.init(mixinPackage);

        ExtensionRegistrar.register(new KiltMixinModifier());

        // Initialize MixinSquared for Forge mods
        MixinSquaredBootstrap.init();

        try {
            // We need to ensure that we're not accidentally registering this twice (Fabric mod has MixinSquared services, for example).
            var cancellersField = MixinCancellerRegistrar.class.getDeclaredField("cancellers");
            cancellersField.setAccessible(true);
            var adjustersField = MixinAnnotationAdjusterRegistrar.class.getDeclaredField("adjusters");
            adjustersField.setAccessible(true);

            var cancellers = (Set<MixinCanceller>) cancellersField.get(null);
            var adjusters = (Set<MixinAnnotationAdjuster>) adjustersField.get(null);

            ServiceLoader.load(MixinCanceller.class).forEach(mixinCanceller -> {
                if (cancellers.stream().noneMatch(e -> e.getClass() == mixinCanceller.getClass())) {
                    MixinCancellerRegistrar.register(mixinCanceller);
                }
            });

            ServiceLoader.load(MixinAnnotationAdjuster.class).forEach(adjuster -> {
                if (adjusters.stream().noneMatch(e -> e.getClass() == adjuster.getClass())) {
                    MixinAnnotationAdjusterRegistrar.register(adjuster);
                }
            });
        } catch (Throwable e) {
            throw new RuntimeException("Failed to inject Forge MixinSquared services into MixinSquared Fabric!", e);
        }
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
        MixinExtensionHelper.preApply(targetClassName, targetClass, mixinClassName, mixinInfo);
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        MixinExtensionHelper.postApply(targetClassName, targetClass, mixinClassName, mixinInfo);
    }
}
