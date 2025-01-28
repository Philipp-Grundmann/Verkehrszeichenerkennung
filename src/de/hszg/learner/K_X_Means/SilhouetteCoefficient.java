package de.hszg.learner.K_X_Means;

import java.util.Arrays;

public class SilhouetteCoefficient {

    public static double calculateSilhouette(double[][] data, int[] labels) {
        //int numPoints = Math.min(1000, data.length);
        int numPoints = data.length;

        double[] silhouetteScores = new double[numPoints];

        for (int i = 0; i < numPoints; i++) {
            double a = calculateAverageDistanceToCluster(i, data, labels);
            double b = calculateAverageDistanceToNearestCluster(i, data, labels);
            silhouetteScores[i] = (b - a) / Math.max(a, b);
            System.out.printf("Silhouette-Wert für Punkt %d: %.4f%n", i, silhouetteScores[i]);
        }

        double sum = 0;
        for (int i = 0; i < numPoints; i++) {
            sum += silhouetteScores[i];
        }

        return sum / numPoints;
    }

    private static double calculateAverageDistanceToCluster(int pointIndex, double[][] data, int[] labels) {
        double sum = 0;
        int count = 0;
        for (int i = 0; i < data.length; i++) {
            if (labels[i] == labels[pointIndex] && i != pointIndex) {
                sum += euclideanDistance(data[pointIndex], data[i]);
                count++;
            }
        }
        return count > 0 ? sum / count : 0;
    }

    private static double calculateAverageDistanceToNearestCluster(int pointIndex, double[][] data, int[] labels) {
        double minDistance = Double.MAX_VALUE;
        for (int cluster = 0; cluster < Arrays.stream(labels).max().getAsInt() + 1; cluster++) {
            if (labels[pointIndex] != cluster) {
                double sum = 0;
                int count = 0;
                for (int i = 0; i < data.length; i++) {
                    if (labels[i] == cluster) {
                        sum += euclideanDistance(data[pointIndex], data[i]);
                        count++;
                    }
                }
                double averageDistance = count > 0 ? sum / count : 0;
                minDistance = Math.min(minDistance, averageDistance);
            }
        }
        return minDistance;
    }

    private static double euclideanDistance(double[] point1, double[] point2) {
        double sum = 0;
        for (int i = 0; i < point1.length; i++) {
            sum += Math.pow(point1[i] - point2[i], 2);
        }
        return Math.sqrt(sum);
    }
}
