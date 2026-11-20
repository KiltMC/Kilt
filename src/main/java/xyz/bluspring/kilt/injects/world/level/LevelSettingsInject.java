package xyz.bluspring.kilt.injects.world.level;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import net.neoforged.neoforge.common.CommonHooks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.injections.world.level.LevelSettingsInjection;

import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;

@Mixin(LevelSettings.class)
public abstract class LevelSettingsInject implements LevelSettingsInjection {
    @Shadow @Final private String levelName;
    @Shadow @Final private GameType gameType;
    @Shadow @Final private LevelSettings.DifficultySettings difficultySettings;
    @Shadow @Final private boolean allowCommands;
    @Shadow @Final private WorldDataConfiguration dataConfiguration;
    @Unique private Lifecycle lifecycle = Lifecycle.stable();

    public LevelSettingsInject(String levelName, GameType gameType, LevelSettings.DifficultySettings difficultySettings, boolean allowCommands, WorldDataConfiguration dataConfiguration) {}

    @CreateInitializer
    public LevelSettingsInject(String levelName, GameType gameType, LevelSettings.DifficultySettings difficultySettings, boolean allowCommands, WorldDataConfiguration dataConfiguration, Lifecycle lifecycle) {
        this(levelName, gameType, difficultySettings, allowCommands, dataConfiguration);
        this.lifecycle = lifecycle;
    }

    @ModifyReturnValue(method = "parse", at = @At("RETURN"))
    private static LevelSettings kilt$parseLifecycleData(LevelSettings original, @Local(argsOnly = true, name = "input") Dynamic<?> dynamic) {
        original.kilt$setLifecycle(CommonHooks.parseLifecycle(dynamic.get("forgeLifecycle").asString("stable")));
        return original;
    }

    @ModifyReturnValue(method = {"withGameType", "withDifficulty", "withDataConfiguration", "copy"}, at = @At("RETURN"))
    private LevelSettings kilt$addLifecycleToCreateMethods(LevelSettings original) {
        original.kilt$setLifecycle(this.lifecycle);
        return original;
    }

    @Override
    public Lifecycle getLifecycle() {
        return lifecycle;
    }

    @Override
    public void kilt$setLifecycle(Lifecycle lifecycle) {
        this.lifecycle = lifecycle;
    }

    @Override
    public LevelSettings withLifecycle(Lifecycle lifecycle) {
        var settings = new LevelSettings(this.levelName, this.gameType, this.difficultySettings, this.allowCommands, this.dataConfiguration);
        settings.kilt$setLifecycle(lifecycle);

        return settings;
    }
}
