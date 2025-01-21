package de.hszg.learner;

import java.io.Serializable;

public interface FeatureVector extends Serializable {

	/**
	 * @return die Anzahl der Merkmale im Merkmalsvektor
	 */
	int getNumFeatures();

	/**
	 * @param i der Index des Merkmals im Vektor
	 * @return den Wert des Merkmals mit Index i
	 */
	double getFeatureValue(int i);

	/**
	 * Gibt eine textuelle Darstellung des Merkmalsvektors zurück
	 * @return String mit den Werten des Merkmalsvektors
	 */
	String getPrintVektorValue();

	/**
	 * Gibt den gesamten Merkmalsvektor zurück
	 * @return das Array der Merkmale
	 */
	double[] getFeatureVektor();

	/**
	 * Gibt den Grid-Spaltenwert zurück
	 * @return die Anzahl der Spalten im Raster
	 */
	int getGridCols();

	/**
	 * Gibt den Grid-Zeilenwert zurück
	 * @return die Anzahl der Zeilen im Raster
	 */
	int getGridRows();
}