package de.hszg.learner.K_X_Means;

import java.io.*;
import java.util.*;

public class DataLoader {
    private String csvFilePath;
    private int randomSeed;

    public DataLoader(String csvFilePath, int randomSeed) {
        this.csvFilePath = csvFilePath;
        this.randomSeed = randomSeed;
    }

    public DataSet loadData(double trainSplitRatio) throws IOException {
        // Alle Zeilen aus der CSV-Datei lesen
        List<String> lines = readCSVFile(csvFilePath);

        // Daten mischen
        Collections.shuffle(lines, new Random(randomSeed));

        // Daten und Labels extrahieren
        List<double[]> data = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        for (String line : lines) {
            String[] parts = line.split(",", 2);
            if (parts.length < 2) {
                System.err.println("Ungültige Zeile: " + line);
                continue;
            }
            labels.add(parts[0]); // Label vor dem ersten Komma
            data.add(parseFeatures(parts[1])); // Features nach dem ersten Komma
        }

        // Trainings- und Testdaten aufteilen
        int trainSize = (int) (data.size() * trainSplitRatio);
        List<double[]> trainData = data.subList(0, trainSize);
        List<String> trainLabels = labels.subList(0, trainSize);

        List<double[]> testData = data.subList(trainSize, data.size());
        List<String> testLabels = labels.subList(trainSize, labels.size());

        // Rückgabe der Sets
        return new DataSet(trainData, trainLabels, testData, testLabels);
    }

    private List<String> readCSVFile(String filePath) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines;
    }

    private double[] parseFeatures(String featureString) {
        String[] featureParts = featureString.split(",");
        double[] features = new double[featureParts.length];
        for (int i = 0; i < featureParts.length; i++) {
            features[i] = Double.parseDouble(featureParts[i].trim());
        }
        return features;
    }

    // Klasse für die Rückgabe der Datensets
    public static class DataSet {
        public final List<double[]> trainData;
        public final List<String> trainLabels;
        public final List<double[]> testData;
        public final List<String> testLabels;

        public DataSet(List<double[]> trainData, List<String> trainLabels,
                       List<double[]> testData, List<String> testLabels) {
            this.trainData = trainData;
            this.trainLabels = trainLabels;
            this.testData = testData;
            this.testLabels = testLabels;
        }
    }
}
