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

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import l2r.gameserver.model.StatsSet;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.model.stats.MoveType;

/**
 * @author Zoey76
 */
public class L2CharTemplate
{
	// BaseStats
	private final int _baseSTR;
	private final int _baseCON;
	private final int _baseDEX;
	private final int _baseINT;
	private final int _baseWIT;
	private final int _baseMEN;
	private final float _baseHpMax;
	private final float _baseCpMax;
	private final float _baseMpMax;
	private final float _baseHpReg;
	private final float _baseMpReg;
	private final int _basePAtk;
	private final int _baseMAtk;
	private final int _basePDef;
	private final int _baseMDef;
	private final int _basePAtkSpd;
	private final int _baseMAtkSpd;
	private final float _baseMReuseRate;
	private final int _baseShldDef;
	private final int _baseAttackRange;
	private final int _baseShldRate;
	private final int _baseCritRate;
	private final int _baseMCritRate;
	// SpecialStats
	private final int _baseBreath;
	private final int _baseAggression;
	private final int _baseBleed;
	private final int _basePoison;
	private final int _baseStun;
	private final int _baseRoot;
	private final int _baseMovement;
	private final int _baseConfusion;
	private final int _baseSleep;
	private final double _baseAggressionVuln;
	private final double _baseBleedVuln;
	private final double _basePoisonVuln;
	private final double _baseStunVuln;
	private final double _baseRootVuln;
	private final double _baseMovementVuln;
	private final double _baseSleepVuln;
	private final double _baseCritVuln;
	private int _baseFire;
	private int _baseWind;
	private int _baseWater;
	private int _baseEarth;
	private int _baseHoly;
	private int _baseDark;
	private double _baseFireRes;
	private double _baseWindRes;
	private double _baseWaterRes;
	private double _baseEarthRes;
	private double _baseHolyRes;
	private double _baseDarkRes;
	
	private final int _baseMpConsumeRate;
	private final int _baseHpConsumeRate;
	
	/**
	 * For client info use {@link #_fCollisionRadius}
	 */
	private final int _collisionRadius;
	
	/**
	 * For client info use {@link #_fCollisionHeight}
	 */
	private final int _collisionHeight;
	
	private final double _fCollisionRadius;
	private final double _fCollisionHeight;
	
	private final float[] _moveType = new float[MoveType.values().length];
	
	public L2CharTemplate(StatsSet set)
	{
		// Base stats
		_baseSTR = set.getInteger("baseSTR", 0);
		_baseCON = set.getInteger("baseCON", 0);
		_baseDEX = set.getInteger("baseDEX", 0);
		_baseINT = set.getInteger("baseINT", 0);
		_baseWIT = set.getInteger("baseWIT", 0);
		_baseMEN = set.getInteger("baseMEN", 0);
		_baseHpMax = set.getFloat("baseHpMax", 0);
		_baseCpMax = set.getFloat("baseCpMax", 0);
		_baseMpMax = set.getFloat("baseMpMax", 0);
		_baseHpReg = set.getFloat("baseHpReg", 0);
		_baseMpReg = set.getFloat("baseMpReg", 0);
		_basePAtk = set.getInteger("basePAtk", 0);
		_baseMAtk = set.getInteger("baseMAtk", 0);
		_basePDef = set.getInteger("basePDef", 0);
		_baseMDef = set.getInteger("baseMDef", 0);
		_basePAtkSpd = set.getInteger("basePAtkSpd", 300);
		_baseMAtkSpd = set.getInteger("baseMAtkSpd", 333);
		_baseMReuseRate = set.getFloat("baseMReuseDelay", 1.f);
		_baseShldDef = set.getInteger("baseShldDef", 0);
		_baseAttackRange = set.getInteger("baseAtkRange", 0);
		_baseShldRate = set.getInteger("baseShldRate", 0);
		_baseCritRate = set.getInteger("baseCritRate", 4);
		_baseMCritRate = set.getInteger("baseMCritRate", 0);
		
		// SpecialStats
		_baseBreath = set.getInteger("baseBreath", 100);
		_baseAggression = set.getInteger("baseAggression", 0);
		_baseBleed = set.getInteger("baseBleed", 0);
		_basePoison = set.getInteger("basePoison", 0);
		_baseStun = set.getInteger("baseStun", 0);
		_baseRoot = set.getInteger("baseRoot", 0);
		_baseMovement = set.getInteger("baseMovement", 0);
		_baseConfusion = set.getInteger("baseConfusion", 0);
		_baseSleep = set.getInteger("baseSleep", 0);
		_baseFire = set.getInteger("baseFire", 0);
		_baseWind = set.getInteger("baseWind", 0);
		_baseWater = set.getInteger("baseWater", 0);
		_baseEarth = set.getInteger("baseEarth", 0);
		_baseHoly = set.getInteger("baseHoly", 0);
		_baseDark = set.getInteger("baseDark", 0);
		_baseAggressionVuln = set.getInteger("baseAggressionVuln", 0);
		_baseBleedVuln = set.getInteger("baseBleedVuln", 0);
		_basePoisonVuln = set.getInteger("basePoisonVuln", 0);
		_baseStunVuln = set.getInteger("baseStunVuln", 0);
		_baseRootVuln = set.getInteger("baseRootVuln", 0);
		_baseMovementVuln = set.getInteger("baseMovementVuln", 0);
		_baseSleepVuln = set.getInteger("baseSleepVuln", 0);
		_baseCritVuln = set.getInteger("baseCritVuln", 1);
		_baseFireRes = set.getInteger("baseFireRes", 0);
		_baseWindRes = set.getInteger("baseWindRes", 0);
		_baseWaterRes = set.getInteger("baseWaterRes", 0);
		_baseEarthRes = set.getInteger("baseEarthRes", 0);
		_baseHolyRes = set.getInteger("baseHolyRes", 0);
		_baseDarkRes = set.getInteger("baseDarkRes", 0);
		
		// C4 Stats
		_baseMpConsumeRate = set.getInteger("baseMpConsumeRate", 0);
		_baseHpConsumeRate = set.getInteger("baseHpConsumeRate", 0);
		
		// Geometry
		_fCollisionHeight = set.getDouble("collision_height", 0);
		_fCollisionRadius = set.getDouble("collision_radius", 0);
		_collisionRadius = (int) _fCollisionRadius;
		_collisionHeight = (int) _fCollisionHeight;
		
		// speed.
		Arrays.fill(_moveType, 1);
		setBaseMoveSpeed(MoveType.RUN, set.getInteger("baseRunSpd", 1));
		setBaseMoveSpeed(MoveType.WALK, set.getInteger("baseWalkSpd", 1));
		setBaseMoveSpeed(MoveType.FAST_SWIM, set.getInteger("baseSwimRunSpd", 1));
		setBaseMoveSpeed(MoveType.SLOW_SWIM, set.getInteger("baseSwimWalkSpd", 1));
	}
	
	/**
	 * @return the baseHpMax
	 */
	public float getBaseHpMax()
	{
		return _baseHpMax;
	}
	
	/**
	 * @return the _baseFire
	 */
	public int getBaseFire()
	{
		return _baseFire;
	}
	
	/**
	 * @return the _baseWind
	 */
	public int getBaseWind()
	{
		return _baseWind;
	}
	
	/**
	 * @return the _baseWater
	 */
	public int getBaseWater()
	{
		return _baseWater;
	}
	
	/**
	 * @return the _baseEarth
	 */
	public int getBaseEarth()
	{
		return _baseEarth;
	}
	
	/**
	 * @return the _baseHoly
	 */
	public int getBaseHoly()
	{
		return _baseHoly;
	}
	
	/**
	 * @return the _baseDark
	 */
	public int getBaseDark()
	{
		return _baseDark;
	}
	
	/**
	 * @return the _baseFireRes
	 */
	public double getBaseFireRes()
	{
		return _baseFireRes;
	}
	
	/**
	 * @return the _baseWindRes
	 */
	public double getBaseWindRes()
	{
		return _baseWindRes;
	}
	
	/**
	 * @return the _baseWaterRes
	 */
	public double getBaseWaterRes()
	{
		return _baseWaterRes;
	}
	
	/**
	 * @return the _baseEarthRes
	 */
	public double getBaseEarthRes()
	{
		return _baseEarthRes;
	}
	
	/**
	 * @return the _baseHolyRes
	 */
	public double getBaseHolyRes()
	{
		return _baseHolyRes;
	}
	
	/**
	 * @return the _baseDarkRes
	 */
	public double getBaseDarkRes()
	{
		return _baseDarkRes;
	}
	
	/**
	 * @return the baseSTR
	 */
	public int getBaseSTR()
	{
		return _baseSTR;
	}
	
	/**
	 * @return the baseCON
	 */
	public int getBaseCON()
	{
		return _baseCON;
	}
	
	/**
	 * @return the baseDEX
	 */
	public int getBaseDEX()
	{
		return _baseDEX;
	}
	
	/**
	 * @return the baseINT
	 */
	public int getBaseINT()
	{
		return _baseINT;
	}
	
	/**
	 * @return the baseWIT
	 */
	public int getBaseWIT()
	{
		return _baseWIT;
	}
	
	/**
	 * @return the baseMEN
	 */
	public int getBaseMEN()
	{
		return _baseMEN;
	}
	
	/**
	 * @return the baseCpMax
	 */
	public float getBaseCpMax()
	{
		return _baseCpMax;
	}
	
	/**
	 * @return the baseMpMax
	 */
	public float getBaseMpMax()
	{
		return _baseMpMax;
	}
	
	/**
	 * @return the baseHpReg
	 */
	public float getBaseHpReg()
	{
		return _baseHpReg;
	}
	
	/**
	 * @return the baseMpReg
	 */
	public float getBaseMpReg()
	{
		return _baseMpReg;
	}
	
	/**
	 * @return the basePAtk
	 */
	public int getBasePAtk()
	{
		return _basePAtk;
	}
	
	/**
	 * @return the baseMAtk
	 */
	public int getBaseMAtk()
	{
		return _baseMAtk;
	}
	
	/**
	 * @return the basePDef
	 */
	public int getBasePDef()
	{
		return _basePDef;
	}
	
	/**
	 * @return the baseMDef
	 */
	public int getBaseMDef()
	{
		return _baseMDef;
	}
	
	/**
	 * @return the basePAtkSpd
	 */
	public int getBasePAtkSpd()
	{
		return _basePAtkSpd;
	}
	
	/**
	 * @return the baseMAtkSpd
	 */
	public int getBaseMAtkSpd()
	{
		return _baseMAtkSpd;
	}
	
	/**
	 * @return the baseMReuseRate
	 */
	public float getBaseMReuseRate()
	{
		return _baseMReuseRate;
	}
	
	/**
	 * @return the baseShldDef
	 */
	public int getBaseShldDef()
	{
		return _baseShldDef;
	}
	
	/**
	 * @return the baseAttackRange
	 */
	public int getBaseAttackRange()
	{
		return _baseAttackRange;
	}
	
	/**
	 * @return the baseShldRate
	 */
	public int getBaseShldRate()
	{
		return _baseShldRate;
	}
	
	/**
	 * @return the baseCritRate
	 */
	public int getBaseCritRate()
	{
		return _baseCritRate;
	}
	
	/**
	 * @return the baseMCritRate
	 */
	public int getBaseMCritRate()
	{
		return _baseMCritRate;
	}
	
	public void setBaseMoveSpeed(MoveType type, float val)
	{
		_moveType[type.ordinal()] = val;
	}
	
	public float getBaseMoveSpeed(MoveType mt)
	{
		return _moveType[mt.ordinal()];
	}
	
	/**
	 * @return the baseBreath
	 */
	public int getBaseBreath()
	{
		return _baseBreath;
	}
	
	/**
	 * @return the baseAggression
	 */
	public int getBaseAggression()
	{
		return _baseAggression;
	}
	
	/**
	 * @return the baseBleed
	 */
	public int getBaseBleed()
	{
		return _baseBleed;
	}
	
	/**
	 * @return the basePoison
	 */
	public int getBasePoison()
	{
		return _basePoison;
	}
	
	/**
	 * @return the baseStun
	 */
	public int getBaseStun()
	{
		return _baseStun;
	}
	
	/**
	 * @return the baseRoot
	 */
	public int getBaseRoot()
	{
		return _baseRoot;
	}
	
	/**
	 * @return the baseMovement
	 */
	public int getBaseMovement()
	{
		return _baseMovement;
	}
	
	/**
	 * @return the baseConfusion
	 */
	public int getBaseConfusion()
	{
		return _baseConfusion;
	}
	
	/**
	 * @return the baseSleep
	 */
	public int getBaseSleep()
	{
		return _baseSleep;
	}
	
	/**
	 * @return the baseAggressionVuln
	 */
	public double getBaseAggressionVuln()
	{
		return _baseAggressionVuln;
	}
	
	/**
	 * @return the baseBleedVuln
	 */
	public double getBaseBleedVuln()
	{
		return _baseBleedVuln;
	}
	
	/**
	 * @return the basePoisonVuln
	 */
	public double getBasePoisonVuln()
	{
		return _basePoisonVuln;
	}
	
	/**
	 * @return the baseStunVuln
	 */
	public double getBaseStunVuln()
	{
		return _baseStunVuln;
	}
	
	/**
	 * @return the baseRootVuln
	 */
	public double getBaseRootVuln()
	{
		return _baseRootVuln;
	}
	
	/**
	 * @return the baseMovementVuln
	 */
	public double getBaseMovementVuln()
	{
		return _baseMovementVuln;
	}
	
	/**
	 * @return the baseSleepVuln
	 */
	public double getBaseSleepVuln()
	{
		return _baseSleepVuln;
	}
	
	/**
	 * @return the baseCritVuln
	 */
	public double getBaseCritVuln()
	{
		return _baseCritVuln;
	}
	
	/**
	 * @return the baseMpConsumeRate
	 */
	public int getBaseMpConsumeRate()
	{
		return _baseMpConsumeRate;
	}
	
	/**
	 * @return the baseHpConsumeRate
	 */
	public int getBaseHpConsumeRate()
	{
		return _baseHpConsumeRate;
	}
	
	/**
	 * @return the collisionRadius
	 */
	public int getCollisionRadius()
	{
		return _collisionRadius;
	}
	
	/**
	 * @return the collisionHeight
	 */
	public int getCollisionHeight()
	{
		return _collisionHeight;
	}
	
	/**
	 * @return the fCollisionRadius
	 */
	public double getfCollisionRadius()
	{
		return _fCollisionRadius;
	}
	
	/**
	 * @return the fCollisionHeight
	 */
	public double getfCollisionHeight()
	{
		return _fCollisionHeight;
	}
	
	/**
	 * @param baseFire the baseFire to set
	 */
	public void setBaseFire(int baseFire)
	{
		_baseFire = baseFire;
	}
	
	/**
	 * @param baseWater the baseWater to set
	 */
	public void setBaseWater(int baseWater)
	{
		_baseWater = baseWater;
	}
	
	/**
	 * @param baseEarth the baseEarth to set
	 */
	public void setBaseEarth(int baseEarth)
	{
		_baseEarth = baseEarth;
	}
	
	/**
	 * @param baseWind the baseWind to set
	 */
	public void setBaseWind(int baseWind)
	{
		_baseWind = baseWind;
	}
	
	/**
	 * @param baseHoly the baseHoly to set
	 */
	public void setBaseHoly(int baseHoly)
	{
		_baseHoly = baseHoly;
	}
	
	/**
	 * @param baseDark the baseDark to set
	 */
	public void setBaseDark(int baseDark)
	{
		_baseDark = baseDark;
	}
	
	/**
	 * @param baseFireRes the baseFireRes to set
	 */
	public void setBaseFireRes(double baseFireRes)
	{
		_baseFireRes = baseFireRes;
	}
	
	/**
	 * @param baseWaterRes the baseWaterRes to set
	 */
	public void setBaseWaterRes(double baseWaterRes)
	{
		_baseWaterRes = baseWaterRes;
	}
	
	/**
	 * @param baseEarthRes the baseEarthRes to set
	 */
	public void setBaseEarthRes(double baseEarthRes)
	{
		_baseEarthRes = baseEarthRes;
	}
	
	/**
	 * @param baseWindRes the baseWindRes to set
	 */
	public void setBaseWindRes(double baseWindRes)
	{
		_baseWindRes = baseWindRes;
	}
	
	/**
	 * @param baseHolyRes the baseHolyRes to set
	 */
	public void setBaseHolyRes(double baseHolyRes)
	{
		_baseHolyRes = baseHolyRes;
	}
	
	/**
	 * @param baseDarkRes the baseDarkRes to set
	 */
	public void setBaseDarkRes(double baseDarkRes)
	{
		_baseDarkRes = baseDarkRes;
	}
	
	/**
	 * Overridden in L2NpcTemplate
	 * @return the characters skills
	 */
	public Map<Integer, L2Skill> getSkills()
	{
		return Collections.emptyMap();
	}
}
