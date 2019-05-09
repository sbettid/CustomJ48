package inf.unibz.it.CustomJ48;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

import weka.classifiers.trees.J48;
import weka.classifiers.trees.j48.ClassifierSplitModel;
import weka.classifiers.trees.j48.ClassifierTree;
import weka.core.Instances;
import weka.core.Utils;

/**
 * 
 * @author Davide Sbetti
 * 
 *         Extension of the weka J48 implementation of the C4.5 algorithm. Used
 *         to allow the user to export the tree in the GraphML or dot format,
 *         with some graphical adjustement for the yED editor (when using
 *         GraphML), using the DFS strategy. (browser live version at
 *         https://www.yworks.com/yed-live/).
 * 
 *         Moreover, the user can apply a pruning actions that removes all the
 *         branches not reached by any instance contained in the training data
 *         set.
 * 
 *         Please note how the data contained in the yED label attribute are
 *         also contained in the description, to ensure that also other editors
 *         can somehow present it to the user since it is an attribute defined
 *         in the specification of GraphML.
 * 
 * 
 *         The dot format is used in its standard specification and therefore it
 *         is compatible with all dot visualizers
 */
public class CustomJ48 extends J48 {

	// Id used to uniquely identify every node in the tree
	private int id = 1;

	// #########################################################################################
	// ## ##
	// ## EXPORT IN THE GRAPHML FORMAT ##
	// ## ##
	// #########################################################################################

	/**
	 * Method to export the given tree in the GraphML format.
	 * 
	 * @param filepath path of the output file
	 * @param pruning  boolean value representing if we would like to apply our
	 *                 pruning criteria or not
	 */
	public void exportGraphML(String filepath, boolean pruning) {

		// String builder used to store the content of the exported file
		StringBuilder outputText = new StringBuilder();

		// Writing graph attributes and head of the document
		outputText.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");

		outputText.append(
				"<graphml xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns http://www.yworks.com/xml/schema/graphml.html/2.0/ygraphml.xsd \" xmlns=\"http://graphml.graphdrawing.org/xmlns\" xmlns:demostyle=\"http://www.yworks.com/yFilesHTML/demos/FlatDemoStyle/1.0\" xmlns:bpmn=\"http://www.yworks.com/xml/yfiles-for-html/bpmn/2.0\" xmlns:demotablestyle=\"http://www.yworks.com/yFilesHTML/demos/FlatDemoTableStyle/1.0\" xmlns:uml=\"http://www.yworks.com/yFilesHTML/demos/UMLDemoStyle/1.0\" "
						+ "xmlns:compat=\"http://www.yworks.com/xml/yfiles-compat-arrows/1.0\" xmlns:GraphvizNodeStyle=\"http://www.yworks.com/yFilesHTML/graphviz-node-style/1.0\" xmlns:VuejsNodeStyle=\"http://www.yworks.com/demos/yfiles-vuejs-node-style/1.0\" xmlns:y=\"http://www.yworks.com/xml/yfiles-common/3.0\" xmlns:x=\"http://www.yworks.com/xml/yfiles-common/markup/3.0\" xmlns:yjs=\"http://www.yworks.com/xml/yfiles-for-html/2.0/xaml\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n");

		// Append the keys, so the types of the variables used to store labels
		// attributes and descriptions
		outputText.append(
				"<key id=\"lab\" for=\"node\" attr.name=\"NodeLabels\" y:attr.uri=\"http://www.yworks.com/xml/yfiles-common/2.0/NodeLabels\"/>\n");
		outputText.append(
				"<key id=\"edgeLab\" for=\"edge\" attr.name=\"EdgeLabels\" y:attr.uri=\"http://www.yworks.com/xml/yfiles-common/2.0/EdgeLabels\"/>\n");
		outputText.append("<key id=\"desc\" for=\"all\" attr.name=\"description\" />\n");

		// We analyze first the root and, if it is not a leaf, then we call the helper
		// method to visit
		// the children of the current node, recursing so on the structure

		// If the root is a leaf print it directly
		if (m_root.isLeaf()) {
			try {
				outputText.append("<node id=\"0\">\n");
				outputText.append("\t<data key=\"lab\">\n");
				outputText.append("\t\t<x:List>\n\t\t\t<y:Label>\n");
				outputText.append("\t\t\t<y:Label.Text>" + m_root.getLocalModel().dumpLabel(0, m_root.getTrainingData())
						+ "</y:Label.Text>\n");
				outputText.append("\t\t\t</y:Label>\n\t\t</x:List>\n");
				outputText.append("\t</data>\n");
				outputText.append("\t<data key=\"desc\">"
						+ m_root.getLocalModel().dumpLabel(0, m_root.getTrainingData()) + "</data>\n");
				outputText.append("</node>\n");

			} catch (Exception e) {
				e.printStackTrace();
			}
		} else { // print the value of the root and then start printing the tree structure

			// Append node description and label
			outputText.append("<node id=\"0\">\n");
			outputText.append("\t<data key=\"lab\">\n");
			outputText.append("\t\t<x:List>\n\t\t\t<y:Label>\n");
			outputText.append("\t\t\t<y:Label.Text>" + m_root.getLocalModel().leftSide(m_root.getTrainingData())
					+ "</y:Label.Text>\n");
			outputText.append("\t\t\t</y:Label>\n\t\t</x:List>\n");
			outputText.append("\t</data>\n");
			outputText.append(
					"\t<data key=\"desc\">" + m_root.getLocalModel().leftSide(m_root.getTrainingData()) + "</data>\n");
			outputText.append("</node>\n");

			// traverse the tree recurring on the sons
			exportGraphML(m_root, 0, outputText, pruning);

		}

		outputText.append("</graphml>\n");

		// Writing everything on the user's specified file
		try {
			PrintWriter myPrinter = new PrintWriter(new FileWriter(filepath));

			myPrinter.print(outputText);

			myPrinter.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

		// Resetting variables
		id = 1;

		// Confirm everything went smooth
		System.out.println("GraphML export on the file " + filepath + " completed!");
	}

	/**
	 * Private helper method using to export the sons of the root after it has been
	 * printed.
	 * 
	 * @param currentNode The current node analyzed, at the beginning the root node
	 * @param parentId    The id of the parent node
	 * @param sb          The string buffer used in the previous exportGraphML
	 *                    method to append the result of the current visit
	 */
	private void exportGraphML(ClassifierTree currentNode, int parentId, StringBuilder sb, boolean pruning) {

		try {

			// Get the sons of the current node
			ClassifierTree[] sons = currentNode.getSons();

			// Get the model and the data at the current node
			ClassifierSplitModel localModel = currentNode.getLocalModel();
			Instances trainingData = currentNode.getTrainingData();

			// Analyze every son
			for (int i = 0; i < sons.length; i++) {

				double nInstances = sons[i].getLocalModel().distribution().total();
				// System.out.println("At the node " + label + ": " + nInstances + "
				// instances");

				if (!pruning || nInstances > 0) {

					if (sons[i].isLeaf()) { // if the son is a leaf

						String label = localModel.dumpLabel(i, trainingData); // get the class label

						// System.out.println("At the node " + label + ": " + nInstances + "
						// instances");

						// Writing the leaf node and both its label/description for cross compatibility
						sb.append("<node id=\"" + id + "\">\n");
						sb.append("\t<data key=\"lab\">\n");
						sb.append("\t\t<x:List>\n\t\t\t<y:Label>\n");
						sb.append("\t\t\t<y:Label.Text>" + label + "</y:Label.Text>\n");
						sb.append("\t\t\t</y:Label>\n\t\t</x:List>\n");
						sb.append("\t</data>\n");
						sb.append("\t<data key=\"desc\">" + label + "</data>\n");
						sb.append("</node>\n");

						// Writing edge between current node and its son
						writeEdge(localModel, trainingData, parentId, i, sb);

						id++; // increment the node id
					} else {

						String label = sons[i].getLocalModel().leftSide(sons[i].getTrainingData());

						// System.out.println("At the node " + label + ": " + nInstances + "
						// instances");

						// Writing a non-leaf node and both its label/description for cross
						// compatibility
						sb.append("<node id=\"" + id + "\">\n");
						sb.append("\t<data key=\"lab\">\n");
						sb.append("\t\t<x:List>\n\t\t\t<y:Label>\n");
						sb.append("\t\t\t<y:Label.Text>" + label + "</y:Label.Text>\n");
						sb.append("\t\t\t</y:Label>\n\t\t</x:List>\n");
						sb.append("\t</data>\n");
						sb.append("\t<data key=\"desc\">" + label + "</data>\n");
						sb.append("</node>\n");

						// Writing edge between current node and its son
						writeEdge(localModel, trainingData, parentId, i, sb);

						id++; // increment the node id

						exportGraphML(sons[i], id - 1, sb, pruning); // recur on the son
					}
				}
			}

		} catch (Exception e) {

			e.printStackTrace();
		}

	}

	/**
	 * Private method used to write the edge from the current node to its parent,
	 * used both for leaf and non-leaf nodes
	 * 
	 * @param localModel   The local model of the current node
	 * @param trainingData The training data at the current node
	 * @param parentId     The id of the parent node
	 * @param current      The id of the current node
	 * @param sb           The string builder used to append the edge
	 */
	private void writeEdge(ClassifierSplitModel localModel, Instances trainingData, int parentId, int current,
			StringBuilder sb) {

		// Writing edge between current node and its son
		sb.append("<edge id=\"e" + id + " \" source=\"" + parentId + "\" target=\"" + id + "\" >\n");
		sb.append("\t<data key=\"edgeLab\">\n");
		sb.append("\t\t<x:List>\n\t\t\t<y:Label>\n");
		sb.append("\t\t\t<y:Label.Text>" + localModel.rightSide(current, trainingData) + "</y:Label.Text>\n");
		sb.append("\t\t\t</y:Label>\n\t\t</x:List>\n");
		sb.append("\t</data>\n");
		sb.append("\t<data key=\"desc\">" + localModel.rightSide(current, trainingData) + "</data>\n");
		sb.append("</edge>\n");
	}

	// #########################################################################################
	// ## ##
	// ## EXPORT IN THE DOT FORMAT ##
	// ## ##
	// #########################################################################################

	/**
	 * Method used to export the built tree in the dot format, with the possibility
	 * to prune branches reached by no instances of the training data.
	 * 
	 * @param filepath Output file path
	 * @param pruning  Whether we would like to prune branches or not
	 */
	public void dotExport(String filepath, boolean pruning) {
		try {
			// Export the tree
			String exportResult = dotExport(pruning);

			// Writing everything on the user's specified file
			PrintWriter myPrinter = new PrintWriter(new FileWriter(filepath));

			myPrinter.print(exportResult);

			myPrinter.close(); // Close the file and return

			System.out.println("Dot export completed successfully");

		} catch (IOException io) {
			System.out.println("Error in writing on the output file: " + io.getStackTrace());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Helper method used to export the tree in the dot format. It writes the root
	 * node and then calls another helper method that will recurse on the children,
	 * pruning the branches reached by no instances.
	 * 
	 * @param pruning Whether we would like to prune or not.
	 * @return returns a string containing the exported tree in the dot format
	 * @throws Exception if something goes wrong
	 */
	private String dotExport(boolean pruning) throws Exception {

		// Create a new string buffer to append all information about the tree
		StringBuffer text = new StringBuffer();
		// Get the model of the root
		ClassifierSplitModel model = m_root.getLocalModel();

		// Start writing the root node
		text.append("digraph J48Tree {\n");
		// If it is a leaf
		if (m_root.isLeaf()) {

			// we append the node
			text.append("N0 [label=\"" + Utils.backQuoteChars(model.dumpLabel(0, m_root.getTrainingData())) + "\" "
					+ "shape=box style=filled ");
			if (m_root.getTrainingData() != null && m_root.getTrainingData().numInstances() > 0) { // with its
																									// information
				text.append("data =\n" + m_root.getTrainingData() + "\n");
				text.append(",\n");
				// and we are done
			}
			text.append("]\n");
		} else {
			// if it is not a leaf, we append the node and its information
			text.append("N0 [label=\"" + Utils.backQuoteChars(model.leftSide(m_root.getTrainingData())) + "\" ");
			if (m_root.getTrainingData() != null && m_root.getTrainingData().numInstances() > 0) {
				text.append("data =\n" + m_root.getTrainingData() + "\n");
				text.append(",\n");
			}
			text.append("]\n");
			// and we call the recursive method on the root to export its sons
			// TODO: change here, we recur only on a son that either is a leaf or has more
			// than one option,
			// otherwise we skip to the next one
			dotExport(m_root, 0, text, pruning);
		}

		return text.toString() + "}\n";
	}

	/**
	 * Helper method that recurs on the children node of the current node, exporting
	 * them in the dot format.
	 * 
	 * @param currentNode The current node analyzed
	 * @param parentId    The id of the parent nodes
	 * @param text        String buffer used to append the information about this
	 *                    subtree
	 * @param pruning     Whether we are pruning or not.
	 * @throws Exception If something goes wrong
	 */
	private void dotExport(ClassifierTree currentNode, int parentId, StringBuffer text, boolean pruning)
			throws Exception {
		// get the sons of the current node
		ClassifierTree[] sons = currentNode.getSons();

		// Get the model and the data at the current node
		ClassifierSplitModel localModel = currentNode.getLocalModel();
		Instances trainingData = currentNode.getTrainingData();

		for (int i = 0; i < sons.length; i++) { // export each son and corresponding subtree

			double nInstances = sons[i].getLocalModel().distribution().total(); // get the number of instances at the
																				// node

			if (!pruning || nInstances > 0) { // if we are pruning we check the number of instances in the subtree

				// System.out.println("At the current node : " + nInstances + "instances");

				// We write the node with its id only if the son is either a leaf or has more
				// than one child,
				// otherwise we set the skip variable to true

				boolean skip = false; // Are we going to skip the son to go directly to the grandson?
				
				if (pruning && !sons[i].isLeaf()) {
					
					if (notEnoughChildren(sons[i])) {
						skip = true;
					}
					
					//System.out.println("DEBUG: enough children? " + notEnoughChildren(sons[i]) );
				}

				if (!skip)
					text.append("N" + parentId + "->" + "N" + id + " [label=\""
							+ Utils.backQuoteChars(localModel.rightSide(i, trainingData).trim()) + "\"]\n");

				// and its information

				if (sons[i].isLeaf()) {
					// If it is a leaf after writing its information we are done
					text.append("N" + id + " [label=\"" + Utils.backQuoteChars(localModel.dumpLabel(i, trainingData))
							+ "\" " + "shape=box style=filled ");
					if (trainingData != null && trainingData.numInstances() > 0) {
						text.append("data =\n" + sons[i].getTrainingData() + "\n");
						text.append(",\n");
					}
					text.append("]\n");
					id++;
				} else {

					if (!skip) {
						// otherwise we recur on the sons
						text.append("N" + id + " [label=\""
								+ Utils.backQuoteChars(sons[i].getLocalModel().leftSide(trainingData)) + "\" ");
						if (trainingData != null && trainingData.numInstances() > 0) {
							text.append("data =\n" + sons[i].getTrainingData() + "\n");
							text.append(",\n");
						}
						text.append("]\n");
						id++;
						
						dotExport(sons[i], id - 1, text, pruning);
					} else {
					
						// TODO: changed here, if skip is true we use as parent id the current one
						// otherwise it is okay like that
						dotExport(sons[i], parentId, text, pruning);
					}
				}
			}
		}
	}
	
	/**
	 * Method used to test if more than one child will survive the pruning process, this affects the tree structure
	 * since we try to short-circuit such paths
	 * @param node The node we are testing, checking its children
	 * @return true if more than one child of node will survive, false otherwise
	 */
	private boolean notEnoughChildren(ClassifierTree node) {
		
		int nSurvivors = 0;
		ClassifierTree[] sons = node.getSons();
		for(int i = 0; i < sons.length; i++) {
			
			if(sons[i].getLocalModel().distribution().total() > 0)
				nSurvivors++;
			
			if(nSurvivors > 1)
				return true;
		}
		
		return false;
		
	}

	// #########################################################################################
	// ## ##
	// ## EXPORT IN JSON ##
	// ## (for chatbot creation purposes) ##
	// #########################################################################################

	/**
	 * Method used, as the previous ones, to export the tree in a particular format
	 * (github link will follow), giving the chance to prune subtrees not reached by
	 * any instance. This time we use a particular JSON format, in order to pass the
	 * exported file to the Node.js application in charge of creating the chatbot.
	 * 
	 * @param filepath Path of the output file
	 * @param pruning  Whether we would like to prune subtrees
	 * @throws Exception If something goes wrong
	 */
	public void JSONExport(String filepath, boolean pruning) {

		try {
			StringBuffer text = new StringBuffer();
			// Get the model of the root
			ClassifierSplitModel model = m_root.getLocalModel();

			text.append("{");

			// Writing the root
			if (m_root.isLeaf()) {
				// we append the node
				text.append(
						"\"label\": \"" + Utils.backQuoteChars(model.dumpLabel(0, m_root.getTrainingData())) + "\"");

			} else {
				// if it is not a leaf, we append the node and its information
				text.append("\"label\":\"" + Utils.backQuoteChars(model.leftSide(m_root.getTrainingData())) + "\" ");
				text.append(", \"children\" : [");

				// and we call the recursive method on the root to export its sons
				JSONExport(m_root, text, pruning);

				text.append("]");
			}

			text.append("}");

			// Writing everything on the user's specified file
			PrintWriter myPrinter = new PrintWriter(new FileWriter(filepath));

			myPrinter.print(text);

			myPrinter.close(); // Close the file and return

			System.out.println("JSON export completed successfully");

		} catch (IOException io) {
			System.out.println("Error while writing on the output file: " + io.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Helper method used to export the sons of the current node, if they satisfy
	 * the pruning condition
	 * 
	 * @param currentNode Current node analyzed
	 * @param text        String buffer used to append the exported nodes
	 * @param pruning     Whether we would like to prune or not
	 * @throws Exception If something goes wrong
	 */
	private void JSONExport(ClassifierTree currentNode, StringBuffer text, boolean pruning) throws Exception {

		// get the sons of the current node
		ClassifierTree[] sons = currentNode.getSons();

		// Get the model and the data at the current node
		ClassifierSplitModel localModel = currentNode.getLocalModel();
		Instances trainingData = currentNode.getTrainingData();

		for (int i = 0; i < sons.length; i++) { // export each son and corresponding subtree

			double nInstances = sons[i].getLocalModel().distribution().total(); // get the number of instances at the
																				// node

			if (!pruning || nInstances > 0) { // if we are pruning we check the number of instances in the subtree

				text.append(i > 0 ? ",{" : "{");
				String tempLabel = Utils.backQuoteChars(localModel.rightSide(i, trainingData).trim());

				String edgeLabel = tempLabel.replaceAll("^= ", "");

				text.append("\"edgeLabel\" : \"" + edgeLabel + "\"");

				if (sons[i].isLeaf()) {
					// If it is a leaf after writing its information we are done
					text.append(",\"label\" : \"" + Utils.backQuoteChars(localModel.dumpLabel(i, trainingData)) + "\"");

				} else {
					// otherwise we recur on the sons
					text.append(",\"label\" : \"" + Utils.backQuoteChars(sons[i].getLocalModel().leftSide(trainingData))
							+ "\" ");

					text.append(",\"children\": [");

					// TODO: changes should be performed here
					// if the son has only one alternative surviving pruning let's recur directly on
					// him
					JSONExport(sons[i], text, pruning);

					text.append("]");
				}
				text.append("}");
			}
		}
	}
}
