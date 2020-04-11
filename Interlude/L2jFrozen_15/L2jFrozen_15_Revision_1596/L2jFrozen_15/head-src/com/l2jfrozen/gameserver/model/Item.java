package com.l2jfrozen.gameserver.model;

import com.l2jfrozen.gameserver.templates.L2Item;
import com.l2jfrozen.gameserver.templates.StatsSet;

/**
 * This class ...
 * @author  luisantonioa
 * @version $Revision: 1.2 $ $Date: 2004/06/27 08:12:59 $
 */
public class Item
{
	public int id;
	public Enum<?> type;
	public String name;
	public StatsSet set;
	public int currentLevel;
	public L2Item item;
}
