package de.hszg.learner;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;

public class MyDataCreatorV2 {

    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        /*//Pfad Philipp Laptop
		new MyDataCreator(
				"C:\\Users\\Philipp\\Documents\\Master\\Maschinelles Lernen\\Verkehrszeichen",
				"C:\\Users\\Philipp\\Documents\\Master\\Maschinelles Lernen\\Verkehrszeichenerkennung\\ergebnisse 14.01.2025",
				7,7);*/
        //Pfad Philipp Festrechner
        new MyDataCreator(
                "D:\\1.2_Master\\IdeaProjects\\Verkehrszeichen",
                ".\\ergebnisse",
                7,7);
        //Pfad Vincent
		/*new MyDataCreator(
				"C:\\Users\\Philipp\\Documents\\Master\\Maschinelles Lernen\\Verkehrszeichen",
				"C:\\Users\\Philipp\\Documents\\Master\\Maschinelles Lernen\\Verkehrszeichenerkennung\\ergebnisse 14.01.2025",
				7,7);*/
    }

    MyDataCreatorV2(String FolderPathnameImages, String FolderPathnameData, int gridCols, int gridRows) {
        List<FeatureVector> AllFeatureVektores = new LinkedList<>();

        Map<File, Concept> fileToConceptMap = loadFilesByFolder(FolderPathnameImages, 500);
        int sum = fileToConceptMap.size();

        if (fileToConceptMap.isEmpty()) {
            System.out.println("Der Ordner der Bilddaten ist leer oder existiert nicht.");
        } else {
            // Parallelisierte Verarbeitung der Bilder
            fileToConceptMap.entrySet().parallelStream().forEach(entry -> {
                File file = entry.getKey();
                Concept concept = entry.getValue();

                if (file.isFile() && isImageFile(file)) {
                    Mat image = Imgcodecs.imread(file.getAbsolutePath());
                    if (!image.empty()) {
                        // Bild wird verarbeitet und Featurevektor wird der Liste hinzugefügt
                        ImageProcessor myImageProcessor = new ImageProcessor(image, concept, gridCols, gridRows);
                        FeatureVector myFeatureVector = myImageProcessor.getOutput_Vektor();

                        synchronized (AllFeatureVektores) { // Thread-sicheres Hinzufügen
                            AllFeatureVektores.add(myFeatureVector);
                        }
                    } else {
                        System.out.println("Fehler beim Laden des Bildes: " + file.getName());
                    }
                }
            });
        }

        String nametag = "_S" + gridCols + "xZ" + gridRows;

        // Speichern der Featurevektoren
        saveManyFeatureVektores(AllFeatureVektores.toArray(new FeatureVector[0]), FolderPathnameData, nametag);
    }

    public static Map<File, Concept> loadFilesByFolder(String rootFolderPath, int maxFiles) {
        Map<Concept, List<File>> filesByFolder = new HashMap<>();
        Map<File, Concept> fileToConceptMap = new HashMap<>();

        File rootFolder = new File(rootFolderPath);
        if (!rootFolder.isDirectory()) {
            System.err.println("Pfad ist kein Verzeichnis: " + rootFolderPath);
            return fileToConceptMap;
        }

        File[] mainFolders = rootFolder.listFiles(File::isDirectory);
        if (mainFolders != null) {
            for (File mainFolder : mainFolders) {
                List<File> filesList = new ArrayList<>();
                loadFilesRecursiv(mainFolder, filesList, maxFiles);
                filesByFolder.put(mainFolderName_StrToConcept(mainFolder.getName()), filesList);
            }
        }

        for (Map.Entry<Concept, List<File>> entry : filesByFolder.entrySet()) {
            Concept concept = entry.getKey();
            List<File> files = entry.getValue();

            for (File file : files) {
                fileToConceptMap.put(file, concept);
            }
        }

        return fileToConceptMap;
    }

    public static Concept mainFolderName_StrToConcept(String MainFolderName) {
        switch (MainFolderName) {
            case "FAHRTRICHTUNG_LINKS": return Concept.Fahrtrichtung_links;
            case "VORFAHRTSSTRASSE": return Concept.Vorfahrtsstraße;
            case "STOP": return Concept.Stoppschild;
            case "VORFAHRT_GEWAEHREN": return Concept.Vorfahrt_gewähren;
            case "FAHRTRICHTUNG_RECHTS": return Concept.Fahrtrichtung_rechts;
            case "VORFAHRT_VON_RECHTS": return Concept.Vorfahrt_von_Rechts;
            default:
                System.out.println("Das Verkehrszeichen konnte nicht identifiziert werden!");
                return Concept.Unknown;
        }
    }

    private static void loadFilesRecursiv(File folder, List<File> filesList, int maxFiles) {
        File[] subFiles = folder.listFiles();
        subFiles = shuffleArrayWithSeed(subFiles, 6438);

        if (subFiles != null && containsOnlyFiles(subFiles)) {
            int fileCount = 0;
            for (File file : subFiles) {
                if (file.isFile()) {
                    filesList.add(file);
                    fileCount++;
                    if (maxFiles != -1 && fileCount >= maxFiles) {
                        break;
                    }
                }
            }
        } else if (subFiles != null) {
            for (File subFile : subFiles) {
                if (subFile.isDirectory()) {
                    loadFilesRecursiv(subFile, filesList, maxFiles);
                }
            }
        }
    }

    private static boolean containsOnlyFiles(File[] files) {
        for (File file : files) {
            if (file.isDirectory()) {
                return false;
            }
        }
        return true;
    }

    public static String generateDateTimeforFilename() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        return "_" + now.format(formatter);
    }

    public static boolean isImageFile(File file) {
        String fileName = file.getName().toLowerCase();
        return fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png") || fileName.endsWith(".bmp");
    }

    public void saveManyFeatureVektores(FeatureVector[] ArrayOfAllFeaturevektores, String FolderPathnameData, String nametag) {
        String filename = FolderPathnameData + "\\VektorData" + generateDateTimeforFilename() + nametag + ".dat";

        List<FeatureVector> res = new LinkedList<>();
        Collections.addAll(res, ArrayOfAllFeaturevektores);
        try (ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(filename)))) {
            out.writeObject(res);
        } catch (Throwable t) {
            System.out.println("Fehler beim Erstellen der Datei: " + filename);
            t.printStackTrace();
        }
    }

    public static File[] shuffleArrayWithSeed(File[] files, int randomSeed) {
        List<File> filesList = new ArrayList<>(Arrays.asList(files));
        Collections.shuffle(filesList, new Random(randomSeed));
        return filesList.toArray(new File[0]);
    }
}
