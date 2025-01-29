package de.hszg.learner.K_X_Means;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class Evaluator {

    private final List<double[]> testData;
    private final List<String> testLabels;
    private final List<ClusterCSVLoader.Cluster> clusters;
    private final String outputPath;

    public Evaluator(List<double[]> testData, List<String> testLabels,
                     List<ClusterCSVLoader.Cluster> clusters, String outputPath) {
        this.testData = testData;
        this.testLabels = testLabels;
        this.clusters = clusters;
        this.outputPath = outputPath;
    }

    public void evaluate() throws IOException {
        Map<String, EvaluationMetrics> classMetrics = new HashMap<>();
        for (ClusterCSVLoader.Cluster cluster : clusters) {
            classMetrics.put(cluster.getAssignedClass(), new EvaluationMetrics());
        }

        // Evaluation durchführen
        for (int i = 0; i < testData.size(); i++) {
            double[] testPoint = testData.get(i);
            String trueLabel = testLabels.get(i);

            // Nächstes Cluster finden
            ClusterCSVLoader.Cluster nearestCluster = getNearestCluster(testPoint);
            String predictedLabel = nearestCluster.getAssignedClass();

            EvaluationMetrics metrics = classMetrics.get(predictedLabel);
            metrics.total++;
            if (predictedLabel.equals(trueLabel)) {
                metrics.correct++;
            } else {
                metrics.incorrect++;
            }
        }

        calculateStatistics(classMetrics);
        writeEvaluationResultsToCSV(classMetrics);
    }

    private ClusterCSVLoader.Cluster getNearestCluster(double[] testPoint) {
        double minDistance = Double.MAX_VALUE;
        ClusterCSVLoader.Cluster nearestCluster = null;

        for (ClusterCSVLoader.Cluster cluster : clusters) {
            double distance = calculateEuclideanDistance(testPoint, cluster.getCentroid());
            if (distance < minDistance) {
                minDistance = distance;
                nearestCluster = cluster;
            }
        }

        return nearestCluster;
    }

    private double calculateEuclideanDistance(double[] point1, double[] point2) {
        double sum = 0;
        for (int i = 0; i < point1.length; i++) {
            sum += Math.pow(point1[i] - point2[i], 2);
        }
        return Math.sqrt(sum);
    }

    private void calculateStatistics(Map<String, EvaluationMetrics> classMetrics) {
        for (EvaluationMetrics metrics : classMetrics.values()) {
            if (metrics.total > 0) {
                metrics.accuracy = (double) metrics.correct / metrics.total;
                metrics.variance = metrics.accuracy * (1 - metrics.accuracy) / metrics.total;
                metrics.stdDev = Math.sqrt(metrics.variance);
                double z = 1.96; // 95% Konfidenzintervall
                metrics.lowerBound = metrics.accuracy - z * metrics.stdDev;
                metrics.upperBound = metrics.accuracy + z * metrics.stdDev;
            }
        }
    }

    private void writeEvaluationResultsToCSV(Map<String, EvaluationMetrics> classMetrics) throws IOException {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath, true))) {
            // Schreibe die Kopfzeile, falls die Datei leer ist
            File file = new File(outputPath);
            if (file.length() == 0) {
                writer.write("Timestamp," + String.join(",", classMetrics.keySet()) +
                        ",OverallAccuracy,OverallStdDev,ConfidenceIntervalLow,ConfidenceIntervalHigh");
                writer.newLine();
            }

            writer.write(timestamp);

            for (Map.Entry<String, EvaluationMetrics> entry : classMetrics.entrySet()) {
                EvaluationMetrics metrics = entry.getValue();

                // Prozentzahlen und Nachkommastellen mit Punkt formatieren
                String accuracy = String.format(Locale.US, "%.2f", metrics.accuracy*100);
                String stdDev = String.format(Locale.US, "%.2f", metrics.stdDev );
                String lowerBound = String.format(Locale.US, "%.2f", metrics.lowerBound );
                String upperBound = String.format(Locale.US, "%.2f", metrics.upperBound );

                // Die Zeile mit der Zählung auskommentieren, um sie zu entfernen
                // writer.write(String.format(",%d/%d (%s%%)", metrics.correct, metrics.total, accuracy));

                // Stattdessen nur den Accuracy-Prozentsatz pro Klasse ausgeben
                writer.write(String.format(",%s", accuracy));
            }

            // Berechnung der Gesamtstatistiken
            double overallStdDev = calculateOverallStdDev(classMetrics.values());
            double[] confidenceInterval = calculateOverallConfidenceInterval(classMetrics.values());
            double overallAccuracy = calculateOverallAccuracy(classMetrics.values());

            // Gesamtstatistiken mit Punkt statt Komma
            String overallAccuracyStr = String.format(Locale.US, "%.2f", overallAccuracy);
            //String overallStdDevStr = String.format(Locale.US, "%.2f", overallStdDev );
            //String lowerCI = String.format(Locale.US, "%.2f", confidenceInterval[0] );
            //String upperCI = String.format(Locale.US, "%.2f", confidenceInterval[1] );

            writer.write(String.format(",%s", //,%s,%s,%s
                    overallAccuracyStr));//, overallStdDevStr, lowerCI, upperCI
            writer.newLine();
        }

        System.out.println("Ergebnisse erfolgreich in " + outputPath + " gespeichert.");
    }

    private double calculateOverallStdDev(Collection<EvaluationMetrics> metrics) {
        double varianceSum = 0.0;
        int totalSamples = 0;

        for (EvaluationMetrics metric : metrics) {
            varianceSum += metric.variance * metric.total;
            totalSamples += metric.total;
        }

        return Math.sqrt(varianceSum / totalSamples);
    }

    private double[] calculateOverallConfidenceInterval(Collection<EvaluationMetrics> metrics) {
        double overallAccuracy = 0.0;
        int totalSamples = 0;

        for (EvaluationMetrics metric : metrics) {
            overallAccuracy += metric.accuracy * metric.total;
            totalSamples += metric.total;
        }
        overallAccuracy /= totalSamples;

        double overallVariance = overallAccuracy * (1 - overallAccuracy) / totalSamples;
        double stdDev = Math.sqrt(overallVariance);
        double z = 1.96; // 95% Konfidenzintervall

        return new double[]{overallAccuracy - z * stdDev, overallAccuracy + z * stdDev};
    }

    private static class EvaluationMetrics {
        int total = 0;
        int correct = 0;
        int incorrect = 0;
        double accuracy = 0.0;
        double variance = 0.0;
        double stdDev = 0.0;
        double lowerBound = 0.0;
        double upperBound = 0.0;
    }

    private double calculateOverallAccuracy(Collection<EvaluationMetrics> metrics) {
        int totalCorrect = 0;
        int totalSamples = 0;

        for (EvaluationMetrics metric : metrics) {
            totalCorrect += metric.correct;
            totalSamples += metric.total;
        }

        return totalSamples > 0 ? (double) totalCorrect / totalSamples : 0.0;
    }

}
