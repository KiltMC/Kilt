package xyz.bluspring.kilt.injects.realmsclient.gui.screens;

import com.mojang.realmsclient.gui.screens.RealmsGenericErrorScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsScreen;

@Mixin(RealmsGenericErrorScreen.class)
public abstract class RealmsGenericErrorScreenInject extends RealmsScreen {
    @Shadow @Final private Screen nextScreen;

    public RealmsGenericErrorScreenInject(Component title) {
        super(title);
    }

    @Override
    public boolean keyPressed(KeyEvent keyEvent) {
        if (keyEvent.isEscape()) {
            minecraft.setScreen(this.nextScreen);
            return true;
        }

        return super.keyPressed(keyEvent);
    }
}
