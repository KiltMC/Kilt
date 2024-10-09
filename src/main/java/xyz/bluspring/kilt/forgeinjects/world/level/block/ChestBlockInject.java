package xyz.bluspring.kilt.forgeinjects.world.level.block;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ChestBlock.class)
public abstract class ChestBlockInject {
    @Shadow @Final public static EnumProperty<ChestType> TYPE;

    @ModifyReturnValue(method = "mirror", at = @At("RETURN"))
    private BlockState kilt$rotateIfMirrored(BlockState original, @Local(argsOnly = true) Mirror mirror) {
        return mirror == Mirror.NONE ? original : original.setValue(TYPE, original.getValue(TYPE).getOpposite());
    }
}
