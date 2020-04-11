package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.model.L2Summon;

/**
 * This class ...
 * @author  Yme
 * @version $Revision: 1.3.2.2.2.4 $ $Date: 2005/03/29 23:15:10 $
 */
public class PetStatusShow extends L2GameServerPacket
{
	private final int summonType;
	
	public PetStatusShow(final L2Summon summon)
	{
		summonType = summon.getSummonType();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xB0);
		writeD(summonType);
	}
	
	@Override
	public String getType()
	{
		return "[S] B0 PetStatusShow";
	}
}
