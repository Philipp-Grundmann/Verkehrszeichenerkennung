package de.hszg.learner;

public class MyFeatureVector implements FeatureVector {

	private Concept concept;
	private int[] feature = new int[126]; //Alle Werte des Arrys werden mit Nullen initalisiert
	// Anzahl Ecken, Feld 0,0 Blau - Gelb - Rot - Schwarz - Weiß, bis Feld 4,4 Blau - Gelb - Rot - Schwarz - Weiß
	// Ecken, Blau, Gelb, Rot, Schwarz, Blau, Gelb, Rot, Schwarz,.. (5*5Felder*5Farben = 125 Farbvektoren)

	MyFeatureVector(int corner, int[]colors, Concept concept_input){
		feature[0] = corner;
		System.arraycopy(colors, 0, feature, 1, colors.length); //Kopiert alle Werte aus colors-Arry in Feature Vektor
		concept = concept_input;
	}
	
	@Override
	public Concept getConcept() {
		return concept;
	}

	@Override
	public int getNumFeatures() {
		System.out.println("Länge des Feature Vektores: "+feature.length);
		return feature.length;
	}

	@Override
	public int getFeatureValue(int i) {
		return feature[i];
	}

}
