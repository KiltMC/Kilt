package xyz.bluspring.kilt.forgeinjects.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.extensions.IForgeKeyMapping;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(KeyMapping.class)
public abstract class KeyMappingInject implements IForgeKeyMapping {
    @Shadow private InputConstants.Key key;
    @Shadow @Final private static Map<InputConstants.Key, KeyMapping> MAP;
    private KeyModifier keyModifierDefault = KeyModifier.NONE;
    private KeyModifier keyModifier = KeyModifier.NONE;
    private IKeyConflictContext keyConflictContext = KeyConflictContext.UNIVERSAL;

    @NotNull
    @Override
    public InputConstants.Key getKey() {
        return this.key;
    }

    @Override
    public void setKeyConflictContext(IKeyConflictContext keyConflictContext) {
        this.keyConflictContext = keyConflictContext;
    }

    @Override
    public IKeyConflictContext getKeyConflictContext() {
        return keyConflictContext;
    }

    @Override
    public KeyModifier getDefaultKeyModifier() {
        return this.keyModifierDefault;
    }

    @Override
    public KeyModifier getKeyModifier() {
        return keyModifier;
    }

    @Override
    public void setKeyModifierAndCode(KeyModifier keyModifier, InputConstants.Key keyCode) {
        MAP.remove(this.key);
        this.key = keyCode;
        if (keyModifier.matches(keyCode))
            keyModifier = KeyModifier.NONE;

        this.keyModifier = keyModifier;
        MAP.put(keyCode, (KeyMapping) (Object) this);
    }
}
