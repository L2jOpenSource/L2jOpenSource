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
import l2r.gameserver.datatables.sql.ClanTable;
import l2r.gameserver.datatables.xml.ClassListData;
import l2r.gameserver.datatables.xml.HennaData;
import l2r.gameserver.enums.InstanceType;
import l2r.gameserver.enums.PcRace;
import l2r.gameserver.instancemanager.CastleManager;
import l2r.gameserver.model.ClanPrivilege;
import l2r.gameserver.model.L2Clan;
import l2r.gameserver.model.L2ClanMember;
import l2r.gameserver.model.actor.FakePc;
import l2r.gameserver.model.actor.templates.L2NpcTemplate;
import l2r.gameserver.model.base.PlayerClass;
import l2r.gameserver.model.base.SubClass;
import l2r.gameserver.model.entity.Castle;
import l2r.gameserver.model.items.L2Henna;
import l2r.gameserver.model.olympiad.OlympiadManager;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.ActionFailed;
import l2r.gameserver.network.serverpackets.ExShowBaseAttributeCancelWindow;
import l2r.gameserver.network.serverpackets.ExShowVariationCancelWindow;
import l2r.gameserver.network.serverpackets.ExShowVariationMakeWindow;
import l2r.gameserver.network.serverpackets.HennaEquipList;
import l2r.gameserver.network.serverpackets.HennaRemoveList;
import l2r.gameserver.network.serverpackets.MagicSkillLaunched;
import l2r.gameserver.network.serverpackets.MagicSkillUse;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.network.serverpackets.SiegeInfo;
import l2r.gameserver.network.serverpackets.SortedWareHouseWithdrawalList;
import l2r.gameserver.network.serverpackets.SortedWareHouseWithdrawalList.WarehouseListType;
import l2r.gameserver.network.serverpackets.SystemMessage;
import l2r.gameserver.network.serverpackets.WareHouseDepositList;
import l2r.gameserver.network.serverpackets.WareHouseWithdrawalList;
import l2r.util.StringUtil;
import gr.reunion.achievementEngine.AchievementsHandler;
import gr.reunion.achievementEngine.AchievementsManager;
import gr.reunion.achievementEngine.base.Achievement;
import gr.reunion.achievementEngine.base.Condition;
import gr.reunion.main.Conditions;
import gr.reunion.securityEngine.SecurityActions;
import gr.reunion.securityEngine.SecurityType;

/**
 * @author -=DoctorNo=-
 */
public final class L2ServicesManagerInstance extends L2NpcInstance
{
	/**
	 * @param objectId
	 * @param template
	 */
	public L2ServicesManagerInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		setInstanceType(InstanceType.L2ServicesManagerInstance);
		FakePc fpc = getFakePc();
		if (fpc != null)
		{
			setTitle(fpc.title);
		}
	}
	
	/**
	 * Method to send the html to char
	 * @param player
	 * @param html
	 */
	public void sendPacket(L2PcInstance player, String html)
	{
		NpcHtmlMessage msg = new NpcHtmlMessage(getObjectId());
		msg.setFile(player.getHtmlPrefix(), "/data/html/ServicesManager/" + html);
		msg.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(msg);
	}
	
	@Override
	public void showChatWindow(L2PcInstance player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(player.getHtmlPrefix(), "data/html/ServicesManager/main.htm");
		html.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(html);
	}
	
	protected static String getSubClassMenu(PcRace pRace)
	{
		if (Config.ALT_GAME_SUBCLASS_EVERYWHERE || (pRace != PcRace.Kamael))
		{
			return "data/html/ServicesManager/subclass/SubClass.htm";
		}
		
		return "data/html/ServicesManager/subclass/SubClass_NoOther.htm";
	}
	
	protected static String getSubClassFail()
	{
		return "data/html/ServicesManager/subclass/SubClass_Fail.htm";
	}
	
	/**
	 * Method to manage all player bypasses
	 * @param player
	 * @param command
	 */
	@Override
	public void onBypassFeedback(final L2PcInstance player, String command)
	{
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
		
		String[] subCommand = command.split("_");
		String[] commandStr = command.split(" ");
		String actualCommand = commandStr[0];
		
		String cmdParams = "";
		String cmdParams2 = "";
		
		if (commandStr.length >= 2)
		{
			cmdParams = commandStr[1];
		}
		if (commandStr.length >= 3)
		{
			cmdParams2 = commandStr[2];
		}
		
		// Page navigation, html command how to starts
		if (command.startsWith("Chat"))
		{
			if (subCommand[1].isEmpty() || (subCommand[1] == null))
			{
				return;
			}
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			html.setFile(player.getHtmlPrefix(), "data/html/ServicesManager/" + subCommand[1]);
			html.replace("%objectId%", String.valueOf(getObjectId()));
			player.sendPacket(html);
		}
		
		// Warehouse
		else if (command.toLowerCase().startsWith("withdrawp")) // WithdrawP
		{
			player.setIsUsingAioWh(true);
			if (Config.L2JMOD_ENABLE_WAREHOUSESORTING_PRIVATE)
			{
				NpcHtmlMessage msg = new NpcHtmlMessage(getObjectId());
				msg.setFile(player.getHtmlPrefix(), "data/html/ServicesManager/warehouse/WhSortedP.htm");
				msg.replace("%objectId%", String.valueOf(getObjectId()));
				player.sendPacket(msg);
			}
			else
			{
				showPWithdrawWindow(player, null, (byte) 0);
			}
			
			return;
		}
		else if (command.toLowerCase().startsWith("withdrawsortedp")) // WithdrawSortedP
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
		else if (command.toLowerCase().startsWith("withdrawc")) // WithdrawC
		{
			player.setIsUsingAioWh(true);
			if (Config.L2JMOD_ENABLE_WAREHOUSESORTING_CLAN)
			{
				NpcHtmlMessage msg = new NpcHtmlMessage(getObjectId());
				msg.setFile(player.getHtmlPrefix(), "data/html/ServicesManager/warehouse/WhSortedC.htm");
				msg.replace("%objectId%", String.valueOf(getObjectId()));
				player.sendPacket(msg);
			}
			else
			{
				showCWithdrawWindow(player, null, (byte) 0);
			}
			
			return;
		}
		else if (command.toLowerCase().startsWith("withdrawsortedc")) // WithdrawSortedC
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
			else if (player.isCursedWeaponEquipped())
			{
				player.sendMessage("You can`t change Subclass while Cursed weapon equiped!");
				return;
			}
			final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			// Subclasses may not be changed while a transformated state.
			if (player.getTransformation() != null)
			{
				html.setFile(player.getHtmlPrefix(), "data/html/ServicesManager/subclass/SubClass_NoTransformed.htm");
				player.sendPacket(html);
				return;
			}
			// Subclasses may not be changed while a summon is active.
			if (player.getSummon() != null)
			{
				html.setFile(player.getHtmlPrefix(), "data/html/ServicesManager/subclass/SubClass_NoSummon.htm");
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
				_log.warn(L2ServicesManagerInstance.class.getName() + ": Wrong numeric values for command " + command);
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
						html.setFile(player.getHtmlPrefix(), "data/html/ServicesManager/subclass/SubClass_Add.htm");
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
							html.setFile(player.getHtmlPrefix(), "data/html/ServicesManager/subclass/SubClass_Fail_Elves.htm");
							player.sendPacket(html);
						}
						else if (player.getRace() == PcRace.Kamael)
						{
							html.setFile(player.getHtmlPrefix(), "data/html/ServicesManager/subclass/SubClass_Fail_Kamael.htm");
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
						html.setFile(player.getHtmlPrefix(), "data/html/ServicesManager/subclass/SubClass_ChangeNo.htm");
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
							html.setFile(player.getHtmlPrefix(), "data/html/ServicesManager/subclass/SubClass_Change.htm");
							html.replace("%list%", content2.toString());
						}
						else
						{
							html.setFile(player.getHtmlPrefix(), "data/html/ServicesManager/subclass/SubClass_ChangeNotFound.htm");
						}
					}
					break;
				case 3: // Change/Cancel Subclass - Initial
					if ((player.getSubClasses() == null) || player.getSubClasses().isEmpty())
					{
						html.setFile(player.getHtmlPrefix(), "data/html/ServicesManager/subclass/SubClass_ModifyEmpty.htm");
						break;
					}
					
					// custom value
					if ((player.getTotalSubClasses() > 3) || (Config.MAX_SUBCLASS > 3))
					{
						html.setFile(player.getHtmlPrefix(), "data/html/ServicesManager/subclass/SubClass_ModifyCustom.htm");
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
						html.setFile(player.getHtmlPrefix(), "data/html/ServicesManager/subclass/SubClass_Modify.htm");
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
						_log.warn(L2ServicesManagerInstance.class.getName() + ": Player " + player.getName() + " has performed a subclass change too fast");
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
						
						html.setFile(player.getHtmlPrefix(), "data/html/ServicesManager/subclass/SubClass_AddOk.htm");
						
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
						_log.warn(L2ServicesManagerInstance.class.getName() + ": Player " + player.getName() + " has performed a subclass change too fast");
						return;
					}
					
					if (player.getClassIndex() == paramOne)
					{
						html.setFile(player.getHtmlPrefix(), "data/html/ServicesManager/subclass/SubClass_Current.htm");
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
							html.setFile(player.getHtmlPrefix(), "data/html/ServicesManager/subclass/SubClass_ModifyChoice1.htm");
							break;
						case 2:
							html.setFile(player.getHtmlPrefix(), "data/html/ServicesManager/subclass/SubClass_ModifyChoice2.htm");
							break;
						case 3:
							html.setFile(player.getHtmlPrefix(), "data/html/ServicesManager/subclass/SubClass_ModifyChoice3.htm");
							break;
						default:
							html.setFile(player.getHtmlPrefix(), "data/html/ServicesManager/subclass/SubClass_ModifyChoice.htm");
					}
					html.replace("%list%", content6.toString());
					break;
				case 7: // Change Subclass - Action
					/**
					 * Warning: the information about this subclass will be removed from the subclass list even if false!
					 */
					if (!player.getFloodProtectors().getSubclass().tryPerformAction("change class"))
					{
						_log.warn(L2ServicesManagerInstance.class.getName() + ": Player " + player.getName() + " has performed a subclass change too fast");
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
						
						html.setFile(player.getHtmlPrefix(), "data/html/ServicesManager/subclass/SubClass_ModifyOk.htm");
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
		
		// Clan and Alliance Commands
		else if (command.startsWith("createclan"))
		{
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
		else if (actualCommand.equalsIgnoreCase("createacademy"))
		{
			if (cmdParams.isEmpty())
			{
				return;
			}
			
			L2VillageMasterInstance.createSubPledge(player, cmdParams, null, L2Clan.SUBUNIT_ACADEMY, 5);
		}
		else if (actualCommand.equalsIgnoreCase("renamepledge"))
		{
			if (cmdParams.isEmpty() || cmdParams2.isEmpty())
			{
				return;
			}
			
			L2VillageMasterInstance.renameSubPledge(player, Integer.parseInt(cmdParams), cmdParams2);
		}
		else if (actualCommand.equalsIgnoreCase("createroyal"))
		{
			if (cmdParams.isEmpty())
			{
				return;
			}
			
			L2VillageMasterInstance.createSubPledge(player, cmdParams, cmdParams2, L2Clan.SUBUNIT_ROYAL1, 6);
		}
		else if (actualCommand.equalsIgnoreCase("createknight"))
		{
			if (cmdParams.isEmpty())
			{
				return;
			}
			
			L2VillageMasterInstance.createSubPledge(player, cmdParams, cmdParams2, L2Clan.SUBUNIT_KNIGHT1, 7);
		}
		else if (actualCommand.equalsIgnoreCase("assignsubplleader"))
		{
			if (cmdParams.isEmpty())
			{
				return;
			}
			
			L2VillageMasterInstance.assignSubPledgeLeader(player, cmdParams, cmdParams2);
		}
		else if (actualCommand.equalsIgnoreCase("createally"))
		{
			if (!checkLeaderConditions(player))
			{
				return;
			}
			if (cmdParams.isEmpty())
			{
				return;
			}
			
			if (player.getClan() == null)
			{
				player.sendPacket(SystemMessageId.ONLY_CLAN_LEADER_CREATE_ALLIANCE);
			}
			else
			{
				player.getClan().createAlly(player, cmdParams);
			}
		}
		else if (command.equalsIgnoreCase("dissolveally"))
		{
			player.getClan().dissolveAlly(player);
		}
		else if (actualCommand.equalsIgnoreCase("dissolveclan"))
		{
			L2VillageMasterInstance.dissolveClan(player, player.getClanId());
		}
		else if (actualCommand.equalsIgnoreCase("changeclanleader"))
		{
			if (cmdParams.isEmpty())
			{
				return;
			}
			
			if (!player.isClanLeader())
			{
				player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
				return;
			}
			
			if (player.getName().equalsIgnoreCase(cmdParams))
			{
				return;
			}
			
			final L2Clan clan = player.getClan();
			final L2ClanMember member = clan.getClanMember(cmdParams);
			if (member == null)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DOES_NOT_EXIST);
				sm.addString(cmdParams);
				player.sendPacket(sm);
				return;
			}
			
			if (!member.isOnline())
			{
				player.sendPacket(SystemMessageId.INVITED_USER_NOT_ONLINE);
				return;
			}
			
			// To avoid clans with null clan leader, academy members shouldn't be eligible for clan leader.
			if (member.getPlayerInstance().isAcademyMember())
			{
				player.sendPacket(SystemMessageId.RIGHT_CANT_TRANSFERRED_TO_ACADEMY_MEMBER);
				return;
			}
			
			if (Config.ALT_CLAN_LEADER_INSTANT_ACTIVATION)
			{
				clan.setNewLeader(member);
			}
			else
			{
				final NpcHtmlMessage msg = new NpcHtmlMessage(getObjectId());
				if (clan.getNewLeaderId() == 0)
				{
					clan.setNewLeaderId(member.getObjectId(), true);
					msg.setFile(player.getHtmlPrefix(), "data/scripts/ai/npc/VillageMasters/Clan/9000-07-success.htm");
				}
				else
				{
					msg.setFile(player.getHtmlPrefix(), "data/scripts/ai/npc/VillageMasters/Clan/9000-07-in-progress.htm");
				}
				player.sendPacket(msg);
			}
		}
		else if (actualCommand.equalsIgnoreCase("recoverclan"))
		{
			L2VillageMasterInstance.recoverClan(player, player.getClanId());
		}
		else if (command.equalsIgnoreCase("increaseclanlevel"))
		{
			if (player.getClan() != null)
			{
				if (player.getClan().levelUpClan(player))
				{
					player.broadcastPacket(new MagicSkillUse(player, 5103, 1, 0, 0));
					player.broadcastPacket(new MagicSkillLaunched(player, 5103, 1));
				}
			}
			else
			{
				player.sendMessage("You must create clan first.");
			}
		}
		else if (actualCommand.equalsIgnoreCase("learnclanskills"))
		{
			if (!checkLeaderConditions(player))
			{
				return;
			}
			L2VillageMasterInstance.showPledgeSkillList(player);
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
	
	public void showAchievementMain(L2PcInstance player, int val)
	{
		TextBuilder tb = new TextBuilder();
		tb.append("<html><body>");
		tb.append("<title>Services manager</title>");
		tb.append("<center><br>");
		tb.append("<img src=\"l2ui.SquareGray\" width=270 height=1><br1>");
		tb.append("<table width=\"268\" cellpadding=\"5\" bgcolor=\"151515\">");
		tb.append("<tr>");
		tb.append("<td valign=\"top\"><center><font color=\"EBDF6C\">Achievements manager</font><br>Are you looking for a challenge ? Then here you are , complete Achievements and get special rewards to grow up easier.</center></td>");
		tb.append("</tr>");
		tb.append("</table><br1>");
		tb.append("<img src=\"l2ui.SquareGray\" width=270 height=1><br>");
		tb.append("</center><br><br><center><img src=\"l2ui.SquareGray\" width=270 height=1><br>");
		tb.append("<table width=180>");
		tb.append("<tr><td><button value=\"Show Achievements\" action=\"bypass -h npc_%objectId%_showMyAchievements\"width=180 height=24 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr><br>");
		tb.append("<tr><td><button value=\"How it works?\" action=\"bypass -h npc_%objectId%_showAchievementHelp\" width=180 height=24 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr><br>");
		tb.append("</table>");
		tb.append("<br><img src=\"l2ui.SquareGray\" width=270 height=1><br1><table width=\"273\" bgcolor=\"151515\"><tr><td><center><font color=\"EBDF6C\"> Your Achievements Statistics</font></center></td></tr></table><br1><img src=\"l2ui.SquareGray\" width=270 height=1><br><br>");
		AchievementsHandler.getAchievemntData(player);
		int completedCount = player.getCompletedAchievements().size();
		
		tb.append("<table width=\"280\"><tr><td>From here you will be informed how many achievements you have already completed.<br>You have completed: " + completedCount + "/<font color=\"EBDF6C\">" + AchievementsManager.getInstance().getAchievementList().size() + "</font></td></tr></table>");
		tb.append("<br><img src=\"l2ui.SquareGray\" width=270 height=1><br>");
		tb.append("<button action=\"bypass -h npc_%objectId%_Chat_main.htm\" value=\"Back\" width=90 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
		
		NpcHtmlMessage msg = new NpcHtmlMessage(this.getObjectId());
		msg.setHtml(tb.toString());
		msg.replace("%objectId%", String.valueOf(this.getObjectId()));
		
		player.sendPacket(msg);
	}
	
	private void showMyAchievements(L2PcInstance player)
	{
		TextBuilder tb = new TextBuilder();
		
		tb.append("<html><title>Services manager</title><body><br>");
		tb.append("<center><img src=\"l2ui.SquareGray\" width=270 height=1><br1>");
		tb.append("<table width=\"270\" cellpadding=\"5\" bgcolor=\"151515\">");
		tb.append("<tr>");
		tb.append("<td valign=\"top\"><center><font color=\"EBDF6C\">Achievements manager</font><br>Are you looking for a challenge ? Then here you are , complete Achievements and get special rewards to grow up easier.</center></td>");
		tb.append("</tr>");
		tb.append("</table><br1>");
		tb.append("<img src=\"l2ui.SquareGray\" width=270 height=1><br>");
		tb.append("</center><br><br>");
		
		if (AchievementsManager.getInstance().getAchievementList().isEmpty())
		{
			tb.append("There are no Achievements created yet!");
		}
		else
		{
			int i = 0;
			
			tb.append("<img src=\"l2ui.SquareGray\" width=280 height=1><br1>");
			tb.append("<table width=280 border=0 bgcolor=\"151515\">");
			tb.append("<tr><td width=115 align=\"left\"><font color=\"EBDF6C\">Name</font></td><td width=50 align=\"center\"><font color=\"EBDF6C\">Info</font></td><td width=115 align=\"center\"><font color=\"EBDF6C\">Status</font></td></tr></table>");
			tb.append("<br1><img src=\"l2ui.SquareGray\" width=280 height=1><br>");
			
			for (Achievement a : AchievementsManager.getInstance().getAchievementList().values())
			{
				tb.append(getTableColor(i));
				tb.append("<tr><td width=115 align=\"left\">" + a.getName() + "</td><td width=50 align=\"center\"><a action=\"bypass -h npc_%objectId%_showAchievementInfo " + a.getId() + "\">info</a></td><td width=115 align=\"center\">" + getStatusString(a.getId(), player) + "</td></tr></table>");
				i++;
			}
			
			tb.append("<br><img src=\"l2ui.squaregray\" width=\"280\" height=\"1s\"><br>");
			tb.append("<center><button value=\"Back\" action=\"bypass -h npc_%objectId%_showAchievementMain\" width=90 height=21 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"></center>");
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
		
		tb.append("<html><title>Services manager</title><body><br>");
		tb.append("<center><img src=\"l2ui.SquareGray\" width=270 height=1><br1>");
		tb.append("<table width=\"270\" cellpadding=\"5\" bgcolor=\"151515\">");
		tb.append("<tr>");
		tb.append("<td valign=\"top\"><center><font color=\"EBDF6C\">Achievements manager</font><br>Are you looking for a challenge ? Then here you are , complete Achievements and get special rewards to grow up easier.</center></td>");
		tb.append("</tr>");
		tb.append("</table><br1><center><img src=\"l2ui.SquareGray\" width=270 height=1></center><br><br>");
		tb.append("</center>");
		tb.append("<center><img src=\"l2ui.SquareGray\" width=270 height=1><br1><table width=270 border=0 bgcolor=\"151515\">");
		tb.append("<tr><td width=270 align=\"center\"><font color=\"EBDF6C\">" + a.getName() + "</font></td></tr></table><br1><img src=\"l2ui.SquareGray\" width=270 height=1><br>");
		tb.append("Status: " + getStatusString(achievementID, player));
		if (a.meetAchievementRequirements(player) && !player.getCompletedAchievements().contains(achievementID))
		{
			tb.append("<button value=\"Receive Reward!\" action=\"bypass -h npc_%objectId%_achievementGetReward " + a.getId() + "\" width=160 height=32 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\">");
		}
		tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1s\"><br1>");
		tb.append("<table width=270 border=0 bgcolor=\"151515\">");
		tb.append("<tr><td width=270 align=\"center\"><font color=\"EBDF6C\">Description</font></td></tr></table><br1><img src=\"l2ui.SquareGray\" width=270 height=1><br>");
		tb.append(a.getDescription());
		tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1s\"><br1>");
		tb.append("<table width=270 border=0 bgcolor=\"151515\">");
		tb.append("<tr><td width=120 align=\"left\"><font color=\"EBDF6C\">Condition To Meet</font></td><td width=55 align=\"center\"><font color=\"EBDF6C\">Value</font></td><td width=95 align=\"center\"><font color=\"EBDF6C\">Status</font></td></tr></table><br1><img src=\"l2ui.SquareGray\" width=270 height=1><br>");
		tb.append(getConditionsStatus(achievementID, player));
		tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1s\"><br>");
		tb.append("<center><button value=\"Back\" action=\"bypass -h npc_%objectId%_showMyAchievements\" width=90 height=21 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"></center>");
		
		NpcHtmlMessage msg = new NpcHtmlMessage(this.getObjectId());
		msg.setHtml(tb.toString());
		msg.replace("%objectId%", String.valueOf(this.getObjectId()));
		
		player.sendPacket(msg);
	}
	
	private void showAchievementHelp(L2PcInstance player)
	{
		TextBuilder tb = new TextBuilder();
		tb.append("<html><title>Services manager</title><body><center><br>");
		tb.append("<center><img src=\"l2ui.SquareGray\" width=270 height=1><br1>");
		tb.append("<table width=\"270\" cellpadding=\"5\" bgcolor=\"151515\">");
		tb.append("<tr>");
		tb.append("<td valign=\"top\"><center><font color=\"EBDF6C\">Achievements manager</font><br>Are you looking for a challenge ? Then here you are , complete Achievements and get special rewards to grow up easier.</center></td>");
		tb.append("</tr>");
		tb.append("</table><br1><img src=\"l2ui.SquareGray\" width=270 height=1>");
		tb.append("</center><br><br>");
		
		tb.append("<br><img src=\"l2ui.SquareGray\" width=270 height=1><br1><table width=\"295\" bgcolor=\"151515\"><tr><td><center><font color=\"EBDF6C\">Usefull Info</font></center></td></tr></table><br1><img src=\"l2ui.SquareGray\" width=270 height=1><br><br>");
		tb.append("You can check status of your achievements, receive reward if every condition of achievement is meet, if not you can check which condition is still not meet, by using info button");
		tb.append("<br><br><img src=\"l2ui.SquareGray\" width=270 height=1><br1><table width=\"276\" bgcolor=\"151515\"><tr><td><center><font color=\"EBDF6C\">Conditions Meanings</font></center></td></tr></table><br1><img src=\"l2ui.SquareGray\" width=270 height=1><br><br>");
		tb.append("</center><font color=\"FF0000\">Not Completed</font> - you did not meet the requirements<br1>");
		tb.append("<font color=\"LEVEL\">Get Reward</font> - you may receive reward, click info<br1>");
		tb.append("<font color=\"5EA82E\">Completed</font> - achievement completed, reward received<br><center>");
		
		tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1s\"><br>");
		tb.append("<center><button value=\"Back\" action=\"bypass -h npc_%objectId%_showAchievementMain\" width=90 height=21 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"></center>");
		
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
	
	private static boolean checkLeaderConditions(L2PcInstance activeChar)
	{
		if (!activeChar.isClanLeader())
		{
			activeChar.sendMessage("Only clan leader can use that fanction.");
			return false;
		}
		return true;
	}
}