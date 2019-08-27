package inf.unibz.it.CustomJ48;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.Scanner;
import java.util.logging.LogManager;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToNominal;

/**
 * @author Davide Sbetti Main used to run the decision tree classifier on a
 *         specified dataset and then export the result in the dot format
 *
 */
public class CustomJ48 {

	public enum ExportFormat {
		DOT, JSON, GRAPHML;
	}
	
	public enum InputFormat{
		CSV, ARFF;
	}
	
	static InputFormat format;
	
	public static void main(String[] args) {

		LogManager.getLogManager().reset(); // Prevent the system from continuously writing logs to the console

		// Since it is a console application let's prepare the options that require an
		// argument
		// We need the user to specify the path of the dataset
		Option datasetPath = Option.builder("d").argName("dataset").hasArg().desc("Specifies the path of the dataset")
				.build();

		// Let's create the option to use a file instead the console stream for the
		// export code
		Option fileStream = Option.builder("f").argName("file").hasArg()
				.desc("Output the export code to the given file").build();

		// The 'e' option allows the user to decide the export format (graphml, dot and
		// json), default is dot
		Option exportFormat = Option.builder("e").argName("format").hasArg()
				.desc("specify the export format (dot, json). Default is: dot").build();

		// The 'e' option allows the user to decide the export format (graphml, dot and
		// json), default is dot
		Option inputFormat = Option.builder("i").argName("format").hasArg()
				.desc("input format for STDIN data set input (csv, arff). Default is: csv").build();

		// And now add them to the options array with the boolean ones (flags)
		Options options = new Options();
		options.addOption(datasetPath);
		options.addOption(fileStream);
		options.addOption(exportFormat);
		options.addOption(inputFormat);
		options.addOption("p", "Enable the pruning feature"); // Enable or no the pruning feature?
		options.addOption("r", "Replace empty strings (with _) to make them actual values"); // replace empty string
																								// with a value
		options.addOption("h", "Prints this help message"); // print the help message
		options.addOption("v", "Prints the software version"); // print software version

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

			// Print Software version
			if (line.hasOption("v")) {
				final Properties properties = new Properties(); //Create property object
				//Load the project properties from the associated file src/main/resources
				//it will cotain the software version extracted directly from the POM file 
				properties.load(CustomJ48.class.getClassLoader().getResourceAsStream("project.properties")); 
				System.out.println("CustomJ48 version " + properties.getProperty("version")); //print it
				return;
			}

			// Let's define the printwriter instance, console or file?
			PrintStream writer;

			if (line.hasOption("f")) {
				writer = new PrintStream(line.getOptionValue("f"), "UTF-8");
			} else {
				writer = new PrintStream(System.out, true, "UTF-8");

			}

			boolean pruning = false; // we set the pruning to the default value

			// and we change it only if the -p flag has been specified
			if (line.hasOption("p"))
				pruning = true;

			ExportFormat export = ExportFormat.DOT; // We set the export format to our defualt value

			// and we change it only if the -e option has been specified

			if (line.hasOption("e")) {
				if (line.getOptionValue("e").equals("graphml")) //change it to graphml
					export = ExportFormat.GRAPHML;
				else if (line.getOptionValue("e").equals("json")) //change it to json
					export = ExportFormat.JSON;
			}

			// prepare to read instances
			DataSource source;
			Instances data;
			boolean replace = false;
			
			if (line.hasOption("d")) { //if the input comes from a file

				String path = line.getOptionValue("d"); //take the path

				if (line.hasOption("r") && !DataSource.isArff(path)) { //if the user wants replace and file is CSV
					replace = true;
					
					String dataSet = read(new FileInputStream(path), replace, true); //read and replace

					data = getInstancesFromCSV(dataSet);
				
				} else { //otherwise we simply read the data set from the file
					source = new DataSource(path);
					data = source.getDataSet();
				}

			} else { //the input comes from STDIN
				
				format = InputFormat.CSV;
				
				if(line.hasOption("i") && line.getOptionValue("i").equals("arff")) //change default format if specified
					format = InputFormat.ARFF;
				
				if(line.hasOption("r") && format.equals(InputFormat.CSV)) //if user wants replace and file is CSV
					replace = true;
				
				System.err.println("Input your data set:"); //print message to the user on a different stream so we don't intefere in case of piping
				
				String dataSet = read(System.in, replace, false); //get dataset from function
				
				switch(format) { //check the format
					case ARFF: //if it is ARFF get the stream and let weka use the arff default loader
						ByteArrayInputStream in = new ByteArrayInputStream(dataSet.getBytes("utf-8"));
						source = new DataSource(in);
						data = source.getDataSet();
					break;
					
					default: //otherwise just use the CSV one
						data = getInstancesFromCSV(dataSet);
					break;
				}
				
			}

			// check for attributes read as string
			if (data.checkForStringAttributes()) {

				// Apply weka filter to convert them to nominal
				StringToNominal filter = new StringToNominal();
				filter.setAttributeRange("first-last");
				filter.setInputFormat(data);

				data = Filter.useFilter(data, filter);
			}

			if (data.classIndex() == -1) // Setting the class attribute to be last one if not explicitly set
				data.setClassIndex(data.numAttributes() - 1);

			// Creating the tree object
			
			CustomJ48Tree tree = new CustomJ48Tree();

			// Setting options

			String[] treeOptions = weka.core.Utils.splitOptions("-M 1 -U -O");
			tree.setOptions(treeOptions);

			tree.buildClassifier(data); // Build the tree
			

			// Export it according to user's options
			switch (export) {

			case GRAPHML:
				tree.exportGraphML(writer, pruning, replace);
				break;
			case JSON:
				tree.JSONExport(writer, pruning, replace);
				break;
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

	
	/**
	 * Function used to read a data set from STDIN or from a file (replacing underscores)
	 * 
	 * @param in Input stream used
	 * @param replaceEmptyStrings are we replacing underscores
	 * @return the data set as string
	 * @throws ParseException if there is already a single underscore as attribute
	 * @throws IOException 
	 */
	private static String read(InputStream in, boolean replaceEmptyStrings, boolean isFile) throws ParseException, IOException {

		// Read and replace the content of the dataset
		BufferedReader myScanner = new BufferedReader(new InputStreamReader(in, "UTF-8"));
		
		String newFile = "";
		int lineNumber = 0;
		
		Pattern p = Pattern.compile("\\b_\\b"); //create RegEx to find pre-existing underscores
		Matcher m = p.matcher("");  //create empty matcher so we can reuse the same without creating different objects, 
		//less memory is so used
		
		
		String fileLine = myScanner.readLine();
		while (fileLine != null) { //while we have another line

			lineNumber++;
			fileLine = fileLine.replaceAll("\\s*,\\s*", ",").trim(); // remove trailing and leading
																						// spaces
			if(replaceEmptyStrings) { //if we are replacing 
				m = m.reset(fileLine); //set the matcher to the current line
			
				if (m.find()) // if it already contains an underscore throw an exception
					throw new ParseException("Underscore character already found during replacement at line " + lineNumber);

				newFile += fileLine.replaceAll(",,", ",_,") + "\n"; // otherwise replace empty strings with underscore
			} else
				newFile += fileLine + "\n"; //add it to content
			
			fileLine = myScanner.readLine();
		}
		
		myScanner.close();

		return newFile; //return the data set
	}
	
	/**
	 * Function used to get instances from a string representing a CSV file
	 * @param file string representing the data set
	 * @return an Instances object containing all instances
	 * @throws IOException if the loader fails
	 */
	private static Instances getInstancesFromCSV(String file) throws IOException {
		
		ByteArrayInputStream in = new ByteArrayInputStream(file.getBytes("utf-8")); // create the input
		// stream from the new
		// content

		CSVLoader csv = new CSVLoader(); // load it as CSV
		csv.setSource(in);

		return csv.getDataSet(); // get the data set
	}

}
