package xyz.bluspring.kilt.injects.world.item;

import net.minecraft.world.item.UseAnim;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.world.item.UseAnimInjection;

@Mixin(UseAnim.class)
public abstract class UseAnimInject implements UseAnimInjection {
    @CreateStatic
    private static UseAnim CUSTOM = UseAnimInjection.CUSTOM;
}
