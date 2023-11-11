package com.loserexe;

import java.util.zip.ZipFile;

import com.loserexe.manifest.Manifest;

public class Modpack {
    private String modloader;
    private Manifest manifest;

    public Modpack(String modpackFileName) {
        try(ZipFile zip = new ZipFile(modpackFileName)) {
            String manifestRaw = new String(zip.getInputStream(zip.getEntry("manifest.json")).readAllBytes());
            manifest = Main.gson.fromJson(manifestRaw, Manifest.class);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        modloader = manifest.getMinecraftMeta().getModLoaders()[0].getId().split("-")[0];

        System.out.println("Minecraft \033[33m" + manifest.getMinecraftMeta().getVersion() + " " + modloader + "\033[0m");
        System.out.println(manifest.getName() + " " +  manifest.getVersion() + " | By " + manifest.getAuthor());
    }

    public Manifest getManifest() {
        return manifest;
    }
}
