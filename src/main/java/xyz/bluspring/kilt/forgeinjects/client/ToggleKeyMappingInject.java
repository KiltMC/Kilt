package xyz.bluspring.kilt.forgeinjects.client;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.ToggleKeyMapping;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.BooleanSupplier;

@Mixin(ToggleKeyMapping.class)
public abstract class ToggleKeyMappingInject extends KeyMapping {
    @Shadow @Final private BooleanSupplier needsToggle;

    public ToggleKeyMappingInject(String name, int keyCode, String category) {
        super(name, keyCode, category);
    }

    @WrapWithCondition(method = "setDown", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;setDown(Z)V"))
    private boolean kilt$checkIfContextActive(KeyMapping instance, boolean value) {
        return instance.isConflictContextAndModifierActive();
    }

    @Override
    public boolean isDown() {
        return this.isDown && (this.isConflictContextAndModifierActive() || needsToggle.getAsBoolean());
    }
}
