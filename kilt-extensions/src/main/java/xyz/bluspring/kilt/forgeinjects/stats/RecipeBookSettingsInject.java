package xyz.bluspring.kilt.forgeinjects.stats;

import com.google.common.collect.ImmutableMap;
import net.minecraft.stats.RecipeBookSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.HashMap;
import java.util.Map;

@Mixin(RecipeBookSettings.class)
public class RecipeBookSettingsInject {
    // because ImmutableMap is a Map, we don't need to worry too much about this.
    // though, if there's a better way of doing this, please tell me.
    @SuppressWarnings("InvalidInjectorMethodSignature")
    @Redirect(at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableMap;of(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Lcom/google/common/collect/ImmutableMap;"), method = "<clinit>")
    private static <K, V> Map<K, V> kilt$replaceFieldsTagWithMutableMap(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
        return new HashMap<>(ImmutableMap.of(k1, v1, k2, v2, k3, v3, k4, v4));
    }
}
