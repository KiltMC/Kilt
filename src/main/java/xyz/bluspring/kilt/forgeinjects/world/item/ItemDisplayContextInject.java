package xyz.bluspring.kilt.forgeinjects.world.item;

import com.mojang.serialization.Codec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraftforge.common.IExtensibleEnum;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryInternal;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.world.item.ItemDisplayContextInjection;

import java.util.Objects;
import java.util.function.IntFunction;

@Mixin(ItemDisplayContext.class)
public class ItemDisplayContextInject implements ItemDisplayContextInjection, IExtensibleEnum {
    @Shadow @Final @Mutable public static Codec<ItemDisplayContext> CODEC;
    @Shadow @Final @Mutable public static IntFunction<ItemDisplayContext> BY_ID;
    @CreateStatic
    private static final IForgeRegistry.AddCallback<ItemDisplayContext> ADD_CALLBACK = ItemDisplayContextInjection.ADD_CALLBACK;

    @Unique private boolean isModded = false;
    @Unique private ItemDisplayContext fallback;

    @Override
    public boolean isModded() {
        return isModded;
    }

    @Override
    public @Nullable ItemDisplayContext fallback() {
        return fallback;
    }

    @Override
    public void kilt$markModded() {
        isModded = true;
    }

    @Override
    public void kilt$setFallback(ItemDisplayContext fallback) {
        this.fallback = fallback;
    }

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void kilt$replaceWithExtraCodec(CallbackInfo ci) {
        CODEC = ExtraCodecs.lazyInitializedCodec(() -> ForgeRegistries.DISPLAY_CONTEXTS.get().getCodec());
        BY_ID = id -> Objects.requireNonNullElse(((IForgeRegistryInternal<ItemDisplayContext>) ForgeRegistries.DISPLAY_CONTEXTS.get()).getValue(id < 0 ? Byte.MAX_VALUE + -id : id), ItemDisplayContext.NONE);
    }
}
