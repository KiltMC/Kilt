package xyz.bluspring.kilt.injects.client.gui.font.providers;

import net.neoforged.fml.common.asm.enumextension.ExtensionInfo;
import net.neoforged.fml.common.asm.enumextension.IExtensibleEnum;
import net.neoforged.fml.common.asm.enumextension.NamedEnum;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;

import net.minecraft.client.gui.font.providers.GlyphProviderType;

@NamedEnum
@Mixin(GlyphProviderType.class)
public abstract class GlyphProviderTypeInject implements IExtensibleEnum {
    @CreateStatic
    private static ExtensionInfo getExtensionInfo() {
        return ExtensionInfo.nonExtended(GlyphProviderType.class);
    }
}
