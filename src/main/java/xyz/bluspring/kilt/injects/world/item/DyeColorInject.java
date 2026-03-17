package xyz.bluspring.kilt.injects.world.item;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.tags.ItemTagsInjection;
import xyz.bluspring.kilt.injections.world.item.DyeColorInjection;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.MapColor;

@Mixin(DyeColor.class)
public abstract class DyeColorInject implements DyeColorInjection {
    @Unique private TagKey<Item> dyesTag;
    @Unique private TagKey<Item> dyedTag;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void kilt$createTagsForDyeName(String string, int i, int id, String name, int textureDefuseColor, MapColor mapColor, int fireworkColor, int textColor, CallbackInfo ci) {
        this.dyesTag = ItemTagsInjection.create(ResourceLocation.fromNamespaceAndPath("c", "dyes/" + name));
        this.dyedTag = ItemTagsInjection.create(ResourceLocation.fromNamespaceAndPath("c", "dyed/" + name));
    }

    @Override
    public TagKey<Item> getTag() {
        return dyesTag;
    }

    @Override
    public TagKey<Item> getDyedTag() {
        return dyedTag;
    }

    @CreateStatic
    private static DyeColor getColor(ItemStack stack) {
        return DyeColorInjection.getColor(stack);
    }
}
