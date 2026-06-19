package xyz.bluspring.kilt.compat.forge.mixin.kubejs;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import net.neoforged.neoforgespi.locating.IModFile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.nio.file.Path;
import java.util.ArrayList;

@IfModLoaded("kubejs")
@Pseudo
@Mixin(targets = "dev.latvian.mods.kubejs.KubeJS", remap = false)
public abstract class KubeJSMixin {
    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Ljava/util/ArrayList;remove(Ljava/lang/Object;)Z"))
    public boolean properlyRemoveKubejs(ArrayList<IModFile> instance, Object thisModFile) {
        Path ourPath = ((IModFile) thisModFile).getFilePath();
        return instance.removeIf(modFile -> modFile.getFilePath().equals(ourPath));
    }
}
