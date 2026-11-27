package xyz.bluspring.kilt.injects.realmsclient.gui.screens;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.realmsclient.gui.screens.RealmsNotificationsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsScreen;

@Mixin(RealmsNotificationsScreen.class)
public abstract class RealmsNotificationsScreenInject extends RealmsScreen {
    @Shadow protected abstract boolean inTitleScreen();

    public RealmsNotificationsScreenInject(Component title) {
        super(title);
    }

    @Definition(id = "height", field = "Lcom/mojang/realmsclient/gui/screens/RealmsNotificationsScreen;height:I")
    @Expression("this.height / 4 + @(48)")
    @ModifyExpressionValue(method = "extractIcons", at = @At("MIXINEXTRAS:EXPRESSION"))
    private int kilt$tryHandleWhenInTitleScreen(int original) {
        return inTitleScreen() ? 32 : original;
    }
}
