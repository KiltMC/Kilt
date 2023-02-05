package xyz.bluspring.kilt.forgeinjects.world.level.biome;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraftforge.common.IExtensibleEnum;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.injections.world.biome.BiomeSpecialEffectsGrassColorModifierInjection;

import java.util.Arrays;
import java.util.stream.Collectors;

@Mixin(BiomeSpecialEffects.GrassColorModifier.class)
// such a long line for just a class, bruh
public abstract class BiomeSpecialEffectsGrassColorModifierInject implements BiomeSpecialEffectsGrassColorModifierInjection, IExtensibleEnum {
    @Shadow public abstract String getName();

    @Shadow @Final @Mutable
    public static Codec<BiomeSpecialEffects.GrassColorModifier> CODEC;
    private ColorModifier delegate;

    @Override
    public void setDelegate(ColorModifier delegate) {
        this.delegate = delegate;
    }

    @Override
    public void init() {
        BY_NAME.put(this.getName(), (BiomeSpecialEffects.GrassColorModifier) (Object) this);
    }

    @Inject(at = @At("TAIL"), method = "<clinit>")
    private static void kilt$changeCodec(CallbackInfo ci) {
        CODEC = IExtensibleEnum.createCodecForExtensibleEnum(BiomeSpecialEffects.GrassColorModifier::values, BiomeSpecialEffectsGrassColorModifierInjection::byName);

        // add all these when the time comes
        var values = Arrays.stream(BiomeSpecialEffects.GrassColorModifier.values()).collect(Collectors.toMap(BiomeSpecialEffects.GrassColorModifier::getName, grassColorModifier -> grassColorModifier));
        BiomeSpecialEffectsGrassColorModifierInjection.BY_NAME.putAll(values);
    }
}
