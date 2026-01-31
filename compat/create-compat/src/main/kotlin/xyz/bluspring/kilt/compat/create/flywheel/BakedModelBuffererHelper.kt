package xyz.bluspring.kilt.compat.create.flywheel

import net.minecraft.core.BlockPos
import net.neoforged.neoforge.client.model.data.ModelData
import java.util.function.Function

object BakedModelBuffererHelper {
    @JvmStatic
    val modelData: ThreadLocal<ModelData> = ThreadLocal.withInitial { ModelData.EMPTY }

    @JvmStatic
    val modelDataLookup: ThreadLocal<Function<BlockPos, ModelData>> = ThreadLocal.withInitial { Function { ModelData.EMPTY } }
}