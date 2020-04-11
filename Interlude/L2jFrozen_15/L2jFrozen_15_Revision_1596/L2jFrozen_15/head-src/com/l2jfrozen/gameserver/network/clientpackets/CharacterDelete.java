package com.l2jfrozen.gameserver.network.clientpackets;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.GameServer;
import com.l2jfrozen.gameserver.network.serverpackets.CharDeleteFail;
import com.l2jfrozen.gameserver.network.serverpackets.CharDeleteOk;
import com.l2jfrozen.gameserver.network.serverpackets.CharSelectInfo;

/**
 * @author eX1steam, l2jfrozen
 */
public final class CharacterDelete extends L2GameClientPacket
{
	private static Logger LOGGER = Logger.getLogger(CharacterDelete.class);
	private int charSlot;
	
	@Override
	protected void readImpl()
	{
		charSlot = readD();
	}
	
	@Override
	protected void runImpl()
	{
		
		if (!getClient().getFloodProtectors().getCharacterSelect().tryPerformAction("CharacterDelete"))
		{
			return;
		}
		
		if (Config.DEBUG)
		{
			LOGGER.debug("DEBUG " + getType() + ": deleting slot:" + charSlot);
		}
		
		try
		{
			final byte answer = getClient().markToDeleteChar(charSlot);
			switch (answer)
			{
				default:
				case -1: // Error
					break;
				case 0: // Success!
					sendPacket(new CharDeleteOk());
					break;
				case 1:
					sendPacket(new CharDeleteFail(CharDeleteFail.REASON_YOU_MAY_NOT_DELETE_CLAN_MEMBER));
					break;
				case 2:
					sendPacket(new CharDeleteFail(CharDeleteFail.REASON_CLAN_LEADERS_MAY_NOT_BE_DELETED));
					break;
			}
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			LOGGER.error("ERROR " + getType() + ":", e);
		}
		
		// Before the char selection, check shutdown status
		if (GameServer.getSelectorThread().isShutdown())
		{
			getClient().closeNow();
			return;
		}
		
		final CharSelectInfo cl = new CharSelectInfo(getClient().getAccountName(), getClient().getSessionId().playOkID1, 0);
		sendPacket(cl);
		getClient().setCharSelection(cl.getCharInfo());
	}
	
	@Override
	public String getType()
	{
		return "[C] 0C CharacterDelete";
	}
}
