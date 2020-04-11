package com.l2jfrozen.gameserver.network.serverpackets;

/**
 * sample format d
 * @version $Revision: 1.1.2.1.2.3 $ $Date: 2005/03/27 15:29:39 $
 */
public class ShowCalculator extends L2GameServerPacket
{
	private final int calculatorId;
	
	/**
	 * @param calculatorId
	 */
	public ShowCalculator(final int calculatorId)
	{
		this.calculatorId = calculatorId;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xdc);
		writeD(calculatorId);
	}
	
	@Override
	public String getType()
	{
		return "[S] dc ShowCalculator";
	}
}
