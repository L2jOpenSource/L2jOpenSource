package l2r.gameserver.model.actor.tasks.player;

import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.items.instance.L2ItemInstance;

import gr.sr.interf.SunriseEvents;

/**
 * @author vGodFather
 */
public class WeaponEquipTask implements Runnable
{
	private final int _itemObjId;
	private final L2PcInstance _activeChar;
	
	public WeaponEquipTask(int itemObjId, L2PcInstance activeChar)
	{
		_itemObjId = itemObjId;
		_activeChar = activeChar;
	}
	
	@Override
	public void run()
	{
		final L2ItemInstance item = _activeChar.getInventory().getItemByObjectId(_itemObjId);
		if (item == null)
		{
			return;
		}
		
		// Equip or unEquip
		_activeChar.useEquippableItem(item, false);
		
		if (SunriseEvents.isInEvent(_activeChar))
		{
			SunriseEvents.onUseItem(_activeChar, item);
		}
	}
}