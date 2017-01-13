package com.bladecoder.ink.runtime;

import java.util.HashMap;
import java.util.Map.Entry;

public class Set {
	private String name;
	private HashMap<String, Integer> items;

	public Set(String name, HashMap<String, Integer> items) {
		this.name = name;
		this.items = items;
	}
	
	public HashMap<String, Integer> getItems() {
		return items;
	}
	
	public String getName() {
		return name;
	}

    public int getValueForItem(String itemName) {
        Integer v = items.get(itemName);
        
        if(v != null)
        	return v;
        
        return 0;
    }
    
    public Integer tryGetValueForItem(String itemName) {
        return items.get(itemName);
    }
    
    public boolean containsItem(String itemName) {
    	return items.containsKey(itemName);
    }
    
    public String getItemWithValue (int val) {
        String itemName = null;
    
        for (Entry<String, Integer> namedItem : items.entrySet()) {
            if (namedItem.getValue() == val) {
                return namedItem.getKey();
            }
        }
    
        return itemName;
    }
}
