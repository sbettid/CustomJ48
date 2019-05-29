package inf.unibz.it.CustomJ48;

import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;


/**
 * @author Davide Sbetti
 * Main used to run the decision tree classifier on a specified dataset 
 * and then export the result in the dot format
 *
 */
public class CustomJ48 
{
    public static void main( String[] args )
    {
    	//Since it is a console application let's prepare the options
    	
//			
//    		DataSource source = new DataSource("datasets/weather.nominal.arff");
//
//			Instances data = source.getDataSet();
//
//			if (data.classIndex() == -1)
//				data.setClassIndex(data.numAttributes() - 1);
//
//			// Creating the tree object
//			CustomJ48 tree = new CustomJ48();
//
//			 // Setting options
//
//			String[] options = weka.core.Utils.splitOptions("-M 1");
//			tree.setOptions(options);
//
//			tree.buildClassifier(data);
//			
			
			System.out.print("Welcome to Custom J48");
			
		
    }
}
