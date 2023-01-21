package xyz.bluspring.kilt.forgeinjects.world.level;

import com.google.common.collect.Lists;
import net.minecraft.world.level.DataPackConfig;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.bluspring.kilt.injections.DataPackConfigInjection;

import java.util.Collection;
import java.util.List;

@Mixin(DataPackConfig.class)
public class DataPackConfigInject implements DataPackConfigInjection {
    @Shadow @Final private List<String> enabled;

    @SuppressWarnings("InvalidInjectorMethodSignature")
    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableList;copyOf(Ljava/util/Collection;)Lcom/google/common/collect/ImmutableList;"))
    public List<String> kilt$makeEnabledPacksMutable(Collection<String> list) {
        return Lists.newArrayList(list);
    }

    @Override
    public void addModPacks(List<String> modPacks) {
        this.enabled.addAll(modPacks.stream().filter(p -> !this.enabled.contains(p)).toList());
    }
}
