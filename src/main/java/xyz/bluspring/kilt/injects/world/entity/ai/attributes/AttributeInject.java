package xyz.bluspring.kilt.injects.world.entity.ai.attributes;

import net.minecraft.network.chat.TextColor;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.neoforged.neoforge.common.extensions.IAttributeExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;

@Mixin(Attribute.class)
public abstract class AttributeInject implements IAttributeExtension {
    @Shadow private Attribute.Sentiment sentiment;

    @CreateStatic private static final TextColor MERGED_RED = TextColor.fromRgb(0xF93131);
    @CreateStatic private static final TextColor MERGED_BLUE = TextColor.fromRgb(0x7A7AF9);
    @CreateStatic private static final TextColor MERGED_GRAY = TextColor.fromRgb(0xCCCCCC);

    @Override
    public TextColor getMergedStyle(boolean isPositive) {
        return switch (this.sentiment) {
            case POSITIVE -> isPositive ? MERGED_BLUE : MERGED_RED;
            case NEUTRAL -> isPositive ? MERGED_RED : MERGED_BLUE;
            case NEGATIVE -> MERGED_GRAY;
        };
    }
}
