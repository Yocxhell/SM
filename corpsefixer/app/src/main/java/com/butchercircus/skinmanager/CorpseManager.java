package com.butchercircus.skinmanager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;

public class CorpseManager {

    private final boolean debugMode;
    private final String arenaHeroesPath;
    private final String workshopPath;
    private final String mirrorFile = "workshopMirror.txt";

    private final List<String> dlcHeroes = Arrays.asList("flagellant", "shieldbreaker", "musketeer");
    private final List<String> systemFolders = Arrays.asList("anim", "fx", "icons_equip");

    public CorpseManager(boolean debugMode, String arenaHeroesPath, String workshopPath) {
        this.debugMode = debugMode;
        this.arenaHeroesPath = arenaHeroesPath;
        this.workshopPath = workshopPath;
    }

    /** Assign corpses using safe/custom mode checked from mod source */
    public void assignCorpses(String selectedMode) {
        File mirror = new File(mirrorFile);
        if (!mirror.exists()) {
            System.out.println("!| workshopMirror.txt not found.");
            return;
        }

        try {
            List<String> lines = Files.readAllLines(mirror.toPath());

            for (String line : lines) {
                if (line.trim().isEmpty()) continue;

                String[] parts = line.split(",");
                if (parts.length != 3) continue;

                String hero = parts[1].split(":")[1].trim();
                String palette = parts[2].split(":")[1].trim();

                // Skip default _A/_B/_C/_D and system folders
                if (palette.matches(hero + "_[ABCD]") || systemFolders.contains(palette.toLowerCase())) {
                    debugPrint("Skipping default/system palette: " + palette);
                    continue;
                }

                File targetHeroDir = new File(arenaHeroesPath, hero);
                if (!targetHeroDir.exists()) targetHeroDir.mkdirs();

                File targetFolder = new File(targetHeroDir, palette);

                // Find the mod folder containing this palette
                File modFolder = findModForPalette(hero, palette);
                if (modFolder == null) {
                    System.out.println("!| Palette not found in any mod: " + palette + " for hero " + hero);
                    continue;
                }

                // Determine assignment type based on mod's anim folder
                File heroFolderInMod = new File(modFolder, "heroes" + File.separator + hero);
                File animFolder = new File(heroFolderInMod, "anim");
                boolean isCustom = false;

                if (animFolder.exists() && animFolder.isDirectory()) {
                    for (File f : animFolder.listFiles()) {
                        if (f.getName().endsWith(".skel") || f.getName().endsWith(".atlas")) {
                            isCustom = true;
                            break;
                        }
                    }
                }

                // Skip if mode does not match
                if ((selectedMode.equalsIgnoreCase("safe") && isCustom) ||
                    (selectedMode.equalsIgnoreCase("custom") && !isCustom)) {
                    debugPrint("Skipping palette due to mode mismatch: " + palette);
                    continue;
                }

                // Determine source folder for copying
                File sourceFolder;
                if (dlcHeroes.contains(hero.toLowerCase())) {
                    sourceFolder = new File(heroFolderInMod, palette);
                } else {
                    // Base heroes always copy _A from arenaHeroesPath
                    sourceFolder = new File(targetHeroDir, hero + "_A");
                }

                if (!sourceFolder.exists()) {
                    System.out.println("!| Source palette missing: " + sourceFolder.getAbsolutePath());
                    continue;
                }

                // Copy the palette
                copyFolder(sourceFolder, targetFolder);

                // Copy dead sprite for DLC heroes
                if (dlcHeroes.contains(hero.toLowerCase())) {
                    copyDlcDeadSprite(targetFolder, hero);
                }

                debugPrint("Created palette folder: " + targetFolder.getName());
            }

            System.out.println("✔ Corpses assignment completed (" + selectedMode + " mode).");

        } catch (IOException e) {
            System.out.println("!| Failed reading workshopMirror.txt");
        }
    }

    /** Remove all extra palettes not _A/_B/_C/_D or system folders */
    public void removeCorpses() {
        File heroesDir = new File(arenaHeroesPath);
        if (!heroesDir.exists() || !heroesDir.isDirectory()) return;

        for (File heroDir : heroesDir.listFiles(File::isDirectory)) {
            String heroName = heroDir.getName();

            for (File folder : heroDir.listFiles(File::isDirectory)) {
                String name = folder.getName();

                if (name.matches(heroName + "_[ABCD]") || systemFolders.contains(name.toLowerCase())) continue;

                deleteFolder(folder);
                debugPrint("Removed extra palette folder: " + name);
            }
        }

        System.out.println("✔ Corpses removal completed.");
    }

    /** Copy folder recursively */
    private void copyFolder(File source, File target) throws IOException {
        if (source.isDirectory()) {
            target.mkdir();
            for (File f : source.listFiles()) copyFolder(f, new File(target, f.getName()));
        } else {
            Files.copy(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /** Delete folder recursively */
    private void deleteFolder(File folder) {
        if (!folder.exists()) return;

        if (folder.isDirectory()) {
            for (File f : folder.listFiles()) deleteFolder(f);
        }

        folder.delete();
    }

    /** Find which mod folder contains the hero/palette */
    private File findModForPalette(String hero, String palette) {
        File workshopDir = new File(workshopPath);
        if (!workshopDir.exists()) return null;

        File[] mods = workshopDir.listFiles(File::isDirectory);
        if (mods == null) return null;

        for (File mod : mods) {
            File heroFolder = new File(mod, "heroes" + File.separator + hero);
            if (!heroFolder.exists()) continue;

            File paletteFolder = new File(heroFolder, palette);
            if (paletteFolder.exists()) return mod;
        }

        return null;
    }

    /** Copy only the dead sprite from _A/anim into DLC palette */
    private void copyDlcDeadSprite(File targetFolder, String heroName) throws IOException {
        File deadSpriteSource = new File(arenaHeroesPath + File.separator +
                                         heroName + File.separator +
                                         heroName + "_A" + File.separator +
                                         "anim" + File.separator +
                                         heroName + ".sprite.dead.png");

        if (!deadSpriteSource.exists()) {
            debugPrint("Dead sprite not found for " + heroName);
            return;
        }

        File targetAnimFolder = new File(targetFolder, "anim");
        if (!targetAnimFolder.exists()) targetAnimFolder.mkdirs();

        File deadSpriteTarget = new File(targetAnimFolder, deadSpriteSource.getName());
        Files.copy(deadSpriteSource.toPath(), deadSpriteTarget.toPath(), StandardCopyOption.REPLACE_EXISTING);

        debugPrint("Copied dead sprite for " + heroName + " to " + targetFolder.getName());
    }

    private void debugPrint(String msg) {
        if (debugMode) System.out.println("DEBUG| " + msg);
    }
}