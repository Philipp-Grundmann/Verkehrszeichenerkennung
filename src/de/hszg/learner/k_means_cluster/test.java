package de.hszg.learner.k_means_cluster;

import smile.clustering.KMeans;
import java.util.Arrays;

public class test {

    public static void main(String[] args) {


                // Gegebenes Set von Merkmalsvektoren (z. B. 2D-Vektoren)
                double[][] featureVectors = {
                        {1.0, 2.0},
                        {1.5, 1.8},
                        {5.0, 8.0},
                        {8.0, 8.0},
                        {1.0, 0.6},
                        {9.0, 11.0},
                        {8.0, 2.0},
                        {10.0, 2.0},
                        {9.0, 3.0}
                };

                // Anzahl der Cluster, die gefunden werden sollen
                int k = 6;

                // k-Means ausführen
                KMeans kmeans = KMeans.fit(featureVectors, k);

                // Ergebnisse anzeigen
                System.out.println("Cluster-Zentren:");
                Arrays.stream(kmeans.centroids).forEach(center ->
                        System.out.printf("Center: [%.2f, %.2f]%n", center[0], center[1])
                );

                System.out.println("\nCluster-Zuweisungen:");
                for (int i = 0; i < featureVectors.length; i++) {
                    System.out.printf("Vektor [%.2f, %.2f] -> Cluster %d%n", featureVectors[i][0], featureVectors[i][1], kmeans.y[i]);
                }
    }
}

