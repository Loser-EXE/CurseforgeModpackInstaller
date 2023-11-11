package com.loserexe.manifest;

public class MinecraftMeta {
    private String version;
    private ModLoader[] modLoaders;

    public String getVersion() {
        return this.version;
    }

    public ModLoader[] getModLoaders() {
        return this.modLoaders;
    }
}
