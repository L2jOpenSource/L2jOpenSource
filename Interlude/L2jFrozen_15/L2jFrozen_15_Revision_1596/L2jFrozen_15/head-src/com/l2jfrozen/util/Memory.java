package com.l2jfrozen.util;

/**
 * @author programmos
 */
public class Memory
{
	public static long getUsedMemory()
	{
		return (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576;
	}
	
	public static long getFreeMemory()
	{
		return (Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory() + Runtime.getRuntime().freeMemory()) / 1048576;
	}
	
	public static long getTotalMemory()
	{
		return Runtime.getRuntime().maxMemory() / 1048576;
	}
}
