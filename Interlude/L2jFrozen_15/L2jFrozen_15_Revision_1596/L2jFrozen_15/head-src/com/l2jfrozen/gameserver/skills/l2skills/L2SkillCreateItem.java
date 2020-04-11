package com.l2jfrozen.gameserver.skills.l2skills;

import com.l2jfrozen.gameserver.idfactory.IdFactory;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ItemList;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.templates.StatsSet;
import com.l2jfrozen.util.random.Rnd;

/**
 * @author Nemesiss
 */
public class L2SkillCreateItem extends L2Skill
{
	private final int[] createItemId;
	private final int createItemCount;
	private final int randomCount;
	
	public L2SkillCreateItem(final StatsSet set)
	{
		super(set);
		createItemId = set.getIntegerArray("create_item_id");
		createItemCount = set.getInteger("create_item_count", 0);
		randomCount = set.getInteger("random_count", 1);
	}
	
	/**
	 * @see com.l2jfrozen.gameserver.model.L2Skill#useSkill(com.l2jfrozen.gameserver.model.L2Character, com.l2jfrozen.gameserver.model.L2Object[])
	 */
	@Override
	public void useSkill(final L2Character activeChar, final L2Object[] targets)
	{
		if (activeChar.isAlikeDead())
		{
			return;
		}
		if (createItemId == null || createItemCount == 0)
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.SKILL_NOT_AVAILABLE));
			return;
		}
		final L2PcInstance player = (L2PcInstance) activeChar;
		if (activeChar instanceof L2PcInstance)
		{
			final int count = createItemCount * (Rnd.nextInt(randomCount) + 1);
			final int rndid = Rnd.nextInt(createItemId.length);
			giveItems(player, createItemId[rndid], count);
		}
	}
	
	/**
	 * @param activeChar
	 * @param itemId
	 * @param count
	 */
	public void giveItems(final L2PcInstance activeChar, final int itemId, final int count)
	{
		final L2ItemInstance item = new L2ItemInstance(IdFactory.getInstance().getNextId(), itemId);
		// if(item == null)
		// return;
		
		item.setCount(count);
		activeChar.getInventory().addItem("Skill", item, activeChar, activeChar);
		
		if (count > 1)
		{
			final SystemMessage smsg = new SystemMessage(SystemMessageId.EARNED_S2_S1_S);
			smsg.addItemName(item.getItemId());
			smsg.addNumber(count);
			activeChar.sendPacket(smsg);
		}
		else
		{
			final SystemMessage smsg = new SystemMessage(SystemMessageId.EARNED_ITEM);
			smsg.addItemName(item.getItemId());
			activeChar.sendPacket(smsg);
		}
		final ItemList il = new ItemList(activeChar, false);
		activeChar.sendPacket(il);
	}
}
