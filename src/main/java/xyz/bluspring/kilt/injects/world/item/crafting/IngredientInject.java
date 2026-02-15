// TRACKED HASH: 297d980725ef2ec87d9bfaf04e6eab59c792fccd
package xyz.bluspring.kilt.injects.world.item.crafting;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.world.item.crafting.IngredientInjection;
import xyz.bluspring.kilt.util.KiltHelper;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@Implements(@Interface(iface = IngredientInjection.class, prefix = "kilt$i$"))
@Mixin(Ingredient.class)
public abstract class IngredientInject implements IngredientInjection {
    // Kilt: StreamCodec handling in CustomIngredientPacketCodecMixin

    @Shadow public abstract ItemStack[] getItems();
    @Shadow @Final private Ingredient.Value[] values;

    @CreateStatic
    private static final MapCodec<Ingredient> MAP_CODEC_NONEMPTY = IngredientInjection.MAP_CODEC_NONEMPTY;

    @CreateStatic
    private static final Codec<List<Ingredient>> LIST_CODEC = IngredientInjection.LIST_CODEC;

    @CreateStatic
    private static final Codec<List<Ingredient>> LIST_CODEC_NONEMPTY = IngredientInjection.LIST_CODEC_NONEMPTY;

    public IngredientInject(Ingredient.Value[] values) {}

    @Unique @Nullable
    private ICustomIngredient customIngredient;

    @CreateInitializer
    public IngredientInject(ICustomIngredient customIngredient) {
        this(new Ingredient.Value[0]);
        this.customIngredient = customIngredient;
    }

    @ModifyExpressionValue(method = "getItems", at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;flatMap(Ljava/util/function/Function;)Ljava/util/stream/Stream;"))
    private <R> Stream<R> kilt$useNeoCustomIngredientItems(Stream<R> original) {
        if (this.customIngredient != null) {
            return (Stream<R>) this.customIngredient.getItems();
        }

        return original;
    }

    @Inject(method = "test(Lnet/minecraft/world/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
    private void kilt$testNeoCustomIngredient(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (stack != null && this.customIngredient != null)
            cir.setReturnValue(this.customIngredient.test(stack));
    }

    @ModifyReturnValue(method = "isEmpty", at = @At("RETURN"))
    private boolean kilt$checkIsCustom(boolean original) {
        return original && !isCustom();
    }

    @Override
    public boolean hasNoItems() {
        ItemStack[] items = this.getItems();
        if (items.length == 0)
            return true;

        if (items.length == 1) {
            ItemStack item = items[0];
            // Kilt: what? why is it actually a barrier in the list???
            return item.getItem() == Items.BARRIER
                && item.getHoverName() instanceof MutableComponent hoverName
                && hoverName.getString().startsWith("Empty Tag: ");
        }

        return false;
    }

    // TODO: make this more mod-compatible
    @Override
    public int hashCode() {
        if (this.customIngredient != null)
            return this.customIngredient.hashCode();

        return Arrays.hashCode(this.values);
    }

    // FIXME: this probably breaks with an accessor, watch over this!
    @Override
    public Ingredient.Value[] getValues() {
        if (this.isCustom()) {
            throw new IllegalStateException("Cannot retrieve values from custom ingredient!");
        }

        return this.values;
    }

    @Override
    public boolean isSimple() {
        return this.customIngredient == null || this.customIngredient.isSimple();
    }

    @Override
    public ICustomIngredient neoforge$getCustomIngredient() {
        if (KiltHelper.INSTANCE.hasMethodOverrideWithReturnType(this.getClass(), Ingredient.class, "getCustomIngredient", ICustomIngredient.class)) {
            return this.getCustomIngredient();
        }

        return this.customIngredient;
    }

    public ICustomIngredient kilt$i$getCustomIngredient() {
        return this.neoforge$getCustomIngredient();
    }

    @Override
    public void kilt$setCustomIngredient(ICustomIngredient customIngredient) {
        this.customIngredient = customIngredient;
    }

    @Override
    public boolean isCustom() {
        return this.customIngredient != null;
    }

    @Mixin(Ingredient.ItemValue.class)
    public abstract static class ItemValueInject {

    }

    @Mixin(Ingredient.TagValue.class)
    public abstract static class TagValueInject {

    }

    @Mixin(Ingredient.Value.class)
    public interface ValueInject {

    }
}