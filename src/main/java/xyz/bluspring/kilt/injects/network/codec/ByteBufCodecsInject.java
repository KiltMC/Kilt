package xyz.bluspring.kilt.injects.network.codec;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.sugar.Local;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.holdersets.HolderSetType;
import net.neoforged.neoforge.registries.holdersets.ICustomHolderSet;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.HolderSet;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.VarInt;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;

@Mixin(ByteBufCodecs.class)
public interface ByteBufCodecsInject {
    @Mixin(targets = "net.minecraft.network.codec.ByteBufCodecs$23")
    abstract class RegistryInject {
        // Kilt: is this needed?
    }

    @Mixin(targets = "net.minecraft.network.codec.ByteBufCodecs$25")
    abstract class HolderSetInject<T> {
        @Shadow @Final private ResourceKey val$registryKey;
        @Unique private final Map<HolderSetType, StreamCodec<RegistryFriendlyByteBuf, ? extends ICustomHolderSet<T>>> holderSetCodecs = new ConcurrentHashMap<>();

        private StreamCodec<RegistryFriendlyByteBuf, ? extends ICustomHolderSet<T>> holderSetCodec(HolderSetType type) {
            return this.holderSetCodecs.computeIfAbsent(type, key -> key.makeStreamCodec(this.val$registryKey));
        }

        private <H extends ICustomHolderSet<T>> H cast(ICustomHolderSet<T> holderSet) {
            return (H) holderSet;
        }

        @Definition(id = "i", local = @Local(type = int.class, ordinal = 0))
        @Expression("i == -1")
        @Inject(method = "decode(Lnet/minecraft/network/RegistryFriendlyByteBuf;)Lnet/minecraft/core/HolderSet;", at = @At("MIXINEXTRAS:EXPRESSION"), cancellable = true)
        private void kilt$handleNeoHolderSetCodec(RegistryFriendlyByteBuf buf, CallbackInfoReturnable<HolderSet<T>> cir, @Local int id) {
            if (id < -1) {
                var type = NeoForgeRegistries.HOLDER_SET_TYPES.byId(-2 - id);
                if (type != null) {
                    cir.setReturnValue(this.holderSetCodec(type).decode(buf));
                }
            }
        }

        @Inject(method = "encode(Lnet/minecraft/network/RegistryFriendlyByteBuf;Lnet/minecraft/core/HolderSet;)V", at = @At("HEAD"), cancellable = true)
        private void kilt$tryHandleNeoHolderSetCodecs(RegistryFriendlyByteBuf buf, HolderSet<T> holderSet, CallbackInfo ci) {
            if (buf.getConnectionType().isNeoForge() && holderSet instanceof ICustomHolderSet<T> customHolderSet) {
                VarInt.write(buf, NeoForgeRegistries.HOLDER_SET_TYPES.getId(customHolderSet.type()));
                this.holderSetCodec(customHolderSet.type()).encode(buf, cast(customHolderSet));
                ci.cancel();
            }
        }
    }
}
