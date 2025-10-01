package xyz.bluspring.kilt.mixin;

import io.github.fabricators_of_create.porting_lib.fluids.FluidType;
import io.github.fabricators_of_create.porting_lib.fluids.sound.SoundAction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.pathfinder.PathType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(FluidType.class)
public interface FluidTypeAccessor {
    @Accessor("descriptionId")
    String kilt$getDescriptionId();

    @Accessor("descriptionId")
    void kilt$setDescriptionId(String id);

    @Accessor
    double getMotionScale();

    @Accessor
    boolean isCanPushEntity();

    @Accessor
    boolean isCanSwim();

    @Accessor
    boolean isCanDrown();

    @Accessor
    float getFallDistanceModifier();

    @Accessor
    boolean isCanExtinguish();

    @Accessor
    boolean isCanConvertToSource();

    @Accessor
    boolean isSupportsBoating();

    @Accessor
    PathType getPathType();

    @Accessor
    PathType getAdjacentPathType();

    @Accessor
    boolean isCanHydrate();

    @Accessor
    int getLightLevel();

    @Accessor
    int getDensity();

    @Accessor
    int getTemperature();

    @Accessor
    int getViscosity();

    @Accessor
    Rarity getRarity();

    @Accessor
    Map<SoundAction, SoundEvent> getSounds();
}
