package xyz.bluspring.kilt.forgeinjects.world.effect;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.common.extensions.IForgeMobEffectInstance;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(MobEffectInstance.class)
public abstract class MobEffectInstanceInject implements IForgeMobEffectInstance {
    // todo: i don't know how much i need to implement here
}
