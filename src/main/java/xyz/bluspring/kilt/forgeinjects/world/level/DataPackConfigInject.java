package xyz.bluspring.kilt.forgeinjects.world.level;

import com.google.common.collect.Lists;
import net.minecraft.world.level.DataPackConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.injections.DataPackConfigInjection;

import java.util.List;

@Mixin(DataPackConfig.class)
public class DataPackConfigInject implements DataPackConfigInjection {
    @Shadow
    public List<String> enabled;

    @Inject(method = "<init>", at = @At("TAIL"))
    public void kilt$makeEnabledPacksMutable(List<String> list, List<String> list2, CallbackInfo ci) {
        this.enabled = Lists.newArrayList(list);
    }

    @Override
    public void addModPacks(List<String> modPacks) {
        this.enabled.addAll(modPacks.stream().filter(p -> !this.enabled.contains(p)).toList());
    }
}
