package com.l2jfrozen.gameserver.network.serverpackets;

import java.util.List;

import com.l2jfrozen.gameserver.model.L2ShortCut;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

/**
 * ShortCutInit format d *(1dddd)/(2ddddd)/(3dddd)
 * @version $Revision: 1.3.2.1.2.4 $ $Date: 2005/03/27 15:29:39 $
 */
public class ShortCutInit extends L2GameServerPacket
{
	private List<L2ShortCut> shortCuts;
	private L2PcInstance activeChar;
	
	public ShortCutInit(final L2PcInstance activeChar)
	{
		this.activeChar = activeChar;
		
		if (this.activeChar == null)
		{
			return;
		}
		
		shortCuts = this.activeChar.getAllShortCuts();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x45);
		writeD(shortCuts.size());
		
		for (L2ShortCut sc : shortCuts)
		{
			writeD(sc.getType());
			writeD(sc.getSlot() + sc.getPage() * 12);
			
			switch (sc.getType())
			{
				case L2ShortCut.TYPE_ITEM: // 1
					writeD(sc.getId());
					writeD(0x01);
					writeD(-1);
					writeD(0x00);
					writeD(0x00);
					writeH(0x00);
					writeH(0x00);
					break;
				
				case L2ShortCut.TYPE_SKILL: // 2
					writeD(sc.getId());
					writeD(sc.getLevel());
					writeC(0x00); // C5
					writeD(0x01); // C6
					break;
				
				default:
					writeD(sc.getId());
					writeD(0x01); // C6
			}
		}
	}
	
	@Override
	public String getType()
	{
		return "[S] 45 ShortCutInit";
	}
}
