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

import l2r.gameserver.enums.AIType;

/**
 * This Data is for NPC Attributes and AI related stuffs.<br>
 * @author ShanSoft
 */
public class L2NpcAIData
{
	// Basic AI
	private int _primarySkillId;
	private int _minskillChance;
	private int _maxskillChance;
	private int _canMove;
	private int _soulshot;
	private int _spiritshot;
	private int _soulshotChance;
	private int _spiritshotChance;
	private int _isChaos;
	private String _clan = null;
	private int _clanRange;
	private String _enemyClan = null;
	private int _enemyRange;
	private int _dodge;
	private int _longRangeSkill;
	private int _shortRangeSkill;
	private int _longRangeChance;
	private int _shortRangeChance;
	private int _switchRangeChance;
	private AIType _aiType = AIType.FIGHTER;
	private int _aggroRange;
	private boolean _showName;
	private boolean _targetable;
	
	public void setPrimarySkillId(int primarySkillId)
	{
		_primarySkillId = primarySkillId;
	}
	
	public void setMinSkillChance(int skill_chance)
	{
		_minskillChance = skill_chance;
	}
	
	public void setMaxSkillChance(int skill_chance)
	{
		_maxskillChance = skill_chance;
	}
	
	public void setCanMove(int canMove)
	{
		_canMove = canMove;
	}
	
	public void setSoulShot(int soulshot)
	{
		_soulshot = soulshot;
	}
	
	public void setSpiritShot(int spiritshot)
	{
		_spiritshot = spiritshot;
	}
	
	public void setSoulShotChance(int soulshotchance)
	{
		_soulshotChance = soulshotchance;
	}
	
	public void setSpiritShotChance(int spiritshotchance)
	{
		_spiritshotChance = spiritshotchance;
	}
	
	public void setShortRangeSkill(int shortrangeskill)
	{
		_shortRangeSkill = shortrangeskill;
	}
	
	public void setShortRangeChance(int shortrangechance)
	{
		_shortRangeChance = shortrangechance;
	}
	
	public void setLongRangeSkill(int longrangeskill)
	{
		_longRangeSkill = longrangeskill;
	}
	
	public void setLongRangeChance(int longrangechance)
	{
		_longRangeChance = longrangechance;
	}
	
	public void setSwitchRangeChance(int switchrangechance)
	{
		_switchRangeChance = switchrangechance;
	}
	
	public void setIsChaos(int ischaos)
	{
		_isChaos = ischaos;
	}
	
	public void setClan(String clan)
	{
		if ((clan != null) && !clan.isEmpty() && !clan.equalsIgnoreCase("null"))
		{
			_clan = clan.intern();
		}
	}
	
	public void setClanRange(int clanRange)
	{
		_clanRange = clanRange;
	}
	
	public void setEnemyClan(String enemyClan)
	{
		if ((enemyClan != null) && !enemyClan.isEmpty() && !enemyClan.equalsIgnoreCase("null"))
		{
			_enemyClan = enemyClan.intern();
		}
	}
	
	public void setEnemyRange(int enemyRange)
	{
		_enemyRange = enemyRange;
	}
	
	public void setDodge(int dodge)
	{
		_dodge = dodge;
	}
	
	public void setAi(String ai)
	{
		if (ai.equalsIgnoreCase("archer"))
		{
			_aiType = AIType.ARCHER;
		}
		else if (ai.equalsIgnoreCase("balanced"))
		{
			_aiType = AIType.BALANCED;
		}
		else if (ai.equalsIgnoreCase("mage"))
		{
			_aiType = AIType.MAGE;
		}
		else if (ai.equalsIgnoreCase("healer"))
		{
			_aiType = AIType.HEALER;
		}
		else if (ai.equalsIgnoreCase("corpse"))
		{
			_aiType = AIType.CORPSE;
		}
		else
		{
			_aiType = AIType.FIGHTER;
		}
	}
	
	public void setAggro(int val)
	{
		_aggroRange = val;
	}
	
	public void setTargetable(boolean val)
	{
		_targetable = val;
	}
	
	public void setShowName(boolean val)
	{
		_showName = val;
	}
	
	public int getPrimarySkillId()
	{
		return _primarySkillId;
	}
	
	public int getMinSkillChance()
	{
		return _minskillChance;
	}
	
	public int getMaxSkillChance()
	{
		return _maxskillChance;
	}
	
	public int getCanMove()
	{
		return _canMove;
	}
	
	public int getSoulShot()
	{
		return _soulshot;
	}
	
	public int getSpiritShot()
	{
		return _spiritshot;
	}
	
	public int getSoulShotChance()
	{
		return _soulshotChance;
	}
	
	public int getSpiritShotChance()
	{
		return _spiritshotChance;
	}
	
	public int getShortRangeSkill()
	{
		return _shortRangeSkill;
	}
	
	public int getShortRangeChance()
	{
		return _shortRangeChance;
	}
	
	public int getLongRangeSkill()
	{
		return _longRangeSkill;
	}
	
	public int getLongRangeChance()
	{
		return _longRangeChance;
	}
	
	public int getSwitchRangeChance()
	{
		return _switchRangeChance;
	}
	
	public int getIsChaos()
	{
		return _isChaos;
	}
	
	public String getClan()
	{
		return _clan;
	}
	
	public int getClanRange()
	{
		return _clanRange;
	}
	
	public String getEnemyClan()
	{
		return _enemyClan;
	}
	
	public int getEnemyRange()
	{
		return _enemyRange;
	}
	
	public int getDodge()
	{
		return _dodge;
	}
	
	public AIType getAiType()
	{
		return _aiType;
	}
	
	public int getAggroRange()
	{
		return _aggroRange;
	}
	
	/**
	 * @return {@code true} if the NPC name should shows above NPC, {@code false} otherwise.
	 */
	public boolean showName()
	{
		return _showName;
	}
	
	/**
	 * @return {@code true} if the NPC can be targeted, {@code false} otherwise.
	 */
	public boolean isTargetable()
	{
		return _targetable;
	}
}
