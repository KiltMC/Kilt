package xyz.bluspring.kilt.injects.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.types.templates.TypeTemplate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.minecraft.util.datafix.schemas.V2832;

@Mixin(V2832.class)
public abstract class V2832Inject {
    @ModifyArg(method = "lambda$registerTypes$0", at = @At(value = "INVOKE", target = "Lcom/mojang/datafixers/DSL;fields(Ljava/lang/String;Lcom/mojang/datafixers/types/templates/TypeTemplate;)Lcom/mojang/datafixers/types/templates/TypeTemplate;", ordinal = 1, remap = false))
    private static TypeTemplate kilt$useOrInDsl(TypeTemplate original) {
        return DSL.or(original, DSL.remainder());
    }
}
