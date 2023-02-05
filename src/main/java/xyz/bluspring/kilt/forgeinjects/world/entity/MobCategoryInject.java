package xyz.bluspring.kilt.forgeinjects.world.entity;

import com.mojang.serialization.Codec;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.common.IExtensibleEnum;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.injections.entity.MobCategoryInjection;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Mixin(MobCategory.class)
public abstract class MobCategoryInject implements MobCategoryInjection, IExtensibleEnum {
    @Shadow @Final @Mutable
    public static Codec<MobCategory> CODEC;

    @Shadow public abstract String getName();

    @Inject(at = @At("TAIL"), method = "<clinit>")
    private static void kilt$replaceCodec(CallbackInfo ci) {
        CODEC = IExtensibleEnum.createCodecForExtensibleEnum(MobCategory::values, MobCategoryInjection::byName);

        var values = Arrays.stream(MobCategory.values()).collect(Collectors.toMap(MobCategory::getName, mobCategory -> mobCategory));
        MobCategoryInjection.BY_NAME.putAll(values);
    }

    @Override
    public void init() {
        BY_NAME.put(this.getName(), (MobCategory) (Object) this);
    }
}
