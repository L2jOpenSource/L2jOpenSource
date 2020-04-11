package com.l2jfrozen.gameserver.handler.itemhandlers;

import com.l2jfrozen.gameserver.handler.IItemHandler;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.MagicSkillUser;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.templates.L2Item;
import com.l2jfrozen.gameserver.templates.L2Weapon;
import com.l2jfrozen.gameserver.templates.L2WeaponType;
import com.l2jfrozen.gameserver.util.Broadcast;

/**
 * @author -Nemesiss-
 */
public class FishShots implements IItemHandler
{
	// All the item IDs that this handler knows.
	private static final int[] ITEM_IDS =
	{
		6535,
		6536,
		6537,
		6538,
		6539,
		6540
	};
	private static final int[] SKILL_IDS =
	{
		2181,
		2182,
		2183,
		2184,
		2185,
		2186
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
		
		if (weaponInst == null || weaponItem.getItemType() != L2WeaponType.ROD)
		{
			return;
		}
		
		if (weaponInst.getChargedFishshot())
		{
			// spiritshot is already active
			return;
		}
		
		final int FishshotId = item.getItemId();
		final int grade = weaponItem.getCrystalType();
		final int count = item.getCount();
		
		weaponItem = null;
		
		if (grade == L2Item.CRYSTAL_NONE && FishshotId != 6535 || grade == L2Item.CRYSTAL_D && FishshotId != 6536 || grade == L2Item.CRYSTAL_C && FishshotId != 6537 || grade == L2Item.CRYSTAL_B && FishshotId != 6538 || grade == L2Item.CRYSTAL_A && FishshotId != 6539 || grade == L2Item.CRYSTAL_S && FishshotId != 6540)
		{
			// 1479 - This fishing shot is not fit for the fishing pole crystal.
			activeChar.sendPacket(new SystemMessage(SystemMessageId.WRONG_FISHINGSHOT_GRADE));
			return;
		}
		
		if (count < 1)
		{
			return;
		}
		
		weaponInst.setChargedFishshot(true);
		activeChar.destroyItemWithoutTrace("Consume", item.getObjectId(), 1, null, false);
		final L2Object oldTarget = activeChar.getTarget();
		activeChar.setTarget(activeChar);
		
		weaponInst = null;
		
		// activeChar.sendPacket(new SystemMessage(SystemMessage.ENABLED_SPIRITSHOT));
		
		MagicSkillUser MSU = new MagicSkillUser(activeChar, SKILL_IDS[grade], 1, 0, 0);
		Broadcast.toSelfAndKnownPlayers(activeChar, MSU);
		MSU = null;
		activeChar.setTarget(oldTarget);
		
		activeChar = null;
	}
	
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}
