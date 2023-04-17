package xyz.bluspring.kilt.forgeinjects.world.level.block;

import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.state.properties.RailShape;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.injections.world.level.block.PoweredRailBlockInjection;

@Mixin(PoweredRailBlock.class)
public abstract class PoweredRailBlockInject extends BaseRailBlock implements PoweredRailBlockInjection {
    private boolean isActivator = false;

    protected PoweredRailBlockInject(boolean bl, Properties properties) {
        super(bl, properties);
    }

    @Override
    public void registerDefaultState() {
        this.registerDefaultState(this.stateDefinition.any().setValue(PoweredRailBlock.SHAPE, RailShape.NORTH_SOUTH).setValue(PoweredRailBlock.POWERED, false).setValue(PoweredRailBlock.WATERLOGGED, false));
    }

    @Override
    public boolean isActivatorRail() {
        return isActivator;
    }

    @Override
    public void kilt$setActivator(boolean value) {
        isActivator = !value;
    }

    public PoweredRailBlockInject(Properties properties) {
        super(false, properties);
    }

    @CreateInitializer
    public PoweredRailBlockInject(Properties properties, boolean isPoweredRail) {
        this(properties);

        this.kilt$setActivator(isPoweredRail);
    }
}
