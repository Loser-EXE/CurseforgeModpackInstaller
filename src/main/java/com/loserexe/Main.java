package com.loserexe;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.loserexe.manifest.Manifest;
import com.loserexe.manifest.ModMeta;
import com.loserexe.modfile.ModFile;


public class Main {
    public static final Gson gson = new Gson();
    public static final String CWD = System.getProperty("user.dir");
    private static final HttpClient client = HttpClient.newHttpClient();

    private static final String CURSEFORGE_API_KEY = "$2a$10$cHdr7iP/mlbdPSyWU8I4KuTQdO6EyZAfJj.IHwGd2SpP5g0E6HRXK";

    private static final String MOD_FILE_URL = "https://api.curseforge.com/v1/mods/%s/files/%s"; //ModID FileID

    private static final CommandLineParser parser = new DefaultParser();
    private static final HelpFormatter helpFormater = new HelpFormatter();

    private static final String name = "Curseforge Modpack Installer";
    private static final String version = "1.0.0";

    private static String modpackFileName;
    private static String apiKey;

    public static void main(String[] args) {
        Options options = new Options();

        Option fileOption = new Option("f", "file", true, "The modpack zip file");
        Option apiKeyOption = new Option("k", "key", true, "Curseforge API Key (Optional)");
        Option helpOption = new Option("h", "help", false, "Prints this message");
        Option versionOption = new Option("v", "version", false, "Prints the version");

        options.addOption(fileOption);
        options.addOption(apiKeyOption);
        options.addOption(helpOption);
        options.addOption(versionOption);

        try {
            CommandLine line = parser.parse(options, args);

            if(line.hasOption(versionOption)) {
                System.out.println(String.format("%s %s", name, version));
                System.exit(0);
            }

            if(line.hasOption(helpOption)) {
                helpFormater.printHelp("Installer", options);
                System.exit(0);
            }

            fileOption.setRequired(true);
            options.addOption(fileOption);
            line = parser.parse(options, args); //So required options not required to run help or version

            modpackFileName = line.getOptionValue(fileOption);

            if(line.hasOption(apiKeyOption)) {
                apiKey = line.getOptionValue(apiKeyOption);
            } else {
                apiKey = CURSEFORGE_API_KEY;
            }
        } catch(ParseException e) {
            System.out.println("Invalid Usage");
            helpFormater.printHelp("Installer", options);
            // e.printStackTrace();
            System.exit(1);
        }

        List<ModFile> mods = new ArrayList<>();
        Modpack modpack = new Modpack(modpackFileName); 
        Manifest manifest = modpack.getManifest();

        System.out.println("\nDownloading files\n");

        for(ModMeta mod : manifest.getMods()) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(String.format(MOD_FILE_URL ,mod.getProjectId(), mod.getFileId())))
                    .header("x-api-key", apiKey)
                    .headers("accept", "application/json")
                    .build();
                HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

                String modRaw = JsonParser.parseString(response.body())
                    .getAsJsonObject().getAsJsonObject("data").toString();

                mods.add(gson.fromJson(modRaw, ModFile.class));
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        }

        
        String path = CWD + "/mods/";
        List<String> failedDownloads = new ArrayList<>();
        new File(path).mkdirs();

        for(ModFile mod : mods) {
            if(mod.getDownloadUrl() == null) {
                failedDownloads.add(mod.getFileName());
                System.out.println("\033[31mUnable to download " + mod.getFileName() + " (Author disabled 3rd party downlaods)" + "\033[0m");
                continue;
            }

            downloadFile(path + mod.getFileName(), mod.getFileName(), mod.getDownloadUrl());
        }
        
        if(failedDownloads.size() != 0) {
            System.out.println("\n\033[31mThe following files were not downloaded because their respective authors disabled 3rd party downloads\n");

            failedDownloads.forEach((name) -> {
                System.out.println(name);
            });
        }

        System.out.print("\033[0m");
    }

    private static void downloadFile(String filePath, String fileName, String url) {
        String styledFileName = "\033[34m" + fileName + "\033[0m";
        System.out.print(styledFileName + " Downloading...");
        File file = new File(filePath);

        if(!file.exists()) {
            try(BufferedInputStream fileInputStream = new BufferedInputStream(new URL(url).openStream())) {
                FileOutputStream fileOutputStream = new FileOutputStream(file);

                byte[] buffer = new byte[1024];
                int bytesRead;
                
                while((bytesRead = fileInputStream.read(buffer, 0, buffer.length)) != -1) {
                    fileOutputStream.write(buffer, 0, bytesRead);
                }

                fileOutputStream.close();
            } catch(Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        }

        System.out.print("\33[2K\r" + styledFileName + " \033[1;32mDone ✓\033[0m");
        System.out.println();
    }
}
