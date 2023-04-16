import java.awt.font.GraphicAttribute;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.Writer;
import java.time.chrono.MinguoChronology;
import static java.lang.Math.min;

import javax.lang.model.element.ExecutableElement;

import java.util.*;

public class project5 {
	//Edge class (nested)
	private static class Edge{
		//data fields
		//Every edge consists of two nodes (from , to) and capacity (since it is weighted graph)
		//Our objective is to solve "Max Flow Problem" so an additional integer flow is necessary
		//residual Edge will be used in the algorithm implementation
		public int from;
		public int to;
		public Edge residual;
		public int flow;
		public final int capacity;

		//Edge constructor
		public Edge(int from, int to, int capacity) {
			this.from = from;
			this.to = to;
			this.capacity = capacity;
		}
		
		// method returns the boolean value depending on the remaining capacity
		//if we use all of the capacity of an edge then it will become "residual" in the following steps
		public boolean isResidual() {
			return (capacity == 0);
		}
		
		//the remaining capacity after flow
		public int remainingCapacity() {
			return capacity - flow;
		}
		
		public void augment(int my_bot_neck) {
			flow += my_bot_neck;
			residual.flow -= my_bot_neck;
		}
		
		//to string method to check
		public String toString(int s,int t) {
			String u = (from == s) ? "s": ((from == t) ? "t" : String.valueOf(from));
			String v = (to== s) ? "s": ((to==t) ? "t" : String.valueOf(to));
			return String.format("Edge %s -> %s | flow = %3d | capacity = %3d | is residual: %s", u,v,flow,capacity,isResidual());
		}
	}
	//nested NetworkFlowProblem class
	private static abstract class NetworkFlowProblem {
		static final long INF = Long.MAX_VALUE /2;
		final int n,s,t;
		private int visitedToken = 1;
		private int[] visited;
		protected boolean solved;
		protected int maxFlow;
		protected List<Edge>[] graph;

		public NetworkFlowProblem(int n, int s,int t) {
			this.n = n;
			this.s = s;
			this.t = t;
			initializeEmptyFlowGraph();
			visited = new int[n];
		}
		
		//construct the empty graph firstly
		private void initializeEmptyFlowGraph() {
			graph = new List[n];
			for (int i = 0; i < n; i++) {
				graph[i]= new ArrayList<Edge>();
			}
		}

		//method adds edge to the graph
		public void addEdge(int from, int to, int capacity) {
			Edge my_first_edge = new Edge(from,to,capacity);
			Edge my_second_edge = new Edge(to, from, 0);
			my_first_edge.residual = my_second_edge;
			my_second_edge.residual = my_first_edge;
			graph[from].add(my_first_edge);
			graph[to].add(my_second_edge);
		}

		//method returns our graph
		public List<Edge>[] getGraph(){
			execute();
			return graph;
		}

		//returns the result 
		public int getMaxFlow() {
			execute();
			return maxFlow;
		}

		public void visit(int i) {
			visited[i] = visitedToken;
		}
		public boolean visited(int i) {
		      return visited[i] == visitedToken;
		    }
		public void makeAllNodesAsUnvisited() {
			visitedToken++;
		}
		private void execute() {
			if (solved) return;
			solved = true;
			solve();
		}
		public abstract void solve();
	}
	// the specific algorithm I am going to use in this project is called Edmonds Karp
	private static class EdmondsKarp extends NetworkFlowProblem{
		public EdmondsKarp(int n, int s, int t) {
			super(n, s, t);
		}
		@Override
		public void solve() {
			int flow;
			do {
				makeAllNodesAsUnvisited();
				flow = bfs();
				maxFlow += flow;
			}while (flow != 0);
		}
		//the Breath Search Traversal method that I am going to use in the graph search 
		private int bfs() {
			Queue<Integer> q = new ArrayDeque<>(n);
			visit(s);
			q.offer(s);

			Edge[] prev = new Edge[n];
			while (!q.isEmpty()) {
				int node = q.poll();
				if (node == t) break;

				for (Edge edge : graph[node]) {
					long cap = edge.remainingCapacity();
					if (cap > 0 && !visited(edge.to)) {
						visit(edge.to);
						prev[edge.to] = edge;
						q.offer(edge.to);
					}
				}
			}
			if (prev[t] == null) return 0;

			int my_bot_neck = Integer.MAX_VALUE;
			for (Edge edge = prev[t]; edge != null; edge = prev[edge.from])
				my_bot_neck = min(my_bot_neck, edge.remainingCapacity());
			for (Edge edge = prev[t]; edge != null; edge = prev[edge.from])
				edge.augment(my_bot_neck);
			return my_bot_neck;

		}
	}
	public static void main (String[] args) {
		File fi = new File(args[0]);
		File fo = new File(args[1]);
		PrintStream writer;
		//reading from input.txt line by line
		try {
			writer = new PrintStream(fo);

		}  
		catch(FileNotFoundException e){  
			e.printStackTrace();  
			return;
		} 

		//creating the Scanner object
		Scanner scan;
		try {
			scan = new Scanner(fi);
		}catch(FileNotFoundException e){  
			e.printStackTrace();  
			return;
		}
		
		//storing all of the text in an ArrayList line by line
		ArrayList<String> my_text = new ArrayList<>();
	
		while(scan.hasNextLine()) {
			String line = scan.nextLine();
			String[] my_array = line.split(" ");
			my_text.add(line);	
		}
		//my_text_size is the number of lines in the input.txt
		int my_text_size = my_text.size();
		//as determined in the description there are going to be 6 regions exactly
		final int region_number = 6;
		String line1 = my_text.get(0);
		//the first line of the input is city_number
		int city_number = Integer.parseInt(line1);
		//			System.out.println(city_number);
		
		//total_number = total number of nodes in the graph
		//+2 stands for source("S") and sink("KL")
		final int total_number = region_number + city_number +2;

		NetworkFlowProblem solver = new EdmondsKarp(total_number, total_number-1, total_number-2);

		//input line contains troops respectively to the regions
		String line2 = my_text.get(1);
		//for region and source distance
		String[] line2_split = line2.split(" ");
		

		//source_names_list will contain r0,..r5,c0,..cn,..KL,S
		ArrayList<String> source_names_list = new ArrayList<>();
		for (int i = 2; i < my_text_size; i++) {
			
			String[] line_i_array = my_text.get(i).split(" ");
			String first = line_i_array[0];
			source_names_list.add(line_i_array[0]);
			
		}
		source_names_list.add("KL");
		source_names_list.add("S");
		//System.out.println(source_names_list);	

		for(int y=0; y<6; y++) {
			solver.addEdge(total_number-1, source_names_list.indexOf(source_names_list.get(y)), Integer.parseInt(line2_split[y]));
		}
		
		//for loop to traverse the text after the first two lines
		for (int j = 2 ; j< my_text_size ; j++) {
			String[] my_line = my_text.get(j).split(" ");
			String first_elem = my_line[0];
			int from  = source_names_list.indexOf(first_elem);
			//as there are pairs like (c0,25) in the input line we will iterate over the line (input_line_length/2) times
			int half_line_length = (my_line.length)/2;
			for (int k = 0; k < half_line_length ; k++) {
				int to = source_names_list.indexOf(my_line[2*k+1]);
				int  capacity = Integer.parseInt(my_line[2*k+2]);
				solver.addEdge(from, to, capacity);
			}
		}

		List<Edge>[] resultGraph = solver.getGraph();
		
//		System.out.println(solver.getMaxFlow());
		//writing the result to the output file
		writer.println(solver.getMaxFlow());

		// Displays all edges part of the resulting residual graph.
//		for (List<Edge> edges : resultGraph) 
//			for (Edge e : edges)
//				System.out.println(e.toString(total_number-1, total_number-2));
		writer.close();
	}
}



