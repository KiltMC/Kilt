package xyz.bluspring.kilt.injects.world.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.HorseArmorItem;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;

@Mixin(HorseArmorItem.class)
public abstract class HorseArmorItemInject extends Item {
    @Unique private ResourceLocation kilt$texture;

    public HorseArmorItemInject(int protection, String identifier, Item.Properties properties) {
        super(properties);
    }

    @CreateInitializer
    public HorseArmorItemInject(int protection, ResourceLocation identifier, Item.Properties properties) {
        this(protection, identifier.toString(), properties);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void kilt$useNamespacedTexture(int protection, String identifier, Properties properties, CallbackInfo ci) {
        if (identifier.contains(":"))
            this.kilt$texture = new ResourceLocation(identifier);
    }

    @Inject(method = "getTexture", at = @At("HEAD"), cancellable = true)
    private void kilt$tryUseCustomTexture(CallbackInfoReturnable<ResourceLocation> cir) {
        if (this.kilt$texture != null) {
            cir.setReturnValue(this.kilt$texture);
        }
    }
}
