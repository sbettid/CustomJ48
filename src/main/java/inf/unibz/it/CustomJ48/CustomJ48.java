package inf.unibz.it.CustomJ48;



import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;

import java.io.PrintWriter;
import java.util.Scanner;
import java.util.logging.LogManager;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import weka.core.Instances;
import weka.core.converters.CSVLoader;
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
		options.addOption("r", "Replace empty strings (with _) to make them actual values"); //replace empty string with a value
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

			
			boolean replace = false; //Replace will be performed here but the back substitution in the export function
			
			DataSource source;
			Instances data;
			
			//We make the missing string a value (_) if the associated option has been activated and the data set is in CSV format
			if(line.hasOption("r") && !DataSource.isArff(dataset)) {
				
					replace = true;
				
					//Read and replace the content of the dataset
					Scanner myScanner = new Scanner(new File(dataset), "utf-8");
					String newFile = "";
					int lineNumber = 0;
					while(myScanner.hasNextLine()) {
						
						lineNumber++;
						String fileLine = myScanner.nextLine().replaceAll("\\s*,\\s*", ",").trim(); //remove trailing and leading spaces
						
						if(fileLine.contains(",_,")) //if it already contains an underscore throw an exception
							throw new ParseException("Underscore character already found during replacement at line " + lineNumber);
						
						newFile += fileLine.replaceAll(",,", ",_,") + "\n";	//otherwise replace empty strings with underscore
					}
					
					ByteArrayInputStream in = new ByteArrayInputStream(newFile.getBytes("utf-8")); //create the input stream from the new content
					
					
					CSVLoader csv = new CSVLoader(); //load it as CSV
					csv.setSource(in);
					
					data = csv.getDataSet(); //get the data set
					
					myScanner.close();
					
			} else { //otherwise we just use the given data set
				source = new DataSource(dataset);
				data = source.getDataSet();
			}
			
			
			if (data.classIndex() == -1) //Setting the last attribute to be last one if not explicitly set
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
				tree.exportGraphML(writer, pruning, replace);
				break;
			case JSON: 
				tree.JSONExport(writer, pruning, replace);
			default: 
				tree.dotExport(writer, pruning, replace);
				break;
				
			}
			
		} catch (ParseException e) {
			
			System.err.println("Parsing of the arguments failed. Reason: " + e.getMessage());
		}

		catch (FileNotFoundException e) {
			e.printStackTrace();

		} catch (Exception e) {

			e.printStackTrace();
		}

	System.exit(0);
	
	}
	
	
}
