package adf.agent.precompute;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class PreData {
	public Map<String, Integer> intValues;
	public Map<String, Double> doubleValues;
	public Map<String, String> stringValues;
	public Map<String, Integer> idValues;
	public Map<String, Boolean> boolValues;

	public Map<String, List<Integer>> intLists;
	public Map<String, List<Double>> doubleLists;
	public Map<String, List<String>> stringLists;
	public Map<String, List<Integer>> idLists;
	public Map<String, List<Boolean>> boolLists;

	public boolean isReady;
	public String readyID;

	public PreData() {
		this.intValues = new HashMap<>();
		this.doubleValues = new HashMap<>();
		this.stringValues = new HashMap<>();
		this.idValues = new HashMap<>();
		this.boolValues = new HashMap<>();
		this.intLists = new HashMap<>();
		this.doubleLists = new HashMap<>();
		this.stringLists = new HashMap<>();
		this.idLists = new HashMap<>();
		this.boolLists = new HashMap<>();
		this.isReady = false;
		this.readyID = "";
	}

	public PreData copy() {
		PreData preData = new PreData();
		preData.intValues = new HashMap<>(this.intValues);
		preData.doubleValues = new HashMap<>(this.doubleValues);
		preData.stringValues = new HashMap<>(this.stringValues);
		preData.idValues = new HashMap<>(this.idValues);
		preData.boolValues = new HashMap<>(this.boolValues);
		preData.intLists = new HashMap<>(this.intLists);
		preData.doubleLists = new HashMap<>(this.doubleLists);
		preData.stringLists = new HashMap<>(this.stringLists);
		preData.idLists = new HashMap<>(this.idLists);
		preData.boolLists = new HashMap<>(this.boolLists);
		preData.isReady = this.isReady;
		preData.readyID = this.readyID;
		return preData;
	}
}
