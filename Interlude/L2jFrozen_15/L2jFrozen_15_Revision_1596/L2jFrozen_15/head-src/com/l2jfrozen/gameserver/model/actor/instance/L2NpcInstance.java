package com.l2jfrozen.gameserver.model.actor.instance;

import static com.l2jfrozen.gameserver.ai.CtrlIntention.AI_INTENTION_ACTIVE;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.ai.CtrlIntention;
import com.l2jfrozen.gameserver.cache.HtmCache;
import com.l2jfrozen.gameserver.datatables.MobGroupTable;
import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.datatables.sql.ClanTable;
import com.l2jfrozen.gameserver.datatables.sql.HelperBuffTable;
import com.l2jfrozen.gameserver.datatables.sql.ItemTable;
import com.l2jfrozen.gameserver.datatables.sql.SpawnTable;
import com.l2jfrozen.gameserver.idfactory.IdFactory;
import com.l2jfrozen.gameserver.managers.CastleManager;
import com.l2jfrozen.gameserver.managers.CustomNpcInstanceManager;
import com.l2jfrozen.gameserver.managers.DimensionalRiftManager;
import com.l2jfrozen.gameserver.managers.FortManager;
import com.l2jfrozen.gameserver.managers.QuestManager;
import com.l2jfrozen.gameserver.managers.TownManager;
import com.l2jfrozen.gameserver.model.L2Attackable;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Clan;
import com.l2jfrozen.gameserver.model.L2DropCategory;
import com.l2jfrozen.gameserver.model.L2DropData;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.L2Skill.SkillType;
import com.l2jfrozen.gameserver.model.L2Summon;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.knownlist.NpcKnownList;
import com.l2jfrozen.gameserver.model.actor.stat.NpcStat;
import com.l2jfrozen.gameserver.model.actor.status.NpcStatus;
import com.l2jfrozen.gameserver.model.entity.event.CTF;
import com.l2jfrozen.gameserver.model.entity.event.DM;
import com.l2jfrozen.gameserver.model.entity.event.L2Event;
import com.l2jfrozen.gameserver.model.entity.event.Lottery;
import com.l2jfrozen.gameserver.model.entity.event.TvT;
import com.l2jfrozen.gameserver.model.entity.olympiad.Olympiad;
import com.l2jfrozen.gameserver.model.entity.sevensigns.SevenSigns;
import com.l2jfrozen.gameserver.model.entity.sevensigns.SevenSignsFestival;
import com.l2jfrozen.gameserver.model.entity.siege.Castle;
import com.l2jfrozen.gameserver.model.entity.siege.Fort;
import com.l2jfrozen.gameserver.model.multisell.L2Multisell;
import com.l2jfrozen.gameserver.model.quest.Quest;
import com.l2jfrozen.gameserver.model.quest.QuestState;
import com.l2jfrozen.gameserver.model.scripts.L2RBManager;
import com.l2jfrozen.gameserver.model.spawn.L2Spawn;
import com.l2jfrozen.gameserver.model.zone.type.L2TownZone;
import com.l2jfrozen.gameserver.network.L2GameClient;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.ExShowVariationCancelWindow;
import com.l2jfrozen.gameserver.network.serverpackets.ExShowVariationMakeWindow;
import com.l2jfrozen.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.MyTargetSelected;
import com.l2jfrozen.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jfrozen.gameserver.network.serverpackets.NpcInfo;
import com.l2jfrozen.gameserver.network.serverpackets.RadarControl;
import com.l2jfrozen.gameserver.network.serverpackets.SocialAction;
import com.l2jfrozen.gameserver.network.serverpackets.StatusUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.network.serverpackets.ValidateLocation;
import com.l2jfrozen.gameserver.skills.Stats;
import com.l2jfrozen.gameserver.taskmanager.DecayTaskManager;
import com.l2jfrozen.gameserver.templates.L2HelperBuff;
import com.l2jfrozen.gameserver.templates.L2Item;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;
import com.l2jfrozen.gameserver.templates.L2Weapon;
import com.l2jfrozen.gameserver.templates.L2WeaponType;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;
import com.l2jfrozen.util.random.Rnd;

import javolution.text.TextBuilder;
import main.EngineModsManager;

/**
 * This class represents a Non-Player-Character in the world. It can be a monster or a friendly character. It also uses a template to fetch some static values. The templates are hardcoded in the client, so we can rely on them.<BR>
 * <BR>
 * L2Character :<BR>
 * <BR>
 * <li>L2Attackable</li>
 * <li>L2BoxInstance</li>
 * <li>L2FolkInstance</li>
 * @version $Revision: 1.32.2.7.2.24 $ $Date: 2009/04/13 09:17:09 $
 * @author  programmos, scoria dev
 */
public class L2NpcInstance extends L2Character
{
	// private static Logger LOGGER = Logger.getLogger(L2NpcInstance.class);
	
	/** The interaction distance of the L2NpcInstance(is used as offset in MovetoLocation method). */
	public static final int INTERACTION_DISTANCE = 150;
	
	/** The Polymorph object that manage this L2NpcInstance's morph to a PcInstance... I wrote this line too =P Darki699 */
	private L2CustomNpcInstance customNpcInstance;
	
	/** The L2Spawn object that manage this L2NpcInstance. */
	private L2Spawn spawnInstance;
	
	/** The flag to specify if this L2NpcInstance is busy. */
	private boolean isBusy = false;
	
	/** The busy message for this L2NpcInstance. */
	private String busyMessage = "";
	
	/** True if endDecayTask has already been called. */
	volatile boolean isDecayed = false;
	
	/** True if a Dwarf has used Spoil on this L2NpcInstance. */
	private boolean isSpoil = false;
	
	/** The castle index in the array of L2Castle this L2NpcInstance belongs to. */
	private int castleIndex = -2;
	
	/** The fortress index in the array of L2Fort this L2NpcInstance belongs to. */
	private int fortIndex = -2;
	
	public boolean isEventMob = false, isEventMobTvT = false, isEventMobDM = false, isEventMobCTF = false, isCTF_throneSpawn = false, isCTF_Flag = false;
	
	private boolean isInTown = false;
	
	public String cTF_FlagTeamName;
	
	private int isSpoiledBy = 0;
	
	protected RandomAnimationTask rAniTask = null;
	
	private int currentLHandId; // normally this shouldn't change from the template, but there exist exceptions
	
	private int currentRHandId; // normally this shouldn't change from the template, but there exist exceptions
	
	private int currentCollisionHeight; // used for npc grow effect skills
	
	private int currentCollisionRadius; // used for npc grow effect skills
	
	/**
	 * Task launching the function onRandomAnimation().
	 */
	protected class RandomAnimationTask implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				if (this != rAniTask)
				{
					return; // Shouldn't happen, but who knows... just to make sure every active npc has only one timer.
				}
				
				if (isMob())
				{
					// Cancel further animation timers until intention is changed to ACTIVE again.
					if (getAI().getIntention() != AI_INTENTION_ACTIVE)
					{
						return;
					}
				}
				else
				{
					if (!isInActiveRegion())
					{
						return;
					}
					// update knownlist to remove playable which aren't in range any more
					getKnownList().updateKnownObjects();
				}
				
				if (!(isDead() || isStunned() || isSleeping() || isParalyzed()))
				{
					onRandomAnimation();
				}
				
				startRandomAnimationTimer();
			}
			catch (final Throwable t)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					t.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Send a packet SocialAction to all L2PcInstance in the knownPlayers of the L2NpcInstance and create a new RandomAnimation Task.<BR>
	 * <BR>
	 */
	public void onRandomAnimation()
	{
		final int min = customNpcInstance != null ? 1 : 2;
		final int max = customNpcInstance != null ? 13 : 3;
		// Send a packet SocialAction to all L2PcInstance in the nownPlayers of the L2NpcInstance
		SocialAction sa = new SocialAction(getObjectId(), Rnd.get(min, max));
		broadcastPacket(sa);
		sa = null;
	}
	
	/**
	 * Create a RandomAnimation Task that will be launched after the calculated delay.<BR>
	 * <BR>
	 */
	public void startRandomAnimationTimer()
	{
		if (!hasRandomAnimation())
		{
			return;
		}
		
		final int minWait = isMob() ? Config.MIN_MONSTER_ANIMATION : Config.MIN_NPC_ANIMATION;
		final int maxWait = isMob() ? Config.MAX_MONSTER_ANIMATION : Config.MAX_NPC_ANIMATION;
		
		// Calculate the delay before the next animation
		final int interval = Rnd.get(minWait, maxWait) * 1000;
		
		// Create a RandomAnimation Task that will be launched after the calculated delay
		rAniTask = new RandomAnimationTask();
		ThreadPoolManager.getInstance().scheduleGeneral(rAniTask, interval);
	}
	
	/**
	 * Check if the server allows Random Animation.<BR>
	 * <BR>
	 * @return true, if successful
	 */
	public boolean hasRandomAnimation()
	{
		return Config.MAX_NPC_ANIMATION > 0;
	}
	
	/**
	 * The Class destroyTemporalNPC.
	 */
	public class DestroyTemporalNPC implements Runnable
	{
		
		private final L2Spawn oldSpawn;
		
		/**
		 * Instantiates a new destroy temporal npc.
		 * @param spawn the spawn
		 */
		public DestroyTemporalNPC(final L2Spawn spawn)
		{
			oldSpawn = spawn;
		}
		
		@Override
		public void run()
		{
			try
			{
				oldSpawn.getLastSpawn().deleteMe();
				oldSpawn.stopRespawn();
				SpawnTable.getInstance().deleteSpawn(oldSpawn, false);
			}
			catch (final Throwable t)
			{
				t.printStackTrace();
			}
		}
	}
	
	/**
	 * The Class destroyTemporalSummon.
	 */
	public class DestroyTemporalSummon implements Runnable
	{
		
		L2Summon summon;
		L2PcInstance player;
		
		/**
		 * Instantiates a new destroy temporal summon.
		 * @param summon the summon
		 * @param player the player
		 */
		public DestroyTemporalSummon(final L2Summon summon, final L2PcInstance player)
		{
			this.summon = summon;
			this.player = player;
		}
		
		@Override
		public void run()
		{
			summon.unSummon(player);
			summon = null;
			player = null;
		}
	}
	
	/**
	 * Constructor of L2NpcInstance (use L2Character constructor).<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Call the L2Character constructor to set the template of the L2Character (copy skills from template to object and link calculators to NPC_STD_CALCULATOR)</li>
	 * <li>Set the name of the L2Character</li>
	 * <li>Create a RandomAnimation Task that will be launched after the calculated delay if the server allow it</li><BR>
	 * <BR>
	 * @param objectId Identifier of the object to initialized
	 * @param template The L2NpcTemplate to apply to the NPC
	 */
	public L2NpcInstance(final int objectId, final L2NpcTemplate template)
	{
		// Call the L2Character constructor to set the template of the L2Character, copy skills from template to object
		// and link calculators to NPC_STD_CALCULATOR
		super(objectId, template);
		getKnownList(); // init knownlist
		getStat(); // init stats
		getStatus(); // init status
		initCharStatusUpdateValues();
		
		// initialize the "current" equipment
		currentLHandId = getTemplate().lhand;
		currentRHandId = getTemplate().rhand;
		// initialize the "current" collisions
		currentCollisionHeight = getTemplate().collisionHeight;
		currentCollisionRadius = getTemplate().collisionRadius;
		
		// Set the name of the L2Character
		setName(template.name);
		
	}
	
	@Override
	public NpcKnownList getKnownList()
	{
		if (super.getKnownList() == null || !(super.getKnownList() instanceof NpcKnownList))
		{
			setKnownList(new NpcKnownList(this));
		}
		
		return (NpcKnownList) super.getKnownList();
	}
	
	@Override
	public NpcStat getStat()
	{
		if (super.getStat() == null || !(super.getStat() instanceof NpcStat))
		{
			setStat(new NpcStat(this));
		}
		
		return (NpcStat) super.getStat();
	}
	
	@Override
	public NpcStatus getStatus()
	{
		if (super.getStatus() == null || !(super.getStatus() instanceof NpcStatus))
		{
			setStatus(new NpcStatus(this));
		}
		
		return (NpcStatus) super.getStatus();
	}
	
	/**
	 * Return the L2NpcTemplate of the L2NpcInstance.
	 * @return the template
	 */
	@Override
	public final L2NpcTemplate getTemplate()
	{
		return (L2NpcTemplate) super.getTemplate();
	}
	
	/**
	 * Return the generic Identifier of this L2NpcInstance contained in the L2NpcTemplate.<BR>
	 * <BR>
	 * @return the npc id
	 */
	public int getNpcId()
	{
		return getTemplate().npcId;
	}
	
	@Override
	public boolean isAttackable()
	{
		if (Config.NPC_ATTACKABLE || this instanceof L2Attackable)
		{
			return true;
		}
		
		return false;
	}
	
	/**
	 * Return the faction Identifier of this L2NpcInstance contained in the L2NpcTemplate.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * If a NPC belows to a Faction, other NPC of the faction inside the Faction range will help it if it's attacked<BR>
	 * <BR>
	 * @return the faction id
	 */
	public final String getFactionId()
	{
		return getTemplate().factionId;
	}
	
	/**
	 * Return the Level of this L2NpcInstance contained in the L2NpcTemplate.<BR>
	 * <BR>
	 * @return the level
	 */
	@Override
	public final int getLevel()
	{
		return getTemplate().level;
	}
	
	/**
	 * Return True if the L2NpcInstance is agressive (ex : L2MonsterInstance in function of aggroRange).<BR>
	 * <BR>
	 * @return true, if is aggressive
	 */
	public boolean isAggressive()
	{
		return false;
	}
	
	/**
	 * Return the Aggro Range of this L2NpcInstance contained in the L2NpcTemplate.<BR>
	 * <BR>
	 * @return the aggro range
	 */
	public int getAggroRange()
	{
		return getTemplate().aggroRange;
	}
	
	/**
	 * Return the Faction Range of this L2NpcInstance contained in the L2NpcTemplate.<BR>
	 * <BR>
	 * @return the faction range
	 */
	public int getFactionRange()
	{
		return getTemplate().factionRange;
	}
	
	/**
	 * Return True if this L2NpcInstance is undead in function of the L2NpcTemplate.<BR>
	 * <BR>
	 * @return true, if is undead
	 */
	@Override
	public boolean isUndead()
	{
		return getTemplate().isUndead;
	}
	
	/**
	 * Send a packet NpcInfo with state of abnormal effect to all L2PcInstance in the knownPlayers of the L2NpcInstance.<BR>
	 * <BR>
	 */
	@Override
	public void updateAbnormalEffect()
	{
		// NpcInfo info = new NpcInfo(this);
		// broadcastPacket(info);
		
		// Send a Server->Client packet NpcInfo with state of abnormal effect to all L2PcInstance in the knownPlayers of the L2NpcInstance
		for (final L2PcInstance player : getKnownList().getKnownPlayers().values())
		{
			if (player != null)
			{
				player.sendPacket(new NpcInfo(this, player));
			}
		}
	}
	
	/**
	 * Return the distance under which the object must be add to knownObject in function of the object type.<BR>
	 * <BR>
	 * <B><U> Values </U> :</B><BR>
	 * <BR>
	 * <li>object is a L2FolkInstance : 0 (don't remember it)</li>
	 * <li>object is a L2Character : 0 (don't remember it)</li>
	 * <li>object is a L2PlayableInstance : 1500</li>
	 * <li>others : 500</li><BR>
	 * <BR>
	 * <B><U> Overriden in </U> :</B><BR>
	 * <BR>
	 * <li>L2Attackable</li><BR>
	 * <BR>
	 * @param  object The Object to add to knownObject
	 * @return        the distance to watch object
	 */
	public int getDistanceToWatchObject(final L2Object object)
	{
		if (object instanceof L2FestivalGuideInstance)
		{
			return 10000;
		}
		
		if (object instanceof L2FolkInstance || !(object instanceof L2Character))
		{
			return 0;
		}
		
		if (object instanceof L2PlayableInstance)
		{
			return 1500;
		}
		
		return 500;
	}
	
	/**
	 * Return the distance after which the object must be remove from knownObject in function of the object type.<BR>
	 * <BR>
	 * <B><U> Values </U> :</B><BR>
	 * <BR>
	 * <li>object is not a L2Character : 0 (don't remember it)</li>
	 * <li>object is a L2FolkInstance : 0 (don't remember it)</li>
	 * <li>object is a L2PlayableInstance : 3000</li>
	 * <li>others : 1000</li><BR>
	 * <BR>
	 * <B><U> Overriden in </U> :</B><BR>
	 * <BR>
	 * <li>L2Attackable</li><BR>
	 * <BR>
	 * @param  object The Object to remove from knownObject
	 * @return        the distance to forget object
	 */
	public int getDistanceToForgetObject(final L2Object object)
	{
		return 2 * getDistanceToWatchObject(object);
	}
	
	/**
	 * Return False.<BR>
	 * <BR>
	 * <B><U> Overriden in </U> :</B><BR>
	 * <BR>
	 * <li>L2MonsterInstance : Check if the attacker is not another L2MonsterInstance</li>
	 * <li>L2PcInstance</li><BR>
	 * <BR>
	 * @param  attacker the attacker
	 * @return          true, if is auto attackable
	 */
	@Override
	public boolean isAutoAttackable(final L2Character attacker)
	{
		return false;
	}
	
	/**
	 * Return the Identifier of the item in the left hand of this L2NpcInstance contained in the L2NpcTemplate.<BR>
	 * <BR>
	 * @return the left hand item
	 */
	public int getLeftHandItem()
	{
		return currentLHandId;
	}
	
	/**
	 * Return the Identifier of the item in the right hand of this L2NpcInstance contained in the L2NpcTemplate.<BR>
	 * <BR>
	 * @return the right hand item
	 */
	public int getRightHandItem()
	{
		return currentRHandId;
	}
	
	/**
	 * Return True if this L2NpcInstance has drops that can be sweeped.<BR>
	 * <BR>
	 * @return true, if is spoil
	 */
	public boolean isSpoil()
	{
		return isSpoil;
	}
	
	/**
	 * Set the spoil state of this L2NpcInstance.<BR>
	 * <BR>
	 * @param isSpoil the new spoil
	 */
	public void setSpoil(final boolean isSpoil)
	{
		this.isSpoil = isSpoil;
	}
	
	/**
	 * Gets the checks if is spoiled by.
	 * @return the checks if is spoiled by
	 */
	public final int getIsSpoiledBy()
	{
		return isSpoiledBy;
	}
	
	/**
	 * Sets the checks if is spoiled by.
	 * @param value the new checks if is spoiled by
	 */
	public final void setIsSpoiledBy(final int value)
	{
		isSpoiledBy = value;
	}
	
	/**
	 * Return the busy status of this L2NpcInstance.<BR>
	 * <BR>
	 * @return true, if is busy
	 */
	public final boolean isBusy()
	{
		return isBusy;
	}
	
	/**
	 * Set the busy status of this L2NpcInstance.<BR>
	 * <BR>
	 * @param isBusy the new busy
	 */
	public void setBusy(final boolean isBusy)
	{
		this.isBusy = isBusy;
	}
	
	/**
	 * Return the busy message of this L2NpcInstance.<BR>
	 * <BR>
	 * @return the busy message
	 */
	public final String getBusyMessage()
	{
		return busyMessage;
	}
	
	/**
	 * Set the busy message of this L2NpcInstance.<BR>
	 * <BR>
	 * @param message the new busy message
	 */
	public void setBusyMessage(final String message)
	{
		busyMessage = message;
	}
	
	/**
	 * Can target.
	 * @param  player the player
	 * @return        true, if successful
	 */
	protected boolean canTarget(final L2PcInstance player)
	{
		if (player.isOutOfControl())
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		// TODO: More checks...
		
		return true;
	}
	
	/**
	 * Can interact.
	 * @param  player the player
	 * @return        true, if successful
	 */
	protected boolean canInteract(final L2PcInstance player)
	{
		if (player.isCastingNow())
		{
			return false;
		}
		else if (player.isDead())
		{
			return false;
		}
		else if (player.isAlikeDead())
		{
			return false;
		}
		else if (player.isSittingTaskLaunched())
		{
			return false;
		}
		else if (player.isSitting())
		{
			return false;
		}
		else if (player.isInStoreMode())
		{
			return false;
		}
		else if (!isInsideRadius(player, INTERACTION_DISTANCE, false, false))
		{
			return false;
		}
		
		return true;
	}
	
	/**
	 * Manage actions when a player click on the L2NpcInstance.<BR>
	 * <BR>
	 * <B><U> Actions on first click on the L2NpcInstance (Select it)</U> :</B><BR>
	 * <BR>
	 * <li>Set the L2NpcInstance as target of the L2PcInstance player (if necessary)</li>
	 * <li>Send a Server->Client packet MyTargetSelected to the L2PcInstance player (display the select window)</li>
	 * <li>If L2NpcInstance is autoAttackable, send a Server->Client packet StatusUpdate to the L2PcInstance in order to update L2NpcInstance HP bar</li>
	 * <li>Send a Server->Client packet ValidateLocation to correct the L2NpcInstance position and heading on the client</li><BR>
	 * <BR>
	 * <B><U> Actions on second click on the L2NpcInstance (Attack it/Intercat with it)</U> :</B><BR>
	 * <BR>
	 * <li>Send a Server->Client packet MyTargetSelected to the L2PcInstance player (display the select window)</li>
	 * <li>If L2NpcInstance is autoAttackable, notify the L2PcInstance AI with AI_INTENTION_ATTACK (after a height verification)</li>
	 * <li>If L2NpcInstance is NOT autoAttackable, notify the L2PcInstance AI with AI_INTENTION_INTERACT (after a distance verification) and show message</li><BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : Each group of Server->Client packet must be terminated by a ActionFailed packet in order to avoid that client wait an other packet</B></FONT><BR>
	 * <BR>
	 * <B><U> Example of use </U> :</B><BR>
	 * <BR>
	 * <li>Client packet : Action, AttackRequest</li><BR>
	 * <BR>
	 * <B><U> Overriden in </U> :</B><BR>
	 * <BR>
	 * <li>L2ArtefactInstance : Manage only fisrt click to select Artefact</li><BR>
	 * <BR>
	 * <li>L2GuardInstance :</li><BR>
	 * <BR>
	 * @param player The L2PcInstance that start an action on the L2NpcInstance
	 */
	@Override
	public void onAction(final L2PcInstance player)
	{
		if (!canTarget(player) || System.currentTimeMillis() - player.getTimerToAttack() < Config.CLICK_TASK)
		{
			return;
		}
		
		// Check if the L2PcInstance already target the L2NpcInstance
		if (this != player.getTarget())
		{
			if (Config.DEBUG)
			{
				LOGGER.debug("new target selected:" + getObjectId());
			}
			
			// Set the target of the L2PcInstance player
			player.setTarget(this);
			
			// Remove player spawn protection
			player.onActionRequest();
			
			// Check if the player is attackable (without a forced attack)
			if (isAutoAttackable(player))
			{
				// Send a Server->Client packet MyTargetSelected to the L2PcInstance player
				// The player.getLevel() - getLevel() permit to display the correct color in the select window
				MyTargetSelected my = new MyTargetSelected(getObjectId(), player.getLevel() - getLevel());
				player.sendPacket(my);
				my = null;
				
				// Send a Server->Client packet StatusUpdate of the L2NpcInstance to the L2PcInstance to update its HP bar
				StatusUpdate su = new StatusUpdate(getObjectId());
				su.addAttribute(StatusUpdate.CUR_HP, (int) getCurrentHp());
				su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
				player.sendPacket(su);
				su = null;
			}
			else
			{
				// Send a Server->Client packet MyTargetSelected to the L2PcInstance player
				MyTargetSelected my = new MyTargetSelected(getObjectId(), 0);
				player.sendPacket(my);
				my = null;
			}
			
			player.setTimerToAttack(System.currentTimeMillis());
			// Send a Server->Client packet ValidateLocation to correct the L2NpcInstance position and heading on the client
			player.sendPacket(new ValidateLocation(this));
		}
		else
		{
			
			player.sendPacket(new ValidateLocation(this));
			
			// Check if the player is attackable (without a forced attack) and isn't dead
			if (isAutoAttackable(player) && !isAlikeDead())
			{
				// Check the height difference
				if (Math.abs(player.getZ() - getZ()) < 400) // this max heigth difference might need some tweaking
				{
					// Set the L2PcInstance Intention to AI_INTENTION_ATTACK
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
					// player.startAttack(this);
				}
				else
				{
					// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
					player.sendPacket(ActionFailed.STATIC_PACKET);
				}
			}
			else if (!isAutoAttackable(player))
			{
				// Calculate the distance between the L2PcInstance and the L2NpcInstance
				if (!canInteract(player))
				{
					// Notify the L2PcInstance AI with AI_INTENTION_INTERACT
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
				}
				else
				{
					// Like L2OFF if char is dead, is sitting, is in trade or is in fakedeath can't interact with npc
					if (player.isSitting() || player.isDead() || player.isFakeDeath() || player.getActiveTradeList() != null)
					{
						return;
					}
					
					// Send a Server->Client packet SocialAction to the all L2PcInstance on the knownPlayer of the L2NpcInstance to display a social action of the L2NpcInstance on their client
					final SocialAction sa = new SocialAction(getObjectId(), Rnd.get(8));
					broadcastPacket(sa);
					// Open a chat window on client with the text of the L2NpcInstance
					if (isEventMob)
					{
						L2Event.showEventHtml(player, String.valueOf(getObjectId()));
					}
					else if (isEventMobTvT)
					{
						TvT.showEventHtml(player, String.valueOf(getObjectId()));
					}
					else if (isEventMobDM)
					{
						DM.showEventHtml(player, String.valueOf(getObjectId()));
					}
					else if (isEventMobCTF)
					{
						CTF.showEventHtml(player, String.valueOf(getObjectId()));
					}
					else if (isCTF_Flag && player.inEventCTF)
					{
						CTF.showFlagHtml(player, String.valueOf(getObjectId()), cTF_FlagTeamName);
					}
					else if (isCTF_throneSpawn)
					{
						CTF.checkRestoreFlags();
					}
					else
					{
						// Open a chat window on client with the text of the L2NpcInstance
						/*
						 * Quest[] qlsa = getTemplate().getEventQuests(Quest.QuestEventType.QUEST_START); if ( (qlsa != null) && qlsa.length > 0) player.setLastQuestNpcObject(getObjectId());
						 */
						
						if (EngineModsManager.onInteract(player, this))
						{
							// Send a Server->Client packet ActionFailed to the player
							player.sendPacket(ActionFailed.STATIC_PACKET);
							return;
						}
						
						final Quest[] qlst = getTemplate().getEventQuests(Quest.QuestEventType.NPC_FIRST_TALK);
						if (qlst.length == 1)
						{
							qlst[0].notifyFirstTalk(this, player);
						}
						else
						{
							showChatWindow(player, 0);
						}
					}
					// Like L2OFF player must rotate to the Npc
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, this);
				}
				
				// to avoid player stuck
				player.sendPacket(ActionFailed.STATIC_PACKET);
			}
			else
			{
				player.sendPacket(ActionFailed.STATIC_PACKET);
			}
		}
	}
	
	/**
	 * Manage and Display the GM console to modify the L2NpcInstance (GM only).<BR>
	 * <BR>
	 * <B><U> Actions (If the L2PcInstance is a GM only)</U> :</B><BR>
	 * <BR>
	 * <li>Set the L2NpcInstance as target of the L2PcInstance player (if necessary)</li>
	 * <li>Send a Server->Client packet MyTargetSelected to the L2PcInstance player (display the select window)</li>
	 * <li>If L2NpcInstance is autoAttackable, send a Server->Client packet StatusUpdate to the L2PcInstance in order to update L2NpcInstance HP bar</li>
	 * <li>Send a Server->Client NpcHtmlMessage() containing the GM console about this L2NpcInstance</li><BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : Each group of Server->Client packet must be terminated by a ActionFailed packet in order to avoid that client wait an other packet</B></FONT><BR>
	 * <BR>
	 * <B><U> Example of use </U> :</B><BR>
	 * <BR>
	 * <li>Client packet : Action</li><BR>
	 * <BR>
	 * @param client The thread that manage the player that pessed Shift and click on the L2NpcInstance
	 */
	@Override
	public void onActionShift(final L2GameClient client)
	{
		// Get the L2PcInstance corresponding to the thread
		L2PcInstance player = client.getActiveChar();
		if (player == null)
		{
			return;
		}
		
		final L2Weapon currentWeapon = player.getActiveWeaponItem();
		
		// Check if the L2PcInstance is a GM
		if (player.getAccessLevel().isGm())
		{
			// Set the target of the L2PcInstance player
			player.setTarget(this);
			
			// Send a Server->Client packet MyTargetSelected to the L2PcInstance player
			// The player.getLevel() - getLevel() permit to display the correct color in the select window
			MyTargetSelected my = new MyTargetSelected(getObjectId(), player.getLevel() - getLevel());
			player.sendPacket(my);
			my = null;
			
			// Check if the player is attackable (without a forced attack)
			if (isAutoAttackable(player))
			{
				// Send a Server->Client packet StatusUpdate of the L2NpcInstance to the L2PcInstance to update its HP bar
				StatusUpdate su = new StatusUpdate(getObjectId());
				su.addAttribute(StatusUpdate.CUR_HP, (int) getCurrentHp());
				su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
				player.sendPacket(su);
				su = null;
			}
			
			// Send a Server->Client NpcHtmlMessage() containing the GM console about this L2NpcInstance
			NpcHtmlMessage html = new NpcHtmlMessage(0);
			TextBuilder html1 = new TextBuilder("<html><body><center><font color=\"LEVEL\">NPC Information</font></center>");
			String className = getClass().getSimpleName();
			html1.append("<br>");
			
			html1.append("Instance Type: " + className + "<br1>Faction: " + getFactionId() + "<br1>Location ID: " + (getSpawn() != null ? getSpawn().getLocation() : 0) + "<br1>");
			
			if (this instanceof L2RaidBossInstance || this instanceof L2GrandBossInstance || this instanceof L2MinionInstance)
			{
				// Do nothing, those instances dont show spawn ID cause they dont exist in spawnlist or custom spawnlist table
			}
			else
			{
				html1.append("Spawn ID: " + getSpawn().getId() + " " + (getSpawn().isCustomSpawn() ? "(Custom spawn)" : "") + "<br1>");
			}
			
			if (this instanceof L2ControllableMobInstance)
			{
				html1.append("Mob Group: " + MobGroupTable.getInstance().getGroupForMob((L2ControllableMobInstance) this).getGroupId() + "<br>");
			}
			else
			{
				html1.append("Respawn Time: " + (getSpawn() != null ? getSpawn().getRespawnDelay() / 1000 + "  Seconds<br>" : "?  Seconds<br>"));
			}
			
			html1.append("<table border=\"0\" width=\"100%\">");
			html1.append("<tr><td>Object ID</td><td>" + getObjectId() + "</td><td>NPC ID</td><td>" + getTemplate().npcId + "</td></tr>");
			html1.append("<tr><td>Castle</td><td>" + getCastle().getCastleId() + "</td><td>Coords</td><td>" + getX() + "," + getY() + "," + getZ() + "</td></tr>");
			html1.append("<tr><td>Level</td><td>" + getLevel() + "</td><td>Aggro</td><td>" + (this instanceof L2Attackable ? ((L2Attackable) this).getAggroRange() : 0) + "</td></tr>");
			html1.append("</table><br>");
			
			html1.append("<font color=\"LEVEL\">Combat</font>");
			html1.append("<table border=\"0\" width=\"100%\">");
			html1.append("<tr><td>Current HP</td><td>" + String.format("%.2f", getCurrentHp()) + "</td><td>Current MP</td><td>" + String.format("%.2f", getCurrentMp()) + "</td></tr>");
			html1.append("<tr><td>Max.HP</td><td>" + (int) (getMaxHp() / getStat().calcStat(Stats.MAX_HP, 1, this, null)) + "*" + getStat().calcStat(Stats.MAX_HP, 1, this, null) + "</td><td>Max.MP</td><td>" + getMaxMp() + "</td></tr>");
			html1.append("<tr><td>P.Atk.</td><td>" + getPAtk(null) + "</td><td>M.Atk.</td><td>" + getMAtk(null, null) + "</td></tr>");
			html1.append("<tr><td>P.Def.</td><td>" + getPDef(null) + "</td><td>M.Def.</td><td>" + getMDef(null, null) + "</td></tr>");
			html1.append("<tr><td>Accuracy</td><td>" + getAccuracy() + "</td><td>Evasion</td><td>" + getEvasionRate(null) + "</td></tr>");
			html1.append("<tr><td>Critical</td><td>" + getCriticalHit(null, null) + "</td><td>Speed</td><td>" + getRunSpeed() + "</td></tr>");
			html1.append("<tr><td>Atk.Speed</td><td>" + getPAtkSpd() + "</td><td>Cast.Speed</td><td>" + getMAtkSpd() + "</td></tr>");
			html1.append("</table><br>");
			
			html1.append("<font color=\"LEVEL\">Basic Stats</font>");
			html1.append("<table border=\"0\" width=\"100%\">");
			html1.append("<tr><td>STR</td><td>" + getSTR() + "</td><td>DEX</td><td>" + getDEX() + "</td><td>CON</td><td>" + getCON() + "</td></tr>");
			html1.append("<tr><td>INT</td><td>" + getINT() + "</td><td>WIT</td><td>" + getWIT() + "</td><td>MEN</td><td>" + getMEN() + "</td></tr>");
			html1.append("</table>");
			
			html1.append("<br><center><table><tr><td><button value=\"Edit NPC\" action=\"bypass -h admin_edit_npc " + getTemplate().npcId + "\" width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"><br1></td>");
			html1.append("<td><button value=\"Kill\" action=\"bypass -h admin_kill\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><br1></tr>");
			html1.append("<tr><td><button value=\"Show DropList\" action=\"bypass -h admin_show_droplist " + getTemplate().npcId + "\" width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
			
			if (this instanceof L2MinionInstance)
			{
				// Do nothing, this instance dont show spawn ID cause they dont exist in spawnlist or custom spawnlist table
			}
			else
			{
				html1.append("<td><button value=\"Delete\" action=\"bypass -h admin_delete\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
			}
			
			html1.append("<tr><td><button value=\"Show Skillist\" action=\"bypass -h admin_show_skilllist_npc " + getTemplate().npcId + "\" width=100 height=20 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td></td></tr>");
			
			if (this instanceof L2MinionInstance)
			{
				// Do nothing, this instance dont show spawn ID cause they dont exist in spawnlist or custom spawnlist table
			}
			else
			{
				html1.append("<tr><td><button value=\"Show Spawnlist\" action=\"bypass -h admin_list_spawns " + getTemplate().getNpcId() + " -1 " + getSpawn().getId() + "\" width=100 height=20 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td></td></tr>");
			}
			
			html1.append("</table></center><br>");
			html1.append("</body></html>");
			
			html.setHtml(html1.toString());
			player.sendPacket(html);
			html = null;
			html1 = null;
			className = null;
		}
		else if (Config.ALT_GAME_VIEWNPC)
		{
			// Set the target of the L2PcInstance player
			player.setTarget(this);
			
			// Send a Server->Client packet MyTargetSelected to the L2PcInstance player
			// The player.getLevel() - getLevel() permit to display the correct color in the select window
			MyTargetSelected my = new MyTargetSelected(getObjectId(), player.getLevel() - getLevel());
			player.sendPacket(my);
			
			// Check if the player is attackable (without a forced attack)
			if (isAutoAttackable(player))
			{
				// Send a Server->Client packet StatusUpdate of the L2NpcInstance to the L2PcInstance to update its HP bar
				StatusUpdate su = new StatusUpdate(getObjectId());
				su.addAttribute(StatusUpdate.CUR_HP, (int) getCurrentHp());
				su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
				player.sendPacket(su);
			}
			
			NpcHtmlMessage html = new NpcHtmlMessage(5);
			StringBuilder htm = new StringBuilder("<html><body>");
			
			htm.append("<br><center><font color=\"LEVEL\">[Combat Stats]</font></center>");
			htm.append("<table border=0 width=\"100%\">");
			htm.append("<tr><td>Max.HP</td><td>" + (int) (getMaxHp() / getStat().calcStat(Stats.MAX_HP, 1, this, null)) + "*" + (int) getStat().calcStat(Stats.MAX_HP, 1, this, null) + "</td><td>Max.MP</td><td>" + getMaxMp() + "</td></tr>");
			htm.append("<tr><td>P.Atk.</td><td>" + getPAtk(null) + "</td><td>M.Atk.</td><td>" + getMAtk(null, null) + "</td></tr>");
			htm.append("<tr><td>P.Def.</td><td>" + getPDef(null) + "</td><td>M.Def.</td><td>" + getMDef(null, null) + "</td></tr>");
			htm.append("<tr><td>Accuracy</td><td>" + getAccuracy() + "</td><td>Evasion</td><td>" + getEvasionRate(null) + "</td></tr>");
			htm.append("<tr><td>Critical</td><td>" + getCriticalHit(null, null) + "</td><td>Speed</td><td>" + getRunSpeed() + "</td></tr>");
			htm.append("<tr><td>Atk.Speed</td><td>" + getPAtkSpd() + "</td><td>Cast.Speed</td><td>" + getMAtkSpd() + "</td></tr>");
			htm.append("<tr><td>Race</td><td>" + getTemplate().race + "</td><td></td><td></td></tr>");
			htm.append("</table>");
			
			htm.append("<br><center><font color=\"LEVEL\">[Basic Stats]</font></center>");
			htm.append("<table border=0 width=\"100%\">");
			htm.append("<tr><td>STR</td><td>" + getSTR() + "</td><td>DEX</td><td>" + getDEX() + "</td><td>CON</td><td>" + getCON() + "</td></tr>");
			htm.append("<tr><td>INT</td><td>" + getINT() + "</td><td>WIT</td><td>" + getWIT() + "</td><td>MEN</td><td>" + getMEN() + "</td></tr>");
			htm.append("</table>");
			
			htm.append("<br><center><font color=\"LEVEL\">[Drop Info]</font></center><br1>");
			htm.append("Rates legend: <font color=\"00ff00\">50%+</font> <font color=\"0000ff\">30%+</font> <font color=\"ff0000\">less than 30%</font><br1>");
			htm.append("<table border=0 width=\"100%\">");
			
			for (L2DropCategory cat : getTemplate().getDropData())
			{
				List<L2DropData> drops = cat.getAllDrops();
				
				if (drops == null)
				{
					continue;
				}
				
				for (L2DropData drop : drops)
				{
					if (drop == null || ItemTable.getInstance().getTemplate(drop.getItemId()) == null)
					{
						continue;
					}
					
					String name = ItemTable.getInstance().getTemplate(drop.getItemId()).getName();
					
					if (drop.getChance() >= 600000)
					{
						htm.append("<tr><td><font color=\"00ff00\">" + name + "</font></td><td>" + (drop.isQuestDrop() ? "Quest" : cat.isSweep() ? "Sweep" : "Drop") + "</td></tr>");
					}
					else if (drop.getChance() >= 300000)
					{
						htm.append("<tr><td><font color=\"0000ff\">" + name + "</font></td><td>" + (drop.isQuestDrop() ? "Quest" : cat.isSweep() ? "Sweep" : "Drop") + "</td></tr>");
					}
					else
					{
						htm.append("<tr><td><font color=\"ff0000\">" + name + "</font></td><td>" + (drop.isQuestDrop() ? "Quest" : cat.isSweep() ? "Sweep" : "Drop") + "</td></tr>");
					}
				}
			}
			
			htm.append("</table>");
			htm.append("</body></html>");
			
			html.setHtml(htm.toString());
			player.sendPacket(html);
		}
		else
		// Like L2OFF set the target of the L2PcInstance player
		{
			// Check if the L2PcInstance already target the L2NpcInstance
			if (this != player.getTarget())
			{
				// Set the target of the L2PcInstance player
				player.setTarget(this);
				
				// Check if the player is attackable (without a forced attack)
				if (isAutoAttackable(player))
				{
					// Send a Server->Client packet MyTargetSelected to the L2PcInstance player
					// The player.getLevel() - getLevel() permit to display the correct color in the select window
					MyTargetSelected my = new MyTargetSelected(getObjectId(), player.getLevel() - getLevel());
					player.sendPacket(my);
					my = null;
					
					// Send a Server->Client packet StatusUpdate of the L2NpcInstance to the L2PcInstance to update its HP bar
					StatusUpdate su = new StatusUpdate(getObjectId());
					su.addAttribute(StatusUpdate.CUR_HP, (int) getCurrentHp());
					su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
					player.sendPacket(su);
					su = null;
				}
				else
				{
					// Send a Server->Client packet MyTargetSelected to the L2PcInstance player
					MyTargetSelected my = new MyTargetSelected(getObjectId(), 0);
					player.sendPacket(my);
					my = null;
				}
				
				player.setTimerToAttack(System.currentTimeMillis());
				player.sendPacket(new ValidateLocation(this));
			}
			else
			{
				player.sendPacket(new ValidateLocation(this));
				// Check if the player is attackable (without a forced attack) and isn't dead
				if (isAutoAttackable(player) && !isAlikeDead())
				{
					// Check the height difference
					if (Math.abs(player.getZ() - getZ()) < 400) // this max heigth difference might need some tweaking
					{
						// Like L2OFF player must not move with shift pressed
						// Only archer can hit from long
						if (!canInteract(player) && currentWeapon != null && currentWeapon.getItemType() != L2WeaponType.BOW)
						{
							// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
							player.sendPacket(ActionFailed.STATIC_PACKET);
						}
						else if (!canInteract(player) && currentWeapon == null)
						{
							// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
							player.sendPacket(ActionFailed.STATIC_PACKET);
						}
						else
						{
							// Set the L2PcInstance Intention to AI_INTENTION_ATTACK
							player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
						}
					}
					else
					{
						// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
						player.sendPacket(ActionFailed.STATIC_PACKET);
					}
				}
				else if (!isAutoAttackable(player))
				{
					// Like L2OFF player must not move with shift pressed
					// Only archer can hit from long
					if (!canInteract(player) && currentWeapon != null && currentWeapon.getItemType() != L2WeaponType.BOW)
					{
						// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
						player.sendPacket(ActionFailed.STATIC_PACKET);
					}
					else if (!canInteract(player) && currentWeapon == null)
					{
						// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
						player.sendPacket(ActionFailed.STATIC_PACKET);
					}
					else
					{
						// Like L2OFF if char is dead, is sitting, is in trade or is in fakedeath can't interact with npc
						if (player.isSitting() || player.isDead() || player.isFakeDeath() || player.getActiveTradeList() != null)
						{
							return;
						}
						
						// Send a Server->Client packet SocialAction to the all L2PcInstance on the knownPlayer of the L2NpcInstance to display a social action of the L2NpcInstance on their client
						final SocialAction sa = new SocialAction(getObjectId(), Rnd.get(8));
						broadcastPacket(sa);
						// Open a chat window on client with the text of the L2NpcInstance
						if (isEventMob)
						{
							L2Event.showEventHtml(player, String.valueOf(getObjectId()));
						}
						else if (isEventMobTvT)
						{
							TvT.showEventHtml(player, String.valueOf(getObjectId()));
						}
						else if (isEventMobDM)
						{
							DM.showEventHtml(player, String.valueOf(getObjectId()));
						}
						else if (isEventMobCTF)
						{
							CTF.showEventHtml(player, String.valueOf(getObjectId()));
						}
						else if (isCTF_Flag && player.inEventCTF)
						{
							CTF.showFlagHtml(player, String.valueOf(getObjectId()), cTF_FlagTeamName);
						}
						else if (isCTF_throneSpawn)
						{
							CTF.checkRestoreFlags();
						}
						else
						{
							// Open a chat window on client with the text of the L2NpcInstance
							/*
							 * Quest[] qlsa = getTemplate().getEventQuests(Quest.QuestEventType.QUEST_START); if ( (qlsa != null) && qlsa.length > 0) player.setLastQuestNpcObject(getObjectId());
							 */
							
							final Quest[] qlst = getTemplate().getEventQuests(Quest.QuestEventType.NPC_FIRST_TALK);
							if (qlst.length == 1)
							{
								qlst[0].notifyFirstTalk(this, player);
							}
							else
							{
								showChatWindow(player, 0);
							}
						}
					}
					
					// to avoid player stuck
					player.sendPacket(ActionFailed.STATIC_PACKET);
				}
				else
				{
					player.sendPacket(ActionFailed.STATIC_PACKET);
				}
			}
			
			// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
			player.sendPacket(ActionFailed.STATIC_PACKET);
			
			player = null;
		}
	}
	
	/**
	 * Return the L2Castle this L2NpcInstance belongs to.
	 * @return the castle
	 */
	public final Castle getCastle()
	{
		// Get castle this NPC belongs to (excluding L2Attackable)
		if (castleIndex < 0)
		{
			final L2TownZone town = TownManager.getInstance().getTown(getX(), getY(), getZ());
			
			if (town != null)
			{
				castleIndex = CastleManager.getInstance().getCastleIndex(town.getTaxById());
			}
			
			if (castleIndex < 0)
			{
				castleIndex = CastleManager.getInstance().findNearestCastlesIndex(this);
			}
			else
			{
				isInTown = true; // Npc was spawned in town
			}
		}
		
		if (castleIndex < 0)
		{
			return null;
		}
		
		return CastleManager.getInstance().getCastles().get(castleIndex);
	}
	
	/**
	 * Return the L2Fort this L2NpcInstance belongs to.
	 * @return the fort
	 */
	public final Fort getFort()
	{
		// Get Fort this NPC belongs to (excluding L2Attackable)
		if (fortIndex < 0)
		{
			final Fort fort = FortManager.getInstance().getFort(getX(), getY(), getZ());
			if (fort != null)
			{
				fortIndex = FortManager.getInstance().getFortIndex(fort.getFortId());
			}
			if (fortIndex < 0)
			{
				fortIndex = FortManager.getInstance().findNearestFortIndex(this);
			}
		}
		if (fortIndex < 0)
		{
			return null;
		}
		return FortManager.getInstance().getForts().get(fortIndex);
	}
	
	/**
	 * Gets the checks if is in town.
	 * @return the checks if is in town
	 */
	public final boolean getIsInTown()
	{
		if (castleIndex < 0)
		{
			getCastle();
		}
		return isInTown;
	}
	
	/**
	 * Open a quest or chat window on client with the text of the L2NpcInstance in function of the command.<BR>
	 * <BR>
	 * <B><U> Example of use </U> :</B><BR>
	 * <BR>
	 * <li>Client packet : RequestBypassToServer</li><BR>
	 * <BR>
	 * @param player  the player
	 * @param command The command string received from client
	 */
	public void onBypassFeedback(final L2PcInstance player, final String command)
	{
		// if (canInteract(player))
		// {
		if (isBusy() && getBusyMessage().length() > 0)
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			html.setFile("/data/html/npcbusy.htm");
			html.replace("%busymessage%", getBusyMessage());
			html.replace("%npcname%", getName());
			html.replace("%playername%", player.getName());
			player.sendPacket(html);
			html = null;
		}
		else if (command.equalsIgnoreCase("TerritoryStatus"))
		{
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			{
				if (getCastle().getOwnerId() > 0)
				{
					html.setFile("/data/html/territorystatus.htm");
					final L2Clan clan = ClanTable.getInstance().getClan(getCastle().getOwnerId());
					html.replace("%clanname%", clan.getName());
					html.replace("%clanleadername%", clan.getLeaderName());
				}
				else
				{
					html.setFile("/data/html/territorynoclan.htm");
				}
			}
			html.replace("%castlename%", getCastle().getName());
			html.replace("%taxpercent%", "" + getCastle().getTaxPercent());
			html.replace("%objectId%", String.valueOf(getObjectId()));
			{
				if (getCastle().getCastleId() > 6)
				{
					html.replace("%territory%", "The Kingdom of Elmore");
				}
				else
				{
					html.replace("%territory%", "The Kingdom of Aden");
				}
			}
			player.sendPacket(html);
			html = null;
		}
		else if (command.startsWith("Quest"))
		{
			String quest = "";
			try
			{
				quest = command.substring(5).trim();
			}
			catch (final IndexOutOfBoundsException ioobe)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					ioobe.printStackTrace();
				}
			}
			if (quest.length() == 0)
			{
				showQuestWindow(player);
			}
			else
			{
				showQuestWindow(player, quest);
			}
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
		else if (command.startsWith("Link"))
		{
			final String path = command.substring(5).trim();
			if (path.indexOf("..") != -1)
			{
				return;
			}
			String filename = "/data/html/" + path;
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			html.setFile(filename);
			html.replace("%objectId%", String.valueOf(getObjectId()));
			player.sendPacket(html);
			filename = null;
			html = null;
		}
		else if (command.startsWith("NobleTeleport"))
		{
			if (!player.isNoble())
			{
				String filename = "/data/html/teleporter/nobleteleporter-no.htm";
				NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				html.setFile(filename);
				html.replace("%objectId%", String.valueOf(getObjectId()));
				html.replace("%npcname%", getName());
				player.sendPacket(html);
				html = null;
				filename = null;
				return;
			}
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
		else if (command.startsWith("Loto"))
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
			if (val == 0)
			{
				// new loto ticket
				for (int i = 0; i < 5; i++)
				{
					player.setLoto(i, 0);
				}
			}
			showLotoWindow(player, val);
		}
		else if (command.startsWith("CPRecovery"))
		{
			makeCPRecovery(player);
		}
		else if (command.startsWith("SupportMagic"))
		{
			makeSupportMagic(player);
		}
		else if (command.startsWith("GiveBlessing"))
		{
			giveBlessingSupport(player);
		}
		else if (command.startsWith("multisell"))
		{
			L2Multisell.getInstance().separateAndSend(Integer.parseInt(command.substring(9).trim()), player, false, getCastle().getTaxRate());
		}
		else if (command.startsWith("exc_multisell"))
		{
			L2Multisell.getInstance().separateAndSend(Integer.parseInt(command.substring(13).trim()), player, true, getCastle().getTaxRate());
		}
		else if (command.startsWith("Augment"))
		{
			final int cmdChoice = Integer.parseInt(command.substring(8, 9).trim());
			switch (cmdChoice)
			{
				case 1:
					player.sendPacket(new SystemMessage(SystemMessageId.SELECT_THE_ITEM_TO_BE_AUGMENTED));
					player.sendPacket(new ExShowVariationMakeWindow());
					break;
				case 2:
					player.sendPacket(new SystemMessage(SystemMessageId.SELECT_THE_ITEM_FROM_WHICH_YOU_WISH_TO_REMOVE_AUGMENTATION));
					player.sendPacket(new ExShowVariationCancelWindow());
					break;
			}
		}
		else if (command.startsWith("npcfind_byid"))
		{
			try
			{
				L2Spawn spawn = SpawnTable.getInstance().getTemplate(Integer.parseInt(command.substring(12).trim()));
				
				if (spawn != null)
				{
					player.sendPacket(new RadarControl(0, 1, spawn.getLocx(), spawn.getLocy(), spawn.getLocz()));
					spawn = null;
				}
			}
			catch (final NumberFormatException nfe)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					nfe.printStackTrace();
				}
				
				player.sendMessage("Wrong command parameters");
			}
		}
		else if (command.startsWith("newbie_give_coupon"))
		{
			try
			{
				final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				if (player.getLevel() > 25 || player.getLevel() < 6 || !player.isNewbie())
				{
					html.setFile("data/html/adventurers_guide/31760-3.htm");
					player.sendPacket(html);
				}
				else if (player.getCoupon(0))
				{
					html.setFile("data/html/adventurers_guide/31760-1.htm");
					player.sendPacket(html);
				}
				else
				{
					player.getInventory().addItem("Weapon Coupon", 7832, 1, player, this);
					player.addCoupon(1);
					html.setFile("data/html/adventurers_guide/31760-2.htm");
					player.sendPacket(html);
				}
			}
			catch (final NumberFormatException nfe)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					nfe.printStackTrace();
				}
				
				player.sendMessage("Wrong command parameters");
			}
		}
		else if (command.startsWith("newbie_give_weapon"))
		{
			try
			{
				final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				if (player.getLevel() > 25 || player.getLevel() < 6 || !player.isNewbie())
				{
					html.setFile("data/html/adventurers_guide/31760-3.htm");
					player.sendPacket(html);
				}
				else
				{
					L2Multisell.getInstance().separateAndSend(10010, player, false, getCastle().getTaxRate());
				}
			}
			catch (final NumberFormatException nfe)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					nfe.printStackTrace();
				}
				
				player.sendMessage("Wrong command parameters");
			}
		}
		else if (command.startsWith("newbie_return_weapon"))
		{
			try
			{
				final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				if (player.getLevel() > 25 || player.getLevel() < 6 || !player.isNewbie())
				{
					html.setFile("data/html/adventurers_guide/31760-3.htm");
					player.sendPacket(html);
				}
				else
				{
					L2Multisell.getInstance().separateAndSend(10011, player, false, getCastle().getTaxRate());
				}
			}
			catch (final NumberFormatException nfe)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					nfe.printStackTrace();
				}
				
				player.sendMessage("Wrong command parameters");
			}
		}
		else if (command.startsWith("traveller_give_coupon"))
		{
			try
			{
				final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				if (player.getLevel() > 25 || player.getClassId().level() != 1 || !player.isNewbie())
				{
					html.setFile("data/html/adventurers_guide/31760-6.htm");
					player.sendPacket(html);
				}
				else if (player.getCoupon(1))
				{
					html.setFile("data/html/adventurers_guide/31760-4.htm");
					player.sendPacket(html);
				}
				else
				{
					player.getInventory().addItem("Weapon Coupon", 7833, 1, player, this);
					player.addCoupon(2);
					html.setFile("data/html/adventurers_guide/31760-5.htm");
					player.sendPacket(html);
				}
			}
			catch (final NumberFormatException nfe)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					nfe.printStackTrace();
				}
				
				player.sendMessage("Wrong command parameters");
			}
		}
		else if (command.startsWith("traveller_give_weapon"))
		{
			try
			{
				final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				if (player.getLevel() > 25 || player.getClassId().level() != 1 || !player.isNewbie())
				{
					html.setFile("data/html/adventurers_guide/31760-6.htm");
					player.sendPacket(html);
				}
				else
				{
					L2Multisell.getInstance().separateAndSend(10012, player, false, getCastle().getTaxRate());
				}
			}
			catch (final NumberFormatException nfe)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					nfe.printStackTrace();
				}
				
				player.sendMessage("Wrong command parameters");
			}
		}
		else if (command.startsWith("traveller_return_weapon"))
		{
			try
			{
				final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				if (player.getLevel() > 25 || player.getClassId().level() != 1 || !player.isNewbie())
				{
					html.setFile("data/html/adventurers_guide/31760-6.htm");
					player.sendPacket(html);
				}
				else
				{
					L2Multisell.getInstance().separateAndSend(10013, player, false, getCastle().getTaxRate());
				}
			}
			catch (final NumberFormatException nfe)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					nfe.printStackTrace();
				}
				
				player.sendMessage("Wrong command parameters");
			}
		}
		else if (command.startsWith("EnterRift"))
		{
			try
			{
				Byte b1 = Byte.parseByte(command.substring(10)); // Selected Area: Recruit, Soldier etc
				DimensionalRiftManager.getInstance().start(player, b1, this);
				b1 = null;
			}
			catch (final Exception e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					e.printStackTrace();
				}
			}
		}
		else if (command.startsWith("ChangeRiftRoom"))
		{
			if (player.isInParty() && player.getParty().isInDimensionalRift())
			{
				player.getParty().getDimensionalRift().manualTeleport(player, this);
			}
			else
			{
				DimensionalRiftManager.getInstance().handleCheat(player, this);
			}
		}
		else if (command.startsWith("ExitRift"))
		{
			if (player.isInParty() && player.getParty().isInDimensionalRift())
			{
				player.getParty().getDimensionalRift().manualExitRift(player, this);
			}
			else
			{
				DimensionalRiftManager.getInstance().handleCheat(player, this);
			}
		}
		else if (command.startsWith("RaidbossLvl_"))
		{
			final int endOfId = command.indexOf('_', 5);
			if (endOfId > 0)
			{
				command.substring(4, endOfId);
			}
			else
			{
				command.substring(4);
			}
			try
			{
				if (command.substring(endOfId + 1).startsWith("40"))
				{
					L2RBManager.RaidbossLevel40(player);
				}
				else if (command.substring(endOfId + 1).startsWith("45"))
				{
					L2RBManager.RaidbossLevel45(player);
				}
				else if (command.substring(endOfId + 1).startsWith("50"))
				{
					L2RBManager.RaidbossLevel50(player);
				}
				else if (command.substring(endOfId + 1).startsWith("55"))
				{
					L2RBManager.RaidbossLevel55(player);
				}
				else if (command.substring(endOfId + 1).startsWith("60"))
				{
					L2RBManager.RaidbossLevel60(player);
				}
				else if (command.substring(endOfId + 1).startsWith("65"))
				{
					L2RBManager.RaidbossLevel65(player);
				}
				else if (command.substring(endOfId + 1).startsWith("70"))
				{
					L2RBManager.RaidbossLevel70(player);
				}
			}
			catch (final NumberFormatException nfe)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					nfe.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Return null (regular NPCs don't have weapons instancies).<BR>
	 * <BR>
	 * @return the active weapon instance
	 */
	@Override
	public L2ItemInstance getActiveWeaponInstance()
	{
		// regular NPCs dont have weapons instancies
		return null;
	}
	
	/**
	 * Return the weapon item equiped in the right hand of the L2NpcInstance or null.<BR>
	 * <BR>
	 * @return the active weapon item
	 */
	@Override
	public L2Weapon getActiveWeaponItem()
	{
		// Get the weapon identifier equiped in the right hand of the L2NpcInstance
		final int weaponId = getTemplate().rhand;
		
		if (weaponId < 1)
		{
			return null;
		}
		
		// Get the weapon item equiped in the right hand of the L2NpcInstance
		final L2Item item = ItemTable.getInstance().getTemplate(getTemplate().rhand);
		
		if (!(item instanceof L2Weapon))
		{
			return null;
		}
		
		return (L2Weapon) item;
	}
	
	/**
	 * Give blessing support.
	 * @param player the player
	 */
	public void giveBlessingSupport(final L2PcInstance player)
	{
		if (player == null)
		{
			return;
		}
		
		// Blessing of protection - author eX1steam.
		// Prevent a cursed weapon weilder of being buffed - I think no need of that becouse karma check > 0
		if (player.isCursedWeaponEquiped())
		{
			return;
		}
		
		final int player_level = player.getLevel();
		// Select the player
		setTarget(player);
		// If the player is too high level, display a message and return
		if (player_level > 39 || player.getClassId().level() >= 2)
		{
			String content = "<html><body>Newbie Guide:<br>I'm sorry, but you are not eligible to receive the protection blessing.<br1>It can only be bestowed on <font color=\"LEVEL\">characters below level 39 who have not made a seccond transfer.</font></body></html>";
			insertObjectIdAndShowChatWindow(player, content);
			content = null;
			return;
		}
		L2Skill skill = SkillTable.getInstance().getInfo(5182, 1);
		doCast(skill);
		skill = null;
	}
	
	/**
	 * Return null (regular NPCs don't have weapons instancies).<BR>
	 * <BR>
	 * @return the secondary weapon instance
	 */
	@Override
	public L2ItemInstance getSecondaryWeaponInstance()
	{
		// regular NPCs dont have weapons instancies
		return null;
	}
	
	/**
	 * Return the weapon item equiped in the left hand of the L2NpcInstance or null.<BR>
	 * <BR>
	 * @return the secondary weapon item
	 */
	@Override
	public L2Weapon getSecondaryWeaponItem()
	{
		// Get the weapon identifier equiped in the right hand of the L2NpcInstance
		final int weaponId = getTemplate().lhand;
		
		if (weaponId < 1)
		{
			return null;
		}
		
		// Get the weapon item equiped in the right hand of the L2NpcInstance
		final L2Item item = ItemTable.getInstance().getTemplate(getTemplate().lhand);
		
		if (!(item instanceof L2Weapon))
		{
			return null;
		}
		
		return (L2Weapon) item;
	}
	
	/**
	 * Send a Server->Client packet NpcHtmlMessage to the L2PcInstance in order to display the message of the L2NpcInstance.<BR>
	 * <BR>
	 * @param player  The L2PcInstance who talks with the L2NpcInstance
	 * @param content The text of the L2NpcMessage
	 */
	public void insertObjectIdAndShowChatWindow(final L2PcInstance player, String content)
	{
		// Send a Server->Client packet NpcHtmlMessage to the L2PcInstance in order to display the message of the L2NpcInstance
		content = content.replaceAll("%objectId%", String.valueOf(getObjectId()));
		NpcHtmlMessage npcReply = new NpcHtmlMessage(getObjectId());
		npcReply.setHtml(content);
		player.sendPacket(npcReply);
		npcReply = null;
	}
	
	/**
	 * Return the pathfile of the selected HTML file in function of the npcId and of the page number.<BR>
	 * <BR>
	 * <B><U> Format of the pathfile </U> :</B><BR>
	 * <BR>
	 * <li>if the file exists on the server (page number = 0) : <B>data/html/default/12006.htm</B> (npcId-page number)</li>
	 * <li>if the file exists on the server (page number > 0) : <B>data/html/default/12006-1.htm</B> (npcId-page number)</li>
	 * <li>if the file doesn't exist on the server : <B>data/html/npcdefault.htm</B> (message : "I have nothing to say to you")</li><BR>
	 * <BR>
	 * <B><U> Overriden in </U> :</B><BR>
	 * <BR>
	 * <li>L2GuardInstance : Set the pathfile to data/html/guard/12006-1.htm (npcId-page number)</li><BR>
	 * <BR>
	 * @param  npcId The Identifier of the L2NpcInstance whose text must be display
	 * @param  val   The number of the page to display
	 * @return       the html path
	 */
	public String getHtmlPath(final int npcId, final int val)
	{
		String pom = "";
		
		if (val == 0)
		{
			pom = "" + npcId;
		}
		else
		{
			pom = npcId + "-" + val;
		}
		
		final String temp = "data/html/default/" + pom + ".htm";
		
		if (!Config.LAZY_CACHE)
		{
			// If not running lazy cache the file must be in the cache or it doesnt exist
			if (HtmCache.getInstance().contains(temp))
			{
				return temp;
			}
		}
		else
		{
			if (HtmCache.getInstance().isLoadable(temp))
			{
				return temp;
			}
		}
		
		// If the file is not found, the standard message "I have nothing to say to you" is returned
		return "data/html/npcdefault.htm";
	}
	
	/**
	 * Open a choose quest window on client with all quests available of the L2NpcInstance.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Send a Server->Client NpcHtmlMessage containing the text of the L2NpcInstance to the L2PcInstance</li><BR>
	 * <BR>
	 * @param player The L2PcInstance that talk with the L2NpcInstance
	 * @param quests The table containing quests of the L2NpcInstance
	 */
	private void showQuestChooseWindow(final L2PcInstance player, final Quest[] quests)
	{
		final TextBuilder sb = new TextBuilder();
		
		sb.append("<html><body><title>Talk about:</title><br>");
		
		String state = "";
		
		for (final Quest q : quests)
		{
			
			if (q == null)
			{
				continue;
			}
			
			sb.append("<a action=\"bypass -h npc_").append(getObjectId()).append("_Quest ").append(q.getName()).append("\">[");
			
			final QuestState qs = player.getQuestState(q.getScriptName());
			
			if (qs == null)
			{
				state = "";
			}
			else if (qs.isStarted() && qs.getInt("cond") > 0)
			{
				state = " (In Progress)";
			}
			else if (qs.isCompleted())
			{
				state = " (Done)";
			}
			
			sb.append(q.getDescr()).append(state).append("]</a><br>");
		}
		
		sb.append("</body></html>");
		
		// Send a Server->Client packet NpcHtmlMessage to the L2PcInstance in order to display the message of the L2NpcInstance
		insertObjectIdAndShowChatWindow(player, sb.toString());
	}
	
	/**
	 * Open a quest window on client with the text of the L2Npc.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Get the text of the quest state in the folder data/scripts/quests/questId/stateId.htm</li>
	 * <li>Send a Server->Client NpcHtmlMessage containing the text of the L2Npc to the L2PcInstance</li>
	 * <li>Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet</li><BR>
	 * <BR>
	 * @param player  The L2PcInstance that talk with the L2Npc
	 * @param questId The Identifier of the quest to display the message
	 */
	public void showQuestWindow(final L2PcInstance player, String questId)
	{
		String content = null;
		
		Quest q = null;
		if (!Config.ALT_DEV_NO_QUESTS)
		{
			q = QuestManager.getInstance().getQuest(questId);
		}
		
		// Get the state of the selected quest
		QuestState qs = player.getQuestState(questId);
		
		if (q == null)
		{
			// No quests found
			content = "<html><body>You are either not on a quest that involves this NPC, or you don't meet this NPC's minimum quest requirements.</body></html>";
		}
		else
		{
			if (player.getWeightPenalty() >= 3 && q.getQuestIntId() >= 1 && q.getQuestIntId() < 1000)
			{
				player.sendPacket(new SystemMessage(SystemMessageId.INVENTORY_LESS_THAN_80_PERCENT));
				return;
			}
			
			if (qs == null)
			{
				if (q.getQuestIntId() >= 1 && q.getQuestIntId() < 20000)
				{
					final Quest[] questList = player.getAllActiveQuests();
					if (questList.length >= 25) // if too many ongoing quests, don't show window and send message
					{
						player.sendMessage("You have too many quests, cannot register");
						return;
					}
				}
				// Check for start point
				for (final Quest temp : getTemplate().getEventQuests(Quest.QuestEventType.QUEST_START))
				{
					if (temp == q)
					{
						qs = q.newQuestState(player);
						break;
					}
				}
			}
		}
		
		if (qs != null)
		{
			// If the quest is already started, no need to show a window
			if (!qs.getQuest().notifyTalk(this, qs))
			{
				return;
			}
			
			questId = qs.getQuest().getName();
			final String stateId = qs.getStateId();
			final String path = Config.DATAPACK_ROOT + "/data/scripts/quests/" + questId + "/" + stateId + ".htm";
			content = HtmCache.getInstance().getHtm(path);
			
			if (Config.DEBUG)
			{
				if (content != null)
				{
					LOGGER.info("Showing quest window for quest " + questId + " html path: " + path);
				}
				else
				{
					LOGGER.info("File not exists for quest " + questId + " html path: " + path);
				}
			}
			
		}
		
		// Send a Server->Client packet NpcHtmlMessage to the L2PcInstance in order to display the message of the L2Npc
		if (content != null)
		{
			insertObjectIdAndShowChatWindow(player, content);
		}
		
		// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	/**
	 * Collect awaiting quests/start points and display a QuestChooseWindow (if several available) or QuestWindow.<BR>
	 * <BR>
	 * @param player The L2PcInstance that talk with the L2NpcInstance
	 */
	public void showQuestWindow(final L2PcInstance player)
	{
		// collect awaiting quests and start points
		final List<Quest> options = new ArrayList<>();
		
		final QuestState[] awaits = player.getQuestsForTalk(getTemplate().npcId);
		final Quest[] starts = getTemplate().getEventQuests(Quest.QuestEventType.QUEST_START);
		
		// Quests are limited between 1 and 999 because those are the quests that are supported by the client.
		// By limitting them there, we are allowed to create custom quests at higher IDs without interfering
		if (awaits != null)
		{
			for (final QuestState x : awaits)
			{
				if (!options.contains(x.getQuest()))
				{
					if (x.getQuest().getQuestIntId() > 0 && x.getQuest().getQuestIntId() < 1000)
					{
						options.add(x.getQuest());
					}
				}
			}
		}
		
		for (final Quest x : starts)
		{
			if (!options.contains(x))
			{
				if (x.getQuestIntId() > 0 && x.getQuestIntId() < 1000)
				{
					options.add(x);
				}
			}
		}
		
		// Display a QuestChooseWindow (if several quests are available) or QuestWindow
		if (options.size() > 1)
		{
			showQuestChooseWindow(player, options.toArray(new Quest[options.size()]));
		}
		else if (options.size() == 1)
		{
			showQuestWindow(player, options.get(0).getName());
		}
		else
		{
			showQuestWindow(player, "");
		}
	}
	
	/**
	 * Open a Loto window on client with the text of the L2NpcInstance.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Get the text of the selected HTML file in function of the npcId and of the page number</li>
	 * <li>Send a Server->Client NpcHtmlMessage containing the text of the L2NpcInstance to the L2PcInstance</li>
	 * <li>Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet</li><BR>
	 * @param player The L2PcInstance that talk with the L2NpcInstance
	 * @param val    The number of the page of the L2NpcInstance to display
	 */
	// 0 - first buy lottery ticket window
	// 1-20 - buttons
	// 21 - second buy lottery ticket window
	// 22 - selected ticket with 5 numbers
	// 23 - current lottery jackpot
	// 24 - Previous winning numbers/Prize claim
	// >24 - check lottery ticket by item object id
	public void showLotoWindow(final L2PcInstance player, final int val)
	{
		final int npcId = getTemplate().npcId;
		String filename;
		SystemMessage sm;
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		
		if (val == 0) // 0 - first buy lottery ticket window
		{
			filename = getHtmlPath(npcId, 1);
			html.setFile(filename);
		}
		else if (val >= 1 && val <= 21) // 1-20 - buttons, 21 - second buy lottery ticket window
		{
			if (!Lottery.getInstance().isStarted())
			{
				// tickets can't be sold
				player.sendPacket(new SystemMessage(SystemMessageId.NO_LOTTERY_TICKETS_CURRENT_SOLD));
				return;
			}
			if (!Lottery.getInstance().isSellableTickets())
			{
				// tickets can't be sold
				player.sendPacket(new SystemMessage(SystemMessageId.NO_LOTTERY_TICKETS_AVAILABLE));
				return;
			}
			
			filename = getHtmlPath(npcId, 5);
			html.setFile(filename);
			
			int count = 0;
			int found = 0;
			// counting buttons and unsetting button if found
			for (int i = 0; i < 5; i++)
			{
				if (player.getLoto(i) == val)
				{
					// unsetting button
					player.setLoto(i, 0);
					found = 1;
				}
				else if (player.getLoto(i) > 0)
				{
					count++;
				}
			}
			
			// if not rearched limit 5 and not unseted value
			if (count < 5 && found == 0 && val <= 20)
			{
				for (int i = 0; i < 5; i++)
				{
					if (player.getLoto(i) == 0)
					{
						player.setLoto(i, val);
						break;
					}
				}
			}
			
			// setting pusshed buttons
			count = 0;
			for (int i = 0; i < 5; i++)
			{
				if (player.getLoto(i) > 0)
				{
					count++;
					String button = String.valueOf(player.getLoto(i));
					if (player.getLoto(i) < 10)
					{
						button = "0" + button;
					}
					final String search = "fore=\"L2UI.lottoNum" + button + "\" back=\"L2UI.lottoNum" + button + "a_check\"";
					final String replace = "fore=\"L2UI.lottoNum" + button + "a_check\" back=\"L2UI.lottoNum" + button + "\"";
					html.replace(search, replace);
				}
			}
			
			if (count == 5)
			{
				final String search = "0\">Return";
				final String replace = "22\">The winner selected the numbers above.";
				html.replace(search, replace);
			}
		}
		else if (val == 22) // 22 - selected ticket with 5 numbers
		{
			if (!Lottery.getInstance().isStarted())
			{
				// tickets can't be sold
				player.sendPacket(new SystemMessage(SystemMessageId.NO_LOTTERY_TICKETS_CURRENT_SOLD));
				return;
			}
			if (!Lottery.getInstance().isSellableTickets())
			{
				// tickets can't be sold
				player.sendPacket(new SystemMessage(SystemMessageId.NO_LOTTERY_TICKETS_AVAILABLE));
				return;
			}
			
			final int price = Config.ALT_LOTTERY_TICKET_PRICE;
			final int lotonumber = Lottery.getInstance().getId();
			int enchant = 0;
			int type2 = 0;
			
			for (int i = 0; i < 5; i++)
			{
				if (player.getLoto(i) == 0)
				{
					return;
				}
				
				if (player.getLoto(i) < 17)
				{
					enchant += Math.pow(2, player.getLoto(i) - 1);
				}
				else
				{
					type2 += Math.pow(2, player.getLoto(i) - 17);
				}
			}
			if (player.getAdena() < price)
			{
				sm = new SystemMessage(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
				player.sendPacket(sm);
				return;
			}
			if (!player.reduceAdena("Loto", price, this, true))
			{
				return;
			}
			Lottery.getInstance().increasePrize(price);
			
			sm = new SystemMessage(SystemMessageId.ACQUIRED);
			sm.addNumber(lotonumber);
			sm.addItemName(4442);
			player.sendPacket(sm);
			sm = null;
			
			L2ItemInstance item = new L2ItemInstance(IdFactory.getInstance().getNextId(), 4442);
			item.setCount(1);
			item.setCustomType1(lotonumber);
			item.setEnchantLevel(enchant);
			item.setCustomType2(type2);
			player.getInventory().addItem("Loto", item, player, this);
			item = null;
			
			InventoryUpdate iu = new InventoryUpdate();
			iu.addItem(item);
			final L2ItemInstance adenaupdate = player.getInventory().getItemByItemId(57);
			iu.addModifiedItem(adenaupdate);
			player.sendPacket(iu);
			iu = null;
			
			filename = getHtmlPath(npcId, 3);
			html.setFile(filename);
		}
		else if (val == 23) // 23 - current lottery jackpot
		{
			filename = getHtmlPath(npcId, 3);
			html.setFile(filename);
		}
		else if (val == 24) // 24 - Previous winning numbers/Prize claim
		{
			filename = getHtmlPath(npcId, 4);
			html.setFile(filename);
			
			final int lotonumber = Lottery.getInstance().getId();
			String message = "";
			for (final L2ItemInstance item : player.getInventory().getItems())
			{
				if (item == null)
				{
					continue;
				}
				if (item.getItemId() == 4442 && item.getCustomType1() < lotonumber)
				{
					message = message + "<a action=\"bypass -h npc_%objectId%_Loto " + item.getObjectId() + "\">" + item.getCustomType1() + " Event Number ";
					final int[] numbers = Lottery.getInstance().decodeNumbers(item.getEnchantLevel(), item.getCustomType2());
					for (int i = 0; i < 5; i++)
					{
						message += numbers[i] + " ";
					}
					final int[] check = Lottery.getInstance().checkTicket(item);
					if (check[0] > 0)
					{
						switch (check[0])
						{
							case 1:
								message += "- 1st Prize";
								break;
							case 2:
								message += "- 2nd Prize";
								break;
							case 3:
								message += "- 3th Prize";
								break;
							case 4:
								message += "- 4th Prize";
								break;
						}
						message += " " + check[1] + "a.";
					}
					message += "</a><br>";
				}
			}
			if (message == "")
			{
				message += "There is no winning lottery ticket...<br>";
			}
			html.replace("%result%", message);
			message = null;
		}
		else if (val > 24) // >24 - check lottery ticket by item object id
		{
			final int lotonumber = Lottery.getInstance().getId();
			final L2ItemInstance item = player.getInventory().getItemByObjectId(val);
			if (item == null || item.getItemId() != 4442 || item.getCustomType1() >= lotonumber)
			{
				return;
			}
			final int[] check = Lottery.getInstance().checkTicket(item);
			
			sm = new SystemMessage(SystemMessageId.DISSAPEARED_ITEM);
			sm.addItemName(4442);
			player.sendPacket(sm);
			sm = null;
			
			final int adena = check[1];
			if (adena > 0)
			{
				player.addAdena("Loto", adena, this, true);
			}
			player.destroyItem("Loto", item, this, false);
			return;
		}
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%race%", "" + Lottery.getInstance().getId());
		html.replace("%adena%", "" + Lottery.getInstance().getPrize());
		html.replace("%ticket_price%", "" + Config.ALT_LOTTERY_TICKET_PRICE);
		html.replace("%prize5%", "" + Config.ALT_LOTTERY_5_NUMBER_RATE * 100);
		html.replace("%prize4%", "" + Config.ALT_LOTTERY_4_NUMBER_RATE * 100);
		html.replace("%prize3%", "" + Config.ALT_LOTTERY_3_NUMBER_RATE * 100);
		html.replace("%prize2%", "" + Config.ALT_LOTTERY_2_AND_1_NUMBER_PRIZE);
		html.replace("%enddate%", "" + DateFormat.getDateInstance().format(Lottery.getInstance().getEndDate()));
		player.sendPacket(html);
		
		html = null;
		
		// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	/**
	 * Make cp recovery.
	 * @param player the player
	 */
	public void makeCPRecovery(final L2PcInstance player)
	{
		if (getNpcId() != 31225 && getNpcId() != 31226)
		{
			return;
		}
		if (player.isCursedWeaponEquiped())
		{
			player.sendMessage("Go away, you're not welcome here.");
			return;
		}
		
		final int neededmoney = 100;
		SystemMessage sm;
		if (!player.reduceAdena("RestoreCP", neededmoney, player.getLastFolkNPC(), true))
		{
			return;
		}
		
		// Skill's animation
		final L2Skill skill = SkillTable.getInstance().getInfo(4380, 1);
		if (skill != null)
		{
			setTarget(player);
			doCast(skill);
		}
		
		player.setCurrentCp(player.getMaxCp());
		// cp restored
		sm = new SystemMessage(SystemMessageId.S1_CP_WILL_BE_RESTORED);
		sm.addString(player.getName());
		player.sendPacket(sm);
		sm = null;
	}
	
	/**
	 * Add Newbie helper buffs to L2Player according to its level.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Get the range level in wich player must be to obtain buff</li>
	 * <li>If player level is out of range, display a message and return</li>
	 * <li>According to player level cast buff</li><BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> Newbie Helper Buff list is define in sql table helper_buff_list</B></FONT><BR>
	 * <BR>
	 * @param player The L2PcInstance that talk with the L2NpcInstance if (!FloodProtector.getInstance().tryPerformAction(player.getObjectId(), FloodProtector.PROTECTED_USEITEM)) return;
	 */
	public void makeSupportMagic(final L2PcInstance player)
	{
		if (player == null)
		{
			return;
		}
		
		// Prevent a cursed weapon weilder of being buffed
		if (player.isCursedWeaponEquiped())
		{
			return;
		}
		
		final int player_level = player.getLevel();
		int lowestLevel = 0;
		int higestLevel = 0;
		
		// Select the player
		setTarget(player);
		
		// Calculate the min and max level between wich the player must be to obtain buff
		if (player.isMageClass())
		{
			lowestLevel = HelperBuffTable.getInstance().getMagicClassLowestLevel();
			higestLevel = HelperBuffTable.getInstance().getMagicClassHighestLevel();
		}
		else
		{
			lowestLevel = HelperBuffTable.getInstance().getPhysicClassLowestLevel();
			higestLevel = HelperBuffTable.getInstance().getPhysicClassHighestLevel();
		}
		
		// If the player is too high level, display a message and return
		if (player_level > higestLevel || !player.isNewbie())
		{
			final String content = "<html><body>Newbie Guide:<br>Only a <font color=\"LEVEL\">novice character of level " + higestLevel + " or less</font> can receive my support magic.<br>Your novice character is the first one that you created and " + "raised in this world.</body></html>";
			insertObjectIdAndShowChatWindow(player, content);
			return;
		}
		
		// If the player is too low level, display a message and return
		if (player_level < lowestLevel)
		{
			final String content = "<html><body>Come back here when you have reached level " + lowestLevel + ". I will give you support magic then.</body></html>";
			insertObjectIdAndShowChatWindow(player, content);
			return;
		}
		
		L2Skill skill = null;
		// Go through the Helper Buff list define in sql table helper_buff_list and cast skill
		for (final L2HelperBuff helperBuffItem : HelperBuffTable.getInstance().getHelperBuffTable())
		{
			if (helperBuffItem.isMagicClassBuff() == player.isMageClass())
			{
				if (player_level >= helperBuffItem.getLowerLevel() && player_level <= helperBuffItem.getUpperLevel())
				{
					skill = SkillTable.getInstance().getInfo(helperBuffItem.getSkillID(), helperBuffItem.getSkillLevel());
					
					if (skill.getSkillType() == SkillType.SUMMON)
					{
						player.doCast(skill);
					}
					else
					{
						doCast(skill);
					}
				}
			}
		}
	}
	
	/**
	 * Show chat window.
	 * @param player the player
	 */
	public void showChatWindow(final L2PcInstance player)
	{
		showChatWindow(player, 0);
	}
	
	/**
	 * Returns true if html exists.
	 * @param  player the player
	 * @param  type   the type
	 * @return        boolean
	 */
	private boolean showPkDenyChatWindow(final L2PcInstance player, final String type)
	{
		String html = HtmCache.getInstance().getHtm("data/html/" + type + "/" + getNpcId() + "-pk.htm");
		
		if (html != null)
		{
			final NpcHtmlMessage pkDenyMsg = new NpcHtmlMessage(getObjectId());
			pkDenyMsg.setHtml(html);
			player.sendPacket(pkDenyMsg);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			html = null;
			return true;
		}
		
		return false;
	}
	
	/*
	 * Returns true if html exists
	 * @param player
	 * @param type
	 * @return boolean
	 */
	/*
	 * private boolean showFlagDenyChatWindow(L2PcInstance player, String type) { String html = HtmCache.getInstance().getHtm("data/html/" + type + "/" + getNpcId() + "-f.htm"); if (html != null) { NpcHtmlMessage pkDenyMsg = new NpcHtmlMessage(getObjectId()); pkDenyMsg.setHtml(html);
	 * player.sendPacket(pkDenyMsg); player.sendPacket(ActionFailed.STATIC_PACKET); html = null; return true; } return false; }
	 */
	
	/**
	 * Open a chat window on client with the text of the L2NpcInstance.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Get the text of the selected HTML file in function of the npcId and of the page number</li>
	 * <li>Send a Server->Client NpcHtmlMessage containing the text of the L2NpcInstance to the L2PcInstance</li>
	 * <li>Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet</li><BR>
	 * @param player The L2PcInstance that talk with the L2NpcInstance
	 * @param val    The number of the page of the L2NpcInstance to display
	 */
	public void showChatWindow(final L2PcInstance player, final int val)
	{
		// Like L2OFF if char is dead, is sitting, is in trade or is in fakedeath can't speak with npcs
		if (player.isSitting() || player.isDead() || player.isFakeDeath() || player.getActiveTradeList() != null)
		{
			return;
		}
		
		if (player.getKarma() > 0)
		{
			if (!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP && this instanceof L2MerchantInstance)
			{
				if (showPkDenyChatWindow(player, "merchant"))
				{
					return;
				}
			}
			else if (!Config.ALT_GAME_KARMA_PLAYER_CAN_USE_GK && this instanceof L2TeleporterInstance)
			{
				if (showPkDenyChatWindow(player, "teleporter"))
				{
					return;
				}
			}
			else if (!Config.ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE && this instanceof L2WarehouseInstance)
			{
				if (showPkDenyChatWindow(player, "warehouse"))
				{
					return;
				}
			}
			else if (!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP && this instanceof L2FishermanInstance)
			{
				if (showPkDenyChatWindow(player, "fisherman"))
				{
					return;
				}
			}
		}
		
		if (getTemplate().type == "L2Auctioneer" && val == 0)
		{
			return;
		}
		
		final int npcId = getTemplate().npcId;
		
		/* For use with Seven Signs implementation */
		String filename = SevenSigns.SEVEN_SIGNS_HTML_PATH;
		final int sealAvariceOwner = SevenSigns.getInstance().getSealOwner(SevenSigns.SEAL_AVARICE);
		final int sealGnosisOwner = SevenSigns.getInstance().getSealOwner(SevenSigns.SEAL_GNOSIS);
		final int playerCabal = SevenSigns.getInstance().getPlayerCabal(player);
		final boolean isSealValidationPeriod = SevenSigns.getInstance().isSealValidationPeriod();
		final int compWinner = SevenSigns.getInstance().getCabalHighestScore();
		
		switch (npcId)
		{
			case 31078:
			case 31079:
			case 31080:
			case 31081:
			case 31082: // Dawn Priests
			case 31083:
			case 31084:
			case 31168:
			case 31692:
			case 31694:
			case 31997:
				switch (playerCabal)
				{
					case SevenSigns.CABAL_DAWN:
						if (isSealValidationPeriod)
						{
							if (compWinner == SevenSigns.CABAL_DAWN)
							{
								if (compWinner != sealGnosisOwner)
								{
									filename += "dawn_priest_2c.htm";
								}
								else
								{
									filename += "dawn_priest_2a.htm";
								}
							}
							else
							{
								filename += "dawn_priest_2b.htm";
							}
						}
						else
						{
							filename += "dawn_priest_1b.htm";
						}
						break;
					case SevenSigns.CABAL_DUSK:
						if (isSealValidationPeriod)
						{
							filename += "dawn_priest_3b.htm";
						}
						else
						{
							filename += "dawn_priest_3a.htm";
						}
						break;
					default:
						if (isSealValidationPeriod)
						{
							if (compWinner == SevenSigns.CABAL_DAWN)
							{
								filename += "dawn_priest_4.htm";
							}
							else
							{
								filename += "dawn_priest_2b.htm";
							}
						}
						else
						{
							filename += "dawn_priest_1a.htm";
						}
						break;
				}
				break;
			case 31085:
			case 31086:
			case 31087:
			case 31088: // Dusk Priest
			case 31089:
			case 31090:
			case 31091:
			case 31169:
			case 31693:
			case 31695:
			case 31998:
				switch (playerCabal)
				{
					case SevenSigns.CABAL_DUSK:
						if (isSealValidationPeriod)
						{
							if (compWinner == SevenSigns.CABAL_DUSK)
							{
								if (compWinner != sealGnosisOwner)
								{
									filename += "dusk_priest_2c.htm";
								}
								else
								{
									filename += "dusk_priest_2a.htm";
								}
							}
							else
							{
								filename += "dusk_priest_2b.htm";
							}
						}
						else
						{
							filename += "dusk_priest_1b.htm";
						}
						break;
					case SevenSigns.CABAL_DAWN:
						if (isSealValidationPeriod)
						{
							filename += "dusk_priest_3b.htm";
						}
						else
						{
							filename += "dusk_priest_3a.htm";
						}
						break;
					default:
						if (isSealValidationPeriod)
						{
							if (compWinner == SevenSigns.CABAL_DUSK)
							{
								filename += "dusk_priest_4.htm";
							}
							else
							{
								filename += "dusk_priest_2b.htm";
							}
						}
						else
						{
							filename += "dusk_priest_1a.htm";
						}
						break;
				}
				break;
			case 31095: //
			case 31096: //
			case 31097: //
			case 31098: // Enter Necropolises
			case 31099: //
			case 31100: //
			case 31101: //
			case 31102: //
				if (isSealValidationPeriod)
				{
					if (Config.ALT_REQUIRE_WIN_7S)
					{
						if (playerCabal != compWinner || sealAvariceOwner != compWinner)
						{
							switch (compWinner)
							{
								case SevenSigns.CABAL_DAWN:
									player.sendPacket(new SystemMessage(SystemMessageId.CAN_BE_USED_BY_DAWN));
									filename += "necro_no.htm";
									break;
								case SevenSigns.CABAL_DUSK:
									player.sendPacket(new SystemMessage(SystemMessageId.CAN_BE_USED_BY_DUSK));
									filename += "necro_no.htm";
									break;
								case SevenSigns.CABAL_NULL:
									filename = getHtmlPath(npcId, val); // do the default!
									break;
							}
						}
						else
						{
							filename = getHtmlPath(npcId, val); // do the default!
						}
					}
					else
					{
						filename = getHtmlPath(npcId, val); // do the default!
					}
				}
				else
				{
					if (playerCabal == SevenSigns.CABAL_NULL)
					{
						filename += "necro_no.htm";
					}
					else
					{
						filename = getHtmlPath(npcId, val); // do the default!
					}
				}
				break;
			case 31114: //
			case 31115: //
			case 31116: // Enter Catacombs
			case 31117: //
			case 31118: //
			case 31119: //
				if (isSealValidationPeriod)
				{
					if (Config.ALT_REQUIRE_WIN_7S)
					{
						if (playerCabal != compWinner || sealGnosisOwner != compWinner)
						{
							switch (compWinner)
							{
								case SevenSigns.CABAL_DAWN:
									player.sendPacket(new SystemMessage(SystemMessageId.CAN_BE_USED_BY_DAWN));
									filename += "cata_no.htm";
									break;
								case SevenSigns.CABAL_DUSK:
									player.sendPacket(new SystemMessage(SystemMessageId.CAN_BE_USED_BY_DUSK));
									filename += "cata_no.htm";
									break;
								case SevenSigns.CABAL_NULL:
									filename = getHtmlPath(npcId, val); // do the default!
									break;
							}
						}
						else
						{
							filename = getHtmlPath(npcId, val); // do the default!
						}
					}
					else
					{
						filename = getHtmlPath(npcId, val); // do the default!
					}
				}
				else
				{
					if (playerCabal == SevenSigns.CABAL_NULL)
					{
						filename += "cata_no.htm";
					}
					else
					{
						filename = getHtmlPath(npcId, val); // do the default!
					}
				}
				break;
			case 31111: // Gatekeeper Spirit (Disciples)
				if (playerCabal == sealAvariceOwner && playerCabal == compWinner)
				{
					switch (sealAvariceOwner)
					{
						case SevenSigns.CABAL_DAWN:
							filename += "spirit_dawn.htm";
							break;
						case SevenSigns.CABAL_DUSK:
							filename += "spirit_dusk.htm";
							break;
						case SevenSigns.CABAL_NULL:
							filename += "spirit_null.htm";
							break;
					}
				}
				else
				{
					filename += "spirit_null.htm";
				}
				break;
			case 31112: // Gatekeeper Spirit (Disciples)
				filename += "spirit_exit.htm";
				break;
			case 31127: //
			case 31128: //
			case 31129: // Dawn Festival Guides
			case 31130: //
			case 31131: //
				filename += "festival/dawn_guide.htm";
				break;
			case 31137: //
			case 31138: //
			case 31139: // Dusk Festival Guides
			case 31140: //
			case 31141: //
				filename += "festival/dusk_guide.htm";
				break;
			case 31092: // Black Marketeer of Mammon
				filename += "blkmrkt_1.htm";
				break;
			case 31113: // Merchant of Mammon
				if (Config.ALT_REQUIRE_WIN_7S)
				{
					switch (compWinner)
					{
						case SevenSigns.CABAL_DAWN:
							if (playerCabal != compWinner || playerCabal != sealAvariceOwner)
							{
								player.sendPacket(new SystemMessage(SystemMessageId.CAN_BE_USED_BY_DAWN));
								return;
							}
							break;
						case SevenSigns.CABAL_DUSK:
							if (playerCabal != compWinner || playerCabal != sealAvariceOwner)
							{
								player.sendPacket(new SystemMessage(SystemMessageId.CAN_BE_USED_BY_DUSK));
								return;
							}
							break;
					}
				}
				filename += "mammmerch_1.htm";
				break;
			case 31126: // Blacksmith of Mammon
				if (Config.ALT_REQUIRE_WIN_7S)
				{
					switch (compWinner)
					{
						case SevenSigns.CABAL_DAWN:
							if (playerCabal != compWinner || playerCabal != sealGnosisOwner)
							{
								player.sendPacket(new SystemMessage(SystemMessageId.CAN_BE_USED_BY_DAWN));
								return;
							}
							break;
						case SevenSigns.CABAL_DUSK:
							if (playerCabal != compWinner || playerCabal != sealGnosisOwner)
							{
								player.sendPacket(new SystemMessage(SystemMessageId.CAN_BE_USED_BY_DUSK));
								return;
							}
							break;
					}
				}
				filename += "mammblack_1.htm";
				break;
			case 31132:
			case 31133:
			case 31134:
			case 31135:
			case 31136: // Festival Witches
			case 31142:
			case 31143:
			case 31144:
			case 31145:
			case 31146:
				filename += "festival/festival_witch.htm";
				break;
			case 31688:
				if (player.isNoble())
				{
					filename = Olympiad.OLYMPIAD_HTML_PATH + "noble_main.htm";
				}
				else
				{
					filename = getHtmlPath(npcId, val);
				}
				break;
			// fixed Monument of Heroes HTML for noble characters like L2OFF
			case 31690:
				if (player.isNoble())
				{
					filename = Olympiad.OLYMPIAD_HTML_PATH + "hero_main.htm";
				}
				else
				{
					filename = getHtmlPath(npcId, val);
				}
				break;
			case 31769:
			case 31770:
			case 31771:
			case 31772:
				if (player.isNoble())
				{
					filename = Olympiad.OLYMPIAD_HTML_PATH + "hero_main.htm";
				}
				else
				{
					filename = getHtmlPath(npcId, val);
				}
				break;
			default:
				if (npcId >= 31865 && npcId <= 31918)
				{
					filename += "rift/GuardianOfBorder.htm";
					break;
				}
				if (npcId >= 31093 && npcId <= 31094 || npcId >= 31172 && npcId <= 31201 || npcId >= 31239 && npcId <= 31254)
				{
					return;
				}
				// Get the text of the selected HTML file in function of the npcId and of the page number
				filename = getHtmlPath(npcId, val);
				break;
		}
		
		if (this instanceof L2CastleTeleporterInstance)
		{
			((L2CastleTeleporterInstance) this).showChatWindow(player);
			return;
		}
		
		// Send a Server->Client NpcHtmlMessage containing the text of the L2NpcInstance to the L2PcInstance
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(filename);
		filename = null;
		
		// String word = "npc-"+npcId+(val>0 ? "-"+val : "" )+"-dialog-append";
		
		if (this instanceof L2MerchantInstance)
		{
			if (Config.LIST_PET_RENT_NPC.contains(npcId))
			{
				html.replace("_Quest", "_RentPet\">Rent Pet</a><br><a action=\"bypass -h npc_%objectId%_Quest");
			}
		}
		html.replace("%playername%", player.getName());
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%festivalMins%", SevenSignsFestival.getInstance().getTimeToNextFestivalStart());
		player.sendPacket(html);
		html = null;
		
		// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	/**
	 * Open a chat window on client with the text specified by the given file name and path,<BR>
	 * relative to the datapack root. <BR>
	 * <BR>
	 * Added by Tempy
	 * @param player   The L2PcInstance that talk with the L2NpcInstance
	 * @param filename The filename that contains the text to send
	 */
	public void showChatWindow(final L2PcInstance player, final String filename)
	{
		// Send a Server->Client NpcHtmlMessage containing the text of the L2NpcInstance to the L2PcInstance
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(html);
		html = null;
		
		// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	/**
	 * Return the Exp Reward of this L2NpcInstance contained in the L2NpcTemplate (modified by RATE_XP).<BR>
	 * <BR>
	 * @return the exp reward
	 */
	public int getExpReward()
	{
		final double rateXp = getStat().calcStat(Stats.MAX_HP, 1, this, null);
		return (int) (getTemplate().rewardExp * rateXp * Config.RATE_XP);
	}
	
	/**
	 * Return the SP Reward of this L2NpcInstance contained in the L2NpcTemplate (modified by RATE_SP).<BR>
	 * <BR>
	 * @return the sp reward
	 */
	public int getSpReward()
	{
		final double rateSp = getStat().calcStat(Stats.MAX_HP, 1, this, null);
		return (int) (getTemplate().rewardSp * rateSp * Config.RATE_SP);
	}
	
	/**
	 * Kill the L2NpcInstance (the corpse disappeared after 7 seconds).<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Create a DecayTask to remove the corpse of the L2NpcInstance after 7 seconds</li>
	 * <li>Set target to null and cancel Attack or Cast</li>
	 * <li>Stop movement</li>
	 * <li>Stop HP/MP/CP Regeneration task</li>
	 * <li>Stop all active skills effects in progress on the L2Character</li>
	 * <li>Send the Server->Client packet StatusUpdate with current HP and MP to all other L2PcInstance to inform</li>
	 * <li>Notify L2Character AI</li><BR>
	 * <BR>
	 * <B><U> Overriden in </U> :</B><BR>
	 * <BR>
	 * <li>L2Attackable</li><BR>
	 * <BR>
	 * @param  killer The L2Character who killed it
	 * @return        true, if successful
	 */
	@Override
	public boolean doDie(final L2Character killer)
	{
		if (!super.doDie(killer))
		{
			return false;
		}
		
		// normally this wouldn't really be needed, but for those few exceptions,
		// we do need to reset the weapons back to the initial templated weapon.
		currentLHandId = getTemplate().lhand;
		currentRHandId = getTemplate().rhand;
		currentCollisionHeight = getTemplate().collisionHeight;
		currentCollisionRadius = getTemplate().collisionRadius;
		DecayTaskManager.getInstance().addDecayTask(this);
		return true;
	}
	
	/**
	 * Set the spawn of the L2NpcInstance.<BR>
	 * <BR>
	 * @param spawn The L2Spawn that manage the L2NpcInstance
	 */
	public void setSpawn(L2Spawn spawn)
	{
		if (spawn == null)
		{
			return;
		}
		
		spawnInstance = spawn;
		// Does this Npc morph into a PcInstance?
		if (CustomNpcInstanceManager.getInstance().isL2CustomNpcInstance(spawnInstance.getId(), getNpcId()))
		{
			new L2CustomNpcInstance(this);
		}
	}
	
	/**
	 * Remove the L2NpcInstance from the world and update its spawn object (for a complete removal use the deleteMe method).<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Remove the L2NpcInstance from the world when the decay task is launched</li>
	 * <li>Decrease its spawn counter</li>
	 * <li>Manage Siege task (killFlag, killCT)</li><BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T REMOVE the object from allObjects of L2World </B></FONT><BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND Server->Client packets to players</B></FONT><BR>
	 * <BR>
	 */
	@Override
	public void onDecay()
	{
		if (isDecayed())
		{
			return;
		}
		setDecayed(true);
		
		// Manage Life Control Tower
		if (this instanceof L2ControlTowerInstance)
		{
			((L2ControlTowerInstance) this).onDeath();
		}
		
		// Remove the L2NpcInstance from the world when the decay task is launched
		super.onDecay();
		
		// Decrease its spawn counter
		if (spawnInstance != null)
		{
			spawnInstance.decreaseCount(this);
		}
	}
	
	/**
	 * Remove PROPERLY the L2NpcInstance from the world.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Remove the L2NpcInstance from the world and update its spawn object</li>
	 * <li>Remove all L2Object from knownObjects and knownPlayer of the L2NpcInstance then cancel Attak or Cast and notify AI</li>
	 * <li>Remove L2Object object from allObjects of L2World</li><BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND Server->Client packets to players</B></FONT><BR>
	 * <BR>
	 */
	public void deleteMe()
	{
		if (getWorldRegion() != null)
		{
			getWorldRegion().removeFromZones(this);
			// FIXME this is just a temp hack, we should find a better solution | for what ?
		}
		
		try
		{
			decayMe();
		}
		catch (final Throwable t)
		{
			LOGGER.error(t);
		}
		
		// Remove all L2Object from knownObjects and knownPlayer of the L2Character then cancel Attak or Cast and notify AI
		try
		{
			getKnownList().removeAllKnownObjects();
		}
		catch (final Throwable t)
		{
			LOGGER.error(t);
		}
		
		// Remove L2Object object from allObjects of L2World
		L2World.getInstance().removeObject(this);
	}
	
	/**
	 * Return the L2Spawn object that manage this L2NpcInstance.<BR>
	 * <BR>
	 * @return the spawn
	 */
	public L2Spawn getSpawn()
	{
		return spawnInstance;
	}
	
	@Override
	public String toString()
	{
		return getTemplate().name;
	}
	
	/**
	 * Checks if is decayed.
	 * @return true, if is decayed
	 */
	public boolean isDecayed()
	{
		return isDecayed;
	}
	
	/**
	 * Sets the decayed.
	 * @param decayed the new decayed
	 */
	public void setDecayed(final boolean decayed)
	{
		isDecayed = decayed;
	}
	
	/**
	 * End decay task.
	 */
	public void endDecayTask()
	{
		if (!isDecayed())
		{
			DecayTaskManager.getInstance().cancelDecayTask(this);
			onDecay();
		}
	}
	
	/**
	 * Checks if is mob.
	 * @return true, if is mob
	 */
	public boolean isMob() // rather delete this check
	{
		return false; // This means we use MAX_NPC_ANIMATION instead of MAX_MONSTER_ANIMATION
	}
	
	// Two functions to change the appearance of the equipped weapons on the NPC
	// This is only useful for a few NPCs and is most likely going to be called from AI
	/**
	 * Sets the l hand id.
	 * @param newWeaponId the new l hand id
	 */
	public void setLHandId(final int newWeaponId)
	{
		currentLHandId = newWeaponId;
	}
	
	/**
	 * Sets the r hand id.
	 * @param newWeaponId the new r hand id
	 */
	public void setRHandId(final int newWeaponId)
	{
		currentRHandId = newWeaponId;
	}
	
	/**
	 * Sets the collision height.
	 * @param height the new collision height
	 */
	public void setCollisionHeight(final int height)
	{
		currentCollisionHeight = height;
	}
	
	/**
	 * Sets the collision radius.
	 * @param radius the new collision radius
	 */
	public void setCollisionRadius(final int radius)
	{
		currentCollisionRadius = radius;
	}
	
	/**
	 * Gets the collision height.
	 * @return the collision height
	 */
	public int getCollisionHeight()
	{
		return currentCollisionHeight;
	}
	
	/**
	 * Gets the collision radius.
	 * @return the collision radius
	 */
	public int getCollisionRadius()
	{
		return currentCollisionRadius;
	}
	
	/**
	 * Gets the custom npc instance.
	 * @return the custom npc instance
	 */
	public L2CustomNpcInstance getCustomNpcInstance()
	{
		return customNpcInstance;
	}
	
	/**
	 * Sets the custom npc instance.
	 * @param arg the new custom npc instance
	 */
	public void setCustomNpcInstance(final L2CustomNpcInstance arg)
	{
		customNpcInstance = arg;
	}
}
