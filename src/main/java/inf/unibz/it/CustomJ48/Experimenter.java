package inf.unibz.it.CustomJ48;

import weka.filters.Filter;
import weka.filters.supervised.instance.Resample;
import weka.filters.unsupervised.attribute.StringToNominal;

import java.util.logging.LogManager;

import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class Experimenter {

	public static void main(String[] args) {
		
		
		String datasetPath = "datasets/" + args[0];
		LogManager.getLogManager().reset(); // Prevent the system from continuously writing logs to the console
		
		try {
			
			DataSource data = new DataSource(datasetPath);
			Instances inst = data.getDataSet();
			
			if (inst.classIndex() == -1) // Setting the class attribute to be last one if not explicitly set
				inst.setClassIndex(inst.numAttributes() - 1);
			
			if (inst.checkForStringAttributes()) {

				// Apply weka filter to convert them to nominal
				StringToNominal filter = new StringToNominal();
				filter.setAttributeRange("first-last");
				filter.setInputFormat(inst);

				inst = Filter.useFilter(inst, filter);
			}
			
			Resample sampler = new Resample();
			sampler.setSampleSizePercent(20);
			sampler.setNoReplacement(true);
			sampler.setBiasToUniformClass(0);
			sampler.setInputFormat(inst);
			
			System.err.println("Instances size before sampling: " + inst.size());
			
			inst = Filter.useFilter(inst, sampler);
			
			System.err.println("Instances size after sampling: " + inst.size());
			
			System.out.println(inst.toString());
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}

}
