package xyz.bluspring.kilt.mixin.client.gui.components;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Button.Builder.class)
public interface ButtonBuilderAccessor {
    @Accessor
    Component getMessage();

    @Accessor
    Button.OnPress getOnPress();

    @Accessor
    Tooltip getTooltip();

    @Accessor
    int getX();

    @Accessor
    int getY();

    @Accessor
    int getWidth();

    @Accessor
    int getHeight();

    @Accessor
    Button.CreateNarration getCreateNarration();
}
