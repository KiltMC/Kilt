package xyz.bluspring.kilt.workarounds.datagen

import io.github.fabricators_of_create.porting_lib.models.generators.block.BlockStateProvider as PortingLibBlockStateProvider
import net.minecraft.data.PackOutput
import net.minecraftforge.client.model.generators.BlockStateProvider
import net.minecraftforge.common.data.ExistingFileHelper

class WorkaroundBlockStateProvider(val parent: PortingLibBlockStateProvider, output: PackOutput, modId: String, fileHelper: ExistingFileHelper) : BlockStateProvider(output, modId, fileHelper) {
    override fun registerStatesAndModels() {
    }
}