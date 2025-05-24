package xyz.bluspring.kilt.mixin.compat.forge.tacz;

import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

@Pseudo
@Mixin(targets = "com.tacz.guns.util.GetJarResources", remap = false)
public abstract class GetJarResourcesMixin {
    @Redirect(method = "lambda$copyFolder$0", at = @At(value = "INVOKE", target = "Ljava/nio/file/Path;resolve(Ljava/lang/String;)Ljava/nio/file/Path;"))
    private static Path kilt$useProperPathRelativize(Path targetPath, String other, @Local(ordinal = 1, argsOnly = true) Path source, @Local(ordinal = 0, argsOnly = true) URI sourceURI) {
        return targetPath.resolve(Paths.get(sourceURI).relativize(source).toString());
    }
}
