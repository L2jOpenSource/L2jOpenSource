package com.l2jfrozen.gameserver.network;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.crypt.nProtect;
import com.l2jfrozen.gameserver.communitybbs.Manager.RegionBBSManager;
import com.l2jfrozen.gameserver.datatables.OfflineTradeTable;
import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.datatables.sql.ClanTable;
import com.l2jfrozen.gameserver.managers.AwayManager;
import com.l2jfrozen.gameserver.model.CharSelectInfoPackage;
import com.l2jfrozen.gameserver.model.L2Clan;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.entity.event.CTF;
import com.l2jfrozen.gameserver.model.entity.event.DM;
import com.l2jfrozen.gameserver.model.entity.event.L2Event;
import com.l2jfrozen.gameserver.model.entity.event.TvT;
import com.l2jfrozen.gameserver.model.entity.olympiad.Olympiad;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.L2GameServerPacket;
import com.l2jfrozen.gameserver.network.serverpackets.LeaveWorld;
import com.l2jfrozen.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jfrozen.gameserver.network.serverpackets.ServerClose;
import com.l2jfrozen.gameserver.thread.LoginServerThread;
import com.l2jfrozen.gameserver.thread.LoginServerThread.SessionKey;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;
import com.l2jfrozen.gameserver.util.EventData;
import com.l2jfrozen.gameserver.util.FloodProtectors;
import com.l2jfrozen.logs.Log;
import com.l2jfrozen.netcore.MMOClient;
import com.l2jfrozen.netcore.MMOConnection;
import com.l2jfrozen.netcore.NetcoreConfig;
import com.l2jfrozen.netcore.ReceivablePacket;
import com.l2jfrozen.util.OlympiadLogger;
import com.l2jfrozen.util.database.L2DatabaseFactory;

import main.data.memory.ObjectData;
import main.holders.objects.PlayerHolder;

/**
 * @author L2JFrozen dev
 */
public final class L2GameClient extends MMOClient<MMOConnection<L2GameClient>> implements Runnable
{
	protected static final Logger LOGGER = Logger.getLogger(L2GameClient.class);
	
	private static final String UPDATE_CHARACTER_DELETE_TIME_TO_ZERO = "UPDATE characters SET deletetime=0 WHERE obj_id=?";
	private static final String SELECT_CHARACTER_CLAN_ID_BY_OBJECT_ID = "SELECT clanId from characters WHERE obj_Id=?";
	private static final String UPDATE_CHAR_DELETE_TIME = "UPDATE characters SET deletetime=? WHERE obj_Id=?";
	
	/**
	 * CONNECTED - client has just connected AUTHED - client has authed but doesn't has character attached to it yet IN_GAME - client has selected a char and is in game
	 * @author KenM
	 */
	public static enum GameClientState
	{
		CONNECTED,
		AUTHED,
		IN_GAME
	}
	
	// floodprotectors
	private final FloodProtectors floodProtectors = new FloodProtectors(this);
	
	public GameClientState state;
	
	// Info
	public String accountName;
	public SessionKey sessionId;
	public L2PcInstance activeChar;
	private final ReentrantLock activeCharLock = new ReentrantLock();
	
	private boolean isAuthedGG;
	private final long connectionStartTime;
	private final List<Integer> charSlotMapping = new ArrayList<>();
	
	// Task
	private ScheduledFuture<?> guardCheckTask = null;
	
	protected ScheduledFuture<?> cleanupTask = null;
	
	private final ClientStats stats;
	
	// Crypt
	public GameCrypt crypt;
	
	// Flood protection
	public long packetsNextSendTick = 0;
	
	// unknownPacket protection
	private int unknownPacketCount = 0;
	
	protected boolean closenow = true;
	private boolean isDetached = false;
	
	protected boolean forcedToClose = false;
	
	private final ArrayBlockingQueue<ReceivablePacket<L2GameClient>> packetQueue;
	private final ReentrantLock queueLock = new ReentrantLock();
	
	private long last_received_packet_action_time = 0;
	
	public L2GameClient(final MMOConnection<L2GameClient> con)
	{
		super(con);
		state = GameClientState.CONNECTED;
		connectionStartTime = System.currentTimeMillis();
		crypt = new GameCrypt();
		stats = new ClientStats();
		packetQueue = new ArrayBlockingQueue<>(NetcoreConfig.getInstance().CLIENT_PACKET_QUEUE_SIZE);
		
		guardCheckTask = nProtect.getInstance().startTask(this);
		ThreadPoolManager.getInstance().scheduleGeneral(() ->
		{
			if (closenow)
			{
				close(new LeaveWorld());
			}
		}, 4000);
		
	}
	
	public byte[] enableCrypt()
	{
		final byte[] key = BlowFishKeygen.getRandomKey();
		GameCrypt.setKey(key, crypt);
		return key;
	}
	
	public GameClientState getState()
	{
		return state;
	}
	
	public void setState(final GameClientState pState)
	{
		if (state != pState)
		{
			state = pState;
			packetQueue.clear();
		}
	}
	
	public ClientStats getStats()
	{
		return stats;
	}
	
	public long getConnectionStartTime()
	{
		return connectionStartTime;
	}
	
	@Override
	public boolean decrypt(final ByteBuffer buf, final int size)
	{
		closenow = false;
		GameCrypt.decrypt(buf.array(), buf.position(), size, crypt);
		return true;
	}
	
	@Override
	public boolean encrypt(final ByteBuffer buf, final int size)
	{
		GameCrypt.encrypt(buf.array(), buf.position(), size, crypt);
		buf.position(buf.position() + size);
		return true;
	}
	
	public L2PcInstance getActiveChar()
	{
		return activeChar;
	}
	
	public void setActiveChar(final L2PcInstance pActiveChar)
	{
		activeChar = pActiveChar;
		if (activeChar != null)
		{
			L2World.getInstance().storeObject(getActiveChar());
		}
	}
	
	public ReentrantLock getActiveCharLock()
	{
		return activeCharLock;
	}
	
	public boolean isAuthedGG()
	{
		return isAuthedGG;
	}
	
	public void setGameGuardOk(final boolean val)
	{
		isAuthedGG = val;
	}
	
	public void setAccountName(final String pAccountName)
	{
		accountName = pAccountName;
	}
	
	public String getAccountName()
	{
		return accountName;
	}
	
	public void setSessionId(final SessionKey sk)
	{
		sessionId = sk;
	}
	
	public SessionKey getSessionId()
	{
		return sessionId;
	}
	
	public void sendPacket(final L2GameServerPacket gsp)
	{
		if (isDetached)
		{
			return;
		}
		
		if (getConnection() != null)
		{
			
			if (Config.DEBUG_PACKETS)
			{
				Log.add("[ServerPacket] SendingGameServerPacket, Client: " + toString() + " Packet:" + gsp.getType(), "GameServerPacketsLog");
			}
			
			if (gsp instanceof NpcHtmlMessage)
			{
				NpcHtmlMessage npcDialog = (NpcHtmlMessage) gsp;
				npcDialog.processHtml(this);
			}
			
			getConnection().sendPacket(gsp);
			gsp.runImpl();
		}
	}
	
	public boolean isDetached()
	{
		return isDetached;
	}
	
	public void setDetached(final boolean b)
	{
		isDetached = b;
	}
	
	/**
	 * Method to handle character deletion
	 * @param  charslot
	 * @return          a byte:
	 *                  <li>-1: Error: No char was found for such charslot, caught exception, etc...
	 *                  <li>0: character is not member of any clan, proceed with deletion
	 *                  <li>1: character is member of a clan, but not clan leader
	 *                  <li>2: character is clan leader
	 */
	public byte markToDeleteChar(final int charslot)
	{
		byte answer = -1;
		int playerObjId = getObjectIdForSlot(charslot);
		
		if (playerObjId < 0)
		{
			return answer;
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SELECT_CHARACTER_CLAN_ID_BY_OBJECT_ID))
		{
			statement.setInt(1, playerObjId);
			
			int clanId = 0;
			
			try (ResultSet rs = statement.executeQuery())
			{
				if (rs.next())
				{
					clanId = rs.getInt(1);
				}
			}
			
			L2Clan clan = ClanTable.getInstance().getClan(clanId);
			
			if (clan == null)
			{
				answer = 0; // Can delete
			}
			else if (clan.getLeaderId() == playerObjId)
			{
				answer = 2; // Clan leaders may not be deleted. Dissolve the clan first and try again.
			}
			else
			{
				answer = 1; // You may not delete a clan member. Withdraw from the clan first and try again.
			}
			
			// Setting delete time
			if (answer == 0)
			{
				if (Config.DELETE_DAYS == 0)
				{
					deleteCharByObjId(playerObjId);
				}
				else
				{
					try (PreparedStatement updateStatement = con.prepareStatement(UPDATE_CHAR_DELETE_TIME))
					{
						updateStatement.setLong(1, System.currentTimeMillis() + Config.DELETE_DAYS * 86400000L); // 24*60*60*1000 = 86400000
						updateStatement.setInt(2, playerObjId);
						updateStatement.executeUpdate();
					}
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.error("L2GameClient.markToDeleteChar : Data error on update delete time of char", e);
			answer = -1;
		}
		
		return answer;
	}
	
	public void markRestoredChar(final int charslot)
	{
		final int objid = getObjectIdForSlot(charslot);
		
		if (objid < 0)
		{
			return;
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(UPDATE_CHARACTER_DELETE_TIME_TO_ZERO))
		{
			statement.setInt(1, objid);
			statement.executeUpdate();
		}
		catch (Exception e)
		{
			LOGGER.error("L2GameClient.markRestoredChar : Could not update delete time to zero", e);
		}
	}
	
	public static void deleteCharByObjId(int playerObjId)
	{
		if (playerObjId < 0)
		{
			return;
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			try (PreparedStatement statement = con.prepareStatement("DELETE FROM character_friends WHERE char_id=? OR friend_id=?"))
			{
				statement.setInt(1, playerObjId);
				statement.setInt(2, playerObjId);
				statement.executeUpdate();
			}
			
			try (PreparedStatement statement = con.prepareStatement("DELETE FROM character_hennas WHERE char_obj_id=?"))
			{
				statement.setInt(1, playerObjId);
				statement.executeUpdate();
			}
			
			try (PreparedStatement statement = con.prepareStatement("DELETE FROM character_macroses WHERE char_obj_id=?"))
			{
				statement.setInt(1, playerObjId);
				statement.executeUpdate();
			}
			
			try (PreparedStatement statement = con.prepareStatement("DELETE FROM character_quests WHERE char_id=?"))
			{
				statement.setInt(1, playerObjId);
				statement.executeUpdate();
			}
			
			try (PreparedStatement statement = con.prepareStatement("DELETE FROM character_recipebook WHERE char_id=?"))
			{
				statement.setInt(1, playerObjId);
				statement.executeUpdate();
			}
			
			try (PreparedStatement statement = con.prepareStatement("DELETE FROM character_shortcuts WHERE char_obj_id=?"))
			{
				statement.setInt(1, playerObjId);
				statement.executeUpdate();
			}
			
			try (PreparedStatement statement = con.prepareStatement("DELETE FROM character_skills WHERE char_obj_id=?"))
			{
				statement.setInt(1, playerObjId);
				statement.executeUpdate();
			}
			
			try (PreparedStatement statement = con.prepareStatement("DELETE FROM character_skills_save WHERE char_obj_id=?"))
			{
				statement.setInt(1, playerObjId);
				statement.executeUpdate();
			}
			
			try (PreparedStatement statement = con.prepareStatement("DELETE FROM character_subclasses WHERE char_obj_id=?"))
			{
				statement.setInt(1, playerObjId);
				statement.executeUpdate();
			}
			
			try (PreparedStatement statement = con.prepareStatement("DELETE FROM heroes WHERE charId=?"))
			{
				statement.setInt(1, playerObjId);
				statement.executeUpdate();
			}
			
			try (PreparedStatement statement = con.prepareStatement("DELETE FROM olympiad_nobles WHERE charId=?"))
			{
				statement.setInt(1, playerObjId);
				statement.executeUpdate();
			}
			
			try (PreparedStatement statement = con.prepareStatement("DELETE FROM seven_signs WHERE char_obj_id=?"))
			{
				statement.setInt(1, playerObjId);
				statement.executeUpdate();
			}
			
			try (PreparedStatement statement = con.prepareStatement("DELETE FROM pets WHERE item_obj_id IN (SELECT object_id FROM items WHERE items.owner_id=?)"))
			{
				statement.setInt(1, playerObjId);
				statement.executeUpdate();
			}
			
			try (PreparedStatement statement = con.prepareStatement("DELETE FROM augmentations WHERE item_object_id IN (SELECT object_id FROM items WHERE items.owner_id=?)"))
			{
				statement.setInt(1, playerObjId);
				statement.executeUpdate();
			}
			
			try (PreparedStatement statement = con.prepareStatement("DELETE FROM items WHERE owner_id=?"))
			{
				statement.setInt(1, playerObjId);
				statement.executeUpdate();
			}
			
			try (PreparedStatement statement = con.prepareStatement("DELETE FROM merchant_lease WHERE player_id=?"))
			{
				statement.setInt(1, playerObjId);
				statement.executeUpdate();
			}
			
			try (PreparedStatement statement = con.prepareStatement("DELETE FROM characters WHERE obj_Id=?"))
			{
				statement.setInt(1, playerObjId);
				statement.executeUpdate();
			}
		}
		catch (Exception e)
		{
			LOGGER.error("L2GameClient.deleteCharByObjId : Data error on deleting char", e);
		}
	}
	
	public L2PcInstance loadCharFromDisk(final int charslot)
	{
		// L2PcInstance character = L2PcInstance.load(getObjectIdForSlot(charslot));
		
		final int objId = getObjectIdForSlot(charslot);
		if (objId < 0)
		{
			return null;
		}
		
		L2PcInstance character = L2World.getInstance().getPlayer(objId);
		if (character != null)
		{
			// exploit prevention, should not happens in normal way
			
			LOGGER.warn("Attempt of double login: " + character.getName() + "(" + objId + ") " + getAccountName());
			
			if (character.getClient() != null)
			{
				character.getClient().closeNow();
			}
			else
			{
				character.deleteMe();
				
				try
				{
					character.store();
				}
				catch (final Exception e2)
				{
					LOGGER.error("fixme:unhandled exception", e2);
				}
				
			}
			
			// return null;
		}
		
		character = L2PcInstance.load(objId);
		// if(character != null)
		// {
		// //restoreInventory(character);
		// //restoreSkills(character);
		// //character.restoreSkills();
		// //restoreShortCuts(character);
		// //restoreWarehouse(character);
		//
		// // preinit some values for each login
		// character.setRunning(); // running is default
		// character.standUp(); // standing is default
		//
		// character.refreshOverloaded();
		// character.refreshExpertisePenalty();
		// character.refreshMasteryPenality();
		// character.refreshMasteryWeapPenality();
		//
		// character.sendPacket(new UserInfo(character));
		// character.broadcastKarma();
		// character.setOnlineStatus(true);
		// }
		// if(character == null)
		// {
		// LOGGER.severe("could not restore in slot: " + charslot);
		// }
		
		// setCharacter(character);
		return character;
	}
	
	/**
	 * @param chars
	 */
	public void setCharSelection(final CharSelectInfoPackage[] chars)
	{
		charSlotMapping.clear();
		
		for (final CharSelectInfoPackage c : chars)
		{
			final int objectId = c.getObjectId();
			
			charSlotMapping.add(objectId);
		}
	}
	
	public void close(final L2GameServerPacket gsp)
	{
		if (getConnection() != null)
		{
			getConnection().close(gsp);
		}
		
	}
	
	/**
	 * @param  charslot
	 * @return
	 */
	private int getObjectIdForSlot(final int charslot)
	{
		if (charslot < 0 || charslot >= charSlotMapping.size())
		{
			LOGGER.warn(toString() + " tried to delete Character in slot " + charslot + " but no characters exits at that slot.");
			return -1;
		}
		
		final Integer objectId = charSlotMapping.get(charslot);
		
		return objectId.intValue();
	}
	
	@Override
	public void onForcedDisconnection(final boolean critical)
	{
		forcedToClose = true;
		
		if (critical && Config.ENABLE_ALL_EXCEPTIONS)
		{
			final String text = "Client " + toString() + " disconnected abnormally.";
			Log.add(text, "Chars_disconnection_logs");
		}
		
		// the force operation will allow to not save client position to prevent again criticals
		// and stuck
		closeNow();
	}
	
	public void stopGuardTask()
	{
		if (guardCheckTask != null)
		{
			guardCheckTask.cancel(true);
			guardCheckTask = null;
		}
		
	}
	
	@Override
	public void onDisconnection()
	{
		// no long running tasks here, do it async
		try
		{
			ThreadPoolManager.getInstance().executeTask(new DisconnectTask());
			
		}
		catch (final RejectedExecutionException e)
		{
			// server is closing
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Close client connection with {@link ServerClose} packet
	 */
	public void closeNow()
	{
		close(0);
	}
	
	/**
	 * Close client connection with {@link ServerClose} packet
	 * @param delay
	 */
	public void close(final int delay)
	{
		
		close(ServerClose.STATIC_PACKET);
		synchronized (this)
		{
			if (cleanupTask != null)
			{
				cancelCleanup();
			}
			cleanupTask = ThreadPoolManager.getInstance().scheduleGeneral(new CleanupTask(), delay); // delayed
		}
		stopGuardTask();
		nProtect.getInstance().closeSession(this);
	}
	
	/**
	 * Produces the best possible string representation of this client.
	 */
	@Override
	public String toString()
	{
		try
		{
			InetAddress address = getConnection().getInetAddress();
			String ip = "N/A";
			
			if (address == null)
			{
				ip = "disconnected";
			}
			else
			{
				ip = address.getHostAddress();
			}
			
			switch (getState())
			{
				case CONNECTED:
					return "[IP: " + ip + "]";
				case AUTHED:
					return "[Account: " + getAccountName() + " - IP: " + ip + "]";
				case IN_GAME:
					address = null;
					return "[Character: " + (getActiveChar() == null ? "disconnected" : getActiveChar().getName()) + " - Account: " + getAccountName() + " - IP: " + ip + "]";
				default:
					address = null;
					throw new IllegalStateException("Missing state on switch");
			}
		}
		catch (final NullPointerException e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			return "[Character read failed due to disconnect]";
		}
	}
	
	protected class CleanupTask implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				// Update BBS
				try
				{
					RegionBBSManager.getInstance().changeCommunityBoard();
				}
				catch (final Exception e)
				{
					e.printStackTrace();
				}
				
				// // we are going to manually save the char below thus we can force the cancel
				// if (_autoSaveInDB != null)
				// autoSaveInDB.cancel(true);
				//
				
				final L2PcInstance player = getActiveChar();
				if (player != null) // this should only happen on connection loss
				{
					if (Config.ENABLE_OLYMPIAD_DISCONNECTION_DEBUG)
					{
						if (player.isInOlympiadMode() || player.inObserverMode())
						{
							if (player.isInOlympiadMode())
							{
								final String text = "Player " + player.getName() + ", Class:" + player.getClassId() + ", Level:" + player.getLevel() + ", Mode: Olympiad, Loc: " + player.getX() + " Y:" + player.getY() + " Z:" + player.getZ() + ", Critical?: " + forcedToClose;
								OlympiadLogger.add(text, "Olympiad_crash_debug");
							}
							else if (player.inObserverMode())
							{
								final String text = "Player " + player.getName() + ", Class:" + player.getClassId() + ", Level:" + player.getLevel() + ", Mode: Observer, Loc: " + player.getX() + " Y:" + player.getY() + " Z:" + player.getZ() + ", Critical?: " + forcedToClose;
								OlympiadLogger.add(text, "Olympiad_crash_debug");
							}
							else
							{
								final String text = "Player " + player.getName() + ", Class:" + player.getClassId() + ", Level:" + player.getLevel() + ", Mode: Default, Loc: " + player.getX() + " Y:" + player.getY() + " Z:" + player.getZ() + ", Critical?: " + forcedToClose;
								OlympiadLogger.add(text, "Olympiad_crash_debug");
							}
						}
					}
					
					// we store all data from players who are disconnected while in an event in order to restore it in the next login
					if (player.atEvent)
					{
						EventData data = new EventData(player.eventX, player.eventY, player.eventZ, player.eventKarma, player.eventPvpKills, player.eventPkKills, player.eventTitle, player.kills, player.eventSitForced);
						
						L2Event.connectionLossData.put(player.getName(), data);
						data = null;
					}
					else
					{
						
						if (player.inEventCTF)
						{
							CTF.onDisconnect(player);
						}
						else if (player.inEventDM)
						{
							DM.onDisconnect(player);
						}
						else if (player.inEventTvT)
						{
							TvT.onDisconnect(player);
						}
					}
					
					if (player.isFlying())
					{
						player.removeSkill(SkillTable.getInstance().getInfo(4289, 1));
					}
					
					if (player.isAway())
					{
						AwayManager.getInstance().extraBack(player);
					}
					
					if (player.isInOlympiadMode())
					{
						Olympiad.getInstance().unRegisterNoble(player);
					}
					
					// Decrease boxes number
					if (player.activeBoxesCount != -1)
					{
						player.decreaseBoxes();
					}
					
					// prevent closing again
					player.setClient(null);
					
					player.deleteMe();
					
					try
					{
						player.store(forcedToClose);
					}
					catch (final Exception e2)
					{
						if (Config.ENABLE_ALL_EXCEPTIONS)
						{
							e2.printStackTrace();
						}
					}
					
				}
				
				setActiveChar(null);
				setDetached(true);
			}
			catch (final Exception e1)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					e1.printStackTrace();
				}
				
				LOGGER.warn("Error while cleanup client.", e1);
			}
			finally
			{
				LoginServerThread.getInstance().sendLogout(getAccountName());
			}
		}
	}
	
	protected class DisconnectTask implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				// Update BBS
				try
				{
					RegionBBSManager.getInstance().changeCommunityBoard();
				}
				catch (final Exception e)
				{
					e.printStackTrace();
				}
				
				// // we are going to manually save the char bellow thus we can force the cancel
				// if(_autoSaveInDB != null)
				// autoSaveInDB.cancel(true);
				
				L2PcInstance player = getActiveChar();
				if (player != null) // this should only happen on connection loss
				{
					// Olympiad crash DEBUG
					if (Config.ENABLE_OLYMPIAD_DISCONNECTION_DEBUG)
					{
						if (player.isInOlympiadMode()
							
							|| player.inObserverMode())
						{
							if (player.isInOlympiadMode())
							{
								final String text = "Player " + player.getName() + ", Class:" + player.getClassId() + ", Level:" + player.getLevel() + ", Mode: Olympiad, Loc: " + player.getX() + " Y:" + player.getY() + " Z:" + player.getZ() + ", Critical?: " + forcedToClose;
								Log.add(text, "Olympiad_crash_debug");
							}
							else if (player.inObserverMode())
							{
								final String text = "Player " + player.getName() + ", Class:" + player.getClassId() + ", Level:" + player.getLevel() + ", Mode: Observer, Loc: " + player.getX() + " Y:" + player.getY() + " Z:" + player.getZ() + ", Critical?: " + forcedToClose;
								Log.add(text, "Olympiad_crash_debug");
							}
							else
							{
								final String text = "Player " + player.getName() + ", Class:" + player.getClassId() + ", Level:" + player.getLevel() + ", Mode: Default, Loc: " + player.getX() + " Y:" + player.getY() + " Z:" + player.getZ() + ", Critical?: " + forcedToClose;
								Log.add(text, "Olympiad_crash_debug");
							}
						}
					}
					
					// we store all data from players who are disconnected while in an event in order to restore it in the next login
					if (player.atEvent)
					{
						EventData data = new EventData(player.eventX, player.eventY, player.eventZ, player.eventKarma, player.eventPvpKills, player.eventPkKills, player.eventTitle, player.kills, player.eventSitForced);
						
						L2Event.connectionLossData.put(player.getName(), data);
						data = null;
					}
					else
					{
						
						if (player.inEventCTF)
						{
							CTF.onDisconnect(player);
						}
						else if (player.inEventDM)
						{
							DM.onDisconnect(player);
						}
						else if (player.inEventTvT)
						{
							TvT.onDisconnect(player);
						}
					}
					
					if (player.isFlying())
					{
						player.removeSkill(SkillTable.getInstance().getInfo(4289, 1));
					}
					
					if (player.isAway())
					{
						AwayManager.getInstance().extraBack(player);
					}
					
					if (player.isInOlympiadMode())
					{
						Olympiad.getInstance().unRegisterNoble(player);
					}
					
					// Decrease boxes number
					if (player.activeBoxesCount != -1)
					{
						player.decreaseBoxes();
					}
					
					if (!player.isKicked() && !player.isInOlympiadMode() && !player.isInFunEvent() && (player.isInStoreMode() && Config.OFFLINE_TRADE_ENABLE || player.isInCraftMode() && Config.OFFLINE_CRAFT_ENABLE))
					{
						player.setOfflineMode(true);
						player.setOnline(false);
						player.leaveParty();
						player.store();
						
						if (Config.OFFLINE_SET_NAME_COLOR)
						{
							player.originalNameColorOffline = player.getAppearance().getNameColor();
							player.getAppearance().setNameColor(Config.OFFLINE_NAME_COLOR);
							player.broadcastUserInfo();
						}
						
						if (player.getOfflineStartTime() == 0)
						{
							player.setOfflineStartTime(System.currentTimeMillis());
						}
						
						OfflineTradeTable.storeOffliner(player);
						
						return;
					}
					
					if (ObjectData.get(PlayerHolder.class, getActiveChar()).isOffline())
					{
						return;
					}
					
					// notify the world about our disconnect
					player.deleteMe();
					
					// store operation
					try
					{
						player.store();
					}
					catch (final Exception e2)
					{
						if (Config.ENABLE_ALL_EXCEPTIONS)
						{
							e2.printStackTrace();
						}
					}
					
				}
				
				setActiveChar(null);
				setDetached(true);
				player = null;
			}
			catch (final Exception e1)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					e1.printStackTrace();
				}
				
				LOGGER.warn("error while disconnecting client", e1);
			}
			finally
			{
				LoginServerThread.getInstance().sendLogout(getAccountName());
			}
		}
	}
	
	public FloodProtectors getFloodProtectors()
	{
		return floodProtectors;
	}
	
	public boolean checkUnknownPackets()
	{
		
		final L2PcInstance player = getActiveChar();
		
		if (player != null && floodProtectors != null && floodProtectors.getUnknownPackets() != null && !floodProtectors.getUnknownPackets().tryPerformAction("check packet"))
		{
			unknownPacketCount++;
			
			if (unknownPacketCount >= Config.MAX_UNKNOWN_PACKETS)
			{
				return true;
			}
			return false;
		}
		unknownPacketCount = 0;
		return false;
	}
	
	private boolean cancelCleanup()
	{
		final Future<?> task = cleanupTask;
		if (task != null)
		{
			cleanupTask = null;
			return task.cancel(true);
		}
		return false;
	}
	
	/**
	 * Returns false if client can receive packets. True if detached, or flood detected, or queue overflow detected and queue still not empty.
	 * @return
	 */
	public boolean dropPacket()
	{
		if (NetcoreConfig.ENABLE_CLIENT_FLOOD_PROTECTION)
		{
			// detached clients can't receive any packets
			if (isDetached)
			{
				return true;
			}
			
			// Ignore flood protector for GM char
			if (getActiveChar() != null && getActiveChar().isGM())
			{
				return false;
			}
			
			// flood protection
			if (getStats().countPacket(packetQueue.size()))
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				return true;
			}
			
			return getStats().dropPacket();
		}
		if (isDetached)
		{
			return true;
		}
		return false;
	}
	
	/**
	 * Counts buffer underflow exceptions.
	 */
	public void onBufferUnderflow()
	{
		if (getStats().countUnderflowException())
		{
			LOGGER.warn("Client " + toString() + " - Disconnected: Too many buffer underflow exceptions.");
			closeNow();
			return;
		}
		if (state == GameClientState.CONNECTED) // in CONNECTED state kick client immediately
		{
			LOGGER.warn("Client " + toString() + " - Disconnected, too many buffer underflows in non-authed state.");
			closeNow();
		}
	}
	
	/**
	 * Add packet to the queue and start worker thread if needed
	 * @param packet
	 */
	public void execute(final ReceivablePacket<L2GameClient> packet)
	{
		if (getStats().countFloods())
		{
			LOGGER.warn("Client " + toString() + " - Disconnected, too many floods:" + getStats().longFloods + " long and " + getStats().shortFloods + " short.");
			closeNow();
			return;
		}
		
		if (!packetQueue.offer(packet))
		{
			if (getStats().countQueueOverflow())
			{
				LOGGER.warn("Client " + toString() + " - Disconnected, too many queue overflows.");
				closeNow();
			}
			else
			{
				sendPacket(ActionFailed.STATIC_PACKET);
			}
			
			return;
		}
		
		if (queueLock.isLocked())
		{
			return;
		}
		
		// save last action time
		last_received_packet_action_time = System.currentTimeMillis();
		// LOGGER.severe("Client " + toString() + " - updated last action state "+_last_received_packet_action_time);
		
		try
		{
			if (state == GameClientState.CONNECTED)
			{
				if (getStats().processedPackets > 3)
				{
					LOGGER.warn("Client " + toString() + " - Disconnected, too many packets in non-authed state.");
					closeNow();
					return;
				}
				
				ThreadPoolManager.getInstance().executeIOPacket(this);
			}
			else
			{
				ThreadPoolManager.getInstance().executePacket(this);
			}
		}
		catch (final RejectedExecutionException e)
		{
			LOGGER.error("fixme:unhandled exception", e);
			// if the server is shutdown we ignore
			if (!ThreadPoolManager.getInstance().isShutdown())
			{
				LOGGER.warn("Failed executing: " + packet.getClass().getSimpleName() + " for Client: " + toString());
			}
		}
	}
	
	@Override
	public void run()
	{
		if (!queueLock.tryLock())
		{
			return;
		}
		
		try
		{
			int count = 0;
			while (true)
			{
				final ReceivablePacket<L2GameClient> packet = packetQueue.poll();
				if (packet == null)
				{
					return;
				}
				
				if (isDetached) // clear queue immediately after detach
				{
					packetQueue.clear();
					return;
				}
				
				try
				{
					packet.run();
				}
				catch (final Exception e)
				{
					LOGGER.error("Exception during execution " + packet.getClass().getSimpleName() + ", client: " + toString() + "," + e.getMessage(), e);
				}
				
				count++;
				if (getStats().countBurst(count))
				{
					return;
				}
			}
		}
		finally
		{
			queueLock.unlock();
		}
	}
	
	/**
	 * @return the forcedToClose
	 */
	public boolean is_forcedToClose()
	{
		return forcedToClose;
	}
	
	public boolean isConnectionAlive()
	{
		// if last received packet time is higher then Config.CHECK_CONNECTION_INACTIVITY_TIME --> check connection
		if (System.currentTimeMillis() - last_received_packet_action_time > Config.CHECK_CONNECTION_INACTIVITY_TIME)
		{
			
			last_received_packet_action_time = System.currentTimeMillis();
			
			return getConnection().isConnected() && !getConnection().isClosed();
			
		}
		
		return true;
	}
	
}
