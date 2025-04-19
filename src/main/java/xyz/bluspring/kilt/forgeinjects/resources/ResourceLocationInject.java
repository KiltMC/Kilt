// TRACKED HASH: ec6c3c6702bb8f7588d211003582f0637ede7a14
package xyz.bluspring.kilt.forgeinjects.resources;

import io.github.fabricators_of_create.porting_lib.extensions.extensions.ResourceLocationExtensions;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.resources.ResourceLocationInjection;

import static net.minecraft.resources.ResourceLocation.isValidNamespace;
import static net.minecraft.resources.ResourceLocation.isValidPath;

@Mixin(ResourceLocation.class)
public abstract class ResourceLocationInject implements ResourceLocationInjection, ResourceLocationExtensions {
    @Shadow @Final public static String DEFAULT_NAMESPACE;

    @Shadow public static ResourceLocation of(String location, char separator) {
        throw new IllegalStateException();
    }

    @Override
    public int compareNamespaced(ResourceLocation o) {
        return ResourceLocationInjection.super.compareNamespaced(o);
    }

    @CreateStatic
    private static ResourceLocation fromNamespaceAndPath(String namespace, String path) {
        return new ResourceLocation(namespace, path);
    }

    @CreateStatic
    private static ResourceLocation parse(String location) {
        return new ResourceLocation(location);
    }

    @CreateStatic
    private static ResourceLocation withDefaultNamespace(String path) {
        return new ResourceLocation(DEFAULT_NAMESPACE, path);
    }

    @CreateStatic
    private static ResourceLocation bySeparator(String location, char separator) {
        return of(location, separator);
    }

    @CreateStatic
    private static @Nullable ResourceLocation tryBySeparator(String location, char separator) {
        int i = location.indexOf(separator);
        if (i >= 0) {
            String s = location.substring(i + 1);
            if (!isValidPath(s)) {
                return null;
            } else if (i != 0) {
                String s1 = location.substring(0, i);
                return isValidNamespace(s1) ? new ResourceLocation(s1, s) : null;
            } else {
                return new ResourceLocation(DEFAULT_NAMESPACE, s);
            }
        } else {
            return isValidPath(location) ? new ResourceLocation(DEFAULT_NAMESPACE, location) : null;
        }
    }
}