package xyz.bluspring.kilt.injects.world.level.block;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Cancellable;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.NoteBlockEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NoteBlock.class)
public abstract class NoteBlockInject {
    @Shadow @Final public static IntegerProperty NOTE;

    @Shadow
    @Final
    public static EnumProperty<NoteBlockInstrument> INSTRUMENT;

    @WrapOperation(method = "useWithoutItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;cycle(Lnet/minecraft/world/level/block/state/properties/Property;)Ljava/lang/Object;"))
    private Object kilt$callForgeNoteChangeEvent(BlockState instance, Property<?> property, Operation<Object> original, @Local(argsOnly = true) Level level, @Local(argsOnly = true) BlockPos pos, @Cancellable CallbackInfoReturnable<InteractionResult> cir) {
        var newNote = CommonHooks.onNoteChange(level, pos, instance, instance.getValue(NOTE), ((BlockState) original.call(instance, property)).getValue(NOTE));

        if (newNote == -1) {
            cir.setReturnValue(InteractionResult.FAIL);
            return null;
        }

        return instance.setValue(NOTE, newNote);
    }

    @Inject(method = "triggerEvent", at = @At("HEAD"), cancellable = true)
    private void firePlayNoteBlockEvent(BlockState state, Level level, BlockPos pos, int id, int param, CallbackInfoReturnable<Boolean> cir, @Local(argsOnly = true) LocalRef<BlockState> stateRef) {
        NoteBlockEvent.Play e = new NoteBlockEvent.Play(level, pos, state, state.getValue(NOTE), state.getValue(INSTRUMENT));
        if (NeoForge.EVENT_BUS.post(e).isCanceled()) {
            cir.setReturnValue(false);
            return;
        }
        stateRef.set(state.setValue(NOTE, e.getVanillaNoteId()).setValue(INSTRUMENT, e.getInstrument()));
    }
}
