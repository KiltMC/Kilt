// TRACKED HASH: d27191d511ec2b22a36acfc37e2404e5b0edc805
package xyz.bluspring.kilt.forgeinjects.world.inventory;

import net.minecraft.world.inventory.RecipeBookType;
import net.neoforged.fml.common.asm.enumextension.ExtensionInfo;
import net.neoforged.fml.common.asm.enumextension.IExtensibleEnum;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.stats.RecipeBookSettingsInjection;
import xyz.bluspring.kilt.injections.world.inventory.RecipeBookTypeInjection;

import java.util.Locale;

@Mixin(RecipeBookType.class)
public class RecipeBookTypeInject implements RecipeBookTypeInjection, IExtensibleEnum {
    @CreateStatic
    private static ExtensionInfo getExtensionInfo() {
        return ExtensionInfo.nonExtended((Class) RecipeBookType.class);
    }
}