package com.l2jfrozen.gameserver.model;

/**
 * This class ...
 * @version $Revision: 1.3.4.1 $ $Date: 2005/03/27 15:29:32 $
 */
public class L2ShortCut
{
	public final static int TYPE_ITEM = 1;
	public final static int TYPE_SKILL = 2;
	public final static int TYPE_ACTION = 3;
	public final static int TYPE_MACRO = 4;
	public final static int TYPE_RECIPE = 5;
	
	private final int slot;
	private final int page;
	private final int type;
	private final int id;
	private final int level;
	
	public L2ShortCut(final int slotId, final int pageId, final int shortcutType, final int shortcutId, final int shortcutLevel, final int unknown)
	{
		slot = slotId;
		page = pageId;
		type = shortcutType;
		id = shortcutId;
		level = shortcutLevel;
		// unk = unknown;
	}
	
	public int getId()
	{
		return id;
	}
	
	public int getLevel()
	{
		return level;
	}
	
	public int getPage()
	{
		return page;
	}
	
	public int getSlot()
	{
		return slot;
	}
	
	public int getType()
	{
		return type;
	}
}
