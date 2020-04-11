package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

/**
 * Format: ch ddcdc.
 * @author KenM
 * @author ProGramMoS
 */

public class ExPCCafePointInfo extends L2GameServerPacket
{
	private final L2PcInstance character;
	private final int m_AddPoint;
	private int m_PeriodType;
	private final int remainTime;
	private int pointType;
	
	/**
	 * Instantiates a new ex pc cafe point info.
	 * @param user     the user
	 * @param modify   the modify
	 * @param add      the add
	 * @param hour     the hour
	 * @param isDouble the double
	 */
	public ExPCCafePointInfo(final L2PcInstance user, final int modify, final boolean add, final int hour, final boolean isDouble)
	{
		character = user;
		m_AddPoint = modify;
		
		if (add)
		{
			m_PeriodType = 1;
			pointType = 1;
		}
		else
		{
			if (add && isDouble)
			{
				m_PeriodType = 1;
				pointType = 0;
			}
			else
			{
				m_PeriodType = 2;
				pointType = 2;
			}
		}
		
		remainTime = hour;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x31);
		writeD(character.getPcBangScore());
		writeD(m_AddPoint);
		writeC(m_PeriodType);
		writeD(remainTime);
		writeC(pointType);
	}
	
	@Override
	public String getType()
	{
		return "[S] FE:31 ExPCCafePointInfo";
	}
}
