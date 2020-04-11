package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.model.L2ShortCut;

/**
 * sample 56 01000000 04000000 dd9fb640 01000000 56 02000000 07000000 38000000 03000000 01000000 56 03000000 00000000 02000000 01000000 format dd d/dd/d d
 * @version $Revision: 1.3.2.1.2.3 $ $Date: 2005/03/27 15:29:39 $
 */
public class ShortCutRegister extends L2GameServerPacket
{
	private final L2ShortCut shortcut;
	
	public ShortCutRegister(final L2ShortCut shortcut)
	{
		this.shortcut = shortcut;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x44);
		
		writeD(shortcut.getType());
		writeD(shortcut.getSlot() + shortcut.getPage() * 12); // C4 Client
		switch (shortcut.getType())
		{
			case L2ShortCut.TYPE_ITEM: // 1
				writeD(shortcut.getId());
				break;
			case L2ShortCut.TYPE_SKILL: // 2
				writeD(shortcut.getId());
				writeD(shortcut.getLevel());
				writeC(0x00); // C5
				break;
			case L2ShortCut.TYPE_ACTION: // 3
				writeD(shortcut.getId());
				break;
			case L2ShortCut.TYPE_MACRO: // 4
				writeD(shortcut.getId());
				break;
			case L2ShortCut.TYPE_RECIPE: // 5
				writeD(shortcut.getId());
				break;
			default:
				writeD(shortcut.getId());
		}
		
		writeD(1);// ??
	}
	
	@Override
	public String getType()
	{
		return "[S] 44 ShortCutRegister";
	}
}
