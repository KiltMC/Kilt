package xyz.bluspring.kilt.injects.world.clock;

import java.util.Map;
import java.util.function.Consumer;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.neoforged.neoforge.common.Tags;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.injections.world.clock.ServerClockManagerInjection;

import net.minecraft.core.Holder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.TriState;
import net.minecraft.world.clock.ServerClockManager;
import net.minecraft.world.clock.WorldClock;

@Mixin(ServerClockManager.class)
public abstract class ServerClockManagerInject implements ServerClockManagerInjection {
    @Shadow
    private MinecraftServer server;

    @Shadow
    @Final
    private Map<Holder<WorldClock>, ServerClockManager.ClockInstance> clocks;

    @Shadow
    protected abstract ServerClockManager.ClockInstance getInstance(Holder<WorldClock> definition);

    @ModifyExpressionValue(method = "lambda$init$0", at = @At(value = "NEW", target = "()Lnet/minecraft/world/clock/ServerClockManager$ClockInstance;"))
    private ServerClockManager.ClockInstance kilt$storeDefinitionInClockInstance(ServerClockManager.ClockInstance original, @Local(name = "definition", argsOnly = true) Holder.Reference<WorldClock> definition) {
        original.kilt$setHolder(definition);
        return original;
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void kilt$avoidTickingIfNoOverworld(CallbackInfo ci) {
        if (this.server.overworld() == null)
            ci.cancel();
    }

    @Definition(id = "advanceTime", local = @Local(type = boolean.class, name = "advanceTime"))
    @Expression("advanceTime")
    @Inject(method = "tick", at = @At(value = "MIXINEXTRAS:EXPRESSION", ordinal = 0))
    private void kilt$advanceAllClocksWithSpecialTags(CallbackInfo ci, @Local(name = "advanceTime") boolean advanceTime) {
        this.clocks.values().forEach(clock -> {
            if (kilt$shouldClockBeAdvancedAhead(clock)) {
                clock.tick(advanceTime);
            }
        });
    }

    @Definition(id = "clocks", field = "Lnet/minecraft/world/clock/ServerClockManager;clocks:Ljava/util/Map;")
    @Definition(id = "values", method = "Ljava/util/Map;values()Ljava/util/Collection;")
    @Definition(id = "forEach", method = "Ljava/util/Collection;forEach(Ljava/util/function/Consumer;)V")
    @Expression("this.clocks.values().forEach(@(?))")
    @ModifyExpressionValue(method = "tick", at = @At("MIXINEXTRAS:EXPRESSION"))
    private Consumer<ServerClockManager.ClockInstance> kilt$filterOutAdvancedClocks(Consumer<ServerClockManager.ClockInstance> original) {
        return clock -> {
            if (!kilt$shouldClockBeAdvancedAhead(clock))
                original.accept(clock);
        };
    }

    @Unique
    private static boolean kilt$shouldClockBeAdvancedAhead(ServerClockManager.ClockInstance clock) {
        var definition = clock.kilt$getHolder();
        if (definition == null)
            return false;

        return definition.is(Tags.WorldClocks.IGNORES_ADVANCE_TIME_RULE);
    }

    @Override
    public float getRate(Holder<WorldClock> definition) {
        return this.getInstance(definition).kilt$getRate();
    }

    @Override
    public float getPartialTick(Holder<WorldClock> definition) {
        return this.getInstance(definition).kilt$getPartialTick();
    }

    @Mixin(ServerClockManager.ClockInstance.class)
    public abstract static class ClockInstanceInject implements ClockInstanceInjection {
        @Unique private @Nullable Holder<WorldClock> holder;

        @Shadow public ClockInstanceInject() {}
        @Shadow public abstract void tick();

        @Shadow
        private boolean paused;

        @CreateInitializer
        public ClockInstanceInject(Holder<WorldClock> holder) {
            this();
            this.holder = holder;
        }

        @Unique private TriState kilt$shouldAdvanceTime = TriState.DEFAULT;

        @Override
        public void tick(boolean advanceTime) {
            this.kilt$shouldAdvanceTime = TriState.from(advanceTime);
            this.tick();
            this.kilt$shouldAdvanceTime = TriState.DEFAULT;
        }

        @Definition(id = "paused", field = "Lnet/minecraft/world/clock/ServerClockManager$ClockInstance;paused:Z")
        @Expression("this.paused == false")
        @ModifyExpressionValue(method = "tick", at = @At("MIXINEXTRAS:EXPRESSION"))
        private boolean kilt$checkIsTicking(boolean original) {
            if (this.kilt$shouldAdvanceTime != TriState.DEFAULT) {
                // Kilt: Override using our check directly, by all means this is getting handled directly by either NeoForge mods or ourselves entirely.
                return this.ticking(this.kilt$shouldAdvanceTime.toBoolean(true));
            }

            // Kilt: otherwise, basically try to recreate the ticking method below.
            return original || this.hasTag(Tags.WorldClocks.IGNORES_PAUSE_COMMAND);
        }

        @Definition(id = "paused", local = @Local(type = boolean.class, name = "paused"))
        @Expression("paused = ?")
        @ModifyVariable(method = "packNetworkState", at = @At(value = "MIXINEXTRAS:EXPRESSION", shift = At.Shift.AFTER), name = "paused")
        private boolean kilt$checkShouldBeTicking(boolean paused) {
            // Kilt: Reconstruct the original code.
            if (this.paused && !this.hasTag(Tags.WorldClocks.IGNORES_PAUSE_COMMAND))
                return true;

            if (this.hasTag(Tags.WorldClocks.IGNORES_ADVANCE_TIME_RULE))
                return false;

            return paused;
        }

        @Unique
        private boolean ticking(boolean advanceTime) {
            if (this.paused && !this.hasTag(Tags.WorldClocks.IGNORES_PAUSE_COMMAND))
                return false;

            return advanceTime || this.hasTag(Tags.WorldClocks.IGNORES_ADVANCE_TIME_RULE);
        }

        @Unique
        private boolean hasTag(TagKey<WorldClock> tag) {
            return this.holder != null && this.holder.is(tag);
        }

        @Override
        public Holder<WorldClock> kilt$getHolder() {
            return this.holder;
        }

        @Override
        public void kilt$setHolder(Holder<WorldClock> definition) {
            this.holder = definition;
        }

        @Override
        @Accessor("partialTick")
        public abstract float kilt$getPartialTick();

        @Override
        @Accessor("rate")
        public abstract float kilt$getRate();
    }
}
