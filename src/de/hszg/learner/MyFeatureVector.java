package de.hszg.learner;

import java.util.Arrays;

public class MyFeatureVector implements FeatureVector {
	private double[] featureVector;
	private int gridRows;
	private int gridCols;

	// Konstruktor ohne Concept:
	public MyFeatureVector(double[] featureVector, int gridRows, int gridCols) {
		this.featureVector = featureVector;
		this.gridRows = gridRows;
		this.gridCols = gridCols;
	}

	@Override
	public int getNumFeatures() {
		return featureVector.length;
	}

	@Override
	public double getFeatureValue(int i) {
		return featureVector[i];
	}

	@Override
	public String getPrintVektorValue() {
		return Arrays.toString(featureVector);
	}

	@Override
	public double[] getFeatureVektor() {
		return featureVector;
	}

	@Override
	public int getGridCols() {
		return gridCols;
	}

	@Override
	public int getGridRows() {
		return gridRows;
	}
}