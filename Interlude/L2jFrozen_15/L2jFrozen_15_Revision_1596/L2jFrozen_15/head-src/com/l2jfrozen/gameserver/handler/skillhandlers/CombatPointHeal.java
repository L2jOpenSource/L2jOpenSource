package com.l2jfrozen.gameserver.handler.skillhandlers;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.handler.ISkillHandler;
import com.l2jfrozen.gameserver.handler.SkillHandler;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.L2Skill.SkillType;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.StatusUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

public class CombatPointHeal implements ISkillHandler
{
	private static final SkillType[] SKILL_IDS =
	{
		SkillType.COMBATPOINTHEAL,
		SkillType.COMBATPOINTPERCENTHEAL
	};
	
	@Override
	public void useSkill(final L2Character actChar, final L2Skill skill, final L2Object[] targets)
	{
		// check for other effects
		try
		{
			ISkillHandler handler = SkillHandler.getInstance().getSkillHandler(SkillType.BUFF);
			
			if (handler != null)
			{
				handler.useSkill(actChar, skill, targets);
			}
			
			handler = null;
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
		}
		
		for (final L2Object object : targets)
		{
			if (!(object instanceof L2Character))
			{
				continue;
			}
			
			final L2Character target = (L2Character) object;
			double cp = skill.getPower();
			if (skill.getSkillType() == SkillType.COMBATPOINTPERCENTHEAL)
			{
				cp = target.getMaxCp() * cp / 100.0;
			}
			final SystemMessage sm = new SystemMessage(SystemMessageId.S1_CP_WILL_BE_RESTORED);
			sm.addNumber((int) cp);
			target.sendPacket(sm);
			
			target.setCurrentCp(cp + target.getCurrentCp());
			final StatusUpdate sump = new StatusUpdate(target.getObjectId());
			sump.addAttribute(StatusUpdate.CUR_CP, (int) target.getCurrentCp());
			target.sendPacket(sump);
		}
	}
	
	@Override
	public SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
