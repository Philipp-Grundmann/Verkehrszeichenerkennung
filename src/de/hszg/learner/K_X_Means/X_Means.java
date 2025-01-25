package de.hszg.learner.K_X_Means;

import java.io.IOException;

public class X_Means {
    public static void main(String[] args) {
        try{

            int randomSeed = 42;
            int maxIterations = 200;
            int maxCluster = 100;
            double trainSplitRatio = 0.8;

            String outputPath = "src/de/hszg/learner/ergebnisse/xmeans_results.csv";
            //String csvFilePath = "src/de/hszg/learner/ergebnisse/VektorData_20250121_193812_S7xZ7.csv";
            String csvFilePath = "src/de/hszg/learner/ergebnisse/VektorData_20250125_154632_S4xZ4.csv";

            DataLoader dataLoader = new DataLoader(csvFilePath, randomSeed);
            DataLoader.DataSet dataSet = dataLoader.loadData(trainSplitRatio);

            //System.out.println(dataSet.trainLabels);

            X_Means_Modell x_means_modell = new X_Means_Modell(maxCluster, maxIterations, randomSeed);
            x_means_modell.run(dataSet.trainData, dataSet.trainLabels, outputPath);

        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
