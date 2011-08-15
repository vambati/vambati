package taruDecoder.hyperGraph;

public class HGEdge {
	
	public int id;

	public int[] items;
	public int goal;

	public String ruleId;
	
	public HGEdge(int id, int[] items, int goal, String ruleId) {
		this.id = id;
		this.items = items;
		this.goal = goal;
		this.ruleId = ruleId;
	}
	
	public int[] getItems(){
		return items;
	}
	
	public int getGoal(){
		return goal;
	}
	
	public String getRuleId(){
		return ruleId;
	}
	
	public String toString() {
		String str = "";
		str += "Items [";
		for (int i = 0; i < items.length; i++) {
			str += " "+items[i];
		}
		str += "] Goal [";
		str += goal;
		str += "]";

		return str;
	}
}
