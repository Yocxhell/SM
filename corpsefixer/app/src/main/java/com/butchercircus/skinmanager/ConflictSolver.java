package com.butchercircus.skinmanager;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

public class ConflictSolver {


    private static final String RESULTS_FILE = "workshopMirror.txt";
    private final String workshopPath;

    public ConflictSolver(String workshopPath) {
        this.workshopPath = workshopPath;
    }

    public void checkConflicts() {

        File file = new File(RESULTS_FILE);

        if (!file.exists()) {
            System.out.println("!| workshopMirror.txt not found. Run a scan first.");
            return;
        }

        Map<String, Set<String>> paletteMap = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {

            String line;

            while ((line = reader.readLine()) != null) {

                if (!line.contains("skinPalette:")) continue;

                String[] parts = line.split(", ");
                if (parts.length < 3) continue;

                String modName = parts[0].replace("mod: ", "").trim();
                String heroClass = parts[1].replace("hero: ", "").trim();
                String skinPalette = parts[2].replace("skinPalette: ", "").trim();

                cleanupPrefixIfOriginalExists(modName, heroClass, skinPalette);

                String key = heroClass + ":" + skinPalette;

                paletteMap
                        .computeIfAbsent(key, k -> new HashSet<>())
                        .add(modName);
            }

        } catch (Exception e) {
            System.out.println("!| Failed reading workshopMirror.txt: " + e.getMessage());
            return;
        }

        boolean conflictFound = false;

        for (Map.Entry<String, Set<String>> entry : paletteMap.entrySet()) {

            Set<String> mods = entry.getValue();

            if (mods.size() > 1) {

                String[] keyParts = entry.getKey().split(":");

                String heroClass = keyParts[0];
                String skinPalette = keyParts[1];

                System.out.println(
                        "Conflict detected -> hero: " + heroClass +
                        ", skinPalette: " + skinPalette +
                        ", mods: " + mods
                );

                conflictFound = true;
            }
        }

        if (!conflictFound) {
            System.out.println("No conflicting skin palettes found\n");
        }
    }

    public void resolveConflicts() {

        File file = new File(RESULTS_FILE);

        if (!file.exists()) {
            System.out.println("!| workshopMirror.txt not found. Run a scan first.");
            return;
        }

        Map<String, List<PaletteConflict>> paletteMap = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {

            String line;

            while ((line = reader.readLine()) != null) {

                if (!line.contains("skinPalette:")) continue;

                String[] parts = line.split(", ");
                if (parts.length < 3) continue;

                String modName = parts[0].replace("mod: ", "").trim();
                String heroClass = parts[1].replace("hero: ", "").trim();
                String paletteName = parts[2].replace("skinPalette: ", "").trim();

                PaletteConflict conflict = new PaletteConflict(
                        workshopPath,
                        modName,
                        heroClass,
                        paletteName
                );

                String key = heroClass + ":" + paletteName;

                paletteMap
                        .computeIfAbsent(key, k -> new ArrayList<>())
                        .add(conflict);
            }

        } catch (Exception e) {
            System.out.println("!| Failed reading workshopMirror.txt: " + e.getMessage());
            return;
        }

        for (Map.Entry<String, List<PaletteConflict>> entry : paletteMap.entrySet()) {

            List<PaletteConflict> conflicts = entry.getValue();

            if (conflicts.size() <= 1) continue;

            conflicts.sort(Comparator.comparingInt(PaletteConflict::getModNumber));

            PaletteConflict base = conflicts.get(0);

            System.out.println("Base palette (kept): " + base);

            for (int i = 1; i < conflicts.size(); i++) {

                PaletteConflict duplicate = conflicts.get(i);

                duplicate.renameWithPrefix("ren" + i + "_");
            }
        }

        System.out.println("Conflicts fixed");
    }

    private void cleanupPrefixIfOriginalExists(String modName, String heroClass, String paletteName) {

        File heroClassFolder = new File(
                workshopPath + File.separator +
                modName + File.separator +
                "heroes" + File.separator +
                heroClass
        );

        if (!heroClassFolder.exists()) return;

        File originalFolder = new File(heroClassFolder, paletteName);

        if (!originalFolder.exists()) return;

        File[] folders = heroClassFolder.listFiles(File::isDirectory);

        if (folders == null) return;

        for (File f : folders) {

            if (f.getName().matches("ren\\d+_" + paletteName)) {

                deleteFolder(f);

                System.out.println("Deleted obsolete prefixed folder: "
                        + f.getAbsolutePath());
            }
        }

        File modFiles = new File(
                workshopPath + File.separator +
                modName + File.separator +
                "modfiles.txt"
        );

        if (modFiles.exists()) {

            try {

                List<String> lines = Files.readAllLines(
                        modFiles.toPath(),
                        StandardCharsets.UTF_8
                );

                for (int i = 0; i < lines.size(); i++) {

                    lines.set(
                            i,
                            lines.get(i).replaceAll("ren\\d+_" + paletteName, paletteName)
                    );
                }

                Files.write(modFiles.toPath(), lines, StandardCharsets.UTF_8);

            } catch (IOException e) {

                System.out.println("!| Failed updating modfiles.txt for mod: " + modName);
            }
        }
    }

    private void deleteFolder(File folder) {

        File[] files = folder.listFiles();

        if (files != null) {

            for (File f : files) {

                if (f.isDirectory()) deleteFolder(f);
                else f.delete();
            }
        }

        folder.delete();
    }

}
