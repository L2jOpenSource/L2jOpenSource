/*
 * Copyright (C) 2004-2014 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.model.actor.templates;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import l2r.gameserver.datatables.xml.ExperienceData;
import l2r.gameserver.datatables.xml.InitialEquipmentData;
import l2r.gameserver.enums.PcRace;
import l2r.gameserver.model.Location;
import l2r.gameserver.model.StatsSet;
import l2r.gameserver.model.base.ClassId;
import l2r.gameserver.model.itemcontainer.Inventory;
import l2r.gameserver.model.items.PcItemTemplate;
import l2r.util.Rnd;

/**
 * @author mkizub, Zoey76
 */
public class L2PcTemplate extends L2CharTemplate
{
	private final ClassId _classId;
	
	private final float[] _baseHp;
	private final float[] _baseMp;
	private final float[] _baseCp;
	
	private final double[] _baseHpReg;
	private final double[] _baseMpReg;
	private final double[] _baseCpReg;
	
	public final double _fCollisionHeightFemale;
	public final double _fCollisionRadiusFemale;
	
	private final int _baseSafeFallHeight;
	
	private final List<PcItemTemplate> _initialEquipment;
	private final List<Location> _creationPoints;
	private final Map<Integer, Integer> _baseSlotDef;
	
	public L2PcTemplate(StatsSet set, List<Location> creationPoints)
	{
		super(set);
		_classId = ClassId.getClassId(set.getInteger("classId"));
		
		_baseHp = new float[ExperienceData.getInstance().getMaxLevel()];
		_baseMp = new float[ExperienceData.getInstance().getMaxLevel()];
		_baseCp = new float[ExperienceData.getInstance().getMaxLevel()];
		_baseHpReg = new double[ExperienceData.getInstance().getMaxLevel()];
		_baseMpReg = new double[ExperienceData.getInstance().getMaxLevel()];
		_baseCpReg = new double[ExperienceData.getInstance().getMaxLevel()];
		
		_baseSlotDef = new HashMap<>(12);
		_baseSlotDef.put(Inventory.PAPERDOLL_CHEST, set.getInteger("basePDefchest", 0));
		_baseSlotDef.put(Inventory.PAPERDOLL_LEGS, set.getInteger("basePDeflegs", 0));
		_baseSlotDef.put(Inventory.PAPERDOLL_HEAD, set.getInteger("basePDefhead", 0));
		_baseSlotDef.put(Inventory.PAPERDOLL_FEET, set.getInteger("basePDeffeet", 0));
		_baseSlotDef.put(Inventory.PAPERDOLL_GLOVES, set.getInteger("basePDefgloves", 0));
		_baseSlotDef.put(Inventory.PAPERDOLL_UNDER, set.getInteger("basePDefunderwear", 0));
		_baseSlotDef.put(Inventory.PAPERDOLL_CLOAK, set.getInteger("basePDefcloak", 0));
		_baseSlotDef.put(Inventory.PAPERDOLL_REAR, set.getInteger("baseMDefrear", 0));
		_baseSlotDef.put(Inventory.PAPERDOLL_LEAR, set.getInteger("baseMDeflear", 0));
		_baseSlotDef.put(Inventory.PAPERDOLL_RFINGER, set.getInteger("baseMDefrfinger", 0));
		_baseSlotDef.put(Inventory.PAPERDOLL_LFINGER, set.getInteger("baseMDefrfinger", 0));
		_baseSlotDef.put(Inventory.PAPERDOLL_NECK, set.getInteger("baseMDefneck", 0));
		
		_fCollisionRadiusFemale = set.getDouble("collisionFemaleradius");
		_fCollisionHeightFemale = set.getDouble("collisionFemaleheight");
		
		_baseSafeFallHeight = set.getInteger("baseSafeFall", 333);
		
		_initialEquipment = InitialEquipmentData.getInstance().getEquipmentList(_classId);
		_creationPoints = creationPoints;
	}
	
	/**
	 * @return the template class Id.
	 */
	public ClassId getClassId()
	{
		return _classId;
	}
	
	/**
	 * @return the template race.
	 */
	public PcRace getRace()
	{
		return _classId.getRace();
	}
	
	/**
	 * @return random Location of created character spawn.
	 */
	public Location getCreationPoint()
	{
		return _creationPoints.get(Rnd.get(_creationPoints.size()));
	}
	
	/**
	 * Sets the value of level upgain parameter.
	 * @param paramName name of parameter
	 * @param level corresponding character level
	 * @param val value of parameter
	 */
	public void setUpgainValue(String paramName, int level, double val)
	{
		switch (paramName)
		{
			case "hp":
			{
				_baseHp[level] = (float) val;
				break;
			}
			case "mp":
			{
				_baseMp[level] = (float) val;
				break;
			}
			case "cp":
			{
				_baseCp[level] = (float) val;
				break;
			}
			case "hpRegen":
			{
				_baseHpReg[level] = val;
				break;
			}
			case "mpRegen":
			{
				_baseMpReg[level] = val;
				break;
			}
			case "cpRegen":
			{
				_baseCpReg[level] = val;
				break;
			}
		}
	}
	
	/**
	 * @param level character level to return value
	 * @return the baseHpMax for given character level
	 */
	public float getBaseHpMax(int level)
	{
		return _baseHp[level];
	}
	
	/**
	 * @param level character level to return value
	 * @return the baseMpMax for given character level
	 */
	public float getBaseMpMax(int level)
	{
		return _baseMp[level];
	}
	
	/**
	 * @param level character level to return value
	 * @return the baseCpMax for given character level
	 */
	public float getBaseCpMax(int level)
	{
		return _baseCp[level];
	}
	
	/**
	 * @param level character level to return value
	 * @return the base HP Regeneration for given character level
	 */
	public double getBaseHpRegen(int level)
	{
		return _baseHpReg[level];
	}
	
	/**
	 * @param level character level to return value
	 * @return the base MP Regeneration for given character level
	 */
	public double getBaseMpRegen(int level)
	{
		return _baseMpReg[level];
	}
	
	/**
	 * @param level character level to return value
	 * @return the base HP Regeneration for given character level
	 */
	public double getBaseCpRegen(int level)
	{
		return _baseCpReg[level];
	}
	
	/**
	 * @param slotId id of inventory slot to return value
	 * @return defence value of charactert for EMPTY given slot
	 */
	public int getBaseDefBySlot(int slotId)
	{
		return _baseSlotDef.containsKey(slotId) ? _baseSlotDef.get(slotId) : 0;
	}
	
	/**
	 * @return the template collision height for female characters.
	 */
	public double getFCollisionHeightFemale()
	{
		return _fCollisionHeightFemale;
	}
	
	/**
	 * @return the template collision radius for female characters.
	 */
	public double getFCollisionRadiusFemale()
	{
		return _fCollisionRadiusFemale;
	}
	
	/**
	 * @return the safe fall height.
	 */
	public int getSafeFallHeight()
	{
		return _baseSafeFallHeight;
	}
	
	/**
	 * @return the initial equipment for this Pc template.
	 */
	public List<PcItemTemplate> getInitialEquipment()
	{
		return _initialEquipment;
	}
	
	/**
	 * @return {@code true} if this Pc template has an initial equipment associated, {@code false} otherwise.
	 */
	public boolean hasInitialEquipment()
	{
		return _initialEquipment != null;
	}
}
