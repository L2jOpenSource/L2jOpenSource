package com.l2jfrozen.gameserver.model.actor.instance;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.ai.CtrlIntention;
import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.managers.CoupleManager;
import com.l2jfrozen.gameserver.model.Inventory;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.entity.Announcements;
import com.l2jfrozen.gameserver.model.entity.Wedding;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.MagicSkillUser;
import com.l2jfrozen.gameserver.network.serverpackets.MyTargetSelected;
import com.l2jfrozen.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jfrozen.gameserver.network.serverpackets.ValidateLocation;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;

/**
 * The Class L2WeddingManagerInstance.
 */
public class L2WeddingManagerInstance extends L2NpcInstance
{
	
	/**
	 * Instantiates a new l2 wedding manager instance.
	 * @param  objectId the object id
	 * @param  template the template
	 * @author          evill33t & squeezed
	 */
	public L2WeddingManagerInstance(final int objectId, final L2NpcTemplate template)
	{
		super(objectId, template);
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
			player.sendPacket(new MyTargetSelected(getObjectId(), 0));
			
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
	
	/**
	 * Show message window.
	 * @param player the player
	 */
	private void showMessageWindow(final L2PcInstance player)
	{
		String filename = "data/html/mods/Wedding_start.htm";
		String replace = String.valueOf(Config.L2JMOD_WEDDING_PRICE);
		
		NpcHtmlMessage html = new NpcHtmlMessage(1);
		html.setFile(filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%replace%", replace);
		html.replace("%npcname%", getName());
		player.sendPacket(html);
		filename = null;
		replace = null;
		html = null;
	}
	
	@Override
	public void onBypassFeedback(final L2PcInstance player, final String command)
	{
		// standard msg
		String filename = "data/html/mods/Wedding_start.htm";
		String replace = "";
		
		// if player has no partner
		if (player.getPartnerId() == 0)
		{
			filename = "data/html/mods/Wedding_nopartner.htm";
			sendHtmlMessage(player, filename, replace);
			return;
		}
		
		L2PcInstance ptarget = (L2PcInstance) L2World.getInstance().findObject(player.getPartnerId());
		// partner online ?
		if (ptarget == null || !ptarget.isOnline())
		{
			filename = "data/html/mods/Wedding_notfound.htm";
			sendHtmlMessage(player, filename, replace);
			return;
		}
		
		// already married ?
		if (player.isMarried())
		{
			filename = "data/html/mods/Wedding_already.htm";
			sendHtmlMessage(player, filename, replace);
			return;
		}
		else if (player.isMarryAccepted())
		{
			filename = "data/html/mods/Wedding_waitforpartner.htm";
			sendHtmlMessage(player, filename, replace);
			return;
		}
		else if (command.startsWith("AcceptWedding"))
		{
			// accept the wedding request
			player.setMarryAccepted(true);
			
			int type;
			if (player.getAppearance().getSex() && ptarget.getAppearance().getSex())
			{
				player.getAppearance().setNameColor(Config.L2JMOD_WEDDING_NAME_COLOR_LESBO);
				ptarget.getAppearance().setNameColor(Config.L2JMOD_WEDDING_NAME_COLOR_LESBO);
				type = 1;
			}
			else if (!player.getAppearance().getSex() && !ptarget.getAppearance().getSex())
			{
				player.getAppearance().setNameColor(Config.L2JMOD_WEDDING_NAME_COLOR_GEY);
				ptarget.getAppearance().setNameColor(Config.L2JMOD_WEDDING_NAME_COLOR_GEY);
				type = 2;
			}
			else
			{
				player.getAppearance().setNameColor(Config.L2JMOD_WEDDING_NAME_COLOR_NORMAL);
				ptarget.getAppearance().setNameColor(Config.L2JMOD_WEDDING_NAME_COLOR_NORMAL);
				type = 0;
			}
			
			final Wedding wedding = CoupleManager.getInstance().getCouple(player.getCoupleId());
			wedding.marry(type);
			
			// messages to the couple
			player.sendMessage("Congratulations you are married!");
			player.setMarried(true);
			player.setMaryRequest(false);
			player.setmarriedType(type);
			ptarget.sendMessage("Congratulations you are married!");
			ptarget.setMarried(true);
			ptarget.setMaryRequest(false);
			ptarget.setmarriedType(type);
			
			if (Config.WEDDING_GIVE_CUPID_BOW)
			{
				player.addItem("Cupids Bow", 9140, 1, player, true);
				player.getInventory().updateDatabase();
				ptarget.addItem("Cupids Bow", 9140, 1, ptarget, true);
				ptarget.getInventory().updateDatabase();
				player.sendSkillList();
				ptarget.sendSkillList();
			}
			
			// wedding march
			MagicSkillUser MSU = new MagicSkillUser(player, player, 2230, 1, 1, 0);
			player.broadcastPacket(MSU);
			MSU = new MagicSkillUser(ptarget, ptarget, 2230, 1, 1, 0);
			ptarget.broadcastPacket(MSU);
			MSU = null;
			
			// fireworks
			L2Skill skill = SkillTable.getInstance().getInfo(2025, 1);
			if (skill != null)
			{
				MSU = new MagicSkillUser(player, player, 2025, 1, 1, 0);
				player.sendPacket(MSU);
				player.broadcastPacket(MSU);
				player.useMagic(skill, false, false);
				MSU = null;
				
				MSU = new MagicSkillUser(ptarget, ptarget, 2025, 1, 1, 0);
				ptarget.sendPacket(MSU);
				ptarget.broadcastPacket(MSU);
				ptarget.useMagic(skill, false, false);
				MSU = null;
				
				skill = null;
			}
			
			if (Config.ANNOUNCE_WEDDING)
			{
				Announcements.getInstance().announceToAll("Congratulations to " + player.getName() + " and " + ptarget.getName() + "! They have been married.");
			}
			
			filename = "data/html/mods/Wedding_accepted.htm";
			replace = ptarget.getName();
			sendHtmlMessage(ptarget, filename, replace);
			return;
		}
		else if (command.startsWith("DeclineWedding"))
		{
			player.setMaryRequest(false);
			ptarget.setMaryRequest(false);
			player.setMarryAccepted(false);
			ptarget.setMarryAccepted(false);
			player.getAppearance().setNameColor(0xFFFFFF);
			ptarget.getAppearance().setNameColor(0xFFFFFF);
			player.sendMessage("You declined");
			ptarget.sendMessage("Your partner declined");
			replace = ptarget.getName();
			filename = "data/html/mods/Wedding_declined.htm";
			sendHtmlMessage(ptarget, filename, replace);
			return;
		}
		else if (player.isMaryRequest())
		{
			// check for formalwear
			if (Config.L2JMOD_WEDDING_FORMALWEAR)
			{
				Inventory inv3 = player.getInventory();
				L2ItemInstance item3 = inv3.getPaperdollItem(10);
				if (item3 == null)
				{
					player.setIsWearingFormalWear(false);
				}
				else
				{
					String strItem = Integer.toString(item3.getItemId());
					String frmWear = Integer.toString(6408);
					player.sendMessage(strItem);
					if (strItem.equals(frmWear))
					{
						player.setIsWearingFormalWear(true);
					}
					else
					{
						player.setIsWearingFormalWear(false);
					}
					strItem = null;
					frmWear = null;
				}
				inv3 = null;
				item3 = null;
			}
			
			if (Config.L2JMOD_WEDDING_FORMALWEAR && !player.isWearingFormalWear())
			{
				filename = "data/html/mods/Wedding_noformal.htm";
				sendHtmlMessage(player, filename, replace);
				return;
			}
			
			filename = "data/html/mods/Wedding_ask.htm";
			player.setMaryRequest(false);
			ptarget.setMaryRequest(false);
			replace = ptarget.getName();
			sendHtmlMessage(player, filename, replace);
			return;
		}
		else if (command.startsWith("AskWedding"))
		{
			// check for formalwear
			if (Config.L2JMOD_WEDDING_FORMALWEAR)
			{
				Inventory inv3 = player.getInventory();
				L2ItemInstance item3 = inv3.getPaperdollItem(10);
				
				if (null == item3)
				{
					player.setIsWearingFormalWear(false);
				}
				else
				{
					String frmWear = Integer.toString(6408);
					String strItem = null;
					strItem = Integer.toString(item3.getItemId());
					
					if (null != strItem && strItem.equals(frmWear))
					{
						player.setIsWearingFormalWear(true);
					}
					else
					{
						player.setIsWearingFormalWear(false);
					}
					frmWear = null;
					strItem = null;
				}
				inv3 = null;
				item3 = null;
			}
			
			if (Config.L2JMOD_WEDDING_FORMALWEAR && !player.isWearingFormalWear())
			{
				filename = "data/html/mods/Wedding_noformal.htm";
				sendHtmlMessage(player, filename, replace);
				return;
			}
			else if (player.getAdena() < Config.L2JMOD_WEDDING_PRICE)
			{
				filename = "data/html/mods/Wedding_adena.htm";
				replace = String.valueOf(Config.L2JMOD_WEDDING_PRICE);
				sendHtmlMessage(player, filename, replace);
				return;
			}
			else
			{
				player.setMarryAccepted(true);
				ptarget.setMaryRequest(true);
				replace = ptarget.getName();
				filename = "data/html/mods/Wedding_requested.htm";
				player.getInventory().reduceAdena("Wedding", Config.L2JMOD_WEDDING_PRICE, player, player.getLastFolkNPC());
				sendHtmlMessage(player, filename, replace);
				return;
			}
		}
		ptarget = null;
		sendHtmlMessage(player, filename, replace);
		filename = null;
		replace = null;
	}
	
	/**
	 * Send html message.
	 * @param player   the player
	 * @param filename the filename
	 * @param replace  the replace
	 */
	private void sendHtmlMessage(final L2PcInstance player, final String filename, final String replace)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(1);
		html.setFile(filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%replace%", replace);
		html.replace("%npcname%", getName());
		player.sendPacket(html);
		html = null;
	}
}
