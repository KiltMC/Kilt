package xyz.bluspring.kilt.injects.world.level.block;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Cancellable;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.neoforge.common.CommonHooks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NoteBlock.class)
public abstract class NoteBlockInject {
    @Shadow @Final public static IntegerProperty NOTE;

    @WrapOperation(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;cycle(Lnet/minecraft/world/level/block/state/properties/Property;)Ljava/lang/Object;"))
    private Object kilt$callForgeNoteChangeEvent(BlockState instance, Property<?> property, Operation<Object> original, @Local(argsOnly = true) Level level, @Local(argsOnly = true) BlockPos pos, @Cancellable CallbackInfoReturnable<InteractionResult> cir) {
        var newNote = CommonHooks.onNoteChange(level, pos, instance, instance.getValue(NOTE), ((BlockState) original.call(instance, property)).getValue(NOTE));

        if (newNote == -1) {
            cir.setReturnValue(InteractionResult.FAIL);
            return null;
        }

        return instance.setValue(NOTE, newNote);
    }
}
