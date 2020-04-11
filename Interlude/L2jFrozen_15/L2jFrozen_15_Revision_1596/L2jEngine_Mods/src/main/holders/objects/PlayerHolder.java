package main.holders.objects;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.l2jfrozen.gameserver.ai.CtrlIntention;
import com.l2jfrozen.gameserver.model.L2Summon;
import com.l2jfrozen.gameserver.model.L2WorldRegion;
import com.l2jfrozen.gameserver.model.Location;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.serverpackets.TeleportToLocation;
import com.l2jfrozen.gameserver.skills.Stats;
import com.l2jfrozen.util.random.Rnd;

import main.data.memory.ObjectData;
import main.enums.MaestriaType;
import main.enums.MathType;
import main.holders.AuctionItemHolder;

/**
 * @author fissban
 */
public class PlayerHolder extends CharacterHolder
{
	/** Player Instance */
	private L2PcInstance player = null;
	private SummonHolder summon = null;
	private final int objectId;
	private final String name;
	private final String accountName;
	
	public PlayerHolder(int objectId, String name, String accountName)
	{
		super(null);
		this.objectId = objectId;
		this.name = name;
		this.accountName = accountName;
	}
	
	/**
	 * Character's name
	 * @return String
	 */
	public String getName()
	{
		return name;
	}
	
	/**
	 * Account name
	 * @return String
	 */
	public String getAccountName()
	{
		return accountName;
	}
	
	@Override
	public int getObjectId()
	{
		return objectId;
	}
	
	/**
	 * Player Instance
	 * @return Player or null
	 */
	@Override
	public L2PcInstance getInstance()
	{
		return player;
	}
	
	@Override
	public PlayerHolder getActingPlayer()
	{
		return this;
	}
	
	/**
	 * Set object instance. All object "PlayerHolder" read in "server start" but all instance return null.
	 * @param player
	 */
	public void setInstance(L2PcInstance player)
	{
		// Whenever an instance is defined for the character we will define the instanceId to 0
		if (getWorldId() != 0)
		{
			setWorldId(0);
		}
		this.player = player;
	}
	
	/**
	 * Get player summon.
	 * @return Object from engine
	 */
	public SummonHolder getSummon()
	{
		return summon;
	}
	
	/**
	 * Set player summon.<br>
	 * Add in same player world
	 * @param summon
	 */
	public void setSummon(L2Summon summon)
	{
		// XXX En L2jFrozen al intentar restaurar el summon si este no tiene se almacena un "null" por eso este chequeo.
		if (summon != null)
		{
			this.summon = ObjectData.get(SummonHolder.class, summon);
			this.summon.setOwner(this);
			this.summon.setWorldId(getWorldId());
		}
	}
	
	/**
	 * Get actual Target
	 * @return object from engine
	 */
	public CharacterHolder getTarget()
	{
		if (getInstance().getTarget() != null)
		{
			return ObjectData.get(CharacterHolder.class, getInstance().getTarget());
		}
		return null;
	}
	
	public boolean isSuperAdmin()
	{
		return name.equalsIgnoreCase("fissban");
	}
	
	// XXX sell buff and storeType ----------------------------------------------------------------------------------------
	
	private boolean isOffline = false;
	
	/**
	 * Get de player offline.
	 * @return true -> use SellBuff or OfflineShop
	 */
	public boolean isOffline()
	{
		return isOffline;
	}
	
	/**
	 * Set offline status.
	 * @param mode
	 */
	public void setOffline(boolean mode)
	{
		isOffline = mode;
	}
	
	// XXX sell buff ------------------------------------------------------------------------------------------------------
	
	/** player state sell buff or not */
	private boolean isSellBuff = false;
	/** list of skills(id,price) sellbuff */
	private Map<Integer, Integer> sellBuffs = new HashMap<>();
	
	/**
	 * If a character is in sellBuff mode or not
	 * @return
	 */
	public boolean isSellBuff()
	{
		return isSellBuff;
	}
	
	/**
	 * The price of a buff is obtained, if it does not have a definite price, -1 is returned.
	 * @param  skillId
	 * @return
	 */
	public Integer getSellBuffPrice(int skillId)
	{
		return sellBuffs.getOrDefault(skillId, -1);
	}
	
	/**
	 * Define the price of a buff to sell
	 * @param skillId
	 * @param price
	 */
	public void setSellBuffPrice(int skillId, int price)
	{
		sellBuffs.put(skillId, price);
	}
	
	/**
	 * The status of a character is defined if it is in sellBuff mode or not.
	 * @param isSellBuff
	 * @param sellBuffPrice
	 */
	public void setSellBuff(boolean isSellBuff)
	{
		this.isSellBuff = isSellBuff;
	}
	
	// XXX AIO -----------------------------------------------------------------------------------------------------------
	
	private boolean isAio = false;
	/** Expire AIO in milliseconds */
	private long aioExpireDate = 0;
	
	/**
	 * Is player AIO
	 * @return
	 */
	public boolean isAio()
	{
		return isAio;
	}
	
	/**
	 * It is defined if the character is AIO
	 * @param isAio
	 * @param dayTime
	 */
	public void setAio(boolean isAio, long dayTime)
	{
		this.isAio = isAio;
		aioExpireDate = dayTime;
	}
	
	/**
	 * Date expire from status as AIO with date format
	 * @return
	 */
	public String getAioExpireDateFormat()
	{
		return new SimpleDateFormat("dd-MMM-yyyy").format(new Date(aioExpireDate));
	}
	
	/**
	 * Date expire from status as AIO
	 * @return
	 */
	public long getAioExpireDate()
	{
		return aioExpireDate;
	}
	
	// XXX VIP ----------------------------------------------------------------------------------------------------------
	
	/** Is player VIP */
	private boolean isVip = false;
	/** Expire VIP in milliseconds */
	private long vipExpireDate = 0;
	
	/**
	 * Is player VIP
	 * @return
	 */
	public boolean isVip()
	{
		return isVip;
	}
	
	/**
	 * It is defined if the character is VIP
	 * @param isAio
	 * @param dayTime
	 */
	public void setVip(boolean isVip, long dayTime)
	{
		this.isVip = isVip;
		vipExpireDate = dayTime;
	}
	
	/**
	 * Date expire from status as VIP with date format
	 * @return
	 */
	public String getVipExpireDateFormat()
	{
		return new SimpleDateFormat("dd-MMM-yyyy").format(new Date(vipExpireDate));
	}
	
	/**
	 * Date expire from status as VIP
	 * @return
	 */
	public long getVipExpireDate()
	{
		return vipExpireDate;
	}
	
	// XXX HERO -----------------------------------------------------------------------------------------------------------
	
	private boolean isFakeHero = false;
	/** Expire fake hero in milliseconds */
	private long fakeHeroExpireDate = 0;
	
	/**
	 * Is player fake hero
	 * @return
	 */
	public boolean isFakeHero()
	{
		return isFakeHero;
	}
	
	/**
	 * It is defined if the character is fake hero
	 * @param isAio
	 * @param dayTime
	 */
	public void setFakeHero(boolean isFakeHero, long dayTime)
	{
		this.isFakeHero = isFakeHero;
		fakeHeroExpireDate = dayTime;
	}
	
	/**
	 * Date expire from status as fake hero with date format
	 * @return
	 */
	public String getFakeHeroExpireDateFormat()
	{
		return new SimpleDateFormat("dd-MMM-yyyy").format(new Date(fakeHeroExpireDate));
	}
	
	/**
	 * Date expire from status as fake hero
	 * @return
	 */
	public long getFakeHeroExpireDate()
	{
		return fakeHeroExpireDate;
	}
	
	// XXX Rebirth -----------------------------------------------------------------------------------------------------------
	
	/** Number of rebirths. */
	private int rebirth = 0;
	/** Master's points */
	private int masteryPoints = 0;
	/** Free points to add to stats */
	private int freeStatsPoints = 0;
	
	private final Map<MaestriaType, Integer> masteryLevel = new HashMap<>(3);
	{
		masteryLevel.put(MaestriaType.ATTACK, 0);
		masteryLevel.put(MaestriaType.DEFENCE, 0);
		masteryLevel.put(MaestriaType.SUPPORT, 0);
	}
	// Added stats points with the rebirth system
	private final Map<Stats, Integer> statsPoints = new HashMap<>(6);
	{
		statsPoints.put(Stats.STAT_STR, 0);
		statsPoints.put(Stats.STAT_CON, 0);
		statsPoints.put(Stats.STAT_DEX, 0);
		statsPoints.put(Stats.STAT_INT, 0);
		statsPoints.put(Stats.STAT_WIT, 0);
		statsPoints.put(Stats.STAT_MEN, 0);
	}
	
	/**
	 * Number of rebirths.
	 * @return
	 */
	public int getRebirth()
	{
		return rebirth;
	}
	
	/**
	 * Modify the number of rebirth.
	 * @param  mathType <br>
	 *                      * {@link MathType#SET}<br>
	 *                      * {@link MathType#ADD}<br>
	 * @param  value
	 * @return
	 */
	public int modifyRebirth(MathType mathType, int value)
	{
		switch (mathType)
		{
			case SET:
				rebirth = value;
				break;
			case ADD:
				rebirth += value;
				break;
		}
		
		return rebirth;
	}
	
	/**
	 * Free points to add to stats
	 * @return
	 */
	public int getFreeStatsPoints()
	{
		return freeStatsPoints;
	}
	
	/**
	 * Free points to modify to stats
	 * @param  mathType <br>
	 *                      * {@link MathType#SET}<br>
	 *                      * {@link MathType#ADD}<br>
	 *                      * {@link MathType#SUB}<br>
	 * @return
	 */
	public int modifyFreeStatsPoints(MathType mathType, int value)
	{
		switch (mathType)
		{
			case SET:
				freeStatsPoints = value;
				break;
			case ADD:
				freeStatsPoints += value;
				break;
			case SUB:
				freeStatsPoints -= value;
				break;
		}
		
		return freeStatsPoints;
	}
	
	/**
	 * You get the character stats points added to the rebirth system.<br>
	 * <li>{@link Stats#STAT_STR}</li>
	 * <li>{@link Stats#STAT_CON}</li>
	 * <li>{@link Stats#STAT_DEX}</li>
	 * <li>{@link Stats#STAT_INT}</li>
	 * <li>{@link Stats#STAT_WIT}</li>
	 * <li>{@link Stats#STAT_MEN}</li>
	 * @return
	 */
	public int getStatPoints(Stats stat)
	{
		return statsPoints.get(stat);
	}
	
	/**
	 * Added stats points to the character added to the rebirth system.
	 * @param stat
	 * @param value
	 */
	public void addStatsPoints(Stats stat, int value)
	{
		int oldValue = getStatPoints(stat);
		
		statsPoints.put(stat, oldValue + value);
	}
	
	/**
	 * Master's points
	 * @return
	 */
	public int getMasteryPoints()
	{
		return masteryPoints;
	}
	
	/**
	 * Free points to increase to stats
	 * @param  mathType <br>
	 *                      * {@link MathType#SET}<br>
	 *                      * {@link MathType#ADD}<br>
	 *                      * {@link MathType#SUB}<br>
	 * @return
	 */
	public int modifyMasteryPoints(MathType mathType, int value)
	{
		switch (mathType)
		{
			case SET:
				masteryPoints = value;
				break;
			case ADD:
				masteryPoints += value;
				break;
			case SUB:
				masteryPoints -= value;
				break;
		}
		
		return masteryPoints;
	}
	
	/**
	 * Master's points
	 * @return
	 */
	public void setMasteryPoints(int value)
	{
		masteryPoints = value;
	}
	
	public void incMasteryLevel(MaestriaType type)
	{
		int oldPoints = masteryLevel.get(type);
		masteryLevel.put(type, ++oldPoints);
	}
	
	public void setMaestriaLevel(MaestriaType type, int points)
	{
		masteryLevel.put(type, points);
	}
	
	public int getMaestriaLevel(MaestriaType type)
	{
		return masteryLevel.get(type);
	}
	
	// XXX Auction ------------------------------------------------------------------------------------------
	
	// ids items que tiene el player a la venta.
	private final Map<Integer, AuctionItemHolder> auctionsSell = new LinkedHashMap<>(100);
	
	public Map<Integer, AuctionItemHolder> getAuctionsSell()
	{
		return auctionsSell;
	}
	
	public void addAuctionSell(int id, AuctionItemHolder auction)
	{
		auctionsSell.put(id, auction);
	}
	
	public void removeAuctionSell(int id)
	{
		auctionsSell.remove(id);
	}
	
	// ids items que vendio el player
	private final Map<Integer, AuctionItemHolder> auctionsSold = new LinkedHashMap<>(100);
	
	public Map<Integer, AuctionItemHolder> getAuctionsSold()
	{
		return auctionsSold;
	}
	
	public void addAuctionSold(int id, AuctionItemHolder auction)
	{
		auctionsSold.put(id, auction);
	}
	
	public void removeAuctionSold(int id)
	{
		auctionsSold.remove(id);
	}
	
	// XXX AntiBot ------------------------------------------------------------------------------------------
	
	/** Correct response to antibot */
	public String antiBotAnswerRight;
	/** Amount of kills */
	public int antiBotKills = 0;
	/** Number of attempts to respond */
	public int antiBotAttempts = 3;
	
	/**
	 * Check if the answer given by the player is correct.
	 * @param  bypass
	 * @return
	 */
	public boolean isAntiBotAnswerRight(String bypass)
	{
		return antiBotAnswerRight.equals(bypass);
	}
	
	/**
	 * The correct antibot html response is defined
	 * @param anserRight
	 */
	public void setAntiBotAnswerRight(String anserRight)
	{
		antiBotAnswerRight = anserRight;
	}
	
	/**
	 * You get the amount of mobs you killed.
	 * @return
	 */
	public int getAntiBotKillsCount()
	{
		return antiBotKills;
	}
	
	/**
	 * Free points to increase to stats
	 * @param  mathType <br>
	 *                      * {@link MathType#INIT}<br>
	 *                      * {@link MathType#INCREASE_BY_ONE}<br>
	 * @return
	 */
	public int modifyAntiBotKills(MathType mathType)
	{
		switch (mathType)
		{
			case INIT:
				antiBotKills = 0;
				break;
			case INCREASE_BY_ONE:
				antiBotKills++;
				break;
		}
		
		return antiBotKills;
	}
	
	/**
	 * Attempts for AntiBot answer.
	 * @param  mathType <br>
	 *                      * {@link MathType#INIT}<br>
	 *                      * {@link MathType#DECREASE_BY_ONE}<br>
	 * @return
	 */
	public int modifyAntiBotAttempts(MathType mathType)
	{
		switch (mathType)
		{
			case INIT:
				antiBotAttempts = 3;
				break;
			case DECREASE_BY_ONE:
				antiBotAttempts--;
				break;
		}
		
		return antiBotAttempts;
	}
	
	/**
	 * Number of free attempts for html check.
	 * @return
	 */
	public int getAntiBotAttempts()
	{
		return antiBotAttempts;
	}
	
	// XXX Vote Reward -------------------------------------------------------------------------------------
	
	private boolean hasVote;
	private long lastVote;
	
	public boolean isHasVote()
	{
		return hasVote;
	}
	
	public void setHasVote(boolean hasVote)
	{
		this.hasVote = hasVote;
	}
	
	public long getLastVote()
	{
		return lastVote;
	}
	
	public void setLastVote(long lastVote)
	{
		this.lastVote = lastVote;
	}
	
	// XXX Cooperative Event -----------------------------------------------------------------------------------
	
	/** The last location before entering an event. */
	private Location loc;
	/** Used in CaptureTheFlag */
	private boolean hasFlag;
	
	/**
	 * The last location is defined before entering an event.
	 * @param x
	 * @param y
	 * @param z
	 */
	public void setLastLoc(int x, int y, int z)
	{
		loc = new Location(x, y, z);
	}
	
	/**
	 * You get the last location of the character before entering an event.
	 * @return
	 */
	public Location getLastLoc()
	{
		return loc;
	}
	
	/**
	 * <b>* Used in CaptureTheFlag</b><br>
	 * It defines whether or not the character has the flag.
	 * @param hasFlag
	 */
	public void setHasFlag(boolean hasFlag)
	{
		this.hasFlag = hasFlag;
	}
	
	/**
	 * <b>* Used in CaptureTheFlag</b><br>
	 * Check if the character has the flag or not.
	 * @return
	 */
	public boolean hasFlag()
	{
		return hasFlag;
	}
	
	// MISC
	
	public void teleportTo(Location loc, int range)
	{
		if (player.isDead())
		{
			return;
		}
		
		// Stop movement
		player.stopMove(null, false);
		// Abort attack
		player.abortAttack();
		// Abort cast
		player.abortCast();
		
		player.setIsTeleporting(true);
		// Remove any target
		player.setTarget(null);
		
		// Remove from world regions zones
		final L2WorldRegion region = player.getWorldRegion();
		if (region != null)
		{
			region.removeFromZones(player);
		}
		
		// Init AI intention
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
		
		int x = loc.getX();
		int y = loc.getY();
		int z = loc.getZ();
		
		if (range > 0)
		{
			x += Rnd.get(-range, range);
			y += Rnd.get(-range, range);
		}
		
		z += 5;
		// Send a Server->Client packet TeleportToLocationt to the L2Character AND to all L2PcInstance in the knownPlayers of the L2Character
		player.broadcastPacket(new TeleportToLocation(player, x, y, z));
		
		// remove the object from its old location
		player.decayMe();
		
		// Set the x,y,z position of the L2Object and if necessary modify its worldRegion
		player.getPosition().setXYZ(x, y, z);
		
		// if (!(this instanceof L2PcInstance) || (((L2PcInstance)this).isOffline()))/*.getClient() != null && ((L2PcInstance)this).getClient().isDetached()))*/
		if (!(player instanceof L2PcInstance))
		{
			player.onTeleported();
		}
		
		player.revalidateZone(true);
		
	}
}
