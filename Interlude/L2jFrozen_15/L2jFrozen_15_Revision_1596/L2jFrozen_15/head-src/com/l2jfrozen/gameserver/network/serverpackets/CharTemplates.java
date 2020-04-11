package com.l2jfrozen.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

import com.l2jfrozen.gameserver.templates.L2PcTemplate;

/**
 * This class ...
 * @version $Revision: 1.3.2.1.2.7 $ $Date: 2005/03/27 15:29:39 $
 */
public class CharTemplates extends L2GameServerPacket
{
	private final List<L2PcTemplate> chars = new ArrayList<>();
	
	public void addChar(final L2PcTemplate template)
	{
		chars.add(template);
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x17);
		writeD(chars.size());
		
		for (final L2PcTemplate temp : chars)
		{
			writeD(temp.race.ordinal());
			writeD(temp.classId.getId());
			writeD(0x46);
			writeD(temp.baseSTR);
			writeD(0x0a);
			writeD(0x46);
			writeD(temp.baseDEX);
			writeD(0x0a);
			writeD(0x46);
			writeD(temp.baseCON);
			writeD(0x0a);
			writeD(0x46);
			writeD(temp.baseINT);
			writeD(0x0a);
			writeD(0x46);
			writeD(temp.baseWIT);
			writeD(0x0a);
			writeD(0x46);
			writeD(temp.baseMEN);
			writeD(0x0a);
		}
	}
	
	@Override
	public String getType()
	{
		return "[S] 23 CharTemplates";
	}
}
