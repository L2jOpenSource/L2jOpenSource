package com.l2jfrozen;

import java.util.HashMap;
import java.util.StringTokenizer;

public class ClassMasterSettings
{
	private final HashMap<Integer, HashMap<Integer, Integer>> claimItems;
	private final HashMap<Integer, HashMap<Integer, Integer>> rewardItems;
	private final HashMap<Integer, Boolean> allowedClassChange;
	
	public ClassMasterSettings(final String configLine)
	{
		claimItems = new HashMap<>();
		rewardItems = new HashMap<>();
		allowedClassChange = new HashMap<>();
		if (configLine != null)
		{
			parseConfigLine(configLine.trim());
		}
	}
	
	private void parseConfigLine(final String configLine)
	{
		final StringTokenizer st = new StringTokenizer(configLine, ";");
		
		while (st.hasMoreTokens())
		{
			final int job = Integer.parseInt(st.nextToken());
			
			allowedClassChange.put(job, true);
			
			HashMap<Integer, Integer> items = new HashMap<>();
			
			if (st.hasMoreTokens())
			{
				final StringTokenizer st2 = new StringTokenizer(st.nextToken(), "[],");
				
				while (st2.hasMoreTokens())
				{
					final StringTokenizer st3 = new StringTokenizer(st2.nextToken(), "()");
					final int itemId = Integer.parseInt(st3.nextToken());
					final int quantity = Integer.parseInt(st3.nextToken());
					items.put(itemId, quantity);
				}
			}
			
			claimItems.put(job, items);
			items = new HashMap<>();
			
			if (st.hasMoreTokens())
			{
				final StringTokenizer st2 = new StringTokenizer(st.nextToken(), "[],");
				
				while (st2.hasMoreTokens())
				{
					final StringTokenizer st3 = new StringTokenizer(st2.nextToken(), "()");
					final int itemId = Integer.parseInt(st3.nextToken());
					final int quantity = Integer.parseInt(st3.nextToken());
					items.put(itemId, quantity);
				}
			}
			rewardItems.put(job, items);
		}
	}
	
	public boolean isAllowed(final int job)
	{
		if (allowedClassChange == null)
		{
			return false;
		}
		if (allowedClassChange.containsKey(job))
		{
			return allowedClassChange.get(job);
		}
		return false;
	}
	
	public HashMap<Integer, Integer> getRewardItems(final int job)
	{
		if (rewardItems.containsKey(job))
		{
			return rewardItems.get(job);
		}
		return null;
	}
	
	public HashMap<Integer, Integer> getRequireItems(final int job)
	{
		if (claimItems.containsKey(job))
		{
			return claimItems.get(job);
		}
		return null;
	}
}