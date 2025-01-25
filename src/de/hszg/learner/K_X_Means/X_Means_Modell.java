package de.hszg.learner.K_X_Means;

import smile.clustering.XMeans;
import de.hszg.learner.K_X_Means.SilhouetteCoefficient;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class X_Means_Modell {

    private final int kMax;
    private final int maxIterations;
    private final long randomSeed;

    public X_Means_Modell(int kMax, int maxIterations, long randomSeed) {
        this.kMax = kMax;
        this.maxIterations = maxIterations;
        this.randomSeed = randomSeed;
    }

    public void run(List<double[]> trainingSetList, List<String> trafficSignLabels, String outputPath) {
        double[][] trainingSet = convertListToArray(trainingSetList);

        System.out.println("Starte X-Means-Clustering mit maximal " + kMax + " Clustern...");
        XMeans xmeans = XMeans.fit(trainingSet, kMax, maxIterations, randomSeed);

        double[][] centroids = xmeans.centroids;
        System.out.println("Gefundene Cluster = " + centroids.length);

        // Cluster den Klassen zuweisen
        Map<Integer, String> clusterToClassMap = classifyClusters(xmeans.y, trainingSetList, trafficSignLabels);

        // Cluster zusammenfassen
        List<Cluster> mergedClusters = mergeSimilarClusters(centroids, clusterToClassMap);

        // Ergebnisse ausgeben
        System.out.println("Neue Anzahl der Cluster nach Zusammenfassung: " + mergedClusters.size());
        for (int i = 0; i < mergedClusters.size(); i++) {
            System.out.printf("Cluster %d -> Klasse: %s%n", i, mergedClusters.get(i).assignedClass);
        }

        // Silhouette-Koeffizient berechnen
        double silhouetteCoefficient = SilhouetteCoefficient.calculateSilhouette(trainingSet, xmeans.y);
        System.out.println("Silhouette-Koeffizient: " + silhouetteCoefficient);

        // Ergebnisse speichern
        saveResultsToCSV(mergedClusters, outputPath);
        System.out.println("X-Means-Clustering abgeschlossen. Ergebnisse wurden in: " + outputPath + " gespeichert.");
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
        double similarityThreshold = averageDistance * 1.1;

        List<Cluster> mergedClusters = new ArrayList<>(clusters);
        boolean[] merged = new boolean[centroids.length];
        int previousClusterCount = mergedClusters.size();
        boolean anyChanges;

        do {
            anyChanges = false; // Reset flag for each iteration

            // Für jedes Cluster überprüfen und zusammenfassen
            List<Cluster> newMergedClusters = new ArrayList<>();
            boolean[] newlyMerged = new boolean[centroids.length];

            for (int i = 0; i < mergedClusters.size(); i++) {
                if (newlyMerged[i]) continue;

                Cluster currentCluster = mergedClusters.get(i);
                List<Cluster> similarClusters = new ArrayList<>();
                similarClusters.add(currentCluster);

                for (int j = i + 1; j < mergedClusters.size(); j++) {
                    if (!newlyMerged[j]) {
                        Cluster otherCluster = mergedClusters.get(j);
                        double distance = euclideanDistance(currentCluster.centroid, otherCluster.centroid);
                        if (distance <= similarityThreshold && currentCluster.assignedClass.equals(otherCluster.assignedClass)) {
                            System.out.printf("Cluster %d und Cluster %d werden zusammengefasst (Klasse: %s).%n", i, j, currentCluster.assignedClass);
                            similarClusters.add(otherCluster);
                            newlyMerged[j] = true;
                            anyChanges = true;  // Änderungen wurden gemacht
                        }
                    }
                }

                if (similarClusters.size() > 1) {
                    Cluster mergedCluster = combineClusters(similarClusters);
                    newMergedClusters.add(mergedCluster);
                } else {
                    newMergedClusters.add(currentCluster);
                }
            }

            mergedClusters = new ArrayList<>(newMergedClusters);

            // Abbruch, wenn keine Cluster mehr zusammengefasst wurden
            if (previousClusterCount == mergedClusters.size()) {
                System.out.println("Keine Cluster mehr zusammengefasst. Abbruch, da keine Veränderung der Anzahl.");
                break;
            }

            // Überprüfen, ob alle Klassen mindestens ein Cluster haben
            if (!hasAllClassesClusters(mergedClusters, clusterToClassMap)) {
                System.out.println("Abbruch, da nicht für jede Klasse ein Cluster vorhanden ist.");
                break;
            }

            previousClusterCount = mergedClusters.size();

        } while (anyChanges);

        System.out.println("Clusterzusammenführung abgeschlossen.");
        return mergedClusters;
    }

    // Hilfsmethode zur Überprüfung, ob für jede Klasse mindestens ein Cluster existiert
    private boolean hasAllClassesClusters(List<Cluster> mergedClusters, Map<Integer, String> clusterToClassMap) {
        Set<String> classesInClusters = new HashSet<>();
        for (Cluster cluster : mergedClusters) {
            classesInClusters.add(cluster.assignedClass);
        }

        Set<String> allClasses = new HashSet<>(clusterToClassMap.values());
        return allClasses.equals(classesInClusters);
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
}