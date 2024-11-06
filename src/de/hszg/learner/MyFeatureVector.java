package de.hszg.learner;

import java.util.Arrays;

public class MyFeatureVector implements FeatureVector {

	private Concept concept;
	private double[] feature;//Alle Werte des Arrys werden mit Nullen initalisiert
	// Anzahl Ecken, Feld 0,0 Blau - Gelb - Rot - Schwarz - Weiß, bis Feld 4,4 Blau - Gelb - Rot - Schwarz - Weiß
	// Ecken, Blau, Gelb, Rot, Schwarz, Blau, Gelb, Rot, Schwarz,.. (5*5Felder*5Farben = 125 Farbvektoren)

	MyFeatureVector(int corner, double[] colors, Concept concept_input){
		feature = new double[colors.length+1];
		feature[0] = (double) corner;
		System.arraycopy(colors, 0, feature, 1, colors.length); //Kopiert alle Werte aus colors-Arry in Feature Vektor

		/*for (int i = 0; i < 125; i++) {
			feature[i+1] = Math.random(); // zufällige Werte zum Testen
		}*/
		concept = concept_input;
	}
	
	@Override
	public Concept getConcept() {
		return concept;
	}

	@Override
	public int getNumFeatures() {
		//System.out.println("Länge des Feature Vektores: "+feature.length);
		return feature.length;
	}

	@Override
	public double getFeatureValue(int i) {
		return feature[i];
	}

	public String getPrintVektorValue(){
		return Arrays.toString(feature);
	}
}
