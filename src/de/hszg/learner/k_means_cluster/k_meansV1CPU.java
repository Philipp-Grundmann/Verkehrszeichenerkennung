package de.hszg.learner.k_means_cluster;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
public class k_meansV1CPU {

private int k; // Anzahl der Cluster
private int maxIterations; // Maximale Anzahl an Iterationen
private List<double[]> data; // Die Datenpunkte
private List<double[]> centroids; // Die Cluster-Zentren


    public k_meansV1CPU(int k, int maxIterations, List<double[]> data) {
        this.k = k;
        this.maxIterations = maxIterations;
        this.data = data;
        this.centroids = new ArrayList<>();
    }

    public static void main(String[] args) {
        try {
            // Pfad zur .dat-Datei
            String filePath = "D:\\1.2_Master\\IdeaProjects\\ML-3\\ergebnisse\\features_without_concept.csv";
            List<double[]> data = readDataFromCsv(filePath);


           // vincent pfade
           // String filePath = "D:\\1.2_Master\\IdeaProjects\\ML-3\\ergebnisse\\features_without_concept.csv";
           // List<double[]> data = readDataFromCsv(filePath);

            int k = 3; // Anzahl der Cluster
            int maxIterations = 100;

            k_meansV1CPU kMeans = new k_meansV1CPU(k, maxIterations, data);
            Map<double[], List<double[]>> clusters = kMeans.fit();

            // Ausgabe der Ergebnisse
            for (Map.Entry<double[], List<double[]>> entry : clusters.entrySet()) {
                System.out.println("Cluster-Zentrum: " + Arrays.toString(entry.getKey()));
                System.out.println("Punkte:");
                for (double[] point : entry.getValue()) {
                    System.out.println(Arrays.toString(point));
                }
            }
            String outputFilePath = "D:\\1.2_Master\\IdeaProjects\\ML-3\\ergebnisse\\kmeans_result.csv";
            writeClustersToCsv(outputFilePath, clusters);

            //vincent pfade

            //String outputFilePath = "D:\\1.2_Master\\IdeaProjects\\ML-3\\ergebnisse\\kmeans_result.csv";
            //writeClustersToCsv(outputFilePath, clusters);

        } catch (IOException | InterruptedException | ExecutionException e) {
            System.err.println("Fehler: " + e.getMessage());
        }

    }

    public Map<double[], List<double[]>> fit() throws InterruptedException, ExecutionException {
        initializeCentroids();
        Map<double[], List<double[]>> clusters = new ConcurrentHashMap<>();
        boolean converged = false;

        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        for (int iter = 0; iter < maxIterations && !converged; iter++) {
            clusters = assignClustersParallel(executor);
            List<double[]> newCentroids = computeNewCentroidsParallel(clusters, executor);
            converged = checkConvergence(newCentroids);
            centroids = newCentroids;
        }

        executor.shutdown();
        return clusters;
    }

    private void initializeCentroids() {
        Random random = new Random();
        Set<Integer> chosenIndices = new HashSet<>();
        while (centroids.size() < k) {
            int index = random.nextInt(data.size());
            if (!chosenIndices.contains(index)) {
                centroids.add(data.get(index));
                chosenIndices.add(index);
            }
        }
    }

    private Map<double[], List<double[]>> assignClustersParallel(ExecutorService executor) throws InterruptedException, ExecutionException {
        ConcurrentMap<double[], List<double[]>> clusters = new ConcurrentHashMap<>();
        for (double[] centroid : centroids) {
            clusters.put(centroid, Collections.synchronizedList(new ArrayList<>()));
        }

        List<Callable<Void>> tasks = data.stream()
                .map(point -> (Callable<Void>) () -> {
                    double[] nearestCentroid = findNearestCentroid(point);
                    clusters.get(nearestCentroid).add(point);
                    return null;
                })
                .collect(Collectors.toList());

        executor.invokeAll(tasks);
        return clusters;
    }

    private List<double[]> computeNewCentroidsParallel(Map<double[], List<double[]>> clusters, ExecutorService executor) throws InterruptedException, ExecutionException {
        List<Future<double[]>> futureCentroids = clusters.entrySet().stream()
                .map(entry -> executor.submit(() -> {
                    List<double[]> clusterPoints = entry.getValue();
                    if (!clusterPoints.isEmpty()) {
                        return calculateMean(clusterPoints);
                    }
                    return entry.getKey();
                }))
                .collect(Collectors.toList());

        List<double[]> newCentroids = new ArrayList<>();
        for (Future<double[]> future : futureCentroids) {
            newCentroids.add(future.get());
        }

        return newCentroids;
    }

    private boolean checkConvergence(List<double[]> newCentroids) {
        for (int i = 0; i < centroids.size(); i++) {
            if (!Arrays.equals(centroids.get(i), newCentroids.get(i))) {
                return false;
            }
        }
        return true;
    }

    private double[] findNearestCentroid(double[] point) {
        double minDistance = Double.MAX_VALUE;
        double[] nearestCentroid = null;

        for (double[] centroid : centroids) {
            double distance = euclideanDistance(point, centroid);
            if (distance < minDistance) {
                minDistance = distance;
                nearestCentroid = centroid;
            }
        }

        return nearestCentroid;
    }

    private double euclideanDistance(double[] point1, double[] point2) {
        double sum = 0.0;
        for (int i = 0; i < point1.length; i++) {
            sum += Math.pow(point1[i] - point2[i], 2);
        }
        return Math.sqrt(sum);
    }

    private double[] calculateMean(List<double[]> points) {
        int dimensions = points.get(0).length;
        double[] mean = new double[dimensions];
        for (double[] point : points) {
            for (int i = 0; i < dimensions; i++) {
                mean[i] += point[i];
            }
        }
        for (int i = 0; i < dimensions; i++) {
            mean[i] /= points.size();
        }
        return mean;
    }

    public static List<double[]> readDataFromCsv(String filePath) throws IOException {
        List<double[]> data = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Spalten durch Kommas getrennt parsen
                String[] parts = line.trim().split(",");
                double[] point = Arrays.stream(parts).mapToDouble(Double::parseDouble).toArray();
                data.add(point);
            }
        }
        return data;
    }

    public static void writeClustersToCsv(String filePath, Map<double[], List<double[]>> clusters) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (Map.Entry<double[], List<double[]>> entry : clusters.entrySet()) {
                double[] centroid = entry.getKey();
                writer.write("Cluster-Zentrum: " + Arrays.toString(centroid));
                writer.newLine();
                writer.write("Punkte:");
                writer.newLine();
                for (double[] point : entry.getValue()) {
                    writer.write(Arrays.toString(point));
                    writer.newLine();
                }
                writer.newLine();
            }
        }
    }

}
