import java.util.ArrayList;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Scanner;

public class SSSP {
	static KahnsAlgorithm k;
	static int n;
	static int m;
	static ArrayList<ArrayList<Integer>> g;
	static int[] indegrees;
	static int[][] costs;
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		k = new KahnsAlgorithm();
		Scanner sc = new Scanner(System.in);
		n = sc.nextInt();
		m = sc.nextInt();
		indegrees = new int[n];
		costs = new int[n][n];
		g = new ArrayList<>();
		for (int i = 0; i < n; i++) {
			g.add(new ArrayList<>());
		}
		for (int i = 0; i < m; i++) {
			// if nodes are not zero-indexed, no need to substract 1
			int x = sc.nextInt() - 1;
			int y = sc.nextInt() - 1;
			costs[x][y] = sc.nextInt();
			g.get(x).add(y);
			indegrees[y]++;
		}
		sc.close();
		int[] shortestPath = shortestPathsNegativeEdges(g, indegrees, costs, n, 0);
		StringBuilder sb = new StringBuilder();
		for (int u : shortestPath) {
			sb.append(u + " ");
		}
		System.out.println(sb.toString());
	}
	/*
	 * Assuming there are no negative cycles, finds shortest path even with negative edges.
	 * Run-time: O(n+m) somehow
	 */
	static int[] shortestPathsNegativeEdges(ArrayList<ArrayList<Integer>> g, int[] indegrees, int[][] costs, int n, int start) {
		int[] d = new int[n];
		ArrayList<Integer> sortedNodes = KahnsAlgorithm.topoSort(g, indegrees, n);
		for (int i = 0; i < n; i++) {
			int v = sortedNodes.get(i);
			if (v == start) {
				d[v] = 0;
			} 
			else if (indegrees[v] == 0) {
				d[v] = Integer.MAX_VALUE;// unreachable 
			} 
			else {
				d[v] = Integer.MAX_VALUE;
				for (int j = 0; j < i; j++) {
					int w = sortedNodes.get(j);
					if (w == v) break;
					d[v] = Math.min(d[v], d[w] + costs[w][v]);
				}
			}
		}
		return d;
	}
	/*
	 * Dijkstra's algorithm idea: sort nodes according to distance from start.
	 * Complexity: O(E log V)
	 */
	static int[] dijkstra (ArrayList<ArrayList<Integer>> g, int[][] costs, int n, int start) {
		PriorityQueue<Pair<Integer>> pq = new PriorityQueue<>();
		HashSet<Integer> settled = new HashSet<>();
		int[] distances = new int[n];
		for (int i = 0; i < n; i++) {
			distances[i] = Integer.MAX_VALUE;
		}
		
		distances[start] = 0;
		pq.add(new Pair<>(start, 0));
		
		while (settled.size() < n) {
			if (pq.isEmpty()) break;
			int u = pq.poll().node;
			if (settled.contains(u)) continue;
			
			settled.add(u);
			for (int v : g.get(u)) {
				if (!settled.contains(v)) {
					int edgeCost = costs[u][v];
					int newDist = edgeCost + distances[u];
					if (newDist >= 0 && newDist < distances[v]) {
						distances[v] = newDist;
						pq.add(new Pair<>(v, distances[v]));
					}
				}
			}
		}
		return distances;
	}
	/*
	 * Finds closest distance if path weights need to be multiplied. Calculates in logarithms, using logarithmic property log(a b) = log (a) + log (b)
	 * Same Dijkstra algorithm, run time is O((n+m) log n)
	 * The answer is 2^distances[finish]
	 */
	static double multiplicativeSP(int start, int finish, ArrayList<ArrayList<Integer>> g, int[][]costs, int n, int m) {
		PriorityQueue<Pair<Double>> pq = new PriorityQueue<>();
		HashSet<Integer> settled = new HashSet<>();
		double[] distances = new double[n];
		for (int i = 0; i < n; i++) {
			distances[i] = Integer.MAX_VALUE;
		}
		
		distances[start] = 0;
		pq.add(new Pair<>(start, 0.0));
		
		while (settled.size() < n) {
			if (pq.isEmpty()) break;
			int u = pq.poll().node;
			if (settled.contains(u)) continue;
			
			settled.add(u);
			for (int v : g.get(u)) {
				if (!settled.contains(v)) {
					int edgeCost = costs[u][v];
					double log2 = Math.log(distances[u]); // calculating log in e because if divided by 2 the results will be less accurate
					double newDist = edgeCost + log2;
					if (newDist >= 0 && newDist < distances[v]) {
						distances[v] = newDist;
						pq.add(new Pair<>(v, distances[v]));
					}
				}
			}
		}
		return Math.pow(distances[finish], Math.E);	
	}
	/*
	 * Bellman Ford algorithm: assuming there is no negative cycle, finds shortest paths to all vertices from starting node start.
	 * Works even when edges are negative.
	 * 
	 * If there is a negative cycle, then it will be found in the n-th iteration, if distance[u] for some node u becomes smaller
	 * 
	 * Run-time: O(n*m)
	 */
	static int[] bellmanFord(int start, int n, ArrayList<ArrayList<Pair<Integer>>> g) {
		ArrayList<ArrayList<Pair<Integer>>> parentList = new ArrayList<>();
		for (int i = 0; i < n; i++) {
			parentList.add(new ArrayList<>());
		}
		for (int i = 0; i < n; i++) {
			for (Pair<Integer> edge : g.get(i)) {
				parentList.get(edge.node).add(new Pair<>(i, edge.distance));
			}
		}
		int[] distance = new int[n];
		int[] predecessor = new int[n];
		for (int i = 0; i < n; i++) {
			distance[i] = Integer.MAX_VALUE;
			predecessor[i] = -1;
		}
		
		distance[start] = 0;
		for (int i = 0; i < n-1; i++) {
			for (int u = 0; u < n; u++) {
				for (Pair<Integer> p : parentList.get(u)) {
					int par = p.node;
					int cost = p.distance;
					if (distance[par] != Integer.MAX_VALUE && distance[par] + cost < distance[u]) {
						distance[u] = distance[par] + cost;
						predecessor[u] = par;
					}
				}
			}
		}
		// repeat the same procedure n-th time. If some distance gets smaller, then there must be a negative cycle.s
		for (int u = 0; u < n; u++) {
			for (Pair<Integer> p : parentList.get(u)) {
				int par = p.node;
				int cost = p.distance;
				if (distance[par] != Integer.MAX_VALUE && distance[par] + cost < distance[u]) {
					// There is a negative cycle
					return null;
				}
			}
		}
		return distance;
	}
	// some weight function that changes cyclically mod n
	public static int c(int i, int j, int n) {
		return (i+j) % n;
	}
	public static int[] rotatingWeights(int start, int n, ArrayList<ArrayList<Pair<Integer>>> g) {
		ArrayList<ArrayList<Pair<Integer>>> newGraph = new ArrayList<>();
		
		for (int t = 0; t < n; t++) {
			for (int i = 0; i < n; i++) {
				ArrayList<Pair<Integer>> a = new ArrayList<>();
				for (int j = 0; j < n; j++) {
					if (i == j) continue;
					a.add(new Pair<Integer>(j, (t+1)%n));
				}
				newGraph.add(a);
			}
		}
		
		ArrayList<ArrayList<Pair<Integer>>> parentList = new ArrayList<>();
		for (int i = 0; i < n; i++) {
			parentList.add(new ArrayList<>());
		}
		for (int i = 0; i < n; i++) {
			for (Pair<Integer> edge : g.get(i)) {
				parentList.get(edge.node).add(new Pair<>(i, edge.distance));
			}
		}
		int[] distance = new int[n*n];
		//int[] predecessor = new int[n];
		for (int i = 0; i < n*n; i++) {
			distance[i] = Integer.MAX_VALUE;
			//predecessor[i] = -1;
		}
		
		distance[start] = 0;
		for (int i = 0; i < n-1; i++) {
			for (int u = 0; u < n; u++) {
				for (Pair<Integer> p : parentList.get(u)) {
					int par = p.node;
					int cost = p.distance;
					if (distance[par] != Integer.MAX_VALUE && distance[par] + cost < distance[u]) {
						distance[u] = distance[par] + cost;
						//predecessor[u] = par;
					}
				}
			}
		}
		// repeat the same procedure n-th time. If some distance gets smaller, then there must be a negative cycle.s
		for (int t = 1; t < n*n; t++) {
			for (int u = 0; u < n*n; u++) {
				for (int from = 0; from < n; from++) {
					if (u == from) continue;
					int weight = c(from, u, (t+1)%n);
					if (distance[u] > weight + distance[from]) {
						distance[u] = weight + distance[from];
					}
				}
			}
//			for (Pair<Integer> p : parentList.get(u)) {
//				int par = p.node;
//				int cost = p.distance;
//				if (distance[par] != Integer.MAX_VALUE && distance[par] + cost < distance[u]) {
//					// There is a negative cycle
//					return null;
//				}
//			}
		}
		int minimalDistance = Integer.MAX_VALUE;
		for (int t = 1; t < n; t++) {
			minimalDistance = Math.min(minimalDistance, distance[1 + t*n]);
		}
		return new int[] {minimalDistance};
	}
	static class Pair<T extends Comparable<T>> implements Comparable<Pair<T>>{
		int node;
		T distance;
		public Pair(int node, T distance) {
			this.node = node;
			this.distance = distance;
		}
		@Override
		public int compareTo(Pair<T> other) {
			return this.distance.compareTo(other.distance);
		}
	}

	static class Edge
	{
		int a;
		int b;
		int w;
		public Edge(int a, int w, int b) {
			this.a = a;
			this.w = w;
			this.b = b;
		}
	}
}

