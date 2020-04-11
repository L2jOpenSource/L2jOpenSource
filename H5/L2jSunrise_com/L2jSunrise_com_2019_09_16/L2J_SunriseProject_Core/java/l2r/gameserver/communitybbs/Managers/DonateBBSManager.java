/*
 * Copyright (C) 2004-2015 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.communitybbs.Managers;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import l2r.gameserver.cache.HtmCache;
import l2r.gameserver.communitybbs.BoardsManager;
import l2r.gameserver.data.sql.CharNameTable;
import l2r.gameserver.data.sql.ClanTable;
import l2r.gameserver.data.xml.impl.MultisellData;
import l2r.gameserver.data.xml.impl.SkillData;
import l2r.gameserver.enums.ZoneIdType;
import l2r.gameserver.model.L2Augmentation;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.itemcontainer.Inventory;
import l2r.gameserver.model.items.instance.L2ItemInstance;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.ExShowScreenMessage;
import l2r.gameserver.network.serverpackets.InventoryUpdate;
import l2r.gameserver.network.serverpackets.MagicSkillUse;
import l2r.gameserver.network.serverpackets.PartySmallWindowAll;
import l2r.gameserver.network.serverpackets.PartySmallWindowDeleteAll;
import l2r.gameserver.network.serverpackets.PledgeInfo;
import l2r.gameserver.network.serverpackets.ShowBoard;
import l2r.gameserver.util.Util;

import gr.sr.configsEngine.configs.impl.CommunityDonateConfigs;
import gr.sr.configsEngine.configs.impl.CustomServerConfigs;
import gr.sr.premiumEngine.PremiumHandler;
import gr.sr.securityEngine.SecurityActions;
import gr.sr.securityEngine.SecurityType;

/**
 * @author L2jSunrise Team
 * @Website www.l2jsunrise.com
 */
public class DonateBBSManager extends BaseBBSManager
{
	public DonateBBSManager()
	{
		loadAugmentBaseStats();
		loadAugmentSecondaryStats();
	}
	
	public String _donationBBSCommand = CommunityDonateConfigs.BYPASS_COMMAND;
	
	private static String AUGMENT_BASE_STAT = "STR +1,24699;INT +1,24701;CON +1,24700;MEN +1,24702";
	private static String AUGMENT_SECONDARY_STAT = CommunityDonateConfigs.COMMUNITY_DONATE_AUGMENT_SKILL;
	public static Map<String, Integer> AUGMENT_BASE_STATS = new LinkedHashMap<>();
	public static Map<String, Integer> AUGMENT_SECONDARY_STATS = new LinkedHashMap<>();
	
	@Override
	public void cbByPass(String command, L2PcInstance activeChar)
	{
		if (!CommunityDonateConfigs.COMMUNITY_DONATE_ALLOW)
		{
			activeChar.sendMessage("This function is disabled by admin.");
			return;
		}
		
		if (command.equals(_donationBBSCommand + ""))
		{
			sendHtm(activeChar, "main", command);
		}
		// navigation handler
		else if (command.startsWith(_donationBBSCommand + "_nav"))
		{
			sendHtm(activeChar, commandSeperator(command, ";"), command);
		}
		// multisell handler
		else if (command.startsWith(_donationBBSCommand + "_multisell"))
		{
			sendMultisell(activeChar, commandSeperator(command, ";"));
		}
		// change name
		else if (command.startsWith(_donationBBSCommand + ";donatename"))
		{
			changeCharName(activeChar, command);
		}
		// change clan name
		else if (command.startsWith(_donationBBSCommand + ";donateClanName"))
		{
			changeClanName(activeChar, command);
		}
		// buy noble
		else if (command.startsWith(_donationBBSCommand + ";donateNoble"))
		{
			giveNoble(activeChar);
		}
		// donate full clan
		else if (command.startsWith(_donationBBSCommand + ";donateFullClan"))
		{
			getFulClan(activeChar);
		}
		// get fame
		else if (command.startsWith(_donationBBSCommand + ";donateFame"))
		{
			getFame(activeChar);
		}
		// get recommends
		else if (command.startsWith(_donationBBSCommand + ";donateRec"))
		{
			getRec(activeChar);
		}
		// become premium
		else if (command.startsWith(_donationBBSCommand + ";donatePremium"))
		{
			givePremium(activeChar, commandSeperator(command, ";"));
		}
		// add augment to weapon
		else if (command.startsWith(_donationBBSCommand + ";donateAugment"))
		{
			giveAugment(activeChar, commandSeperator(command, ";"));
		}
		else
		{
			separateAndSend("<html><body><br><br><center>Command : " + command + " needs core development</center><br><br></body></html>", activeChar);
		}
	}
	
	private void sendMultisell(L2PcInstance activeChar, String multisell)
	{
		if (!CommunityDonateConfigs.COMMUNITY_DONATE_SHOP_ALLOW)
		{
			activeChar.sendMessage("This function is disabled by admin.");
			sendHtm(activeChar, "main", _donationBBSCommand);
			return;
		}
		
		if (!CommunityDonateConfigs.COMMUNITY_DONATE_SHOP_NONPEACE && !activeChar.isInsideZone(ZoneIdType.PEACE))
		{
			activeChar.sendMessage("You cannot use this function outside peace zone.");
			sendHtm(activeChar, "main", _donationBBSCommand);
			return;
		}
		
		try
		{
			int multi = Integer.valueOf(multisell);
			
			if (CommunityDonateConfigs.MULTISELL_LIST.contains(multi))
			{
				activeChar.setIsUsingAioMultisell(true);
				sendHtm(activeChar, "main", _donationBBSCommand);
				MultisellData.getInstance().separateAndSend(multi, activeChar, null, false);
			}
			else
			{
				SecurityActions.startSecurity(activeChar, SecurityType.COMMUNITY_SYSTEM);
			}
		}
		catch (Exception e)
		{
			SecurityActions.startSecurity(activeChar, SecurityType.COMMUNITY_SYSTEM);
		}
		
	}
	
	private void giveAugment(L2PcInstance activeChar, String command)
	{
		if (!CommunityDonateConfigs.COMMUNITY_DONATE_AUGMENT_ALLOW)
		{
			activeChar.sendMessage("This function is disabled by admin.");
			sendHtm(activeChar, "augment", _donationBBSCommand + "_nav;augment");
			return;
		}
		
		if (!CommunityDonateConfigs.COMMUNITY_DONATE_AUGMENT_NONPEACE && !activeChar.isInsideZone(ZoneIdType.PEACE))
		{
			activeChar.sendMessage("You cannot use this function outside peace zone.");
			sendHtm(activeChar, "augment", _donationBBSCommand + "_nav;augment");
			return;
		}
		
		String temp_stat = "";
		String[] stats = command.split(" ");
		switch (stats.length)
		{
			case 4:
				temp_stat = stats[3];
				break;
			case 5:
				temp_stat = stats[3] + " " + stats[4];
				break;
			case 6:
				temp_stat = stats[3] + " " + stats[4] + " " + stats[5];
				break;
		}
		
		int baseStat = AUGMENT_BASE_STATS.get(stats[1] + " " + stats[2]);
		int secondarySkill = AUGMENT_SECONDARY_STATS.get(temp_stat) + 8358;
		
		L2ItemInstance parmorInstance = activeChar.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
		if ((parmorInstance == null) || !parmorInstance.isWeapon() || (parmorInstance.isHeroItem() && !CustomServerConfigs.ALT_ALLOW_REFINE_HERO_ITEM) || (parmorInstance.isCommonItem() || (parmorInstance.isEtcItem() || (parmorInstance.isTimeLimitedItem()))))
		{
			activeChar.sendPacket(SystemMessageId.THIS_IS_NOT_A_SUITABLE_ITEM);
			sendHtm(activeChar, "augment", _donationBBSCommand + "_nav;augment");
			return;
		}
		
		if (parmorInstance.isAugmented())
		{
			activeChar.sendPacket(SystemMessageId.ONCE_AN_ITEM_IS_AUGMENTED_IT_CANNOT_BE_AUGMENTED_AGAIN);
			sendHtm(activeChar, "augment", _donationBBSCommand + "_nav;augment");
			return;
		}
		
		if (!getPayment(activeChar, CommunityDonateConfigs.COMMUNITY_DONATE_AUGMENT_ID, CommunityDonateConfigs.COMMUNITY_DONATE_AUGMENT_PRICE))
		{
			sendHtm(activeChar, "augment", _donationBBSCommand + "_nav;augment");
			return;
		}
		
		// set augment skill
		activeChar.getInventory().unEquipItemInSlot(Inventory.PAPERDOLL_RHAND);
		parmorInstance.setAugmentation(new L2Augmentation(((secondarySkill << 16) + baseStat)));
		activeChar.getInventory().equipItem(parmorInstance);
		activeChar.sendPacket(SystemMessageId.THE_ITEM_WAS_SUCCESSFULLY_AUGMENTED);
		activeChar.sendPacket(new ExShowScreenMessage("You got " + stats[1] + " " + stats[2] + " and " + temp_stat + "", 5000));
		activeChar.broadcastPacket(new MagicSkillUse(activeChar, 6463, 1, 1000, 0));
		
		// send packets
		final InventoryUpdate iu = new InventoryUpdate();
		iu.addModifiedItem(parmorInstance);
		activeChar.sendInventoryUpdate(iu);
		activeChar.broadcastUserInfo();
		
		sendHtm(activeChar, "augment", _donationBBSCommand + "_nav;augment");
	}
	
	private void givePremium(L2PcInstance activeChar, String command)
	{
		int _premium_price = 0;
		switch (commandSeperator(command, " "))
		{
			case "1":
				_premium_price = CommunityDonateConfigs.COMMUNITY_DONATE_PREMIUM_PRICE_1_MONTH;
				break;
			case "2":
				_premium_price = CommunityDonateConfigs.COMMUNITY_DONATE_PREMIUM_PRICE_2_MONTH;
				break;
			case "3":
				_premium_price = CommunityDonateConfigs.COMMUNITY_DONATE_PREMIUM_PRICE_3_MONTH;
				break;
		}
		
		if (!CommunityDonateConfigs.COMMUNITY_DONATE_PREMIUM_ALLOW)
		{
			activeChar.sendMessage("This function is disabled by admin.");
			sendHtm(activeChar, "premium", _donationBBSCommand + "_nav;premium");
			return;
		}
		
		if (!CommunityDonateConfigs.COMMUNITY_DONATE_PREMIUM_ALLOW && !activeChar.isInsideZone(ZoneIdType.PEACE))
		{
			activeChar.sendMessage("You cannot use this function outside peace zone.");
			sendHtm(activeChar, "premium", _donationBBSCommand + "_nav;premium");
			return;
		}
		
		if (activeChar.isPremium())
		{
			activeChar.sendMessage("You are already premium user.");
			sendHtm(activeChar, "premium", _donationBBSCommand + "_nav;premium");
			return;
		}
		
		if (!getPayment(activeChar, CommunityDonateConfigs.COMMUNITY_DONATE_PREMIUM_ID, _premium_price))
		{
			sendHtm(activeChar, "premium", _donationBBSCommand + "_nav;premium");
			return;
		}
		
		PremiumHandler.addPremiumServices(Integer.parseInt(commandSeperator(command, " ")), activeChar);
		activeChar.sendMessage("Cogratulations, you are a premium user.");
		activeChar.sendPacket(new ExShowScreenMessage("Cogratulations, you are a premium user.", 5000));
		activeChar.broadcastPacket(new MagicSkillUse(activeChar, 6463, 1, 1000, 0));
		
		sendHtm(activeChar, "premium", _donationBBSCommand + "_nav;premium");
	}
	
	private void getRec(L2PcInstance activeChar)
	{
		if (!CommunityDonateConfigs.COMMUNITY_DONATE_REC_ALLOW)
		{
			activeChar.sendMessage("This function is disabled by admin.");
			sendHtm(activeChar, "main", _donationBBSCommand);
			return;
		}
		
		if (!CommunityDonateConfigs.COMMUNITY_DONATE_REC_NONPEACE && !activeChar.isInsideZone(ZoneIdType.PEACE))
		{
			activeChar.sendMessage("You cannot use this function outside peace zone.");
			sendHtm(activeChar, "main", _donationBBSCommand);
			return;
		}
		
		if (activeChar.getRecomHave() < 255)
		{
			if (!getPayment(activeChar, CommunityDonateConfigs.COMMUNITY_DONATE_REC_ID, CommunityDonateConfigs.COMMUNITY_DONATE_REC_PRICE))
			{
				sendHtm(activeChar, "main", _donationBBSCommand);
				return;
			}
			
			activeChar.setRecomHave(255);
			activeChar.broadcastUserInfo();
			activeChar.sendMessage("Your recommends have been increased to maximum.");
		}
		else
		{
			activeChar.sendMessage("You already have " + activeChar.getRecomHave() + " Recommends.");
		}
		sendHtm(activeChar, "main", _donationBBSCommand);
	}
	
	private void getFame(L2PcInstance activeChar)
	{
		if (!CommunityDonateConfigs.COMMUNITY_DONATE_FAME_ALLOW)
		{
			activeChar.sendMessage("This function is disabled by admin.");
			sendHtm(activeChar, "main", _donationBBSCommand);
			return;
		}
		
		if (!CommunityDonateConfigs.COMMUNITY_DONATE_FAME_NONPEACE && !activeChar.isInsideZone(ZoneIdType.PEACE))
		{
			activeChar.sendMessage("You cannot use this function outside peace zone.");
			sendHtm(activeChar, "main", _donationBBSCommand);
			return;
		}
		
		if (activeChar.getFame() < 100000)
		{
			if (!getPayment(activeChar, CommunityDonateConfigs.COMMUNITY_DONATE_FAME_ID, CommunityDonateConfigs.COMMUNITY_DONATE_FAME_PRICE))
			{
				sendHtm(activeChar, "main", _donationBBSCommand);
				return;
			}
			
			activeChar.setFame(activeChar.getFame() + CommunityDonateConfigs.COMMUNITY_DONATE_FAME_AMOUNT);
			activeChar.sendUserInfo(true);
			activeChar.sendMessage("Your Fame has been increased to " + activeChar.getFame() + ".");
		}
		else
		{
			activeChar.sendMessage("You already have " + activeChar.getFame() + " Fame.");
		}
		sendHtm(activeChar, "main", _donationBBSCommand);
	}
	
	//@formatter:off
	private static List<Integer> clanSkillsLevel = Arrays.asList(372, 375, 378, 381, 389, 391, 374, 380, 382, 383, 384, 385, 386, 387, 388, 390, 371, 376, 377, 370, 373, 379 );
	private static List<Integer> clanSquadSkills = Arrays.asList( 611, 612, 613, 614, 615, 616 );
	//@formatter:on
	
	private void getFulClan(L2PcInstance activeChar)
	{
		if (!CommunityDonateConfigs.COMMUNITY_DONATE_FULL_CLAN_ALLOW)
		{
			activeChar.sendMessage("This function is disabled by admin.");
			sendHtm(activeChar, "fullClan", _donationBBSCommand + "_nav;fullClan");
			return;
		}
		
		if (!CommunityDonateConfigs.COMMUNITY_DONATE_FULL_CLAN_NONPEACE && !activeChar.isInsideZone(ZoneIdType.PEACE))
		{
			activeChar.sendMessage("You cannot use this function outside peace zone.");
			sendHtm(activeChar, "fullClan", _donationBBSCommand + "_nav;fullClan");
			return;
		}
		
		if (activeChar.getClan() == null)
		{
			activeChar.sendMessage("You don't have a clan.");
			sendHtm(activeChar, "fullClan", _donationBBSCommand + "_nav;fullClan");
			return;
		}
		else if ((activeChar.getClan() == null) || (!activeChar.isClanLeader()))
		{
			activeChar.sendMessage("You are not a clan leader.");
			sendHtm(activeChar, "fullClan", _donationBBSCommand + "_nav;fullClan");
			return;
		}
		else if (activeChar.getClan().getLevel() == 11)
		{
			activeChar.sendMessage("Your clan is already Level 11.");
			sendHtm(activeChar, "fullClan", _donationBBSCommand + "_nav;fullClan");
			return;
		}
		
		if (!getPayment(activeChar, CommunityDonateConfigs.COMMUNITY_DONATE_FULL_CLAN_ID, CommunityDonateConfigs.COMMUNITY_DONATE_FULL_CLAN_PRICE))
		{
			sendHtm(activeChar, "fullClan", _donationBBSCommand + "_nav;fullClan");
			return;
		}
		
		activeChar.getClan().changeLevel(11);
		activeChar.getClan().addReputationScore(CommunityDonateConfigs.COMMUNITY_DONATE_FULL_CLAN_REP_AMOUNT, true);
		activeChar.getClan().addNewSkill(SkillData.getInstance().getInfo(391, 1));
		
		clanSkillsLevel.forEach(id -> activeChar.getClan().addNewSkill(SkillData.getInstance().getInfo(id, 3)));
		clanSquadSkills.forEach(id -> activeChar.getClan().addNewSkill(SkillData.getInstance().getInfo(id, 3), 0));
		
		activeChar.broadcastPacket(new MagicSkillUse(activeChar, 6463, 1, 1000, 0));
		activeChar.sendMessage("You purchased clan level 11 with full skills.");
		activeChar.sendPacket(new ExShowScreenMessage("Acquired clan level 11 full skills, Congratulations!", 5000));
		
		sendHtm(activeChar, "fullClan", _donationBBSCommand + "_nav;fullClan");
	}
	
	private void changeCharName(L2PcInstance activeChar, String command)
	{
		if (!CommunityDonateConfigs.COMMUNITY_DONATE_NAME_CHANGE_ALLOW)
		{
			activeChar.sendMessage("This function is disabled by admin.");
			sendHtm(activeChar, "charName", command);
			return;
		}
		
		if (!CommunityDonateConfigs.COMMUNITY_DONATE_NAME_CHANGE_NONPEACE && !activeChar.isInsideZone(ZoneIdType.PEACE))
		{
			activeChar.sendMessage("You cannot use this function outside peace zone.");
			sendHtm(activeChar, "charName", command);
			return;
		}
		
		String val = "";
		
		try
		{
			val = commandSeperator(command, " ");
		}
		catch (Exception e) // case of empty character name
		{
			activeChar.sendMessage("New name box cannot be empty.");
			sendHtm(activeChar, "charName", command);
			return;
		}
		
		if (!Util.isAlphaNumeric(val))
		{
			activeChar.sendMessage("Invalid character name.");
			sendHtm(activeChar, "charName", command);
			return;
		}
		
		if (val.length() > 16)
		{
			activeChar.sendMessage("Max character length is 16.");
			sendHtm(activeChar, "charName", command);
			return;
		}
		
		if (CharNameTable.getInstance().getIdByName(val) > 0)
		{
			activeChar.sendMessage("Warning, name " + val + " already exists.");
			sendHtm(activeChar, "charName", command);
			return;
		}
		
		if (!getPayment(activeChar, CommunityDonateConfigs.COMMUNITY_DONATE_NAME_CHANGE_ID, CommunityDonateConfigs.COMMUNITY_DONATE_NAME_CHANGE_PRICE))
		{
			sendHtm(activeChar, "charName", command);
			return;
		}
		
		activeChar.setName(val);
		activeChar.getAppearance().setVisibleName(val);
		activeChar.store();
		activeChar.broadcastUserInfo();
		activeChar.broadcastPacket(new MagicSkillUse(activeChar, 6463, 1, 1000, 0));
		activeChar.sendMessage("Your name has been changed to " + val + ".");
		activeChar.sendPacket(new ExShowScreenMessage("Your new character name: " + val, 5000));
		
		if (activeChar.isInParty())
		{
			// Delete party window for other party members
			activeChar.getParty().broadcastToPartyMembers(activeChar, new PartySmallWindowDeleteAll());
			for (L2PcInstance member : activeChar.getParty().getMembers())
			{
				// And re-add
				if (member != activeChar)
				{
					member.sendPacket(new PartySmallWindowAll(member, activeChar.getParty()));
				}
			}
		}
		if (activeChar.getClan() != null)
		{
			activeChar.getClan().broadcastClanStatus();
		}
		
		sendHtm(activeChar, "charName", command);
	}
	
	private void changeClanName(L2PcInstance activeChar, String command)
	{
		if (!CommunityDonateConfigs.COMMUNITY_DONATE_CLAN_NAME_CHANGE_ALLOW)
		{
			activeChar.sendMessage("This function is disabled by admin.");
			sendHtm(activeChar, "clanName", command);
			return;
		}
		
		if (!CommunityDonateConfigs.COMMUNITY_DONATE_CLAN_NAME_CHANGE_NONPEACE && !activeChar.isInsideZone(ZoneIdType.PEACE))
		{
			activeChar.sendMessage("You cannot use this function outside peace zone.");
			sendHtm(activeChar, "clanName", command);
			return;
		}
		
		if ((activeChar.getClan() == null) || (!activeChar.isClanLeader()))
		{
			activeChar.sendMessage("You do not own a clan.");
			sendHtm(activeChar, "clanName", command);
			return;
		}
		
		String val = "";
		
		try
		{
			val = commandSeperator(command, " ");
		}
		catch (Exception e)
		{
			// Case of empty character name
			activeChar.sendMessage("New name box cannot be empty.");
			sendHtm(activeChar, "clanName", command);
			return;
		}
		
		if (!Util.isAlphaNumeric(val) && (val.length() > 16))
		{
			activeChar.sendPacket(SystemMessageId.CLAN_NAME_INCORRECT);
			sendHtm(activeChar, "clanName", command);
			return;
		}
		
		if (val.length() > 16)
		{
			activeChar.sendMessage("Max character length is 16.");
			sendHtm(activeChar, "clanName", command);
			return;
		}
		
		if (ClanTable.getInstance().getClanByName(val) != null)
		{
			activeChar.sendMessage("Warning, clan name " + val + " already exists.");
			sendHtm(activeChar, "clanName", command);
			return;
		}
		
		if (!getPayment(activeChar, CommunityDonateConfigs.COMMUNITY_DONATE_CLAN_NAME_CHANGE_ID, CommunityDonateConfigs.COMMUNITY_DONATE_CLAN_NAME_CHANGE_PRICE))
		{
			sendHtm(activeChar, "clanName", command);
			return;
		}
		
		activeChar.getClan().setName(val);
		activeChar.getClan().updateClanNameInDB();
		activeChar.broadcastPacket(new MagicSkillUse(activeChar, 6463, 1, 1000, 0));
		activeChar.sendMessage("Your clan name has been changed to " + val + ".");
		activeChar.sendPacket(new ExShowScreenMessage("Your new clan name: " + val, 5000));
		activeChar.getClan().broadcastClanStatus();
		activeChar.sendPacket(new PledgeInfo(activeChar.getClan()));
		activeChar.broadcastUserInfo();
		
		if (activeChar.isInParty())
		{
			// Delete party window for other party members
			activeChar.getParty().broadcastToPartyMembers(activeChar, new PartySmallWindowDeleteAll());
			for (L2PcInstance member : activeChar.getParty().getMembers())
			{
				// And re-add
				if (member != activeChar)
				{
					member.sendPacket(new PartySmallWindowAll(member, activeChar.getParty()));
				}
			}
		}
		
		sendHtm(activeChar, "clanName", command);
	}
	
	private void giveNoble(L2PcInstance activeChar)
	{
		if (!CommunityDonateConfigs.COMMUNITY_DONATE_NOBLE_ALLOW)
		{
			activeChar.sendMessage("This function is disabled by admin.");
			sendHtm(activeChar, "main", _donationBBSCommand);
			return;
		}
		
		if (!CommunityDonateConfigs.COMMUNITY_DONATE_NOBLE_NONPEACE && !activeChar.isInsideZone(ZoneIdType.PEACE))
		{
			activeChar.sendMessage("You cannot use this function outside peace zone.");
			sendHtm(activeChar, "main", _donationBBSCommand);
			return;
		}
		
		if (!activeChar.isNoble())
		{
			if (!getPayment(activeChar, CommunityDonateConfigs.COMMUNITY_DONATE_NOBLE_ID, CommunityDonateConfigs.COMMUNITY_DONATE_NOBLE_PRICE))
			{
				sendHtm(activeChar, "main", _donationBBSCommand);
				return;
			}
			
			activeChar.setNoble(true);
			activeChar.addItem("Tiara", 7694, 1, null, true);
			activeChar.setTarget(activeChar);
			activeChar.broadcastPacket(new MagicSkillUse(activeChar, 6463, 1, 1000, 0));
			activeChar.broadcastUserInfo();
			activeChar.sendPacket(new ExShowScreenMessage("You obtained Noblesse Status, Congratulations!", 5000));
			activeChar.sendMessage("You have obtained Noblesse Status.");
		}
		else
		{
			activeChar.sendMessage("You already have Noblesse Status.");
		}
		sendHtm(activeChar, "main", _donationBBSCommand);
	}
	
	private String commandSeperator(String command, String sumbol)
	{
		StringTokenizer st = new StringTokenizer(command, sumbol);
		st.nextToken();
		String dat = st.nextToken();
		return dat;
	}
	
	private boolean getPayment(L2PcInstance activeChar, int id, int amount)
	{
		if (activeChar.destroyItemByItemId("DonateShop", id, amount, activeChar, true))
		{
			return true;
		}
		return false;
	}
	
	private void sendHtm(L2PcInstance activeChar, String file, String command)
	{
		BoardsManager.getInstance().addBypass(activeChar, "Donation Command", command);
		
		String content = "";
		String filepath = "data/html/CommunityBoard/donate/" + file + ".htm";
		
		File filecom = new File(filepath);
		
		if (!filecom.exists())
		{
			content = "<html><body><br><br><center>The command " + command + " points to file(" + filepath + ") that NOT exists.</center></body></html>";
			separateAndSend(content, activeChar);
			return;
		}
		
		content = HtmCache.getInstance().getHtm(activeChar, activeChar.getHtmlPrefix(), filepath);
		content = content.replace("%ccn_pr%", String.valueOf(CommunityDonateConfigs.COMMUNITY_DONATE_NAME_CHANGE_PRICE));
		content = content.replace("%ccc_pr%", String.valueOf(CommunityDonateConfigs.COMMUNITY_DONATE_CLAN_NAME_CHANGE_PRICE));
		content = content.replace("%nbl_pr%", String.valueOf(CommunityDonateConfigs.COMMUNITY_DONATE_NOBLE_PRICE));
		content = content.replace("%maxc_pr%", String.valueOf(CommunityDonateConfigs.COMMUNITY_DONATE_FULL_CLAN_PRICE));
		content = content.replace("%fame_pr%", String.valueOf(CommunityDonateConfigs.COMMUNITY_DONATE_FAME_PRICE));
		content = content.replace("%rec_pr%", String.valueOf(CommunityDonateConfigs.COMMUNITY_DONATE_REC_PRICE));
		content = content.replace("%aug_pr%", String.valueOf(CommunityDonateConfigs.COMMUNITY_DONATE_AUGMENT_PRICE));
		content = content.replace("%prem_pr1%", String.valueOf(CommunityDonateConfigs.COMMUNITY_DONATE_PREMIUM_PRICE_1_MONTH));
		content = content.replace("%prem_pr2%", String.valueOf(CommunityDonateConfigs.COMMUNITY_DONATE_PREMIUM_PRICE_2_MONTH));
		content = content.replace("%prem_pr3%", String.valueOf(CommunityDonateConfigs.COMMUNITY_DONATE_PREMIUM_PRICE_3_MONTH));
		
		separateAndSend(content, activeChar);
	}
	
	public static void loadAugmentBaseStats()
	{
		for (String s : AUGMENT_BASE_STAT.split(";"))
		{
			String[] i = s.split(",");
			AUGMENT_BASE_STATS.put(i[0], Integer.parseInt(i[1]));
		}
	}
	
	public static void loadAugmentSecondaryStats()
	{
		for (String s : AUGMENT_SECONDARY_STAT.split(";"))
		{
			String[] i = s.split(",");
			AUGMENT_SECONDARY_STATS.put(i[0], Integer.parseInt(i[1]));
		}
	}
	
	@Override
	protected void separateAndSend(String html, L2PcInstance acha)
	{
		html = html.replace("\t", "");
		html = html.replace("%command%", _donationBBSCommand);
		if (html.length() < 8180)
		{
			acha.sendPacket(new ShowBoard(html, "101"));
			acha.sendPacket(new ShowBoard(null, "102"));
			acha.sendPacket(new ShowBoard(null, "103"));
		}
		else if (html.length() < (8180 * 2))
		{
			acha.sendPacket(new ShowBoard(html.substring(0, 8180), "101"));
			acha.sendPacket(new ShowBoard(html.substring(8180, html.length()), "102"));
			acha.sendPacket(new ShowBoard(null, "103"));
		}
		else if (html.length() < (8180 * 3))
		{
			acha.sendPacket(new ShowBoard(html.substring(0, 8180), "101"));
			acha.sendPacket(new ShowBoard(html.substring(8180, 8180 * 2), "102"));
			acha.sendPacket(new ShowBoard(html.substring(8180 * 2, html.length()), "103"));
		}
	}
	
	@Override
	public void parsewrite(String url, String ar1, String ar2, String ar3, String ar4, String ar5, L2PcInstance activeChar)
	{
	
	}
	
	public static DonateBBSManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final DonateBBSManager _instance = new DonateBBSManager();
	}
}