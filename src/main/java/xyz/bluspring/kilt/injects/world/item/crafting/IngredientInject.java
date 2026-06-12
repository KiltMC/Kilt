// TRACKED HASH: 297d980725ef2ec87d9bfaf04e6eab59c792fccd
package xyz.bluspring.kilt.injects.world.item.crafting;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.neoforged.neoforge.common.crafting.CraftingHelper;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.world.item.crafting.IngredientInjection;
import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackLinkedSet;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

@Implements(@Interface(iface = IngredientInjection.class, prefix = "kilt$i$"))
@Mixin(Ingredient.class)
public abstract class IngredientInject implements IngredientInjection {
    // Kilt: StreamCodec handling in CustomIngredientPacketCodecMixin

    @Shadow public abstract ItemStack[] getItems();
    @Shadow @Final private Ingredient.Value[] values;

    @Shadow @Final @Mutable public static Codec<Ingredient> CODEC;
    @Shadow @Final @Mutable public static Codec<Ingredient> CODEC_NONEMPTY;
    @Shadow @Final @Mutable public static StreamCodec<RegistryFriendlyByteBuf, Ingredient> CONTENTS_STREAM_CODEC;

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

    @ModifyReceiver(method = "getItems", at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;toArray(Ljava/util/function/IntFunction;)[Ljava/lang/Object;"))
    private <A extends ItemStack> Stream<A> kilt$properlyDistinctifyStacks(Stream<A> instance, IntFunction<A[]> intFunction) {
        return (Stream<A>) instance.collect(Collectors.toCollection(ItemStackLinkedSet::createTypeAndComponentsSet)).stream();
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

    @WrapOperation(method = "equals", at = @At(value = "INVOKE", target = "Ljava/util/Arrays;equals([Ljava/lang/Object;[Ljava/lang/Object;)Z"))
    private boolean kilt$checkIsCustomIngredientEqual(Object[] a, Object[] a2, Operation<Boolean> original, @Local Ingredient ingredient) {
        return Objects.equals(this.customIngredient, ingredient.neoforge$getCustomIngredient()) && original.call(a, a2);
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

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void kilt$initNeoIngredientCodecs(CallbackInfo ci) {
        // FIXME: I don't know what the fuck is happening, but using this with Supplementaries results in any boat + a chest = an acacia boat with cannon????
        CODEC = Codec.withAlternative(CraftingHelper.kilt$makeIngredientCodec(CODEC.listOf()), CODEC);
        CODEC_NONEMPTY = Codec.withAlternative(CraftingHelper.kilt$makeIngredientCodec(CODEC_NONEMPTY.listOf()), CODEC_NONEMPTY);

        StreamCodec<RegistryFriendlyByteBuf, Ingredient> original = CONTENTS_STREAM_CODEC;
        StreamCodec<RegistryFriendlyByteBuf, ICustomIngredient> customIngredientCodec = ByteBufCodecs.registry(NeoForgeRegistries.Keys.INGREDIENT_TYPES)
            .dispatch(ICustomIngredient::getType, IngredientType::streamCodec);

        CONTENTS_STREAM_CODEC = StreamCodec.of((buf, value) -> {
            if (value.isSimple()) {
                original.encode(buf, value);
            } else {
                buf.writeVarInt(-1);
                customIngredientCodec.encode(buf, value.neoforge$getCustomIngredient());
            }
        }, (buf) -> {
            buf.markReaderIndex();
            var size = buf.readVarInt();
            if (size == -1) {
                try {
                    return IngredientInjection.create(customIngredientCodec.decode(buf));
                } catch (Throwable ignored) {
                }
            }

            buf.resetReaderIndex();
            return original.decode(buf);
        });
    }

    @Mixin(Ingredient.ItemValue.class)
    public abstract static class ItemValueInject {
        @Shadow
        public abstract ItemStack item();

        @Override
        public int hashCode() {
            return 31 * this.item().getItem().hashCode() + this.item().getCount();
        }
    }

    @Mixin(Ingredient.TagValue.class)
    public abstract static class TagValueInject {

    }

    @Mixin(Ingredient.Value.class)
    public interface ValueInject {

    }
}
