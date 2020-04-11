package com.l2jfrozen.gameserver.network.clientpackets;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.crypt.nProtect;
import com.l2jfrozen.crypt.nProtect.RestrictionType;
import com.l2jfrozen.gameserver.communitybbs.Manager.RegionBBSManager;
import com.l2jfrozen.gameserver.controllers.GameTimeController;
import com.l2jfrozen.gameserver.datatables.GmListTable;
import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.datatables.csv.MapRegionTable;
import com.l2jfrozen.gameserver.datatables.sql.AccessLevels;
import com.l2jfrozen.gameserver.datatables.sql.AdminCommandAccessRights;
import com.l2jfrozen.gameserver.managers.CastleManager;
import com.l2jfrozen.gameserver.managers.ClanHallManager;
import com.l2jfrozen.gameserver.managers.CoupleManager;
import com.l2jfrozen.gameserver.managers.CrownManager;
import com.l2jfrozen.gameserver.managers.DimensionalRiftManager;
import com.l2jfrozen.gameserver.managers.FortSiegeManager;
import com.l2jfrozen.gameserver.managers.OlympiadStadiaManager;
import com.l2jfrozen.gameserver.managers.PetitionManager;
import com.l2jfrozen.gameserver.managers.SiegeManager;
import com.l2jfrozen.gameserver.model.Inventory;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Clan;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2ClassMasterInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.base.ClassLevel;
import com.l2jfrozen.gameserver.model.base.PlayerClass;
import com.l2jfrozen.gameserver.model.base.Race;
import com.l2jfrozen.gameserver.model.entity.Announcements;
import com.l2jfrozen.gameserver.model.entity.ClanHall;
import com.l2jfrozen.gameserver.model.entity.Hero;
import com.l2jfrozen.gameserver.model.entity.Wedding;
import com.l2jfrozen.gameserver.model.entity.event.CTF;
import com.l2jfrozen.gameserver.model.entity.event.DM;
import com.l2jfrozen.gameserver.model.entity.event.L2Event;
import com.l2jfrozen.gameserver.model.entity.event.TvT;
import com.l2jfrozen.gameserver.model.entity.sevensigns.SevenSigns;
import com.l2jfrozen.gameserver.model.entity.siege.Castle;
import com.l2jfrozen.gameserver.model.entity.siege.FortSiege;
import com.l2jfrozen.gameserver.model.entity.siege.Siege;
import com.l2jfrozen.gameserver.model.quest.Quest;
import com.l2jfrozen.gameserver.model.quest.QuestState;
import com.l2jfrozen.gameserver.model.zone.type.L2OlympiadStadiumZone;
import com.l2jfrozen.gameserver.network.Disconnection;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ClientSetTime;
import com.l2jfrozen.gameserver.network.serverpackets.CreatureSay;
import com.l2jfrozen.gameserver.network.serverpackets.Die;
import com.l2jfrozen.gameserver.network.serverpackets.EtcStatusUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.ExShowScreenMessage;
import com.l2jfrozen.gameserver.network.serverpackets.ExStorageMaxCount;
import com.l2jfrozen.gameserver.network.serverpackets.FriendList;
import com.l2jfrozen.gameserver.network.serverpackets.HennaInfo;
import com.l2jfrozen.gameserver.network.serverpackets.ItemList;
import com.l2jfrozen.gameserver.network.serverpackets.MagicSkillUser;
import com.l2jfrozen.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jfrozen.gameserver.network.serverpackets.PledgeShowMemberListAll;
import com.l2jfrozen.gameserver.network.serverpackets.PledgeShowMemberListUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.PledgeSkillList;
import com.l2jfrozen.gameserver.network.serverpackets.PledgeStatusChanged;
import com.l2jfrozen.gameserver.network.serverpackets.QuestList;
import com.l2jfrozen.gameserver.network.serverpackets.ShortCutInit;
import com.l2jfrozen.gameserver.network.serverpackets.SignsSky;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.network.serverpackets.UserInfo;
import com.l2jfrozen.gameserver.thread.TaskPriority;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;
import com.l2jfrozen.gameserver.util.Util;

import main.EngineModsManager;

public class EnterWorld extends L2GameClientPacket
{
	private static Logger LOGGER = Logger.getLogger(EnterWorld.class);
	private static final L2Skill SUPER_HASTE_4 = SkillTable.getInstance().getInfo(7029, 4);
	private static final L2Skill SHADOW_SENSE = SkillTable.getInstance().getInfo(294, 1);
	
	private final SimpleDateFormat fmt = new SimpleDateFormat("H:mm.");
	
	public TaskPriority getPriority()
	{
		return TaskPriority.PR_URGENT;
	}
	
	@Override
	protected void readImpl()
	{
		// this is just a trigger packet. it has no content
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
		{
			LOGGER.warn("EnterWorld failed! activeChar is null...");
			getClient().closeNow();
			return;
		}
		
		// Set lock at login
		activeChar.setLocked(true);
		
		// Register in flood protector
		// FloodProtector.getInstance().registerNewPlayer(activeChar.getObjectId());
		
		if (L2World.getInstance().findObject(activeChar.getObjectId()) != null)
		{
			if (Config.DEBUG)
			{
				LOGGER.warn("DEBUG " + getType() + ": User already exist in OID map! User " + activeChar.getName() + " is character clone");
				// activeChar.closeNetConnection(); // Do nothing?
			}
		}
		
		if (!activeChar.isGM() && !activeChar.isVIP() && Config.CHECK_NAME_ON_LOGIN)
		{
			if (activeChar.getName().length() < 3 || activeChar.getName().length() > 16 || !Util.isAlphaNumeric(activeChar.getName()) || !isValidName(activeChar.getName()))
			{
				LOGGER.warn("Charname: " + activeChar.getName() + " is invalid. EnterWorld failed.");
				getClient().closeNow();
				return;
			}
		}
		
		// Set online status
		activeChar.setOnline(true);
		
		activeChar.setRunning(); // running is default
		activeChar.standUp(); // standing is default
		
		activeChar.broadcastKarma(); // include UserInfo
		
		// Engage and notify Partner
		if (Config.L2JMOD_ALLOW_WEDDING)
		{
			engage(activeChar);
			notifyPartner(activeChar, activeChar.getPartnerId());
		}
		
		onEnterGM(activeChar);
		
		Quest.playerEnter(activeChar);
		activeChar.sendPacket(new QuestList());
		
		activeChar.loadVariables();
		activeChar.loadAccountVariables();
		
		if (Config.PLAYER_SPAWN_PROTECTION > 0)
		{
			activeChar.setProtection(true);
		}
		
		activeChar.spawnMe(activeChar.getX(), activeChar.getY(), activeChar.getZ());
		
		if (SevenSigns.getInstance().isSealValidationPeriod())
		{
			sendPacket(new SignsSky());
		}
		
		// Buff and Status icons
		if (Config.STORE_SKILL_COOLTIME)
		{
			activeChar.restoreEffects();
		}
		
		activeChar.sendPacket(new EtcStatusUpdate(activeChar));
		
		// Apply augmentation boni for equipped items
		for (final L2ItemInstance temp : activeChar.getInventory().getAugmentedItems())
		{
			if (temp != null && temp.isEquipped())
			{
				temp.getAugmentation().applyBoni(activeChar);
			}
		}
		
		// Apply death penalty
		activeChar.restoreDeathPenaltyBuffLevel();
		
		if (L2Event.active && L2Event.connectionLossData.containsKey(activeChar.getName()) && L2Event.isOnEvent(activeChar))
		{
			L2Event.restoreChar(activeChar);
		}
		else if (L2Event.connectionLossData.containsKey(activeChar.getName()))
		{
			L2Event.restoreAndTeleChar(activeChar);
		}
		
		// SECURE FIX - Anti Overenchant Cheat!!
		if (Config.MAX_ITEM_ENCHANT_KICK > 0)
		{
			for (L2ItemInstance i : activeChar.getInventory().getItems())
			{
				if (!activeChar.isGM())
				{
					if (i.isEquipable())
					{
						if (i.getEnchantLevel() > Config.MAX_ITEM_ENCHANT_KICK)
						{
							// Delete Item Over enchanted
							activeChar.getInventory().destroyItem(null, i, activeChar, null);
							// Message to Player
							activeChar.sendMessage("[Server]: You have over enchanted items you will be kicked from server!");
							activeChar.sendMessage("[Server]: Respect our server rules.");
							// Message with screen
							sendPacket(new ExShowScreenMessage(" You have an over enchanted item, you will be kicked from server! ", 6000));
							// Punishment e LOGGER in audit
							Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " has Overenchanted  item! Kicked! ", Config.DEFAULT_PUNISH);
							// Logger in console
							LOGGER.warn("#### ATTENTION ####");
							LOGGER.warn(i + "(" + i.getItemId() + ")" + i.getEnchantLevel() + " item has been removed from " + activeChar);
						}
					}
				}
			}
		}
		
		long customHeroEnd = activeChar.getVariableLong(L2PcInstance.HERO_END, -1L);
		
		if (customHeroEnd == 0)
		{
			activeChar.setHero(true);
		}
		else if (customHeroEnd > System.currentTimeMillis())
		{
			activeChar.setHero(true);
			activeChar.setHeroEndDate(customHeroEnd);
		}
		else if (customHeroEnd != -1)
		{
			activeChar.removeVariable(L2PcInstance.HERO_END, true);
		}
		
		long vipEnd = activeChar.getVariableLong(L2PcInstance.VIP_END, -1L);
		
		if (vipEnd == 0)
		{
			activeChar.setVIP(true);
		}
		else if (vipEnd > System.currentTimeMillis())
		{
			activeChar.setVIP(true);
			activeChar.setVIPEndDate(vipEnd);
		}
		else if (vipEnd != -1)
		{
			activeChar.removeVariable(L2PcInstance.VIP_END, true);
		}
		
		long aioEnd = activeChar.getVariableLong(L2PcInstance.AIO_END, -1L);
		
		if (aioEnd == 0)
		{
			activeChar.setAio(true);
		}
		else if (aioEnd > System.currentTimeMillis())
		{
			activeChar.setAio(true);
			activeChar.setAioEndDate(aioEnd);
		}
		else if (aioEnd != -1)
		{
			activeChar.removeAioSkills();
			activeChar.removeVariable(L2PcInstance.AIO_END, true);
		}
		
		applyColorSystem(activeChar);
		
		// Expand Skill
		final ExStorageMaxCount esmc = new ExStorageMaxCount(activeChar);
		activeChar.sendPacket(esmc);
		
		activeChar.getMacroses().sendUpdate();
		
		// Send packets info
		sendPacket(new ClientSetTime()); // SetClientTime
		sendPacket(new UserInfo(activeChar)); //
		sendPacket(new HennaInfo(activeChar));
		sendPacket(new FriendList(activeChar));
		sendPacket(new ItemList(activeChar, false));
		sendPacket(new ShortCutInit(activeChar));
		
		// Reload inventory to give SA skill
		activeChar.getInventory().reloadEquippedItems();
		
		// Welcome to Lineage II
		sendPacket(new SystemMessage(SystemMessageId.WELCOME_TO_LINEAGE));
		
		SevenSigns.getInstance().sendCurrentPeriodMsg(activeChar);
		Announcements.getInstance().showAnnouncements(activeChar);
		
		loadTutorial(activeChar);
		
		// Check for crowns
		CrownManager.getInstance().checkCrowns(activeChar);
		
		// Check player skills
		if (Config.CHECK_SKILLS_ON_ENTER && !Config.ALT_GAME_SKILL_LEARN)
		{
			activeChar.checkAllowedSkills();
		}
		
		PetitionManager.getInstance().checkPetitionMessages(activeChar);
		
		// Send user info again .. just like the real client
		// sendPacket(ui);
		
		if (activeChar.getClanId() != 0 && activeChar.getClan() != null)
		{
			sendPacket(new PledgeShowMemberListAll(activeChar.getClan(), activeChar));
			sendPacket(new PledgeStatusChanged(activeChar.getClan()));
		}
		
		if (activeChar.isAlikeDead())
		{
			sendPacket(new Die(activeChar)); // No broadcast needed since the player will already spawn dead to others
		}
		
		if (Config.ALLOW_WATER)
		{
			activeChar.checkWaterState();
		}
		
		if (Hero.getInstance().getHeroes() != null && Hero.getInstance().getHeroes().containsKey(activeChar.getObjectId()))
		{
			activeChar.setHero(true);
		}
		
		setPledgeClass(activeChar);
		
		for (final String name : activeChar.getFriendList())
		{
			final L2PcInstance friend = L2World.getInstance().getPlayer(name);
			
			if (friend != null)
			{
				friend.sendPacket(new SystemMessage(SystemMessageId.FRIEND_S1_HAS_LOGGED_IN).addString(activeChar.getName()));
			}
		}
		
		notifyClanMembers(activeChar);
		notifySponsorOrApprentice(activeChar);
		
		activeChar.setTarget(activeChar);
		
		activeChar.onPlayerEnter();
		
		if (Config.PCB_ENABLE)
		{
			activeChar.showPcBangWindow();
		}
		
		if (Config.ANNOUNCE_CASTLE_LORDS)
		{
			notifyCastleOwner(activeChar);
		}
		
		L2OlympiadStadiumZone zone = OlympiadStadiaManager.getInstance().getStadium(activeChar);
		if (zone != null)
		{
			LOGGER.warn("Player " + activeChar.getName() + " logged in INSIDE of Olympiad Stadium, maybe player got critical error while observing Olympiad.");
			zone.characterList.remove(activeChar.getObjectId());
		}
		
		if (DimensionalRiftManager.getInstance().checkIfInRiftZone(activeChar.getX(), activeChar.getY(), activeChar.getZ(), false))
		{
			DimensionalRiftManager.getInstance().teleportToWaitingRoom(activeChar);
		}
		
		if (activeChar.getClanJoinExpiryTime() > System.currentTimeMillis())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.CLAN_MEMBERSHIP_TERMINATED));
		}
		
		if (activeChar.getClan() != null)
		{
			activeChar.sendPacket(new PledgeSkillList(activeChar.getClan()));
			
			for (final Siege siege : SiegeManager.getInstance().getSieges())
			{
				if (!siege.getIsInProgress())
				{
					continue;
				}
				
				if (siege.checkIsAttacker(activeChar.getClan()))
				{
					activeChar.setSiegeState((byte) 1);
					break;
				}
				else if (siege.checkIsDefender(activeChar.getClan()))
				{
					activeChar.setSiegeState((byte) 2);
					break;
				}
			}
			
			for (final FortSiege fortsiege : FortSiegeManager.getInstance().getSieges())
			{
				if (!fortsiege.getIsInProgress())
				{
					continue;
				}
				
				if (fortsiege.checkIsAttacker(activeChar.getClan()))
				{
					activeChar.setSiegeState((byte) 1);
					break;
				}
				else if (fortsiege.checkIsDefender(activeChar.getClan()))
				{
					activeChar.setSiegeState((byte) 2);
					break;
				}
			}
			
			// Add message at connexion if clanHall not paid. Possibly this is custom...
			final ClanHall clanHall = ClanHallManager.getInstance().getClanHallByOwner(activeChar.getClan());
			
			if (clanHall != null)
			{
				if (!clanHall.getPaid())
				{
					activeChar.sendPacket(new SystemMessage(SystemMessageId.PAYMENT_FOR_YOUR_CLAN_HALL_HAS_NOT_BEEN_MADE_PLEASE_MAKE_PAYMENT_TO_YOUR_CLAN_WAREHOUSE_BY_S1_TOMORROW));
				}
			}
		}
		
		if (!activeChar.isGM() && activeChar.getSiegeState() < 2 && activeChar.isInsideZone(L2Character.ZONE_SIEGE))
		{
			// Attacker or spectator logging in to a siege zone. Actually should be checked for inside castle only?
			activeChar.teleToLocation(MapRegionTable.TeleportWhereType.Town);
			activeChar.sendMessage("You have been teleported to the nearest town due to you being in siege zone");
		}
		
		RegionBBSManager.getInstance().changeCommunityBoard();
		
		if (TvT.savePlayers.contains(activeChar.getName()))
		{
			TvT.addDisconnectedPlayer(activeChar);
		}
		
		if (CTF.savePlayers.contains(activeChar.getName()))
		{
			CTF.addDisconnectedPlayer(activeChar);
		}
		
		if (DM.savePlayers.contains(activeChar.getName()))
		{
			DM.addDisconnectedPlayer(activeChar);
		}
		
		// Means that it's not ok multiBox situation, so logout
		if (!activeChar.checkMultiBox())
		{
			activeChar.sendMessage("I'm sorry, but multibox is not allowed here.");
			activeChar.logout();
		}
		
		sendHellows(activeChar);
		
		if (Config.ALLOW_CLASS_MASTERS && Config.ALLOW_REMOTE_CLASS_MASTERS)
		{
			final L2ClassMasterInstance master_instance = L2ClassMasterInstance.getInstance();
			
			if (master_instance != null)
			{
				
				final ClassLevel lvlnow = PlayerClass.values()[activeChar.getClassId().getId()].getLevel();
				
				if (activeChar.getLevel() >= 20 && lvlnow == ClassLevel.First)
				{
					L2ClassMasterInstance.getInstance().onAction(activeChar);
				}
				else if (activeChar.getLevel() >= 40 && lvlnow == ClassLevel.Second)
				{
					L2ClassMasterInstance.getInstance().onAction(activeChar);
				}
				else if (activeChar.getLevel() >= 76 && lvlnow == ClassLevel.Third)
				{
					L2ClassMasterInstance.getInstance().onAction(activeChar);
				}
				
			}
			else
			{
				LOGGER.info("Attention: Remote ClassMaster is Enabled, but not inserted into DataBase. Remember to install 31288 Custom_Npc ..");
			}
		}
		
		// Apply night/day bonus on skill Shadow Sense
		if (activeChar.getRace() == Race.darkelf)
		{
			if (GameTimeController.getInstance().isNowNight())
			{
				activeChar.addSkill(SHADOW_SENSE, false);
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.NIGHT_EFFECT_APPLIES);
				sm.addSkillName(294);
				sendPacket(sm);
			}
			else
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.DAY_EFFECT_DISAPPEARS);
				sm.addSkillName(294);
				sendPacket(sm);
				
				if (activeChar.getSkillLevel(SHADOW_SENSE.getId()) > 0)
				{
					activeChar.removeSkill(SHADOW_SENSE.getId());
				}
			}
		}
		
		if (!nProtect.getInstance().checkRestriction(activeChar, RestrictionType.RESTRICT_ENTER))
		{
			activeChar.setIsImobilised(true);
			activeChar.disableAllSkills();
			ThreadPoolManager.getInstance().scheduleGeneral(new Disconnection(activeChar), 20000);
		}
		
		// Elrokian Trap like L2OFF
		final L2ItemInstance rhand = activeChar.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
		if (rhand != null && rhand.getItemId() == 8763)
		{
			activeChar.addSkill(SkillTable.getInstance().getInfo(3626, 1));
			activeChar.addSkill(SkillTable.getInstance().getInfo(3627, 1));
			activeChar.addSkill(SkillTable.getInstance().getInfo(3628, 1));
		}
		else
		{
			activeChar.removeSkill(3626, true);
			activeChar.removeSkill(3627, true);
			activeChar.removeSkill(3628, true);
		}
		
		// If it's a Beta server all players got GM SPEED skill for better testing
		if (Config.BETASERVER)
		{
			activeChar.addSkill(SkillTable.getInstance().getInfo(7029, 4), false);
			activeChar.sendMessage("Server is on Beta mode. Skill Gm Haste 4 added for better testing.");
		}
		
		// Send all skills to char
		activeChar.sendSkillList();
		
		// Close lock at login
		activeChar.setLocked(false);
		
		EngineModsManager.onEnterWorld(activeChar);
	}
	
	private boolean isValidName(final String text)
	{
		boolean result = true;
		final String test = text;
		Pattern pattern;
		
		try
		{
			pattern = Pattern.compile(Config.CNAME_TEMPLATE);
		}
		catch (final PatternSyntaxException e) // case of illegal pattern
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			LOGGER.warn("ERROR " + getType() + ": Character name pattern of config is wrong!");
			pattern = Pattern.compile(".*");
		}
		
		final Matcher regexp = pattern.matcher(test);
		if (!regexp.matches())
		{
			result = false;
		}
		
		return result;
	}
	
	private void onEnterGM(L2PcInstance activeChar)
	{
		if (activeChar.isGM())
		{
			if (Config.GM_STARTUP_INVULNERABLE && AdminCommandAccessRights.getInstance().hasAccess("admin_invul", activeChar.getAccessLevel()))
			{
				activeChar.setIsInvul(true);
			}
			
			if (Config.GM_STARTUP_INVISIBLE && AdminCommandAccessRights.getInstance().hasAccess("admin_invisible", activeChar.getAccessLevel()))
			{
				activeChar.getAppearance().setInvisible();
			}
			
			if (Config.GM_STARTUP_SILENCE && AdminCommandAccessRights.getInstance().hasAccess("admin_silence", activeChar.getAccessLevel()))
			{
				activeChar.setMessageRefusal(true);
			}
			
			if (Config.GM_STARTUP_AUTO_LIST && AdminCommandAccessRights.getInstance().hasAccess("admin_gmliston", activeChar.getAccessLevel()))
			{
				GmListTable.getInstance().addGm(activeChar, false);
			}
			else
			{
				GmListTable.getInstance().addGm(activeChar, true);
			}
			
			int accesslevel = activeChar.getAccessLevel().getLevel();
			
			activeChar.getAppearance().setNameColor(AccessLevels.getInstance().getAccessLevel(accesslevel).getNameColor());
			
			activeChar.getAppearance().setTitleColor(AccessLevels.getInstance().getAccessLevel(accesslevel).getTitleColor());
			
			activeChar.addSkill(SUPER_HASTE_4, false);
		}
	}
	
	private void sendHellows(final L2PcInstance activeChar)
	{
		if (Config.ALT_SERVER_NAME_ENABLED)
		{
			sendPacket(new SystemMessage(SystemMessageId.S1_S2).addString("Welcome to " + Config.ALT_Server_Name));
		}
		
		if (Config.ONLINE_PLAYERS_ON_LOGIN)
		{
			sendPacket(new SystemMessage(SystemMessageId.S1_S2).addString("There are " + L2World.getInstance().getAllPlayers().size() + " players online."));
		}
		
		if (activeChar.getFirstLog() && Config.NEW_PLAYER_EFFECT)
		{
			final L2Skill skill = SkillTable.getInstance().getInfo(2025, 1);
			if (skill != null)
			{
				final MagicSkillUser MSU = new MagicSkillUser(activeChar, activeChar, 2025, 1, 1, 0);
				activeChar.sendPacket(MSU);
				activeChar.broadcastPacket(MSU);
				activeChar.useMagic(skill, false, false);
			}
			activeChar.setFirstLog(false);
			activeChar.updateFirstLog();
		}
		
		if (Config.WELCOME_HTM && isValidName(activeChar.getName()))
		{
			final String Welcome_Path = "data/html/welcome.htm";
			final File mainText = new File(Config.DATAPACK_ROOT, Welcome_Path);
			if (mainText.exists())
			{
				final NpcHtmlMessage html = new NpcHtmlMessage(1);
				html.setFile(Welcome_Path);
				html.replace("%name%", activeChar.getName());
				sendPacket(html);
			}
		}
		
		if (activeChar.getClan() != null && activeChar.getClan().isNoticeEnabled())
		{
			final String clanNotice = "data/html/clanNotice.htm";
			final File mainText = new File(Config.DATAPACK_ROOT, clanNotice);
			if (mainText.exists())
			{
				final NpcHtmlMessage html = new NpcHtmlMessage(1);
				html.setFile(clanNotice);
				html.replace("%clan_name%", activeChar.getClan().getName());
				html.replace("%notice_text%", activeChar.getClan().getNotice().replaceAll("\r\n", "<br>"));
				sendPacket(html);
			}
		}
		
		if (Config.PM_MESSAGE_ON_START)
		{
			activeChar.sendPacket(new CreatureSay(2, Say2.HERO_VOICE, Config.PM_TEXT1, Config.PM_SERVER_NAME));
			activeChar.sendPacket(new CreatureSay(15, Say2.PARTYROOM_COMMANDER, activeChar.getName(), Config.PM_TEXT2));
		}
		
		if (Config.SERVER_TIME_ON_START)
		{
			activeChar.sendMessage("Server time is " + fmt.format(new Date(System.currentTimeMillis())));
		}
	}
	
	private void applyColorSystem(final L2PcInstance activeChar)
	{
		// GMs player dont be affected by color system
		if (activeChar.isGM())
		{
			return;
		}
		
		activeChar.updatePvPColor(activeChar.getPvpKills());
		activeChar.updatePkColor(activeChar.getPkKills());
		activeChar.updateClanLeaderColor();
		activeChar.updateAIOColor();
		activeChar.updateMarriedColor();
		activeChar.updateVIPColor();
	}
	
	private void engage(L2PcInstance cha)
	{
		final int chaid = cha.getObjectId();
		
		for (Wedding couple : CoupleManager.getInstance().getCouples())
		{
			if (couple.getPlayer1Id() == chaid || couple.getPlayer2Id() == chaid)
			{
				if (couple.getMaried())
				{
					cha.setMarried(true);
					cha.setmarriedType(couple.getType());
				}
				
				cha.setCoupleId(couple.getId());
				
				if (couple.getPlayer1Id() == chaid)
				{
					cha.setPartnerId(couple.getPlayer2Id());
				}
				else
				{
					cha.setPartnerId(couple.getPlayer1Id());
				}
			}
		}
	}
	
	private void notifyPartner(L2PcInstance cha, int partnerId)
	{
		if (cha.getPartnerId() != 0)
		{
			L2PcInstance partner = null;
			
			if (L2World.getInstance().findObject(cha.getPartnerId()) instanceof L2PcInstance)
			{
				partner = (L2PcInstance) L2World.getInstance().findObject(cha.getPartnerId());
			}
			
			if (partner != null)
			{
				partner.sendMessage("Your partner has logged in");
			}
		}
	}
	
	private void notifyClanMembers(L2PcInstance activeChar)
	{
		final L2Clan clan = activeChar.getClan();
		if (clan != null)
		{
			clan.getClanMember(activeChar.getObjectId()).setPlayerInstance(activeChar);
			clan.broadcastToOtherOnlineMembers(new SystemMessage(SystemMessageId.CLAN_MEMBER_S1_LOGGED_IN).addString(activeChar.getName()), activeChar);
			clan.broadcastToOtherOnlineMembers(new PledgeShowMemberListUpdate(activeChar), activeChar);
		}
	}
	
	private void notifySponsorOrApprentice(L2PcInstance activeChar)
	{
		if (activeChar.getSponsor() != 0)
		{
			final L2PcInstance sponsor = (L2PcInstance) L2World.getInstance().findObject(activeChar.getSponsor());
			if (sponsor != null)
			{
				sponsor.sendPacket(new SystemMessage(SystemMessageId.YOUR_APPRENTICE_S1_HAS_LOGGED_IN).addString(activeChar.getName()));
			}
		}
		else if (activeChar.getApprentice() != 0)
		{
			final L2PcInstance apprentice = (L2PcInstance) L2World.getInstance().findObject(activeChar.getApprentice());
			if (apprentice != null)
			{
				apprentice.sendPacket(new SystemMessage(SystemMessageId.YOUR_SPONSOR_S1_HAS_LOGGED_IN).addString(activeChar.getName()));
			}
		}
	}
	
	private void loadTutorial(L2PcInstance player)
	{
		final QuestState qs = player.getQuestState("255_Tutorial");
		if (qs != null)
		{
			qs.getQuest().notifyEvent("UC", null, player);
		}
	}
	
	private void setPledgeClass(L2PcInstance activeChar)
	{
		int pledgeClass = 0;
		
		if (activeChar.getClan() != null)
		{
			pledgeClass = activeChar.getClan().getClanMember(activeChar.getObjectId()).calculatePledgeClass(activeChar);
		}
		
		if (activeChar.isNoble() && pledgeClass < 5)
		{
			pledgeClass = 5;
		}
		
		if (activeChar.isHero())
		{
			pledgeClass = 8;
		}
		
		activeChar.setPledgeClass(pledgeClass);
	}
	
	private void notifyCastleOwner(L2PcInstance activeChar)
	{
		L2Clan clan = activeChar.getClan();
		if (clan != null)
		{
			if (clan.getCastleId() > 0)
			{
				final Castle castle = CastleManager.getInstance().getCastleById(clan.getCastleId());
				if (castle != null && activeChar.getObjectId() == clan.getLeaderId())
				{
					Announcements.getInstance().announceToAll("Lord " + activeChar.getName() + " Ruler Of " + castle.getName() + " Castle is now Online!");
				}
			}
		}
	}
	
	@Override
	public String getType()
	{
		return "[C] 03 EnterWorld";
	}
}