package xyz.bluspring.kilt.injects.world.level.levelgen;

import java.util.List;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.neoforged.neoforge.common.world.PieceBeardifierModifier;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.world.level.levelgen.Beardifier;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePiece;

@Mixin(Beardifier.class)
public abstract class BeardifierInject {
    @Shadow
    private static BoundingBox includeBoundingBox(@Nullable BoundingBox encompassingBox, BoundingBox newBox) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    @Definition(id = "structurePiece", local = @Local(type = StructurePiece.class))
    @Definition(id = "PoolElementStructurePiece", type = PoolElementStructurePiece.class)
    @Expression("structurePiece instanceof PoolElementStructurePiece")
    @WrapOperation(method = "forStructuresInChunk", at = @At("MIXINEXTRAS:EXPRESSION"))
    private static boolean kilt$addForgePieceBeardifierModifier(Object object, Operation<Boolean> original, @Local(name = "anyPieceBoundingBox") LocalRef<BoundingBox> anyPieceBoundingBox, @Local(name = "rigids") List<Beardifier.Rigid> rigids) {
        if (object instanceof PieceBeardifierModifier modifier) {
            rigids.add(new Beardifier.Rigid(modifier.getBeardifierBox(), modifier.getTerrainAdjustment(), modifier.getGroundLevelDelta()));
            anyPieceBoundingBox.set(includeBoundingBox(anyPieceBoundingBox.get(), modifier.getBeardifierBox()));
            return false;
        }

        return original.call(object);
    }
}
