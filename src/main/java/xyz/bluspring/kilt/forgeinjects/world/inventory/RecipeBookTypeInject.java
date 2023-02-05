package xyz.bluspring.kilt.forgeinjects.world.inventory;

import net.minecraft.world.inventory.RecipeBookType;
import net.minecraftforge.common.IExtensibleEnum;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.injections.stats.RecipeBookSettingsInjection;
import xyz.bluspring.kilt.injections.world.inventory.RecipeBookTypeInjection;

import java.util.Locale;

@Mixin(RecipeBookType.class)
public class RecipeBookTypeInject implements RecipeBookTypeInjection, IExtensibleEnum {
    @Override
    public void init() {
        var name = ((RecipeBookType) (Object) this).name().toLowerCase(Locale.ROOT).replace("_", "");
        RecipeBookSettingsInjection.addTagsForType((RecipeBookType) (Object) this, "is" + name + "GuiOpen", "is" + name + "FilteringCraftable");
    }
}
