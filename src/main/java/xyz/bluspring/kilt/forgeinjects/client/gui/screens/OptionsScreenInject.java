package xyz.bluspring.kilt.forgeinjects.client.gui.screens;

import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(OptionsScreen.class)
public abstract class OptionsScreenInject extends Screen {
    @Shadow @Final private Screen lastScreen;

    protected OptionsScreenInject(Component title) {
        super(title);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.lastScreen instanceof PauseScreen ? null : this.lastScreen);
    }
}
