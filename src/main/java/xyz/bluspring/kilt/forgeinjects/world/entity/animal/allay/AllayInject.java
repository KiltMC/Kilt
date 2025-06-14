package xyz.bluspring.kilt.forgeinjects.world.entity.animal.allay;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.ForgeEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Allay.class)
public abstract class AllayInject extends PathfinderMob {
    protected AllayInject(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    @ModifyReturnValue(method = "wantsToPickUp", at = @At("RETURN"))
    private boolean kilt$checkMobGriefingEvent(boolean original) {
        return original && ForgeEventFactory.getMobGriefingEvent(this.level(), this);
    }
}
