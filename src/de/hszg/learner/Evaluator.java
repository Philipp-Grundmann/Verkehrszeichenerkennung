package de.hszg.learner;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


public class Evaluator {
	/** the percentage (between 0 und 100) of vectors from the data to be used for the test, all others are training
	*/
	private static int testRate = 60;
	/*
	//Philips Pfade
	//Datei welche Featurevektoren enthält und eingelesen wird
	static String filename_feature_vektor ="C:\\Users\\Philipp\\Documents\\Master\\Maschinelles Lernen\\VektorData_20241030_105858.dat";

	//Datei in welche die Ergebnisse einer Laufzeit eingelesen werden
	//String filename_results_statistics = "C:\\Users\\Philipp\\Documents\\Master\\Maschinelles Lernen\\ergebnisse_"+MyDataCreator.generateDateTimeforFilename()+".csv";
	String filename_results_statistics = "C:\\Users\\Philipp\\Documents\\Master\\Maschinelles Lernen\\ergebniss.csv";
*/

	//Markus Pfade
	//Datei welche Featurevektoren enthält und eingelesen wird
	//static String filename_feature_vektor ="C:\\Code\\Daten\\VektorData_20241030_011840.dat";
	static String filename_feature_vektor ="C:\\Code\\Daten\\Evaulation\\VektorData_20241122_224715_S5xZ5";

	//Datei in welche die Ergebnisse einer Laufzeit eingelesen werden
	//String filename_results_statistics = "C:\\Users\\Philipp\\Documents\\Master\\Maschinelles Lernen\\ergebnisse_"+MyDataCreator.generateDateTimeforFilename()+".csv";
	String filename_results_statistics = "C:\\Code\\Daten\\ergebniss.csv";




	public Evaluator(String filename) {
		//List<FeatureVector> vectors = readData(filename);

		//String filename_feature_vektor ="C:\\Code\\Daten\\VektorData_20241030_011840.dat";

		List<FeatureVector> vectors = readData(filename);
		//Learner learner = new LazyLerningKNN_Lerner(3);
		Learner learner = new NeuronalesNetzwerkLearner(vectors.getFirst().getSizePerImage());

		// TODO: folgendes muss zur Evaluierung mehrfach ausgef�hrt werden
		// Verschiedene Teilmengen finden und Verschiedene Reihenfolgen festlegen,
		// wie oft, das h�ngt vom gew�nschten Vertrauensintervall ab
		int i=0;

		do{
			vectors = mixData(vectors, i);					//shuffle mit i als Seed
			//int testdatenmenge=7500;
			//int testdatenmenge=1000;
			int testdatenmenge=vectors.size()-1;


			if (testdatenmenge > (vectors.size()-1)) { //Zur Sicherheit falls zu wenig Daten vorhanden
				testdatenmenge=vectors.size()-1;
			}

			List<FeatureVector> vectors_use = new LinkedList<>(); //Möglichkeit die Anzahl der Testdaten zu verringern
			vectors_use.addAll(vectors.subList(0,testdatenmenge));


			List<List<FeatureVector>> sets = extractTrainingData(vectors_use); //Aufteilung der Featurevektordaten in Test und Trainingsdaten

			Vector<Integer> result_static=new Vector<>();		//Vektor mit Werten bezüglich des Lernprozesses
			result_static.add(0, vectors_use.size());	//Vektor 0 = Gesamtanzahl aller Datensätze
			result_static.add(1, sets.get(0).size());	//Vektor 1 = Anzahl der Trainingsdaten
			result_static.add(2, sets.get(1).size());	//Vektor 2 = Anzahl der Testdaten
			result_static.add(3, i);					//Vektor 3 = N eines Durchganges
			//result_static.add(4,testdatenmenge); 			//Unnötig da bereits in Vektor 0 hinterlegt


			Vector<Integer> result_lern=learner.learn(sets.get(0));

			Vector<Integer> result_classify = evaluate(sets.get(1),learner);
			evalResult(result_static, result_classify, result_lern);

			i++;
		}while(i<100); //TODO: eine andere Abbruchbedingung verwenden



	}
	/**
	 * Evaluate the result from the test for output or furthjer considerations
	 * @param result_static Ergebnissvektor der Lernfunktion mit Einstellungsparametern
	 * @param result_classify Ergebnissvektor des Klassifizierungsprozesses, welche die Grundlage für eine statistische Auswertung bilden
	 */
	private void evalResult(Vector<Integer>result_static, Vector<Integer> result_classify,Vector<Integer> result_lern) {
		// TODO hier muss mehr Auswertung passieren, insbes: Vertrauensintervalle etc
		System.out.println("Learning result: \n correct: "+result_classify.get(0)+"\n unknown: "+result_classify.get(1)+"\n wrong: "+result_classify.get(2));

		// Angenommene Werte für richtig, falsch und unbekannt klassifizierte Bilder
		int richtig = result_classify.get(0);  // Beispielwert für richtig klassifizierte Bilder
		int unbekannt = result_classify.get(1); // Beispielwert für unbekannt klassifizierte Bilder
		int falsch = result_classify.get(2);    // Beispielwert für falsch klassifizierte Bilder


		// Gesamtanzahl der Bilder berechnen
		int gesamt = richtig + falsch + unbekannt;

		if (gesamt == 0) {
			System.out.println("Die Gesamtanzahl der Bilder darf nicht 0 sein.");
			return;
		}

		// Konstante für 95%-Konfidenzintervall (z-Wert = 1.96)
		double zWert = 1.96;

		// Berechnungen
		double erfolgsrate = berechneErfolgsrate(richtig, gesamt);
		double beispielFehler = berechneBeispielFehler(falsch+unbekannt, gesamt);
		double standardabweichung = berechneStandardabweichung(erfolgsrate, gesamt);
		double[] konfidenzintervall = berechneKonfidenzintervall(erfolgsrate, zWert, standardabweichung);

		// Ausgabe der Ergebnisse
		System.out.printf("Erfolgsrate (richtig klassifizierte Bilder): %.4f%n", erfolgsrate);
		System.out.printf("Beispiel-Fehler (falsch klassifizierte Bilder): %.4f%n", beispielFehler);
		System.out.printf("Standardabweichung: %.4f%n", standardabweichung);
		System.out.printf("Konfidenzintervall: [%.4f, %.4f]%n", konfidenzintervall[0], konfidenzintervall[1]);

		// Pfad zur CSV-Datei ist in Klasse globale definiert
		schreibeStatistikInCsv(filename_results_statistics,result_static, erfolgsrate, beispielFehler, standardabweichung, konfidenzintervall, result_lern);

	}
	/** evaluate the learner with a given test set. 
	 * 
	 * @param list: The set of test examples containing the correct concept
	 * @param learner: The learner to be tests
	 * 
	 * @return a vector containing the test results: success, unknown, false
	 */
	 private Vector<Integer> evaluate(List<FeatureVector> list, Learner learner) {
		int success=0;int unknown=0;int fault =0;
		for(FeatureVector fv : list){
			Concept c = learner.classify(fv).get(0);
			if(c.equals(Concept.Unknown)) unknown++;
			else if(c.equals(fv.getConcept())) success++;
			else fault++;
				
		}
		Vector<Integer> res = new Vector<>();
		res.add(0,success);
		res.add(1,unknown);
		res.add(2,fault);
		return res;
	}
/**
 * 
 * @param vectors a list of vectors
 * @return list containing the same vectors as parameter but 
 * (usually) in different order
 */
	private List<FeatureVector> mixData(List<FeatureVector> vectors, int seedValue) {
		Collections.shuffle(vectors, new Random(seedValue));
		return vectors;
	}

	/**
	 * Split the set of festure vectors in a set of traing data and a set of test data.
	 * For representative results it is essential to mix the order of vectors 
	 * before splitting the set
	 * 
	 * @param vectors :a List fo Feature Vectors we can use for the test
	 * @return a List containt two Lists: first the training data, second the test data they are disjunct subsets of vector 
	 *
	 */
	private List<List<FeatureVector>> extractTrainingData(
			List<FeatureVector> vectors) {
		List<List<FeatureVector>> result = new LinkedList<>();
		List<FeatureVector> trainingData = new LinkedList<>();
		List<FeatureVector> testData = new LinkedList<>();
		
		int cut = (int) (testRate/100.0 * vectors.size());
		trainingData.addAll(vectors.subList(0,cut));
		testData.addAll(vectors.subList(cut+1, vectors.size()));
		
		result.add(trainingData);
		result.add(testData);
		return result;
	}

	/** read data from file
	 * 
	 * @param filename the file with this name should contain a serialized List<FeatureVector> containt all the data
	 * @return all the data
	 */
	private List<FeatureVector> readData(String filename) {
		List<FeatureVector> vectors = null;
		try{
			ObjectInputStream in = new ObjectInputStream(new BufferedInputStream( new FileInputStream(filename)));
			vectors = (List<FeatureVector>) in.readObject();
			in.close();
		}catch(Throwable t){
			System.out.println("Could not read Data from file: "+filename);
			System.exit(1);
		}
		return vectors;
	}

	/** startet das Programm mit den hinterlegten Trainingsdaten
	 */
	public static void main(String[] args){
		//filename_feature_vektor ist als static String innerhalb der Klasse definiert
		//String filename= "C:\\Code\\Daten\\Evaulation\\VektorData_20241122_224715_S5xZ5.dat";
		//String filename= "C:\\Code\\Daten\\Evaulation\\VektorData_20241122_224715_S5xZ5";

		new Evaluator("C:\\Code\\Daten\\Evaulation\\VektorData_20241122_202106_S2xZ2.dat");
		new Evaluator("C:\\Code\\Daten\\Evaulation\\VektorData_20241122_210952_S3xZ3.dat");
		new Evaluator("C:\\Code\\Daten\\Evaulation\\VektorData_20241122_215836_S4xZ4.dat");
		new Evaluator("C:\\Code\\Daten\\Evaulation\\VektorData_20241122_224715_S5xZ5.dat");
		new Evaluator("C:\\Code\\Daten\\Evaulation\\VektorData_20241122_233608_S7xZ7.dat");
		new Evaluator("C:\\Code\\Daten\\Evaulation\\VektorData_20241123_002424_S10xZ10.dat");
		new Evaluator("C:\\Code\\Daten\\Evaulation\\VektorData_20241123_011239_S15xZ15.dat");
		new Evaluator("C:\\Code\\Daten\\Evaulation\\VektorData_20241123_020045_S4xZ3.dat");
		new Evaluator("C:\\Code\\Daten\\Evaulation\\VektorData_20241123_024852_S3xZ4.dat");
		new Evaluator("C:\\Code\\Daten\\Evaulation\\VektorData_20241123_033655_S3xZ5.dat");
		new Evaluator("C:\\Code\\Daten\\Evaulation\\VektorData_20241123_042455_S5xZ3.dat");

/*		Testcode für Concept to Bit
		System.out.println("Concept in Bit: ");
		System.out.println("Concept: "+Concept.Unknown.toString()+" Bitfolge: "+ Arrays.toString(NeuronalesNetzwerkLearner.ConceptToOutputArray(Concept.Unknown)));
		System.out.println("Concept: "+Concept.Vorfahrt_von_Rechts.toString()+" Bitfolge: "+ Arrays.toString(NeuronalesNetzwerkLearner.ConceptToOutputArray(Concept.Vorfahrt_von_Rechts)));
		System.out.println("Concept: "+Concept.Stoppschild.toString()+" Bitfolge: "+ Arrays.toString(NeuronalesNetzwerkLearner.ConceptToOutputArray(Concept.Stoppschild)));
		System.out.println("Concept: "+Concept.Fahrtrichtung_links.toString()+" Bitfolge: "+ Arrays.toString(NeuronalesNetzwerkLearner.ConceptToOutputArray(Concept.Fahrtrichtung_links)));
		System.out.println("Concept: "+Concept.Fahrtrichtung_rechts.toString()+" Bitfolge: "+ Arrays.toString(NeuronalesNetzwerkLearner.ConceptToOutputArray(Concept.Fahrtrichtung_rechts)));
		System.out.println("Concept: "+Concept.Vorfahrt_gewähren.toString()+" Bitfolge: "+ Arrays.toString(NeuronalesNetzwerkLearner.ConceptToOutputArray(Concept.Vorfahrt_gewähren)));
		System.out.println("Concept: "+Concept.Vorfahrtsstraße.toString()+" Bitfolge: "+ Arrays.toString(NeuronalesNetzwerkLearner.ConceptToOutputArray(Concept.Vorfahrtsstraße)));
*/
	}

	// Methode zur Berechnung der Erfolgsrate (richtig klassifizierte Bilder / alle Bilder)
	public static double berechneErfolgsrate(int richtig, int gesamt) {
		return (double) richtig / gesamt;
	}

	// Methode zur Berechnung des durchschnittlichen Fehlers bzw. Beispiel-Fehler (falsch klassifizierte Bilder / alle Bilder)
	public static double berechneBeispielFehler(int falsch, int gesamt) {
		return (1.0/gesamt)*falsch;
	}

	// Methode zur Berechnung der Standardabweichung für eine Binomialverteilung
	public static double berechneStandardabweichung(double erfolgsrate, int gesamt) {
		return Math.sqrt(erfolgsrate * (1 - erfolgsrate) / gesamt);
	}

	// Methode zur Berechnung des Konfidenzintervalls
	public static double[] berechneKonfidenzintervall(double erfolgsrate, double zWert, double standardabweichung) {
		double untereGrenze = erfolgsrate - zWert * standardabweichung;
		double obereGrenze = erfolgsrate + zWert * standardabweichung;

		// Sicherheitskontrolle, damit das Konfidenzintervall zwischen 0 und 1 bleibt
		untereGrenze = Math.max(0, untereGrenze);
		obereGrenze = Math.min(1, obereGrenze);

		return new double[]{untereGrenze, obereGrenze};
	}

	public static void schreibeStatistikInCsv(
			String dateiPfad,
			Vector<Integer> result_static,
			double erfolgsrate,
			double durchschnittlicherFehler,
			double standardabweichung,
			double[] konfidenzintervall,
			Vector<Integer> result_lern
			) {

		// Format für Datum und Uhrzeit
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd; HH:mm:ss");
		String zeitstempel = LocalDateTime.now().format(formatter);

		// CSV-Zeile im gewünschten Format erstellen
		String zeile = String.format("%s; %d; %d; %d; %d; %.4f; %.4f; %.4f; %.4f; %.4f; %d; %d; %d; %d; %d; %d; %d; %n",
				zeitstempel,
				result_static.get(0),result_static.get(1), result_static.get(2), result_static.get(3),
				erfolgsrate, durchschnittlicherFehler, standardabweichung,
				konfidenzintervall[0], konfidenzintervall[1],
				result_lern.get(0), result_lern.get(1),  result_lern.get(2), result_lern.get(3), result_lern.get(4), result_lern.get(5), result_lern.get(6));

		try {
			// Überprüfen, ob die Datei existiert
			File datei = new File(dateiPfad);
			boolean istDateiNeu = !datei.exists();

			// Datei im Append-Modus öffnen
			try (FileWriter writer = new FileWriter(dateiPfad, true)) {
				// Falls die Datei neu ist, schreibe die Kopfzeile
				if (istDateiNeu) {
					//writer.write("Datum; Uhrzeit; Erfolgsrate; Durchschnittlicher Fehler; Standardabweichung; Konfidenzintervall\n");
					writer.write("Datum; Uhrzeit;Gesamtanzahl; Anz_Trainingsdaten; Anz_Testdaten; n-Runde; Erfolgsrate; Beispiel-Fehler; Standardabweichung; Konfidenzintervall-Unten; Konfidenzintervall-Oben;" +
							"Anz_Trainingsdaten; Anz_Epoch; Anz_Perceptron; Anz_IMGVectoren; Anz_GridCols; Anz_GridRows\n");
				}
				// Schreibe die Datenzeile
				writer.write(zeile);
				System.out.println("Ergebnisse wurden erfolgreich in die CSV-Datei geschrieben.");
			}
		} catch (IOException e) {
			System.out.println("Fehler beim Schreiben in die CSV-Datei: " + e.getMessage());
		}
		/*result_lern:
		*       res.add(0, trainingSet.size()); //Anzahl aller gelernten Feature-Vektoren
        res.add(1, epochs); //Anzahl Epochen
        res.add(2, anz_perceptron); //Anzahl Percepton
        res.add(3, trainingSet.getFirst().getNumFeatures()); //Anzahl Features je Bild
        res.add(4, trainingSet.getFirst().getSizePerImage()); //Sollte das Gleiche sein
        res.add(5, trainingSet.getFirst().getGridCols());
        res.add(6, trainingSet.getFirst().getGridRows());
		* */



	}
}
