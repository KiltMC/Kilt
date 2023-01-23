package xyz.bluspring.kilt.forgeinjects.data;

import net.minecraft.WorldVersion;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import org.apache.commons.compress.utils.Lists;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.injections.data.DataGeneratorInjection;
import xyz.bluspring.kilt.mixin.DataGeneratorAccessor;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

@Mixin(DataGenerator.class)
public class DataGeneratorInject implements DataGeneratorInjection {
    @Shadow @Final private Collection<Path> inputFolders;

    @Shadow @Final private List<DataProvider> allProviders;

    @Redirect(at = @At(value = "FIELD", target = "Lnet/minecraft/data/DataGenerator;inputFolders:Ljava/util/Collection;", opcode = Opcodes.PUTFIELD), method = "<init>")
    public void kilt$makeInputsMutable(DataGenerator instance, Collection<Path> value) {
        ((DataGeneratorAccessor) instance).setInputFolders(Lists.newArrayList(value.iterator()));
    }

    @Override
    public List<DataProvider> getProviders() {
        return this.allProviders;
    }

    @Override
    public void addInput(Path value) {
        inputFolders.add(value);
    }
}
