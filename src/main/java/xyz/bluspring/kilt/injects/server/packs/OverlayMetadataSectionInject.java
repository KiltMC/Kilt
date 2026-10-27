package xyz.bluspring.kilt.injects.server.packs;

import java.util.List;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.serialization.Codec;
import net.neoforged.neoforge.common.conditions.ConditionalOps;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.server.packs.OverlayMetadataSectionInjection;

import net.minecraft.server.packs.OverlayMetadataSection;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionType;

@Mixin(OverlayMetadataSection.class)
public abstract class OverlayMetadataSectionInject {
    @CreateStatic private static final MetadataSectionType<OverlayMetadataSection> NEOFORGE_CLIENT_TYPE = OverlayMetadataSectionInjection.NEOFORGE_CLIENT_TYPE;
    @CreateStatic private static final MetadataSectionType<OverlayMetadataSection> NEOFORGE_SERVER_TYPE = OverlayMetadataSectionInjection.NEOFORGE_SERVER_TYPE;

    @CreateStatic
    private static MetadataSectionType<OverlayMetadataSection> forPackTypeNeoForge(PackType packType) {
        return OverlayMetadataSectionInjection.forPackTypeNeoForge(packType);
    }

    @Mixin(OverlayMetadataSection.OverlayEntry.class)
    public abstract static class OverlayEntryInject {
        @Definition(id = "CODEC", field = "Lnet/minecraft/server/packs/OverlayMetadataSection$OverlayEntry$IntermediateEntry;CODEC:Lcom/mojang/serialization/Codec;")
        @Definition(id = "listOf", method = "Lcom/mojang/serialization/Codec;listOf()Lcom/mojang/serialization/Codec;")
        @Expression("CODEC.listOf()")
        @WrapOperation(method = "listCodecForPackType", at = @At("MIXINEXTRAS:EXPRESSION"))
        private static <A> Codec<List<A>> kilt$wrapWithConditionalCodec(Codec<A> instance, Operation<Codec<List<A>>> original) {
            return ConditionalOps.kilt$decodeListWithElementConditions(original.call(instance), instance);
        }
    }
}
