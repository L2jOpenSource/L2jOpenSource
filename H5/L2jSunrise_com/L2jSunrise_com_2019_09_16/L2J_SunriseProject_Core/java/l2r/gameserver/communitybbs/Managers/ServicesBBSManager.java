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
import java.util.List;
import java.util.StringTokenizer;

import l2r.gameserver.GameTimeController;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.cache.HtmCache;
import l2r.gameserver.communitybbs.BoardsManager;
import l2r.gameserver.data.sql.CharNameTable;
import l2r.gameserver.data.sql.ClanTable;
import l2r.gameserver.data.xml.impl.HennaData;
import l2r.gameserver.data.xml.impl.ItemData;
import l2r.gameserver.data.xml.impl.MultisellData;
import l2r.gameserver.enums.CtrlIntention;
import l2r.gameserver.enums.ZoneIdType;
import l2r.gameserver.instancemanager.SiegeManager;
import l2r.gameserver.instancemanager.TownManager;
import l2r.gameserver.model.Elementals;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.entity.olympiad.OlympiadManager;
import l2r.gameserver.model.itemcontainer.Inventory;
import l2r.gameserver.model.items.L2Henna;
import l2r.gameserver.model.items.instance.L2ItemInstance;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.ActionFailed;
import l2r.gameserver.network.serverpackets.ExBuyList;
import l2r.gameserver.network.serverpackets.ExBuySellList;
import l2r.gameserver.network.serverpackets.ExShowVariationCancelWindow;
import l2r.gameserver.network.serverpackets.ExShowVariationMakeWindow;
import l2r.gameserver.network.serverpackets.HennaEquipList;
import l2r.gameserver.network.serverpackets.HennaRemoveList;
import l2r.gameserver.network.serverpackets.InventoryUpdate;
import l2r.gameserver.network.serverpackets.MagicSkillUse;
import l2r.gameserver.network.serverpackets.PartySmallWindowAll;
import l2r.gameserver.network.serverpackets.PartySmallWindowDeleteAll;
import l2r.gameserver.network.serverpackets.SetupGauge;
import l2r.gameserver.network.serverpackets.ShowBoard;
import l2r.gameserver.network.serverpackets.WareHouseDepositList;
import l2r.gameserver.util.Broadcast;
import l2r.gameserver.util.Util;

import gr.sr.configsEngine.configs.impl.CommunityServicesConfigs;
import gr.sr.configsEngine.configs.impl.IndividualVoteSystemConfigs;
import gr.sr.interf.SunriseEvents;
import gr.sr.javaBuffer.AutoBuff;
import gr.sr.javaBuffer.BufferPacketCategories;
import gr.sr.javaBuffer.BufferPacketSender;
import gr.sr.javaBuffer.JavaBufferBypass;
import gr.sr.javaBuffer.PlayerMethods;
import gr.sr.javaBuffer.buffCommunity.dynamicHtmls.GenerateHtmls;
import gr.sr.javaBuffer.runnable.BuffDeleter;
import gr.sr.main.Conditions;
import gr.sr.main.TopListsLoader;
import gr.sr.securityEngine.SecurityActions;
import gr.sr.securityEngine.SecurityType;
import gr.sr.voteEngine.old.VoteHandler;

/**
 * @author L2jSunrise Team
 * @Website www.l2jsunrise.com
 */
public class ServicesBBSManager extends BaseBBSManager
{
	public String _servicesBBSCommand = CommunityServicesConfigs.BYPASS_COMMAND;
	
	@Override
	public void cbByPass(String command, L2PcInstance activeChar)
	{
		if (!CommunityServicesConfigs.COMMUNITY_SERVICES_ALLOW)
		{
			activeChar.sendMessage("This function is disabled by admin.");
			return;
		}
		
		String path = "data/html/CommunityBoard/services/";
		String filepath = "";
		String content = "";
		
		if (command.equals(_servicesBBSCommand + ""))
		{
			BoardsManager.getInstance().addBypass(activeChar, "Service Command", command);
			filepath = path + "main.htm";
			content = HtmCache.getInstance().getHtm(activeChar, activeChar.getHtmlPrefix(), filepath);
			separateAndSend(content, activeChar);
		}
		else if (command.startsWith(_servicesBBSCommand + ";gatekeeper"))
		{
			BoardsManager.getInstance().addBypass(activeChar, "Service Gatekeeper", command);
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			st.nextToken();
			String file = st.nextToken();
			path = "data/html/CommunityBoard/services/gatekeeper/";
			sendHtm(activeChar, filepath, path, file, command);
		}
		else if (command.startsWith(_servicesBBSCommand + ";"))
		{
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			String file = st.nextToken();
			sendHtm(activeChar, filepath, path, file, command);
		}
		else if (command.startsWith(_servicesBBSCommand + "_sendMultisell"))
		{
			BoardsManager.getInstance().addBypass(activeChar, "Service Multisell", command);
			if (!CommunityServicesConfigs.COMMUNITY_SERVICES_SHOP_ALLOW)
			{
				activeChar.sendMessage("This function is disabled by admin.");
				return;
			}
			
			if (!CommunityServicesConfigs.COMMUNITY_SERVICES_SHOP_NONPEACE && !activeChar.isInsideZone(ZoneIdType.PEACE))
			{
				activeChar.sendMessage("You cannot use this function outside peace zone.");
			}
			else
			{
				try
				{
					String multisell = commandSeperator(command);
					int multi = Integer.valueOf(multisell);
					
					switch (multi)
					{
						case 90525:
							content = HtmCache.getInstance().getHtm(activeChar, activeChar.getHtmlPrefix(), "data/html/CommunityBoard/services/symbolMaker.htm");
							break;
						case 90526:
							content = HtmCache.getInstance().getHtm(activeChar, activeChar.getHtmlPrefix(), "data/html/CommunityBoard/services/warehouse.htm");
							break;
						case 90539:
						case 90540:
						case 90541:
							content = HtmCache.getInstance().getHtm(activeChar, activeChar.getHtmlPrefix(), "data/html/CommunityBoard/services/exclusiveShop.htm");
							break;
						default:
							content = HtmCache.getInstance().getHtm(activeChar, activeChar.getHtmlPrefix(), "data/html/CommunityBoard/services/gmshop.htm");
							break;
					}
					
					if ((multi == 90527) || (multi == 90528) || (multi == 90529) || (multi == 90530) || (multi == 90531) || (multi == 90532) || (multi == 90533) || (multi == 90534) || (multi == 90535) || (multi == 90536) || (multi == 90537) || (multi == 90538))
					{
						content = HtmCache.getInstance().getHtm(activeChar, activeChar.getHtmlPrefix(), "data/html/CommunityBoard/services/blacksmith.htm");
					}
					
					separateAndSend(content, activeChar);
					
					if (CommunityServicesConfigs.MULTISELL_LIST.contains(multi))
					{
						activeChar.setIsUsingAioMultisell(true);
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
		}
		else if (command.startsWith(_servicesBBSCommand + "_CommunitySell"))
		{
			BoardsManager.getInstance().addBypass(activeChar, "Service Sell", command);
			content = HtmCache.getInstance().getHtm(activeChar, activeChar.getHtmlPrefix(), "data/html/CommunityBoard/services/gmshop.htm");
			separateAndSend(content, activeChar);
			
			activeChar.setIsUsingAioMultisell(true);
			activeChar.sendPacket(new ExBuyList(activeChar));
			activeChar.sendPacket(new ExBuySellList(activeChar, 0, true));
		}
		else if (command.startsWith(_servicesBBSCommand + "_teleport"))
		{
			BoardsManager.getInstance().addBypass(activeChar, "Service Teleport", command);
			content = HtmCache.getInstance().getHtm(activeChar, activeChar.getHtmlPrefix(), "data/html/CommunityBoard/services/gatekeeper/main_gk.htm");
			separateAndSend(content, activeChar);
			
			if (!CommunityServicesConfigs.COMMUNITY_SERVICES_TP_ALLOW)
			{
				activeChar.sendMessage("This function is disabled by admin.");
				return;
			}
			
			if (activeChar.isJailed() || activeChar.isAlikeDead() || activeChar.isInOlympiadMode() || activeChar.inObserverMode() || SunriseEvents.isInEvent(activeChar) || OlympiadManager.getInstance().isRegistered(activeChar))
			{
				activeChar.sendMessage("Cannot use at the moment.");
				return;
			}
			
			try
			{
				String tp = commandSeperator(command);
				Integer[] c = new Integer[3];
				c[0] = TopListsLoader.getInstance().getTeleportInfo(Integer.parseInt(tp))[0];
				c[1] = TopListsLoader.getInstance().getTeleportInfo(Integer.parseInt(tp))[1];
				c[2] = TopListsLoader.getInstance().getTeleportInfo(Integer.parseInt(tp))[2];
				boolean onlyForNobless = TopListsLoader.getInstance().getTeleportInfo(Integer.parseInt(tp))[3] == 1;
				int itemIdToGet = TopListsLoader.getInstance().getTeleportInfo(Integer.parseInt(tp))[4];
				int price = TopListsLoader.getInstance().getTeleportInfo(Integer.parseInt(tp))[5];
				
				if (!CommunityServicesConfigs.ALLOW_TELEPORT_DURING_SIEGE)
				{
					if (SiegeManager.getInstance().getSiege(c[0], c[1], c[2]) != null)
					{
						activeChar.sendPacket(SystemMessageId.NO_PORT_THAT_IS_IN_SIGE);
						return;
					}
					else if (TownManager.townHasCastleInSiege(c[0], c[1]) && activeChar.isInsideZone(ZoneIdType.TOWN))
					{
						activeChar.sendPacket(SystemMessageId.NO_PORT_THAT_IS_IN_SIGE);
						return;
					}
				}
				
				if (Conditions.checkPlayerItemCount(activeChar, itemIdToGet, price))
				{
					if (onlyForNobless && !activeChar.isNoble() && !activeChar.isGM())
					{
						activeChar.sendMessage("Only noble chars can teleport there.");
						return;
					}
					
					if (activeChar.isTransformed())
					{
						if ((activeChar.getTransformationId() == 9) || (activeChar.getTransformationId() == 8))
						{
							activeChar.untransform();
						}
					}
					
					if (activeChar.isInsideZone(ZoneIdType.PEACE) || activeChar.isGM())
					{
						activeChar.setInstanceId(0);
						activeChar.teleToLocation(c[0], c[1], c[2]);
					}
					else
					{
						activeChar.abortCast();
						activeChar.abortAttack();
						activeChar.sendPacket(ActionFailed.STATIC_PACKET);
						activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
						activeChar.setTarget(activeChar);
						activeChar.disableAllSkills();
						Broadcast.toSelfAndKnownPlayersInRadius(activeChar, new MagicSkillUse(activeChar, 1050, 1, 30000, 0), 810000);
						activeChar.sendPacket(new SetupGauge(SetupGauge.BLUE, 30000));
						activeChar.setSkillCast(ThreadPoolManager.getInstance().scheduleGeneral(new Teleport(activeChar, c[0], c[1], c[2]), 30000));
						activeChar.forceIsCasting(10 + GameTimeController.getInstance().getGameTicks() + (30000 / GameTimeController.MILLIS_IN_TICK));
					}
					
					activeChar.destroyItemByItemId("Community Teleport", itemIdToGet, price, activeChar, true);
				}
			}
			catch (Exception e)
			{
				SecurityActions.startSecurity(activeChar, SecurityType.COMMUNITY_SYSTEM);
			}
		}
		else if (command.startsWith(_servicesBBSCommand + "_drawSymbol"))
		{
			BoardsManager.getInstance().addBypass(activeChar, "Service Symbol Add", command);
			content = HtmCache.getInstance().getHtm(activeChar, activeChar.getHtmlPrefix(), "data/html/CommunityBoard/services/symbolMaker.htm");
			separateAndSend(content, activeChar);
			
			List<L2Henna> tato = HennaData.getInstance().getHennaList(activeChar.getClassId());
			activeChar.sendPacket(new HennaEquipList(activeChar, tato));
		}
		else if (command.startsWith(_servicesBBSCommand + "_removeSymbol"))
		{
			BoardsManager.getInstance().addBypass(activeChar, "Service Symbol Remove", command);
			content = HtmCache.getInstance().getHtm(activeChar, activeChar.getHtmlPrefix(), "data/html/CommunityBoard/services/symbolMaker.htm");
			separateAndSend(content, activeChar);
			
			boolean hasHennas = false;
			for (int i = 1; i <= 3; i++)
			{
				L2Henna henna = activeChar.getHennaEx().getHenna(i);
				if (henna != null)
				{
					hasHennas = true;
				}
			}
			
			if (hasHennas)
			{
				activeChar.sendPacket(new HennaRemoveList(activeChar));
			}
			else
			{
				activeChar.sendMessage("You do not have dyes.");
			}
		}
		else if (command.startsWith(_servicesBBSCommand + "_addAugment"))
		{
			BoardsManager.getInstance().addBypass(activeChar, "Service Augment Add", command);
			content = HtmCache.getInstance().getHtm(activeChar, activeChar.getHtmlPrefix(), "data/html/CommunityBoard/services/blacksmith.htm");
			separateAndSend(content, activeChar);
			
			activeChar.sendPacket(new ExShowVariationMakeWindow());
		}
		else if (command.startsWith(_servicesBBSCommand + "_delAugment"))
		{
			BoardsManager.getInstance().addBypass(activeChar, "Service Augment Remove", command);
			content = HtmCache.getInstance().getHtm(activeChar, activeChar.getHtmlPrefix(), "data/html/CommunityBoard/services/blacksmith.htm");
			separateAndSend(content, activeChar);
			
			activeChar.sendPacket(new ExShowVariationCancelWindow());
		}
		else if (command.toLowerCase().startsWith(_servicesBBSCommand + "_pwithdraw"))
		{
			BoardsManager.getInstance().addBypass(activeChar, "Service Private Wh Withdraw", command);
			content = HtmCache.getInstance().getHtm(activeChar, activeChar.getHtmlPrefix(), "data/html/CommunityBoard/services/warehouse.htm");
			separateAndSend(content, activeChar);
			
			GenerateHtmls.showPWithdrawWindow(activeChar, null, (byte) 0);
		}
		else if (command.toLowerCase().startsWith(_servicesBBSCommand + "_cwithdraw"))
		{
			BoardsManager.getInstance().addBypass(activeChar, "Service Clan Wh Withdraw", command);
			content = HtmCache.getInstance().getHtm(activeChar, activeChar.getHtmlPrefix(), "data/html/CommunityBoard/services/warehouse.htm");
			separateAndSend(content, activeChar);
			
			GenerateHtmls.showCWithdrawWindow(activeChar, null, (byte) 0);
		}
		else if (command.startsWith(_servicesBBSCommand + "_ndeposit"))
		{
			BoardsManager.getInstance().addBypass(activeChar, "Service Private Wh Deposit", command);
			content = HtmCache.getInstance().getHtm(activeChar, activeChar.getHtmlPrefix(), "data/html/CommunityBoard/services/warehouse.htm");
			separateAndSend(content, activeChar);
			
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			activeChar.setActiveWarehouse(activeChar.getWarehouse());
			if (activeChar.getWarehouse().getSize() == activeChar.getWareHouseLimit())
			{
				activeChar.sendPacket(SystemMessageId.WAREHOUSE_FULL);
				return;
			}
			activeChar.setIsUsingAioWh(true);
			activeChar.tempInventoryDisable();
			activeChar.sendPacket(new WareHouseDepositList(activeChar, WareHouseDepositList.PRIVATE));
		}
		else if (command.startsWith(_servicesBBSCommand + "_clandeposit"))
		{
			BoardsManager.getInstance().addBypass(activeChar, "Service Clan Wh Deposit", command);
			content = HtmCache.getInstance().getHtm(activeChar, activeChar.getHtmlPrefix(), "data/html/CommunityBoard/services/warehouse.htm");
			separateAndSend(content, activeChar);
			
			if (activeChar.getClan() == null)
			{
				activeChar.sendPacket(SystemMessageId.YOU_ARE_NOT_A_CLAN_MEMBER);
				return;
			}
			
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			activeChar.setActiveWarehouse(activeChar.getClan().getWarehouse());
			if (activeChar.getClan().getLevel() == 0)
			{
				activeChar.sendPacket(SystemMessageId.ONLY_LEVEL_1_CLAN_OR_HIGHER_CAN_USE_WAREHOUSE);
				return;
			}
			
			activeChar.setIsUsingAioWh(true);
			activeChar.setActiveWarehouse(activeChar.getClan().getWarehouse());
			activeChar.tempInventoryDisable();
			activeChar.sendPacket(new WareHouseDepositList(activeChar, WareHouseDepositList.CLAN));
		}
		else if (command.startsWith(_servicesBBSCommand + "_washPK"))
		{
			BoardsManager.getInstance().addBypass(activeChar, "Service Clean Pk", command);
			content = HtmCache.getInstance().getHtm(activeChar, activeChar.getHtmlPrefix(), "data/html/CommunityBoard/services/exclusiveShop_decreasePK.htm");
			content = activeChar.getPkKills() > 0 ? content.replaceAll("%replace%", buttons(activeChar)) : content.replaceAll("%replace%", "<table width=750 height=20><tr><td align=center>You dont have PK Points to wash.</td></tr></table>");
			separateAndSend(content, activeChar);
		}
		else if (command.startsWith(_servicesBBSCommand + "_deletePK"))
		{
			content = HtmCache.getInstance().getHtm(activeChar, activeChar.getHtmlPrefix(), "data/html/CommunityBoard/services/exclusiveShop_decreasePK.htm");
			content = content.replaceAll("%replace%", buttons(activeChar));
			separateAndSend(content, activeChar);
			
			if (!CommunityServicesConfigs.COMMUNITY_SERVICES_WASH_PK_ALLOW)
			{
				activeChar.sendMessage("This function is disabled by admin.");
				return;
			}
			
			if (!CommunityServicesConfigs.COMMUNITY_SERVICES_WASH_PK_NONPEACE && !activeChar.isInsideZone(ZoneIdType.PEACE))
			{
				activeChar.sendMessage("You cannot use this function outside peace zone.");
			}
			else
			{
				int i = Integer.parseInt(commandSeperator(command));
				if (Conditions.checkPlayerItemCount(activeChar, CommunityServicesConfigs.COMMUNITY_SERVICES_WASH_PK_ID, CommunityServicesConfigs.COMMUNITY_SERVICES_WASH_PK_PRICE * i))
				{
					int kills = activeChar.getPkKills();
					if ((kills - i) >= 0)
					{
						activeChar.setPkKills(kills - i);
						activeChar.broadcastUserInfo();
						activeChar.sendMessage(i + " PK points removed.");
						activeChar.destroyItemByItemId("Community Decrease PK", CommunityServicesConfigs.COMMUNITY_SERVICES_WASH_PK_ID, CommunityServicesConfigs.COMMUNITY_SERVICES_WASH_PK_PRICE * i, activeChar, true);
					}
				}
			}
		}
		else if (command.startsWith(_servicesBBSCommand + "_vote"))
		{
			if (command.startsWith(_servicesBBSCommand + "_vote_main"))
			{
				BoardsManager.getInstance().addBypass(activeChar, "Service Vote Main", command);
				content = HtmCache.getInstance().getHtm(activeChar, activeChar.getHtmlPrefix(), "data/html/CommunityBoard/services/vote.htm");
				content = content.replaceAll("%voteBanners%", getVoteBanners(activeChar));
				separateAndSend(content, activeChar);
				return;
			}
			
			if (command.contains(" "))
			{
				final String[] subCommand = command.split(" ");
				String site = subCommand[1];
				
				BoardsManager.getInstance().addBypass(activeChar, "Service Vote " + site, command);
				content = HtmCache.getInstance().getHtm(activeChar, activeChar.getHtmlPrefix(), "data/html/CommunityBoard/services/vote.htm");
				content = content.replaceAll("%voteBanners%", getVoteBanners(activeChar));
				separateAndSend(content, activeChar);
				
				if (!VoteHandler.voteChecks(activeChar, site))
				{
					return;
				}
				
				VoteHandler.preActivateVoting(activeChar, site);
			}
		}
		else if (command.startsWith(_servicesBBSCommand + "_atrEnchant"))
		{
			BoardsManager.getInstance().addBypass(activeChar, "Service Attribute Enchant", command);
			
			if (!CommunityServicesConfigs.COMMUNITY_SERVICES_ATTRIBUTE_MANAGER_ALLOW)
			{
				activeChar.sendMessage("This function is disabled by admin.");
				return;
			}
			
			if (!CommunityServicesConfigs.COMMUNITY_SERVICES_ATTRIBUTE_MANAGER_NONPEACE && !activeChar.isInsideZone(ZoneIdType.PEACE))
			{
				activeChar.sendMessage("You cannot use this function outside peace zone.");
				return;
			}
			
			int currency = CommunityServicesConfigs.COMMUNITY_SERVICES_ATTRIBUTE_MANAGER_ID;
			String currencyName = ItemData.getInstance().getTemplate(currency).getName();
			int weaponPrice = CommunityServicesConfigs.COMMUNITY_SERVICES_ATTRIBUTE_MANAGER_PRICE_WEAPON;
			int armorPrice = CommunityServicesConfigs.COMMUNITY_SERVICES_ATTRIBUTE_MANAGER_PRICE_ARMOR;
			int elementWeaponValue = CommunityServicesConfigs.COMMUNITY_SERVICES_ATTRIBUTE_LVL_FOR_WEAPON;
			int elementArmorValue = CommunityServicesConfigs.COMMUNITY_SERVICES_ATTRIBUTE_LVL_FOR_ARMOR;
			
			content = HtmCache.getInstance().getHtm(activeChar, activeChar.getHtmlPrefix(), "data/html/CommunityBoard/services/exclusiveShop_atrEnchant.htm");
			content = content.replaceAll("%currency%", ItemData.getInstance().getTemplate(currency).getName());
			
			content = content.replaceAll("%weaponCurrency%", String.valueOf(weaponPrice) + " " + currencyName);
			content = content.replaceAll("%armorCurrency%", String.valueOf(armorPrice) + " " + currencyName);
			
			content = content.replaceAll("%armorAttValue%", String.valueOf(elementArmorValue));
			content = content.replaceAll("%weaponAttValue%", String.valueOf(elementWeaponValue));
			
			if (command.startsWith(_servicesBBSCommand + "_atrEnchantHtml"))
			{
				separateAndSend(content, activeChar);
				return;
			}
			
			final String[] subCommand = command.split(" ");
			String loc = subCommand[1];
			int armorType = 0;
			
			switch (loc)
			{
				case "head":
					armorType = Inventory.PAPERDOLL_HEAD;
					break;
				case "chest":
					armorType = Inventory.PAPERDOLL_CHEST;
					break;
				case "gloves":
					armorType = Inventory.PAPERDOLL_GLOVES;
					break;
				case "feet":
					armorType = Inventory.PAPERDOLL_FEET;
					break;
				case "legs":
					armorType = Inventory.PAPERDOLL_LEGS;
					break;
				case "weapon":
					armorType = Inventory.PAPERDOLL_RHAND;
					break;
				default:
					separateAndSend(content, activeChar);
					activeChar.sendMessage("You cannot enchant items that are not equipped.");
					return;
			}
			
			String type = subCommand[2];
			int typeId = 0;
			switch (type)
			{
				case "Fire":
					typeId = 0;
					break;
				case "Water":
					typeId = 1;
					break;
				case "Wind":
					typeId = 2;
					break;
				case "Earth":
					typeId = 3;
					break;
				case "Holy":
					typeId = 4;
					break;
				case "Dark":
					typeId = 5;
					break;
				default:
					separateAndSend(content, activeChar);
					activeChar.sendMessage("You cannot enchant the item. Wrong element.");
					return;
			}
			
			if (Conditions.checkPlayerItemCount(activeChar, currency, loc.equals("weapon") ? weaponPrice : armorPrice))
			{
				L2ItemInstance parmorInstance = activeChar.getInventory().getPaperdollItem(armorType);
				if ((parmorInstance != null) && (parmorInstance.getLocationSlot() == armorType))
				{
					byte elementtoAdd = (byte) typeId;
					byte opositeElement = Elementals.getOppositeElement(elementtoAdd);
					Elementals oldElement = parmorInstance.getElemental(elementtoAdd);
					
					switch (parmorInstance.getItem().getCrystalType())
					{
						case NONE:
						case A:
						case B:
						case C:
						case D:
							activeChar.sendMessage("Invalid item grade.");
							return;
						default:
							break;
					}
					
					if ((parmorInstance.isWeapon() && (parmorInstance.getElementals() != null)) || (parmorInstance.isArmor() && (oldElement != null) && (parmorInstance.getElementals() != null) && (parmorInstance.getElementals().length >= 3)))
					{
						separateAndSend(content, activeChar);
						activeChar.sendPacket(SystemMessageId.ANOTHER_ELEMENTAL_POWER_ALREADY_ADDED);
						return;
					}
					
					if (parmorInstance.isWeapon())
					{
						if ((oldElement != null) && (oldElement.getValue() >= elementWeaponValue))
						{
							separateAndSend(content, activeChar);
							activeChar.sendMessage("You cannot add same attribute to item!");
							return;
						}
						
						if (parmorInstance.getElementals() != null)
						{
							for (Elementals elm : parmorInstance.getElementals())
							{
								if (parmorInstance.isEquipped())
								{
									parmorInstance.getElemental(elm.getElement()).removeBonus(activeChar);
								}
								parmorInstance.clearElementAttr(elm.getElement());
							}
						}
					}
					else if (parmorInstance.isArmor())
					{
						if (parmorInstance.getElementals() != null)
						{
							for (Elementals elm : parmorInstance.getElementals())
							{
								if (elm.getElement() == opositeElement)
								{
									separateAndSend(content, activeChar);
									activeChar.sendMessage("You cannot add opposite attribute to item!");
									return;
								}
								if ((elm.getElement() == elementtoAdd) && (elm.getValue() >= elementArmorValue))
								{
									separateAndSend(content, activeChar);
									activeChar.sendMessage("You cannot add same attribute to item!");
									return;
								}
							}
						}
					}
					
					activeChar.destroyItemByItemId("Community Attribute Manager", currency, parmorInstance.isWeapon() ? weaponPrice : armorPrice, activeChar, true);
					activeChar.getInventory().unEquipItemInSlot(armorType);
					parmorInstance.setElementAttr(elementtoAdd, parmorInstance.isWeapon() ? elementWeaponValue : elementArmorValue);
					activeChar.getInventory().equipItem(parmorInstance);
					activeChar.sendMessage("Successfully added " + subCommand[2] + " attribute to your item.");
					
					final InventoryUpdate iu = new InventoryUpdate();
					iu.addModifiedItem(parmorInstance);
					activeChar.sendInventoryUpdate(iu);
				}
				else
				{
					activeChar.sendMessage("You cannot attribute items that are not equipped!");
				}
			}
			separateAndSend(content, activeChar);
		}
		else if (command.startsWith(_servicesBBSCommand + "_changename"))
		{
			BoardsManager.getInstance().addBypass(activeChar, "Service Name Change", command);
			content = HtmCache.getInstance().getHtm(activeChar, activeChar.getHtmlPrefix(), "data/html/CommunityBoard/services/exclusiveShop.htm");
			separateAndSend(content, activeChar);
			
			if (!CommunityServicesConfigs.COMMUNITY_SERVICES_NAME_CHANGE_ALLOW)
			{
				activeChar.sendMessage("This function is disabled by admin.");
				return;
			}
			
			if (!CommunityServicesConfigs.COMMUNITY_SERVICES_NAME_CHANGE_NONPEACE && !activeChar.isInsideZone(ZoneIdType.PEACE))
			{
				activeChar.sendMessage("You cannot use this function outside peace zone.");
			}
			else
			{
				try
				{
					String val = commandSeperator(command);
					if (!Util.isAlphaNumeric(val))
					{
						activeChar.sendMessage("Invalid character name.");
						return;
					}
					
					if (Conditions.checkPlayerItemCount(activeChar, CommunityServicesConfigs.COMMUNITY_SERVICES_NAME_CHANGE_ID, CommunityServicesConfigs.COMMUNITY_SERVICES_NAME_CHANGE_PRICE))
					{
						if (CharNameTable.getInstance().getIdByName(val) > 0)
						{
							activeChar.sendMessage("Warning, name " + val + " already exists.");
							return;
						}
						
						activeChar.destroyItemByItemId("Community Name Change", CommunityServicesConfigs.COMMUNITY_SERVICES_NAME_CHANGE_ID, CommunityServicesConfigs.COMMUNITY_SERVICES_NAME_CHANGE_PRICE, activeChar, true);
						activeChar.setName(val);
						activeChar.getAppearance().setVisibleName(val);
						activeChar.store();
						activeChar.sendMessage("Your name has been changed to " + val);
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
						
						if (activeChar.getClan() != null)
						{
							activeChar.getClan().broadcastClanStatus();
						}
					}
				}
				catch (StringIndexOutOfBoundsException e)
				{
					activeChar.sendMessage("Player name box cannot be empty.");
				}
			}
		}
		// Change clan name
		else if (command.startsWith(_servicesBBSCommand + "_changeclanname"))
		{
			BoardsManager.getInstance().addBypass(activeChar, "Service Clan Name Change", command);
			content = HtmCache.getInstance().getHtm(activeChar, activeChar.getHtmlPrefix(), "data/html/CommunityBoard/services/exclusiveShop.htm");
			separateAndSend(content, activeChar);
			
			if (!CommunityServicesConfigs.COMMUNITY_SERVICES_CLAN_NAME_CHANGE_ALLOW)
			{
				activeChar.sendMessage("This function is disabled by admin.");
				return;
			}
			
			if (!CommunityServicesConfigs.COMMUNITY_SERVICES_CLAN_NAME_CHANGE_NONPEACE && !activeChar.isInsideZone(ZoneIdType.PEACE))
			{
				activeChar.sendMessage("You cannot use this function outside peace zone.");
			}
			else
			{
				try
				{
					String val = commandSeperator(command);
					if ((activeChar.getClan() == null) || !activeChar.isClanLeader())
					{
						activeChar.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
						return;
					}
					
					if (!Util.isAlphaNumeric(val))
					{
						activeChar.sendPacket(SystemMessageId.CLAN_NAME_INCORRECT);
						return;
					}
					
					if (Conditions.checkPlayerItemCount(activeChar, CommunityServicesConfigs.COMMUNITY_SERVICES_CLAN_NAME_CHANGE_ID, CommunityServicesConfigs.COMMUNITY_SERVICES_CLAN_NAME_CHANGE_PRICE))
					{
						if (ClanTable.getInstance().getClanByName(val) != null)
						{
							activeChar.sendMessage("Warning, clan name " + val + " already exists.");
							return;
						}
						
						activeChar.destroyItemByItemId("Community Clan Name Change", CommunityServicesConfigs.COMMUNITY_SERVICES_CLAN_NAME_CHANGE_ID, CommunityServicesConfigs.COMMUNITY_SERVICES_CLAN_NAME_CHANGE_PRICE, activeChar, true);
						activeChar.getClan().setName(val);
						activeChar.getClan().updateClanNameInDB();
						activeChar.sendMessage("Your clan name has been changed to " + val);
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
						
						if (activeChar.getClan() != null)
						{
							activeChar.getClan().broadcastClanStatus();
						}
					}
				}
				catch (StringIndexOutOfBoundsException e)
				{
					activeChar.sendMessage("Clan name box cannot be empty.");
				}
			}
		}
		else if (command.startsWith(_servicesBBSCommand + "_buffer"))
		{
			BoardsManager.getInstance().addBypass(activeChar, "Service Buffer", command);
			content = HtmCache.getInstance().getHtm(activeChar, activeChar.getHtmlPrefix(), "data/html/CommunityBoard/services/buffer/main.htm");
			separateAndSend(content, activeChar);
		}
		else if (command.startsWith(_servicesBBSCommand + "_functions_buffer"))
		{
			final String[] subCommand = command.split("_");
			
			if (((activeChar.isInCombat() || (activeChar.getPvpFlag() != 0)) && !activeChar.isInsideZone(ZoneIdType.PEACE)) || activeChar.isJailed() || activeChar.isAlikeDead() || activeChar.isInOlympiadMode() || activeChar.inObserverMode() || SunriseEvents.isInEvent(activeChar) || OlympiadManager.getInstance().isRegistered(activeChar))
			{
				activeChar.sendMessage("Cannot use at the moment.");
				content = HtmCache.getInstance().getHtm(activeChar, activeChar.getHtmlPrefix(), "data/html/CommunityBoard/services/buffer/main.htm");
				separateAndSend(content, activeChar);
				return;
			}
			
			// Page navigation, html command how to starts
			if (subCommand[4].startsWith("page"))
			{
				if (subCommand[5].isEmpty() || (subCommand[5] == null))
				{
					return;
				}
				
				content = HtmCache.getInstance().getHtm(activeChar, activeChar.getHtmlPrefix(), "data/html/CommunityBoard/services/buffer/" + subCommand[5]);
				separateAndSend(content, activeChar);
			}
			// Method to remove all players buffs
			else if (subCommand[4].startsWith("removebuff"))
			{
				activeChar.stopAllEffects();
				BufferPacketSender.sendPacket(activeChar, "functions.htm", BufferPacketCategories.COMMUNITY, 1);
			}
			// Method to restore HP/MP/CP
			else if (subCommand[4].startsWith("healme"))
			{
				BoardsManager.getInstance().addBypass(activeChar, "Service Buffer Heal", command);
				if ((activeChar.getPvpFlag() != 0) && !activeChar.isInsideZone(ZoneIdType.PEACE))
				{
					activeChar.sendMessage("Cannot use this feature here with flag.");
					BufferPacketSender.sendPacket(activeChar, "functions.htm", BufferPacketCategories.COMMUNITY, 1);
					return;
				}
				
				activeChar.setCurrentHpMp(activeChar.getMaxHp(), activeChar.getMaxMp());
				activeChar.setCurrentCp(activeChar.getMaxCp());
				BufferPacketSender.sendPacket(activeChar, "functions.htm", BufferPacketCategories.COMMUNITY, 1);
			}
			// Method to give auto buffs depends on class
			else if (subCommand[4].startsWith("autobuff"))
			{
				BoardsManager.getInstance().addBypass(activeChar, "Service Buffer Auto Buff", command);
				if ((activeChar.getPvpFlag() != 0) && !activeChar.isInsideZone(ZoneIdType.PEACE))
				{
					activeChar.sendMessage("Cannot use this feature here with flag.");
					BufferPacketSender.sendPacket(activeChar, "functions.htm", BufferPacketCategories.COMMUNITY, 1);
					return;
				}
				
				AutoBuff.autoBuff(activeChar);
				BufferPacketSender.sendPacket(activeChar, "functions.htm", BufferPacketCategories.COMMUNITY, 1);
			}
			// Send buffs from profile to player or party or pet
			else if (subCommand[4].startsWith("bufffor"))
			{
				if (subCommand[4].startsWith("buffforpet"))
				{
					JavaBufferBypass.callPetBuffCommand(activeChar, subCommand[5]);
				}
				else if (subCommand[4].startsWith("buffforparty"))
				{
					JavaBufferBypass.callPartyBuffCommand(activeChar, subCommand[5]);
				}
				else if (subCommand[4].startsWith("buffforme"))
				{
					JavaBufferBypass.callSelfBuffCommand(activeChar, subCommand[5]);
				}
				
				BufferPacketSender.sendPacket(activeChar, "main.htm", BufferPacketCategories.COMMUNITY, 1);
			}
			// Method to give single buffs
			else if (subCommand[4].startsWith("buff"))
			{
				JavaBufferBypass.callBuffCommand(activeChar, subCommand[5], subCommand[4], 1);
			}
			// Scheme create new profile
			else if (subCommand[4].startsWith("saveProfile"))
			{
				try
				{
					JavaBufferBypass.callSaveProfile(activeChar, subCommand[5], 1);
				}
				catch (Exception e)
				{
					activeChar.sendMessage("Please specify a valid profile name.");
					BufferPacketSender.sendPacket(activeChar, "newSchemeProfile.htm", BufferPacketCategories.COMMUNITY, 1);
					return;
				}
			}
			else if (subCommand[4].startsWith("showAvaliable"))
			{
				JavaBufferBypass.callAvailableCommand(activeChar, subCommand[4], subCommand[5], 1);
			}
			else if (subCommand[4].startsWith("add"))
			{
				JavaBufferBypass.callAddCommand(activeChar, subCommand[4], subCommand[5], subCommand[6], 1);
			}
			// Method to delete player's selected profile
			else if (subCommand[4].startsWith("deleteProfile"))
			{
				PlayerMethods.delProfile(subCommand[5], activeChar);
				BufferPacketSender.sendPacket(activeChar, "main.htm", BufferPacketCategories.COMMUNITY, 1);
			}
			else if (subCommand[4].startsWith("showBuffsToDelete"))
			{
				GenerateHtmls.showBuffsToDelete(activeChar, subCommand[5], "removeBuffs");
			}
			else if (subCommand[4].startsWith("removeBuffs"))
			{
				ThreadPoolManager.getInstance().executeGeneral(new BuffDeleter(activeChar, subCommand[5], Integer.parseInt(subCommand[6]), 1));
			}
			else if (subCommand[4].startsWith("showProfiles"))
			{
				GenerateHtmls.showSchemeToEdit(activeChar, subCommand[5]);
			}
		}
		else
		{
			separateAndSend("<html><body><br><br><center>Command : " + command + " needs core development</center><br><br></body></html>", activeChar);
		}
	}
	
	private String buttons(L2PcInstance activeChar)
	{
		String add = "";
		final int[] PKS =
		{
			1,
			2,
			5,
			10,
			25,
			50,
			100,
			250,
			500,
			1000
		};
		
		for (int pk : PKS)
		{
			if (activeChar.getPkKills() <= pk)
			{
				break;
			}
			add += getPkButton(pk);
		}
		
		if (activeChar.getPkKills() != 0)
		{
			add += getPkButton(activeChar.getPkKills());
		}
		
		return add;
	}
	
	private String getPkButton(int i)
	{
		return "<table width=750 height=20><tr><td align=center><button value=\"for " + i + " PK - " + (CommunityServicesConfigs.COMMUNITY_SERVICES_WASH_PK_PRICE * i) + " " + ItemData.getInstance().getTemplate(CommunityServicesConfigs.COMMUNITY_SERVICES_WASH_PK_ID).getName() + " \" action=\"bypass " + _servicesBBSCommand + "_deletePK " + i + "\" width=280 height=22 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr></table>";
	}
	
	private String commandSeperator(String command)
	{
		StringTokenizer st = new StringTokenizer(command, " ");
		st.nextToken();
		String dat = st.nextToken();
		return dat;
	}
	
	private class Teleport implements Runnable
	{
		L2PcInstance _activeChar;
		private final int _x, _y, _z;
		
		Teleport(L2PcInstance activeChar, int x, int y, int z)
		{
			_activeChar = activeChar;
			_x = x;
			_y = y;
			_z = z;
		}
		
		@Override
		public void run()
		{
			_activeChar.setInstanceId(0);
			_activeChar.teleToLocation(_x, _y, _z, true);
			_activeChar.setIsCastingNow(false);
			_activeChar.enableAllSkills();
		}
	}
	
	private void sendHtm(L2PcInstance activeChar, String filepath, String path, String file, String command)
	{
		String content = "";
		filepath = path + file + ".htm";
		File filecom = new File(filepath);
		
		if (!filecom.exists())
		{
			content = "<html><body><br><br><center>The command " + command + " points to file(" + filepath + ") that NOT exists.</center></body></html>";
			separateAndSend(content, activeChar);
			return;
		}
		
		content = HtmCache.getInstance().getHtm(activeChar, activeChar.getHtmlPrefix(), filepath);
		if (content.isEmpty())
		{
			content = "<html><body><br><br><center>Content Empty: The command " + command + " points to an invalid or empty html file(" + filepath + ").</center></body></html>";
		}
		
		separateAndSend(content, activeChar);
	}
	
	@Override
	protected void separateAndSend(String html, L2PcInstance acha)
	{
		html = html.replace("\t", "");
		html = html.replace("%command%", _servicesBBSCommand);
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
	
	public static String getVoteBanners(L2PcInstance activeChar)
	{
		String voteBanners = "";
		
		if (IndividualVoteSystemConfigs.VOTE_MANAGER_ALLOW_HOPZONE)
		{
			voteBanners += "<tr><td width=540 height=58 align=center valign=top>" + "<table border=0 width=540 height=38 cellspacing=4 cellpadding=3 bgcolor=232836>" + "<tr><td width=40 align=right valign=top>" + "<img src=\"icon.etc_orcish_talisman_i00\" width=32 height=32>" + "</td><td width=520 align=left valign=top>" + "<font color=\"a22020\">Vote in Hopzone</font><br1> <font color=c1b33a>Info:</font> You can vote " + VoteHandler.getWhenCanVote(activeChar, "HopZone") + "" + "</td><td width=80 height=39 align=right>" + "<table border=0 cellspacing=0 cellpadding=0 width=80>" + "<tr>" + "<td width=40 align=right valign=center>" + "Vote</td>" + "<td width=40 align=right valign=center>" + "<button value=\"\" action=\"bypass " + CommunityServicesConfigs.BYPASS_COMMAND + "_vote HopZone\" width=32 height=32 back=\"L2UI_ct1.MiniMap_DF_PlusBtn_Red_Down\" fore=\"L2UI_ct1.MiniMap_DF_PlusBtn_Red\">" + "</td>" + "</tr>" + "</table>" + "</td>" + "</tr>" + "</table>" + "</td>" + "</tr>";
		}
		if (IndividualVoteSystemConfigs.VOTE_MANAGER_ALLOW_TOPZONE)
		{
			voteBanners += "<tr><td width=540 height=58 align=center valign=top>" + "<table border=0 width=540 height=38 cellspacing=4 cellpadding=3 bgcolor=232836>" + "<tr><td width=40 align=right valign=top>" + "<img src=\"icon.etc_barka_badge_officer_i02\" width=32 height=32>" + "</td><td width=520 align=left valign=top>" + "<font color=\"a22020\">Vote in Topzone</font><br1> <font color=c1b33a>Info:</font> You can vote " + VoteHandler.getWhenCanVote(activeChar, "TopZone") + "" + "</td><td width=80 height=39 align=right>" + "<table border=0 cellspacing=0 cellpadding=0 width=80>" + "<tr>" + "<td width=40 align=right valign=center>" + "Vote</td>" + "<td width=40 align=right valign=center>" + "<button value=\"\" action=\"bypass " + CommunityServicesConfigs.BYPASS_COMMAND + "_vote TopZone\" width=32 height=32 back=\"L2UI_ct1.MiniMap_DF_PlusBtn_Red_Down\" fore=\"L2UI_ct1.MiniMap_DF_PlusBtn_Red\">" + "</td>" + "</tr>" + "</table>" + "</td>" + "</tr>" + "</table>" + "</td>" + "</tr>";
		}
		if (IndividualVoteSystemConfigs.VOTE_MANAGER_ALLOW_TOPCO)
		{
			voteBanners += "<tr><td width=540 height=58 align=center valign=top>" + "<table border=0 width=540 height=38 cellspacing=4 cellpadding=3 bgcolor=232836>" + "<tr><td width=40 align=right valign=top>" + "<img src=\"icon.etc_jewel_white_i00\" width=32 height=32>" + "</td><td width=520 align=left valign=top>" + "<font color=\"a22020\">Vote in Top.co</font><br1> <font color=c1b33a>Info:</font> You can vote " + VoteHandler.getWhenCanVote(activeChar, "L2TopCo") + "" + "</td><td width=80 height=39 align=right>" + "<table border=0 cellspacing=0 cellpadding=0 width=80>" + "<tr>" + "<td width=40 align=right valign=center>" + "Vote</td>" + "<td width=40 align=right valign=center>" + "<button value=\"\" action=\"bypass " + CommunityServicesConfigs.BYPASS_COMMAND + "_vote L2TopCo\" width=32 height=32 back=\"L2UI_ct1.MiniMap_DF_PlusBtn_Red_Down\" fore=\"L2UI_ct1.MiniMap_DF_PlusBtn_Red\">" + "</td>" + "</tr>" + "</table>" + "</td>" + "</tr>" + "</table>" + "</td>" + "</tr>";
		}
		if (IndividualVoteSystemConfigs.VOTE_MANAGER_ALLOW_NETWORK)
		{
			voteBanners += "<tr><td width=540 height=58 align=center valign=top>" + "<table border=0 width=540 height=38 cellspacing=4 cellpadding=3 bgcolor=232836>" + "<tr><td width=40 align=right valign=top>" + "<img src=\"icon.etc_Symbol_of_dawn_i00\" width=32 height=32>" + "</td><td width=520 align=left valign=top>" + "<font color=\"a22020\">Vote in L2NetWork</font><br1> <font color=c1b33a>Info:</font> You can vote " + VoteHandler.getWhenCanVote(activeChar, "L2NetWork") + "" + "</td><td width=80 height=39 align=right>" + "<table border=0 cellspacing=0 cellpadding=0 width=80>" + "<tr>" + "<td width=40 align=right valign=center>" + "Vote</td>" + "<td width=40 align=right valign=center>" + "<button value=\"\" action=\"bypass " + CommunityServicesConfigs.BYPASS_COMMAND + "_vote L2NetWork\" width=32 height=32 back=\"L2UI_ct1.MiniMap_DF_PlusBtn_Red_Down\" fore=\"L2UI_ct1.MiniMap_DF_PlusBtn_Red\">" + "</td>" + "</tr>" + "</table>" + "</td>" + "</tr>" + "</table>" + "</td>" + "</tr>";
		}
		if (IndividualVoteSystemConfigs.VOTE_MANAGER_ALLOW_GAMEBYTES)
		{
			voteBanners += "<tr><td width=540 height=58 align=center valign=top>" + "<table border=0 width=540 height=38 cellspacing=4 cellpadding=3 bgcolor=232836>" + "<tr><td width=40 align=right valign=top>" + "<img src=\"icon.etc_nutrients_of_cupid_i00\" width=32 height=32>" + "</td><td width=520 align=left valign=top>" + "<font color=\"a22020\">Vote in Gamebytes</font><br1> <font color=c1b33a>Info:</font> You can vote " + VoteHandler.getWhenCanVote(activeChar, "GameBytes") + "" + "</td><td width=80 height=39 align=right>" + "<table border=0 cellspacing=0 cellpadding=0 width=80>" + "<tr>" + "<td width=40 align=right valign=center>" + "Vote</td>" + "<td width=40 align=right valign=center>" + "<button value=\"\" action=\"bypass " + CommunityServicesConfigs.BYPASS_COMMAND + "_vote GameBytes\" width=32 height=32 back=\"L2UI_ct1.MiniMap_DF_PlusBtn_Red_Down\" fore=\"L2UI_ct1.MiniMap_DF_PlusBtn_Red\">" + "</td>" + "</tr>" + "</table>" + "</td>" + "</tr>" + "</table>" + "</td>" + "</tr>";
		}
		if (IndividualVoteSystemConfigs.VOTE_MANAGER_ALLOW_TOPGS00)
		{
			voteBanners += "<tr><td width=540 height=58 align=center valign=top>" + "<table border=0 width=540 height=38 cellspacing=4 cellpadding=3 bgcolor=232836>" + "<tr><td width=40 align=right valign=top>" + "<img src=\"icon.etc_badge_gold_i00\" width=32 height=32>" + "</td><td width=520 align=left valign=top>" + "<font color=\"a22020\">Vote in TopGs200</font><br1> <font color=c1b33a>Info:</font> You can vote " + VoteHandler.getWhenCanVote(activeChar, "TopGs200") + "" + "</td><td width=80 height=39 align=right>" + "<table border=0 cellspacing=0 cellpadding=0 width=80>" + "<tr>" + "<td width=40 align=right valign=center>" + "Vote</td>" + "<td width=40 align=right valign=center>" + "<button value=\"\" action=\"bypass " + CommunityServicesConfigs.BYPASS_COMMAND + "_vote TopGs200\" width=32 height=32 back=\"L2UI_ct1.MiniMap_DF_PlusBtn_Red_Down\" fore=\"L2UI_ct1.MiniMap_DF_PlusBtn_Red\">" + "</td>" + "</tr>" + "</table>" + "</td>" + "</tr>" + "</table>" + "</td>" + "</tr>";
		}
		if (IndividualVoteSystemConfigs.VOTE_MANAGER_ALLOW_TOPSERVERS200)
		{
			voteBanners += "<tr><td width=540 height=58 align=center valign=top>" + "<table border=0 width=540 height=38 cellspacing=4 cellpadding=3 bgcolor=232836>" + "<tr><td width=40 align=right valign=top>" + "<img src=\"icon.energy_condenser_i00\" width=32 height=32>" + "</td><td width=520 align=left valign=top>" + "<font color=\"a22020\">Vote in TopServer200</font><br1> <font color=c1b33a>Info:</font> You can vote " + VoteHandler.getWhenCanVote(activeChar, "TopServers200") + "" + "</td><td width=80 height=39 align=right>" + "<table border=0 cellspacing=0 cellpadding=0 width=80>" + "<tr>" + "<td width=40 align=right valign=center>" + "Vote</td>" + "<td width=40 align=right valign=center>" + "<button value=\"\" action=\"bypass " + CommunityServicesConfigs.BYPASS_COMMAND + "_vote TopServers200\" width=32 height=32 back=\"L2UI_ct1.MiniMap_DF_PlusBtn_Red_Down\" fore=\"L2UI_ct1.MiniMap_DF_PlusBtn_Red\">" + "</td>" + "</tr>" + "</table>" + "</td>" + "</tr>" + "</table>" + "</td>" + "</tr>";
		}
		if (IndividualVoteSystemConfigs.ENABLE_TRIES)
		{
			if (Integer.parseInt(activeChar.getVar("vote_tries", "9999")) == 9999)
			{
				activeChar.setVar("vote_tries", String.valueOf(IndividualVoteSystemConfigs.TRIES_AMOUNT));
			}
			voteBanners += "<tr><td width=540 align=center valign=top><br>Tries: <font color=AE9977>" + activeChar.getVar("vote_tries", "0") + "</font></td></tr>";
		}
		return voteBanners;
	}
	
	@Override
	public void parsewrite(String url, String ar1, String ar2, String ar3, String ar4, String ar5, L2PcInstance activeChar)
	{
	
	}
	
	public static ServicesBBSManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final ServicesBBSManager _instance = new ServicesBBSManager();
	}
}