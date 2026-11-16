package xyz.bluspring.kilt.injects.util.datafix.schemas;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.types.templates.TypeTemplate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.minecraft.util.datafix.schemas.V2832;

@Mixin(V2832.class)
public abstract class V2832Inject {
    @Definition(id = "fields", method = "Lcom/mojang/datafixers/DSL;fields(Ljava/lang/String;Lcom/mojang/datafixers/types/templates/TypeTemplate;)Lcom/mojang/datafixers/types/templates/TypeTemplate;")
    @Definition(id = "taggedChoiceLazy", method = "Lcom/mojang/datafixers/DSL;taggedChoiceLazy(Ljava/lang/String;Lcom/mojang/datafixers/types/Type;Ljava/util/Map;)Lcom/mojang/datafixers/types/templates/TaggedChoice;")
    @Expression("fields('generator', taggedChoiceLazy('type', ?, ?))")
    @ModifyArg(method = "method_38837", at = @At("MIXINEXTRAS:EXPRESSION"))
    private static TypeTemplate kilt$useOrInDsl(TypeTemplate original) {
        return DSL.or(original, DSL.remainder());
    }
}
