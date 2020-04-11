package com.l2jfrozen.gameserver.model.actor.instance;

import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Effect.EffectType;
import com.l2jfrozen.gameserver.model.entity.sevensigns.SevenSigns;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;

/**
 * The Class L2CastleMagicianInstance.
 * @author Kerberos | ZaKaX
 */
public class L2CastleMagicianInstance extends L2NpcInstance
{
	
	/** The Constant COND_ALL_FALSE. */
	protected static final int COND_ALL_FALSE = 0;
	
	/** The Constant COND_BUSY_BECAUSE_OF_SIEGE. */
	protected static final int COND_BUSY_BECAUSE_OF_SIEGE = 1;
	
	/** The Constant COND_OWNER. */
	protected static final int COND_OWNER = 2;
	
	/**
	 * Instantiates a new l2 castle magician instance.
	 * @param objectId the object id
	 * @param template the template
	 */
	public L2CastleMagicianInstance(final int objectId, final L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void showChatWindow(final L2PcInstance player, final int val)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		String filename = "data/html/castlemagician/magician-no.htm";
		
		final int condition = validateCondition(player);
		if (condition > COND_ALL_FALSE)
		{
			if (condition == COND_BUSY_BECAUSE_OF_SIEGE)
			{
				filename = "data/html/castlemagician/magician-busy.htm"; // Busy because of siege
			}
			else if (condition == COND_OWNER) // Clan owns castle
			{
				if (val == 0)
				{
					filename = "data/html/castlemagician/magician.htm";
				}
				else
				{
					filename = "data/html/castlemagician/magician-" + val + ".htm";
				}
			}
		}
		
		final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(html);
	}
	
	@Override
	public void onBypassFeedback(final L2PcInstance player, final String command)
	{
		if (command.startsWith("Chat"))
		{
			int val = 0;
			try
			{
				val = Integer.parseInt(command.substring(5));
			}
			catch (IndexOutOfBoundsException | NumberFormatException ioobe)
			{
			}
			showChatWindow(player, val);
			return;
		}
		else if (command.equals("gotoleader"))
		{
			if (player.getClan() != null)
			{
				final L2PcInstance clanLeader = player.getClan().getLeader().getPlayerInstance();
				if (clanLeader == null)
				{
					return;
				}
				
				if (clanLeader.getFirstEffect(EffectType.CLAN_GATE) != null)
				{
					if (!validateGateCondition(clanLeader, player))
					{
						return;
					}
					
					player.teleToLocation(clanLeader.getX(), clanLeader.getY(), clanLeader.getZ(), false);
					return;
				}
				final String filename = "data/html/castlemagician/magician-nogate.htm";
				showChatWindow(player, filename);
			}
			return;
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}
	
	/**
	 * Validate condition.
	 * @param  player the player
	 * @return        the int
	 */
	protected int validateCondition(final L2PcInstance player)
	{
		if (getCastle() != null && getCastle().getCastleId() > 0)
		{
			if (player.getClan() != null)
			{
				if (getCastle().getZone().isSiegeActive())
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
	
	/**
	 * Validate gate condition.
	 * @param  clanLeader the clan leader
	 * @param  player     the player
	 * @return            true, if successful
	 */
	private static final boolean validateGateCondition(final L2PcInstance clanLeader, final L2PcInstance player)
	{
		if (clanLeader.isAlikeDead() || clanLeader.isInStoreMode() || clanLeader.isRooted() || clanLeader.isInCombat() || clanLeader.isInOlympiadMode() || clanLeader.isFestivalParticipant() || clanLeader.inObserverMode() || clanLeader.isInsideZone(L2Character.ZONE_NOSUMMONFRIEND))
		{
			player.sendMessage("Couldn't teleport to clan leader. The requirements was not meet.");
			return false;
		}
		
		if (player.isIn7sDungeon())
		{
			final int targetCabal = SevenSigns.getInstance().getPlayerCabal(clanLeader);
			if (SevenSigns.getInstance().isSealValidationPeriod())
			{
				if (targetCabal != SevenSigns.getInstance().getCabalHighestScore())
				{
					player.sendMessage("Couldn't teleport to clan leader. The requirements was not meet.");
					return false;
				}
			}
			else
			{
				if (targetCabal == SevenSigns.CABAL_NULL)
				{
					player.sendMessage("Couldn't teleport to clan leader. The requirements was not meet.");
					return false;
				}
			}
		}
		
		return true;
	}
}