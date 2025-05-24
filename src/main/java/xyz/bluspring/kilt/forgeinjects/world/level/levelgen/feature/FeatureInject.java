package xyz.bluspring.kilt.forgeinjects.world.level.levelgen.feature;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraftforge.common.Tags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Feature.class)
public abstract class FeatureInject {
    @ModifyReturnValue(method = "isStone", at = @At("RETURN"))
    private static boolean kilt$checkIsStone(boolean original, @Local(argsOnly = true) BlockState state) {
        return original || state.is(Tags.Blocks.STONE);
    }
}
