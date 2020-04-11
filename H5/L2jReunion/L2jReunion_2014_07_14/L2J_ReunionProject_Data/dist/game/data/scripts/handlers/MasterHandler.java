/*
 * Copyright (C) 2004-2013 L2J DataPack
 *
 * This file is part of L2J DataPack.
 *
 * L2J DataPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * L2J DataPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package handlers;

import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import l2r.Config;
import l2r.gameserver.handler.ActionHandler;
import l2r.gameserver.handler.ActionShiftHandler;
import l2r.gameserver.handler.AdminCommandHandler;
import l2r.gameserver.handler.BypassHandler;
import l2r.gameserver.handler.ChatHandler;
import l2r.gameserver.handler.ItemHandler;
import l2r.gameserver.handler.PunishmentHandler;
import l2r.gameserver.handler.SkillHandler;
import l2r.gameserver.handler.TargetHandler;
import l2r.gameserver.handler.TelnetHandler;
import l2r.gameserver.handler.UserCommandHandler;
import l2r.gameserver.handler.VoicedCommandHandler;
import l2r.gameserver.scripts.handlers.actionhandlers.L2ArtefactInstanceAction;
import l2r.gameserver.scripts.handlers.actionhandlers.L2DecoyAction;
import l2r.gameserver.scripts.handlers.actionhandlers.L2DoorInstanceAction;
import l2r.gameserver.scripts.handlers.actionhandlers.L2DoorInstanceActionShift;
import l2r.gameserver.scripts.handlers.actionhandlers.L2ItemInstanceAction;
import l2r.gameserver.scripts.handlers.actionhandlers.L2ItemInstanceActionShift;
import l2r.gameserver.scripts.handlers.actionhandlers.L2NpcAction;
import l2r.gameserver.scripts.handlers.actionhandlers.L2NpcActionShift;
import l2r.gameserver.scripts.handlers.actionhandlers.L2PcInstanceAction;
import l2r.gameserver.scripts.handlers.actionhandlers.L2PcInstanceActionShift;
import l2r.gameserver.scripts.handlers.actionhandlers.L2PetInstanceAction;
import l2r.gameserver.scripts.handlers.actionhandlers.L2StaticObjectInstanceAction;
import l2r.gameserver.scripts.handlers.actionhandlers.L2StaticObjectInstanceActionShift;
import l2r.gameserver.scripts.handlers.actionhandlers.L2SummonAction;
import l2r.gameserver.scripts.handlers.actionhandlers.L2SummonActionShift;
import l2r.gameserver.scripts.handlers.actionhandlers.L2TrapAction;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminAdmin;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminAnnouncements;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminBBS;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminBuffs;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminCHSiege;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminCamera;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminChangeAccessLevel;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminCheckBots;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminClan;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminCreateItem;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminCursedWeapons;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminCustomCreateItem;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminDebug;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminDelete;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminDisconnect;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminDoorControl;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminEditChar;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminEditNpc;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminEffects;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminElement;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminEnchant;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminExpSp;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminFightCalculator;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminFortSiege;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminGeoEditor;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminGeodata;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminGm;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminGmChat;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminGraciaSeeds;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminHeal;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminHellbound;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminHtml;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminInstance;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminInstanceZone;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminInvul;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminKick;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminKill;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminLevel;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminLogin;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminMammon;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminManor;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminMenu;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminMessages;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminMobGroup;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminMonsterRace;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminPForge;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminPathNode;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminPcCondOverride;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminPetition;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminPledge;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminPolymorph;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminPremium;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminPunishment;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminQuest;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminReload;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminRepairChar;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminRes;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminRide;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminScan;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminShop;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminShowQuests;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminShutdown;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminSiege;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminSkill;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminSpawn;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminSummon;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminTarget;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminTargetSay;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminTeleport;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminTerritoryWar;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminTest;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminUnblockIp;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminVitality;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminZone;
import l2r.gameserver.scripts.handlers.bypasshandlers.ArenaBuff;
import l2r.gameserver.scripts.handlers.bypasshandlers.Augment;
import l2r.gameserver.scripts.handlers.bypasshandlers.Buy;
import l2r.gameserver.scripts.handlers.bypasshandlers.BuyShadowItem;
import l2r.gameserver.scripts.handlers.bypasshandlers.ChatLink;
import l2r.gameserver.scripts.handlers.bypasshandlers.ClanWarehouse;
import l2r.gameserver.scripts.handlers.bypasshandlers.Festival;
import l2r.gameserver.scripts.handlers.bypasshandlers.Freight;
import l2r.gameserver.scripts.handlers.bypasshandlers.ItemAuctionLink;
import l2r.gameserver.scripts.handlers.bypasshandlers.Link;
import l2r.gameserver.scripts.handlers.bypasshandlers.Loto;
import l2r.gameserver.scripts.handlers.bypasshandlers.ManorManager;
import l2r.gameserver.scripts.handlers.bypasshandlers.Multisell;
import l2r.gameserver.scripts.handlers.bypasshandlers.Observation;
import l2r.gameserver.scripts.handlers.bypasshandlers.OlympiadManagerLink;
import l2r.gameserver.scripts.handlers.bypasshandlers.OlympiadObservation;
import l2r.gameserver.scripts.handlers.bypasshandlers.PlayerHelp;
import l2r.gameserver.scripts.handlers.bypasshandlers.PrivateWarehouse;
import l2r.gameserver.scripts.handlers.bypasshandlers.QuestLink;
import l2r.gameserver.scripts.handlers.bypasshandlers.QuestList;
import l2r.gameserver.scripts.handlers.bypasshandlers.ReceivePremium;
import l2r.gameserver.scripts.handlers.bypasshandlers.ReleaseAttribute;
import l2r.gameserver.scripts.handlers.bypasshandlers.RemoveDeathPenalty;
import l2r.gameserver.scripts.handlers.bypasshandlers.RentPet;
import l2r.gameserver.scripts.handlers.bypasshandlers.Rift;
import l2r.gameserver.scripts.handlers.bypasshandlers.SkillList;
import l2r.gameserver.scripts.handlers.bypasshandlers.SupportBlessing;
import l2r.gameserver.scripts.handlers.bypasshandlers.SupportMagic;
import l2r.gameserver.scripts.handlers.bypasshandlers.TerritoryStatus;
import l2r.gameserver.scripts.handlers.bypasshandlers.VoiceCommand;
import l2r.gameserver.scripts.handlers.bypasshandlers.Wear;
import l2r.gameserver.scripts.handlers.chathandlers.ChatAll;
import l2r.gameserver.scripts.handlers.chathandlers.ChatAlliance;
import l2r.gameserver.scripts.handlers.chathandlers.ChatBattlefield;
import l2r.gameserver.scripts.handlers.chathandlers.ChatClan;
import l2r.gameserver.scripts.handlers.chathandlers.ChatHeroVoice;
import l2r.gameserver.scripts.handlers.chathandlers.ChatParty;
import l2r.gameserver.scripts.handlers.chathandlers.ChatPartyMatchRoom;
import l2r.gameserver.scripts.handlers.chathandlers.ChatPartyRoomAll;
import l2r.gameserver.scripts.handlers.chathandlers.ChatPartyRoomCommander;
import l2r.gameserver.scripts.handlers.chathandlers.ChatPetition;
import l2r.gameserver.scripts.handlers.chathandlers.ChatShout;
import l2r.gameserver.scripts.handlers.chathandlers.ChatTell;
import l2r.gameserver.scripts.handlers.chathandlers.ChatTrade;
import l2r.gameserver.scripts.handlers.itemhandlers.AioItemBuff;
import l2r.gameserver.scripts.handlers.itemhandlers.AioItemNpcs;
import l2r.gameserver.scripts.handlers.itemhandlers.BeastSoulShot;
import l2r.gameserver.scripts.handlers.itemhandlers.BeastSpice;
import l2r.gameserver.scripts.handlers.itemhandlers.BeastSpiritShot;
import l2r.gameserver.scripts.handlers.itemhandlers.BlessedSpiritShot;
import l2r.gameserver.scripts.handlers.itemhandlers.Book;
import l2r.gameserver.scripts.handlers.itemhandlers.Bypass;
import l2r.gameserver.scripts.handlers.itemhandlers.Calculator;
import l2r.gameserver.scripts.handlers.itemhandlers.Disguise;
import l2r.gameserver.scripts.handlers.itemhandlers.Elixir;
import l2r.gameserver.scripts.handlers.itemhandlers.EnchantAttribute;
import l2r.gameserver.scripts.handlers.itemhandlers.EnchantScrolls;
import l2r.gameserver.scripts.handlers.itemhandlers.EnergyStarStone;
import l2r.gameserver.scripts.handlers.itemhandlers.EventItem;
import l2r.gameserver.scripts.handlers.itemhandlers.ExtractableItems;
import l2r.gameserver.scripts.handlers.itemhandlers.FishShots;
import l2r.gameserver.scripts.handlers.itemhandlers.Harvester;
import l2r.gameserver.scripts.handlers.itemhandlers.ItemSkills;
import l2r.gameserver.scripts.handlers.itemhandlers.ItemSkillsTemplate;
import l2r.gameserver.scripts.handlers.itemhandlers.ManaPotion;
import l2r.gameserver.scripts.handlers.itemhandlers.Maps;
import l2r.gameserver.scripts.handlers.itemhandlers.MercTicket;
import l2r.gameserver.scripts.handlers.itemhandlers.NicknameColor;
import l2r.gameserver.scripts.handlers.itemhandlers.PaganKeys;
import l2r.gameserver.scripts.handlers.itemhandlers.PetFood;
import l2r.gameserver.scripts.handlers.itemhandlers.Recipes;
import l2r.gameserver.scripts.handlers.itemhandlers.RollingDice;
import l2r.gameserver.scripts.handlers.itemhandlers.ScrollOfResurrection;
import l2r.gameserver.scripts.handlers.itemhandlers.Seed;
import l2r.gameserver.scripts.handlers.itemhandlers.SevenSignsRecord;
import l2r.gameserver.scripts.handlers.itemhandlers.SoulShots;
import l2r.gameserver.scripts.handlers.itemhandlers.SpecialXMas;
import l2r.gameserver.scripts.handlers.itemhandlers.SpiritShot;
import l2r.gameserver.scripts.handlers.itemhandlers.SummonItems;
import l2r.gameserver.scripts.handlers.itemhandlers.TeleportBookmark;
import l2r.gameserver.scripts.handlers.punishmenthandlers.BanHandler;
import l2r.gameserver.scripts.handlers.punishmenthandlers.ChatBanHandler;
import l2r.gameserver.scripts.handlers.punishmenthandlers.JailHandler;
import l2r.gameserver.scripts.handlers.skillhandlers.Blow;
import l2r.gameserver.scripts.handlers.skillhandlers.Continuous;
import l2r.gameserver.scripts.handlers.skillhandlers.Detection;
import l2r.gameserver.scripts.handlers.skillhandlers.Disablers;
import l2r.gameserver.scripts.handlers.skillhandlers.Dummy;
import l2r.gameserver.scripts.handlers.skillhandlers.Fishing;
import l2r.gameserver.scripts.handlers.skillhandlers.FishingSkill;
import l2r.gameserver.scripts.handlers.skillhandlers.GiveReco;
import l2r.gameserver.scripts.handlers.skillhandlers.InstantJump;
import l2r.gameserver.scripts.handlers.skillhandlers.Manadam;
import l2r.gameserver.scripts.handlers.skillhandlers.Mdam;
import l2r.gameserver.scripts.handlers.skillhandlers.NornilsPower;
import l2r.gameserver.scripts.handlers.skillhandlers.Pdam;
import l2r.gameserver.scripts.handlers.skillhandlers.Resurrect;
import l2r.gameserver.scripts.handlers.skillhandlers.ShiftTarget;
import l2r.gameserver.scripts.handlers.skillhandlers.Sow;
import l2r.gameserver.scripts.handlers.skillhandlers.TransformDispel;
import l2r.gameserver.scripts.handlers.skillhandlers.Trap;
import l2r.gameserver.scripts.handlers.skillhandlers.Unlock;
import l2r.gameserver.scripts.handlers.targethandlers.Area;
import l2r.gameserver.scripts.handlers.targethandlers.AreaCorpseMob;
import l2r.gameserver.scripts.handlers.targethandlers.AreaFriendly;
import l2r.gameserver.scripts.handlers.targethandlers.AreaSummon;
import l2r.gameserver.scripts.handlers.targethandlers.Aura;
import l2r.gameserver.scripts.handlers.targethandlers.AuraCorpseMob;
import l2r.gameserver.scripts.handlers.targethandlers.BehindArea;
import l2r.gameserver.scripts.handlers.targethandlers.BehindAura;
import l2r.gameserver.scripts.handlers.targethandlers.Clan;
import l2r.gameserver.scripts.handlers.targethandlers.ClanMember;
import l2r.gameserver.scripts.handlers.targethandlers.CorpseClan;
import l2r.gameserver.scripts.handlers.targethandlers.CorpseMob;
import l2r.gameserver.scripts.handlers.targethandlers.CorpsePet;
import l2r.gameserver.scripts.handlers.targethandlers.CorpsePlayer;
import l2r.gameserver.scripts.handlers.targethandlers.EnemySummon;
import l2r.gameserver.scripts.handlers.targethandlers.FlagPole;
import l2r.gameserver.scripts.handlers.targethandlers.FrontArea;
import l2r.gameserver.scripts.handlers.targethandlers.FrontAura;
import l2r.gameserver.scripts.handlers.targethandlers.Ground;
import l2r.gameserver.scripts.handlers.targethandlers.Holy;
import l2r.gameserver.scripts.handlers.targethandlers.One;
import l2r.gameserver.scripts.handlers.targethandlers.OwnerPet;
import l2r.gameserver.scripts.handlers.targethandlers.Party;
import l2r.gameserver.scripts.handlers.targethandlers.PartyClan;
import l2r.gameserver.scripts.handlers.targethandlers.PartyMember;
import l2r.gameserver.scripts.handlers.targethandlers.PartyNotMe;
import l2r.gameserver.scripts.handlers.targethandlers.PartyOther;
import l2r.gameserver.scripts.handlers.targethandlers.Pet;
import l2r.gameserver.scripts.handlers.targethandlers.Self;
import l2r.gameserver.scripts.handlers.targethandlers.Summon;
import l2r.gameserver.scripts.handlers.targethandlers.Unlockable;
import l2r.gameserver.scripts.handlers.telnethandlers.ChatsHandler;
import l2r.gameserver.scripts.handlers.telnethandlers.DebugHandler;
import l2r.gameserver.scripts.handlers.telnethandlers.HelpHandler;
import l2r.gameserver.scripts.handlers.telnethandlers.PlayerHandler;
import l2r.gameserver.scripts.handlers.telnethandlers.ReloadHandler;
import l2r.gameserver.scripts.handlers.telnethandlers.ServerHandler;
import l2r.gameserver.scripts.handlers.telnethandlers.StatusHandler;
import l2r.gameserver.scripts.handlers.telnethandlers.ThreadHandler;
import l2r.gameserver.scripts.handlers.usercommandhandlers.ChannelDelete;
import l2r.gameserver.scripts.handlers.usercommandhandlers.ChannelInfo;
import l2r.gameserver.scripts.handlers.usercommandhandlers.ChannelLeave;
import l2r.gameserver.scripts.handlers.usercommandhandlers.ClanPenalty;
import l2r.gameserver.scripts.handlers.usercommandhandlers.ClanWarsList;
import l2r.gameserver.scripts.handlers.usercommandhandlers.Dismount;
import l2r.gameserver.scripts.handlers.usercommandhandlers.InstanceZone;
import l2r.gameserver.scripts.handlers.usercommandhandlers.Loc;
import l2r.gameserver.scripts.handlers.usercommandhandlers.Mount;
import l2r.gameserver.scripts.handlers.usercommandhandlers.MyBirthday;
import l2r.gameserver.scripts.handlers.usercommandhandlers.OlympiadStat;
import l2r.gameserver.scripts.handlers.usercommandhandlers.PartyInfo;
import l2r.gameserver.scripts.handlers.usercommandhandlers.SiegeStatus;
import l2r.gameserver.scripts.handlers.usercommandhandlers.Time;
import l2r.gameserver.scripts.handlers.usercommandhandlers.Unstuck;
import l2r.gameserver.scripts.handlers.voicedcommandhandlers.AioItemVCmd;
import l2r.gameserver.scripts.handlers.voicedcommandhandlers.Antibot;
import l2r.gameserver.scripts.handlers.voicedcommandhandlers.Banking;
import l2r.gameserver.scripts.handlers.voicedcommandhandlers.CcpVCmd;
import l2r.gameserver.scripts.handlers.voicedcommandhandlers.ChangePassword;
import l2r.gameserver.scripts.handlers.voicedcommandhandlers.ChatAdmin;
import l2r.gameserver.scripts.handlers.voicedcommandhandlers.Debug;
import l2r.gameserver.scripts.handlers.voicedcommandhandlers.EvenlyDistributeItems;
import l2r.gameserver.scripts.handlers.voicedcommandhandlers.Hellbound;
import l2r.gameserver.scripts.handlers.voicedcommandhandlers.ItemBufferVCmd;
import l2r.gameserver.scripts.handlers.voicedcommandhandlers.Lang;
import l2r.gameserver.scripts.handlers.voicedcommandhandlers.OnlineVCmd;
import l2r.gameserver.scripts.handlers.voicedcommandhandlers.PremiumVCmd;
import l2r.gameserver.scripts.handlers.voicedcommandhandlers.PvpZoneVCmd;
import l2r.gameserver.scripts.handlers.voicedcommandhandlers.RepairVCmd;
import l2r.gameserver.scripts.handlers.voicedcommandhandlers.TeleportsVCmd;
import l2r.gameserver.scripts.handlers.voicedcommandhandlers.VotePanelVCmd;
import l2r.gameserver.scripts.handlers.voicedcommandhandlers.VoteVCmd;
import l2r.gameserver.scripts.handlers.voicedcommandhandlers.Wedding;
import gr.reunion.configsEngine.AioBufferConfigs;
import gr.reunion.configsEngine.AioItemsConfigs;
import gr.reunion.configsEngine.AntibotConfigs;
import gr.reunion.configsEngine.ChaoticZoneConfigs;
import gr.reunion.configsEngine.CustomServerConfigs;
import gr.reunion.configsEngine.GetRewardVoteSystemConfigs;
import gr.reunion.configsEngine.IndividualVoteSystemConfigs;
import gr.reunion.configsEngine.PremiumServiceConfigs;
import gr.reunion.voteEngine.RewardVote;

/**
 * Master handler.
 * @author UnAfraid
 */
public class MasterHandler
{
	private static final Logger _log = Logger.getLogger(MasterHandler.class.getName());
	
	private static final Class<?>[] _loadInstances =
	{
		ActionHandler.class,
		ActionShiftHandler.class,
		AdminCommandHandler.class,
		BypassHandler.class,
		ChatHandler.class,
		ItemHandler.class,
		PunishmentHandler.class,
		SkillHandler.class,
		UserCommandHandler.class,
		VoicedCommandHandler.class,
		TargetHandler.class,
		TelnetHandler.class,
	};
	
	private static final Class<?>[][] _handlers =
	{
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
		},
		{
			// Action Shift Handlers
			L2DoorInstanceActionShift.class,
			L2ItemInstanceActionShift.class,
			L2NpcActionShift.class,
			L2PcInstanceActionShift.class,
			L2StaticObjectInstanceActionShift.class,
			L2SummonActionShift.class,
		},
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
			AdminPcCondOverride.class,
			AdminCreateItem.class,
			AdminCursedWeapons.class,
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
			AdminGeodata.class,
			AdminGeoEditor.class,
			AdminGm.class,
			AdminGmChat.class,
			AdminGraciaSeeds.class,
			AdminHeal.class,
			AdminHellbound.class,
			AdminHtml.class,
			AdminInstance.class,
			AdminInstanceZone.class,
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
			AdminPathNode.class,
			AdminPetition.class,
			AdminPForge.class,
			AdminPledge.class,
			AdminPolymorph.class,
			AdminPunishment.class,
			AdminPremium.class,
			AdminQuest.class,
			AdminReload.class,
			AdminRepairChar.class,
			AdminRes.class,
			AdminScan.class,
			AdminRide.class,
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
			AdminCustomCreateItem.class,
		},
		{
			// Bypass Handlers
			Augment.class,
			ArenaBuff.class,
			Buy.class,
			BuyShadowItem.class,
			ChatLink.class,
			ClanWarehouse.class,
			Festival.class,
			Freight.class,
			ItemAuctionLink.class,
			Link.class,
			Loto.class,
			ManorManager.class,
			Multisell.class,
			Observation.class,
			OlympiadObservation.class,
			OlympiadManagerLink.class,
			QuestLink.class,
			PlayerHelp.class,
			PrivateWarehouse.class,
			QuestList.class,
			ReceivePremium.class,
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
		},
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
		},
		{
			// Item Handlers
			ScrollOfResurrection.class,
			SoulShots.class,
			SpiritShot.class,
			BlessedSpiritShot.class,
			BeastSoulShot.class,
			BeastSpiritShot.class,
			Bypass.class,
			Calculator.class,
			PaganKeys.class,
			Maps.class,
			NicknameColor.class,
			Recipes.class,
			RollingDice.class,
			EnchantAttribute.class,
			EnchantScrolls.class,
			ExtractableItems.class,
			Book.class,
			SevenSignsRecord.class,
			ItemSkills.class,
			ItemSkillsTemplate.class,
			Seed.class,
			Harvester.class,
			MercTicket.class,
			FishShots.class,
			PetFood.class,
			SpecialXMas.class,
			SummonItems.class,
			BeastSpice.class,
			TeleportBookmark.class,
			Elixir.class,
			Disguise.class,
			ManaPotion.class,
			EnergyStarStone.class,
			EventItem.class,
			(AioBufferConfigs.ENABLE_AIO_BUFFER ? AioItemBuff.class : null),
			(AioItemsConfigs.ENABLE_AIO_NPCS ? AioItemNpcs.class : null),
		},
		{
			// Punishment Handlers
			BanHandler.class,
			ChatBanHandler.class,
			JailHandler.class,
		},
		{
			// Skill Handlers
			Blow.class,
			Continuous.class,
			Detection.class,
			Disablers.class,
			Dummy.class,
			Fishing.class,
			FishingSkill.class,
			GiveReco.class,
			InstantJump.class,
			Manadam.class,
			Mdam.class,
			NornilsPower.class,
			Pdam.class,
			Resurrect.class,
			ShiftTarget.class,
			Sow.class,
			TransformDispel.class,
			Trap.class,
			Unlock.class,
		},
		{
			// User Command Handlers
			ClanPenalty.class,
			ClanWarsList.class,
			Dismount.class,
			Unstuck.class,
			InstanceZone.class,
			Loc.class,
			Mount.class,
			PartyInfo.class,
			Time.class,
			OlympiadStat.class,
			ChannelLeave.class,
			ChannelDelete.class,
			ChannelInfo.class,
			MyBirthday.class,
			SiegeStatus.class,
		},
		{
			// Voiced Command Handlers
			// TODO: Add configuration options for this voiced commands:
			// CastleVCmd.class,
			// SetVCmd.class,
			VoteVCmd.class,
			(CustomServerConfigs.ENABLE_CHARACTER_CONTROL_PANEL ? CcpVCmd.class : null),
			(PremiumServiceConfigs.USE_PREMIUM_SERVICE ? PremiumVCmd.class : null),
			(AntibotConfigs.ENABLE_ANTIBOT_SYSTEMS ? Antibot.class : null),
			(ChaoticZoneConfigs.ENABLE_CHAOTIC_ZONE ? PvpZoneVCmd.class : null),
			(AioBufferConfigs.ENABLE_AIO_BUFFER && PremiumServiceConfigs.USE_PREMIUM_SERVICE ? ItemBufferVCmd.class : null),
			(IndividualVoteSystemConfigs.ENABLE_VOTE_SYSTEM ? VotePanelVCmd.class : null),
			(GetRewardVoteSystemConfigs.ENABLE_VOTE_SYSTEM ? RewardVote.class : null),
			(CustomServerConfigs.ALLOW_ONLINE_COMMAND ? OnlineVCmd.class : null),
			(CustomServerConfigs.ALLOW_REPAIR_COMMAND ? RepairVCmd.class : null),
			(CustomServerConfigs.ALLOW_TELEPORTS_COMMAND ? TeleportsVCmd.class : null),
			(CustomServerConfigs.EVENLY_DISTRIBUTED_ITEMS ? EvenlyDistributeItems.class : null),
			(AioItemsConfigs.ALLOW_AIO_ITEM_COMMAND && AioItemsConfigs.ENABLE_AIO_NPCS ? AioItemVCmd.class : null),
			(Config.L2JMOD_ALLOW_WEDDING ? Wedding.class : null),
			(Config.BANKING_SYSTEM_ENABLED ? Banking.class : null),
			(Config.L2JMOD_CHAT_ADMIN ? ChatAdmin.class : null),
			(Config.L2JMOD_MULTILANG_ENABLE && Config.L2JMOD_MULTILANG_VOICED_ALLOW ? Lang.class : null),
			(Config.L2JMOD_DEBUG_VOICE_COMMAND ? Debug.class : null),
			(Config.L2JMOD_ALLOW_CHANGE_PASSWORD ? ChangePassword.class : null),
			(Config.L2JMOD_HELLBOUND_STATUS ? Hellbound.class : null),
		},
		{
			// Target Handlers
			Area.class,
			AreaCorpseMob.class,
			AreaFriendly.class,
			AreaSummon.class,
			Aura.class,
			AuraCorpseMob.class,
			BehindArea.class,
			BehindAura.class,
			Clan.class,
			ClanMember.class,
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
			Pet.class,
			Self.class,
			Summon.class,
			Unlockable.class,
		},
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
		},
	};
	
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		_log.log(Level.INFO, "Loading Handlers...");
		
		Object loadInstance = null;
		Method method = null;
		Class<?>[] interfaces = null;
		Object handler = null;
		
		for (int i = 0; i < _loadInstances.length; i++)
		{
			try
			{
				method = _loadInstances[i].getMethod("getInstance");
				loadInstance = method.invoke(_loadInstances[i]);
			}
			catch (Exception e)
			{
				_log.log(Level.WARNING, "Failed invoking getInstance method for handler: " + _loadInstances[i].getSimpleName(), e);
				continue;
			}
			
			method = null;
			
			for (Class<?> c : _handlers[i])
			{
				if (c == null)
				{
					continue; // Disabled handler
				}
				
				try
				{
					// Don't wtf some classes extending another like ItemHandler, Elixir, etc.. and we need to find where the hell is interface xD
					interfaces = c.getInterfaces().length > 0 ? // Standardly handler has implementation
					c.getInterfaces() : c.getSuperclass().getInterfaces().length > 0 ? // No? then it extends another handler like (ItemSkills->ItemSkillsTemplate)
					c.getSuperclass().getInterfaces() : c.getSuperclass().getSuperclass().getInterfaces(); // O noh that's Elixir->ItemSkills->ItemSkillsTemplate
					if (method == null)
					{
						method = loadInstance.getClass().getMethod("registerHandler", interfaces);
					}
					handler = c.newInstance();
					if (method.getParameterTypes()[0].isInstance(handler))
					{
						method.invoke(loadInstance, handler);
					}
				}
				catch (Exception e)
				{
					_log.log(Level.WARNING, "Failed loading handler: " + c.getSimpleName(), e);
					continue;
				}
			}
			// And lets try get size
			try
			{
				method = loadInstance.getClass().getMethod("size");
				Object returnVal = method.invoke(loadInstance);
				_log.log(Level.INFO, loadInstance.getClass().getSimpleName() + ": Loaded " + returnVal + " Handlers");
			}
			catch (Exception e)
			{
				_log.log(Level.WARNING, "Failed invoking size method for handler: " + loadInstance.getClass().getSimpleName(), e);
				continue;
			}
		}
		
		_log.log(Level.INFO, "Handlers Loaded...");
	}
}