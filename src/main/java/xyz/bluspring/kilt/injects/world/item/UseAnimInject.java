package xyz.bluspring.kilt.injects.world.item;

import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.injections.world.item.UseAnimInjection;

import net.minecraft.world.item.UseAnim;

@Mixin(UseAnim.class)
public enum UseAnimInject implements UseAnimInjection {
    CUSTOM;
}
