package com.butchercircus.skinmanager;

public class App {

    private static boolean debugMode = false;

    public static void main(String[] args) {

        CatchInput input = new CatchInput();
        Config config = new Config();
        String userInput;
        int i = 0;

        System.out.println("\n=== SkinManager v0.1.0 beta ===");

        // Load or ask for root path
        if (!config.load()) {
            String root = input.stringValue(
                "?| Enter Darkest Dungeon root path (.../Steam/steamapps/common/DarkestDungeon): "
            );
            config.derivePathsFromRoot(root);
            config.save();
        }

        // CLI loop
        do {
            debugPrint("\nDEBUG| Workshop mods path='" + config.getWorkshopPath() + "'");
            debugPrint("DEBUG| Butcher circus DLC heroes path='" + config.getArenaHeroesPath() + "'");

            i++;
            userInput = input.stringValue(
                "\n--------------------------MODE--------------------------\n" +
                i + " | 'debug_mode' -> toggle debug mode (currently: " + (debugMode ? "ON" : "OFF") + ")\n" +
                "--------------------------------------------------------\n" +
                "-----------------------MANAGE-SKINS---------------------\n" +
                "  | 'scan' -> check all workshop mods for DD\n" +
                "  | 'check_conflicts' -> list conflicting skin palettes\n" +
                "  | 'resolve_conflicts' -> rename conflicting skin palettes\n" +
                "--------------------------------------------------------\n" +
                "-----------SKINS+BUTCHER_CIRCUS-COMPATIBILITY-----------\n" +
                "  | 'assign_corpses' -> assigns corpses for skins in BC\n" +
                "  | 'remove_corpses' -> undoes corpse assignment\n" +
                "--------------------------------------------------------\n" +
                "  | 'exit' -> quit\n\n\n"
            );

            switch (userInput.toLowerCase()) {

                case "debug_mode" -> {
                    debugMode = !debugMode;
                    System.out.println("\n | Debug mode is now " + (debugMode ? "ON\n" : "OFF\n"));
                }

                case "scan" -> {
                    ScannerTool scannerTool = new ScannerTool(debugMode);
                    scannerTool.scanWorkshop(config.getWorkshopPath());
                }

                case "check_conflicts" -> {
                    ConflictSolver solver = new ConflictSolver(config.getWorkshopPath());
                    solver.checkConflicts();
                }

                case "resolve_conflicts" -> {
                    ConflictSolver solver = new ConflictSolver(config.getWorkshopPath());
                    solver.resolveConflicts();
                }

                case "assign_corpses" -> {
                    String assignment_mode = input.stringValue(
                        "\n?| Choose assignment mode ('safe'|'custom'): "
                    );

                    if("custom".equals(assignment_mode))
                    { System.out.println("!| Assignment mode 'custom' not implemented yet");
                    } else {
                        CorpseManager manager = new CorpseManager(debugMode, config.getArenaHeroesPath(), config.getWorkshopPath());
                        manager.assignCorpses(assignment_mode);
                    }
                }

                case "remove_corpses" -> {
                    CorpseManager manager = new CorpseManager(debugMode, config.getArenaHeroesPath(), config.getWorkshopPath());
                    manager.removeCorpses();
                }

                case "exit" -> { }

                default -> System.out.println("!| Unknown command");
            }

        } while (!userInput.equalsIgnoreCase("exit"));

        System.out.println("\nExiting...");
    }

    public static void debugPrint(String message) {
        if (debugMode) {
            System.out.println(message);
        }
    }
}