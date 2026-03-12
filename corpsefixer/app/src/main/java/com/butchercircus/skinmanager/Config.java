package com.butchercircus.skinmanager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class Config {

    private static final String CONFIG_FILE = "config.txt";

    private String workshopPath;
    private String arenaHeroesPath;

    public String getWorkshopPath() {
        return workshopPath;
    }

    public String getArenaHeroesPath() {
        return arenaHeroesPath;
    }

    // Try to load paths from config.txt
    public boolean load() {
        try {
            File file = new File(CONFIG_FILE);
            if (!file.exists()) return false;

            Scanner reader = new Scanner(file);

            while (reader.hasNextLine()) {
                String line = reader.nextLine().trim();

                if (line.startsWith("workshop=")) {
                    String[] parts = line.split("=", 2);
                    workshopPath = parts.length > 1 ? parts[1].trim() : "";
                }
                if (line.startsWith("arena_heroes=")) {
                    String[] parts = line.split("=", 2);
                    arenaHeroesPath = parts.length > 1 ? parts[1].trim() : "";
                }
            }

            reader.close();
            return workshopPath != null && arenaHeroesPath != null && !workshopPath.isEmpty() && !arenaHeroesPath.isEmpty();

        } catch (FileNotFoundException e) {
            return false;
        }
    }

    // Save paths to config.txt
    public void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            writer.write("workshop=" + workshopPath + "\n");
            writer.write("arena_heroes=" + arenaHeroesPath);
        } catch (IOException e) {
            System.out.println("Failed to save config.");
        }
    }

    // Derive the two paths from the root path
    public void derivePathsFromRoot(String root) {
        // Butcher Circus DLC heroes path
        arenaHeroesPath = root
                + File.separator + "dlc"
                + File.separator + "1117860_arena_mp"
                + File.separator + "heroes";

        // Workshop mods path
        File rootFile = new File(root);
        File commonDir = rootFile.getParentFile();       // steamapps\common
        File steamappsDir = commonDir.getParentFile();   // steamapps

        workshopPath = steamappsDir.getAbsolutePath()
                + File.separator + "workshop"
                + File.separator + "content"
                + File.separator + "262060";
    }
}