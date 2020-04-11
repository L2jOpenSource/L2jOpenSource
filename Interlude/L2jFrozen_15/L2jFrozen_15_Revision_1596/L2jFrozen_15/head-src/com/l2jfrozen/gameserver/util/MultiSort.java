package com.l2jfrozen.gameserver.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.l2jfrozen.Config;

import javolution.util.FastList;

/**
 * Descending Integer Sort Algorithm - Fast ordering system. - Can easily be ported elsewhere. - Can handle any number of values, from a list or even from a map. - Handles duplicate values.
 * @author Tempy
 */
public class MultiSort
{
	public static final int SORT_ASCENDING = 0;
	public static final int SORT_DESCENDING = 1;
	
	private List<?> keyList;
	private List<Integer> valueList;
	
	private boolean isSortDescending;
	private boolean isSorted;
	
	public MultiSort(final int[] valueList)
	{
		this.valueList = getIntList(valueList);
	}
	
	public MultiSort(final Collection<Integer> valueList)
	{
		this.valueList = getIntList(valueList);
	}
	
	public MultiSort(final Object[] keyList, final int[] valueList)
	{
		this.keyList = getList(keyList);
		this.valueList = getIntList(valueList);
	}
	
	public MultiSort(final Map<?, Integer> valueMap)
	{
		keyList = getList(valueMap.keySet());
		valueList = getIntList(valueMap.values());
	}
	
	private final List<Integer> getIntList(final Collection<Integer> valueList)
	{
		return Arrays.asList(valueList.toArray(new Integer[valueList.size()]));
	}
	
	private final List<Integer> getIntList(final int[] valueList)
	{
		final Integer[] tempIntList = new Integer[valueList.length];
		
		for (int i = 0; i < valueList.length; i++)
		{
			tempIntList[i] = valueList[i];
		}
		
		return Arrays.asList(tempIntList);
	}
	
	private final List<?> getList(final Collection<?> valueList)
	{
		return getList(valueList.toArray(new Object[valueList.size()]));
	}
	
	private final List<Object> getList(final Object[] valueList)
	{
		return Arrays.asList(valueList);
	}
	
	public final int getCount()
	{
		return getValues().size();
	}
	
	public final int getHarmonicMean()
	{
		if (getValues().isEmpty())
		{
			return -1;
		}
		
		int totalValue = 0;
		
		for (final int currValue : getValues())
		{
			totalValue += 1 / currValue;
		}
		
		return getCount() / totalValue;
	}
	
	public final List<?> getKeys()
	{
		if (keyList == null)
		{
			return new FastList<>();
		}
		
		return keyList;
	}
	
	public final int getFrequency(final int checkValue)
	{
		return Collections.frequency(getValues(), checkValue);
	}
	
	public final int getMaxValue()
	{
		return Collections.max(getValues());
	}
	
	public final int getMinValue()
	{
		return Collections.min(getValues());
	}
	
	public final int getMean()
	{
		if (getValues().isEmpty())
		{
			return -1;
		}
		
		return getTotalValue() / getCount();
	}
	
	public final double getStandardDeviation()
	{
		if (getValues().isEmpty())
		{
			return -1;
		}
		
		final List<Double> tempValList = new FastList<>();
		
		final int meanValue = getMean();
		final int numValues = getCount();
		
		for (final int value : getValues())
		{
			final double adjValue = Math.pow(value - meanValue, 2);
			tempValList.add(adjValue);
		}
		
		double totalValue = 0;
		
		for (final double storedVal : tempValList)
		{
			totalValue += storedVal;
		}
		
		return Math.sqrt(totalValue / (numValues - 1));
	}
	
	public final int getTotalValue()
	{
		if (getValues().isEmpty())
		{
			return 0;
		}
		
		int totalValue = 0;
		
		for (final int currValue : getValues())
		{
			totalValue += currValue;
		}
		
		return totalValue;
	}
	
	public final List<Integer> getValues()
	{
		if (valueList == null)
		{
			return new FastList<>();
		}
		
		return valueList;
	}
	
	public final boolean isSortDescending()
	{
		return isSortDescending;
	}
	
	public final boolean isSorted()
	{
		return isSorted;
	}
	
	public final void setSortDescending(final boolean isDescending)
	{
		isSortDescending = isDescending;
	}
	
	public boolean sort()
	{
		try
		{
			final List<Object> newKeyList = new FastList<>();
			final List<Integer> newValueList = new FastList<>();
			
			// Sort the list of values in ascending numerical order.
			Collections.sort(getValues());
			
			int lastValue = 0;
			
			if (!isSortDescending())
			{
				// If there are no keys, just return the ascendingly sorted values.
				if (getKeys().isEmpty())
				{
					return true;
				}
				
				// Iterate through the list of ordered numerical values.
				for (int i = getValues().size() - 1; i > -1; i--)
				{
					final int currValue = getValues().get(i);
					
					// If the current value is equal to the last value, we have at least one
					// duplicate that has been outputted already, so continue.
					if (currValue == lastValue)
					{
						continue;
					}
					
					// Set the last value to the current value, to prevent duplication.
					lastValue = currValue;
					
					// Iterate through each key and match it to its stored integer value,
					// then output both sets of data in the correct descending numerical order.
					for (int j = 0; j < getKeys().size(); j++)
					{
						final Object currKey = getKeys().get(j);
						
						if (getValues().get(j) == currValue)
						{
							newKeyList.add(currKey);
							newValueList.add(currValue);
						}
					}
				}
			}
			else
			{
				// If there are no keys, just sort the value list in reverse order.
				if (getKeys().isEmpty())
				{
					Collections.reverse(getValues());
					return true;
				}
				
				// Do the exact same as above, but in descending order.
				for (int i = 0; i < getValues().size(); i++)
				{
					final int currValue = getValues().get(i);
					
					if (currValue == lastValue)
					{
						continue;
					}
					
					lastValue = currValue;
					
					for (int j = 0; j < getKeys().size(); j++)
					{
						final Object currKey = getKeys().get(j);
						
						if (getValues().get(j) == currValue)
						{
							newKeyList.add(currKey);
							newValueList.add(currValue);
						}
					}
				}
			}
			
			keyList = newKeyList;
			valueList = newValueList;
			isSorted = true;
			return true;
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			return false;
		}
	}
}
