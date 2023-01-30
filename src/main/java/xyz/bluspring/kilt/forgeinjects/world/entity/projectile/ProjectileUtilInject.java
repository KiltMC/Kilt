package xyz.bluspring.kilt.forgeinjects.world.entity.projectile;

import net.minecraft.world.entity.projectile.ProjectileUtil;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.injections.entity.ProjectileUtilInjection;

@Mixin(ProjectileUtil.class)
public class ProjectileUtilInject implements ProjectileUtilInjection {
}
