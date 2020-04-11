/*
 * Copyright (C) 2004-2019 L2J DataPack
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
package custom.Services;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.logging.Logger;

import com.l2jserver.commons.database.pool.impl.ConnectionFactory;
import com.l2jserver.gameserver.data.sql.impl.CharNameTable;
import com.l2jserver.gameserver.data.sql.impl.ClanTable;
import com.l2jserver.gameserver.enums.Race;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.olympiad.OlympiadManager;
import com.l2jserver.gameserver.model.quest.Quest;
import com.l2jserver.gameserver.model.quest.QuestState;
import com.l2jserver.gameserver.network.serverpackets.MagicSkillUse;

/**
 * @author Colet
 */
public class Services extends Quest
{
	public static final Logger _log = Logger.getLogger(Services.class.getName());
	
	// NPC Id
	int servicesNpc = 36631;
	
	// Noble Items
	int nobleItemId = 23004;
	long nobleItemCount = 10;
	
	// PK Reduce Items
	int pkReduceItemId = 23004;
	long pkReduceItemCount = 1;
	
	// Change Name Items
	int changeNameItemId = 23004;
	long changeNameItemCount = 5;
	boolean logNameChanges = true;
	
	// Change Clan Name Items
	int changeClanNameItemId = 23004;
	long changeClanNameItemCount = 10;
	boolean logClanNameChanges = true;
	int clanMinLevel = 5;
	
	// Clan Level Items
	int[] clanLevelItemsId =
	{
		23004, // Level 0 to 1
		23004, // Level 1 to 2
		23004, // Level 2 to 3
		23004, // Level 3 to 4
		23004, // Level 4 to 5
		23004, // Level 5 to 6
		23004, // Level 6 to 7
		23004, // Level 7 to 8
		23004, // Level 8 to 9
		23004, // Level 9 to 10
		23004
		// Level 10 to 11
	};
	
	long[] clanLevelItemsCount =
	{
		1, // Level 0 to 1
		2, // Level 1 to 2
		4, // Level 2 to 3
		7, // Level 3 to 4
		9, // Level 4 to 5
		10, // Level 5 to 6
		15, // Level 6 to 7
		20, // Level 7 to 8
		30, // Level 8 to 9
		40, // Level 9 to 10
		50
		// Level 10 to 11
	};
	
	// Clan Reputation Points Items
	int clanReputationPointsItemId = 23004;
	long clanReputationPointsItemCount = 1;
	
	// Change Gender Items
	int changeGenderItemId = 23004;
	long changeGenderItemCount = 5;
	
	public Services(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(servicesNpc);
		addFirstTalkId(servicesNpc);
		addTalkId(servicesNpc);
	}
	
	public static void main(String[] args)
	{
		new Services(-1, Services.class.getSimpleName(), "custom");
		_log.info("Services Manager: Enabled.");
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if (player.getQuestState(getName()) == null)
		{
			newQuestState(player);
		}
		else if (player.isInCombat())
		{
			return "Services-Blocked.htm";
		}
		else if (player.getPvpFlag() == 1)
		{
			return "Services-Blocked.htm";
		}
		else if (player.getKarma() != 0)
		{
			return "Services-Blocked.htm";
		}
		else if (OlympiadManager.getInstance().isRegistered(player))
		{
			return "Services-Blocked.htm";
		}
		
		return "Services.htm";
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmlText = event;
		QuestState st = player.getQuestState(getName());
		
		if (event.equals("setNoble"))
		{
			if (!player.isNoble())
			{
				if (st.getQuestItemsCount(nobleItemId) >= nobleItemCount)
				{
					st.takeItems(nobleItemId, nobleItemCount);
					player.setNoble(true);
					player.setTarget(player);
					player.broadcastPacket(new MagicSkillUse(player, 5103, 1, 1000, 0));
					player.broadcastUserInfo();
					return "NoblesseServices-Success.htm";
				}
				return "NoblesseServices-NoItems.htm";
			}
			return "NoblesseServices-AlredyNoble.htm";
		}
		else if (event.equals("levelUpClan"))
		{
			if (!player.isClanLeader())
			{
				return "ClanLevelUp-NoLeader.htm";
			}
			if (player.getClan().getLevel() == 11)
			{
				return "ClanLevelUp-MaxLevel.htm";
			}
			if (((player.getClan().getLevel() <= 1) || (player.getClan().getLevel() == 2) || (player.getClan().getLevel() == 3) || (player.getClan().getLevel() == 4)))
			{
				st.takeItems(clanLevelItemsId[0], clanLevelItemsCount[0]);
				player.getClan().setLevel(player.getClan().getLevel() + 1);
				player.getClan().broadcastClanStatus();
				player.sendMessage("Your clan is now level " + player.getClan().getLevel() + ".");
				player.setTarget(player);
				player.broadcastPacket(new MagicSkillUse(player, 5103, 1, 1000, 0));
				return "ClanLevelUp.htm";
			}
			else if (player.getClan().getLevel() == 5)
			{
				if (st.getQuestItemsCount(clanLevelItemsId[0]) >= clanLevelItemsCount[0])
				{
					st.takeItems(clanLevelItemsId[0], clanLevelItemsCount[0]);
					player.getClan().setLevel(player.getClan().getLevel() + 1);
					player.getClan().broadcastClanStatus();
					player.sendMessage("Your clan is now level " + player.getClan().getLevel() + ".");
					player.setTarget(player);
					player.broadcastPacket(new MagicSkillUse(player, 5103, 1, 1000, 0));
					return "ClanLevelUp.htm";
				}
				return "ClanLevelUp-NoItems.htm";
			}
			else if (player.getClan().getLevel() == 6)
			{
				if (st.getQuestItemsCount(clanLevelItemsId[0]) >= clanLevelItemsCount[0])
				{
					st.takeItems(clanLevelItemsId[0], clanLevelItemsCount[0]);
					player.getClan().setLevel(player.getClan().getLevel() + 1);
					player.getClan().broadcastClanStatus();
					player.sendMessage("Your clan is now level " + player.getClan().getLevel() + ".");
					player.setTarget(player);
					player.broadcastPacket(new MagicSkillUse(player, 5103, 1, 1000, 0));
					return "ClanLevelUp.htm";
				}
				return "ClanLevelUp-NoItems.htm";
			}
			else if (player.getClan().getLevel() == 7)
			{
				if (st.getQuestItemsCount(clanLevelItemsId[0]) >= clanLevelItemsCount[0])
				{
					st.takeItems(clanLevelItemsId[0], clanLevelItemsCount[0]);
					player.getClan().setLevel(player.getClan().getLevel() + 1);
					player.getClan().broadcastClanStatus();
					player.sendMessage("Your clan is now level " + player.getClan().getLevel() + ".");
					player.setTarget(player);
					player.broadcastPacket(new MagicSkillUse(player, 5103, 1, 1000, 0));
					return "ClanLevelUp.htm";
				}
				return "ClanLevelUp-NoItems.htm";
			}
			else if (player.getClan().getLevel() == 8)
			{
				if (st.getQuestItemsCount(clanLevelItemsId[0]) >= clanLevelItemsCount[0])
				{
					st.takeItems(clanLevelItemsId[0], clanLevelItemsCount[0]);
					player.getClan().setLevel(player.getClan().getLevel() + 1);
					player.getClan().broadcastClanStatus();
					player.sendMessage("Your clan is now level " + player.getClan().getLevel() + ".");
					player.setTarget(player);
					player.broadcastPacket(new MagicSkillUse(player, 5103, 1, 1000, 0));
					return "ClanLevelUp.htm";
				}
				return "ClanLevelUp-NoItems.htm";
			}
			else if (player.getClan().getLevel() == 9)
			{
				if (st.getQuestItemsCount(clanLevelItemsId[0]) >= clanLevelItemsCount[0])
				{
					st.takeItems(clanLevelItemsId[0], clanLevelItemsCount[0]);
					player.getClan().setLevel(player.getClan().getLevel() + 1);
					player.getClan().broadcastClanStatus();
					player.sendMessage("Your clan is now level " + player.getClan().getLevel() + ".");
					player.setTarget(player);
					player.broadcastPacket(new MagicSkillUse(player, 5103, 1, 1000, 0));
					return "ClanLevelUp.htm";
				}
				return "ClanLevelUp-NoItems.htm";
			}
			else if (player.getClan().getLevel() == 10)
			{
				if (st.getQuestItemsCount(clanLevelItemsId[0]) >= clanLevelItemsCount[0])
				{
					st.takeItems(clanLevelItemsId[0], clanLevelItemsCount[0]);
					player.getClan().setLevel(player.getClan().getLevel() + 1);
					player.getClan().broadcastClanStatus();
					player.sendMessage("Your clan is now level " + player.getClan().getLevel() + ".");
					player.setTarget(player);
					player.broadcastPacket(new MagicSkillUse(player, 5103, 1, 1000, 0));
					return "ClanLevelUp.htm";
				}
				return "ClanLevelUp-NoItems.htm";
			}
			
			try (Connection con = ConnectionFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement("UPDATE clan_data SET clan_level=? WHERE clan_id=?"))
			{
				statement.setInt(1, player.getClan().getLevel());
				statement.setInt(2, player.getClan().getId());
				statement.execute();
				statement.close();
			}
			catch (Exception e)
			{
				_log.info("Error updating clan level for player " + player.getName() + ". Error: " + e);
			}
			
			player.getClan().broadcastClanStatus();
			return "ClanLevelUp.htm";
		}
		else if (event.equals("changeGender"))
		{
			if (player.getRace().equals(Race.KAMAEL))
			{
				player.sendMessage("Sorry this option isn't possible for Kamael");
				return "Services.htm";
			}
			
			if (st.getQuestItemsCount(changeGenderItemId) >= changeGenderItemCount)
			{
				st.takeItems(changeGenderItemId, changeGenderItemCount);
				player.getAppearance().setSex(player.getAppearance().getSex() ? false : true);
				player.setTarget(player);
				player.broadcastPacket(new MagicSkillUse(player, 5103, 1, 1000, 0));
				player.broadcastUserInfo();
				return "ChangeGender-Success.htm";
			}
			return "ChangeGender-NoItems.htm";
		}
		else if (event.startsWith("changeName"))
		{
			try
			{
				String newName = event.substring(11);
				
				if (st.getQuestItemsCount(changeNameItemId) >= changeNameItemCount)
				{
					if (newName == null)
					{
						return "ChangeName.htm";
					}
					if (!newName.matches("^[a-zA-Z0-9]+$"))
					{
						player.sendMessage("Incorrect name. Please try again.");
						return "ChangeName.htm";
					}
					else if (newName.equals(player.getName()))
					{
						player.sendMessage("Please, choose a different name.");
						return "ChangeName.htm";
					}
					else if (CharNameTable.getInstance().doesCharNameExist(newName))
					{
						player.sendMessage("The name " + newName + " already exists.");
						return "ChangeName.htm";
					}
					else
					{
						if (logNameChanges)
						{
							String fileName = "log/Services/Name Change - " + player.getName() + ".txt";
							new File(fileName);
							FileWriter fileText = new FileWriter(fileName);
							BufferedWriter fileContent = new BufferedWriter(fileText);
							fileContent.write("Character name change info:\r\n\r\nCharacter original name: " + player.getName() + "\r\nCharacter new name: " + newName);
							fileContent.close();
						}
						
						st.takeItems(changeNameItemId, changeNameItemCount);
						player.setName(newName);
						player.storeMe();
						player.sendMessage("Your new character name is " + newName);
						player.broadcastUserInfo();
						return "ChangeName-Success.htm";
					}
				}
				return "ChangeName-NoItems.htm";
			}
			catch (Exception e)
			{
				player.sendMessage("Please, insert a correct name.");
				return "ChangeName.htm";
			}
		}
		else if (event.startsWith("reducePks"))
		{
			try
			{
				String pkReduceString = event.substring(10);
				int pkReduceCount = Integer.parseInt(pkReduceString);
				
				if (player.getPkKills() != 0)
				{
					if (pkReduceCount == 0)
					{
						player.sendMessage("Please, put a higher value.");
						return "PkServices.htm";
					}
					if (st.getQuestItemsCount(pkReduceItemId) >= pkReduceItemCount)
					{
						st.takeItems(pkReduceItemId, pkReduceItemCount * pkReduceCount);
						player.setPkKills(player.getPkKills() - pkReduceCount);
						player.sendMessage("You have successfuly cleaned " + pkReduceCount + " PKs.");
						player.broadcastUserInfo();
						return "PkServices-Success.htm";
					}
					return "PkServices-NoItems.htm";
				}
				return "PkServices-NoPks.htm";
			}
			catch (Exception e)
			{
				player.sendMessage("Incorrect value. Please try again.");
				return "PkServices.htm";
			}
		}
		else if (event.startsWith("changeClanName"))
		{
			if (player.getClan() == null)
			{
				return "ChangeClanName-NoClan.htm";
			}
			try
			{
				String newClanName = event.substring(15);
				
				if (st.getQuestItemsCount(changeClanNameItemId) >= changeClanNameItemCount)
				{
					if (newClanName == null)
					{
						return "ChangeClanName.htm";
					}
					if (!player.isClanLeader())
					{
						player.sendMessage("Only the clan leader can change the clan name.");
						return "ChangeClanName.htm";
					}
					else if (player.getClan().getLevel() < clanMinLevel)
					{
						player.sendMessage("Your clan must be at least level " + clanMinLevel + " to change the name.");
						return "ChangeClanName.htm";
					}
					else if (!newClanName.matches("^[a-zA-Z0-9]+$"))
					{
						player.sendMessage("Incorrect name. Please try again.");
						return "ChangeClanName.htm";
					}
					else if (newClanName.equals(player.getClan().getName()))
					{
						player.sendMessage("Please, choose a different name.");
						return "ChangeClanName.htm";
					}
					else if (null != ClanTable.getInstance().getClanByName(newClanName))
					{
						player.sendMessage("The name " + newClanName + " already exists.");
						return "ChangeClanName.htm";
					}
					else
					{
						if (logClanNameChanges)
						{
							String fileName = "log/Services/Clan Name Change - " + player.getClan().getName() + ".txt";
							new File(fileName);
							FileWriter fileText = new FileWriter(fileName);
							BufferedWriter fileContent = new BufferedWriter(fileText);
							fileContent.write("Clan name change info:\r\n\r\nClan original name: " + player.getClan().getName() + "\r\nClan new name: " + newClanName + "\r\nClan Leader: " + player.getName());
							fileContent.close();
						}
						
						st.takeItems(changeNameItemId, changeNameItemCount);
						player.getClan().setName(newClanName);
						
						try (Connection con = ConnectionFactory.getInstance().getConnection();
							PreparedStatement statement = con.prepareStatement("UPDATE clan_data SET clan_name=? WHERE clan_id=?"))
						{
							statement.setString(1, newClanName);
							statement.setInt(2, player.getClan().getId());
							statement.execute();
							statement.close();
						}
						catch (Exception e)
						{
							_log.info("Error updating clan name for player " + player.getName() + ". Error: " + e);
						}
						
						player.sendMessage("Your new clan name is " + newClanName);
						player.getClan().broadcastClanStatus();
						return "ChangeClanName-Success.htm";
					}
				}
				return "ChangeClanName-NoItems.htm";
			}
			catch (Exception e)
			{
				player.sendMessage("Please, insert a correct name.");
				return "ChangeClanName.htm";
			}
		}
		else if (event.startsWith("setReputationPoints"))
		{
			try
			{
				String reputationPointsString = event.substring(20);
				int reputationPointsCount = Integer.parseInt(reputationPointsString);
				
				if (player.getClan() == null)
				{
					return "ClanReputationPoints-NoClan.htm";
				}
				else if (!player.isClanLeader())
				{
					return "ClanReputationPoints-NoLeader.htm";
				}
				else
				{
					if (reputationPointsCount == 0)
					{
						player.sendMessage("Please, put a higher value.");
						return "ClanReputationPoints.htm";
					}
					if (st.getQuestItemsCount(clanReputationPointsItemId) >= clanReputationPointsItemCount)
					{
						st.takeItems(clanReputationPointsItemId, clanReputationPointsItemCount * reputationPointsCount);
						player.getClan().addReputationScore(player.getClan().getReputationScore() + reputationPointsCount, true);
						player.getClan().broadcastClanStatus();
						return "ClanReputationPoints-Success.htm";
					}
					return "ClanReputationPoints-NoItems.htm";
				}
			}
			catch (Exception e)
			{
				player.sendMessage("Incorrect value. Please try again.");
				return "ClanReputationPoints.htm";
			}
		}
		
		return htmlText;
	}
}