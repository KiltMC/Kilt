package xyz.bluspring.kilt.injections.client.renderer.block.dispatch;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.neoforged.neoforge.client.model.block.CustomBlockModelDefinition;
import xyz.bluspring.kilt.helpers.ClientRecordMixinRefMaps;
import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelDispatcher;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

public interface BlockStateModelDispatcherInjection {
    AtomicReference<Codec<BlockStateModelDispatcher>> kilt$vanillaCodecProvider = new AtomicReference<>();

    static MapCodec<BlockStateModelDispatcher> kilt$getVanillaCodec() {
        return MapCodec.assumeMapUnsafe(kilt$vanillaCodecProvider.get());
    }

    static BlockStateModelDispatcher create(Optional<BlockStateModelDispatcher.SimpleModelSelectors> simpleModels, Optional<BlockStateModelDispatcher.MultiPartDefinition> multiPart, Optional<CustomBlockModelDefinition> customDefinition) {
        var dispatcher = new BlockStateModelDispatcher(simpleModels, multiPart);
        ClientRecordMixinRefMaps.EXTENDED_BLOCK_STATE_MODEL_DISPATCHER.put(dispatcher, new ClientRecordMixinRefMaps.ExtendedBlockStateModelDispatcher(customDefinition));
        return dispatcher;
    }

    static BlockStateModelDispatcher create(CustomBlockModelDefinition customDefinition) {
        return create(Optional.empty(), Optional.empty(), Optional.of(customDefinition));
    }

    default Optional<CustomBlockModelDefinition> customDefinition() {
        throw KiltHelper.createMixinException(BlockStateModelDispatcherInjection.class, "customDefinition");
    }

    default Map<BlockState, BlockStateModel.UnbakedRoot> instantiateVanilla(StateDefinition<Block, BlockState> stateDefinition, Supplier<String> source) {
        throw KiltHelper.createMixinException(BlockStateModelDispatcherInjection.class, "instantiateVanilla");
    }
}
