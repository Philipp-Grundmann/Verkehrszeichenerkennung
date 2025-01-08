import smile.clustering.KMeans;
import smile.plot.swing.ScatterPlot;
import smile.plot.swing.Canvas;
import java.awt.Color;
public class test {

    public static void main(String[] args) {
        // Beispiel-Datensatz
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

        // Anzahl der Cluster
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

        // Visualisierung vorbereiten
        Color[] colors = {Color.RED, Color.BLUE, Color.GREEN, Color.ORANGE, Color.MAGENTA}; // Mehr Farben hinzufügen, wenn nötig
        Color[] pointColors = new Color[data.length];
        for (int i = 0; i < data.length; i++) {
            pointColors[i] = colors[kmeans.y[i] % colors.length];
        }

        // Streudiagramm zeichnen
        ScatterPlot plot = ScatterPlot.of(data, pointColors);

        // Canvas anzeigen
        Canvas canvas = new Canvas(plot);
        canvas.setTitle("K-Means Clustering");
        canvas.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        canvas.show();
    }
}

