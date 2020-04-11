package com.l2jfrozen.gameserver.handler.itemhandlers;

import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.handler.IItemHandler;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PetInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

public class CustomPotions implements IItemHandler
{
	private static final int[] ITEM_IDS =
	{
		9720,
		9721,
		9722,
		9723,
		9724,
		9725,
		9726,
		9727,
		9728,
		9729,
		9730,
		9731,
	};
	
	@Override
	public synchronized void useItem(final L2PlayableInstance playable, final L2ItemInstance item)
	{
		L2PcInstance activeChar;
		boolean res = false;
		
		if (playable instanceof L2PcInstance)
		{
			activeChar = (L2PcInstance) playable;
		}
		else if (playable instanceof L2PetInstance)
		{
			activeChar = ((L2PetInstance) playable).getOwner();
		}
		else
		{
			return;
		}
		
		if (activeChar.isInOlympiadMode())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT));
			return;
		}
		
		if (activeChar.isAllSkillsDisabled())
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		final int itemId = item.getItemId();
		
		if (itemId >= 9720 && itemId <= 9731)
		{
			res = usePotion(activeChar, itemId, 1);
		}
		
		activeChar = null;
		
		if (res)
		{
			playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
		}
	}
	
	public boolean usePotion(final L2PcInstance activeChar, final int magicId, final int level)
	{
		final L2Skill skill = SkillTable.getInstance().getInfo(magicId, level);
		if (skill != null)
		{
			activeChar.doCast(skill);
			if (!((activeChar.isSitting() || activeChar.isParalyzed() || activeChar.isAway() || activeChar.isFakeDeath()) && !skill.isPotion()))
			{
				return true;
			}
		}
		return false;
	}
	
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}
