package com.l2jfrozen.gameserver.handler.skillhandlers;

import com.l2jfrozen.gameserver.ai.CtrlEvent;
import com.l2jfrozen.gameserver.handler.ISkillHandler;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.L2Skill.SkillType;
import com.l2jfrozen.gameserver.model.actor.instance.L2MonsterInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.skills.Formulas;

/**
 * @author drunk
 */
public class Spoil implements ISkillHandler
{
	// private static Logger LOGGER = Logger.getLogger(Spoil.class);
	private static final SkillType[] SKILL_IDS =
	{
		SkillType.SPOIL
	};
	
	@Override
	public void useSkill(final L2Character activeChar, final L2Skill skill, final L2Object[] targets)
	{
		if (!(activeChar instanceof L2PcInstance))
		{
			return;
		}
		
		if (targets == null)
		{
			return;
		}
		
		for (final L2Object target1 : targets)
		{
			if (!(target1 instanceof L2MonsterInstance))
			{
				continue;
			}
			
			L2MonsterInstance target = (L2MonsterInstance) target1;
			
			if (target.isSpoil())
			{
				activeChar.sendPacket(new SystemMessage(SystemMessageId.ALREDAY_SPOILED));
				continue;
			}
			
			// SPOIL SYSTEM by Lbaldi
			boolean spoil = false;
			if (!target.isDead())
			{
				spoil = Formulas.calcMagicSuccess(activeChar, (L2Character) target1, skill);
				
				if (spoil)
				{
					target.setSpoil(true);
					target.setIsSpoiledBy(activeChar.getObjectId());
					activeChar.sendPacket(new SystemMessage(SystemMessageId.SPOIL_SUCCESS));
				}
				else
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.S1_WAS_UNAFFECTED_BY_S2);
					sm.addString(target.getName());
					sm.addSkillName(skill.getDisplayId());
					activeChar.sendPacket(sm);
					sm = null;
				}
				target.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, activeChar);
			}
			
			target = null;
		}
	}
	
	@Override
	public SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
