package inf.unibz.it.CustomJ48;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.logging.LogManager;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

/**
 * @author Davide Sbetti Main used to run the decision tree classifier on a
 *         specified dataset and then export the result in the dot format
 *
 */
public class CustomJ48 {
	
	public enum ExportFormat{
		DOT,JSON, GRAPHML;
	}
	
	public static void main(String[] args) {
		
		LogManager.getLogManager().reset(); //Prevent the system from continuously writing logs to the console
		
		// Since it is a console application let's prepare the options that require an
		// argument
		// We need the user to specify the path of the dataset
		Option datasetPath = Option.builder("d").argName("dataset").hasArg()
				.desc("Specifies the path of the dataset (REQUIRED)").build();

		// Let's create the option to use a file instead the console stream for the
		// export code
		Option fileStream = Option.builder("f").argName("file").hasArg()
				.desc("Output the export code to the given file").build();

		// The 'e' option allows the user to decide the export format (graphml, dot and
		// json), default is dot
		Option exportFormat = Option.builder("e").argName("format").hasArg()
				.desc("specify the export format (graphml, dot, json). Default is: dot").build();

		// And now add them to the options array with the boolean ones (flags)
		Options options = new Options();
		options.addOption(datasetPath);
		options.addOption(fileStream);
		options.addOption(exportFormat);
		options.addOption("p", "Enable the pruning feature"); // Enable or no the pruning feature?
		options.addOption("h", "Print this help message"); // print the help message

		CommandLineParser parser = new DefaultParser(); // create the parser

		try {

			CommandLine line = parser.parse(options, args); // and try to parse the arguments

			// if the help option is used we print the help menu and we exit
			if (line.hasOption("h")) {
				// automatically generate the help statement
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("customj48", options);
				return;
			}

			// If the user does not specify the dataset, we print an error message and we
			// exit
			if (!line.hasOption("d")) {
				System.err.println("The -d option is required to specify the dataset path");
				return;
			}

			// otherwise we specify it
			String dataset = line.getOptionValue("d");

			// Let's define the printwriter instance, console or file?
			PrintWriter writer;
			
			if (line.hasOption("f")) {
				writer = new PrintWriter(line.getOptionValue("f"));
			} else {
				writer = new PrintWriter(System.out);
				
			}

			boolean pruning = false; // we set the pruning to the default value

			// and we change it only if the -p flag has been specified
			if (line.hasOption("p"))
				pruning = true;

			ExportFormat export = ExportFormat.DOT; // We set the export format to our defualt value

			// and we change it only if the -e option has been specified

			if (line.hasOption("e")) {
				if (line.getOptionValue("e").equals("graphml"))
					export = ExportFormat.GRAPHML;
				else if (line.getOptionValue("e").equals("json"))
					export = ExportFormat.JSON;
			}

			// we have now all the information to create the decision tree and export it  based on the
			// specified options
			
			//System.out.println("Dataset path is " + dataset);
			DataSource source = new DataSource(dataset);
			
			Instances data = source.getDataSet();
			
			if (data.classIndex() == -1)
				data.setClassIndex(data.numAttributes() - 1);
			
			// Creating the tree object
			
			CustomJ48Tree tree = new CustomJ48Tree();

			// Setting options

			String[] treeOptions = weka.core.Utils.splitOptions("-M 1 -U");
			tree.setOptions(treeOptions);

			tree.buildClassifier(data); //Build the tree
			
			//Export it according to user's options
			switch(export) {
				
			case GRAPHML: 
				tree.exportGraphML(writer, pruning);
				break;
			case JSON: 
				tree.JSONExport(writer, pruning);
			default: 
				tree.dotExport(writer, pruning);
				break;
				
			}
			
		} catch (ParseException e) {
			// oops, something went wrong
			System.err.println("Parsing of the arguments failed. Reason: " + e.getMessage());
		}

		catch (FileNotFoundException e) {
			e.printStackTrace();

		} catch (Exception e) {

			e.printStackTrace();
		}

	}
}
