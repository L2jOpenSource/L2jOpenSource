package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

/**
 * This class ...
 * @version $Revision: 1.4.2.1.2.3 $ $Date: 2005/03/27 15:29:57 $
 */
public class CreatureSay extends L2GameServerPacket
{
	private final int objectId;
	private final int textType;
	private final String charName;
	private final String text;
	
	/**
	 * @param objectId
	 * @param messageType
	 * @param charName
	 * @param text
	 */
	public CreatureSay(final int objectId, final int messageType, final String charName, final String text)
	{
		this.objectId = objectId;
		textType = messageType;
		this.charName = charName;
		this.text = text;
		// setLifeTime(0);
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x4a);
		writeD(objectId);
		writeD(textType);
		writeS(charName);
		writeS(text);
		
		final L2PcInstance pci = getClient().getActiveChar();
		if (pci != null)
		{
			pci.broadcastSnoop(textType, charName, text, this);
		}
	}
	
	@Override
	public String getType()
	{
		return "[S] 4A CreatureSay";
	}
}