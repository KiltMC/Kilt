package xyz.bluspring.kilt.injections.client.model;

import net.neoforged.neoforge.client.entity.animation.json.AnimationHolder;
import net.neoforged.neoforge.client.entity.animation.json.AnimationLoader;
import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.AnimationState;

public interface HierarchicalModelInjection {
    static AnimationHolder getAnimation(ResourceLocation key) {
        return AnimationLoader.INSTANCE.getAnimationHolder(key);
    }

    default void animate(AnimationState state, AnimationHolder animation, float ageInTicks) {
        throw KiltHelper.createMixinException(HierarchicalModelInjection.class, "animate");
    }

    default void animateWalk(AnimationHolder animation, float limbSwing, float limbSwingAmount, float maxAnimationSpeed, float animationScaleFactor) {
        throw KiltHelper.createMixinException(HierarchicalModelInjection.class, "animateWalk");
    }

    default void animate(AnimationState state, AnimationHolder animation, float ageInTicks, float speed) {
        throw KiltHelper.createMixinException(HierarchicalModelInjection.class, "animate");
    }

    default void applyStatic(AnimationHolder animation) {
        throw KiltHelper.createMixinException(HierarchicalModelInjection.class, "applyStatic");
    }
}
