package com.l2jfrozen.gameserver.model.actor.instance;

import java.util.StringTokenizer;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.ai.CtrlIntention;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.MyTargetSelected;
import com.l2jfrozen.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jfrozen.gameserver.network.serverpackets.ValidateLocation;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;

/**
 * @author programmos, scoria dev
 */
public class L2FortMerchantInstance extends L2NpcWalkerInstance
{
	public L2FortMerchantInstance(final int objectID, final L2NpcTemplate template)
	{
		super(objectID, template);
	}
	
	@Override
	public void onAction(final L2PcInstance player)
	{
		if (!canTarget(player))
		{
			return;
		}
		
		// Check if the L2PcInstance already target the L2NpcInstance
		if (this != player.getTarget())
		{
			// Set the target of the L2PcInstance player
			player.setTarget(this);
			
			// Send a Server->Client packet MyTargetSelected to the L2PcInstance player
			MyTargetSelected my = new MyTargetSelected(getObjectId(), 0);
			player.sendPacket(my);
			my = null;
			
			// Send a Server->Client packet ValidateLocation to correct the L2NpcInstance position and heading on the client
			player.sendPacket(new ValidateLocation(this));
		}
		else
		{
			// Calculate the distance between the L2PcInstance and the L2NpcInstance
			if (!canInteract(player))
			{
				// Notify the L2PcInstance AI with AI_INTENTION_INTERACT
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
			}
			else
			{
				showMessageWindow(player);
			}
		}
		// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	@Override
	public void onBypassFeedback(final L2PcInstance player, final String command)
	{
		StringTokenizer st = new StringTokenizer(command, " ");
		String actualCommand = st.nextToken(); // Get actual command
		
		String par = "";
		if (st.countTokens() >= 1)
		{
			par = st.nextToken();
		}
		
		// LOGGER.info("actualCommand : " + actualCommand);
		// LOGGER.info("par : " + par);
		
		if (actualCommand.equalsIgnoreCase("Chat"))
		{
			int val = 0;
			try
			{
				val = Integer.parseInt(par);
			}
			catch (IndexOutOfBoundsException | NumberFormatException ioobe)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					ioobe.printStackTrace();
				}
			}
			showMessageWindow(player, val);
		}
		else if (actualCommand.equalsIgnoreCase("showSiegeInfo"))
		{
			showSiegeInfoWindow(player);
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
		st = null;
		actualCommand = null;
	}
	
	private void showMessageWindow(final L2PcInstance player)
	{
		showMessageWindow(player, 0);
	}
	
	private void showMessageWindow(final L2PcInstance player, final int val)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		
		String filename;
		
		if (val == 0)
		{
			filename = "data/html/fortress/merchant.htm";
		}
		else
		{
			filename = "data/html/fortress/merchant-" + val + ".htm";
		}
		
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcId%", String.valueOf(getNpcId()));
		if (getFort().getOwnerClan() != null)
		{
			html.replace("%clanname%", getFort().getOwnerClan().getName());
		}
		else
		{
			html.replace("%clanname%", "NPC");
		}
		
		html.replace("%castleid%", Integer.toString(getCastle().getCastleId()));
		player.sendPacket(html);
		filename = null;
		html = null;
	}
	
	/**
	 * If siege is in progress shows the Busy HTML<BR>
	 * else Shows the SiegeInfo window
	 * @param player
	 */
	public void showSiegeInfoWindow(final L2PcInstance player)
	{
		if (validateCondition(player))
		{
			getFort().getSiege().listRegisterClan(player);
		}
		else
		{
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			html.setFile("data/html/fortress/merchant-busy.htm");
			html.replace("%fortname%", getFort().getName());
			html.replace("%objectId%", String.valueOf(getObjectId()));
			player.sendPacket(html);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			html = null;
		}
	}
	
	private boolean validateCondition(final L2PcInstance player)
	{
		if (getFort().getSiege().getIsInProgress())
		{
			return false; // Busy because of siege
		}
		return true;
	}
	
}
