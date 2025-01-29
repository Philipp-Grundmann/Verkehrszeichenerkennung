package de.hszg.learner.K_X_Means;
import smile.clustering.KMeans;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class K_Means_Modell {

    private final int numClusters;
    private final int maxIterations;
    private final long randomSeed;
    private final String silhouetteOutputPath;

    public K_Means_Modell(int numClusters, int maxIterations, long randomSeed, String silhouetteOutputPath) {
        this.numClusters = numClusters;
        this.maxIterations = maxIterations;
        this.randomSeed = randomSeed;
        this.silhouetteOutputPath = silhouetteOutputPath;
    }

    public List<Cluster> run(List<double[]> trainingSetList, List<String> trafficSignLabels, String outputPath, List<String> testLabels, List<double[]> testData) {
        double[][] trainingSet = convertListToArray(trainingSetList);

        System.out.println("Starte K-Means-Clustering mit " + numClusters + " Clustern...");
        KMeans kmeans = KMeans.fit(trainingSet, numClusters, maxIterations, randomSeed);

        double[][] centroids = kmeans.centroids;
        System.out.println("Gefundene Cluster = " + centroids.length);

        // Cluster den Klassen zuweisen
        Map<Integer, String> clusterToClassMap = classifyClusters(kmeans.y, trainingSetList, trafficSignLabels);

        // Cluster zusammenfassen
        List<Cluster> mergedClusters = mergeSimilarClusters(centroids, clusterToClassMap);


        // Ergebnisse ausgeben
        System.out.println("Neue Anzahl der Cluster nach Zusammenfassung: " + mergedClusters.size());
        for (int i = 0; i < mergedClusters.size(); i++) {
            System.out.printf("Cluster %d -> Klasse: %s%n", i, mergedClusters.get(i).assignedClass);
        }

        // Ergebnisse speichern
        saveResultsToCSV(mergedClusters, outputPath);
        System.out.println("K-Means-Clustering abgeschlossen. Ergebnisse wurden in: " + outputPath + " gespeichert.");


        // Silhouette-Koeffizient berechnen
        double silhouetteCoefficient = SilhouetteCoefficient.calculateSilhouette(trainingSet, kmeans.y);
        System.out.println("Silhouette-Koeffizient: " + silhouetteCoefficient);

        saveSilhouetteResultsToCSV(silhouetteCoefficient, silhouetteOutputPath);

        // Ergebnisse speichern
        saveResultsToCSV(mergedClusters, outputPath);
        System.out.println("X-Means-Clustering abgeschlossen. Ergebnisse wurden in: " + outputPath + " gespeichert.");
        return mergedClusters;

    }


    private Map<Integer, String> classifyClusters(int[] clusterLabels, List<double[]> trainingSetList, List<String> trafficSignLabels) {
        Map<Integer, Map<String, Integer>> clusterClassCounts = new HashMap<>();

        // Zähle Klassen pro Cluster
        for (int i = 0; i < clusterLabels.length; i++) {
            int cluster = clusterLabels[i];
            String signClass = trafficSignLabels.get(i);
            clusterClassCounts.putIfAbsent(cluster, new HashMap<>());
            clusterClassCounts.get(cluster).put(signClass, clusterClassCounts.get(cluster).getOrDefault(signClass, 0) + 1);
        }

        // Bestimme die Klasse mit den meisten Stimmen für jeden Cluster
        Map<Integer, String> clusterToClassMap = new HashMap<>();
        for (Map.Entry<Integer, Map<String, Integer>> entry : clusterClassCounts.entrySet()) {
            int cluster = entry.getKey();
            Map<String, Integer> classCounts = entry.getValue();

            String dominantClass = classCounts.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .get()
                    .getKey();
            clusterToClassMap.put(cluster, dominantClass);
        }

        // Ausgabe der Klassifizierung
        for (Map.Entry<Integer, String> entry : clusterToClassMap.entrySet()) {
            System.out.printf("Cluster %d -> Klasse: %s%n", entry.getKey(), entry.getValue());
        }
        return clusterToClassMap;
    }

    private double calculateAverageDistance(double[][] centroids) {
        double totalDistance = 0.0;
        int count = 0;

        for (int i = 0; i < centroids.length; i++) {
            for (int j = i + 1; j < centroids.length; j++) {
                totalDistance += euclideanDistance(centroids[i], centroids[j]);
                count++;
            }
        }

        return totalDistance / count;
    }

    private List<Cluster> mergeSimilarClusters(double[][] centroids, Map<Integer, String> clusterToClassMap) {
        System.out.println("Fasse ähnliche Cluster basierend auf Zentroiden zusammen...");

        List<Cluster> clusters = new ArrayList<>();
        for (int i = 0; i < centroids.length; i++) {
            clusters.add(new Cluster(centroids[i], clusterToClassMap.get(i)));
        }

        double averageDistance = calculateAverageDistance(centroids);
        double similarityThreshold = averageDistance * 0.5;

        List<Cluster> mergedClusters = new ArrayList<>();
        boolean[] merged = new boolean[centroids.length];

        for (int i = 0; i < clusters.size(); i++) {
            if (merged[i]) continue;

            Cluster currentCluster = clusters.get(i);
            List<Cluster> similarClusters = new ArrayList<>();
            similarClusters.add(currentCluster);

            for (int j = i + 1; j < clusters.size(); j++) {
                if (!merged[j]) {
                    double distance = euclideanDistance(currentCluster.centroid, clusters.get(j).centroid);

                    if (distance <= similarityThreshold) {
                        System.out.printf("Cluster %d und Cluster %d werden zusammengefasst.%n", i, j);
                        similarClusters.add(clusters.get(j));
                        merged[j] = true;
                    }
                }
            }

            Cluster mergedCluster = combineClusters(similarClusters);
            mergedClusters.add(mergedCluster);
        }

        return mergedClusters;
    }

    private Cluster combineClusters(List<Cluster> similarClusters) {
        int dimensions = similarClusters.get(0).centroid.length;
        double[] combinedCentroid = new double[dimensions];
        Map<String, Integer> classVotes = new HashMap<>();

        for (Cluster cluster : similarClusters) {
            for (int d = 0; d < dimensions; d++) {
                combinedCentroid[d] += cluster.centroid[d];
            }

            classVotes.put(cluster.assignedClass, classVotes.getOrDefault(cluster.assignedClass, 0) + 1);
        }

        for (int d = 0; d < dimensions; d++) {
            combinedCentroid[d] /= similarClusters.size();
        }

        String dominantClass = classVotes.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .get()
                .getKey();

        return new Cluster(combinedCentroid, dominantClass);
    }

    private double euclideanDistance(double[] point1, double[] point2) {
        double sum = 0;
        for (int i = 0; i < point1.length; i++) {
            sum += Math.pow(point1[i] - point2[i], 2);
        }
        return Math.sqrt(sum);
    }

    private void saveResultsToCSV(List<Cluster> clusters, String outputPath) {
        try (FileWriter writer = new FileWriter(outputPath)) {
            writer.write("Cluster,AssignedClass,Centroid\n");

            for (int i = 0; i < clusters.size(); i++) {
                Cluster cluster = clusters.get(i);
                writer.write(i + "," + cluster.assignedClass + "," + Arrays.toString(cluster.centroid).replaceAll("[\\[\\] ]", "") + "\n");
            }

            System.out.println("Ergebnisse erfolgreich in die CSV-Datei geschrieben.");

        } catch (IOException e) {
            System.err.println("Fehler beim Schreiben der CSV-Datei: " + e.getMessage());
        }
    }

    private double[][] convertListToArray(List<double[]> list) {
        return list.toArray(new double[0][0]);
    }

    private static class Cluster {
        double[] centroid;
        String assignedClass;

        Cluster(double[] centroid, String assignedClass) {
            this.centroid = centroid;
            this.assignedClass = assignedClass;
        }
    }

    private void saveSilhouetteResultsToCSV(double silhouetteCoefficient, String outputPath) {
        try (FileWriter writer = new FileWriter(outputPath, true)) {
            // Kopfzeile hinzufügen, falls die Datei leer ist
            File file = new File(outputPath);
            if (file.length() == 0) {
                writer.write("Timestamp,SilhouetteCoefficient\n");
            }

            // Silhouette-Wert mit Zeitstempel speichern
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            writer.write(String.format("%s,%.4f\n", timestamp, silhouetteCoefficient));

            System.out.println("Silhouette-Ergebnisse erfolgreich in die CSV-Datei geschrieben.");
        } catch (IOException e) {
            System.err.println("Fehler beim Schreiben der Silhouette-Ergebnisse in die CSV-Datei: " + e.getMessage());
        }
    }

}