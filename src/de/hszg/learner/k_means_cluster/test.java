package de.hszg.learner.k_means_cluster;
import smile.clustering.KMeans;
import smile.plot.swing.ScatterPlot;
import java.awt.Color;
public class test {

        public static void main(String[] args) {
            // Beispiel-Datensatz: Zwei Cluster
            double[][] data = {
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

            // Anzahl der Cluster (z.B. 2)
            int k = 2;

            // k-Means ausführen
            KMeans kmeans = KMeans.fit(data, k);

            // Ergebnisse anzeigen
            System.out.println("Cluster-Zentren:");
            for (double[] center : kmeans.centroids) {
                System.out.printf("Center: [%.2f, %.2f]%n", center[0], center[1]);
            }

            System.out.println("\nCluster-Zuweisungen:");
            for (int i = 0; i < data.length; i++) {
                System.out.printf("Punkt [%.2f, %.2f] -> Cluster %d%n", data[i][0], data[i][1], kmeans.y[i]);
            }

            // Visualisierung (optional)
            ScatterPlot plot = ScatterPlot.of(data, kmeans.y);
            plot.setLegend(true);
            plot.window().setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
            plot.window();
        }
    }

