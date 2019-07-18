package inf.unibz.it.CustomJ48;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import org.omg.CORBA.portable.InputStream;

import weka.core.Instances;
import weka.core.converters.CSVLoader;
import weka.core.converters.ConverterUtils.DataSource;

public class TestClass {

	public static void main(String[] args) throws Exception {
		String file = "";
		//read file
		Scanner myScanner = new Scanner(new File("datasets/example.csv"));
		String newFile = "";
		while(myScanner.hasNextLine()) {
			newFile += myScanner.nextLine() + "\n";
			
		}
		
		myScanner.close();
		
		
		//try to build the tree
		ByteArrayInputStream in = new ByteArrayInputStream(newFile.getBytes());
		
		CSVLoader csv = new CSVLoader();
		csv.setSource(in);
		
		Instances data = csv.getDataSet();
		
		if (data.classIndex() == -1) //Setting the last attribute to be last one
			data.setClassIndex(data.numAttributes() - 1);
		
		CustomJ48Tree tree = new CustomJ48Tree();

		// Setting options

		String[] treeOptions = weka.core.Utils.splitOptions("-M 1 -U");
		tree.setOptions(treeOptions);

		tree.buildClassifier(data); //Build the tree
		
		System.out.println(tree.toString());
	}

}
