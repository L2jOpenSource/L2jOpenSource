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
package l2r.gameserver.model;

import l2r.gameserver.model.stats.MoveType;

/**
 * Stats definition for each pet level.
 * @author JIV, Zoey76
 */
public class L2PetLevelData
{
	private final int _ownerExpTaken;
	private final int _petFeedBattle;
	private final int _petFeedNormal;
	private final float _petMAtk;
	private final long _petMaxExp;
	private final int _petMaxFeed;
	private final float _petMaxHP;
	private final float _petMaxMP;
	private final float _petMDef;
	private final float _petPAtk;
	private final float _petPDef;
	private final float _petRegenHP;
	private final float _petRegenMP;
	private final short _petSoulShot;
	private final short _petSpiritShot;
	private final int _walkSpeedOnRide;
	private final int _runSpeedOnRide;
	private final int _slowSwimSpeedOnRide;
	private final int _fastSwimSpeedOnRide;
	private final int _slowFlySpeedOnRide;
	private final int _fastFlySpeedOnRide;
	
	public L2PetLevelData(StatsSet set)
	{
		_ownerExpTaken = set.getInteger("get_exp_type");
		_petMaxExp = set.getLong("exp");
		_petMaxHP = set.getFloat("org_hp");
		_petMaxMP = set.getFloat("org_mp");
		_petPAtk = set.getFloat("org_pattack");
		_petPDef = set.getFloat("org_pdefend");
		_petMAtk = set.getFloat("org_mattack");
		_petMDef = set.getFloat("org_mdefend");
		_petMaxFeed = set.getInteger("max_meal");
		_petFeedBattle = set.getInteger("consume_meal_in_battle");
		_petFeedNormal = set.getInteger("consume_meal_in_normal");
		_petRegenHP = set.getFloat("org_hp_regen");
		_petRegenMP = set.getFloat("org_mp_regen");
		_petSoulShot = set.getShort("soulshot_count");
		_petSpiritShot = set.getShort("spiritshot_count");
		_walkSpeedOnRide = set.getInteger("walkSpeedOnRide", 0);
		_runSpeedOnRide = set.getInteger("runSpeedOnRide", 0);
		_slowSwimSpeedOnRide = set.getInteger("slowSwimSpeedOnRide", 0);
		_fastSwimSpeedOnRide = set.getInteger("fastSwimSpeedOnRide", 0);
		_slowFlySpeedOnRide = set.getInteger("slowFlySpeedOnRide", 0);
		_fastFlySpeedOnRide = set.getInteger("fastFlySpeedOnRide", 0);
	}
	
	/**
	 * @return the owner's experience points consumed by the pet.
	 */
	public int getOwnerExpTaken()
	{
		return _ownerExpTaken;
	}
	
	/**
	 * @return the pet's food consume rate at battle state.
	 */
	public int getPetFeedBattle()
	{
		return _petFeedBattle;
	}
	
	/**
	 * @return the pet's food consume rate at normal state.
	 */
	public int getPetFeedNormal()
	{
		return _petFeedNormal;
	}
	
	/**
	 * @return the pet's Magical Attack.
	 */
	public float getPetMAtk()
	{
		return _petMAtk;
	}
	
	/**
	 * @return the pet's maximum experience points.
	 */
	public long getPetMaxExp()
	{
		return _petMaxExp;
	}
	
	/**
	 * @return the pet's maximum feed points.
	 */
	public int getPetMaxFeed()
	{
		return _petMaxFeed;
	}
	
	/**
	 * @return the pet's maximum HP.
	 */
	public float getPetMaxHP()
	{
		return _petMaxHP;
	}
	
	/**
	 * @return the pet's maximum MP.
	 */
	public float getPetMaxMP()
	{
		return _petMaxMP;
	}
	
	/**
	 * @return the pet's Magical Defense.
	 */
	public float getPetMDef()
	{
		return _petMDef;
	}
	
	/**
	 * @return the pet's Physical Attack.
	 */
	public float getPetPAtk()
	{
		return _petPAtk;
	}
	
	/**
	 * @return the pet's Physical Defense.
	 */
	public float getPetPDef()
	{
		return _petPDef;
	}
	
	/**
	 * @return the pet's HP regeneration rate.
	 */
	public float getPetRegenHP()
	{
		return _petRegenHP;
	}
	
	/**
	 * @return the pet's MP regeneration rate.
	 */
	public float getPetRegenMP()
	{
		return _petRegenMP;
	}
	
	/**
	 * @return the pet's soulshot use count.
	 */
	public short getPetSoulShot()
	{
		return _petSoulShot;
	}
	
	/**
	 * @return the pet's spiritshot use count.
	 */
	public short getPetSpiritShot()
	{
		return _petSpiritShot;
	}
	
	/**
	 * @param mt movement type
	 * @return the base riding speed of given movement type.
	 */
	public int getSpeedOnRide(MoveType mt)
	{
		switch (mt)
		{
			case WALK:
				return _walkSpeedOnRide;
			case RUN:
				return _runSpeedOnRide;
			case SLOW_SWIM:
				return _slowSwimSpeedOnRide;
			case FAST_SWIM:
				return _fastSwimSpeedOnRide;
			case SLOW_FLY:
				return _slowFlySpeedOnRide;
			case FAST_FLY:
				return _fastFlySpeedOnRide;
		}
		
		return 0;
	}
}
