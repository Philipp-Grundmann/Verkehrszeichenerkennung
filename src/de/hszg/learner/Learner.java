package de.hszg.learner;
import java.util.List;
import java.util.Vector;

public interface Learner {
	/** The training method, that changes the internal state of the learner such that 
	 * it will classify examples of a similar set (i.e. the testSet better.
	 * 
	 * @param trainingSet contains feature vectors and corresponding concepts 
	 * to provide experience to learn from  
	 */
	Vector<Integer> learn(List<FeatureVector> trainingSet);
	
	/**
	 * find the concept of the example from the internal knowledge of the lerner
	 * this method must not consider example.getConcept() at all!!
	 * 
	 * @param example: is a feature vector
	 * @return Array of Concepts; erster Wert ermitteltes Concept;
	 */
	Vector<Concept> classify(FeatureVector example);
}
