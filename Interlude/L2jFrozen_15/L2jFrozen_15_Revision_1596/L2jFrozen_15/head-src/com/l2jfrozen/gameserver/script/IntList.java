package com.l2jfrozen.gameserver.script;

/**
 * @author Luis Arias
 */
public class IntList
{
	public static int[] parse(final String range)
	{
		if (range.contains("-"))
		{
			return getIntegerList(range.split("-"));
		}
		else if (range.contains(","))
		{
			return getIntegerList(range.split(","));
		}
		
		final int[] list =
		{
			getInt(range)
		};
		return list;
	}
	
	private static int getInt(final String number)
	{
		return Integer.parseInt(number);
	}
	
	private static int[] getIntegerList(final String[] numbers)
	{
		final int[] list = new int[numbers.length];
		for (int i = 0; i < list.length; i++)
		{
			list[i] = getInt(numbers[i]);
		}
		return list;
	}
}
