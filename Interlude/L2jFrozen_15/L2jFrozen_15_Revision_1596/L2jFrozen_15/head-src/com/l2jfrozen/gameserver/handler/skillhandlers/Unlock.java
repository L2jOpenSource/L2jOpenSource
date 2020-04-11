package com.l2jfrozen.gameserver.handler.skillhandlers;

import com.l2jfrozen.gameserver.ai.CtrlIntention;
import com.l2jfrozen.gameserver.handler.ISkillHandler;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.L2Skill.SkillType;
import com.l2jfrozen.gameserver.model.actor.instance.L2ChestInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.SocialAction;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.skills.Formulas;
import com.l2jfrozen.util.random.Rnd;

public class Unlock implements ISkillHandler
{
	// private static Logger LOGGER = Logger.getLogger(Unlock.class);
	private static final SkillType[] SKILL_IDS =
	{
		SkillType.UNLOCK
	};
	
	@Override
	public void useSkill(final L2Character activeChar, final L2Skill skill, final L2Object[] targets)
	{
		L2Object[] targetList = skill.getTargetList(activeChar);
		
		if (targetList == null)
		{
			return;
		}
		
		for (final L2Object element : targetList)
		{
			L2Object target = element;
			
			final boolean success = Formulas.getInstance().calculateUnlockChance(skill);
			if (target instanceof L2DoorInstance)
			{
				L2DoorInstance door = (L2DoorInstance) target;
				if (!door.isUnlockable())
				{
					activeChar.sendPacket(new SystemMessage(SystemMessageId.UNABLE_TO_UNLOCK_DOOR));
					activeChar.sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
				
				if (success && (!door.isOpen()))
				{
					door.openMe();
					door.onOpen();
					SystemMessage msg = new SystemMessage(SystemMessageId.S1_S2);
					msg.addString("Unlock the door!");
					activeChar.sendPacket(msg);
					msg = null;
				}
				else
				{
					activeChar.sendPacket(new SystemMessage(SystemMessageId.FAILED_TO_UNLOCK_DOOR));
				}
				door = null;
			}
			else if (target instanceof L2ChestInstance)
			{
				L2ChestInstance chest = (L2ChestInstance) element;
				
				if (chest.getCurrentHp() <= 0 || chest.isInteracted())
				{
					activeChar.sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
				
				int chestChance = 0;
				int chestGroup = 0;
				int chestTrapLimit = 0;
				
				if (chest.getLevel() > 60)
				{
					chestGroup = 4;
				}
				else if (chest.getLevel() > 40)
				{
					chestGroup = 3;
				}
				else if (chest.getLevel() > 30)
				{
					chestGroup = 2;
				}
				else
				{
					chestGroup = 1;
				}
				
				switch (chestGroup)
				{
					case 1:
					{
						if (skill.getLevel() > 10)
						{
							chestChance = 100;
						}
						else if (skill.getLevel() >= 3)
						{
							chestChance = 50;
						}
						else if (skill.getLevel() == 2)
						{
							chestChance = 45;
						}
						else if (skill.getLevel() == 1)
						{
							chestChance = 40;
						}
						
						chestTrapLimit = 10;
					}
						break;
					case 2:
					{
						if (skill.getLevel() > 12)
						{
							chestChance = 100;
						}
						else if (skill.getLevel() >= 7)
						{
							chestChance = 50;
						}
						else if (skill.getLevel() == 6)
						{
							chestChance = 45;
						}
						else if (skill.getLevel() == 5)
						{
							chestChance = 40;
						}
						else if (skill.getLevel() == 4)
						{
							chestChance = 35;
						}
						else if (skill.getLevel() == 3)
						{
							chestChance = 30;
						}
						
						chestTrapLimit = 30;
					}
						break;
					case 3:
					{
						if (skill.getLevel() >= 14)
						{
							chestChance = 50;
						}
						else if (skill.getLevel() == 13)
						{
							chestChance = 45;
						}
						else if (skill.getLevel() == 12)
						{
							chestChance = 40;
						}
						else if (skill.getLevel() == 11)
						{
							chestChance = 35;
						}
						else if (skill.getLevel() == 10)
						{
							chestChance = 30;
						}
						else if (skill.getLevel() == 9)
						{
							chestChance = 25;
						}
						else if (skill.getLevel() == 8)
						{
							chestChance = 20;
						}
						else if (skill.getLevel() == 7)
						{
							chestChance = 15;
						}
						else if (skill.getLevel() == 6)
						{
							chestChance = 10;
						}
						
						chestTrapLimit = 50;
					}
						break;
					case 4:
					{
						if (skill.getLevel() >= 14)
						{
							chestChance = 50;
						}
						else if (skill.getLevel() == 13)
						{
							chestChance = 45;
						}
						else if (skill.getLevel() == 12)
						{
							chestChance = 40;
						}
						else if (skill.getLevel() == 11)
						{
							chestChance = 35;
						}
						
						chestTrapLimit = 80;
					}
						break;
				}
				
				if (Rnd.get(100) <= chestChance)
				{
					activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), 3));
					chest.setSpecialDrop();
					chest.setMustRewardExpSp(false);
					chest.setInteracted();
					chest.reduceCurrentHp(99999999, activeChar);
				}
				else
				{
					activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), 13));
					if (Rnd.get(100) < chestTrapLimit)
					{
						chest.chestTrap(activeChar);
					}
					chest.setInteracted();
					chest.addDamageHate(activeChar, 0, 1);
					chest.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, activeChar);
				}
				chest = null;
			}
			target = null;
		}
		targetList = null;
	}
	
	@Override
	public SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}