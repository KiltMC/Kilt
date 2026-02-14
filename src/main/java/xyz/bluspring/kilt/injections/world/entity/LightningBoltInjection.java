package xyz.bluspring.kilt.injections.world.entity;

import xyz.bluspring.kilt.util.KiltHelper;

public interface LightningBoltInjection {
    default void setDamage(float damage) {
        throw KiltHelper.createMixinException(LightningBoltInjection.class, "setDamage");
    }

    default float getDamage() {
        throw KiltHelper.createMixinException(LightningBoltInjection.class, "getDamage");
    }
}
