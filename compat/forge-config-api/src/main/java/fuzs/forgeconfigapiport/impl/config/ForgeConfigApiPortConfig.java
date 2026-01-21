package fuzs.forgeconfigapiport.impl.config;

import com.electronwill.nightconfig.core.ConfigSpec;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.file.FileNotFoundAction;
import com.electronwill.nightconfig.core.io.ParsingException;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.electronwill.nightconfig.toml.TomlFormat;
import com.google.common.collect.ImmutableMap;
import fuzs.forgeconfigapiport.impl.ForgeConfigAPIPort;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;

// Copied and modified from https://github.com/Fuzss/forgeconfigapiport/blob/1.20.1/Common/src/main/java/fuzs/forgeconfigapiport/impl/config/ForgeConfigApiPortConfig.java
// which is licensed under the MPLv2 - https://github.com/Fuzss/forgeconfigapiport/blob/1.20.1/LICENSE.md
public class ForgeConfigApiPortConfig {
    public static final ForgeConfigApiPortConfig INSTANCE;
    private static final String CONFIG_FILE_NAME = ForgeConfigAPIPort.MOD_ID + ".toml";
    private static final Map<String, Object> CONFIG_VALUES = ImmutableMap.<String, Object>builder().put("defaultConfigsPath", "defaultconfigs").put("forceGlobalServerConfigs", true).put("recreateConfigsWhenParsingFails", true).put("correctConfigValuesFromDefaultConfig", true).build();
    private static final ConfigSpec CONFIG_SPEC;

    static {
        CONFIG_SPEC = new ConfigSpec();
        for (Map.Entry<String, Object> entry : CONFIG_VALUES.entrySet()) {
            CONFIG_SPEC.define(entry.getKey(), entry.getValue());
        }
        INSTANCE = new ForgeConfigApiPortConfig();
    }

    private CommentedFileConfig configData;

    private ForgeConfigApiPortConfig() {
        this.loadFrom(FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILE_NAME));
    }

    // copied from FML config
    private void loadFrom(final Path configFile) {
        // autosave and autoreload are disabled to work around issues with the file watcher thread not terminating
        // and therefore causing dedicated servers to hang in Night Config v3.7.0+ (which we do not ship, but other mods will)
        this.configData = CommentedFileConfig.builder(configFile, TomlFormat.instance()).sync()
            .onFileNotFound(FileNotFoundAction.copyData(Objects.requireNonNull(this.getClass().getResourceAsStream("/" + CONFIG_FILE_NAME))))
            .writingMode(WritingMode.REPLACE)
            .build();

        try {
            this.configData.load();
        } catch (ParsingException e) {
            throw new RuntimeException("Failed to load %s config from %s".formatted(ForgeConfigAPIPort.MOD_NAME, configFile), e);
        }

        if (!CONFIG_SPEC.isCorrect(this.configData)) {
            ForgeConfigAPIPort.LOGGER.warn("Configuration file {} is not correct. Correcting", configFile);
            CONFIG_SPEC.correct(this.configData, (action, path, incorrectValue, correctedValue) -> ForgeConfigAPIPort.LOGGER.warn("Incorrect key {} was corrected from {} to {}", path, incorrectValue, correctedValue));
        }

        this.configData.save();
    }

    @SuppressWarnings("unchecked")
    public <T> T getValue(String key) {
        if (!CONFIG_VALUES.containsKey(key)) {
            throw new IllegalArgumentException("%s is not a know config value key".formatted(key));
        }
        return this.configData.<T>getOptional(key).orElse((T) CONFIG_VALUES.get(key));
    }
}
