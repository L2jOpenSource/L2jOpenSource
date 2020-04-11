package com.l2jfrozen.gameserver.handler.itemhandlers;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.ai.CtrlIntention;
import com.l2jfrozen.gameserver.controllers.GameTimeController;
import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.datatables.csv.MapRegionTable;
import com.l2jfrozen.gameserver.handler.IItemHandler;
import com.l2jfrozen.gameserver.managers.CastleManager;
import com.l2jfrozen.gameserver.managers.ClanHallManager;
import com.l2jfrozen.gameserver.managers.FortManager;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jfrozen.gameserver.model.entity.event.CTF;
import com.l2jfrozen.gameserver.model.entity.event.DM;
import com.l2jfrozen.gameserver.model.entity.event.TvT;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.MagicSkillUser;
import com.l2jfrozen.gameserver.network.serverpackets.SetupGauge;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;

/**
 * This class ...
 * @version $Revision: 1.2.3 $ $Date: 2009/04/29 14:01:12 $
 */

public class ScrollOfEscape implements IItemHandler
{
	// all the items ids that this handler knowns
	private static final int[] ITEM_IDS =
	{
		736,
		1830,
		1829,
		1538,
		3958,
		5858,
		5859,
		7117,
		7118,
		7119,
		7120,
		7121,
		7122,
		7123,
		7124,
		7125,
		7126,
		7127,
		7128,
		7129,
		7130,
		7131,
		7132,
		7133,
		7134,
		7135,
		7554,
		7555,
		7556,
		7557,
		7558,
		7559,
		7618,
		7619
	};
	
	@Override
	public void useItem(final L2PlayableInstance playable, final L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance))
		{
			return;
		}
		
		L2PcInstance activeChar = (L2PcInstance) playable;
		
		if (checkConditions(activeChar))
		{
			return;
		}
		
		// Check to see if player is sitting
		if (activeChar.isSitting())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.CANT_MOVE_SITTING));
			return;
		}
		
		// if(activeChar._inEventTvT && TvT._started)
		if (activeChar.inEventTvT && TvT.isStarted())
		{
			activeChar.sendMessage("You can't use Scroll of Escape in TvT.");
			return;
		}
		
		// if(activeChar._inEventDM && DM._started)
		if (activeChar.inEventDM && DM.isStarted())
		{
			activeChar.sendMessage("You can't use Scroll of Escape in DM.");
			return;
		}
		
		// if(activeChar._inEventCTF && CTF._started)
		if (activeChar.inEventCTF && CTF.isStarted())
		{
			activeChar.sendMessage("You can't use Scroll of Escape in CTF.");
			return;
		}
		
		// Check to see if player is on olympiad
		if (activeChar.isInOlympiadMode())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT));
			return;
		}
		
		if (!Config.ALLOW_SOE_IN_PVP && activeChar.getPvpFlag() != 0)
		{
			activeChar.sendMessage("You Can't Use SOE In PvP!");
			return;
		}
		
		// Check to see if the player is in a festival.
		if (activeChar.isFestivalParticipant())
		{
			activeChar.sendPacket(SystemMessage.sendString("You may not use an escape skill in a festival."));
			return;
		}
		
		// Check to see if player is in jail
		if (activeChar.isInJail())
		{
			activeChar.sendPacket(SystemMessage.sendString("You can not escape from jail."));
			return;
		}
		
		// Check to see if player is in a duel
		if (activeChar.isInDuel())
		{
			activeChar.sendPacket(SystemMessage.sendString("You cannot use escape skills during a duel."));
			return;
		}
		
		if (activeChar.isParalyzed())
		{
			activeChar.sendPacket(SystemMessage.sendString("You may not use an escape skill in a paralyzed."));
			return;
		}
		
		// activeChar.abortCast();
		activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		// SoE Animation section
		// Check if this is a blessed scroll, if it is then shorten the cast time.
		final int itemId = item.getItemId();
		
		final SystemMessage sm3 = new SystemMessage(SystemMessageId.USE_S1);
		sm3.addItemName(itemId);
		activeChar.sendPacket(sm3);
		
		final int escapeSkill = itemId == 1538 || itemId == 5858 || itemId == 5859 || itemId == 3958 || itemId == 10130 ? 2036 : 2013;
		
		if (!activeChar.destroyItem("Consume", item.getObjectId(), 1, null, false))
		{
			return;
		}
		
		activeChar.disableAllSkills();
		
		// fix soe
		L2Object oldtarget = activeChar.getTarget();
		activeChar.setTarget(activeChar);
		
		final L2Skill skill = SkillTable.getInstance().getInfo(escapeSkill, 1);
		final MagicSkillUser msu = new MagicSkillUser(activeChar, escapeSkill, 1, skill.getHitTime(), 0);
		activeChar.broadcastPacket(msu);
		activeChar.setTarget(oldtarget);
		SetupGauge sg = new SetupGauge(0, skill.getHitTime());
		activeChar.sendPacket(sg);
		oldtarget = null;
		sg = null;
		// End SoE Animation section
		activeChar.setTarget(null);
		
		SystemMessage sm = new SystemMessage(SystemMessageId.S1_DISAPPEARED);
		sm.addItemName(itemId);
		activeChar.sendPacket(sm);
		sm = null;
		
		EscapeFinalizer ef = new EscapeFinalizer(activeChar, itemId);
		// continue execution later
		activeChar.setSkillCast(ThreadPoolManager.getInstance().scheduleEffect(ef, skill.getHitTime()));
		activeChar.setSkillCastEndTime(10 + GameTimeController.getGameTicks() + skill.getHitTime() / GameTimeController.MILLIS_IN_TICK);
		
		ef = null;
		activeChar = null;
	}
	
	static class EscapeFinalizer implements Runnable
	{
		private final L2PcInstance activeChar;
		private final int itemId;
		
		EscapeFinalizer(final L2PcInstance activeChar, final int itemId)
		{
			this.activeChar = activeChar;
			this.itemId = itemId;
		}
		
		@Override
		public void run()
		{
			if (activeChar.isDead())
			{
				return;
			}
			
			activeChar.enableAllSkills();
			
			activeChar.setIsIn7sDungeon(false);
			
			try
			{
				
				// escape to castle if own's one
				if ((itemId == 1830 || itemId == 5859))
				{
					if (CastleManager.getInstance().getCastleByOwner(activeChar.getClan()) != null)
					{
						activeChar.teleToLocation(MapRegionTable.TeleportWhereType.Castle);
					}
					else
					{
						activeChar.teleToLocation(MapRegionTable.TeleportWhereType.Town);
					}
				}
				// escape to fortress if own's one if own's one
				else if ((itemId == 1830 || itemId == 5859))
				{
					if (FortManager.getInstance().getFortByOwner(activeChar.getClan()) != null)
					{
						activeChar.teleToLocation(MapRegionTable.TeleportWhereType.Fortress);
					}
					else
					{
						activeChar.teleToLocation(MapRegionTable.TeleportWhereType.Town);
					}
				}
				else if ((itemId == 1829 || itemId == 5858) && activeChar.getClan() != null && ClanHallManager.getInstance().getClanHallByOwner(activeChar.getClan()) != null) // escape to clan hall if own's one
				{
					activeChar.teleToLocation(MapRegionTable.TeleportWhereType.ClanHall);
				}
				else if (itemId == 5858) // do nothing
				{
					activeChar.sendPacket(new SystemMessage(SystemMessageId.CLAN_HAS_NO_CLAN_HALL));
					return;
				}
				else if (activeChar.getKarma() > 0 && Config.ALT_KARMA_TELEPORT_TO_FLORAN)
				{
					activeChar.teleToLocation(17836, 170178, -3507, true); // Floran
					return;
				}
				else
				{
					if (itemId < 7117)
					{
						activeChar.teleToLocation(MapRegionTable.TeleportWhereType.Town);
					}
					else
					{
						switch (itemId)
						{
							case 7117:
								activeChar.teleToLocation(-84318, 244579, -3730, true); // Talking Island
								break;
							case 7554:
								activeChar.teleToLocation(-84318, 244579, -3730, true); // Talking Island quest scroll
								break;
							case 7118:
								activeChar.teleToLocation(46934, 51467, -2977, true); // Elven Village
								break;
							case 7555:
								activeChar.teleToLocation(46934, 51467, -2977, true); // Elven Village quest scroll
								break;
							case 7119:
								activeChar.teleToLocation(9745, 15606, -4574, true); // Dark Elven Village
								break;
							case 7556:
								activeChar.teleToLocation(9745, 15606, -4574, true); // Dark Elven Village quest scroll
								break;
							case 7120:
								activeChar.teleToLocation(-44836, -112524, -235, true); // Orc Village
								break;
							case 7557:
								activeChar.teleToLocation(-44836, -112524, -235, true); // Orc Village quest scroll
								break;
							case 7121:
								activeChar.teleToLocation(115113, -178212, -901, true); // Dwarven Village
								break;
							case 7558:
								activeChar.teleToLocation(115113, -178212, -901, true); // Dwarven Village quest scroll
								break;
							case 7122:
								activeChar.teleToLocation(-80826, 149775, -3043, true); // Gludin Village
								break;
							case 7123:
								activeChar.teleToLocation(-12678, 122776, -3116, true); // Gludio Castle Town
								break;
							case 7124:
								activeChar.teleToLocation(15670, 142983, -2705, true); // Dion Castle Town
								break;
							case 7125:
								activeChar.teleToLocation(17836, 170178, -3507, true); // Floran
								break;
							case 7126:
								activeChar.teleToLocation(83400, 147943, -3404, true); // Giran Castle Town
								break;
							case 7559:
								activeChar.teleToLocation(83400, 147943, -3404, true); // Giran Castle Town quest scroll
								break;
							case 7127:
								activeChar.teleToLocation(105918, 109759, -3207, true); // Hardin's Private Academy
								break;
							case 7128:
								activeChar.teleToLocation(111409, 219364, -3545, true); // Heine
								break;
							case 7129:
								activeChar.teleToLocation(82956, 53162, -1495, true); // Oren Castle Town
								break;
							case 7130:
								activeChar.teleToLocation(85348, 16142, -3699, true); // Ivory Tower
								break;
							case 7131:
								activeChar.teleToLocation(116819, 76994, -2714, true); // Hunters Village
								break;
							case 7132:
								activeChar.teleToLocation(146331, 25762, -2018, true); // Aden Castle Town
								break;
							case 7133:
								activeChar.teleToLocation(147928, -55273, -2734, true); // Goddard Castle Town
								break;
							case 7134:
								activeChar.teleToLocation(43799, -47727, -798, true); // Rune Castle Town
								break;
							case 7135:
								activeChar.teleToLocation(87331, -142842, -1317, true); // Schuttgart Castle Town
								break;
							case 7618:
								activeChar.teleToLocation(149864, -81062, -5618, true); // Ketra Orc Village
								break;
							case 7619:
								activeChar.teleToLocation(108275, -53785, -2524, true); // Varka Silenos Village
								break;
							default:
								activeChar.teleToLocation(MapRegionTable.TeleportWhereType.Town);
								break;
						}
					}
				}
			}
			catch (final Throwable e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	private static boolean checkConditions(final L2PcInstance actor)
	{
		return actor.isStunned() || actor.isSleeping() || actor.isParalyzed() || actor.isFakeDeath() || actor.isTeleporting() || actor.isMuted() || actor.isAlikeDead() || actor.isAllSkillsDisabled();
	}
	
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}
