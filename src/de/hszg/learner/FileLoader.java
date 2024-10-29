package de.hszg.learner;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileLoader {
    public static void main(String[] args) {
        String rootFolderPath = "C:\\path\\to\\your\\folder"; // Pfad zum Hauptordner anpassen
        int maxFiles = 5; // Anzahl der einzulesenden Dateien; setze auf -1, um alle Dateien einzulesen

        Map<String, List<File>> filesByFolder = loadFilesByFolder(rootFolderPath, maxFiles);

        // Ausgabe der eingelesenen Dateien pro Stammordner
        filesByFolder.forEach((folderName, fileList) -> {
            System.out.println("Ordner: " + folderName);
            fileList.forEach(file -> System.out.println("  Datei: " + file.getName()));
        });
    }

    /**
     * Lädt Dateien aus Stammordnern und ihren Unterordnern und gibt sie in einer Map zurück.
     *
     * @param rootFolderPath Pfad zum Hauptordner, der alle Stammordner enthält
     * @param maxFiles       maximale Anzahl der Dateien, die pro Stammordner geladen werden sollen (-1 für alle Dateien)
     * @return eine Map, in der jeder Stammordner als Schlüssel und seine Dateien als Wert gespeichert sind
     */
    public static Map<String, List<File>> loadFilesByFolder(String rootFolderPath, int maxFiles) {
        Map<String, List<File>> filesByFolder = new HashMap<>();
        File rootFolder = new File(rootFolderPath);

        if (!rootFolder.isDirectory()) {
            System.err.println("Pfad ist kein Verzeichnis: " + rootFolderPath);
            return filesByFolder;
        }

        File[] mainFolders = rootFolder.listFiles(File::isDirectory);
        if (mainFolders != null) {
            for (File mainFolder : mainFolders) {
                List<File> filesList = new ArrayList<>();
                loadFilesRecursively(mainFolder, filesList, maxFiles);
                filesByFolder.put(mainFolder.getName(), filesList);
            }
        }
        return filesByFolder;
    }

    /**
     * Lädt rekursiv Dateien aus dem angegebenen Ordner und fügt sie zur Liste hinzu.
     *
     * @param folder   der Ordner, dessen Dateien geladen werden sollen
     * @param filesList Liste, der Dateien hinzugefügt werden sollen
     * @param maxFiles maximale Anzahl der Dateien, die geladen werden sollen (-1 für alle Dateien)
     */
    private static void loadFilesRecursively(File folder, List<File> filesList, int maxFiles) {
        if (maxFiles != -1 && filesList.size() >= maxFiles) return;

        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    loadFilesRecursively(file, filesList, maxFiles);
                } else {
                    filesList.add(file);
                    if (maxFiles != -1 && filesList.size() >= maxFiles) break;
                }
            }
        }
    }
}
