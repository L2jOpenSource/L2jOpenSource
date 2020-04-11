package com.l2jfrozen.gameserver.model.actor.instance;

import java.util.StringTokenizer;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.controllers.TradeController;
import com.l2jfrozen.gameserver.model.L2TradeList;
import com.l2jfrozen.gameserver.model.multisell.L2Multisell;
import com.l2jfrozen.gameserver.network.L2GameClient;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.BuyList;
import com.l2jfrozen.gameserver.network.serverpackets.MyTargetSelected;
import com.l2jfrozen.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jfrozen.gameserver.network.serverpackets.Ride;
import com.l2jfrozen.gameserver.network.serverpackets.SellList;
import com.l2jfrozen.gameserver.network.serverpackets.StatusUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.WearList;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;

import javolution.text.TextBuilder;

/**
 * This class ...
 * @version $Revision: 1.10.4.9 $ $Date: 2005/04/11 10:06:08 $
 */
public class L2MerchantInstance extends L2FolkInstance
{
	// private static Logger LOGGER = Logger.getLogger(L2MerchantInstance.class);
	
	/**
	 * Instantiates a new l2 merchant instance.
	 * @param objectId the object id
	 * @param template the template
	 */
	public L2MerchantInstance(final int objectId, final L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public String getHtmlPath(final int npcId, final int val)
	{
		String pom = "";
		
		if (val == 0)
		{
			pom = "" + npcId;
		}
		else
		{
			pom = npcId + "-" + val;
		}
		
		return "data/html/merchant/" + pom + ".htm";
	}
	
	/**
	 * Show wear window.
	 * @param player the player
	 * @param val    the val
	 */
	private void showWearWindow(final L2PcInstance player, final int val)
	{
		player.tempInvetoryDisable();
		
		if (Config.DEBUG)
		{
			LOGGER.debug("Showing wearlist");
		}
		
		L2TradeList list = TradeController.getInstance().getBuyList(val);
		
		if (list != null)
		{
			WearList bl = new WearList(list, player.getAdena(), player.getExpertiseIndex());
			player.sendPacket(bl);
			list = null;
			bl = null;
		}
		else
		{
			LOGGER.warn("no buylist with id:" + val);
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
	}
	
	/**
	 * Show buy window.
	 * @param player the player
	 * @param val    the val
	 */
	private void showBuyWindow(final L2PcInstance player, final int val)
	{
		double taxRate = 0;
		
		if (getIsInTown())
		{
			taxRate = getCastle().getTaxRate();
		}
		
		player.tempInvetoryDisable();
		
		if (Config.DEBUG)
		{
			LOGGER.debug("Showing buylist");
		}
		
		L2TradeList list = TradeController.getInstance().getBuyList(val);
		
		if (list != null && list.getNpcId().equals(String.valueOf(getNpcId())))
		{
			if (player.isGM())
			{
				player.sendMessage("MERCHANT BUYLIST SHOP ID: " + val);
			}
			
			BuyList bl = new BuyList(list, player.getAdena(), taxRate);
			player.sendPacket(bl);
			list = null;
			bl = null;
		}
		else
		{
			LOGGER.warn("possible client hacker: " + player.getName() + " attempting to buy from GM shop! (L2MechantInstance)");
			LOGGER.warn("buylist id:" + val);
		}
		
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	/**
	 * Show sell window.
	 * @param player the player
	 */
	private void showSellWindow(final L2PcInstance player)
	{
		if (Config.DEBUG)
		{
			LOGGER.debug("Showing selllist");
		}
		
		player.sendPacket(new SellList(player));
		
		if (Config.DEBUG)
		{
			LOGGER.debug("Showing sell window");
		}
		
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	@Override
	public void onBypassFeedback(final L2PcInstance player, final String command)
	{
		StringTokenizer st = new StringTokenizer(command, " ");
		String actualCommand = st.nextToken(); // Get actual command
		
		if (actualCommand.equalsIgnoreCase("Buy"))
		{
			if (st.countTokens() < 1)
			{
				return;
			}
			
			final int val = Integer.parseInt(st.nextToken());
			showBuyWindow(player, val);
		}
		else if (actualCommand.equalsIgnoreCase("Sell"))
		{
			showSellWindow(player);
		}
		else if (actualCommand.equalsIgnoreCase("RentPet"))
		{
			if (Config.ALLOW_RENTPET)
			{
				if (st.countTokens() < 1)
				{
					showRentPetWindow(player);
				}
				else
				{
					final int val = Integer.parseInt(st.nextToken());
					tryRentPet(player, val);
				}
			}
		}
		else if (actualCommand.equalsIgnoreCase("Wear") && Config.ALLOW_WEAR)
		{
			if (st.countTokens() < 1)
			{
				return;
			}
			
			final int val = Integer.parseInt(st.nextToken());
			showWearWindow(player, val);
		}
		else if (actualCommand.equalsIgnoreCase("Multisell"))
		{
			if (st.countTokens() < 1)
			{
				return;
			}
			
			final int val = Integer.parseInt(st.nextToken());
			L2Multisell.getInstance().separateAndSend(val, player, false, getCastle().getTaxRate());
		}
		else if (actualCommand.equalsIgnoreCase("Exc_Multisell"))
		{
			if (st.countTokens() < 1)
			{
				return;
			}
			
			final int val = Integer.parseInt(st.nextToken());
			L2Multisell.getInstance().separateAndSend(val, player, true, getCastle().getTaxRate());
		}
		else
		{
			// this class dont know any other commands, let forward
			// the command to the parent class
			
			super.onBypassFeedback(player, command);
		}
		st = null;
		actualCommand = null;
	}
	
	/**
	 * Show rent pet window.
	 * @param player the player
	 */
	public void showRentPetWindow(final L2PcInstance player)
	{
		if (!Config.LIST_PET_RENT_NPC.contains(getTemplate().npcId))
		{
			return;
		}
		
		TextBuilder html1 = new TextBuilder("<html><body>Pet Manager:<br>");
		html1.append("You can rent a wyvern or strider for adena.<br>My prices:<br1>");
		html1.append("<table border=0><tr><td>Ride</td></tr>");
		html1.append("<tr><td>Wyvern</td><td>Strider</td></tr>");
		html1.append("<tr><td><a action=\"bypass -h npc_%objectId%_RentPet 1\">30 sec/1800 adena</a></td><td><a action=\"bypass -h npc_%objectId%_RentPet 11\">30 sec/900 adena</a></td></tr>");
		html1.append("<tr><td><a action=\"bypass -h npc_%objectId%_RentPet 2\">1 min/7200 adena</a></td><td><a action=\"bypass -h npc_%objectId%_RentPet 12\">1 min/3600 adena</a></td></tr>");
		html1.append("<tr><td><a action=\"bypass -h npc_%objectId%_RentPet 3\">10 min/720000 adena</a></td><td><a action=\"bypass -h npc_%objectId%_RentPet 13\">10 min/360000 adena</a></td></tr>");
		html1.append("<tr><td><a action=\"bypass -h npc_%objectId%_RentPet 4\">30 min/6480000 adena</a></td><td><a action=\"bypass -h npc_%objectId%_RentPet 14\">30 min/3240000 adena</a></td></tr>");
		html1.append("</table>");
		html1.append("</body></html>");
		
		insertObjectIdAndShowChatWindow(player, html1.toString());
		html1 = null;
	}
	
	/**
	 * Try rent pet.
	 * @param player the player
	 * @param val    the val
	 */
	public void tryRentPet(final L2PcInstance player, int val)
	{
		if (player == null || player.getPet() != null || player.isMounted() || player.isRentedPet())
		{
			return;
		}
		if (!player.disarmWeapons())
		{
			return;
		}
		
		int petId;
		double price = 1;
		final int cost[] =
		{
			1800,
			7200,
			720000,
			6480000
		};
		final int ridetime[] =
		{
			30,
			60,
			600,
			1800
		};
		
		if (val > 10)
		{
			petId = 12526;
			val -= 10;
			price /= 2;
		}
		else
		{
			petId = 12621;
		}
		
		if (val < 1 || val > 4)
		{
			return;
		}
		
		price *= cost[val - 1];
		final int time = ridetime[val - 1];
		
		if (!player.reduceAdena("Rent", (int) price, player.getLastFolkNPC(), true))
		{
			return;
		}
		
		Ride mount = new Ride(player.getObjectId(), Ride.ACTION_MOUNT, petId);
		player.broadcastPacket(mount);
		
		player.setMountType(mount.getMountType());
		player.startRentPet(time);
		mount = null;
	}
	
	@Override
	public void onActionShift(final L2GameClient client)
	{
		L2PcInstance player = client.getActiveChar();
		if (player == null)
		{
			return;
		}
		
		if (player.getAccessLevel().isGm())
		{
			player.setTarget(this);
			
			MyTargetSelected my = new MyTargetSelected(getObjectId(), player.getLevel() - getLevel());
			player.sendPacket(my);
			my = null;
			
			if (isAutoAttackable(player))
			{
				StatusUpdate su = new StatusUpdate(getObjectId());
				su.addAttribute(StatusUpdate.CUR_HP, (int) getCurrentHp());
				su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
				player.sendPacket(su);
				su = null;
			}
			
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			TextBuilder html1 = new TextBuilder("<html><body><center><font color=\"LEVEL\">NPC Information</font></center>");
			html1.append("<br>");
			html1.append("Instance Type: " + getClass().getSimpleName() + "<br1>");
			html1.append("Spawn ID: " + getSpawn().getId() + " " + (getSpawn().isCustomSpawn() ? "(Custom spawn)" : "") + "<br1>");
			html1.append("<table border=\"0\" width=\"100%\">");
			html1.append("<tr><td>Object ID: " + getObjectId() + "</td></tr>");
			html1.append("<tr><td>Template ID: " + getTemplate().npcId + "</td></tr>");
			html1.append("<tr><td><br></td></tr>");
			
			html1.append("<tr><td>HP: " + getCurrentHp() + "</td></tr>");
			html1.append("<tr><td>MP: " + getCurrentMp() + "</td></tr>");
			html1.append("<tr><td>Level: " + getLevel() + "</td></tr>");
			html1.append("<tr><td><br></td></tr>");
			
			// changed by terry 2005-02-22 21:45
			html1.append("</table><table><tr><td><button value=\"Edit NPC\" action=\"bypass -h admin_edit_npc " + getTemplate().npcId + "\" width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
			html1.append("<td><button value=\"Kill\" action=\"bypass -h admin_kill\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
			html1.append("<tr><td><button value=\"Show DropList\" action=\"bypass -h admin_show_droplist " + getTemplate().npcId + "\" width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
			html1.append("<td><button value=\"Delete\" action=\"bypass -h admin_delete\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
			html1.append("<tr><td><button value=\"Show Spawnlist\" action=\"bypass -h admin_list_spawns " + getTemplate().getNpcId() + " -1 " + getSpawn().getId() + "\" width=100 height=20 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td></td></tr>");
			html1.append("</table>");
			
			if (player.isGM())
			{
				html1.append("<button value=\"Lease next week\" action=\"bypass -h npc_" + getObjectId() + "_Lease\" width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
				html1.append("<button value=\"Abort current leasing\" action=\"bypass -h npc_" + getObjectId() + "_Lease next\" width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
			}
			
			html1.append("</body></html>");
			
			html.setHtml(html1.toString());
			player.sendPacket(html);
		}
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
}
