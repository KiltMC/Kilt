// TRACKED HASH: ad9e0035fc910cb9d79f74951b83b361e6bd1ba9
package xyz.bluspring.kilt.injects.world.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.Rarity;
import net.neoforged.fml.common.asm.enumextension.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.world.item.RarityInjection;

import java.util.function.UnaryOperator;

@IndexedEnum
@NamedEnum(1)
@NetworkedEnum(NetworkedEnum.NetworkCheck.BIDIRECTIONAL)
@Mixin(Rarity.class)
public class RarityInject implements RarityInjection, IExtensibleEnum {
    private UnaryOperator<Style> styleModifier;

    RarityInject(String fieldName, int ordinal, int id, String name, ChatFormatting color) {}

    @Inject(method = "<init>(Ljava/lang/String;IILjava/lang/String;Lnet/minecraft/ChatFormatting;)V", at = @At("RETURN"))
    public void kilt$init(String string, int i, int id, String name, ChatFormatting color, CallbackInfo ci) {
        this.styleModifier = style -> style.withColor(color);
    }

    @CreateInitializer
    RarityInject(String fieldName, int ordinal, int id, String name, UnaryOperator<Style> styleModifier) {
        this(fieldName, ordinal, id, name, ChatFormatting.BLACK);
        this.styleModifier = styleModifier;
    }

    @Override
    public UnaryOperator<Style> getStyleModifier() {
        return styleModifier;
    }

    @CreateStatic
    private static ExtensionInfo getExtensionInfo() {
        return ExtensionInfo.nonExtended(Rarity.class);
    }
}
