package xyz.bluspring.kilt.forgeinjects.world.level.block;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DoublePlantBlock.class)
public abstract class DoublePlantBlockInject {
    @Definition(id = "state", local = @Local(type = BlockState.class, argsOnly = true))
    @Definition(id = "getValue", method = "Lnet/minecraft/world/level/block/state/BlockState;getValue(Lnet/minecraft/world/level/block/state/properties/Property;)Ljava/lang/Comparable;")
    @Definition(id = "HALF", field = "Lnet/minecraft/world/level/block/DoublePlantBlock;HALF:Lnet/minecraft/world/level/block/state/properties/EnumProperty;")
    @Definition(id = "UPPER", field = "Lnet/minecraft/world/level/block/state/properties/DoubleBlockHalf;UPPER:Lnet/minecraft/world/level/block/state/properties/DoubleBlockHalf;")
    @Expression("state.getValue(HALF) != UPPER")
    @ModifyExpressionValue(method = "canSurvive", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$ensureCanSurviveInWorldGenPlacement(boolean original, @Local(argsOnly = true) BlockState state) {
        // Kilt: Not sure how necessary this method is, also ironically this method is arguably easier by having this in the first if.
        return original || state.getBlock() != (Object) this;
    }
}
