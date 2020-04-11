package com.l2jfrozen.gameserver.model.actor.instance;

import com.l2jfrozen.gameserver.templates.L2Henna;

/**
 * This class represents a Non-Player-Character in the world. it can be a monster or a friendly character. it also uses a template to fetch some static values. the templates are hardcoded in the client, so we can rely on them.
 * @version $Revision$ $Date$
 */

public class L2HennaInstance
{
	// private static Logger LOGGER = Logger.getLogger(L2HennaInstance.class);
	
	private final L2Henna hennaTemplate;
	private int symbolId;
	private int itemIdDye;
	private int price;
	private int statINT;
	private int statSTR;
	private int statCON;
	private int statMEM;
	private int statDEX;
	private int statWIT;
	private int amountDyeRequire;
	
	public L2HennaInstance(final L2Henna template)
	{
		hennaTemplate = template;
		symbolId = hennaTemplate.symbolId;
		itemIdDye = hennaTemplate.dye;
		amountDyeRequire = hennaTemplate.amount;
		price = hennaTemplate.price;
		statINT = hennaTemplate.statINT;
		statSTR = hennaTemplate.statSTR;
		statCON = hennaTemplate.statCON;
		statMEM = hennaTemplate.statMEM;
		statDEX = hennaTemplate.statDEX;
		statWIT = hennaTemplate.statWIT;
	}
	
	public String getName()
	{
		String res = "";
		if (statINT > 0)
		{
			res = res + "INT +" + statINT;
		}
		else if (statSTR > 0)
		{
			res = res + "STR +" + statSTR;
		}
		else if (statCON > 0)
		{
			res = res + "CON +" + statCON;
		}
		else if (statMEM > 0)
		{
			res = res + "MEN +" + statMEM;
		}
		else if (statDEX > 0)
		{
			res = res + "DEX +" + statDEX;
		}
		else if (statWIT > 0)
		{
			res = res + "WIT +" + statWIT;
		}
		
		if (statINT < 0)
		{
			res = res + ", INT " + statINT;
		}
		else if (statSTR < 0)
		{
			res = res + ", STR " + statSTR;
		}
		else if (statCON < 0)
		{
			res = res + ", CON " + statCON;
		}
		else if (statMEM < 0)
		{
			res = res + ", MEN " + statMEM;
		}
		else if (statDEX < 0)
		{
			res = res + ", DEX " + statDEX;
		}
		else if (statWIT < 0)
		{
			res = res + ", WIT " + statWIT;
		}
		
		return res;
	}
	
	public L2Henna getTemplate()
	{
		return hennaTemplate;
	}
	
	public int getSymbolId()
	{
		return symbolId;
	}
	
	public void setSymbolId(final int SymbolId)
	{
		symbolId = SymbolId;
	}
	
	public int getItemIdDye()
	{
		return itemIdDye;
	}
	
	public void setItemIdDye(final int ItemIdDye)
	{
		itemIdDye = ItemIdDye;
	}
	
	public int getAmountDyeRequire()
	{
		return amountDyeRequire;
	}
	
	public void setAmountDyeRequire(final int AmountDyeRequire)
	{
		amountDyeRequire = AmountDyeRequire;
	}
	
	public int getPrice()
	{
		return price;
	}
	
	public void setPrice(final int Price)
	{
		price = Price;
	}
	
	public int getStatINT()
	{
		return statINT;
	}
	
	public void setStatINT(final int StatINT)
	{
		statINT = StatINT;
	}
	
	public int getStatSTR()
	{
		return statSTR;
	}
	
	public void setStatSTR(final int StatSTR)
	{
		statSTR = StatSTR;
	}
	
	public int getStatCON()
	{
		return statCON;
	}
	
	public void setStatCON(final int StatCON)
	{
		statCON = StatCON;
	}
	
	public int getStatMEM()
	{
		return statMEM;
	}
	
	public void setStatMEM(final int StatMEM)
	{
		statMEM = StatMEM;
	}
	
	public int getStatDEX()
	{
		return statDEX;
	}
	
	public void setStatDEX(final int StatDEX)
	{
		statDEX = StatDEX;
	}
	
	public int getStatWIT()
	{
		return statWIT;
	}
	
	public void setStatWIT(final int StatWIT)
	{
		statWIT = StatWIT;
	}
}
