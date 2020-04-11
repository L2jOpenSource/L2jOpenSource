package com.l2jfrozen.gameserver.templates;

import java.util.ArrayList;
import java.util.List;

import com.l2jfrozen.gameserver.datatables.sql.ItemTable;
import com.l2jfrozen.gameserver.model.base.ClassId;
import com.l2jfrozen.gameserver.model.base.Race;

/**
 * @author mkizub
 */
public class L2PcTemplate extends L2CharTemplate
{
	
	/** The Class object of the L2PcInstance */
	public final Race race;
	public final ClassId classId;
	
	public final int currentCollisionRadius;
	public final int currentCollisionHeight;
	public final String className;
	
	public final int spawnX;
	public final int spawnY;
	public final int spawnZ;
	
	public final int classBaseLevel;
	public final float lvlHpAdd;
	public final float lvlHpMod;
	public final float lvlCpAdd;
	public final float lvlCpMod;
	public final float lvlMpAdd;
	public final float lvlMpMod;
	
	private final List<L2Item> items = new ArrayList<>();
	
	public L2PcTemplate(final StatsSet set)
	{
		super(set);
		classId = ClassId.values()[set.getInteger("classId")];
		race = Race.values()[set.getInteger("raceId")];
		className = set.getString("className");
		currentCollisionRadius = set.getInteger("collision_radius");
		currentCollisionHeight = set.getInteger("collision_height");
		
		spawnX = set.getInteger("spawnX");
		spawnY = set.getInteger("spawnY");
		spawnZ = set.getInteger("spawnZ");
		
		classBaseLevel = set.getInteger("classBaseLevel");
		lvlHpAdd = set.getFloat("lvlHpAdd");
		lvlHpMod = set.getFloat("lvlHpMod");
		lvlCpAdd = set.getFloat("lvlCpAdd");
		lvlCpMod = set.getFloat("lvlCpMod");
		lvlMpAdd = set.getFloat("lvlMpAdd");
		lvlMpMod = set.getFloat("lvlMpMod");
	}
	
	/**
	 * add starter equipment
	 * @param itemId
	 */
	public void addItem(final int itemId)
	{
		final L2Item item = ItemTable.getInstance().getTemplate(itemId);
		if (item != null)
		{
			items.add(item);
		}
	}
	
	/**
	 * @return itemIds of all the starter equipment
	 */
	public L2Item[] getItems()
	{
		return items.toArray(new L2Item[items.size()]);
	}
	
	/**
	 * @return
	 */
	public double getCollisionRadius()
	{
		return currentCollisionRadius;
	}
	
	/**
	 * @return
	 */
	public double getCollisionHeight()
	{
		return currentCollisionHeight;
	}
	
	public int getBaseFallSafeHeight(final boolean female)
	{
		if (classId.getRace() == Race.darkelf || classId.getRace() == Race.elf)
		{
			return classId.isMage() ? (female ? 330 : 300) : female ? 380 : 350;
		}
		else if (classId.getRace() == Race.dwarf)
		{
			return female ? 200 : 180;
		}
		else if (classId.getRace() == Race.human)
		{
			return classId.isMage() ? (female ? 220 : 200) : female ? 270 : 250;
		}
		else if (classId.getRace() == Race.orc)
		{
			return classId.isMage() ? (female ? 280 : 250) : female ? 220 : 200;
		}
		
		return 400;
		
		/*
		 * Dark Elf Fighter F 380 Dark Elf Fighter M 350 Dark Elf Mystic F 330 Dark Elf Mystic M 300 Dwarf Fighter F 200 Dwarf Fighter M 180 Elf Fighter F 380 Elf Fighter M 350 Elf Mystic F 330 Elf Mystic M 300 Human Fighter F 270 Human Fighter M 250 Human Mystic F 220 Human Mystic M 200 Orc Fighter F 220
		 * Orc Fighter M 200 Orc Mystic F 280 Orc Mystic M 250
		 */
	}
	
	public final int getFallHeight()
	{
		return 333; // TODO: unhardcode it
	}
}
