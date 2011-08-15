package taruDecoder.hyperGraph;

import java.util.*;

import taruHypothesis.HypothesisScoreComparator;

public class HyperGraph {

	private static HypothesisScoreComparator hypScoreComparator = new HypothesisScoreComparator();
	
	public Vector<HGEdge> edges;
	public Vector<HGVertex> vertices;

	private boolean binarized;
	
	public HyperGraph(int vertexCount) {
		edges = new Vector<HGEdge>(vertexCount);
		vertices = new Vector<HGVertex>(vertexCount);
		binarized = false;
	}

	public HyperGraph(int vertexCount, boolean binarized) {
		edges = new Vector<HGEdge>(vertexCount);
		vertices = new Vector<HGVertex>(vertexCount);
		this.binarized = binarized;
	}
	
	public static HypothesisScoreComparator getHypScoreComparator(){
		return hypScoreComparator;
	}
	
	public int addVertex(HGVertex vertex) {
		vertices.addElement(vertex);
		return vertices.size() - 1;
	}

	public boolean addEdge(int[] itemIds, int goalId, String ruleId) {

		// Check for self loops
		for (int itemIndex = 0; itemIndex < itemIds.length; itemIndex++) {
			if (itemIds[itemIndex] == goalId) {
				System.err.println(goalId + " <- " + itemIds[itemIndex]);
				System.err.println("Self link dropped!");
				return false;
			}
		}

		// Create a link
		HGEdge HGEdge = new HGEdge(edges.size(), itemIds, goalId, ruleId);
		edges.addElement(HGEdge);

		// Add the incoming link to goal vertex
		vertices.elementAt(goalId).addInEdge(edges.size() - 1);

		// Add the outgoing link from each item verted to the goal
		//vertices.elementAt(goalId).addInEdge(edges.size() - 1);
		return true;
	}

	// When a structure changing operation on hypergraph will invalidate the edge pointers,
	// this function should be used to flush out all the pointers.
	public void flushEdgePointers(){
		// empty the edges 
		edges.clear();
		
		// empty the in edges in all the vertecies
		for(HGVertex v : vertices){
			v.flushInEdges();
			v.flushHypsWithEdgePointers();
		}
	}
	
	public void sortHyps(){
		for(HGVertex v : vertices){
			v.sortHyps();
		}
	}
	
	public void flushHyps(){
		// empty the in edges in all the vertecies
		for(HGVertex v : vertices){
			v.flushHypsWithEdgePointers();
		}
	}
	
	public int getVertexSize(){
		return vertices.size();
	}
	
	public int getEdgeSize(){
		return edges.size();
	}
	
	public HGVertex getVertex(int index){
		return vertices.elementAt(index);
	}

	public HGEdge getEdge(int index){
		return edges.elementAt(index);
	}

	public boolean isBinarized(){
		return binarized;
	}
	
	public void print() {
		String str = "";
		for (int i = 0; i < edges.size(); i++) {
			str += "Edge " + i + ": ";
			str += edges.elementAt(i).toString();
			str += " ||| ";
		}
		str += "\n";
		
		for(int i = 0; i < vertices.size(); i++){
			HGVertex v = vertices.elementAt(i);
			str += "\nVertex "+ i + ": " + v.toString();
		}
		
		System.out.println(str);
	}
}
