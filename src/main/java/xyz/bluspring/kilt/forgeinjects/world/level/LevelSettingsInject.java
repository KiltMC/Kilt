package xyz.bluspring.kilt.forgeinjects.world.level;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraftforge.common.ForgeHooks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.injections.world.level.LevelSettingsInjection;

@Mixin(LevelSettings.class)
public abstract class LevelSettingsInject implements LevelSettingsInjection {
    @Shadow @Final private String levelName;
    @Shadow @Final private GameType gameType;
    @Shadow @Final private boolean hardcore;
    @Shadow @Final private Difficulty difficulty;
    @Shadow @Final private boolean allowCommands;
    @Shadow @Final private GameRules gameRules;
    @Shadow @Final private DataPackConfig dataPackConfig;
    @Unique private Lifecycle lifecycle = Lifecycle.stable();

    public LevelSettingsInject(String levelName, GameType gameType, boolean hardcore, Difficulty difficulty, boolean allowCommands, GameRules gameRules, DataPackConfig dataConfiguration) {}

    @CreateInitializer
    public LevelSettingsInject(String levelName, GameType gameType, boolean hardcore, Difficulty difficulty, boolean allowCommands, GameRules gameRules, DataPackConfig dataConfiguration, Lifecycle lifecycle) {
        this(levelName, gameType, hardcore, difficulty, allowCommands, gameRules, dataConfiguration);
        this.lifecycle = lifecycle;
    }

    @ModifyReturnValue(method = "parse", at = @At("RETURN"))
    private static LevelSettings kilt$parseLifecycleData(LevelSettings original, @Local(argsOnly = true) Dynamic<?> dynamic) {
        ((LevelSettingsInjection) (Object) original).kilt$setLifecycle(ForgeHooks.parseLifecycle(dynamic.get("forgeLifecycle").asString("stable")));
        return original;
    }

    @ModifyReturnValue(method = {"withGameType", "withDifficulty", "withDataPackConfig", "copy"}, at = @At("RETURN"))
    private LevelSettings kilt$addLifecycleToCreateMethods(LevelSettings original) {
        ((LevelSettingsInjection) (Object) original).kilt$setLifecycle(this.lifecycle);
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
        var settings = new LevelSettings(this.levelName, this.gameType, this.hardcore, this.difficulty, this.allowCommands, this.gameRules, this.dataPackConfig);
        ((LevelSettingsInjection) (Object) settings).kilt$setLifecycle(lifecycle);

        return settings;
    }
}