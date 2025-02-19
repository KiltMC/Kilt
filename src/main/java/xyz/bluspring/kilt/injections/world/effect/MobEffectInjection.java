package xyz.bluspring.kilt.injections.world.effect;

public interface MobEffectInjection {
    default Object getEffectRendererInternal() {
        throw new IllegalStateException();
    }
}
