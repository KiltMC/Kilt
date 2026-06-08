// TRACKED HASH: 66608c9c6edf5ebf79f290052253701c1e738d8e
package xyz.bluspring.kilt.injects.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.extensions.IKeyMappingExtension;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyMappingLookup;
import net.neoforged.neoforge.client.settings.KeyModifier;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;

import java.util.Map;
import java.util.Set;

@Mixin(KeyMapping.class)
public abstract class KeyMappingInject implements IKeyMappingExtension {
    @Shadow private InputConstants.Key key;
    @Shadow @Final private static Map<InputConstants.Key, KeyMapping> MAP;
    @Unique private static final KeyMappingLookup FORGE_MAP = new KeyMappingLookup();
    @Mutable @Shadow @Final private String name;
    @Mutable @Shadow @Final private InputConstants.Key defaultKey;
    @Mutable @Shadow @Final private String category;
    @Shadow @Final private static Map<String, KeyMapping> ALL;
    @Shadow @Final private static Set<String> CATEGORIES;
    @Shadow @Final private static Map<String, Integer> CATEGORY_SORT_ORDER;
    @Unique private KeyModifier keyModifierDefault = KeyModifier.NONE;
    @Unique private KeyModifier keyModifier = KeyModifier.NONE;
    @Unique private IKeyConflictContext keyConflictContext = KeyConflictContext.UNIVERSAL;

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
        FORGE_MAP.put(keyCode, (KeyMapping) (Object) this);
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

    @Inject(method = "same", at = @At("HEAD"), cancellable = true)
    private void kilt$addKeyConflictContextToSimilarityCheck(KeyMapping binding, CallbackInfoReturnable<Boolean> cir) {
        if (getKeyConflictContext().conflicts(binding.getKeyConflictContext()) || binding.getKeyConflictContext().conflicts(getKeyConflictContext())) {
            KeyModifier keyModifier = getKeyModifier();
            KeyModifier otherKeyModifier = binding.getKeyModifier();
            if (keyModifier.matches(binding.getKey()) || otherKeyModifier.matches(getKey())) {
                cir.setReturnValue(true);
            } else if (getKey().equals(binding.getKey())) {
                // IN_GAME key contexts have a conflict when at least one modifier is NONE.
                // For example: If you hold shift to crouch, you can still press E to open your inventory. This means that a Shift+E hotkey is in conflict with E.
                // GUI and other key contexts do not have this limitation.
                cir.setReturnValue(keyModifier == otherKeyModifier ||
                    (getKeyConflictContext().conflicts(KeyConflictContext.IN_GAME) &&
                        (keyModifier == KeyModifier.NONE || otherKeyModifier == KeyModifier.NONE)));
            }
        }
    }

    @Inject(method = "resetMapping", at = @At("HEAD"))
    private static void kilt$resetForgeMap(CallbackInfo ci) {
        FORGE_MAP.clear();
    }

    @Inject(method = "resetMapping", at = @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"))
    private static void kilt$registerMappingToForgeMap(CallbackInfo ci, @Local KeyMapping mapping) {
        FORGE_MAP.put(mapping.getKey(), mapping);
    }

    @WrapMethod(method = "getTranslatedKeyMessage")
    private Component kilt$addKeyModifierToMessage(Operation<Component> original) {
        return this.getKeyModifier().getCombinedName(this.key, original::call);
    }

    @ModifyReturnValue(method = "isDefault", at = @At("RETURN"))
    private boolean kilt$addModifierToDefaultCheck(boolean original) {
        return original && this.getKeyModifier() == this.getDefaultKeyModifier();
    }

    @ModifyReturnValue(method = "isDown", at = @At("RETURN"))
    private boolean kilt$addConflictContextAndModifierToDownCheck(boolean original) {
        return original && this.isConflictContextAndModifierActive();
    }

    @WrapMethod(method = "click")
    private static void kilt$wrapKeyClick(InputConstants.Key key, Operation<Void> original, @Share("currentMap") LocalRef<KeyMapping> currentMap) {
        var keys = FORGE_MAP.getAll(key);
        for (KeyMapping keyMapping : keys) {
            currentMap.set(keyMapping);
            original.call(key);
        }
        if (FORGE_MAP.kilt$isEmpty()) {
            original.call(key);
        }
    }

    @WrapOperation(method = "click", at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;"))
    private static <K, V> V kilt$useForgeMappingIfPossibleForClick(Map<K, V> instance, K o, Operation<V> original, @Share("currentMap") LocalRef<KeyMapping> currentMap) {
        if (currentMap.get() != null) {
            return (V) currentMap.get();
        }

        //noinspection MixinExtrasOperationParameters
        return original.call(instance, o);
    }

    @WrapMethod(method = "set")
    private static void kilt$wrapKeySet(InputConstants.Key key, boolean held, Operation<Void> original, @Share("currentMap") LocalRef<KeyMapping> currentMap) {
        var keys = FORGE_MAP.getAll(key);
        for (KeyMapping keyMapping : keys) {
            currentMap.set(keyMapping);
            original.call(key, held);
        }
        if (FORGE_MAP.kilt$isEmpty()) {
            original.call(key, held);
        }
    }

    @WrapOperation(method = "set", at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;"))
    private static <K, V> V kilt$useForgeMappingIfPossible(Map<K, V> instance, K o, Operation<V> original, @Share("currentMap") LocalRef<KeyMapping> currentMap) {
        if (currentMap.get() != null) {
            return (V) currentMap.get();
        }

        //noinspection MixinExtrasOperationParameters
        return original.call(instance, o);
    }

    // Kilt-exclusive injects
    @Inject(method = "<init>(Ljava/lang/String;Lcom/mojang/blaze3d/platform/InputConstants$Type;ILjava/lang/String;)V", at = @At("TAIL"))
    private void kilt$addKeyValueInForgeMap(String name, InputConstants.Type type, int keyCode, String category, CallbackInfo ci) {
        FORGE_MAP.put(this.key, (KeyMapping) (Object) this);
    }

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
        if (keyConflictContext == null)
            keyConflictContext = KeyConflictContext.UNIVERSAL;

        return keyConflictContext;
    }

    @Override
    public KeyModifier getDefaultKeyModifier() {
        if (keyModifierDefault == null)
            keyModifierDefault = KeyModifier.NONE;

        return this.keyModifierDefault;
    }

    @Override
    public KeyModifier getKeyModifier() {
        if (keyModifier == null)
            keyModifier = KeyModifier.NONE;

        return keyModifier;
    }

    @Override
    public void setKeyModifierAndCode(KeyModifier keyModifier, InputConstants.Key keyCode) {
        MAP.remove(this.key);
        FORGE_MAP.remove((KeyMapping) (Object) this);

        if (keyModifier == null || keyModifier.matches(keyCode))
            keyModifier = KeyModifier.NONE;

        this.key = keyCode;
        this.keyModifier = keyModifier;

        MAP.put(keyCode, (KeyMapping) (Object) this);
        FORGE_MAP.put(keyCode, (KeyMapping) (Object) this);
    }
}
