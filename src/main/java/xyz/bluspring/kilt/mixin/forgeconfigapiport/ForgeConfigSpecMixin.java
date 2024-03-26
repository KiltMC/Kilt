package xyz.bluspring.kilt.mixin.forgeconfigapiport;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import net.minecraftforge.common.ForgeConfigSpec;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

@Mixin(value = ForgeConfigSpec.Builder.class, remap = false)
public abstract class ForgeConfigSpecMixin {
    @Shadow public abstract <T> ForgeConfigSpec.ConfigValue<List<? extends T>> defineListAllowEmpty(List<String> path, Supplier<List<? extends T>> defaultSupplier, Predicate<Object> elementValidator);

    public <T> ForgeConfigSpec.ConfigValue<List<? extends T>> defineListAllowEmpty(String path, List<? extends T> defaultValue, Predicate<Object> elementValidator) {
        return defineListAllowEmpty(split(path), defaultValue, elementValidator);
    }
    public <T> ForgeConfigSpec.ConfigValue<List<? extends T>> defineListAllowEmpty(String path, Supplier<List<? extends T>> defaultSupplier, Predicate<Object> elementValidator) {
        return defineListAllowEmpty(split(path), defaultSupplier, elementValidator);
    }
    public <T> ForgeConfigSpec.ConfigValue<List<? extends T>> defineListAllowEmpty(List<String> path, List<? extends T> defaultValue, Predicate<Object> elementValidator) {
        return defineListAllowEmpty(path, () -> defaultValue, elementValidator);
    }

    @Unique private static final Joiner LINE_JOINER = Joiner.on("\n");
    @Unique private static final Joiner DOT_JOINER = Joiner.on(".");
    @Unique private static final Splitter DOT_SPLITTER = Splitter.on(".");

    @Unique
    private static List<String> split(String path)
    {
        return Lists.newArrayList(DOT_SPLITTER.split(path));
    }
}
