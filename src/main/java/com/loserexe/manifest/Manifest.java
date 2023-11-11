package com.loserexe.manifest;


public class Manifest {
    private MinecraftMeta minecraft;
    private String name;
    private String version;
    private String author;
    private ModMeta[] files;

    public MinecraftMeta getMinecraftMeta() {
        return this.minecraft;
    }

    public String getName() {
        return this.name;
    }

    public String getVersion() {
        return this.version;
    }

    public String getAuthor() {
        return this.author;
    }

    public ModMeta[] getMods() {
        return this.files;
    }
}
