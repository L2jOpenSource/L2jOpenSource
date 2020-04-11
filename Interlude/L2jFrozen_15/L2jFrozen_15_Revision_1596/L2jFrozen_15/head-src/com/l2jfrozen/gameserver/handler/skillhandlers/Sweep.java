package com.l2jfrozen.gameserver.handler.skillhandlers;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.handler.ISkillHandler;
import com.l2jfrozen.gameserver.model.L2Attackable;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.L2Skill.SkillType;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.ItemList;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

/**
 * @author drunk
 */
public class Sweep implements ISkillHandler
{
	// private static Logger LOGGER = Logger.getLogger(Sweep.class);
	private static final SkillType[] SKILL_IDS =
	{
		SkillType.SWEEP
	};
	
	@Override
	public void useSkill(final L2Character activeChar, final L2Skill skill, final L2Object[] targets)
	{
		if (!(activeChar instanceof L2PcInstance))
		{
			return;
		}
		
		final L2PcInstance player = (L2PcInstance) activeChar;
		final InventoryUpdate iu = Config.FORCE_INVENTORY_UPDATE ? null : new InventoryUpdate();
		boolean send = false;
		
		for (final L2Object target1 : targets)
		{
			if (!(target1 instanceof L2Attackable))
			{
				continue;
			}
			
			final L2Attackable target = (L2Attackable) target1;
			L2Attackable.RewardItem[] items = null;
			boolean isSweeping = false;
			synchronized (target)
			{
				if (target.isSweepActive())
				{
					items = target.takeSweep();
					isSweeping = true;
				}
			}
			
			if (isSweeping)
			{
				if (items == null || items.length == 0)
				{
					continue;
				}
				for (final L2Attackable.RewardItem ritem : items)
				{
					if (player.isInParty())
					{
						player.getParty().distributeItem(player, ritem, true, target);
					}
					else
					{
						L2ItemInstance item = player.getInventory().addItem("Sweep", ritem.getItemId(), ritem.getCount(), player, target);
						if (iu != null)
						{
							iu.addItem(item);
						}
						send = true;
						item = null;
						
						SystemMessage smsg;
						
						if (ritem.getCount() > 1)
						{
							smsg = new SystemMessage(SystemMessageId.EARNED_S2_S1_S); // earned $s2$s1
							smsg.addItemName(ritem.getItemId());
							smsg.addNumber(ritem.getCount());
						}
						else
						{
							smsg = new SystemMessage(SystemMessageId.EARNED_ITEM); // earned $s1
							smsg.addItemName(ritem.getItemId());
						}
						player.sendPacket(smsg);
						smsg = null;
					}
				}
			}
			target.endDecayTask();
			
			if (send)
			{
				if (iu != null)
				{
					player.sendPacket(iu);
				}
				else
				{
					player.sendPacket(new ItemList(player, false));
				}
			}
		}
	}
	
	@Override
	public SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
