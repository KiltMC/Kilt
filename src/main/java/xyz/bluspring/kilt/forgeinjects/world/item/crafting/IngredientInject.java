package xyz.bluspring.kilt.forgeinjects.world.item.crafting;

import com.google.gson.JsonElement;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.crafting.CraftingHelper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.item.crafting.IngredientInjection;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@Mixin(Ingredient.class)
public class IngredientInject implements IngredientInjection {
    private static final AtomicInteger INVALIDATION_COUNTER = IngredientInjection.INVALIDATION_COUNTER;

    @CreateStatic
    private static void invalidateAll() {
        IngredientInjection.invalidateAll();
    }

    @CreateStatic
    private static Ingredient merge(Collection<Ingredient> parts) {
        return IngredientInjection.merge(parts);
    }

    @Shadow @Nullable public ItemStack[] itemStacks;
    @Shadow @Nullable public IntList stackingIds;
    private int invalidationCounter;

    @Override
    public boolean checkInvalidation() {
        var currentInvalidationCounter = IngredientInjection.INVALIDATION_COUNTER.get();
        if (this.invalidationCounter != currentInvalidationCounter) {
            invalidate();
            return true;
        }

        return false;
    }

    @Override
    public void markValid() {
        invalidationCounter = INVALIDATION_COUNTER.get();
    }

    @Override
    public void invalidate() {
        this.itemStacks = null;
        this.stackingIds = null;
    }

    @Override
    public boolean isVanilla() {
        return this.getClass().getPackageName().startsWith("net.minecraft");
    }

    @Inject(at = @At("HEAD"), method = "fromNetwork", cancellable = true)
    private static void kilt$checkForgeRecipeFromNetwork(FriendlyByteBuf friendlyByteBuf, CallbackInfoReturnable<Ingredient> cir) {
        var size = friendlyByteBuf.readVarInt();
        if (size == -1) {
            cir.setReturnValue(CraftingHelper.getIngredient(friendlyByteBuf.readResourceLocation(), friendlyByteBuf));
            return;
        }

        cir.setReturnValue(Ingredient.fromValues(Stream.generate(() -> new Ingredient.ItemValue(friendlyByteBuf.readItem())).limit(size)));
    }

    @Inject(at = @At(value = "INVOKE", target = "Lcom/google/gson/JsonElement;isJsonObject()Z", shift = At.Shift.BEFORE), method = "fromJson", cancellable = true)
    private static void kilt$checkForgeRecipeFromJson(JsonElement jsonElement, CallbackInfoReturnable<Ingredient> cir) {
        var ret = CraftingHelper.getIngredient(jsonElement);
        if (ret != null)
            cir.setReturnValue(ret);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/crafting/Ingredient;dissolve()V", shift = At.Shift.AFTER), method = "toNetwork", cancellable = true)
    public void kilt$writeNonVanillaIds(FriendlyByteBuf friendlyByteBuf, CallbackInfo ci) {
        if (!this.isVanilla()) {
            CraftingHelper.write(friendlyByteBuf, (Ingredient) (Object) this);
            ci.cancel();
        }
    }

    @Mixin(Ingredient.TagValue.class)
    public static class TagValueInject {
        @Shadow @Final public TagKey<Item> tag;

        @Inject(at = @At("RETURN"), method = "getItems")
        public void kilt$addEmptyTag(CallbackInfoReturnable<Collection<ItemStack>> cir) {
            var list = cir.getReturnValue();

            if (list.size() == 0) {
                list.add(new ItemStack(Blocks.BARRIER).setHoverName(Component.literal("Empty Tag: " + this.tag.location())));
            }
        }
    }
}
