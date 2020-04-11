package com.l2jfrozen.gameserver.model.actor.instance;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.L2Clan;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.network.serverpackets.WareHouseDepositList;
import com.l2jfrozen.gameserver.network.serverpackets.WareHouseWithdrawalList;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;

/**
 * The Class L2CastleWarehouseInstance.
 * @author l3x
 */
public class L2CastleWarehouseInstance extends L2FolkInstance
{
	
	/** The Constant COND_ALL_FALSE. */
	protected static final int COND_ALL_FALSE = 0;
	
	/** The Constant COND_BUSY_BECAUSE_OF_SIEGE. */
	protected static final int COND_BUSY_BECAUSE_OF_SIEGE = 1;
	
	/** The Constant COND_OWNER. */
	protected static final int COND_OWNER = 2;
	
	/**
	 * Instantiates a new l2 castle warehouse instance.
	 * @param objectId the object id
	 * @param template the template
	 */
	public L2CastleWarehouseInstance(final int objectId, final L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	/**
	 * Show retrieve window.
	 * @param player the player
	 */
	private void showRetrieveWindow(final L2PcInstance player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		player.setActiveWarehouse(player.getWarehouse());
		
		if (player.getActiveWarehouse().getSize() == 0)
		{
			player.sendPacket(new SystemMessage(SystemMessageId.NO_ITEM_DEPOSITED_IN_WH));
			return;
		}
		
		player.sendPacket(new WareHouseWithdrawalList(player, WareHouseWithdrawalList.PRIVATE));
	}
	
	/**
	 * Show deposit window.
	 * @param player the player
	 */
	private void showDepositWindow(final L2PcInstance player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		player.setActiveWarehouse(player.getWarehouse());
		player.tempInvetoryDisable();
		
		player.sendPacket(new WareHouseDepositList(player, WareHouseDepositList.PRIVATE));
	}
	
	/**
	 * Show deposit window clan.
	 * @param player the player
	 */
	private void showDepositWindowClan(final L2PcInstance player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		if (player.getClan() != null)
		{
			if (player.getClan().getLevel() == 0)
			{
				player.sendPacket(new SystemMessage(SystemMessageId.ONLY_LEVEL_1_CLAN_OR_HIGHER_CAN_USE_WAREHOUSE));
			}
			else
			{
				if ((player.getClanPrivileges() & L2Clan.CP_CL_VIEW_WAREHOUSE) != L2Clan.CP_CL_VIEW_WAREHOUSE)
				{
					player.sendPacket(new SystemMessage(SystemMessageId.ONLY_CLAN_LEADER_CAN_RETRIEVE_ITEMS_FROM_CLAN_WAREHOUSE));
				}
				player.setActiveWarehouse(player.getClan().getWarehouse());
				player.tempInvetoryDisable();
				
				player.sendPacket(new WareHouseDepositList(player, WareHouseDepositList.CLAN));
			}
		}
	}
	
	/**
	 * Show withdraw window clan.
	 * @param player the player
	 */
	private void showWithdrawWindowClan(final L2PcInstance player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		if ((player.getClanPrivileges() & L2Clan.CP_CL_VIEW_WAREHOUSE) != L2Clan.CP_CL_VIEW_WAREHOUSE)
		{
			player.sendPacket(new SystemMessage(SystemMessageId.YOU_DO_NOT_HAVE_THE_RIGHT_TO_USE_CLAN_WAREHOUSE));
			return;
		}
		
		if (player.getClan().getLevel() == 0)
		{
			player.sendPacket(new SystemMessage(SystemMessageId.ONLY_LEVEL_1_CLAN_OR_HIGHER_CAN_USE_WAREHOUSE));
		}
		else
		{
			player.setActiveWarehouse(player.getClan().getWarehouse());
			player.sendPacket(new WareHouseWithdrawalList(player, WareHouseWithdrawalList.CLAN));
		}
	}
	
	@Override
	public void onBypassFeedback(final L2PcInstance player, final String command)
	{
		if (player.getActiveEnchantItem() != null)
		{
			LOGGER.info("Player " + player.getName() + " trying to use enchant exploit, ban this player!");
			player.closeNetConnection();
			return;
		}
		
		if (command.startsWith("WithdrawP"))
		{
			showRetrieveWindow(player);
		}
		else if (command.equals("DepositP"))
		{
			showDepositWindow(player);
		}
		else if (command.equals("WithdrawC"))
		{
			showWithdrawWindowClan(player);
		}
		else if (command.equals("DepositC"))
		{
			showDepositWindowClan(player);
		}
		else if (command.startsWith("Chat"))
		{
			int val = 0;
			try
			{
				val = Integer.parseInt(command.substring(5));
			}
			catch (IndexOutOfBoundsException | NumberFormatException ioobe)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					ioobe.printStackTrace();
				}
			}
			showChatWindow(player, val);
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}
	
	@Override
	public void showChatWindow(final L2PcInstance player, final int val)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		String filename = "data/html/castlewarehouse/castlewarehouse-no.htm";
		
		final int condition = validateCondition(player);
		if (condition > COND_ALL_FALSE)
		{
			if (condition == COND_BUSY_BECAUSE_OF_SIEGE)
			{
				filename = "data/html/castlewarehouse/castlewarehouse-busy.htm"; // Busy because of siege
			}
			else if (condition == COND_OWNER) // Clan owns castle
			{
				if (val == 0)
				{
					filename = "data/html/castlewarehouse/castlewarehouse.htm";
				}
				else
				{
					filename = "data/html/castlewarehouse/castlewarehouse-" + val + ".htm";
				}
			}
		}
		
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcname%", getName());
		player.sendPacket(html);
		filename = null;
		html = null;
	}
	
	/**
	 * Validate condition.
	 * @param  player the player
	 * @return        the int
	 */
	protected int validateCondition(final L2PcInstance player)
	{
		if (player.isGM())
		{
			return COND_OWNER;
		}
		if (getCastle() != null && getCastle().getCastleId() > 0)
		{
			if (player.getClan() != null)
			{
				if (getCastle().getSiege().getIsInProgress())
				{
					return COND_BUSY_BECAUSE_OF_SIEGE; // Busy because of siege
				}
				else if (getCastle().getOwnerId() == player.getClanId())
				{
					return COND_OWNER;
				}
			}
		}
		return COND_ALL_FALSE;
	}
}
