package net.sf.l2j.gameserver.model.craft;

import java.util.ArrayList;
import java.util.List;

/**
 * A datacontainer used by private workshop system. It retains a List of {@link ManufactureItem}s, the store name and the shop state.
 */
public class ManufactureList
{
	private final List<ManufactureItem> _list = new ArrayList<>();
	
	private boolean _confirmed;
	private boolean _isDwarven;
	
	private String _storeName;
	
	public ManufactureList()
	{
	}
	
	public List<ManufactureItem> getList()
	{
		return _list;
	}
	
	public void add(ManufactureItem item)
	{
		_list.add(item);
	}
	
	public boolean hasConfirmed()
	{
		return _confirmed;
	}
	
	public void setConfirmedTrade(boolean confirmed)
	{
		_confirmed = confirmed;
	}
	
	public boolean isDwarven()
	{
		return _isDwarven;
	}
	
	public void setState(boolean isDwarven)
	{
		_isDwarven = isDwarven;
	}
	
	public String getStoreName()
	{
		return _storeName;
	}
	
	public void setStoreName(String storeName)
	{
		_storeName = storeName;
	}
	
	/**
	 * Clear the items {@link List}.
	 */
	public void clear()
	{
		_list.clear();
	}
}