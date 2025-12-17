package xyz.bluspring.kilt.mixin.compat.porting_lib.extensions;

import io.github.fabricators_of_create.porting_lib.item.UseFirstBehaviorItem;
import net.minecraftforge.common.extensions.IForgeItem;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(UseFirstBehaviorItem.class)
public interface UseFirstBehaviorItemMixin extends IForgeItem {
}
