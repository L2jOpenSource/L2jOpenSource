package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.cache.CrestCache;

/**
 * <code>
 * sample
 * 0000: c7 6d 06 00 00 36 05 00 00 42 4d 36 05 00 00 00    .m...6...BM6....
 * 0010: 00 00 00 36 04 00 00 28 00 00 00 10 00 00 00 10    ...6...(........
 * 0020: 00 00 00 01 00 08 00 00 00 00 00 00 01 00 00 c4    ................
 * 0030: ...
 * 0530: 10 91 00 00 00 60 9b d1 01 e4 6e ee 52 97 dd       .....`....n.R..
 * </code> format dd x...x
 * @version $Revision: 1.3.2.1.2.4 $ $Date: 2005/03/27 15:29:39 $
 */
public class AllyCrest extends L2GameServerPacket
{
	private final int allyCrestId;
	private final byte[] data;
	
	public AllyCrest(final int crestId)
	{
		allyCrestId = crestId;
		data = CrestCache.getInstance().getAllyCrest(allyCrestId);
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xae);
		writeD(allyCrestId);
		if (data != null)
		{
			writeD(data.length);
			writeB(data);
		}
		else
		{
			writeD(0);
		}
	}
	
	@Override
	public String getType()
	{
		return "[S] ae AllyCrest";
	}
}
