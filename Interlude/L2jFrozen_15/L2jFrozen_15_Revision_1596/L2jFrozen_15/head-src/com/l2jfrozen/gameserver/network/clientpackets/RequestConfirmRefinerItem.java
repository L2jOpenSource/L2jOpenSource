package com.l2jfrozen.gameserver.network.clientpackets;

import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ExConfirmVariationRefiner;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.templates.L2Item;

/**
 * Fromat(ch) dd
 * @author -Wooden-
 */
public class RequestConfirmRefinerItem extends L2GameClientPacket
{
	private static final int GEMSTONE_D = 2130;
	private static final int GEMSTONE_C = 2131;
	
	private int targetItemObjId;
	private int refinerItemObjId;
	
	@Override
	protected void readImpl()
	{
		targetItemObjId = readD();
		refinerItemObjId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		final L2ItemInstance targetItem = (L2ItemInstance) L2World.getInstance().findObject(targetItemObjId);
		final L2ItemInstance refinerItem = (L2ItemInstance) L2World.getInstance().findObject(refinerItemObjId);
		
		if (targetItem == null || refinerItem == null)
		{
			return;
		}
		
		final int itemGrade = targetItem.getItem().getItemGrade();
		final int refinerItemId = refinerItem.getItem().getItemId();
		
		// is the item a life stone?
		if (refinerItemId < 8723 || refinerItemId > 8762)
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.THIS_IS_NOT_A_SUITABLE_ITEM));
			return;
		}
		
		int gemstoneCount = 0;
		int gemstoneItemId = 0;
		getLifeStoneLevel(refinerItemId);
		final SystemMessage sm = new SystemMessage(SystemMessageId.REQUIRES_S1_S2);
		
		switch (itemGrade)
		{
			case L2Item.CRYSTAL_C:
				gemstoneCount = 20;
				gemstoneItemId = GEMSTONE_D;
				sm.addNumber(gemstoneCount);
				sm.addString("Gemstone D");
				break;
			case L2Item.CRYSTAL_B:
				gemstoneCount = 30;
				gemstoneItemId = GEMSTONE_D;
				sm.addNumber(gemstoneCount);
				sm.addString("Gemstone D");
				break;
			case L2Item.CRYSTAL_A:
				gemstoneCount = 20;
				gemstoneItemId = GEMSTONE_C;
				sm.addNumber(gemstoneCount);
				sm.addString("Gemstone C");
				break;
			case L2Item.CRYSTAL_S:
				gemstoneCount = 25;
				gemstoneItemId = GEMSTONE_C;
				sm.addNumber(gemstoneCount);
				sm.addString("Gemstone C");
				break;
		}
		
		activeChar.sendPacket(new ExConfirmVariationRefiner(refinerItemObjId, refinerItemId, gemstoneItemId, gemstoneCount));
		activeChar.sendPacket(sm);
	}
	
	private int getLifeStoneGrade(int itemId)
	{
		itemId -= 8723;
		if (itemId < 10)
		{
			return 0; // normal grade
		}
		
		if (itemId < 20)
		{
			return 1; // mid grade
		}
		
		if (itemId < 30)
		{
			return 2; // high grade
		}
		
		return 3; // top grade
	}
	
	private int getLifeStoneLevel(int itemId)
	{
		itemId -= 10 * getLifeStoneGrade(itemId);
		itemId -= 8722;
		return itemId;
	}
	
	@Override
	public String getType()
	{
		return "[C] D0:2A RequestConfirmRefinerItem";
	}
}