package de.hszg.learner.K_X_Means;

import java.util.*;

public class Evaluator {
    // Testdaten und die wahren Labels
    private final List<double[]> testData;
    private final List<String> testLabels;
    private final List<ClusterCSVLoader.Cluster> clusters;

    public Evaluator(List<double[]> testData, List<String> testLabels, List<ClusterCSVLoader.Cluster> clusters) {
        this.testData = testData;
        this.testLabels = testLabels;
        this.clusters = clusters;
    }

    public Vector<Integer> evaluate() {
        int correct = 0;
        int unknown = 0;
        int incorrect = 0;

        // Zähle die Erfolge und Misserfolge für jede Klasse
        Map<String, Integer> classCounts = new HashMap<>();
        Map<String, Integer> classSuccesses = new HashMap<>();

        // Initialisiere Erfolgs- und Zählwerte für alle Klassen
        for (ClusterCSVLoader.Cluster cluster : clusters) {
            classCounts.put(cluster.getAssignedClass(), 0);
            classSuccesses.put(cluster.getAssignedClass(), 0);
        }

        // Gehe durch alle Testdaten und führe die Klassifizierung durch
        for (int i = 0; i < testData.size(); i++) {
            double[] testPoint = testData.get(i);
            String trueLabel = testLabels.get(i);

            // Bestimme das Clusterzentrum, das dem Testpunkt am nächsten ist
            ClusterCSVLoader.Cluster nearestCluster = getNearestCluster(testPoint);
            String predictedLabel = nearestCluster != null ? nearestCluster.getAssignedClass() : "Unknown";

            // Vergleiche die Vorhersage mit dem wahren Label
            if (predictedLabel.equals(trueLabel)) {
                correct++;
                classSuccesses.put(predictedLabel, classSuccesses.get(predictedLabel) + 1);
            } else if (predictedLabel.equals("Unknown")) {
                unknown++;
            } else {
                incorrect++;
            }

            // Zähle auch die Gesamtliste der Klassifizierungen für jede Klasse
            if (!predictedLabel.equals("Unknown")) {
                classCounts.put(predictedLabel, classCounts.get(predictedLabel) + 1);
            }
        }

        // Drucke die Auswertung
        printEvaluationResults(correct, unknown, incorrect, classCounts, classSuccesses);

        // Gebe die Klassifikationsergebnisse zurück
        Vector<Integer> results = new Vector<>();
        results.add(correct);
        results.add(unknown);
        results.add(incorrect);

        return results;
    }

    private ClusterCSVLoader.Cluster getNearestCluster(double[] testPoint) {
        double minDistance = Double.MAX_VALUE;
        ClusterCSVLoader.Cluster nearestCluster = null;

        // Bestimme das nächstgelegene Cluster
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

    private void printEvaluationResults(int correct, int unknown, int incorrect,
                                        Map<String, Integer> classCounts, Map<String, Integer> classSuccesses) {
        // Drucke allgemeine Ergebnisse
        System.out.println("Evaluation Results:");
        System.out.println("Correct: " + correct);
        System.out.println("Unknown: " + unknown);
        System.out.println("Incorrect: " + incorrect);

        // Drucke Ergebnisse pro Klasse
        for (String className : classCounts.keySet()) {
            int totalClassCount = classCounts.get(className);
            int correctClassCount = classSuccesses.get(className);
            double accuracy = totalClassCount > 0 ? (double) correctClassCount / totalClassCount : 0.0;
            System.out.printf("Class %s: %d/%d correct (%.2f%%)\n", className, correctClassCount, totalClassCount, accuracy * 100);
        }
    }
}
