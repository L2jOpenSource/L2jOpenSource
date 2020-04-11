package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.controllers.GameTimeController;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

/**
 * This class ...
 * @version $Revision: 1.4.2.5.2.6 $ $Date: 2005/03/27 15:29:39 $
 */
public class CharSelected extends L2GameServerPacket
{
	private final L2PcInstance activeChar;
	private final int sessionId;
	
	/**
	 * @param cha
	 * @param sessionId
	 */
	public CharSelected(final L2PcInstance cha, final int sessionId)
	{
		activeChar = cha;
		this.sessionId = sessionId;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x15);
		
		writeS(activeChar.getName());
		writeD(activeChar.getCharId()); // ??
		writeS(activeChar.getTitle());
		writeD(sessionId);
		writeD(activeChar.getClanId());
		writeD(0x00); // ??
		writeD(activeChar.getAppearance().getSex() ? 1 : 0);
		writeD(activeChar.getRace().ordinal());
		writeD(activeChar.getClassId().getId());
		writeD(0x01); // active ??
		writeD(activeChar.getX());
		writeD(activeChar.getY());
		writeD(activeChar.getZ());
		
		writeF(activeChar.getCurrentHp());
		writeF(activeChar.getCurrentMp());
		writeD(activeChar.getSp());
		writeQ(activeChar.getExp());
		writeD(activeChar.getLevel());
		writeD(activeChar.getKarma()); // thx evill33t
		writeD(0x0); // ?
		writeD(activeChar.getINT());
		writeD(activeChar.getSTR());
		writeD(activeChar.getCON());
		writeD(activeChar.getMEN());
		writeD(activeChar.getDEX());
		writeD(activeChar.getWIT());
		for (int i = 0; i < 30; i++)
		{
			writeD(0x00);
		}
		// writeD(0); //c3
		// writeD(0); //c3
		// writeD(0); //c3
		
		writeD(0x00); // c3 work
		writeD(0x00); // c3 work
		
		// extra info
		writeD(GameTimeController.getInstance().getGameTime()); // in-game time
		
		writeD(0x00); //
		
		writeD(0x00); // c3
		
		writeD(0x00); // c3 InspectorBin
		writeD(0x00); // c3
		writeD(0x00); // c3
		writeD(0x00); // c3
		
		writeD(0x00); // c3 InspectorBin for 528 client
		writeD(0x00); // c3
		writeD(0x00); // c3
		writeD(0x00); // c3
		writeD(0x00); // c3
		writeD(0x00); // c3
		writeD(0x00); // c3
		writeD(0x00); // c3
	}
	
	@Override
	public String getType()
	{
		return "[S] 15 CharSelected";
	}
}