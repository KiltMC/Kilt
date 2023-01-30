package xyz.bluspring.kilt.forgeinjects.world.entity;

import net.minecraft.world.entity.MobCategory;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.injections.entity.MobCategoryInjection;

@Mixin(MobCategory.class)
public class MobCategoryInject implements MobCategoryInjection {
}
