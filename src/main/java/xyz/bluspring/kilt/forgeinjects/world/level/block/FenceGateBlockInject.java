// TRACKED HASH: 13883d3a815e6b78fb2ecf76ce46710d2684597f
package xyz.bluspring.kilt.forgeinjects.world.level.block;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.WoodType;
import org.spongepowered.asm.mixin.*;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;

@Mixin(FenceGateBlock.class)
public abstract class FenceGateBlockInject extends HorizontalDirectionalBlock {
    @Shadow @Final public static BooleanProperty OPEN;
    @Shadow @Final public static BooleanProperty POWERED;
    @Shadow @Final public static BooleanProperty IN_WALL;
    @Shadow @Final @Mutable
    private WoodType type;
    @Unique private SoundEvent openSound;
    @Unique private SoundEvent closeSound;

    public FenceGateBlockInject(BlockBehaviour.Properties props, WoodType type) {
        super(props);
    }

    @CreateInitializer
    public FenceGateBlockInject(BlockBehaviour.Properties props, SoundEvent openSound, SoundEvent closeSound) {
        this(props, new WoodType("kilt_fake_type", BlockSetType.STONE, SoundType.STONE, SoundType.STONE, closeSound, openSound));
        this.openSound = openSound;
        this.closeSound = closeSound;
    }
}