package com.l2jfrozen.gameserver.model.multisell;

import java.util.ArrayList;
import java.util.List;

/**
 * @author programmos
 */
public class MultiSellListContainer
{
	private int listId;
	private boolean applyTaxes = false;
	private boolean maintainEnchantment = false;
	
	List<MultiSellEntry> entriesC;
	
	public MultiSellListContainer()
	{
		entriesC = new ArrayList<>();
	}
	
	/**
	 * @param listId The listId to set.
	 */
	public void setListId(final int listId)
	{
		this.listId = listId;
	}
	
	public void setApplyTaxes(final boolean applyTaxes)
	{
		this.applyTaxes = applyTaxes;
	}
	
	public void setMaintainEnchantment(final boolean maintainEnchantment)
	{
		this.maintainEnchantment = maintainEnchantment;
	}
	
	/**
	 * @return Returns the listId.
	 */
	public int getListId()
	{
		return listId;
	}
	
	public boolean getApplyTaxes()
	{
		return applyTaxes;
	}
	
	public boolean getMaintainEnchantment()
	{
		return maintainEnchantment;
	}
	
	public void addEntry(final MultiSellEntry e)
	{
		entriesC.add(e);
	}
	
	public List<MultiSellEntry> getEntries()
	{
		return entriesC;
	}
}
