package de.hszg.learner.K_X_Means;
import smile.clustering.KMeans;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class K_Means_Modell {

    private final int numClusters;
    private final int maxIterations;
    private final long randomSeed;

    public K_Means_Modell(int numClusters, int maxIterations, long randomSeed) {
        this.numClusters = numClusters;
        this.maxIterations = maxIterations;
        this.randomSeed = randomSeed;
    }

    public void run(List<double[]> trainingSetList, String outputPath) {
        // Konvertiere Liste zu Array
        double[][] trainingSet = convertListToArray(trainingSetList);

        // K-Means Clustering
        System.out.println("Starte K-Means-Clustering mit " + numClusters + " Clustern...");
        KMeans kmeans = KMeans.fit(trainingSet, numClusters, maxIterations, randomSeed);

        // Ergebnisse speichern
        saveResultsToCSV(kmeans.y, trainingSet, outputPath);

        System.out.println("K-Means-Clustering abgeschlossen. Ergebnisse wurden in: " + outputPath + " gespeichert.");
    }

    private void saveResultsToCSV(int[] clusterLabels, double[][] trainingSet, String outputPath) {
        try (FileWriter writer = new FileWriter(outputPath)) {
            // CSV-Header
            writer.write("Cluster,Features\n");

            // Daten und Cluster-Labels schreiben
            for (int i = 0; i < trainingSet.length; i++) {
                StringBuilder line = new StringBuilder();
                line.append(clusterLabels[i]).append(",");
                line.append(Arrays.toString(trainingSet[i]).replaceAll("[\\[\\] ]", ""));
                writer.write(line.toString());
                writer.write("\n");
            }

            System.out.println("Ergebnisse erfolgreich in die CSV-Datei geschrieben.");

        } catch (IOException e) {
            System.err.println("Fehler beim Schreiben der CSV-Datei: " + e.getMessage());
        }
    }

    private double[][] convertListToArray(List<double[]> list) {
        return list.toArray(new double[0][0]);
    }
}