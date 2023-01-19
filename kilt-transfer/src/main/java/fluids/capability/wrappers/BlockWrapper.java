/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package fluids.capability.wrappers;

import fluids.FluidStack;
import fluids.FluidType;
import fluids.FluidUtil;
import fluids.capability.IFluidHandler;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import fluids.capability.templates.VoidFluidHandler;

/**
 * Wrapper around any block, only accounts for fluid placement, otherwise the block acts a void.
 * If the block in question inherits from the Forge implementations,
 * consider using {@link FluidBlockWrapper}.
 */
public class BlockWrapper extends VoidFluidHandler
{
    protected final BlockState state;
    protected final Level world;
    protected final BlockPos blockPos;

    public BlockWrapper(BlockState state, Level world, BlockPos blockPos)
    {
        this.state = state;
        this.world = world;
        this.blockPos = blockPos;
    }

    @Override
    public int fill(FluidStack resource, IFluidHandler.FluidAction action)
    {
        // NOTE: "Filling" means placement in this context!
        if (resource.getAmount() < FluidType.BUCKET_VOLUME)
        {
            return 0;
        }
        if (action.execute())
        {
            FluidUtil.destroyBlockOnFluidPlacement(world, blockPos);
            world.setBlock(blockPos, state, Block.UPDATE_ALL_IMMEDIATE);
        }
        return FluidType.BUCKET_VOLUME;
    }

    public static class LiquidContainerBlockWrapper extends VoidFluidHandler
    {
        protected final LiquidBlockContainer liquidContainer;
        protected final Level world;
        protected final BlockPos blockPos;

        public LiquidContainerBlockWrapper(LiquidBlockContainer liquidContainer, Level world, BlockPos blockPos)
        {
            this.liquidContainer = liquidContainer;
            this.world = world;
            this.blockPos = blockPos;
        }

        @Override
        public int fill(FluidStack resource, IFluidHandler.FluidAction action)
        {
            // NOTE: "Filling" means placement in this context!
            if (resource.getAmount() >= FluidType.BUCKET_VOLUME)
            {
                BlockState state = world.getBlockState(blockPos);
                if (liquidContainer.canPlaceLiquid(world, blockPos, state, resource.getFluid()))
                {
                    //If we are executing try to actually fill the container, if it failed return that we failed
                    if (action.simulate() || liquidContainer.placeLiquid(world, blockPos, state, resource.getFluid().getFluidType().getStateForPlacement(world, blockPos, resource)))
                    {
                        return FluidType.BUCKET_VOLUME;
                    }
                }
            }
            return 0;
        }
    }
}
