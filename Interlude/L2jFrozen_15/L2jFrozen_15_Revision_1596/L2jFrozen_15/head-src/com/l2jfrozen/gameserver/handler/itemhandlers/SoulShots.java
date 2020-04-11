package com.l2jfrozen.gameserver.handler.itemhandlers;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.handler.IItemHandler;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ExAutoSoulShot;
import com.l2jfrozen.gameserver.network.serverpackets.MagicSkillUser;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.skills.Stats;
import com.l2jfrozen.gameserver.templates.L2Item;
import com.l2jfrozen.gameserver.templates.L2Weapon;
import com.l2jfrozen.gameserver.util.Broadcast;

/**
 * This class ...
 * @version $Revision: 1.2.4.5 $ $Date: 2009/04/13 03:12:07 $
 * @author  programmos
 */

public class SoulShots implements IItemHandler
{
	// All the item IDs that this handler knows.
	private static final int[] ITEM_IDS =
	{
		5789,
		1835,
		1463,
		1464,
		1465,
		1466,
		1467
	};
	private static final int[] SKILL_IDS =
	{
		2039,
		2150,
		2151,
		2152,
		2153,
		2154
	};
	
	@Override
	public void useItem(final L2PlayableInstance playable, final L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance))
		{
			return;
		}
		
		L2PcInstance activeChar = (L2PcInstance) playable;
		L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();
		L2Weapon weaponItem = activeChar.getActiveWeaponItem();
		final int itemId = item.getItemId();
		
		// Check if Soulshot can be used
		if (weaponInst == null || weaponItem.getSoulShotCount() == 0)
		{
			if (!activeChar.getAutoSoulShot().containsKey(itemId))
			{
				activeChar.sendPacket(new SystemMessage(SystemMessageId.CANNOT_USE_SOULSHOTS));
			}
			return;
		}
		
		// Check for correct grade
		final int weaponGrade = weaponItem.getCrystalType();
		if (weaponGrade == L2Item.CRYSTAL_NONE && itemId != 5789 && itemId != 1835 || weaponGrade == L2Item.CRYSTAL_D && itemId != 1463 || weaponGrade == L2Item.CRYSTAL_C && itemId != 1464 || weaponGrade == L2Item.CRYSTAL_B && itemId != 1465 || weaponGrade == L2Item.CRYSTAL_A && itemId != 1466 || weaponGrade == L2Item.CRYSTAL_S && itemId != 1467)
		{
			if (!activeChar.getAutoSoulShot().containsKey(itemId))
			{
				activeChar.sendPacket(new SystemMessage(SystemMessageId.SOULSHOTS_GRADE_MISMATCH));
			}
			return;
		}
		
		activeChar.soulShotLock.lock();
		try
		{
			// Check if Soulshot is already active
			if (weaponInst.getChargedSoulshot() != L2ItemInstance.CHARGED_NONE)
			{
				return;
			}
			
			// Consume Soulshots if player has enough of them
			final int saSSCount = (int) activeChar.getStat().calcStat(Stats.SOULSHOT_COUNT, 0, null, null);
			final int SSCount = saSSCount == 0 ? weaponItem.getSoulShotCount() : saSSCount;
			
			weaponItem = null;
			
			// TODO: test ss
			if (!Config.DONT_DESTROY_SS)
			{
				if (!activeChar.destroyItemWithoutTrace("Consume", item.getObjectId(), SSCount, null, false))
				{
					if (activeChar.getAutoSoulShot().containsKey(itemId))
					{
						activeChar.removeAutoSoulShot(itemId);
						activeChar.sendPacket(new ExAutoSoulShot(itemId, 0));
						
						SystemMessage sm = new SystemMessage(SystemMessageId.AUTO_USE_OF_S1_CANCELLED);
						sm.addString(item.getItem().getName());
						activeChar.sendPacket(sm);
						sm = null;
					}
					else
					{
						activeChar.sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_SOULSHOTS));
					}
					return;
				}
			}
			
			// Charge soulshot
			weaponInst.setChargedSoulshot(L2ItemInstance.CHARGED_SOULSHOT);
			
			weaponInst = null;
		}
		finally
		{
			activeChar.soulShotLock.unlock();
		}
		
		// Send message to client
		activeChar.sendPacket(new SystemMessage(SystemMessageId.ENABLED_SOULSHOT));
		Broadcast.toSelfAndKnownPlayersInRadius(activeChar, new MagicSkillUser(activeChar, activeChar, SKILL_IDS[weaponGrade], 1, 0, 0), 360000/* 600 */);
		
		activeChar = null;
	}
	
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}
