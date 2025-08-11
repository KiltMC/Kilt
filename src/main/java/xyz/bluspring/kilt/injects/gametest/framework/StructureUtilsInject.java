package xyz.bluspring.kilt.injects.gametest.framework;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.gametest.framework.StructureUtils;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(StructureUtils.class)
public abstract class StructureUtilsInject {
    @Definition(id = "string", local = @Local(type = String.class, ordinal = 1))
    @Definition(id = "structureName", local = @Local(type = String.class, argsOnly = true))
    @Expression("string = structureName + ?")
    @ModifyVariable(method = "getStructureTemplate", at = @At("MIXINEXTRAS:EXPRESSION"), argsOnly = true)
    private static String kilt$usePathOfStructureName(String original) {
        return new ResourceLocation(original).getPath();
    }
}
