package net.sf.l2j.gameserver.network.clientpackets;

public class SendTimeCheck extends L2GameClientPacket
{
	@SuppressWarnings("unused")
	private int _requestId;
	@SuppressWarnings("unused")
	private int _responseId;
	
	@Override
	protected void readImpl()
	{
		_requestId = readD();
		_responseId = readD();
	}
	
	@Override
	protected void runImpl()
	{
	}
}