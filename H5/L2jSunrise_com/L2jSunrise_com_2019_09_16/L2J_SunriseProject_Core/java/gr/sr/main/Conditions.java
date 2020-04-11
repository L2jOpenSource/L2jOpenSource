package gr.sr.main;

import java.util.Iterator;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import l2r.Config;
import l2r.gameserver.enums.ZoneIdType;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.base.ClassId;
import l2r.gameserver.model.base.PlayerClass;
import l2r.gameserver.model.base.SubClass;
import l2r.gameserver.model.entity.olympiad.OlympiadManager;
import l2r.gameserver.model.quest.Quest;
import l2r.gameserver.model.quest.QuestState;
import l2r.gameserver.network.SystemMessageId;

import gr.sr.interf.SunriseEvents;

/**
 * @author L2jSunrise Team
 * @Website www.l2jsunrise.com
 */
public class Conditions
{
	private Conditions()
	{
		// Dummy default
	}
	
	/**
	 * Check the general requirements for the player to be able to use it
	 * @param player [L2PcInstance]
	 * @return boolean
	 */
	public static boolean checkPlayerConditions(L2PcInstance player)
	{
		if (player.isGM())
		{
			return true;
		}
		
		// Restrictions added here
		if (player.isInOlympiadMode() || player.isInOlympiad() || player.inObserverMode() || OlympiadManager.getInstance().isRegistered(player))
		{
			player.sendMessage("Cannot use while in Olympiad.");
			return false;
		}
		
		if (player.getKarma() > 0)
		{
			player.sendMessage("Cannot use while hava karma.");
			return false;
		}
		
		if (player.isJailed())
		{
			player.sendMessage("Cannot use while in Jail.");
			return false;
		}
		
		if (player.isEnchanting())
		{
			player.sendMessage("Cannot use while Enchanting.");
			return false;
		}
		
		if (player.isAlikeDead())
		{
			player.sendMessage("Cannot use while Dead or Fake Death.");
			return false;
		}
		
		if (player.isInsideZone(ZoneIdType.ZONE_CHAOTIC))
		{
			player.sendMessage("Cannot use in chaotic zone.");
			return false;
		}
		
		if ((player.getPvpFlag() != 0) && !player.isInsideZone(ZoneIdType.PEACE))
		{
			player.sendMessage("Cannot use while in pvp flag.");
			return false;
		}
		
		if (player.isInCombat() && !player.isInsideZone(ZoneIdType.PEACE))
		{
			player.sendMessage("Cannot use while in combat.");
			return false;
		}
		
		return true;
	}
	
	/**
	 * Check the basic requirements for the player to be able to use it
	 * @param player [L2PcInstance]
	 * @return boolean
	 */
	public static boolean checkItemBufferConditions(L2PcInstance player)
	{
		if (player.isGM())
		{
			return true;
		}
		
		// Restrictions added here
		if (player.isInOlympiadMode() || player.isInOlympiad() || player.inObserverMode() || OlympiadManager.getInstance().isRegistered(player))
		{
			player.sendMessage("Cannot use while in Olympiad.");
			return false;
		}
		
		if (SunriseEvents.isRegistered(player) || SunriseEvents.isInEvent(player))
		{
			player.sendMessage("Cannot use while in event.");
			return false;
		}
		
		if (player.getKarma() > 0)
		{
			player.sendMessage("Cannot use while hava karma.");
			return false;
		}
		
		if (player.isJailed())
		{
			player.sendMessage("Cannot use while in Jail.");
			return false;
		}
		
		if (player.isEnchanting())
		{
			player.sendMessage("Cannot use while Enchanting.");
			return false;
		}
		
		if (player.isAlikeDead())
		{
			player.sendMessage("Cannot use while Dead or Fake Death.");
			return false;
		}
		
		if (player.isInsideZone(ZoneIdType.ZONE_CHAOTIC))
		{
			player.sendMessage("Cannot use in chaotic zone.");
			return false;
		}
		
		if ((player.getPvpFlag() != 0) && !player.isInsideZone(ZoneIdType.PEACE))
		{
			player.sendMessage("Cannot use while in pvp flag.");
			return false;
		}
		
		if (player.isInCombat() && !player.isInsideZone(ZoneIdType.PEACE))
		{
			player.sendMessage("Cannot use while in combat.");
			return false;
		}
		
		return true;
	}
	
	/**
	 * Check the basic requirements for the player to be able to use it
	 * @param player [L2PcInstance]
	 * @return boolean
	 */
	public static boolean checkPlayerBasicConditions(L2PcInstance player)
	{
		if (player.isGM())
		{
			return true;
		}
		
		// Restrictions added here
		if (player.isInOlympiadMode() || player.isInOlympiad() || player.inObserverMode() || OlympiadManager.getInstance().isRegistered(player))
		{
			player.sendMessage("Cannot use while in Olympiad.");
			return false;
		}
		
		if (player.getKarma() > 0)
		{
			player.sendMessage("Cannot use while hava karma.");
			return false;
		}
		
		if (player.isEnchanting())
		{
			player.sendMessage("Cannot use while Enchanting.");
			return false;
		}
		
		if (player.isAlikeDead())
		{
			player.sendMessage("Cannot use while Dead or Fake Death.");
			return false;
		}
		
		return true;
	}
	
	/**
	 * Check the item count in inventory
	 * @param player [L2PcInstance]
	 * @param itemId
	 * @param count
	 * @return boolean
	 */
	public static boolean checkPlayerItemCount(L2PcInstance player, int itemId, int count)
	{
		if ((player.getInventory().getItemByItemId(itemId) == null) || (player.getInventory().getItemByItemId(itemId).getCount() < count))
		{
			player.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
			return false;
		}
		return true;
	}
	
	/**
	 * Check the item count in inventory
	 * @param player [L2PcInstance]
	 * @param itemId
	 * @param count
	 * @return boolean
	 */
	public static boolean checkPlayerItemCount(L2PcInstance player, int itemId, long count)
	{
		if ((player.getInventory().getItemByItemId(itemId) == null) || (player.getInventory().getItemByItemId(itemId).getCount() < count))
		{
			player.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
			return false;
		}
		return true;
	}
	
	/**
	 * Method to check if it is a valid clan name
	 * @param name
	 * @return
	 */
	public static boolean isValidName(String name)
	{
		Pattern pattern;
		try
		{
			pattern = Pattern.compile(Config.CLAN_NAME_TEMPLATE);
		}
		catch (PatternSyntaxException e)
		{
			pattern = Pattern.compile(".*");
		}
		return pattern.matcher(name).matches();
	}
	
	public static boolean checkQuests(L2PcInstance player)
	{
		// Noble players can add Sub-Classes without quests
		if (player.isNoble())
		{
			return true;
		}
		
		QuestState qs = player.getQuestState(Quest.FATES_WHISPER);
		if ((qs == null) || !qs.isCompleted())
		{
			return false;
		}
		
		qs = player.getQuestState(Quest.MIMIRS_ELIXIR);
		if ((qs == null) || !qs.isCompleted())
		{
			return false;
		}
		
		return true;
	}
	
	/**
	 * Returns list of available subclasses Base class and already used subclasses removed
	 * @param player
	 * @return
	 */
	public final static Set<PlayerClass> getAvailableSubClasses(L2PcInstance player)
	{
		// get player base class
		final int currentBaseId = player.getBaseClass();
		final ClassId baseCID = ClassId.getClassId(currentBaseId);
		
		// we need 2nd occupation ID
		final int baseClassId;
		if (baseCID.level() > 2)
		{
			baseClassId = baseCID.getParent().ordinal();
		}
		else
		{
			baseClassId = currentBaseId;
		}
		
		Set<PlayerClass> availSubs = PlayerClass.values()[baseClassId].getAvailableSubclasses(player);
		
		if ((availSubs != null) && !availSubs.isEmpty())
		{
			for (Iterator<PlayerClass> availSub = availSubs.iterator(); availSub.hasNext();)
			{
				PlayerClass pclass = availSub.next();
				
				// check for the village master
				if (!checkVillageMaster(pclass))
				{
					availSub.remove();
					continue;
				}
				
				// scan for already used subclasses
				int availClassId = pclass.ordinal();
				ClassId cid = ClassId.getClassId(availClassId);
				SubClass prevSubClass;
				ClassId subClassId;
				for (Iterator<SubClass> subList = iterSubClasses(player); subList.hasNext();)
				{
					prevSubClass = subList.next();
					subClassId = ClassId.getClassId(prevSubClass.getClassId());
					
					if (subClassId.equalsOrChildOf(cid))
					{
						availSub.remove();
						break;
					}
				}
			}
		}
		
		return availSubs;
	}
	
	/**
	 * Check new subclass classId for validity (villagemaster race/type, is not contains in previous subclasses, is contains in allowed subclasses) Base class not added into allowed subclasses.
	 * @param player
	 * @param classId
	 * @return
	 */
	public final static boolean isValidNewSubClass(L2PcInstance player, int classId)
	{
		if (!checkVillageMaster(classId))
		{
			return false;
		}
		
		final ClassId cid = ClassId.values()[classId];
		SubClass sub;
		ClassId subClassId;
		for (Iterator<SubClass> subList = iterSubClasses(player); subList.hasNext();)
		{
			sub = subList.next();
			subClassId = ClassId.values()[sub.getClassId()];
			
			if (subClassId.equalsOrChildOf(cid))
			{
				return false;
			}
		}
		
		// get player base class
		final int currentBaseId = player.getBaseClass();
		final ClassId baseCID = ClassId.getClassId(currentBaseId);
		
		// we need 2nd occupation ID
		final int baseClassId;
		if (baseCID.level() > 2)
		{
			baseClassId = baseCID.getParent().ordinal();
		}
		else
		{
			baseClassId = currentBaseId;
		}
		
		Set<PlayerClass> availSubs = PlayerClass.values()[baseClassId].getAvailableSubclasses(player);
		if ((availSubs == null) || availSubs.isEmpty())
		{
			return false;
		}
		
		boolean found = false;
		for (PlayerClass pclass : availSubs)
		{
			if (pclass.ordinal() == classId)
			{
				found = true;
				break;
			}
		}
		return found;
	}
	
	/**
	 * Returns true if this classId allowed for master
	 * @param classId
	 * @return
	 */
	public final static boolean checkVillageMaster(int classId)
	{
		return checkVillageMaster(PlayerClass.values()[classId]);
	}
	
	/**
	 * Returns true if this PlayerClass is allowed for master
	 * @param pclass
	 * @return
	 */
	public final static boolean checkVillageMaster(PlayerClass pclass)
	{
		return true;
	}
	
	public static final Iterator<SubClass> iterSubClasses(L2PcInstance player)
	{
		return player.getSubClasses().values().iterator();
	}
}