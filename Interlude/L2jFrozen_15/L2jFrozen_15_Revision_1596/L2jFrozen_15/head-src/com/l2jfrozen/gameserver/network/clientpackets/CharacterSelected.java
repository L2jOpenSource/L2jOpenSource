package com.l2jfrozen.gameserver.network.clientpackets;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.crypt.nProtect;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.L2GameClient.GameClientState;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.CharSelected;

@SuppressWarnings("unused")
public class CharacterSelected extends L2GameClientPacket
{
	private static Logger LOGGER = Logger.getLogger(CharacterSelected.class);
	private int charSlot;
	private int unk1, unk2, unk3, unk4; // new in C4
	
	@Override
	protected void readImpl()
	{
		charSlot = readD();
		unk1 = readH();
		unk2 = readD();
		unk3 = readD();
		unk4 = readD();
	}
	
	@Override
	protected void runImpl()
	{
		// if there is a playback.dat file in the current directory, it will be sent to the client instead of any regular packets
		// to make this work, the first packet in the playback.dat has to be a [S]0x21 packet
		// after playback is done, the client will not work correct and need to exit
		// playLogFile(getConnection()); // try to play LOGGER file
		
		if (!getClient().getFloodProtectors().getCharacterSelect().tryPerformAction("CharacterSelect"))
		{
			return;
		}
		
		// we should always be abble to acquire the lock but if we cant lock then nothing should be done (ie repeated packet)
		if (getClient().getActiveCharLock().tryLock())
		{
			try
			{
				// should always be null but if not then this is repeated packet and nothing should be done here
				if (getClient().getActiveChar() == null)
				{
					// The L2PcInstance must be created here, so that it can be attached to the L2GameClient
					if (Config.DEBUG)
					{
						LOGGER.debug("DEBUG " + getType() + ": selected slot:" + charSlot);
					}
					
					// Load up character from disk
					final L2PcInstance cha = getClient().loadCharFromDisk(charSlot);
					
					if (cha == null)
					{
						LOGGER.warn(getType() + ": Character could not be loaded (slot:" + charSlot + ")");
						sendPacket(ActionFailed.STATIC_PACKET);
						return;
					}
					
					if (cha.getAccessLevel().getLevel() < 0)
					{
						cha.deleteMe();
						return;
					}
					
					cha.setClient(getClient());
					getClient().setActiveChar(cha);
					nProtect.getInstance().sendRequest(getClient());
					getClient().setState(GameClientState.IN_GAME);
					sendPacket(new CharSelected(cha, getClient().getSessionId().playOkID1));
				}
			}
			catch (final Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				getClient().getActiveCharLock().unlock();
			}
		}
	}
	
	@Override
	public String getType()
	{
		return "[C] 0D CharacterSelected";
	}
}