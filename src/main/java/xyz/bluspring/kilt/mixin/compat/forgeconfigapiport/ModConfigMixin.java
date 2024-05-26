package xyz.bluspring.kilt.mixin.compat.forgeconfigapiport;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.config.IConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.Kilt;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;

@Mixin(value = ModConfig.class, remap = false)
public class ModConfigMixin {
    @Shadow private CommentedConfig configData;
    @Shadow @Final private String modId;
    @Shadow @Final private String fileName;

    public ModConfigMixin(final ModConfig.Type type, final IConfigSpec<?> spec, String modId, final String fileName) {}

    @CreateInitializer
    public ModConfigMixin(final ModConfig.Type type, final IConfigSpec<?> spec, ModContainer mod, final String fileName) {
        this(type, spec, mod.getModId(), fileName);
    }

    @Inject(method = "save", at = @At("HEAD"), cancellable = true)
    private void kilt$checkTypeOfConfig(CallbackInfo ci) {
        // TODO: figure out what causes this
        if (!(this.configData instanceof CommentedFileConfig)) {
            Kilt.Companion.getLogger().error("Mod ID {} in file {} wasn't detected as a file config! We're cancelling the save method here so the game doesn't crash.", this.modId, this.fileName);
            ci.cancel();
        }
    }
}
