package com.l2jfrozen.gameserver.handler.itemhandlers;

import com.l2jfrozen.gameserver.handler.IItemHandler;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.ShowXMasSeal;

/**
 * @author devScarlet & mrTJO
 */
public class SpecialXMas implements IItemHandler
{
	private static int[] itemIds =
	{
		5555
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
		
		if (activeChar.isParalyzed())
		{
			activeChar.sendMessage("You Cannot Use This While You Are Paralyzed");
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (itemId == 5555) // Token of Love
		{
			ShowXMasSeal SXS = new ShowXMasSeal(5555);
			activeChar.sendPacket(SXS);
			// activeChar.broadcastPacket(SXS);
			SXS = null;
		}
		activeChar = null;
	}
	
	/**
	 * @see com.l2jfrozen.gameserver.handler.IItemHandler#getItemIds()
	 */
	@Override
	public int[] getItemIds()
	{
		return itemIds;
	}
}
