package de.hszg.learner;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.List;

public class MyDataCreator {

	private static final String filename = "DummyData.dat";
	MyDataCreator(){
		int[] DummyColorArray = new int[125];
		int DummyCorner = 1;

		FeatureVector[] f = new FeatureVector[6];
		f[0] = new MyFeatureVector(DummyCorner,DummyColorArray,Concept.Stoppschild);
		f[1] = new MyFeatureVector(DummyCorner,DummyColorArray,Concept.Stoppschild);
		f[2] = new MyFeatureVector(DummyCorner,DummyColorArray,Concept.Stoppschild);
		f[3] = new MyFeatureVector(DummyCorner,DummyColorArray,Concept.Stoppschild);
		f[4] = new MyFeatureVector(DummyCorner,DummyColorArray,Concept.Stoppschild);
		f[5] = new MyFeatureVector(DummyCorner,DummyColorArray,Concept.Stoppschild);
		
		List<FeatureVector> res = new LinkedList<>();
		for(FeatureVector fv : f) res.add(fv);
		try{
			ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(filename)));
			out.writeObject(res);
			out.close();
		}catch(Throwable t){
			System.out.println("DummyDataCreator: Could not create DummyData.dat");
			t.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new MyDataCreator();
	}

}
