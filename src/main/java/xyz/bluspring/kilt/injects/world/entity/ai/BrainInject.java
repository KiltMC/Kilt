package xyz.bluspring.kilt.injects.world.entity.ai;

import com.mojang.datafixers.util.Pair;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.memory.ExpirableValue;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.entity.schedule.Schedule;
import net.neoforged.neoforge.common.util.BrainBuilder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.injections.world.entity.ai.BrainInjection;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Mixin(Brain.class)
public abstract class BrainInject<E extends LivingEntity> implements BrainInjection<E> {
    @Shadow @Final private Map<MemoryModuleType<?>, Optional<? extends ExpirableValue<?>>> memories;

    @Shadow @Final private Map<SensorType<? extends Sensor<? super E>>, Sensor<? super E>> sensors;

    @Shadow @Final private Map<Integer, Map<Activity, Set<BehaviorControl<? super E>>>> availableBehaviorsByPriority;

    @Shadow public abstract Schedule getSchedule();

    @Shadow @Final private Map<Activity, Set<Pair<MemoryModuleType<?>, MemoryStatus>>> activityRequirements;

    @Shadow @Final private Map<Activity, Set<MemoryModuleType<?>>> activityMemoriesToEraseWhenStopped;

    @Shadow private Set<Activity> coreActivities;

    @Shadow private Activity defaultActivity;

    @Shadow @Final private Set<Activity> activeActivities;

    @Shadow public abstract void setDefaultActivity(Activity newFallbackActivity);

    @Shadow public abstract void setCoreActivities(Set<Activity> newActivities);

    @Shadow public abstract void setSchedule(Schedule newSchedule);

    @Override
    public BrainBuilder<E> createBuilder() {
        var builder = new BrainBuilder<>((Brain<E>) (Object) this);
        builder.getMemoryTypes().addAll(this.memories.keySet());
        builder.getSensorTypes().addAll(this.sensors.keySet());
        builder.addAvailableBehaviorsByPriorityFrom(this.availableBehaviorsByPriority);
        builder.setSchedule(this.getSchedule());
        builder.addActivityRequirementsFrom(this.activityRequirements);
        builder.addActivityMemoriesToEraseWhenStoppedFrom(this.activityMemoriesToEraseWhenStopped);
        builder.getCoreActivities().addAll(this.coreActivities);
        builder.setDefaultActivity(this.defaultActivity);
        builder.setActiveActivites(this.activeActivities);

        return builder;
    }

    @Override
    public void copyFromBuilder(BrainBuilder<E> builder) {
        builder.addAvailableBehaviorsByPriorityTo(this.availableBehaviorsByPriority);
        this.setSchedule(builder.getSchedule());
        builder.addActivityRequirementsTo(this.activityRequirements);
        builder.addActivityMemoriesToEraseWhenStoppedTo(this.activityMemoriesToEraseWhenStopped);
        this.setCoreActivities(builder.getCoreActivities());
        this.setDefaultActivity(builder.getDefaultActivity());
        this.activeActivities.addAll(builder.getActiveActivites());
    }
}
