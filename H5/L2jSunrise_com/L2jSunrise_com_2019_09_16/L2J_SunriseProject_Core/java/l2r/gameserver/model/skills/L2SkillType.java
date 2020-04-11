/*
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
package l2r.gameserver.model.skills;

import java.lang.reflect.Constructor;

import l2r.gameserver.model.StatsSet;
import l2r.gameserver.model.skills.l2skills.L2SkillChargeDmg;
import l2r.gameserver.model.skills.l2skills.L2SkillCreateItem;
import l2r.gameserver.model.skills.l2skills.L2SkillDefault;
import l2r.gameserver.model.skills.l2skills.L2SkillDrain;
import l2r.gameserver.model.skills.l2skills.L2SkillLearnSkill;
import l2r.gameserver.model.skills.l2skills.L2SkillSignet;
import l2r.gameserver.model.skills.l2skills.L2SkillSignetCasttime;
import l2r.gameserver.model.skills.l2skills.L2SkillSummon;
import l2r.gameserver.model.skills.l2skills.L2SkillTeleport;

/**
 * @author nBd
 */
public enum L2SkillType
{
	// Damage
	PDAM,
	MDAM,
	DOT,
	MDOT,
	DRAIN(L2SkillDrain.class),
	BLOW,
	CHARGEDAM(L2SkillChargeDmg.class),
	SIGNET(L2SkillSignet.class),
	SIGNET_CASTTIME(L2SkillSignetCasttime.class),
	// Disablers
	BLEED,
	POISON,
	STUN,
	ROOT,
	FEAR,
	SLEEP,
	CONFUSE_MOB_ONLY,
	MUTE,
	PARALYZE,
	DISARM,
	// Aggro
	AGGDAMAGE,
	AGGDEBUFF,
	// Misc
	CHAIN_HEAL,
	UNLOCK,
	UNLOCK_SPECIAL,
	DUMMY,
	// Creation
	CREATE_ITEM(L2SkillCreateItem.class),
	LEARN_SKILL(L2SkillLearnSkill.class),
	// Summons
	SUMMON(L2SkillSummon.class),
	ERASE,
	BETRAY,
	
	BUFF,
	DEBUFF,
	CONT,
	FUSION,
	
	RECALL(L2SkillTeleport.class),
	TELEPORT(L2SkillTeleport.class),
	
	// Skill is done within the core.
	COREDONE,
	// unimplemented
	NOTDONE;
	
	private final Class<? extends L2Skill> _class;
	
	public L2Skill makeSkill(StatsSet set)
	{
		try
		{
			Constructor<? extends L2Skill> c = _class.getConstructor(StatsSet.class);
			
			return c.newInstance(set);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	private L2SkillType()
	{
		_class = L2SkillDefault.class;
	}
	
	private L2SkillType(Class<? extends L2Skill> classType)
	{
		_class = classType;
	}
}
