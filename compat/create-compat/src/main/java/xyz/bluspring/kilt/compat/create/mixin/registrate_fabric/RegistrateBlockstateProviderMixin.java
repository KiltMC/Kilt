package xyz.bluspring.kilt.compat.create.mixin.registrate_fabric;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.*;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.workarounds.datagen.WorkaroundBlockStateProvider;

@Mixin(RegistrateBlockstateProvider.class)
public abstract class RegistrateBlockstateProviderMixin {
    private BlockStateProvider kilt$forgeProvider;

    public RegistrateBlockstateProviderMixin(AbstractRegistrate<?> parent, PackOutput packOutput, io.github.fabricators_of_create.porting_lib.data.ExistingFileHelper exFileHelper) {
    }

    @CreateInitializer
    public RegistrateBlockstateProviderMixin(AbstractRegistrate<?> parent, PackOutput packOutput, ExistingFileHelper exFileHelper) {
        this(parent, packOutput, exFileHelper.kilt$asPortingLib());
        this.kilt$forgeProvider = new WorkaroundBlockStateProvider((RegistrateBlockstateProvider) (Object) this, packOutput, parent.getModid(), exFileHelper);
    }

    public BlockModelProvider models() {
        return this.kilt$forgeProvider.models();
    }

    public ItemModelProvider itemModels() {
        return this.kilt$forgeProvider.itemModels();
    }

    public VariantBlockStateBuilder getVariantBuilder(Block block) {
        return this.kilt$forgeProvider.getVariantBuilder(block);
    }

    public MultiPartBlockStateBuilder getMultipartBuilder(Block block) {
        return this.kilt$forgeProvider.getMultipartBuilder(block);
    }
}
