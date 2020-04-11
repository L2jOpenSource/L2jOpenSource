package com.l2jfrozen.gameserver.handler.itemhandlers;

import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.handler.IItemHandler;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.actor.instance.L2ChestInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

public class ChestKey implements IItemHandler
{
	public static final int INTERACTION_DISTANCE = 100;
	
	private static final int[] ITEM_IDS =
	{
		6665,
		6666,
		6667,
		6668,
		6669,
		6670,
		6671,
		6672
		// deluxe key
	};
	
	@Override
	public void useItem(final L2PlayableInstance playable, final L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance))
		{
			return;
		}
		
		L2PcInstance activeChar = (L2PcInstance) playable;
		final int itemId = item.getItemId();
		L2Skill skill = SkillTable.getInstance().getInfo(2229, itemId - 6664);// box key skill
		L2Object target = activeChar.getTarget();
		
		if (!(target instanceof L2ChestInstance))
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
		}
		else
		{
			L2ChestInstance chest = (L2ChestInstance) target;
			if (chest.isDead() || chest.isInteracted())
			{
				activeChar.sendMessage("The chest Is empty.");
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				
				return;
			}
			activeChar.useMagic(skill, false, false);
			chest = null;
		}
		
		activeChar = null;
		skill = null;
		target = null;
	}
	
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
	
}
