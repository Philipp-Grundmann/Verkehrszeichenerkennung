package de.hszg.learner.K_X_Means;

import java.io.*;
import java.util.*;

public class ClusterCSVLoader {
    private final String csvFilePath;

    public ClusterCSVLoader(String csvFilePath) {
        this.csvFilePath = csvFilePath;
    }

    public List<Cluster> loadClusters() throws IOException {
        List<Cluster> clusters = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", 3);
                if (parts.length < 3) {
                    System.err.println("Ungültige Zeile: " + line);
                    continue;
                }

                try {
                    // Klasse und zugewiesene Klasse auslesen
                    int clusterNumber = Integer.parseInt(parts[0].trim());
                    String assignedClass = parts[1].trim();

                    // Zentroid-Werte auslesen
                    double[] centroid = parseCentroid(parts[2]);

                    // Cluster-Objekt erstellen und hinzufügen
                    clusters.add(new Cluster(clusterNumber, assignedClass, centroid));
                } catch (NumberFormatException e) {
                    System.err.println("Fehler beim Parsen der Cluster-Daten: " + line);
                }
            }
        }

        return clusters;
    }

    private double[] parseCentroid(String centroidString) {
        String[] parts = centroidString.split(",");
        double[] centroid = new double[parts.length];
        for (int i = 0; i < parts.length; i++) {
            centroid[i] = Double.parseDouble(parts[i].trim());
        }
        return centroid;
    }

    public static class Cluster {
        private final int clusterNumber;
        private final String assignedClass;
        private final double[] centroid;

        public Cluster(int clusterNumber, String assignedClass, double[] centroid) {
            this.clusterNumber = clusterNumber;
            this.assignedClass = assignedClass;
            this.centroid = centroid;
        }

        public int getClusterNumber() {
            return clusterNumber;
        }

        public String getAssignedClass() {
            return assignedClass;
        }

        public double[] getCentroid() {
            return centroid;
        }
    }
}
