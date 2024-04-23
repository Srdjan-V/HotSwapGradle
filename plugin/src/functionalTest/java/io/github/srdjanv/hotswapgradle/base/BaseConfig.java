package io.github.srdjanv.hotswapgradle.base;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

abstract class BaseConfig {
    protected final File projectDir;
    protected final File outFile;
    protected final List<Supplier<String>> plugins = new ArrayList<>();
    protected final List<Supplier<String>> commonOptions = new ArrayList<>();
    protected final List<Consumer<StringBuilder>> customBuilders = new ArrayList<>();

    protected BaseConfig(File projectDir, String outFile) {
        this.projectDir = projectDir;
        this.outFile = new File(projectDir, outFile);
    }

    public void plugin(Supplier<String> plugin) {
        plugins.add(plugin);
    }

    public void append(Supplier<String> option) {
        commonOptions.add(option);
    }

    public void append(Consumer<StringBuilder> customBuilder) {
        customBuilders.add(customBuilder);
    }

    void buildFile() {
        var builder = new StringBuilder();
        if (!plugins.isEmpty()) {
            builder.append("plugins {").append(System.lineSeparator());
            for (Supplier<String> plugin : plugins) {
                builder.append(plugin.get());
                builder.append(System.lineSeparator());
            }
            builder.append("}").append(System.lineSeparator());
        }
        for (var commonOption : commonOptions) {
            builder.append(commonOption.get());
            builder.append(System.lineSeparator());
        }
        for (var customOption : customBuilders) {
            customOption.accept(builder);
            builder.append(System.lineSeparator());
        }

        write(outFile, builder.toString());
    }

    void write(File file, String string) {
        file.getParentFile().mkdirs();
        try (Writer writer = new FileWriter(file)) {
            file.createNewFile();
            writer.write(string);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
