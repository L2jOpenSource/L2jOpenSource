package com.l2jfrozen.gameserver.handler.itemhandlers;

import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.handler.IItemHandler;
import com.l2jfrozen.gameserver.managers.CastleManorManager;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2MonsterInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

/**
 * @author l3x
 */
public class Harvester implements IItemHandler
{
	
	private static final int[] ITEM_IDS =
	{
		5125
		/* Harvester */
	};
	L2PcInstance activeChar;
	L2MonsterInstance target;
	
	@Override
	public void useItem(final L2PlayableInstance playable, final L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance))
		{
			return;
		}
		
		if (CastleManorManager.getInstance().isDisabled())
		{
			return;
		}
		
		activeChar = (L2PcInstance) playable;
		if (activeChar.getTarget() == null || !(activeChar.getTarget() instanceof L2MonsterInstance))
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		target = (L2MonsterInstance) activeChar.getTarget();
		if (target == null || !target.isDead())
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		final L2Skill skill = SkillTable.getInstance().getInfo(2098, 1); // harvesting skill
		activeChar.useMagic(skill, false, false);
	}
	
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}
