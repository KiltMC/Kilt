package xyz.bluspring.kilt.injects.world.level.levelgen;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.world.level.levelgen.Beardifier;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.neoforged.neoforge.common.world.PieceBeardifierModifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Beardifier.class)
public abstract class BeardifierInject {
    @Definition(id = "structurePiece", local = @Local(type = StructurePiece.class))
    @Definition(id = "PoolElementStructurePiece", type = PoolElementStructurePiece.class)
    @Expression("structurePiece instanceof PoolElementStructurePiece")
    @ModifyExpressionValue(method = "method_42694", at = @At("MIXINEXTRAS:EXPRESSION"))
    private static boolean kilt$addForgePieceBeardifierModifier(boolean original, @Local StructurePiece piece, @Local(ordinal = 0, argsOnly = true) ObjectList<Beardifier.Rigid> list) {
        if (piece instanceof PieceBeardifierModifier modifier) {
            list.add(new Beardifier.Rigid(modifier.getBeardifierBox(), modifier.getTerrainAdjustment(), modifier.getGroundLevelDelta()));
            return false;
        }

        return original;
    }
}
