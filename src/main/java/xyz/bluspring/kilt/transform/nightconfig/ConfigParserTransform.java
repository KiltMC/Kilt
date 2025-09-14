package xyz.bluspring.kilt.transform.nightconfig;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.ConfigFormat;
import com.electronwill.nightconfig.core.file.FileNotFoundAction;
import com.electronwill.nightconfig.core.io.ConfigParser;
import com.electronwill.nightconfig.core.io.ParsingException;
import com.electronwill.nightconfig.core.io.ParsingMode;
import net.lenni0451.classtransform.annotations.CInline;
import net.lenni0451.classtransform.annotations.CLocalVariable;
import net.lenni0451.classtransform.annotations.CShadow;
import net.lenni0451.classtransform.annotations.CTransformer;
import net.lenni0451.classtransform.annotations.injection.CWrapCatch;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.util.KiltLoggers;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

// A reimplementation of https://github.com/Fuzss/nightconfigfixes/blob/main/1.20/Fabric/src/main/java/fuzs/nightconfigfixes/mixin/ConfigParserFabricMixin.java
// that involves using ClassTransform instead, as we load NightConfig far too early for mixin to actually kick in.
@CTransformer(ConfigParser.class)
public interface ConfigParserTransform<C extends Config> {
    @CShadow
    ConfigFormat<C> getFormat();

    @CShadow
    C parse(InputStream input, Charset charset);

    @CShadow
    void parse(InputStream input, Config destination, ParsingMode parsingMode, Charset charset);

    @CInline
    @CWrapCatch(value = "parse(Ljava/nio/file/Path;Lcom/electronwill/nightconfig/core/file/FileNotFoundAction;Ljava/nio/charset/Charset;)Lcom/electronwill/nightconfig/core/Config;", target = "Lcom/electronwill/nightconfig/core/io/ConfigParser;parse(Ljava/io/InputStream;Ljava/nio/charset/Charset;)Lcom/electronwill/nightconfig/core/Config;")
    private C kilt$nightconfigfixes$catchAndRecreateConfig(ParsingException exception,
                                                           @CLocalVariable(name = "file") Path file,
                                                           @CLocalVariable(name = "nefAction") FileNotFoundAction nefAction,
                                                           @CLocalVariable(name = "charset") Charset charset
    ) {
        try {
            Files.delete(file);

            if (nefAction.run(file, this.getFormat())) {
                C config;
                try (InputStream input = Files.newInputStream(file)) {
                    config = this.parse(input, charset);
                }

                KiltLoggers.NIGHT_CONFIG_FIXES.warn("Configuration file {} could not be parsed. Correcting.", file.toAbsolutePath());
                return config;
            }
        } catch (Throwable t) {
            exception.addSuppressed(t);
        }

        throw exception;
    }

    @CInline
    @CWrapCatch(value = "parse(Ljava/nio/file/Path;Lcom/electronwill/nightconfig/core/Config;Lcom/electronwill/nightconfig/core/io/ParsingMode;Lcom/electronwill/nightconfig/core/file/FileNotFoundAction;Ljava/nio/charset/Charset;)V", target = "Lcom/electronwill/nightconfig/core/io/ConfigParser;parse(Ljava/io/InputStream;Lcom/electronwill/nightconfig/core/Config;Lcom/electronwill/nightconfig/core/io/ParsingMode;Ljava/nio/charset/Charset;)V")
    private void kilt$nightconfigfixes$catchAndRecreateConfig(ParsingException exception,
                                                           @CLocalVariable(name = "file") Path file,
                                                           @CLocalVariable(name = "destination") Config destination,
                                                           @CLocalVariable(name = "parsingMode") ParsingMode parsingMode,
                                                           @CLocalVariable(name = "nefAction") FileNotFoundAction nefAction,
                                                           @CLocalVariable(name = "charset") Charset charset
    ) {
        try {
            Files.delete(file);

            if (nefAction.run(file, this.getFormat())) {
                try (InputStream input = Files.newInputStream(file)) {
                    this.parse(input, destination, parsingMode, charset);
                }

                KiltLoggers.NIGHT_CONFIG_FIXES.warn("Configuration file {} could not be parsed. Correcting.", file.toAbsolutePath());
                return;
            }
        } catch (Throwable t) {
            exception.addSuppressed(t);
        }

        throw exception;
    }
}
