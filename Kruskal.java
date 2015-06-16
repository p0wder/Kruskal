/*
 * Author:  Scott Gramig
 * Project: Kruskal's MST
 */

import java.io.*;
import java.util.*;

public class Kruskal 
{
	public static String[] str;//array to hold lines for adj matrix
	public int vertices, visited[], tree[][];
	public boolean finished = false, cycle = false;
	private List<Edge> edges;
	private static Scanner input;//Scanner object

	public static void main(String[] args)throws FileNotFoundException, UnsupportedEncodingException
	{
		System.out.println("Kruskal's Algorithm for Minimum Spanning Tree");
		Scanner kb = new Scanner(System.in);//input from keyboard
		int adj_matrix[][];
		int vertices = 0;
		String line = null; //var to hold input line from text file
		String fileName, newFile; //input and output files

		System.out.print("\nEnter the file name to use:  ");
		fileName = kb.nextLine();//user input location of file

		//Output to file
		System.out.print("\nName the desired output file(.txt):  ");
		newFile = kb.nextLine();//creating a file to store output
		File file =  new File(newFile);
		file.getParentFile().mkdirs();
		PrintWriter pw = new PrintWriter(file, "UTF-8");
		
		input = new Scanner(new File(fileName));//reading the text file from user specified location

		HashMap<String, Integer> verticesMap = new HashMap<String, Integer>();
		int nodeCounter = 0;

		ArrayList<String> keyToValue = new ArrayList<String>();

		//find the number of vertices in the data-set and create arraylist for keyToValue
		while(input.hasNext())
		{
			++vertices;
			line = input.nextLine();
			str = line.split(", ");
			verticesMap.put(str[0], nodeCounter);
			keyToValue.add(str[0]);
			nodeCounter++;
		}

		input.reset();
		input = new Scanner(new File(fileName));

		//initialize all locations to 0
		adj_matrix = new int[vertices][vertices];
		for(int i = 0; i < vertices; i++)
			for(int j = 0; j < vertices; j++)
			{
				adj_matrix[i][j] = 0;
			}

		while(input.hasNext())
		{
			line = input.nextLine();//line from file

			str = line.split(", ");//splits the string into a dynamically allocated array with regex ", " to separate everything

			//creating a adjacency matrix
			for (int i = 1; i < str.length; i = i + 2)
			{
				adj_matrix[verticesMap.get(str[0])][verticesMap.get(str[i])] = Integer.parseInt(str[i+1]);
				adj_matrix[verticesMap.get(str[i])][verticesMap.get(str[0])] = Integer.parseInt(str[i+1]);
			}
		}

		Kruskal k = new Kruskal(vertices);
		k.KruskalMST(adj_matrix);

		HashMap<String, String> parentMap = new HashMap<String, String>();
		//initialize the hashmap of parents to A - A, B - B, etc
		for (int i = 0; i < keyToValue.size(); i++)
			parentMap.put(keyToValue.get(i), keyToValue.get(i));

		pw.println("The Minimum Spanning Tree");
		for (int i = 0; i < k.edges.size(); i++)
		{
			String sourceNode = keyToValue.get(k.edges.get(i).source);
			String destinationNode = keyToValue.get(k.edges.get(i).destination);
			
			getLastParentOfSource(sourceNode, parentMap);
			getLastParentOfDestination(destinationNode, parentMap);
			
			//this will print to file once setup to do so
			if(lastParentDestination != lastParentSource)
			{
				pw.println(sourceNode + ", " + destinationNode);
				parentMap.put(lastParentSource, lastParentDestination);
			}
		}
		pw.close();
		kb.close();
	}
	class CheckCycle
	{
		private Stack<Integer> stack;
		private int adjMatrix[][];

		public CheckCycle()
		{
			stack = new Stack<Integer>();
		}
		public boolean checkCycle(int adj_matrix[][], int source)
		{
			int numOfNodes = adj_matrix[source].length - 1;

			adjMatrix = new int[numOfNodes][numOfNodes];
			for(int sourcevertex = 0; sourcevertex < numOfNodes; sourcevertex++)
			{
				for(int destinationvertex = 0; destinationvertex < numOfNodes; destinationvertex++)
				{
					adjMatrix[sourcevertex][destinationvertex] = adj_matrix[sourcevertex][destinationvertex];
				}
			}

			int visited[] = new int[numOfNodes*numOfNodes];//test
			int element = source;
			int i = source;
			visited[source] = 1;
			stack.push(source);

			while(!stack.isEmpty())
			{
				element = stack.peek();
				i = element;
				while(i < numOfNodes)
				{
					if(adjMatrix[element][i] >= 1 && visited[i] == 1)
					{
						if(stack.contains(i))
						{
							cycle = true;
							return cycle;
						}
					}
					if(adjMatrix[element][i] >= 1 && visited[i] == 0)
					{
						stack.push(i);
						visited[i] = 1;
						adjMatrix[element][i] = 0;
						adjMatrix[i][element] = 0;
						element = i;
						i = 1;
						continue;
					}
					i++;
				}
				stack.pop();
			}
			return cycle;
		}
	}
	//used to create an edge object
	class Edge
	{
		int source, destination, weight;
		//prints graphical representation of nodes of an edge and its weight
		public String toString()
		{
			return "Node1: " + source + "\tto" + "\tNode2: " + destination + "\tWeight: " + weight;
		}
	}
	
	public Kruskal(int vertices)
	{
		this.vertices = vertices;
		edges = new LinkedList<Edge>();
		visited = new int[this.vertices*this.vertices];
		tree = new int[vertices][vertices];
	}

	public void KruskalMST(int adjMatrix[][])
	{
		for(int source = 0; source < vertices; source++)
			for(int destination = source; destination < vertices; destination++)
				if(adjMatrix[source][destination] != 0 && source != destination)
				{
					Edge edge = new Edge();
					edge.source = source;
					edge.destination = destination;
					edge.weight = adjMatrix[source][destination];
					edges.add(edge);
				}
		Collections.sort(edges, new EdgeCompare());
//This block of code prints out every edge and its weight
/*		
		Iterator<Edge> iter = edges.iterator();
		System.out.println("Each edge and weight");
		while(iter.hasNext())
		{
			System.out.println(iter.next());
		}
*/
	}

	class EdgeCompare implements Comparator<Edge>
	{
		public int compare(Edge edge1, Edge edge2)
		{
			if(edge1.weight < edge2.weight)
				return -1;
			if(edge1.weight > edge2.weight)
				return 1;
			return 0;
		}
	}
//the next two methods recursively decide which edges will be in the minimum spanning tree and detect if there is a cycle
	static String lastParentSource;
	static String lastParentDestination;

	static public void getLastParentOfSource(String node, HashMap<String, String> currParentArray)
	{
		if (currParentArray.get(node) == node)
			lastParentSource = node;
		else
			getLastParentOfSource(currParentArray.get(node), currParentArray);
	}

	static public void getLastParentOfDestination(String node, HashMap<String, String> currParentArray)
	{
		if (currParentArray.get(node) == node)
			lastParentDestination = node;
		else
			getLastParentOfDestination(currParentArray.get(node), currParentArray);
	}
}
