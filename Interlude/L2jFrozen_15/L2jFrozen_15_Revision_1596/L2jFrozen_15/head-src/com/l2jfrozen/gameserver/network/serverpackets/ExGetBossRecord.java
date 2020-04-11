package com.l2jfrozen.gameserver.network.serverpackets;

import java.util.Map;

/**
 * Format: ch ddd [ddd].
 * @author KenM
 */
public class ExGetBossRecord extends L2GameServerPacket
{
	private final Map<Integer, Integer> bossRecordInfo;
	private final int ranking;
	private final int totalPoints;
	
	/**
	 * Instantiates a new ex get boss record.
	 * @param ranking    the ranking
	 * @param totalScore the total score
	 * @param list       the list
	 */
	public ExGetBossRecord(final int ranking, final int totalScore, final Map<Integer, Integer> list)
	{
		this.ranking = ranking;
		totalPoints = totalScore;
		bossRecordInfo = list;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x33);
		writeD(ranking);
		writeD(totalPoints);
		if (bossRecordInfo == null)
		{
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
		}
		else
		{
			writeD(bossRecordInfo.size());
			for (final int bossId : bossRecordInfo.keySet())
			{
				writeD(bossId);
				writeD(bossRecordInfo.get(bossId));
				writeD(0x00); // ??
			}
		}
	}
	
	/**
	 * Gets the type.
	 * @return the type
	 */
	@Override
	public String getType()
	{
		return "[S] FE:33 ExGetBossRecord";
	}
}
