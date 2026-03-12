package com.butchercircus.skinmanager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ScannerTool {

    private final boolean debugMode;

    private static final String RESULTS_FILE = "workshopMirror.txt";

    // Map of valid hero classes
    private final Map<String, String> heroClassMap = new HashMap<>();

    public ScannerTool(boolean debugMode) {
        this.debugMode = debugMode;
        initializeHeroClassMap();
    }

    private void initializeHeroClassMap() {
        //base game heroes
        heroClassMap.put("abomination", "Abomination");
        heroClassMap.put("antiquarian", "Antiquarian");
        heroClassMap.put("arbalest", "Arbalest");
        heroClassMap.put("bounty_hunter", "BountyHunter");
        heroClassMap.put("crusader", "Crusader");
        heroClassMap.put("grave_robber", "GraveRobber");
        heroClassMap.put("hellion", "Hellion");
        heroClassMap.put("highwayman", "Highwayman");
        heroClassMap.put("houndmaster", "Houndmaster");
        heroClassMap.put("jester", "Jester");
        heroClassMap.put("leper", "Leper");
        heroClassMap.put("man_at_arms", "ManAtArms");
        heroClassMap.put("occultist", "Occultist");
        heroClassMap.put("plague_doctor", "PlagueDoctor");
        heroClassMap.put("vestal", "Vestal");
        //dlc heroes
        heroClassMap.put("musketeer", "Musketeer");
        heroClassMap.put("flagellant", "Flagellant");
        heroClassMap.put("shieldbreaker", "Shieldbreaker");
    }

    /**
     * Layer 1: Scan Workshop mods
     * @param workshopPath
     */
    public void scanWorkshop(String workshopPath) {

        File workshopDir = new File(workshopPath);

        if (!workshopDir.exists() || !workshopDir.isDirectory()) {
            System.out.println("!| Workshop path not found or is not a directory.");
            return;
        }

        System.out.println("Scanning Workshop mods in: " + workshopPath);

        try (FileWriter writer = new FileWriter(RESULTS_FILE)) {

            writer.write("=== SkinManager Scan Results ===\n\n");

            File[] modFolders = workshopDir.listFiles(File::isDirectory);

            if (modFolders == null || modFolders.length == 0) {
                System.out.println("!| No mods found in workshop directory.");
                return;
            }

            for (File mod : modFolders) {

                File heroesFolder = new File(mod, "heroes");

                if (!heroesFolder.exists() || !heroesFolder.isDirectory()) {
                    debugPrint("Skipping mod '" + mod.getName() + "' (no heroes folder)");
                    continue;
                }

                debugPrint("\n=== Mod: " + mod.getName() + " ===");

                scanHeroesFolder(heroesFolder, mod.getName(), writer);
            }

        } catch (IOException e) {
            System.out.println("!| Failed to write scan results.");
        }
    }

    /**
     * Layer 2 & 3: Scan hero classes and palette folders
     */
    private void scanHeroesFolder(File heroesFolder, String modName, FileWriter writer) throws IOException {

        File[] heroClassFolders = heroesFolder.listFiles(File::isDirectory);

        if (heroClassFolders == null || heroClassFolders.length == 0) {
            debugPrint("'heroes' folder empty: " + heroesFolder.getAbsolutePath());
            return;
        }

        for (File classFolder : heroClassFolders) {

            String heroClassName = classFolder.getName();

            if (!heroClassMap.containsKey(heroClassName)) {
                debugPrint("Unknown hero class in mod: " + heroClassName);
                continue;
            }

            debugPrint("Valid hero class detected: " + heroClassName);

            File[] paletteFolders = classFolder.listFiles(File::isDirectory);

            if (paletteFolders == null) continue;

            for (File palette : paletteFolders) {

                String paletteName = palette.getName();

                // Ignore system folders
                if (paletteName.equalsIgnoreCase("anim")
                        || paletteName.equalsIgnoreCase("fx")
                        || paletteName.equalsIgnoreCase("icons_equip")) {

                    debugPrint("Skipping system folder: " + paletteName);
                    continue;
                }

                if (!paletteName.matches(heroClassName + "_[ABCD]")) {

                    String resultLine = "mod: " + modName
                            + ", hero: " + heroClassName
                            + ", skinPalette: " + paletteName;

                    System.out.println(" !| Unsupported skin palette -> " + resultLine);

                    writer.write(resultLine + System.lineSeparator());
                }
            }
        }
    }
    
    private void debugPrint(String message) {
        if (debugMode) {
            System.out.println("DEBUG| " + message);
        }
    }
}