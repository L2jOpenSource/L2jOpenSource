/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.model.actor.instance;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import javolution.text.TextBuilder;
import l2r.Config;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.datatables.sql.CharNameTable;
import l2r.gameserver.datatables.sql.ClanTable;
import l2r.gameserver.datatables.sql.NpcTable;
import l2r.gameserver.datatables.xml.ClassListData;
import l2r.gameserver.datatables.xml.HennaData;
import l2r.gameserver.datatables.xml.ItemData;
import l2r.gameserver.datatables.xml.MultiSell;
import l2r.gameserver.datatables.xml.SkillData;
import l2r.gameserver.datatables.xml.TransformData;
import l2r.gameserver.enums.InstanceType;
import l2r.gameserver.enums.PcRace;
import l2r.gameserver.idfactory.IdFactory;
import l2r.gameserver.instancemanager.CastleManager;
import l2r.gameserver.instancemanager.GrandBossManager;
import l2r.gameserver.model.ClanPrivilege;
import l2r.gameserver.model.L2Augmentation;
import l2r.gameserver.model.actor.FakePc;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.templates.L2NpcTemplate;
import l2r.gameserver.model.base.PlayerClass;
import l2r.gameserver.model.base.SubClass;
import l2r.gameserver.model.entity.Castle;
import l2r.gameserver.model.itemcontainer.Inventory;
import l2r.gameserver.model.items.L2Henna;
import l2r.gameserver.model.items.instance.L2ItemInstance;
import l2r.gameserver.model.olympiad.OlympiadManager;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.ActionFailed;
import l2r.gameserver.network.serverpackets.CharInfo;
import l2r.gameserver.network.serverpackets.ExBrExtraUserInfo;
import l2r.gameserver.network.serverpackets.ExShowBaseAttributeCancelWindow;
import l2r.gameserver.network.serverpackets.ExShowVariationCancelWindow;
import l2r.gameserver.network.serverpackets.ExShowVariationMakeWindow;
import l2r.gameserver.network.serverpackets.HennaEquipList;
import l2r.gameserver.network.serverpackets.HennaRemoveList;
import l2r.gameserver.network.serverpackets.InventoryUpdate;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.network.serverpackets.PartySmallWindowAll;
import l2r.gameserver.network.serverpackets.PartySmallWindowDeleteAll;
import l2r.gameserver.network.serverpackets.SiegeInfo;
import l2r.gameserver.network.serverpackets.SortedWareHouseWithdrawalList;
import l2r.gameserver.network.serverpackets.SortedWareHouseWithdrawalList.WarehouseListType;
import l2r.gameserver.network.serverpackets.UserInfo;
import l2r.gameserver.network.serverpackets.WareHouseDepositList;
import l2r.gameserver.network.serverpackets.WareHouseWithdrawalList;
import l2r.gameserver.util.Util;
import l2r.util.StringUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gr.reunion.achievementEngine.AchievementsHandler;
import gr.reunion.achievementEngine.AchievementsManager;
import gr.reunion.achievementEngine.base.Achievement;
import gr.reunion.achievementEngine.base.Condition;
import gr.reunion.aioItem.runnable.TransformFinalizer;
import gr.reunion.configsEngine.AioItemsConfigs;
import gr.reunion.configsEngine.CustomServerConfigs;
import gr.reunion.configsEngine.LeaderboardsConfigs;
import gr.reunion.dataHolder.PlayersTopData;
import gr.reunion.datatables.CustomTable;
import gr.reunion.donateEngine.DonateHandler;
import gr.reunion.imageGeneratorEngine.GenerateLogos;
import gr.reunion.leaderboards.ArenaLeaderboard;
import gr.reunion.leaderboards.CraftLeaderboard;
import gr.reunion.leaderboards.FishermanLeaderboard;
import gr.reunion.leaderboards.TvTLeaderboard;
import gr.reunion.main.Conditions;
import gr.reunion.securityEngine.SecurityActions;
import gr.reunion.securityEngine.SecurityType;

/**
 * @author -=DoctorNo=- Version 3.3 Last Edit: 19-10-2012
 */
public final class L2AioNpcInstance extends L2Npc
{
	/**
	 * @param objectId
	 * @param template
	 */
	public L2AioNpcInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		setInstanceType(InstanceType.L2AioNpcInstance);
		FakePc fpc = getFakePc();
		if (fpc != null)
		{
			setTitle(fpc.title);
		}
	}
	
	// Variable Section
	// Global Variable Item ID To Check or Destroy
	private static int itemIdToGet;
	// Global Variable
	private static int price;
	private static Logger _log = LoggerFactory.getLogger(L2AioNpcInstance.class);
	private static final int[] BOSSES =
	{
		29001,
		29006,
		29014,
		29019,
		29020,
		29022,
		29028,
		29118
	};
	private static final String[] COMMANDS =
	{
		"withdrawp",
		"withdrawsortedp",
		"withdrawc",
		"withdrawsortedc"
	};
	
	/**
	 * Method to send the html to char
	 * @param player
	 * @param html
	 */
	public void sendPacket(L2PcInstance player, String html)
	{
		NpcHtmlMessage msg = new NpcHtmlMessage(getObjectId());
		msg.setFile(player.getHtmlPrefix(), "/data/html/AioNpc/" + html);
		msg.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(msg);
	}
	
	@Override
	public void showChatWindow(L2PcInstance player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(player.getHtmlPrefix(), "data/html/AioNpc/main.htm");
		html.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(html);
	}
	
	protected static String getSubClassMenu(PcRace pRace)
	{
		if (Config.ALT_GAME_SUBCLASS_EVERYWHERE || (pRace != PcRace.Kamael))
		{
			return "data/html/AioNpc/subclass/SubClass.htm";
		}
		
		return "data/html/AioNpc/subclass/SubClass_NoOther.htm";
	}
	
	protected static String getSubClassFail()
	{
		return "data/html/AioNpc/subclass/SubClass_Fail.htm";
	}
	
	/**
	 * Method to manage all player bypasses
	 * @param player
	 * @param command
	 */
	@Override
	public void onBypassFeedback(final L2PcInstance player, String command)
	{
		final String[] subCommand = command.split("_");
		
		// No null pointers
		if (player == null)
		{
			return;
		}
		
		// Restrictions Section
		if (!Conditions.checkPlayerBasicConditions(player))
		{
			return;
		}
		
		// Page navigation, html command how to starts
		if (command.startsWith("Chat"))
		{
			if (subCommand[1].isEmpty() || (subCommand[1] == null))
			{
				return;
			}
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			html.setFile(player.getHtmlPrefix(), "data/html/AioNpc/" + subCommand[1]);
			html.replace("%objectId%", String.valueOf(getObjectId()));
			player.sendPacket(html);
		}
		
		// Warehouse
		else if (command.toLowerCase().startsWith(COMMANDS[0])) // WithdrawP
		{
			player.setIsUsingAioWh(true);
			if (Config.L2JMOD_ENABLE_WAREHOUSESORTING_PRIVATE)
			{
				NpcHtmlMessage msg = new NpcHtmlMessage(getObjectId());
				msg.setFile(player.getHtmlPrefix(), "data/html/AioNpc/warehouse/WhSortedP.htm");
				msg.replace("%objectId%", String.valueOf(getObjectId()));
				player.sendPacket(msg);
			}
			else
			{
				showPWithdrawWindow(player, null, (byte) 0);
			}
			
			return;
		}
		else if (command.toLowerCase().startsWith(COMMANDS[1])) // WithdrawSortedP
		{
			player.setIsUsingAioWh(true);
			final String param[] = command.split(" ");
			
			if (param.length > 2)
			{
				showPWithdrawWindow(player, WarehouseListType.valueOf(param[1]), SortedWareHouseWithdrawalList.getOrder(param[2]));
			}
			else if (param.length > 1)
			{
				showPWithdrawWindow(player, WarehouseListType.valueOf(param[1]), SortedWareHouseWithdrawalList.A2Z);
			}
			else
			{
				showPWithdrawWindow(player, WarehouseListType.ALL, SortedWareHouseWithdrawalList.A2Z);
			}
			
			return;
		}
		else if (command.toLowerCase().startsWith(COMMANDS[2])) // WithdrawC
		{
			player.setIsUsingAioWh(true);
			if (Config.L2JMOD_ENABLE_WAREHOUSESORTING_CLAN)
			{
				NpcHtmlMessage msg = new NpcHtmlMessage(getObjectId());
				msg.setFile(player.getHtmlPrefix(), "data/html/mods/WhSortedC.htm");
				msg.replace("%objectId%", String.valueOf(getObjectId()));
				player.sendPacket(msg);
			}
			else
			{
				showCWithdrawWindow(player, null, (byte) 0);
			}
			
			return;
		}
		else if (command.toLowerCase().startsWith(COMMANDS[3])) // WithdrawSortedC
		{
			player.setIsUsingAioWh(true);
			final String param[] = command.split(" ");
			
			if (param.length > 2)
			{
				showCWithdrawWindow(player, WarehouseListType.valueOf(param[1]), SortedWareHouseWithdrawalList.getOrder(param[2]));
			}
			else if (param.length > 1)
			{
				showCWithdrawWindow(player, WarehouseListType.valueOf(param[1]), SortedWareHouseWithdrawalList.A2Z);
			}
			else
			{
				showCWithdrawWindow(player, WarehouseListType.ALL, SortedWareHouseWithdrawalList.A2Z);
			}
			return;
		}
		else if (command.startsWith("ndeposit"))
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			player.setActiveWarehouse(player.getWarehouse());
			if (player.getWarehouse().getSize() == player.getWareHouseLimit())
			{
				player.sendPacket(SystemMessageId.WAREHOUSE_FULL);
				return;
			}
			player.setIsUsingAioWh(true);
			player.tempInventoryDisable();
			player.sendPacket(new WareHouseDepositList(player, WareHouseDepositList.PRIVATE));
		}
		else if (command.startsWith("clandeposit"))
		{
			if (player.getClan() == null)
			{
				player.sendPacket(SystemMessageId.YOU_ARE_NOT_A_CLAN_MEMBER);
				return;
			}
			player.sendPacket(ActionFailed.STATIC_PACKET);
			player.setActiveWarehouse(player.getClan().getWarehouse());
			
			if (player.getClan().getLevel() == 0)
			{
				player.sendPacket(SystemMessageId.ONLY_LEVEL_1_CLAN_OR_HIGHER_CAN_USE_WAREHOUSE);
				return;
			}
			player.setIsUsingAioWh(true);
			player.setActiveWarehouse(player.getClan().getWarehouse());
			player.tempInventoryDisable();
			player.sendPacket(new WareHouseDepositList(player, WareHouseDepositList.CLAN));
		}
		
		else if (command.startsWith("showMyAchievements"))
		{
			AchievementsHandler.getAchievemntData(player);
			showMyAchievements(player);
		}
		else if (command.startsWith("showAchievementInfo"))
		{
			try
			{
				StringTokenizer st = new StringTokenizer(command, " ");
				st.nextToken();
				int id = Integer.parseInt(st.nextToken());
				
				showAchievementInfo(id, player);
			}
			catch (Exception e)
			{
				SecurityActions.startSecurity(player, SecurityType.AIO_NPC);
			}
		}
		else if (command.startsWith("achievementGetReward"))
		{
			try
			{
				StringTokenizer st = new StringTokenizer(command, " ");
				st.nextToken();
				int id = Integer.parseInt(st.nextToken());
				
				if (!AchievementsManager.checkConditions(id, player))
				{
					return;
				}
				
				AchievementsManager.getInstance().rewardForAchievement(id, player);
				AchievementsHandler.saveAchievementData(player, id);
				showMyAchievements(player);
			}
			catch (Exception e)
			{
				SecurityActions.startSecurity(player, SecurityType.AIO_NPC);
			}
		}
		else if (command.startsWith("showAchievementStats"))
		{
			showAchievementStats(player);
		}
		else if (command.startsWith("showAchievementHelp"))
		{
			showAchievementHelp(player);
		}
		else if (command.startsWith("showAchievementMain"))
		{
			showAchievementMain(player, 0);
		}
		else if (command.startsWith("siege_"))
		{
			int castleId = 0;
			
			if (command.startsWith("siege_gludio"))
			{
				castleId = 1;
			}
			else if (command.startsWith("siege_dion"))
			{
				castleId = 2;
			}
			else if (command.startsWith("siege_giran"))
			{
				castleId = 3;
			}
			else if (command.startsWith("siege_oren"))
			{
				castleId = 4;
			}
			else if (command.startsWith("siege_aden"))
			{
				castleId = 5;
			}
			else if (command.startsWith("siege_innadril"))
			{
				castleId = 6;
			}
			else if (command.startsWith("siege_goddard"))
			{
				castleId = 7;
			}
			else if (command.startsWith("siege_rune"))
			{
				castleId = 8;
			}
			else if (command.startsWith("siege_schuttgart"))
			{
				castleId = 9;
			}
			
			Castle castle = CastleManager.getInstance().getCastleById(castleId);
			if ((castle != null) && (castleId != 0))
			{
				player.sendPacket(new SiegeInfo(castle));
			}
		}
		
		// Subclass system
		else if (command.startsWith("Subclass"))
		{
			// Subclasses may not be changed while a skill is in use.
			if (player.isCastingNow() || player.isAllSkillsDisabled())
			{
				player.sendPacket(SystemMessageId.SUBCLASS_NO_CHANGE_OR_CREATE_WHILE_SKILL_IN_USE);
				return;
			}
			else if (player.isInCombat())
			{
				player.sendMessage("Sub classes may not be created or changed while being in combat.");
				return;
			}
			else if (OlympiadManager.getInstance().isRegistered(player))
			{
				player.sendMessage("You can not change subclass when registered for Olympiad.");
				return;
			}
			else if (player.isInParty())
			{
				player.sendMessage("Sub classes may not be created or changed while being in party.");
				return;
			}
			else if (player.isCursedWeaponEquipped())
			{
				player.sendMessage("You can`t change Subclass while Cursed weapon equiped!");
				return;
			}
			final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			// Subclasses may not be changed while a transformated state.
			if (player.getTransformation() != null)
			{
				html.setFile(player.getHtmlPrefix(), "data/html/AioNpc/subclass/SubClass_NoTransformed.htm");
				player.sendPacket(html);
				return;
			}
			// Subclasses may not be changed while a summon is active.
			if (player.getSummon() != null)
			{
				html.setFile(player.getHtmlPrefix(), "data/html/AioNpc/subclass/SubClass_NoSummon.htm");
				player.sendPacket(html);
				return;
			}
			// Subclasses may not be changed while you have exceeded your inventory limit.
			if (!player.isInventoryUnder90(true))
			{
				player.sendPacket(SystemMessageId.NOT_SUBCLASS_WHILE_INVENTORY_FULL);
				return;
			}
			// Subclasses may not be changed while a you are over your weight limit.
			if (player.getWeightPenalty() >= 2)
			{
				player.sendPacket(SystemMessageId.NOT_SUBCLASS_WHILE_OVERWEIGHT);
				return;
			}
			
			int cmdChoice = 0;
			int paramOne = 0;
			int paramTwo = 0;
			try
			{
				cmdChoice = Integer.parseInt(command.substring(9, 10).trim());
				
				int endIndex = command.indexOf(' ', 11);
				if (endIndex == -1)
				{
					endIndex = command.length();
				}
				
				if (command.length() > 11)
				{
					paramOne = Integer.parseInt(command.substring(11, endIndex).trim());
					if (command.length() > endIndex)
					{
						paramTwo = Integer.parseInt(command.substring(endIndex).trim());
					}
				}
			}
			catch (Exception NumberFormatException)
			{
				_log.warn(L2AioNpcInstance.class.getName() + ": Wrong numeric values for command " + command);
			}
			
			Set<PlayerClass> subsAvailable = null;
			switch (cmdChoice)
			{
				case 0: // Subclass change menu
					html.setFile(player.getHtmlPrefix(), getSubClassMenu(player.getRace()));
					break;
				case 1: // Add Subclass - Initial
					// Avoid giving player an option to add a new sub class, if they have max sub-classes already.
					if (player.getTotalSubClasses() >= Config.MAX_SUBCLASS)
					{
						html.setFile(player.getHtmlPrefix(), getSubClassFail());
						break;
					}
					
					subsAvailable = Conditions.getAvailableSubClasses(player);
					if ((subsAvailable != null) && !subsAvailable.isEmpty())
					{
						html.setFile(player.getHtmlPrefix(), "data/html/AioNpc/subclass/SubClass_Add.htm");
						final StringBuilder content1 = StringUtil.startAppend(200);
						for (PlayerClass subClass : subsAvailable)
						{
							StringUtil.append(content1, "<a action=\"bypass -h npc_%objectId%_Subclass 4 ", String.valueOf(subClass.ordinal()), "\" msg=\"1268;", ClassListData.getInstance().getClass(subClass.ordinal()).getClassName(), "\">", ClassListData.getInstance().getClass(subClass.ordinal()).getClientCode(), "</a><br>");
						}
						html.replace("%list%", content1.toString());
					}
					else
					{
						if ((player.getRace() == PcRace.Elf) || (player.getRace() == PcRace.DarkElf))
						{
							html.setFile(player.getHtmlPrefix(), "data/html/AioNpc/subclass/SubClass_Fail_Elves.htm");
							player.sendPacket(html);
						}
						else if (player.getRace() == PcRace.Kamael)
						{
							html.setFile(player.getHtmlPrefix(), "data/html/AioNpc/subclass/SubClass_Fail_Kamael.htm");
							player.sendPacket(html);
						}
						else
						{
							player.sendMessage("There are no sub classes available at this time.");
						}
						
						return;
					}
					break;
				case 2: // Change Class - Initial
					if (player.getSubClasses().isEmpty())
					{
						html.setFile(player.getHtmlPrefix(), "data/html/AioNpc/subclass/SubClass_ChangeNo.htm");
					}
					else
					{
						final StringBuilder content2 = StringUtil.startAppend(200);
						if (Conditions.checkVillageMaster(player.getBaseClass()))
						{
							StringUtil.append(content2, "<a action=\"bypass -h npc_%objectId%_Subclass 5 0\">", ClassListData.getInstance().getClass(player.getBaseClass()).getClientCode(), "</a><br>");
						}
						
						for (Iterator<SubClass> subList = Conditions.iterSubClasses(player); subList.hasNext();)
						{
							SubClass subClass = subList.next();
							if (Conditions.checkVillageMaster(subClass.getClassDefinition()))
							{
								StringUtil.append(content2, "<a action=\"bypass -h npc_%objectId%_Subclass 5 ", String.valueOf(subClass.getClassIndex()), "\">", ClassListData.getInstance().getClass(subClass.getClassId()).getClientCode(), "</a><br>");
							}
						}
						
						if (content2.length() > 0)
						{
							html.setFile(player.getHtmlPrefix(), "data/html/AioNpc/subclass/SubClass_Change.htm");
							html.replace("%list%", content2.toString());
						}
						else
						{
							html.setFile(player.getHtmlPrefix(), "data/html/AioNpc/subclass/SubClass_ChangeNotFound.htm");
						}
					}
					break;
				case 3: // Change/Cancel Subclass - Initial
					if ((player.getSubClasses() == null) || player.getSubClasses().isEmpty())
					{
						html.setFile(player.getHtmlPrefix(), "data/html/AioNpc/subclass/SubClass_ModifyEmpty.htm");
						break;
					}
					
					// custom value
					if ((player.getTotalSubClasses() > 3) || (Config.MAX_SUBCLASS > 3))
					{
						html.setFile(player.getHtmlPrefix(), "data/html/AioNpc/subclass/SubClass_ModifyCustom.htm");
						final StringBuilder content3 = StringUtil.startAppend(200);
						int classIndex = 1;
						
						for (Iterator<SubClass> subList = Conditions.iterSubClasses(player); subList.hasNext();)
						{
							SubClass subClass = subList.next();
							
							StringUtil.append(content3, "Sub-class ", String.valueOf(classIndex++), "<br>", "<a action=\"bypass -h npc_%objectId%_Subclass 6 ", String.valueOf(subClass.getClassIndex()), "\">", ClassListData.getInstance().getClass(subClass.getClassId()).getClientCode(), "</a><br>");
						}
						html.replace("%list%", content3.toString());
					}
					else
					{
						// retail html contain only 3 subclasses
						html.setFile(player.getHtmlPrefix(), "data/html/AioNpc/subclass/SubClass_Modify.htm");
						if (player.getSubClasses().containsKey(1))
						{
							html.replace("%sub1%", ClassListData.getInstance().getClass(player.getSubClasses().get(1).getClassId()).getClientCode());
						}
						else
						{
							html.replace("<a action=\"bypass -h npc_%objectId%_Subclass 6 1\">%sub1%</a><br>", "");
						}
						
						if (player.getSubClasses().containsKey(2))
						{
							html.replace("%sub2%", ClassListData.getInstance().getClass(player.getSubClasses().get(2).getClassId()).getClientCode());
						}
						else
						{
							html.replace("<a action=\"bypass -h npc_%objectId%_Subclass 6 2\">%sub2%</a><br>", "");
						}
						
						if (player.getSubClasses().containsKey(3))
						{
							html.replace("%sub3%", ClassListData.getInstance().getClass(player.getSubClasses().get(3).getClassId()).getClientCode());
						}
						else
						{
							html.replace("<a action=\"bypass -h npc_%objectId%_Subclass 6 3\">%sub3%</a><br>", "");
						}
					}
					break;
				case 4: // Add Subclass - Action (Subclass 4 x[x])
					/**
					 * If the character is less than level 75 on any of their previously chosen classes then disallow them to change to their most recently added sub-class choice.
					 */
					if (!player.getFloodProtectors().getSubclass().tryPerformAction("add subclass"))
					{
						_log.warn(L2AioNpcInstance.class.getName() + ": Player " + player.getName() + " has performed a subclass change too fast");
						return;
					}
					
					boolean allowAddition = true;
					
					if (player.getTotalSubClasses() >= Config.MAX_SUBCLASS)
					{
						allowAddition = false;
					}
					
					if (player.getLevel() < 75)
					{
						allowAddition = false;
					}
					
					if (allowAddition)
					{
						if (!player.getSubClasses().isEmpty())
						{
							for (Iterator<SubClass> subList = Conditions.iterSubClasses(player); subList.hasNext();)
							{
								SubClass subClass = subList.next();
								
								if (subClass.getLevel() < 75)
								{
									allowAddition = false;
									break;
								}
							}
						}
					}
					
					/**
					 * If quest checking is enabled, verify if the character has completed the Mimir's Elixir (Path to Subclass) and Fate's Whisper (A Grade Weapon) quests by checking for instances of their unique reward items. If they both exist, remove both unique items and continue with adding
					 * the sub-class.
					 */
					if (allowAddition && !Config.ALT_GAME_SUBCLASS_WITHOUT_QUESTS)
					{
						allowAddition = Conditions.checkQuests(player);
					}
					
					if (allowAddition && Conditions.isValidNewSubClass(player, paramOne))
					{
						if (!player.addSubClass(paramOne, player.getTotalSubClasses() + 1))
						{
							return;
						}
						
						player.setActiveClass(player.getTotalSubClasses());
						
						html.setFile(player.getHtmlPrefix(), "data/html/AioNpc/subclass/SubClass_AddOk.htm");
						
						player.sendPacket(SystemMessageId.ADD_NEW_SUBCLASS); // Subclass added.
					}
					else
					{
						html.setFile(player.getHtmlPrefix(), getSubClassFail());
					}
					break;
				case 5: // Change Class - Action
					/**
					 * If the character is less than level 75 on any of their previously chosen classes then disallow them to change to their most recently added sub-class choice. Note: paramOne = classIndex
					 */
					if (!player.getFloodProtectors().getSubclass().tryPerformAction("change class"))
					{
						_log.warn(L2AioNpcInstance.class.getName() + ": Player " + player.getName() + " has performed a subclass change too fast");
						return;
					}
					
					if (player.getClassIndex() == paramOne)
					{
						html.setFile(player.getHtmlPrefix(), "data/html/AioNpc/subclass/SubClass_Current.htm");
						break;
					}
					
					if (paramOne == 0)
					{
						if (!Conditions.checkVillageMaster(player.getBaseClass()))
						{
							return;
						}
					}
					else
					{
						try
						{
							if (!Conditions.checkVillageMaster(player.getSubClasses().get(paramOne).getClassDefinition()))
							{
								return;
							}
						}
						catch (NullPointerException e)
						{
							return;
						}
					}
					
					player.setActiveClass(paramOne);
					player.sendPacket(SystemMessageId.SUBCLASS_TRANSFER_COMPLETED); // Transfer completed.
					return;
				case 6: // Change/Cancel Subclass - Choice
					// validity check
					if ((paramOne < 1) || (paramOne > Config.MAX_SUBCLASS))
					{
						return;
					}
					
					subsAvailable = Conditions.getAvailableSubClasses(player);
					// another validity check
					if ((subsAvailable == null) || subsAvailable.isEmpty())
					{
						player.sendMessage("There are no sub classes available at this time.");
						return;
					}
					
					final StringBuilder content6 = StringUtil.startAppend(200);
					for (PlayerClass subClass : subsAvailable)
					{
						StringUtil.append(content6, "<a action=\"bypass -h npc_%objectId%_Subclass 7 ", String.valueOf(paramOne), " ", String.valueOf(subClass.ordinal()), "\" msg=\"1445;", "\">", ClassListData.getInstance().getClass(subClass.ordinal()).getClientCode(), "</a><br>");
					}
					
					switch (paramOne)
					{
						case 1:
							html.setFile(player.getHtmlPrefix(), "data/html/AioNpc/subclass/SubClass_ModifyChoice1.htm");
							break;
						case 2:
							html.setFile(player.getHtmlPrefix(), "data/html/AioNpc/subclass/SubClass_ModifyChoice2.htm");
							break;
						case 3:
							html.setFile(player.getHtmlPrefix(), "data/html/AioNpc/subclass/SubClass_ModifyChoice3.htm");
							break;
						default:
							html.setFile(player.getHtmlPrefix(), "data/html/AioNpc/subclass/SubClass_ModifyChoice.htm");
					}
					html.replace("%list%", content6.toString());
					break;
				case 7: // Change Subclass - Action
					/**
					 * Warning: the information about this subclass will be removed from the subclass list even if false!
					 */
					if (!player.getFloodProtectors().getSubclass().tryPerformAction("change class"))
					{
						_log.warn(L2AioNpcInstance.class.getName() + ": Player " + player.getName() + " has performed a subclass change too fast");
						return;
					}
					
					if (!Conditions.isValidNewSubClass(player, paramTwo))
					{
						return;
					}
					
					if (player.modifySubClass(paramOne, paramTwo))
					{
						player.abortCast();
						player.stopAllEffectsExceptThoseThatLastThroughDeath(); // all effects from old subclass stopped!
						player.stopAllEffectsNotStayOnSubclassChange();
						player.stopCubics();
						player.setActiveClass(paramOne);
						
						html.setFile(player.getHtmlPrefix(), "data/html/AioNpc/subclass/SubClass_ModifyOk.htm");
						html.replace("%name%", ClassListData.getInstance().getClass(paramTwo).getClientCode());
						
						player.sendPacket(SystemMessageId.ADD_NEW_SUBCLASS); // Subclass added.
					}
					else
					{
						/**
						 * This isn't good! modifySubClass() removed subclass from memory we must update _classIndex! Else IndexOutOfBoundsException can turn up some place down the line along with other seemingly unrelated problems.
						 */
						player.setActiveClass(0); // Also updates _classIndex plus switching _classid to baseclass.
						
						player.sendMessage("The sub class could not be added, you have been reverted to your base class.");
						return;
					}
					break;
			}
			html.replace("%objectId%", String.valueOf(getObjectId()));
			player.sendPacket(html);
		}
		
		// Create new clan
		else if (command.startsWith("createclan"))
		{
			String[] commandStr = command.split(" ");
			String cmdParams = "";
			
			if (commandStr.length >= 2)
			{
				cmdParams = commandStr[1];
			}
			
			if (cmdParams.isEmpty())
			{
				return;
			}
			
			if (!Conditions.isValidName(cmdParams))
			{
				player.sendPacket(SystemMessageId.CLAN_NAME_INCORRECT);
				return;
			}
			
			ClanTable.getInstance().createClan(player, cmdParams);
		}
		
		// Add all clan skills
		else if (command.startsWith("clanskills"))
		{
			itemIdToGet = AioItemsConfigs.GET_FULL_CLAN_COIN;
			price = AioItemsConfigs.GET_FULL_CLAN_PRICE;
			
			if ((player.getClan() == null) || (!player.isClanLeader()))
			{
				player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
				return;
			}
			
			if (!Conditions.checkPlayerItemCount(player, itemIdToGet, price))
			{
				return;
			}
			
			player.destroyItemByItemId("Clan donate", itemIdToGet, price, player, true);
			player.getClan().changeLevel(11);
			player.sendMessage("Clan level set to 11");
			player.getClan().addReputationScore(500000, true);
			player.getClan().addNewSkill(SkillData.getInstance().getInfo(391, 1));
			int[] squad =
			{
				611,
				612,
				613,
				614,
				615,
				616
			};
			int[] normal =
			{
				370,
				371,
				372,
				373,
				374,
				375,
				376,
				377,
				378,
				379,
				380,
				381,
				382,
				383,
				384,
				385,
				386,
				387,
				388,
				389,
				390
			};
			for (int ids : normal)
			{
				player.getClan().addNewSkill(SkillData.getInstance().getInfo(ids, 3));
			}
			for (int ids : squad)
			{
				player.getClan().addNewSkill(SkillData.getInstance().getInfo(ids, 3), 0);
			}
			
			player.sendMessage("You have successfully perform this action");
		}
		
		// Change player name
		else if (command.startsWith("changename"))
		{
			try
			{
				itemIdToGet = AioItemsConfigs.CHANGE_NAME_COIN;
				price = AioItemsConfigs.CHANGE_NAME_PRICE;
				
				String val = command.substring(11);
				if (!Util.isAlphaNumeric(val))
				{
					player.sendMessage("Invalid character name.");
					return;
				}
				
				if (!Conditions.checkPlayerItemCount(player, itemIdToGet, price))
				{
					return;
				}
				
				if (CharNameTable.getInstance().getIdByName(val) > 0)
				{
					player.sendMessage("Warning, name " + val + " already exists.");
					return;
				}
				player.destroyItemByItemId("Name Change", itemIdToGet, price, player, true);
				player.setName(val);
				player.getAppearance().setVisibleName(val);
				player.store();
				
				player.sendMessage("Your name has been changed to " + val);
				player.broadcastUserInfo();
				
				if (player.isInParty())
				{
					// Delete party window for other party members
					player.getParty().broadcastToPartyMembers(player, new PartySmallWindowDeleteAll());
					for (L2PcInstance member : player.getParty().getMembers())
					{
						// And re-add
						if (member != player)
						{
							member.sendPacket(new PartySmallWindowAll(member, player.getParty()));
						}
					}
				}
				if (player.getClan() != null)
				{
					player.getClan().broadcastClanStatus();
				}
			}
			catch (StringIndexOutOfBoundsException e)
			{
				// Case of empty character name
				player.sendMessage("Player name box cannot be empty.");
			}
		}
		
		// Change clan name
		else if (command.startsWith("changeclanname"))
		{
			try
			{
				itemIdToGet = AioItemsConfigs.CHANGE_CNAME_COIN;
				price = AioItemsConfigs.CHANGE_CNAME_PRICE;
				String val = command.substring(15);
				if ((player.getClan() == null) || (!player.isClanLeader()))
				{
					player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
					return;
				}
				if (!Util.isAlphaNumeric(val))
				{
					player.sendPacket(SystemMessageId.CLAN_NAME_INCORRECT);
					return;
				}
				
				if (!Conditions.checkPlayerItemCount(player, itemIdToGet, price))
				{
					return;
				}
				
				if (ClanTable.getInstance().getClanByName(val) != null)
				{
					player.sendMessage("Warning, clan name " + val + " already exists.");
					return;
				}
				
				player.destroyItemByItemId("Clan Name Change", itemIdToGet, price, player, true);
				player.getClan().setName(val);
				player.getClan().updateClanNameInDB();
				player.sendMessage("Your clan name has been changed to " + val);
				player.broadcastUserInfo();
				
				if (player.isInParty())
				{
					// Delete party window for other party members
					player.getParty().broadcastToPartyMembers(player, new PartySmallWindowDeleteAll());
					for (L2PcInstance member : player.getParty().getMembers())
					{
						// And re-add
						if (member != player)
						{
							member.sendPacket(new PartySmallWindowAll(member, player.getParty()));
						}
					}
				}
				if (player.getClan() != null)
				{
					player.getClan().broadcastClanStatus();
				}
			}
			catch (StringIndexOutOfBoundsException e)
			{
				// Case of empty character name
				player.sendMessage("Clan name box cannot be empty.");
			}
		}
		
		// Method to add specific augment to a weapon
		else if (command.startsWith("addaugment"))
		{
			int value = 0;
			int pskill = 0;
			
			String[] stats = command.split(" "); // str, int, men, con
			
			if (stats[1].equals("STR+1"))
			{
				value = 16341;
			}
			else if (stats[1].equals("INT+1"))
			{
				value = 16343;
			}
			else if (stats[1].equals("MEN+1"))
			{
				value = 16344;
			}
			else if (stats[1].equals("CON+1"))
			{
				value = 16342;
			}
			
			if (stats[2].equals("Heal_Empower"))
			{
				pskill = 16279;
			}
			else if (stats[2].equals("Wild_Magic"))
			{
				pskill = 16336;
			}
			else if (stats[2].equals("Empower"))
			{
				pskill = 16281;
			}
			else if (stats[2].equals("Magic_Barrier"))
			{
				pskill = 16282;
			}
			else if (stats[2].equals("Might"))
			{
				pskill = 16283;
			}
			else if (stats[2].equals("Shield"))
			{
				pskill = 16284;
			}
			else if (stats[2].equals("Duel_Might"))
			{
				pskill = 16285;
			}
			
			pskill = pskill + 8358;
			int armorType = -1;
			armorType = Inventory.PAPERDOLL_RHAND;
			itemIdToGet = AioItemsConfigs.AUGMENT_COIN;
			price = AioItemsConfigs.AUGMENT_PRICE;
			
			if (!Conditions.checkPlayerItemCount(player, itemIdToGet, price))
			{
				return;
			}
			
			L2ItemInstance itemInstance = null;
			L2ItemInstance parmorInstance = player.getInventory().getPaperdollItem(armorType);
			if (parmorInstance == null)
			{
				player.sendMessage("Equip the weapon for augmentation.");
				return;
			}
			if (parmorInstance.isAugmented())
			{
				player.sendPacket(SystemMessageId.ONCE_AN_ITEM_IS_AUGMENTED_IT_CANNOT_BE_AUGMENTED_AGAIN);
				return;
			}
			if (parmorInstance.isHeroItem() || (parmorInstance.isShadowItem() || (parmorInstance.isCommonItem() || (parmorInstance.isEtcItem() || (parmorInstance.isTimeLimitedItem())))))
			{
				player.sendPacket(SystemMessageId.THIS_IS_NOT_A_SUITABLE_ITEM);
				return;
			}
			if (parmorInstance.getLocationSlot() == armorType)
			{
				itemInstance = parmorInstance;
			}
			
			if (itemInstance != null)
			{
				// set augment skill
				player.destroyItemByItemId("augment", itemIdToGet, price, player, true);
				player.getInventory().unEquipItemInSlot(armorType);
				itemInstance.setAugmentation(new L2Augmentation(((pskill << 16) + value)));
				player.getInventory().equipItem(itemInstance);
				player.sendPacket(SystemMessageId.THE_ITEM_WAS_SUCCESSFULLY_AUGMENTED);
				
				// send packets
				InventoryUpdate iu = new InventoryUpdate();
				iu.addModifiedItem(itemInstance);
				player.sendPacket(iu);
				player.broadcastPacket(new CharInfo(player));
				player.sendPacket(new UserInfo(player));
				player.broadcastPacket(new ExBrExtraUserInfo(player));
			}
		}
		
		// Teleport
		else if (command.startsWith("teleportTo"))
		{
			itemIdToGet = AioItemsConfigs.AIO_TPCOIN;
			price = AioItemsConfigs.AIO_PRICE_PERTP;
			
			if (player.isTransformed())
			{
				if ((player.getTransformationId() == 9) || (player.getTransformationId() == 8))
				{
					player.untransform();
				}
			}
			
			if (!Conditions.checkPlayerItemCount(player, itemIdToGet, price))
			{
				return;
			}
			
			try
			{
				Integer[] c = new Integer[3];
				c[0] = CustomTable.getInstance().getCoords(Integer.parseInt(subCommand[1]))[0];
				c[1] = CustomTable.getInstance().getCoords(Integer.parseInt(subCommand[1]))[1];
				c[2] = CustomTable.getInstance().getCoords(Integer.parseInt(subCommand[1]))[2];
				player.destroyItemByItemId("AIO Teleport", itemIdToGet, price, player, true);
				player.teleToLocation(c[0], c[1], c[2]);
			}
			catch (Exception e)
			{
				SecurityActions.startSecurity(player, SecurityType.AIO_NPC);
			}
		}
		
		// Leaderboard commands
		else if (command.startsWith("rankarenainfo"))
		{
			if (LeaderboardsConfigs.RANK_ARENA_ENABLED)
			{
				NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				html.setHtml(ArenaLeaderboard.getInstance().showHtm(player.getObjectId()));
				player.sendPacket(html);
			}
			else
			{
				player.sendMessage("This service is currently disabled.");
			}
		}
		
		else if (command.startsWith("rankfishermaninfo"))
		{
			if (LeaderboardsConfigs.RANK_FISHERMAN_ENABLED)
			{
				NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				html.setHtml(FishermanLeaderboard.getInstance().showHtm(player.getObjectId()));
				player.sendPacket(html);
			}
			else
			{
				player.sendMessage("This service is currently disabled.");
			}
		}
		
		else if (command.startsWith("rankcraftinfo"))
		{
			if (LeaderboardsConfigs.RANK_CRAFT_ENABLED)
			{
				NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				html.setHtml(CraftLeaderboard.getInstance().showHtm(player.getObjectId()));
				player.sendPacket(html);
			}
			else
			{
				player.sendMessage("This service is currently disabled.");
			}
		}
		
		else if (command.startsWith("ranktvtinfo"))
		{
			if (LeaderboardsConfigs.RANK_TVT_ENABLED)
			{
				NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				html.setHtml(TvTLeaderboard.getInstance().showHtm(player.getObjectId()));
				player.sendPacket(html);
			}
			else
			{
				player.sendMessage("This service is currently disabled.");
			}
		}
		
		// Change gender options
		else if (command.startsWith("changeGender"))
		{
			if (command.startsWith("changeGenderDonate"))
			{
				itemIdToGet = AioItemsConfigs.CHANGE_GENDER_DONATE_COIN;
				price = AioItemsConfigs.CHANGE_GENDER_DONATE_PRICE;
				
			}
			else if (command.startsWith("changeGenderNormal"))
			{
				itemIdToGet = AioItemsConfigs.CHANGE_GENDER_NORMAL_COIN;
				price = AioItemsConfigs.CHANGE_GENDER_NORMAL_PRICE;
			}
			
			if (!Conditions.checkPlayerItemCount(player, itemIdToGet, price))
			{
				return;
			}
			
			player.destroyItemByItemId("changeGender", itemIdToGet, price, player, true);
			player.getAppearance().setSex(player.getAppearance().getSex() ? false : true);
			player.sendMessage("Your gender has been changed.");
			player.broadcastUserInfo();
			// Transform-untransorm player quickly to force the client to reload the character textures
			TransformData.getInstance().transformPlayer(105, player);
			TransformFinalizer ef = new TransformFinalizer(player);
			player.setSkillCast(ThreadPoolManager.getInstance().scheduleGeneral(ef, 200));
		}
		
		// GM Shop
		else if (command.startsWith("showMultiSellWindow"))
		{
			try
			{
				int multi = Integer.valueOf(subCommand[1]);
				player.setIsUsingAioMultisell(true);
				MultiSell.getInstance().separateAndSend(multi, player, null, false);
			}
			catch (Exception e)
			{
				SecurityActions.startSecurity(player, SecurityType.AIO_NPC);
			}
		}
		
		// Donate generate captcha code
		else if (command.startsWith("donateFormMain"))
		{
			NpcHtmlMessage playerReply = new NpcHtmlMessage(5);
			
			// Random image file name
			int imgId = IdFactory.getInstance().getNextId();
			// Conversion from .png to .dds, and crest packed send
			GenerateLogos.getInstance().captchaLogo(player, imgId);
			playerReply.setHtml("<html><title>Donate Captcha System</title>" + "<body>" + "<center>Enter the 5-digits code below and click Confirm." + "<br><img src=\"Crest.crest_" + Config.SERVER_ID + "_" + imgId + "\" width=256 height=64>" + "<br><font color=\"888888\">(There are only english uppercase letters.)</font>" + "<br><edit var=\"captcha\" width=110><br><button value=\"Confirm\" action=\"bypass -h npc_%objectId%_confirmDonateCode $captcha\" width=80 height=26 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"></center></body></html>");
			playerReply.replace("%objectId%", String.valueOf(getObjectId()));
			player.sendPacket(playerReply);
			player.setDonateCode(GenerateLogos.getInstance().getFinalString());
			GenerateLogos.getInstance().getFinalString().replace(0, 5, "");
			return;
		}
		
		// Donate captcha code
		else if (command.startsWith("confirmDonateCode"))
		{
			String value = command.substring(17);
			StringTokenizer st = new StringTokenizer(value, " ");
			try
			{
				String newpass = null, repeatnewpass = null;
				if (st.hasMoreTokens())
				{
					newpass = st.nextToken();
				}
				repeatnewpass = player.getDonateCode();
				
				if (!((newpass == null) || (repeatnewpass == null)))
				{
					if (newpass.equals(repeatnewpass)) // Right:)
					{
						sendPacket(player, "donate/donateform.htm");
						return;
					}
				}
				if ((newpass == null) || !newpass.equals(repeatnewpass))
				{
					player.sendMessage("Incorrect captcha code try again.");
				}
			}
			catch (Exception e)
			{
				player.sendMessage("A problem occured while adding captcha!");
				_log.warn(String.valueOf(e));
			}
		}
		
		// Donate form
		else if (command.startsWith("sendDonateForm"))
		{
			DonateHandler.sendDonateForm(player, command);
		}
		else if (command.startsWith("topFa"))
		{
			if (CustomServerConfigs.TOP_LISTS_RELOAD_TIME > 0)
			{
				showTopFa(player);
			}
			else
			{
				player.sendMessage("Top Lists: Disabled.");
			}
		}
		
		// Top PvP info
		else if (command.startsWith("TopPvp"))
		{
			if (CustomServerConfigs.TOP_LISTS_RELOAD_TIME > 0)
			{
				showTopPvp(player);
			}
			else
			{
				player.sendMessage("Top Lists: Disabled.");
			}
		}
		
		// Top pk info
		else if (command.startsWith("TopPk"))
		{
			if (CustomServerConfigs.TOP_LISTS_RELOAD_TIME > 0)
			{
				showTopPk(player);
			}
			else
			{
				player.sendMessage("Top Lists: Disabled.");
			}
		}
		
		// Top clan info
		else if (command.startsWith("TopClan"))
		{
			if (CustomServerConfigs.TOP_LISTS_RELOAD_TIME > 0)
			{
				showTopClan(player);
			}
			else
			{
				player.sendMessage("Top Lists: Disabled.");
			}
		}
		
		// Grand boss info
		else if (command.startsWith("rbinfo"))
		{
			showRbInfo(player);
		}
		else if (command.startsWith("addAugment"))
		{
			player.sendPacket(new ExShowVariationMakeWindow());
		}
		else if (command.startsWith("delAugment"))
		{
			player.sendPacket(new ExShowVariationCancelWindow());
		}
		else if (command.startsWith("removeAtt"))
		{
			player.sendPacket(new ExShowBaseAttributeCancelWindow(player));
		}
		else if (command.startsWith("drawSymbol"))
		{
			List<L2Henna> tato = HennaData.getInstance().getHennaList(player.getClassId());
			player.sendPacket(new HennaEquipList(player, tato));
		}
		else if (command.startsWith("removeSymbol"))
		{
			boolean hasHennas = false;
			
			for (int i = 1; i <= 3; i++)
			{
				L2Henna henna = player.getHenna(i);
				
				if (henna != null)
				{
					hasHennas = true;
				}
			}
			if (hasHennas)
			{
				player.sendPacket(new HennaRemoveList(player));
			}
			else
			{
				player.sendMessage("You do not have dyes.");
			}
		}
		else if (command.startsWith("adenaToItem"))
		{
			if (player.getAdena() > AioItemsConfigs.AIO_EXCHANGE_PRICE)
			{
				player.destroyItemByItemId("Aio Item", 57, AioItemsConfigs.AIO_EXCHANGE_PRICE, player, true);
				player.addItem("Aio Item", AioItemsConfigs.AIO_EXCHANGE_ID, 1, player, true);
			}
			else
			{
				player.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
			}
			sendPacket(player, "service/exchange.htm");
		}
		else if (command.startsWith("itemToAdena"))
		{
			itemIdToGet = AioItemsConfigs.AIO_EXCHANGE_ID;
			price = 1;
			
			if (Conditions.checkPlayerItemCount(player, itemIdToGet, price))
			{
				player.destroyItemByItemId("Aio Item", AioItemsConfigs.AIO_EXCHANGE_ID, 1, player, true);
				player.addItem("Aio Item", 57, AioItemsConfigs.AIO_EXCHANGE_PRICE, player, true);
			}
			else
			{
				player.sendMessage("Not enough " + ItemData.getInstance().getTemplate(AioItemsConfigs.AIO_EXCHANGE_ID).getName() + ".");
			}
			
			sendPacket(player, "service/exchange.htm");
		}
	}
	
	private static final void showCWithdrawWindow(L2PcInstance player, WarehouseListType itemtype, byte sortorder)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		
		if (!player.hasClanPrivilege(ClanPrivilege.CL_VIEW_WAREHOUSE))
		{
			player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_THE_RIGHT_TO_USE_CLAN_WAREHOUSE);
			return;
		}
		
		player.setActiveWarehouse(player.getClan().getWarehouse());
		
		if (player.getActiveWarehouse().getSize() == 0)
		{
			player.sendPacket(SystemMessageId.NO_ITEM_DEPOSITED_IN_WH);
			return;
		}
		
		if (itemtype != null)
		{
			player.sendPacket(new SortedWareHouseWithdrawalList(player, WareHouseWithdrawalList.CLAN, itemtype, sortorder));
		}
		else
		{
			player.sendPacket(new WareHouseWithdrawalList(player, WareHouseWithdrawalList.CLAN));
		}
		
		if (Config.DEBUG)
		{
			_log.info("Source: L2WarehouseInstance.java; Player: " + player.getName() + "; Command: showRetrieveWindowClan; Message: Showing stored items.");
		}
	}
	
	private static final void showPWithdrawWindow(L2PcInstance player, WarehouseListType itemtype, byte sortorder)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		player.setActiveWarehouse(player.getWarehouse());
		
		if (player.getActiveWarehouse().getSize() == 0)
		{
			player.sendPacket(SystemMessageId.NO_ITEM_DEPOSITED_IN_WH);
			return;
		}
		
		if (itemtype != null)
		{
			player.sendPacket(new SortedWareHouseWithdrawalList(player, WareHouseWithdrawalList.PRIVATE, itemtype, sortorder));
		}
		else
		{
			player.sendPacket(new WareHouseWithdrawalList(player, WareHouseWithdrawalList.PRIVATE));
		}
		
		if (Config.DEBUG)
		{
			_log.info("Source: L2WarehouseInstance.java; Player: " + player.getName() + "; Command: showRetrieveWindow; Message: Showing stored items.");
		}
	}
	
	private void showTopFa(L2PcInstance player)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		TextBuilder sb = new TextBuilder();
		sb.append("<html><title>Top Fa</title><body><center><br>");
		sb.append("<table border=1 width = 280>");
		sb.append("<tr>");
		sb.append("<td><font color=FFD700>No</font></td><td><font color=FFD700>Character Name:</font></td><td><font color=FFD700>Clan Name:</font></td><td><font color=FFD700>Fa Count:</font></td>");
		sb.append("</tr>");
		int count = 1;
		for (PlayersTopData playerData : CustomTable.getInstance().getTopFa())
		{
			String name = playerData.getCharName();
			String cName = playerData.getClanName();
			long countFa = playerData.getFa();
			
			sb.append("<tr>");
			sb.append("<td align=center>" + count + "</td><td>" + name + "</td><td align=center>" + cName + "</td><td align=center>" + countFa + "</td>");
			sb.append("</tr>");
			sb.append("<br>");
			count = count + 1;
		}
		sb.append("</table>");
		sb.append("<br><center>");
		sb.append("<br><img src=L2UI.SquareWhite width=280 height=1>");
		sb.append("<td><button value=\"Back\" action=\"bypass -h npc_%objectId%_Chat_service/toplists.htm\" width=90 height=21 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"></td>");
		sb.append("</center>");
		sb.append("</body></html>");
		html.setHtml(sb.toString());
		html.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(html);
	}
	
	private void showTopPvp(L2PcInstance player)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		TextBuilder sb = new TextBuilder();
		sb.append("<html><title>Top PvP</title><body><center><br>");
		sb.append("<table border=1 width = 280>");
		sb.append("<tr>");
		sb.append("<td><font color=FFD700>No</font></td><td><font color=FFD700>Character Name:</font></td><td><font color=FFD700>Clan Name:</font></td><td><font color=FFD700>PvP Kills:</font></td>");
		sb.append("</tr>");
		int count = 1;
		for (PlayersTopData playerData : CustomTable.getInstance().getTopPvp())
		{
			String name = playerData.getCharName();
			String cName = playerData.getClanName();
			int pvp = playerData.getPvp();
			
			sb.append("<tr>");
			sb.append("<td align=center>" + count + "</td><td>" + name + "</td><td align=center>" + cName + "</td><td align=center>" + pvp + "</td>");
			sb.append("</tr>");
			sb.append("<br>");
			count = count + 1;
		}
		sb.append("</table>");
		sb.append("<br><center>");
		sb.append("<br><img src=L2UI.SquareWhite width=280 height=1>");
		sb.append("<td><button value=\"Back\" action=\"bypass -h npc_%objectId%_Chat_service/toplists.htm\" width=90 height=21 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"></td>");
		sb.append("</center>");
		sb.append("</body></html>");
		html.setHtml(sb.toString());
		html.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(html);
	}
	
	private void showTopPk(L2PcInstance player)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		TextBuilder sb = new TextBuilder();
		sb.append("<html><title>Top Pk</title><body><center><br>");
		sb.append("<table border=1 width = 280>");
		sb.append("<tr>");
		sb.append("<td><font color=FFD700>No</font></td><td><font color=FFD700>Character Name:</font></td><td><font color=FFD700>Pk Kills:</font></td>");
		sb.append("</tr>");
		int count = 1;
		for (PlayersTopData playerData : CustomTable.getInstance().getTopPk())
		{
			String name = playerData.getCharName();
			int pvp = playerData.getPk();
			
			sb.append("<tr>");
			sb.append("<td align=center>" + count + "</td><td>" + name + "</td><td align=center>" + pvp + "</td>");
			sb.append("</tr>");
			sb.append("<br>");
			count = count + 1;
		}
		sb.append("</table>");
		sb.append("<br><center>");
		sb.append("<br><img src=L2UI.SquareWhite width=280 height=1>");
		sb.append("<td><button value=\"Back\" action=\"bypass -h npc_%objectId%_Chat_service/toplists.htm\" width=90 height=21 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"></td>");
		sb.append("</center>");
		sb.append("</body></html>");
		html.setHtml(sb.toString());
		html.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(html);
	}
	
	private void showTopClan(L2PcInstance player)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		TextBuilder sb = new TextBuilder();
		sb.append("<html><title>Top Clan</title><body><center><br>");
		sb.append("<table border=1 width = 280>");
		sb.append("<tr>");
		sb.append("<td><font color=FFD700>No</font></td><td><font color=FFD700>Leader's Name:</font></td><td><font color=FFD700>Clan Name:</font></td><td><font color=FFD700>Clan Level:</font></td>");
		sb.append("</tr>");
		int count = 1;
		for (PlayersTopData playerData : CustomTable.getInstance().getTopClan())
		{
			String name = playerData.getCharName();
			String cName = playerData.getClanName();
			int cLevel = playerData.getClanLevel();
			
			sb.append("<tr>");
			sb.append("<td align=center>" + count + "</td><td>" + name + "</td><td align=center>" + cName + "</td><td align=center>" + cLevel + "</td>");
			sb.append("</tr>");
			sb.append("<br>");
			count = count + 1;
		}
		sb.append("</table>");
		sb.append("<br><center>");
		sb.append("<br><img src=L2UI.SquareWhite width=280 height=1>");
		sb.append("<td><button value=\"Back\" action=\"bypass -h npc_%objectId%_Chat_service/toplists.htm\" width=90 height=21 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"></td>");
		sb.append("</center>");
		sb.append("</body></html>");
		html.setHtml(sb.toString());
		html.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(html);
	}
	
	private final void showRbInfo(L2PcInstance player)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		TextBuilder tb = new TextBuilder();
		tb.append("<html><title>Rb Info</title><body><br>");
		tb.append("<br>");
		tb.append("<font color=00FFFF>Grand Boss Info</font>");
		tb.append("<center>");
		tb.append("<img src=L2UI.SquareGray width=280 height=1><br>");
		tb.append("</center>");
		tb.append("<br>");
		tb.append("<center>");
		tb.append("<table width = 280>");
		for (int boss : BOSSES)
		{
			String name = NpcTable.getInstance().getTemplate(boss).getName();
			long delay = GrandBossManager.getInstance().getStatsSet(boss).getLong("respawn_time");
			if (delay <= System.currentTimeMillis())
			{
				tb.append("<tr>");
				tb.append("<td><font color=\"00C3FF\">" + name + "</color>:</td> " + "<td><font color=\"9CC300\">Is Alive</color></td>" + "<br1>");
				tb.append("</tr>");
			}
			else
			{
				int hours = (int) ((delay - System.currentTimeMillis()) / 1000 / 60 / 60);
				int mins = (int) (((delay - (hours * 60 * 60 * 1000)) - System.currentTimeMillis()) / 1000 / 60);
				int seconts = (int) (((delay - ((hours * 60 * 60 * 1000) + (mins * 60 * 1000))) - System.currentTimeMillis()) / 1000);
				tb.append("<tr>");
				tb.append("<td><font color=\"00C3FF\">" + name + "</color></td>" + "<td><font color=\"FFFFFF\">" + " " + "Respawn in :</color></td>" + " " + "<td><font color=\"32C332\">" + hours + " : " + mins + " : " + seconts + "</color></td><br1>");
				tb.append("</tr>");
			}
		}
		tb.append("</table>");
		tb.append("</center>");
		tb.append("<br><center>");
		tb.append("<br><img src=L2UI.SquareWhite width=280 height=1>");
		tb.append("<td><button value=\"Back\" action=\"bypass -h npc_%objectId%_Chat_service/services.htm\" width=90 height=21 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"></td>");
		tb.append("</center>");
		tb.append("</body></html>");
		html.setHtml(tb.toString());
		html.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(html);
	}
	
	public void showAchievementMain(L2PcInstance player, int val)
	{
		TextBuilder tb = new TextBuilder();
		tb.append("<html><title>Achievements Manager</title><body><center><br>");
		tb.append("Hello <font color=\"LEVEL\">" + player.getName() + "</font><br>Are you looking for challenge?");
		tb.append("<br><img src=\"L2UI.SquareWhite\" width=\"280\" height=\"1\"><br><br>");
		tb.append("<button value=\"My Achievements\" action=\"bypass -h npc_%objectId%_showMyAchievements\" width=160 height=32 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\">");
		tb.append("<button value=\"Statistics\" action=\"bypass -h npc_%objectId%_showAchievementStats\" width=160 height=32 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\">");
		tb.append("<button value=\"Help\" action=\"bypass -h npc_%objectId%_showAchievementHelp\" width=160 height=32 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\">");
		tb.append("<br><br><img src=\"L2UI.SquareWhite\" width=\"280\" height=\"1\">");
		tb.append("<td><button value=\"Back\" action=\"bypass -h npc_%objectId%_Chat_service/services.htm\" width=90 height=21 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"></td>");
		NpcHtmlMessage msg = new NpcHtmlMessage(this.getObjectId());
		msg.setHtml(tb.toString());
		msg.replace("%objectId%", String.valueOf(this.getObjectId()));
		
		player.sendPacket(msg);
	}
	
	private void showMyAchievements(L2PcInstance player)
	{
		TextBuilder tb = new TextBuilder();
		tb.append("<html><title>Achievements Manager</title><body><br>");
		
		tb.append("<center><font color=\"LEVEL\">My achievements</font>:</center><br>");
		
		if (AchievementsManager.getInstance().getAchievementList().isEmpty())
		{
			tb.append("There are no Achievements created yet!");
		}
		else
		{
			int i = 0;
			
			tb.append("<table width=280 border=0 bgcolor=\"33FF33\">");
			tb.append("<tr><td width=115 align=\"left\">Name:</td><td width=50 align=\"center\">Info:</td><td width=115 align=\"center\">Status:</td></tr></table>");
			tb.append("<br><img src=\"l2ui.squaregray\" width=\"280\" height=\"1\"><br>");
			
			for (Achievement a : AchievementsManager.getInstance().getAchievementList().values())
			{
				tb.append(getTableColor(i));
				tb.append("<tr><td width=115 align=\"left\">" + a.getName() + "</td><td width=50 align=\"center\"><a action=\"bypass -h npc_%objectId%_showAchievementInfo " + a.getId() + "\">info</a></td><td width=115 align=\"center\">" + getStatusString(a.getId(), player) + "</td></tr></table>");
				i++;
			}
			
			tb.append("<br><img src=\"l2ui.squaregray\" width=\"280\" height=\"1s\"><br>");
			tb.append("<center><button value=\"Back\" action=\"bypass -h npc_%objectId%_showAchievementMain\" width=160 height=32 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"></center>");
		}
		
		NpcHtmlMessage msg = new NpcHtmlMessage(this.getObjectId());
		msg.setHtml(tb.toString());
		msg.replace("%objectId%", String.valueOf(this.getObjectId()));
		
		player.sendPacket(msg);
	}
	
	private void showAchievementInfo(int achievementID, L2PcInstance player)
	{
		Achievement a = AchievementsManager.getInstance().getAchievementList().get(achievementID);
		
		TextBuilder tb = new TextBuilder();
		tb.append("<html><title>Achievements Manager</title><body><br>");
		
		tb.append("<center><table width=270 border=0 bgcolor=\"33FF33\">");
		tb.append("<tr><td width=270 align=\"center\">" + a.getName() + "</td></tr></table><br>");
		tb.append("Status: " + getStatusString(achievementID, player));
		
		if (a.meetAchievementRequirements(player) && !player.getCompletedAchievements().contains(achievementID))
		{
			tb.append("<button value=\"Receive Reward!\" action=\"bypass -h npc_%objectId%_achievementGetReward " + a.getId() + "\" width=160 height=32 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\">");
		}
		
		tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1s\"><br>");
		
		tb.append("<table width=270 border=0 bgcolor=\"33FF33\">");
		tb.append("<tr><td width=270 align=\"center\">Description</td></tr></table><br>");
		tb.append(a.getDescription());
		tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1s\"><br>");
		
		tb.append("<table width=280 border=0 bgcolor=\"33FF33\">");
		tb.append("<tr><td width=120 align=\"left\">Condition To Meet:</td><td width=55 align=\"center\">Value:</td><td width=95 align=\"center\">Status:</td></tr></table>");
		tb.append(getConditionsStatus(achievementID, player));
		tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1s\"><br>");
		tb.append("<center><button value=\"Back\" action=\"bypass -h npc_%objectId%_showMyAchievements\" width=160 height=32 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"></center>");
		
		NpcHtmlMessage msg = new NpcHtmlMessage(this.getObjectId());
		msg.setHtml(tb.toString());
		msg.replace("%objectId%", String.valueOf(this.getObjectId()));
		
		player.sendPacket(msg);
	}
	
	private void showAchievementStats(L2PcInstance player)
	{
		TextBuilder tb = new TextBuilder();
		tb.append("<html><title>Achievements Manager</title><body><center><br>");
		tb.append("Check your <font color=\"LEVEL\">Achievements </font>statistics:");
		tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1\"><br>");
		
		AchievementsHandler.getAchievemntData(player);
		int completedCount = player.getCompletedAchievements().size();
		
		tb.append("You have completed: " + completedCount + "/<font color=\"LEVEL\">" + AchievementsManager.getInstance().getAchievementList().size() + "</font>");
		
		tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1s\"><br>");
		tb.append("<center><button value=\"Back\" action=\"bypass -h npc_%objectId%_showAchievementMain\" width=160 height=32 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"></center>");
		
		NpcHtmlMessage msg = new NpcHtmlMessage(this.getObjectId());
		msg.setHtml(tb.toString());
		msg.replace("%objectId%", String.valueOf(this.getObjectId()));
		
		player.sendPacket(msg);
	}
	
	private void showAchievementHelp(L2PcInstance player)
	{
		TextBuilder tb = new TextBuilder();
		tb.append("<html><title>Achievements Manager</title><body><center><br>");
		tb.append("Achievements  <font color=\"LEVEL\">Help </font>page:");
		tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1\"><br>");
		
		tb.append("You can check status of your achievements, receive reward if every condition of achievement is meet, if not you can check which condition is still not meet, by using info button");
		tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1s\"><br>");
		tb.append("<font color=\"FF0000\">Not Completed</font> - you did not meet the achivement requirements.<br>");
		tb.append("<font color=\"LEVEL\">Get Reward</font> - you may receive reward, click info.<br>");
		tb.append("<font color=\"5EA82E\">Completed</font> - achievement completed, reward received.<br>");
		
		tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1s\"><br>");
		tb.append("<center><button value=\"Back\" action=\"bypass -h npc_%objectId%_showAchievementMain\" width=160 height=32 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"></center>");
		
		NpcHtmlMessage msg = new NpcHtmlMessage(this.getObjectId());
		msg.setHtml(tb.toString());
		msg.replace("%objectId%", String.valueOf(this.getObjectId()));
		
		player.sendPacket(msg);
	}
	
	private String getStatusString(int achievementID, L2PcInstance player)
	{
		if (player.getCompletedAchievements().contains(achievementID))
		{
			return "<font color=\"5EA82E\">Completed</font>";
		}
		if (AchievementsManager.getInstance().getAchievementList().get(achievementID).meetAchievementRequirements(player))
		{
			return "<font color=\"LEVEL\">Get Reward</font>";
		}
		return "<font color=\"FF0000\">Not Completed</font>";
	}
	
	private String getTableColor(int i)
	{
		if ((i % 2) == 0)
		{
			return "<table width=280 border=0 bgcolor=\"444444\">";
		}
		return "<table width=280 border=0>";
	}
	
	private String getConditionsStatus(int achievementID, L2PcInstance player)
	{
		int i = 0;
		String s = "</center>";
		Achievement a = AchievementsManager.getInstance().getAchievementList().get(achievementID);
		String completed = "<font color=\"5EA82E\">Completed</font></td></tr></table>";
		String notcompleted = "<font color=\"FF0000\">Not Completed</font></td></tr></table>";
		
		for (Condition c : a.getConditions())
		{
			s += getTableColor(i);
			s += "<tr><td width=120 align=\"left\">" + c.getType().getText() + "</td><td width=55 align=\"center\">" + c.getValue() + "</td><td width=95 align=\"center\">";
			i++;
			
			if (c.meetConditionRequirements(player))
			{
				s += completed;
			}
			else
			{
				s += notcompleted;
			}
		}
		return s;
	}
}