package com.loserexe.modfile;

public class ModFile {
    private String fileName;
    private String downloadUrl;
    private boolean isServerPack;

    public String getFileName() {
        return this.fileName;
    }

    public String getDownloadUrl() {
        return this.downloadUrl;
    }

    public boolean isServerPack() {
        return this.isServerPack;
    }
}
