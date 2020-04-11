package net.sf.l2j.gameserver.model.itemcontainer.listeners;

import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;

public interface OnEquipListener
{
	public void onEquip(int slot, ItemInstance item, Playable actor);
	
	public void onUnequip(int slot, ItemInstance item, Playable actor);
}