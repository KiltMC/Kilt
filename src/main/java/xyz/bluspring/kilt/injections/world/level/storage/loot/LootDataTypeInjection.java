package xyz.bluspring.kilt.injections.world.level.storage.loot;

import java.util.Optional;
import java.util.function.BiConsumer;

import com.mojang.serialization.Codec;
import org.jetbrains.annotations.Nullable;
import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.resources.ResourceLocation;

public interface LootDataTypeInjection<T> {
    default @Nullable T defaultValue() {
        throw KiltHelper.createMixinException(LootDataTypeInjection.class, "defaultValue");
    }

    default Codec<Optional<T>> conditionalCodec() {
        throw KiltHelper.createMixinException(LootDataTypeInjection.class, "conditionalCodec");
    }

    default BiConsumer<T, ResourceLocation> idSetter() {
        throw KiltHelper.createMixinException(LootDataTypeInjection.class, "idSetter");
    }
}
