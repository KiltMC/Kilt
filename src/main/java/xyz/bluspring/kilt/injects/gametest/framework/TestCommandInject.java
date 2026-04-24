package xyz.bluspring.kilt.injects.gametest.framework;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.gametest.framework.TestCommand;
import net.minecraft.world.level.block.entity.StructureBlockEntity;

@Mixin(TestCommand.class)
public abstract class TestCommandInject {
    @WrapOperation(method = "showPos", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/StructureBlockEntity;getMetaData()Ljava/lang/String;"))
    private static String kilt$tryUseStructureName(StructureBlockEntity instance, Operation<String> original) {
        return original.call(instance).isBlank() ? instance.getStructureName() : original.call(instance);
    }
}
