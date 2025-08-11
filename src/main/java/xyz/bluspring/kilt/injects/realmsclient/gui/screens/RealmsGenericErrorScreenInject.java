package xyz.bluspring.kilt.injects.realmsclient.gui.screens;

import com.mojang.realmsclient.gui.screens.RealmsGenericErrorScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsScreen;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(RealmsGenericErrorScreen.class)
public abstract class RealmsGenericErrorScreenInject extends RealmsScreen {
    @Shadow @Final private Screen nextScreen;

    public RealmsGenericErrorScreenInject(Component title) {
        super(title);
    }

    @Intrinsic
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.minecraft.setScreen(this.nextScreen);
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
