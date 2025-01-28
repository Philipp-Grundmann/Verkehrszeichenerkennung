package de.hszg.learner.K_X_Means;

import java.io.IOException;
import java.util.List;

public class X_Means {
    public static void main(String[] args) {

        //for (int Epochen = 0; Epochen < 100; Epochen++) {
            try {

                int randomSeed = 42;
                int maxIterations = 100;
                int maxCluster = 100;
                double trainSplitRatio = 0.8;


                String outputPath = "src/de/hszg/learner/ergebnisse/xmeans_results.csv";
                //String csvFilePath = "src/de/hszg/learner/ergebnisse/VektorData_20250121_193812_S7xZ7.csv";
                String csvFilePath = "src/de/hszg/learner/ergebnisse/VektorData_20250125_194044_S3xZ3.csv";
                String resultPath = "src\\de\\hszg\\learner\\VektorData_20250125_154632_S4xZ4.csv";


                DataLoader dataLoader = new DataLoader(csvFilePath, randomSeed);
                DataLoader.DataSet dataSet = dataLoader.loadData(trainSplitRatio);

                //System.out.println(dataSet.trainLabels);

                X_Means_Modell x_means_modell = new X_Means_Modell(maxCluster, maxIterations, randomSeed); //Epochen =seed
                x_means_modell.run(dataSet.trainData, dataSet.trainLabels, outputPath);

                // Cluster laden
                ClusterCSVLoader clusterLoader = new ClusterCSVLoader(outputPath);
                List<ClusterCSVLoader.Cluster> clusters = clusterLoader.loadClusters();

                // Evaluator initialisieren
                Evaluator evaluator = new Evaluator(dataSet.testData, dataSet.testLabels, clusters, resultPath);
                // Evaluation durchführen
                evaluator.evaluate();
            } catch (IOException e) {
                System.err.println("Fehler beim Laden der Dateien: " + e.getMessage());
            //}
        }
    }
}
