package inf.unibz.it.CustomJ48;


import java.io.IOException;
import java.io.PrintWriter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

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
public class CustomJ48Tree extends J48 {

	// Id used to uniquely identify every node in the tree
	private int id = 1;
	private Document document;

	// #########################################################################################
	// ## ##
	// ## EXPORT IN THE GRAPHML FORMAT ##
	// ## ##
	// #########################################################################################

	/**
	 * Method to export the given tree in the GraphML format.
	 * 
	 * @param writer The writer instance for the output
	 * @param pruning  boolean value representing if we would like to apply our
	 *                 pruning criteria or not
	 * @param replace  boolean value representing if we are replacing back underscore                
	 * @throws ParserConfigurationException  For misconfiguration
	 * @throws TransformerException  If the XML Transformer is not able to write the XML file
	 */
	public void exportGraphML(PrintWriter writer, boolean pruning, boolean replace) throws ParserConfigurationException, TransformerException {

		
		 DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance(); //get a document factory instance
		 
         DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder(); //get a document builder instance
         
         //get the document
         document = documentBuilder.newDocument();
       //Create the root element (graphml) with all the references to the schemas
         Element root = document.createElement("graphml");
         root.setAttribute("xmlns", "http://graphml.graphdrawing.org/xmlns");
         root.setAttribute("xmlns:java", "http://www.yworks.com/xml/yfiles-common/1.0/java");
         root.setAttribute("xmlns:sys", "http://www.yworks.com/xml/yfiles-common/markup/primitives/2.0");
         root.setAttribute("xmlns:bpmn", "http://www.yworks.com/xml/yfiles-for-html/bpmn/2.0");
         root.setAttribute("xmlns:x", "http://www.yworks.com/xml/yfiles-common/markup/2.0");
         root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
         root.setAttribute("xmlns:y", "http://www.yworks.com/xml/graphml");
         root.setAttribute("xmlns:yed", "http://www.yworks.com/xml/yed/3");
         root.setAttribute("xsi:schemaLocation", "http://graphml.graphdrawing.org/xmlns http://www.yworks.com/xml/schema/graphml/1.1/ygraphml.xsd");
       
         document.appendChild(root);
 		
         // Append the keys, so the types of the variables used to store labels
 		// attributes and descriptions
		Element graphmlKey = document.createElement("key"); //Create the label key for yEd
		graphmlKey.setAttribute("id", "d7");
		graphmlKey.setAttribute("for", "graphml");
		graphmlKey.setAttribute("yfiles.type", "resources");
		
		root.appendChild(graphmlKey);
		
				
		Element descKey = document.createElement("key"); //create the description key, standard graphml 
		descKey.setAttribute("id", "desc");
		descKey.setAttribute("for", "all");
		descKey.setAttribute("attr.name", "description");
		root.appendChild(descKey);
		
		Element labKey = document.createElement("key"); //create the node key
		labKey.setAttribute("id", "d6");
		labKey.setAttribute("for", "node");
		labKey.setAttribute("yfiles.type", "nodegraphics");
		root.appendChild(labKey);
		
		Element edgeKey = document.createElement("key"); //create the edge key
		edgeKey.setAttribute("id", "d10");
		edgeKey.setAttribute("for", "edge");
		edgeKey.setAttribute("yfiles.type", "edgegraphics");
		root.appendChild(edgeKey);
		
		Element graph = document.createElement("graph"); //Create the graph element
		graph.setAttribute("edgedefault", "directed");
		root.appendChild(graph);
		
		//Create the root node structure, label will be added later in the if  
		Element treeRoot = document.createElement("node");
		treeRoot.setAttribute("id", "0");
		graph.appendChild(treeRoot);
		//Create the label structure
		Element dataLab = document.createElement("data");
		dataLab.setAttribute("key", "d6");
		
		Element xList = document.createElement("y:ShapeNode");
		
		
		// If the root is a leaf 
		if (m_root.isLeaf()) {
			try {
				//Get the right label
				String text = m_root.getLocalModel().dumpLabel(0, m_root.getTrainingData());
				//Add it to the node
				Element yLabelText = document.createElement("y:NodeLabel");
				
				yLabelText.appendChild(document.createTextNode(text));
				xList.appendChild(yLabelText);
				dataLab.appendChild(xList);
				treeRoot.appendChild(dataLab);
				
				//Create also the standard GraphML description
				Element dataDesc = document.createElement("data");
				dataDesc.setAttribute("key", "desc");
				dataDesc.appendChild(document.createTextNode(text));
				
				treeRoot.appendChild(dataDesc); //append the node and terminate, it was a leaf
			
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else { // print the value of the root and then start printing the tree structure

			//Get the right label
			String text = m_root.getLocalModel().leftSide(m_root.getTrainingData());
			
			//Add it to the structure
			Element yLabelText = document.createElement("y:NodeLabel");
			yLabelText.appendChild(document.createTextNode(text));
			xList.appendChild(yLabelText);
			
			
			dataLab.appendChild(xList);
			treeRoot.appendChild(dataLab);
			//Add also the description
			Element dataDesc = document.createElement("data");
			dataDesc.setAttribute("key", "desc");
			dataDesc.appendChild(document.createTextNode(text));
			
			treeRoot.appendChild(dataDesc);
			
			// traverse the tree recurring on the sons
			exportGraphML(m_root, 0, graph, pruning, replace);

		}
		
		// Writing everything on the user's specified file
		
		TransformerFactory transformerFactory = TransformerFactory.newInstance(); //get the transformere
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //Forma the file with new lines and indentations
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        
        DOMSource domSource = new DOMSource(document); //give the source
        StreamResult streamResult = new StreamResult(writer); //give the destination

        transformer.transform(domSource, streamResult); //write

		// Resetting variables and close stream
		id = 1;
		writer.close();
		
		// Confirm everything went smooth
		System.out.println("GraphML export completed!");
	}

	/**
	 * Private helper method using to export the sons of the root after it has been
	 * printed.
	 * 
	 * @param currentNode The current node analyzed, at the beginning the root node
	 * @param parentId    The id of the parent node
	 * @param graph       DOM element representing the final graph, used to add nodes
	 * @param pruning	  boolean value representing if we are pruning branches or not	  
	 * @param replace	  boolean value representing if we are replacing back underscores or not
	 */
	private void exportGraphML(ClassifierTree currentNode, int parentId, Element graph, boolean pruning, boolean replace) {

		try {

			// Get the sons of the current node
			ClassifierTree[] sons = currentNode.getSons();

			// Get the model and the data at the current node
			ClassifierSplitModel localModel = currentNode.getLocalModel();
			Instances trainingData = currentNode.getTrainingData();

			// Analyze every son
			for (int i = 0; i < sons.length; i++) {

				double nInstances = sons[i].getLocalModel().distribution().total();
				
				//If we are not pruning or there are some instances
				if (!pruning || nInstances > 0) {
					
					// Create the node element with the id
					Element current = document.createElement("node");
					current.setAttribute("id", id + "");
					graph.appendChild(current);
					//Create the label structure
					Element dataLab = document.createElement("data");
					dataLab.setAttribute("key", "d6");
					
					Element xList = document.createElement("y:ShapeNode");
					
					if (sons[i].isLeaf()) { // if the son is a leaf

						String label = localModel.dumpLabel(i, trainingData); // get the class label

						//Add the class label to the label structure
						Element yLabelText = document.createElement("y:NodeLabel");
						yLabelText.appendChild(document.createTextNode(label));
						xList.appendChild(yLabelText);
						
						//Add everything to the node
						
						dataLab.appendChild(xList);
						current.appendChild(dataLab);
						
						//Create also the description with the same class label for cross compatibility
						Element dataDesc = document.createElement("data");
						dataDesc.setAttribute("key", "desc");
						dataDesc.appendChild(document.createTextNode(label));
						
						current.appendChild(dataDesc);
						
						graph.appendChild(current); //add node to graph
						
						// Writing edge between current node and its son
						writeEdge(localModel, trainingData, parentId, i, graph, replace);

						id++; // increment the node id
					} else {
						
						//get the correct label
						String label = sons[i].getLocalModel().leftSide(sons[i].getTrainingData());

						//Add the label to the structure 
						Element yLabelText = document.createElement("y:NodeLabel");
						yLabelText.appendChild(document.createTextNode(label));
						xList.appendChild(yLabelText);
						
						//Append everything to the node
						
						dataLab.appendChild(xList);
						current.appendChild(dataLab);
						
						//Write description for cross compatibility
						Element dataDesc = document.createElement("data");
						dataDesc.setAttribute("key", "desc");
						dataDesc.appendChild(document.createTextNode(label));
						
						current.appendChild(dataDesc);
						
						graph.appendChild(current);//add node to graph

						// Writing edge between current node and its son
						writeEdge(localModel, trainingData, parentId, i, graph, replace);

						id++; // increment the node id

						exportGraphML(sons[i], id - 1, graph, pruning, replace); // recur on the sons
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
	 * @param graph        DOM element representing the final graph
	 * @param replace	   Boolean value representing if we are replacing back underscores or not
	 */
	private void writeEdge(ClassifierSplitModel localModel, Instances trainingData, int parentId, int current,
			Element graph, boolean replace) {
		
		String labelText = localModel.rightSide(current, trainingData).trim(); //taking edge label
		
		if(replace) { //if we are replacing
			
			labelText = replace_underscore(labelText); //get the resulting label from the replacing function
		}
			
		// Writing edge between current node and its son
		//Create the element with the ids of source and destination
		Element edge = document.createElement("edge");
		edge.setAttribute("id", "e" + id);
		edge.setAttribute("source", "" + parentId);
		edge.setAttribute("target", "" + id);
		//create the data label structure
		Element dataLab = document.createElement("data");
		dataLab.setAttribute("key", "d10");
		
		Element xList = document.createElement("y:PolyLineEdge");
		
		//Add the label text
		Element yLabelText = document.createElement("y:EdgeLabel");
		yLabelText.appendChild(document.createTextNode(labelText));
		
		//add everything to the node
		xList.appendChild(yLabelText);
		dataLab.appendChild(xList);
		edge.appendChild(dataLab);
		
		//Alwasy standard description for cross compatibility
		Element dataDesc = document.createElement("data");
		dataDesc.setAttribute("key", "desc");
		dataDesc.appendChild(document.createTextNode(labelText));
		edge.appendChild(dataDesc);
		
		graph.appendChild(edge); //add edge to graph
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
	public void dotExport(PrintWriter writer, boolean pruning, boolean replace) {
		try {
			// Export the tree
			StringBuffer exportResult = dotExport(pruning, replace);

			//writeOnStream(writer, exportResult, escapeChars);
			writer.print(exportResult);
			writer.close();
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
	private StringBuffer dotExport(boolean pruning, boolean replace) throws Exception {

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
			dotExport(m_root, 0, text, pruning, replace);
		}
		
		text.append("}\n");
		
		return text;
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
	private void dotExport(ClassifierTree currentNode, int parentId, StringBuffer text, boolean pruning, boolean replace)
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
					
					String labelText = Utils.backQuoteChars(localModel.rightSide(i, trainingData).trim());
					
					if(replace)
						labelText = replace_underscore(labelText);
				
					text.append("N" + parentId + "->" + "N" + id + " [label=\""
							+ labelText + "\"]\n");

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

					
						// otherwise we recur on the sons
						text.append("N" + id + " [label=\""
								+ Utils.backQuoteChars(sons[i].getLocalModel().leftSide(trainingData)) + "\" ");
						if (trainingData != null && trainingData.numInstances() > 0) {
							text.append("data =\n" + sons[i].getTrainingData() + "\n");
							text.append(",\n");
						}
						text.append("]\n");
						id++;
						
						dotExport(sons[i], id - 1, text, pruning, replace);
					
				}
			}
		}
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
	public void JSONExport(PrintWriter writer, boolean pruning, boolean replace) {

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
				JSONExport(m_root, text, pruning, replace);

				text.append("]");
			}

			text.append("}");

			//writeOnStream(writer, text, escapeChars);
			
			writer.println(text);
			writer.close();
			
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
	private void JSONExport(ClassifierTree currentNode, StringBuffer text, boolean pruning, boolean replace) throws Exception {

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
				
				if(replace)
					tempLabel = replace_underscore(tempLabel);
				
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

					//and we analyze the son
					JSONExport(sons[i], text, pruning, replace);

					text.append("]");
				}
				text.append("}");
			}
		}
	}
	
	
	/*
	 * UTILITY FUNCTIONS
	 */
	
	/**
	 * Utility function used to replace the underscore character used to represent the empty string when we use the replacing function
	 * @param text text we would like to replace
	 * @return the replaced text
	 */
	private String replace_underscore(String text) {
		String result = text;
		
		if(result.equals("= _") )
			result = " = ";
		
		return result;
	}

}
