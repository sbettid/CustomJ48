package inf.unibz.it.CustomJ48;

import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

/**
 * @author Davide Sbetti
 * Main used to run the decision tree classifier on a specified dataset 
 * and then export the result in the dot format
 *
 */
public class App 
{
    public static void main( String[] args )
    {
    	try {
			
    		DataSource source = new DataSource("datasets/trainingData.csv");

			Instances data = source.getDataSet();

			if (data.classIndex() == -1)
				data.setClassIndex(data.numAttributes() - 1);

			// Creating the tree object
			CustomJ48 tree = new CustomJ48();

			// Setting options

			String[] options = weka.core.Utils.splitOptions("-M 1");
			tree.setOptions(options);

			tree.buildClassifier(data);
			
			tree.dotExport("graph.gv", false); //Export tree using the dot format in a gv file
			
			tree.JSONExport("graph.json", false); //Export tree using the JSON format for the chatbot
			
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
}
