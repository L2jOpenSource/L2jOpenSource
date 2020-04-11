package com.l2jfrozen.gameserver.model.actor.instance;

import com.l2jfrozen.gameserver.ai.CtrlIntention;
import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.managers.ClanHallManager;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.entity.ClanHall;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.MyTargetSelected;
import com.l2jfrozen.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jfrozen.gameserver.network.serverpackets.Ride;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.network.serverpackets.ValidateLocation;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;

/**
 * The Class L2WyvernManagerInstance.
 */
public class L2WyvernManagerInstance extends L2CastleChamberlainInstance
{
	
	/** The Constant COND_CLAN_OWNER. */
	protected static final int COND_CLAN_OWNER = 3;
	
	/** The clan hall id. */
	private int clanHallId = -1;
	
	/**
	 * Instantiates a new l2 wyvern manager instance.
	 * @param objectId the object id
	 * @param template the template
	 */
	public L2WyvernManagerInstance(final int objectId, final L2NpcTemplate template)
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
			if (player.getPet() == null)
			{
				if (player.isMounted())
				{
					final SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
					sm.addString("You Already Have a Pet or Are Mounted.");
					player.sendPacket(sm);
				}
				else
				{
					final SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
					sm.addString("Summon your Strider first.");
					player.sendPacket(sm);
				}
				return;
			}
			else if (player.getPet().getNpcId() == 12526 || player.getPet().getNpcId() == 12527 || player.getPet().getNpcId() == 12528)
			{
				if (player.getInventory().getItemByItemId(1460) != null && player.getInventory().getItemByItemId(1460).getCount() >= 10)
				{
					if (player.getPet().getLevel() < 55)
					{
						final SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
						sm.addString("Your Strider Has not reached the required level.");
						player.sendPacket(sm);
					}
					else
					{
						if (!player.disarmWeapons())
						{
							return;
						}
						player.getPet().unSummon(player);
						player.getInventory().destroyItemByItemId("Wyvern", 1460, 10, player, player.getTarget());
						final Ride mount = new Ride(player.getObjectId(), Ride.ACTION_MOUNT, 12621);
						player.sendPacket(mount);
						player.broadcastPacket(mount);
						player.setMountType(mount.getMountType());
						player.addSkill(SkillTable.getInstance().getInfo(4289, 1));
						final SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
						sm.addString("The Wyvern has been summoned successfully!");
						player.sendPacket(sm);
					}
				}
				else
				{
					final SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
					sm.addString("You need 10 Crystals: B Grade.");
					player.sendPacket(sm);
				}
				return;
			}
			else
			{
				final SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
				sm.addString("Unsummon your pet.");
				player.sendPacket(sm);
			}
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
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	/**
	 * Show message window.
	 * @param player the player
	 */
	private void showMessageWindow(final L2PcInstance player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		String filename = "data/html/wyvernmanager/wyvernmanager-no.htm";
		if (getClanHall() != null)
		{
			filename = "data/html/wyvernmanager/wyvernmanager-clan-no.htm";
		}
		final int condition = validateCondition(player);
		if (condition > COND_ALL_FALSE)
		{
			if (condition == COND_OWNER)
			{
				filename = "data/html/wyvernmanager/wyvernmanager.htm"; // Owner message window
			}
			else if (condition == COND_CLAN_OWNER)
			{
				filename = "data/html/wyvernmanager/wyvernmanager-clan.htm";
			}
		}
		NpcHtmlMessage html = new NpcHtmlMessage(1);
		html.setFile(filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcname%", getName());
		player.sendPacket(html);
		html = null;
		filename = null;
	}
	
	/**
	 * Return the L2ClanHall this L2NpcInstance belongs to.
	 * @return the clan hall
	 */
	public final ClanHall getClanHall()
	{
		if (clanHallId < 0)
		{
			ClanHall temp = ClanHallManager.getInstance().getNearbyClanHall(getX(), getY(), 500);
			
			if (temp != null)
			{
				clanHallId = temp.getId();
				temp = null;
			}
			
			if (clanHallId < 0)
			{
				return null;
			}
		}
		return ClanHallManager.getInstance().getClanHallById(clanHallId);
	}
	
	@Override
	protected int validateCondition(final L2PcInstance player)
	{
		if (getClanHall() != null && player.getClan() != null)
		{
			if (getClanHall().getOwnerId() == player.getClanId() && player.isClanLeader())
			{
				return COND_CLAN_OWNER; // Owner of the clanhall
			}
		}
		else if (super.getCastle() != null && super.getCastle().getCastleId() > 0)
		{
			if (player.getClan() != null)
			{
				// Checks if player is in Sieage Zone, he can't use wyvern!!
				if (super.isInsideZone(L2Character.ZONE_SIEGE) || super.getCastle().getSiege().getIsInProgress())
				{
					return COND_BUSY_BECAUSE_OF_SIEGE; // Busy because of siege
				}
				else if (super.getCastle().getOwnerId() == player.getClanId() // Clan owns castle
					&& player.isClanLeader())
				{
					return COND_OWNER; // Owner
				}
			}
		}
		
		return COND_ALL_FALSE;
	}
}
