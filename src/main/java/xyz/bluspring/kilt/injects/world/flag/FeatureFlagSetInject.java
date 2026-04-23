package xyz.bluspring.kilt.injects.world.flag;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import it.unimi.dsi.fastutil.HashCommon;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.injections.world.flag.FeatureFlagSetInjection;
import xyz.bluspring.kilt.util.IteratorWrapper;

import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlagUniverse;

@Mixin(FeatureFlagSet.class)
public abstract class FeatureFlagSetInject implements FeatureFlagSetInjection {
    @Unique private static final long[] EMPTY_EXT_MASK = new long[0];

    @Unique private long[] extendedMask = EMPTY_EXT_MASK;

    private FeatureFlagSetInject(@Nullable FeatureFlagUniverse universe, long mask) {
    }

    @CreateInitializer
    private FeatureFlagSetInject(@Nullable FeatureFlagUniverse universe, long mask, long[] extendedMask) {
        this(universe, mask);
        this.extendedMask = extendedMask;
    }

    @ModifyExpressionValue(method = "create", at = @At(value = "NEW", target = "(Lnet/minecraft/world/flag/FeatureFlagUniverse;J)Lnet/minecraft/world/flag/FeatureFlagSet;"))
    private static FeatureFlagSet kilt$addExtendedMaskToFlagSet(FeatureFlagSet original, @Local(argsOnly = true) FeatureFlagUniverse universe, @Local(argsOnly = true) Collection<FeatureFlag> flags) {
        original.kilt$setExtendedMask(computeExtendedMask(universe, 0, 0L, flags));
        return original;
    }

    @WrapOperation(method = {"of(Lnet/minecraft/world/flag/FeatureFlag;)Lnet/minecraft/world/flag/FeatureFlagSet;", "of(Lnet/minecraft/world/flag/FeatureFlag;[Lnet/minecraft/world/flag/FeatureFlag;)Lnet/minecraft/world/flag/FeatureFlagSet;"}, at = @At(value = "FIELD", target = "Lnet/minecraft/world/flag/FeatureFlag;mask:J", opcode = Opcodes.GETFIELD))
    private static long kilt$checkMaskIndexForMask(FeatureFlag instance, Operation<Long> original) {
        return instance.kilt$extMaskIndex() >= 0 ? 0 : original.call(instance);
    }

    @ModifyExpressionValue(method = "of(Lnet/minecraft/world/flag/FeatureFlag;)Lnet/minecraft/world/flag/FeatureFlagSet;", at = @At(value = "NEW", target = "(Lnet/minecraft/world/flag/FeatureFlagUniverse;J)Lnet/minecraft/world/flag/FeatureFlagSet;"))
    private static FeatureFlagSet kilt$addExtendedMaskToFlagSet(FeatureFlagSet original, @Local(argsOnly = true) FeatureFlag flag) {
        original.kilt$setExtendedMask(computeExtendedMask(flag.kilt$universe(), flag.kilt$extMaskIndex(), flag.kilt$mask(), List.of()));
        return original;
    }

    @ModifyExpressionValue(method = "of(Lnet/minecraft/world/flag/FeatureFlag;[Lnet/minecraft/world/flag/FeatureFlag;)Lnet/minecraft/world/flag/FeatureFlagSet;", at = @At(value = "NEW", target = "(Lnet/minecraft/world/flag/FeatureFlagUniverse;J)Lnet/minecraft/world/flag/FeatureFlagSet;"))
    private static FeatureFlagSet kilt$addExtendedMaskToFlagSet(FeatureFlagSet original, @Local(argsOnly = true) FeatureFlag flag, @Local(argsOnly = true) FeatureFlag[] flags) {
        original.kilt$setExtendedMask(computeExtendedMask(flag.kilt$universe(), flag.kilt$extMaskIndex(), flag.kilt$mask(), flags.length == 0 ? List.of() : Arrays.asList(flags)));
        return original;
    }

    @ModifyExpressionValue(method = "computeMask", at = @At(value = "INVOKE", target = "Ljava/lang/Iterable;iterator()Ljava/util/Iterator;"))
    private static <T extends FeatureFlag> Iterator<T> kilt$filterExtendedMasks(Iterator<T> original) {
        return new IteratorWrapper<>(original, flag -> flag.kilt$extMaskIndex() >= 0 ? null : flag);
    }

    @Unique
    private static long[] computeExtendedMask(FeatureFlagUniverse universe, int firstExtIndex, long firstMask, Iterable<FeatureFlag> otherFlags) {
        long[] extMask = EMPTY_EXT_MASK;
        if (firstExtIndex >= 0) {
            extMask = new long[firstExtIndex + 1];
            extMask[firstExtIndex] |= firstMask;
        }

        for (FeatureFlag flag : otherFlags) {
            if (flag.kilt$extMaskIndex() < 0)
                continue;

            if (universe != flag.kilt$universe())
                throw new IllegalStateException("Mismatched feature universe, expected '" + universe + "', but got '" + flag.kilt$universe() + "'");

            if (flag.kilt$extMaskIndex() >= extMask.length)
                extMask = Arrays.copyOfRange(extMask, 0, flag.kilt$extMaskIndex() + 1);

            extMask[flag.kilt$extMaskIndex()] |= flag.kilt$mask();
        }

        return extMask;
    }

    @Expression("(? & ?) != 0")
    @ModifyExpressionValue(method = "contains", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$checkContainsWithExtendedMask(boolean original, @Local(argsOnly = true) FeatureFlag flag) {
        if (flag.kilt$extMaskIndex() < 0) {
            return original;
        }

        if (this.extendedMask.length > flag.kilt$extMaskIndex()) {
            return (this.extendedMask[flag.kilt$extMaskIndex()] & flag.kilt$mask()) != 0;
        }

        return false;
    }

    @Expression("(? & ~?) == 0")
    @ModifyExpressionValue(method = "isSubsetOf", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$checkSubsetWithExtendedMask(boolean original, @Local(argsOnly = true) FeatureFlagSet set) {
        int len = Math.max(this.extendedMask.length, set.kilt$extendedMask().length);
        for (int i = 0; i < len; i++) {
            long thisMask = i < this.extendedMask.length ? this.extendedMask[i] : 0;
            long otherMask = i < set.kilt$extendedMask().length ? set.kilt$extendedMask()[i] : 0;
            if ((thisMask & ~otherMask) != 0)
                return false;
        }

        return original;
    }

    @Expression("(? & ?) != 0")
    @ModifyExpressionValue(method = "intersects", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$checkIntersectsWithExtendedMask(boolean original, @Local(argsOnly = true) FeatureFlagSet set) {
        int len = Math.min(this.extendedMask.length, set.kilt$extendedMask().length);
        for (int i = 0; i < len; i++) {
            long thisMask = this.extendedMask[i];
            long otherMask = set.kilt$extendedMask()[i];
            if ((thisMask & otherMask) != 0)
                return true;
        }

        return original;
    }

    @ModifyExpressionValue(method = "join", at = @At(value = "NEW", target = "(Lnet/minecraft/world/flag/FeatureFlagUniverse;J)Lnet/minecraft/world/flag/FeatureFlagSet;"))
    private FeatureFlagSet kilt$addExtendedMaskToSet(FeatureFlagSet original, @Local(argsOnly = true) FeatureFlagSet other) {
        long[] extMask = EMPTY_EXT_MASK;
        if (this.extendedMask.length > 0 || other.kilt$extendedMask().length > 0) {
            extMask = new long[Math.max(this.extendedMask.length, other.kilt$extendedMask().length)];
            for (int i = 0; i < extMask.length; i++) {
                long thisMask = i < this.extendedMask.length ? this.extendedMask[i] : 0;
                long otherMask = i < other.kilt$extendedMask().length ? other.kilt$extendedMask()[i] : 0;
                extMask[i] = thisMask | otherMask;
            }
        }

        original.kilt$setExtendedMask(extMask);
        return original;
    }

    @ModifyExpressionValue(method = "subtract", at = @At(value = "NEW", target = "(Lnet/minecraft/world/flag/FeatureFlagUniverse;J)Lnet/minecraft/world/flag/FeatureFlagSet;"))
    private FeatureFlagSet kilt$removeExtendedMaskFromSet(FeatureFlagSet original, @Local(argsOnly = true) FeatureFlagSet other) {
        long[] extMask = EMPTY_EXT_MASK;
        if (this.extendedMask.length > 0 || other.kilt$extendedMask().length > 0) {
            extMask = new long[this.extendedMask.length];
            for (int i = 0; i < extMask.length; i++) {
                long otherMask = i < other.kilt$extendedMask().length ? other.kilt$extendedMask()[i] : 0;
                extMask[i] = this.extendedMask[i] & ~otherMask;
            }
        }

        original.kilt$setExtendedMask(extMask);
        return original;
    }

    @Definition(id = "mask", field = "Lnet/minecraft/world/flag/FeatureFlagSet;mask:J")
    @Definition(id = "featureFlagSet", local = @Local(type = FeatureFlagSet.class))
    @Expression("this.mask == featureFlagSet.mask")
    @ModifyExpressionValue(method = "equals", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$checkExtendedMaskMatches(boolean original, @Local FeatureFlagSet other) {
        return original && Arrays.equals(this.extendedMask, other.kilt$extendedMask());
    }

    @ModifyReturnValue(method = "hashCode", at = @At("RETURN"))
    private int kilt$addExtendedMaskToHashCode(int original) {
        for (long extMask : this.extendedMask) {
            original = 13 * original + (int) HashCommon.mix(extMask);
        }

        return original;
    }

    @Override
    public long[] kilt$extendedMask() {
        return this.extendedMask;
    }

    @Override
    public void kilt$setExtendedMask(long[] extendedMask) {
        this.extendedMask = extendedMask;
    }
}
