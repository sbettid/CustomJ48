# Custom J48

Custom J48 is an extension of the J48 class of the [Weka library](https://www.cs.waikato.ac.nz/ml/weka/) part of a Bachelor thesis project. J48 is a java implementation of the C4.5 machine learning algorithm.

The goal of this extension was to add the ability to export the generated decision trees in multiple formats, used then for the creation of a chatbot application, adding also a pruning ability. 

## Export formats

Trees generated using the J48 class can be exported in three different formats:

- GraphML format, whose specification can be found [here](http://graphml.graphdrawing.org/), with some graphical adjustments that allow a correct visualization using the yED editor (accessible also in a [browser live version](https://www.yworks.com/yed-live/)). 
- DOT format, used also as default by Weka
- JSON format, based on the following specification: 

    `{label: “node1”, children: [{ edgeLabel: “to_node_2”, label: node_2 }, { edgeLabel: “to_node_3”,       label: “node_3”, children […]  }]}`

    The tree is specified in a recursive way, where each node, except the leafs, contains the list of      its children nodes. Moreover, every node has a label attribute specifying its label/question and       this property will be the one asked to the user during the conversation. All nodes (beside the         root) have an edgeLabel property, which rapresents the label of the edge from the parent node and,     in our case, also the answer to the parent's question that will determine the next node in the         path.


Independently from chosen export format, it is possible to activate the pruning capability. 

## Pruning feature

In the scope of our project, the pruning capability was necessary because the algorithm, given an attribute, adds an edge labelled with any value that it is able to find for the given attribute in the whole dataset, although possibly no instances reach that node, an intended behaviour used to avoid overfitting. 

However, in our case, such branches were not desired and would have led to a misleading tree. For this reason, the different export functions that were encoded, allow a parameter that enables the pruning of branches reached by no instances as soon as they are discovered.

At this point, the pruning feature introduced a new potential inconvenient. Some node could have most of their branches cut off by the pruning procedure with only one of them surviving. The path that has to be taken at that point becomes trivial and would only include an unnecessary step. 

For this reason, the export functions were modified to include a “short circuiting feature” that skips nodes with only one branch surviving the pruning operation (in case the latter one is active, clearly) recurring directly on the grandchild instead.

## Installation

The J48 application requires Weka 3.8.3 stable. Dependencies have all been managed using Maven and therefore the installation
can be performed as follow: 

1. Clone this repository or download the code as a compressed archive and decompress it.
2. Modify the App.java class, that is used as launcher class, to perfomr your tests
3. Build and run the project using your IDE or the maven command `mvn package`

## Usage

## License