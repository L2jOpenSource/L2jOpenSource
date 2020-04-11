package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.Shortcut;

public class ShortCutRegister extends L2GameServerPacket
{
	private final Shortcut _shortcut;
	
	public ShortCutRegister(Shortcut shortcut)
	{
		_shortcut = shortcut;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x44);
		
		writeD(_shortcut.getType().ordinal());
		writeD(_shortcut.getSlot() + _shortcut.getPage() * 12);
		switch (_shortcut.getType())
		{
			case ITEM:
				writeD(_shortcut.getId());
				writeD(_shortcut.getCharacterType());
				writeD(_shortcut.getSharedReuseGroup());
				break;
			
			case SKILL:
				writeD(_shortcut.getId());
				writeD(_shortcut.getLevel());
				writeC(0x00);
				writeD(_shortcut.getCharacterType());
				break;
			
			default:
				writeD(_shortcut.getId());
				writeD(_shortcut.getCharacterType());
		}
	}
}