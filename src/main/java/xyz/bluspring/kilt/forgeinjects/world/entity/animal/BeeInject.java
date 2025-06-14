package xyz.bluspring.kilt.forgeinjects.world.entity.animal;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Bee.class)
public abstract class BeeInject {
    @Definition(id = "blockEntity", local = @Local(type = BlockEntity.class))
    @Definition(id = "getType", method = "Lnet/minecraft/world/level/block/entity/BlockEntity;getType()Lnet/minecraft/world/level/block/entity/BlockEntityType;")
    @Expression("blockEntity.getType() == ?")
    @ModifyExpressionValue(method = "isHiveValid", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$checkIfBeehiveBlockEntity(boolean original, @Local BlockEntity blockEntity) {
        return original || blockEntity instanceof BeehiveBlockEntity;
    }
}
