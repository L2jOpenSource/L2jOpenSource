package com.l2jfrozen.gameserver.model.actor.instance;

import com.l2jfrozen.gameserver.ai.CtrlIntention;
import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jfrozen.gameserver.network.serverpackets.Ride;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;

/**
 * @author Scoria, Qwerty
 */

public class L2FortWyvernManagerInstance extends L2NpcInstance
{
	protected static final int COND_ALL_FALSE = 0;
	protected static final int COND_BUSY_BECAUSE_OF_SIEGE = 1;
	protected static final int COND_OWNER = 2;
	
	public L2FortWyvernManagerInstance(final int objectId, final L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onBypassFeedback(final L2PcInstance player, final String command)
	{
		if (command.startsWith("RideWyvern"))
		{
			if (!player.isClanLeader())
			{
				player.sendMessage("Only clan leaders are allowed.");
				return;
			}
			
			int petItemId = 0;
			L2ItemInstance petItem = null;
			
			if (player.getPet() == null)
			{
				if (player.isMounted())
				{
					petItem = player.getInventory().getItemByObjectId(player.getMountObjectID());
					if (petItem != null)
					{
						petItemId = petItem.getItemId();
					}
				}
			}
			else
			{
				petItemId = player.getPet().getControlItemId();
			}
			
			if (petItemId == 0 || !player.isMounted())
			{
				player.sendMessage("Ride your strider first...");
				NpcHtmlMessage html = new NpcHtmlMessage(1);
				html.setFile("data/html/fortress/wyvernmanager-explain.htm");
				html.replace("%count%", String.valueOf(10));
				player.sendPacket(html);
				html = null;
				return;
			}
			else if (player.isMounted() && petItem != null && petItem.getEnchantLevel() < 55)
			{
				NpcHtmlMessage html = new NpcHtmlMessage(1);
				html.setFile("data/html/fortress/wyvernmanager-explain.htm");
				html.replace("%count%", String.valueOf(10));
				player.sendPacket(html);
				html = null;
				return;
			}
			
			// Wyvern requires Config.MANAGER_CRYSTAL_COUNT crystal for ride...
			if (player.getInventory().getItemByItemId(1460) != null && player.getInventory().getItemByItemId(1460).getCount() >= 10)
			{
				if (!player.disarmWeapons())
				{
					return;
				}
				
				if (player.isMounted())
				{
					player.dismount();
				}
				
				if (player.getPet() != null)
				{
					player.getPet().unSummon(player);
				}
				
				player.getInventory().destroyItemByItemId("Wyvern", 1460, 10, player, player.getTarget());
				
				final Ride mount = new Ride(player.getObjectId(), Ride.ACTION_MOUNT, 12621);
				player.sendPacket(mount);
				player.broadcastPacket(mount);
				player.setMountType(mount.getMountType());
				
				player.addSkill(SkillTable.getInstance().getInfo(4289, 1));
				player.sendMessage("The Wyvern has been summoned successfully!");
				
			}
			else
			{
				NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				html.setFile("data/html/fortress/wyvernmanager-explain.htm");
				html.replace("%count%", String.valueOf(10));
				player.sendPacket(html);
				html = null;
				player.sendMessage("You need 10 Crystals: B Grade.");
			}
			
			petItem = null;
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
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
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	private void showMessageWindow(final L2PcInstance player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		String filename = "data/html/fortress/wyvernmanager-no.htm";
		
		final int condition = validateCondition(player);
		
		if (condition > COND_ALL_FALSE)
		{
			if (condition == COND_OWNER)
			{
				filename = "data/html/fortress/wyvernmanager.htm";
			}
		}
		
		NpcHtmlMessage html = new NpcHtmlMessage(1);
		html.setFile(filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%count%", String.valueOf(10));
		player.sendPacket(html);
		filename = null;
		html = null;
	}
	
	protected int validateCondition(final L2PcInstance player)
	{
		if (getFort() != null && getFort().getFortId() > 0)
		{
			if (player.getClan() != null)
			{
				if (getFort().getSiege().getIsInProgress())
				{
					return COND_BUSY_BECAUSE_OF_SIEGE; // Busy because of siege
				}
				else if (getFort().getOwnerId() == player.getClanId() && player.isClanLeader())
				{
					return COND_OWNER; // Owner
				}
			}
		}
		return COND_ALL_FALSE;
	}
}
