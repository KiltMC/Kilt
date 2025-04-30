package xyz.bluspring.kilt.injections.world.effect;

import net.minecraftforge.client.extensions.common.IClientMobEffectExtensions;

import java.util.function.Consumer;

public interface MobEffectInjection {
    Object getEffectRendererInternal();

    default void initializeClient(Consumer<IClientMobEffectExtensions> consumer) {};
}
