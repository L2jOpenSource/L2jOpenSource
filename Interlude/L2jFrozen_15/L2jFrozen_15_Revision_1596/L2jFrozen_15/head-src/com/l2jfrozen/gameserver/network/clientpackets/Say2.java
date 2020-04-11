package com.l2jfrozen.gameserver.network.clientpackets;

import java.nio.BufferUnderflowException;
import java.util.Collection;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.csv.MapRegionTable;
import com.l2jfrozen.gameserver.handler.IVoicedCommandHandler;
import com.l2jfrozen.gameserver.handler.VoicedCommandHandler;
import com.l2jfrozen.gameserver.managers.PetitionManager;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance.PunishLevel;
import com.l2jfrozen.gameserver.network.SystemChatChannelId;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.CreatureSay;
import com.l2jfrozen.gameserver.network.serverpackets.SocialAction;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.util.Util;

import main.EngineModsManager;

public final class Say2 extends L2GameClientPacket
{
	private static Logger LOGGER = Logger.getLogger(Say2.class);
	private static java.util.logging.Logger logChat = java.util.logging.Logger.getLogger("chat");
	
	public final static int ALL = 0; // Normal chat - White
	public final static int SHOUT = 1; // ! - Orange
	public final static int TELL = 2; // " - Whisper - Purple
	public final static int PARTY = 3; // # - Green
	public final static int CLAN = 4; // @ - Dark Blue
	public final static int GM = 5; // //gmchat - White
	public final static int PETITION_PLAYER = 6; // used for petition - White
	public final static int PETITION_GM = 7; // * used for petition - Aqua green
	public final static int TRADE = 8; // + - Light Pink
	public final static int ALLIANCE = 9; // $ - Light Green
	public final static int ANNOUNCEMENT = 10; // //announce - Light Blue
	public static final int BOAT = 11; // Will crash client
	public static final int L2FRIEND = 12; // Will crash client
	public static final int MSNCHAT = 13; // Nothing to show
	public static final int PARTYMATCH_ROOM = 14; // Nothing to show
	public final static int PARTYROOM_COMMANDER = 15; // Light Red
	public final static int PARTYROOM_ALL = 16; // Light Yellow
	public final static int HERO_VOICE = 17; // % Light Blue
	public final static int CRITICAL_ANNOUNCE = 18; // Aqua green
	
	private final static String[] CHAT_NAMES =
	{
		"ALL",
		"SHOUT",
		"TELL",
		"PARTY",
		"CLAN",
		"GM",
		"PETITION_PLAYER",
		"PETITION_GM",
		"TRADE",
		"ALLIANCE",
		"ANNOUNCEMENT",
		"BOAT",
		"WILLCRASHCLIENT:)",
		"FAKEALL?",
		"PARTYMATCH_ROOM",
		"PARTYROOM_COMMANDER",
		"PARTYROOM_ALL",
		"HERO_VOICE",
		"CRITICAL_ANNOUNCEMENT"
	};
	
	private String text;
	private int type;
	private SystemChatChannelId type2Check;
	private String target;
	
	@Override
	protected void readImpl()
	{
		text = readS();
		try
		{
			type = readD();
			type2Check = SystemChatChannelId.getChatType(type);
			
		}
		catch (final BufferUnderflowException e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			type = CHAT_NAMES.length;
			type2Check = SystemChatChannelId.CHAT_NONE;
		}
		target = type == TELL ? readS() : null;
	}
	
	@Override
	protected void runImpl()
	{
		if (Config.DEBUG)
		{
			LOGGER.info("Say2: Msg Type = '" + type + "' Text = '" + text + "'.");
		}
		
		if (type < 0 || type >= CHAT_NAMES.length)
		{
			LOGGER.warn("Say2: Invalid type: " + type);
			return;
		}
		
		final L2PcInstance activeChar = getClient().getActiveChar();
		
		// Anti-PHX Announce
		if (type2Check == SystemChatChannelId.CHAT_NONE || type2Check == SystemChatChannelId.CHAT_ANNOUNCE || type2Check == SystemChatChannelId.CHAT_CRITICAL_ANNOUNCE || type2Check == SystemChatChannelId.CHAT_SYSTEM || type2Check == SystemChatChannelId.CHAT_CUSTOM || type2Check == SystemChatChannelId.CHAT_GM_PET && !activeChar.isGM())
		{
			LOGGER.warn("[Anti-PHX Announce] Illegal Chat ( " + type2Check + " ) channel was used by character: [" + activeChar.getName() + "]");
			return;
		}
		
		if (activeChar == null)
		{
			LOGGER.warn("[Say2.java] Active Character is null.");
			return;
		}
		
		if (activeChar.isChatBanned() && !activeChar.isGM() && type != CLAN && type != ALLIANCE && type != PARTY)
		{
			activeChar.sendMessage("You may not chat while a chat ban is in effect.");
			return;
		}
		
		if (activeChar.isInJail() && Config.JAIL_DISABLE_CHAT)
		{
			if (type == TELL || type == SHOUT || type == TRADE || type == HERO_VOICE)
			{
				activeChar.sendMessage("You can not chat with players outside of the jail.");
				return;
			}
		}
		
		if (!getClient().getFloodProtectors().getSayAction().tryPerformAction("Say2"))
		{
			activeChar.sendMessage("You cannot speak too fast.");
			return;
		}
		
		if (activeChar.isCursedWeaponEquiped() && (type == TRADE || type == SHOUT))
		{
			activeChar.sendMessage("Shout and trade chatting cannot be used while possessing a cursed weapon.");
			return;
		}
		
		if (type == PETITION_PLAYER && activeChar.isGM())
		{
			type = PETITION_GM;
		}
		
		if (text.length() > Config.MAX_CHAT_LENGTH)
		{
			LOGGER.warn("Say2: Msg Type = '" + type + "' Text length more than " + Config.MAX_CHAT_LENGTH + " truncate them.");
			
			text = text.substring(0, Config.MAX_CHAT_LENGTH);
		}
		
		if (Config.LOG_CHAT)
		{
			final LogRecord record = new LogRecord(Level.INFO, text);
			record.setLoggerName("chat");
			
			if (type == TELL)
			{
				record.setParameters(new Object[]
				{
					CHAT_NAMES[type],
					"[" + activeChar.getName() + " to " + target + "]"
				});
			}
			else
			{
				record.setParameters(new Object[]
				{
					CHAT_NAMES[type],
					"[" + activeChar.getName() + "]"
				});
			}
			
			logChat.log(record);
		}
		
		if (Config.L2WALKER_PROTEC && type == TELL && checkBot(text))
		{
			Util.handleIllegalPlayerAction(activeChar, "Client Emulator Detect: Player " + activeChar.getName() + " using l2walker.", Config.DEFAULT_PUNISH);
			return;
		}
		text = text.replaceAll("\\\\n", "");
		
		// Say Filter implementation
		if (Config.USE_SAY_FILTER)
		{
			checkText(activeChar);
		}
		
		if (Config.ENABLE_SAY_SOCIAL_ACTIONS && !activeChar.isAlikeDead() && !activeChar.isDead())
		{
			if ((text.equalsIgnoreCase("hello") || text.equalsIgnoreCase("hey") || text.equalsIgnoreCase("aloha") || text.equalsIgnoreCase("alo") || text.equalsIgnoreCase("ciao") || text.equalsIgnoreCase("hi")) && (!activeChar.isRunning() || !activeChar.isAttackingNow() || !activeChar.isCastingNow() || !activeChar.isCastingPotionNow()))
			{
				activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), 2));
			}
			
			if ((text.equalsIgnoreCase("lol") || text.equalsIgnoreCase("haha") || text.equalsIgnoreCase("xaxa") || text.equalsIgnoreCase("ghgh") || text.equalsIgnoreCase("jaja")) && (!activeChar.isRunning() || !activeChar.isAttackingNow() || !activeChar.isCastingNow() || !activeChar.isCastingPotionNow()))
			{
				activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), 10));
			}
			
			if ((text.equalsIgnoreCase("yes") || text.equalsIgnoreCase("si") || text.equalsIgnoreCase("yep")) && (!activeChar.isRunning() || !activeChar.isAttackingNow() || !activeChar.isCastingNow() || !activeChar.isCastingPotionNow()))
			{
				activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), 6));
			}
			
			if ((text.equalsIgnoreCase("no") || text.equalsIgnoreCase("nop") || text.equalsIgnoreCase("nope")) && (!activeChar.isRunning() || !activeChar.isAttackingNow() || !activeChar.isCastingNow() || !activeChar.isCastingPotionNow()))
			{
				activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), 5));
			}
			
		}
		
		if (EngineModsManager.onVoiced(activeChar, text))
		{
			return;
		}
		
		L2Object saymode = activeChar.getSayMode();
		if (saymode != null)
		{
			String name = saymode.getName();
			int actor = saymode.getObjectId();
			type = 0;
			Collection<L2Object> list = saymode.getKnownList().getKnownObjects().values();
			
			CreatureSay cs = new CreatureSay(actor, type, name, text);
			for (L2Object obj : list)
			{
				if (obj == null || !(obj instanceof L2Character))
				{
					continue;
				}
				
				L2Character chara = (L2Character) obj;
				chara.sendPacket(cs);
			}
			return;
		}
		
		CreatureSay cs = new CreatureSay(activeChar.getObjectId(), type, activeChar.getName(), text);
		switch (type)
		{
			case TELL:
				L2PcInstance receiver = L2World.getInstance().getPlayer(target);
				
				if (receiver == null)
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.S1_IS_NOT_ONLINE);
					sm.addString(target);
					activeChar.sendPacket(sm);
					return;
				}
				
				if (!receiver.getBlockList().isInBlockList(activeChar.getName()) || activeChar.isGM())
				{
					if (receiver.isAway())
					{
						activeChar.sendMessage("Player is Away try again later.");
						return;
					}
					
					if (Config.JAIL_DISABLE_CHAT && receiver.isInJail())
					{
						activeChar.sendMessage("Player is in jail.");
						return;
					}
					
					if (receiver.isChatBanned() && !activeChar.isGM())
					{
						activeChar.sendMessage("Player is chat banned.");
						return;
					}
					
					if (receiver.isInOfflineMode())
					{
						activeChar.sendMessage("Player is in offline mode.");
						return;
					}
					
					if (!receiver.getMessageRefusal())
					{
						receiver.sendPacket(cs);
						activeChar.sendPacket(new CreatureSay(activeChar.getObjectId(), type, "->" + receiver.getName(), text));
					}
					else
					{
						activeChar.sendPacket(new SystemMessage(SystemMessageId.THE_PERSON_IS_IN_MESSAGE_REFUSAL_MODE));
					}
				}
				else if (receiver.getBlockList().isInBlockList(activeChar.getName()))
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.S1_HAS_ADDED_YOU_TO_IGNORE_LIST);
					sm.addString(target);
					activeChar.sendPacket(sm);
				}
				break;
			case SHOUT:
				
				// Flood protect Say
				if (!getClient().getFloodProtectors().getGlobalChat().tryPerformAction("global chat"))
				{
					return;
				}
				
				if (Config.DEFAULT_GLOBAL_CHAT.equalsIgnoreCase("on") || Config.DEFAULT_GLOBAL_CHAT.equalsIgnoreCase("gm") && activeChar.isGM())
				{
					if (Config.GLOBAL_CHAT_WITH_PVP)
					{
						if (activeChar.getPvpKills() < Config.GLOBAL_PVP_AMOUNT && !activeChar.isGM())
						{
							activeChar.sendMessage("You must have at least " + Config.GLOBAL_PVP_AMOUNT + " pvp kills in order to speak in global chat");
							return;
						}
						int region = MapRegionTable.getInstance().getMapRegion(activeChar.getX(), activeChar.getY());
						for (L2PcInstance player : L2World.getInstance().getAllPlayers())
						{
							if (region == MapRegionTable.getInstance().getMapRegion(player.getX(), player.getY()))
							{
								// Like L2OFF if player is blocked can't read the message
								if (!player.getBlockList().isInBlockList(activeChar.getName()))
								{
									player.sendPacket(cs);
								}
							}
						}
					}
					else
					{
						int region = MapRegionTable.getInstance().getMapRegion(activeChar.getX(), activeChar.getY());
						for (L2PcInstance player : L2World.getInstance().getAllPlayers())
						{
							if (region == MapRegionTable.getInstance().getMapRegion(player.getX(), player.getY()))
							{
								// Like L2OFF if player is blocked can't read the message
								if (!player.getBlockList().isInBlockList(activeChar.getName()))
								{
									player.sendPacket(cs);
								}
							}
						}
					}
				}
				else if (Config.DEFAULT_GLOBAL_CHAT.equalsIgnoreCase("GLOBAL"))
				{
					if (Config.GLOBAL_CHAT_WITH_PVP)
					{
						if (activeChar.getPvpKills() < Config.GLOBAL_PVP_AMOUNT && !activeChar.isGM())
						{
							activeChar.sendMessage("You must have at least " + Config.GLOBAL_PVP_AMOUNT + " pvp kills in order to speak in global chat");
							return;
						}
						for (L2PcInstance player : L2World.getInstance().getAllPlayers())
						{
							// Like L2OFF if player is blocked can't read the message
							if (!player.getBlockList().isInBlockList(activeChar.getName()))
							{
								player.sendPacket(cs);
							}
						}
					}
					else
					{
						for (L2PcInstance player : L2World.getInstance().getAllPlayers())
						{
							// Like L2OFF if player is blocked can't read the message
							if (!player.getBlockList().isInBlockList(activeChar.getName()))
							{
								player.sendPacket(cs);
							}
						}
					}
				}
				break;
			case TRADE:
				if (Config.DEFAULT_TRADE_CHAT.equalsIgnoreCase("ON"))
				{
					if (Config.TRADE_CHAT_WITH_PVP)
					{
						if (activeChar.getPvpKills() <= Config.TRADE_PVP_AMOUNT && !activeChar.isGM())
						{
							activeChar.sendMessage("You must have at least " + Config.TRADE_PVP_AMOUNT + "  pvp kills in order to speak in trade chat");
							return;
						}
						for (L2PcInstance player : L2World.getInstance().getAllPlayers())
						{
							// Like L2OFF if player is blocked can't read the message
							if (!player.getBlockList().isInBlockList(activeChar.getName()))
							{
								player.sendPacket(cs);
							}
						}
					}
					else
					{
						for (L2PcInstance player : L2World.getInstance().getAllPlayers())
						{
							// Like L2OFF if player is blocked can't read the message
							if (!player.getBlockList().isInBlockList(activeChar.getName()))
							{
								player.sendPacket(cs);
							}
						}
					}
				}
				else if (Config.DEFAULT_TRADE_CHAT.equalsIgnoreCase("limited"))
				{
					if (Config.TRADE_CHAT_WITH_PVP)
					{
						if (activeChar.getPvpKills() <= Config.TRADE_PVP_AMOUNT && !activeChar.isGM())
						{
							activeChar.sendMessage("You must have at least " + Config.TRADE_PVP_AMOUNT + "  pvp kills in order to speak in trade chat");
							return;
						}
						
						int region = MapRegionTable.getInstance().getMapRegion(activeChar.getX(), activeChar.getY());
						
						for (L2PcInstance player : L2World.getInstance().getAllPlayers())
						{
							if (region == MapRegionTable.getInstance().getMapRegion(player.getX(), player.getY()))
							{
								// Like L2OFF if player is blocked can't read the message
								if (!player.getBlockList().isInBlockList(activeChar.getName()))
								{
									player.sendPacket(cs);
								}
							}
						}
					}
					else if (Config.TRADE_CHAT_IS_NOOBLE)
					{
						if (!activeChar.isNoble() && !activeChar.isGM())
						{
							activeChar.sendMessage("Only Nobless Players Can Use This Chat");
							return;
						}
						
						int region = MapRegionTable.getInstance().getMapRegion(activeChar.getX(), activeChar.getY());
						for (L2PcInstance player : L2World.getInstance().getAllPlayers())
						{
							if (region == MapRegionTable.getInstance().getMapRegion(player.getX(), player.getY()))
							{
								// Like L2OFF if player is blocked can't read the message
								if (!player.getBlockList().isInBlockList(activeChar.getName()))
								{
									player.sendPacket(cs);
								}
							}
						}
						
					}
					else
					{
						int region = MapRegionTable.getInstance().getMapRegion(activeChar.getX(), activeChar.getY());
						for (L2PcInstance player : L2World.getInstance().getAllPlayers())
						{
							if (region == MapRegionTable.getInstance().getMapRegion(player.getX(), player.getY()))
							{
								// Like L2OFF if player is blocked can't read the message
								if (!player.getBlockList().isInBlockList(activeChar.getName()))
								{
									player.sendPacket(cs);
								}
							}
						}
					}
					
				}
				break;
			case ALL:
				if (text.startsWith("."))
				{
					StringTokenizer st = new StringTokenizer(text);
					IVoicedCommandHandler vch;
					String command = "";
					String target = "";
					
					if (st.countTokens() > 1)
					{
						command = st.nextToken().substring(1);
						target = text.substring(command.length() + 2);
						vch = VoicedCommandHandler.getInstance().getVoicedCommandHandler(command);
					}
					else
					{
						command = text.substring(1);
						
						if (Config.DEBUG)
						{
							LOGGER.info("Command: " + command);
						}
						
						vch = VoicedCommandHandler.getInstance().getVoicedCommandHandler(command);
					}
					
					if (vch != null)
					{
						vch.useVoicedCommand(command, activeChar, target);
						break;
					}
				}
				
				for (L2PcInstance player : activeChar.getKnownList().getKnownPlayers().values())
				{
					if (player != null && activeChar.isInsideRadius(player, 1250, false, true))
					{
						// Like L2OFF if player is blocked can't read the message
						if (!player.getBlockList().isInBlockList(activeChar.getName()))
						{
							player.sendPacket(cs);
						}
					}
				}
				activeChar.sendPacket(cs);
				
				break;
			case CLAN:
				if (activeChar.getClan() != null)
				{
					activeChar.getClan().broadcastToOnlineMembers(cs);
				}
				break;
			case ALLIANCE:
				if (activeChar.getClan() != null)
				{
					activeChar.getClan().broadcastToOnlineAllyMembers(cs);
				}
				break;
			case PARTY:
				if (activeChar.isInParty())
				{
					activeChar.getParty().broadcastToPartyMembers(cs);
				}
				break;
			case PETITION_PLAYER:
			case PETITION_GM:
				if (!PetitionManager.getInstance().isPlayerInConsultation(activeChar))
				{
					activeChar.sendPacket(new SystemMessage(SystemMessageId.YOU_ARE_NOT_IN_PETITION_CHAT));
					break;
				}
				
				PetitionManager.getInstance().sendActivePetitionMessage(activeChar, text);
				break;
			case PARTYROOM_ALL:
				if (activeChar.isInParty())
				{
					if (activeChar.getParty().isInCommandChannel() && activeChar.getParty().isLeader(activeChar))
					{
						activeChar.getParty().getCommandChannel().broadcastCSToChannelMembers(cs, activeChar);
					}
				}
				break;
			case PARTYROOM_COMMANDER:
				if (activeChar.isInParty())
				{
					if (activeChar.getParty().isInCommandChannel() && activeChar.getParty().getCommandChannel().getChannelLeader().equals(activeChar))
					{
						activeChar.getParty().getCommandChannel().broadcastCSToChannelMembers(cs, activeChar);
					}
				}
				break;
			case HERO_VOICE:
				if (activeChar.isGM())
				{
					for (L2PcInstance player : L2World.getInstance().getAllPlayers())
					{
						if (player == null)
						{
							continue;
						}
						
						player.sendPacket(cs);
					}
				}
				else if (activeChar.isHero())
				{
					// Flood protect Hero Voice
					if (!getClient().getFloodProtectors().getHeroVoice().tryPerformAction("hero voice"))
					{
						return;
					}
					
					for (L2PcInstance player : L2World.getInstance().getAllPlayers())
					{
						if (player == null)
						{
							continue;
						}
						
						// Like L2OFF if player is blocked can't read the message
						if (!player.getBlockList().isInBlockList(activeChar.getName()))
						{
							player.sendPacket(cs);
						}
					}
				}
				break;
		}
	}
	
	private static final String[] WALKER_COMMAND_LIST =
	{
		"USESKILL",
		"USEITEM",
		"BUYITEM",
		"SELLITEM",
		"SAVEITEM",
		"LOADITEM",
		"MSG",
		"SET",
		"DELAY",
		"LABEL",
		"JMP",
		"CALL",
		"RETURN",
		"MOVETO",
		"NPCSEL",
		"NPCDLG",
		"DLGSEL",
		"CHARSTATUS",
		"POSOUTRANGE",
		"POSINRANGE",
		"GOHOME",
		"SAY",
		"EXIT",
		"PAUSE",
		"STRINDLG",
		"STRNOTINDLG",
		"CHANGEWAITTYPE",
		"FORCEATTACK",
		"ISMEMBER",
		"REQUESTJOINPARTY",
		"REQUESTOUTPARTY",
		"QUITPARTY",
		"MEMBERSTATUS",
		"CHARBUFFS",
		"ITEMCOUNT",
		"FOLLOWTELEPORT"
	};
	
	private boolean checkBot(String text)
	{
		for (String botCommand : WALKER_COMMAND_LIST)
		{
			if (text.startsWith(botCommand))
			{
				return true;
			}
		}
		return false;
	}
	
	private void checkText(final L2PcInstance activeChar)
	{
		if (Config.USE_SAY_FILTER)
		{
			String filteredText = text.toLowerCase();
			
			for (String pattern : Config.FILTER_LIST)
			{
				filteredText = filteredText.replaceAll("(?i)" + pattern, Config.CHAT_FILTER_CHARS);
			}
			
			if (!filteredText.equalsIgnoreCase(text))
			{
				if (Config.CHAT_FILTER_PUNISHMENT.equalsIgnoreCase("chat"))
				{
					activeChar.setPunishLevel(PunishLevel.CHAT, Config.CHAT_FILTER_PUNISHMENT_PARAM1);
					activeChar.sendMessage("Administrator banned you chat from " + Config.CHAT_FILTER_PUNISHMENT_PARAM1 + " minutes");
				}
				else if (Config.CHAT_FILTER_PUNISHMENT.equalsIgnoreCase("karma"))
				{
					activeChar.setKarma(Config.CHAT_FILTER_PUNISHMENT_PARAM2);
					activeChar.sendMessage("You have get " + Config.CHAT_FILTER_PUNISHMENT_PARAM2 + " karma for bad words");
				}
				else if (Config.CHAT_FILTER_PUNISHMENT.equalsIgnoreCase("jail"))
				{
					activeChar.setPunishLevel(PunishLevel.JAIL, Config.CHAT_FILTER_PUNISHMENT_PARAM1);
				}
				activeChar.sendMessage("The word " + text + " is not allowed!");
				text = filteredText;
			}
		}
	}
	
	@Override
	public String getType()
	{
		return "[C] 38 Say2";
	}
}