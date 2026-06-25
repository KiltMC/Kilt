package xyz.bluspring.kilt.injects.world.flag;

import java.util.Map;

import com.google.common.base.Preconditions;
import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.injections.world.flag.FeatureFlagInjection;
import xyz.bluspring.kilt.injections.world.flag.FeatureFlagRegistryInjection;

import net.minecraft.resources.Identifier;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlagRegistry;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlagUniverse;

@Mixin(FeatureFlagRegistry.class)
public abstract class FeatureFlagRegistryInject implements FeatureFlagRegistryInjection {
    @Shadow @Final private Map<Identifier, FeatureFlag> names;

    @Override
    public FeatureFlag getFlag(Identifier id) {
        return Preconditions.checkNotNull(this.names.get(id), "Flag %s was not registered", id);
    }

    @Override
    public Map<Identifier, FeatureFlag> getAllFlags() {
        return this.names;
    }

    @Override
    public boolean hasAnyModdedFlags() {
        return this.names.values().stream().anyMatch(FeatureFlagInjection::isModded);
    }

    @Mixin(FeatureFlagRegistry.Builder.class)
    public static abstract class BuilderInject implements FeatureFlagRegistryInjection.BuilderInjection {
        @Shadow
        public abstract FeatureFlag create(Identifier identifier);

        @Unique private boolean kilt$isModded = false;

        @Override
        public FeatureFlag create(Identifier id, boolean modded) {
            this.kilt$isModded = modded;
            FeatureFlag flag = this.create(id);
            this.kilt$isModded = false;

            return flag;
        }

        @Definition(id = "id", field = "Lnet/minecraft/world/flag/FeatureFlagRegistry$Builder;id:I")
        @Expression("this.id >= 64")
        @ModifyExpressionValue(method = "create", at = @At("MIXINEXTRAS:EXPRESSION"))
        private boolean kilt$removeFeatureFlagLimit(boolean original) {
            return false;
        }

        @WrapOperation(method = "create", at = @At(value = "NEW", target = "(Lnet/minecraft/world/flag/FeatureFlagUniverse;I)Lnet/minecraft/world/flag/FeatureFlag;"))
        private FeatureFlag kilt$calculateFeatureFlagId(FeatureFlagUniverse universe, int mask, Operation<FeatureFlag> original) {
            FeatureFlag flag = original.call(universe, mask % FeatureFlagSet.MAX_CONTAINER_SIZE);
            flag.kilt$setExtMaskIndex(mask / FeatureFlagSet.MAX_CONTAINER_SIZE);
            flag.kilt$setModded(this.kilt$isModded);

            return flag;
        }
    }
}
