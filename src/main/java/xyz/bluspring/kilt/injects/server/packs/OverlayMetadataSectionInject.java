package xyz.bluspring.kilt.injects.server.packs;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.serialization.Codec;
import net.minecraft.server.packs.OverlayMetadataSection;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.neoforged.neoforge.common.conditions.ConditionalOps;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.server.packs.OverlayMetadataSectionInjection;

import java.util.List;

@Mixin(OverlayMetadataSection.class)
public abstract class OverlayMetadataSectionInject {
    @Definition(id = "CODEC", field = "Lnet/minecraft/server/packs/OverlayMetadataSection$OverlayEntry;CODEC:Lcom/mojang/serialization/Codec;")
    @Definition(id = "listOf", method = "Lcom/mojang/serialization/Codec;listOf()Lcom/mojang/serialization/Codec;")
    @Expression("CODEC.listOf()")
    @WrapOperation(method = "method_52429", at = @At("MIXINEXTRAS:EXPRESSION"))
    private static Codec<List<OverlayMetadataSection.OverlayEntry>> kilt$wrapWithConditionalOps(Codec<OverlayMetadataSection.OverlayEntry> instance, Operation<Codec<List<OverlayMetadataSection.OverlayEntry>>> original) {
        return ConditionalOps.kilt$decodeListWithElementConditions(instance, original.call(instance));
    }

    @CreateStatic private static final MetadataSectionType<OverlayMetadataSection> NEOFORGE_TYPE = OverlayMetadataSectionInjection.NEOFORGE_TYPE;
}
