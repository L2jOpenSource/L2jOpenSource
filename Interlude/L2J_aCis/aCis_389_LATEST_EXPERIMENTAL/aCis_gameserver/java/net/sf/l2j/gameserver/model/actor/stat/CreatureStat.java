package net.sf.l2j.gameserver.model.actor.stat;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.enums.skills.ElementType;
import net.sf.l2j.gameserver.enums.skills.Stats;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.skills.Calculator;
import net.sf.l2j.gameserver.skills.L2Skill;

public class CreatureStat
{
	private final Creature _creature;
	
	private long _exp = 0;
	private int _sp = 0;
	private byte _level = 1;
	
	public CreatureStat(Creature creature)
	{
		_creature = creature;
	}
	
	public Creature getActor()
	{
		return _creature;
	}
	
	public long getExp()
	{
		return _exp;
	}
	
	public void setExp(long value)
	{
		_exp = value;
	}
	
	public int getSp()
	{
		return _sp;
	}
	
	public void setSp(int value)
	{
		_sp = value;
	}
	
	public byte getLevel()
	{
		return _level;
	}
	
	public void setLevel(byte value)
	{
		_level = value;
	}
	
	/**
	 * @param stat : The {@link Stats} to calculate.
	 * @param init : The initial value of the {@link Stats} before applying modifiers.
	 * @param target : The {@link Creature} target whose properties will be used in the calculation.
	 * @param skill : The {@link L2Skill} whose properties will be used in the calculation.
	 * @return The value with modifiers of a given {@link Stats} that will be applied on the targeted {@link Creature}.
	 */
	public final double calcStat(Stats stat, double init, Creature target, L2Skill skill)
	{
		if (stat == null)
			return init;
		
		// Retrieve the Calculator, based on parameterized Stats ordinal.
		final Calculator calculator = _creature.getCalculators()[stat.ordinal()];
		if (calculator == null || calculator.size() == 0)
			return init;
		
		// Launch the calculation.
		double value = calculator.calc(_creature, target, skill, init);
		
		// Enforce positive value, based on parameterized Stats.
		if (value <= 0 && stat.cantBeNegative())
			value = 1.0;
		
		return value;
	}
	
	/**
	 * @return the STR of this {@link Creature}.
	 */
	public int getSTR()
	{
		return _creature.getTemplate().getBaseSTR();
	}
	
	/**
	 * @return the DEX of this {@link Creature}.
	 */
	public int getDEX()
	{
		return _creature.getTemplate().getBaseDEX();
	}
	
	/**
	 * @return the CON of this {@link Creature}.
	 */
	public int getCON()
	{
		return _creature.getTemplate().getBaseCON();
	}
	
	/**
	 * @return the INT of this {@link Creature}.
	 */
	public int getINT()
	{
		return _creature.getTemplate().getBaseINT();
	}
	
	/**
	 * @return the MEN of this {@link Creature}.
	 */
	public int getMEN()
	{
		return _creature.getTemplate().getBaseMEN();
	}
	
	/**
	 * @return the WIT of this {@link Creature}.
	 */
	public int getWIT()
	{
		return _creature.getTemplate().getBaseWIT();
	}
	
	/**
	 * @param target : The {@link Creature} target whose properties will be used in the calculation.
	 * @param skill : The {@link L2Skill} whose properties will be used in the calculation.
	 * @return The physical critical hit rate (base+modifier) of this {@link Creature}. It can't exceed 500.
	 */
	public int getCriticalHit(Creature target, L2Skill skill)
	{
		return Math.min((int) calcStat(Stats.CRITICAL_RATE, _creature.getTemplate().getBaseCritRate(), target, skill), 500);
	}
	
	/**
	 * @param target : The {@link Creature} target whose properties will be used in the calculation.
	 * @param skill : The {@link L2Skill} whose properties will be used in the calculation.
	 * @return The magical critical hit rate (base+modifier) of this {@link Creature}.
	 */
	public final int getMCriticalHit(Creature target, L2Skill skill)
	{
		return (int) calcStat(Stats.MCRITICAL_RATE, 8, target, skill);
	}
	
	/**
	 * @param target : The {@link Creature} target whose properties will be used in the calculation.
	 * @return The evasion rate (base+modifier) of this {@link Creature}.
	 */
	public int getEvasionRate(Creature target)
	{
		return (int) calcStat(Stats.EVASION_RATE, 0, target, null);
	}
	
	/**
	 * @return The accuracy (base+modifier) of this {@link Creature}.
	 */
	public int getAccuracy()
	{
		return (int) calcStat(Stats.ACCURACY_COMBAT, 0, null, null);
	}
	
	/**
	 * @return The maximum HP of this {@link Creature}, based on its current level.
	 */
	public int getMaxHp()
	{
		return (int) calcStat(Stats.MAX_HP, _creature.getTemplate().getBaseHpMax(_creature.getLevel()), null, null);
	}
	
	/**
	 * @return The maximum CP of this {@link Creature}. Overriden on PlayerStat.
	 */
	public int getMaxCp()
	{
		return 0;
	}
	
	/**
	 * @return The maximum MP of this {@link Creature}, based on its current level.
	 */
	public int getMaxMp()
	{
		return (int) calcStat(Stats.MAX_MP, _creature.getTemplate().getBaseMpMax(_creature.getLevel()), null, null);
	}
	
	/**
	 * @param target : The {@link Creature} target whose properties will be used in the calculation.
	 * @param skill : The {@link L2Skill} whose properties will be used in the calculation.
	 * @return The MAtk (base+modifier) of this {@link Creature} for a given {@link L2Skill} and {@link Creature} target.
	 */
	public int getMAtk(Creature target, L2Skill skill)
	{
		return (int) calcStat(Stats.MAGIC_ATTACK, _creature.getTemplate().getBaseMAtk() * ((_creature.isChampion()) ? Config.CHAMPION_ATK : 1), target, skill);
	}
	
	/**
	 * @return The MAtk Speed (base+modifier) of this {@link Creature}.
	 */
	public int getMAtkSpd()
	{
		return (int) calcStat(Stats.MAGIC_ATTACK_SPEED, 333.0 * ((_creature.isChampion()) ? Config.CHAMPION_SPD_ATK : 1), null, null);
	}
	
	/**
	 * @param target : The {@link Creature} target whose properties will be used in the calculation.
	 * @param skill : The {@link L2Skill} whose properties will be used in the calculation.
	 * @return The MDef (base+modifier) of this {@link Creature} for a given {@link L2Skill} and {@link Creature} target.
	 */
	public int getMDef(Creature target, L2Skill skill)
	{
		// Calculate modifiers Magic Attack
		return (int) calcStat(Stats.MAGIC_DEFENCE, _creature.getTemplate().getBaseMDef() * ((_creature.isRaidRelated()) ? Config.RAID_DEFENCE_MULTIPLIER : 1), target, skill);
	}
	
	/**
	 * @param target : The {@link Creature} target whose properties will be used in the calculation.
	 * @return The PAtk (base+modifier) of this {@link Creature} for a given {@link Creature} target.
	 */
	public int getPAtk(Creature target)
	{
		return (int) calcStat(Stats.POWER_ATTACK, _creature.getTemplate().getBasePAtk() * ((_creature.isChampion()) ? Config.CHAMPION_ATK : 1), target, null);
	}
	
	/**
	 * @return The PAtk Speed (base+modifier) of this {@link Creature}.
	 */
	public int getPAtkSpd()
	{
		return (int) calcStat(Stats.POWER_ATTACK_SPEED, _creature.getTemplate().getBasePAtkSpd() * ((_creature.isChampion()) ? Config.CHAMPION_SPD_ATK : 1), null, null);
	}
	
	/**
	 * @param target : The {@link Creature} target whose properties will be used in the calculation.
	 * @return The PDef (base+modifier) of this {@link Creature} for a given {@link Creature} target.
	 */
	public int getPDef(Creature target)
	{
		return (int) calcStat(Stats.POWER_DEFENCE, _creature.getTemplate().getBasePDef() * ((_creature.isRaidRelated()) ? Config.RAID_DEFENCE_MULTIPLIER : 1), target, null);
	}
	
	/**
	 * @return The Physical Attack range (base+modifier) of this {@link Creature}.
	 */
	public int getPhysicalAttackRange()
	{
		return _creature.getAttackType().getRange();
	}
	
	/**
	 * @return The shield defense rate (base+modifier) of this {@link Creature}.
	 */
	public final int getShldDef()
	{
		return (int) calcStat(Stats.SHIELD_DEFENCE, 0, null, null);
	}
	
	/**
	 * @param skill : The {@link L2Skill} whose properties will be used in the calculation.
	 * @return The mana consumption of the {@link L2Skill} set as parameter.
	 */
	public final int getMpConsume(L2Skill skill)
	{
		if (skill == null)
			return 1;
		
		double mpConsume = skill.getMpConsume();
		
		if (skill.isDance())
		{
			if (_creature != null && _creature.getDanceCount() > 0)
				mpConsume += _creature.getDanceCount() * skill.getNextDanceMpCost();
			
			return (int) calcStat(Stats.DANCE_MP_CONSUME_RATE, mpConsume, null, null);
		}
		
		if (skill.isMagic())
			return (int) calcStat(Stats.MAGICAL_MP_CONSUME_RATE, mpConsume, null, null);
		
		return (int) calcStat(Stats.PHYSICAL_MP_CONSUME_RATE, mpConsume, null, null);
	}
	
	/**
	 * @param skill : The {@link L2Skill} whose properties will be used in the calculation.
	 * @return The initial mana consumption of the {@link L2Skill} set as parameter.
	 */
	public final int getMpInitialConsume(L2Skill skill)
	{
		if (skill == null)
			return 1;
		
		double mpConsume = skill.getMpInitialConsume();
		
		if (skill.isDance())
			return (int) calcStat(Stats.DANCE_MP_CONSUME_RATE, mpConsume, null, null);
		
		if (skill.isMagic())
			return (int) calcStat(Stats.MAGICAL_MP_CONSUME_RATE, mpConsume, null, null);
		
		return (int) calcStat(Stats.PHYSICAL_MP_CONSUME_RATE, mpConsume, null, null);
	}
	
	/**
	 * @param element : The {@link ElementType} to test.
	 * @return The calculated attack power of a given {@link ElementType} for this {@link Creature}.
	 */
	public int getAttackElementValue(ElementType element)
	{
		return (element == ElementType.NONE) ? 0 : (int) calcStat(element.getAtkStat(), 0, null, null);
	}
	
	/**
	 * @param element : The {@link ElementType} to test.
	 * @return The calculated defense power of a given {@link ElementType} for this {@link Creature}.
	 */
	public double getDefenseElementValue(ElementType element)
	{
		return (element == ElementType.NONE) ? 1. : calcStat(element.getResStat(), 1., null, null);
	}
	
	/**
	 * @return The base running speed, given by owner template. Player is affected by mount type.
	 */
	public int getBaseRunSpeed()
	{
		return _creature.getTemplate().getBaseRunSpeed();
	}
	
	/**
	 * @return The base walking speed, given by owner template. Player is affected by mount type.
	 */
	public int getBaseWalkSpeed()
	{
		return _creature.getTemplate().getBaseWalkSpeed();
	}
	
	/**
	 * @return The base movement speed, given by owner template and movement status. Player is affected by mount type and by being in L2WaterZone.
	 */
	protected final int getBaseMoveSpeed()
	{
		return _creature.isRunning() ? getBaseRunSpeed() : getBaseWalkSpeed();
	}
	
	/**
	 * @return The movement speed multiplier, which is used by client to set correct character/object movement speed.
	 */
	public final float getMovementSpeedMultiplier()
	{
		return getMoveSpeed() / getBaseMoveSpeed();
	}
	
	/**
	 * @return The attack speed multiplier, which is used by client to set correct character/object attack speed.
	 */
	public final float getAttackSpeedMultiplier()
	{
		return (float) ((1.1) * getPAtkSpd() / _creature.getTemplate().getBasePAtkSpd());
	}
	
	/**
	 * @return The movement speed, given by owner template, status and effects.
	 */
	public float getMoveSpeed()
	{
		return (float) calcStat(Stats.RUN_SPEED, getBaseMoveSpeed(), null, null);
	}
	
	/**
	 * @param isStillWalking : If set to True, we use walking speed rather than running speed.
	 * @return An emulated movement speed, based on client animation.
	 */
	public float getRealMoveSpeed(boolean isStillWalking)
	{
		return getMoveSpeed();
	}
}