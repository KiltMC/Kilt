package xyz.bluspring.kilt.compat.fabric.mixin.spark;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import kotlin.collections.CollectionsKt;
import me.lucko.spark.fabric.FabricClassSourceLookup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

import net.fabricmc.loader.api.ModContainer;

@Pseudo
@IfModLoaded("spark")
@Mixin(FabricClassSourceLookup.class)
public abstract class FabricClassSourceLookupMixin {
    @ModifyExpressionValue(method = "constructPathToModIdMap", at = @At(value = "INVOKE", target = "Lnet/fabricmc/loader/api/ModContainer;getRootPaths()Ljava/util/List;"))
    private static List<Path> kilt$spark$filterOutDuplicatePaths(List<Path> original, @Share("knownPaths") LocalRef<Set<Path>> knownPathsRef, @Local(name = "mod") ModContainer container) {
        if (knownPathsRef.get() == null)
            knownPathsRef.set(new HashSet<>());

        var knownPaths = knownPathsRef.get();
        return CollectionsKt.filter(original, knownPaths::add);
    }
}
