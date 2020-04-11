package com.l2jfrozen.gameserver.model;

import java.util.ArrayList;
import java.util.List;

public class L2ManufactureList
{
	private List<L2ManufactureItem> list;
	private boolean confirmed;
	private String manufactureStoreName;
	
	public L2ManufactureList()
	{
		list = new ArrayList<>();
		confirmed = false;
	}
	
	public int size()
	{
		return list.size();
	}
	
	public void setConfirmedTrade(final boolean x)
	{
		confirmed = x;
	}
	
	public boolean hasConfirmed()
	{
		return confirmed;
	}
	
	/**
	 * @param manufactureStoreName
	 */
	public void setStoreName(final String manufactureStoreName)
	{
		this.manufactureStoreName = manufactureStoreName;
	}
	
	public String getStoreName()
	{
		return manufactureStoreName;
	}
	
	public void add(final L2ManufactureItem item)
	{
		list.add(item);
	}
	
	public List<L2ManufactureItem> getList()
	{
		return list;
	}
	
	public void setList(final List<L2ManufactureItem> list)
	{
		this.list = list;
	}
	
}
