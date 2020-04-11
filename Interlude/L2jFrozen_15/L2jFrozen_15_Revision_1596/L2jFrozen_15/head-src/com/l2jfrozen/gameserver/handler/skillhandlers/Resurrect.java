package com.l2jfrozen.gameserver.handler.skillhandlers;

import java.util.ArrayList;
import java.util.List;

import com.l2jfrozen.gameserver.handler.ISkillHandler;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.L2Skill.SkillTargetType;
import com.l2jfrozen.gameserver.model.L2Skill.SkillType;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PetInstance;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.skills.Formulas;
import com.l2jfrozen.gameserver.taskmanager.DecayTaskManager;

/**
 * This class ...
 * @version $Revision: 1.1.2.5.2.4 $ $Date: 2005/04/03 15:55:03 $
 */

public class Resurrect implements ISkillHandler
{
	// private static Logger LOGGER = Logger.getLogger(Resurrect.class);
	
	private static final SkillType[] SKILL_IDS =
	{
		SkillType.RESURRECT
	};
	
	@Override
	public void useSkill(final L2Character activeChar, final L2Skill skill, final L2Object[] targets)
	{
		L2PcInstance player = null;
		if (activeChar instanceof L2PcInstance)
		{
			player = (L2PcInstance) activeChar;
		}
		
		L2Character target = null;
		L2PcInstance targetPlayer;
		List<L2Character> targetToRes = new ArrayList<>();
		
		for (final L2Object target2 : targets)
		{
			if (target2 == null)
			{
				continue;
			}
			
			target = (L2Character) target2;
			if (target instanceof L2PcInstance)
			{
				targetPlayer = (L2PcInstance) target;
				
				// Check for same party or for same clan, if target is for clan.
				if (skill.getTargetType() == SkillTargetType.TARGET_CORPSE_CLAN)
				{
					if ((player == null) || player.getClanId() != targetPlayer.getClanId())
					{
						continue;
					}
				}
				
				targetPlayer = null;
			}
			
			if (target.isVisible())
			{
				targetToRes.add(target);
			}
		}
		
		player = null;
		target = null;
		targetPlayer = null;
		
		if (targetToRes.size() == 0)
		{
			activeChar.abortCast();
			activeChar.sendPacket(SystemMessage.sendString("No valid target to resurrect"));
		}
		
		for (final L2Character cha : targetToRes)
		{
			if (activeChar instanceof L2PcInstance)
			{
				if (cha instanceof L2PcInstance)
				{
					((L2PcInstance) cha).reviveRequest((L2PcInstance) activeChar, skill, false);
				}
				else if (cha instanceof L2PetInstance)
				{
					if (((L2PetInstance) cha).getOwner() == activeChar)
					{
						cha.doRevive(Formulas.getInstance().calculateSkillResurrectRestorePercent(skill.getPower(), activeChar));
					}
					else
					{
						((L2PetInstance) cha).getOwner().reviveRequest((L2PcInstance) activeChar, skill, true);
					}
				}
				else
				{
					cha.doRevive(Formulas.getInstance().calculateSkillResurrectRestorePercent(skill.getPower(), activeChar));
				}
			}
			else
			{
				DecayTaskManager.getInstance().cancelDecayTask(cha);
				cha.doRevive(Formulas.getInstance().calculateSkillResurrectRestorePercent(skill.getPower(), activeChar));
			}
		}
		
		targetToRes = null;
		
		if (skill.isMagic() && skill.useSpiritShot())
		{
			if (activeChar.checkBss())
			{
				activeChar.removeBss();
			}
			if (activeChar.checkSps())
			{
				activeChar.removeSps();
			}
		}
		else if (skill.useSoulShot())
		{
			if (activeChar.checkSs())
			{
				activeChar.removeSs();
			}
		}
		
	}
	
	@Override
	public SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
