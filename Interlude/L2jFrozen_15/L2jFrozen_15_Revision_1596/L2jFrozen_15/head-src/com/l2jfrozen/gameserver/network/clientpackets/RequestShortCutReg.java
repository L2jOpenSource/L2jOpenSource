package com.l2jfrozen.gameserver.network.clientpackets;

import com.l2jfrozen.gameserver.model.L2ShortCut;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.serverpackets.ShortCutRegister;

public final class RequestShortCutReg extends L2GameClientPacket
{
	private int type;
	private int id;
	private int slot;
	private int page;
	private int unk;
	
	@Override
	protected void readImpl()
	{
		type = readD();
		final int slot = readD();
		id = readD();
		unk = readD();
		
		this.slot = slot % 12;
		page = slot / 12;
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		switch (type)
		{
			case 0x01: // item
			case 0x03: // action
			case 0x04: // macro
			case 0x05: // recipe
			{
				final L2ShortCut sc = new L2ShortCut(slot, page, type, id, -1, unk);
				sendPacket(new ShortCutRegister(sc));
				activeChar.registerShortCut(sc);
				break;
			}
			case 0x02: // skill
			{
				final int level = activeChar.getSkillLevel(id);
				if (level > 0)
				{
					final L2ShortCut sc = new L2ShortCut(slot, page, type, id, level, unk);
					sendPacket(new ShortCutRegister(sc));
					activeChar.registerShortCut(sc);
				}
				break;
			}
		}
	}
	
	@Override
	public String getType()
	{
		return "[C] 33 RequestShortCutReg";
	}
}