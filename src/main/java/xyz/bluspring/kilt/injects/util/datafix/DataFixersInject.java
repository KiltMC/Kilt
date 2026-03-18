package xyz.bluspring.kilt.injects.util.datafix;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;

import com.google.common.collect.ImmutableMap;
import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.DataFixerBuilder;
import com.mojang.datafixers.schemas.Schema;
import net.neoforged.neoforge.common.data.fixes.NeoForgeEntityLegacyAttributesFix;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.AttributesRename;

@Mixin(DataFixers.class)
public abstract class DataFixersInject {
    @Shadow @Final private static BiFunction<Integer, Schema, Schema> SAME_NAMESPACED;

    @Shadow
    private static UnaryOperator<String> createRenamer(Map<String, String> renameMap) {
        throw new AssertionError();
    }

    @Definition(id = "builder", local = @Local(type = DataFixerBuilder.class, argsOnly = true))
    @Definition(id = "addSchema", method = "Lcom/mojang/datafixers/DataFixerBuilder;addSchema(ILjava/util/function/BiFunction;)Lcom/mojang/datafixers/schemas/Schema;")
    @Definition(id = "SAME_NAMESPACED", field = "Lnet/minecraft/util/datafix/DataFixers;SAME_NAMESPACED:Ljava/util/function/BiFunction;")
    @Expression("builder.addSchema(3803, SAME_NAMESPACED)")
    @Inject(method = "addFixers", at = @At("MIXINEXTRAS:EXPRESSION"))
    private static void kilt$addNeoLegacyAttributesFixSchema(DataFixerBuilder builder, CallbackInfo ci) {
        Schema neoSchema3801 = builder.addSchema(3801, SAME_NAMESPACED);
        builder.addFixer(new NeoForgeEntityLegacyAttributesFix("(Neo) Remove step height attribute", neoSchema3801, List.of("neoforge:step_height")));
    }

    @Definition(id = "builder", local = @Local(type = DataFixerBuilder.class, argsOnly = true))
    @Definition(id = "addSchema", method = "Lcom/mojang/datafixers/DataFixerBuilder;addSchema(ILjava/util/function/BiFunction;)Lcom/mojang/datafixers/schemas/Schema;")
    @Expression("builder.addSchema(3807, ?)")
    @Inject(method = "addFixers", at = @At("MIXINEXTRAS:EXPRESSION"))
    private static void kilt$addNeoReachAttributesToVanillaSchema(DataFixerBuilder builder, CallbackInfo ci) {
        Schema neoSchema3804 = builder.addSchema(3804, SAME_NAMESPACED);
        builder.addFixer(new AttributesRename(neoSchema3804, "(Neo) Rename reach attributes to vanilla", createRenamer(
            ImmutableMap.of(
                "neoforge:entity_reach", "minecraft:player.entity_interaction_range",
                "neoforge:block_reach", "minecraft:player.block_interaction_range"
            )
        )));
    }

    @Definition(id = "builder", local = @Local(type = DataFixerBuilder.class, argsOnly = true))
    @Definition(id = "addSchema", method = "Lcom/mojang/datafixers/DataFixerBuilder;addSchema(ILjava/util/function/BiFunction;)Lcom/mojang/datafixers/schemas/Schema;")
    @Expression("builder.addSchema(3816, ?)")
    @Inject(method = "addFixers", at = @At("MIXINEXTRAS:EXPRESSION"))
    private static void kilt$addNeoGravityAttributesToVanillaSchema(DataFixerBuilder builder, CallbackInfo ci) {
        Schema neoSchema3815 = builder.addSchema(3815, SAME_NAMESPACED);
        builder.addFixer(new AttributesRename(neoSchema3815, "(Neo) Rename gravity attribute to vanilla", createRenamer(
            ImmutableMap.of(
                "neoforge:entity_gravity", "minecraft:generic.gravity"
            )
        )));
    }
}
