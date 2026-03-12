package com.butchercircus.skinmanager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class PaletteConflict {

    private final String workshopPath;
    private final String modName;
    private final String heroClass;
    private String paletteName;

    public PaletteConflict(String workshopPath, String modName, String heroClass, String paletteName) {
        this.workshopPath = workshopPath;
        this.modName = modName;
        this.heroClass = heroClass;
        this.paletteName = paletteName;
    }

    public int getModNumber() {

        try {
            String modNumStr = modName.split("_")[0];
            return Integer.parseInt(modNumStr);
        } catch (NumberFormatException e) {
            return Integer.MAX_VALUE;
        }
    }

    public void renameWithPrefix(String prefix) {

        String newPaletteName = prefix + paletteName;

        File paletteFolder = new File(
                workshopPath + File.separator +
                modName + File.separator +
                "heroes" + File.separator +
                heroClass + File.separator +
                paletteName
        );

        File newFolder = new File(
                workshopPath + File.separator +
                modName + File.separator +
                "heroes" + File.separator +
                heroClass + File.separator +
                newPaletteName
        );

        if (!paletteFolder.exists()) {

            System.out.println("!| Palette folder not found: "
                    + paletteFolder.getAbsolutePath());

            return;
        }

        boolean success = paletteFolder.renameTo(newFolder);

        if (!success) {

            System.out.println("!| Failed to rename palette folder: "
                    + paletteFolder.getAbsolutePath());

            return;
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
                            lines.get(i).replace(paletteName, newPaletteName)
                    );
                }

                Files.write(modFiles.toPath(), lines, StandardCharsets.UTF_8);

            } catch (IOException e) {

                System.out.println("!| Failed updating modfiles.txt for mod: "
                        + modName);
            }
        }

        System.out.println(
                "Renamed: " + paletteName +
                " -> " + newPaletteName +
                " in mod: " + modName
        );

        paletteName = newPaletteName;
    }

    @Override
    public String toString() {

        return "hero: " + heroClass +
                ", palette: " + paletteName +
                ", mod: " + modName;
    }

}
