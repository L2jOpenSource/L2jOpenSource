package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.model.actor.instance.L2HennaInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

public class HennaItemInfo extends L2GameServerPacket
{
	private final L2PcInstance activeChar;
	private final L2HennaInstance henna;
	
	public HennaItemInfo(final L2HennaInstance henna, final L2PcInstance player)
	{
		this.henna = henna;
		activeChar = player;
	}
	
	@Override
	protected final void writeImpl()
	{
		
		writeC(0xe3);
		writeD(henna.getSymbolId()); // symbol Id
		writeD(henna.getItemIdDye()); // item id of dye
		writeD(henna.getAmountDyeRequire()); // total amount of dye require
		writeD(henna.getPrice()); // total amount of aden require to draw symbol
		writeD(1); // able to draw or not 0 is false and 1 is true
		writeD(activeChar.getAdena());
		
		writeD(activeChar.getINT()); // current INT
		writeC(activeChar.getINT() + henna.getStatINT()); // equip INT
		writeD(activeChar.getSTR()); // current STR
		writeC(activeChar.getSTR() + henna.getStatSTR()); // equip STR
		writeD(activeChar.getCON()); // current CON
		writeC(activeChar.getCON() + henna.getStatCON()); // equip CON
		writeD(activeChar.getMEN()); // current MEM
		writeC(activeChar.getMEN() + henna.getStatMEM()); // equip MEM
		writeD(activeChar.getDEX()); // current DEX
		writeC(activeChar.getDEX() + henna.getStatDEX()); // equip DEX
		writeD(activeChar.getWIT()); // current WIT
		writeC(activeChar.getWIT() + henna.getStatWIT()); // equip WIT
	}
	
	@Override
	public String getType()
	{
		return "[S] E3 HennaItemInfo";
	}
}
