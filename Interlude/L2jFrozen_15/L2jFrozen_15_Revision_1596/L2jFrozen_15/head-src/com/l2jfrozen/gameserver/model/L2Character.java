package com.l2jfrozen.gameserver.model;

import static com.l2jfrozen.gameserver.ai.CtrlIntention.AI_INTENTION_ATTACK;
import static com.l2jfrozen.gameserver.ai.CtrlIntention.AI_INTENTION_FOLLOW;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.ai.CtrlEvent;
import com.l2jfrozen.gameserver.ai.CtrlIntention;
import com.l2jfrozen.gameserver.ai.L2AttackableAI;
import com.l2jfrozen.gameserver.ai.L2CharacterAI;
import com.l2jfrozen.gameserver.controllers.GameTimeController;
import com.l2jfrozen.gameserver.datatables.HeroSkillTable;
import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.datatables.csv.DoorTable;
import com.l2jfrozen.gameserver.datatables.csv.MapRegionTable;
import com.l2jfrozen.gameserver.datatables.csv.MapRegionTable.TeleportWhereType;
import com.l2jfrozen.gameserver.datatables.sql.NpcTable;
import com.l2jfrozen.gameserver.geo.GeoData;
import com.l2jfrozen.gameserver.geo.pathfinding.Node;
import com.l2jfrozen.gameserver.geo.pathfinding.PathFinding;
import com.l2jfrozen.gameserver.handler.ISkillHandler;
import com.l2jfrozen.gameserver.handler.SkillHandler;
import com.l2jfrozen.gameserver.handler.itemhandlers.Potions;
import com.l2jfrozen.gameserver.managers.DimensionalRiftManager;
import com.l2jfrozen.gameserver.managers.DuelManager;
import com.l2jfrozen.gameserver.managers.GrandBossManager;
import com.l2jfrozen.gameserver.managers.RaidBossSpawnManager;
import com.l2jfrozen.gameserver.model.L2Skill.SkillTargetType;
import com.l2jfrozen.gameserver.model.L2Skill.SkillType;
import com.l2jfrozen.gameserver.model.actor.instance.L2BoatInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2ControlTowerInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2EffectPointInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2GrandBossInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2GuardInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2MinionInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2MonsterInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2NpcWalkerInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance.SkillDat;
import com.l2jfrozen.gameserver.model.actor.instance.L2PetInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2RaidBossInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2RiftInvaderInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2SiegeFlagInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2SummonInstance;
import com.l2jfrozen.gameserver.model.actor.knownlist.CharKnownList;
import com.l2jfrozen.gameserver.model.actor.knownlist.ObjectKnownList.KnownListAsynchronousUpdateTask;
import com.l2jfrozen.gameserver.model.actor.position.L2CharPosition;
import com.l2jfrozen.gameserver.model.actor.position.ObjectPosition;
import com.l2jfrozen.gameserver.model.actor.stat.CharStat;
import com.l2jfrozen.gameserver.model.actor.status.CharStatus;
import com.l2jfrozen.gameserver.model.entity.Duel;
import com.l2jfrozen.gameserver.model.entity.event.CTF;
import com.l2jfrozen.gameserver.model.entity.event.DM;
import com.l2jfrozen.gameserver.model.entity.event.L2Event;
import com.l2jfrozen.gameserver.model.entity.event.TvT;
import com.l2jfrozen.gameserver.model.entity.olympiad.Olympiad;
import com.l2jfrozen.gameserver.model.extender.BaseExtender.EventType;
import com.l2jfrozen.gameserver.model.quest.Quest;
import com.l2jfrozen.gameserver.model.quest.QuestState;
import com.l2jfrozen.gameserver.model.zone.type.L2BossZone;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.Attack;
import com.l2jfrozen.gameserver.network.serverpackets.BeginRotation;
import com.l2jfrozen.gameserver.network.serverpackets.ChangeMoveType;
import com.l2jfrozen.gameserver.network.serverpackets.ChangeWaitType;
import com.l2jfrozen.gameserver.network.serverpackets.CharInfo;
import com.l2jfrozen.gameserver.network.serverpackets.CharMoveToLocation;
import com.l2jfrozen.gameserver.network.serverpackets.ExOlympiadSpelledInfo;
import com.l2jfrozen.gameserver.network.serverpackets.L2GameServerPacket;
import com.l2jfrozen.gameserver.network.serverpackets.MagicEffectIcons;
import com.l2jfrozen.gameserver.network.serverpackets.MagicSkillCanceld;
import com.l2jfrozen.gameserver.network.serverpackets.MagicSkillLaunched;
import com.l2jfrozen.gameserver.network.serverpackets.MagicSkillUser;
import com.l2jfrozen.gameserver.network.serverpackets.MyTargetSelected;
import com.l2jfrozen.gameserver.network.serverpackets.NpcInfo;
import com.l2jfrozen.gameserver.network.serverpackets.PartySpelled;
import com.l2jfrozen.gameserver.network.serverpackets.PetInfo;
import com.l2jfrozen.gameserver.network.serverpackets.RelationChanged;
import com.l2jfrozen.gameserver.network.serverpackets.Revive;
import com.l2jfrozen.gameserver.network.serverpackets.SetupGauge;
import com.l2jfrozen.gameserver.network.serverpackets.StatusUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.StopMove;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.network.serverpackets.TargetUnselected;
import com.l2jfrozen.gameserver.network.serverpackets.TeleportToLocation;
import com.l2jfrozen.gameserver.network.serverpackets.ValidateLocation;
import com.l2jfrozen.gameserver.network.serverpackets.ValidateLocationInVehicle;
import com.l2jfrozen.gameserver.skills.Calculator;
import com.l2jfrozen.gameserver.skills.Formulas;
import com.l2jfrozen.gameserver.skills.Stats;
import com.l2jfrozen.gameserver.skills.effects.EffectCharge;
import com.l2jfrozen.gameserver.skills.funcs.Func;
import com.l2jfrozen.gameserver.skills.holders.ISkillsHolder;
import com.l2jfrozen.gameserver.templates.L2CharTemplate;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;
import com.l2jfrozen.gameserver.templates.L2Weapon;
import com.l2jfrozen.gameserver.templates.L2WeaponType;
import com.l2jfrozen.gameserver.templates.StatsSet;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;
import com.l2jfrozen.gameserver.util.Util;
import com.l2jfrozen.util.Point3D;
import com.l2jfrozen.util.random.Rnd;

import javolution.util.FastTable;
import main.EngineModsManager;

/**
 * Mother class of all character objects of the world (PC, NPC...)<BR>
 * <BR>
 * L2Character :<BR>
 * <BR>
 * <li>L2CastleGuardInstance</li>
 * <li>L2DoorInstance</li>
 * <li>L2NpcInstance</li>
 * <li>L2PlayableInstance</li><BR>
 * <BR>
 * <B><U> Concept of L2CharTemplate</U> :</B><BR>
 * <BR>
 * Each L2Character owns generic and static properties (ex : all Keltir have the same number of HP...). All of those properties are stored in a different template for each type of L2Character. Each template is loaded once in the server cache memory (reduce memory use). When a new instance of
 * L2Character is spawned, server just create a link between the instance and the template. This link is stored in <B>_template</B><BR>
 * <BR>
 * @version $Revision: 1.5.5 $ $Date: 2009/05/12 19:45:27 $
 * @authors eX1steam, programmos, L2Scoria dev&sword dev
 */
public abstract class L2Character extends L2Object implements ISkillsHolder
{
	public static final Logger LOGGER = Logger.getLogger(L2Character.class);
	
	private long attackStance;
	private List<L2Character> attackByList;
	// private L2Character attackingChar;
	private L2Skill lastSkillCast;
	private L2Skill lastPotionCast;
	private boolean isBuffProtected = false; // Protect From Debuffs
	private boolean isAfraid = false; // Flee in a random direction
	private boolean isConfused = false; // Attack anyone randomly
	private boolean isFakeDeath = false; // Fake death
	private boolean isFlying = false; // Is flying Wyvern?
	private boolean isFallsdown = false; // Falls down
	private boolean isMuted = false; // Cannot use magic
	private boolean isPsychicalMuted = false; // Cannot use psychical skills
	private boolean isKilledAlready = false;
	private int isImobilised = 0;
	private boolean isOverloaded = false; // the char is carrying too much
	private boolean isParalyzed = false;
	private boolean isRiding = false; // Is Riding strider?
	private boolean isPendingRevive = false;
	private boolean isRooted = false; // Cannot move until root timed out
	private boolean isRunning = false;
	private boolean isImmobileUntilAttacked = false; // Is in immobile until attacked.
	private boolean isSleeping = false; // Cannot move/attack until sleep timed out or monster is attacked
	private boolean isStunned = false; // Cannot move/attack until stun timed out
	private boolean isBetrayed = false; // Betrayed by own summon
	private boolean isBlockBuff = false; // Got blocked buff bar
	private boolean isBlockDebuff = false; // Got blocked debuff bar
	protected boolean isTeleporting = false;
	protected boolean isInvul = false;
	protected boolean isUnkillable = false;
	protected boolean isAttackDisabled = false;
	private int lastHealAmount = 0;
	private CharStat stat;
	private CharStatus status;
	private L2CharTemplate templateCharacter; // The link on the L2CharTemplate object containing generic and static properties of this L2Character type (ex : Max HP, Speed...)
	private String title;
	private String aiClass = "default";
	private double hpUpdateIncCheck = .0;
	private double hpUpdateDecCheck = .0;
	private double hpUpdateInterval = .0;
	private boolean champion = false;
	private Calculator[] calculators;
	protected final Map<Integer, L2Skill> skills;
	protected final Map<Integer, L2Skill> triggeredSkills;
	protected ChanceSkillList chanceSkills;
	protected ForceBuff forceBuff;
	private boolean blocked;
	private boolean meditated;
	
	/**
	 * Zone system<br>
	 * x^2 or x*x.
	 */
	public static final int ZONE_PVP = 1;
	
	/** The Constant ZONE_PEACE. */
	public static final int ZONE_PEACE = 2;
	
	/** The Constant ZONE_SIEGE. */
	public static final int ZONE_SIEGE = 4;
	
	/** The Constant ZONE_MOTHERTREE. */
	public static final int ZONE_MOTHERTREE = 8;
	
	/** The Constant ZONE_CLANHALL. */
	public static final int ZONE_CLANHALL = 16;
	
	/** The Constant ZONE_UNUSED. */
	public static final int ZONE_UNUSED = 32;
	
	/** The Constant ZONE_NOLANDING. */
	public static final int ZONE_NOLANDING = 64;
	
	/** The Constant ZONE_WATER. */
	public static final int ZONE_WATER = 128;
	
	/** The Constant ZONE_JAIL. */
	public static final int ZONE_JAIL = 256;
	
	/** The Constant ZONE_MONSTERTRACK. */
	public static final int ZONE_MONSTERTRACK = 512;
	
	/** The Constant ZONE_SWAMP. */
	public static final int ZONE_SWAMP = 1024;
	
	/** The Constant ZONE_NOSUMMONFRIEND. */
	public static final int ZONE_NOSUMMONFRIEND = 2048;
	
	/** The Constant ZONE_OLY. */
	public static final int ZONE_OLY = 4096;
	
	/** The Constant ZONE_NOHQ. */
	public static final int ZONE_NOHQ = 8192;
	
	/** The Constant ZONE_DANGERAREA. */
	public static final int ZONE_DANGERAREA = 16384;
	
	/** The Constant ZONE_NOSTORE. */
	public static final int ZONE_NOSTORE = 32768;
	
	private int currentZones = 0;
	private boolean advanceFlag = false;
	private int advanceMultiplier = 1;
	
	/**
	 * Checks if is inside zone.
	 * @param  zone the zone
	 * @return      true, if is inside zone
	 */
	public boolean isInsideZone(final int zone)
	{
		return (currentZones & zone) != 0;
	}
	
	/**
	 * Sets the inside zone.
	 * @param zone  the zone
	 * @param state the state
	 */
	public void setInsideZone(final int zone, final boolean state)
	{
		if (state)
		{
			currentZones |= zone;
		}
		else if (isInsideZone(zone))
		{
			currentZones ^= zone;
		}
	}
	
	/**
	 * This will return true if the player is GM,<br>
	 * but if the player is not GM it will return false.
	 * @return GM status
	 */
	public boolean charIsGM()
	{
		if (this instanceof L2PcInstance)
		{
			if (((L2PcInstance) this).isGM())
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Constructor of L2Character.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * Each L2Character owns generic and static properties (ex : all Keltir have the same number of HP...). All of those properties are stored in a different template for each type of L2Character. Each template is loaded once in the server cache memory (reduce memory use). When a new instance of
	 * L2Character is spawned, server just create a link between the instance and the template This link is stored in <B>_template</B><BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Set the template of the L2Character</li>
	 * <li>Set overloaded to false (the charcater can take more items)</li> <BR>
	 * <BR>
	 * <li>If L2Character is a L2NPCInstance, copy skills from template to object</li>
	 * <li>If L2Character is a L2NPCInstance, link calculators to NPC_STD_CALCULATOR</li><BR>
	 * <BR>
	 * <li>If L2Character is NOT a L2NPCInstance, create an empty skills slot</li>
	 * <li>If L2Character is a L2PcInstance or L2Summon, copy basic Calculator set to object</li><BR>
	 * <BR>
	 * @param objectId Identifier of the object to initialized
	 * @param template The L2CharTemplate to apply to the object
	 */
	// MAKE TO US From DREAM
	// int attackcountmax = (int) Math.round(calcStat(Stats.POLE_TARGERT_COUNT, 3, null, null));
	public L2Character(final int objectId, final L2CharTemplate template)
	{
		super(objectId);
		getKnownList();
		
		// Set its template to the new L2Character
		templateCharacter = template;
		
		triggeredSkills = new HashMap<>();
		
		if (template != null && this instanceof L2NpcInstance)
		{
			// Copy the Standard Calcultors of the L2NPCInstance in calculators
			calculators = NPC_STD_CALCULATOR;
			
			// Copy the skills of the L2NPCInstance from its template to the L2Character Instance
			// The skills list can be affected by spell effects so it's necessary to make a copy
			// to avoid that a spell affecting a L2NPCInstance, affects others L2NPCInstance of the same type too.
			skills = ((L2NpcTemplate) template).getSkills();
			
			for (final Map.Entry<Integer, L2Skill> skill : skills.entrySet())
			{
				addStatFuncs(skill.getValue().getStatFuncs(null, this));
			}
			
			if (!Config.NPC_ATTACKABLE || !(this instanceof L2Attackable) && !(this instanceof L2ControlTowerInstance) && !(this instanceof L2SiegeFlagInstance) && !(this instanceof L2EffectPointInstance))
			{
				setIsInvul(true);
			}
		}
		else
		// not L2NpcInstance
		{
			// Initialize the ConcurrentHashMap skills to null
			skills = new ConcurrentHashMap<>();
			
			// If L2Character is a L2PcInstance or a L2Summon, create the basic calculator set
			calculators = new Calculator[Stats.NUM_STATS];
			Formulas.getInstance().addFuncsToNewCharacter(this);
			
			if (!(this instanceof L2Attackable) && !isAttackable() && !(this instanceof L2DoorInstance))
			{
				setIsInvul(true);
			}
		}
		
		/*
		 * if(!(this instanceof L2PcInstance) && !(this instanceof L2MonsterInstance) && !(this instanceof L2GuardInstance) && !(this instanceof L2SiegeGuardInstance) && !(this instanceof L2ControlTowerInstance) && !(this instanceof L2DoorInstance) && !(this instanceof L2FriendlyMobInstance) && !(this
		 * instanceof L2SiegeSummonInstance) && !(this instanceof L2PetInstance) && !(this instanceof L2SummonInstance) && !(this instanceof L2SiegeFlagInstance) && !(this instanceof L2EffectPointInstance) && !(this instanceof L2CommanderInstance) && !(this instanceof L2FortSiegeGuardInstance)) {
		 * ///////////////////////////////////////////////////////////////////////////////////////////// setIsInvul(true); }
		 */
	}
	
	/**
	 * Inits the char status update values.
	 */
	protected void initCharStatusUpdateValues()
	{
		hpUpdateInterval = getMaxHp() / 352.0; // MAX_HP div MAX_HP_BAR_PX
		hpUpdateIncCheck = getMaxHp();
		hpUpdateDecCheck = getMaxHp() - hpUpdateInterval;
	}
	
	/**
	 * Remove the L2Character from the world when the decay task is launched.<BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T REMOVE the object from allObjects of L2World </B></FONT><BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND Server->Client packets to players</B></FONT><BR>
	 * <BR>
	 */
	public void onDecay()
	{
		L2WorldRegion reg = getWorldRegion();
		
		if (reg != null)
		{
			reg.removeFromZones(this);
		}
		
		decayMe();
		
		reg = null;
	}
	
	@Override
	public void onSpawn()
	{
		super.onSpawn();
		revalidateZone();
	}
	
	public void onTeleported()
	{
		if (!isTeleporting())
		{
			return;
		}
		
		final ObjectPosition pos = getPosition();
		
		if (pos != null)
		{
			spawnMe(getPosition().getX(), getPosition().getY(), getPosition().getZ());
		}
		
		setIsTeleporting(false);
		
		if (isPendingRevive)
		{
			doRevive();
		}
		
		final L2Summon pet = getPet();
		
		// Modify the position of the pet if necessary
		if (pet != null && pos != null)
		{
			pet.setFollowStatus(false);
			pet.teleToLocation(pos.getX() + Rnd.get(-100, 100), pos.getY() + Rnd.get(-100, 100), pos.getZ(), false);
			pet.setFollowStatus(true);
		}
		
	}
	
	/**
	 * Add L2Character instance that is attacking to the attacker list.<BR>
	 * <BR>
	 * @param player The L2Character that attcks this one
	 */
	public void addAttackerToAttackByList(final L2Character player)
	{
		if (player == null || player == this || getAttackByList() == null || getAttackByList().contains(player))
		{
			return;
		}
		
		getAttackByList().add(player);
	}
	
	/**
	 * Send a packet to the L2Character AND to all L2PcInstance in the knownPlayers of the L2Character.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * L2PcInstance in the detection area of the L2Character are identified in <B>_knownPlayers</B>. In order to inform other players of state modification on the L2Character, server just need to go through knownPlayers to send Server->Client Packet<BR>
	 * <BR>
	 */
	
	protected byte startingRotationCounter = 4;
	
	/**
	 * Checks if is starting rotation allowed.
	 * @return true, if is starting rotation allowed
	 */
	public synchronized boolean isStartingRotationAllowed()
	{
		// This function is called too often from movement arrow
		startingRotationCounter--;
		if (startingRotationCounter < 0)
		{
			startingRotationCounter = 4;
		}
		
		if (startingRotationCounter == 4)
		{
			return true;
		}
		return false;
	}
	
	/**
	 * Broadcast packet.
	 * @param mov the mov
	 */
	public final void broadcastPacket(final L2GameServerPacket mov)
	{
		if (!(mov instanceof CharInfo))
		{
			sendPacket(mov);
		}
		
		// don't broadcast anytime the rotating packet
		if (mov instanceof BeginRotation && !isStartingRotationAllowed())
		{
			return;
		}
		
		// if (Config.DEBUG) LOGGER.fine("players to notify:" + knownPlayers.size() + " packet:"+mov.getType());
		
		for (final L2PcInstance player : getKnownList().getKnownPlayers().values())
		{
			if (player != null)
			{
				/*
				 * TEMP FIX: If player is not visible don't send packets broadcast to all his KnowList. This will avoid GM detection with l2net and olympiad's crash. We can now find old problems with invisible mode.
				 */
				if (this instanceof L2PcInstance && !player.isGM() && (((L2PcInstance) this).getAppearance().isInvisible() || ((L2PcInstance) this).inObserverMode()))
				{
					return;
				}
				
				try
				{
					player.sendPacket(mov);
					
					if (mov instanceof CharInfo && this instanceof L2PcInstance)
					{
						final int relation = ((L2PcInstance) this).getRelation(player);
						if (getKnownList().getKnownRelations().get(player.getObjectId()) != null && getKnownList().getKnownRelations().get(player.getObjectId()) != relation)
						{
							player.sendPacket(new RelationChanged((L2PcInstance) this, relation, player.isAutoAttackable(this)));
						}
					}
					// if(Config.DEVELOPER && !isInsideRadius(player, 3500, false, false)) LOGGER.warn("broadcastPacket: Too far player see event!");
				}
				catch (final NullPointerException e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Send a packet to the L2Character AND to all L2PcInstance in the radius (max knownlist radius) from the L2Character.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * L2PcInstance in the detection area of the L2Character are identified in <B>_knownPlayers</B>. In order to inform other players of state modification on the L2Character, server just need to go through knownPlayers to send Server->Client Packet<BR>
	 * <BR>
	 * @param mov               the mov
	 * @param radiusInKnownlist the radius in knownlist
	 */
	public final void broadcastPacket(final L2GameServerPacket mov, final int radiusInKnownlist)
	{
		if (!(mov instanceof CharInfo))
		{
			sendPacket(mov);
		}
		
		// if (Config.DEBUG) LOGGER.fine("players to notify:" + knownPlayers.size() + " packet:"+mov.getType());
		
		for (final L2PcInstance player : getKnownList().getKnownPlayers().values())
		{
			try
			{
				if (!isInsideRadius(player, radiusInKnownlist, false, false))
				{
					continue;
				}
				
				player.sendPacket(mov);
				
				if (mov instanceof CharInfo && this instanceof L2PcInstance)
				{
					final int relation = ((L2PcInstance) this).getRelation(player);
					if (getKnownList().getKnownRelations().get(player.getObjectId()) != null && getKnownList().getKnownRelations().get(player.getObjectId()) != relation)
					{
						player.sendPacket(new RelationChanged((L2PcInstance) this, relation, player.isAutoAttackable(this)));
					}
				}
			}
			catch (final NullPointerException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Need hp update.
	 * @param  barPixels the bar pixels
	 * @return           true if hp update should be done, false if not
	 */
	protected boolean needHpUpdate(final int barPixels)
	{
		final double currentHp = getCurrentHp();
		
		if (currentHp <= 1.0 || getMaxHp() < barPixels)
		{
			return true;
		}
		
		if (currentHp <= hpUpdateDecCheck || currentHp >= hpUpdateIncCheck)
		{
			if (currentHp == getMaxHp())
			{
				hpUpdateIncCheck = currentHp + 1;
				hpUpdateDecCheck = currentHp - hpUpdateInterval;
			}
			else
			{
				final double doubleMulti = currentHp / hpUpdateInterval;
				int intMulti = (int) doubleMulti;
				
				hpUpdateDecCheck = hpUpdateInterval * (doubleMulti < intMulti ? intMulti-- : intMulti);
				hpUpdateIncCheck = hpUpdateDecCheck + hpUpdateInterval;
			}
			return true;
		}
		
		return false;
	}
	
	/**
	 * Send the Server->Client packet StatusUpdate with current HP and MP to all other L2PcInstance to inform.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Create the Server->Client packet StatusUpdate with current HP and MP</li>
	 * <li>Send the Server->Client packet StatusUpdate with current HP and MP to all L2Character called statusListener that must be informed of HP/MP updates of this L2Character</li><BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND CP information</B></FONT><BR>
	 * <BR>
	 * <B><U> Overriden in </U> :</B><BR>
	 * <BR>
	 * <li>L2PcInstance : Send current HP,MP and CP to the L2PcInstance and only current HP, MP and Level to all other L2PcInstance of the Party</li><BR>
	 * <BR>
	 */
	public void broadcastStatusUpdate()
	{
		if (getStatus().getStatusListener().isEmpty())
		{
			return;
		}
		
		if (!needHpUpdate(352))
		{
			return;
		}
		
		if (Config.DEBUG)
		{
			LOGGER.debug("Broadcast Status Update for " + getObjectId() + "(" + getName() + "). HP: " + getCurrentHp());
		}
		
		// Create the Server->Client packet StatusUpdate with current HP and MP
		StatusUpdate su = null;
		if (Config.FORCE_COMPLETE_STATUS_UPDATE && this instanceof L2PcInstance)
		{
			su = new StatusUpdate((L2PcInstance) this);
		}
		else
		{
			su = new StatusUpdate(getObjectId());
			su.addAttribute(StatusUpdate.CUR_HP, (int) getCurrentHp());
			su.addAttribute(StatusUpdate.CUR_MP, (int) getCurrentMp());
		}
		
		// Go through the StatusListener
		// Send the Server->Client packet StatusUpdate with current HP and MP
		for (final L2Character temp : getStatus().getStatusListener())
		{
			if (temp != null)
			{
				temp.sendPacket(su);
			}
		}
	}
	
	/**
	 * Not Implemented.<BR>
	 * <BR>
	 * <B><U> Overridden in </U> :</B><BR>
	 * <BR>
	 * <li>L2PcInstance</li><BR>
	 * <BR>
	 * @param mov the mov
	 */
	public void sendPacket(final L2GameServerPacket mov)
	{
		// default implementation
	}
	
	private boolean inTownWar = false;
	
	/**
	 * Checks if is in town war.
	 * @return true, if is in town war
	 */
	public final boolean isinTownWar()
	{
		return inTownWar;
	}
	
	/**
	 * Sets the in town war.
	 * @param value the new in town war
	 */
	public final void setInTownWar(final boolean value)
	{
		inTownWar = value;
	}
	
	/**
	 * Teleport a L2Character and its pet if necessary.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Stop the movement of the L2Character</li>
	 * <li>Set the x,y,z position of the L2Object and if necessary modify its worldRegion</li>
	 * <li>Send a Server->Client packet TeleportToLocationt to the L2Character AND to all L2PcInstance in its knownPlayers</li>
	 * <li>Modify the position of the pet if necessary</li><BR>
	 * <BR>
	 * @param x                 the x
	 * @param y                 the y
	 * @param z                 the z
	 * @param allowRandomOffset the allow random offset
	 */
	public void teleToLocation(int x, int y, int z, final boolean allowRandomOffset)
	{
		if (Config.TW_DISABLE_GK && isinTownWar() && !isDead())
		{
			if (isPlayer())
			{
				L2PcInstance player = (L2PcInstance) this;
				{
					if (!player.isGM())
					{
						player.sendMessage("Teleport disabled during Town War");
						player.sendPacket(ActionFailed.STATIC_PACKET);
						return;
					}
				}
			}
		}
		
		// Stop movement
		stopMove(null, false);
		abortAttack();
		abortCast();
		
		setIsTeleporting(true);
		setTarget(null);
		
		// Remove from world regions zones
		final L2WorldRegion region = getWorldRegion();
		if (region != null)
		{
			region.removeFromZones(this);
		}
		
		getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
		
		if (Config.RESPAWN_RANDOM_ENABLED && allowRandomOffset)
		{
			x += Rnd.get(-Config.RESPAWN_RANDOM_MAX_OFFSET, Config.RESPAWN_RANDOM_MAX_OFFSET);
			y += Rnd.get(-Config.RESPAWN_RANDOM_MAX_OFFSET, Config.RESPAWN_RANDOM_MAX_OFFSET);
		}
		
		z += 5;
		
		if (Config.DEBUG)
		{
			LOGGER.debug("Teleporting to: " + x + ", " + y + ", " + z);
		}
		
		// Send a Server->Client packet TeleportToLocationt to the L2Character AND to all L2PcInstance in the knownPlayers of the L2Character
		broadcastPacket(new TeleportToLocation(this, x, y, z));
		
		// remove the object from its old location
		decayMe();
		
		// Set the x,y,z position of the L2Object and if necessary modify its worldRegion
		getPosition().setXYZ(x, y, z);
		
		// if (!(this instanceof L2PcInstance) || (((L2PcInstance)this).isOffline()))/*.getClient() != null && ((L2PcInstance)this).getClient().isDetached()))*/
		if (!(this instanceof L2PcInstance))
		{
			onTeleported();
		}
		
		revalidateZone(true);
	}
	
	protected byte zoneValidateCounter = 4;
	
	/**
	 * Revalidate zone.
	 * @param force the force
	 */
	public void revalidateZone(final boolean force)
	{
		final L2WorldRegion region = getWorldRegion();
		if (region == null)
		{
			return;
		}
		
		// This function is called too often from movement code
		if (force)
		{
			zoneValidateCounter = 4;
		}
		else
		{
			zoneValidateCounter--;
			if (zoneValidateCounter < 0)
			{
				zoneValidateCounter = 4;
			}
			else
			{
				return;
			}
		}
		region.revalidateZones(this);
	}
	
	/**
	 * Tele to location.
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 */
	public void teleToLocation(final int x, final int y, final int z)
	{
		teleToLocation(x, y, z, false);
	}
	
	/**
	 * Tele to location.
	 * @param loc               the loc
	 * @param allowRandomOffset the allow random offset
	 */
	public void teleToLocation(final Location loc, final boolean allowRandomOffset)
	{
		int x = loc.getX();
		int y = loc.getY();
		int z = loc.getZ();
		
		if (this instanceof L2PcInstance && DimensionalRiftManager.getInstance().checkIfInRiftZone(getX(), getY(), getZ(), true))
		{ // true -> ignore waiting room :)
			L2PcInstance player = (L2PcInstance) this;
			player.sendMessage("You have been sent to the waiting room.");
			
			if (player.isInParty() && player.getParty().isInDimensionalRift())
			{
				player.getParty().getDimensionalRift().usedTeleport(player);
			}
			
			final int[] newCoords = DimensionalRiftManager.getInstance().getRoom((byte) 0, (byte) 0).getTeleportCoords();
			
			x = newCoords[0];
			y = newCoords[1];
			z = newCoords[2];
			
			player = null;
		}
		teleToLocation(x, y, z, allowRandomOffset);
	}
	
	/**
	 * Tele to location.
	 * @param teleportWhere the teleport where
	 */
	public void teleToLocation(final TeleportWhereType teleportWhere)
	{
		teleToLocation(MapRegionTable.getInstance().getTeleToLocation(this, teleportWhere), true);
	}
	
	/**
	 * Launch a physical attack against a target (Simple, Bow, Pole or Dual).<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Get the active weapon (always equiped in the right hand)</li><BR>
	 * <BR>
	 * <li>If weapon is a bow, check for arrows, MP and bow re-use delay (if necessary, equip the L2PcInstance with arrows in left hand)</li>
	 * <li>If weapon is a bow, consume MP and set the new period of bow non re-use</li><BR>
	 * <BR>
	 * <li>Get the Attack Speed of the L2Character (delay (in milliseconds) before next attack)</li>
	 * <li>Select the type of attack to start (Simple, Bow, Pole or Dual) and verify if SoulShot are charged then start calculation</li>
	 * <li>If the Server->Client packet Attack contains at least 1 hit, send the Server->Client packet Attack to the L2Character AND to all L2PcInstance in the knownPlayers of the L2Character</li>
	 * <li>Notify AI with EVT_READY_TO_ACT</li><BR>
	 * <BR>
	 * @param target The L2Character targeted
	 */
	protected void doAttack(final L2Character target)
	{
		if (Config.DEBUG)
		{
			LOGGER.debug(getName() + " doAttack: target=" + target);
		}
		
		if (target == null)
		{
			return;
		}
		
		// Like L2OFF wait that the hit task finish and then player can move
		if (this instanceof L2PcInstance && ((L2PcInstance) this).isMovingTaskDefined() && !((L2PcInstance) this).isAttackingNow())
		{
			final L2ItemInstance rhand = ((L2PcInstance) this).getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
			if (rhand != null && rhand.getItemType() != L2WeaponType.BOW || rhand == null)
			{
				((L2PcInstance) this).startMovingTask();
				return;
			}
		}
		
		if (isAlikeDead())
		{
			// If L2PcInstance is dead or the target is dead, the action is stoped
			getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (this instanceof L2NpcInstance && target.isAlikeDead())
		{
			// If L2PcInstance is dead or the target is dead, the action is stoped
			getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (this instanceof L2PcInstance && target.isDead() && !target.isFakeDeath())
		{
			// If L2PcInstance is dead or the target is dead, the action is stoped
			getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (!getKnownList().knowsObject(target))
		{
			// If L2PcInstance is dead or the target is dead, the action is stoped
			getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (this instanceof L2PcInstance && isDead())
		{
			// If L2PcInstance is dead or the target is dead, the action is stoped
			getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (target instanceof L2PcInstance && ((L2PcInstance) target).getDuelState() == Duel.DUELSTATE_DEAD)
		{
			// If L2PcInstance is dead or the target is dead, the action is stoped
			getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (target instanceof L2DoorInstance && !((L2DoorInstance) target).isAttackable(this))
		{
			return;
		}
		
		if (isAttackingDisabled())
		{
			return;
		}
		
		if (this instanceof L2PcInstance)
		{
			if (((L2PcInstance) this).inObserverMode())
			{
				sendPacket(new SystemMessage(SystemMessageId.OBSERVERS_CANNOT_PARTICIPATE));
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			if (target instanceof L2PcInstance)
			{
				if (((L2PcInstance) target).isCursedWeaponEquiped() && ((L2PcInstance) this).getLevel() <= Config.MAX_LEVEL_NEWBIE)
				{
					((L2PcInstance) this).sendMessage("Can't attack a cursed player when under level 21.");
					sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
				
				if (((L2PcInstance) this).isCursedWeaponEquiped() && ((L2PcInstance) target).getLevel() <= Config.MAX_LEVEL_NEWBIE)
				{
					((L2PcInstance) this).sendMessage("Can't attack a newbie player using a cursed weapon.");
					sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
			}
			
			// thank l2dot
			if (getObjectId() == target.getObjectId())
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			if (target instanceof L2NpcInstance && Config.DISABLE_ATTACK_NPC_TYPE)
			{
				final String mobtype = ((L2NpcInstance) target).getTemplate().type;
				if (!Config.LIST_ALLOWED_NPC_TYPES.contains(mobtype))
				{
					final SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
					sm.addString("Npc Type " + mobtype + " has Protection - No Attack Allowed!");
					((L2PcInstance) this).sendPacket(sm);
					((L2PcInstance) this).sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
			}
		}
		
		// Get the active weapon instance (always equiped in the right hand)
		L2ItemInstance weaponInst = getActiveWeaponInstance();
		
		// Get the active weapon item corresponding to the active weapon instance (always equiped in the right hand)
		L2Weapon weaponItem = getActiveWeaponItem();
		
		if (weaponItem != null && weaponItem.getItemType() == L2WeaponType.ROD)
		{
			// You can't make an attack with a fishing pole.
			((L2PcInstance) this).sendPacket(new SystemMessage(SystemMessageId.CANNOT_ATTACK_WITH_FISHING_POLE));
			getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		/*
		 * TEMPFIX: Check client Z coordinate instead of server z to avoid exploit killing Zaken from others floor
		 */
		if (target instanceof L2GrandBossInstance && ((L2GrandBossInstance) target).getNpcId() == 29022)
		{
			if (Math.abs(getClientZ() - target.getZ()) > 200)
			{
				sendPacket(new SystemMessage(SystemMessageId.CANT_SEE_TARGET));
				getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}
		
		// GeoData Los Check here (or dz > 1000)
		if (!GeoData.getInstance().canSeeTarget(this, target))
		{
			sendPacket(new SystemMessage(SystemMessageId.CANT_SEE_TARGET));
			getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Check for a bow
		if (weaponItem != null && weaponItem.getItemType() == L2WeaponType.BOW)
		{
			// Equip arrows needed in left hand and send a Server->Client packet ItemList to the L2PcINstance then return True
			if (!checkAndEquipArrows())
			{
				// Cancel the action because the L2PcInstance have no arrow
				getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
				
				sendPacket(ActionFailed.STATIC_PACKET);
				sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_ARROWS));
				return;
			}
			
			// Check for arrows and MP
			if (this instanceof L2PcInstance)
			{
				// Checking if target has moved to peace zone - only for player-bow attacks at the moment
				// Other melee is checked in movement code and for offensive spells a check is done every time
				if (target.isInsidePeaceZone((L2PcInstance) this))
				{
					getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
					sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
				
				// Verify if the bow can be use
				if (disableBowAttackEndTime <= GameTimeController.getGameTicks())
				{
					// Verify if L2PcInstance owns enough MP
					final int saMpConsume = (int) getStat().calcStat(Stats.MP_CONSUME, 0, null, null);
					final int mpConsume = saMpConsume == 0 ? weaponItem.getMpConsume() : saMpConsume;
					
					if (getCurrentMp() < mpConsume)
					{
						// If L2PcInstance doesn't have enough MP, stop the attack
						ThreadPoolManager.getInstance().scheduleAi(new NotifyAITask(CtrlEvent.EVT_READY_TO_ACT), 1000);
						
						sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_MP));
						sendPacket(ActionFailed.STATIC_PACKET);
						return;
					}
					// If L2PcInstance have enough MP, the bow consummes it
					getStatus().reduceMp(mpConsume);
					
					// Set the period of bow non re-use
					disableBowAttackEndTime = 5 * GameTimeController.TICKS_PER_SECOND + GameTimeController.getGameTicks();
				}
				else
				{
					// Cancel the action because the bow can't be re-use at this moment
					ThreadPoolManager.getInstance().scheduleAi(new NotifyAITask(CtrlEvent.EVT_READY_TO_ACT), 1000);
					
					sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
				
			}
			else if (this instanceof L2NpcInstance)
			{
				if (disableBowAttackEndTime > GameTimeController.getGameTicks())
				{
					return;
				}
			}
		}
		
		if (EngineModsManager.canAttack(this, target))
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Add the L2PcInstance to knownObjects and knownPlayer of the target
		target.getKnownList().addKnownObject(this);
		
		// Reduce the current CP if TIREDNESS configuration is activated
		if (Config.ALT_GAME_TIREDNESS)
		{
			setCurrentCp(getCurrentCp() - 10);
		}
		
		final int timeAtk = calculateTimeBetweenAttacks(target, weaponItem);
		
		// Recharge any active auto soulshot tasks for player (or player's summon if one exists).
		if (this instanceof L2PcInstance)
		{
			((L2PcInstance) this).rechargeAutoSoulShot(true, false, false, timeAtk);
		}
		else if (this instanceof L2Summon)
		{
			((L2Summon) this).getOwner().rechargeAutoSoulShot(true, false, true, timeAtk);
		}
		
		// Verify if soulshots are charged.
		boolean wasSSCharged;
		
		if (this instanceof L2Summon && !(this instanceof L2PetInstance))
		{
			wasSSCharged = ((L2Summon) this).getChargedSoulShot() != L2ItemInstance.CHARGED_NONE;
		}
		else
		{
			wasSSCharged = weaponInst != null && weaponInst.getChargedSoulshot() != L2ItemInstance.CHARGED_NONE;
		}
		
		// Get the Attack Speed of the L2Character (delay (in milliseconds) before next attack)
		// the hit is calculated to happen halfway to the animation - might need further tuning e.g. in bow case
		final int timeToHit = timeAtk / 2;
		attackEndTime = GameTimeController.getGameTicks();
		attackEndTime += timeAtk / GameTimeController.MILLIS_IN_TICK;
		attackEndTime -= 1;
		
		int ssGrade = 0;
		
		if (weaponItem != null)
		{
			ssGrade = weaponItem.getCrystalType();
		}
		
		// Create a Server->Client packet Attack
		Attack attack = new Attack(this, wasSSCharged, ssGrade);
		
		boolean hitted;
		
		// Set the Attacking Body part to CHEST
		setAttackingBodypart();
		
		// Heading calculation on every attack
		setHeading(Util.calculateHeadingFrom(getX(), getY(), target.getX(), target.getY()));
		
		// Get the Attack Reuse Delay of the L2Weapon
		final int reuse = calculateReuseTime(target, weaponItem);
		
		// Select the type of attack to start
		if (weaponItem == null)
		{
			hitted = doAttackHitSimple(attack, target, timeToHit);
		}
		else if (weaponItem.getItemType() == L2WeaponType.BOW)
		{
			hitted = doAttackHitByBow(attack, target, timeAtk, reuse);
		}
		else if (weaponItem.getItemType() == L2WeaponType.POLE)
		{
			hitted = doAttackHitByPole(attack, timeToHit);
		}
		else if (isUsingDualWeapon())
		{
			hitted = doAttackHitByDual(attack, target, timeToHit);
		}
		else
		{
			hitted = doAttackHitSimple(attack, target, timeToHit);
		}
		
		// Flag the attacker if it's a L2PcInstance outside a PvP area
		L2PcInstance player = null;
		
		if (this instanceof L2PcInstance)
		{
			player = (L2PcInstance) this;
		}
		else if (this instanceof L2Summon)
		{
			player = ((L2Summon) this).getOwner();
		}
		
		if (player != null)
		{
			player.updatePvPStatus(target);
		}
		
		// Check if hit isn't missed
		if (!hitted)
		{
			// MAJAX fix
			sendPacket(new SystemMessage(SystemMessageId.MISSED_TARGET));
			// Abort the attack of the L2Character and send Server->Client ActionFailed packet
			abortAttack();
		}
		else
		{
			/*
			 * ADDED BY nexus - 2006-08-17 As soon as we know that our hit landed, we must discharge any active soulshots. This must be done so to avoid unwanted soulshot consumption.
			 */
			
			// If we didn't miss the hit, discharge the shoulshots, if any
			if (this instanceof L2Summon && !(this instanceof L2PetInstance))
			{
				((L2Summon) this).setChargedSoulShot(L2ItemInstance.CHARGED_NONE);
			}
			else if (weaponInst != null)
			{
				weaponInst.setChargedSoulshot(L2ItemInstance.CHARGED_NONE);
			}
			
			if (player != null)
			{
				if (player.isCursedWeaponEquiped())
				{
					// If hitted by a cursed weapon, Cp is reduced to 0
					if (!target.isInvul())
					{
						target.setCurrentCp(0);
					}
				}
				else if (player.isHero())
				{
					if (target instanceof L2PcInstance && ((L2PcInstance) target).isCursedWeaponEquiped())
					{
						// If a cursed weapon is hitted by a Hero, Cp is reduced to 0
						target.setCurrentCp(0);
					}
				}
			}
			
			weaponInst = null;
			weaponItem = null;
		}
		
		// If the Server->Client packet Attack contains at least 1 hit, send the Server->Client packet Attack
		// to the L2Character AND to all L2PcInstance in the knownPlayers of the L2Character
		if (attack.hasHits())
		{
			broadcastPacket(attack);
			fireEvent(EventType.ATTACK.name, new Object[]
			{
				getTarget()
			});
		}
		
		// Like L2OFF mobs id 27181 can teleport players near cabrio
		if (this instanceof L2MonsterInstance && ((L2MonsterInstance) this).getNpcId() == 27181)
		{
			final int rndNum = Rnd.get(100);
			final L2PcInstance gettarget = (L2PcInstance) getTarget();
			
			if (rndNum < 5 && gettarget != null)
			{
				gettarget.teleToLocation(179768, 6364, -2734);
			}
		}
		
		// Like L2OFF if target is not auto attackable you give only one hit
		if (this instanceof L2PcInstance && target instanceof L2PcInstance && !target.isAutoAttackable(this))
		{
			((L2PcInstance) this).getAI().clientStopAutoAttack();
			((L2PcInstance) this).getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, this);
		}
		
		// Notify AI with EVT_READY_TO_ACT
		ThreadPoolManager.getInstance().scheduleAi(new NotifyAITask(CtrlEvent.EVT_READY_TO_ACT), timeAtk + reuse);
		
		attack = null;
		player = null;
	}
	
	/**
	 * Launch a Bow attack.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Calculate if hit is missed or not</li>
	 * <li>Consumme arrows</li>
	 * <li>If hit isn't missed, calculate if shield defense is efficient</li>
	 * <li>If hit isn't missed, calculate if hit is critical</li>
	 * <li>If hit isn't missed, calculate physical damages</li>
	 * <li>If the L2Character is a L2PcInstance, Send a Server->Client packet SetupGauge</li>
	 * <li>Create a new hit task with Medium priority</li>
	 * <li>Calculate and set the disable delay of the bow in function of the Attack Speed</li>
	 * <li>Add this hit to the Server-Client packet Attack</li><BR>
	 * <BR>
	 * @param  attack Server->Client packet Attack in which the hit will be added
	 * @param  target The L2Character targeted
	 * @param  sAtk   The Attack Speed of the attacker
	 * @param  reuse  the reuse
	 * @return        True if the hit isn't missed
	 */
	private boolean doAttackHitByBow(final Attack attack, final L2Character target, final int sAtk, final int reuse)
	{
		int damage1 = 0;
		boolean shld1 = false;
		boolean crit1 = false;
		
		// Calculate if hit is missed or not
		final boolean miss1 = Formulas.calcHitMiss(this, target);
		
		// Consumme arrows
		reduceArrowCount();
		
		playerMove = null;
		
		// Check if hit isn't missed
		if (!miss1)
		{
			// Calculate if shield defense is efficient
			shld1 = Formulas.calcShldUse(this, target);
			
			// Calculate if hit is critical
			crit1 = Formulas.calcCrit(getStat().getCriticalHit(target, null));
			
			// Calculate physical damages
			damage1 = (int) Formulas.calcPhysDam(this, target, null, shld1, crit1, false, attack.soulshot);
		}
		
		// Check if the L2Character is a L2PcInstance
		if (this instanceof L2PcInstance)
		{
			// Send a system message
			sendPacket(new SystemMessage(SystemMessageId.GETTING_READY_TO_SHOOT_AN_ARROW));
			
			// Send a Server->Client packet SetupGauge
			SetupGauge sg = new SetupGauge(SetupGauge.RED, sAtk + reuse);
			sendPacket(sg);
			sg = null;
		}
		
		// Create a new hit task with Medium priority
		ThreadPoolManager.getInstance().scheduleAi(new HitTask(target, damage1, crit1, miss1, attack.soulshot, shld1), sAtk);
		
		// Calculate and set the disable delay of the bow in function of the Attack Speed
		disableBowAttackEndTime = (sAtk + reuse) / GameTimeController.MILLIS_IN_TICK + GameTimeController.getGameTicks();
		
		// Add this hit to the Server-Client packet Attack
		attack.addHit(target, damage1, miss1, crit1, shld1);
		
		// Return true if hit isn't missed
		return !miss1;
	}
	
	/**
	 * Launch a Dual attack.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Calculate if hits are missed or not</li>
	 * <li>If hits aren't missed, calculate if shield defense is efficient</li>
	 * <li>If hits aren't missed, calculate if hit is critical</li>
	 * <li>If hits aren't missed, calculate physical damages</li>
	 * <li>Create 2 new hit tasks with Medium priority</li>
	 * <li>Add those hits to the Server-Client packet Attack</li><BR>
	 * <BR>
	 * @param  attack Server->Client packet Attack in which the hit will be added
	 * @param  target The L2Character targeted
	 * @param  sAtk   the s atk
	 * @return        True if hit 1 or hit 2 isn't missed
	 */
	private boolean doAttackHitByDual(final Attack attack, final L2Character target, final int sAtk)
	{
		int damage1 = 0;
		int damage2 = 0;
		boolean shld1 = false;
		boolean shld2 = false;
		boolean crit1 = false;
		boolean crit2 = false;
		
		// Calculate if hits are missed or not
		final boolean miss1 = Formulas.calcHitMiss(this, target);
		final boolean miss2 = Formulas.calcHitMiss(this, target);
		
		// Check if hit 1 isn't missed
		if (!miss1)
		{
			// Calculate if shield defense is efficient against hit 1
			shld1 = Formulas.calcShldUse(this, target);
			
			// Calculate if hit 1 is critical
			crit1 = Formulas.calcCrit(getStat().getCriticalHit(target, null));
			
			// Calculate physical damages of hit 1
			damage1 = (int) Formulas.calcPhysDam(this, target, null, shld1, crit1, true, attack.soulshot);
			damage1 /= 2;
		}
		
		// Check if hit 2 isn't missed
		if (!miss2)
		{
			// Calculate if shield defense is efficient against hit 2
			shld2 = Formulas.calcShldUse(this, target);
			
			// Calculate if hit 2 is critical
			crit2 = Formulas.calcCrit(getStat().getCriticalHit(target, null));
			
			// Calculate physical damages of hit 2
			damage2 = (int) Formulas.calcPhysDam(this, target, null, shld2, crit2, true, attack.soulshot);
			damage2 /= 2;
		}
		
		// Create a new hit task with Medium priority for hit 1
		ThreadPoolManager.getInstance().scheduleAi(new HitTask(target, damage1, crit1, miss1, attack.soulshot, shld1), sAtk / 2);
		
		// Create a new hit task with Medium priority for hit 2 with a higher delay
		ThreadPoolManager.getInstance().scheduleAi(new HitTask(target, damage2, crit2, miss2, attack.soulshot, shld2), sAtk);
		
		// Add those hits to the Server-Client packet Attack
		attack.addHit(target, damage1, miss1, crit1, shld1);
		attack.addHit(target, damage2, miss2, crit2, shld2);
		
		// Return true if hit 1 or hit 2 isn't missed
		return !miss1 || !miss2;
	}
	
	/**
	 * Launch a Pole attack.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Get all visible objects in a spheric area near the L2Character to obtain possible targets</li>
	 * <li>If possible target is the L2Character targeted, launch a simple attack against it</li>
	 * <li>If possible target isn't the L2Character targeted but is attakable, launch a simple attack against it</li><BR>
	 * <BR>
	 * @param  attack Server->Client packet Attack in which the hit will be added
	 * @param  sAtk   the s atk
	 * @return        True if one hit isn't missed
	 */
	private boolean doAttackHitByPole(final Attack attack, final int sAtk)
	{
		boolean hitted = false;
		
		double angleChar, angleTarget;
		final int maxRadius = (int) getStat().calcStat(Stats.POWER_ATTACK_RANGE, 66, null, null);
		final int maxAngleDiff = (int) getStat().calcStat(Stats.POWER_ATTACK_ANGLE, 120, null, null);
		
		if (getTarget() == null)
		{
			return false;
		}
		
		angleTarget = Util.calculateAngleFrom(this, getTarget());
		setHeading((int) (angleTarget / 9.0 * 1610.0));
		
		angleChar = Util.convertHeadingToDegree(getHeading());
		double attackpercent = 85;
		final int attackcountmax = (int) getStat().calcStat(Stats.ATTACK_COUNT_MAX, 3, null, null);
		int attackcount = 0;
		
		if (angleChar <= 0)
		{
			angleChar += 360;
		}
		
		L2Character target;
		for (final L2Object obj : getKnownList().getKnownObjects().values())
		{
			if (obj instanceof L2Character)
			{
				if (obj instanceof L2PetInstance && this instanceof L2PcInstance && ((L2PetInstance) obj).getOwner() == (L2PcInstance) this)
				{
					continue;
				}
				
				if (!Util.checkIfInRange(maxRadius, this, obj, false))
				{
					continue;
				}
				
				if (Math.abs(obj.getZ() - getZ()) > Config.DIFFERENT_Z_CHANGE_OBJECT)
				{
					continue;
				}
				
				angleTarget = Util.calculateAngleFrom(this, obj);
				
				if (Math.abs(angleChar - angleTarget) > maxAngleDiff && Math.abs(angleChar + 360 - angleTarget) > maxAngleDiff && Math.abs(angleChar - (angleTarget + 360)) > maxAngleDiff)
				{
					continue;
				}
				
				target = (L2Character) obj;
				
				if (!target.isAlikeDead())
				{
					attackcount += 1;
					
					if (attackcount <= attackcountmax)
					{
						if (target == getAI().getAttackTarget() || target.isAutoAttackable(this))
						{
							hitted |= doAttackHitSimple(attack, target, attackpercent, sAtk);
							attackpercent /= 1.15;
							
							// Flag player if the target is another player
							if (this instanceof L2PcInstance && obj instanceof L2PcInstance)
							{
								((L2PcInstance) this).updatePvPStatus(target);
							}
						}
					}
				}
			}
		}
		target = null;
		// Return true if one hit isn't missed
		return hitted;
	}
	
	/**
	 * Launch a simple attack.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Calculate if hit is missed or not</li>
	 * <li>If hit isn't missed, calculate if shield defense is efficient</li>
	 * <li>If hit isn't missed, calculate if hit is critical</li>
	 * <li>If hit isn't missed, calculate physical damages</li>
	 * <li>Create a new hit task with Medium priority</li>
	 * <li>Add this hit to the Server-Client packet Attack</li><BR>
	 * <BR>
	 * @param  attack Server->Client packet Attack in which the hit will be added
	 * @param  target The L2Character targeted
	 * @param  sAtk   the s atk
	 * @return        True if the hit isn't missed
	 */
	private boolean doAttackHitSimple(final Attack attack, final L2Character target, final int sAtk)
	{
		return doAttackHitSimple(attack, target, 100, sAtk);
	}
	
	/**
	 * Do attack hit simple.
	 * @param  attack        the attack
	 * @param  target        the target
	 * @param  attackpercent the attackpercent
	 * @param  sAtk          the s atk
	 * @return               true, if successful
	 */
	private boolean doAttackHitSimple(final Attack attack, final L2Character target, final double attackpercent, final int sAtk)
	{
		int damage1 = 0;
		boolean shld1 = false;
		boolean crit1 = false;
		
		// Calculate if hit is missed or not
		final boolean miss1 = Formulas.calcHitMiss(this, target);
		
		// Check if hit isn't missed
		if (!miss1)
		{
			// Calculate if shield defense is efficient
			shld1 = Formulas.calcShldUse(this, target);
			
			// Calculate if hit is critical
			crit1 = Formulas.calcCrit(getStat().getCriticalHit(target, null));
			
			// Calculate physical damages
			damage1 = (int) Formulas.calcPhysDam(this, target, null, shld1, crit1, false, attack.soulshot);
			
			if (attackpercent != 100)
			{
				damage1 = (int) (damage1 * attackpercent / 100);
			}
		}
		
		// Create a new hit task with Medium priority
		ThreadPoolManager.getInstance().scheduleAi(new HitTask(target, damage1, crit1, miss1, attack.soulshot, shld1), sAtk);
		
		// Add this hit to the Server-Client packet Attack
		attack.addHit(target, damage1, miss1, crit1, shld1);
		
		// Return true if hit isn't missed
		return !miss1;
	}
	
	/**
	 * Manage the casting task (casting and interrupt time, re-use delay...) and display the casting bar and animation on client.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Verify the possibilty of the the cast : skill is a spell, caster isn't muted...</li>
	 * <li>Get the list of all targets (ex : area effects) and define the L2Charcater targeted (its stats will be used in calculation)</li>
	 * <li>Calculate the casting time (base + modifier of MAtkSpd), interrupt time and re-use delay</li>
	 * <li>Send a Server->Client packet MagicSkillUser (to diplay casting animation), a packet SetupGauge (to display casting bar) and a system message</li>
	 * <li>Disable all skills during the casting time (create a task EnableAllSkills)</li>
	 * <li>Disable the skill during the re-use delay (create a task EnableSkill)</li>
	 * <li>Create a task MagicUseTask (that will call method onMagicUseTimer) to launch the Magic Skill at the end of the casting time</li><BR>
	 * <BR>
	 * @param skill The L2Skill to use
	 */
	public void doCast(final L2Skill skill)
	{
		final L2Character activeChar = this;
		
		if (skill == null)
		{
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
			return;
		}
		
		// Check if the skill is a magic spell and if the L2Character is not muted
		if (skill.isMagic() && isMuted() && !skill.isPotion())
		{
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
			return;
		}
		
		// Check if player is using Hero weapon to avoid heal exploit with Infinity Rod
		// Skill ID 2286 : Scroll of Recovery
		// Skill ID 5041 : Charm of Courage
		if ((skill.getId() == 2286 || skill.getId() == 5041) && activeChar instanceof L2PcInstance)
		{
			L2PcInstance player = (L2PcInstance) activeChar;
			if (player.getActiveWeaponInstance() != null && player.getActiveWeaponInstance().isHeroItem())
			{
				getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
				return;
			}
		}
		
		// Check if the skill is psychical and if the L2Character is not psychical_muted
		if (!skill.isMagic() && isPsychicalMuted() && !skill.isPotion())
		{
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
			return;
		}
		
		// Can't use Hero and resurrect skills during Olympiad
		if (activeChar instanceof L2PcInstance && ((L2PcInstance) activeChar).isInOlympiadMode() && (skill.isHeroSkill() || skill.getSkillType() == SkillType.RESURRECT))
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.THIS_SKILL_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT);
			sendPacket(sm);
			sm = null;
			return;
		}
		
		// Like L2OFF you can't use skills when you are attacking now
		if (activeChar instanceof L2PcInstance && !skill.isPotion())
		{
			final L2ItemInstance rhand = ((L2PcInstance) this).getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
			if (rhand != null && rhand.getItemType() == L2WeaponType.BOW)
			{
				if (isAttackingNow())
				{
					return;
				}
			}
		}
		
		// prevent casting signets to peace zone
		if (skill.getSkillType() == SkillType.SIGNET || skill.getSkillType() == SkillType.SIGNET_CASTTIME)
		{
			/*
			 * for (L2Effect effect : getAllEffects()) { if (effect.getEffectType() == L2Effect.EffectType.SIGNET_EFFECT || effect.getEffectType() == L2Effect.EffectType.SIGNET_GROUND) { SystemMessage sm = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED); sm.addSkillName(skill.getId()); sendPacket(sm);
			 * return; } }
			 */
			
			final L2WorldRegion region = getWorldRegion();
			if (region == null)
			{
				return;
			}
			boolean canCast = true;
			if (skill.getTargetType() == SkillTargetType.TARGET_GROUND && this instanceof L2PcInstance)
			{
				final Point3D wp = ((L2PcInstance) this).getCurrentSkillWorldPosition();
				if (!region.checkEffectRangeInsidePeaceZone(skill, wp.getX(), wp.getY(), wp.getZ()))
				{
					canCast = false;
				}
			}
			else if (!region.checkEffectRangeInsidePeaceZone(skill, getX(), getY(), getZ()))
			{
				canCast = false;
			}
			if (!canCast)
			{
				final SystemMessage sm = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
				sm.addSkillName(skill.getId());
				sendPacket(sm);
				return;
			}
		}
		
		// Recharge AutoSoulShot
		
		final int atkTime = Formulas.getInstance().calcMAtkSpd(activeChar, skill, skill.getHitTime());
		if (skill.useSoulShot())
		{
			if (activeChar instanceof L2PcInstance)
			{
				((L2PcInstance) activeChar).rechargeAutoSoulShot(true, false, false, atkTime);
			}
			else if (this instanceof L2Summon)
			{
				((L2Summon) activeChar).getOwner().rechargeAutoSoulShot(true, false, true, atkTime);
			}
		}
		
		// else if (skill.useFishShot())
		// {
		// if (this instanceof L2PcInstance)
		// ((L2PcInstance)this).rechargeAutoSoulShot(true, false, false);
		// }
		
		// Get all possible targets of the skill in a table in function of the skill target type
		final L2Object[] targets = skill.getTargetList(activeChar);
		// Set the target of the skill in function of Skill Type and Target Type
		L2Character target = null;
		
		if (skill.getTargetType() == SkillTargetType.TARGET_AURA || skill.getTargetType() == SkillTargetType.TARGET_GROUND || skill.isPotion())
		{
			target = this;
		}
		else if (targets == null || targets.length == 0)
		{
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
			return;
		}
		else if ((skill.getSkillType() == SkillType.BUFF || skill.getSkillType() == SkillType.HEAL || skill.getSkillType() == SkillType.COMBATPOINTHEAL || skill.getSkillType() == SkillType.COMBATPOINTPERCENTHEAL || skill.getSkillType() == SkillType.MANAHEAL || skill.getSkillType() == SkillType.REFLECT || skill.getSkillType() == SkillType.SEED
			|| skill.getTargetType() == L2Skill.SkillTargetType.TARGET_SELF || skill.getTargetType() == L2Skill.SkillTargetType.TARGET_PET || skill.getTargetType() == L2Skill.SkillTargetType.TARGET_PARTY || skill.getTargetType() == L2Skill.SkillTargetType.TARGET_CLAN || skill.getTargetType() == L2Skill.SkillTargetType.TARGET_ALLY) && !skill.isPotion())
		{
			target = (L2Character) targets[0];
			
			/*
			 * if (this instanceof L2PcInstance && target instanceof L2PcInstance && target.getAI().getIntention() == CtrlIntention.AI_INTENTION_ATTACK) { if(skill.getSkillType() == SkillType.BUFF || skill.getSkillType() == SkillType.HOT || skill.getSkillType() == SkillType.HEAL || skill.getSkillType() ==
			 * SkillType.HEAL_PERCENT || skill.getSkillType() == SkillType.MANAHEAL || skill.getSkillType() == SkillType.MANAHEAL_PERCENT || skill.getSkillType() == SkillType.BALANCE_LIFE) target.setLastBuffer(this); if (((L2PcInstance)this).isInParty() && skill.getTargetType() ==
			 * L2Skill.SkillTargetType.TARGET_PARTY) { for (L2PcInstance member : ((L2PcInstance)this).getParty().getPartyMembers()) member.setLastBuffer(this); } }
			 */
		}
		else
		{
			target = (L2Character) getTarget();
		}
		
		if (target == null)
		{
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
			return;
		}
		
		// Player can't heal rb config
		if (!Config.PLAYERS_CAN_HEAL_RB && activeChar instanceof L2PcInstance && !((L2PcInstance) activeChar).isGM() && (target instanceof L2RaidBossInstance || target instanceof L2GrandBossInstance) && (skill.getSkillType() == SkillType.HEAL || skill.getSkillType() == SkillType.HEAL_PERCENT))
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (activeChar instanceof L2PcInstance && target instanceof L2NpcInstance && Config.DISABLE_ATTACK_NPC_TYPE)
		{
			final String mobtype = ((L2NpcInstance) target).getTemplate().type;
			if (!Config.LIST_ALLOWED_NPC_TYPES.contains(mobtype))
			{
				final SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
				sm.addString("Npc Type " + mobtype + " has Protection - No Attack Allowed!");
				((L2PcInstance) activeChar).sendPacket(sm);
				((L2PcInstance) activeChar).sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}
		
		if (skill.isPotion())
		{
			setLastPotionCast(skill);
		}
		else
		{
			setLastSkillCast(skill);
		}
		
		// Get the Identifier of the skill
		final int magicId = skill.getId();
		
		// Get the Display Identifier for a skill that client can't display
		final int displayId = skill.getDisplayId();
		
		// Get the level of the skill
		int level = skill.getLevel();
		
		if (level < 1)
		{
			level = 1;
		}
		
		// Get the casting time of the skill (base)
		int hitTime = skill.getHitTime();
		int coolTime = skill.getCoolTime();
		final boolean effectWhileCasting = skill.hasEffectWhileCasting();
		
		final boolean forceBuff = skill.getSkillType() == SkillType.FORCE_BUFF && target instanceof L2PcInstance;
		
		// Calculate the casting time of the skill (base + modifier of MAtkSpd)
		// Don't modify the skill time for FORCE_BUFF skills. The skill time for those skills represent the buff time.
		if (!effectWhileCasting && !forceBuff && !skill.isStaticHitTime())
		{
			hitTime = Formulas.getInstance().calcMAtkSpd(activeChar, skill, hitTime);
			
			if (coolTime > 0)
			{
				coolTime = Formulas.getInstance().calcMAtkSpd(activeChar, skill, coolTime);
			}
		}
		
		// Calculate altered Cast Speed due to BSpS/SpS only for Magic skills
		if ((checkBss() || checkSps()) && !skill.isStaticHitTime() && !skill.isPotion() && skill.isMagic())
		{
			
			// Only takes 70% of the time to cast a BSpS/SpS cast
			hitTime = (int) (0.70 * hitTime);
			coolTime = (int) (0.70 * coolTime);
			
			// Because the following are magic skills that do not actively 'eat' BSpS/SpS,
			// I must 'eat' them here so players don't take advantage of infinite speed increase
			/* MANAHEAL, MANARECHARGE, RESURRECT, RECALL */
			// if (skill.getSkillType() == SkillType.MANAHEAL || skill.getSkillType() == SkillType.MANARECHARGE || skill.getSkillType() == SkillType.RESURRECT || skill.getSkillType() == SkillType.RECALL)
			// {
			// if (checkBss())
			// removeBss();
			// else
			// removeSps();
			// }
			
		}
		
		/*
		 * // Calculate altered Cast Speed due to BSpS/SpS L2ItemInstance weaponInst = getActiveWeaponInstance(); if(weaponInst != null && skill.isMagic() && !forceBuff && skill.getTargetType() != SkillTargetType.TARGET_SELF && !skill.isStaticHitTime() && !skill.isPotion()) {
		 * if(weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT || weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_SPIRITSHOT) { //Only takes 70% of the time to cast a BSpS/SpS cast hitTime = (int) (0.70 * hitTime); coolTime = (int) (0.70 * coolTime); //Because the
		 * following are magic skills that do not actively 'eat' BSpS/SpS, //I must 'eat' them here so players don't take advantage of infinite speed increase if(skill.getSkillType() == SkillType.BUFF || skill.getSkillType() == SkillType.MANAHEAL || skill.getSkillType() == SkillType.RESURRECT ||
		 * skill.getSkillType() == SkillType.RECALL || skill.getSkillType() == SkillType.DOT) { weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE); } } } weaponInst = null;
		 */
		
		if (skill.isPotion())
		{
			// Set the castEndTime and castInterruptTim. +10 ticks for lag situations, will be reseted in onMagicFinalizer
			castPotionEndTime = 10 + GameTimeController.getGameTicks() + (coolTime + hitTime) / GameTimeController.MILLIS_IN_TICK;
			castPotionInterruptTime = -2 + GameTimeController.getGameTicks() + hitTime / GameTimeController.MILLIS_IN_TICK;
			
		}
		else
		{
			// Set the castEndTime and castInterruptTim. +10 ticks for lag situations, will be reseted in onMagicFinalizer
			castEndTime = 10 + GameTimeController.getGameTicks() + (coolTime + hitTime) / GameTimeController.MILLIS_IN_TICK;
			castInterruptTime = -2 + GameTimeController.getGameTicks() + hitTime / GameTimeController.MILLIS_IN_TICK;
			
		}
		
		// Init the reuse time of the skill
		// int reuseDelay = (int)(skill.getReuseDelay() * getStat().getMReuseRate(skill));
		// reuseDelay *= 333.0 / (skill.isMagic() ? getMAtkSpd() : getPAtkSpd());
		int reuseDelay = skill.getReuseDelay();
		
		if (activeChar instanceof L2PcInstance && Formulas.getInstance().calcSkillMastery(activeChar))
		{
			reuseDelay = 0;
		}
		else if (!skill.isStaticReuse() && !skill.isPotion())
		{
			if (skill.isMagic())
			{
				reuseDelay *= getStat().getMReuseRate(skill);
			}
			else
			{
				reuseDelay *= getStat().getPReuseRate(skill);
			}
			
			reuseDelay *= 333.0 / (skill.isMagic() ? getMAtkSpd() : getPAtkSpd());
		}
		
		// To turn local player in target direction
		setHeading(Util.calculateHeadingFrom(getX(), getY(), target.getX(), target.getY()));
		
		/*
		 * if(skill.isOffensive() && skill.getTargetType() != SkillTargetType.TARGET_AURA && target.isBehind(this)) { moveToLocation(target.getX(), target.getY(), target.getZ(), 0); stopMove(null); }
		 */
		
		// Start the effect as long as the player is casting.
		if (effectWhileCasting)
		{
			callSkill(skill, targets);
		}
		
		if (!skill.isToggle()) // Toggle skills should not broadcast MagicSkillUse.
		{
			// Send a Server->Client packet MagicSkillUser with target, displayId, level, skillTime, reuseDelay
			// to the L2Character AND to all L2PcInstance in the knownPlayers of the L2Character
			broadcastPacket(new MagicSkillUser(this, target, displayId, level, hitTime, reuseDelay));
		}
		
		// Send a system message USE_S1 to the L2Character
		if (activeChar instanceof L2PcInstance && magicId != 1312)
		{
			if (skill.isPotion())
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.USE_S1_);
				if (magicId == 2005)
				{
					sm.addItemName(728);
				}
				else if (magicId == 2003)
				{
					sm.addItemName(726);
				}
				else if (magicId == 2166 && skill.getLevel() == 2)
				{
					sm.addItemName(5592);
				}
				else if (magicId == 2166 && skill.getLevel() == 1)
				{
					sm.addItemName(5591);
				}
				else
				{
					sm.addSkillName(magicId, skill.getLevel());
				}
				sendPacket(sm);
				sm = null;
			}
			else
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.USE_S1);
				if (magicId == 2005)
				{
					sm.addItemName(728);
				}
				else if (magicId == 2003)
				{
					sm.addItemName(726);
				}
				else if (magicId == 2166 && skill.getLevel() == 2)
				{
					sm.addItemName(5592);
				}
				else if (magicId == 2166 && skill.getLevel() == 1)
				{
					sm.addItemName(5591);
				}
				else
				{
					sm.addSkillName(magicId, skill.getLevel());
				}
				
				// Skill 2046 is used only for animation on pets
				if (magicId != 2046)
				{
					sendPacket(sm);
				}
				sm = null;
			}
		}
		
		// Skill reuse check
		if (reuseDelay > 30000)
		{
			addTimeStamp(skill, reuseDelay);
		}
		
		// Check if this skill consume mp on start casting
		final int initmpcons = getStat().getMpInitialConsume(skill);
		
		if (initmpcons > 0)
		{
			StatusUpdate su = new StatusUpdate(getObjectId());
			
			if (skill.isDance())
			{
				getStatus().reduceMp(calcStat(Stats.DANCE_MP_CONSUME_RATE, initmpcons, null, null));
			}
			else if (skill.isMagic())
			{
				getStatus().reduceMp(calcStat(Stats.MAGICAL_MP_CONSUME_RATE, initmpcons, null, null));
			}
			else
			{
				getStatus().reduceMp(calcStat(Stats.PHYSICAL_MP_CONSUME_RATE, initmpcons, null, null));
			}
			
			su.addAttribute(StatusUpdate.CUR_MP, (int) getCurrentMp());
			sendPacket(su);
			su = null;
		}
		
		// Disable the skill during the re-use delay and create a task EnableSkill with Medium priority to enable it at the end of the re-use delay
		if (reuseDelay > 10)
		{
			disableSkill(skill, reuseDelay);
		}
		
		// For force buff skills, start the effect as long as the player is casting.
		if (forceBuff)
		{
			startForceBuff(target, skill);
		}
		
		// launch the magic in hitTime milliseconds
		if (hitTime > 210)
		{
			// Send a Server->Client packet SetupGauge with the color of the gauge and the casting time
			if (activeChar instanceof L2PcInstance && !forceBuff)
			{
				SetupGauge sg = new SetupGauge(SetupGauge.BLUE, hitTime);
				sendPacket(sg);
				sg = null;
			}
			
			// Disable all skills during the casting
			if (!skill.isPotion())
			{ // for particular potion is the timestamp to disable particular skill
				
				disableAllSkills();
				
				if (skillCast != null) // delete previous skill cast
				{
					skillCast.cancel(true);
					skillCast = null;
				}
				
			}
			
			// Create a task MagicUseTask to launch the MagicSkill at the end of the casting time (hitTime)
			// For client animation reasons (party buffs especially) 200 ms before!
			if (getForceBuff() != null || effectWhileCasting)
			{
				if (skill.isPotion())
				{
					potionCast = ThreadPoolManager.getInstance().scheduleEffect(new MagicUseTask(targets, skill, coolTime, 2), hitTime);
				}
				else
				{
					skillCast = ThreadPoolManager.getInstance().scheduleEffect(new MagicUseTask(targets, skill, coolTime, 2), hitTime);
				}
			}
			else
			{
				if (skill.isPotion())
				{
					potionCast = ThreadPoolManager.getInstance().scheduleEffect(new MagicUseTask(targets, skill, coolTime, 1), hitTime - 200);
				}
				else
				{
					skillCast = ThreadPoolManager.getInstance().scheduleEffect(new MagicUseTask(targets, skill, coolTime, 1), hitTime - 200);
				}
			}
		}
		else
		{
			onMagicLaunchedTimer(targets, skill, coolTime, true);
		}
		fireEvent(EventType.CAST.name, new Object[]
		{
			skill,
			target,
			targets
		});
		
	}
	
	/**
	 * Index according to skill id the current timestamp of use.<br>
	 * @param skill the s
	 * @param r     the r
	 */
	public void addTimeStamp(final L2Skill skill, final int r)
	{
		
	}
	
	/**
	 * Index according to skill id the current timestamp of use.<br>
	 * @param skill the s
	 */
	public void removeTimeStamp(final L2Skill skill)
	{
		
	}
	
	/**
	 * Starts a force buff on target.<br>
	 * @param target the target
	 * @param skill  the skill
	 */
	public void startForceBuff(final L2Character target, final L2Skill skill)
	{
		if (skill.getSkillType() != SkillType.FORCE_BUFF)
		{
			return;
		}
		
		if (forceBuff == null)
		{
			forceBuff = new ForceBuff(this, target, skill);
		}
	}
	
	/**
	 * Kill the L2Character.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Set target to null and cancel Attack or Cast</li>
	 * <li>Stop movement</li>
	 * <li>Stop HP/MP/CP Regeneration task</li>
	 * <li>Stop all active skills effects in progress on the L2Character</li>
	 * <li>Send the Server->Client packet StatusUpdate with current HP and MP to all other L2PcInstance to inform</li>
	 * <li>Notify L2Character AI</li><BR>
	 * <BR>
	 * <B><U> Overriden in </U> :</B><BR>
	 * <BR>
	 * <li>L2NpcInstance : Create a DecayTask to remove the corpse of the L2NpcInstance after 7 seconds</li>
	 * <li>L2Attackable : Distribute rewards (EXP, SP, Drops...) and notify Quest Engine</li>
	 * <li>L2PcInstance : Apply Death Penalty, Manage gain/loss Karma and Item Drop</li><BR>
	 * <BR>
	 * @param  killer The L2Character who killed it
	 * @return        true, if successful
	 */
	public boolean doDie(final L2Character killer)
	{
		// killing is only possible one time
		synchronized (this)
		{
			if (isKilledAlready())
			{
				return false;
			}
			
			setIsKilledAlready(true);
		}
		// Set target to null and cancel Attack or Cast
		setTarget(null);
		
		// Stop fear to avoid possible bug with char position after death
		if (isAfraid())
		{
			stopFear(null);
		}
		
		// Stop movement
		stopMove(null);
		
		// Stop HP/MP/CP Regeneration task
		getStatus().stopHpMpRegeneration();
		
		// Stop all active skills effects in progress on the L2Character,
		// if the Character isn't affected by Soul of The Phoenix or Salvation
		if (this instanceof L2PlayableInstance && ((L2PlayableInstance) this).isPhoenixBlessed())
		{
			if (((L2PlayableInstance) this).isNoblesseBlessed())
			{
				((L2PlayableInstance) this).stopNoblesseBlessing(null);
			}
			if (((L2PlayableInstance) this).getCharmOfLuck())
			{
				((L2PlayableInstance) this).stopCharmOfLuck(null);
			}
		}
		// Same thing if the Character isn't a Noblesse Blessed L2PlayableInstance
		else if (this instanceof L2PlayableInstance && ((L2PlayableInstance) this).isNoblesseBlessed())
		{
			((L2PlayableInstance) this).stopNoblesseBlessing(null);
			
			if (((L2PlayableInstance) this).getCharmOfLuck())
			{
				((L2PlayableInstance) this).stopCharmOfLuck(null);
			}
		}
		else
		{
			if (this instanceof L2PcInstance)
			{
				
				final L2PcInstance player = (L2PcInstance) this;
				
				// to avoid Event Remove buffs on die
				if (player.inEventDM && DM.isStarted())
				{
					if (Config.DM_REMOVE_BUFFS_ON_DIE)
					{
						stopAllEffects();
					}
				}
				else if (player.inEventTvT && TvT.isStarted())
				{
					if (Config.TVT_REMOVE_BUFFS_ON_DIE)
					{
						stopAllEffects();
					}
				}
				else if (player.inEventCTF && CTF.isStarted())
				{
					if (Config.CTF_REMOVE_BUFFS_ON_DIE)
					{
						stopAllEffects();
					}
				}
				else if (Config.LEAVE_BUFFS_ON_DIE) // this means that the player is not in event
				{
					stopAllEffects();
				}
			}
			else
			// this means all other characters, including Summons
			{
				stopAllEffects();
			}
		}
		
		// if killer is the same then the most damager/hated
		L2Character mostHated = null;
		if (this instanceof L2Attackable)
		{
			mostHated = ((L2Attackable) this).mostHated;
		}
		
		if (mostHated != null && isInsideRadius(mostHated, 200, false, false))
		{
			calculateRewards(mostHated);
		}
		else
		{
			calculateRewards(killer);
		}
		
		// Send the Server->Client packet StatusUpdate with current HP and MP to all other L2PcInstance to inform
		broadcastStatusUpdate();
		
		// Notify L2Character AI
		getAI().notifyEvent(CtrlEvent.EVT_DEAD, null);
		
		if (getWorldRegion() != null)
		{
			getWorldRegion().onDeath(this);
		}
		
		// Notify Quest of character's death
		for (final QuestState qs : getNotifyQuestOfDeath())
		{
			qs.getQuest().notifyDeath(killer == null ? this : killer, this, qs);
		}
		
		getNotifyQuestOfDeath().clear();
		
		getAttackByList().clear();
		
		// If character is PhoenixBlessed a resurrection popup will show up
		if (this instanceof L2PlayableInstance && ((L2PlayableInstance) this).isPhoenixBlessed())
		{
			((L2PcInstance) this).reviveRequest((L2PcInstance) this, null, false);
		}
		fireEvent(EventType.DIE.name, new Object[]
		{
			killer
		});
		
		// Update active skills in progress (In Use and Not In Use because stacked) icones on client
		updateEffectIcons();
		
		// After dead mob check if the killer got a moving task actived
		if (killer instanceof L2PcInstance)
		{
			if (((L2PcInstance) killer).isMovingTaskDefined())
			{
				((L2PcInstance) killer).startMovingTask();
			}
		}
		
		return true;
	}
	
	/**
	 * Calculate rewards.
	 * @param killer the killer
	 */
	protected void calculateRewards(final L2Character killer)
	{
	}
	
	/** Sets HP, MP and CP and revives the L2Character. */
	public void doRevive()
	{
		if (!isTeleporting())
		{
			setIsPendingRevive(false);
			
			if (this instanceof L2PlayableInstance && ((L2PlayableInstance) this).isPhoenixBlessed())
			{
				((L2PlayableInstance) this).stopPhoenixBlessing(null);
				
				// Like L2OFF Soul of The Phoenix and Salvation restore all hp,cp,mp.
				status.setCurrentCp(getMaxCp());
				status.setCurrentHp(getMaxHp());
				status.setCurrentMp(getMaxMp());
			}
			else
			{
				status.setCurrentCp(getMaxCp() * Config.RESPAWN_RESTORE_CP);
				status.setCurrentHp(getMaxHp() * Config.RESPAWN_RESTORE_HP);
			}
		}
		// Start broadcast status
		broadcastPacket(new Revive(this));
		
		if (getWorldRegion() != null)
		{
			getWorldRegion().onRevive(this);
		}
		else
		{
			setIsPendingRevive(true);
		}
		fireEvent(EventType.REVIVE.name, (Object[]) null);
	}
	
	/**
	 * Revives the L2Character using skill.
	 * @param revivePower the revive power
	 */
	public void doRevive(final double revivePower)
	{
		doRevive();
	}
	
	/**
	 * Check if the active L2Skill can be casted.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Check if the L2Character can cast (ex : not sleeping...)</li>
	 * <li>Check if the target is correct</li>
	 * <li>Notify the AI with AI_INTENTION_CAST and target</li><BR>
	 * <BR>
	 * @param skill The L2Skill to use
	 */
	protected void useMagic(final L2Skill skill)
	{
		if (skill == null || isDead())
		{
			return;
		}
		
		// Check if the L2Character can cast
		if (!skill.isPotion() && isAllSkillsDisabled())
		{
			// must be checked by caller
			return;
		}
		
		// Ignore the passive skill request. why does the client send it anyway ??
		if (skill.isPassive() || skill.isChance())
		{
			return;
		}
		
		// Get the target for the skill
		L2Object target = null;
		
		switch (skill.getTargetType())
		{
			case TARGET_AURA: // AURA, SELF should be cast even if no target has been found
			case TARGET_SELF:
				target = this;
				break;
			default:
				// Get the first target of the list
				target = skill.getFirstOfTargetList(this);
				break;
		}
		
		// Notify the AI with AI_INTENTION_CAST and target
		getAI().setIntention(CtrlIntention.AI_INTENTION_CAST, skill, target);
		target = null;
	}
	
	/**
	 * Return the L2CharacterAI of the L2Character and if its null create a new one.
	 * @return the aI
	 */
	public L2CharacterAI getAI()
	{
		if (aiCharacter == null)
		{
			synchronized (this)
			{
				if (aiCharacter == null)
				{
					aiCharacter = new L2CharacterAI(new AIAccessor());
				}
			}
		}
		
		return aiCharacter;
	}
	
	/**
	 * Sets the aI.
	 * @param newAI the new aI
	 */
	public void setAI(final L2CharacterAI newAI)
	{
		L2CharacterAI oldAI = getAI();
		
		if (oldAI != null && oldAI != newAI && oldAI instanceof L2AttackableAI)
		{
			((L2AttackableAI) oldAI).stopAITask();
		}
		aiCharacter = newAI;
		
		oldAI = null;
	}
	
	/**
	 * Return True if the L2Character has a L2CharacterAI.
	 * @return true, if successful
	 */
	public boolean hasAI()
	{
		return aiCharacter != null;
	}
	
	/**
	 * Return True if the L2Character is RaidBoss or his minion.
	 * @return true, if is raid
	 */
	@Override
	public boolean isRaid()
	{
		return false;
	}
	
	/**
	 * Return True if the L2Character is an Npc.
	 * @return true, if is npc
	 */
	@Override
	public boolean isNpc()
	{
		return false;
	}
	
	/**
	 * Return a list of L2Character that attacked.
	 * @return the attack by list
	 */
	public List<L2Character> getAttackByList()
	{
		if (attackByList == null)
		{
			attackByList = new ArrayList<>();
		}
		
		return attackByList;
	}
	
	/**
	 * Gets the last skill cast.
	 * @return the last skill cast
	 */
	public final L2Skill getLastSkillCast()
	{
		return lastSkillCast;
	}
	
	/**
	 * Sets the last skill cast.
	 * @param skill the new last skill cast
	 */
	public void setLastSkillCast(final L2Skill skill)
	{
		lastSkillCast = skill;
	}
	
	/**
	 * Gets the last potion cast.
	 * @return the last potion cast
	 */
	public final L2Skill getLastPotionCast()
	{
		return lastPotionCast;
	}
	
	/**
	 * Sets the last potion cast.
	 * @param skill the new last potion cast
	 */
	public void setLastPotionCast(final L2Skill skill)
	{
		lastPotionCast = skill;
	}
	
	/**
	 * Checks if is afraid.
	 * @return true, if is afraid
	 */
	public final boolean isAfraid()
	{
		return isAfraid;
	}
	
	/**
	 * Sets the checks if is afraid.
	 * @param value the new checks if is afraid
	 */
	public final void setIsAfraid(final boolean value)
	{
		isAfraid = value;
	}
	
	/**
	 * Return True if the L2Character is dead or use fake death.
	 * @return true, if is alike dead
	 */
	public final boolean isAlikeDead()
	{
		return isFakeDeath() || !(getCurrentHp() > 0.01);
	}
	
	/**
	 * Return True if the L2Character can't use its skills (ex : stun, sleep...).
	 * @return true, if is all skills disabled
	 */
	public final boolean isAllSkillsDisabled()
	{
		return allSkillsDisabled || isImmobileUntilAttacked() || isStunned() || isSleeping() || isParalyzed();
	}
	
	/**
	 * Return True if the L2Character can't attack (stun, sleep, attackEndTime, fakeDeath, paralyse).
	 * @return true, if is attacking disabled
	 */
	public boolean isAttackingDisabled()
	{
		return isImmobileUntilAttacked() || isStunned() || isSleeping() || isFallsdown() || attackEndTime > GameTimeController.getGameTicks() || isFakeDeath() || isParalyzed() || isAttackDisabled();
	}
	
	/**
	 * Gets the calculators.
	 * @return the calculators
	 */
	public final Calculator[] getCalculators()
	{
		return calculators;
	}
	
	/**
	 * Checks if is confused.
	 * @return true, if is confused
	 */
	public final boolean isConfused()
	{
		return isConfused;
	}
	
	/**
	 * Sets the checks if is confused.
	 * @param value the new checks if is confused
	 */
	public final void setIsConfused(final boolean value)
	{
		isConfused = value;
	}
	
	/**
	 * Return True if the L2Character is dead.
	 * @return true, if is dead
	 */
	public final boolean isDead()
	{
		return !isFakeDeath() && getCurrentHp() < 0.5;
	}
	
	/**
	 * Checks if is fake death.
	 * @return true, if is fake death
	 */
	public final boolean isFakeDeath()
	{
		return isFakeDeath;
	}
	
	/**
	 * Sets the checks if is fake death.
	 * @param value the new checks if is fake death
	 */
	public final void setIsFakeDeath(final boolean value)
	{
		isFakeDeath = value;
	}
	
	/**
	 * Return True if the L2Character is flying.
	 * @return true, if is flying
	 */
	public final boolean isFlying()
	{
		return isFlying;
	}
	
	/**
	 * Set the L2Character flying mode to True.
	 * @param mode the new checks if is flying
	 */
	public final void setIsFlying(final boolean mode)
	{
		isFlying = mode;
	}
	
	/**
	 * Checks if is fallsdown.
	 * @return true, if is fallsdown
	 */
	public final boolean isFallsdown()
	{
		return isFallsdown;
	}
	
	/**
	 * Sets the checks if is fallsdown.
	 * @param value the new checks if is fallsdown
	 */
	public final void setIsFallsdown(final boolean value)
	{
		isFallsdown = value;
	}
	
	/**
	 * Checks if is imobilised.
	 * @return true, if is imobilised
	 */
	public boolean isImobilised()
	{
		return isImobilised > 0;
	}
	
	/**
	 * Sets the checks if is imobilised.
	 * @param value the new checks if is imobilised
	 */
	public void setIsImobilised(final boolean value)
	{
		// Stop this if he is moving
		getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		
		if (value)
		{
			isImobilised++;
		}
		else
		{
			isImobilised--;
		}
	}
	
	public boolean isBlockBuff()
	{
		return isBlockBuff;
	}
	
	public void setBlockBuff(final boolean blockBuff)
	{
		isBlockBuff = blockBuff;
	}
	
	public boolean isBlockDebuff()
	{
		return isBlockDebuff;
	}
	
	public void setBlockDebuff(final boolean blockDebuff)
	{
		isBlockDebuff = blockDebuff;
	}
	
	/**
	 * Checks if is killed already.
	 * @return true, if is killed already
	 */
	public final boolean isKilledAlready()
	{
		return isKilledAlready;
	}
	
	/**
	 * Sets the checks if is killed already.
	 * @param value the new checks if is killed already
	 */
	public final void setIsKilledAlready(final boolean value)
	{
		isKilledAlready = value;
	}
	
	/**
	 * Checks if is muted.
	 * @return true, if is muted
	 */
	public final boolean isMuted()
	{
		return isMuted;
	}
	
	/**
	 * Sets the checks if is muted.
	 * @param value the new checks if is muted
	 */
	public final void setIsMuted(final boolean value)
	{
		isMuted = value;
	}
	
	/**
	 * Checks if is psychical muted.
	 * @return true, if is psychical muted
	 */
	public final boolean isPsychicalMuted()
	{
		return isPsychicalMuted;
	}
	
	/**
	 * Sets the checks if is psychical muted.
	 * @param value the new checks if is psychical muted
	 */
	public final void setIsPsychicalMuted(final boolean value)
	{
		isPsychicalMuted = value;
	}
	
	/**
	 * Return True if the L2Character can't move (stun, root, sleep, overload, paralyzed).
	 * @return true, if is movement disabled
	 */
	public boolean isMovementDisabled()
	{
		return isImmobileUntilAttacked() || isStunned() || isRooted() || isSleeping() || isOverloaded() || isParalyzed() || isImobilised() || isFakeDeath() || isFallsdown();
	}
	
	/**
	 * Return True if the L2Character can be controlled by the player (confused, afraid).
	 * @return true, if is out of control
	 */
	public final boolean isOutOfControl()
	{
		return isConfused() || isAfraid() || isBlocked();
	}
	
	/**
	 * Checks if is overloaded.
	 * @return true, if is overloaded
	 */
	public final boolean isOverloaded()
	{
		return isOverloaded;
	}
	
	/**
	 * Set the overloaded status of the L2Character is overloaded (if True, the L2PcInstance can't take more item).
	 * @param value the new checks if is overloaded
	 */
	public final void setIsOverloaded(final boolean value)
	{
		isOverloaded = value;
	}
	
	/**
	 * Checks if is paralyzed.
	 * @return true, if is paralyzed
	 */
	public final boolean isParalyzed()
	{
		return isParalyzed;
	}
	
	/**
	 * Sets the checks if is paralyzed.
	 * @param value the new checks if is paralyzed
	 */
	public final void setIsParalyzed(final boolean value)
	{
		if (petrified)
		{
			return;
		}
		isParalyzed = value;
	}
	
	/**
	 * Checks if is pending revive.
	 * @return true, if is pending revive
	 */
	public final boolean isPendingRevive()
	{
		return isDead() && isPendingRevive;
	}
	
	/**
	 * Sets the checks if is pending revive.
	 * @param value the new checks if is pending revive
	 */
	public final void setIsPendingRevive(final boolean value)
	{
		isPendingRevive = value;
	}
	
	/**
	 * Return the L2Summon of the L2Character.<BR>
	 * <BR>
	 * <B><U> Overriden in </U> :</B><BR>
	 * <BR>
	 * <li>L2PcInstance</li><BR>
	 * <BR>
	 * @return the pet
	 */
	public L2Summon getPet()
	{
		return null;
	}
	
	/**
	 * Return True if the L2Character is ridding.
	 * @return true, if is riding
	 */
	public final boolean isRiding()
	{
		return isRiding;
	}
	
	/**
	 * Set the L2Character riding mode to True.
	 * @param mode the new checks if is riding
	 */
	public final void setIsRiding(final boolean mode)
	{
		isRiding = mode;
	}
	
	/**
	 * Checks if is rooted.
	 * @return true, if is rooted
	 */
	public final boolean isRooted()
	{
		return isRooted;
	}
	
	/**
	 * Sets the checks if is rooted.
	 * @param value the new checks if is rooted
	 */
	public final void setIsRooted(final boolean value)
	{
		isRooted = value;
	}
	
	/**
	 * Return True if the L2Character is running.
	 * @return true, if is running
	 */
	public final boolean isRunning()
	{
		return isRunning;
	}
	
	/**
	 * Sets the checks if is running.
	 * @param value the new checks if is running
	 */
	public final void setIsRunning(final boolean value)
	{
		isRunning = value;
		broadcastPacket(new ChangeMoveType(this));
	}
	
	/**
	 * Set the L2Character movement type to run and send Server->Client packet ChangeMoveType to all others L2PcInstance.
	 */
	public final void setRunning()
	{
		if (!isRunning())
		{
			setIsRunning(true);
		}
	}
	
	/**
	 * Checks if is immobile until attacked.
	 * @return true, if is immobile until attacked
	 */
	public final boolean isImmobileUntilAttacked()
	{
		return isImmobileUntilAttacked;
	}
	
	/**
	 * Sets the checks if is immobile until attacked.
	 * @param value the new checks if is immobile until attacked
	 */
	public final void setIsImmobileUntilAttacked(final boolean value)
	{
		isImmobileUntilAttacked = value;
	}
	
	/**
	 * Checks if is sleeping.
	 * @return true, if is sleeping
	 */
	public final boolean isSleeping()
	{
		return isSleeping;
	}
	
	/**
	 * Sets the checks if is sleeping.
	 * @param value the new checks if is sleeping
	 */
	public final void setIsSleeping(final boolean value)
	{
		isSleeping = value;
	}
	
	/**
	 * Checks if is stunned.
	 * @return true, if is stunned
	 */
	public final boolean isStunned()
	{
		return isStunned;
	}
	
	/**
	 * Sets the checks if is stunned.
	 * @param value the new checks if is stunned
	 */
	public final void setIsStunned(final boolean value)
	{
		isStunned = value;
	}
	
	/**
	 * Checks if is betrayed.
	 * @return true, if is betrayed
	 */
	public final boolean isBetrayed()
	{
		return isBetrayed;
	}
	
	/**
	 * Sets the checks if is betrayed.
	 * @param value the new checks if is betrayed
	 */
	public final void setIsBetrayed(final boolean value)
	{
		isBetrayed = value;
	}
	
	/**
	 * Checks if is teleporting.
	 * @return true, if is teleporting
	 */
	public final boolean isTeleporting()
	{
		return isTeleporting;
	}
	
	/**
	 * Sets the checks if is teleporting.
	 * @param value the new checks if is teleporting
	 */
	public void setIsTeleporting(final boolean value)
	{
		isTeleporting = value;
	}
	
	/**
	 * Sets the checks if is invul.
	 * @param b the new checks if is invul
	 */
	public void setIsInvul(final boolean b)
	{
		if (petrified)
		{
			return;
		}
		
		isInvul = b;
	}
	
	/**
	 * Checks if is invul.
	 * @return true, if is invul
	 */
	public boolean isInvul()
	{
		return isInvul || isTeleporting;
	}
	
	/**
	 * Checks if is undead.
	 * @return true, if is undead
	 */
	public boolean isUndead()
	{
		return templateCharacter.isUndead;
	}
	
	@Override
	public CharKnownList getKnownList()
	{
		if (super.getKnownList() == null || !(super.getKnownList() instanceof CharKnownList))
		{
			setKnownList(new CharKnownList(this));
		}
		
		return (CharKnownList) super.getKnownList();
	}
	
	/**
	 * Gets the stat.
	 * @return the stat
	 */
	public CharStat getStat()
	{
		if (stat == null)
		{
			stat = new CharStat(this);
		}
		
		return stat;
	}
	
	/**
	 * Sets the stat.
	 * @param value the new stat
	 */
	public final void setStat(final CharStat value)
	{
		stat = value;
	}
	
	/**
	 * Gets the status.
	 * @return the status
	 */
	public CharStatus getStatus()
	{
		if (status == null)
		{
			status = new CharStatus(this);
		}
		
		return status;
	}
	
	/**
	 * Sets the status.
	 * @param value the new status
	 */
	public final void setStatus(final CharStatus value)
	{
		status = value;
	}
	
	/**
	 * Gets the template.
	 * @return the template
	 */
	public L2CharTemplate getTemplate()
	{
		return templateCharacter;
	}
	
	/**
	 * Set the template of the L2Character.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * Each L2Character owns generic and static properties (ex : all Keltir have the same number of HP...). All of those properties are stored in a different template for each type of L2Character. Each template is loaded once in the server cache memory (reduce memory use). When a new instance of
	 * L2Character is spawned, server just create a link between the instance and the template This link is stored in <B>_template</B><BR>
	 * <BR>
	 * <B><U> Assert </U> :</B><BR>
	 * <BR>
	 * <li>this instanceof L2Character</li><BR>
	 * <BR
	 * @param template the new template
	 */
	protected synchronized final void setTemplate(final L2CharTemplate template)
	{
		templateCharacter = template;
	}
	
	/**
	 * Return the Title of the L2Character.
	 * @return the title
	 */
	public final String getTitle()
	{
		if (title == null)
		{
			return "";
		}
		
		return title;
	}
	
	/**
	 * Set the Title of the L2Character.
	 * @param value the new title
	 */
	public final void setTitle(String value)
	{
		if (value == null)
		{
			value = "";
		}
		
		if (this instanceof L2PcInstance && value.length() > 16)
		{
			value = value.substring(0, 15);
		}
		
		title = value; // public final void setTitle(String value) { title = value; }
	}
	
	/**
	 * Set the L2Character movement type to walk and send Server->Client packet ChangeMoveType to all others L2PcInstance.
	 */
	public final void setWalking()
	{
		if (isRunning())
		{
			setIsRunning(false);
		}
	}
	
	/**
	 * Task lauching the function enableSkill().
	 */
	class EnableSkill implements Runnable
	{
		
		L2Skill skillId;
		
		/**
		 * Instantiates a new enable skill.
		 * @param skill the skill
		 */
		public EnableSkill(final L2Skill skill)
		{
			skillId = skill;
		}
		
		@Override
		public void run()
		{
			try
			{
				enableSkill(skillId);
			}
			catch (final Throwable e)
			{
				LOGGER.error("", e);
			}
		}
	}
	
	/**
	 * Task lauching the function onHitTimer().<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>If the attacker/target is dead or use fake death, notify the AI with EVT_CANCEL and send a Server->Client packet ActionFailed (if attacker is a L2PcInstance)</li>
	 * <li>If attack isn't aborted, send a message system (critical hit, missed...) to attacker/target if they are L2PcInstance</li>
	 * <li>If attack isn't aborted and hit isn't missed, reduce HP of the target and calculate reflection damage to reduce HP of attacker if necessary</li>
	 * <li>if attack isn't aborted and hit isn't missed, manage attack or cast break of the target (calculating rate, sending message...)</li><BR>
	 * <BR>
	 */
	class HitTask implements Runnable
	{
		L2Character hitTarget;
		int damage;
		boolean crit;
		boolean miss;
		boolean shld;
		boolean soulshot;
		
		/**
		 * Instantiates a new hit task.
		 * @param target   the target
		 * @param damage   the damage
		 * @param crit     the crit
		 * @param miss     the miss
		 * @param soulshot the soulshot
		 * @param shld     the shld
		 */
		public HitTask(final L2Character target, final int damage, final boolean crit, final boolean miss, final boolean soulshot, final boolean shld)
		{
			hitTarget = target;
			this.damage = damage;
			this.crit = crit;
			this.shld = shld;
			this.miss = miss;
			this.soulshot = soulshot;
		}
		
		@Override
		public void run()
		{
			try
			{
				onHitTimer(hitTarget, damage, crit, miss, soulshot, shld);
			}
			catch (final Throwable e)
			{
				LOGGER.error("fixme:hit task unhandled exception", e);
			}
		}
	}
	
	/**
	 * Task lauching the magic skill phases.
	 */
	class MagicUseTask implements Runnable
	{
		L2Object[] targets;
		L2Skill skill;
		int coolTime;
		int phase;
		
		/**
		 * Instantiates a new magic use task.
		 * @param targets  the targets
		 * @param skill    the skill
		 * @param coolTime the cool time
		 * @param phase    the phase
		 */
		public MagicUseTask(final L2Object[] targets, final L2Skill skill, final int coolTime, final int phase)
		{
			this.targets = targets;
			this.skill = skill;
			this.coolTime = coolTime;
			this.phase = phase;
		}
		
		@Override
		public void run()
		{
			try
			{
				switch (phase)
				{
					case 1:
						onMagicLaunchedTimer(targets, skill, coolTime, false);
						break;
					case 2:
						onMagicHitTimer(targets, skill, coolTime, false);
						break;
					case 3:
						onMagicFinalizer(targets, skill);
						break;
					default:
						break;
				}
			}
			catch (final Throwable e)
			{
				LOGGER.error("", e);
				e.printStackTrace();
				enableAllSkills();
			}
		}
	}
	
	/**
	 * Task lauching the function useMagic().
	 */
	class QueuedMagicUseTask implements Runnable
	{
		L2PcInstance currPlayer;
		L2Skill queuedSkill;
		boolean isCtrlPressed;
		boolean isShiftPressed;
		
		/**
		 * Instantiates a new queued magic use task.
		 * @param currPlayer     the curr player
		 * @param queuedSkill    the queued skill
		 * @param isCtrlPressed  the is ctrl pressed
		 * @param isShiftPressed the is shift pressed
		 */
		public QueuedMagicUseTask(final L2PcInstance currPlayer, final L2Skill queuedSkill, final boolean isCtrlPressed, final boolean isShiftPressed)
		{
			this.currPlayer = currPlayer;
			this.queuedSkill = queuedSkill;
			this.isCtrlPressed = isCtrlPressed;
			this.isShiftPressed = isShiftPressed;
		}
		
		@Override
		public void run()
		{
			try
			{
				currPlayer.useMagic(queuedSkill, isCtrlPressed, isShiftPressed);
			}
			catch (final Throwable e)
			{
				LOGGER.error("", e);
			}
		}
	}
	
	/**
	 * Task of AI notification.
	 */
	public class NotifyAITask implements Runnable
	{
		private final CtrlEvent evt;
		
		/**
		 * Instantiates a new notify ai task.
		 * @param evt the evt
		 */
		NotifyAITask(final CtrlEvent evt)
		{
			this.evt = evt;
		}
		
		@Override
		public void run()
		{
			try
			{
				getAI().notifyEvent(evt, null);
			}
			catch (final Throwable t)
			{
				LOGGER.warn("", t);
			}
		}
	}
	
	/**
	 * Task lauching the function stopPvPFlag().
	 */
	class PvPFlag implements Runnable
	{
		
		/**
		 * Instantiates a new pvp flag.
		 */
		public PvPFlag()
		{
			// null
		}
		
		@Override
		public void run()
		{
			try
			{
				// LOGGER.fine("Checking pvp time: " + getlastPvpAttack());
				// "lastattack: " lastAttackTime "currenttime: "
				// System.currentTimeMillis());
				if (System.currentTimeMillis() > getPvpFlagLasts())
				{
					// LOGGER.fine("Stopping PvP");
					stopPvPFlag();
				}
				else if (System.currentTimeMillis() > getPvpFlagLasts() - 5000)
				{
					updatePvPFlag(2);
				}
				else
				{
					updatePvPFlag(1);
					// Start a new PvP timer check
					// checkPvPFlag();
				}
			}
			catch (final Exception e)
			{
				LOGGER.warn("error in pvp flag task:", e);
			}
		}
	}
	
	/** Map 32 bits (0x0000) containing all abnormal effect in progress. */
	private int abnormalEffects;
	
	/**
	 * FastTable containing all active skills effects in progress of a L2Character.
	 */
	private final FastTable<L2Effect> effectsTable = new FastTable<>();
	
	/** The table containing the List of all stacked effect in progress for each Stack group Identifier. */
	protected Map<String, List<L2Effect>> stackedEffects = new HashMap<>();
	
	public static final int ABNORMAL_EFFECT_BLEEDING = 0x000001;
	public static final int ABNORMAL_EFFECT_POISON = 0x000002;
	public static final int ABNORMAL_EFFECT_REDCIRCLE = 0x000004;
	public static final int ABNORMAL_EFFECT_ICE = 0x000008;
	public static final int ABNORMAL_EFFECT_WIND = 0x0000010;
	public static final int ABNORMAL_EFFECT_FEAR = 0x0000020;
	public static final int ABNORMAL_EFFECT_STUN = 0x000040;
	public static final int ABNORMAL_EFFECT_SLEEP = 0x000080;
	public static final int ABNORMAL_EFFECT_MUTED = 0x000100;
	public static final int ABNORMAL_EFFECT_ROOT = 0x000200;
	public static final int ABNORMAL_EFFECT_HOLD_1 = 0x000400;
	public static final int ABNORMAL_EFFECT_HOLD_2 = 0x000800;
	public static final int ABNORMAL_EFFECT_UNKNOWN_13 = 0x001000;
	public static final int ABNORMAL_EFFECT_BIG_HEAD = 0x002000;
	public static final int ABNORMAL_EFFECT_FLAME = 0x004000;
	public static final int ABNORMAL_EFFECT_UNKNOWN_16 = 0x008000;
	public static final int ABNORMAL_EFFECT_GROW = 0x010000;
	public static final int ABNORMAL_EFFECT_FLOATING_ROOT = 0x020000;
	public static final int ABNORMAL_EFFECT_DANCE_STUNNED = 0x040000;
	public static final int ABNORMAL_EFFECT_PARALYZE = 0x0400;
	public static final int ABNORMAL_EFFECT_FIREROOT_STUN = 0x080000;
	public static final int ABNORMAL_EFFECT_STEALTH = 0x100000;
	public static final int ABNORMAL_EFFECT_IMPRISIONING_1 = 0x200000;
	public static final int ABNORMAL_EFFECT_IMPRISIONING_2 = 0x400000;
	public static final int ABNORMAL_EFFECT_MAGIC_CIRCLE = 0x800000;
	public static final int ABNORMAL_EFFECT_CONFUSED = 0x0020;
	public static final int ABNORMAL_EFFECT_AFRAID = 0x0010;
	
	/**
	 * Launch and add L2Effect (including Stack Group management) to L2Character and update client magic icone.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * All active skills effects in progress on the L2Character are identified in ConcurrentHashMap(Integer,L2Effect) <B>_effects</B>. The Integer key of effects is the L2Skill Identifier that has created the L2Effect.<BR>
	 * <BR>
	 * Several same effect can't be used on a L2Character at the same time. Indeed, effects are not stackable and the last cast will replace the previous in progress. More, some effects belong to the same Stack Group (ex WindWald and Haste Potion). If 2 effects of a same group are used at the same time
	 * on a L2Character, only the more efficient (identified by its priority order) will be preserve.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Add the L2Effect to the L2Character effects</li>
	 * <li>If this effect doesn't belong to a Stack Group, add its Funcs to the Calculator set of the L2Character (remove the old one if necessary)</li>
	 * <li>If this effect has higher priority in its Stack Group, add its Funcs to the Calculator set of the L2Character (remove previous stacked effect Funcs if necessary)</li>
	 * <li>If this effect has NOT higher priority in its Stack Group, set the effect to Not In Use</li>
	 * <li>Update active skills in progress icones on player client</li><BR>
	 * @param newEffect the new effect
	 */
	public synchronized void addEffect(final L2Effect newEffect)
	{
		if (newEffect == null)
		{
			return;
		}
		
		final L2Effect[] effects = getAllEffects();
		
		// Make sure there's no same effect previously
		for (final L2Effect effect : effects)
		{
			if (effect == null)
			{
				
				synchronized (effectsTable)
				{
					effectsTable.remove(effect);
				}
				continue;
			}
			
			if (effect.getSkill().getId() == newEffect.getSkill().getId() && effect.getEffectType() == newEffect.getEffectType() && effect.getStackType() == newEffect.getStackType())
			{
				if (this instanceof L2PcInstance)
				{
					
					final L2PcInstance player = (L2PcInstance) this;
					
					if (player.isInDuel())
					{
						DuelManager.getInstance().getDuel(player.getDuelId()).onBuffStop(player, effect);
					}
					
				}
				
				if ((newEffect.getSkill().getSkillType() == SkillType.BUFF || newEffect.getEffectType() == L2Effect.EffectType.BUFF || newEffect.getEffectType() == L2Effect.EffectType.HEAL_OVER_TIME) && newEffect.getStackOrder() >= effect.getStackOrder())
				{
					effect.exit(false);
				}
				else
				{
					// newEffect.exit(false);
					newEffect.stopEffectTask();
					return;
				}
			}
		}
		
		final L2Skill tempskill = newEffect.getSkill();
		
		// Remove first Buff if number of buffs > BUFFS_MAX_AMOUNT
		if (getBuffCount() >= getMaxBuffCount() && !doesStack(tempskill) && (tempskill.getSkillType() == L2Skill.SkillType.BUFF || tempskill.getSkillType() == L2Skill.SkillType.REFLECT || tempskill.getSkillType() == L2Skill.SkillType.HEAL_PERCENT || tempskill.getSkillType() == L2Skill.SkillType.MANAHEAL_PERCENT) && !(tempskill.getId() > 4360 && tempskill.getId() < 4367)
			&& !(tempskill.getId() > 4550 && tempskill.getId() < 4555))
		{
			if (newEffect.isHerbEffect())
			{
				newEffect.exit(false);
				return;
			}
			removeFirstBuff(tempskill.getId());
		}
		
		// Remove first DeBuff if number of debuffs > DEBUFFS_MAX_AMOUNT
		if (getDeBuffCount() >= Config.DEBUFFS_MAX_AMOUNT && !doesStack(tempskill) && tempskill.is_Debuff())
		{
			removeFirstDeBuff(tempskill.getId());
		}
		
		synchronized (effectsTable)
		{
			// Add the L2Effect to all effect in progress on the L2Character
			if (!newEffect.getSkill().isToggle())
			{
				int pos = 0;
				
				for (int i = 0; i < effectsTable.size(); i++)
				{
					if (effectsTable.get(i) == null)
					{
						effectsTable.remove(i);
						i--;
						continue;
					}
					
					if (effectsTable.get(i) != null)
					{
						final int skillid = effectsTable.get(i).getSkill().getId();
						
						if (!effectsTable.get(i).getSkill().isToggle() && !(skillid > 4360 && skillid < 4367))
						{
							pos++;
						}
					}
					else
					{
						break;
					}
				}
				effectsTable.add(pos, newEffect);
			}
			else
			{
				effectsTable.addLast(newEffect);
			}
			
		}
		
		// Check if a stack group is defined for this effect
		if (newEffect.getStackType().equals("none"))
		{
			// Set this L2Effect to In Use
			newEffect.setInUse(true);
			
			// Add Funcs of this effect to the Calculator set of the L2Character
			addStatFuncs(newEffect.getStatFuncs());
			
			// Update active skills in progress icones on player client
			updateEffectIcons();
			return;
		}
		
		// Get the list of all stacked effects corresponding to the stack type of the L2Effect to add
		List<L2Effect> stackQueue = stackedEffects.get(newEffect.getStackType());
		
		if (stackQueue == null)
		{
			stackQueue = new ArrayList<>();
		}
		
		// L2Effect tempEffect = null;
		
		if (stackQueue.size() > 0)
		{
			// Get the first stacked effect of the Stack group selected
			if (effectsTable.contains(stackQueue.get(0)))
			{
				// Remove all Func objects corresponding to this stacked effect from the Calculator set of the L2Character
				removeStatsOwner(stackQueue.get(0));
				
				// Set the L2Effect to Not In Use
				stackQueue.get(0).setInUse(false);
			}
		}
		
		// Add the new effect to the stack group selected at its position
		stackQueue = effectQueueInsert(newEffect, stackQueue);
		
		if (stackQueue == null)
		{
			return;
		}
		
		// Update the Stack Group table stackedEffects of the L2Character
		stackedEffects.put(newEffect.getStackType(), stackQueue);
		
		// Get the first stacked effect of the Stack group selected
		if (effectsTable.contains(stackQueue.get(0)))
		{
			// Set this L2Effect to In Use
			stackQueue.get(0).setInUse(true);
			
			// Add all Func objects corresponding to this stacked effect to the Calculator set of the L2Character
			addStatFuncs(stackQueue.get(0).getStatFuncs());
		}
		
		// Update active skills in progress (In Use and Not In Use because stacked) icones on client
		updateEffectIcons();
	}
	
	/**
	 * Insert an effect at the specified position in a Stack Group.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * Several same effect can't be used on a L2Character at the same time. Indeed, effects are not stackable and the last cast will replace the previous in progress. More, some effects belong to the same Stack Group (ex WindWald and Haste Potion). If 2 effects of a same group are used at the same time
	 * on a L2Character, only the more efficient (identified by its priority order) will be preserve.<BR>
	 * <BR>
	 * @param  newStackedEffect the new stacked effect
	 * @param  stackQueue       The Stack Group in wich the effect must be added
	 * @return                  the list
	 */
	private List<L2Effect> effectQueueInsert(final L2Effect newStackedEffect, final List<L2Effect> stackQueue)
	{
		// Create an Iterator to go through the list of stacked effects in progress on the L2Character
		Iterator<L2Effect> queueIterator = stackQueue.iterator();
		
		int i = 0;
		while (queueIterator.hasNext())
		{
			final L2Effect cur = queueIterator.next();
			if (newStackedEffect.getStackOrder() < cur.getStackOrder())
			{
				i++;
			}
			else
			{
				break;
			}
		}
		
		// Add the new effect to the Stack list in function of its position in the Stack group
		stackQueue.add(i, newStackedEffect);
		
		// skill.exit() could be used, if the users don't wish to see "effect
		// removed" always when a timer goes off, even if the buff isn't active
		// any more (has been replaced). but then check e.g. npc hold and raid petrify.
		if (Config.EFFECT_CANCELING && !newStackedEffect.isHerbEffect() && stackQueue.size() > 1)
		{
			synchronized (effectsTable)
			{
				
				effectsTable.remove(stackQueue.get(1));
				
			}
			
			stackQueue.remove(1);
		}
		
		queueIterator = null;
		
		return stackQueue;
	}
	
	/**
	 * Stop and remove L2Effect (including Stack Group management) from L2Character and update client magic icone.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * All active skills effects in progress on the L2Character are identified in ConcurrentHashMap(Integer,L2Effect) <B>_effects</B>. The Integer key of effects is the L2Skill Identifier that has created the L2Effect.<BR>
	 * <BR>
	 * Several same effect can't be used on a L2Character at the same time. Indeed, effects are not stackable and the last cast will replace the previous in progress. More, some effects belong to the same Stack Group (ex WindWald and Haste Potion). If 2 effects of a same group are used at the same time
	 * on a L2Character, only the more efficient (identified by its priority order) will be preserve.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Remove Func added by this effect from the L2Character Calculator (Stop L2Effect)</li>
	 * <li>If the L2Effect belongs to a not empty Stack Group, replace theses Funcs by next stacked effect Funcs</li>
	 * <li>Remove the L2Effect from effects of the L2Character</li>
	 * <li>Update active skills in progress icones on player client</li><BR>
	 * @param effect the effect
	 */
	public final void removeEffect(final L2Effect effect)
	{
		if (effect == null/* || effects == null */)
		{
			return;
		}
		
		if (effect.getStackType() == "none")
		{
			// Remove Func added by this effect from the L2Character Calculator
			removeStatsOwner(effect);
		}
		else
		{
			if (stackedEffects == null)
			{
				return;
			}
			
			// Get the list of all stacked effects corresponding to the stack type of the L2Effect to add
			final List<L2Effect> stackQueue = stackedEffects.get(effect.getStackType());
			
			if (stackQueue == null || stackQueue.size() < 1)
			{
				return;
			}
			
			// Get the Identifier of the first stacked effect of the Stack group selected
			final L2Effect frontEffect = stackQueue.get(0);
			
			// Remove the effect from the Stack Group
			final boolean removed = stackQueue.remove(effect);
			
			if (removed)
			{
				// Check if the first stacked effect was the effect to remove
				if (frontEffect == effect)
				{
					// Remove all its Func objects from the L2Character calculator set
					removeStatsOwner(effect);
					
					// Check if there's another effect in the Stack Group
					if (stackQueue.size() > 0)
					{
						// Add its list of Funcs to the Calculator set of the L2Character
						if (effectsTable.contains(stackQueue.get(0)))
						{
							
							// Add its list of Funcs to the Calculator set of the L2Character
							addStatFuncs(stackQueue.get(0).getStatFuncs());
							// Set the effect to In Use
							stackQueue.get(0).setInUse(true);
							
						}
						
					}
				}
				if (stackQueue.isEmpty())
				{
					stackedEffects.remove(effect.getStackType());
				}
				else
				{
					// Update the Stack Group table stackedEffects of the L2Character
					stackedEffects.put(effect.getStackType(), stackQueue);
				}
			}
			
		}
		
		synchronized (effectsTable)
		{
			// Remove the active skill L2effect from effects of the L2Character
			effectsTable.remove(effect);
			
		}
		
		// Update active skills in progress (In Use and Not In Use because stacked) icones on client
		updateEffectIcons();
	}
	
	/**
	 * Active abnormal effects flags in the binary mask and send Server->Client UserInfo/CharInfo packet.<BR>
	 * <BR>
	 * @param mask the mask
	 */
	public final void startAbnormalEffect(final int mask)
	{
		abnormalEffects |= mask;
		updateAbnormalEffect();
	}
	
	/**
	 * immobile start.
	 */
	public final void startImmobileUntilAttacked()
	{
		setIsImmobileUntilAttacked(true);
		abortAttack();
		abortCast();
		getAI().notifyEvent(CtrlEvent.EVT_SLEEPING);
		updateAbnormalEffect();
	}
	
	/**
	 * Active the abnormal effect Confused flag, notify the L2Character AI and send Server->Client UserInfo/CharInfo packet.<BR>
	 * <BR>
	 */
	public final void startConfused()
	{
		setIsConfused(true);
		getAI().notifyEvent(CtrlEvent.EVT_CONFUSED);
		updateAbnormalEffect();
	}
	
	/**
	 * Active the abnormal effect Fake Death flag, notify the L2Character AI and send Server->Client UserInfo/CharInfo packet.<BR>
	 * <BR>
	 */
	public final void startFakeDeath()
	{
		setIsFallsdown(true);
		setIsFakeDeath(true);
		/* Aborts any attacks/casts if fake dead */
		abortAttack();
		abortCast();
		stopMove(null);
		getAI().notifyEvent(CtrlEvent.EVT_FAKE_DEATH, null);
		broadcastPacket(new ChangeWaitType(this, ChangeWaitType.WT_START_FAKEDEATH));
	}
	
	/**
	 * Active the abnormal effect Fear flag, notify the L2Character AI and send Server->Client UserInfo/CharInfo packet.<BR>
	 * <BR>
	 */
	public final void startFear()
	{
		setIsAfraid(true);
		getAI().notifyEvent(CtrlEvent.EVT_AFFRAID);
		updateAbnormalEffect();
	}
	
	/**
	 * Active the abnormal effect Muted flag, notify the L2Character AI and send Server->Client UserInfo/CharInfo packet.<BR>
	 * <BR>
	 */
	public final void startMuted()
	{
		setIsMuted(true);
		/* Aborts any casts if muted */
		abortCast();
		getAI().notifyEvent(CtrlEvent.EVT_MUTED);
		updateAbnormalEffect();
	}
	
	/**
	 * Active the abnormal effect Psychical_Muted flag, notify the L2Character AI and send Server->Client UserInfo/CharInfo packet.<BR>
	 * <BR>
	 */
	public final void startPsychicalMuted()
	{
		setIsPsychicalMuted(true);
		getAI().notifyEvent(CtrlEvent.EVT_MUTED);
		updateAbnormalEffect();
	}
	
	/**
	 * Active the abnormal effect Root flag, notify the L2Character AI and send Server->Client UserInfo/CharInfo packet.<BR>
	 * <BR>
	 */
	public final void startRooted()
	{
		setIsRooted(true);
		stopMove(null);
		getAI().notifyEvent(CtrlEvent.EVT_ROOTED, null);
		updateAbnormalEffect();
	}
	
	/**
	 * Active the abnormal effect Sleep flag, notify the L2Character AI and send Server->Client UserInfo/CharInfo packet.<BR>
	 * <BR>
	 */
	public final void startSleeping()
	{
		setIsSleeping(true);
		/* Aborts any attacks/casts if sleeped */
		abortAttack();
		abortCast();
		stopMove(null);
		getAI().notifyEvent(CtrlEvent.EVT_SLEEPING, null);
		updateAbnormalEffect();
	}
	
	/**
	 * Launch a Stun Abnormal Effect on the L2Character.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Calculate the success rate of the Stun Abnormal Effect on this L2Character</li>
	 * <li>If Stun succeed, active the abnormal effect Stun flag, notify the L2Character AI and send Server->Client UserInfo/CharInfo packet</li>
	 * <li>If Stun NOT succeed, send a system message Failed to the L2PcInstance attacker</li><BR>
	 * <BR>
	 */
	public final void startStunning()
	{
		if (isStunned())
		{
			return;
		}
		
		setIsStunned(true);
		/* Aborts any attacks/casts if stunned */
		abortAttack();
		abortCast();
		getAI().stopFollow(); // Like L2OFF char stop to follow if sticked to another one
		stopMove(null);
		getAI().notifyEvent(CtrlEvent.EVT_STUNNED, null);
		updateAbnormalEffect();
	}
	
	/**
	 * Start betray.
	 */
	public final void startBetray()
	{
		setIsBetrayed(true);
		getAI().notifyEvent(CtrlEvent.EVT_BETRAYED, null);
		updateAbnormalEffect();
	}
	
	/**
	 * Stop betray.
	 */
	public final void stopBetray()
	{
		stopEffects(L2Effect.EffectType.BETRAY);
		setIsBetrayed(false);
		updateAbnormalEffect();
	}
	
	/**
	 * Modify the abnormal effect map according to the mask.<BR>
	 * <BR>
	 * @param mask the mask
	 */
	public final void stopAbnormalEffect(final int mask)
	{
		abnormalEffects &= ~mask;
		updateAbnormalEffect();
	}
	
	/**
	 * Stop all active skills effects in progress on the L2Character.<BR>
	 * <BR>
	 */
	public final void stopAllEffects()
	{
		
		final L2Effect[] effects = getAllEffects();
		
		for (final L2Effect effect : effects)
		{
			
			if (effect != null)
			{
				effect.exit(true);
			}
			else
			{
				synchronized (effectsTable)
				{
					effectsTable.remove(effect);
				}
			}
		}
		
		if (this instanceof L2PcInstance)
		{
			((L2PcInstance) this).updateAndBroadcastStatus(2);
		}
		
	}
	
	/**
	 * Stop immobilization until attacked abnormal L2Effect.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Delete a specified/all (if effect=null) immobilization until attacked abnormal L2Effect from L2Character and update client magic icon</li>
	 * <li>Set the abnormal effect flag muted to False</li>
	 * <li>Notify the L2Character AI</li>
	 * <li>Send Server->Client UserInfo/CharInfo packet</li><BR>
	 * <BR>
	 * @param effect the effect
	 */
	public final void stopImmobileUntilAttacked(final L2Effect effect)
	{
		if (effect == null)
		{
			stopEffects(L2Effect.EffectType.IMMOBILEUNTILATTACKED);
		}
		else
		{
			removeEffect(effect);
			stopSkillEffects(effect.getSkill().getNegateId());
		}
		
		setIsImmobileUntilAttacked(false);
		getAI().notifyEvent(CtrlEvent.EVT_THINK);
		updateAbnormalEffect();
	}
	
	/**
	 * Stop a specified/all Confused abnormal L2Effect.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Delete a specified/all (if effect=null) Confused abnormal L2Effect from L2Character and update client magic icone</li>
	 * <li>Set the abnormal effect flag confused to False</li>
	 * <li>Notify the L2Character AI</li>
	 * <li>Send Server->Client UserInfo/CharInfo packet</li><BR>
	 * <BR>
	 * @param effect the effect
	 */
	public final void stopConfused(final L2Effect effect)
	{
		if (effect == null)
		{
			stopEffects(L2Effect.EffectType.CONFUSION);
		}
		else
		{
			removeEffect(effect);
		}
		
		setIsConfused(false);
		getAI().notifyEvent(CtrlEvent.EVT_THINK, null);
		updateAbnormalEffect();
	}
	
	/**
	 * Stop and remove the L2Effects corresponding to the L2Skill Identifier and update client magic icone.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * All active skills effects in progress on the L2Character are identified in ConcurrentHashMap(Integer,L2Effect) <B>_effects</B>. The Integer key of effects is the L2Skill Identifier that has created the L2Effect.<BR>
	 * <BR>
	 * @param skillId the skill id
	 */
	public final void stopSkillEffects(final int skillId)
	{
		final L2Effect[] effects = getAllEffects();
		
		for (final L2Effect effect : effects)
		{
			
			if (effect == null || effect.getSkill() == null)
			{
				
				synchronized (effectsTable)
				{
					effectsTable.remove(effect);
				}
				continue;
				
			}
			
			if (effect.getSkill().getId() == skillId)
			{
				effect.exit(true);
			}
			
		}
		
	}
	
	/**
	 * Stop and remove all L2Effect of the selected type (ex : BUFF, DMG_OVER_TIME...) from the L2Character and update client magic icone.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * All active skills effects in progress on the L2Character are identified in ConcurrentHashMap(Integer,L2Effect) <B>_effects</B>. The Integer key of effects is the L2Skill Identifier that has created the L2Effect.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Remove Func added by this effect from the L2Character Calculator (Stop L2Effect)</li>
	 * <li>Remove the L2Effect from effects of the L2Character</li>
	 * <li>Update active skills in progress icones on player client</li><BR>
	 * <BR>
	 * @param type The type of effect to stop ((ex : BUFF, DMG_OVER_TIME...)
	 */
	public final void stopEffects(final L2Effect.EffectType type)
	{
		final L2Effect[] effects = getAllEffects();
		
		for (final L2Effect effect : effects)
		{
			
			if (effect == null)
			{
				
				synchronized (effectsTable)
				{
					effectsTable.remove(effect);
				}
				continue;
				
			}
			
			// LOGGER.info("Character Effect Type: "+effects[i].getEffectType());
			if (effect.getEffectType() == type)
			{
				effect.exit(true);
			}
			
		}
		
	}
	
	/**
	 * Stop and remove the L2Effects corresponding to the L2SkillType and update client magic icon.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * All active skills effects in progress on the L2Character are identified in ConcurrentHashMap(Integer,L2Effect) <B>_effects</B>. The Integer key of effects is the L2Skill Identifier that has created the L2Effect.<BR>
	 * <BR>
	 * @param skillType The L2SkillType of the L2Effect to remove from effects
	 * @param power     the power
	 */
	public final void stopSkillEffects(final SkillType skillType, final double power)
	{
		final L2Effect[] effects = getAllEffects();
		
		for (final L2Effect effect : effects)
		{
			
			if (effect == null || effect.getSkill() == null)
			{
				
				synchronized (effectsTable)
				{
					effectsTable.remove(effect);
				}
				continue;
				
			}
			
			if (effect.getSkill().getSkillType() == skillType && (power == 0 || effect.getSkill().getPower() <= power))
			{
				effect.exit(true);
			}
			
		}
		
	}
	
	/**
	 * Stop skill effects.
	 * @param skillType the skill type
	 */
	public final void stopSkillEffects(final SkillType skillType)
	{
		stopSkillEffects(skillType, -1);
	}
	
	/**
	 * Stop a specified/all Fake Death abnormal L2Effect.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Delete a specified/all (if effect=null) Fake Death abnormal L2Effect from L2Character and update client magic icone</li>
	 * <li>Set the abnormal effect flag fake_death to False</li>
	 * <li>Notify the L2Character AI</li><BR>
	 * <BR>
	 * @param effect the effect
	 */
	public final void stopFakeDeath(final L2Effect effect)
	{
		if (effect == null)
		{
			stopEffects(L2Effect.EffectType.FAKE_DEATH);
		}
		else
		{
			removeEffect(effect);
		}
		
		setIsFakeDeath(false);
		setIsFallsdown(false);
		// if this is a player instance, start the grace period for this character (grace from mobs only)!
		if (this instanceof L2PcInstance)
		{
			((L2PcInstance) this).setRecentFakeDeath(true);
		}
		
		ChangeWaitType revive = new ChangeWaitType(this, ChangeWaitType.WT_STOP_FAKEDEATH);
		broadcastPacket(revive);
		broadcastPacket(new Revive(this));
		getAI().notifyEvent(CtrlEvent.EVT_THINK, null);
		
		revive = null;
	}
	
	/**
	 * Stop a specified/all Fear abnormal L2Effect.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Delete a specified/all (if effect=null) Fear abnormal L2Effect from L2Character and update client magic icone</li>
	 * <li>Set the abnormal effect flag affraid to False</li>
	 * <li>Notify the L2Character AI</li>
	 * <li>Send Server->Client UserInfo/CharInfo packet</li><BR>
	 * <BR>
	 * @param effect the effect
	 */
	public final void stopFear(final L2Effect effect)
	{
		if (effect == null)
		{
			stopEffects(L2Effect.EffectType.FEAR);
		}
		else
		{
			removeEffect(effect);
		}
		
		setIsAfraid(false);
		updateAbnormalEffect();
	}
	
	/**
	 * Stop a specified/all Muted abnormal L2Effect.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Delete a specified/all (if effect=null) Muted abnormal L2Effect from L2Character and update client magic icone</li>
	 * <li>Set the abnormal effect flag muted to False</li>
	 * <li>Notify the L2Character AI</li>
	 * <li>Send Server->Client UserInfo/CharInfo packet</li><BR>
	 * <BR>
	 * @param effect the effect
	 */
	public final void stopMuted(final L2Effect effect)
	{
		if (effect == null)
		{
			stopEffects(L2Effect.EffectType.MUTE);
		}
		else
		{
			removeEffect(effect);
		}
		
		setIsMuted(false);
		updateAbnormalEffect();
	}
	
	/**
	 * Stop psychical muted.
	 * @param effect the effect
	 */
	public final void stopPsychicalMuted(final L2Effect effect)
	{
		if (effect == null)
		{
			stopEffects(L2Effect.EffectType.PSYCHICAL_MUTE);
		}
		else
		{
			removeEffect(effect);
		}
		
		setIsPsychicalMuted(false);
		updateAbnormalEffect();
	}
	
	/**
	 * Stop a specified/all Root abnormal L2Effect.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Delete a specified/all (if effect=null) Root abnormal L2Effect from L2Character and update client magic icone</li>
	 * <li>Set the abnormal effect flag rooted to False</li>
	 * <li>Notify the L2Character AI</li>
	 * <li>Send Server->Client UserInfo/CharInfo packet</li><BR>
	 * <BR>
	 * @param effect the effect
	 */
	public final void stopRooting(final L2Effect effect)
	{
		if (effect == null)
		{
			stopEffects(L2Effect.EffectType.ROOT);
		}
		else
		{
			removeEffect(effect);
		}
		
		setIsRooted(false);
		getAI().notifyEvent(CtrlEvent.EVT_THINK, null);
		updateAbnormalEffect();
	}
	
	/**
	 * Stop a specified/all Sleep abnormal L2Effect.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Delete a specified/all (if effect=null) Sleep abnormal L2Effect from L2Character and update client magic icone</li>
	 * <li>Set the abnormal effect flag sleeping to False</li>
	 * <li>Notify the L2Character AI</li>
	 * <li>Send Server->Client UserInfo/CharInfo packet</li><BR>
	 * <BR>
	 * @param effect the effect
	 */
	public final void stopSleeping(final L2Effect effect)
	{
		if (effect == null)
		{
			stopEffects(L2Effect.EffectType.SLEEP);
		}
		else
		{
			removeEffect(effect);
		}
		
		setIsSleeping(false);
		getAI().notifyEvent(CtrlEvent.EVT_THINK, null);
		updateAbnormalEffect();
	}
	
	/**
	 * Stop a specified/all Stun abnormal L2Effect.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Delete a specified/all (if effect=null) Stun abnormal L2Effect from L2Character and update client magic icone</li>
	 * <li>Set the abnormal effect flag stuned to False</li>
	 * <li>Notify the L2Character AI</li>
	 * <li>Send Server->Client UserInfo/CharInfo packet</li><BR>
	 * <BR>
	 * @param effect the effect
	 */
	public final void stopStunning(final L2Effect effect)
	{
		if (!isStunned())
		{
			return;
		}
		
		if (effect == null)
		{
			stopEffects(L2Effect.EffectType.STUN);
		}
		else
		{
			removeEffect(effect);
		}
		
		setIsStunned(false);
		getAI().notifyEvent(CtrlEvent.EVT_THINK, null);
		updateAbnormalEffect();
	}
	
	/**
	 * Not Implemented.<BR>
	 * <BR>
	 * <B><U> Overridden in</U> :</B><BR>
	 * <BR>
	 * <li>L2NPCInstance</li>
	 * <li>L2PcInstance</li>
	 * <li>L2Summon</li>
	 * <li>L2DoorInstance</li><BR>
	 * <BR>
	 */
	public abstract void updateAbnormalEffect();
	
	/**
	 * Update active skills in progress (In Use and Not In Use because stacked) icones on client.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * All active skills effects in progress (In Use and Not In Use because stacked) are represented by an icone on the client.<BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method ONLY UPDATE the client of the player and not clients of all players in the party.</B></FONT><BR>
	 * <BR>
	 */
	public final void updateEffectIcons()
	{
		updateEffectIcons(false);
	}
	
	/**
	 * Update effect icons.
	 * @param partyOnly the party only
	 */
	public final void updateEffectIcons(final boolean partyOnly)
	{
		// Create a L2PcInstance of this if needed
		L2PcInstance player = null;
		
		if (this instanceof L2PcInstance)
		{
			player = (L2PcInstance) this;
		}
		
		// Create a L2Summon of this if needed
		L2Summon summon = null;
		if (this instanceof L2Summon)
		{
			summon = (L2Summon) this;
			player = summon.getOwner();
			summon.getOwner().sendPacket(new PetInfo(summon));
		}
		
		// Create the main packet if needed
		MagicEffectIcons mi = null;
		if (!partyOnly)
		{
			mi = new MagicEffectIcons();
		}
		
		// Create the party packet if needed
		PartySpelled ps = null;
		if (summon != null)
		{
			ps = new PartySpelled(summon);
		}
		else if (player != null && player.isInParty())
		{
			ps = new PartySpelled(player);
		}
		
		// Create the olympiad spectator packet if needed
		ExOlympiadSpelledInfo os = null;
		if (player != null && player.isInOlympiadMode())
		{
			os = new ExOlympiadSpelledInfo(player);
		}
		
		if (mi == null && ps == null && os == null)
		{
			return; // nothing to do (should not happen)
		}
		
		// Add special effects
		// Note: Now handled by EtcStatusUpdate packet
		// NOTE: CHECK IF THEY WERE EVEN VISIBLE TO OTHERS...
		/*
		 * if (player != null && mi != null) { if (player.getWeightPenalty() > 0) mi.addEffect(4270, player.getWeightPenalty(), -1); if (player.getExpertisePenalty() > 0) mi.addEffect(4267, 1, -1); if (player.getMessageRefusal()) mi.addEffect(4269, 1, -1); }
		 */
		
		// Go through all effects if any
		synchronized (effectsTable)
		{
			
			for (int i = 0; i < effectsTable.size(); i++)
			{
				
				if (effectsTable.get(i) == null || effectsTable.get(i).getSkill() == null)
				{
					
					effectsTable.remove(i);
					i--;
					continue;
					
				}
				
				if (effectsTable.get(i).getEffectType() == L2Effect.EffectType.CHARGE && player != null)
				{
					// handled by EtcStatusUpdate
					continue;
				}
				
				if (effectsTable.get(i).getInUse())
				{
					if (mi != null)
					{
						effectsTable.get(i).addIcon(mi);
					}
					// Like L2OFF toggle and healing potions must not be showed on party buff list
					if (ps != null && !effectsTable.get(i).getSkill().isToggle() && !(effectsTable.get(i).getSkill().getId() == 2031) && !(effectsTable.get(i).getSkill().getId() == 2037) && !(effectsTable.get(i).getSkill().getId() == 2032))
					{
						effectsTable.get(i).addPartySpelledIcon(ps);
					}
					if (os != null)
					{
						effectsTable.get(i).addOlympiadSpelledIcon(os);
					}
				}
				
			}
			
		}
		
		// Send the packets if needed
		if (mi != null)
		{
			sendPacket(mi);
		}
		
		if (ps != null && player != null)
		{
			// summon info only needs to go to the owner, not to the whole party
			// player info: if in party, send to all party members except one's self.
			// if not in party, send to self.
			if (player.isInParty() && summon == null)
			{
				player.getParty().broadcastToPartyMembers(player, ps);
			}
			else
			{
				player.sendPacket(ps);
			}
		}
		
		if (os != null)
		{
			if (player != null && Olympiad.getInstance().getSpectators(player.getOlympiadGameId()) != null)
			{
				for (final L2PcInstance spectator : Olympiad.getInstance().getSpectators(player.getOlympiadGameId()))
				{
					if (spectator == null)
					{
						continue;
					}
					spectator.sendPacket(os);
				}
			}
		}
		
	}
	
	// Property - Public
	/**
	 * Return a map of 16 bits (0x0000) containing all abnormal effect in progress for this L2Character.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * In Server->Client packet, each effect is represented by 1 bit of the map (ex : BLEEDING = 0x0001 (bit 1), SLEEP = 0x0080 (bit 8)...). The map is calculated by applying a BINARY OR operation on each effect.<BR>
	 * <BR>
	 * <B><U> Example of use </U> :</B><BR>
	 * <BR>
	 * <li>Server Packet : CharInfo, NpcInfo, NpcInfoPoly, UserInfo...</li><BR>
	 * <BR>
	 * @return the abnormal effect
	 */
	public int getAbnormalEffect()
	{
		int ae = abnormalEffects;
		
		if (isStunned())
		{
			ae |= ABNORMAL_EFFECT_STUN;
		}
		if (isRooted())
		{
			ae |= ABNORMAL_EFFECT_ROOT;
		}
		if (isSleeping())
		{
			ae |= ABNORMAL_EFFECT_SLEEP;
		}
		if (isConfused())
		{
			ae |= ABNORMAL_EFFECT_CONFUSED;
		}
		if (isMuted())
		{
			ae |= ABNORMAL_EFFECT_MUTED;
		}
		if (isAfraid())
		{
			ae |= ABNORMAL_EFFECT_AFRAID;
		}
		if (isPsychicalMuted())
		{
			ae |= ABNORMAL_EFFECT_MUTED;
		}
		
		return ae;
	}
	
	/**
	 * Return all active skills effects in progress on the L2Character.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * All active skills effects in progress on the L2Character are identified in <B>_effects</B>. The Integer key of effects is the L2Skill Identifier that has created the effect.<BR>
	 * <BR>
	 * @return A table containing all active skills effect in progress on the L2Character
	 */
	public final L2Effect[] getAllEffects()
	{
		synchronized (effectsTable)
		{
			final L2Effect[] output = effectsTable.toArray(new L2Effect[effectsTable.size()]);
			
			return output;
		}
	}
	
	/**
	 * Return L2Effect in progress on the L2Character corresponding to the L2Skill Identifier.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * All active skills effects in progress on the L2Character are identified in <B>_effects</B>.
	 * @param  index The L2Skill Identifier of the L2Effect to return from the effects
	 * @return       The L2Effect corresponding to the L2Skill Identifier
	 */
	public final L2Effect getFirstEffect(final int index)
	{
		final L2Effect[] effects = getAllEffects();
		
		L2Effect effNotInUse = null;
		
		for (final L2Effect effect : effects)
		{
			
			if (effect == null)
			{
				synchronized (effectsTable)
				{
					effectsTable.remove(effect);
				}
				continue;
			}
			
			if (effect.getSkill().getId() == index)
			{
				if (effect.getInUse())
				{
					return effect;
				}
				
				if (effNotInUse == null)
				{
					effNotInUse = effect;
				}
			}
			
		}
		
		return effNotInUse;
		
	}
	
	/**
	 * Gets the first effect.
	 * @param  type the type
	 * @return      the first effect
	 */
	public final L2Effect getFirstEffect(final SkillType type)
	{
		
		final L2Effect[] effects = getAllEffects();
		
		L2Effect effNotInUse = null;
		
		for (final L2Effect effect : effects)
		{
			
			if (effect == null)
			{
				synchronized (effectsTable)
				{
					effectsTable.remove(effect);
				}
				continue;
			}
			
			if (effect.getSkill().getSkillType() == type)
			{
				if (effect.getInUse())
				{
					return effect;
				}
				
				if (effNotInUse == null)
				{
					effNotInUse = effect;
				}
			}
			
		}
		
		return effNotInUse;
		
	}
	
	/**
	 * Return the first L2Effect in progress on the L2Character created by the L2Skill.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * All active skills effects in progress on the L2Character are identified in <B>_effects</B>.
	 * @param  skill The L2Skill whose effect must be returned
	 * @return       The first L2Effect created by the L2Skill
	 */
	public final L2Effect getFirstEffect(final L2Skill skill)
	{
		final L2Effect[] effects = getAllEffects();
		
		L2Effect effNotInUse = null;
		
		for (final L2Effect effect : effects)
		{
			
			if (effect == null)
			{
				synchronized (effectsTable)
				{
					effectsTable.remove(effect);
				}
				continue;
			}
			
			if (effect.getSkill() == skill)
			{
				if (effect.getInUse())
				{
					return effect;
				}
				
				if (effNotInUse == null)
				{
					effNotInUse = effect;
				}
			}
			
		}
		
		return effNotInUse;
		
	}
	
	/**
	 * Return the first L2Effect in progress on the L2Character corresponding to the Effect Type (ex : BUFF, STUN, ROOT...).<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * All active skills effects in progress on the L2Character are identified in ConcurrentHashMap(Integer,L2Effect) <B>_effects</B>. The Integer key of effects is the L2Skill Identifier that has created the L2Effect.<BR>
	 * <BR>
	 * @param  tp The Effect Type of skills whose effect must be returned
	 * @return    The first L2Effect corresponding to the Effect Type
	 */
	public final L2Effect getFirstEffect(final L2Effect.EffectType tp)
	{
		final L2Effect[] effects = getAllEffects();
		
		L2Effect effNotInUse = null;
		
		for (final L2Effect effect : effects)
		{
			
			if (effect == null)
			{
				synchronized (effectsTable)
				{
					effectsTable.remove(effect);
				}
				continue;
			}
			
			if (effect.getEffectType() == tp)
			{
				if (effect.getInUse())
				{
					return effect;
				}
				
				if (effNotInUse == null)
				{
					effNotInUse = effect;
				}
			}
			
		}
		
		return effNotInUse;
		
	}
	
	public L2Effect getLastEffect()
	{
		return getAllEffects()[getAllEffects().length - 1];
	}
	
	/**
	 * Gets the charge effect.
	 * @return the charge effect
	 */
	public EffectCharge getChargeEffect()
	{
		
		final L2Effect effect = getFirstEffect(SkillType.CHARGE);
		if (effect != null)
		{
			return (EffectCharge) effect;
		}
		
		return null;
		
	}
	
	/**
	 * This class permit to the L2Character AI to obtain informations and uses L2Character method.
	 */
	public class AIAccessor
	{
		
		/**
		 * Instantiates a new aI accessor.
		 */
		public AIAccessor()
		{
			// null
		}
		
		/**
		 * Return the L2Character managed by this Accessor AI.<BR>
		 * <BR>
		 * @return the actor
		 */
		public L2Character getActor()
		{
			return L2Character.this;
		}
		
		/**
		 * Accessor to L2Character moveToLocation() method with an interaction area.<BR>
		 * <BR>
		 * @param x      the x
		 * @param y      the y
		 * @param z      the z
		 * @param offset the offset
		 */
		public void moveTo(final int x, final int y, final int z, final int offset)
		{
			moveToLocation(x, y, z, offset);
		}
		
		/**
		 * Accessor to L2Character moveToLocation() method without interaction area.<BR>
		 * <BR>
		 * @param x the x
		 * @param y the y
		 * @param z the z
		 */
		public void moveTo(final int x, final int y, final int z)
		{
			moveToLocation(x, y, z, 0);
		}
		
		/**
		 * Accessor to L2Character stopMove() method.<BR>
		 * <BR>
		 * @param pos the pos
		 */
		public void stopMove(final L2CharPosition pos)
		{
			L2Character.this.stopMove(pos);
		}
		
		/**
		 * Accessor to L2Character doAttack() method.<BR>
		 * <BR>
		 * @param target the target
		 */
		public void doAttack(final L2Character target)
		{
			L2Character.this.doAttack(target);
		}
		
		/**
		 * Accessor to L2Character doCast() method.<BR>
		 * <BR>
		 * @param skill the skill
		 */
		public void doCast(final L2Skill skill)
		{
			L2Character.this.doCast(skill);
		}
		
		/**
		 * Create a NotifyAITask.<BR>
		 * <BR>
		 * @param  evt the evt
		 * @return     the notify ai task
		 */
		public NotifyAITask newNotifyTask(final CtrlEvent evt)
		{
			return new NotifyAITask(evt);
		}
		
		/**
		 * Cancel the AI.<BR>
		 * <BR>
		 */
		public void detachAI()
		{
			aiCharacter = null;
		}
	}
	
	/**
	 * This class group all mouvement data.<BR>
	 * <BR>
	 * <B><U> Data</U> :</B><BR>
	 * <BR>
	 * <li>_moveTimestamp : Last time position update</li>
	 * <li>_xDestination, yDestination, zDestination : Position of the destination</li>
	 * <li>_xMoveFrom, yMoveFrom, zMoveFrom : Position of the origin</li>
	 * <li>_moveStartTime : Start time of the movement</li>
	 * <li>_ticksToMove : Nb of ticks between the start and the destination</li>
	 * <li>_xSpeedTicks, ySpeedTicks : Speed in unit/ticks</li><BR>
	 * <BR>
	 */
	public static class MoveData
	{
		// when we retrieve x/y/z we use GameTimeControl.getGameTicks()
		// if we are moving, but move timestamp==gameticks, we don't need
		// to recalculate position
		public int moveStartTime;
		public int moveTimestamp;
		public int xDestination;
		public int yDestination;
		public int zDestination;
		public double xAccurate;
		public double yAccurate;
		public double zAccurate;
		public int heading;
		public boolean disregardingGeodata;
		public int onGeodataPathIndex;
		public Node[] geoPath;
		public int geoPathAccurateTx;
		public int geoPathAccurateTy;
		public int geoPathGtx;
		public int geoPathGty;
	}
	
	protected List<Integer> disabledSkills;
	private boolean allSkillsDisabled;
	
	// private int flyingRunSpeed;
	// private int floatingWalkSpeed;
	// private int flyingWalkSpeed;
	// private int floatingRunSpeed;
	
	protected MoveData playerMove;
	private int headingNumber;
	private L2Object target;
	
	// set by the start of casting, in game ticks
	private int castEndTime;
	private int castInterruptTime;
	
	// set by the start of casting, in game ticks
	private int castPotionEndTime;
	@SuppressWarnings("unused")
	private int castPotionInterruptTime;
	
	// set by the start of attack, in game ticks
	int attackEndTime;
	private int attacking;
	private int disableBowAttackEndTime;
	
	/** Table of calculators containing all standard NPC calculator (ex : ACCURACY_COMBAT, EVASION_RATE. */
	private static final Calculator[] NPC_STD_CALCULATOR;
	static
	{
		NPC_STD_CALCULATOR = Formulas.getInstance().getStdNPCCalculators();
	}
	
	protected L2CharacterAI aiCharacter;
	protected Future<?> skillCast;
	protected Future<?> potionCast;
	private int clientX;
	private int clientY;
	private int clientZ;
	private int clientHeading;
	/** List of all QuestState instance that needs to be notified of this character's death. */
	private List<QuestState> notifyQuestOfDeathList = new ArrayList<>();
	
	/**
	 * Add QuestState instance that is to be notified of character's death.<BR>
	 * <BR>
	 * @param qs The QuestState that subscribe to this event
	 */
	public void addNotifyQuestOfDeath(final QuestState qs)
	{
		if (qs == null || notifyQuestOfDeathList.contains(qs))
		{
			return;
		}
		
		notifyQuestOfDeathList.add(qs);
	}
	
	/**
	 * Return a list of L2Character that attacked.<BR>
	 * <BR>
	 * @return the notify quest of death
	 */
	public List<QuestState> getNotifyQuestOfDeath()
	{
		if (notifyQuestOfDeathList == null)
		{
			notifyQuestOfDeathList = new ArrayList<>();
		}
		
		return notifyQuestOfDeathList;
	}
	
	/**
	 * Add a Func to the Calculator set of the L2Character.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * A L2Character owns a table of Calculators called <B>_calculators</B>. Each Calculator (a calculator per state) own a table of Func object. A Func object is a mathematic function that permit to calculate the modifier of a state (ex : REGENERATE_HP_RATE...). To reduce cache memory use,
	 * L2NPCInstances who don't have skills share the same Calculator set called <B>NPC_STD_CALCULATOR</B>.<BR>
	 * <BR>
	 * That's why, if a L2NPCInstance is under a skill/spell effect that modify one of its state, a copy of the NPC_STD_CALCULATOR must be create in its calculators before addind new Func object.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>If calculators is linked to NPC_STD_CALCULATOR, create a copy of NPC_STD_CALCULATOR in calculators</li>
	 * <li>Add the Func object to calculators</li><BR>
	 * <BR>
	 * @param f The Func object to add to the Calculator corresponding to the state affected
	 */
	public final synchronized void addStatFunc(final Func f)
	{
		if (f == null)
		{
			return;
		}
		
		// Check if Calculator set is linked to the standard Calculator set of NPC
		if (calculators == NPC_STD_CALCULATOR)
		{
			// Create a copy of the standard NPC Calculator set
			calculators = new Calculator[Stats.NUM_STATS];
			
			for (int i = 0; i < Stats.NUM_STATS; i++)
			{
				if (NPC_STD_CALCULATOR[i] != null)
				{
					calculators[i] = new Calculator(NPC_STD_CALCULATOR[i]);
				}
			}
		}
		
		// Select the Calculator of the affected state in the Calculator set
		final int stat = f.stat.ordinal();
		
		if (calculators[stat] == null)
		{
			calculators[stat] = new Calculator();
		}
		
		// Add the Func to the calculator corresponding to the state
		calculators[stat].addFunc(f);
		
	}
	
	/**
	 * Add a list of Funcs to the Calculator set of the L2Character.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * A L2Character owns a table of Calculators called <B>_calculators</B>. Each Calculator (a calculator per state) own a table of Func object. A Func object is a mathematic function that permit to calculate the modifier of a state (ex : REGENERATE_HP_RATE...). <BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method is ONLY for L2PcInstance</B></FONT><BR>
	 * <BR>
	 * <B><U> Example of use </U> :</B><BR>
	 * <BR>
	 * <li>Equip an item from inventory</li>
	 * <li>Learn a new passive skill</li>
	 * <li>Use an active skill</li><BR>
	 * <BR>
	 * @param funcs The list of Func objects to add to the Calculator corresponding to the state affected
	 */
	public final synchronized void addStatFuncs(final Func[] funcs)
	{
		
		List<Stats> modifiedStats = new ArrayList<>();
		
		for (Func f : funcs)
		{
			modifiedStats.add(f.stat);
			addStatFunc(f);
		}
		
		broadcastModifiedStats(modifiedStats);
	}
	
	/**
	 * Remove a Func from the Calculator set of the L2Character.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * A L2Character owns a table of Calculators called <B>_calculators</B>. Each Calculator (a calculator per state) own a table of Func object. A Func object is a mathematic function that permit to calculate the modifier of a state (ex : REGENERATE_HP_RATE...). To reduce cache memory use,
	 * L2NPCInstances who don't have skills share the same Calculator set called <B>NPC_STD_CALCULATOR</B>.<BR>
	 * <BR>
	 * That's why, if a L2NPCInstance is under a skill/spell effect that modify one of its state, a copy of the NPC_STD_CALCULATOR must be create in its calculators before addind new Func object.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Remove the Func object from calculators</li><BR>
	 * <BR>
	 * <li>If L2Character is a L2NPCInstance and calculators is equal to NPC_STD_CALCULATOR, free cache memory and just create a link on NPC_STD_CALCULATOR in calculators</li><BR>
	 * <BR>
	 * @param f The Func object to remove from the Calculator corresponding to the state affected
	 */
	public final synchronized void removeStatFunc(final Func f)
	{
		if (f == null)
		{
			return;
		}
		
		// Select the Calculator of the affected state in the Calculator set
		final int stat = f.stat.ordinal();
		
		if (calculators[stat] == null)
		{
			return;
		}
		
		// Remove the Func object from the Calculator
		calculators[stat].removeFunc(f);
		
		if (calculators[stat].size() == 0)
		{
			calculators[stat] = null;
		}
		
		// If possible, free the memory and just create a link on NPC_STD_CALCULATOR
		if (this instanceof L2NpcInstance)
		{
			int i = 0;
			
			for (; i < Stats.NUM_STATS; i++)
			{
				if (!Calculator.equalsCals(calculators[i], NPC_STD_CALCULATOR[i]))
				{
					break;
				}
			}
			
			if (i >= Stats.NUM_STATS)
			{
				calculators = NPC_STD_CALCULATOR;
			}
		}
	}
	
	/**
	 * Remove a list of Funcs from the Calculator set of the L2PcInstance.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * A L2Character owns a table of Calculators called <B>_calculators</B>. Each Calculator (a calculator per state) own a table of Func object. A Func object is a mathematic function that permit to calculate the modifier of a state (ex : REGENERATE_HP_RATE...). <BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method is ONLY for L2PcInstance</B></FONT><BR>
	 * <BR>
	 * <B><U> Example of use </U> :</B><BR>
	 * <BR>
	 * <li>Unequip an item from inventory</li>
	 * <li>Stop an active skill</li><BR>
	 * <BR>
	 * @param funcs The list of Func objects to add to the Calculator corresponding to the state affected
	 */
	public final synchronized void removeStatFuncs(final Func[] funcs)
	{
		List<Stats> modifiedStats = new ArrayList<>();
		
		for (Func f : funcs)
		{
			modifiedStats.add(f.stat);
			removeStatFunc(f);
		}
		
		broadcastModifiedStats(modifiedStats);
	}
	
	/**
	 * Remove all Func objects with the selected owner from the Calculator set of the L2Character.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * A L2Character owns a table of Calculators called <B>_calculators</B>. Each Calculator (a calculator per state) own a table of Func object. A Func object is a mathematic function that permit to calculate the modifier of a state (ex : REGENERATE_HP_RATE...). To reduce cache memory use,
	 * L2NPCInstances who don't have skills share the same Calculator set called <B>NPC_STD_CALCULATOR</B>.<BR>
	 * <BR>
	 * That's why, if a L2NPCInstance is under a skill/spell effect that modify one of its state, a copy of the NPC_STD_CALCULATOR must be create in its calculators before addind new Func object.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Remove all Func objects of the selected owner from calculators</li><BR>
	 * <BR>
	 * <li>If L2Character is a L2NPCInstance and calculators is equal to NPC_STD_CALCULATOR, free cache memory and just create a link on NPC_STD_CALCULATOR in calculators</li><BR>
	 * <BR>
	 * <B><U> Example of use </U> :</B><BR>
	 * <BR>
	 * <li>Unequip an item from inventory</li>
	 * <li>Stop an active skill</li><BR>
	 * <BR>
	 * @param owner The Object(Skill, Item...) that has created the effect
	 */
	public final void removeStatsOwner(final Object owner)
	{
		List<Stats> modifiedStats = null;
		
		int i = 0;
		// Go through the Calculator set
		synchronized (calculators)
		{
			for (final Calculator calc : calculators)
			{
				if (calc != null)
				{
					// Delete all Func objects of the selected owner
					if (modifiedStats != null)
					{
						modifiedStats.addAll(calc.removeOwner(owner));
					}
					else
					{
						modifiedStats = calc.removeOwner(owner);
					}
					
					if (calc.size() == 0)
					{
						calculators[i] = null;
					}
				}
				i++;
			}
			
			// If possible, free the memory and just create a link on NPC_STD_CALCULATOR
			if (this instanceof L2NpcInstance)
			{
				i = 0;
				for (; i < Stats.NUM_STATS; i++)
				{
					if (!Calculator.equalsCals(calculators[i], NPC_STD_CALCULATOR[i]))
					{
						break;
					}
				}
				
				if (i >= Stats.NUM_STATS)
				{
					calculators = NPC_STD_CALCULATOR;
				}
			}
			
			if (owner instanceof L2Effect && !((L2Effect) owner).preventExitUpdate)
			{
				broadcastModifiedStats(modifiedStats);
			}
		}
		
		modifiedStats = null;
	}
	
	/**
	 * Broadcast modified stats.
	 * @param stats the stats
	 */
	public void broadcastModifiedStats(List<Stats> stats)
	{
		if (stats == null || stats.isEmpty())
		{
			return;
		}
		
		boolean broadcastFull = false;
		boolean otherStats = false;
		StatusUpdate su = null;
		
		for (final Stats stat : stats)
		{
			if (stat == Stats.POWER_ATTACK_SPEED)
			{
				if (su == null)
				{
					su = new StatusUpdate(getObjectId());
				}
				
				su.addAttribute(StatusUpdate.ATK_SPD, getPAtkSpd());
			}
			else if (stat == Stats.MAGIC_ATTACK_SPEED)
			{
				if (su == null)
				{
					su = new StatusUpdate(getObjectId());
				}
				
				su.addAttribute(StatusUpdate.CAST_SPD, getMAtkSpd());
			}
			// else if (stat==Stats.MAX_HP) //
			// {
			// if (su == null) su = new StatusUpdate(getObjectId());
			// su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
			// }
			else if (stat == Stats.MAX_CP)
			{
				if (this instanceof L2PcInstance)
				{
					if (su == null)
					{
						su = new StatusUpdate(getObjectId());
					}
					
					su.addAttribute(StatusUpdate.MAX_CP, getMaxCp());
				}
			}
			// else if (stat==Stats.MAX_MP)
			// {
			// if (su == null) su = new StatusUpdate(getObjectId());
			// su.addAttribute(StatusUpdate.MAX_MP, getMaxMp());
			// }
			else if (stat == Stats.RUN_SPEED)
			{
				broadcastFull = true;
			}
			else
			{
				otherStats = true;
			}
		}
		
		if (this instanceof L2PcInstance)
		{
			if (broadcastFull)
			{
				((L2PcInstance) this).updateAndBroadcastStatus(2);
			}
			else
			{
				if (otherStats)
				{
					((L2PcInstance) this).updateAndBroadcastStatus(1);
					if (su != null)
					{
						for (final L2PcInstance player : getKnownList().getKnownPlayers().values())
						{
							try
							{
								player.sendPacket(su);
							}
							catch (final NullPointerException e)
							{
								e.printStackTrace();
							}
						}
					}
				}
				else if (su != null)
				{
					broadcastPacket(su);
				}
			}
		}
		else if (this instanceof L2NpcInstance)
		{
			if (broadcastFull && getKnownList() != null && getKnownList().getKnownPlayers() != null)
			{
				for (final L2PcInstance player : getKnownList().getKnownPlayers().values())
				{
					if (player != null)
					{
						player.sendPacket(new NpcInfo((L2NpcInstance) this, player));
					}
				}
			}
			else if (su != null)
			{
				broadcastPacket(su);
			}
		}
		else if (this instanceof L2Summon)
		{
			if (broadcastFull)
			{
				for (final L2PcInstance player : getKnownList().getKnownPlayers().values())
				{
					if (player != null)
					{
						player.sendPacket(new NpcInfo((L2Summon) this, player));
					}
				}
			}
			else if (su != null)
			{
				broadcastPacket(su);
			}
		}
		else if (su != null)
		{
			broadcastPacket(su);
		}
		
		su = null;
	}
	
	/**
	 * Return the orientation of the L2Character.<BR>
	 * <BR>
	 * @return the heading
	 */
	public final int getHeading()
	{
		return headingNumber;
	}
	
	/**
	 * Set the orientation of the L2Character.<BR>
	 * <BR>
	 * @param heading the new heading
	 */
	public final void setHeading(final int heading)
	{
		headingNumber = heading;
	}
	
	/**
	 * Return the X destination of the L2Character or the X position if not in movement.<BR>
	 * <BR>
	 * @return the client x
	 */
	public final int getClientX()
	{
		return clientX;
	}
	
	/**
	 * Gets the client y.
	 * @return the client y
	 */
	public final int getClientY()
	{
		return clientY;
	}
	
	/**
	 * Gets the client z.
	 * @return the client z
	 */
	public final int getClientZ()
	{
		return clientZ;
	}
	
	/**
	 * Gets the client heading.
	 * @return the client heading
	 */
	public final int getClientHeading()
	{
		return clientHeading;
	}
	
	/**
	 * Sets the client x.
	 * @param val the new client x
	 */
	public final void setClientX(final int val)
	{
		clientX = val;
	}
	
	/**
	 * Sets the client y.
	 * @param val the new client y
	 */
	public final void setClientY(final int val)
	{
		clientY = val;
	}
	
	/**
	 * Sets the client z.
	 * @param val the new client z
	 */
	public final void setClientZ(final int val)
	{
		clientZ = val;
	}
	
	/**
	 * Sets the client heading.
	 * @param val the new client heading
	 */
	public final void setClientHeading(final int val)
	{
		clientHeading = val;
	}
	
	/**
	 * Gets the xdestination.
	 * @return the xdestination
	 */
	public final int getXdestination()
	{
		final MoveData m = playerMove;
		
		if (m != null)
		{
			return m.xDestination;
		}
		
		return getX();
	}
	
	/**
	 * Return the Y destination of the L2Character or the Y position if not in movement.<BR>
	 * <BR>
	 * @return the ydestination
	 */
	public final int getYdestination()
	{
		final MoveData m = playerMove;
		
		if (m != null)
		{
			return m.yDestination;
		}
		
		return getY();
	}
	
	/**
	 * Return the Z destination of the L2Character or the Z position if not in movement.<BR>
	 * <BR>
	 * @return the zdestination
	 */
	public final int getZdestination()
	{
		final MoveData m = playerMove;
		
		if (m != null)
		{
			return m.zDestination;
		}
		
		return getZ();
	}
	
	/**
	 * Return True if the L2Character is in combat.<BR>
	 * <BR>
	 * @return true, if is in combat
	 */
	public boolean isInCombat()
	{
		if (getAI() == null)
		{
			return false;
		}
		
		return getAI().getAttackTarget() != null || getAI().isAutoAttacking();
	}
	
	/**
	 * Return True if the L2Character is moving.<BR>
	 * <BR>
	 * @return true, if is moving
	 */
	public final boolean isMoving()
	{
		return playerMove != null;
	}
	
	/**
	 * Return True if the L2Character is travelling a calculated path.<BR>
	 * <BR>
	 * @return true, if is on geodata path
	 */
	public final boolean isOnGeodataPath()
	{
		final MoveData move = playerMove;
		
		if (move == null)
		{
			return false;
		}
		
		try
		{
			if (move.onGeodataPathIndex == -1)
			{
				return false;
			}
			
			if (move.onGeodataPathIndex == move.geoPath.length - 1)
			{
				return false;
			}
		}
		catch (final NullPointerException e)
		{
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	/**
	 * Return True if the L2Character is casting.<BR>
	 * <BR>
	 * @return true, if is casting now
	 */
	public final boolean isCastingNow()
	{
		
		final L2Effect mog = getFirstEffect(L2Effect.EffectType.SIGNET_GROUND);
		if (mog != null)
		{
			return true;
		}
		
		return castEndTime > GameTimeController.getGameTicks();
	}
	
	/**
	 * Return True if the L2Character is casting.<BR>
	 * <BR>
	 * @return true, if is casting potion now
	 */
	public final boolean isCastingPotionNow()
	{
		return castPotionEndTime > GameTimeController.getGameTicks();
	}
	
	/**
	 * Return True if the cast of the L2Character can be aborted.<BR>
	 * <BR>
	 * @return true, if successful
	 */
	public final boolean canAbortCast()
	{
		return castInterruptTime > GameTimeController.getGameTicks();
	}
	
	/**
	 * Return True if the L2Character is attacking.<BR>
	 * <BR>
	 * @return true, if is attacking now
	 */
	public final boolean isAttackingNow()
	{
		return attackEndTime > GameTimeController.getGameTicks();
	}
	
	/**
	 * Return True if the L2Character has aborted its attack.<BR>
	 * <BR>
	 * @return true, if is attack aborted
	 */
	public final boolean isAttackAborted()
	{
		return attacking <= 0;
	}
	
	/**
	 * Abort the attack of the L2Character and send Server->Client ActionFailed packet.<BR>
	 * <BR>
	 * see com.l2jfrozen.gameserver.model.L2Character
	 */
	public final void abortAttack()
	{
		if (isAttackingNow())
		{
			attacking = 0;
			sendPacket(ActionFailed.STATIC_PACKET);
		}
	}
	
	/**
	 * Returns body part (paperdoll slot) we are targeting right now.
	 * @return the attacking body part
	 */
	public final int getAttackingBodyPart()
	{
		return attacking;
	}
	
	/**
	 * Abort the cast of the L2Character and send Server->Client MagicSkillCanceld/ActionFailed packet.<BR>
	 * <BR>
	 */
	public final void abortCast()
	{
		abortCast(false);
		
	}
	
	/**
	 * Abort the cast of the L2Character and send Server->Client MagicSkillCanceld/ActionFailed packet.<BR>
	 * <BR>
	 * @param force the force
	 */
	public final void abortCast(final boolean force)
	{
		if (isCastingNow() || force)
		{
			castEndTime = 0;
			castInterruptTime = 0;
			
			if (skillCast != null)
			{
				skillCast.cancel(true);
				skillCast = null;
			}
			
			if (getForceBuff() != null)
			{
				getForceBuff().onCastAbort();
			}
			
			final L2Effect mog = getFirstEffect(L2Effect.EffectType.SIGNET_GROUND);
			if (mog != null)
			{
				mog.exit(true);
			}
			
			// cancels the skill hit scheduled task
			enableAllSkills(); // re-enables the skills
			if (this instanceof L2PcInstance)
			{
				getAI().notifyEvent(CtrlEvent.EVT_FINISH_CASTING); // setting back previous intention
			}
			
			broadcastPacket(new MagicSkillCanceld(getObjectId())); // broadcast packet to stop animations client-side
			sendPacket(ActionFailed.STATIC_PACKET); // send an "action failed" packet to the caster
			
		}
	}
	
	/**
	 * Update the position of the L2Character during a movement and return True if the movement is finished.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * At the beginning of the move action, all properties of the movement are stored in the MoveData object called <B>_move</B> of the L2Character. The position of the start point and of the destination permit to estimated in function of the movement speed the time to achieve the destination.<BR>
	 * <BR>
	 * When the movement is started (ex : by MovetoLocation), this method will be called each 0.1 sec to estimate and update the L2Character position on the server. Note, that the current server position can differe from the current client position even if each movement is straight foward. That's why,
	 * client send regularly a Client->Server ValidatePosition packet to eventually correct the gap on the server. But, it's always the server position that is used in range calculation.<BR>
	 * <BR>
	 * At the end of the estimated movement time, the L2Character position is automatically set to the destination position even if the movement is not finished.<BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : The current Z position is obtained FROM THE CLIENT by the Client->Server ValidatePosition Packet. But x and y positions must be calculated to avoid that players try to modify their movement speed.</B></FONT><BR>
	 * <BR>
	 * @param  gameTicks Nb of ticks since the server start
	 * @return           True if the movement is finished
	 */
	public boolean updatePosition(final int gameTicks)
	{
		// Get movement data
		final MoveData m = playerMove;
		
		if (m == null)
		{
			return true;
		}
		
		if (!isVisible())
		{
			playerMove = null;
			return true;
		}
		
		if (m.moveTimestamp == 0)
		{
			m.moveTimestamp = m.moveStartTime;
			m.xAccurate = getX();
			m.yAccurate = getY();
		}
		
		// Check if the position has alreday be calculated
		if (m.moveTimestamp == gameTicks)
		{
			return false;
		}
		
		final int xPrev = getX();
		final int yPrev = getY();
		int zPrev = getZ();
		
		double dx, dy, dz, distFraction;
		if (Config.COORD_SYNCHRONIZE == 1)
		// the only method that can modify x,y while moving (otherwise move would/should be set null)
		{
			dx = m.xDestination - xPrev;
			dy = m.yDestination - yPrev;
		}
		else
		// otherwise we need saved temporary values to avoid rounding errors
		{
			dx = m.xDestination - m.xAccurate;
			dy = m.yDestination - m.yAccurate;
		}
		// Z coordinate will follow geodata or client values
		if (Config.GEODATA > 0 && Config.COORD_SYNCHRONIZE == 2 && !isFlying() && !isInsideZone(L2Character.ZONE_WATER) && !m.disregardingGeodata && GameTimeController.getGameTicks() % 10 == 0 // once a second to reduce possible cpu load
			&& !(this instanceof L2BoatInstance))
		{
			final short geoHeight = GeoData.getInstance().getSpawnHeight(xPrev, yPrev, zPrev - 30, zPrev + 30, getObjectId());
			dz = m.zDestination - geoHeight;
			// quite a big difference, compare to validatePosition packet
			if (this instanceof L2PcInstance && Math.abs(((L2PcInstance) this).getClientZ() - geoHeight) > 200 && Math.abs(((L2PcInstance) this).getClientZ() - geoHeight) < 1500)
			{
				dz = m.zDestination - zPrev; // allow diff
			}
			else if (isInCombat() && Math.abs(dz) > 200 && dx * dx + dy * dy < 40000) // allow mob to climb up to pcinstance
			{
				dz = m.zDestination - zPrev; // climbing
			}
			else
			{
				zPrev = geoHeight;
			}
		}
		else
		{
			dz = m.zDestination - zPrev;
		}
		
		float speed;
		if (this instanceof L2BoatInstance)
		{
			speed = ((L2BoatInstance) this).boatSpeed;
		}
		else
		{
			speed = getStat().getMoveSpeed();
		}
		
		final double distPassed = speed * (gameTicks - m.moveTimestamp) / GameTimeController.TICKS_PER_SECOND;
		if (dx * dx + dy * dy < 10000 && dz * dz > 2500) // close enough, allows error between client and server geodata if it cannot be avoided
		{
			distFraction = distPassed / Math.sqrt(dx * dx + dy * dy);
		}
		else
		{
			distFraction = distPassed / Math.sqrt(dx * dx + dy * dy + dz * dz);
		}
		
		// if (Config.DEVELOPER) LOGGER.warn("Move Ticks:" + (gameTicks - m._moveTimestamp) + ", distPassed:" + distPassed + ", distFraction:" + distFraction);
		
		if (distFraction > 1) // already there
		{
			// Set the position of the L2Character to the destination
			super.getPosition().setXYZ(m.xDestination, m.yDestination, m.zDestination);
			if (this instanceof L2BoatInstance)
			{
				((L2BoatInstance) this).updatePeopleInTheBoat(m.xDestination, m.yDestination, m.zDestination);
			}
			else
			{
				revalidateZone();
			}
		}
		else
		{
			m.xAccurate += dx * distFraction;
			m.yAccurate += dy * distFraction;
			
			// Set the position of the L2Character to estimated after parcial move
			super.getPosition().setXYZ((int) m.xAccurate, (int) m.yAccurate, zPrev + (int) (dz * distFraction + 0.5));
			if (this instanceof L2BoatInstance)
			{
				((L2BoatInstance) this).updatePeopleInTheBoat((int) m.xAccurate, (int) m.yAccurate, zPrev + (int) (dz * distFraction + 0.5));
			}
			else
			{
				revalidateZone();
			}
		}
		
		// Set the timer of last position update to now
		m.moveTimestamp = gameTicks;
		
		return distFraction > 1;
	}
	
	/**
	 * Revalidate zone.
	 */
	public void revalidateZone()
	{
		if (getWorldRegion() == null)
		{
			return;
		}
		
		getWorldRegion().revalidateZones(this);
	}
	
	/**
	 * Stop movement of the L2Character (Called by AI Accessor only).<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Delete movement data of the L2Character</li>
	 * <li>Set the current position (x,y,z), its current L2WorldRegion if necessary and its heading</li>
	 * <li>Remove the L2Object object from gmList** of GmListTable</li>
	 * <li>Remove object from knownObjects and knownPlayer* of all surrounding L2WorldRegion L2Characters</li><BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T send Server->Client packet StopMove/StopRotation </B></FONT><BR>
	 * <BR>
	 * @param pos the pos
	 */
	public void stopMove(final L2CharPosition pos)
	{
		stopMove(pos, true);
	}
	
	/**
	 * TODO: test broadcast head packets ffro !in boat.
	 * @param pos                the pos
	 * @param updateKnownObjects the update known objects
	 */
	public void stopMove(final L2CharPosition pos, final boolean updateKnownObjects)
	{
		// Delete movement data of the L2Character
		playerMove = null;
		
		// Set AI_INTENTION_IDLE
		if (this instanceof L2PcInstance && getAI() != null)
		{
			((L2PcInstance) this).getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		}
		
		// Set the current position (x,y,z), its current L2WorldRegion if necessary and its heading
		// All data are contained in a L2CharPosition object
		if (pos != null)
		{
			getPosition().setXYZ(pos.x, pos.y, GeoData.getInstance().getHeight(pos.x, pos.y, pos.z));
			setHeading(pos.heading);
			
			if (this instanceof L2PcInstance)
			{
				((L2PcInstance) this).revalidateZone(true);
				
				if (((L2PcInstance) this).isInBoat())
				{
					broadcastPacket(new ValidateLocationInVehicle(this));
				}
			}
		}
		
		broadcastPacket(new StopMove(this));
		
		if (updateKnownObjects)
		{
			ThreadPoolManager.getInstance().executeTask(new KnownListAsynchronousUpdateTask(this));
		}
	}
	
	/**
	 * Target a L2Object (add the target to the L2Character target, knownObject and L2Character to knownObject of the L2Object).<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * The L2Object (including L2Character) targeted is identified in <B>_target</B> of the L2Character<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Set the target of L2Character to L2Object</li>
	 * <li>If necessary, add L2Object to knownObject of the L2Character</li>
	 * <li>If necessary, add L2Character to knownObject of the L2Object</li>
	 * <li>If object==null, cancel Attak or Cast</li><BR>
	 * <BR>
	 * <B><U> Overriden in </U> :</B><BR>
	 * <BR>
	 * <li>L2PcInstance : Remove the L2PcInstance from the old target statusListener and add it to the new target if it was a L2Character</li><BR>
	 * <BR>
	 * @param object L2object to target
	 */
	public void setTarget(L2Object object)
	{
		if (object != null && !object.isVisible())
		{
			object = null;
		}
		
		if (object != null && object != target)
		{
			getKnownList().addKnownObject(object);
			object.getKnownList().addKnownObject(this);
		}
		
		// If object==null, Cancel Attak or Cast
		if (object == null)
		{
			if (target != null)
			{
				final TargetUnselected my = new TargetUnselected(this);
				
				// No need to broadcast the packet to all players
				if (this instanceof L2PcInstance)
				{
					// Send packet just to me and to party, not to any other that does not use the information
					if (!isInParty())
					{
						sendPacket(my);
					}
					else
					{
						getParty().broadcastToPartyMembers(my);
					}
				}
				else
				{
					sendPacket(new TargetUnselected(this));
				}
			}
		}
		
		target = object;
	}
	
	/**
	 * Return the identifier of the L2Object targeted or -1.<BR>
	 * <BR>
	 * @return the target id
	 */
	public final int getTargetId()
	{
		if (target != null)
		{
			return target.getObjectId();
		}
		
		return -1;
	}
	
	/**
	 * Return the L2Object targeted or null.<BR>
	 * <BR>
	 * @return the target
	 */
	public final L2Object getTarget()
	{
		return target;
	}
	
	// called from AIAccessor only
	/**
	 * Calculate movement data for a move to location action and add the L2Character to movingObjects of GameTimeController (only called by AI Accessor).<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * At the beginning of the move action, all properties of the movement are stored in the MoveData object called <B>_move</B> of the L2Character. The position of the start point and of the destination permit to estimated in function of the movement speed the time to achieve the destination.<BR>
	 * <BR>
	 * All L2Character in movement are identified in <B>movingObjects</B> of GameTimeController that will call the updatePosition method of those L2Character each 0.1s.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Get current position of the L2Character</li>
	 * <li>Calculate distance (dx,dy) between current position and destination including offset</li>
	 * <li>Create and Init a MoveData object</li>
	 * <li>Set the L2Character move object to MoveData object</li>
	 * <li>Add the L2Character to movingObjects of the GameTimeController</li>
	 * <li>Create a task to notify the AI that L2Character arrives at a check point of the movement</li><BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T send Server->Client packet MoveToPawn/CharMoveToLocation </B></FONT><BR>
	 * <BR>
	 * <B><U> Example of use </U> :</B><BR>
	 * <BR>
	 * <li>AI : onIntentionMoveTo(L2CharPosition), onIntentionPickUp(L2Object), onIntentionInteract(L2Object)</li>
	 * <li>FollowTask</li><BR>
	 * <BR>
	 * @param x      The X position of the destination
	 * @param y      The Y position of the destination
	 * @param z      The Y position of the destination
	 * @param offset The size of the interaction area of the L2Character targeted
	 */
	protected void moveToLocation(int x, int y, int z, int offset)
	{
		// Block movment during Event start
		if (this instanceof L2PcInstance)
		{
			if (L2Event.active && ((L2PcInstance) this).eventSitForced)
			{
				((L2PcInstance) this).sendMessage("A dark force beyond your mortal understanding makes your knees to shake when you try to stand up...");
				((L2PcInstance) this).getClient().sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			else if (TvT.isSitForced() && ((L2PcInstance) this).inEventTvT || CTF.isSitForced() && ((L2PcInstance) this).inEventCTF || DM.is_sitForced() && ((L2PcInstance) this).inEventDM)
			{
				((L2PcInstance) this).sendMessage("A dark force beyond your mortal understanding makes your knees to shake when you try to stand up...");
				((L2PcInstance) this).getClient().sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}
		
		// when start to move again, it has to stop sitdown task
		if (this instanceof L2PcInstance)
		{
			((L2PcInstance) this).setPosticipateSit(false);
		}
		
		// Fix archer bug with movment/hittask
		if (this instanceof L2PcInstance && isAttackingNow())
		{
			final L2ItemInstance rhand = ((L2PcInstance) this).getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
			if (rhand != null && rhand.getItemType() == L2WeaponType.BOW)
			{
				return;
			}
		}
		
		// Get the Move Speed of the L2Charcater
		final float speed = getStat().getMoveSpeed();
		
		if (speed <= 0 || isMovementDisabled())
		{
			return;
		}
		
		// Get current position of the L2Character
		final int curX = super.getX();
		final int curY = super.getY();
		final int curZ = super.getZ();
		
		// Calculate distance (dx,dy) between current position and destination
		//
		double dx = x - curX;
		double dy = y - curY;
		double dz = z - curZ;
		double distance = Math.sqrt(dx * dx + dy * dy);
		
		if (Config.GEODATA > 0 && isInsideZone(ZONE_WATER) && distance > 700)
		{
			final double divider = 700 / distance;
			x = curX + (int) (divider * dx);
			y = curY + (int) (divider * dy);
			z = curZ + (int) (divider * dz);
			dx = x - curX;
			dy = y - curY;
			dz = z - curZ;
			distance = Math.sqrt(dx * dx + dy * dy);
		}
		
		/*
		 * if(Config.DEBUG) { LOGGER.fine("distance to target:" + distance); }
		 */
		
		// Define movement angles needed
		// ^
		// | X (x,y)
		// | /
		// | /distance
		// | /
		// |/ angle
		// X ---------->
		// (curx,cury)
		
		double cos;
		double sin;
		
		// Check if a movement offset is defined or no distance to go through
		if (offset > 0 || distance < 1)
		{
			// approximation for moving closer when z coordinates are different
			//
			offset -= Math.abs(dz);
			
			if (offset < 5)
			{
				offset = 5;
			}
			
			// If no distance to go through, the movement is canceled
			if (distance < 1 || distance - offset <= 0)
			{
				sin = 0;
				cos = 1;
				distance = 0;
				x = curX;
				y = curY;
				
				if (Config.DEBUG)
				{
					LOGGER.debug("Already in range, no movement needed.");
				}
				
				// Notify the AI that the L2Character is arrived at destination
				getAI().notifyEvent(CtrlEvent.EVT_ARRIVED, null);
				
				return;
			}
			// Calculate movement angles needed
			sin = dy / distance;
			cos = dx / distance;
			
			distance -= offset - 5; // due to rounding error, we have to move a bit closer to be in range
			
			// Calculate the new destination with offset included
			x = curX + (int) (distance * cos);
			y = curY + (int) (distance * sin);
			
		}
		else
		{
			// Calculate movement angles needed
			sin = dy / distance;
			cos = dx / distance;
		}
		
		// Create and Init a MoveData object
		MoveData m = new MoveData();
		
		// GEODATA MOVEMENT CHECKS AND PATHFINDING
		
		m.onGeodataPathIndex = -1; // Initialize not on geodata path
		m.disregardingGeodata = false;
		
		if (Config.GEODATA > 0 && !isFlying() && (!isInsideZone(ZONE_WATER) || isInsideZone(ZONE_SIEGE)) && !(this instanceof L2NpcWalkerInstance))
		{
			final double originalDistance = distance;
			final int originalX = x;
			final int originalY = y;
			final int originalZ = z;
			final int gtx = originalX - L2World.MAP_MIN_X >> 4;
			final int gty = originalY - L2World.MAP_MIN_Y >> 4;
			
			// Movement checks:
			// when geodata == 2, for all characters except mobs returning home (could be changed later to teleport if pathfinding fails)
			// when geodata == 1, for l2playableinstance and l2riftinstance only
			if (Config.GEODATA > 0 && !(this instanceof L2Attackable && ((L2Attackable) this).isReturningToSpawnPoint()) || this instanceof L2PcInstance || this instanceof L2Summon && !(getAI().getIntention() == AI_INTENTION_FOLLOW) || this instanceof L2RiftInvaderInstance || isAfraid())
			{
				if (isOnGeodataPath())
				{
					try
					{
						if (gtx == playerMove.geoPathGtx && gty == playerMove.geoPathGty)
						{
							return;
						}
						playerMove.onGeodataPathIndex = -1; // Set not on geodata path
					}
					catch (final NullPointerException e)
					{
						e.printStackTrace();
					}
				}
				
				if (curX < L2World.MAP_MIN_X || curX > L2World.MAP_MAX_X || curY < L2World.MAP_MIN_Y || curY > L2World.MAP_MAX_Y)
				{
					// Temporary fix for character outside world region errors
					LOGGER.warn("Character " + getName() + " outside world area, in coordinates x:" + curX + " y:" + curY);
					getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
					if (this instanceof L2PcInstance)
					{
						((L2PcInstance) this).deleteMe();
					}
					else
					{
						onDecay();
					}
					
					return;
				}
				final Location destiny = GeoData.getInstance().moveCheck(curX, curY, curZ, x, y, z);
				// location different if destination wasn't reached (or just z coord is different)
				x = destiny.getX();
				y = destiny.getY();
				z = destiny.getZ();
				distance = Math.sqrt((x - curX) * (x - curX) + (y - curY) * (y - curY));
				
			}
			// Pathfinding checks. Only when geodata setting is 2, the LoS check gives shorter result
			// than the original movement was and the LoS gives a shorter distance than 2000
			// This way of detecting need for pathfinding could be changed.
			if ((this instanceof L2PcInstance && Config.ALLOW_PLAYERS_PATHNODE || !(this instanceof L2PcInstance)) && Config.GEODATA == 2 && originalDistance - distance > 100 && distance < 2000 && !isAfraid())
			{
				// Path calculation
				// Overrides previous movement check
				if (this instanceof L2PlayableInstance || isInCombat() || this instanceof L2MinionInstance)
				{
					// int gx = (curX - L2World.MAP_MIN_X) >> 4;
					// int gy = (curY - L2World.MAP_MIN_Y) >> 4;
					
					m.geoPath = PathFinding.getInstance().findPath(curX, curY, curZ, originalX, originalY, originalZ);
					if (m.geoPath == null || m.geoPath.length < 2) // No path found
					{
						// Even though there's no path found (remember geonodes aren't perfect),
						// the mob is attacking and right now we set it so that the mob will go
						// after target anyway, is dz is small enough. Summons will follow their masters no matter what.
						if (Config.ALLOW_PLAYERS_PATHNODE && this instanceof L2PcInstance/* this instanceof L2PcInstance || */
							|| !(this instanceof L2PlayableInstance) && Math.abs(z - curZ) > 140 || this instanceof L2Summon && !((L2Summon) this).getFollowStatus())
						{
							getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
							return;
						}
						m.disregardingGeodata = true;
						x = originalX;
						y = originalY;
						z = originalZ;
						distance = originalDistance;
					}
					else
					{
						m.onGeodataPathIndex = 0; // on first segment
						m.geoPathGtx = gtx;
						m.geoPathGty = gty;
						m.geoPathAccurateTx = originalX;
						m.geoPathAccurateTy = originalY;
						
						x = m.geoPath[m.onGeodataPathIndex].getX();
						y = m.geoPath[m.onGeodataPathIndex].getY();
						z = m.geoPath[m.onGeodataPathIndex].getZ();
						
						// check for doors in the route
						if (DoorTable.getInstance().checkIfDoorsBetween(curX, curY, curZ, x, y, z))
						{
							m.geoPath = null;
							getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
							return;
						}
						
						for (int i = 0; i < m.geoPath.length - 1; i++)
						{
							if (DoorTable.getInstance().checkIfDoorsBetween(m.geoPath[i], m.geoPath[i + 1]))
							{
								m.geoPath = null;
								getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
								return;
							}
						}
						
						dx = x - curX;
						dy = y - curY;
						distance = Math.sqrt(dx * dx + dy * dy);
						sin = dy / distance;
						cos = dx / distance;
					}
				}
			}
			// If no distance to go through, the movement is canceled
			if ((this instanceof L2PcInstance && Config.ALLOW_PLAYERS_PATHNODE || !(this instanceof L2PcInstance)) && distance < 1 && (Config.GEODATA == 2 || this instanceof L2PlayableInstance || this instanceof L2RiftInvaderInstance || isAfraid()))
			{
				/*
				 * sin = 0; cos = 1; distance = 0; x = curX; y = curY;
				 */
				
				if (this instanceof L2Summon)
				{
					((L2Summon) this).setFollowStatus(false);
				}
				
				// getAI().notifyEvent(CtrlEvent.EVT_ARRIVED, null);
				getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
				
				return;
			}
		}
		
		// Caclulate the Nb of ticks between the current position and the destination
		// One tick added for rounding reasons
		final int ticksToMove = 1 + (int) (GameTimeController.TICKS_PER_SECOND * distance / speed);
		
		// Calculate and set the heading of the L2Character
		setHeading((int) (Math.atan2(-sin, -cos) * 10430.37835) + 32768);
		
		/*
		 * if(Config.DEBUG) { LOGGER.fine("dist:" + distance + "speed:" + speed + " ttt:" + ticksToMove + " heading:" + getHeading()); }
		 */
		
		m.xDestination = x;
		m.yDestination = y;
		m.zDestination = z; // this is what was requested from client
		m.heading = 0;
		
		m.moveStartTime = GameTimeController.getGameTicks();
		
		/*
		 * if(Config.DEBUG) { LOGGER.fine("time to target:" + ticksToMove); }
		 */
		
		// Set the L2Character move object to MoveData object
		playerMove = m;
		
		// Add the L2Character to movingObjects of the GameTimeController
		// The GameTimeController manage objects movement
		GameTimeController.getInstance().registerMovingObject(this);
		
		// Create a task to notify the AI that L2Character arrives at a check point of the movement
		if (ticksToMove * GameTimeController.MILLIS_IN_TICK > 3000)
		{
			ThreadPoolManager.getInstance().scheduleAi(new NotifyAITask(CtrlEvent.EVT_ARRIVED_REVALIDATE), 2000);
		}
		
		// the CtrlEvent.EVT_ARRIVED will be sent when the character will actually arrive
		// to destination by GameTimeController
		
		m = null;
	}
	
	/**
	 * Move to next route point.
	 * @return true, if successful
	 */
	public boolean moveToNextRoutePoint()
	{
		if (!isOnGeodataPath())
		{
			// Cancel the move action
			playerMove = null;
			return false;
		}
		
		// Get the Move Speed of the L2Charcater
		final float speed = getStat().getMoveSpeed();
		
		if (speed <= 0 || isMovementDisabled())
		{
			// Cancel the move action
			playerMove = null;
			return false;
		}
		
		MoveData md = playerMove;
		if (md == null)
		{
			return false;
		}
		
		// Create and Init a MoveData object
		MoveData m = new MoveData();
		
		// Update MoveData object
		m.onGeodataPathIndex = md.onGeodataPathIndex + 1; // next segment
		m.geoPath = md.geoPath;
		m.geoPathGtx = md.geoPathGtx;
		m.geoPathGty = md.geoPathGty;
		m.geoPathAccurateTx = md.geoPathAccurateTx;
		m.geoPathAccurateTy = md.geoPathAccurateTy;
		
		if (md.onGeodataPathIndex == md.geoPath.length - 2)
		{
			m.xDestination = md.geoPathAccurateTx;
			m.yDestination = md.geoPathAccurateTy;
			m.zDestination = md.geoPath[m.onGeodataPathIndex].getZ();
		}
		else
		{
			m.xDestination = md.geoPath[m.onGeodataPathIndex].getX();
			m.yDestination = md.geoPath[m.onGeodataPathIndex].getY();
			m.zDestination = md.geoPath[m.onGeodataPathIndex].getZ();
		}
		
		final double dx = m.xDestination - super.getX();
		final double dy = m.yDestination - super.getY();
		final double distance = Math.sqrt(dx * dx + dy * dy);
		final double sin = dy / distance;
		final double cos = dx / distance;
		
		// Caclulate the Nb of ticks between the current position and the destination
		// One tick added for rounding reasons
		final int ticksToMove = 1 + (int) (GameTimeController.TICKS_PER_SECOND * distance / speed);
		
		setHeading((int) (Math.atan2(-sin, -cos) * 10430.37835) + 32768);
		m.heading = 0; // ?
		
		m.moveStartTime = GameTimeController.getGameTicks();
		
		if (Config.DEBUG)
		{
			LOGGER.debug("Time to target:" + ticksToMove);
		}
		
		// Set the L2Character move object to MoveData object
		playerMove = m;
		
		// Add the L2Character to movingObjects of the GameTimeController
		// The GameTimeController manage objects movement
		GameTimeController.getInstance().registerMovingObject(this);
		
		// Create a task to notify the AI that L2Character arrives at a check point of the movement
		if (ticksToMove * GameTimeController.MILLIS_IN_TICK > 3000)
		{
			ThreadPoolManager.getInstance().scheduleAi(new NotifyAITask(CtrlEvent.EVT_ARRIVED_REVALIDATE), 2000);
		}
		
		// the CtrlEvent.EVT_ARRIVED will be sent when the character will actually arrive
		// to destination by GameTimeController
		
		// Send a Server->Client packet CharMoveToLocation to the actor and all L2PcInstance in its knownPlayers
		CharMoveToLocation msg = new CharMoveToLocation(this);
		broadcastPacket(msg);
		
		msg = null;
		m = null;
		md = null;
		
		return true;
	}
	
	/**
	 * Validate movement heading.
	 * @param  heading the heading
	 * @return         true, if successful
	 */
	public boolean validateMovementHeading(final int heading)
	{
		MoveData md = playerMove;
		
		if (md == null)
		{
			return true;
		}
		
		boolean result = true;
		
		if (md.heading != heading)
		{
			result = md.heading == 0;
			md.heading = heading;
		}
		
		md = null;
		
		return result;
	}
	
	/**
	 * Return the distance between the current position of the L2Character and the target (x,y).<BR>
	 * <BR>
	 * @param      x X position of the target
	 * @param      y Y position of the target
	 * @return       the plan distance
	 * @deprecated   use getPlanDistanceSq(int x, int y, int z)
	 */
	@Deprecated
	public final double getDistance(final int x, final int y)
	{
		final double dx = x - getX();
		final double dy = y - getY();
		
		return Math.sqrt(dx * dx + dy * dy);
	}
	
	/**
	 * Return the distance between the current position of the L2Character and the target (x,y).<BR>
	 * <BR>
	 * @param      x X position of the target
	 * @param      y Y position of the target
	 * @param      z the z
	 * @return       the plan distance
	 * @deprecated   use getPlanDistanceSq(int x, int y, int z)
	 */
	@Deprecated
	public final double getDistance(final int x, final int y, final int z)
	{
		final double dx = x - getX();
		final double dy = y - getY();
		final double dz = z - getZ();
		
		return Math.sqrt(dx * dx + dy * dy + dz * dz);
	}
	
	/**
	 * Return the squared distance between the current position of the L2Character and the given object.<BR>
	 * <BR>
	 * @param  object L2Object
	 * @return        the squared distance
	 */
	public final double getDistanceSq(final L2Object object)
	{
		return getDistanceSq(object.getX(), object.getY(), object.getZ());
	}
	
	/**
	 * Return the squared distance between the current position of the L2Character and the given x, y, z.<BR>
	 * <BR>
	 * @param  x X position of the target
	 * @param  y Y position of the target
	 * @param  z Z position of the target
	 * @return   the squared distance
	 */
	public final double getDistanceSq(final int x, final int y, final int z)
	{
		final double dx = x - getX();
		final double dy = y - getY();
		final double dz = z - getZ();
		
		return dx * dx + dy * dy + dz * dz;
	}
	
	/**
	 * Return the squared plan distance between the current position of the L2Character and the given object.<BR>
	 * (check only x and y, not z)<BR>
	 * <BR>
	 * @param  object L2Object
	 * @return        the squared plan distance
	 */
	public final double getPlanDistanceSq(final L2Object object)
	{
		return getPlanDistanceSq(object.getX(), object.getY());
	}
	
	/**
	 * Return the squared plan distance between the current position of the L2Character and the given x, y, z.<BR>
	 * (check only x and y, not z)<BR>
	 * <BR>
	 * @param  x X position of the target
	 * @param  y Y position of the target
	 * @return   the squared plan distance
	 */
	public final double getPlanDistanceSq(final int x, final int y)
	{
		final double dx = x - getX();
		final double dy = y - getY();
		
		return dx * dx + dy * dy;
	}
	
	/**
	 * Check if this object is inside the given radius around the given object. Warning: doesn't cover collision radius!<BR>
	 * <BR>
	 * @param  object      the target
	 * @param  radius      the radius around the target
	 * @param  checkZ      should we check Z axis also
	 * @param  strictCheck true if (distance < radius), false if (distance <= radius)
	 * @return             true is the L2Character is inside the radius.
	 */
	public final boolean isInsideRadius(final L2Object object, final int radius, final boolean checkZ, final boolean strictCheck)
	{
		if (object != null)
		{
			return isInsideRadius(object.getX(), object.getY(), object.getZ(), radius, checkZ, strictCheck);
		}
		return false;
	}
	
	/**
	 * Check if this object is inside the given plan radius around the given point. Warning: doesn't cover collision radius!<BR>
	 * <BR>
	 * @param  x           X position of the target
	 * @param  y           Y position of the target
	 * @param  radius      the radius around the target
	 * @param  strictCheck true if (distance < radius), false if (distance <= radius)
	 * @return             true is the L2Character is inside the radius.
	 */
	public final boolean isInsideRadius(final int x, final int y, final int radius, final boolean strictCheck)
	{
		return isInsideRadius(x, y, 0, radius, false, strictCheck);
	}
	
	/**
	 * Check if this object is inside the given radius around the given point.<BR>
	 * <BR>
	 * @param  x           X position of the target
	 * @param  y           Y position of the target
	 * @param  z           Z position of the target
	 * @param  radius      the radius around the target
	 * @param  checkZ      should we check Z axis also
	 * @param  strictCheck true if (distance < radius), false if (distance <= radius)
	 * @return             true is the L2Character is inside the radius.
	 */
	public final boolean isInsideRadius(final int x, final int y, final int z, final int radius, final boolean checkZ, final boolean strictCheck)
	{
		final double dx = x - getX();
		final double dy = y - getY();
		final double dz = z - getZ();
		
		if (strictCheck)
		{
			if (checkZ)
			{
				return dx * dx + dy * dy + dz * dz < radius * radius;
			}
			return dx * dx + dy * dy < radius * radius;
		}
		if (checkZ)
		{
			return dx * dx + dy * dy + dz * dz <= radius * radius;
		}
		return dx * dx + dy * dy <= radius * radius;
	}
	
	/**
	 * Return the Weapon Expertise Penalty of the L2Character.<BR>
	 * <BR>
	 * @return the weapon expertise penalty
	 */
	public float getWeaponExpertisePenalty()
	{
		return 1.f;
	}
	
	/**
	 * Return the Armour Expertise Penalty of the L2Character.<BR>
	 * <BR>
	 * @return the armour expertise penalty
	 */
	public float getArmourExpertisePenalty()
	{
		return 1.f;
	}
	
	/**
	 * Set attacking corresponding to Attacking Body part to CHEST.<BR>
	 * <BR>
	 */
	public void setAttackingBodypart()
	{
		attacking = Inventory.PAPERDOLL_CHEST;
	}
	
	/**
	 * Retun True if arrows are available.<BR>
	 * <BR>
	 * <B><U> Overriden in </U> :</B><BR>
	 * <BR>
	 * <li>L2PcInstance</li><BR>
	 * <BR>
	 * @return true, if successful
	 */
	protected boolean checkAndEquipArrows()
	{
		return true;
	}
	
	/**
	 * Add Exp and Sp to the L2Character.<BR>
	 * <BR>
	 * <B><U> Overriden in </U> :</B><BR>
	 * <BR>
	 * <li>L2PcInstance</li>
	 * <li>L2PetInstance</li><BR>
	 * <BR>
	 * @param addToExp the add to exp
	 * @param addToSp  the add to sp
	 */
	public void addExpAndSp(final long addToExp, final int addToSp)
	{
		// Dummy method (overridden by players and pets)
	}
	
	/**
	 * Return the active weapon instance (always equiped in the right hand).<BR>
	 * <BR>
	 * <B><U> Overriden in </U> :</B><BR>
	 * <BR>
	 * <li>L2PcInstance</li><BR>
	 * <BR>
	 * @return the active weapon instance
	 */
	public abstract L2ItemInstance getActiveWeaponInstance();
	
	/**
	 * Return the active weapon item (always equiped in the right hand).<BR>
	 * <BR>
	 * <B><U> Overriden in </U> :</B><BR>
	 * <BR>
	 * <li>L2PcInstance</li><BR>
	 * <BR>
	 * @return the active weapon item
	 */
	public abstract L2Weapon getActiveWeaponItem();
	
	/**
	 * Return the secondary weapon instance (always equiped in the left hand).<BR>
	 * <BR>
	 * <B><U> Overriden in </U> :</B><BR>
	 * <BR>
	 * <li>L2PcInstance</li><BR>
	 * <BR>
	 * @return the secondary weapon instance
	 */
	public abstract L2ItemInstance getSecondaryWeaponInstance();
	
	/**
	 * Return the secondary weapon item (always equiped in the left hand).<BR>
	 * <BR>
	 * <B><U> Overriden in </U> :</B><BR>
	 * <BR>
	 * <li>L2PcInstance</li><BR>
	 * <BR>
	 * @return the secondary weapon item
	 */
	public abstract L2Weapon getSecondaryWeaponItem();
	
	/**
	 * Manage hit process (called by Hit Task).<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>If the attacker/target is dead or use fake death, notify the AI with EVT_CANCEL and send a Server->Client packet ActionFailed (if attacker is a L2PcInstance)</li>
	 * <li>If attack isn't aborted, send a message system (critical hit, missed...) to attacker/target if they are L2PcInstance</li>
	 * <li>If attack isn't aborted and hit isn't missed, reduce HP of the target and calculate reflection damage to reduce HP of attacker if necessary</li>
	 * <li>if attack isn't aborted and hit isn't missed, manage attack or cast break of the target (calculating rate, sending message...)</li><BR>
	 * <BR>
	 * @param target   The L2Character targeted
	 * @param damage   Nb of HP to reduce
	 * @param crit     True if hit is critical
	 * @param miss     True if hit is missed
	 * @param soulshot True if SoulShot are charged
	 * @param shld     True if shield is efficient
	 */
	protected void onHitTimer(final L2Character target, int damage, final boolean crit, final boolean miss, final boolean soulshot, final boolean shld)
	{
		// If the attacker/target is dead or use fake death, notify the AI with EVT_CANCEL
		// and send a Server->Client packet ActionFailed (if attacker is a L2PcInstance)
		if (target == null || isAlikeDead() || this instanceof L2NpcInstance && ((L2NpcInstance) this).isEventMob)
		{
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
			return;
		}
		
		if (this instanceof L2NpcInstance && target.isAlikeDead() || target.isDead() || !getKnownList().knowsObject(target) && !(this instanceof L2DoorInstance))
		{
			// getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null);
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
			
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (miss)
		{
			if (target instanceof L2PcInstance)
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.AVOIDED_S1S_ATTACK);
				
				if (this instanceof L2Summon)
				{
					final int mobId = ((L2Summon) this).getTemplate().npcId;
					sm.addNpcName(mobId);
				}
				else
				{
					sm.addString(getName());
				}
				
				((L2PcInstance) target).sendPacket(sm);
				
				sm = null;
			}
		}
		
		// If attack isn't aborted, send a message system (critical hit, missed...) to attacker/target if they are L2PcInstance
		if (!isAttackAborted())
		{
			if (Config.ALLOW_RAID_BOSS_PETRIFIED && (this instanceof L2PcInstance || this instanceof L2Summon)) // Check if option is True Or False.
			{
				boolean to_be_cursed = false;
				
				// check on BossZone raid lvl
				if (!(target instanceof L2PlayableInstance) && !(target instanceof L2SummonInstance))
				{ // this must work just on mobs/raids
					
					if (target.isRaid() && getLevel() > target.getLevel() + 8 || !(target instanceof L2PcInstance) && target.getTarget() != null && target.getTarget() instanceof L2RaidBossInstance && getLevel() > ((L2RaidBossInstance) target.getTarget()).getLevel() + 8
						|| !(target instanceof L2PcInstance) && target.getTarget() != null && target.getTarget() instanceof L2GrandBossInstance && getLevel() > ((L2GrandBossInstance) target.getTarget()).getLevel() + 8)
					
					{
						to_be_cursed = true;
					}
					
					// advanced check too if not already cursed
					if (!to_be_cursed)
					{
						int boss_id = -1;
						L2NpcTemplate boss_template = null;
						final L2BossZone boss_zone = GrandBossManager.getInstance().getZone(this);
						
						if (boss_zone != null)
						{
							boss_id = boss_zone.getBossId();
						}
						
						// boolean alive = false;
						
						if (boss_id != -1)
						{
							boss_template = NpcTable.getInstance().getTemplate(boss_id);
							
							if (boss_template != null && getLevel() > boss_template.getLevel() + 8)
							{
								L2MonsterInstance boss_instance = null;
								
								if (boss_template.type.equals("L2RaidBoss"))
								{
									final StatsSet actual_boss_stat = RaidBossSpawnManager.getInstance().getStatsSet(boss_id);
									if (actual_boss_stat != null)
									{
										// alive = actual_boss_stat.getLong("respawnTime") == 0;
										boss_instance = RaidBossSpawnManager.getInstance().getBoss(boss_id);
									}
								}
								else if (boss_template.type.equals("L2GrandBoss"))
								{
									final StatsSet actual_boss_stat = GrandBossManager.getInstance().getStatsSet(boss_id);
									if (actual_boss_stat != null)
									{
										// alive = actual_boss_stat.getLong("respawn_time") == 0;
										boss_instance = GrandBossManager.getInstance().getBoss(boss_id);
									}
								}
								
								// max allowed rage into take cursed is 3000
								if (boss_instance != null/* && alive */ && boss_instance.isInsideRadius(this, 3000, false, false))
								{
									to_be_cursed = true;
								}
							}
						}
					}
				}
				
				if (to_be_cursed)
				{
					L2Skill skill = SkillTable.getInstance().getInfo(4515, 1);
					
					if (skill != null)
					{
						abortAttack();
						abortCast();
						getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
						skill.getEffects(target, this, false, false, false);
						
						if (this instanceof L2Summon)
						{
							final L2Summon src = (L2Summon) this;
							if (src.getOwner() != null)
							{
								src.getOwner().abortAttack();
								src.getOwner().abortCast();
								src.getOwner().getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
								skill.getEffects(target, src.getOwner(), false, false, false);
							}
						}
					}
					else
					{
						LOGGER.warn("Skill 4515 at level 1 is missing in DP.");
					}
					
					skill = null;
					
					if (target instanceof L2MinionInstance)
					{
						((L2MinionInstance) target).getLeader().stopHating(this);
						
						List<L2MinionInstance> spawnedMinions = ((L2MinionInstance) target).getLeader().getSpawnedMinions();
						if (spawnedMinions != null && spawnedMinions.size() > 0)
						{
							Iterator<L2MinionInstance> itr = spawnedMinions.iterator();
							L2MinionInstance minion;
							while (itr.hasNext())
							{
								minion = itr.next();
								if (((L2MinionInstance) target).getLeader().getMostHated() == null)
								{
									((L2AttackableAI) minion.getAI()).setGlobalAggro(-25);
									minion.clearAggroList();
									minion.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
									minion.setWalking();
								}
								if (minion != null && !minion.isDead())
								{
									((L2AttackableAI) minion.getAI()).setGlobalAggro(-25);
									minion.clearAggroList();
									minion.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
									minion.addDamage(((L2MinionInstance) target).getLeader().getMostHated(), 100);
								}
							}
							itr = null;
							spawnedMinions = null;
							minion = null;
						}
					}
					else
					{
						((L2Attackable) target).stopHating(this);
						List<L2MinionInstance> spawnedMinions = ((L2MonsterInstance) target).getSpawnedMinions();
						if (spawnedMinions != null && spawnedMinions.size() > 0)
						{
							Iterator<L2MinionInstance> itr = spawnedMinions.iterator();
							L2MinionInstance minion;
							while (itr.hasNext())
							{
								minion = itr.next();
								if (((L2Attackable) target).getMostHated() == null)
								{
									((L2AttackableAI) minion.getAI()).setGlobalAggro(-25);
									minion.clearAggroList();
									minion.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
									minion.setWalking();
								}
								if (minion != null && !minion.isDead())
								{
									((L2AttackableAI) minion.getAI()).setGlobalAggro(-25);
									minion.clearAggroList();
									minion.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
									minion.addDamage(((L2Attackable) target).getMostHated(), 100);
								}
							}
							itr = null;
							spawnedMinions = null;
							minion = null;
						}
					}
					
					damage = 0; // prevents messing up drop calculation
				}
			}
			
			sendDamageMessage(target, damage, false, crit, miss);
			
			// If L2Character target is a L2PcInstance, send a system message
			if (target instanceof L2PcInstance)
			{
				L2PcInstance enemy = (L2PcInstance) target;
				
				// Check if shield is efficient
				if (shld)
				{
					enemy.sendPacket(new SystemMessage(SystemMessageId.SHIELD_DEFENCE_SUCCESSFULL));
					// else if (!miss && damage < 1)
					// enemy.sendMessage("You hit the target's armor.");
				}
				
				enemy = null;
			}
			else if (target instanceof L2Summon)
			{
				L2Summon activeSummon = (L2Summon) target;
				
				SystemMessage sm = new SystemMessage(SystemMessageId.PET_RECEIVED_S2_DAMAGE_BY_S1);
				sm.addString(getName());
				sm.addNumber(damage);
				activeSummon.getOwner().sendPacket(sm);
				
				sm = null;
				activeSummon = null;
			}
			
			if (!miss && damage > 0)
			{
				L2Weapon weapon = getActiveWeaponItem();
				final boolean isBow = weapon != null && weapon.getItemType().toString().equalsIgnoreCase("Bow");
				
				if (!isBow) // Do not reflect or absorb if weapon is of type bow
				{
					// Absorb HP from the damage inflicted
					final double absorbPercent = getStat().calcStat(Stats.ABSORB_DAMAGE_PERCENT, 0, null, null);
					
					if (absorbPercent > 0)
					{
						final int maxCanAbsorb = (int) (getMaxHp() - getCurrentHp());
						int absorbDamage = (int) (absorbPercent / 100. * damage);
						
						if (absorbDamage > maxCanAbsorb)
						{
							absorbDamage = maxCanAbsorb; // Can't absord more than max hp
						}
						
						if (absorbDamage > 0)
						{
							setCurrentHp(getCurrentHp() + absorbDamage);
							
							// Custom messages - nice but also more network load
							/*
							 * if (this instanceof L2PcInstance) ((L2PcInstance)this).sendMessage("You absorbed " + absorbDamage + " damage."); else if (this instanceof L2Summon) ((L2Summon)this).getOwner().sendMessage("Summon absorbed " + absorbDamage + " damage."); else if (Config.DEBUG) LOGGER.info(getName() + " absorbed "
							 * + absorbDamage + " damage.");
							 */
						}
					}
					
					// Reduce HP of the target and calculate reflection damage to reduce HP of attacker if necessary
					final double reflectPercent = target.getStat().calcStat(Stats.REFLECT_DAMAGE_PERCENT, 0, null, null);
					
					if (reflectPercent > 0)
					{
						int reflectedDamage = (int) (reflectPercent / 100. * damage);
						damage -= reflectedDamage;
						
						if (reflectedDamage > target.getMaxHp())
						{
							reflectedDamage = target.getMaxHp();
						}
						
						getStatus().reduceHp(reflectedDamage, target, true);
						
						// Custom messages - nice but also more network load
						/*
						 * if (target instanceof L2PcInstance) ((L2PcInstance)target).sendMessage("You reflected " + reflectedDamage + " damage."); else if (target instanceof L2Summon) ((L2Summon)target).getOwner().sendMessage("Summon reflected " + reflectedDamage + " damage."); if (this instanceof L2PcInstance)
						 * ((L2PcInstance)this).sendMessage("Target reflected to you " + reflectedDamage + " damage."); else if (this instanceof L2Summon) ((L2Summon)this).getOwner().sendMessage("Target reflected to your summon " + reflectedDamage + " damage.");
						 */
					}
				}
				
				target.reduceCurrentHp(damage, this);
				
				// Notify AI with EVT_ATTACKED
				target.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, this);
				getAI().clientStartAutoAttack();
				
				// Manage attack or cast break of the target (calculating rate, sending message...)
				if (!target.isRaid() && Formulas.calcAtkBreak(target, damage))
				{
					target.breakAttack();
					target.breakCast();
				}
				
				// Maybe launch chance skills on us
				if (chanceSkills != null)
				{
					chanceSkills.onHit(target, false, crit);
				}
				
				// Maybe launch chance skills on target
				if (target.getChanceSkills() != null)
				{
					target.getChanceSkills().onHit(this, true, crit);
				}
				
				weapon = null;
			}
			
			// Launch weapon Special ability effect if available
			L2Weapon activeWeapon = getActiveWeaponItem();
			
			if (activeWeapon != null)
			{
				activeWeapon.getSkillEffects(this, target, crit);
			}
			
			/*
			 * COMMENTED OUT BY nexus - 2006-08-17 We must not discharge the soulshouts at the onHitTimer method, as this can cause unwanted soulshout consumption if the attacker recharges the soulshot right after an attack request but before his hit actually lands on the target. The soulshot discharging has
			 * been moved to the doAttack method: As soon as we know that we didn't missed the hit there, then we must discharge any charged soulshots.
			 */
			/*
			 * L2ItemInstance weapon = getActiveWeaponInstance(); if (!miss) { if (this instanceof L2Summon && !(this instanceof L2PetInstance)) { if (((L2Summon)this).getChargedSoulShot() != L2ItemInstance.CHARGED_NONE) ((L2Summon)this).setChargedSoulShot(L2ItemInstance.CHARGED_NONE); } else { if (weapon !=
			 * null && weapon.getChargedSoulshot() != L2ItemInstance.CHARGED_NONE) weapon.setChargedSoulshot(L2ItemInstance.CHARGED_NONE); } }
			 */
			
			activeWeapon = null;
			
			if (this instanceof L2PcInstance && ((L2PcInstance) this).isMovingTaskDefined())
			{
				final L2ItemInstance rhand = ((L2PcInstance) this).getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
				if (rhand != null && rhand.getItemType() == L2WeaponType.BOW)
				{
					((L2PcInstance) this).startMovingTask();
				}
			}
			return;
		}
		
		if (this instanceof L2PcInstance && ((L2PcInstance) this).isMovingTaskDefined())
		{
			final L2ItemInstance rhand = ((L2PcInstance) this).getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
			if (rhand != null && rhand.getItemType() == L2WeaponType.BOW)
			{
				((L2PcInstance) this).startMovingTask();
			}
		}
		
		getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
	}
	
	/**
	 * Break an attack and send Server->Client ActionFailed packet and a System Message to the L2Character.<BR>
	 * <BR>
	 */
	public void breakAttack()
	{
		if (isAttackingNow())
		{
			// Abort the attack of the L2Character and send Server->Client ActionFailed packet
			abortAttack();
			
			if (this instanceof L2PcInstance)
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				
				// Send a system message
				sendPacket(new SystemMessage(SystemMessageId.ATTACK_FAILED));
			}
		}
	}
	
	/**
	 * Break a cast and send Server->Client ActionFailed packet and a System Message to the L2Character.<BR>
	 * <BR>
	 */
	public void breakCast()
	{
		// damage can only cancel magical skills
		if (isCastingNow() && canAbortCast() && getLastSkillCast() != null && getLastSkillCast().isMagic())
		{
			// Abort the cast of the L2Character and send Server->Client MagicSkillCanceld/ActionFailed packet.
			abortCast();
			
			if (this instanceof L2PcInstance)
			{
				// Send a system message
				sendPacket(new SystemMessage(SystemMessageId.CASTING_INTERRUPTED));
			}
		}
	}
	
	/**
	 * Reduce the arrow number of the L2Character.<BR>
	 * <BR>
	 * <B><U> Overriden in </U> :</B><BR>
	 * <BR>
	 * <li>L2PcInstance</li><BR>
	 * <BR>
	 */
	protected void reduceArrowCount()
	{
		// default is to do nothin
	}
	
	/**
	 * Manage Forced attack (shift + select target).<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>If L2Character or target is in a town area, send a system message TARGET_IN_PEACEZONE a Server->Client packet ActionFailed</li>
	 * <li>If target is confused, send a Server->Client packet ActionFailed</li>
	 * <li>If L2Character is a L2ArtefactInstance, send a Server->Client packet ActionFailed</li>
	 * <li>Send a Server->Client packet MyTargetSelected to start attack and Notify AI with AI_INTENTION_ATTACK</li><BR>
	 * <BR>
	 * @param player The L2PcInstance to attack
	 */
	@Override
	public void onForcedAttack(final L2PcInstance player)
	{
		if (player.getTarget() == null || !(player.getTarget() instanceof L2Character))
		{
			// If target is not attackable, send a Server->Client packet ActionFailed
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (isInsidePeaceZone(player))
		{
			// If L2Character or target is in a peace zone, send a system message TARGET_IN_PEACEZONE a Server->Client packet ActionFailed
			player.sendPacket(new SystemMessage(SystemMessageId.TARGET_IN_PEACEZONE));
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.isInOlympiadMode() && player.getTarget() != null && player.getTarget() instanceof L2PlayableInstance)
		{
			L2PcInstance target;
			
			if (player.getTarget() instanceof L2Summon)
			{
				target = ((L2Summon) player.getTarget()).getOwner();
			}
			else
			{
				target = (L2PcInstance) player.getTarget();
			}
			
			if (target.isInOlympiadMode() && !player.isInOlympiadFight() && player.getOlympiadGameId() == target.getOlympiadGameId())
			{
				// if L2PcInstance is in Olympia and the match isn't already start, send a Server->Client packet ActionFailed
				player.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			target = null;
		}
		
		if (player.isConfused() || player.isBlocked())
		{
			// If target is confused, send a Server->Client packet ActionFailed
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// GeoData Los Check or dz > 1000
		if (!GeoData.getInstance().canSeeTarget(player, this))
		{
			player.sendPacket(new SystemMessage(SystemMessageId.CANT_SEE_TARGET));
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		// Notify AI with AI_INTENTION_ATTACK
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
	}
	
	/**
	 * Return True if inside peace zone.<BR>
	 * <BR>
	 * @param  attacker the attacker
	 * @return          true, if is inside peace zone
	 */
	public boolean isInsidePeaceZone(final L2PcInstance attacker)
	{
		return isInsidePeaceZone(attacker, this);
		
	}
	
	/**
	 * Checks if is inside peace zone.
	 * @param  attacker the attacker
	 * @param  target   the target
	 * @return          true, if is inside peace zone
	 */
	public static boolean isInsidePeaceZone(final L2Object attacker, final L2Object target)
	{
		if (target == null)
		{
			return false;
		}
		
		if (target instanceof L2NpcInstance && Config.DISABLE_ATTACK_NPC_TYPE)
		{
			final String mobtype = ((L2NpcInstance) target).getTemplate().type;
			if (Config.LIST_ALLOWED_NPC_TYPES.contains(mobtype))
			{
				return false;
			}
		}
		
		// Attack Monster on Peace Zone like L2OFF.
		if (target instanceof L2MonsterInstance || attacker instanceof L2MonsterInstance && Config.ALT_MOB_AGRO_IN_PEACEZONE)
		{
			return false;
		}
		
		// Attack Guard on Peace Zone like L2OFF.
		if (target instanceof L2GuardInstance || attacker instanceof L2GuardInstance)
		{
			return false;
		}
		// Attack NPC on Peace Zone like L2OFF.
		if (target instanceof L2NpcInstance || attacker instanceof L2NpcInstance)
		{
			return false;
		}
		
		if (Config.ALT_GAME_KARMA_PLAYER_CAN_BE_KILLED_IN_PEACEZONE)
		{
			// allows red to be attacked and red to attack flagged players
			if (target instanceof L2PcInstance && ((L2PcInstance) target).getKarma() > 0)
			{
				return false;
			}
			
			if (target instanceof L2Summon && ((L2Summon) target).getOwner().getKarma() > 0)
			{
				return false;
			}
			
			if (attacker instanceof L2PcInstance && ((L2PcInstance) attacker).getKarma() > 0)
			{
				if (target instanceof L2PcInstance && ((L2PcInstance) target).getPvpFlag() > 0)
				{
					return false;
				}
				
				if (target instanceof L2Summon && ((L2Summon) target).getOwner().getPvpFlag() > 0)
				{
					return false;
				}
			}
			
			if (attacker instanceof L2Summon && ((L2Summon) attacker).getOwner().getKarma() > 0)
			{
				if (target instanceof L2PcInstance && ((L2PcInstance) target).getPvpFlag() > 0)
				{
					return false;
				}
				
				if (target instanceof L2Summon && ((L2Summon) target).getOwner().getPvpFlag() > 0)
				{
					return false;
				}
			}
		}
		
		// Right now only L2PcInstance has up-to-date zone status...
		//
		L2PcInstance src = null;
		L2PcInstance dst = null;
		
		if (attacker instanceof L2PlayableInstance && target instanceof L2PlayableInstance)
		{
			if (attacker instanceof L2PcInstance)
			{
				src = (L2PcInstance) attacker;
			}
			else if (attacker instanceof L2Summon)
			{
				src = ((L2Summon) attacker).getOwner();
			}
			
			if (target instanceof L2PcInstance)
			{
				dst = (L2PcInstance) target;
			}
			else if (target instanceof L2Summon)
			{
				dst = ((L2Summon) target).getOwner();
			}
		}
		
		if (src != null && src.getAccessLevel().allowPeaceAttack())
		{
			return false;
		}
		
		// checks on event status
		if (src != null && dst != null)
		{
			// Attacker and target can fight in olympiad with peace zone
			if (src.isInOlympiadMode() && src.isInOlympiadFight() && dst.isInOlympiadMode() && dst.isInOlympiadFight())
			{
				return false;
			}
			
			if (dst.isInFunEvent() && src.isInFunEvent())
			{
				
				if (src.isInStartedTVTEvent() && dst.isInStartedTVTEvent())
				{
					return false;
				}
				else if (src.isInStartedDMEvent() && dst.isInStartedDMEvent())
				{
					return false;
				}
				else if (src.isInStartedCTFEvent() && dst.isInStartedCTFEvent())
				{
					return false;
					// else
					// different events in same location --> already checked
				}
			}
		}
		
		if (attacker instanceof L2Character && ((L2Character) attacker).isInsideZone(ZONE_PEACE)
		// the townzone has to be already peace zone
		// || TownManager.getInstance().getTown(attacker.getX(), attacker.getY(), attacker.getZ())!= null
		)
		{
			return true;
		}
		
		if (target instanceof L2Character && ((L2Character) target).isInsideZone(ZONE_PEACE)
		// the townzone has to be already peace zone
		// || TownManager.getInstance().getTown(target.getX(), target.getY(), target.getZ())!= null
		)
		{
			return true;
		}
		
		return false;
	}
	
	/**
	 * return true if this character is inside an active grid.
	 * @return the boolean
	 */
	public Boolean isInActiveRegion()
	{
		try
		{
			final L2WorldRegion region = L2World.getInstance().getRegion(getX(), getY());
			return region != null && region.isActive();
		}
		catch (final Exception e)
		{
			if (this instanceof L2PcInstance)
			{
				LOGGER.warn("Player " + getName() + " at bad coords: (x: " + getX() + ", y: " + getY() + ", z: " + getZ() + ").");
				
				((L2PcInstance) this).sendMessage("Error with your coordinates! Please reboot your game fully!");
				((L2PcInstance) this).teleToLocation(80753, 145481, -3532, false); // Near Giran luxury shop
			}
			else
			{
				LOGGER.warn("Object " + getName() + " at bad coords: (x: " + getX() + ", y: " + getY() + ", z: " + getZ() + ").");
				decayMe();
			}
			return false;
		}
	}
	
	/**
	 * Return True if the L2Character has a Party in progress.<BR>
	 * <BR>
	 * @return true, if is in party
	 */
	public boolean isInParty()
	{
		return false;
	}
	
	/**
	 * Return the L2Party object of the L2Character.<BR>
	 * <BR>
	 * @return the party
	 */
	public L2Party getParty()
	{
		return null;
	}
	
	/**
	 * Return the Attack Speed of the L2Character (delay (in milliseconds) before next attack).<BR>
	 * <BR>
	 * @param  target the target
	 * @param  weapon the weapon
	 * @return        the int
	 */
	public int calculateTimeBetweenAttacks(final L2Character target, final L2Weapon weapon)
	{
		double atkSpd = 0;
		if (weapon != null)
		{
			switch (weapon.getItemType())
			{
				case BOW:
					atkSpd = getStat().getPAtkSpd();
					return (int) (1500 * 345 / atkSpd);
				case DAGGER:
					atkSpd = getStat().getPAtkSpd();
					// atkSpd /= 1.15;
					break;
				default:
					atkSpd = getStat().getPAtkSpd();
			}
		}
		else
		{
			atkSpd = getPAtkSpd();
		}
		
		return Formulas.getInstance().calcPAtkSpd(this, target, atkSpd);
	}
	
	/**
	 * Calculate reuse time.
	 * @param  target the target
	 * @param  weapon the weapon
	 * @return        the int
	 */
	public int calculateReuseTime(final L2Character target, final L2Weapon weapon)
	{
		if (weapon == null)
		{
			return 0;
		}
		
		int reuse = weapon.getAttackReuseDelay();
		
		// only bows should continue for now
		if (reuse == 0)
		{
			return 0;
		}
		
		// else if (reuse < 10) reuse = 1500;
		reuse *= getStat().getReuseModifier(target);
		
		final double atkSpd = getStat().getPAtkSpd();
		
		switch (weapon.getItemType())
		{
			case BOW:
				return (int) (reuse * 345 / atkSpd);
			default:
				return (int) (reuse * 312 / atkSpd);
		}
	}
	
	/**
	 * Return True if the L2Character use a dual weapon.<BR>
	 * <BR>
	 * @return true, if is using dual weapon
	 */
	public boolean isUsingDualWeapon()
	{
		return false;
	}
	
	/**
	 * Add a skill to the L2Character skills and its Func objects to the calculator set of the L2Character.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * All skills own by a L2Character are identified in <B>_skills</B><BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Replace oldSkill by newSkill or Add the newSkill</li>
	 * <li>If an old skill has been replaced, remove all its Func objects of L2Character calculator set</li>
	 * <li>Add Func objects of newSkill to the calculator set of the L2Character</li><BR>
	 * <BR>
	 * <B><U> Overriden in </U> :</B><BR>
	 * <BR>
	 * <li>L2PcInstance : Save update in the character_skills table of the database</li><BR>
	 * <BR>
	 * @param  newSkill The L2Skill to add to the L2Character
	 * @return          The L2Skill replaced or null if just added a new L2Skill
	 */
	@Override
	public L2Skill addSkill(final L2Skill newSkill)
	{
		L2Skill oldSkill = null;
		
		if (newSkill != null)
		{
			// Replace oldSkill by newSkill or Add the newSkill
			oldSkill = skills.put(newSkill.getId(), newSkill);
			
			// If an old skill has been replaced, remove all its Func objects
			if (oldSkill != null)
			{
				// if skill came with another one, we should delete the other one too.
				if (oldSkill.triggerAnotherSkill())
				{
					if (Config.DEBUG)
					{
						LOGGER.info("Removing Triggherable Skill: " + oldSkill.getTriggeredId());
					}
					
					triggeredSkills.remove(oldSkill.getTriggeredId());
					removeSkill(oldSkill.getTriggeredId(), true);
				}
				removeStatsOwner(oldSkill);
				
				// final Func[] skill_funcs = oldSkill.getStatFuncs(null, this);
				
				// // Remove old func if single effect skill is defined
				// if(newSkill.is_singleEffect()
				// && skill_funcs.length>0)
				// removeStatFuncs(skill_funcs);
				
			}
			
			// Add Func objects of newSkill to the calculator set of the L2Character
			addStatFuncs(newSkill.getStatFuncs(null, this));
			
			if (oldSkill != null && chanceSkills != null)
			{
				removeChanceSkill(oldSkill.getId());
			}
			if (newSkill.isChance())
			{
				addChanceSkill(newSkill);
			}
			
			if (newSkill.isChance() && newSkill.triggerAnotherSkill())
			{
				final L2Skill triggeredSkill = SkillTable.getInstance().getInfo(newSkill.getTriggeredId(), newSkill.getTriggeredLevel());
				addSkill(triggeredSkill);
			}
			
			if (newSkill.triggerAnotherSkill())
			{
				if (Config.DEBUG)
				{
					LOGGER.info("Adding Triggherable Skill: " + newSkill.getTriggeredId());
				}
				triggeredSkills.put(newSkill.getTriggeredId(), SkillTable.getInstance().getInfo(newSkill.getTriggeredId(), newSkill.getTriggeredLevel()));
			}
			
		}
		
		return oldSkill;
	}
	
	/**
	 * Adds the chance skill.
	 * @param skill the skill
	 */
	public void addChanceSkill(final L2Skill skill)
	{
		synchronized (this)
		{
			if (chanceSkills == null)
			{
				chanceSkills = new ChanceSkillList(this);
			}
			
			chanceSkills.put(skill, skill.getChanceCondition());
		}
	}
	
	/**
	 * Removes the chance skill.
	 * @param id the id
	 */
	public void removeChanceSkill(final int id)
	{
		synchronized (this)
		{
			for (final L2Skill skill : chanceSkills.keySet())
			{
				if (skill.getId() == id)
				{
					chanceSkills.remove(skill);
				}
			}
			
			if (chanceSkills.size() == 0)
			{
				chanceSkills = null;
			}
		}
	}
	
	/**
	 * Remove a skill from the L2Character and its Func objects from calculator set of the L2Character.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * All skills own by a L2Character are identified in <B>_skills</B><BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Remove the skill from the L2Character skills</li>
	 * <li>Remove all its Func objects from the L2Character calculator set</li><BR>
	 * <BR>
	 * <B><U> Overriden in </U> :</B><BR>
	 * <BR>
	 * <li>L2PcInstance : Save update in the character_skills table of the database</li><BR>
	 * <BR>
	 * @param  skill The L2Skill to remove from the L2Character
	 * @return       The L2Skill removed
	 */
	public synchronized L2Skill removeSkill(final L2Skill skill)
	{
		if (skill == null)
		{
			return null;
		}
		
		// Remove the skill from the L2Character skills
		return removeSkill(skill.getId());
	}
	
	/**
	 * Removes the skill.
	 * @param  skillId the skill id
	 * @return         the l2 skill
	 */
	public L2Skill removeSkill(final int skillId)
	{
		return removeSkill(skillId, true);
	}
	
	/**
	 * Removes the skill.
	 * @param  skillId      the skill id
	 * @param  cancelEffect the cancel effect
	 * @return              the l2 skill
	 */
	public L2Skill removeSkill(final int skillId, final boolean cancelEffect)
	{
		// Remove the skill from the L2Character skills
		final L2Skill oldSkill = skills.remove(skillId);
		// Remove all its Func objects from the L2Character calculator set
		if (oldSkill != null)
		{
			// this is just a fail-safe againts buggers and gm dummies...
			if (oldSkill.triggerAnotherSkill())
			{
				if (Config.DEBUG)
				{
					LOGGER.info("Removing Triggherable Skill: " + oldSkill.getTriggeredId());
				}
				removeSkill(oldSkill.getTriggeredId(), true);
				triggeredSkills.remove(oldSkill.getTriggeredId());
			}
			
			// Stop casting if this skill is used right now
			if (getLastSkillCast() != null && isCastingNow())
			{
				if (oldSkill.getId() == getLastSkillCast().getId())
				{
					abortCast();
				}
			}
			
			if (cancelEffect || oldSkill.isToggle())
			{
				final L2Effect e = getFirstEffect(oldSkill);
				if (e == null)
				{
					removeStatsOwner(oldSkill);
					stopSkillEffects(oldSkill.getId());
				}
			}
			
			if (oldSkill.isChance() && chanceSkills != null)
			{
				removeChanceSkill(oldSkill.getId());
			}
			removeStatsOwner(oldSkill);
		}
		return oldSkill;
	}
	
	/**
	 * Return all skills own by the L2Character in a table of L2Skill.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * All skills own by a L2Character are identified in <B>_skills</B> the L2Character <BR>
	 * <BR>
	 * @return the all skills
	 */
	public final L2Skill[] getAllSkills()
	{
		return skills.values().toArray(new L2Skill[skills.values().size()]);
	}
	
	/**
	 * @return the map containing this character skills.
	 */
	@Override
	public Map<Integer, L2Skill> getSkills()
	{
		return skills;
	}
	
	/**
	 * Gets the chance skills.
	 * @return the chance skills
	 */
	public ChanceSkillList getChanceSkills()
	{
		return chanceSkills;
	}
	
	/**
	 * Return the level of a skill owned by the L2Character.<BR>
	 * <BR>
	 * @param  skillId The identifier of the L2Skill whose level must be returned
	 * @return         The level of the L2Skill identified by skillId
	 */
	@Override
	public int getSkillLevel(final int skillId)
	{
		final L2Skill skill = skills.get(skillId);
		
		if (skill == null)
		{
			return -1;
		}
		
		return skill.getLevel();
	}
	
	/**
	 * Return True if the skill is known by the L2Character.<BR>
	 * <BR>
	 * @param  skillId The identifier of the L2Skill to check the knowledge
	 * @return         the known skill
	 */
	@Override
	public final L2Skill getKnownSkill(final int skillId)
	{
		return skills.get(skillId);
	}
	
	/**
	 * Return the number of skills of type(Buff, Debuff, HEAL_PERCENT, MANAHEAL_PERCENT) affecting this L2Character.<BR>
	 * <BR>
	 * @return The number of Buffs affecting this L2Character
	 */
	public int getBuffCount()
	{
		final L2Effect[] effects = getAllEffects();
		
		int numBuffs = 0;
		
		for (final L2Effect e : effects)
		{
			if (e == null)
			{
				synchronized (effectsTable)
				{
					effectsTable.remove(e);
				}
				continue;
			}
			
			if ((e.getSkill().getSkillType() == L2Skill.SkillType.BUFF || e.getSkill().getId() == 1416 || e.getSkill().getSkillType() == L2Skill.SkillType.REFLECT || e.getSkill().getSkillType() == L2Skill.SkillType.HEAL_PERCENT || e.getSkill().getSkillType() == L2Skill.SkillType.MANAHEAL_PERCENT) && !(e.getSkill().getId() > 4360 && e.getSkill().getId() < 4367)) // 7s
			// buffs
			{
				numBuffs++;
			}
		}
		
		return numBuffs;
	}
	
	/**
	 * Return the number of skills of type(Debuff, poison, slow, etc.) affecting this L2Character.<BR>
	 * <BR>
	 * @return The number of debuff affecting this L2Character
	 */
	public int getDeBuffCount()
	{
		final L2Effect[] effects = getAllEffects();
		int numDeBuffs = 0;
		
		for (final L2Effect e : effects)
		{
			if (e == null)
			{
				synchronized (effectsTable)
				{
					effectsTable.remove(e);
				}
				continue;
			}
			
			// Check for all debuff skills
			if (e.getSkill().is_Debuff())
			{
				numDeBuffs++;
			}
		}
		
		return numDeBuffs;
	}
	
	/**
	 * Gets the max buff count.
	 * @return the max buff count
	 */
	public int getMaxBuffCount()
	{
		return Config.BUFFS_MAX_AMOUNT + Math.max(0, getSkillLevel(L2Skill.SKILL_DIVINE_INSPIRATION));
	}
	
	/**
	 * Removes the first Buff of this L2Character.<BR>
	 * <BR>
	 * @param preferSkill If != 0 the given skill Id will be removed instead of first
	 */
	public void removeFirstBuff(final int preferSkill)
	{
		final L2Effect[] effects = getAllEffects();
		
		L2Effect removeMe = null;
		
		for (final L2Effect e : effects)
		{
			if (e == null)
			{
				synchronized (effectsTable)
				{
					effectsTable.remove(e);
				}
				continue;
			}
			
			if ((e.getSkill().getSkillType() == L2Skill.SkillType.BUFF || e.getSkill().getSkillType() == L2Skill.SkillType.REFLECT || e.getSkill().getSkillType() == L2Skill.SkillType.HEAL_PERCENT || e.getSkill().getSkillType() == L2Skill.SkillType.MANAHEAL_PERCENT) && !(e.getSkill().getId() > 4360 && e.getSkill().getId() < 4367))
			{
				if (preferSkill == 0)
				{
					removeMe = e;
					break;
				}
				else if (e.getSkill().getId() == preferSkill)
				{
					removeMe = e;
					break;
				}
				else if (removeMe == null)
				{
					removeMe = e;
				}
			}
		}
		
		if (removeMe != null)
		{
			removeMe.exit(true);
		}
	}
	
	/**
	 * Removes the first DeBuff of this L2Character.<BR>
	 * <BR>
	 * @param preferSkill If != 0 the given skill Id will be removed instead of first
	 */
	public void removeFirstDeBuff(final int preferSkill)
	{
		final L2Effect[] effects = getAllEffects();
		
		L2Effect removeMe = null;
		
		for (final L2Effect e : effects)
		{
			if (e == null)
			{
				
				synchronized (effectsTable)
				{
					effectsTable.remove(e);
				}
				continue;
			}
			
			if (e.getSkill().is_Debuff())
			{
				if (preferSkill == 0)
				{
					removeMe = e;
					break;
				}
				else if (e.getSkill().getId() == preferSkill)
				{
					removeMe = e;
					break;
				}
				else if (removeMe == null)
				{
					removeMe = e;
				}
			}
		}
		
		if (removeMe != null)
		{
			removeMe.exit(true);
		}
	}
	
	/**
	 * Gets the dance count.
	 * @return the dance count
	 */
	public int getDanceCount()
	{
		int danceCount = 0;
		
		final L2Effect[] effects = getAllEffects();
		
		for (final L2Effect e : effects)
		{
			if (e == null)
			{
				synchronized (effectsTable)
				{
					effectsTable.remove(e);
				}
				continue;
			}
			
			if (e.getSkill().isDance() && e.getInUse())
			{
				danceCount++;
			}
		}
		
		return danceCount;
	}
	
	/**
	 * Checks if the given skill stacks with an existing one.<BR>
	 * <BR>
	 * @param  checkSkill the skill to be checked
	 * @return            Returns whether or not this skill will stack
	 */
	public boolean doesStack(final L2Skill checkSkill)
	{
		if (effectsTable.size() < 1 || checkSkill.effectTemplates == null || checkSkill.effectTemplates.length < 1 || checkSkill.effectTemplates[0].stackType == null)
		{
			return false;
		}
		
		final String stackType = checkSkill.effectTemplates[0].stackType;
		
		if (stackType.equals("none"))
		{
			return false;
		}
		
		final L2Effect[] effects = getAllEffects();
		
		for (final L2Effect e : effects)
		{
			if (e == null)
			{
				synchronized (effectsTable)
				{
					effectsTable.remove(e);
				}
				continue;
			}
			
			if (e.getStackType() != null && e.getStackType().equals(stackType))
			{
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Manage the magic skill launching task (MP, HP, Item consummation...) and display the magic skill animation on client.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Send a Server->Client packet MagicSkillLaunched (to display magic skill animation) to all L2PcInstance of L2Charcater knownPlayers</li>
	 * <li>Consumme MP, HP and Item if necessary</li>
	 * <li>Send a Server->Client packet StatusUpdate with MP modification to the L2PcInstance</li>
	 * <li>Launch the magic skill in order to calculate its effects</li>
	 * <li>If the skill type is PDAM, notify the AI of the target with AI_INTENTION_ATTACK</li>
	 * <li>Notify the AI of the L2Character with EVT_FINISH_CASTING</li><BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : A magic skill casting MUST BE in progress</B></FONT><BR>
	 * <BR>
	 * @param targets  the targets
	 * @param skill    The L2Skill to use
	 * @param coolTime the cool time
	 * @param instant  the instant
	 */
	public void onMagicLaunchedTimer(final L2Object[] targets, final L2Skill skill, final int coolTime, final boolean instant)
	{
		if (skill == null || (targets == null || targets.length <= 0) && skill.getTargetType() != SkillTargetType.TARGET_AURA)
		{
			skillCast = null;
			enableAllSkills();
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
			
			return;
		}
		
		// Escaping from under skill's radius and peace zone check. First version, not perfect in AoE skills.
		int escapeRange = 0;
		
		if (skill.getEffectRange() > escapeRange)
		{
			escapeRange = skill.getEffectRange();
		}
		else if (skill.getCastRange() < 0 && skill.getSkillRadius() > 80)
		{
			escapeRange = skill.getSkillRadius();
		}
		
		L2Object[] final_targets = null;
		int skipped = 0;
		
		if (escapeRange > 0)
		{
			List<L2Character> targetList = new ArrayList<>();
			
			for (int i = 0; targets != null && i < targets.length; i++)
			{
				if (targets[i] instanceof L2Character)
				{
					if (!Util.checkIfInRange(escapeRange, this, targets[i], true))
					{
						continue;
					}
					
					// Check if the target is behind a wall
					if (skill.getSkillRadius() > 0 && skill.isOffensive() && Config.GEODATA > 0 && !GeoData.getInstance().canSeeTarget(this, targets[i]))
					{
						skipped++;
						continue;
					}
					
					if (skill.isOffensive())
					{
						if (this instanceof L2PcInstance)
						{
							if (((L2Character) targets[i]).isInsidePeaceZone((L2PcInstance) this))
							{
								continue;
							}
						}
						else
						{
							if (L2Character.isInsidePeaceZone(this, targets[i]))
							{
								continue;
							}
						}
					}
					targetList.add((L2Character) targets[i]);
				}
				// else
				// {
				// if (Config.DEBUG)
				// LOGGER.warn("Class cast bad: "+targets[i].getClass().toString());
				// }
			}
			if (targetList.isEmpty() && skill.getTargetType() != SkillTargetType.TARGET_AURA)
			{
				if (this instanceof L2PcInstance)
				{
					for (int i = 0; i < skipped; i++)
					{
						sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANT_SEE_TARGET));
					}
					
				}
				
				abortCast();
				return;
			}
			final_targets = targetList.toArray(new L2Character[targetList.size()]);
			targetList = null;
			
		}
		else
		{
			
			final_targets = targets;
			
		}
		
		// if the skill is not a potion and player
		// is not casting now
		// Ensure that a cast is in progress
		// Check if player is using fake death.
		// Potions can be used while faking death.
		if (!skill.isPotion())
		{
			if (!isCastingNow() || isAlikeDead())
			{
				skillCast = null;
				enableAllSkills();
				
				getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
				
				castEndTime = 0;
				castInterruptTime = 0;
				return;
			}
		}
		
		// Get the display identifier of the skill
		final int magicId = skill.getDisplayId();
		
		// Get the level of the skill
		int level = getSkillLevel(skill.getId());
		
		if (level < 1)
		{
			level = 1;
		}
		
		// Send a Server->Client packet MagicSkillLaunched to the L2Character AND to all L2PcInstance in the knownPlayers of the L2Character
		if (!skill.isPotion())
		{
			broadcastPacket(new MagicSkillLaunched(this, magicId, level, final_targets));
		}
		
		if (instant)
		{
			onMagicHitTimer(final_targets, skill, coolTime, true);
		}
		else
		{
			if (skill.isPotion())
			{
				potionCast = ThreadPoolManager.getInstance().scheduleEffect(new MagicUseTask(final_targets, skill, coolTime, 2), 200);
			}
			else
			{
				skillCast = ThreadPoolManager.getInstance().scheduleEffect(new MagicUseTask(final_targets, skill, coolTime, 2), 200);
			}
			
		}
		
	}
	
	/*
	 * Runs in the end of skill casting
	 */
	/**
	 * On magic hit timer.
	 * @param targets  the targets
	 * @param skill    the skill
	 * @param coolTime the cool time
	 * @param instant  the instant
	 */
	public void onMagicHitTimer(final L2Object[] targets, final L2Skill skill, final int coolTime, final boolean instant)
	{
		if (skill == null || (targets == null || targets.length <= 0) && skill.getTargetType() != SkillTargetType.TARGET_AURA)
		{
			skillCast = null;
			enableAllSkills();
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
			
			return;
		}
		
		if (getForceBuff() != null)
		{
			skillCast = null;
			enableAllSkills();
			
			getForceBuff().onCastAbort();
			
			return;
		}
		
		final L2Effect mog = getFirstEffect(L2Effect.EffectType.SIGNET_GROUND);
		if (mog != null)
		{
			skillCast = null;
			enableAllSkills();
			
			// close skill if it's not SIGNET_CASTTIME
			if (mog.getSkill().getSkillType() != SkillType.SIGNET_CASTTIME)
			{
				mog.exit(true);
			}
			
			final L2Object target = targets == null ? null : targets[0];
			if (target != null)
			{
				notifyQuestEventSkillFinished(skill, target);
			}
			return;
		}
		
		final L2Object[] targets2 = targets;
		try
		{
			if (targets2 != null && targets2.length != 0)
			{
				
				// Go through targets table
				for (final L2Object target2 : targets2)
				{
					if (target2 == null)
					{
						continue;
					}
					
					if (target2 instanceof L2PlayableInstance)
					{
						L2Character target = (L2Character) target2;
						
						// If the skill is type STEALTH(ex: Dance of Shadow)
						if (skill.isAbnormalEffectByName(ABNORMAL_EFFECT_STEALTH))
						{
							final L2Effect silentMove = target.getFirstEffect(L2Effect.EffectType.SILENT_MOVE);
							if (silentMove != null)
							{
								silentMove.exit(true);
							}
						}
						
						if (skill.getSkillType() == SkillType.BUFF || skill.getSkillType() == SkillType.SEED)
						{
							SystemMessage smsg = new SystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
							smsg.addString(skill.getName());
							target.sendPacket(smsg);
							smsg = null;
						}
						
						if (this instanceof L2PcInstance && target instanceof L2Summon)
						{
							((L2Summon) target).getOwner().sendPacket(new PetInfo((L2Summon) target));
							sendPacket(new NpcInfo((L2Summon) target, this));
							
							// The PetInfo packet wipes the PartySpelled (list of active spells' icons). Re-add them
							((L2Summon) target).updateEffectIcons(true);
						}
						
						target = null;
					}
				}
				
			}
			
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
		
		try
		{
			
			StatusUpdate su = new StatusUpdate(getObjectId());
			boolean isSendStatus = false;
			
			// Consume MP of the L2Character and Send the Server->Client packet StatusUpdate with current HP and MP to all other L2PcInstance to inform
			final double mpConsume = getStat().getMpConsume(skill);
			
			if (mpConsume > 0)
			{
				if (skill.isDance())
				{
					getStatus().reduceMp(calcStat(Stats.DANCE_MP_CONSUME_RATE, mpConsume, null, null));
				}
				else if (skill.isMagic())
				{
					getStatus().reduceMp(calcStat(Stats.MAGICAL_MP_CONSUME_RATE, mpConsume, null, null));
				}
				else
				{
					getStatus().reduceMp(calcStat(Stats.PHYSICAL_MP_CONSUME_RATE, mpConsume, null, null));
				}
				
				su.addAttribute(StatusUpdate.CUR_MP, (int) getCurrentMp());
				isSendStatus = true;
			}
			
			// Consume HP if necessary and Send the Server->Client packet StatusUpdate with current HP and MP to all other L2PcInstance to inform
			if (skill.getHpConsume() > 0)
			{
				double consumeHp;
				
				consumeHp = calcStat(Stats.HP_CONSUME_RATE, skill.getHpConsume(), null, null);
				
				if (consumeHp + 1 >= getCurrentHp())
				{
					consumeHp = getCurrentHp() - 1.0;
				}
				
				getStatus().reduceHp(consumeHp, this);
				
				su.addAttribute(StatusUpdate.CUR_HP, (int) getCurrentHp());
				isSendStatus = true;
			}
			
			// Send a Server->Client packet StatusUpdate with MP modification to the L2PcInstance
			if (isSendStatus)
			{
				sendPacket(su);
			}
			
			// Consume Items if necessary and Send the Server->Client packet InventoryUpdate with Item modification to all the L2Character
			if (skill.getItemConsume() > 0)
			{
				consumeItem(skill.getItemConsumeId(), skill.getItemConsume());
			}
			
			// Launch the magic skill in order to calculate its effects
			callSkill(skill, targets);
			
			su = null;
			
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
		
		if (instant || coolTime == 0)
		{
			
			onMagicFinalizer(targets, skill);
			
		}
		else
		{
			if (skill.isPotion())
			{
				potionCast = ThreadPoolManager.getInstance().scheduleEffect(new MagicUseTask(targets, skill, coolTime, 3), coolTime);
			}
			else
			{
				skillCast = ThreadPoolManager.getInstance().scheduleEffect(new MagicUseTask(targets, skill, coolTime, 3), coolTime);
			}
			
		}
	}
	
	/*
	 * Runs after skill hitTime+coolTime
	 */
	/**
	 * On magic finalizer.
	 * @param targets the targets
	 * @param skill   the skill
	 */
	public void onMagicFinalizer(final L2Object[] targets, final L2Skill skill)
	{
		if (skill.isPotion())
		{
			potionCast = null;
			castPotionEndTime = 0;
			castPotionInterruptTime = 0;
		}
		else
		{
			skillCast = null;
			castEndTime = 0;
			castInterruptTime = 0;
			
			enableAllSkills();
			
			// if the skill has changed the character's state to something other than STATE_CASTING
			// then just leave it that way, otherwise switch back to STATE_IDLE.
			// if(isCastingNow())
			// getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null);
			if (skill.getId() != 345 && skill.getId() != 346)
			{
				// Like L2OFF while use a skill and next interntion == null the char stop auto attack
				if (getAI().getNextIntention() == null && skill.getSkillType() == SkillType.PDAM && skill.getCastRange() < 400 || skill.getSkillType() == SkillType.BLOW || skill.getSkillType() == SkillType.DRAIN_SOUL || skill.getSkillType() == SkillType.SOW || skill.getSkillType() == SkillType.SPOIL)
				{
					if (this instanceof L2PcInstance)
					{
						final L2PcInstance currPlayer = (L2PcInstance) this;
						final SkillDat skilldat = currPlayer.getCurrentSkill();
						// Like L2OFF if the skill is BLOW the player doesn't auto attack
						// If on XML skill nextActionAttack = true the char auto attack
						// If CTRL is pressed the autoattack is aborted (like L2OFF)
						if (skilldat != null && !skilldat.isCtrlPressed() && skill.nextActionIsAttack() && getTarget() != null && getTarget() instanceof L2Character)
						{
							getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, getTarget());
						}
					}
					else
					// case NPC
					{
						if (skill.nextActionIsAttack() && getTarget() != null && getTarget() instanceof L2Character)
						{
							getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, getTarget());
						}
						else if (skill.isOffensive() && !(skill.getSkillType() == SkillType.UNLOCK) && !(skill.getSkillType() == SkillType.BLOW) && !(skill.getSkillType() == SkillType.DELUXE_KEY_UNLOCK) && skill.getId() != 345 && skill.getId() != 346)
						{
							getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, getTarget());
							getAI().clientStartAutoAttack();
						}
					}
				}
				if (this instanceof L2PcInstance)
				{
					final L2PcInstance currPlayer = (L2PcInstance) this;
					final SkillDat skilldat = currPlayer.getCurrentSkill();
					if (skilldat != null && !skilldat.isCtrlPressed() && skill.isOffensive() && !(skill.getSkillType() == SkillType.UNLOCK) && !(skill.getSkillType() == SkillType.BLOW) && !(skill.getSkillType() == SkillType.DELUXE_KEY_UNLOCK) && skill.getId() != 345 && skill.getId() != 346)
					{
						if (!skill.isMagic() && skill.nextActionIsAttack())
						{
							getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, getTarget());
						}
						
						getAI().clientStartAutoAttack();
					}
				}
				else
				// case npc
				{
					if (skill.isOffensive() && !(skill.getSkillType() == SkillType.UNLOCK) && !(skill.getSkillType() == SkillType.BLOW) && !(skill.getSkillType() == SkillType.DELUXE_KEY_UNLOCK) && skill.getId() != 345 && skill.getId() != 346)
					{
						if (!skill.isMagic())
						{
							getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, getTarget());
						}
						
						getAI().clientStartAutoAttack();
					}
				}
			}
			else
			{
				getAI().clientStopAutoAttack();
			}
			
			// Notify the AI of the L2Character with EVT_FINISH_CASTING
			getAI().notifyEvent(CtrlEvent.EVT_FINISH_CASTING);
			
			notifyQuestEventSkillFinished(skill, getTarget());
			
			/*
			 * If character is a player, then wipe their current cast state and check if a skill is queued. If there is a queued skill, launch it and wipe the queue.
			 */
			if (this instanceof L2PcInstance)
			{
				L2PcInstance currPlayer = (L2PcInstance) this;
				SkillDat queuedSkill = currPlayer.getQueuedSkill();
				
				currPlayer.setCurrentSkill(null, false, false);
				
				if (queuedSkill != null)
				{
					currPlayer.setQueuedSkill(null, false, false);
					
					// DON'T USE : Recursive call to useMagic() method
					// currPlayer.useMagic(queuedSkill.getSkill(), queuedSkill.isCtrlPressed(), queuedSkill.isShiftPressed());
					ThreadPoolManager.getInstance().executeTask(new QueuedMagicUseTask(currPlayer, queuedSkill.getSkill(), queuedSkill.isCtrlPressed(), queuedSkill.isShiftPressed()));
				}
				
				queuedSkill = null;
				
				final L2Weapon activeWeapon = getActiveWeaponItem();
				// Launch weapon Special ability skill effect if available
				if (activeWeapon != null)
				{
					try
					{
						if (targets != null && targets.length > 0)
						{
							for (final L2Object target : targets)
							{
								if (target != null && target instanceof L2Character && !((L2Character) target).isDead())
								{
									final L2Character player = (L2Character) target;
									
									if (activeWeapon.getSkillEffects(this, player, skill))
									{
										sendPacket(SystemMessage.sendString("Target affected by weapon special ability!"));
									}
								}
								
							}
						}
					}
					catch (final Exception e)
					{
						e.printStackTrace();
					}
				}
				
				currPlayer = null;
			}
		}
	}
	
	// Quest event ON_SPELL_FNISHED
	/**
	 * Notify quest event skill finished.
	 * @param skill  the skill
	 * @param target the target
	 */
	private void notifyQuestEventSkillFinished(final L2Skill skill, final L2Object target)
	{
		if (this instanceof L2NpcInstance && (target instanceof L2PcInstance || target instanceof L2Summon))
		{
			
			final L2PcInstance player = target instanceof L2PcInstance ? (L2PcInstance) target : ((L2Summon) target).getOwner();
			
			for (final Quest quest : ((L2NpcTemplate) getTemplate()).getEventQuests(Quest.QuestEventType.ON_SPELL_FINISHED))
			{
				quest.notifySpellFinished((L2NpcInstance) this, player, skill);
			}
		}
	}
	
	/**
	 * Reduce the item number of the L2Character.<BR>
	 * <BR>
	 * <B><U> Overriden in </U> :</B><BR>
	 * <BR>
	 * <li>L2PcInstance</li><BR>
	 * <BR>
	 * @param itemConsumeId the item consume id
	 * @param itemCount     the item count
	 */
	public void consumeItem(final int itemConsumeId, final int itemCount)
	{
	}
	
	/**
	 * Enable a skill (remove it from disabledSkills of the L2Character).<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * All skills disabled are identified by their skillId in <B>_disabledSkills</B> of the L2Character <BR>
	 * <BR>
	 * @param skill
	 */
	public void enableSkill(final L2Skill skill)
	{
		if (disabledSkills == null)
		{
			return;
		}
		
		disabledSkills.remove(Integer.valueOf(skill.getReuseHashCode()));
		
		if (this instanceof L2PcInstance)
		{
			removeTimeStamp(skill);
		}
	}
	
	/**
	 * Disable a skill (add it to disabledSkills of the L2Character).<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * All skills disabled are identified by their skillId in <B>_disabledSkills</B> of the L2Character <BR>
	 * <BR>
	 * @param skill The identifier of the L2Skill to disable
	 */
	public void disableSkill(final L2Skill skill)
	{
		if (disabledSkills == null)
		{
			disabledSkills = Collections.synchronizedList(new ArrayList<Integer>());
		}
		
		disabledSkills.add(skill.getReuseHashCode());
	}
	
	/**
	 * Disable this skill id for the duration of the delay in milliseconds.
	 * @param skill the skill thats going to be disabled
	 * @param delay (seconds * 1000)
	 */
	public void disableSkill(final L2Skill skill, final long delay)
	{
		if (skill == null)
		{
			return;
		}
		
		disableSkill(skill);
		
		if (delay > 10)
		{
			ThreadPoolManager.getInstance().scheduleAi(new EnableSkill(skill), delay);
		}
	}
	
	/**
	 * Check if a skill is disabled.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * All skills disabled are identified by their skillId in <B>_disabledSkills</B> of the L2Character <BR>
	 * <BR>
	 * @param  pSkill the skill to know if its disabled
	 * @return        true, if is skill disabled
	 */
	public boolean isSkillDisabled(final L2Skill pSkill)
	{
		final L2Skill skill = pSkill;
		
		if (isAllSkillsDisabled() && !skill.isPotion())
		{
			return true;
		}
		
		if (this instanceof L2PcInstance)
		{
			final L2PcInstance activeChar = (L2PcInstance) this;
			
			if ((skill.getSkillType() == SkillType.FISHING || skill.getSkillType() == SkillType.REELING || skill.getSkillType() == SkillType.PUMPING) && !activeChar.isFishing() && activeChar.getActiveWeaponItem() != null && activeChar.getActiveWeaponItem().getItemType() != L2WeaponType.ROD)
			{
				if (skill.getSkillType() == SkillType.PUMPING)
				{
					// Pumping skill is available only while fishing
					activeChar.sendPacket(new SystemMessage(SystemMessageId.CAN_USE_PUMPING_ONLY_WHILE_FISHING));
				}
				else if (skill.getSkillType() == SkillType.REELING)
				{
					// Reeling skill is available only while fishing
					activeChar.sendPacket(new SystemMessage(SystemMessageId.CAN_USE_REELING_ONLY_WHILE_FISHING));
				}
				else if (skill.getSkillType() == SkillType.FISHING)
				{
					// Player hasn't fishing pole equiped
					activeChar.sendPacket(new SystemMessage(SystemMessageId.FISHING_POLE_NOT_EQUIPPED));
				}
				
				final SystemMessage sm = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
				sm.addString(skill.getName());
				activeChar.sendPacket(sm);
				return true;
			}
			
			if ((skill.getSkillType() == SkillType.FISHING || skill.getSkillType() == SkillType.REELING || skill.getSkillType() == SkillType.PUMPING) && activeChar.getActiveWeaponItem() == null)
			{
				final SystemMessage sm = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
				sm.addString(skill.getName());
				activeChar.sendPacket(sm);
				return true;
			}
			
			if ((skill.getSkillType() == SkillType.REELING || skill.getSkillType() == SkillType.PUMPING) && !activeChar.isFishing() && activeChar.getActiveWeaponItem() != null && activeChar.getActiveWeaponItem().getItemType() == L2WeaponType.ROD)
			{
				if (skill.getSkillType() == SkillType.PUMPING)
				{
					// Pumping skill is available only while fishing
					activeChar.sendPacket(new SystemMessage(SystemMessageId.CAN_USE_PUMPING_ONLY_WHILE_FISHING));
				}
				else if (skill.getSkillType() == SkillType.REELING)
				{
					// Reeling skill is available only while fishing
					activeChar.sendPacket(new SystemMessage(SystemMessageId.CAN_USE_REELING_ONLY_WHILE_FISHING));
				}
				
				final SystemMessage sm = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
				sm.addString(skill.getName());
				activeChar.sendPacket(sm);
				return true;
			}
			
			if (activeChar.isHero() && HeroSkillTable.isHeroSkill(pSkill.getId()) && activeChar.isInOlympiadMode() && activeChar.isInOlympiadFight())
			{
				activeChar.sendMessage("You can't use Hero skills during Olympiad match.");
				return true;
			}
		}
		
		if (disabledSkills == null)
		{
			return false;
		}
		
		return disabledSkills.contains(pSkill.getReuseHashCode());
	}
	
	/**
	 * Disable all skills (set allSkillsDisabled to True).<BR>
	 * <BR>
	 */
	public void disableAllSkills()
	{
		if (Config.DEBUG)
		{
			LOGGER.debug("All skills disabled");
		}
		
		allSkillsDisabled = true;
	}
	
	/**
	 * Enable all skills (set allSkillsDisabled to False).<BR>
	 * <BR>
	 */
	public void enableAllSkills()
	{
		if (Config.DEBUG)
		{
			LOGGER.debug("All skills enabled");
		}
		
		allSkillsDisabled = false;
	}
	
	/**
	 * Launch the magic skill and calculate its effects on each target contained in the targets table.<BR>
	 * <BR>
	 * @param skill   The L2Skill to use
	 * @param targets The table of L2Object targets
	 */
	public void callSkill(final L2Skill skill, final L2Object[] targets)
	{
		try
		{
			if (skill.isToggle() && getFirstEffect(skill.getId()) != null)
			{
				return;
			}
			
			if (targets == null || targets.length == 0)
			{
				getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
				return;
			}
			
			// Do initial checkings for skills and set pvp flag/draw aggro when needed
			for (final L2Object target : targets)
			{
				if (target instanceof L2Character)
				{
					// Set some values inside target's instance for later use
					L2Character player = (L2Character) target;
					
					if (skill.getEffectType() == L2Skill.SkillType.BUFF)
					{
						if (player.isBlockBuff())
						{
							continue;
						}
					}
					
					/*
					 * LOGGER.info("--"+skill.getId()); L2Weapon activeWeapon = getActiveWeaponItem(); // Launch weapon Special ability skill effect if available if(activeWeapon != null && !((L2Character) target).isDead()) { if(activeWeapon.getSkillEffects(this, player, skill).length > 0 && this instanceof
					 * L2PcInstance) { sendPacket(SystemMessage.sendString("Target affected by weapon special ability!")); } }
					 */
					
					if (target instanceof L2Character)
					{
						final L2Character targ = (L2Character) target;
						
						if (ChanceSkillList.canTriggerByCast(this, targ, skill))
						{
							// Maybe launch chance skills on us
							if (chanceSkills != null)
							{
								chanceSkills.onSkillHit(targ, false, skill.isMagic(), skill.isOffensive());
							}
							// Maybe launch chance skills on target
							if (targ.getChanceSkills() != null)
							{
								targ.getChanceSkills().onSkillHit(this, true, skill.isMagic(), skill.isOffensive());
							}
						}
					}
					
					if (Config.ALLOW_RAID_BOSS_PETRIFIED && (this instanceof L2PcInstance || this instanceof L2Summon)) // Check if option is True Or False.
					{
						boolean to_be_cursed = false;
						
						// check on BossZone raid lvl
						if (!(player.getTarget() instanceof L2PlayableInstance) && !(player.getTarget() instanceof L2SummonInstance))
						{ // this must work just on mobs/raids
							
							if (player.isRaid() && getLevel() > player.getLevel() + 8 || !(player instanceof L2PcInstance) && player.getTarget() != null && player.getTarget() instanceof L2RaidBossInstance && getLevel() > ((L2RaidBossInstance) player.getTarget()).getLevel() + 8
								|| !(player instanceof L2PcInstance) && player.getTarget() != null && player.getTarget() instanceof L2GrandBossInstance && getLevel() > ((L2GrandBossInstance) player.getTarget()).getLevel() + 8)
							{
								to_be_cursed = true;
							}
							
							// advanced check too if not already cursed
							if (!to_be_cursed)
							{
								int boss_id = -1;
								L2NpcTemplate boss_template = null;
								final L2BossZone boss_zone = GrandBossManager.getInstance().getZone(this);
								
								if (boss_zone != null)
								{
									boss_id = boss_zone.getBossId();
								}
								
								// boolean alive = false;
								
								if (boss_id != -1)
								{
									boss_template = NpcTable.getInstance().getTemplate(boss_id);
									
									if (boss_template != null && getLevel() > boss_template.getLevel() + 8)
									{
										L2MonsterInstance boss_instance = null;
										
										if (boss_template.type.equals("L2RaidBoss"))
										{
											final StatsSet actual_boss_stat = RaidBossSpawnManager.getInstance().getStatsSet(boss_id);
											if (actual_boss_stat != null)
											{
												// alive = actual_boss_stat.getLong("respawnTime") == 0;
												boss_instance = RaidBossSpawnManager.getInstance().getBoss(boss_id);
											}
										}
										else if (boss_template.type.equals("L2GrandBoss"))
										{
											final StatsSet actual_boss_stat = GrandBossManager.getInstance().getStatsSet(boss_id);
											if (actual_boss_stat != null)
											{
												// alive = actual_boss_stat.getLong("respawn_time") == 0;
												boss_instance = GrandBossManager.getInstance().getBoss(boss_id);
											}
										}
										
										// max allowed rage into take cursed is 3000
										if (boss_instance != null/* && alive */ && boss_instance.isInsideRadius(this, 3000, false, false))
										{
											to_be_cursed = true;
										}
									}
								}
							}
						}
						
						if (to_be_cursed)
						{
							if (skill.isMagic())
							{
								L2Skill tempSkill = SkillTable.getInstance().getInfo(4215, 1);
								if (tempSkill != null)
								{
									abortAttack();
									abortCast();
									getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
									tempSkill.getEffects(player, this, false, false, false);
									
									if (this instanceof L2Summon)
									{
										
										final L2Summon src = (L2Summon) this;
										if (src.getOwner() != null)
										{
											src.getOwner().abortAttack();
											src.getOwner().abortCast();
											src.getOwner().getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
											tempSkill.getEffects(player, src.getOwner(), false, false, false);
										}
									}
									
								}
								else
								{
									LOGGER.warn("Skill 4215 at level 1 is missing in DP.");
								}
								
								tempSkill = null;
							}
							else
							{
								L2Skill tempSkill = SkillTable.getInstance().getInfo(4515, 1);
								if (tempSkill != null)
								{
									tempSkill.getEffects(player, this, false, false, false);
								}
								else
								{
									LOGGER.warn("Skill 4515 at level 1 is missing in DP.");
								}
								
								tempSkill = null;
								
								if (player instanceof L2MinionInstance)
								{
									((L2MinionInstance) player).getLeader().stopHating(this);
									List<L2MinionInstance> spawnedMinions = ((L2MonsterInstance) player).getSpawnedMinions();
									if (spawnedMinions != null && spawnedMinions.size() > 0)
									{
										Iterator<L2MinionInstance> itr = spawnedMinions.iterator();
										L2MinionInstance minion;
										while (itr.hasNext())
										{
											minion = itr.next();
											if (((L2Attackable) player).getMostHated() == null)
											{
												((L2AttackableAI) minion.getAI()).setGlobalAggro(-25);
												minion.clearAggroList();
												minion.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
												minion.setWalking();
											}
											if (minion != null && !minion.isDead())
											{
												((L2AttackableAI) minion.getAI()).setGlobalAggro(-25);
												minion.clearAggroList();
												minion.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
												minion.addDamage(((L2Attackable) player).getMostHated(), 100);
											}
										}
										itr = null;
										spawnedMinions = null;
										minion = null;
									}
								}
								else
								{
									List<L2MinionInstance> spawnedMinions = ((L2MonsterInstance) player).getSpawnedMinions();
									if (spawnedMinions != null && spawnedMinions.size() > 0)
									{
										Iterator<L2MinionInstance> itr = spawnedMinions.iterator();
										L2MinionInstance minion;
										while (itr.hasNext())
										{
											minion = itr.next();
											if (((L2Attackable) player).getMostHated() == null)
											{
												((L2AttackableAI) minion.getAI()).setGlobalAggro(-25);
												minion.clearAggroList();
												minion.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
												minion.setWalking();
											}
											if (minion != null && !minion.isDead())
											{
												((L2AttackableAI) minion.getAI()).setGlobalAggro(-25);
												minion.clearAggroList();
												minion.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
												minion.addDamage(((L2Attackable) player).getMostHated(), 100);
											}
										}
										itr = null;
										spawnedMinions = null;
										minion = null;
									}
								}
							}
							return;
						}
					}
					
					L2PcInstance activeChar = null;
					
					if (this instanceof L2PcInstance)
					{
						activeChar = (L2PcInstance) this;
					}
					else if (this instanceof L2Summon)
					{
						activeChar = ((L2Summon) this).getOwner();
					}
					
					if (activeChar != null)
					{
						if (skill.isOffensive())
						{
							if (player instanceof L2PcInstance || player instanceof L2Summon)
							{
								// Signets are a special case, casted on target_self but don't harm self
								if (skill.getSkillType() != L2Skill.SkillType.SIGNET && skill.getSkillType() != L2Skill.SkillType.SIGNET_CASTTIME)
								{
									player.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, activeChar);
									activeChar.updatePvPStatus(player);
								}
							}
							else if (player instanceof L2Attackable)
							{
								switch (skill.getSkillType())
								{
									case AGGREDUCE:
									case AGGREDUCE_CHAR:
									case AGGREMOVE:
										break;
									default:
										((L2Character) target).addAttackerToAttackByList(this);
										/*
										 * ((L2Character) target).getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, this); - Deprecated Notify the AI that is being attacked. It should be notified once the skill is finished in order to avoid AI action previous to skill end. IE: Backstab on monsters, the AI rotates previous to skill end so it
										 * doesn't make affect. We calculate the hit time to know when the AI should rotate.
										 */
										int hitTime = Formulas.getInstance().calcMAtkSpd(activeChar, skill, skill.getHitTime());
										if ((checkBss() || checkSps()) && !skill.isStaticHitTime() && !skill.isPotion() && skill.isMagic())
										{
											hitTime = (int) (0.70 * hitTime);
										}
										ThreadPoolManager.getInstance().scheduleGeneral(new notifyAiTaskDelayed(CtrlEvent.EVT_ATTACKED, this, target), hitTime);
										break;
								}
							}
						}
						else
						{
							if (player instanceof L2PcInstance)
							{
								// Casting non offensive skill on player with pvp flag set or with karma
								if (!player.equals(this) && (((L2PcInstance) player).getPvpFlag() > 0 || ((L2PcInstance) player).getKarma() > 0))
								{
									activeChar.updatePvPStatus();
								}
							}
							else if (player instanceof L2Attackable && !(skill.getSkillType() == L2Skill.SkillType.SUMMON) && !(skill.getSkillType() == L2Skill.SkillType.BEAST_FEED) && !(skill.getSkillType() == L2Skill.SkillType.UNLOCK) && !(skill.getSkillType() == L2Skill.SkillType.DELUXE_KEY_UNLOCK))
							{
								activeChar.updatePvPStatus(this);
							}
						}
						player = null;
						// activeWeapon = null;
					}
					activeChar = null;
				}
				if (target instanceof L2MonsterInstance)
				{
					if (!skill.isOffensive() && skill.getSkillType() != SkillType.UNLOCK && skill.getSkillType() != SkillType.SUMMON && skill.getSkillType() != SkillType.DELUXE_KEY_UNLOCK && skill.getSkillType() != SkillType.BEAST_FEED)
					{
						L2PcInstance activeChar = null;
						
						if (this instanceof L2PcInstance)
						{
							activeChar = (L2PcInstance) this;
							activeChar.updatePvPStatus(activeChar);
						}
						else if (this instanceof L2Summon)
						{
							activeChar = ((L2Summon) this).getOwner();
						}
					}
				}
			}
			
			ISkillHandler handler = null;
			
			if (skill.isToggle())
			{
				// Check if the skill effects are already in progress on the L2Character
				if (getFirstEffect(skill.getId()) != null)
				{
					handler = SkillHandler.getInstance().getSkillHandler(skill.getSkillType());
					
					if (handler != null)
					{
						handler.useSkill(this, skill, targets);
					}
					else
					{
						skill.useSkill(this, targets);
					}
					
					return;
				}
			}
			
			// Check if over-hit is possible
			if (skill.isOverhit())
			{
				// Set the "over-hit enabled" flag on each of the possible targets
				for (final L2Object target : targets)
				{
					L2Character player = (L2Character) target;
					if (player instanceof L2Attackable)
					{
						((L2Attackable) player).overhitEnabled(true);
					}
					
					player = null;
				}
			}
			
			// Get the skill handler corresponding to the skill type (PDAM, MDAM, SWEEP...) started in gameserver
			handler = SkillHandler.getInstance().getSkillHandler(skill.getSkillType());
			
			// Launch the magic skill and calculate its effects
			if (handler != null)
			{
				handler.useSkill(this, skill, targets);
			}
			else
			{
				skill.useSkill(this, targets);
			}
			
			// if the skill is a potion, must delete the potion item
			if (skill.isPotion() && this instanceof L2PlayableInstance)
			{
				Potions.delete_Potion_Item((L2PlayableInstance) this, skill.getId(), skill.getLevel());
			}
			
			if (this instanceof L2PcInstance || this instanceof L2Summon)
			{
				L2PcInstance caster = this instanceof L2PcInstance ? (L2PcInstance) this : ((L2Summon) this).getOwner();
				for (final L2Object target : targets)
				{
					if (target instanceof L2NpcInstance)
					{
						L2NpcInstance npc = (L2NpcInstance) target;
						
						for (final Quest quest : npc.getTemplate().getEventQuests(Quest.QuestEventType.ON_SKILL_USE))
						{
							quest.notifySkillUse(npc, caster, skill);
						}
						
						npc = null;
					}
				}
				
				if (skill.getAggroPoints() > 0)
				{
					for (final L2Object spMob : caster.getKnownList().getKnownObjects().values())
					{
						if (spMob instanceof L2NpcInstance)
						{
							L2NpcInstance npcMob = (L2NpcInstance) spMob;
							
							if (npcMob.isInsideRadius(caster, 1000, true, true) && npcMob.hasAI() && npcMob.getAI().getIntention() == AI_INTENTION_ATTACK)
							{
								L2Object npcTarget = npcMob.getTarget();
								
								for (final L2Object target : targets)
								{
									if (npcTarget == target || npcMob == target)
									{
										npcMob.seeSpell(caster, target, skill);
									}
								}
								
								npcTarget = null;
							}
							
							npcMob = null;
						}
					}
				}
				
				caster = null;
			}
			
			handler = null;
		}
		catch (final Exception e)
		{
			LOGGER.warn("", e);
		}
		
		if (this instanceof L2PcInstance && ((L2PcInstance) this).isMovingTaskDefined() && !skill.isPotion())
		{
			((L2PcInstance) this).startMovingTask();
		}
	}
	
	/**
	 * See spell.
	 * @param caster the caster
	 * @param target the target
	 * @param skill  the skill
	 */
	public void seeSpell(final L2PcInstance caster, final L2Object target, final L2Skill skill)
	{
		if (this instanceof L2Attackable)
		{
			((L2Attackable) this).addDamageHate(caster, 0, -skill.getAggroPoints());
		}
	}
	
	/**
	 * Return True if the L2Character is behind the target and can't be seen.<BR>
	 * <BR>
	 * @param  target the target
	 * @return        true, if is behind
	 */
	public boolean isBehind(final L2Object target)
	{
		double angleChar, angleTarget, angleDiff; //
		final double maxAngleDiff = 40;
		
		if (target == null)
		{
			return false;
		}
		
		if (target instanceof L2Character)
		{
			((L2Character) target).sendPacket(new ValidateLocation(this));
			sendPacket(new ValidateLocation((L2Character) target));
			
			L2Character target1 = (L2Character) target;
			angleChar = Util.calculateAngleFrom(target1, this);
			angleTarget = Util.convertHeadingToDegree(target1.getHeading());
			angleDiff = angleChar - angleTarget;
			
			if (angleDiff <= -360 + maxAngleDiff)
			{
				angleDiff += 360;
			}
			
			if (angleDiff >= 360 - maxAngleDiff)
			{
				angleDiff -= 360;
			}
			
			if (Math.abs(angleDiff) <= maxAngleDiff)
			{
				if (Config.DEBUG)
				{
					LOGGER.info("Char " + getName() + " is behind " + target.getName());
				}
				
				return true;
			}
			
			target1 = null;
		}
		
		return false;
	}
	
	/**
	 * Checks if is behind target.
	 * @return true, if is behind target
	 */
	public boolean isBehindTarget()
	{
		return isBehind(getTarget());
	}
	
	/**
	 * Returns true if target is in front of L2Character (shield def etc).
	 * @param  target   the target
	 * @param  maxAngle the max angle
	 * @return          true, if is facing
	 */
	public boolean isFacing(final L2Object target, final int maxAngle)
	{
		double angleChar, angleTarget, angleDiff, maxAngleDiff;
		if (target == null)
		{
			return false;
		}
		maxAngleDiff = maxAngle / 2;
		angleTarget = Util.calculateAngleFrom(this, target);
		angleChar = Util.convertHeadingToDegree(getHeading());
		angleDiff = angleChar - angleTarget;
		if (angleDiff <= -360 + maxAngleDiff)
		{
			angleDiff += 360;
		}
		if (angleDiff >= 360 - maxAngleDiff)
		{
			angleDiff -= 360;
		}
		if (Math.abs(angleDiff) <= maxAngleDiff)
		{
			return true;
		}
		return false;
	}
	
	/**
	 * Return True if the L2Character is behind the target and can't be seen.<BR>
	 * <BR>
	 * @param  target the target
	 * @return        true, if is front
	 */
	public boolean isFront(final L2Object target)
	{
		double angleChar, angleTarget, angleDiff;
		final double maxAngleDiff = 40;
		
		if (target == null)
		{
			return false;
		}
		
		if (target instanceof L2Character)
		{
			((L2Character) target).sendPacket(new ValidateLocation(this));
			sendPacket(new ValidateLocation((L2Character) target));
			
			L2Character target1 = (L2Character) target;
			angleChar = Util.calculateAngleFrom(target1, this);
			angleTarget = Util.convertHeadingToDegree(target1.getHeading());
			angleDiff = angleChar - angleTarget;
			
			if (angleDiff <= -180 + maxAngleDiff)
			{
				angleDiff += 180;
			}
			
			if (angleDiff >= 180 - maxAngleDiff)
			{
				angleDiff -= 180;
			}
			
			if (Math.abs(angleDiff) <= maxAngleDiff)
			{
				if (isBehindTarget())
				{
					return false;
				}
				
				if (Config.DEBUG)
				{
					LOGGER.info("Char " + getName() + " is side " + target.getName());
				}
				
				return true;
			}
			
			target1 = null;
		}
		
		return false;
	}
	
	/**
	 * Checks if is front target.
	 * @return true, if is front target
	 */
	public boolean isFrontTarget()
	{
		return isFront(getTarget());
	}
	
	/**
	 * Return True if the L2Character is side the target and can't be seen.<BR>
	 * <BR>
	 * @param  target the target
	 * @return        true, if is side
	 */
	public boolean isSide(final L2Object target)
	{
		if (target == null)
		{
			return false;
		}
		
		if (target instanceof L2Character)
		{
			if (isBehindTarget() || isFrontTarget())
			{
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Checks if is side target.
	 * @return true, if is side target
	 */
	public boolean isSideTarget()
	{
		return isSide(getTarget());
	}
	
	/**
	 * Return 1.<BR>
	 * <BR>
	 * @return the level mod
	 */
	public double getLevelMod()
	{
		return 1;
	}
	
	/**
	 * Sets the skill cast.
	 * @param newSkillCast the new skill cast
	 */
	public final void setSkillCast(final Future<?> newSkillCast)
	{
		skillCast = newSkillCast;
	}
	
	/**
	 * Sets the skill cast end time.
	 * @param newSkillCastEndTime the new skill cast end time
	 */
	public final void setSkillCastEndTime(final int newSkillCastEndTime)
	{
		castEndTime = newSkillCastEndTime;
		// for interrupt -12 ticks; first removing the extra second and then -200 ms
		castInterruptTime = newSkillCastEndTime - 12;
	}
	
	private Future<?> pvPRegTask;
	private long pvpFlagLasts;
	
	/**
	 * Sets the pvp flag lasts.
	 * @param time the new pvp flag lasts
	 */
	public void setPvpFlagLasts(final long time)
	{
		pvpFlagLasts = time;
	}
	
	/**
	 * Gets the pvp flag lasts.
	 * @return the pvp flag lasts
	 */
	public long getPvpFlagLasts()
	{
		return pvpFlagLasts;
	}
	
	/**
	 * Start pvp flag.
	 */
	public void startPvPFlag()
	{
		updatePvPFlag(1);
		
		pvPRegTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new PvPFlag(), 1000, 1000);
	}
	
	/**
	 * Stop pvp reg task.
	 */
	public void stopPvpRegTask()
	{
		if (pvPRegTask != null)
		{
			pvPRegTask.cancel(true);
		}
	}
	
	/**
	 * Stop pvp flag.
	 */
	public void stopPvPFlag()
	{
		stopPvpRegTask();
		
		updatePvPFlag(0);
		
		pvPRegTask = null;
	}
	
	/**
	 * Update pvp flag.
	 * @param value the value
	 */
	public void updatePvPFlag(final int value)
	{
	}
	
	/**
	 * Return a Random Damage in function of the weapon.<BR>
	 * <BR>
	 * @param  target the target
	 * @return        the random damage
	 */
	public final int getRandomDamage(final L2Character target)
	{
		final L2Weapon weaponItem = getActiveWeaponItem();
		
		if (weaponItem == null)
		{
			return 5 + (int) Math.sqrt(getLevel());
		}
		
		return weaponItem.getRandomDamage();
	}
	
	@Override
	public String toString()
	{
		return "mob " + getObjectId();
	}
	
	/**
	 * Gets the attack end time.
	 * @return the attack end time
	 */
	public int getAttackEndTime()
	{
		return attackEndTime;
	}
	
	/**
	 * Not Implemented.<BR>
	 * <BR>
	 * @return the level
	 */
	public abstract int getLevel();
	
	/**
	 * Calc stat.
	 * @param  stat   the stat
	 * @param  init   the init
	 * @param  target the target
	 * @param  skill  the skill
	 * @return        the double
	 */
	public final double calcStat(final Stats stat, final double init, final L2Character target, final L2Skill skill)
	{
		return getStat().calcStat(stat, init, target, skill);
	}
	
	/**
	 * Gets the accuracy.
	 * @return the accuracy
	 */
	public int getAccuracy()
	{
		return getStat().getAccuracy();
	}
	
	/**
	 * Gets the attack speed multiplier.
	 * @return the attack speed multiplier
	 */
	public final float getAttackSpeedMultiplier()
	{
		return getStat().getAttackSpeedMultiplier();
	}
	
	/**
	 * Gets the cON.
	 * @return the cON
	 */
	public int getCON()
	{
		return getStat().getCON();
	}
	
	/**
	 * Gets the dEX.
	 * @return the dEX
	 */
	public int getDEX()
	{
		return getStat().getDEX();
	}
	
	/**
	 * Gets the critical dmg.
	 * @param  target the target
	 * @param  init   the init
	 * @return        the critical dmg
	 */
	public final double getCriticalDmg(final L2Character target, final double init)
	{
		return getStat().getCriticalDmg(target, init);
	}
	
	/**
	 * Gets the critical hit.
	 * @param  target the target
	 * @param  skill  the skill
	 * @return        the critical hit
	 */
	public int getCriticalHit(final L2Character target, final L2Skill skill)
	{
		return getStat().getCriticalHit(target, skill);
	}
	
	/**
	 * Gets the evasion rate.
	 * @param  target the target
	 * @return        the evasion rate
	 */
	public int getEvasionRate(final L2Character target)
	{
		return getStat().getEvasionRate(target);
	}
	
	/**
	 * Gets the iNT.
	 * @return the iNT
	 */
	public int getINT()
	{
		return getStat().getINT();
	}
	
	/**
	 * Gets the magical attack range.
	 * @param  skill the skill
	 * @return       the magical attack range
	 */
	public final int getMagicalAttackRange(final L2Skill skill)
	{
		return getStat().getMagicalAttackRange(skill);
	}
	
	/**
	 * Gets the max cp.
	 * @return the max cp
	 */
	public final int getMaxCp()
	{
		return getStat().getMaxCp();
	}
	
	/**
	 * Gets the m atk.
	 * @param  target the target
	 * @param  skill  the skill
	 * @return        the m atk
	 */
	public int getMAtk(final L2Character target, final L2Skill skill)
	{
		return getStat().getMAtk(target, skill);
	}
	
	/**
	 * Gets the m atk spd.
	 * @return the m atk spd
	 */
	public int getMAtkSpd()
	{
		return getStat().getMAtkSpd();
	}
	
	/**
	 * Gets the max mp.
	 * @return the max mp
	 */
	public int getMaxMp()
	{
		return getStat().getMaxMp();
	}
	
	/**
	 * Gets the max hp.
	 * @return the max hp
	 */
	public int getMaxHp()
	{
		return getStat().getMaxHp();
	}
	
	/**
	 * Gets the m critical hit.
	 * @param  target the target
	 * @param  skill  the skill
	 * @return        the m critical hit
	 */
	public final int getMCriticalHit(final L2Character target, final L2Skill skill)
	{
		return getStat().getMCriticalHit(target, skill);
	}
	
	/**
	 * Gets the m def.
	 * @param  target the target
	 * @param  skill  the skill
	 * @return        the m def
	 */
	public int getMDef(final L2Character target, final L2Skill skill)
	{
		return getStat().getMDef(target, skill);
	}
	
	/**
	 * Gets the mEN.
	 * @return the mEN
	 */
	public int getMEN()
	{
		return getStat().getMEN();
	}
	
	/**
	 * Gets the m reuse rate.
	 * @param  skill the skill
	 * @return       the m reuse rate
	 */
	public double getMReuseRate(final L2Skill skill)
	{
		return getStat().getMReuseRate(skill);
	}
	
	/**
	 * Gets the movement speed multiplier.
	 * @return the movement speed multiplier
	 */
	public float getMovementSpeedMultiplier()
	{
		return getStat().getMovementSpeedMultiplier();
	}
	
	/**
	 * Gets the p atk.
	 * @param  target the target
	 * @return        the p atk
	 */
	public int getPAtk(final L2Character target)
	{
		return getStat().getPAtk(target);
	}
	
	/**
	 * Gets the p atk animals.
	 * @param  target the target
	 * @return        the p atk animals
	 */
	public double getPAtkAnimals(final L2Character target)
	{
		return getStat().getPAtkAnimals(target);
	}
	
	/**
	 * Gets the p atk dragons.
	 * @param  target the target
	 * @return        the p atk dragons
	 */
	public double getPAtkDragons(final L2Character target)
	{
		return getStat().getPAtkDragons(target);
	}
	
	/**
	 * Gets the p atk angels.
	 * @param  target the target
	 * @return        the p atk angels
	 */
	public double getPAtkAngels(final L2Character target)
	{
		return getStat().getPAtkAngels(target);
	}
	
	/**
	 * Gets the p atk insects.
	 * @param  target the target
	 * @return        the p atk insects
	 */
	public double getPAtkInsects(final L2Character target)
	{
		return getStat().getPAtkInsects(target);
	}
	
	/**
	 * Gets the p atk monsters.
	 * @param  target the target
	 * @return        the p atk monsters
	 */
	public double getPAtkMonsters(final L2Character target)
	{
		return getStat().getPAtkMonsters(target);
	}
	
	/**
	 * Gets the p atk plants.
	 * @param  target the target
	 * @return        the p atk plants
	 */
	public double getPAtkPlants(final L2Character target)
	{
		return getStat().getPAtkPlants(target);
	}
	
	/**
	 * Gets the p atk spd.
	 * @return the p atk spd
	 */
	public int getPAtkSpd()
	{
		return getStat().getPAtkSpd();
	}
	
	/**
	 * Gets the p atk undead.
	 * @param  target the target
	 * @return        the p atk undead
	 */
	public double getPAtkUndead(final L2Character target)
	{
		return getStat().getPAtkUndead(target);
	}
	
	/**
	 * Gets the p def undead.
	 * @param  target the target
	 * @return        the p def undead
	 */
	public double getPDefUndead(final L2Character target)
	{
		return getStat().getPDefUndead(target);
	}
	
	/**
	 * Gets the p def plants.
	 * @param  target the target
	 * @return        the p def plants
	 */
	public double getPDefPlants(final L2Character target)
	{
		return getStat().getPDefPlants(target);
	}
	
	/**
	 * Gets the p def insects.
	 * @param  target the target
	 * @return        the p def insects
	 */
	public double getPDefInsects(final L2Character target)
	{
		return getStat().getPDefInsects(target);
	}
	
	/**
	 * Gets the p def animals.
	 * @param  target the target
	 * @return        the p def animals
	 */
	public double getPDefAnimals(final L2Character target)
	{
		return getStat().getPDefAnimals(target);
	}
	
	/**
	 * Gets the p def monsters.
	 * @param  target the target
	 * @return        the p def monsters
	 */
	public double getPDefMonsters(final L2Character target)
	{
		return getStat().getPDefMonsters(target);
	}
	
	/**
	 * Gets the p def dragons.
	 * @param  target the target
	 * @return        the p def dragons
	 */
	public double getPDefDragons(final L2Character target)
	{
		return getStat().getPDefDragons(target);
	}
	
	/**
	 * Gets the p def angels.
	 * @param  target the target
	 * @return        the p def angels
	 */
	public double getPDefAngels(final L2Character target)
	{
		return getStat().getPDefAngels(target);
	}
	
	/**
	 * Gets the p def.
	 * @param  target the target
	 * @return        the p def
	 */
	public int getPDef(final L2Character target)
	{
		return getStat().getPDef(target);
	}
	
	/**
	 * Gets the p atk giants.
	 * @param  target the target
	 * @return        the p atk giants
	 */
	public double getPAtkGiants(final L2Character target)
	{
		return getStat().getPAtkGiants(target);
	}
	
	/**
	 * Gets the p atk magic creatures.
	 * @param  target the target
	 * @return        the p atk magic creatures
	 */
	public double getPAtkMagicCreatures(final L2Character target)
	{
		return getStat().getPAtkMagicCreatures(target);
	}
	
	/**
	 * Gets the p def giants.
	 * @param  target the target
	 * @return        the p def giants
	 */
	public double getPDefGiants(final L2Character target)
	{
		return getStat().getPDefGiants(target);
	}
	
	/**
	 * Gets the p def magic creatures.
	 * @param  target the target
	 * @return        the p def magic creatures
	 */
	public double getPDefMagicCreatures(final L2Character target)
	{
		return getStat().getPDefMagicCreatures(target);
	}
	
	/**
	 * Gets the physical attack range.
	 * @return the physical attack range
	 */
	public final int getPhysicalAttackRange()
	{
		return getStat().getPhysicalAttackRange();
	}
	
	/**
	 * Gets the run speed.
	 * @return the run speed
	 */
	public int getRunSpeed()
	{
		return getStat().getRunSpeed();
	}
	
	/**
	 * Gets the shld def.
	 * @return the shld def
	 */
	public final int getShldDef()
	{
		return getStat().getShldDef();
	}
	
	/**
	 * Gets the sTR.
	 * @return the sTR
	 */
	public int getSTR()
	{
		return getStat().getSTR();
	}
	
	/**
	 * Gets the walk speed.
	 * @return the walk speed
	 */
	public final int getWalkSpeed()
	{
		return getStat().getWalkSpeed();
	}
	
	/**
	 * Gets the wIT.
	 * @return the wIT
	 */
	public int getWIT()
	{
		return getStat().getWIT();
	}
	
	/**
	 * Adds the status listener.
	 * @param object the object
	 */
	public void addStatusListener(final L2Character object)
	{
		getStatus().addStatusListener(object);
	}
	
	/**
	 * Reduce current hp.
	 * @param i        the i
	 * @param attacker the attacker
	 */
	public void reduceCurrentHp(final double i, final L2Character attacker)
	{
		reduceCurrentHp(i, attacker, true);
	}
	
	/**
	 * Reduce current hp.
	 * @param i        the i
	 * @param attacker the attacker
	 * @param awake    the awake
	 */
	public void reduceCurrentHp(final double i, final L2Character attacker, final boolean awake)
	{
		if (this instanceof L2NpcInstance)
		{
			if (Config.INVUL_NPC_LIST.contains(Integer.valueOf(((L2NpcInstance) this).getNpcId())))
			{
				return;
			}
		}
		
		if (Config.L2JMOD_CHAMPION_ENABLE && isChampion() && Config.L2JMOD_CHAMPION_HP != 0)
		{
			getStatus().reduceHp(i / Config.L2JMOD_CHAMPION_HP, attacker, awake);
		}
		else if (is_advanceFlag())
		{
			getStatus().reduceHp(i / advanceMultiplier, attacker, awake);
		}
		else if (isUnkillable())
		{
			final double hpToReduce = getCurrentHp() - 1;
			if (i > getCurrentHp())
			{
				getStatus().reduceHp(hpToReduce, attacker, awake);
			}
			else
			{
				getStatus().reduceHp(i, attacker, awake);
			}
		}
		else
		{
			getStatus().reduceHp(i, attacker, awake);
		}
	}
	
	private long nextReducingHPByOverTime = -1;
	
	public void reduceCurrentHpByDamOverTime(final double i, final L2Character attacker, final boolean awake, final int period)
	{
		if (nextReducingHPByOverTime > System.currentTimeMillis())
		{
			return;
		}
		
		nextReducingHPByOverTime = System.currentTimeMillis() + period * 1000;
		reduceCurrentHp(i, attacker, awake);
		
	}
	
	private long nextReducingMPByOverTime = -1;
	
	public void reduceCurrentMpByDamOverTime(final double i, final int period)
	{
		if (nextReducingMPByOverTime > System.currentTimeMillis())
		{
			return;
		}
		
		nextReducingMPByOverTime = System.currentTimeMillis() + period * 1000;
		reduceCurrentMp(i);
		
	}
	
	/**
	 * Reduce current mp.
	 * @param i the i
	 */
	public void reduceCurrentMp(final double i)
	{
		getStatus().reduceMp(i);
	}
	
	/**
	 * Removes the status listener.
	 * @param object the object
	 */
	public void removeStatusListener(final L2Character object)
	{
		getStatus().removeStatusListener(object);
	}
	
	/**
	 * Stop hp mp regeneration.
	 */
	protected void stopHpMpRegeneration()
	{
		getStatus().stopHpMpRegeneration();
	}
	
	// Property - Public
	/**
	 * Gets the current cp.
	 * @return the current cp
	 */
	public final double getCurrentCp()
	{
		return getStatus().getCurrentCp();
	}
	
	/**
	 * Sets the current cp.
	 * @param newCp the new current cp
	 */
	public final void setCurrentCp(final Double newCp)
	{
		setCurrentCp((double) newCp);
	}
	
	/**
	 * Sets the current cp.
	 * @param newCp the new current cp
	 */
	public final void setCurrentCp(final double newCp)
	{
		getStatus().setCurrentCp(newCp);
	}
	
	/**
	 * Gets the current hp.
	 * @return the current hp
	 */
	public final double getCurrentHp()
	{
		return getStatus().getCurrentHp();
	}
	
	/**
	 * Sets the current hp.
	 * @param newHp the new current hp
	 */
	public final void setCurrentHp(final double newHp)
	{
		getStatus().setCurrentHp(newHp);
	}
	
	/**
	 * Sets the current hp direct.
	 * @param newHp the new current hp direct
	 */
	public final void setCurrentHpDirect(final double newHp)
	{
		getStatus().setCurrentHpDirect(newHp);
	}
	
	/**
	 * Sets the current cp direct.
	 * @param newCp the new current cp direct
	 */
	public final void setCurrentCpDirect(final double newCp)
	{
		getStatus().setCurrentCpDirect(newCp);
	}
	
	/**
	 * Sets the current mp direct.
	 * @param newMp the new current mp direct
	 */
	public final void setCurrentMpDirect(final double newMp)
	{
		getStatus().setCurrentMpDirect(newMp);
	}
	
	/**
	 * Sets the current hp mp.
	 * @param newHp the new hp
	 * @param newMp the new mp
	 */
	public final void setCurrentHpMp(final double newHp, final double newMp)
	{
		getStatus().setCurrentHpMp(newHp, newMp);
	}
	
	/**
	 * Gets the current mp.
	 * @return the current mp
	 */
	public final double getCurrentMp()
	{
		return getStatus().getCurrentMp();
	}
	
	/**
	 * Sets the current mp.
	 * @param newMp the new current mp
	 */
	public final void setCurrentMp(final Double newMp)
	{
		setCurrentMp((double) newMp);
	}
	
	/**
	 * Sets the current mp.
	 * @param newMp the new current mp
	 */
	public final void setCurrentMp(final double newMp)
	{
		getStatus().setCurrentMp(newMp);
	}
	
	/**
	 * Sets the ai class.
	 * @param aiClass the new ai class
	 */
	public void setAiClass(final String aiClass)
	{
		this.aiClass = aiClass;
	}
	
	/**
	 * Gets the ai class.
	 * @return the ai class
	 */
	public String getAiClass()
	{
		return aiClass;
	}
	
	/*
	 * public L2Character getLastBuffer() { return lastBuffer; }
	 */
	
	/**
	 * Sets the champion.
	 * @param champ the new champion
	 */
	public void setChampion(final boolean champ)
	{
		champion = champ;
	}
	
	/**
	 * Checks if is champion.
	 * @return true, if is champion
	 */
	public boolean isChampion()
	{
		return champion;
	}
	
	/**
	 * Gets the last heal amount.
	 * @return the last heal amount
	 */
	public int getLastHealAmount()
	{
		return lastHealAmount;
	}
	
	/*
	 * public void setLastBuffer(L2Character buffer) { lastBuffer = buffer; }
	 */
	
	/**
	 * Sets the last heal amount.
	 * @param hp the new last heal amount
	 */
	public void setLastHealAmount(final int hp)
	{
		lastHealAmount = hp;
	}
	
	public boolean is_advanceFlag()
	{
		return advanceFlag;
	}
	
	/**
	 * @param advanceFlag
	 */
	public void set_advanceFlag(final boolean advanceFlag)
	{
		this.advanceFlag = advanceFlag;
	}
	
	/**
	 * @param advanceMultiplier
	 */
	public void set_advanceMultiplier(final int advanceMultiplier)
	{
		this.advanceMultiplier = advanceMultiplier;
	}
	
	/**
	 * Check if character reflected skill.
	 * @param  skill the skill
	 * @return       true, if successful
	 */
	public boolean reflectSkill(final L2Skill skill)
	{
		final double reflect = calcStat(skill.isMagic() ? Stats.REFLECT_SKILL_MAGIC : Stats.REFLECT_SKILL_PHYSIC, 0, null, null);
		
		if (Rnd.get(100) < reflect)
		{
			return true;
		}
		
		return false;
	}
	
	/**
	 * Vengeance skill.
	 * @param  skill the skill
	 * @return       true, if successful
	 */
	public boolean vengeanceSkill(final L2Skill skill)
	{
		if (!skill.isMagic() && skill.getCastRange() <= 40)
		{
			final double venganceChance = calcStat(Stats.VENGEANCE_SKILL_PHYSICAL_DAMAGE, 0, null, skill);
			if (venganceChance > Rnd.get(100))
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Send system message about damage.<BR>
	 * <BR>
	 * <B><U> Overriden in </U> :</B><BR>
	 * <BR>
	 * <li>L2PcInstance
	 * <li>L2SummonInstance
	 * <li>L2PetInstance</li><BR>
	 * <BR>
	 * @param target the target
	 * @param damage the damage
	 * @param mcrit  the mcrit
	 * @param pcrit  the pcrit
	 * @param miss   the miss
	 */
	public void sendDamageMessage(final L2Character target, final int damage, final boolean mcrit, final boolean pcrit, final boolean miss)
	{
	}
	
	/**
	 * Gets the force buff.
	 * @return the force buff
	 */
	public ForceBuff getForceBuff()
	{
		return forceBuff;
	}
	
	/**
	 * Sets the force buff.
	 * @param fb the new force buff
	 */
	public void setForceBuff(final ForceBuff fb)
	{
		forceBuff = fb;
	}
	
	/**
	 * Checks if is fear immune.
	 * @return true, if is fear immune
	 */
	public boolean isFearImmune()
	{
		return false;
	}
	
	/**
	 * Restore hpmp.
	 */
	public void restoreHPMP()
	{
		getStatus().setCurrentHpMp(getMaxHp(), getMaxMp());
	}
	
	/**
	 * Restore cp.
	 */
	public void restoreCP()
	{
		getStatus().setCurrentCp(getMaxCp());
	}
	
	/**
	 * Block.
	 */
	public void block()
	{
		blocked = true;
	}
	
	/**
	 * Unblock.
	 */
	public void unblock()
	{
		blocked = false;
	}
	
	/**
	 * Checks if is blocked.
	 * @return true, if is blocked
	 */
	public boolean isBlocked()
	{
		return blocked;
	}
	
	/**
	 * Checks if is meditated.
	 * @return true, if is meditated
	 */
	public boolean isMeditated()
	{
		return meditated;
	}
	
	/**
	 * Sets the meditated.
	 * @param meditated the new meditated
	 */
	public void setMeditated(final boolean meditated)
	{
		this.meditated = meditated;
	}
	
	/**
	 * Update attack stance.
	 */
	public void updateAttackStance()
	{
		attackStance = System.currentTimeMillis();
	}
	
	/**
	 * Gets the attack stance.
	 * @return the attack stance
	 */
	public long getAttackStance()
	{
		return attackStance;
	}
	
	private boolean petrified = false;
	
	/**
	 * Checks if is petrified.
	 * @return the petrified
	 */
	public boolean isPetrified()
	{
		return petrified;
	}
	
	/**
	 * Sets the petrified.
	 * @param petrified the petrified to set
	 */
	public void setPetrified(final boolean petrified)
	{
		if (petrified)
		{
			setIsParalyzed(petrified);
			setIsInvul(petrified);
			this.petrified = petrified;
		}
		else
		{
			this.petrified = petrified;
			setIsParalyzed(petrified);
			setIsInvul(petrified);
		}
	}
	
	/**
	 * Check bss.
	 * @return true, if successful
	 */
	public boolean checkBss()
	{
		
		boolean bss = false;
		
		final L2ItemInstance weaponInst = getActiveWeaponInstance();
		
		if (weaponInst != null)
		{
			if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT)
			{
				bss = true;
				// ponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
			}
			
		}
		// If there is no weapon equipped, check for an active summon.
		else if (this instanceof L2Summon)
		{
			final L2Summon activeSummon = (L2Summon) this;
			
			if (activeSummon.getChargedSpiritShot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT)
			{
				bss = true;
				// activeSummon.setChargedSpiritShot(L2ItemInstance.CHARGED_NONE);
			}
			
		}
		
		return bss;
	}
	
	/**
	 * Removes the bss.
	 */
	synchronized public void removeBss()
	{
		
		final L2ItemInstance weaponInst = getActiveWeaponInstance();
		
		if (weaponInst != null)
		{
			if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT)
			{
				weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
			}
			
		}
		// If there is no weapon equipped, check for an active summon.
		else if (this instanceof L2Summon)
		{
			final L2Summon activeSummon = (L2Summon) this;
			
			if (activeSummon.getChargedSpiritShot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT)
			{
				activeSummon.setChargedSpiritShot(L2ItemInstance.CHARGED_NONE);
			}
			
		}
		
		reloadShots(true);
	}
	
	/**
	 * Check sps.
	 * @return true, if successful
	 */
	public boolean checkSps()
	{
		
		boolean ss = false;
		
		final L2ItemInstance weaponInst = getActiveWeaponInstance();
		
		if (weaponInst != null)
		{
			if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_SPIRITSHOT)
			{
				ss = true;
				// weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
			}
		}
		// If there is no weapon equipped, check for an active summon.
		else if (this instanceof L2Summon)
		{
			final L2Summon activeSummon = (L2Summon) this;
			
			if (activeSummon.getChargedSpiritShot() == L2ItemInstance.CHARGED_SPIRITSHOT)
			{
				ss = true;
				// activeSummon.setChargedSpiritShot(L2ItemInstance.CHARGED_NONE);
			}
		}
		
		return ss;
		
	}
	
	/**
	 * Removes the sps.
	 */
	synchronized public void removeSps()
	{
		
		final L2ItemInstance weaponInst = getActiveWeaponInstance();
		
		if (weaponInst != null)
		{
			if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_SPIRITSHOT)
			{
				weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
			}
		}
		// If there is no weapon equipped, check for an active summon.
		else if (this instanceof L2Summon)
		{
			final L2Summon activeSummon = (L2Summon) this;
			
			if (activeSummon.getChargedSpiritShot() == L2ItemInstance.CHARGED_SPIRITSHOT)
			{
				activeSummon.setChargedSpiritShot(L2ItemInstance.CHARGED_NONE);
			}
		}
		
		reloadShots(true);
	}
	
	/**
	 * Check ss.
	 * @return true, if successful
	 */
	public boolean checkSs()
	{
		
		boolean ss = false;
		
		final L2ItemInstance weaponInst = getActiveWeaponInstance();
		
		if (weaponInst != null)
		{
			if (weaponInst.getChargedSoulshot() == L2ItemInstance.CHARGED_SOULSHOT)
			{
				ss = true;
				// weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
			}
		}
		// If there is no weapon equipped, check for an active summon.
		else if (this instanceof L2Summon)
		{
			final L2Summon activeSummon = (L2Summon) this;
			
			if (activeSummon.getChargedSoulShot() == L2ItemInstance.CHARGED_SOULSHOT)
			{
				ss = true;
				// activeSummon.setChargedSpiritShot(L2ItemInstance.CHARGED_NONE);
			}
		}
		
		return ss;
		
	}
	
	/**
	 * Removes the ss.
	 */
	public void removeSs()
	{
		
		final L2ItemInstance weaponInst = getActiveWeaponInstance();
		
		if (weaponInst != null)
		{
			if (weaponInst.getChargedSoulshot() == L2ItemInstance.CHARGED_SOULSHOT)
			{
				weaponInst.setChargedSoulshot(L2ItemInstance.CHARGED_NONE);
			}
		}
		// If there is no weapon equipped, check for an active summon.
		else if (this instanceof L2Summon)
		{
			final L2Summon activeSummon = (L2Summon) this;
			
			if (activeSummon.getChargedSoulShot() == L2ItemInstance.CHARGED_SOULSHOT)
			{
				activeSummon.setChargedSoulShot(L2ItemInstance.CHARGED_NONE);
			}
		}
		reloadShots(false);
	}
	
	/**
	 * Return a multiplier based on weapon random damage<BR>
	 * <BR>
	 * .
	 * @return the random damage multiplier
	 */
	public final double getRandomDamageMultiplier()
	{
		final L2Weapon activeWeapon = getActiveWeaponItem();
		int random;
		
		if (activeWeapon != null)
		{
			random = activeWeapon.getRandomDamage();
		}
		else
		{
			random = 5 + (int) Math.sqrt(getLevel());
		}
		
		return 1 + (double) Rnd.get(0 - random, random) / 100;
	}
	
	/**
	 * Sets the checks if is buff protected.
	 * @param value the new checks if is buff protected
	 */
	public final void setIsBuffProtected(final boolean value)
	{
		isBuffProtected = value;
	}
	
	/**
	 * Checks if is buff protected.
	 * @return true, if is buff protected
	 */
	public boolean isBuffProtected()
	{
		return isBuffProtected;
	}
	
	public Map<Integer, L2Skill> get_triggeredSkills()
	{
		return triggeredSkills;
	}
	
	/**
	 * Set target of L2Attackable and update it.
	 * @author:               Nefer
	 * @param   trasformedNpc
	 */
	public void setTargetTrasformedNpc(final L2Attackable trasformedNpc)
	{
		if (trasformedNpc == null)
		{
			return;
		}
		
		// Set the target of the L2PcInstance player
		setTarget(trasformedNpc);
		
		// Send a Server->Client packet MyTargetSelected to the L2PcInstance player
		// The player.getLevel() - getLevel() permit to display the correct color in the select window
		MyTargetSelected my = new MyTargetSelected(trasformedNpc.getObjectId(), getLevel() - trasformedNpc.getLevel());
		sendPacket(my);
		my = null;
		
		// Send a Server->Client packet StatusUpdate of the L2NpcInstance to the L2PcInstance to update its HP bar
		StatusUpdate su = new StatusUpdate(trasformedNpc.getObjectId());
		su.addAttribute(StatusUpdate.CUR_HP, (int) trasformedNpc.getCurrentHp());
		su.addAttribute(StatusUpdate.MAX_HP, trasformedNpc.getMaxHp());
		sendPacket(su);
		su = null;
	}
	
	/**
	 * @return if the object can be killed
	 */
	public boolean isUnkillable()
	{
		return isUnkillable;
	}
	
	public void setIsUnkillable(final boolean value)
	{
		isUnkillable = value;
	}
	
	public boolean isAttackDisabled()
	{
		return isAttackDisabled;
	}
	
	public void setIsAttackDisabled(final boolean value)
	{
		isAttackDisabled = value;
	}
	
	/*
	 * AI not. Task
	 */
	static class notifyAiTaskDelayed implements Runnable
	{
		
		CtrlEvent event;
		Object object;
		L2Object tgt;
		
		notifyAiTaskDelayed(final CtrlEvent evt, final Object obj, final L2Object target)
		{
			event = evt;
			object = obj;
			tgt = target;
		}
		
		@Override
		public void run()
		{
			((L2Character) tgt).getAI().notifyEvent(event, object);
		}
		
	}
	
	synchronized public void reloadShots(final boolean isMagic)
	{
		if (this instanceof L2PcInstance)
		{
			((L2PcInstance) this).rechargeAutoSoulShot(!isMagic, isMagic, false);
		}
		else if (this instanceof L2Summon)
		{
			((L2Summon) this).getOwner().rechargeAutoSoulShot(!isMagic, isMagic, true);
		}
	}
}
