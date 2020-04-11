package com.l2jfrozen.gameserver.handler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2SiegeGuardInstance;
import com.l2jfrozen.gameserver.model.entity.sevensigns.SevenSigns;
import com.l2jfrozen.gameserver.model.spawn.L2Spawn;
import com.l2jfrozen.gameserver.model.spawn.SpawnListener;
import com.l2jfrozen.gameserver.network.serverpackets.CreatureSay;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;
import com.l2jfrozen.util.CloseUtil;
import com.l2jfrozen.util.database.DatabaseUtils;
import com.l2jfrozen.util.database.L2DatabaseFactory;
import com.l2jfrozen.util.random.Rnd;

/**
 * Auto Chat Handler Allows NPCs to automatically send messages to nearby players at a set time interval.
 * @author Tempy
 */
public class AutoChatHandler implements SpawnListener
{
	protected static final Logger LOGGER = Logger.getLogger(AutoChatHandler.class);
	private static AutoChatHandler instance;
	
	private static final long DEFAULT_CHAT_DELAY = 30000; // 30 secs by default
	
	protected Map<Integer, AutoChatInstance> registeredChats;
	
	protected AutoChatHandler()
	{
		registeredChats = new HashMap<>();
		restoreChatData();
		L2Spawn.addSpawnListener(this);
	}
	
	private void restoreChatData()
	{
		int numLoaded = 0;
		
		Connection con = null;
		
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT * FROM auto_chat ORDER BY groupId ASC");
			final ResultSet rs = statement.executeQuery();
			
			while (rs.next())
			{
				numLoaded++;
				
				PreparedStatement statement2 = con.prepareStatement("SELECT * FROM auto_chat_text WHERE groupId=?");
				statement2.setInt(1, rs.getInt("groupId"));
				ResultSet rs2 = statement2.executeQuery();
				
				rs2.last();
				final String[] chatTexts = new String[rs2.getRow()];
				
				int i = 0;
				
				rs2.first();
				
				while (rs2.next())
				{
					chatTexts[i] = rs2.getString("chatText");
					i++;
				}
				
				registerGlobalChat(rs.getInt("npcId"), chatTexts, rs.getLong("chatDelay"));
				
				statement2.close();
				rs2.close();
				statement2 = null;
				rs2 = null;
			}
			
			rs.close();
			DatabaseUtils.close(statement);
			statement = null;
			
			if (Config.DEBUG)
			{
				LOGGER.debug("AutoChatHandler: Loaded " + numLoaded + " chat group(s) from the database.");
			}
		}
		catch (final Exception e)
		{
			LOGGER.warn("AutoSpawnHandler: Could not restore chat data: " + e);
		}
		finally
		{
			CloseUtil.close(con);
			con = null;
		}
	}
	
	public static AutoChatHandler getInstance()
	{
		if (instance == null)
		{
			instance = new AutoChatHandler();
		}
		
		return instance;
	}
	
	public int size()
	{
		return registeredChats.size();
	}
	
	/**
	 * Registers a globally active auto chat for ALL instances of the given NPC ID. <BR>
	 * Returns the associated auto chat instance.
	 * @param  npcId
	 * @param  chatTexts
	 * @param  chatDelay (-1 = default delay)
	 * @return           AutoChatInstance chatInst
	 */
	public AutoChatInstance registerGlobalChat(final int npcId, final String[] chatTexts, final long chatDelay)
	{
		return registerChat(npcId, null, chatTexts, chatDelay);
	}
	
	/**
	 * Registers a NON globally-active auto chat for the given NPC instance, and adds to the currently assigned chat instance for this NPC ID, otherwise creates a new instance if a previous one is not found. <BR>
	 * Returns the associated auto chat instance.
	 * @param  npcInst
	 * @param  chatTexts
	 * @param  chatDelay (-1 = default delay)
	 * @return           AutoChatInstance chatInst
	 */
	public AutoChatInstance registerChat(final L2NpcInstance npcInst, final String[] chatTexts, final long chatDelay)
	{
		return registerChat(npcInst.getNpcId(), npcInst, chatTexts, chatDelay);
	}
	
	private final AutoChatInstance registerChat(final int npcId, final L2NpcInstance npcInst, final String[] chatTexts, long chatDelay)
	{
		AutoChatInstance chatInst = null;
		
		if (chatDelay < 0)
		{
			chatDelay = DEFAULT_CHAT_DELAY;
		}
		
		if (registeredChats.containsKey(npcId))
		{
			chatInst = registeredChats.get(npcId);
		}
		else
		{
			chatInst = new AutoChatInstance(npcId, chatTexts, chatDelay, (npcInst == null));
		}
		
		if (npcInst != null)
		{
			chatInst.addChatDefinition(npcInst);
		}
		
		registeredChats.put(npcId, chatInst);
		
		return chatInst;
	}
	
	/**
	 * Removes and cancels ALL auto chat definition for the given NPC ID, and removes its chat instance if it exists.
	 * @param  npcId
	 * @return       boolean removedSuccessfully
	 */
	public boolean removeChat(final int npcId)
	{
		final AutoChatInstance chatInst = registeredChats.get(npcId);
		
		return removeChat(chatInst);
	}
	
	/**
	 * Removes and cancels ALL auto chats for the given chat instance.
	 * @param  chatInst
	 * @return          removedSuccessfully
	 */
	public boolean removeChat(final AutoChatInstance chatInst)
	{
		if (chatInst == null)
		{
			return false;
		}
		
		registeredChats.remove(chatInst.getNPCId());
		chatInst.setActive(false);
		
		if (Config.DEBUG)
		{
			LOGGER.debug("AutoChatHandler: Removed auto chat for NPC ID " + chatInst.getNPCId());
		}
		
		return true;
	}
	
	/**
	 * Returns the associated auto chat instance either by the given NPC ID or object ID.
	 * @param  id
	 * @param  byObjectId
	 * @return            chatInst
	 */
	public AutoChatInstance getAutoChatInstance(final int id, final boolean byObjectId)
	{
		if (!byObjectId)
		{
			return registeredChats.get(id);
		}
		
		for (final AutoChatInstance chatInst : registeredChats.values())
		{
			if (chatInst.getChatDefinition(id) != null)
			{
				return chatInst;
			}
		}
		
		return null;
	}
	
	/**
	 * Sets the active state of all auto chat instances to that specified, and cancels the scheduled chat task if necessary.
	 * @param isActive
	 */
	public void setAutoChatActive(final boolean isActive)
	{
		for (final AutoChatInstance chatInst : registeredChats.values())
		{
			chatInst.setActive(isActive);
		}
	}
	
	/**
	 * Used in conjunction with a SpawnListener, this method is called every time an NPC is spawned in the world. <BR>
	 * <BR>
	 * If an auto chat instance is set to be "global", all instances matching the registered NPC ID will be added to that chat instance.
	 */
	@Override
	public void npcSpawned(final L2NpcInstance npc)
	{
		synchronized (registeredChats)
		{
			if (npc == null)
			{
				return;
			}
			
			final int npcId = npc.getNpcId();
			
			if (registeredChats.containsKey(npcId))
			{
				AutoChatInstance chatInst = registeredChats.get(npcId);
				
				if (chatInst != null && chatInst.isGlobal())
				{
					chatInst.addChatDefinition(npc);
				}
				
				chatInst = null;
			}
		}
	}
	
	/**
	 * Auto Chat Instance <BR>
	 * <BR>
	 * Manages the auto chat instances for a specific registered NPC ID.
	 * @author Tempy
	 */
	public class AutoChatInstance
	{
		protected int npcId;
		private long defaultDelay = DEFAULT_CHAT_DELAY;
		private String[] defaultTexts;
		private boolean defaultRandom = false;
		
		private boolean globalChat = false;
		private boolean isActive;
		
		private final Map<Integer, AutoChatDefinition> chatDefinitions = new HashMap<>();
		protected ScheduledFuture<?> chatTask;
		
		protected AutoChatInstance(final int npcId, final String[] chatTexts, final long chatDelay, final boolean isGlobal)
		{
			defaultTexts = chatTexts;
			this.npcId = npcId;
			defaultDelay = chatDelay;
			globalChat = isGlobal;
			
			if (Config.DEBUG)
			{
				LOGGER.debug("AutoChatHandler: Registered auto chat for NPC ID " + npcId + " (Global Chat = " + globalChat + ").");
			}
			
			setActive(true);
		}
		
		protected AutoChatDefinition getChatDefinition(final int objectId)
		{
			return chatDefinitions.get(objectId);
		}
		
		protected AutoChatDefinition[] getChatDefinitions()
		{
			final Collection<AutoChatDefinition> values = chatDefinitions.values();
			
			return values.toArray(new AutoChatDefinition[values.size()]);
		}
		
		/**
		 * Defines an auto chat for an instance matching this auto chat instance's registered NPC ID, and launches the scheduled chat task. <BR>
		 * Returns the object ID for the NPC instance, with which to refer to the created chat definition. <BR>
		 * <B>Note</B>: Uses pre-defined default values for texts and chat delays from the chat instance.
		 * @param  npcInst
		 * @return         objectId
		 */
		public int addChatDefinition(final L2NpcInstance npcInst)
		{
			return addChatDefinition(npcInst, null, 0);
		}
		
		/**
		 * Defines an auto chat for an instance matching this auto chat instance's registered NPC ID, and launches the scheduled chat task. <BR>
		 * Returns the object ID for the NPC instance, with which to refer to the created chat definition.
		 * @param  npcInst
		 * @param  chatTexts
		 * @param  chatDelay
		 * @return           objectId
		 */
		public int addChatDefinition(final L2NpcInstance npcInst, final String[] chatTexts, final long chatDelay)
		{
			final int objectId = npcInst.getObjectId();
			
			AutoChatDefinition chatDef = new AutoChatDefinition(this, npcInst, chatTexts, chatDelay);
			
			if (npcInst instanceof L2SiegeGuardInstance)
			{
				chatDef.setRandomChat(true);
			}
			
			chatDefinitions.put(objectId, chatDef);
			
			chatDef = null;
			
			return objectId;
		}
		
		/**
		 * Removes a chat definition specified by the given object ID.
		 * @param  objectId
		 * @return          removedSuccessfully
		 */
		public boolean removeChatDefinition(final int objectId)
		{
			if (!chatDefinitions.containsKey(objectId))
			{
				return false;
			}
			
			AutoChatDefinition chatDefinition = chatDefinitions.get(objectId);
			chatDefinition.setActive(false);
			
			chatDefinitions.remove(objectId);
			
			chatDefinition = null;
			
			return true;
		}
		
		/**
		 * Tests if this auto chat instance is active.
		 * @return boolean isActive
		 */
		public boolean isActive()
		{
			return isActive;
		}
		
		/**
		 * Tests if this auto chat instance applies to ALL currently spawned instances of the registered NPC ID.
		 * @return boolean isGlobal
		 */
		public boolean isGlobal()
		{
			return globalChat;
		}
		
		/**
		 * Tests if random order is the DEFAULT for new chat definitions.
		 * @return boolean isRandom
		 */
		public boolean isDefaultRandom()
		{
			return defaultRandom;
		}
		
		/**
		 * Tests if the auto chat definition given by its object ID is set to be random.
		 * @param  objectId
		 * @return          isRandom
		 */
		public boolean isRandomChat(final int objectId)
		{
			if (!chatDefinitions.containsKey(objectId))
			{
				return false;
			}
			
			return chatDefinitions.get(objectId).isRandomChat();
		}
		
		/**
		 * Returns the ID of the NPC type managed by this auto chat instance.
		 * @return int npcId
		 */
		public int getNPCId()
		{
			return npcId;
		}
		
		/**
		 * Returns the number of auto chat definitions stored for this instance.
		 * @return int definitionCount
		 */
		public int getDefinitionCount()
		{
			return chatDefinitions.size();
		}
		
		/**
		 * Returns a list of all NPC instances handled by this auto chat instance.
		 * @return L2NpcInstance[] npcInsts
		 */
		public L2NpcInstance[] getNPCInstanceList()
		{
			final List<L2NpcInstance> npcInsts = new ArrayList<>();
			
			for (final AutoChatDefinition chatDefinition : chatDefinitions.values())
			{
				npcInsts.add(chatDefinition.npcInstance);
			}
			
			return npcInsts.toArray(new L2NpcInstance[npcInsts.size()]);
		}
		
		/**
		 * A series of methods used to get and set default values for new chat definitions.
		 * @return
		 */
		public long getDefaultDelay()
		{
			return defaultDelay;
		}
		
		public String[] getDefaultTexts()
		{
			return defaultTexts;
		}
		
		public void setDefaultChatDelay(final long delayValue)
		{
			defaultDelay = delayValue;
		}
		
		public void setDefaultChatTexts(final String[] textsValue)
		{
			defaultTexts = textsValue;
		}
		
		public void setDefaultRandom(final boolean randValue)
		{
			defaultRandom = randValue;
		}
		
		/**
		 * Sets a specific chat delay for the specified auto chat definition given by its object ID.
		 * @param objectId
		 * @param delayValue
		 */
		public void setChatDelay(final int objectId, final long delayValue)
		{
			AutoChatDefinition chatDef = getChatDefinition(objectId);
			
			if (chatDef != null)
			{
				chatDef.setChatDelay(delayValue);
			}
			
			chatDef = null;
		}
		
		/**
		 * Sets a specific set of chat texts for the specified auto chat definition given by its object ID.
		 * @param objectId
		 * @param textsValue
		 */
		public void setChatTexts(final int objectId, final String[] textsValue)
		{
			AutoChatDefinition chatDef = getChatDefinition(objectId);
			
			if (chatDef != null)
			{
				chatDef.setChatTexts(textsValue);
			}
			
			chatDef = null;
		}
		
		/**
		 * Sets specifically to use random chat order for the auto chat definition given by its object ID.
		 * @param objectId
		 * @param randValue
		 */
		public void setRandomChat(final int objectId, final boolean randValue)
		{
			AutoChatDefinition chatDef = getChatDefinition(objectId);
			
			if (chatDef != null)
			{
				chatDef.setRandomChat(randValue);
			}
			
			chatDef = null;
		}
		
		/**
		 * Sets the activity of ALL auto chat definitions handled by this chat instance.
		 * @param activeValue
		 */
		public void setActive(final boolean activeValue)
		{
			if (isActive == activeValue)
			{
				return;
			}
			
			isActive = activeValue;
			
			if (!isGlobal())
			{
				for (final AutoChatDefinition chatDefinition : chatDefinitions.values())
				{
					chatDefinition.setActive(activeValue);
				}
				
				return;
			}
			
			if (isActive())
			{
				AutoChatRunner acr = new AutoChatRunner(npcId, -1);
				chatTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(acr, defaultDelay, defaultDelay);
				acr = null;
			}
			else
			{
				chatTask.cancel(false);
			}
		}
		
		/**
		 * Auto Chat Definition <BR>
		 * <BR>
		 * Stores information about specific chat data for an instance of the NPC ID specified by the containing auto chat instance. <BR>
		 * Each NPC instance of this type should be stored in a subsequent AutoChatDefinition class.
		 * @author Tempy
		 */
		private class AutoChatDefinition
		{
			protected int chatIndex = 0;
			protected L2NpcInstance npcInstance;
			
			protected AutoChatInstance chatInstance;
			
			private long chatDelay = 0;
			private String[] chatTexts = null;
			private boolean isActiveDefinition;
			private boolean randomChat;
			
			protected AutoChatDefinition(final AutoChatInstance chatInst, final L2NpcInstance npcInst, final String[] chatTexts, final long chatDelay)
			{
				npcInstance = npcInst;
				
				chatInstance = chatInst;
				randomChat = chatInst.isDefaultRandom();
				
				this.chatDelay = chatDelay;
				this.chatTexts = chatTexts;
				
				if (Config.DEBUG)
				{
					LOGGER.info("AutoChatHandler: Chat definition added for NPC ID " + npcInstance.getNpcId() + " (Object ID = " + npcInstance.getObjectId() + ").");
				}
				
				// If global chat isn't enabled for the parent instance,
				// then handle the chat task locally.
				if (!chatInst.isGlobal())
				{
					setActive(true);
				}
			}
			
			/*
			 * protected AutoChatDefinition(AutoChatInstance chatInst, L2NpcInstance npcInst) { this(chatInst, npcInst, null, -1); }
			 */
			
			protected String[] getChatTexts()
			{
				if (chatTexts != null)
				{
					return chatTexts;
				}
				return chatInstance.getDefaultTexts();
			}
			
			private long getChatDelay()
			{
				if (chatDelay > 0)
				{
					return chatDelay;
				}
				return chatInstance.getDefaultDelay();
			}
			
			private boolean isActive()
			{
				return isActiveDefinition;
			}
			
			boolean isRandomChat()
			{
				return randomChat;
			}
			
			void setRandomChat(final boolean randValue)
			{
				randomChat = randValue;
			}
			
			void setChatDelay(final long delayValue)
			{
				chatDelay = delayValue;
			}
			
			void setChatTexts(final String[] textsValue)
			{
				chatTexts = textsValue;
			}
			
			void setActive(final boolean activeValue)
			{
				if (isActive() == activeValue)
				{
					return;
				}
				
				if (activeValue)
				{
					AutoChatRunner acr = new AutoChatRunner(npcId, npcInstance.getObjectId());
					
					if (getChatDelay() == 0)
					{
						// Schedule it set to 5Ms, isn't error, if use 0 sometine
						// chatDefinition return null in AutoChatRunner
						chatTask = ThreadPoolManager.getInstance().scheduleGeneral(acr, 5);
					}
					else
					{
						chatTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(acr, getChatDelay(), getChatDelay());
					}
					
					acr = null;
				}
				else
				{
					chatTask.cancel(false);
				}
				
				isActiveDefinition = activeValue;
			}
		}
		
		/**
		 * Auto Chat Runner <BR>
		 * <BR>
		 * Represents the auto chat scheduled task for each chat instance.
		 * @author Tempy
		 */
		private class AutoChatRunner implements Runnable
		{
			private final int runnerNpcId;
			private final int objectId;
			
			protected AutoChatRunner(final int pNpcId, final int pObjectId)
			{
				runnerNpcId = pNpcId;
				objectId = pObjectId;
			}
			
			@Override
			public synchronized void run()
			{
				AutoChatInstance chatInst = registeredChats.get(runnerNpcId);
				AutoChatDefinition[] chatDefinitions;
				
				if (chatInst.isGlobal())
				{
					chatDefinitions = chatInst.getChatDefinitions();
				}
				else
				{
					AutoChatDefinition chatDef = chatInst.getChatDefinition(objectId);
					
					if (chatDef == null)
					{
						LOGGER.warn("AutoChatHandler: Auto chat definition is NULL for NPC ID " + npcId + ".");
						return;
					}
					
					chatDefinitions = new AutoChatDefinition[]
					{
						chatDef
					};
					chatDef = null;
				}
				
				if (Config.DEBUG)
				{
					LOGGER.info("AutoChatHandler: Running auto chat for " + chatDefinitions.length + " instances of NPC ID " + npcId + "." + " (Global Chat = " + chatInst.isGlobal() + ")");
				}
				
				for (final AutoChatDefinition chatDef : chatDefinitions)
				{
					try
					{
						L2NpcInstance chatNpc = chatDef.npcInstance;
						List<L2PcInstance> nearbyPlayers = new ArrayList<>();
						List<L2PcInstance> nearbyGMs = new ArrayList<>();
						
						for (final L2Character player : chatNpc.getKnownList().getKnownCharactersInRadius(1500))
						{
							if (!(player instanceof L2PcInstance))
							{
								continue;
							}
							
							if (((L2PcInstance) player).isGM())
							{
								nearbyGMs.add((L2PcInstance) player);
							}
							else
							{
								nearbyPlayers.add((L2PcInstance) player);
							}
						}
						
						final int maxIndex = chatDef.getChatTexts().length;
						int lastIndex = Rnd.nextInt(maxIndex);
						
						String creatureName = chatNpc.getName();
						String text;
						
						if (!chatDef.isRandomChat())
						{
							lastIndex = chatDef.chatIndex;
							lastIndex++;
							
							if (lastIndex == maxIndex)
							{
								lastIndex = 0;
							}
							
							chatDef.chatIndex = lastIndex;
						}
						
						text = chatDef.getChatTexts()[lastIndex];
						
						if (text == null)
						{
							return;
						}
						
						if (!nearbyPlayers.isEmpty())
						{
							final int randomPlayerIndex = Rnd.nextInt(nearbyPlayers.size());
							
							L2PcInstance randomPlayer = nearbyPlayers.get(randomPlayerIndex);
							
							final int winningCabal = SevenSigns.getInstance().getCabalHighestScore();
							int losingCabal = SevenSigns.CABAL_NULL;
							
							if (winningCabal == SevenSigns.CABAL_DAWN)
							{
								losingCabal = SevenSigns.CABAL_DUSK;
							}
							else if (winningCabal == SevenSigns.CABAL_DUSK)
							{
								losingCabal = SevenSigns.CABAL_DAWN;
							}
							
							if (text.indexOf("%player_random%") > -1)
							{
								text = text.replaceAll("%player_random%", randomPlayer.getName());
							}
							
							if (text.indexOf("%player_cabal_winner%") > -1)
							{
								for (final L2PcInstance nearbyPlayer : nearbyPlayers)
								{
									if (SevenSigns.getInstance().getPlayerCabal(nearbyPlayer) == winningCabal)
									{
										text = text.replaceAll("%player_cabal_winner%", nearbyPlayer.getName());
										break;
									}
								}
							}
							
							if (text.indexOf("%player_cabal_loser%") > -1)
							{
								for (final L2PcInstance nearbyPlayer : nearbyPlayers)
								{
									if (SevenSigns.getInstance().getPlayerCabal(nearbyPlayer) == losingCabal)
									{
										text = text.replaceAll("%player_cabal_loser%", nearbyPlayer.getName());
										break;
									}
								}
							}
							
							randomPlayer = null;
						}
						
						if (text == null)
						{
							return;
						}
						
						if (text.contains("%player_cabal_loser%") || text.contains("%player_cabal_winner%") || text.contains("%player_random%"))
						{
							return;
						}
						
						CreatureSay cs = new CreatureSay(chatNpc.getObjectId(), 0, creatureName, text);
						
						for (final L2PcInstance nearbyPlayer : nearbyPlayers)
						{
							nearbyPlayer.sendPacket(cs);
						}
						
						for (final L2PcInstance nearbyGM : nearbyGMs)
						{
							nearbyGM.sendPacket(cs);
						}
						
						cs = null;
						
						if (Config.DEBUG)
						{
							LOGGER.debug("AutoChatHandler: Chat propogation for object ID " + chatNpc.getObjectId() + " (" + creatureName + ") with text '" + text + "' sent to " + nearbyPlayers.size() + " nearby players.");
						}
						
						text = null;
						creatureName = null;
						nearbyGMs = null;
						nearbyPlayers = null;
						chatNpc = null;
					}
					catch (final Exception e)
					{
						e.printStackTrace();
						return;
					}
					
					chatDefinitions = null;
					chatInst = null;
				}
			}
		}
	}
}
