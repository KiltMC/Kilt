// TRACKED HASH: 66608c9c6edf5ebf79f290052253701c1e738d8e
package xyz.bluspring.kilt.forgeinjects.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.resources.language.I18n;
import net.minecraftforge.client.extensions.IForgeKeyMapping;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.*;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;

import java.util.Map;
import java.util.Set;

@Mixin(KeyMapping.class)
public abstract class KeyMappingInject implements IForgeKeyMapping {
    @Shadow private InputConstants.Key key;
    @Shadow @Final private static Map<InputConstants.Key, KeyMapping> MAP;
    @Mutable
    @Shadow @Final private String name;
    @Mutable
    @Shadow @Final private InputConstants.Key defaultKey;
    @Mutable
    @Shadow @Final private String category;
    @Shadow @Final private static Map<String, KeyMapping> ALL;
    @Shadow @Final private static Set<String> CATEGORIES;
    @Shadow @Final private static Map<String, Integer> CATEGORY_SORT_ORDER;
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

    @CreateInitializer
    public KeyMappingInject(String description, IKeyConflictContext keyConflictContext, final InputConstants.Type inputType, final int keyCode, String category) {
        this(description, keyConflictContext, inputType.getOrCreate(keyCode), category);
    }

    @CreateInitializer
    public KeyMappingInject(String description, IKeyConflictContext keyConflictContext, InputConstants.Key keyCode, String category) {
        this(description, keyConflictContext, KeyModifier.NONE, keyCode, category);
    }

    @CreateInitializer
    public KeyMappingInject(String description, IKeyConflictContext keyConflictContext, KeyModifier keyModifier, final InputConstants.Type inputType, final int keyCode, String category) {
        this(description, keyConflictContext, keyModifier, inputType.getOrCreate(keyCode), category);
    }

    @CreateInitializer
    public KeyMappingInject(String description, IKeyConflictContext keyConflictContext, KeyModifier keyModifier, InputConstants.Key keyCode, String category) {
        this.name = description;
        this.key = keyCode;
        this.defaultKey = keyCode;
        this.category = category;
        this.keyConflictContext = keyConflictContext;
        this.keyModifier = keyModifier;
        this.keyModifierDefault = keyModifier;
        if (this.keyModifier.matches(keyCode))
            this.keyModifier = KeyModifier.NONE;

        ALL.put(description, (KeyMapping) (Object) this);
        MAP.put(keyCode, (KeyMapping) (Object) this);
        CATEGORIES.add(category);
    }

    /**
     * @author BluSpring, MinecraftForge, NeoForge
     * @reason the game will crash on opening the screen otherwise.
     */
    @Overwrite
    public int compareTo(KeyMapping mapping) {
        if (this.category.equals(mapping.getCategory())) return I18n.get(this.name).compareTo(I18n.get(mapping.getName()));
        Integer tCat = CATEGORY_SORT_ORDER.get(this.category);
        Integer oCat = CATEGORY_SORT_ORDER.get(mapping.getCategory());
        if (tCat == null && oCat != null) return 1;
        if (tCat != null && oCat == null) return -1;
        if (tCat == null && oCat == null) return I18n.get(this.category).compareTo(I18n.get(mapping.getCategory()));
        return tCat.compareTo(oCat);
    }
}