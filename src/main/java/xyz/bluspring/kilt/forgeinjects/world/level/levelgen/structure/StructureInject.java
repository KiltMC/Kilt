package xyz.bluspring.kilt.forgeinjects.world.level.levelgen.structure;

import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraftforge.common.world.ModifiableStructureInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.injections.world.structure.StructureInjection;

@Mixin(Structure.class)
public class StructureInject implements StructureInjection {
    private ModifiableStructureInfo modifiableStructureInfo;

    @Inject(at = @At("TAIL"), method = "<init>")
    public void kilt$loadModifiedStructureSettings(Structure.StructureSettings structureSettings, CallbackInfo ci) {
        modifiableStructureInfo = new ModifiableStructureInfo(new ModifiableStructureInfo.StructureInfo(structureSettings));
    }

    @Inject(at = @At("HEAD"), method = "method_42698", cancellable = true)
    private static void kilt$useOriginalStructureSettings(Structure structure, CallbackInfoReturnable<Structure.StructureSettings> cir) {
        cir.setReturnValue(((StructureInjection) structure).modifiableStructureInfo().getOriginalStructureInfo().structureSettings());
    }

    @Override
    public Structure.StructureSettings getModifiedStructureSettings() {
        return modifiableStructureInfo().get().structureSettings();
    }

    @Override
    public ModifiableStructureInfo modifiableStructureInfo() {
        return modifiableStructureInfo;
    }
}
