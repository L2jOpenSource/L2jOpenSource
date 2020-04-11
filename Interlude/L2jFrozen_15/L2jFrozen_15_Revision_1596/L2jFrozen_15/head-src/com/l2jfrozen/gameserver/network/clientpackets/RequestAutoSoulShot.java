package com.l2jfrozen.gameserver.network.clientpackets;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.ShotsTable;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ExAutoSoulShot;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

public final class RequestAutoSoulShot extends L2GameClientPacket
{
	private static Logger LOGGER = Logger.getLogger(RequestAutoSoulShot.class);
	
	// format cd
	private int itemId;
	private int type; // 1 = on : 0 = off;
	
	@Override
	protected void readImpl()
	{
		itemId = readD();
		type = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
		{
			return;
		}
		
		// Like L2OFF you can't use soulshots while sitting
		if (activeChar.isSitting() && ShotsTable.getInstance().isShot(itemId))
		{
			final SystemMessage sm = new SystemMessage(SystemMessageId.CANNOT_AUTO_USE_LACK_OF_S1);
			sm.addItemName(itemId);
			activeChar.sendPacket(sm);
			return;
		}
		
		if (activeChar.getPrivateStoreType() == 0 && activeChar.getActiveRequester() == null && !activeChar.isDead())
		{
			if (Config.DEBUG)
			{
				LOGGER.debug("AutoSoulShot:" + itemId);
			}
			
			final L2ItemInstance item = activeChar.getInventory().getItemByItemId(itemId);
			
			if (item != null)
			{
				if (type == 1)
				{
					
					// Fishingshots are not automatic on retail
					if (itemId < 6535 || itemId > 6540)
					{
						activeChar.addAutoSoulShot(itemId);
						
						// Attempt to charge first shot on activation
						if (itemId == 6645 || itemId == 6646 || itemId == 6647)
						{
							// Like L2OFF you can active automatic SS only if you have a pet
							if (activeChar.getPet() != null)
							{
								// activeChar.addAutoSoulShot(_itemId);
								// ExAutoSoulShot atk = new ExAutoSoulShot(_itemId, type);
								// activeChar.sendPacket(atk);
								
								// start the auto soulshot use
								final SystemMessage sm = new SystemMessage(SystemMessageId.USE_OF_S1_WILL_BE_AUTO);
								sm.addString(item.getItemName());
								activeChar.sendPacket(sm);
								
								activeChar.rechargeAutoSoulShot(true, true, true, 0);
							}
							else
							{
								final SystemMessage sm = new SystemMessage(SystemMessageId.NO_SERVITOR_CANNOT_AUTOMATE_USE);
								sm.addString(item.getItemName());
								activeChar.sendPacket(sm);
								return;
							}
						}
						else
						{
							if (activeChar.getActiveWeaponItem() != activeChar.getFistsWeaponItem() && item.getItem().getCrystalType() == activeChar.getActiveWeaponItem().getCrystalType())
							{
								if (itemId >= 3947 && itemId <= 3952 && activeChar.isInOlympiadMode())
								{
									final SystemMessage sm = new SystemMessage(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT);
									sm.addString(item.getItemName());
									activeChar.sendPacket(sm);
								}
								else
								{
									// activeChar.addAutoSoulShot(_itemId);
									
									// start the auto soulshot use
									final SystemMessage sm = new SystemMessage(SystemMessageId.USE_OF_S1_WILL_BE_AUTO);
									sm.addString(item.getItemName());
									activeChar.sendPacket(sm);
									
								}
								
							}
							else
							{
								if (itemId >= 2509 && itemId <= 2514 || itemId >= 3947 && itemId <= 3952 || itemId == 5790)
								{
									activeChar.sendPacket(new SystemMessage(SystemMessageId.SPIRITSHOTS_GRADE_MISMATCH));
								}
								else
								{
									activeChar.sendPacket(new SystemMessage(SystemMessageId.SOULSHOTS_GRADE_MISMATCH));
								}
							}
							
							activeChar.rechargeAutoSoulShot(true, true, false, 0);
							
						}
					}
					
				}
				else if (type == 0)
				{
					activeChar.removeAutoSoulShot(itemId);
					// ExAutoSoulShot atk = new ExAutoSoulShot(_itemId, type);
					// activeChar.sendPacket(atk);
					
					// cancel the auto soulshot use
					final SystemMessage sm = new SystemMessage(SystemMessageId.AUTO_USE_OF_S1_CANCELLED);
					sm.addString(item.getItemName());
					activeChar.sendPacket(sm);
				}
				
				final ExAutoSoulShot atk = new ExAutoSoulShot(itemId, type);
				activeChar.sendPacket(atk);
			}
		}
	}
	
	@Override
	public String getType()
	{
		return "[C] CF RequestAutoSoulShot";
	}
}
