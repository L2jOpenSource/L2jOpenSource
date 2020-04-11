package com.l2jfrozen.gameserver.network.serverpackets;

/**
 * Send Private (Friend) Message Format: c dSSS d: Unknown S: Sending Player S: Receiving Player S: Message
 * @author Tempy
 */
public class FriendRecvMsg extends L2GameServerPacket
{
	
	private final String sender, receiver, message;
	
	public FriendRecvMsg(final String sender, final String reciever, final String message)
	{
		this.sender = sender;
		receiver = reciever;
		this.message = message;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xfd);
		
		writeD(0); // ??
		writeS(receiver);
		writeS(sender);
		writeS(message);
	}
	
	@Override
	public String getType()
	{
		return "[S] FD FriendRecvMsg";
	}
}
