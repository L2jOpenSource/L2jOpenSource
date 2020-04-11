package handlers;

import l2r.Config;
import l2r.gameserver.handler.ActionHandler;
import l2r.gameserver.handler.ActionShiftHandler;
import l2r.gameserver.handler.AdminCommandHandler;
import l2r.gameserver.handler.BypassHandler;
import l2r.gameserver.handler.ChatHandler;
import l2r.gameserver.handler.IHandler;
import l2r.gameserver.handler.ItemHandler;
import l2r.gameserver.handler.PlayerActionHandler;
import l2r.gameserver.handler.PunishmentHandler;
import l2r.gameserver.handler.SkillHandler;
import l2r.gameserver.handler.TargetHandler;
import l2r.gameserver.handler.TelnetHandler;
import l2r.gameserver.handler.UserCommandHandler;
import l2r.gameserver.handler.VoicedCommandHandler;

import gr.sr.configsEngine.configs.impl.AioItemsConfigs;
import gr.sr.configsEngine.configs.impl.AntibotConfigs;
import gr.sr.configsEngine.configs.impl.BufferConfigs;
import gr.sr.configsEngine.configs.impl.ChaoticZoneConfigs;
import gr.sr.configsEngine.configs.impl.CustomServerConfigs;
import gr.sr.configsEngine.configs.impl.GetRewardVoteSystemConfigs;
import gr.sr.configsEngine.configs.impl.PremiumServiceConfigs;
import gr.sr.voteEngine.RewardVote;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import handlers.actionhandlers.L2ArtefactInstanceAction;
import handlers.actionhandlers.L2DecoyAction;
import handlers.actionhandlers.L2DoorInstanceAction;
import handlers.actionhandlers.L2ItemInstanceAction;
import handlers.actionhandlers.L2NpcAction;
import handlers.actionhandlers.L2PcInstanceAction;
import handlers.actionhandlers.L2PetInstanceAction;
import handlers.actionhandlers.L2StaticObjectInstanceAction;
import handlers.actionhandlers.L2SummonAction;
import handlers.actionhandlers.L2TrapAction;
import handlers.actionshifthandlers.L2DoorInstanceActionShift;
import handlers.actionshifthandlers.L2ItemInstanceActionShift;
import handlers.actionshifthandlers.L2NpcActionShift;
import handlers.actionshifthandlers.L2PcInstanceActionShift;
import handlers.actionshifthandlers.L2StaticObjectInstanceActionShift;
import handlers.actionshifthandlers.L2SummonActionShift;
import handlers.admincommandhandlers.AdminAdmin;
import handlers.admincommandhandlers.AdminAnnouncements;
import handlers.admincommandhandlers.AdminBBS;
import handlers.admincommandhandlers.AdminBuffs;
import handlers.admincommandhandlers.AdminCHSiege;
import handlers.admincommandhandlers.AdminCamera;
import handlers.admincommandhandlers.AdminChangeAccessLevel;
import handlers.admincommandhandlers.AdminCheckBots;
import handlers.admincommandhandlers.AdminClan;
import handlers.admincommandhandlers.AdminCreateItem;
import handlers.admincommandhandlers.AdminCursedWeapons;
import handlers.admincommandhandlers.AdminCustomCreateItem;
import handlers.admincommandhandlers.AdminDebug;
import handlers.admincommandhandlers.AdminDelete;
import handlers.admincommandhandlers.AdminDisconnect;
import handlers.admincommandhandlers.AdminDoorControl;
import handlers.admincommandhandlers.AdminEditChar;
import handlers.admincommandhandlers.AdminEditNpc;
import handlers.admincommandhandlers.AdminEffects;
import handlers.admincommandhandlers.AdminElement;
import handlers.admincommandhandlers.AdminEnchant;
import handlers.admincommandhandlers.AdminExpSp;
import handlers.admincommandhandlers.AdminFightCalculator;
import handlers.admincommandhandlers.AdminFortSiege;
import handlers.admincommandhandlers.AdminGamePoints;
import handlers.admincommandhandlers.AdminGeodata;
import handlers.admincommandhandlers.AdminGm;
import handlers.admincommandhandlers.AdminGmChat;
import handlers.admincommandhandlers.AdminGraciaSeeds;
import handlers.admincommandhandlers.AdminGrandBoss;
import handlers.admincommandhandlers.AdminHWIDBan;
import handlers.admincommandhandlers.AdminHeal;
import handlers.admincommandhandlers.AdminHellbound;
import handlers.admincommandhandlers.AdminHtml;
import handlers.admincommandhandlers.AdminInstance;
import handlers.admincommandhandlers.AdminInstanceZone;
import handlers.admincommandhandlers.AdminInventory;
import handlers.admincommandhandlers.AdminInvul;
import handlers.admincommandhandlers.AdminKick;
import handlers.admincommandhandlers.AdminKill;
import handlers.admincommandhandlers.AdminLevel;
import handlers.admincommandhandlers.AdminLogin;
import handlers.admincommandhandlers.AdminLogsViewer;
import handlers.admincommandhandlers.AdminMammon;
import handlers.admincommandhandlers.AdminManor;
import handlers.admincommandhandlers.AdminMenu;
import handlers.admincommandhandlers.AdminMessages;
import handlers.admincommandhandlers.AdminMobGroup;
import handlers.admincommandhandlers.AdminMonsterRace;
import handlers.admincommandhandlers.AdminOlympiad;
import handlers.admincommandhandlers.AdminPForge;
import handlers.admincommandhandlers.AdminPathNode;
import handlers.admincommandhandlers.AdminPcCondOverride;
import handlers.admincommandhandlers.AdminPetition;
import handlers.admincommandhandlers.AdminPledge;
import handlers.admincommandhandlers.AdminPolymorph;
import handlers.admincommandhandlers.AdminPremium;
import handlers.admincommandhandlers.AdminPunishment;
import handlers.admincommandhandlers.AdminQuest;
import handlers.admincommandhandlers.AdminReload;
import handlers.admincommandhandlers.AdminRepairChar;
import handlers.admincommandhandlers.AdminRes;
import handlers.admincommandhandlers.AdminRide;
import handlers.admincommandhandlers.AdminScan;
import handlers.admincommandhandlers.AdminShop;
import handlers.admincommandhandlers.AdminShowQuests;
import handlers.admincommandhandlers.AdminShutdown;
import handlers.admincommandhandlers.AdminSiege;
import handlers.admincommandhandlers.AdminSkill;
import handlers.admincommandhandlers.AdminSpawn;
import handlers.admincommandhandlers.AdminSummon;
import handlers.admincommandhandlers.AdminTarget;
import handlers.admincommandhandlers.AdminTargetSay;
import handlers.admincommandhandlers.AdminTeleport;
import handlers.admincommandhandlers.AdminTerritoryWar;
import handlers.admincommandhandlers.AdminTest;
import handlers.admincommandhandlers.AdminUnblockIp;
import handlers.admincommandhandlers.AdminVitality;
import handlers.admincommandhandlers.AdminZone;
import handlers.bypasshandlers.ArenaBuff;
import handlers.bypasshandlers.Augment;
import handlers.bypasshandlers.Buy;
import handlers.bypasshandlers.BuyShadowItem;
import handlers.bypasshandlers.ChatLink;
import handlers.bypasshandlers.ClanWarehouse;
import handlers.bypasshandlers.ElcardiaBuff;
import handlers.bypasshandlers.Festival;
import handlers.bypasshandlers.Freight;
import handlers.bypasshandlers.ItemAuctionLink;
import handlers.bypasshandlers.Link;
import handlers.bypasshandlers.Loto;
import handlers.bypasshandlers.Multisell;
import handlers.bypasshandlers.Observation;
import handlers.bypasshandlers.OlympiadManagerLink;
import handlers.bypasshandlers.OlympiadObservation;
import handlers.bypasshandlers.PlayerHelp;
import handlers.bypasshandlers.PrivateWarehouse;
import handlers.bypasshandlers.QuestLink;
import handlers.bypasshandlers.QuestList;
import handlers.bypasshandlers.ReleaseAttribute;
import handlers.bypasshandlers.RemoveDeathPenalty;
import handlers.bypasshandlers.RentPet;
import handlers.bypasshandlers.Rift;
import handlers.bypasshandlers.SkillList;
import handlers.bypasshandlers.SupportBlessing;
import handlers.bypasshandlers.SupportMagic;
import handlers.bypasshandlers.TerritoryStatus;
import handlers.bypasshandlers.VoiceCommand;
import handlers.bypasshandlers.Wear;
import handlers.chathandlers.ChatAll;
import handlers.chathandlers.ChatAlliance;
import handlers.chathandlers.ChatBattlefield;
import handlers.chathandlers.ChatClan;
import handlers.chathandlers.ChatHeroVoice;
import handlers.chathandlers.ChatParty;
import handlers.chathandlers.ChatPartyMatchRoom;
import handlers.chathandlers.ChatPartyRoomAll;
import handlers.chathandlers.ChatPartyRoomCommander;
import handlers.chathandlers.ChatPetition;
import handlers.chathandlers.ChatShout;
import handlers.chathandlers.ChatTell;
import handlers.chathandlers.ChatTrade;
import handlers.itemhandlers.AioItemBuff;
import handlers.itemhandlers.AioItemNpcs;
import handlers.itemhandlers.BeastSoulShot;
import handlers.itemhandlers.BeastSpiritShot;
import handlers.itemhandlers.BlessedSpiritShot;
import handlers.itemhandlers.Book;
import handlers.itemhandlers.Bypass;
import handlers.itemhandlers.Calculator;
import handlers.itemhandlers.CharmOfCourage;
import handlers.itemhandlers.ChristmasTree;
import handlers.itemhandlers.Disguise;
import handlers.itemhandlers.Elixir;
import handlers.itemhandlers.EnchantAttribute;
import handlers.itemhandlers.EnchantScrolls;
import handlers.itemhandlers.EventItem;
import handlers.itemhandlers.ExtractableItems;
import handlers.itemhandlers.FishShots;
import handlers.itemhandlers.Harvester;
import handlers.itemhandlers.ItemSkills;
import handlers.itemhandlers.ItemSkillsTemplate;
import handlers.itemhandlers.ManaPotion;
import handlers.itemhandlers.Maps;
import handlers.itemhandlers.MercTicket;
import handlers.itemhandlers.NicknameColor;
import handlers.itemhandlers.PetFood;
import handlers.itemhandlers.Recipes;
import handlers.itemhandlers.RollingDice;
import handlers.itemhandlers.Seed;
import handlers.itemhandlers.SevenSignsRecord;
import handlers.itemhandlers.SoulShots;
import handlers.itemhandlers.SpecialXMas;
import handlers.itemhandlers.SpiritShot;
import handlers.itemhandlers.SummonItems;
import handlers.itemhandlers.TeleportBookmark;
import handlers.playeractions.AirshipAction;
import handlers.playeractions.BotReport;
import handlers.playeractions.PetAttack;
import handlers.playeractions.PetHold;
import handlers.playeractions.PetMove;
import handlers.playeractions.PetSkillUse;
import handlers.playeractions.PetStop;
import handlers.playeractions.PrivateStore;
import handlers.playeractions.Ride;
import handlers.playeractions.RunWalk;
import handlers.playeractions.ServitorAttack;
import handlers.playeractions.ServitorHold;
import handlers.playeractions.ServitorMove;
import handlers.playeractions.ServitorSkillUse;
import handlers.playeractions.ServitorStop;
import handlers.playeractions.SitStand;
import handlers.playeractions.SocialAction;
import handlers.playeractions.UnsummonPet;
import handlers.playeractions.UnsummonServitor;
import handlers.punishmenthandlers.BanHandler;
import handlers.punishmenthandlers.ChatBanHandler;
import handlers.punishmenthandlers.JailHandler;
import handlers.skillhandlers.Blow;
import handlers.skillhandlers.ChainHeal;
import handlers.skillhandlers.Continuous;
import handlers.skillhandlers.Disablers;
import handlers.skillhandlers.Dummy;
import handlers.skillhandlers.Mdam;
import handlers.skillhandlers.Pdam;
import handlers.skillhandlers.Unlock;
import handlers.targethandlers.Area;
import handlers.targethandlers.AreaCorpseMob;
import handlers.targethandlers.AreaFriendly;
import handlers.targethandlers.AreaSummon;
import handlers.targethandlers.Aura;
import handlers.targethandlers.AuraCorpseMob;
import handlers.targethandlers.AuraFriendly;
import handlers.targethandlers.AuraUndeadEnemy;
import handlers.targethandlers.BehindArea;
import handlers.targethandlers.BehindAura;
import handlers.targethandlers.Clan;
import handlers.targethandlers.ClanMember;
import handlers.targethandlers.CommandChannel;
import handlers.targethandlers.CorpseClan;
import handlers.targethandlers.CorpseMob;
import handlers.targethandlers.CorpsePet;
import handlers.targethandlers.CorpsePlayer;
import handlers.targethandlers.EnemySummon;
import handlers.targethandlers.FlagPole;
import handlers.targethandlers.FrontArea;
import handlers.targethandlers.FrontAura;
import handlers.targethandlers.Ground;
import handlers.targethandlers.Holy;
import handlers.targethandlers.One;
import handlers.targethandlers.OwnerPet;
import handlers.targethandlers.Party;
import handlers.targethandlers.PartyClan;
import handlers.targethandlers.PartyMember;
import handlers.targethandlers.PartyNotMe;
import handlers.targethandlers.PartyOther;
import handlers.targethandlers.PartyTarget;
import handlers.targethandlers.Pet;
import handlers.targethandlers.Self;
import handlers.targethandlers.Siege;
import handlers.targethandlers.Summon;
import handlers.targethandlers.TargetParty;
import handlers.targethandlers.Unlockable;
import handlers.telnethandlers.ChatsHandler;
import handlers.telnethandlers.DebugHandler;
import handlers.telnethandlers.HelpHandler;
import handlers.telnethandlers.PlayerHandler;
import handlers.telnethandlers.ReloadHandler;
import handlers.telnethandlers.ServerHandler;
import handlers.telnethandlers.StatusHandler;
import handlers.telnethandlers.ThreadHandler;
import handlers.usercommandhandlers.ChannelDelete;
import handlers.usercommandhandlers.ChannelInfo;
import handlers.usercommandhandlers.ChannelLeave;
import handlers.usercommandhandlers.ClanPenalty;
import handlers.usercommandhandlers.ClanWarsList;
import handlers.usercommandhandlers.Dismount;
import handlers.usercommandhandlers.InstanceZone;
import handlers.usercommandhandlers.Loc;
import handlers.usercommandhandlers.Mount;
import handlers.usercommandhandlers.MyBirthday;
import handlers.usercommandhandlers.OlympiadStat;
import handlers.usercommandhandlers.PartyInfo;
import handlers.usercommandhandlers.SiegeStatus;
import handlers.usercommandhandlers.Time;
import handlers.usercommandhandlers.Unstuck;
import handlers.voicedcommandhandlers.AioItemVCmd;
import handlers.voicedcommandhandlers.Antibot;
import handlers.voicedcommandhandlers.Banking;
import handlers.voicedcommandhandlers.CcpVCmd;
import handlers.voicedcommandhandlers.ChangePassword;
import handlers.voicedcommandhandlers.ChatAdmin;
import handlers.voicedcommandhandlers.Debug;
import handlers.voicedcommandhandlers.EvenlyDistributeItems;
import handlers.voicedcommandhandlers.Hellbound;
import handlers.voicedcommandhandlers.ItemBufferVCmd;
import handlers.voicedcommandhandlers.Lang;
import handlers.voicedcommandhandlers.OnlineVCmd;
import handlers.voicedcommandhandlers.PingVCmd;
import handlers.voicedcommandhandlers.PremiumVCmd;
import handlers.voicedcommandhandlers.PvpZoneVCmd;
import handlers.voicedcommandhandlers.RepairVCmd;
import handlers.voicedcommandhandlers.TeleportsVCmd;
import handlers.voicedcommandhandlers.Wedding;

/**
 * Master handler.
 * @author vGodFather
 */
public class MasterHandler
{
	private static final Logger _log = LoggerFactory.getLogger(MasterHandler.class);
	
	private static final Class<?>[] ACTION_HANDLERS =
	{
		// Action Handlers
		L2ArtefactInstanceAction.class,
		L2DecoyAction.class,
		L2DoorInstanceAction.class,
		L2ItemInstanceAction.class,
		L2NpcAction.class,
		L2PcInstanceAction.class,
		L2PetInstanceAction.class,
		L2StaticObjectInstanceAction.class,
		L2SummonAction.class,
		L2TrapAction.class,
	};
	
	private static final Class<?>[] ACTION_SHIFT_HANDLERS =
	{
		// Action Shift Handlers
		L2DoorInstanceActionShift.class,
		L2ItemInstanceActionShift.class,
		L2NpcActionShift.class,
		L2PcInstanceActionShift.class,
		L2StaticObjectInstanceActionShift.class,
		L2SummonActionShift.class,
	};
	
	private static final Class<?>[] ADMIN_HANDLERS =
	{
		// Admin Command Handlers
		AdminAdmin.class,
		AdminAnnouncements.class,
		AdminBBS.class,
		AdminBuffs.class,
		AdminCamera.class,
		AdminChangeAccessLevel.class,
		AdminCheckBots.class,
		AdminCHSiege.class,
		AdminClan.class,
		AdminCreateItem.class,
		AdminCursedWeapons.class,
		AdminCustomCreateItem.class,
		AdminDebug.class,
		AdminDelete.class,
		AdminDisconnect.class,
		AdminDoorControl.class,
		AdminEditChar.class,
		AdminEditNpc.class,
		AdminEffects.class,
		AdminElement.class,
		AdminEnchant.class,
		AdminExpSp.class,
		AdminFightCalculator.class,
		AdminFortSiege.class,
		AdminGamePoints.class,
		AdminGeodata.class,
		AdminGm.class,
		AdminGmChat.class,
		AdminGraciaSeeds.class,
		AdminGrandBoss.class,
		AdminHeal.class,
		AdminHellbound.class,
		AdminHtml.class,
		AdminHWIDBan.class,
		AdminInstance.class,
		AdminInstanceZone.class,
		AdminInventory.class,
		AdminInvul.class,
		AdminKick.class,
		AdminKill.class,
		AdminLevel.class,
		AdminLogin.class,
		AdminMammon.class,
		AdminManor.class,
		AdminMenu.class,
		AdminMessages.class,
		AdminMobGroup.class,
		AdminMonsterRace.class,
		AdminOlympiad.class,
		AdminPathNode.class,
		AdminPcCondOverride.class,
		AdminPetition.class,
		AdminPForge.class,
		AdminPledge.class,
		AdminPolymorph.class,
		AdminPremium.class,
		AdminPunishment.class,
		AdminQuest.class,
		AdminReload.class,
		AdminRepairChar.class,
		AdminRes.class,
		AdminRide.class,
		AdminScan.class,
		AdminShop.class,
		AdminShowQuests.class,
		AdminShutdown.class,
		AdminSiege.class,
		AdminSkill.class,
		AdminSpawn.class,
		AdminSummon.class,
		AdminTarget.class,
		AdminTargetSay.class,
		AdminTeleport.class,
		AdminTerritoryWar.class,
		AdminTest.class,
		AdminUnblockIp.class,
		AdminVitality.class,
		AdminZone.class,
		AdminLogsViewer.class,
	};
	
	private static final Class<?>[] BYPASS_HANDLERS =
	{
		// Bypass Handlers
		ArenaBuff.class,
		Augment.class,
		Buy.class,
		BuyShadowItem.class,
		ChatLink.class,
		ClanWarehouse.class,
		ElcardiaBuff.class,
		Festival.class,
		Freight.class,
		ItemAuctionLink.class,
		Link.class,
		Loto.class,
		Multisell.class,
		Observation.class,
		OlympiadManagerLink.class,
		OlympiadObservation.class,
		PlayerHelp.class,
		PrivateWarehouse.class,
		QuestLink.class,
		QuestList.class,
		ReleaseAttribute.class,
		RemoveDeathPenalty.class,
		RentPet.class,
		Rift.class,
		SkillList.class,
		SupportBlessing.class,
		SupportMagic.class,
		TerritoryStatus.class,
		VoiceCommand.class,
		Wear.class,
	};
	
	private static final Class<?>[] CHAT_HANDLERS =
	{
		// Chat Handlers
		ChatAll.class,
		ChatAlliance.class,
		ChatBattlefield.class,
		ChatClan.class,
		ChatHeroVoice.class,
		ChatParty.class,
		ChatPartyMatchRoom.class,
		ChatPartyRoomAll.class,
		ChatPartyRoomCommander.class,
		ChatPetition.class,
		ChatShout.class,
		ChatTell.class,
		ChatTrade.class,
	};
	
	private static final Class<?>[] ITEM_HANDLERS =
	{
		// Item Handlers
		(BufferConfigs.ENABLE_ITEM_BUFFER ? AioItemBuff.class : null),
		(AioItemsConfigs.ENABLE_AIO_NPCS ? AioItemNpcs.class : null),
		BeastSoulShot.class,
		BeastSpiritShot.class,
		BlessedSpiritShot.class,
		Book.class,
		Bypass.class,
		Calculator.class,
		CharmOfCourage.class,
		ChristmasTree.class,
		Disguise.class,
		Elixir.class,
		EnchantAttribute.class,
		EnchantScrolls.class,
		EventItem.class,
		ExtractableItems.class,
		FishShots.class,
		Harvester.class,
		ItemSkills.class,
		ItemSkillsTemplate.class,
		ManaPotion.class,
		Maps.class,
		MercTicket.class,
		NicknameColor.class,
		PetFood.class,
		Recipes.class,
		RollingDice.class,
		Seed.class,
		SevenSignsRecord.class,
		SoulShots.class,
		SpecialXMas.class,
		SpiritShot.class,
		SummonItems.class,
		TeleportBookmark.class,
	};
	
	private static final Class<?>[] PUNISHMENT_HANDLERS =
	{
		// Punishment Handlers
		BanHandler.class,
		ChatBanHandler.class,
		JailHandler.class,
	};
	
	private static final Class<?>[] SKILL_HANDLERS =
	{
		// Skill Handlers
		Blow.class,
		ChainHeal.class,
		Continuous.class,
		Disablers.class,
		Dummy.class,
		Mdam.class,
		Pdam.class,
		Unlock.class,
	};
	
	private static final Class<?>[] USER_COMMAND_HANDLERS =
	{
		// User Command Handlers
		ChannelDelete.class,
		ChannelInfo.class,
		ChannelLeave.class,
		ClanPenalty.class,
		ClanWarsList.class,
		Dismount.class,
		InstanceZone.class,
		Loc.class,
		Mount.class,
		MyBirthday.class,
		OlympiadStat.class,
		PartyInfo.class,
		SiegeStatus.class,
		Time.class,
		Unstuck.class,
	};
	
	private static final Class<?>[] TARGET_HANDLERS =
	{
		// Target Handlers
		Area.class,
		AreaCorpseMob.class,
		AreaFriendly.class,
		AreaSummon.class,
		Aura.class,
		AuraCorpseMob.class,
		AuraFriendly.class,
		AuraUndeadEnemy.class,
		BehindArea.class,
		BehindAura.class,
		Clan.class,
		ClanMember.class,
		CommandChannel.class,
		CorpseClan.class,
		CorpseMob.class,
		CorpsePet.class,
		CorpsePlayer.class,
		EnemySummon.class,
		FlagPole.class,
		FrontArea.class,
		FrontAura.class,
		Ground.class,
		Holy.class,
		One.class,
		OwnerPet.class,
		Party.class,
		PartyClan.class,
		PartyMember.class,
		PartyNotMe.class,
		PartyOther.class,
		PartyTarget.class,
		Pet.class,
		Self.class,
		Siege.class,
		Summon.class,
		TargetParty.class,
		Unlockable.class,
	};
	
	private static final Class<?>[] TELNET_HANDLERS =
	{
		// Telnet Handlers
		ChatsHandler.class,
		DebugHandler.class,
		HelpHandler.class,
		PlayerHandler.class,
		ReloadHandler.class,
		ServerHandler.class,
		StatusHandler.class,
		ThreadHandler.class,
	};
	
	private static final Class<?>[] PLAYER_ACTION_HANDLERS_ =
	{
		// Action Handlers
		AirshipAction.class,
		BotReport.class,
		PetAttack.class,
		PetHold.class,
		PetMove.class,
		PetSkillUse.class,
		PetStop.class,
		PrivateStore.class,
		Ride.class,
		RunWalk.class,
		ServitorAttack.class,
		ServitorHold.class,
		ServitorMove.class,
		ServitorSkillUse.class,
		ServitorStop.class,
		SitStand.class,
		SocialAction.class,
		UnsummonPet.class,
		UnsummonServitor.class,
	};
	
	private static final Class<?>[] VOICED_COMMAND_HANDLERS =
	{
		// Voiced Command Handlers
		(AioItemsConfigs.ALLOW_AIO_ITEM_COMMAND && AioItemsConfigs.ENABLE_AIO_NPCS ? AioItemVCmd.class : null),
		(AntibotConfigs.ENABLE_ANTIBOT_SYSTEMS ? Antibot.class : null),
		(Config.BANKING_SYSTEM_ENABLED ? Banking.class : null),
		(CustomServerConfigs.ENABLE_CHARACTER_CONTROL_PANEL ? CcpVCmd.class : null),
		(Config.L2JMOD_ALLOW_CHANGE_PASSWORD ? ChangePassword.class : null),
		(Config.L2JMOD_CHAT_ADMIN ? ChatAdmin.class : null),
		(Config.L2JMOD_DEBUG_VOICE_COMMAND ? Debug.class : null),
		(CustomServerConfigs.EVENLY_DISTRIBUTED_ITEMS ? EvenlyDistributeItems.class : null),
		(Config.L2JMOD_HELLBOUND_STATUS ? Hellbound.class : null),
		(BufferConfigs.ENABLE_ITEM_BUFFER && PremiumServiceConfigs.USE_PREMIUM_SERVICE ? ItemBufferVCmd.class : null),
		(Config.L2JMOD_MULTILANG_ENABLE && Config.L2JMOD_MULTILANG_VOICED_ALLOW ? Lang.class : null),
		(CustomServerConfigs.ALLOW_ONLINE_COMMAND ? OnlineVCmd.class : null),
		(PremiumServiceConfigs.USE_PREMIUM_SERVICE ? PremiumVCmd.class : null),
		(ChaoticZoneConfigs.ENABLE_CHAOTIC_ZONE ? PvpZoneVCmd.class : null),
		(CustomServerConfigs.ALLOW_REPAIR_COMMAND ? RepairVCmd.class : null),
		(CustomServerConfigs.ALLOW_TELEPORTS_COMMAND ? TeleportsVCmd.class : null),
		PingVCmd.class,
		(Config.L2JMOD_ALLOW_WEDDING ? Wedding.class : null),
		(GetRewardVoteSystemConfigs.ENABLE_VOTE_SYSTEM ? RewardVote.class : null),
	};
	
	private void loadHandlers(IHandler<?, ?> handler, Class<?>[] classes)
	{
		for (Class<?> c : classes)
		{
			if (c == null)
			{
				continue;
			}
			
			try
			{
				handler.registerByClass(c);
			}
			catch (Exception ex)
			{
				_log.error("Failed loading handler {}!", c.getSimpleName(), ex);
			}
		}
		
		_log.info("{}: Loaded {} handlers.", handler.getClass().getSimpleName(), handler.size());
	}
	
	public MasterHandler()
	{
		final long startCache = System.currentTimeMillis();
		loadHandlers(VoicedCommandHandler.getInstance(), VOICED_COMMAND_HANDLERS);
		loadHandlers(ActionHandler.getInstance(), ACTION_HANDLERS);
		loadHandlers(ActionShiftHandler.getInstance(), ACTION_SHIFT_HANDLERS);
		loadHandlers(SkillHandler.getInstance(), SKILL_HANDLERS);
		loadHandlers(PlayerActionHandler.getInstance(), PLAYER_ACTION_HANDLERS_);
		loadHandlers(AdminCommandHandler.getInstance(), ADMIN_HANDLERS);
		loadHandlers(BypassHandler.getInstance(), BYPASS_HANDLERS);
		loadHandlers(ChatHandler.getInstance(), CHAT_HANDLERS);
		loadHandlers(ItemHandler.getInstance(), ITEM_HANDLERS);
		loadHandlers(PunishmentHandler.getInstance(), PUNISHMENT_HANDLERS);
		loadHandlers(UserCommandHandler.getInstance(), USER_COMMAND_HANDLERS);
		loadHandlers(TargetHandler.getInstance(), TARGET_HANDLERS);
		loadHandlers(TelnetHandler.getInstance(), TELNET_HANDLERS);
		_log.info(MasterHandler.class.getSimpleName() + " loaded. (GenTime: {} ms) ", (System.currentTimeMillis() - startCache));
	}
	
	public static void main(String[] args)
	{
		new MasterHandler();
	}
}