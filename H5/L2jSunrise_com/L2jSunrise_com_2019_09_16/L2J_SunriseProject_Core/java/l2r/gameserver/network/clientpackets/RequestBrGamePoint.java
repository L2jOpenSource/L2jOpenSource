package l2r.gameserver.network.clientpackets;

import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.serverpackets.ExBrGamePoint;

/**
 * Created by GodFather
 */
public class RequestBrGamePoint extends L2GameClientPacket
{
	private static final String TYPE = "[C] D0:65 RequestBrGamePoint";
	
	@Override
	protected void readImpl()
	{
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		
		if (player == null)
		{
			return;
		}
		
		player.sendPacket(new ExBrGamePoint(player));
	}
	
	@Override
	public String getType()
	{
		return TYPE;
	}
}