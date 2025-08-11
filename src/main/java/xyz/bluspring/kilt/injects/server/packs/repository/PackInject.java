// TRACKED HASH: 3c15b31dfec44b9332f86e2c6d5babfb7b181f59
package xyz.bluspring.kilt.injects.server.packs.repository;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.flag.FeatureFlagSet;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.injections.server.packs.metadata.pack.PackMetadataSectionInjection;
import xyz.bluspring.kilt.injections.server.packs.repository.PackInfoInjection;
import xyz.bluspring.kilt.injections.server.packs.repository.PackInjection;

@Mixin(Pack.class)
public abstract class PackInject implements PackInjection {
    @Unique private boolean hidden;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void kilt$setHidden(String id, boolean required, Pack.ResourcesSupplier resources, Component title, Pack.Info info, PackCompatibility compatibility, Pack.Position defaultPosition, boolean fixedPosition, PackSource packSource, CallbackInfo ci) {
        this.hidden = ((PackInfoInjection) (Object) info).hidden();
    }

    @ModifyExpressionValue(method = "readPackInfo", at = @At(value = "NEW", target = "(Lnet/minecraft/network/chat/Component;ILnet/minecraft/world/flag/FeatureFlagSet;)Lnet/minecraft/server/packs/repository/Pack$Info;"))
    private static Pack.Info kilt$addForgeDataToInfo(Pack.Info original, @Local PackMetadataSection section, @Local PackResources resources) {
        var info = ((PackInfoInjection) (Object) original);
        info.kilt$markForge();
        info.kilt$setResourceFormat(((PackMetadataSectionInjection) section).getPackFormat(PackType.CLIENT_RESOURCES));
        info.kilt$setDataFormat(((PackMetadataSectionInjection) section).getPackFormat(PackType.SERVER_DATA));
        info.kilt$setHidden(resources.isHidden());

        return original;
    }

    @Override
    public boolean isHidden() {
        return hidden;
    }

    @Mixin(Pack.Info.class)
    public abstract static class InfoInject implements PackInfoInjection {
        @Shadow @Final private int format;
        @Unique private boolean kilt$isForge = false;
        @Unique private int dataFormat;
        @Unique private int resourceFormat;
        @Unique private boolean hidden;

        public InfoInject(Component description, int format, FeatureFlagSet requestedFeatures) {}

        @CreateInitializer
        public InfoInject(Component description, int dataFormat, int resourceFormat, FeatureFlagSet requestedFeatures, boolean hidden) {
            this(description, resourceFormat, requestedFeatures);
            this.dataFormat = dataFormat;
            this.resourceFormat = resourceFormat;
            this.hidden = hidden;
            this.kilt$isForge = true;
        }

        @Override
        public void kilt$markForge() {
            this.kilt$isForge = true;
        }

        @Override
        public boolean hidden() {
            return this.hidden;
        }

        @Override
        public int dataFormat() {
            if (!kilt$isForge)
                return format;

            return dataFormat;
        }

        @Override
        public int resourceFormat() {
            if (!kilt$isForge)
                return format;

            return resourceFormat;
        }

        @Override
        public void kilt$setDataFormat(int format) {
            this.dataFormat = format;
        }

        @Override
        public void kilt$setResourceFormat(int format) {
            this.resourceFormat = format;
        }

        @Override
        public void kilt$setHidden(boolean hidden) {
            this.hidden = hidden;
        }

        @Override
        public int getFormat(PackType type) {
            return type == PackType.SERVER_DATA ? this.dataFormat() : this.resourceFormat();
        }

        @ModifyExpressionValue(method = "compatibility", at = @At(value = "FIELD", target = "Lnet/minecraft/server/packs/repository/Pack$Info;format:I"))
        private int kilt$useForgeFormat(int original, @Local(argsOnly = true) PackType type) {
            if (kilt$isForge) {
                return this.getFormat(type);
            }

            return original;
        }
    }
}