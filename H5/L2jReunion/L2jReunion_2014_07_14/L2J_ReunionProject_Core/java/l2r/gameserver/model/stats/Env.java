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
package l2r.gameserver.model.stats;

import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.instance.L2CubicInstance;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.effects.L2Effect;
import l2r.gameserver.model.items.instance.L2ItemInstance;
import l2r.gameserver.model.skills.L2Skill;

/**
 * An Env object is just a class to pass parameters to a calculator such as L2PcInstance, L2ItemInstance, Initial value.
 * @author Zoey76
 */
public final class Env
{
	private double _baseValue;
	private boolean _blessedSpiritShot = false;
	private L2Character _character;
	private L2CubicInstance _cubic;
	private L2Effect _effect;
	private L2ItemInstance _item;
	private byte _shield = 0;
	private L2Skill _skill;
	private boolean _skillMastery = false;
	private boolean _soulShot = false;
	private boolean _spiritShot = false;
	private L2Character _target;
	private double _value;
	
	public Env()
	{
	}
	
	public Env(byte shield, boolean soulShot, boolean spiritShot, boolean blessedSpiritShot)
	{
		_shield = shield;
		_soulShot = soulShot;
		_spiritShot = spiritShot;
		_blessedSpiritShot = blessedSpiritShot;
	}
	
	/**
	 * @return the _baseValue
	 */
	public double getBaseValue()
	{
		return _baseValue;
	}
	
	/**
	 * @return the _character
	 */
	public L2Character getCharacter()
	{
		return _character;
	}
	
	/**
	 * @return the _cubic
	 */
	public L2CubicInstance getCubic()
	{
		return _cubic;
	}
	
	/**
	 * @return the _effect
	 */
	public L2Effect getEffect()
	{
		return _effect;
	}
	
	/**
	 * @return the _item
	 */
	public L2ItemInstance getItem()
	{
		return _item;
	}
	
	/**
	 * @return the acting player.
	 */
	public L2PcInstance getPlayer()
	{
		return _character == null ? null : _character.getActingPlayer();
	}
	
	/**
	 * @return the _shield
	 */
	public byte getShield()
	{
		return _shield;
	}
	
	/**
	 * @return the _skill
	 */
	public L2Skill getSkill()
	{
		return _skill;
	}
	
	/**
	 * @return the _target
	 */
	public L2Character getTarget()
	{
		return _target;
	}
	
	/**
	 * @return the _value
	 */
	public double getValue()
	{
		return _value;
	}
	
	/**
	 * @return the _blessedSpiritShot
	 */
	public boolean isBlessedSpiritShot()
	{
		return _blessedSpiritShot;
	}
	
	/**
	 * @return the _skillMastery
	 */
	public boolean isSkillMastery()
	{
		return _skillMastery;
	}
	
	/**
	 * @return the _soulShot
	 */
	public boolean isSoulShot()
	{
		return _soulShot;
	}
	
	/**
	 * @return the _spiritShot
	 */
	public boolean isSpiritShot()
	{
		return _spiritShot;
	}
	
	/**
	 * @param baseValue the _baseValue to set
	 */
	public void setBaseValue(double baseValue)
	{
		_baseValue = baseValue;
	}
	
	/**
	 * @param blessedSpiritShot the _blessedSpiritShot to set
	 */
	public void setBlessedSpiritShot(boolean blessedSpiritShot)
	{
		_blessedSpiritShot = blessedSpiritShot;
	}
	
	/**
	 * @param character the _character to set
	 */
	public void setCharacter(L2Character character)
	{
		_character = character;
	}
	
	/**
	 * @param cubic the _cubic to set
	 */
	public void setCubic(L2CubicInstance cubic)
	{
		_cubic = cubic;
	}
	
	/**
	 * @param effect the _effect to set
	 */
	public void setEffect(L2Effect effect)
	{
		_effect = effect;
	}
	
	/**
	 * @param item the _item to set
	 */
	public void setItem(L2ItemInstance item)
	{
		_item = item;
	}
	
	/**
	 * @param shield the _shield to set
	 */
	public void setShield(byte shield)
	{
		_shield = shield;
	}
	
	/**
	 * @param skill the _skill to set
	 */
	public void setSkill(L2Skill skill)
	{
		_skill = skill;
	}
	
	/**
	 * @param skillMastery the _skillMastery to set
	 */
	public void setSkillMastery(boolean skillMastery)
	{
		_skillMastery = skillMastery;
	}
	
	/**
	 * @param soulShot the _soulShot to set
	 */
	public void setSoulShot(boolean soulShot)
	{
		_soulShot = soulShot;
	}
	
	/**
	 * @param spiritShot the _spiritShot to set
	 */
	public void setSpiritShot(boolean spiritShot)
	{
		_spiritShot = spiritShot;
	}
	
	/**
	 * @param target the _target to set
	 */
	public void setTarget(L2Character target)
	{
		_target = target;
	}
	
	/**
	 * @param value the _value to set
	 */
	public void setValue(double value)
	{
		_value = value;
	}
	
	public void addValue(double value)
	{
		_value += value;
	}
	
	public void subValue(double value)
	{
		_value -= value;
	}
	
	public void mulValue(double value)
	{
		_value *= value;
	}
	
	public void divValue(double value)
	{
		_value /= value;
	}
}
