# Custom J48

Custom J48 is an extension of the J48 class of the [Weka library](https://www.cs.waikato.ac.nz/ml/weka/) part of a Bachelor thesis project. J48 is a java implementation of the C4.5 machine learning algorithm.

The goal of this extension was to add the ability to export the generated decision trees in multiple formats, used then for the creation of a chatbot application, adding also a pruning ability. 

## Export formats

Trees generated using the J48 class can be exported in three different formats:

- GraphML format, whose specification can be found [here](http://graphml.graphdrawing.org/), with some graphical adjustments that allow a correct visualization using the yED editor (accessible also in a [browser live version](https://www.yworks.com/yed-live/)). 
- DOT format, used also as default by Weka
- JSON format, based on the following specification: 

    `{label: “node1”, children: [{ edgeLabel: “to_node_2”, label: node_2 }, { edgeLabel: “to_node_3”,       label: “node_3”, children […]  }]}`

    The tree is specified in a recursive way, where each node, except the leafs, contains the list of      its children nodes. Moreover, every node has a label attribute specifying the attribute used to split the data at the given point. All nodes (except the root) have an edgeLabel property, which represents the label of the edge from the parent node.


Independently from the chosen export format, it is possible to activate the pruning capability. 

## Pruning feature

In the scope of our project, the pruning capability was necessary because the algorithm, given an attribute, adds an edge labelled with any value that it is able to find for the given attribute in the whole dataset, although possibly no instances reach that node, an intended behaviour used by C4.5 to avoid overfitting. 

However, in our chatbot creation case, we assumed that the data set contains all possible legal combinations and therefore, such branches, were not desired and would have led to a misleading tree. For this reason, the different export functions that were encoded allow a parameter that enables the pruning of branches reached by no instances as soon as they are discovered.


## Installation

There are two ways to use this application: 

1. If you do not need to modify the sources of the application you can simply use the compiled JAR that you can find 
here on GitLab at the following address [address goes here]. Once downloaded, you can simply launch it with the java command `java -jar customJ48.jar -d your_dataset` specifying after the name of the file the desired options (more on that in the [Usage section](#usage)).

2. On the other hand, if you would like to modify the source code, feel free to do it! The J48 application requires Weka 3.8.3 stable. Dependencies have all been managed using Maven and therefore you can install and run the software following these steps:
	1. Make sure that you have Maven install. If that is not the case, you can download the latest version from the [Maven website](https://maven.apache.org/). 
	2. Clone this repository to your local folder
	3. Modify the sources as needed
	4. Run the software using either your IDE (but please, remember to pass the necessary console arguments as explained in the [Usage section](#usage)) or generate your compiled JAR, containing all the required dependencies in one package,  with the following command: `mvn clean compile assembly:single`. For more instructions on the precise usage, the next section could be helpful. 

## Usage

The software is a command line application that supports the following parameters: 

```
usage: customj48
-d <dataset>   Specifies the path of the dataset (REQUIRED)
-e <format>    specify the export format (graphml, dot, json). Default is: dot
-f <file>      Output the export code to the given file instead of using the console
-h             Print this help message
-p             Enable the pruning feature
```

The `-d` option is the only one required, it specifies the dataset file that will be used to build the decison tree. 
The accepted formats are all the ones accepted by the Weka library and therefore both ARFF and CSV. 

The `-e` option specifies the desired format for the export of the tree. The default one is DOT, but GRAPHML and JSON are also available. See more details about the export formats in the [dedicated section](#export-formats). 
PS: if you use the JSON format and you are interested in the creation of chatbots, take a look at the [associated project](https://gitlab.inf.unibz.it/Davide.Sbetti/bot_interpreter).

The `-f` option specifies a file path that is used to export the decision tree, in the desired format, to a file instead of using the console, which is the default option.

The `-h` option prints the help message.

The `-p` option enables the custom pruning feature described in the [pruning section](#pruning-feature). 

## License

This software is distributed under the GPL v3.0 license, the complete text can be found in the [LICENSE](LICENSE) file.