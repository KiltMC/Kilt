package xyz.bluspring.kilt.injects.client.gui.screens.worldselection;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.neoforged.neoforge.client.PresetEditorManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;
import java.util.Optional;

@Mixin(WorldCreationUiState.class)
public abstract class WorldCreationUiStateInject {
    @WrapOperation(method = "getPresetEditor", at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;"))
    private <K, V> V kilt$getForgePresetEditor(Map<K, V> instance, Object o, Operation<V> original) {
        var existing = original.call(instance, o);

        if (existing != null)
            return existing;

        return (V) ((Optional<ResourceKey<WorldPreset>>) o).map(PresetEditorManager::get).orElse(null);
    }
}
