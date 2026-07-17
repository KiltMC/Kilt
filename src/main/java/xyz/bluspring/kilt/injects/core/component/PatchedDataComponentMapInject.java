package xyz.bluspring.kilt.injects.core.component;

import java.util.Optional;

import com.google.common.math.IntMath;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMaps;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.injections.core.component.PatchedDataComponentMapInjection;
import xyz.bluspring.kilt.mixin.DataComponentPatchAccessor;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.PatchedDataComponentMap;

@Mixin(PatchedDataComponentMap.class)
public abstract class PatchedDataComponentMapInject implements PatchedDataComponentMapInjection {
    @Shadow private Reference2ObjectMap<DataComponentType<?>, Optional<?>> patch;

    @Override
    public boolean isPatchEmpty() {
        return this.patch.isEmpty();
    }

    @Override
    public boolean patchEquals(DataComponentPatch patch) {
        return this.patch.equals(((DataComponentPatchAccessor) (Object) patch).kilt$getMap());
    }

    // Neo: Change implementation of hashCode to reduce collisions.
    // For a map, hashCode is specified as the sum of the hash codes of its entries.
    // We do that, but change the entry hash code to 8191^<key hash> * <value hash>,
    // where <key hash> is the lower bits of the identity hash code of the key.
    @WrapOperation(method = "hashCode", at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/objects/Reference2ObjectMap;hashCode()I"))
    private int kilt$useModifiedHashCodeImplementation(Reference2ObjectMap<DataComponentType<?>, Optional<?>> instance, Operation<Integer> original) { // Kilt: this is normally static, we're not doing that. if someone needs that, we can do it.
        int h = 0, n = instance.size();
        var iterator = Reference2ObjectMaps.fastIterator(instance);

        while (n-- != 0) {
            var entry = iterator.next();
            int exponent = System.identityHashCode(entry.getKey()) & 0xff;
            // Use 8191 instead of the usual 31, as 31 can produce many collisions with typical integer component ranges (0-255) if the exponent difference is only 1
            int entryHash = IntMath.pow(8191, exponent) * entry.getValue().hashCode();
            h += entryHash;
        }

        return h;
    }
}
