/*
 * Copyright (C) 2004-2014 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.model.actor;

import static l2r.gameserver.enums.CtrlIntention.AI_INTENTION_ACTIVE;
import static l2r.gameserver.enums.CtrlIntention.AI_INTENTION_ATTACK;
import static l2r.gameserver.enums.CtrlIntention.AI_INTENTION_FOLLOW;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantLock;

import javolution.util.FastList;
import javolution.util.FastMap;
import javolution.util.WeakFastSet;
import l2r.Config;
import l2r.gameserver.GameTimeController;
import l2r.gameserver.GeoData;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.ai.L2AttackableAI;
import l2r.gameserver.ai.L2CharacterAI;
import l2r.gameserver.datatables.xml.CategoryData;
import l2r.gameserver.datatables.xml.DoorData;
import l2r.gameserver.datatables.xml.ItemData;
import l2r.gameserver.datatables.xml.SkillData;
import l2r.gameserver.enums.CategoryType;
import l2r.gameserver.enums.CtrlEvent;
import l2r.gameserver.enums.CtrlIntention;
import l2r.gameserver.enums.InstanceType;
import l2r.gameserver.enums.PcCondOverride;
import l2r.gameserver.enums.QuestEventType;
import l2r.gameserver.enums.ShotType;
import l2r.gameserver.enums.Team;
import l2r.gameserver.enums.TeleportWhereType;
import l2r.gameserver.enums.ZoneIdType;
import l2r.gameserver.handler.ISkillHandler;
import l2r.gameserver.handler.SkillHandler;
import l2r.gameserver.instancemanager.DimensionalRiftManager;
import l2r.gameserver.instancemanager.InstanceManager;
import l2r.gameserver.instancemanager.MapRegionManager;
import l2r.gameserver.instancemanager.TerritoryWarManager;
import l2r.gameserver.instancemanager.TownManager;
import l2r.gameserver.model.ChanceSkillList;
import l2r.gameserver.model.CharEffectList;
import l2r.gameserver.model.FusionSkill;
import l2r.gameserver.model.L2AccessLevel;
import l2r.gameserver.model.L2Object;
import l2r.gameserver.model.L2Party;
import l2r.gameserver.model.L2World;
import l2r.gameserver.model.L2WorldRegion;
import l2r.gameserver.model.Location;
import l2r.gameserver.model.TimeStamp;
import l2r.gameserver.model.actor.events.CharEvents;
import l2r.gameserver.model.actor.instance.L2EventMapGuardInstance;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.actor.instance.L2PcInstance.SkillDat;
import l2r.gameserver.model.actor.instance.L2PetInstance;
import l2r.gameserver.model.actor.instance.L2RiftInvaderInstance;
import l2r.gameserver.model.actor.knownlist.CharKnownList;
import l2r.gameserver.model.actor.stat.CharStat;
import l2r.gameserver.model.actor.status.CharStatus;
import l2r.gameserver.model.actor.tasks.character.FlyToLocationTask;
import l2r.gameserver.model.actor.tasks.character.HitTask;
import l2r.gameserver.model.actor.tasks.character.MagicUseTask;
import l2r.gameserver.model.actor.tasks.character.NotifyAITask;
import l2r.gameserver.model.actor.tasks.character.QueuedMagicUseTask;
import l2r.gameserver.model.actor.tasks.character.UsePotionTask;
import l2r.gameserver.model.actor.templates.L2CharTemplate;
import l2r.gameserver.model.actor.transform.Transform;
import l2r.gameserver.model.effects.AbnormalEffect;
import l2r.gameserver.model.effects.EffectFlag;
import l2r.gameserver.model.effects.L2Effect;
import l2r.gameserver.model.effects.L2EffectType;
import l2r.gameserver.model.entity.Instance;
import l2r.gameserver.model.holders.InvulSkillHolder;
import l2r.gameserver.model.holders.SkillHolder;
import l2r.gameserver.model.interfaces.IChanceSkillTrigger;
import l2r.gameserver.model.interfaces.ILocational;
import l2r.gameserver.model.interfaces.ISkillsHolder;
import l2r.gameserver.model.itemcontainer.Inventory;
import l2r.gameserver.model.items.L2Item;
import l2r.gameserver.model.items.L2Weapon;
import l2r.gameserver.model.items.instance.L2ItemInstance;
import l2r.gameserver.model.items.type.WeaponType;
import l2r.gameserver.model.options.OptionsSkillHolder;
import l2r.gameserver.model.options.OptionsSkillType;
import l2r.gameserver.model.quest.Quest;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.model.skills.L2SkillType;
import l2r.gameserver.model.skills.funcs.Func;
import l2r.gameserver.model.skills.l2skills.L2SkillSummon;
import l2r.gameserver.model.skills.targets.L2TargetType;
import l2r.gameserver.model.stats.BaseStats;
import l2r.gameserver.model.stats.Calculator;
import l2r.gameserver.model.stats.Formulas;
import l2r.gameserver.model.stats.Stats;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.AbstractNpcInfo;
import l2r.gameserver.network.serverpackets.ActionFailed;
import l2r.gameserver.network.serverpackets.Attack;
import l2r.gameserver.network.serverpackets.ChangeMoveType;
import l2r.gameserver.network.serverpackets.ChangeWaitType;
import l2r.gameserver.network.serverpackets.ExRotation;
import l2r.gameserver.network.serverpackets.L2GameServerPacket;
import l2r.gameserver.network.serverpackets.MagicSkillCanceld;
import l2r.gameserver.network.serverpackets.MagicSkillLaunched;
import l2r.gameserver.network.serverpackets.MagicSkillUse;
import l2r.gameserver.network.serverpackets.MoveToLocation;
import l2r.gameserver.network.serverpackets.Revive;
import l2r.gameserver.network.serverpackets.ServerObjectInfo;
import l2r.gameserver.network.serverpackets.SetupGauge;
import l2r.gameserver.network.serverpackets.SocialAction;
import l2r.gameserver.network.serverpackets.StatusUpdate;
import l2r.gameserver.network.serverpackets.StopMove;
import l2r.gameserver.network.serverpackets.SystemMessage;
import l2r.gameserver.network.serverpackets.TeleportToLocation;
import l2r.gameserver.pathfinding.AbstractNodeLoc;
import l2r.gameserver.pathfinding.PathFinding;
import l2r.gameserver.taskmanager.AttackStanceTaskManager;
import l2r.gameserver.util.Util;
import l2r.util.Rnd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gr.reunion.configsEngine.CustomServerConfigs;
import gr.reunion.interf.ReunionEvents;

/**
 * Mother class of all character objects of the world (PC, NPC...)<br>
 * L2Character:<br>
 * <ul>
 * <li>L2DoorInstance</li>
 * <li>L2Playable</li>
 * <li>L2Npc</li>
 * <li>L2StaticObjectInstance_attackend</li>
 * <li>L2Trap</li>
 * <li>L2Vehicle</li>
 * </ul>
 * <br>
 * <b>Concept of L2CharTemplate:</b><br>
 * Each L2Character owns generic and static properties (ex : all Keltir have the same number of HP...).<br>
 * All of those properties are stored in a different template for each type of L2Character.<br>
 * Each template is loaded once in the server cache memory (reduce memory use).<br>
 * When a new instance of L2Character is spawned, server just create a link between the instance and the template.<br>
 * This link is stored in {@link #_template}
 * @version $Revision: 1.53.2.45.2.34 $ $Date: 2005/04/11 10:06:08 $
 */
public abstract class L2Character extends L2Object implements ISkillsHolder
{
	public static final Logger _log = LoggerFactory.getLogger(L2Character.class);
	
	private volatile Set<L2Character> _attackByList;
	private volatile boolean _isCastingNow = false;
	private boolean _protected = false;
	private volatile boolean _isCastingSimultaneouslyNow = false;
	private L2Skill _lastSkillCast;
	private L2Skill _lastSimultaneousSkillCast;
	
	private boolean _isDead = false;
	private boolean _isImmobilized = false;
	private boolean _isOverloaded = false; // the char is carrying too much
	private boolean _isParalyzed = false;
	private boolean _isPendingRevive = false;
	private boolean _isRunning = false;
	private boolean _isNoRndWalk = false; // Is no random walk
	protected boolean _showSummonAnimation = false;
	protected boolean _isTeleporting = false;
	private boolean _isInvul = false;
	private boolean _isMortal = true; // Char will die when HP decreased to 0
	private boolean _isFlying = false;
	
	private CharStat _stat;
	private CharStatus _status;
	private CharEvents _events;
	private L2CharTemplate _template; // The link on the L2CharTemplate object containing generic and static properties of this L2Character type (ex : Max HP, Speed...)
	private String _title;
	
	public static final double MAX_HP_BAR_PX = 352.0;
	
	private double _hpUpdateIncCheck = .0;
	private double _hpUpdateDecCheck = .0;
	private double _hpUpdateInterval = .0;
	
	/** Table of Calculators containing all used calculator */
	private Calculator[] _calculators;
	/** Map containing all skills of this character. */
	private final FastMap<Integer, L2Skill> _skills = new FastMap<>();
	/** Map containing the active chance skills on this character */
	private volatile ChanceSkillList _chanceSkills;
	/** Map containing the skill reuse time stamps. */
	private volatile Map<Integer, TimeStamp> _reuseTimeStampsSkills = null;
	/** Map containing the item reuse time stamps. */
	private volatile Map<Integer, TimeStamp> _reuseTimeStampsItems = null;
	/** Map containing all the disabled skills. */
	private volatile Map<Integer, Long> _disabledSkills = null;
	private boolean _allSkillsDisabled;
	/** Current force buff this caster is casting to a target */
	protected FusionSkill _fusionSkill;
	
	private final byte[] _zones = new byte[ZoneIdType.getZoneCount()];
	protected byte _zoneValidateCounter = 4;
	
	private L2Character _debugger = null;
	
	private final ReentrantLock _teleportLock;
	
	private Team _team = Team.NONE;
	
	protected long _exceptions = 0L;
	
	private boolean _lethalable = true;
	
	protected final String COND_EXCEPTIONS = "COND_EX_" + getObjectId();
	
	private volatile Map<Integer, OptionsSkillHolder> _triggerSkills;
	
	private volatile Map<Integer, InvulSkillHolder> _invulAgainst;
	
	/** Movement data of this L2Character */
	protected MoveData _move;
	
	/** This creature's target. */
	private L2Object _target;
	
	// set by the start of attack, in game ticks
	private int _attackEndTime;
	private int _attacking;
	private int _disableBowAttackEndTime;
	private int _disableCrossBowAttackEndTime;
	
	private int _castInterruptTime;
	
	/** Table of calculators containing all standard NPC calculator (ex : ACCURACY_COMBAT, EVASION_RATE) */
	private static final Calculator[] NPC_STD_CALCULATOR = Formulas.getStdNPCCalculators();
	
	protected L2CharacterAI _ai;
	
	/** Future Skill Cast */
	protected Future<?> _skillCast;
	protected Future<?> _skillCast2;
	
	/**
	 * @return True if debugging is enabled for this L2Character
	 */
	public boolean isDebug()
	{
		return _debugger != null;
	}
	
	/**
	 * Sets L2Character instance, to which debug packets will be send
	 * @param d
	 */
	public void setDebug(L2Character d)
	{
		_debugger = d;
	}
	
	/**
	 * Send debug packet.
	 * @param pkt
	 */
	public void sendDebugPacket(L2GameServerPacket pkt)
	{
		if (_debugger != null)
		{
			_debugger.sendPacket(pkt);
		}
	}
	
	/**
	 * Send debug text string
	 * @param msg
	 */
	public void sendDebugMessage(String msg)
	{
		if (_debugger != null)
		{
			_debugger.sendMessage(msg);
		}
	}
	
	/**
	 * @return character inventory, default null, overridden in L2Playable types and in L2NPcInstance
	 */
	public Inventory getInventory()
	{
		return null;
	}
	
	public boolean destroyItemByItemId(String process, int itemId, long count, L2Object reference, boolean sendMessage)
	{
		// Default: NPCs consume virtual items for their skills
		// TODO: should be logged if even happens.. should be false
		return true;
	}
	
	public boolean destroyItem(String process, int objectId, long count, L2Object reference, boolean sendMessage)
	{
		// Default: NPCs consume virtual items for their skills
		// TODO: should be logged if even happens.. should be false
		return true;
	}
	
	/**
	 * Check if the character is in the given zone Id.
	 * @param zone the zone Id to check
	 * @return {code true} if the character is in that zone
	 */
	@Override
	public final boolean isInsideZone(ZoneIdType zone)
	{
		Instance instance = InstanceManager.getInstance().getInstance(getInstanceId());
		switch (zone)
		{
			case PVP:
				if ((instance != null) && instance.isPvPInstance())
				{
					return true;
				}
				return (_zones[ZoneIdType.PVP.getId()] > 0) && (_zones[ZoneIdType.PEACE.getId()] == 0);
			case PEACE:
				if ((instance != null) && instance.isPvPInstance())
				{
					return false;
				}
		}
		return _zones[zone.getId()] > 0;
	}
	
	/**
	 * @param zone
	 * @param state
	 */
	public final void setInsideZone(ZoneIdType zone, final boolean state)
	{
		if (state)
		{
			_zones[zone.getId()]++;
		}
		else
		{
			_zones[zone.getId()]--;
			if (_zones[zone.getId()] < 0)
			{
				_zones[zone.getId()] = 0;
			}
		}
	}
	
	/**
	 * This will return true if the player is transformed,<br>
	 * but if the player is not transformed it will return false.
	 * @return transformation status
	 */
	public boolean isTransformed()
	{
		return false;
	}
	
	public Transform getTransformation()
	{
		return null;
	}
	
	/**
	 * This will untransform a player if they are an instance of L2Pcinstance and if they are transformed.
	 */
	public void untransform()
	{
		// Just a place holder
	}
	
	/**
	 * This will return true if the player is GM,<br>
	 * but if the player is not GM it will return false.
	 * @return GM status
	 */
	public boolean isGM()
	{
		return false;
	}
	
	/**
	 * Overridden in L2PcInstance.
	 * @return the access level.
	 */
	public L2AccessLevel getAccessLevel()
	{
		return null;
	}
	
	/**
	 * Constructor of L2Character.<br>
	 * <B><U>Concept</U>:</B><br>
	 * Each L2Character owns generic and static properties (ex : all Keltir have the same number of HP...).<br>
	 * All of those properties are stored in a different template for each type of L2Character. Each template is loaded once in the server cache memory (reduce memory use).<br>
	 * When a new instance of L2Character is spawned, server just create a link between the instance and the template This link is stored in <B>_template</B><br>
	 * <B><U> Actions</U>:</B>
	 * <ul>
	 * <li>Set the _template of the L2Character</li>
	 * <li>Set _overloaded to false (the character can take more items)</li>
	 * <li>If L2Character is a L2NPCInstance, copy skills from template to object</li>
	 * <li>If L2Character is a L2NPCInstance, link _calculators to NPC_STD_CALCULATOR</li>
	 * <li>If L2Character is NOT a L2NPCInstance, create an empty _skills slot</li>
	 * <li>If L2Character is a L2PcInstance or L2Summon, copy basic Calculator set to object</li>
	 * </ul>
	 * @param objectId Identifier of the object to initialized
	 * @param template The L2CharTemplate to apply to the object
	 */
	public L2Character(int objectId, L2CharTemplate template)
	{
		super(objectId);
		if (template == null)
		{
			throw new NullPointerException("Template is null!");
		}
		
		setInstanceType(InstanceType.L2Character);
		initCharStat();
		initCharStatus();
		initCharEvents();
		
		// Set its template to the new L2Character
		_template = template;
		
		if (isDoor())
		{
			_calculators = Formulas.getStdDoorCalculators();
		}
		else if (isNpc())
		{
			// Copy the Standard Calculators of the L2NPCInstance in _calculators
			_calculators = NPC_STD_CALCULATOR;
			
			// Copy the skills of the L2NPCInstance from its template to the L2Character Instance
			// The skills list can be affected by spell effects so it's necessary to make a copy
			// to avoid that a spell affecting a L2NPCInstance, affects others L2NPCInstance of the same type too.
			for (L2Skill skill : template.getSkills().values())
			{
				addSkill(skill);
			}
		}
		else
		{
			// If L2Character is a L2PcInstance or a L2Summon, create the basic calculator set
			_calculators = new Calculator[Stats.NUM_STATS];
			
			if (isSummon())
			{
				// Copy the skills of the L2Summon from its template to the L2Character Instance
				// The skills list can be affected by spell effects so it's necessary to make a copy
				// to avoid that a spell affecting a L2Summon, affects others L2Summon of the same type too.
				for (L2Skill skill : template.getSkills().values())
				{
					addSkill(skill);
				}
			}
			
			Formulas.addFuncsToNewCharacter(this);
		}
		
		setIsInvul(true);
		_teleportLock = new ReentrantLock();
	}
	
	protected void initCharStatusUpdateValues()
	{
		_hpUpdateIncCheck = getMaxHp();
		_hpUpdateInterval = _hpUpdateIncCheck / MAX_HP_BAR_PX;
		_hpUpdateDecCheck = _hpUpdateIncCheck - _hpUpdateInterval;
	}
	
	/**
	 * Remove the L2Character from the world when the decay task is launched.<br>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T REMOVE the object from _allObjects of L2World </B></FONT><BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND Server->Client packets to players</B></FONT>
	 */
	public void onDecay()
	{
		L2WorldRegion reg = getWorldRegion();
		decayMe();
		if (reg != null)
		{
			reg.removeFromZones(this);
		}
	}
	
	@Override
	public void onSpawn()
	{
		super.onSpawn();
		revalidateZone(true);
	}
	
	public void onTeleported()
	{
		if (!_teleportLock.tryLock())
		{
			return;
		}
		try
		{
			if (!isTeleporting())
			{
				return;
			}
			spawnMe(getX(), getY(), getZ());
			setIsTeleporting(false);
			getEvents().onTeleported();
		}
		finally
		{
			_teleportLock.unlock();
		}
	}
	
	/**
	 * Add L2Character instance that is attacking to the attacker list.
	 * @param player The L2Character that attacks this one
	 */
	public void addAttackerToAttackByList(L2Character player)
	{
		// DS: moved to L2Attackable
	}
	
	/**
	 * Send a packet to the L2Character AND to all L2PcInstance in the _KnownPlayers of the L2Character.<br>
	 * <B><U>Concept</U>:</B><br>
	 * L2PcInstance in the detection area of the L2Character are identified in <B>_knownPlayers</B>.<br>
	 * In order to inform other players of state modification on the L2Character, server just need to go through _knownPlayers to send Server->Client Packet
	 * @param mov
	 */
	public void broadcastPacket(L2GameServerPacket mov)
	{
		mov.setInvisible(isInvisible());
		Collection<L2PcInstance> plrs = getKnownList().getKnownPlayers().values();
		for (L2PcInstance player : plrs)
		{
			if (player != null)
			{
				player.sendPacket(mov);
			}
		}
	}
	
	/**
	 * Send a packet to the L2Character AND to all L2PcInstance in the radius (max knownlist radius) from the L2Character.<br>
	 * <B><U>Concept</U>:</B><br>
	 * L2PcInstance in the detection area of the L2Character are identified in <B>_knownPlayers</B>.<br>
	 * In order to inform other players of state modification on the L2Character, server just need to go through _knownPlayers to send Server->Client Packet
	 * @param mov
	 * @param radiusInKnownlist
	 */
	public void broadcastPacket(L2GameServerPacket mov, int radiusInKnownlist)
	{
		mov.setInvisible(isInvisible());
		Collection<L2PcInstance> plrs = getKnownList().getKnownPlayers().values();
		for (L2PcInstance player : plrs)
		{
			if ((player != null) && isInsideRadius(player, radiusInKnownlist, false, false))
			{
				player.sendPacket(mov);
			}
		}
	}
	
	/**
	 * @return true if hp update should be done, false if not
	 */
	protected boolean needHpUpdate()
	{
		double currentHp = getCurrentHp();
		double maxHp = getMaxHp();
		
		if ((currentHp <= 1.0) || (maxHp < MAX_HP_BAR_PX))
		{
			return true;
		}
		
		if ((currentHp < _hpUpdateDecCheck) || (Math.abs(currentHp - _hpUpdateDecCheck) <= 1e-6) || (currentHp > _hpUpdateIncCheck) || (Math.abs(currentHp - _hpUpdateIncCheck) <= 1e-6))
		{
			if (Math.abs(currentHp - maxHp) <= 1e-6)
			{
				_hpUpdateIncCheck = currentHp + 1;
				_hpUpdateDecCheck = currentHp - _hpUpdateInterval;
			}
			else
			{
				double doubleMulti = currentHp / _hpUpdateInterval;
				int intMulti = (int) doubleMulti;
				
				_hpUpdateDecCheck = _hpUpdateInterval * (doubleMulti < intMulti ? intMulti-- : intMulti);
				_hpUpdateIncCheck = _hpUpdateDecCheck + _hpUpdateInterval;
			}
			
			return true;
		}
		
		return false;
	}
	
	/**
	 * Send the Server->Client packet StatusUpdate with current HP and MP to all other L2PcInstance to inform.<br>
	 * <B><U>Actions</U>:</B>
	 * <ul>
	 * <li>Create the Server->Client packet StatusUpdate with current HP and MP</li>
	 * <li>Send the Server->Client packet StatusUpdate with current HP and MP to all L2Character called _statusListener that must be informed of HP/MP updates of this L2Character</li>
	 * </ul>
	 * <FONT COLOR=#FF0000><B><U>Caution</U>: This method DOESN'T SEND CP information</B></FONT>
	 */
	public void broadcastStatusUpdate()
	{
		if (getStatus().getStatusListener().isEmpty() || !needHpUpdate())
		{
			return;
		}
		
		// Create the Server->Client packet StatusUpdate with current HP
		StatusUpdate su = new StatusUpdate(this);
		su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
		su.addAttribute(StatusUpdate.CUR_HP, (int) getCurrentHp());
		
		// Go through the StatusListener
		// Send the Server->Client packet StatusUpdate with current HP and MP
		for (L2Character temp : getStatus().getStatusListener())
		{
			if (temp != null)
			{
				temp.sendPacket(su);
			}
		}
	}
	
	/**
	 * @param text
	 */
	public void sendMessage(String text)
	{
		// default implementation
	}
	
	/**
	 * Teleport a L2Character and its pet if necessary.<br>
	 * <B><U>Actions</U>:</B>
	 * <ul>
	 * <li>Stop the movement of the L2Character</li>
	 * <li>Set the x,y,z position of the L2Object and if necessary modify its _worldRegion</li>
	 * <li>Send a Server->Client packet TeleportToLocationt to the L2Character AND to all L2PcInstance in its _KnownPlayers</li>
	 * <li>Modify the position of the pet if necessary</li>
	 * </ul>
	 * @param x
	 * @param y
	 * @param z
	 * @param heading
	 * @param instanceId
	 * @param randomOffset
	 */
	public void teleToLocation(int x, int y, int z, int heading, int instanceId, int randomOffset)
	{
		setInstanceId(instanceId);
		
		if (isPlayer() && DimensionalRiftManager.getInstance().checkIfInRiftZone(getX(), getY(), getZ(), false)) // true -> ignore waiting room :)
		{
			L2PcInstance player = getActingPlayer();
			player.sendMessage("You have been sent to the waiting room.");
			if (player.isInParty() && player.getParty().isInDimensionalRift())
			{
				player.getParty().getDimensionalRift().usedTeleport(player);
			}
			int[] newCoords = DimensionalRiftManager.getInstance().getRoom((byte) 0, (byte) 0).getTeleportCoorinates();
			x = newCoords[0];
			y = newCoords[1];
			z = newCoords[2];
		}
		
		if (_isPendingRevive)
		{
			doRevive();
		}
		stopMove(null, false);
		abortAttack();
		abortCast();
		
		setIsTeleporting(true);
		setTarget(null);
		
		getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
		
		if (Config.OFFSET_ON_TELEPORT_ENABLED && (randomOffset > 0))
		{
			x += Rnd.get(-randomOffset, randomOffset);
			y += Rnd.get(-randomOffset, randomOffset);
		}
		
		z += 5;
		
		// Send a Server->Client packet TeleportToLocationt to the L2Character AND to all L2PcInstance in the _KnownPlayers of the L2Character
		broadcastPacket(new TeleportToLocation(this, x, y, z, heading));
		
		// remove the object from its old location
		decayMe();
		
		// Set the x,y,z position of the L2Object and if necessary modify its _worldRegion
		setXYZ(x, y, z);
		
		// temporary fix for heading on teleports
		if (heading != 0)
		{
			setHeading(heading);
		}
		
		// allow recall of the detached characters
		if (!isPlayer() || ((getActingPlayer().getClient() != null) && getActingPlayer().getClient().isDetached()))
		{
			onTeleported();
		}
		
		revalidateZone(true);
	}
	
	public void teleToLocation(int x, int y, int z, int heading, int instanceId, boolean randomOffset)
	{
		teleToLocation(x, y, z, heading, instanceId, (randomOffset) ? Config.MAX_OFFSET_ON_TELEPORT : 0);
	}
	
	public void teleToLocation(int x, int y, int z, int heading, int instanceId)
	{
		teleToLocation(x, y, z, heading, instanceId, 0);
	}
	
	public void teleToLocation(int x, int y, int z, int heading, boolean randomOffset)
	{
		teleToLocation(x, y, z, heading, -1, (randomOffset) ? Config.MAX_OFFSET_ON_TELEPORT : 0);
	}
	
	public void teleToLocation(int x, int y, int z, int heading)
	{
		teleToLocation(x, y, z, heading, -1, 0);
	}
	
	public void teleToLocation(int x, int y, int z, boolean randomOffset)
	{
		teleToLocation(x, y, z, 0, -1, (randomOffset) ? Config.MAX_OFFSET_ON_TELEPORT : 0);
	}
	
	public void teleToLocation(int x, int y, int z)
	{
		teleToLocation(x, y, z, 0, -1, 0);
	}
	
	public void teleToLocation(ILocational loc, int randomOffset)
	{
		teleToLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getHeading(), loc.getInstanceId(), randomOffset);
	}
	
	public void teleToLocation(ILocational loc, boolean randomOffset)
	{
		teleToLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getHeading(), loc.getInstanceId(), (randomOffset) ? Config.MAX_OFFSET_ON_TELEPORT : 0);
	}
	
	public void teleToLocation(ILocational loc)
	{
		teleToLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getHeading(), loc.getInstanceId(), 0);
	}
	
	public void teleToLocation(TeleportWhereType teleportWhere)
	{
		teleToLocation(MapRegionManager.getInstance().getTeleToLocation(this, teleportWhere), true);
	}
	
	/**
	 * Launch a physical attack against a target (Simple, Bow, Pole or Dual).<br>
	 * <B><U>Actions</U>:</B>
	 * <ul>
	 * <li>Get the active weapon (always equipped in the right hand)</li>
	 * <li>If weapon is a bow, check for arrows, MP and bow re-use delay (if necessary, equip the L2PcInstance with arrows in left hand)</li>
	 * <li>If weapon is a bow, consume MP and set the new period of bow non re-use</li>
	 * <li>Get the Attack Speed of the L2Character (delay (in milliseconds) before next attack)</li>
	 * <li>Select the type of attack to start (Simple, Bow, Pole or Dual) and verify if SoulShot are charged then start calculation</li>
	 * <li>If the Server->Client packet Attack contains at least 1 hit, send the Server->Client packet Attack to the L2Character AND to all L2PcInstance in the _KnownPlayers of the L2Character</li>
	 * <li>Notify AI with EVT_READY_TO_ACT</li>
	 * </ul>
	 * @param target The L2Character targeted
	 */
	protected void doAttack(L2Character target)
	{
		if ((target == null) || isAttackingDisabled() || !getEvents().onAttack(target))
		{
			return;
		}
		
		if (isAttackingDisabled())
		{
			return;
		}
		
		if (getActingPlayer() != null)
		{
			L2PcInstance owner = getActingPlayer();
			
			if (ReunionEvents.isInEvent(owner))
			{
				if (!ReunionEvents.canAttack(owner, target))
				{
					sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
			}
		}
		
		if (!isAlikeDead())
		{
			if ((isNpc() && target.isAlikeDead()) || !getKnownList().knowsObject(target))
			{
				getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			else if (isPlayer())
			{
				if (target.isDead())
				{
					getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
					sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
				
				L2PcInstance actor = getActingPlayer();
				// Players riding wyvern or with special (flying) transformations can do melee attacks, only with skills
				if ((actor.isMounted() && (actor.getMountNpcId() == 12621)) || (actor.isTransformed() && !actor.getTransformation().canAttack()))
				{
					sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
			}
		}
		
		// Check if attacker's weapon can attack
		if (getActiveWeaponItem() != null)
		{
			L2Weapon wpn = getActiveWeaponItem();
			if (!wpn.isAttackWeapon() && !isGM())
			{
				if (wpn.getItemType() == WeaponType.FISHINGROD)
				{
					sendPacket(SystemMessageId.CANNOT_ATTACK_WITH_FISHING_POLE);
				}
				else
				{
					sendPacket(SystemMessageId.THAT_WEAPON_CANT_ATTACK);
				}
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}
		
		if (getActingPlayer() != null)
		{
			if (getActingPlayer().inObserverMode())
			{
				sendPacket(SystemMessageId.OBSERVERS_CANNOT_PARTICIPATE);
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			else if ((target.getActingPlayer() != null) && (getActingPlayer().getSiegeState() > 0) && isInsideZone(ZoneIdType.SIEGE) && (target.getActingPlayer().getSiegeState() == getActingPlayer().getSiegeState()) && (target.getActingPlayer() != this) && (target.getActingPlayer().getSiegeSide() == getActingPlayer().getSiegeSide()))
			{
				if (getActingPlayer().isInSameParty(target.getActingPlayer()) || getActingPlayer().isInSameChannel(target.getActingPlayer()) || getActingPlayer().isInSameClan(target.getActingPlayer()) || getActingPlayer().isInSameAlly(target.getActingPlayer()))
				{
					if (TerritoryWarManager.getInstance().isTWInProgress())
					{
						sendPacket(SystemMessageId.YOU_CANNOT_ATTACK_A_MEMBER_OF_THE_SAME_TERRITORY);
					}
					else
					{
						sendPacket(SystemMessageId.FORCED_ATTACK_IS_IMPOSSIBLE_AGAINST_SIEGE_SIDE_TEMPORARY_ALLIED_MEMBERS);
					}
					sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
			}
			
			// Checking if target has moved to peace zone
			else if (target.isInsidePeaceZone(getActingPlayer()))
			{
				getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}
		else if (isInsidePeaceZone(this, target))
		{
			getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		stopEffectsOnAction();
		
		// Get the active weapon item corresponding to the active weapon instance (always equipped in the right hand)
		L2Weapon weaponItem = getActiveWeaponItem();
		
		// GeoData Los Check here (or dz > 1000)
		if (!GeoData.getInstance().canSeeTarget(this, target))
		{
			sendPacket(SystemMessageId.CANT_SEE_TARGET);
			getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// BOW and CROSSBOW checks
		if ((weaponItem != null) && !isTransformed())
		{
			if (weaponItem.getItemType() == WeaponType.BOW)
			{
				// Check for arrows and MP
				if (isPlayer())
				{
					// Checking if target has moved to peace zone - only for player-bow attacks at the moment
					// Other melee is checked in movement code and for offensive spells a check is done every time
					if (target.isInsidePeaceZone(getActingPlayer()))
					{
						getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
						sendPacket(ActionFailed.STATIC_PACKET);
						return;
					}
					
					// Equip arrows needed in left hand and send a Server->Client packet ItemList to the L2PcInstance then return True
					if (!checkAndEquipArrows())
					{
						// Cancel the action because the L2PcInstance have no arrow
						getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
						sendPacket(ActionFailed.STATIC_PACKET);
						sendPacket(SystemMessageId.NOT_ENOUGH_ARROWS);
						return;
					}
					
					// Verify if the bow can be use
					if (_disableBowAttackEndTime <= GameTimeController.getInstance().getGameTicks())
					{
						// Verify if L2PcInstance owns enough MP
						int mpConsume = weaponItem.getMpConsume();
						if ((weaponItem.getReducedMpConsume() > 0) && (Rnd.get(100) < weaponItem.getReducedMpConsumeChance()))
						{
							mpConsume = weaponItem.getReducedMpConsume();
						}
						mpConsume = (int) calcStat(Stats.BOW_MP_CONSUME_RATE, mpConsume, null, null);
						
						if (getCurrentMp() < mpConsume)
						{
							// If L2PcInstance doesn't have enough MP, stop the attack
							ThreadPoolManager.getInstance().scheduleAi(new NotifyAITask(this, CtrlEvent.EVT_READY_TO_ACT), 1000);
							sendPacket(SystemMessageId.NOT_ENOUGH_MP);
							sendPacket(ActionFailed.STATIC_PACKET);
							return;
						}
						
						// If L2PcInstance have enough MP, the bow consumes it
						if (mpConsume > 0)
						{
							getStatus().reduceMp(mpConsume);
						}
						
						// Set the period of bow no re-use
						_disableBowAttackEndTime = (5 * GameTimeController.TICKS_PER_SECOND) + GameTimeController.getInstance().getGameTicks();
					}
					else
					{
						// Cancel the action because the bow can't be re-use at this moment
						ThreadPoolManager.getInstance().scheduleAi(new NotifyAITask(this, CtrlEvent.EVT_READY_TO_ACT), 1000);
						sendPacket(ActionFailed.STATIC_PACKET);
						return;
					}
				}
			}
			if (weaponItem.getItemType() == WeaponType.CROSSBOW)
			{
				// Check for bolts
				if (isPlayer())
				{
					// Checking if target has moved to peace zone - only for player-crossbow attacks at the moment
					// Other melee is checked in movement code and for offensive spells a check is done every time
					if (target.isInsidePeaceZone(getActingPlayer()))
					{
						getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
						sendPacket(ActionFailed.STATIC_PACKET);
						return;
					}
					
					// Equip bolts needed in left hand and send a Server->Client packet ItemList to the L2PcINstance then return True
					if (!checkAndEquipBolts())
					{
						// Cancel the action because the L2PcInstance have no arrow
						getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
						sendPacket(ActionFailed.STATIC_PACKET);
						sendPacket(SystemMessageId.NOT_ENOUGH_BOLTS);
						return;
					}
					
					// Verify if the crossbow can be use
					if (_disableCrossBowAttackEndTime <= GameTimeController.getInstance().getGameTicks())
					{
						// Verify if L2PcInstance owns enough MP
						int mpConsume = weaponItem.getMpConsume();
						if ((weaponItem.getReducedMpConsume() > 0) && (Rnd.get(100) < weaponItem.getReducedMpConsumeChance()))
						{
							mpConsume = weaponItem.getReducedMpConsume();
						}
						mpConsume = (int) calcStat(Stats.BOW_MP_CONSUME_RATE, mpConsume, null, null);
						
						if (getCurrentMp() < mpConsume)
						{
							// If L2PcInstance doesn't have enough MP, stop the attack
							ThreadPoolManager.getInstance().scheduleAi(new NotifyAITask(this, CtrlEvent.EVT_READY_TO_ACT), 1000);
							sendPacket(SystemMessageId.NOT_ENOUGH_MP);
							sendPacket(ActionFailed.STATIC_PACKET);
							return;
						}
						
						// If L2PcInstance have enough MP, the bow consumes it
						if (mpConsume > 0)
						{
							getStatus().reduceMp(mpConsume);
						}
						
						// Set the period of crossbow no re-use
						_disableCrossBowAttackEndTime = (5 * GameTimeController.TICKS_PER_SECOND) + GameTimeController.getInstance().getGameTicks();
					}
					else
					{
						// Cancel the action because the crossbow can't be re-use at this moment
						ThreadPoolManager.getInstance().scheduleAi(new NotifyAITask(this, CtrlEvent.EVT_READY_TO_ACT), 1000);
						sendPacket(ActionFailed.STATIC_PACKET);
						return;
					}
				}
				else if (isNpc())
				{
					if (_disableCrossBowAttackEndTime > GameTimeController.getInstance().getGameTicks())
					{
						return;
					}
				}
			}
		}
		
		if (ReunionEvents.isInEvent(this) && ReunionEvents.isInEvent(target))
		{
			if (!ReunionEvents.onAttack(this, target))
			{
				getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}
		
		// Add the L2PcInstance to _knownObjects and _knownPlayer of the target
		target.getKnownList().addKnownObject(this);
		
		// Reduce the current CP if TIREDNESS configuration is activated
		if (Config.ALT_GAME_TIREDNESS)
		{
			setCurrentCp(getCurrentCp() - 10);
		}
		
		// Verify if soulshots are charged.
		final boolean wasSSCharged = isChargedShot(ShotType.SOULSHOTS);
		// Get the Attack Speed of the L2Character (delay (in milliseconds) before next attack)
		final int timeAtk = calculateTimeBetweenAttacks(target, weaponItem);
		// the hit is calculated to happen halfway to the animation - might need further tuning e.g. in bow case
		final int timeToHit = timeAtk / 2;
		_attackEndTime = (GameTimeController.getInstance().getGameTicks() + (timeAtk / GameTimeController.MILLIS_IN_TICK)) - 1;
		final int ssGrade = (weaponItem != null) ? weaponItem.getItemGradeSPlus().getId() : 0;
		// Create a Server->Client packet Attack
		Attack attack = new Attack(this, target, wasSSCharged, ssGrade);
		
		// Set the Attacking Body part to CHEST
		setAttackingBodypart();
		// Make sure that char is facing selected target
		// also works: setHeading(Util.convertDegreeToClientHeading(Util.calculateAngleFrom(this, target)));
		setHeading(Util.calculateHeadingFrom(this, target));
		
		// Get the Attack Reuse Delay of the L2Weapon
		int reuse = calculateReuseTime(target, weaponItem);
		boolean hitted;
		// Select the type of attack to start
		if ((weaponItem == null) || isTransformed())
		{
			hitted = doAttackHitSimple(attack, target, timeToHit);
		}
		else if (weaponItem.getItemType() == WeaponType.BOW)
		{
			hitted = doAttackHitByBow(attack, target, timeAtk, reuse);
		}
		else if (weaponItem.getItemType() == WeaponType.CROSSBOW)
		{
			hitted = doAttackHitByCrossBow(attack, target, timeAtk, reuse);
		}
		else if (weaponItem.getItemType() == WeaponType.POLE)
		{
			hitted = doAttackHitByPole(attack, target, timeToHit);
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
		L2PcInstance player = getActingPlayer();
		
		if (player != null)
		{
			AttackStanceTaskManager.getInstance().addAttackStanceTask(player);
		}
		
		if (player != null)
		{
			if (player.getSummon() != target)
			{
				player.updatePvPStatus(target);
			}
		}
		// Check if hit isn't missed
		if (!hitted)
		{
			abortAttack(); // Abort the attack of the L2Character and send Server->Client ActionFailed packet
		}
		else
		{
			// If we didn't miss the hit, discharge the shoulshots, if any
			setChargedShot(ShotType.SOULSHOTS, false);
			
			if (player != null)
			{
				if (player.isCursedWeaponEquipped())
				{
					// If hitted by a cursed weapon, Cp is reduced to 0
					if (!target.isInvul())
					{
						target.setCurrentCp(0);
					}
				}
				else if (player.isHero())
				{
					// If a cursed weapon is hit by a Hero, Cp is reduced to 0
					if (target.isPlayer() && target.getActingPlayer().isCursedWeaponEquipped())
					{
						target.setCurrentCp(0);
					}
				}
			}
		}
		
		// If the Server->Client packet Attack contains at least 1 hit, send the Server->Client packet Attack
		// to the L2Character AND to all L2PcInstance in the _KnownPlayers of the L2Character
		if (attack.hasHits())
		{
			broadcastPacket(attack);
		}
		
		// Notify AI with EVT_READY_TO_ACT
		ThreadPoolManager.getInstance().scheduleAi(new NotifyAITask(this, CtrlEvent.EVT_READY_TO_ACT), timeAtk + reuse);
	}
	
	/**
	 * Launch a Bow attack.<br>
	 * <B><U>Actions</U>:</B>
	 * <ul>
	 * <li>Calculate if hit is missed or not</li>
	 * <li>Consume arrows</li>
	 * <li>If hit isn't missed, calculate if shield defense is efficient</li>
	 * <li>If hit isn't missed, calculate if hit is critical</li>
	 * <li>If hit isn't missed, calculate physical damages</li>
	 * <li>If the L2Character is a L2PcInstance, Send a Server->Client packet SetupGauge</li>
	 * <li>Create a new hit task with Medium priority</li>
	 * <li>Calculate and set the disable delay of the bow in function of the Attack Speed</li>
	 * <li>Add this hit to the Server-Client packet Attack</li>
	 * </ul>
	 * @param attack Server->Client packet Attack in which the hit will be added
	 * @param target The L2Character targeted
	 * @param sAtk The Attack Speed of the attacker
	 * @param reuse
	 * @return True if the hit isn't missed
	 */
	private boolean doAttackHitByBow(Attack attack, L2Character target, int sAtk, int reuse)
	{
		int damage1 = 0;
		byte shld1 = 0;
		boolean crit1 = false;
		
		// Calculate if hit is missed or not
		boolean miss1 = Formulas.calcHitMiss(this, target);
		
		// Consume arrows
		reduceArrowCount(false);
		
		_move = null;
		
		// Check if hit isn't missed
		if (!miss1)
		{
			// Calculate if shield defense is efficient
			shld1 = Formulas.calcShldUse(this, target);
			
			// Calculate if hit is critical
			crit1 = Formulas.calcCrit(getStat().getCriticalHit(target, null), false, target);
			
			// Calculate physical damages
			damage1 = (int) Formulas.calcPhysDam(this, target, null, shld1, crit1, false, attack.hasSoulshot());
			
			// Bows Ranged Damage Formula (Damage gradually decreases when 60% or lower than full hit range, and increases when 60% or higher).
			// full hit range is 500 which is the base bow range, and the 60% of this is 800.
			damage1 *= (Math.sqrt(getDistanceSq(target)) / 4000) + 0.8;
		}
		
		// Check if the L2Character is a L2PcInstance
		if (isPlayer())
		{
			// Send a system message
			sendPacket(SystemMessageId.GETTING_READY_TO_SHOOT_AN_ARROW);
			
			// Send a Server->Client packet SetupGauge
			SetupGauge sg = new SetupGauge(SetupGauge.RED, sAtk + reuse);
			sendPacket(sg);
		}
		
		// Create a new hit task with Medium priority
		ThreadPoolManager.getInstance().scheduleAi(new HitTask(this, target, damage1, crit1, miss1, attack.hasSoulshot(), shld1), sAtk);
		
		// Calculate and set the disable delay of the bow in function of the Attack Speed
		_disableBowAttackEndTime = ((sAtk + reuse) / GameTimeController.MILLIS_IN_TICK) + GameTimeController.getInstance().getGameTicks();
		
		// Add this hit to the Server-Client packet Attack
		attack.addHit(target, damage1, miss1, crit1, shld1);
		
		// Return true if hit isn't missed
		return !miss1;
	}
	
	/**
	 * Launch a CrossBow attack.<br>
	 * <B><U>Actions</U>:</B>
	 * <ul>
	 * <li>Calculate if hit is missed or not</li>
	 * <li>Consume bolts</li>
	 * <li>If hit isn't missed, calculate if shield defense is efficient</li>
	 * <li>If hit isn't missed, calculate if hit is critical</li>
	 * <li>If hit isn't missed, calculate physical damages</li>
	 * <li>If the L2Character is a L2PcInstance, Send a Server->Client packet SetupGauge</li>
	 * <li>Create a new hit task with Medium priority</li>
	 * <li>Calculate and set the disable delay of the crossbow in function of the Attack Speed</li>
	 * <li>Add this hit to the Server-Client packet Attack</li>
	 * @param attack Server->Client packet Attack in which the hit will be added
	 * @param target The L2Character targeted
	 * @param sAtk The Attack Speed of the attacker
	 * @param reuse
	 * @return True if the hit isn't missed
	 */
	private boolean doAttackHitByCrossBow(Attack attack, L2Character target, int sAtk, int reuse)
	{
		int damage1 = 0;
		byte shld1 = 0;
		boolean crit1 = false;
		
		// Calculate if hit is missed or not
		boolean miss1 = Formulas.calcHitMiss(this, target);
		
		// Consume bolts
		reduceArrowCount(true);
		
		_move = null;
		
		// Check if hit isn't missed
		if (!miss1)
		{
			// Calculate if shield defense is efficient
			shld1 = Formulas.calcShldUse(this, target);
			
			// Calculate if hit is critical
			crit1 = Formulas.calcCrit(getStat().getCriticalHit(target, null), false, target);
			
			// Calculate physical damages
			damage1 = (int) Formulas.calcPhysDam(this, target, null, shld1, crit1, false, attack.hasSoulshot());
		}
		
		// Check if the L2Character is a L2PcInstance
		if (isPlayer())
		{
			// Send a system message
			sendPacket(SystemMessageId.CROSSBOW_PREPARING_TO_FIRE);
			
			// Send a Server->Client packet SetupGauge
			SetupGauge sg = new SetupGauge(SetupGauge.RED, sAtk + reuse);
			sendPacket(sg);
		}
		
		// Create a new hit task with Medium priority
		ThreadPoolManager.getInstance().scheduleAi(new HitTask(this, target, damage1, crit1, miss1, attack.hasSoulshot(), shld1), sAtk);
		
		// Calculate and set the disable delay of the bow in function of the Attack Speed
		_disableCrossBowAttackEndTime = ((sAtk + reuse) / GameTimeController.MILLIS_IN_TICK) + GameTimeController.getInstance().getGameTicks();
		
		// Add this hit to the Server-Client packet Attack
		attack.addHit(target, damage1, miss1, crit1, shld1);
		
		// Return true if hit isn't missed
		return !miss1;
	}
	
	/**
	 * Launch a Dual attack.<br>
	 * <B><U>Actions</U>:</B>
	 * <ul>
	 * <li>Calculate if hits are missed or not</li>
	 * <li>If hits aren't missed, calculate if shield defense is efficient</li>
	 * <li>If hits aren't missed, calculate if hit is critical</li>
	 * <li>If hits aren't missed, calculate physical damages</li>
	 * <li>Create 2 new hit tasks with Medium priority</li>
	 * <li>Add those hits to the Server-Client packet Attack</li>
	 * </ul>
	 * @param attack Server->Client packet Attack in which the hit will be added
	 * @param target The L2Character targeted
	 * @param sAtk
	 * @return True if hit 1 or hit 2 isn't missed
	 */
	private boolean doAttackHitByDual(Attack attack, L2Character target, int sAtk)
	{
		int damage1 = 0;
		int damage2 = 0;
		byte shld1 = 0;
		byte shld2 = 0;
		boolean crit1 = false;
		boolean crit2 = false;
		
		// Calculate if hits are missed or not
		boolean miss1 = Formulas.calcHitMiss(this, target);
		boolean miss2 = Formulas.calcHitMiss(this, target);
		
		// Check if hit 1 isn't missed
		if (!miss1)
		{
			// Calculate if shield defense is efficient against hit 1
			shld1 = Formulas.calcShldUse(this, target);
			
			// Calculate if hit 1 is critical
			crit1 = Formulas.calcCrit(getStat().getCriticalHit(target, null), false, target);
			
			// Calculate physical damages of hit 1
			damage1 = (int) Formulas.calcPhysDam(this, target, null, shld1, crit1, true, attack.hasSoulshot());
			damage1 /= 2;
		}
		
		// Check if hit 2 isn't missed
		if (!miss2)
		{
			// Calculate if shield defense is efficient against hit 2
			shld2 = Formulas.calcShldUse(this, target);
			
			// Calculate if hit 2 is critical
			crit2 = Formulas.calcCrit(getStat().getCriticalHit(target, null), false, target);
			
			// Calculate physical damages of hit 2
			damage2 = (int) Formulas.calcPhysDam(this, target, null, shld2, crit2, true, attack.hasSoulshot());
			damage2 /= 2;
		}
		
		// Create a new hit task with Medium priority for hit 1
		ThreadPoolManager.getInstance().scheduleAi(new HitTask(this, target, damage1, crit1, miss1, attack.hasSoulshot(), shld1), sAtk / 2);
		
		// Create a new hit task with Medium priority for hit 2 with a higher delay
		ThreadPoolManager.getInstance().scheduleAi(new HitTask(this, target, damage2, crit2, miss2, attack.hasSoulshot(), shld2), sAtk);
		
		// Add those hits to the Server-Client packet Attack
		attack.addHit(target, damage1, miss1, crit1, shld1);
		attack.addHit(target, damage2, miss2, crit2, shld2);
		
		// Return true if hit 1 or hit 2 isn't missed
		return (!miss1 || !miss2);
	}
	
	/**
	 * Launch a Pole attack.<br>
	 * <B><U>Actions</U>:</B>
	 * <ul>
	 * <li>Get all visible objects in a spherical area near the L2Character to obtain possible targets</li>
	 * <li>If possible target is the L2Character targeted, launch a simple attack against it</li>
	 * <li>If possible target isn't the L2Character targeted but is attackable, launch a simple attack against it</li>
	 * </ul>
	 * @param attack Server->Client packet Attack in which the hit will be added
	 * @param target
	 * @param sAtk
	 * @return True if one hit isn't missed
	 */
	private boolean doAttackHitByPole(Attack attack, L2Character target, int sAtk)
	{
		// double angleChar;
		int maxRadius = getStat().getPhysicalAttackRange();
		int maxAngleDiff = getStat().getPhysicalAttackAngle();
		
		// o1 x: 83420 y: 148158 (Giran)
		// o2 x: 83379 y: 148081 (Giran)
		// dx = -41
		// dy = -77
		// distance between o1 and o2 = 87.24
		// arctan2 = -120 (240) degree (excel arctan2(dx, dy); java arctan2(dy, dx))
		//
		// o2
		//
		// o1 ----- (heading)
		// In the diagram above:
		// o1 has a heading of 0/360 degree from horizontal (facing East)
		// Degree of o2 in respect to o1 = -120 (240) degree
		//
		// o2 / (heading)
		// /
		// o1
		// In the diagram above
		// o1 has a heading of -80 (280) degree from horizontal (facing north east)
		// Degree of o2 in respect to 01 = -40 (320) degree
		
		// Get char's heading degree
		// angleChar = Util.convertHeadingToDegree(getHeading());
		
		// H5 Changes: without Polearm Mastery (skill 216) max simultaneous attacks is 3 (1 by default + 2 in skill 3599).
		int attackRandomCountMax = (int) getStat().calcStat(Stats.ATTACK_COUNT_MAX, 1, null, null);
		int attackcount = 0;
		
		// if (angleChar <= 0) angleChar += 360;
		
		boolean hitted = doAttackHitSimple(attack, target, 100, sAtk);
		double attackpercent = 85;
		L2Character temp;
		Collection<L2Object> objs = getKnownList().getKnownObjects().values();
		
		for (L2Object obj : objs)
		{
			if (obj == target)
			{
				continue; // do not hit twice
			}
			// Check if the L2Object is a L2Character
			if (obj instanceof L2Character)
			{
				if (obj.isPet() && isPlayer() && (((L2PetInstance) obj).getOwner() == getActingPlayer()))
				{
					continue;
				}
				
				if (!Util.checkIfInRange(maxRadius, this, obj, false))
				{
					continue;
				}
				
				// otherwise hit too high/low. 650 because mob z coord
				// sometimes wrong on hills
				if (Math.abs(obj.getZ() - getZ()) > 650)
				{
					continue;
				}
				if (!isFacing(obj, maxAngleDiff))
				{
					continue;
				}
				
				if (isAttackable() && obj.isPlayer() && getTarget().isAttackable())
				{
					continue;
				}
				
				if (isAttackable() && obj.isAttackable() && (((L2Attackable) this).getEnemyClan() == null) && (((L2Attackable) this).getIsChaos() == 0))
				{
					continue;
				}
				
				if (isAttackable() && obj.isAttackable() && !((L2Attackable) this).getEnemyClan().equals(((L2Attackable) obj).getClan()) && (((L2Attackable) this).getIsChaos() == 0))
				{
					continue;
				}
				
				temp = (L2Character) obj;
				
				// Launch a simple attack against the L2Character targeted
				if (!temp.isAlikeDead())
				{
					if ((temp == getAI().getAttackTarget()) || temp.isAutoAttackable(this))
					{
						hitted |= doAttackHitSimple(attack, temp, attackpercent, sAtk);
						attackpercent /= 1.15;
						
						attackcount++;
						if (attackcount > attackRandomCountMax)
						{
							break;
						}
					}
				}
			}
		}
		
		// Return true if one hit isn't missed
		return hitted;
	}
	
	/**
	 * Launch a simple attack.<br>
	 * <B><U>Actions</U>:</B>
	 * <ul>
	 * <li>Calculate if hit is missed or not</li>
	 * <li>If hit isn't missed, calculate if shield defense is efficient</li>
	 * <li>If hit isn't missed, calculate if hit is critical</li>
	 * <li>If hit isn't missed, calculate physical damages</li>
	 * <li>Create a new hit task with Medium priority</li>
	 * <li>Add this hit to the Server-Client packet Attack</li>
	 * </ul>
	 * @param attack Server->Client packet Attack in which the hit will be added
	 * @param target The L2Character targeted
	 * @param sAtk
	 * @return True if the hit isn't missed
	 */
	private boolean doAttackHitSimple(Attack attack, L2Character target, int sAtk)
	{
		return doAttackHitSimple(attack, target, 100, sAtk);
	}
	
	private boolean doAttackHitSimple(Attack attack, L2Character target, double attackpercent, int sAtk)
	{
		int damage1 = 0;
		byte shld1 = 0;
		boolean crit1 = false;
		
		// Calculate if hit is missed or not
		boolean miss1 = Formulas.calcHitMiss(this, target);
		
		// Check if hit isn't missed
		if (!miss1)
		{
			// Calculate if shield defense is efficient
			shld1 = Formulas.calcShldUse(this, target);
			
			// Calculate if hit is critical
			crit1 = Formulas.calcCrit(getStat().getCriticalHit(target, null), false, target);
			
			// Calculate physical damages
			damage1 = (int) Formulas.calcPhysDam(this, target, null, shld1, crit1, false, attack.hasSoulshot());
			
			if (attackpercent != 100)
			{
				damage1 = (int) ((damage1 * attackpercent) / 100);
			}
		}
		
		// Create a new hit task with Medium priority
		ThreadPoolManager.getInstance().scheduleAi(new HitTask(this, target, damage1, crit1, miss1, attack.hasSoulshot(), shld1), sAtk);
		
		// Add this hit to the Server-Client packet Attack
		attack.addHit(target, damage1, miss1, crit1, shld1);
		
		// Return true if hit isn't missed
		return !miss1;
	}
	
	/**
	 * Manage the casting task (casting and interrupt time, re-use delay...) and display the casting bar and animation on client.<br>
	 * <B><U>Actions</U>:</B>
	 * <ul>
	 * <li>Verify the possibility of the the cast : skill is a spell, caster isn't muted...</li>
	 * <li>Get the list of all targets (ex : area effects) and define the L2Charcater targeted (its stats will be used in calculation)</li>
	 * <li>Calculate the casting time (base + modifier of MAtkSpd), interrupt time and re-use delay</li>
	 * <li>Send a Server->Client packet MagicSkillUser (to display casting animation), a packet SetupGauge (to display casting bar) and a system message</li>
	 * <li>Disable all skills during the casting time (create a task EnableAllSkills)</li>
	 * <li>Disable the skill during the re-use delay (create a task EnableSkill)</li>
	 * <li>Create a task MagicUseTask (that will call method onMagicUseTimer) to launch the Magic Skill at the end of the casting time</li>
	 * </ul>
	 * @param skill The L2Skill to use
	 */
	public void doCast(L2Skill skill)
	{
		beginCast(skill, false);
	}
	
	public void doSimultaneousCast(L2Skill skill)
	{
		beginCast(skill, true);
	}
	
	public void doCast(L2Skill skill, L2Character target, L2Object[] targets)
	{
		if (!checkDoCastConditions(skill))
		{
			setIsCastingNow(false);
			return;
		}
		
		// Override casting type
		if (skill.isSimultaneousCast())
		{
			doSimultaneousCast(skill, target, targets);
			return;
		}
		
		stopEffectsOnAction();
		
		beginCast(skill, false, target, targets);
	}
	
	public void doSimultaneousCast(L2Skill skill, L2Character target, L2Object[] targets)
	{
		if (!checkDoCastConditions(skill))
		{
			setIsCastingSimultaneouslyNow(false);
			return;
		}
		stopEffectsOnAction();
		
		// Recharge AutoSoulShot
		// this method should not used with L2Playable
		
		beginCast(skill, true, target, targets);
	}
	
	private void beginCast(L2Skill skill, boolean simultaneously)
	{
		boolean abort = false;
		
		if (getActingPlayer() != null)
		{
			L2PcInstance owner = getActingPlayer();
			
			if (ReunionEvents.isInEvent(owner) && (getTarget() instanceof L2Character))
			{
				if (!ReunionEvents.isSkillNeutral(owner, skill))
				{
					if (ReunionEvents.isSkillOffensive(owner, skill) && !ReunionEvents.canAttack(owner, (L2Character) getTarget()) && (owner != getTarget()))
					{
						abort = true;
					}
					else if (!ReunionEvents.isSkillOffensive(owner, skill) && !ReunionEvents.canSupport(owner, (L2Character) getTarget()))
					{
						abort = true;
					}
				}
			}
		}
		
		if (!checkDoCastConditions(skill) || abort)
		{
			if (simultaneously)
			{
				setIsCastingSimultaneouslyNow(false);
			}
			else
			{
				setIsCastingNow(false);
			}
			if (isPlayer())
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				getAI().setIntention(AI_INTENTION_ACTIVE);
			}
			return;
		}
		
		// Override casting type
		if (skill.isSimultaneousCast() && !simultaneously)
		{
			simultaneously = true;
		}
		
		stopEffectsOnAction();
		
		// Set the target of the skill in function of Skill Type and Target Type
		L2Character target = null;
		// Get all possible targets of the skill in a table in function of the skill target type
		L2Object[] targets = skill.getTargetList(this);
		
		boolean doit = false;
		
		// AURA skills should always be using caster as target
		switch (skill.getTargetType())
		{
			case AREA_SUMMON: // We need it to correct facing
				target = getSummon();
				break;
			case AURA:
			case AURA_CORPSE_MOB:
			case FRONT_AURA:
			case BEHIND_AURA:
			case GROUND:
				target = this;
				break;
			case SELF:
			case PET:
			case SERVITOR:
			case SUMMON:
			case OWNER_PET:
			case PARTY:
			case CLAN:
			case PARTY_CLAN:
			case COMMAND_CHANNEL:
				doit = true;
			default:
				if (targets.length == 0)
				{
					if (simultaneously)
					{
						setIsCastingSimultaneouslyNow(false);
					}
					else
					{
						setIsCastingNow(false);
					}
					// Send a Server->Client packet ActionFailed to the L2PcInstance
					if (isPlayer())
					{
						sendPacket(ActionFailed.STATIC_PACKET);
						getAI().setIntention(AI_INTENTION_ACTIVE);
					}
					return;
				}
				
				switch (skill.getSkillType())
				{
					case BUFF:
						doit = true;
						break;
					case DUMMY:
						if (skill.hasEffectType(L2EffectType.CPHEAL, L2EffectType.HEAL))
						{
							doit = true;
						}
						break;
				}
				
				if (doit)
				{
					target = (L2Character) targets[0];
				}
				else
				{
					target = (L2Character) getTarget();
				}
		}
		beginCast(skill, simultaneously, target, targets);
	}
	
	private void beginCast(L2Skill skill, boolean simultaneously, L2Character target, L2Object[] targets)
	{
		if (target == null)
		{
			if (simultaneously)
			{
				setIsCastingSimultaneouslyNow(false);
			}
			else
			{
				setIsCastingNow(false);
			}
			if (isPlayer())
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				getAI().setIntention(AI_INTENTION_ACTIVE);
			}
			return;
		}
		
		if (skill.getSkillType() == L2SkillType.RESURRECT)
		{
			if (isResurrectionBlocked() || target.isResurrectionBlocked())
			{
				sendPacket(SystemMessageId.REJECT_RESURRECTION); // Reject resurrection
				target.sendPacket(SystemMessageId.REJECT_RESURRECTION); // Reject resurrection
				
				if (simultaneously)
				{
					setIsCastingSimultaneouslyNow(false);
				}
				else
				{
					setIsCastingNow(false);
				}
				
				if (isPlayer())
				{
					getAI().setIntention(AI_INTENTION_ACTIVE);
					sendPacket(ActionFailed.STATIC_PACKET);
				}
				return;
			}
		}
		
		// Get the Identifier of the skill
		int magicId = skill.getId();
		
		// Get the Base Casting Time of the Skills.
		int skillTime = (skill.getHitTime() + skill.getCoolTime());
		
		boolean effectWhileCasting = (skill.getSkillType() == L2SkillType.FUSION) || (skill.getSkillType() == L2SkillType.SIGNET_CASTTIME);
		// Don't modify the skill time for FORCE_BUFF skills. The skill time for those skills represent the buff time.
		if (!effectWhileCasting)
		{
			// Calculate the Casting Time of the "Non-Static" Skills (with caster PAtk/MAtkSpd).
			if (!skill.isStatic())
			{
				skillTime = Formulas.calcAtkSpd(this, skill, skillTime);
			}
			// Calculate the Casting Time of Magic Skills (reduced in 40% if using SPS/BSPS)
			if (skill.isMagic() && (isChargedShot(ShotType.SPIRITSHOTS) || isChargedShot(ShotType.BLESSED_SPIRITSHOTS)))
			{
				skillTime = (int) (0.6 * skillTime);
			}
		}
		
		// Avoid broken Casting Animation.
		// Client can't handle less than 550ms Casting Animation in Magic Skills with more than 550ms base.
		if (skill.isMagic() && ((skill.getHitTime() + skill.getCoolTime()) > 550) && (skillTime < 550))
		{
			skillTime = 550;
		}
		// Client can't handle less than 500ms Casting Animation in Physical Skills with 500ms base or more.
		else if (!skill.isStatic() && ((skill.getHitTime() + skill.getCoolTime()) >= 500) && (skillTime < 500))
		{
			skillTime = 500;
		}
		
		// queue herbs and potions
		if (isCastingSimultaneouslyNow() && simultaneously)
		{
			ThreadPoolManager.getInstance().scheduleAi(new UsePotionTask(this, skill), 100);
			return;
		}
		
		// Set the _castInterruptTime and casting status (L2PcInstance already has this true)
		if (simultaneously)
		{
			setIsCastingSimultaneouslyNow(true);
		}
		else
		{
			setIsCastingNow(true);
		}
		
		if (!simultaneously)
		{
			_castInterruptTime = -2 + GameTimeController.getInstance().getGameTicks() + (skillTime / GameTimeController.MILLIS_IN_TICK);
			setLastSkillCast(skill);
		}
		else
		{
			setLastSimultaneousSkillCast(skill);
		}
		
		// Calculate the Reuse Time of the Skill
		int reuseDelay;
		if (skill.isStaticReuse() || skill.isStatic())
		{
			reuseDelay = (skill.getReuseDelay());
		}
		else if (skill.isMagic())
		{
			reuseDelay = (int) (skill.getReuseDelay() * calcStat(Stats.MAGIC_REUSE_RATE, 1, null, null));
		}
		else
		{
			reuseDelay = (int) (skill.getReuseDelay() * calcStat(Stats.P_REUSE, 1, null, null));
		}
		
		boolean skillMastery = Formulas.calcSkillMastery(this, skill);
		
		// Skill reuse check
		if ((reuseDelay > 30000) && !skillMastery)
		{
			addTimeStamp(skill, reuseDelay);
		}
		
		// Check if this skill consume mp on start casting
		int initmpcons = getStat().getMpInitialConsume(skill);
		if (initmpcons > 0)
		{
			getStatus().reduceMp(initmpcons);
			StatusUpdate su = new StatusUpdate(this);
			su.addAttribute(StatusUpdate.CUR_MP, (int) getCurrentMp());
			sendPacket(su);
		}
		
		// Disable the skill during the re-use delay and create a task EnableSkill with Medium priority to enable it at the end of the re-use delay
		if (reuseDelay > 10)
		{
			if (skillMastery)
			{
				reuseDelay = 100;
				
				if (getActingPlayer() != null)
				{
					getActingPlayer().sendPacket(SystemMessageId.SKILL_READY_TO_USE_AGAIN);
				}
			}
			
			disableSkill(skill, reuseDelay);
		}
		
		// Make sure that char is facing selected target
		if (target != this)
		{
			setHeading(Util.calculateHeadingFrom(this, target));
			broadcastPacket(new ExRotation(getObjectId(), getHeading()));
		}
		
		// For force buff skills, start the effect as long as the player is casting.
		if (effectWhileCasting)
		{
			// Consume Items if necessary and Send the Server->Client packet InventoryUpdate with Item modification to all the L2Character
			if (skill.getItemConsumeId() > 0)
			{
				if (!destroyItemByItemId("Consume", skill.getItemConsumeId(), skill.getItemConsume(), null, true))
				{
					sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
					if (simultaneously)
					{
						setIsCastingSimultaneouslyNow(false);
					}
					else
					{
						setIsCastingNow(false);
					}
					
					if (isPlayer())
					{
						getAI().setIntention(AI_INTENTION_ACTIVE);
					}
					return;
				}
			}
			
			// Consume Souls if necessary
			if (skill.getMaxSoulConsumeCount() > 0)
			{
				if (isPlayer())
				{
					if (!getActingPlayer().decreaseSouls(skill.getMaxSoulConsumeCount(), skill))
					{
						if (simultaneously)
						{
							setIsCastingSimultaneouslyNow(false);
						}
						else
						{
							setIsCastingNow(false);
						}
						return;
					}
				}
			}
			
			if (skill.getSkillType() == L2SkillType.FUSION)
			{
				startFusionSkill(target, skill);
			}
			else
			{
				callSkill(skill, targets);
			}
		}
		
		// Send a Server->Client packet MagicSkillUser with target, displayId, level, skillTime, reuseDelay
		// to the L2Character AND to all L2PcInstance in the _KnownPlayers of the L2Character
		broadcastPacket(new MagicSkillUse(this, target, skill.getDisplayId(), skill.getDisplayLevel(), skillTime, reuseDelay));
		
		// Send a system message USE_S1 to the L2Character
		if (isPlayer())
		{
			SystemMessage sm = null;
			switch (magicId)
			{
				case 1312: // Fishing
				{
					// Done in L2PcInstance.startFishing()
					break;
				}
				case 2046: // Wolf Collar
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.SUMMON_A_PET);
					break;
				}
				default:
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.USE_S1);
					sm.addSkillName(skill);
					break;
				}
			}
			
			sendPacket(sm);
		}
		
		if (isPlayable())
		{
			if (!effectWhileCasting && (skill.getItemConsumeId() > 0))
			{
				if (!destroyItemByItemId("Consume", skill.getItemConsumeId(), skill.getItemConsume(), null, true))
				{
					getActingPlayer().sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
					abortCast();
					return;
				}
			}
			
			// reduce talisman mana on skill use
			if ((skill.getReferenceItemId() > 0) && (ItemData.getInstance().getTemplate(skill.getReferenceItemId()).getBodyPart() == L2Item.SLOT_DECO))
			{
				for (L2ItemInstance item : getInventory().getItemsByItemId(skill.getReferenceItemId()))
				{
					if (item.isEquipped())
					{
						item.decreaseMana(false, item.useSkillDisTime());
						break;
					}
				}
			}
		}
		
		// Before start AI Cast Broadcast Fly Effect is Need
		if (skill.getFlyType() != null)
		{
			ThreadPoolManager.getInstance().scheduleEffect(new FlyToLocationTask(this, target, skill), 50);
		}
		
		MagicUseTask mut = new MagicUseTask(this, targets, skill, skillTime, simultaneously);
		
		// launch the magic in skillTime milliseconds
		if (skillTime > 0)
		{
			// Send a Server->Client packet SetupGauge with the color of the gauge and the casting time
			if (isPlayer() && !effectWhileCasting)
			{
				sendPacket(new SetupGauge(SetupGauge.BLUE, skillTime));
			}
			
			if (skill.getHitCounts() > 0)
			{
				skillTime = (skillTime * skill.getHitTimings()[0]) / 100;
			}
			
			if (effectWhileCasting)
			{
				mut.setPhase(2);
			}
			
			if (simultaneously)
			{
				Future<?> future = _skillCast2;
				if (future != null)
				{
					future.cancel(true);
					_skillCast2 = null;
				}
				
				// Create a task MagicUseTask to launch the MagicSkill at the end of the casting time (skillTime)
				// For client animation reasons (party buffs especially) 400 ms before!
				_skillCast2 = ThreadPoolManager.getInstance().scheduleEffect(mut, skillTime - 400);
			}
			else
			{
				Future<?> future = _skillCast;
				if (future != null)
				{
					future.cancel(true);
					_skillCast = null;
				}
				
				// Create a task MagicUseTask to launch the MagicSkill at the end of the casting time (skillTime)
				// For client animation reasons (party buffs especially) 400 ms before!
				_skillCast = ThreadPoolManager.getInstance().scheduleEffect(mut, skillTime - 400);
			}
		}
		else
		{
			mut.setSkillTime(0);
			onMagicLaunchedTimer(mut);
		}
	}
	
	/**
	 * Check if casting of skill is possible
	 * @param skill
	 * @return True if casting is possible
	 */
	public boolean checkDoCastConditions(L2Skill skill)
	{
		if (CustomServerConfigs.CAN_USE_BLINK_RUSH_WHILE_ROOTED)
		{
			if ((skill == null) || isSkillDisabled(skill))
			{
				// Send a Server->Client packet ActionFailed to the L2PcInstance
				sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}
		}
		else
		{
			if ((skill == null) || isSkillDisabled(skill) || (((skill.getFlyRadius() > 0) || (skill.getFlyType() != null)) && isMovementDisabled()))
			{
				// Send a Server->Client packet ActionFailed to the L2PcInstance
				sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}
		}
		
		// Check if the caster has enough MP
		if (getCurrentMp() < (getStat().getMpConsume(skill) + getStat().getMpInitialConsume(skill)))
		{
			// Send a System Message to the caster
			sendPacket(SystemMessageId.NOT_ENOUGH_MP);
			
			// Send a Server->Client packet ActionFailed to the L2PcInstance
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		// Check if the caster has enough HP
		if (getCurrentHp() <= skill.getHpConsume())
		{
			// Send a System Message to the caster
			sendPacket(SystemMessageId.NOT_ENOUGH_HP);
			
			// Send a Server->Client packet ActionFailed to the L2PcInstance
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		// Skill mute checks.
		if (!skill.isStatic())
		{
			// Check if the skill is a magic spell and if the L2Character is not muted
			if (skill.isMagic())
			{
				if (isMuted())
				{
					// Send a Server->Client packet ActionFailed to the L2PcInstance
					sendPacket(ActionFailed.STATIC_PACKET);
					return false;
				}
			}
			else
			{
				// Check if the skill is physical and if the L2Character is not physical_muted
				if (isPhysicalMuted())
				{
					// Send a Server->Client packet ActionFailed to the L2PcInstance
					sendPacket(ActionFailed.STATIC_PACKET);
					return false;
				}
			}
		}
		
		// prevent casting signets to peace zone
		if ((skill.getSkillType() == L2SkillType.SIGNET) || (skill.getSkillType() == L2SkillType.SIGNET_CASTTIME))
		{
			L2WorldRegion region = getWorldRegion();
			if (region == null)
			{
				return false;
			}
			boolean canCast = true;
			if ((skill.getTargetType() == L2TargetType.GROUND) && isPlayer())
			{
				Location wp = getActingPlayer().getCurrentSkillWorldPosition();
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
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
				sm.addSkillName(skill);
				sendPacket(sm);
				return false;
			}
		}
		
		// Check if the caster's weapon is limited to use only its own skills
		if (getActiveWeaponItem() != null)
		{
			L2Weapon wep = getActiveWeaponItem();
			if (wep.useWeaponSkillsOnly() && !isGM() && wep.hasSkills())
			{
				boolean found = false;
				for (SkillHolder sh : wep.getSkills())
				{
					if (sh.getSkillId() == skill.getId())
					{
						found = true;
					}
				}
				
				if (!found)
				{
					if (getActingPlayer() != null)
					{
						sendPacket(SystemMessageId.WEAPON_CAN_USE_ONLY_WEAPON_SKILL);
					}
					return false;
				}
			}
		}
		
		// Check if the spell consumes an Item
		// TODO: combine check and consume
		if ((skill.getItemConsumeId() > 0) && (getInventory() != null))
		{
			// Get the L2ItemInstance consumed by the spell
			L2ItemInstance requiredItems = getInventory().getItemByItemId(skill.getItemConsumeId());
			
			// Check if the caster owns enough consumed Item to cast
			if ((requiredItems == null) || (requiredItems.getCount() < skill.getItemConsume()))
			{
				// Checked: when a summon skill failed, server show required consume item count
				if (skill.getSkillType() == L2SkillType.SUMMON)
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.SUMMONING_SERVITOR_COSTS_S2_S1);
					sm.addItemName(skill.getItemConsumeId());
					sm.addInt(skill.getItemConsume());
					sendPacket(sm);
				}
				else
				{
					// Send a System Message to the caster
					sendPacket(SystemMessageId.THERE_ARE_NOT_ENOUGH_NECESSARY_ITEMS_TO_USE_THE_SKILL);
				}
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Gets the item reuse time stamps map.
	 * @return the item reuse time stamps map
	 */
	public final Map<Integer, TimeStamp> getItemReuseTimeStamps()
	{
		return _reuseTimeStampsItems;
	}
	
	/**
	 * Adds a item reuse time stamp.
	 * @param item the item
	 * @param reuse the reuse
	 */
	public final void addTimeStampItem(L2ItemInstance item, long reuse)
	{
		addTimeStampItem(item, reuse, -1);
	}
	
	/**
	 * Adds a item reuse time stamp.<br>
	 * Used for restoring purposes.
	 * @param item the item
	 * @param reuse the reuse
	 * @param systime the system time
	 */
	public final void addTimeStampItem(L2ItemInstance item, long reuse, long systime)
	{
		if (_reuseTimeStampsItems == null)
		{
			synchronized (this)
			{
				if (_reuseTimeStampsItems == null)
				{
					_reuseTimeStampsItems = new ConcurrentHashMap<>();
				}
			}
		}
		_reuseTimeStampsItems.put(item.getObjectId(), new TimeStamp(item, reuse, systime));
	}
	
	/**
	 * Gets the item remaining reuse time for a given item object ID.
	 * @param itemObjId the item object ID
	 * @return if the item has a reuse time stamp, the remaining time, otherwise -1
	 */
	public synchronized final long getItemRemainingReuseTime(int itemObjId)
	{
		if ((_reuseTimeStampsItems == null) || !_reuseTimeStampsItems.containsKey(itemObjId))
		{
			return -1;
		}
		return _reuseTimeStampsItems.get(itemObjId).getRemaining();
	}
	
	/**
	 * Gets the item remaining reuse time for a given shared reuse item group.
	 * @param group the shared reuse item group
	 * @return if the shared reuse item group has a reuse time stamp, the remaining time, otherwise -1
	 */
	public final long getReuseDelayOnGroup(int group)
	{
		if (group > 0)
		{
			try
			{
				for (TimeStamp ts : _reuseTimeStampsItems.values())
				{
					if ((ts.getSharedReuseGroup() == group) && ts.hasNotPassed())
					{
						return ts.getRemaining();
					}
				}
			}
			catch (Exception e)
			{
				return -1;
			}
		}
		return -1;
	}
	
	/**
	 * Gets the skill reuse time stamps map.
	 * @return the skill reuse time stamps map
	 */
	public final Map<Integer, TimeStamp> getSkillReuseTimeStamps()
	{
		return _reuseTimeStampsSkills;
	}
	
	/**
	 * Adds the skill reuse time stamp.
	 * @param skill the skill
	 * @param reuse the delay
	 */
	public final void addTimeStamp(L2Skill skill, long reuse)
	{
		addTimeStamp(skill, reuse, -1);
	}
	
	/**
	 * Adds the skill reuse time stamp.<br>
	 * Used for restoring purposes.
	 * @param skill the skill
	 * @param reuse the reuse
	 * @param systime the system time
	 */
	public final void addTimeStamp(L2Skill skill, long reuse, long systime)
	{
		if (_reuseTimeStampsSkills == null)
		{
			synchronized (this)
			{
				if (_reuseTimeStampsSkills == null)
				{
					_reuseTimeStampsSkills = new ConcurrentHashMap<>();
				}
			}
		}
		_reuseTimeStampsSkills.put(skill.getReuseHashCode(), new TimeStamp(skill, reuse, systime));
	}
	
	/**
	 * Removes a skill reuse time stamp.
	 * @param skill the skill to remove
	 */
	public synchronized final void removeTimeStamp(L2Skill skill)
	{
		if (_reuseTimeStampsSkills != null)
		{
			_reuseTimeStampsSkills.remove(skill.getReuseHashCode());
		}
	}
	
	/**
	 * Removes all skill reuse time stamps.
	 */
	public synchronized final void resetTimeStamps()
	{
		if (_reuseTimeStampsSkills != null)
		{
			_reuseTimeStampsSkills.clear();
		}
	}
	
	/**
	 * Gets the skill remaining reuse time for a given skill hash code.
	 * @param hashCode the skill hash code
	 * @return if the skill has a reuse time stamp, the remaining time, otherwise -1
	 */
	public synchronized final long getSkillRemainingReuseTime(int hashCode)
	{
		if ((_reuseTimeStampsSkills == null) || !_reuseTimeStampsSkills.containsKey(hashCode))
		{
			return -1;
		}
		return _reuseTimeStampsSkills.get(hashCode).getRemaining();
	}
	
	/**
	 * Verifies if the skill is under reuse time.
	 * @param hashCode the skill hash code
	 * @return {@code true} if the skill is under reuse time, {@code false} otherwise
	 */
	public synchronized final boolean hasSkillReuse(int hashCode)
	{
		if ((_reuseTimeStampsSkills == null) || !_reuseTimeStampsSkills.containsKey(hashCode))
		{
			return false;
		}
		return _reuseTimeStampsSkills.get(hashCode).hasNotPassed();
	}
	
	/**
	 * Gets the skill reuse time stamp.
	 * @param hashCode the skill hash code
	 * @return if the skill has a reuse time stamp, the skill reuse time stamp, otherwise {@code null}
	 */
	public synchronized final TimeStamp getSkillReuseTimeStamp(int hashCode)
	{
		return _reuseTimeStampsSkills != null ? _reuseTimeStampsSkills.get(hashCode) : null;
	}
	
	public void startFusionSkill(L2Character target, L2Skill skill)
	{
		if (skill.getSkillType() != L2SkillType.FUSION)
		{
			return;
		}
		
		if (_fusionSkill == null)
		{
			_fusionSkill = new FusionSkill(this, target, skill);
		}
	}
	
	/**
	 * Kill the L2Character.<br>
	 * <B><U>Actions</U>:</B>
	 * <ul>
	 * <li>Set target to null and cancel Attack or Cast</li>
	 * <li>Stop movement</li>
	 * <li>Stop HP/MP/CP Regeneration task</li>
	 * <li>Stop all active skills effects in progress on the L2Character</li>
	 * <li>Send the Server->Client packet StatusUpdate with current HP and MP to all other L2PcInstance to inform</li>
	 * <li>Notify L2Character AI</li>
	 * </ul>
	 * @param killer The L2Character who killed it
	 * @return false if the player is already dead.
	 */
	public boolean doDie(L2Character killer)
	{
		// killing is only possible one time
		synchronized (this)
		{
			if (isDead())
			{
				return false;
			}
			// now reset currentHp to zero
			setCurrentHp(0);
			setIsDead(true);
		}
		if (!getEvents().onDeath(killer))
		{
			return false;
		}
		
		// Set target to null and cancel Attack or Cast
		setTarget(null);
		
		// Stop movement
		stopMove(null);
		
		// Stop HP/MP/CP Regeneration task
		getStatus().stopHpMpRegeneration();
		
		// Stop all active skills effects in progress on the L2Character,
		// if the Character isn't affected by Soul of The Phoenix or Salvation
		if (isPlayable() && ((L2Playable) this).isPhoenixBlessed())
		{
			if (((L2Playable) this).isCharmOfLuckAffected())
			{
				stopEffects(L2EffectType.CHARM_OF_LUCK);
			}
			if (((L2Playable) this).isNoblesseBlessed())
			{
				stopEffects(L2EffectType.NOBLESSE_BLESSING);
			}
		}
		// Same thing if the Character isn't a Noblesse Blessed L2Playable
		else if (isPlayable() && ((L2Playable) this).isNoblesseBlessed())
		{
			stopEffects(L2EffectType.NOBLESSE_BLESSING);
			
			if (((L2Playable) this).isCharmOfLuckAffected())
			{
				stopEffects(L2EffectType.CHARM_OF_LUCK);
			}
		}
		else
		{
			stopAllEffectsExceptThoseThatLastThroughDeath();
		}
		
		if (isPlayer() && (getActingPlayer().getAgathionId() != 0))
		{
			getActingPlayer().setAgathionId(0);
		}
		calculateRewards(killer);
		
		// Send the Server->Client packet StatusUpdate with current HP and MP to all other L2PcInstance to inform
		broadcastStatusUpdate();
		
		// Notify L2Character AI
		if (hasAI())
		{
			getAI().notifyEvent(CtrlEvent.EVT_DEAD);
		}
		
		if (getWorldRegion() != null)
		{
			getWorldRegion().onDeath(this);
		}
		
		getAttackByList().clear();
		// If character is PhoenixBlessed
		// or has charm of courage inside siege battlefield (exact operation to be confirmed)
		// a resurrection popup will show up
		if (isSummon())
		{
			if (((L2Summon) this).isPhoenixBlessed() && (((L2Summon) this).getOwner() != null))
			{
				((L2Summon) this).getOwner().reviveRequest(((L2Summon) this).getOwner(), null, true);
			}
		}
		if (isPlayer())
		{
			if (((L2Playable) this).isPhoenixBlessed())
			{
				getActingPlayer().reviveRequest(getActingPlayer(), null, false);
			}
			else if (isAffected(EffectFlag.CHARM_OF_COURAGE) && getActingPlayer().isInSiege())
			{
				getActingPlayer().reviveRequest(getActingPlayer(), null, false);
			}
		}
		try
		{
			if (_fusionSkill != null)
			{
				abortCast();
			}
			
			for (L2Character character : getKnownList().getKnownCharacters())
			{
				if ((character.getFusionSkill() != null) && (character.getFusionSkill().getTarget() == this))
				{
					character.abortCast();
				}
			}
		}
		catch (Exception e)
		{
			_log.error("deleteMe()", e);
		}
		return true;
	}
	
	public void deleteMe()
	{
		setDebug(null);
		
		if (hasAI())
		{
			getAI().stopAITask();
		}
	}
	
	protected void calculateRewards(L2Character killer)
	{
	}
	
	/** Sets HP, MP and CP and revives the L2Character. */
	public void doRevive()
	{
		if (!isDead())
		{
			return;
		}
		
		if (!isTeleporting())
		{
			setIsPendingRevive(false);
			setIsDead(false);
			boolean restorefull = false;
			
			if (isPlayable() && ((L2Playable) this).isPhoenixBlessed())
			{
				restorefull = true;
				stopEffects(L2EffectType.PHOENIX_BLESSING);
			}
			if (restorefull)
			{
				_status.setCurrentCp(getCurrentCp()); // this is not confirmed, so just trigger regeneration
				_status.setCurrentHp(getMaxHp()); // confirmed
				_status.setCurrentMp(getMaxMp()); // and also confirmed
			}
			else
			{
				if ((Config.RESPAWN_RESTORE_CP > 0) && (getCurrentCp() < (getMaxCp() * Config.RESPAWN_RESTORE_CP)))
				{
					_status.setCurrentCp(getMaxCp() * Config.RESPAWN_RESTORE_CP);
				}
				if ((Config.RESPAWN_RESTORE_HP > 0) && (getCurrentHp() < (getMaxHp() * Config.RESPAWN_RESTORE_HP)))
				{
					_status.setCurrentHp(getMaxHp() * Config.RESPAWN_RESTORE_HP);
				}
				if ((Config.RESPAWN_RESTORE_MP > 0) && (getCurrentMp() < (getMaxMp() * Config.RESPAWN_RESTORE_MP)))
				{
					_status.setCurrentMp(getMaxMp() * Config.RESPAWN_RESTORE_MP);
				}
			}
			
			// Start broadcast status
			broadcastPacket(new Revive(this));
			if (getWorldRegion() != null)
			{
				getWorldRegion().onRevive(this);
			}
		}
		else
		{
			setIsPendingRevive(true);
		}
	}
	
	/**
	 * Revives the L2Character using skill.
	 * @param revivePower
	 */
	public void doRevive(double revivePower)
	{
		doRevive();
	}
	
	/**
	 * Gets this creature's AI.
	 * @return the AI
	 */
	public final L2CharacterAI getAI()
	{
		if (_ai == null)
		{
			synchronized (this)
			{
				if (_ai == null)
				{
					_ai = initAI();
				}
			}
		}
		return _ai;
	}
	
	/**
	 * Initialize this creature's AI.<br>
	 * OOP approach to be overridden in child classes.
	 * @return the new AI
	 */
	protected L2CharacterAI initAI()
	{
		return new L2CharacterAI(new AIAccessor());
	}
	
	public void setAI(L2CharacterAI newAI)
	{
		L2CharacterAI oldAI = _ai;
		if ((oldAI != null) && (oldAI != newAI) && (oldAI instanceof L2AttackableAI))
		{
			((L2AttackableAI) oldAI).stopAITask();
		}
		_ai = newAI;
	}
	
	/**
	 * Verifies if this creature has an AI,
	 * @return {@code true} if this creature has an AI, {@code false} otherwise
	 */
	public boolean hasAI()
	{
		return _ai != null;
	}
	
	/**
	 * @return True if the L2Character is RaidBoss or his minion.
	 */
	public boolean isRaid()
	{
		return false;
	}
	
	/**
	 * @return True if the L2Character is minion.
	 */
	public boolean isMinion()
	{
		return false;
	}
	
	/**
	 * @return True if the L2Character is minion of RaidBoss.
	 */
	public boolean isRaidMinion()
	{
		return false;
	}
	
	/**
	 * @return a list of L2Character that attacked.
	 */
	public final Set<L2Character> getAttackByList()
	{
		if (_attackByList == null)
		{
			synchronized (this)
			{
				if (_attackByList == null)
				{
					_attackByList = new WeakFastSet<>(true);
				}
			}
		}
		return _attackByList;
	}
	
	public final L2Skill getLastSimultaneousSkillCast()
	{
		return _lastSimultaneousSkillCast;
	}
	
	public void setLastSimultaneousSkillCast(L2Skill skill)
	{
		_lastSimultaneousSkillCast = skill;
	}
	
	public final L2Skill getLastSkillCast()
	{
		return _lastSkillCast;
	}
	
	public void setLastSkillCast(L2Skill skill)
	{
		_lastSkillCast = skill;
	}
	
	public final boolean isNoRndWalk()
	{
		return _isNoRndWalk;
	}
	
	public final void setIsNoRndWalk(boolean value)
	{
		_isNoRndWalk = value;
	}
	
	public final boolean isAfraid()
	{
		return isAffected(EffectFlag.FEAR);
	}
	
	/**
	 * @return True if the L2Character can't use its skills (ex : stun, sleep...).
	 */
	public final boolean isAllSkillsDisabled()
	{
		return _allSkillsDisabled || isStunned() || isSleeping() || isParalyzed();
	}
	
	/**
	 * @return True if the L2Character can't attack (stun, sleep, attackEndTime, fakeDeath, paralyze, attackMute).
	 */
	public boolean isAttackingDisabled()
	{
		return isFlying() || isStunned() || isSleeping() || (_attackEndTime > GameTimeController.getInstance().getGameTicks()) || isAlikeDead() || isParalyzed() || isPhysicalAttackMuted() || isCoreAIDisabled();
	}
	
	public final Calculator[] getCalculators()
	{
		return _calculators;
	}
	
	public final boolean isConfused()
	{
		return isAffected(EffectFlag.CONFUSED);
	}
	
	/**
	 * @return True if the L2Character is dead or use fake death.
	 */
	public boolean isAlikeDead()
	{
		return _isDead;
	}
	
	/**
	 * @return True if the L2Character is dead.
	 */
	public final boolean isDead()
	{
		return _isDead;
	}
	
	public final void setIsDead(boolean value)
	{
		_isDead = value;
	}
	
	public boolean isImmobilized()
	{
		return _isImmobilized;
	}
	
	public void setIsImmobilized(boolean value)
	{
		_isImmobilized = value;
	}
	
	public final boolean isMuted()
	{
		return isAffected(EffectFlag.MUTED);
	}
	
	public final boolean isPhysicalMuted()
	{
		return isAffected(EffectFlag.PSYCHICAL_MUTED);
	}
	
	public final boolean isPhysicalAttackMuted()
	{
		return isAffected(EffectFlag.PSYCHICAL_ATTACK_MUTED);
	}
	
	/**
	 * @return True if the L2Character can't move (stun, root, sleep, overload, paralyzed).
	 */
	public boolean isMovementDisabled()
	{
		// check for isTeleporting to prevent teleport cheating (if appear packet not received)
		return isStunned() || isRooted() || isSleeping() || isOverloaded() || isParalyzed() || isImmobilized() || isAlikeDead() || isTeleporting();
	}
	
	/**
	 * @return True if the L2Character can not be controlled by the player (confused, afraid).
	 */
	public final boolean isOutOfControl()
	{
		return isConfused() || isAfraid();
	}
	
	public final boolean isOverloaded()
	{
		return _isOverloaded;
	}
	
	/**
	 * Set the overloaded status of the L2Character is overloaded (if True, the L2PcInstance can't take more item).
	 * @param value
	 */
	public final void setIsOverloaded(boolean value)
	{
		_isOverloaded = value;
	}
	
	public final boolean isParalyzed()
	{
		return _isParalyzed || isAffected(EffectFlag.PARALYZED);
	}
	
	public final void setIsParalyzed(boolean value)
	{
		_isParalyzed = value;
	}
	
	public final boolean isPendingRevive()
	{
		return isDead() && _isPendingRevive;
	}
	
	public final void setIsPendingRevive(boolean value)
	{
		_isPendingRevive = value;
	}
	
	public final boolean isDisarmed()
	{
		return isAffected(EffectFlag.DISARMED);
	}
	
	/**
	 * @return the summon
	 */
	public L2Summon getSummon()
	{
		return null;
	}
	
	/**
	 * @return {@code true} if the character has a summon, {@code false} otherwise
	 */
	public final boolean hasSummon()
	{
		return getSummon() != null;
	}
	
	/**
	 * @return {@code true} if the character has a pet, {@code false} otherwise
	 */
	public final boolean hasPet()
	{
		return hasSummon() && getSummon().isPet();
	}
	
	/**
	 * @return {@code true} if the character has a servitor, {@code false} otherwise
	 */
	public final boolean hasServitor()
	{
		return hasSummon() && getSummon().isServitor();
	}
	
	public final boolean isRooted()
	{
		return isAffected(EffectFlag.ROOTED);
	}
	
	/**
	 * @return True if the L2Character is running.
	 */
	public boolean isRunning()
	{
		return _isRunning;
	}
	
	public final void setIsRunning(boolean value)
	{
		_isRunning = value;
		if (getRunSpeed() != 0)
		{
			broadcastPacket(new ChangeMoveType(this));
		}
		if (isPlayer())
		{
			getActingPlayer().broadcastUserInfo();
		}
		else if (isSummon())
		{
			((L2Summon) this).broadcastStatusUpdate();
		}
		else if (isNpc())
		{
			Collection<L2PcInstance> plrs = getKnownList().getKnownPlayers().values();
			for (L2PcInstance player : plrs)
			{
				if ((player == null) || !isVisibleFor(player))
				{
					continue;
				}
				else if (getRunSpeed() == 0)
				{
					player.sendPacket(new ServerObjectInfo((L2Npc) this, player));
				}
				else
				{
					player.sendPacket(new AbstractNpcInfo.NpcInfo((L2Npc) this, player));
				}
			}
		}
	}
	
	/** Set the L2Character movement type to run and send Server->Client packet ChangeMoveType to all others L2PcInstance. */
	public final void setRunning()
	{
		if (!isRunning())
		{
			setIsRunning(true);
		}
	}
	
	public final boolean isSleeping()
	{
		return isAffected(EffectFlag.SLEEP);
	}
	
	public final boolean isStunned()
	{
		return isAffected(EffectFlag.STUNNED);
	}
	
	public final boolean isBetrayed()
	{
		return isAffected(EffectFlag.BETRAYED);
	}
	
	public final boolean isTeleporting()
	{
		return _isTeleporting;
	}
	
	public void setIsTeleporting(boolean value)
	{
		_isTeleporting = value;
	}
	
	public void setIsInvul(boolean b)
	{
		_isInvul = b;
	}
	
	public boolean isInvul()
	{
		return _isInvul || _isTeleporting || isAffected(EffectFlag.INVUL);
	}
	
	public void setIsMortal(boolean b)
	{
		_isMortal = b;
	}
	
	public boolean isMortal()
	{
		return _isMortal;
	}
	
	public boolean isUndead()
	{
		return false;
	}
	
	public boolean isResurrectionBlocked()
	{
		return isAffected(EffectFlag.BLOCK_RESURRECTION);
	}
	
	public final boolean isFlying()
	{
		return _isFlying;
	}
	
	public final void setIsFlying(boolean mode)
	{
		_isFlying = mode;
	}
	
	@Override
	public CharKnownList getKnownList()
	{
		return ((CharKnownList) super.getKnownList());
	}
	
	@Override
	public void initKnownList()
	{
		setKnownList(new CharKnownList(this));
	}
	
	public CharStat getStat()
	{
		return _stat;
	}
	
	/**
	 * Initializes the CharStat class of the L2Object, is overwritten in classes that require a different CharStat Type.<br>
	 * Removes the need for instanceof checks.
	 */
	public void initCharStat()
	{
		_stat = new CharStat(this);
	}
	
	public final void setStat(CharStat value)
	{
		_stat = value;
	}
	
	public CharStatus getStatus()
	{
		return _status;
	}
	
	/**
	 * Initializes the CharStatus class of the L2Object, is overwritten in classes that require a different CharStatus Type.<br>
	 * Removes the need for instanceof checks.
	 */
	public void initCharStatus()
	{
		_status = new CharStatus(this);
	}
	
	public final void setStatus(CharStatus value)
	{
		_status = value;
	}
	
	public void initCharEvents()
	{
		_events = new CharEvents(this);
	}
	
	public void setCharEvents(CharEvents events)
	{
		_events = events;
	}
	
	public CharEvents getEvents()
	{
		return _events;
	}
	
	public L2CharTemplate getTemplate()
	{
		return _template;
	}
	
	/**
	 * Set the template of the L2Character.<br>
	 * <B><U>Concept</U>:</B><br>
	 * Each L2Character owns generic and static properties (ex : all Keltir have the same number of HP...).<br>
	 * All of those properties are stored in a different template for each type of L2Character.<br>
	 * Each template is loaded once in the server cache memory (reduce memory use).<br>
	 * When a new instance of L2Character is spawned, server just create a link between the instance and the template This link is stored in <B>_template</B>.
	 * @param template
	 */
	protected final void setTemplate(L2CharTemplate template)
	{
		_template = template;
	}
	
	/**
	 * @return the Title of the L2Character.
	 */
	public final String getTitle()
	{
		return _title;
	}
	
	/**
	 * Set the Title of the L2Character.
	 * @param value
	 */
	public final void setTitle(String value)
	{
		if (value == null)
		{
			_title = "";
		}
		else
		{
			_title = value.length() > 21 ? value.substring(0, 20) : value;
		}
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
	
	// Abnormal Effect - NEED TO REMOVE ONCE L2CHARABNORMALEFFECT IS COMPLETE
	/** Map 32 bits (0x0000) containing all abnormal effect in progress */
	private int _AbnormalEffects;
	
	protected CharEffectList _effects = new CharEffectList(this);
	
	public final CharEffectList getEffectList()
	{
		return _effects;
	}
	
	private int _SpecialEffects;
	
	/**
	 * Launch and add L2Effect (including Stack Group management) to L2Character and update client magic icon.<br>
	 * <B><U>Concept</U>:</B><br>
	 * All active skills effects in progress on the L2Character are identified in ConcurrentHashMap(Integer,L2Effect) <B>_effects</B>.<br>
	 * The Integer key of _effects is the L2Skill Identifier that has created the L2Effect.<br>
	 * Several same effect can't be used on a L2Character at the same time.<br>
	 * Indeed, effects are not stackable and the last cast will replace the previous in progress.<br>
	 * More, some effects belong to the same Stack Group (ex WindWald and Haste Potion).<br>
	 * If 2 effects of a same group are used at the same time on a L2Character, only the more efficient (identified by its priority order) will be preserve.<br>
	 * <B><U>Actions</U>:</B>
	 * <ul>
	 * <li>Add the L2Effect to the L2Character _effects</li>
	 * <li>If this effect doesn't belong to a Stack Group, add its Funcs to the Calculator set of the L2Character (remove the old one if necessary)</li>
	 * <li>If this effect has higher priority in its Stack Group, add its Funcs to the Calculator set of the L2Character (remove previous stacked effect Funcs if necessary)</li>
	 * <li>If this effect has NOT higher priority in its Stack Group, set the effect to Not In Use</li>
	 * <li>Update active skills in progress icons on player client</li>
	 * </ul>
	 * @param newEffect
	 */
	public void addEffect(L2Effect newEffect)
	{
		_effects.queueEffect(newEffect, false);
	}
	
	/**
	 * Stop and remove L2Effect (including Stack Group management) from L2Character and update client magic icon.<br>
	 * <B><U>Concept</U>:</B><br>
	 * All active skills effects in progress on the L2Character are identified in ConcurrentHashMap(Integer,L2Effect) <B>_effects</B>.<br>
	 * The Integer key of _effects is the L2Skill Identifier that has created the L2Effect.<br>
	 * Several same effect can't be used on a L2Character at the same time.<br>
	 * Indeed, effects are not stackable and the last cast will replace the previous in progress.<br>
	 * More, some effects belong to the same Stack Group (ex Wind Walk and Haste Potion).<br>
	 * If 2 effects of a same group are used at the same time on a L2Character, only the more efficient (identified by its priority order) will be preserve.<br>
	 * <B><U>Actions</U>:</B>
	 * <ul>
	 * <li>Remove Func added by this effect from the L2Character Calculator (Stop L2Effect)</li>
	 * <li>If the L2Effect belongs to a not empty Stack Group, replace theses Funcs by next stacked effect Funcs</li>
	 * <li>Remove the L2Effect from _effects of the L2Character</li>
	 * <li>Update active skills in progress icons on player client</li>
	 * </ul>
	 * @param effect
	 */
	public final void removeEffect(L2Effect effect)
	{
		_effects.queueEffect(effect, true);
	}
	
	// Add NevitAdvent by pmq Start
	public final void startSpecialEffect(AbnormalEffect mask)
	{
		_SpecialEffects |= mask.getMask();
		updateAbnormalEffect();
	}
	
	public final void stopSpecialEffect(AbnormalEffect mask)
	{
		_SpecialEffects &= ~mask.getMask();
		updateAbnormalEffect();
	}
	
	// Add NevitAdvent by pmq End
	
	/**
	 * Active abnormal effects flags in the binary mask and send Server->Client UserInfo/CharInfo packet.
	 * @param mask
	 */
	public final void startAbnormalEffect(AbnormalEffect mask)
	{
		_AbnormalEffects |= mask.getMask();
		updateAbnormalEffect();
	}
	
	/**
	 * Active special effects flags in the binary mask and send Server->Client UserInfo/CharInfo packet.
	 * @param mask
	 */
	public final void startSpecialEffect(AbnormalEffect[] mask)
	{
		for (AbnormalEffect special : mask)
		{
			_SpecialEffects |= special.getMask();
		}
		updateAbnormalEffect();
	}
	
	public final void startAbnormalEffect(int mask)
	{
		_AbnormalEffects |= mask;
		updateAbnormalEffect();
	}
	
	public final void startSpecialEffect(int mask)
	{
		_SpecialEffects |= mask;
		updateAbnormalEffect();
	}
	
	/**
	 * Active the abnormal effect Confused flag, notify the L2Character AI and send Server->Client UserInfo/CharInfo packet.
	 */
	public final void startConfused()
	{
		getAI().notifyEvent(CtrlEvent.EVT_CONFUSED);
		updateAbnormalEffect();
	}
	
	/**
	 * Active the abnormal effect Fake Death flag, notify the L2Character AI and send Server->Client UserInfo/CharInfo packet.
	 */
	public final void startFakeDeath()
	{
		if (!isPlayer())
		{
			return;
		}
		
		getActingPlayer().setIsFakeDeath(true);
		// Aborts any attacks/casts if fake dead
		abortAttack();
		abortCast();
		stopMove(null);
		getAI().notifyEvent(CtrlEvent.EVT_FAKE_DEATH);
		broadcastPacket(new ChangeWaitType(this, ChangeWaitType.WT_START_FAKEDEATH));
	}
	
	/**
	 * Active the abnormal effect Fear flag, notify the L2Character AI and send Server->Client UserInfo/CharInfo packet.
	 */
	public final void startFear()
	{
		getAI().notifyEvent(CtrlEvent.EVT_AFRAID);
		updateAbnormalEffect();
	}
	
	/**
	 * Active the abnormal effect Muted flag, notify the L2Character AI and send Server->Client UserInfo/CharInfo packet.
	 */
	public final void startMuted()
	{
		/* Aborts any casts if muted */
		abortCast();
		getAI().notifyEvent(CtrlEvent.EVT_MUTED);
		updateAbnormalEffect();
	}
	
	/**
	 * Active the abnormal effect Psychical_Muted flag, notify the L2Character AI and send Server->Client UserInfo/CharInfo packet.
	 */
	public final void startPsychicalMuted()
	{
		getAI().notifyEvent(CtrlEvent.EVT_MUTED);
		updateAbnormalEffect();
	}
	
	/**
	 * Active the abnormal effect Root flag, notify the L2Character AI and send Server->Client UserInfo/CharInfo packet.
	 */
	public final void startRooted()
	{
		stopMove(null);
		getAI().notifyEvent(CtrlEvent.EVT_ROOTED);
		updateAbnormalEffect();
	}
	
	/**
	 * Active the abnormal effect Sleep flag, notify the L2Character AI and send Server->Client UserInfo/CharInfo packet.
	 */
	public final void startSleeping()
	{
		/* Aborts any attacks/casts if asleep */
		abortAttack();
		abortCast();
		stopMove(null);
		getAI().notifyEvent(CtrlEvent.EVT_SLEEPING);
		updateAbnormalEffect();
	}
	
	/**
	 * Launch a Stun Abnormal Effect on the L2Character.<br>
	 * <B><U>Actions</U>:</B>
	 * <ul>
	 * <li>Calculate the success rate of the Stun Abnormal Effect on this L2Character</li>
	 * <li>If Stun succeed, active the abnormal effect Stun flag, notify the L2Character AI and send Server->Client UserInfo/CharInfo packet</li>
	 * <li>If Stun NOT succeed, send a system message Failed to the L2PcInstance attacker</li>
	 * </ul>
	 */
	public final void startStunning()
	{
		// Aborts any attacks/casts if stunned
		abortAttack();
		abortCast();
		stopMove(null);
		getAI().notifyEvent(CtrlEvent.EVT_STUNNED);
		if (!isSummon())
		{
			getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		}
		updateAbnormalEffect();
	}
	
	public final void startParalyze()
	{
		// Aborts any attacks/casts if paralyzed
		abortAttack();
		abortCast();
		stopMove(null);
		getAI().notifyEvent(CtrlEvent.EVT_PARALYZED);
	}
	
	/**
	 * Modify the abnormal effect map according to the mask.
	 * @param mask
	 */
	public final void stopAbnormalEffect(AbnormalEffect mask)
	{
		_AbnormalEffects &= ~mask.getMask();
		updateAbnormalEffect();
	}
	
	/**
	 * Modify the special effect map according to the mask.
	 * @param mask
	 */
	public final void stopSpecialEffect(AbnormalEffect[] mask)
	{
		for (AbnormalEffect special : mask)
		{
			_SpecialEffects &= ~special.getMask();
		}
		updateAbnormalEffect();
	}
	
	public final void stopAbnormalEffect(int mask)
	{
		_AbnormalEffects &= ~mask;
		updateAbnormalEffect();
	}
	
	public final void stopSpecialEffect(int mask)
	{
		_SpecialEffects &= ~mask;
		updateAbnormalEffect();
	}
	
	/**
	 * Stop all active skills effects in progress on the L2Character.
	 */
	public void stopAllEffects()
	{
		_effects.stopAllEffects();
	}
	
	public void stopAllEffectsExceptThoseThatLastThroughDeath()
	{
		_effects.stopAllEffectsExceptThoseThatLastThroughDeath();
	}
	
	/**
	 * Stop and remove the L2Effects corresponding to the L2Skill Identifier and update client magic icon.<br>
	 * <B><U>Concept</U>:</B><br>
	 * All active skills effects in progress on the L2Character are identified in ConcurrentHashMap(Integer,L2Effect) <B>_effects</B>.<br>
	 * The Integer key of _effects is the L2Skill Identifier that has created the L2Effect.
	 * @param skillId the L2Skill Identifier of the L2Effect to remove from _effects
	 */
	public void stopSkillEffects(int skillId)
	{
		_effects.stopSkillEffects(skillId);
	}
	
	/**
	 * Stop and remove the L2Effects corresponding to the L2SkillType and update client magic icon.<br>
	 * <B><U>Concept</U>:</B><br>
	 * All active skills effects in progress on the L2Character are identified in ConcurrentHashMap(Integer,L2Effect) <B>_effects</B>.<br>
	 * The Integer key of _effects is the L2Skill Identifier that has created the L2Effect.
	 * @param skillType The L2SkillType of the L2Effect to remove from _effects
	 * @param negateLvl
	 */
	public final void stopSkillEffects(L2SkillType skillType, int negateLvl)
	{
		_effects.stopSkillEffects(skillType, negateLvl);
	}
	
	public final void stopSkillEffects(L2SkillType skillType)
	{
		_effects.stopSkillEffects(skillType, -1);
	}
	
	/**
	 * Stop and remove all L2Effect of the selected type (ex : BUFF, DMG_OVER_TIME...) from the L2Character and update client magic icon.<br>
	 * <B><U>Concept</U>:</B><br>
	 * All active skills effects in progress on the L2Character are identified in ConcurrentHashMap(Integer,L2Effect) <B>_effects</B>.<br>
	 * The Integer key of _effects is the L2Skill Identifier that has created the L2Effect.<br>
	 * <B><U>Actions</U>:</B>
	 * <ul>
	 * <li>Remove Func added by this effect from the L2Character Calculator (Stop L2Effect)</li>
	 * <li>Remove the L2Effect from _effects of the L2Character</li>
	 * <li>Update active skills in progress icons on player client</li>
	 * </ul>
	 * @param type The type of effect to stop ((ex : BUFF, DMG_OVER_TIME...)
	 */
	public final void stopEffects(L2EffectType type)
	{
		_effects.stopEffects(type);
	}
	
	/**
	 * Exits all buffs effects of the skills with "removedOnAnyAction" set.<br>
	 * Called on any action except movement (attack, cast).
	 */
	public final void stopEffectsOnAction()
	{
		_effects.stopEffectsOnAction();
	}
	
	/**
	 * Exits all buffs effects of the skills with "removedOnDamage" set.<br>
	 * Called on decreasing HP and mana burn.
	 * @param awake
	 */
	public final void stopEffectsOnDamage(boolean awake)
	{
		_effects.stopEffectsOnDamage(awake);
	}
	
	/**
	 * Stop a specified/all Fake Death abnormal L2Effect.<br>
	 * <B><U>Actions</U>:</B>
	 * <ul>
	 * <li>Delete a specified/all (if effect=null) Fake Death abnormal L2Effect from L2Character and update client magic icon</li>
	 * <li>Set the abnormal effect flag _fake_death to False</li>
	 * <li>Notify the L2Character AI</li>
	 * </ul>
	 * @param removeEffects
	 */
	public final void stopFakeDeath(boolean removeEffects)
	{
		if (removeEffects)
		{
			stopEffects(L2EffectType.FAKE_DEATH);
		}
		
		// if this is a player instance, start the grace period for this character (grace from mobs only)!
		if (isPlayer())
		{
			getActingPlayer().setIsFakeDeath(false);
			getActingPlayer().setRecentFakeDeath(true);
		}
		
		ChangeWaitType revive = new ChangeWaitType(this, ChangeWaitType.WT_STOP_FAKEDEATH);
		broadcastPacket(revive);
		// TODO: Temp hack: players see FD on ppl that are moving: Teleport to someone who uses FD - if he gets up he will fall down again for that client -
		// even tho he is actually standing... Probably bad info in CharInfo packet?
		broadcastPacket(new Revive(this));
	}
	
	/**
	 * Stop a specified/all Fear abnormal L2Effect.<br>
	 * <B><U>Actions</U>:</B>
	 * <ul>
	 * <li>Delete a specified/all (if effect=null) Fear abnormal L2Effect from L2Character and update client magic icon</li>
	 * <li>Set the abnormal effect flag _affraid to False</li>
	 * <li>Notify the L2Character AI</li>
	 * <li>Send Server->Client UserInfo/CharInfo packet</li>
	 * </ul>
	 * @param removeEffects
	 */
	public final void stopFear(boolean removeEffects)
	{
		if (removeEffects)
		{
			stopEffects(L2EffectType.FEAR);
		}
		updateAbnormalEffect();
	}
	
	/**
	 * Stop a specified/all Muted abnormal L2Effect.<br>
	 * <B><U>Actions</U>:</B>
	 * <ul>
	 * <li>Delete a specified/all (if effect=null) Muted abnormal L2Effect from L2Character and update client magic icon</li>
	 * <li>Set the abnormal effect flag _muted to False</li>
	 * <li>Notify the L2Character AI</li>
	 * <li>Send Server->Client UserInfo/CharInfo packet</li>
	 * </ul>
	 * @param removeEffects
	 */
	public final void stopMuted(boolean removeEffects)
	{
		if (removeEffects)
		{
			stopEffects(L2EffectType.MUTE);
		}
		
		updateAbnormalEffect();
	}
	
	public final void stopPsychicalMuted(boolean removeEffects)
	{
		if (removeEffects)
		{
			stopEffects(L2EffectType.PHYSICAL_MUTE);
		}
		
		updateAbnormalEffect();
	}
	
	/**
	 * Stop a specified/all Root abnormal L2Effect.<br>
	 * <B><U>Actions</U>:</B>
	 * <ul>
	 * <li>Delete a specified/all (if effect=null) Root abnormal L2Effect from L2Character and update client magic icon</li>
	 * <li>Set the abnormal effect flag _rooted to False</li>
	 * <li>Notify the L2Character AI</li>
	 * <li>Send Server->Client UserInfo/CharInfo packet</li>
	 * </ul>
	 * @param removeEffects
	 */
	public final void stopRooting(boolean removeEffects)
	{
		if (removeEffects)
		{
			stopEffects(L2EffectType.ROOT);
		}
		
		if (!isPlayer())
		{
			getAI().notifyEvent(CtrlEvent.EVT_THINK);
		}
		updateAbnormalEffect();
	}
	
	/**
	 * Stop a specified/all Sleep abnormal L2Effect.<br>
	 * <B><U>Actions</U>:</B>
	 * <ul>
	 * <li>Delete a specified/all (if effect=null) Sleep abnormal L2Effect from L2Character and update client magic icon</li>
	 * <li>Set the abnormal effect flag _sleeping to False</li>
	 * <li>Notify the L2Character AI</li>
	 * <li>Send Server->Client UserInfo/CharInfo packet</li>
	 * </ul>
	 * @param removeEffects
	 */
	public final void stopSleeping(boolean removeEffects)
	{
		if (removeEffects)
		{
			stopEffects(L2EffectType.SLEEP);
		}
		
		if (!isPlayer())
		{
			getAI().notifyEvent(CtrlEvent.EVT_THINK);
		}
		updateAbnormalEffect();
	}
	
	/**
	 * Stop a specified/all Stun abnormal L2Effect.<br>
	 * <B><U>Actions</U>:</B>
	 * <ul>
	 * <li>Delete a specified/all (if effect=null) Stun abnormal L2Effect from L2Character and update client magic icon</li>
	 * <li>Set the abnormal effect flag _stuned to False</li>
	 * <li>Notify the L2Character AI</li>
	 * <li>Send Server->Client UserInfo/CharInfo packet</li>
	 * </ul>
	 * @param removeEffects
	 */
	public final void stopStunning(boolean removeEffects)
	{
		if (removeEffects)
		{
			stopEffects(L2EffectType.STUN);
		}
		
		if (!isPlayer())
		{
			getAI().notifyEvent(CtrlEvent.EVT_THINK);
		}
		updateAbnormalEffect();
	}
	
	/**
	 * Stop L2Effect: Transformation.<br>
	 * <B><U>Actions</U>:</B>
	 * <ul>
	 * <li>Remove Transformation Effect</li>
	 * <li>Notify the L2Character AI</li>
	 * <li>Send Server->Client UserInfo/CharInfo packet</li>
	 * </ul>
	 * @param removeEffects
	 */
	public final void stopTransformation(boolean removeEffects)
	{
		if (removeEffects)
		{
			stopEffects(L2EffectType.TRANSFORMATION);
		}
		
		// if this is a player instance, then untransform, also set the transform_id column equal to 0 if not cursed.
		if (isPlayer())
		{
			if (getActingPlayer().getTransformation() != null)
			{
				getActingPlayer().untransform();
			}
		}
		
		if (!isPlayer())
		{
			getAI().notifyEvent(CtrlEvent.EVT_THINK);
		}
		updateAbnormalEffect();
	}
	
	public abstract void updateAbnormalEffect();
	
	/**
	 * Update active skills in progress (In Use and Not In Use because stacked) icons on client.<br>
	 * <B><U>Concept</U>:</B><br>
	 * All active skills effects in progress (In Use and Not In Use because stacked) are represented by an icon on the client.<br>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method ONLY UPDATE the client of the player and not clients of all players in the party.</B></FONT>
	 */
	public final void updateEffectIcons()
	{
		updateEffectIcons(false);
	}
	
	/**
	 * Updates Effect Icons for this character(player/summon) and his party if any.
	 * @param partyOnly
	 */
	public void updateEffectIcons(boolean partyOnly)
	{
		// overridden
	}
	
	/**
	 * <B><U>Concept</U>:</B><br>
	 * In Server->Client packet, each effect is represented by 1 bit of the map (ex : BLEEDING = 0x0001 (bit 1), SLEEP = 0x0080 (bit 8)...). The map is calculated by applying a BINARY OR operation on each effect.<br>
	 * <B><U>Example of use </U>:</B>
	 * <ul>
	 * <li>Server Packet : CharInfo, NpcInfo, NpcInfoPoly, UserInfo...</li>
	 * </ul>
	 * @return a map of 16 bits (0x0000) containing all abnormal effect in progress for this L2Character.
	 */
	public int getAbnormalEffect()
	{
		int ae = _AbnormalEffects;
		if (!isFlying() && isStunned())
		{
			ae |= AbnormalEffect.STUN.getMask();
		}
		if (!isFlying() && isRooted())
		{
			ae |= AbnormalEffect.ROOT.getMask();
		}
		if (isSleeping())
		{
			ae |= AbnormalEffect.SLEEP.getMask();
		}
		if (isConfused())
		{
			ae |= AbnormalEffect.FEAR.getMask();
		}
		if (isMuted())
		{
			ae |= AbnormalEffect.MUTED.getMask();
		}
		if (isPhysicalMuted())
		{
			ae |= AbnormalEffect.MUTED.getMask();
		}
		if (isAfraid())
		{
			ae |= AbnormalEffect.SKULL_FEAR.getMask();
		}
		return ae;
	}
	
	/**
	 * <B><U>Concept</U>:</B><br>
	 * In Server->Client packet, each effect is represented by 1 bit of the map (ex : INVULNERABLE = 0x0001 (bit 1), PINK_AFFRO = 0x0020 (bit 6)...). The map is calculated by applying a BINARY OR operation on each effect.<br>
	 * <B><U>Example of use </U>:</B>
	 * <ul>
	 * <li>Server Packet : CharInfo, UserInfo...</li>
	 * </ul>
	 * @return a map of 32 bits (0x00000000) containing all special effect in progress for this L2Character.
	 */
	public int getSpecialEffect()
	{
		int se = _SpecialEffects;
		if (isFlying() && isStunned())
		{
			se |= AbnormalEffect.S_AIR_STUN.getMask();
		}
		if (isFlying() && isRooted())
		{
			se |= AbnormalEffect.S_AIR_ROOT.getMask();
		}
		return se;
	}
	
	/**
	 * Return all active skills effects in progress on the L2Character.<br>
	 * <B><U>Concept</U>:</B><br>
	 * All active skills effects in progress on the L2Character are identified in <B>_effects</B>.<br>
	 * The Integer key of _effects is the L2Skill Identifier that has created the effect.
	 * @return A table containing all active skills effect in progress on the L2Character
	 */
	public final L2Effect[] getAllEffects()
	{
		return _effects.getAllEffects();
	}
	
	/**
	 * Return L2Effect in progress on the L2Character corresponding to the L2Skill Identifier.<br>
	 * <B><U>Concept</U>:</B><br>
	 * All active skills effects in progress on the L2Character are identified in <B>_effects</B>.
	 * @param skillId The L2Skill Identifier of the L2Effect to return from the _effects
	 * @return The L2Effect corresponding to the L2Skill Identifier
	 */
	public final L2Effect getFirstEffect(int skillId)
	{
		return _effects.getFirstEffect(skillId);
	}
	
	/**
	 * Return the first L2Effect in progress on the L2Character created by the L2Skill.<br>
	 * <B><U>Concept</U>:</B><br>
	 * All active skills effects in progress on the L2Character are identified in <B>_effects</B>.
	 * @param skill The L2Skill whose effect must be returned
	 * @return The first L2Effect created by the L2Skill
	 */
	public final L2Effect getFirstEffect(L2Skill skill)
	{
		return _effects.getFirstEffect(skill);
	}
	
	/**
	 * Return the first L2Effect in progress on the L2Character corresponding to the Effect Type (ex : BUFF, STUN, ROOT...).<br>
	 * <B><U>Concept</U>:</B><br>
	 * All active skills effects in progress on the L2Character are identified in <B>_effects</B>.
	 * @param tp The Effect Type of skills whose effect must be returned
	 * @return The first L2Effect corresponding to the Effect Type
	 */
	public final L2Effect getFirstEffect(L2EffectType tp)
	{
		return _effects.getFirstEffect(tp);
	}
	
	public final L2Effect getFirstPassiveEffect(L2EffectType type)
	{
		return _effects.getFirstPassiveEffect(type);
	}
	
	// TODO: NEED TO ORGANIZE AND MOVE TO PROPER PLACE
	/** This class permit to the L2Character AI to obtain informations and uses L2Character method */
	public class AIAccessor
	{
		/**
		 * @return the L2Character managed by this Accessor AI.
		 */
		public L2Character getActor()
		{
			return L2Character.this;
		}
		
		/**
		 * Accessor to L2Character moveToLocation() method with an interaction area.
		 * @param x
		 * @param y
		 * @param z
		 * @param offset
		 */
		public void moveTo(int x, int y, int z, int offset)
		{
			moveToLocation(x, y, z, offset);
		}
		
		/**
		 * Accessor to L2Character moveToLocation() method without interaction area.
		 * @param x
		 * @param y
		 * @param z
		 */
		public void moveTo(int x, int y, int z)
		{
			moveToLocation(x, y, z, 0);
		}
		
		/**
		 * Accessor to L2Character stopMove() method.
		 * @param pos
		 */
		public void stopMove(Location pos)
		{
			L2Character.this.stopMove(pos);
		}
		
		/**
		 * Accessor to L2Character doAttack() method.
		 * @param target
		 */
		public void doAttack(L2Character target)
		{
			L2Character.this.doAttack(target);
		}
		
		/**
		 * Accessor to L2Character doCast() method.
		 * @param skill
		 */
		public void doCast(L2Skill skill)
		{
			L2Character.this.doCast(skill);
		}
		
		/**
		 * Create a NotifyAITask.
		 * @param evt
		 * @return
		 */
		public NotifyAITask newNotifyTask(CtrlEvent evt)
		{
			return new NotifyAITask(getActor(), evt);
		}
		
		/**
		 * Cancel the AI.
		 */
		public void detachAI()
		{
			// Skip character, if it is controlled by Walking Manager
			if (isWalker())
			{
				return;
			}
			
			setAI(null);
		}
	}
	
	/**
	 * This class group all movement data.<br>
	 * <B><U> Data</U> :</B>
	 * <ul>
	 * <li>_moveTimestamp : Last time position update</li>
	 * <li>_xDestination, _yDestination, _zDestination : Position of the destination</li>
	 * <li>_xMoveFrom, _yMoveFrom, _zMoveFrom : Position of the origin</li>
	 * <li>_moveStartTime : Start time of the movement</li>
	 * <li>_ticksToMove : Nb of ticks between the start and the destination</li>
	 * <li>_xSpeedTicks, _ySpeedTicks : Speed in unit/ticks</li>
	 * </ul>
	 */
	public static class MoveData
	{
		// when we retrieve x/y/z we use GameTimeControl.getGameTicks()
		// if we are moving, but move timestamp==gameticks, we don't need
		// to recalculate position
		public int _moveStartTime;
		public int _moveTimestamp; // last update
		public int _xDestination;
		public int _yDestination;
		public int _zDestination;
		public double _xAccurate; // otherwise there would be rounding errors
		public double _yAccurate;
		public double _zAccurate;
		public int _heading;
		
		public boolean disregardingGeodata;
		public int onGeodataPathIndex;
		public List<AbstractNodeLoc> geoPath;
		public int geoPathAccurateTx;
		public int geoPathAccurateTy;
		public int geoPathGtx;
		public int geoPathGty;
	}
	
	/**
	 * Add a Func to the Calculator set of the L2Character. <B><U> Concept</U> :</B> A L2Character owns a table of Calculators called <B>_calculators</B>. Each Calculator (a calculator per state) own a table of Func object. A Func object is a mathematic function that permit to calculate the modifier
	 * of a state (ex : REGENERATE_HP_RATE...). To reduce cache memory use, L2NPCInstances who don't have skills share the same Calculator set called <B>NPC_STD_CALCULATOR</B>. That's why, if a L2NPCInstance is under a skill/spell effect that modify one of its state, a copy of the NPC_STD_CALCULATOR
	 * must be create in its _calculators before addind new Func object. <B><U> Actions</U> :</B> <li>If _calculators is linked to NPC_STD_CALCULATOR, create a copy of NPC_STD_CALCULATOR in _calculators</li> <li>Add the Func object to _calculators</li>
	 * @param f The Func object to add to the Calculator corresponding to the state affected
	 */
	public final void addStatFunc(Func f)
	{
		if (f == null)
		{
			return;
		}
		
		synchronized (this)
		{
			// Check if Calculator set is linked to the standard Calculator set of NPC
			if (_calculators == NPC_STD_CALCULATOR)
			{
				// Create a copy of the standard NPC Calculator set
				_calculators = new Calculator[Stats.NUM_STATS];
				
				for (int i = 0; i < Stats.NUM_STATS; i++)
				{
					if (NPC_STD_CALCULATOR[i] != null)
					{
						_calculators[i] = new Calculator(NPC_STD_CALCULATOR[i]);
					}
				}
			}
			
			// Select the Calculator of the affected state in the Calculator set
			int stat = f.stat.ordinal();
			
			if (_calculators[stat] == null)
			{
				_calculators[stat] = new Calculator();
			}
			
			// Add the Func to the calculator corresponding to the state
			_calculators[stat].addFunc(f);
		}
	}
	
	/**
	 * Add a list of Funcs to the Calculator set of the L2Character.<br>
	 * <B><U>Concept</U>:</B><br>
	 * A L2Character owns a table of Calculators called <B>_calculators</B>.<br>
	 * Each Calculator (a calculator per state) own a table of Func object.<br>
	 * A Func object is a mathematic function that permit to calculate the modifier of a state (ex : REGENERATE_HP_RATE...).<br>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method is ONLY for L2PcInstance</B></FONT><br>
	 * <B><U>Example of use</U>:</B>
	 * <ul>
	 * <li>Equip an item from inventory</li>
	 * <li>Learn a new passive skill</li>
	 * <li>Use an active skill</li>
	 * </ul>
	 * @param funcs The list of Func objects to add to the Calculator corresponding to the state affected
	 */
	public final void addStatFuncs(Func[] funcs)
	{
		if (!isPlayer() && getKnownList().getKnownPlayers().isEmpty())
		{
			for (Func f : funcs)
			{
				addStatFunc(f);
			}
		}
		else
		{
			final List<Stats> modifiedStats = new ArrayList<>();
			for (Func f : funcs)
			{
				modifiedStats.add(f.stat);
				addStatFunc(f);
			}
			broadcastModifiedStats(modifiedStats);
		}
	}
	
	/**
	 * Remove a Func from the Calculator set of the L2Character.<br>
	 * <B><U>Concept</U>:</B><br>
	 * A L2Character owns a table of Calculators called <B>_calculators</B>.<br>
	 * Each Calculator (a calculator per state) own a table of Func object.<br>
	 * A Func object is a mathematic function that permit to calculate the modifier of a state (ex : REGENERATE_HP_RATE...).<br>
	 * To reduce cache memory use, L2NPCInstances who don't have skills share the same Calculator set called <B>NPC_STD_CALCULATOR</B>.<br>
	 * That's why, if a L2NPCInstance is under a skill/spell effect that modify one of its state, a copy of the NPC_STD_CALCULATOR must be create in its _calculators before addind new Func object.<br>
	 * <B><U>Actions</U>:</B>
	 * <ul>
	 * <li>Remove the Func object from _calculators</li>
	 * <li>If L2Character is a L2NPCInstance and _calculators is equal to NPC_STD_CALCULATOR, free cache memory and just create a link on NPC_STD_CALCULATOR in _calculators</li>
	 * </ul>
	 * @param f The Func object to remove from the Calculator corresponding to the state affected
	 */
	public final void removeStatFunc(Func f)
	{
		if (f == null)
		{
			return;
		}
		
		// Select the Calculator of the affected state in the Calculator set
		int stat = f.stat.ordinal();
		
		synchronized (this)
		{
			if (_calculators[stat] == null)
			{
				return;
			}
			
			// Remove the Func object from the Calculator
			_calculators[stat].removeFunc(f);
			
			if (_calculators[stat].size() == 0)
			{
				_calculators[stat] = null;
			}
			
			// If possible, free the memory and just create a link on NPC_STD_CALCULATOR
			if (isNpc())
			{
				int i = 0;
				for (; i < Stats.NUM_STATS; i++)
				{
					if (!Calculator.equalsCals(_calculators[i], NPC_STD_CALCULATOR[i]))
					{
						break;
					}
				}
				
				if (i >= Stats.NUM_STATS)
				{
					_calculators = NPC_STD_CALCULATOR;
				}
			}
		}
	}
	
	/**
	 * Remove a list of Funcs from the Calculator set of the L2PcInstance.<br>
	 * <B><U>Concept</U>:</B><br>
	 * A L2Character owns a table of Calculators called <B>_calculators</B>.<br>
	 * Each Calculator (a calculator per state) own a table of Func object.<br>
	 * A Func object is a mathematic function that permit to calculate the modifier of a state (ex : REGENERATE_HP_RATE...).<br>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method is ONLY for L2PcInstance</B></FONT><br>
	 * <B><U>Example of use</U>:</B>
	 * <ul>
	 * <li>Unequip an item from inventory</li>
	 * <li>Stop an active skill</li>
	 * </ul>
	 * @param funcs The list of Func objects to add to the Calculator corresponding to the state affected
	 */
	public final void removeStatFuncs(Func[] funcs)
	{
		if (!isPlayer() && getKnownList().getKnownPlayers().isEmpty())
		{
			for (Func f : funcs)
			{
				removeStatFunc(f);
			}
		}
		else
		{
			final List<Stats> modifiedStats = new ArrayList<>();
			for (Func f : funcs)
			{
				modifiedStats.add(f.stat);
				removeStatFunc(f);
			}
			
			broadcastModifiedStats(modifiedStats);
		}
	}
	
	/**
	 * Remove all Func objects with the selected owner from the Calculator set of the L2Character.<br>
	 * <B><U>Concept</U>:</B><br>
	 * A L2Character owns a table of Calculators called <B>_calculators</B>.<br>
	 * Each Calculator (a calculator per state) own a table of Func object.<br>
	 * A Func object is a mathematic function that permit to calculate the modifier of a state (ex : REGENERATE_HP_RATE...).<br>
	 * To reduce cache memory use, L2NPCInstances who don't have skills share the same Calculator set called <B>NPC_STD_CALCULATOR</B>.<br>
	 * That's why, if a L2NPCInstance is under a skill/spell effect that modify one of its state, a copy of the NPC_STD_CALCULATOR must be create in its _calculators before addind new Func object.<br>
	 * <B><U>Actions</U>:</B>
	 * <ul>
	 * <li>Remove all Func objects of the selected owner from _calculators</li>
	 * <li>If L2Character is a L2NPCInstance and _calculators is equal to NPC_STD_CALCULATOR, free cache memory and just create a link on NPC_STD_CALCULATOR in _calculators</li>
	 * </ul>
	 * <B><U>Example of use</U>:</B>
	 * <ul>
	 * <li>Unequip an item from inventory</li>
	 * <li>Stop an active skill</li>
	 * </ul>
	 * @param owner The Object(Skill, Item...) that has created the effect
	 */
	public final void removeStatsOwner(Object owner)
	{
		List<Stats> modifiedStats = null;
		int i = 0;
		// Go through the Calculator set
		synchronized (this)
		{
			for (Calculator calc : _calculators)
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
						_calculators[i] = null;
					}
				}
				i++;
			}
			
			// If possible, free the memory and just create a link on NPC_STD_CALCULATOR
			if (isNpc())
			{
				i = 0;
				for (; i < Stats.NUM_STATS; i++)
				{
					if (!Calculator.equalsCals(_calculators[i], NPC_STD_CALCULATOR[i]))
					{
						break;
					}
				}
				
				if (i >= Stats.NUM_STATS)
				{
					_calculators = NPC_STD_CALCULATOR;
				}
			}
			
			if (owner instanceof L2Effect)
			{
				if (!((L2Effect) owner).preventExitUpdate)
				{
					broadcastModifiedStats(modifiedStats);
				}
			}
			else
			{
				broadcastModifiedStats(modifiedStats);
			}
		}
	}
	
	protected void broadcastModifiedStats(List<Stats> stats)
	{
		if ((stats == null) || stats.isEmpty())
		{
			return;
		}
		
		if (isSummon())
		{
			L2Summon summon = (L2Summon) this;
			if (summon.getOwner() != null)
			{
				summon.updateAndBroadcastStatus(1);
			}
		}
		else
		{
			boolean broadcastFull = false;
			StatusUpdate su = new StatusUpdate(this);
			
			for (Stats stat : stats)
			{
				if (stat == Stats.POWER_ATTACK_SPEED)
				{
					su.addAttribute(StatusUpdate.ATK_SPD, getPAtkSpd());
				}
				else if (stat == Stats.MAGIC_ATTACK_SPEED)
				{
					su.addAttribute(StatusUpdate.CAST_SPD, getMAtkSpd());
				}
				else if (stat == Stats.MOVE_SPEED)
				{
					broadcastFull = true;
				}
			}
			
			if (isPlayer())
			{
				if (broadcastFull)
				{
					getActingPlayer().updateAndBroadcastStatus(2);
				}
				else
				{
					getActingPlayer().updateAndBroadcastStatus(1);
					if (su.hasAttributes())
					{
						broadcastPacket(su);
					}
				}
				if ((getSummon() != null) && isAffected(EffectFlag.SERVITOR_SHARE))
				{
					getSummon().broadcastStatusUpdate();
				}
			}
			else if (isNpc())
			{
				if (broadcastFull)
				{
					Collection<L2PcInstance> plrs = getKnownList().getKnownPlayers().values();
					for (L2PcInstance player : plrs)
					{
						if ((player == null) || !isVisibleFor(player))
						{
							continue;
						}
						if (getRunSpeed() == 0)
						{
							player.sendPacket(new ServerObjectInfo((L2Npc) this, player));
						}
						else
						{
							player.sendPacket(new AbstractNpcInfo.NpcInfo((L2Npc) this, player));
						}
					}
				}
				else if (su.hasAttributes())
				{
					broadcastPacket(su);
				}
			}
			else if (su.hasAttributes())
			{
				broadcastPacket(su);
			}
		}
	}
	
	public final int getXdestination()
	{
		MoveData m = _move;
		
		if (m != null)
		{
			return m._xDestination;
		}
		
		return getX();
	}
	
	/**
	 * @return the Y destination of the L2Character or the Y position if not in movement.
	 */
	public final int getYdestination()
	{
		MoveData m = _move;
		
		if (m != null)
		{
			return m._yDestination;
		}
		
		return getY();
	}
	
	/**
	 * @return the Z destination of the L2Character or the Z position if not in movement.
	 */
	public final int getZdestination()
	{
		MoveData m = _move;
		
		if (m != null)
		{
			return m._zDestination;
		}
		
		return getZ();
	}
	
	/**
	 * @return True if the L2Character is in combat.
	 */
	public boolean isInCombat()
	{
		return hasAI() && ((getAI().getAttackTarget() != null) || getAI().isAutoAttacking());
	}
	
	/**
	 * @return True if the L2Character is moving.
	 */
	public final boolean isMoving()
	{
		return _move != null;
	}
	
	/**
	 * @return True if the L2Character is travelling a calculated path.
	 */
	public final boolean isOnGeodataPath()
	{
		MoveData m = _move;
		if (m == null)
		{
			return false;
		}
		if (m.onGeodataPathIndex == -1)
		{
			return false;
		}
		if (m.onGeodataPathIndex == (m.geoPath.size() - 1))
		{
			return false;
		}
		return true;
	}
	
	/**
	 * @return True if the L2Character is casting.
	 */
	public final boolean isCastingNow()
	{
		return _isCastingNow;
	}
	
	public void setIsCastingNow(boolean value)
	{
		_isCastingNow = value;
	}
	
	public final boolean isCastingSimultaneouslyNow()
	{
		return _isCastingSimultaneouslyNow;
	}
	
	public void setIsCastingSimultaneouslyNow(boolean value)
	{
		_isCastingSimultaneouslyNow = value;
	}
	
	/**
	 * @return True if the cast of the L2Character can be aborted.
	 */
	public final boolean canAbortCast()
	{
		return _castInterruptTime > GameTimeController.getInstance().getGameTicks();
	}
	
	public int getCastInterruptTime()
	{
		return _castInterruptTime;
	}
	
	/**
	 * @return True if the L2Character is attacking.
	 */
	public boolean isAttackingNow()
	{
		return _attackEndTime > GameTimeController.getInstance().getGameTicks();
	}
	
	/**
	 * @return True if the L2Character has aborted its attack.
	 */
	public final boolean isAttackAborted()
	{
		return _attacking <= 0;
	}
	
	/**
	 * Abort the attack of the L2Character and send Server->Client ActionFailed packet.
	 */
	public final void abortAttack()
	{
		if (isAttackingNow())
		{
			_attacking = 0;
			sendPacket(ActionFailed.STATIC_PACKET);
		}
	}
	
	/**
	 * @return body part (paperdoll slot) we are targeting right now
	 */
	public final int getAttackingBodyPart()
	{
		return _attacking;
	}
	
	/**
	 * Abort the cast of the L2Character and send Server->Client MagicSkillCanceld/ActionFailed packet.
	 */
	public final void abortCast()
	{
		if (isCastingNow() || isCastingSimultaneouslyNow())
		{
			Future<?> future = _skillCast;
			// cancels the skill hit scheduled task
			if (future != null)
			{
				future.cancel(true);
				_skillCast = null;
			}
			future = _skillCast2;
			if (future != null)
			{
				future.cancel(true);
				_skillCast2 = null;
			}
			
			if (getFusionSkill() != null)
			{
				getFusionSkill().onCastAbort();
			}
			
			L2Effect mog = getFirstEffect(L2EffectType.SIGNET_GROUND);
			if (mog != null)
			{
				mog.exit();
			}
			
			if (_allSkillsDisabled)
			{
				enableAllSkills(); // this remains for forced skill use, e.g. scroll of escape
			}
			setIsCastingNow(false);
			setIsCastingSimultaneouslyNow(false);
			// safeguard for cannot be interrupt any more
			_castInterruptTime = 0;
			if (isPlayer())
			{
				getAI().notifyEvent(CtrlEvent.EVT_FINISH_CASTING); // setting back previous intention
			}
			broadcastPacket(new MagicSkillCanceld(getObjectId())); // broadcast packet to stop animations client-side
			sendPacket(ActionFailed.STATIC_PACKET); // send an "action failed" packet to the caster
		}
	}
	
	/**
	 * Update the position of the L2Character during a movement and return True if the movement is finished.<br>
	 * <B><U>Concept</U>:</B><br>
	 * At the beginning of the move action, all properties of the movement are stored in the MoveData object called <B>_move</B> of the L2Character.<br>
	 * The position of the start point and of the destination permit to estimated in function of the movement speed the time to achieve the destination.<br>
	 * When the movement is started (ex : by MovetoLocation), this method will be called each 0.1 sec to estimate and update the L2Character position on the server.<br>
	 * Note, that the current server position can differe from the current client position even if each movement is straight foward.<br>
	 * That's why, client send regularly a Client->Server ValidatePosition packet to eventually correct the gap on the server.<br>
	 * But, it's always the server position that is used in range calculation. At the end of the estimated movement time,<br>
	 * the L2Character position is automatically set to the destination position even if the movement is not finished.<br>
	 * <FONT COLOR=#FF0000><B><U>Caution</U>: The current Z position is obtained FROM THE CLIENT by the Client->Server ValidatePosition Packet.<br>
	 * But x and y positions must be calculated to avoid that players try to modify their movement speed.</B></FONT>
	 * @param gameTicks Nb of ticks since the server start
	 * @return True if the movement is finished
	 */
	public boolean updatePosition(int gameTicks)
	{
		// Get movement data
		MoveData m = _move;
		
		if (m == null)
		{
			return true;
		}
		
		if (!isVisible())
		{
			_move = null;
			return true;
		}
		
		// Check if this is the first update
		if (m._moveTimestamp == 0)
		{
			m._moveTimestamp = m._moveStartTime;
			m._xAccurate = getX();
			m._yAccurate = getY();
		}
		
		// Check if the position has already been calculated
		if (m._moveTimestamp == gameTicks)
		{
			return false;
		}
		
		int xPrev = getX();
		int yPrev = getY();
		int zPrev = getZ(); // the z coordinate may be modified by coordinate synchronizations
		
		double dx, dy, dz;
		if (Config.COORD_SYNCHRONIZE == 1)
		// the only method that can modify x,y while moving (otherwise _move would/should be set null)
		{
			dx = m._xDestination - xPrev;
			dy = m._yDestination - yPrev;
		}
		else
		// otherwise we need saved temporary values to avoid rounding errors
		{
			dx = m._xDestination - m._xAccurate;
			dy = m._yDestination - m._yAccurate;
		}
		
		final boolean isFloating = isFlying() || isInsideZone(ZoneIdType.WATER);
		
		// Z coordinate will follow geodata or client values
		if ((Config.GEODATA > 0) && (Config.COORD_SYNCHRONIZE == 2) && !isFloating && !m.disregardingGeodata && ((GameTimeController.getInstance().getGameTicks() % 10) == 0 // once a second to reduce possible cpu load
		) && GeoData.getInstance().hasGeo(xPrev, yPrev))
		{
			int geoHeight = GeoData.getInstance().getSpawnHeight(xPrev, yPrev, zPrev - 30, zPrev + 30);
			dz = m._zDestination - geoHeight;
			// quite a big difference, compare to validatePosition packet
			if (isPlayer() && (Math.abs(getActingPlayer().getClientZ() - geoHeight) > 200) && (Math.abs(getActingPlayer().getClientZ() - geoHeight) < 1500))
			{
				dz = m._zDestination - zPrev; // allow diff
			}
			else if (isInCombat() && (Math.abs(dz) > 200) && (((dx * dx) + (dy * dy)) < 40000)) // allow mob to climb up to pcinstance
			{
				dz = m._zDestination - zPrev; // climbing
			}
			else
			{
				zPrev = geoHeight;
			}
		}
		else
		{
			dz = m._zDestination - zPrev;
		}
		
		double delta = (dx * dx) + (dy * dy);
		if ((delta < 10000) && ((dz * dz) > 2500) // close enough, allows error between client and server geodata if it cannot be avoided
			&& !isFloating)
		{
			delta = Math.sqrt(delta);
		}
		else
		{
			delta = Math.sqrt(delta + (dz * dz));
		}
		
		double distFraction = Double.MAX_VALUE;
		if (delta > 1)
		{
			final double distPassed = (getStat().getMoveSpeed() * (gameTicks - m._moveTimestamp)) / GameTimeController.TICKS_PER_SECOND;
			distFraction = distPassed / delta;
		}
		
		// if (Config.DEVELOPER) _log.warn("Move Ticks:" + (gameTicks - m._moveTimestamp) + ", distPassed:" + distPassed + ", distFraction:" + distFraction);
		
		if (distFraction > 1)
		{
			// Set the position of the L2Character to the destination
			super.setXYZ(m._xDestination, m._yDestination, m._zDestination);
		}
		else
		{
			m._xAccurate += dx * distFraction;
			m._yAccurate += dy * distFraction;
			
			// Set the position of the L2Character to estimated after parcial move
			super.setXYZ((int) (m._xAccurate), (int) (m._yAccurate), zPrev + (int) ((dz * distFraction) + 0.5));
		}
		revalidateZone(false);
		
		// Set the timer of last position update to now
		m._moveTimestamp = gameTicks;
		
		return (distFraction > 1);
	}
	
	public void revalidateZone(boolean force)
	{
		if (getWorldRegion() == null)
		{
			return;
		}
		
		// This function is called too often from movement code
		if (force)
		{
			_zoneValidateCounter = 4;
		}
		else
		{
			_zoneValidateCounter--;
			if (_zoneValidateCounter < 0)
			{
				_zoneValidateCounter = 4;
			}
			else
			{
				return;
			}
		}
		
		getWorldRegion().revalidateZones(this);
	}
	
	/**
	 * Stop movement of the L2Character (Called by AI Accessor only).<br>
	 * <B><U>Actions</U>:</B>
	 * <ul>
	 * <li>Delete movement data of the L2Character</li>
	 * <li>Set the current position (x,y,z), its current L2WorldRegion if necessary and its heading</li>
	 * <li>Remove the L2Object object from _gmList of GmListTable</li>
	 * <li>Remove object from _knownObjects and _knownPlayer of all surrounding L2WorldRegion L2Characters</li>
	 * </ul>
	 * <FONT COLOR=#FF0000><B><U>Caution</U>: This method DOESN'T send Server->Client packet StopMove/StopRotation</B></FONT>
	 * @param loc
	 */
	public void stopMove(Location loc)
	{
		stopMove(loc, false);
	}
	
	public void stopMove(Location loc, boolean updateKnownObjects)
	{
		// Delete movement data of the L2Character
		_move = null;
		
		// if (getAI() != null)
		// getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		
		// Set the current position (x,y,z), its current L2WorldRegion if necessary and its heading
		// All data are contained in a Location object
		if (loc != null)
		{
			setXYZ(loc.getX(), loc.getY(), loc.getZ());
			setHeading(loc.getHeading());
			revalidateZone(true);
		}
		broadcastPacket(new StopMove(this));
		if (Config.MOVE_BASED_KNOWNLIST && updateKnownObjects)
		{
			getKnownList().findObjects();
		}
	}
	
	/**
	 * @return Returns the showSummonAnimation.
	 */
	public boolean isShowSummonAnimation()
	{
		return _showSummonAnimation;
	}
	
	/**
	 * @param showSummonAnimation The showSummonAnimation to set.
	 */
	public void setShowSummonAnimation(boolean showSummonAnimation)
	{
		_showSummonAnimation = showSummonAnimation;
	}
	
	/**
	 * Target a L2Object (add the target to the L2Character _target, _knownObject and L2Character to _KnownObject of the L2Object).<br>
	 * <B><U>Concept</U>:</B><br>
	 * The L2Object (including L2Character) targeted is identified in <B>_target</B> of the L2Character.<br>
	 * <B><U>Actions</U>:</B>
	 * <ul>
	 * <li>Set the _target of L2Character to L2Object</li>
	 * <li>If necessary, add L2Object to _knownObject of the L2Character</li>
	 * <li>If necessary, add L2Character to _KnownObject of the L2Object</li>
	 * <li>If object==null, cancel Attak or Cast</li>
	 * </ul>
	 * @param object L2object to target
	 */
	public void setTarget(L2Object object)
	{
		if ((object != null) && !object.isVisible())
		{
			object = null;
		}
		
		if ((object != null) && (object != _target))
		{
			getKnownList().addKnownObject(object);
			object.getKnownList().addKnownObject(this);
		}
		
		_target = object;
	}
	
	/**
	 * @return the identifier of the L2Object targeted or -1.
	 */
	public final int getTargetId()
	{
		if (_target != null)
		{
			return _target.getObjectId();
		}
		return -1;
	}
	
	/**
	 * @return the L2Object targeted or null.
	 */
	public final L2Object getTarget()
	{
		return _target;
	}
	
	// called from AIAccessor only
	/**
	 * Calculate movement data for a move to location action and add the L2Character to movingObjects of GameTimeController (only called by AI Accessor).<br>
	 * <B><U>Concept</U>:</B><br>
	 * At the beginning of the move action, all properties of the movement are stored in the MoveData object called <B>_move</B> of the L2Character.<br>
	 * The position of the start point and of the destination permit to estimated in function of the movement speed the time to achieve the destination.<br>
	 * All L2Character in movement are identified in <B>movingObjects</B> of GameTimeController that will call the updatePosition method of those L2Character each 0.1s.<br>
	 * <B><U>Actions</U>:</B>
	 * <ul>
	 * <li>Get current position of the L2Character</li>
	 * <li>Calculate distance (dx,dy) between current position and destination including offset</li>
	 * <li>Create and Init a MoveData object</li>
	 * <li>Set the L2Character _move object to MoveData object</li>
	 * <li>Add the L2Character to movingObjects of the GameTimeController</li>
	 * <li>Create a task to notify the AI that L2Character arrives at a check point of the movement</li>
	 * </ul>
	 * <FONT COLOR=#FF0000><B><U>Caution</U>: This method DOESN'T send Server->Client packet MoveToPawn/CharMoveToLocation.</B></FONT><br>
	 * <B><U>Example of use</U>:</B>
	 * <ul>
	 * <li>AI : onIntentionMoveTo(L2CharPosition), onIntentionPickUp(L2Object), onIntentionInteract(L2Object)</li>
	 * <li>FollowTask</li>
	 * </ul>
	 * @param x The X position of the destination
	 * @param y The Y position of the destination
	 * @param z The Y position of the destination
	 * @param offset The size of the interaction area of the L2Character targeted
	 */
	public void moveToLocation(int x, int y, int z, int offset)
	{
		// Get the Move Speed of the L2Charcater
		float speed = getStat().getMoveSpeed();
		if ((speed <= 0) || isMovementDisabled())
		{
			return;
		}
		
		// Get current position of the L2Character
		final int curX = super.getX();
		final int curY = super.getY();
		final int curZ = super.getZ();
		
		// Calculate distance (dx,dy) between current position and destination
		// TODO: improve Z axis move/follow support when dx,dy are small compared to dz
		double dx = (x - curX);
		double dy = (y - curY);
		double dz = (z - curZ);
		double distance = Math.sqrt((dx * dx) + (dy * dy));
		
		final boolean verticalMovementOnly = isFlying() && (distance == 0) && (dz != 0);
		if (verticalMovementOnly)
		{
			distance = Math.abs(dz);
		}
		
		// make water move short and use no geodata checks for swimming chars
		// distance in a click can easily be over 3000
		if ((Config.GEODATA > 0) && isInsideZone(ZoneIdType.WATER) && (distance > 700))
		{
			double divider = 700 / distance;
			x = curX + (int) (divider * dx);
			y = curY + (int) (divider * dy);
			z = curZ + (int) (divider * dz);
			dx = (x - curX);
			dy = (y - curY);
			dz = (z - curZ);
			distance = Math.sqrt((dx * dx) + (dy * dy));
		}
		
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
		if ((offset > 0) || (distance < 1))
		{
			// approximation for moving closer when z coordinates are different
			// TODO: handle Z axis movement better
			offset -= Math.abs(dz);
			if (offset < 5)
			{
				offset = 5;
			}
			
			// If no distance to go through, the movement is canceled
			if ((distance < 1) || ((distance - offset) <= 0))
			{
				// Notify the AI that the L2Character is arrived at destination
				getAI().notifyEvent(CtrlEvent.EVT_ARRIVED);
				
				return;
			}
			// Calculate movement angles needed
			sin = dy / distance;
			cos = dx / distance;
			
			distance -= (offset - 5); // due to rounding error, we have to move a bit closer to be in range
			
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
		
		if ((Config.GEODATA > 0) && !isFlying() // flying chars not checked - even canSeeTarget doesn't work yet
			&& (!isInsideZone(ZoneIdType.WATER) || isInsideZone(ZoneIdType.SIEGE))) // swimming also not checked unless in siege zone - but distance is limited
		{
			final boolean isInVehicle = isPlayer() && (getActingPlayer().getVehicle() != null);
			if (isInVehicle)
			{
				m.disregardingGeodata = true;
			}
			
			double originalDistance = distance;
			int originalX = x;
			int originalY = y;
			int originalZ = z;
			int gtx = (originalX - L2World.MAP_MIN_X) >> 4;
			int gty = (originalY - L2World.MAP_MIN_Y) >> 4;
			
			// Movement checks:
			// when geodata == 2, for all characters except mobs returning home (could be changed later to teleport if pathfinding fails)
			// when geodata == 1, for L2Playable and L2RiftInvaderInstance only
			if (((Config.GEODATA == 2) && !(isAttackable() && ((L2Attackable) this).isReturningToSpawnPoint())) || (isPlayer() && !(isInVehicle && (distance > 1500))) || (isSummon() && !(getAI().getIntention() == AI_INTENTION_FOLLOW)) // assuming intention_follow only when following owner
				|| isAfraid() || (this instanceof L2RiftInvaderInstance))
			{
				if (isOnGeodataPath())
				{
					try
					{
						if ((gtx == _move.geoPathGtx) && (gty == _move.geoPathGty))
						{
							return;
						}
						_move.onGeodataPathIndex = -1; // Set not on geodata path
					}
					catch (NullPointerException e)
					{
						// nothing
					}
				}
				
				if ((curX < L2World.MAP_MIN_X) || (curX > L2World.MAP_MAX_X) || (curY < L2World.MAP_MIN_Y) || (curY > L2World.MAP_MAX_Y))
				{
					// Temporary fix for character outside world region errors
					_log.warn("Character " + getName() + " outside world area, in coordinates x:" + curX + " y:" + curY);
					getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
					if (isPlayer())
					{
						getActingPlayer().logout();
					}
					else if (isSummon())
					{
						return; // preventation when summon get out of world coords, player will not loose him, unsummon handled from pcinstance
					}
					else
					{
						onDecay();
					}
					return;
				}
				Location destiny = GeoData.getInstance().moveCheck(curX, curY, curZ, x, y, z, getInstanceId());
				// location different if destination wasn't reached (or just z coord is different)
				x = destiny.getX();
				y = destiny.getY();
				z = destiny.getZ();
				dx = x - curX;
				dy = y - curY;
				dz = z - curZ;
				distance = verticalMovementOnly ? Math.abs(dz * dz) : Math.sqrt((dx * dx) + (dy * dy));
			}
			// Pathfinding checks. Only when geodata setting is 2, the LoS check gives shorter result
			// than the original movement was and the LoS gives a shorter distance than 2000
			// This way of detecting need for pathfinding could be changed.
			if ((Config.GEODATA == 2) && ((originalDistance - distance) > 30) && (distance < 2000) && !isAfraid())
			{
				// Path calculation
				// Overrides previous movement check
				if ((isPlayable() && !isInVehicle) || isMinion() || isInCombat())
				{
					m.geoPath = PathFinding.getInstance().findPath(curX, curY, curZ, originalX, originalY, originalZ, getInstanceId(), isPlayable());
					if ((m.geoPath == null) || (m.geoPath.size() < 2)) // No path found
					{
						// * Even though there's no path found (remember geonodes aren't perfect),
						// the mob is attacking and right now we set it so that the mob will go
						// after target anyway, is dz is small enough.
						// * With cellpathfinding this approach could be changed but would require taking
						// off the geonodes and some more checks.
						// * Summons will follow their masters no matter what.
						// * Currently minions also must move freely since L2AttackableAI commands
						// them to move along with their leader
						if (isPlayer() || (!isPlayable() && !isMinion() && (Math.abs(z - curZ) > 140)) || (isSummon() && !((L2Summon) this).getFollowStatus()))
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
						
						x = m.geoPath.get(m.onGeodataPathIndex).getX();
						y = m.geoPath.get(m.onGeodataPathIndex).getY();
						z = m.geoPath.get(m.onGeodataPathIndex).getZ();
						
						// check for doors in the route
						if (DoorData.getInstance().checkIfDoorsBetween(curX, curY, curZ, x, y, z, getInstanceId(), false))
						{
							m.geoPath = null;
							getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
							return;
						}
						for (int i = 0; i < (m.geoPath.size() - 1); i++)
						{
							if (DoorData.getInstance().checkIfDoorsBetween(m.geoPath.get(i), m.geoPath.get(i + 1), getInstanceId()))
							{
								m.geoPath = null;
								getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
								return;
							}
						}
						
						dx = x - curX;
						dy = y - curY;
						dz = z - curZ;
						distance = verticalMovementOnly ? Math.abs(dz * dz) : Math.sqrt((dx * dx) + (dy * dy));
						sin = dy / distance;
						cos = dx / distance;
					}
				}
			}
			// If no distance to go through, the movement is canceled
			if ((distance < 1) && ((Config.GEODATA == 2) || isPlayable() || (this instanceof L2RiftInvaderInstance) || isAfraid()))
			{
				if (isSummon())
				{
					((L2Summon) this).setFollowStatus(false);
				}
				getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
				return;
			}
		}
		
		// Apply Z distance for flying or swimming for correct timing calculations
		if ((isFlying() || isInsideZone(ZoneIdType.WATER)) && !verticalMovementOnly)
		{
			distance = Math.sqrt((distance * distance) + (dz * dz));
		}
		
		// Caclulate the Nb of ticks between the current position and the destination
		// One tick added for rounding reasons
		int ticksToMove = 1 + (int) ((GameTimeController.TICKS_PER_SECOND * distance) / speed);
		m._xDestination = x;
		m._yDestination = y;
		m._zDestination = z; // this is what was requested from client
		
		// Calculate and set the heading of the L2Character
		m._heading = 0; // initial value for coordinate sync
		// Does not broke heading on vertical movements
		if (!verticalMovementOnly)
		{
			setHeading(Util.calculateHeadingFrom(cos, sin));
		}
		
		m._moveStartTime = GameTimeController.getInstance().getGameTicks();
		
		// Set the L2Character _move object to MoveData object
		_move = m;
		
		// Add the L2Character to movingObjects of the GameTimeController
		// The GameTimeController manage objects movement
		GameTimeController.getInstance().registerMovingObject(this);
		
		// Create a task to notify the AI that L2Character arrives at a check point of the movement
		if ((ticksToMove * GameTimeController.MILLIS_IN_TICK) > 3000)
		{
			ThreadPoolManager.getInstance().scheduleAi(new NotifyAITask(this, CtrlEvent.EVT_ARRIVED_REVALIDATE), 2000);
		}
		
		// the CtrlEvent.EVT_ARRIVED will be sent when the character will actually arrive
		// to destination by GameTimeController
	}
	
	public boolean moveToNextRoutePoint()
	{
		if (!isOnGeodataPath())
		{
			// Cancel the move action
			_move = null;
			return false;
		}
		
		// Get the Move Speed of the L2Charcater
		float speed = getStat().getMoveSpeed();
		if ((speed <= 0) || isMovementDisabled())
		{
			// Cancel the move action
			_move = null;
			return false;
		}
		
		MoveData md = _move;
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
		
		if (md.onGeodataPathIndex == (md.geoPath.size() - 2))
		{
			m._xDestination = md.geoPathAccurateTx;
			m._yDestination = md.geoPathAccurateTy;
			m._zDestination = md.geoPath.get(m.onGeodataPathIndex).getZ();
		}
		else
		{
			m._xDestination = md.geoPath.get(m.onGeodataPathIndex).getX();
			m._yDestination = md.geoPath.get(m.onGeodataPathIndex).getY();
			m._zDestination = md.geoPath.get(m.onGeodataPathIndex).getZ();
		}
		double dx = (m._xDestination - super.getX());
		double dy = (m._yDestination - super.getY());
		double distance = Math.sqrt((dx * dx) + (dy * dy));
		// Calculate and set the heading of the L2Character
		if (distance != 0)
		{
			setHeading(Util.calculateHeadingFrom(getX(), getY(), m._xDestination, m._yDestination));
		}
		
		// Caclulate the Nb of ticks between the current position and the destination
		// One tick added for rounding reasons
		int ticksToMove = 1 + (int) ((GameTimeController.TICKS_PER_SECOND * distance) / speed);
		
		m._heading = 0; // initial value for coordinate sync
		
		m._moveStartTime = GameTimeController.getInstance().getGameTicks();
		
		// Set the L2Character _move object to MoveData object
		_move = m;
		
		// Add the L2Character to movingObjects of the GameTimeController
		// The GameTimeController manage objects movement
		GameTimeController.getInstance().registerMovingObject(this);
		
		// Create a task to notify the AI that L2Character arrives at a check point of the movement
		if ((ticksToMove * GameTimeController.MILLIS_IN_TICK) > 3000)
		{
			ThreadPoolManager.getInstance().scheduleAi(new NotifyAITask(this, CtrlEvent.EVT_ARRIVED_REVALIDATE), 2000);
		}
		
		// the CtrlEvent.EVT_ARRIVED will be sent when the character will actually arrive
		// to destination by GameTimeController
		
		// Send a Server->Client packet CharMoveToLocation to the actor and all L2PcInstance in its _knownPlayers
		MoveToLocation msg = new MoveToLocation(this);
		broadcastPacket(msg);
		
		return true;
	}
	
	public boolean validateMovementHeading(int heading)
	{
		MoveData m = _move;
		
		if (m == null)
		{
			return true;
		}
		
		boolean result = true;
		if (m._heading != heading)
		{
			result = (m._heading == 0); // initial value or false
			m._heading = heading;
		}
		
		return result;
	}
	
	/**
	 * Return the distance between the current position of the L2Character and the target (x,y).
	 * @param x X position of the target
	 * @param y Y position of the target
	 * @return the plan distance
	 * @deprecated use getPlanDistanceSq(int x, int y, int z)
	 */
	@Deprecated
	public final double getDistance(int x, int y)
	{
		double dx = x - getX();
		double dy = y - getY();
		
		return Math.sqrt((dx * dx) + (dy * dy));
	}
	
	/**
	 * Return the distance between the current position of the L2Character and the target (x,y).
	 * @param x X position of the target
	 * @param y Y position of the target
	 * @param z
	 * @return the plan distance
	 * @deprecated use getPlanDistanceSq(int x, int y, int z)
	 */
	@Deprecated
	public final double getDistance(int x, int y, int z)
	{
		double dx = x - getX();
		double dy = y - getY();
		double dz = z - getZ();
		
		return Math.sqrt((dx * dx) + (dy * dy) + (dz * dz));
	}
	
	/**
	 * Return the squared distance between the current position of the L2Character and the given object.
	 * @param object L2Object
	 * @return the squared distance
	 */
	public final double getDistanceSq(L2Object object)
	{
		return getDistanceSq(object.getX(), object.getY(), object.getZ());
	}
	
	/**
	 * Return the squared distance between the current position of the L2Character and the given x, y, z.
	 * @param x X position of the target
	 * @param y Y position of the target
	 * @param z Z position of the target
	 * @return the squared distance
	 */
	public final double getDistanceSq(int x, int y, int z)
	{
		double dx = x - getX();
		double dy = y - getY();
		double dz = z - getZ();
		
		return ((dx * dx) + (dy * dy) + (dz * dz));
	}
	
	/**
	 * Return the squared plan distance between the current position of the L2Character and the given object.<BR>
	 * (check only x and y, not z)
	 * @param object L2Object
	 * @return the squared plan distance
	 */
	public final double getPlanDistanceSq(L2Object object)
	{
		return getPlanDistanceSq(object.getX(), object.getY());
	}
	
	/**
	 * Return the squared plan distance between the current position of the L2Character and the given x, y, z.<BR>
	 * (check only x and y, not z)
	 * @param x X position of the target
	 * @param y Y position of the target
	 * @return the squared plan distance
	 */
	public final double getPlanDistanceSq(int x, int y)
	{
		double dx = x - getX();
		double dy = y - getY();
		
		return ((dx * dx) + (dy * dy));
	}
	
	/**
	 * Check if this object is inside the given radius around the given point.
	 * @param loc Location of the target
	 * @param radius the radius around the target
	 * @param checkZAxis should we check Z axis also
	 * @param strictCheck true if (distance < radius), false if (distance <= radius)
	 * @return true if the L2Character is inside the radius.
	 */
	public final boolean isInsideRadius(ILocational loc, int radius, boolean checkZAxis, boolean strictCheck)
	{
		return isInsideRadius(loc.getX(), loc.getY(), loc.getZ(), radius, checkZAxis, strictCheck);
	}
	
	/**
	 * Check if this object is inside the given radius around the given point.
	 * @param x X position of the target
	 * @param y Y position of the target
	 * @param z Z position of the target
	 * @param radius the radius around the target
	 * @param checkZAxis should we check Z axis also
	 * @param strictCheck true if (distance < radius), false if (distance <= radius)
	 * @return true if the L2Character is inside the radius.
	 */
	public final boolean isInsideRadius(int x, int y, int z, int radius, boolean checkZAxis, boolean strictCheck)
	{
		final double distance = calculateDistance(x, y, z, checkZAxis, true);
		return (strictCheck) ? (distance < (radius * radius)) : (distance <= (radius * radius));
	}
	
	/**
	 * Set _attacking corresponding to Attacking Body part to CHEST.
	 */
	public void setAttackingBodypart()
	{
		_attacking = Inventory.PAPERDOLL_CHEST;
	}
	
	/**
	 * <B><U> Overridden in </U> :</B> <li>L2PcInstance</li>
	 * @return True if arrows are available.
	 */
	protected boolean checkAndEquipArrows()
	{
		return true;
	}
	
	/**
	 * <B><U> Overridden in </U> :</B> <li>L2PcInstance</li>
	 * @return True if bolts are available.
	 */
	protected boolean checkAndEquipBolts()
	{
		return true;
	}
	
	/**
	 * Add Exp and Sp to the L2Character.<br>
	 * <B><U> Overridden in </U> :</B> <li>L2PcInstance</li> <li>L2PetInstance</li>
	 * @param addToExp
	 * @param addToSp
	 */
	public void addExpAndSp(long addToExp, int addToSp)
	{
		// Dummy method (overridden by players and pets)
	}
	
	/**
	 * <B><U> Overridden in </U> :</B> <li>L2PcInstance</li>
	 * @return the active weapon instance (always equiped in the right hand).
	 */
	public abstract L2ItemInstance getActiveWeaponInstance();
	
	/**
	 * <B><U> Overridden in </U> :</B> <li>L2PcInstance</li>
	 * @return the active weapon item (always equiped in the right hand).
	 */
	public abstract L2Weapon getActiveWeaponItem();
	
	/**
	 * <B><U> Overridden in </U> :</B> <li>L2PcInstance</li>
	 * @return the secondary weapon instance (always equiped in the left hand).
	 */
	public abstract L2ItemInstance getSecondaryWeaponInstance();
	
	/**
	 * <B><U> Overridden in </U> :</B> <li>L2PcInstance</li>
	 * @return the secondary {@link L2Item} item (always equiped in the left hand).
	 */
	public abstract L2Item getSecondaryWeaponItem();
	
	/**
	 * Manage hit process (called by Hit Task).<br>
	 * <B><U>Actions</U>:</B>
	 * <ul>
	 * <li>If the attacker/target is dead or use fake death, notify the AI with EVT_CANCEL and send a Server->Client packet ActionFailed (if attacker is a L2PcInstance)</li>
	 * <li>If attack isn't aborted, send a message system (critical hit, missed...) to attacker/target if they are L2PcInstance</li>
	 * <li>If attack isn't aborted and hit isn't missed, reduce HP of the target and calculate reflection damage to reduce HP of attacker if necessary</li>
	 * <li>if attack isn't aborted and hit isn't missed, manage attack or cast break of the target (calculating rate, sending message...)</li>
	 * </ul>
	 * @param target The L2Character targeted
	 * @param damage Nb of HP to reduce
	 * @param crit True if hit is critical
	 * @param miss True if hit is missed
	 * @param soulshot True if SoulShot are charged
	 * @param shld True if shield is efficient
	 */
	public void onHitTimer(L2Character target, int damage, boolean crit, boolean miss, boolean soulshot, byte shld)
	{
		// If the attacker/target is dead or use fake death, notify the AI with EVT_CANCEL
		// and send a Server->Client packet ActionFailed (if attacker is a L2PcInstance)
		if ((target == null) || isAlikeDead())
		{
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
			return;
		}
		
		if ((isNpc() && target.isAlikeDead()) || target.isDead() || (!getKnownList().knowsObject(target) && !isDoor()))
		{
			// getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null);
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
			
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (miss)
		{
			// Notify target AI
			if (target.hasAI())
			{
				target.getAI().notifyEvent(CtrlEvent.EVT_EVADED, this);
			}
			
			// ON_EVADED_HIT
			if (target.getChanceSkills() != null)
			{
				target.getChanceSkills().onEvadedHit(this);
			}
		}
		
		// Send message about damage/crit or miss
		sendDamageMessage(target, damage, false, crit, miss);
		
		// If attack isn't aborted, send a message system (critical hit, missed...) to attacker/target if they are L2PcInstance
		if (!isAttackAborted())
		{
			// Check Raidboss attack
			// Character will be petrified if attacking a raid that's more
			// than 8 levels lower
			if (target.isRaid() && target.giveRaidCurse() && !Config.RAID_DISABLE_CURSE)
			{
				if (getLevel() > (target.getLevel() + 8))
				{
					L2Skill skill = SkillData.FrequentSkill.RAID_CURSE2.getSkill();
					
					if (skill != null)
					{
						abortAttack();
						abortCast();
						getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
						skill.getEffects(target, this);
					}
					else
					{
						_log.warn("Skill 4515 at level 1 is missing in DP.");
					}
					
					damage = 0; // prevents messing up drop calculation
				}
			}
			
			// If L2Character target is a L2PcInstance, send a system message
			if (target.isPlayer())
			{
				L2PcInstance enemy = target.getActingPlayer();
				enemy.getAI().clientStartAutoAttack();
			}
			
			if (!miss && (damage > 0))
			{
				L2Weapon weapon = getActiveWeaponItem();
				boolean isBow = ((weapon != null) && ((weapon.getItemType() == WeaponType.BOW) || (weapon.getItemType() == WeaponType.CROSSBOW)));
				int reflectedDamage = 0;
				
				if (!isBow && !target.isInvul()) // Do not reflect if weapon is of type bow or target is invunlerable
				{
					// quick fix for no drop from raid if boss attack high-level char with damage reflection
					if (!target.isRaid() || (getActingPlayer() == null) || (getActingPlayer().getLevel() <= (target.getLevel() + 8)))
					{
						// Reduce HP of the target and calculate reflection damage to reduce HP of attacker if necessary
						double reflectPercent = target.getStat().calcStat(Stats.REFLECT_DAMAGE_PERCENT, 0, null, null);
						
						if (reflectPercent > 0)
						{
							reflectedDamage = (int) ((reflectPercent / 100.) * damage);
							
							if (reflectedDamage > target.getMaxHp())
							{
								reflectedDamage = target.getMaxHp();
							}
						}
					}
				}
				
				// reduce targets HP
				target.reduceCurrentHp(damage, this, null);
				target.notifyDamageReceived(damage, this, null, crit, false);
				
				if (reflectedDamage > 0)
				{
					reduceCurrentHp(reflectedDamage, target, true, false, null);
					notifyDamageReceived(reflectedDamage, target, null, crit, false);
				}
				
				if (!isBow) // Do not absorb if weapon is of type bow
				{
					// Absorb HP from the damage inflicted
					double absorbPercent = getStat().calcStat(Stats.ABSORB_DAMAGE_PERCENT, 0, null, null);
					
					if (absorbPercent > 0)
					{
						int maxCanAbsorb = (int) (getMaxRecoverableHp() - getCurrentHp());
						int absorbDamage = (int) ((absorbPercent / 100.) * damage);
						
						if (absorbDamage > maxCanAbsorb)
						{
							absorbDamage = maxCanAbsorb; // Can't absord more than max hp
						}
						
						if (absorbDamage > 0)
						{
							setCurrentHp(getCurrentHp() + absorbDamage);
						}
					}
					
					// Absorb MP from the damage inflicted
					absorbPercent = getStat().calcStat(Stats.ABSORB_MANA_DAMAGE_PERCENT, 0, null, null);
					
					if (absorbPercent > 0)
					{
						int maxCanAbsorb = (int) (getMaxRecoverableMp() - getCurrentMp());
						int absorbDamage = (int) ((absorbPercent / 100.) * damage);
						
						if (absorbDamage > maxCanAbsorb)
						{
							absorbDamage = maxCanAbsorb; // Can't absord more than max hp
						}
						
						if (absorbDamage > 0)
						{
							setCurrentMp(getCurrentMp() + absorbDamage);
						}
					}
					
				}
				
				// Notify AI with EVT_ATTACKED
				if (target.hasAI())
				{
					target.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, this);
				}
				getAI().clientStartAutoAttack();
				if (isSummon())
				{
					L2PcInstance owner = ((L2Summon) this).getOwner();
					if (owner != null)
					{
						owner.getAI().clientStartAutoAttack();
					}
				}
				
				// Manage attack or cast break of the target (calculating rate, sending message...)
				if (!target.isRaid() && Formulas.calcAtkBreak(target, damage))
				{
					target.breakAttack();
					target.breakCast();
				}
				
				// Maybe launch chance skills on us
				if (_chanceSkills != null)
				{
					_chanceSkills.onHit(target, damage, false, crit);
					// Reflect triggers onHit
					if (reflectedDamage > 0)
					{
						// vGodFather fix
						// _chanceSkills.onHit(target, reflectedDamage, false, false); // test fix
						_chanceSkills.onHit(target, reflectedDamage, true, false); // original
					}
				}
				
				if (_triggerSkills != null)
				{
					for (OptionsSkillHolder holder : _triggerSkills.values())
					{
						if ((!crit && (holder.getSkillType() == OptionsSkillType.ATTACK)) || ((holder.getSkillType() == OptionsSkillType.CRITICAL) && crit))
						{
							if (Rnd.get(100) < holder.getChance())
							{
								makeTriggerCast(holder.getSkill(), target);
							}
						}
					}
				}
				
				// Maybe launch chance skills on target
				if (target.getChanceSkills() != null)
				{
					target.getChanceSkills().onHit(this, damage, true, crit);
				}
			}
			
			// Launch weapon Special ability effect if available
			L2Weapon activeWeapon = getActiveWeaponItem();
			if (activeWeapon != null)
			{
				activeWeapon.getSkillEffects(this, target, crit);
			}
			if ((this instanceof L2EventMapGuardInstance) && (target instanceof L2PcInstance))
			{
				target.doDie(this);
			}
			
			// Recharge any active auto-soulshot tasks for current creature.
			rechargeShots(true, false);
			
			return;
		}
		
		if (!isCastingNow() && !isCastingSimultaneouslyNow())
		{
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
		}
	}
	
	/**
	 * Break an attack and send Server->Client ActionFailed packet and a System Message to the L2Character.
	 */
	public void breakAttack()
	{
		if (isAttackingNow())
		{
			// Abort the attack of the L2Character and send Server->Client ActionFailed packet
			abortAttack();
			if (isPlayer())
			{
				// Send a system message
				sendPacket(SystemMessageId.ATTACK_FAILED);
			}
		}
	}
	
	/**
	 * Break a cast and send Server->Client ActionFailed packet and a System Message to the L2Character.
	 */
	public void breakCast()
	{
		// damage can only cancel magical & static skills
		if (isCastingNow() && canAbortCast() && (getLastSkillCast() != null) && (getLastSkillCast().isMagic() || getLastSkillCast().isStatic()))
		{
			// Abort the cast of the L2Character and send Server->Client MagicSkillCanceld/ActionFailed packet.
			abortCast();
			
			if (isPlayer())
			{
				// Send a system message
				sendPacket(SystemMessageId.CASTING_INTERRUPTED);
			}
		}
	}
	
	/**
	 * Reduce the arrow number of the L2Character.<br>
	 * <B><U> Overridden in </U> :</B> <li>L2PcInstance</li>
	 * @param bolts
	 */
	protected void reduceArrowCount(boolean bolts)
	{
		// default is to do nothing
	}
	
	/**
	 * Manage Forced attack (shift + select target).<br>
	 * <B><U>Actions</U>:</B>
	 * <ul>
	 * <li>If L2Character or target is in a town area, send a system message TARGET_IN_PEACEZONE a Server->Client packet ActionFailed</li>
	 * <li>If target is confused, send a Server->Client packet ActionFailed</li>
	 * <li>If L2Character is a L2ArtefactInstance, send a Server->Client packet ActionFailed</li>
	 * <li>Send a Server->Client packet MyTargetSelected to start attack and Notify AI with AI_INTENTION_ATTACK</li>
	 * </ul>
	 * @param player The L2PcInstance to attack
	 */
	@Override
	public void onForcedAttack(L2PcInstance player)
	{
		if (isInsidePeaceZone(player))
		{
			// If L2Character or target is in a peace zone, send a system message TARGET_IN_PEACEZONE a Server->Client packet ActionFailed
			player.sendPacket(SystemMessageId.TARGET_IN_PEACEZONE);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if (player.isInOlympiadMode() && (player.getTarget() != null) && player.getTarget().isPlayable())
		{
			L2PcInstance target = null;
			L2Object object = player.getTarget();
			if ((object != null) && object.isPlayable())
			{
				target = object.getActingPlayer();
			}
			
			if ((target == null) || (target.isInOlympiadMode() && (!player.isOlympiadStart() || (player.getOlympiadGameId() != target.getOlympiadGameId()))))
			{
				// if L2PcInstance is in Olympia and the match isn't already start, send a Server->Client packet ActionFailed
				player.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}
		if ((player.getTarget() != null) && !player.getTarget().canBeAttacked() && !player.getAccessLevel().allowPeaceAttack())
		{
			// If target is not attackable, send a Server->Client packet ActionFailed
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if (player.isConfused())
		{
			// If target is confused, send a Server->Client packet ActionFailed
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		// GeoData Los Check or dz > 1000
		if (!GeoData.getInstance().canSeeTarget(player, this))
		{
			player.sendPacket(SystemMessageId.CANT_SEE_TARGET);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if (player.getBlockCheckerArena() != -1)
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		// Notify AI with AI_INTENTION_ATTACK
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
	}
	
	/**
	 * @param attacker
	 * @return True if inside peace zone.
	 */
	public boolean isInsidePeaceZone(L2PcInstance attacker)
	{
		return isInsidePeaceZone(attacker, this);
	}
	
	public boolean isInsidePeaceZone(L2PcInstance attacker, L2Object target)
	{
		return (!attacker.getAccessLevel().allowPeaceAttack() && isInsidePeaceZone((L2Object) attacker, target));
	}
	
	public boolean isInsidePeaceZone(L2Object attacker, L2Object target)
	{
		if (target == null)
		{
			return false;
		}
		if (!(target.isPlayable() && attacker.isPlayable()))
		{
			return false;
		}
		if (InstanceManager.getInstance().getInstance(getInstanceId()).isPvPInstance())
		{
			return false;
		}
		
		if (TerritoryWarManager.PLAYER_WITH_WARD_CAN_BE_KILLED_IN_PEACEZONE && TerritoryWarManager.getInstance().isTWInProgress())
		{
			if (target.isPlayer() && target.getActingPlayer().isCombatFlagEquipped())
			{
				return false;
			}
		}
		
		if (Config.ALT_GAME_KARMA_PLAYER_CAN_BE_KILLED_IN_PEACEZONE)
		{
			// allows red to be attacked and red to attack flagged players
			if ((target.getActingPlayer() != null) && (target.getActingPlayer().getKarma() > 0))
			{
				return false;
			}
			if ((attacker.getActingPlayer() != null) && (attacker.getActingPlayer().getKarma() > 0) && (target.getActingPlayer() != null) && (target.getActingPlayer().getPvpFlag() > 0))
			{
				return false;
			}
			
			if ((attacker instanceof L2Character) && (target instanceof L2Character))
			{
				return (target.isInsideZone(ZoneIdType.PEACE) || attacker.isInsideZone(ZoneIdType.PEACE));
			}
			if (attacker instanceof L2Character)
			{
				return ((TownManager.getTown(target.getX(), target.getY(), target.getZ()) != null) || attacker.isInsideZone(ZoneIdType.PEACE));
			}
		}
		
		if ((attacker instanceof L2Character) && (target instanceof L2Character))
		{
			return (target.isInsideZone(ZoneIdType.PEACE) || attacker.isInsideZone(ZoneIdType.PEACE));
		}
		if (attacker instanceof L2Character)
		{
			return ((TownManager.getTown(target.getX(), target.getY(), target.getZ()) != null) || attacker.isInsideZone(ZoneIdType.PEACE));
		}
		return ((TownManager.getTown(target.getX(), target.getY(), target.getZ()) != null) || (TownManager.getTown(attacker.getX(), attacker.getY(), attacker.getZ()) != null));
	}
	
	/**
	 * @return true if this character is inside an active grid.
	 */
	public boolean isInActiveRegion()
	{
		L2WorldRegion region = getWorldRegion();
		return ((region != null) && (region.isActive()));
	}
	
	/**
	 * @return True if the L2Character has a Party in progress.
	 */
	public boolean isInParty()
	{
		return false;
	}
	
	/**
	 * @return the L2Party object of the L2Character.
	 */
	public L2Party getParty()
	{
		return null;
	}
	
	/**
	 * @param target
	 * @param weapon
	 * @return the Attack Speed of the L2Character (delay (in milliseconds) before next attack).
	 */
	public int calculateTimeBetweenAttacks(L2Character target, L2Weapon weapon)
	{
		double atkSpd = 0;
		if ((weapon != null) && !isTransformed())
		{
			switch (weapon.getItemType())
			{
				case BOW:
					atkSpd = getStat().getPAtkSpd();
					return (int) ((1500 * 345) / atkSpd);
				case CROSSBOW:
					atkSpd = getStat().getPAtkSpd();
					return (int) ((1200 * 345) / atkSpd);
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
		
		return Formulas.calcPAtkSpd(this, target, atkSpd);
	}
	
	public int calculateReuseTime(L2Character target, L2Weapon weapon)
	{
		if ((weapon == null) || isTransformed())
		{
			return 0;
		}
		
		int reuse = weapon.getReuseDelay();
		// only bows should continue for now
		if (reuse == 0)
		{
			return 0;
		}
		
		reuse *= getStat().getWeaponReuseModifier(target);
		double atkSpd = getStat().getPAtkSpd();
		switch (weapon.getItemType())
		{
			case BOW:
			case CROSSBOW:
				return (int) ((reuse * 345) / atkSpd);
			default:
				return (int) ((reuse * 312) / atkSpd);
		}
	}
	
	/**
	 * @return True if the L2Character use a dual weapon.
	 */
	public boolean isUsingDualWeapon()
	{
		return false;
	}
	
	/**
	 * Add a skill to the L2Character _skills and its Func objects to the calculator set of the L2Character.<br>
	 * <B><U>Concept</U>:</B><br>
	 * All skills own by a L2Character are identified in <B>_skills</B><br>
	 * <B><U>Actions</U>:</B>
	 * <ul>
	 * <li>Replace oldSkill by newSkill or Add the newSkill</li>
	 * <li>If an old skill has been replaced, remove all its Func objects of L2Character calculator set</li>
	 * <li>Add Func objects of newSkill to the calculator set of the L2Character</li>
	 * </ul>
	 * <B><U>Overridden in</U>:</B>
	 * <ul>
	 * <li>L2PcInstance : Save update in the character_skills table of the database</li>
	 * </ul>
	 * @param newSkill The L2Skill to add to the L2Character
	 * @return The L2Skill replaced or null if just added a new L2Skill
	 */
	@Override
	public L2Skill addSkill(L2Skill newSkill)
	{
		L2Skill oldSkill = null;
		if (newSkill != null)
		{
			// Replace oldSkill by newSkill or Add the newSkill
			oldSkill = _skills.put(newSkill.getId(), newSkill);
			// If an old skill has been replaced, remove all its Func objects
			if (oldSkill != null)
			{
				// if skill came with another one, we should delete the other one too.
				if ((oldSkill.triggerAnotherSkill()))
				{
					removeSkill(oldSkill.getTriggeredId(), true);
				}
				removeStatsOwner(oldSkill);
			}
			// Add Func objects of newSkill to the calculator set of the L2Character
			addStatFuncs(newSkill.getStatFuncs(null, this));
			
			if ((oldSkill != null) && (_chanceSkills != null))
			{
				removeChanceSkill(oldSkill.getId());
			}
			if (newSkill.isChance())
			{
				addChanceTrigger(newSkill);
			}
			
			// Add passive effects if there are any.
			newSkill.getEffectsPassive(this);
		}
		return oldSkill;
	}
	
	public L2Skill removeSkill(L2Skill skill, boolean cancelEffect)
	{
		return (skill != null) ? removeSkill(skill.getId(), cancelEffect) : null;
	}
	
	public L2Skill removeSkill(int skillId)
	{
		return removeSkill(skillId, true);
	}
	
	public L2Skill removeSkill(int skillId, boolean cancelEffect)
	{
		// Remove the skill from the L2Character _skills
		L2Skill oldSkill = _skills.remove(skillId);
		// Remove all its Func objects from the L2Character calculator set
		if (oldSkill != null)
		{
			// this is just a fail-safe againts buggers and gm dummies...
			if ((oldSkill.triggerAnotherSkill()) && (oldSkill.getTriggeredId() > 0))
			{
				removeSkill(oldSkill.getTriggeredId(), true);
			}
			
			// does not abort casting of the transformation dispell
			if (oldSkill.getSkillType() != L2SkillType.TRANSFORMDISPEL)
			{
				// Stop casting if this skill is used right now
				if ((getLastSkillCast() != null) && isCastingNow())
				{
					if (oldSkill.getId() == getLastSkillCast().getId())
					{
						abortCast();
					}
				}
				if ((getLastSimultaneousSkillCast() != null) && isCastingSimultaneouslyNow())
				{
					if (oldSkill.getId() == getLastSimultaneousSkillCast().getId())
					{
						abortCast();
					}
				}
			}
			
			// Remove passive effects.
			_effects.removePassiveEffects(skillId);
			
			if (cancelEffect || oldSkill.isToggle())
			{
				// for now, to support transformations, we have to let their
				// effects stay when skill is removed
				L2Effect e = getFirstEffect(oldSkill);
				if ((e == null) || (e.getEffectType() != L2EffectType.TRANSFORMATION))
				{
					removeStatsOwner(oldSkill);
					stopSkillEffects(oldSkill.getId());
				}
			}
			if (isPlayer())
			{
				// TODO: Unhardcode it!
				if ((oldSkill instanceof L2SkillSummon) && (oldSkill.getId() == 710) && hasSummon() && (getSummon().getId() == 14870))
				{
					getActingPlayer().getSummon().unSummon(getActingPlayer());
				}
			}
			
			if (oldSkill.isChance() && (_chanceSkills != null))
			{
				removeChanceSkill(oldSkill.getId());
			}
		}
		
		return oldSkill;
	}
	
	public void removeChanceSkill(int id)
	{
		if (_chanceSkills == null)
		{
			return;
		}
		synchronized (_chanceSkills)
		{
			for (IChanceSkillTrigger trigger : _chanceSkills.keySet())
			{
				if (!(trigger instanceof L2Skill))
				{
					continue;
				}
				if (((L2Skill) trigger).getId() == id)
				{
					_chanceSkills.remove(trigger);
				}
			}
		}
	}
	
	public void addChanceTrigger(IChanceSkillTrigger trigger)
	{
		if (_chanceSkills == null)
		{
			synchronized (this)
			{
				if (_chanceSkills == null)
				{
					_chanceSkills = new ChanceSkillList(this);
				}
			}
		}
		_chanceSkills.put(trigger, trigger.getTriggeredChanceCondition());
	}
	
	public void removeChanceEffect(IChanceSkillTrigger effect)
	{
		if (_chanceSkills == null)
		{
			return;
		}
		_chanceSkills.remove(effect);
	}
	
	public void onStartChanceEffect(byte element)
	{
		if (_chanceSkills == null)
		{
			return;
		}
		
		_chanceSkills.onStart(element);
	}
	
	public void onActionTimeChanceEffect(byte element)
	{
		if (_chanceSkills == null)
		{
			return;
		}
		
		_chanceSkills.onActionTime(element);
	}
	
	public void onExitChanceEffect(byte element)
	{
		if (_chanceSkills == null)
		{
			return;
		}
		
		_chanceSkills.onExit(element);
	}
	
	/**
	 * <B><U>Concept</U>:</B><br>
	 * All skills own by a L2Character are identified in <B>_skills</B> the L2Character
	 * @return all skills own by the L2Character in a table of L2Skill.
	 */
	public final Collection<L2Skill> getAllSkills()
	{
		return new ArrayList<>(_skills.values());
	}
	
	/**
	 * @return the map containing this character skills.
	 */
	@Override
	public Map<Integer, L2Skill> getSkills()
	{
		return _skills;
	}
	
	public ChanceSkillList getChanceSkills()
	{
		return _chanceSkills;
	}
	
	/**
	 * Return the level of a skill owned by the L2Character.
	 * @param skillId The identifier of the L2Skill whose level must be returned
	 * @return The level of the L2Skill identified by skillId
	 */
	@Override
	public int getSkillLevel(int skillId)
	{
		final L2Skill skill = getKnownSkill(skillId);
		return (skill == null) ? -1 : skill.getLevel();
	}
	
	/**
	 * @param skillId The identifier of the L2Skill to check the knowledge
	 * @return the skill from the known skill.
	 */
	@Override
	public final L2Skill getKnownSkill(int skillId)
	{
		return _skills.get(skillId);
	}
	
	/**
	 * Return the number of buffs affecting this L2Character.
	 * @return The number of Buffs affecting this L2Character
	 */
	public int getBuffCount()
	{
		return _effects.getBuffCount();
	}
	
	public int getDanceCount()
	{
		return _effects.getDanceCount();
	}
	
	/**
	 * Manage the magic skill launching task (MP, HP, Item consummation...) and display the magic skill animation on client.<br>
	 * <B><U>Actions</U>:</B>
	 * <ul>
	 * <li>Send a Server->Client packet MagicSkillLaunched (to display magic skill animation) to all L2PcInstance of L2Charcater _knownPlayers</li>
	 * <li>Consumme MP, HP and Item if necessary</li>
	 * <li>Send a Server->Client packet StatusUpdate with MP modification to the L2PcInstance</li>
	 * <li>Launch the magic skill in order to calculate its effects</li>
	 * <li>If the skill type is PDAM, notify the AI of the target with AI_INTENTION_ATTACK</li>
	 * <li>Notify the AI of the L2Character with EVT_FINISH_CASTING</li>
	 * </ul>
	 * <FONT COLOR=#FF0000><B><U>Caution</U>: A magic skill casting MUST BE in progress</B></FONT>
	 * @param mut
	 */
	public void onMagicLaunchedTimer(MagicUseTask mut)
	{
		final L2Skill skill = mut.getSkill();
		L2Object[] targets = mut.getTargets();
		
		if ((skill == null) || (targets == null))
		{
			abortCast();
			return;
		}
		
		if (targets.length == 0)
		{
			switch (skill.getTargetType())
			{
			// only AURA-type skills can be cast without target
				case AURA:
				case FRONT_AURA:
				case BEHIND_AURA:
				case AURA_CORPSE_MOB:
					break;
				default:
					abortCast();
					return;
			}
		}
		
		// Escaping from under skill's radius and peace zone check. First version, not perfect in AoE skills.
		int escapeRange = 0;
		if (skill.getEffectRange() > escapeRange)
		{
			escapeRange = skill.getEffectRange();
		}
		else if ((skill.getCastRange() < 0) && (skill.getAffectRange() > 80))
		{
			escapeRange = skill.getAffectRange();
		}
		
		if ((targets.length > 0) && (escapeRange > 0))
		{
			int _skiprange = 0;
			int _skipgeo = 0;
			int _skippeace = 0;
			List<L2Character> targetList = new FastList<>(targets.length);
			for (L2Object target : targets)
			{
				if (target instanceof L2Character)
				{
					if (!isInsideRadius(target.getX(), target.getY(), target.getZ(), escapeRange + getTemplate().getCollisionRadius(), true, false))
					{
						_skiprange++;
						continue;
					}
					if ((escapeRange > 0) && (Config.GEODATA > 0) && !GeoData.getInstance().canSeeTarget(this, target))
					{
						_skipgeo++;
						continue;
					}
					if (skill.isOffensive())
					{
						if (isPlayer())
						{
							if (((L2Character) target).isInsidePeaceZone(getActingPlayer()))
							{
								_skippeace++;
								continue;
							}
						}
						else
						{
							if (((L2Character) target).isInsidePeaceZone(this, target))
							{
								_skippeace++;
								continue;
							}
						}
					}
					targetList.add((L2Character) target);
				}
			}
			if (targetList.isEmpty())
			{
				if (isPlayer())
				{
					if (_skiprange > 0)
					{
						sendPacket(SystemMessageId.DIST_TOO_FAR_CASTING_STOPPED);
					}
					else if (_skipgeo > 0)
					{
						sendPacket(SystemMessageId.CANT_SEE_TARGET);
					}
					else if (_skippeace > 0)
					{
						sendPacket(SystemMessageId.A_MALICIOUS_SKILL_CANNOT_BE_USED_IN_PEACE_ZONE);
					}
				}
				abortCast();
				return;
			}
			mut.setTargets(targetList.toArray(new L2Character[targetList.size()]));
		}
		
		// Ensure that a cast is in progress
		// Check if player is using fake death.
		// Static skills can be used while faking death.
		if ((mut.isSimultaneous() && !isCastingSimultaneouslyNow()) || (!mut.isSimultaneous() && !isCastingNow()) || (isAlikeDead() && !skill.isStatic()))
		{
			// now cancels both, simultaneous and normal
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
			return;
		}
		
		// Send a Server->Client packet MagicSkillLaunched to the L2Character AND to all L2PcInstance in the _KnownPlayers of the L2Character
		if (!skill.isStatic())
		{
			broadcastPacket(new MagicSkillLaunched(this, skill.getDisplayId(), skill.getDisplayLevel(), targets));
		}
		
		mut.setPhase(2);
		if (mut.getSkillTime() == 0)
		{
			onMagicHitTimer(mut);
		}
		else
		{
			_skillCast = ThreadPoolManager.getInstance().scheduleEffect(mut, 400);
		}
	}
	
	// Runs in the end of skill casting
	public void onMagicHitTimer(MagicUseTask mut)
	{
		final L2Skill skill = mut.getSkill();
		final L2Object[] targets = mut.getTargets();
		
		if ((skill == null) || (targets == null))
		{
			abortCast();
			return;
		}
		
		if (getFusionSkill() != null)
		{
			if (mut.isSimultaneous())
			{
				_skillCast2 = null;
				setIsCastingSimultaneouslyNow(false);
			}
			else
			{
				_skillCast = null;
				setIsCastingNow(false);
			}
			getFusionSkill().onCastAbort();
			notifyQuestEventSkillFinished(skill, targets[0]);
			return;
		}
		L2Effect mog = getFirstEffect(L2EffectType.SIGNET_GROUND);
		if (mog != null)
		{
			if (mut.isSimultaneous())
			{
				_skillCast2 = null;
				setIsCastingSimultaneouslyNow(false);
			}
			else
			{
				_skillCast = null;
				setIsCastingNow(false);
			}
			mog.exit();
			notifyQuestEventSkillFinished(skill, targets[0]);
			return;
		}
		
		try
		{
			// Go through targets table
			for (L2Object tgt : targets)
			{
				if (tgt.isPlayable())
				{
					L2Character target = (L2Character) tgt;
					
					if (skill.getSkillType() == L2SkillType.BUFF)
					{
						SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
						smsg.addSkillName(skill);
						target.sendPacket(smsg);
					}
					
					if (isPlayer() && target.isSummon())
					{
						((L2Summon) target).updateAndBroadcastStatus(1);
					}
				}
			}
			
			rechargeShots(skill.useSoulShot(), skill.useSpiritShot());
			
			final StatusUpdate su = new StatusUpdate(this);
			boolean isSendStatus = false;
			
			// Consume MP of the L2Character and Send the Server->Client packet StatusUpdate with current HP and MP to all other L2PcInstance to inform
			double mpConsume = getStat().getMpConsume(skill);
			
			if (mpConsume > 0)
			{
				if (mpConsume > getCurrentMp())
				{
					sendPacket(SystemMessageId.NOT_ENOUGH_MP);
					abortCast();
					return;
				}
				
				getStatus().reduceMp(mpConsume);
				su.addAttribute(StatusUpdate.CUR_MP, (int) getCurrentMp());
				isSendStatus = true;
			}
			
			// Consume HP if necessary and Send the Server->Client packet StatusUpdate with current HP and MP to all other L2PcInstance to inform
			if (skill.getHpConsume() > 0)
			{
				double consumeHp = skill.getHpConsume();
				
				if (consumeHp >= getCurrentHp())
				{
					sendPacket(SystemMessageId.NOT_ENOUGH_HP);
					abortCast();
					return;
				}
				
				getStatus().reduceHp(consumeHp, this, true);
				
				su.addAttribute(StatusUpdate.CUR_HP, (int) getCurrentHp());
				isSendStatus = true;
			}
			
			// Consume CP if necessary and Send the Server->Client packet StatusUpdate with current CP/HP and MP to all other L2PcInstance to inform
			if (skill.getCpConsume() > 0)
			{
				double consumeCp;
				
				consumeCp = skill.getCpConsume();
				if ((consumeCp + 1) >= getCurrentHp())
				{
					consumeCp = getCurrentHp() - 1.0;
				}
				
				getStatus().reduceCp((int) consumeCp);
				su.addAttribute(StatusUpdate.CUR_CP, (int) getCurrentCp());
				isSendStatus = true;
			}
			
			// Send a Server->Client packet StatusUpdate with MP modification to the L2PcInstance
			if (isSendStatus)
			{
				sendPacket(su);
			}
			
			if (isPlayer())
			{
				// Consume Charges
				if (skill.getChargeConsume() > 0)
				{
					getActingPlayer().decreaseCharges(skill.getChargeConsume());
				}
				
				// Consume Souls if necessary
				if (skill.getMaxSoulConsumeCount() > 0)
				{
					if (!getActingPlayer().decreaseSouls(skill.getMaxSoulConsumeCount(), skill))
					{
						abortCast();
						return;
					}
				}
				
				if (ReunionEvents.isInEvent((L2PcInstance) this))
				{
					ReunionEvents.onUseSkill((L2PcInstance) this, skill);
				}
			}
			
			// Launch the magic skill in order to calculate its effects
			callSkill(mut.getSkill(), mut.getTargets());
		}
		catch (NullPointerException e)
		{
			_log.warn(String.valueOf(e));
		}
		
		if (mut.getSkillTime() > 0)
		{
			mut.setCount(mut.getCount() + 1);
			if (mut.getCount() < skill.getHitCounts())
			{
				int skillTime = (mut.getSkillTime() * skill.getHitTimings()[mut.getCount()]) / 100;
				if (mut.isSimultaneous())
				{
					_skillCast2 = ThreadPoolManager.getInstance().scheduleEffect(mut, skillTime);
				}
				else
				{
					_skillCast = ThreadPoolManager.getInstance().scheduleEffect(mut, skillTime);
				}
				return;
			}
		}
		
		mut.setPhase(3);
		if (mut.getSkillTime() == 0)
		{
			onMagicFinalizer(mut);
		}
		else
		{
			if (mut.isSimultaneous())
			{
				_skillCast2 = ThreadPoolManager.getInstance().scheduleEffect(mut, 0);
			}
			else
			{
				_skillCast = ThreadPoolManager.getInstance().scheduleEffect(mut, 0);
			}
		}
	}
	
	// Runs after skillTime
	public void onMagicFinalizer(MagicUseTask mut)
	{
		if (mut.isSimultaneous())
		{
			_skillCast2 = null;
			setIsCastingSimultaneouslyNow(false);
			return;
		}
		
		_skillCast = null;
		setIsCastingNow(false);
		_castInterruptTime = 0;
		
		final L2Skill skill = mut.getSkill();
		final L2Object target = mut.getTargets().length > 0 ? mut.getTargets()[0] : null;
		
		// On each repeat recharge shots before cast.
		if (mut.getCount() > 0)
		{
			rechargeShots(mut.getSkill().useSoulShot(), mut.getSkill().useSpiritShot());
		}
		
		// Attack target after skill use
		if ((skill.nextActionIsAttack()) && (getTarget() instanceof L2Character) && (getTarget() != this) && (target != null) && (getTarget() == target) && target.canBeAttacked())
		{
			if ((getAI().getNextIntention() == null) || (getAI().getNextIntention().getCtrlIntention() != CtrlIntention.AI_INTENTION_MOVE_TO))
			{
				getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
			}
		}
		if (skill.isOffensive() && !(skill.getSkillType() == L2SkillType.UNLOCK) && !(skill.getSkillType() == L2SkillType.DELUXE_KEY_UNLOCK))
		{
			getAI().clientStartAutoAttack();
		}
		
		// Notify the AI of the L2Character with EVT_FINISH_CASTING
		getAI().notifyEvent(CtrlEvent.EVT_FINISH_CASTING);
		
		notifyQuestEventSkillFinished(skill, target);
		
		// If character is a player, then wipe their current cast state and check if a skill is queued.
		// If there is a queued skill, launch it and wipe the queue.
		if (isPlayer())
		{
			L2PcInstance currPlayer = getActingPlayer();
			SkillDat queuedSkill = currPlayer.getQueuedSkill();
			
			currPlayer.setCurrentSkill(null, false, false);
			
			if (queuedSkill != null)
			{
				currPlayer.setQueuedSkill(null, false, false);
				
				// DON'T USE : Recursive call to useMagic() method
				// currPlayer.useMagic(queuedSkill.getSkill(), queuedSkill.isCtrlPressed(), queuedSkill.isShiftPressed());
				ThreadPoolManager.getInstance().executeGeneral(new QueuedMagicUseTask(currPlayer, queuedSkill.getSkill(), queuedSkill.isCtrlPressed(), queuedSkill.isShiftPressed()));
			}
		}
	}
	
	// Quest event ON_SPELL_FNISHED
	protected void notifyQuestEventSkillFinished(L2Skill skill, L2Object target)
	{
		
	}
	
	/**
	 * Gets the disabled skills map.
	 * @return the disabled skills map
	 */
	public Map<Integer, Long> getDisabledSkills()
	{
		return _disabledSkills;
	}
	
	/**
	 * Enables a skill.
	 * @param skill the skill to enable
	 */
	public void enableSkill(L2Skill skill)
	{
		if ((skill == null) || (_disabledSkills == null))
		{
			return;
		}
		
		_disabledSkills.remove(Integer.valueOf(skill.getReuseHashCode()));
	}
	
	/**
	 * Disables a skill for a given time.<br>
	 * If delay is lesser or equal than zero, skill will be disabled "forever".
	 * @param skill the skill to disable
	 * @param delay delay in milliseconds
	 */
	public void disableSkill(L2Skill skill, long delay)
	{
		if (skill == null)
		{
			return;
		}
		
		if (_disabledSkills == null)
		{
			synchronized (this)
			{
				if (_disabledSkills == null)
				{
					_disabledSkills = new ConcurrentHashMap<>();
				}
			}
		}
		
		_disabledSkills.put(skill.getReuseHashCode(), delay > 0 ? System.currentTimeMillis() + delay : Long.MAX_VALUE);
	}
	
	/**
	 * Removes all the disabled skills.
	 */
	public synchronized final void resetDisabledSkills()
	{
		if (_disabledSkills != null)
		{
			_disabledSkills.clear();
		}
	}
	
	/**
	 * <B><U>Concept</U>:</B><br>
	 * All skills disabled are identified by their reuse hashcodes in <B>_disabledSkills</B> of the L2Character
	 * @param skill The L2Skill to check
	 * @return true if a skill is disabled.
	 */
	public boolean isSkillDisabled(L2Skill skill)
	{
		if (skill == null)
		{
			return true;
		}
		
		return isSkillDisabled(skill.getReuseHashCode());
	}
	
	/**
	 * Verifies if the skill is disabled.
	 * @param hashCode the skill hash code
	 * @return {@code true} if the skill is disabled, {@code false} otherwise
	 */
	public boolean isSkillDisabled(int hashCode)
	{
		if (isAllSkillsDisabled())
		{
			return true;
		}
		
		if (_disabledSkills == null)
		{
			return false;
		}
		
		final Long stamp = _disabledSkills.get(hashCode);
		if (stamp == null)
		{
			return false;
		}
		
		if (stamp < System.currentTimeMillis())
		{
			_disabledSkills.remove(hashCode);
			return false;
		}
		return true;
	}
	
	/**
	 * Disables all skills.
	 */
	public void disableAllSkills()
	{
		_allSkillsDisabled = true;
	}
	
	/**
	 * Enables all skills, except those under reuse time or previously disabled.
	 */
	public void enableAllSkills()
	{
		_allSkillsDisabled = false;
	}
	
	/**
	 * Launch the magic skill and calculate its effects on each target contained in the targets table.
	 * @param skill The L2Skill to use
	 * @param targets The table of L2Object targets
	 */
	public void callSkill(L2Skill skill, L2Object[] targets)
	{
		try
		{
			// Get the skill handler corresponding to the skill type (PDAM, MDAM, SWEEP...) started in gameserver
			ISkillHandler handler = SkillHandler.getInstance().getHandler(skill.getSkillType());
			L2Weapon activeWeapon = getActiveWeaponItem();
			
			// Check if the toggle skill effects are already in progress on the L2Character
			if (skill.isToggle() && (getFirstEffect(skill.getId()) != null))
			{
				return;
			}
			
			// Initial checks
			for (L2Object trg : targets)
			{
				if (trg instanceof L2Character)
				{
					// Set some values inside target's instance for later use
					L2Character target = (L2Character) trg;
					
					// Check Raidboss attack and
					// check buffing chars who attack raidboss. Results in mute.
					L2Character targetsAttackTarget = null;
					L2Character targetsCastTarget = null;
					if (target.hasAI())
					{
						targetsAttackTarget = target.getAI().getAttackTarget();
						targetsCastTarget = target.getAI().getCastTarget();
					}
					if (!Config.RAID_DISABLE_CURSE && ((target.isRaid() && target.giveRaidCurse() && (getLevel() > (target.getLevel() + 8))) || (!skill.isOffensive() && (targetsAttackTarget != null) && targetsAttackTarget.isRaid() && targetsAttackTarget.giveRaidCurse() && targetsAttackTarget.getAttackByList().contains(target) // has
																																																																																		// attacked
																																																																																		// raid
					&& (getLevel() > (targetsAttackTarget.getLevel() + 8))) || (!skill.isOffensive() && (targetsCastTarget != null) && targetsCastTarget.isRaid() && targetsCastTarget.giveRaidCurse() && targetsCastTarget.getAttackByList().contains(target) // has attacked raid
					&& (getLevel() > (targetsCastTarget.getLevel() + 8)))))
					{
						if (skill.isMagic())
						{
							L2Skill tempSkill = SkillData.FrequentSkill.RAID_CURSE.getSkill();
							if (tempSkill != null)
							{
								abortAttack();
								abortCast();
								getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
								tempSkill.getEffects(target, this);
							}
							
							_log.warn("Skill 4215 at level 1 is missing in DP.");
						}
						else
						{
							L2Skill tempSkill = SkillData.FrequentSkill.RAID_CURSE2.getSkill();
							if (tempSkill != null)
							{
								abortAttack();
								abortCast();
								getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
								tempSkill.getEffects(target, this);
							}
							
							_log.warn("Skill 4515 at level 1 is missing in DP.");
						}
						return;
					}
					
					// Check if over-hit is possible
					if (skill.isOverhit())
					{
						if (target.isAttackable())
						{
							((L2Attackable) target).overhitEnabled(true);
						}
					}
					
					if (!skill.isStatic())
					{
						if ((activeWeapon != null) && !target.isDead())
						{
							if ((activeWeapon.getSkillEffects(this, target, skill).length > 0) && isPlayer())
							{
								SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_BEEN_ACTIVATED);
								sm.addSkillName(skill);
								sendPacket(sm);
							}
						}
						
						if (_chanceSkills != null)
						{
							_chanceSkills.onSkillHit(target, skill, false);
						}
						
						if (target.getChanceSkills() != null)
						{
							target.getChanceSkills().onSkillHit(this, skill, true);
						}
						
						if (_triggerSkills != null)
						{
							for (OptionsSkillHolder holder : _triggerSkills.values())
							{
								if ((skill.isMagic() && (holder.getSkillType() == OptionsSkillType.MAGIC)) || (skill.isPhysical() && (holder.getSkillType() == OptionsSkillType.ATTACK)))
								{
									if (Rnd.get(100) < holder.getChance())
									{
										makeTriggerCast(holder.getSkill(), target);
									}
								}
							}
						}
					}
				}
			}
			
			// Launch the magic skill and calculate its effects
			if (handler != null)
			{
				handler.useSkill(this, skill, targets);
			}
			else
			{
				skill.useSkill(this, targets);
			}
			
			L2PcInstance player = getActingPlayer();
			if (player != null)
			{
				for (L2Object target : targets)
				{
					// EVT_ATTACKED and PvPStatus
					if (target instanceof L2Character)
					{
						if (skill.isOffensive())
						{
							if (target.isPlayer() || target.isSummon() || target.isTrap())
							{
								// Signets are a special case, casted on target_self but don't harm self
								if ((skill.getSkillType() != L2SkillType.SIGNET) && (skill.getSkillType() != L2SkillType.SIGNET_CASTTIME))
								{
									if (target.isPlayer())
									{
										target.getActingPlayer().getAI().clientStartAutoAttack();
									}
									else if (target.isSummon() && ((L2Character) target).hasAI())
									{
										L2PcInstance owner = ((L2Summon) target).getOwner();
										if (owner != null)
										{
											owner.getAI().clientStartAutoAttack();
										}
									}
									// attack of the own pet does not flag player
									// triggering trap not flag trap owner
									if ((player.getSummon() != target) && !isTrap())
									{
										player.updatePvPStatus((L2Character) target);
									}
								}
							}
							else if (target.isAttackable())
							{
								switch (skill.getId())
								{
									case 51: // Lure
									case 511: // Temptation
										break;
									default:
										// add attacker into list
										((L2Character) target).addAttackerToAttackByList(this);
								}
							}
							// notify target AI about the attack
							if (((L2Character) target).hasAI())
							{
								switch (skill.getSkillType())
								{
									case AGGREDUCE:
									case AGGREDUCE_CHAR:
									case AGGREMOVE:
										break;
									default:
										((L2Character) target).getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, this);
								}
							}
						}
						else
						{
							if (target.isPlayer())
							{
								// Casting non offensive skill on player with pvp flag set or with karma
								if (!(target.equals(this) || target.equals(player)) && ((target.getActingPlayer().getPvpFlag() > 0) || (target.getActingPlayer().getKarma() > 0)))
								{
									player.updatePvPStatus();
								}
							}
							else if (target.isAttackable())
							{
								switch (skill.getSkillType())
								{
									case SUMMON:
									case UNLOCK:
									case DELUXE_KEY_UNLOCK:
									case UNLOCK_SPECIAL:
										break;
									default:
										player.updatePvPStatus();
								}
							}
						}
					}
				}
				
				// Mobs in range 1000 see spell
				Collection<L2Object> objs = player.getKnownList().getKnownObjects().values();
				for (L2Object spMob : objs)
				{
					if ((spMob != null) && spMob.isNpc())
					{
						final L2Npc npcMob = (L2Npc) spMob;
						if ((npcMob.isInsideRadius(player, 1000, true, true)) && (npcMob.getTemplate().getEventQuests(QuestEventType.ON_SKILL_SEE) != null))
						{
							for (Quest quest : npcMob.getTemplate().getEventQuests(QuestEventType.ON_SKILL_SEE))
							{
								quest.notifySkillSee(npcMob, player, skill, targets, isSummon());
							}
						}
						
						// On Skill See logic
						if (npcMob.isAttackable())
						{
							final L2Attackable attackable = (L2Attackable) npcMob;
							
							int skillEffectPoint = skill.getAggroPoints();
							
							if (player.hasSummon())
							{
								if ((targets.length == 1) && Util.contains(targets, player.getSummon()))
								{
									skillEffectPoint = 0;
								}
								
								if (skillEffectPoint > 0)
								{
									if (attackable.hasAI() && (attackable.getAI().getIntention() == AI_INTENTION_ATTACK))
									{
										L2Object npcTarget = attackable.getTarget();
										for (L2Object skillTarget : targets)
										{
											if ((npcTarget == skillTarget) || (npcMob == skillTarget))
											{
												L2Character originalCaster = isSummon() ? getSummon() : player;
												attackable.addDamageHate(originalCaster, 0, (skillEffectPoint * 150) / (attackable.getLevel() + 7));
											}
										}
									}
								}
							}
						}
					}
				}
			}
			// Notify AI
			if (skill.isOffensive())
			{
				switch (skill.getSkillType())
				{
					case AGGREDUCE:
					case AGGREDUCE_CHAR:
					case AGGREMOVE:
						break;
					default:
						for (L2Object target : targets)
						{
							if ((target instanceof L2Character) && ((L2Character) target).hasAI())
							{
								// notify target AI about the attack
								((L2Character) target).getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, this);
							}
						}
						break;
				}
			}
		}
		catch (Exception e)
		{
			_log.warn(getClass().getSimpleName() + ": callSkill() failed.", e);
		}
	}
	
	/**
	 * @param target
	 * @return True if the L2Character is behind the target and can't be seen.
	 */
	public boolean isBehind(L2Object target)
	{
		double angleChar, angleTarget, angleDiff, maxAngleDiff = 60;
		
		if (target == null)
		{
			return false;
		}
		
		if (target instanceof L2Character)
		{
			L2Character target1 = (L2Character) target;
			angleChar = Util.calculateAngleFrom(this, target1);
			angleTarget = Util.convertHeadingToDegree(target1.getHeading());
			angleDiff = angleChar - angleTarget;
			if (angleDiff <= (-360 + maxAngleDiff))
			{
				angleDiff += 360;
			}
			if (angleDiff >= (360 - maxAngleDiff))
			{
				angleDiff -= 360;
			}
			if (Math.abs(angleDiff) <= maxAngleDiff)
			{
				return true;
			}
		}
		else
		{
			_log.info("isBehindTarget's target not an L2 Character.");
		}
		return false;
	}
	
	public boolean isBehindTarget()
	{
		return isBehind(getTarget());
	}
	
	/**
	 * @param target
	 * @return True if the target is facing the L2Character.
	 */
	public boolean isInFrontOf(L2Character target)
	{
		double angleChar, angleTarget, angleDiff, maxAngleDiff = 60;
		if (target == null)
		{
			return false;
		}
		
		angleTarget = Util.calculateAngleFrom(target, this);
		angleChar = Util.convertHeadingToDegree(target.getHeading());
		angleDiff = angleChar - angleTarget;
		if (angleDiff <= (-360 + maxAngleDiff))
		{
			angleDiff += 360;
		}
		if (angleDiff >= (360 - maxAngleDiff))
		{
			angleDiff -= 360;
		}
		return Math.abs(angleDiff) <= maxAngleDiff;
	}
	
	/**
	 * @param target
	 * @param maxAngle
	 * @return true if target is in front of L2Character (shield def etc)
	 */
	public boolean isFacing(L2Object target, int maxAngle)
	{
		double angleChar, angleTarget, angleDiff, maxAngleDiff;
		if (target == null)
		{
			return false;
		}
		maxAngleDiff = maxAngle / 2.;
		angleTarget = Util.calculateAngleFrom(this, target);
		angleChar = Util.convertHeadingToDegree(getHeading());
		angleDiff = angleChar - angleTarget;
		if (angleDiff <= (-360 + maxAngleDiff))
		{
			angleDiff += 360;
		}
		if (angleDiff >= (360 - maxAngleDiff))
		{
			angleDiff -= 360;
		}
		return Math.abs(angleDiff) <= maxAngleDiff;
	}
	
	public boolean isInFrontOfTarget()
	{
		L2Object target = getTarget();
		if (target instanceof L2Character)
		{
			return isInFrontOf((L2Character) target);
		}
		return false;
	}
	
	/**
	 * @return the Level Modifier ((level + 89) / 100).
	 */
	public double getLevelMod()
	{
		return ((getLevel() + 89) / 100d);
	}
	
	public final void setSkillCast(Future<?> newSkillCast)
	{
		_skillCast = newSkillCast;
	}
	
	/**
	 * Sets _isCastingNow to true and _castInterruptTime is calculated from end time (ticks)
	 * @param newSkillCastEndTick
	 */
	public final void forceIsCasting(int newSkillCastEndTick)
	{
		setIsCastingNow(true);
		// for interrupt -400 ms
		_castInterruptTime = newSkillCastEndTick - 4;
	}
	
	private boolean _AIdisabled = false;
	
	public void updatePvPFlag(int value)
	{
		// Overridden in L2PcInstance
	}
	
	/**
	 * @return a multiplier based on weapon random damage
	 */
	public final double getRandomDamageMultiplier()
	{
		L2Weapon activeWeapon = getActiveWeaponItem();
		int random;
		
		if (activeWeapon != null)
		{
			random = activeWeapon.getRandomDamage();
		}
		else
		{
			random = 5 + (int) Math.sqrt(getLevel());
		}
		
		return (1 + ((double) Rnd.get(0 - random, random) / 100));
	}
	
	public int getAttackEndTime()
	{
		return _attackEndTime;
	}
	
	public int getBowAttackEndTime()
	{
		return _disableBowAttackEndTime;
	}
	
	/**
	 * Not Implemented.
	 * @return
	 */
	public abstract int getLevel();
	
	// Stat - NEED TO REMOVE ONCE L2CHARSTAT IS COMPLETE
	public final double calcStat(Stats stat, double init, L2Character target, L2Skill skill)
	{
		return getStat().calcStat(stat, init, target, skill);
	}
	
	public int getAccuracy()
	{
		return getStat().getAccuracy();
	}
	
	public final float getAttackSpeedMultiplier()
	{
		return getStat().getAttackSpeedMultiplier();
	}
	
	public int getCON()
	{
		return getStat().getCON();
	}
	
	public int getDEX()
	{
		return getStat().getDEX();
	}
	
	public final double getCriticalDmg(L2Character target, double init)
	{
		return getStat().getCriticalDmg(target, init);
	}
	
	public int getCriticalHit(L2Character target, L2Skill skill)
	{
		return getStat().getCriticalHit(target, skill);
	}
	
	public int getEvasionRate(L2Character target)
	{
		return getStat().getEvasionRate(target);
	}
	
	public int getINT()
	{
		return getStat().getINT();
	}
	
	public final int getMagicalAttackRange(L2Skill skill)
	{
		return getStat().getMagicalAttackRange(skill);
	}
	
	public final int getMaxCp()
	{
		return getStat().getMaxCp();
	}
	
	public final int getMaxRecoverableCp()
	{
		return getStat().getMaxRecoverableCp();
	}
	
	public int getMAtk(L2Character target, L2Skill skill)
	{
		return getStat().getMAtk(target, skill);
	}
	
	public int getMAtkSpd()
	{
		return getStat().getMAtkSpd();
	}
	
	public int getMaxMp()
	{
		return getStat().getMaxMp();
	}
	
	public int getMaxRecoverableMp()
	{
		return getStat().getMaxRecoverableMp();
	}
	
	public int getMaxHp()
	{
		return getStat().getMaxHp();
	}
	
	public int getMaxRecoverableHp()
	{
		return getStat().getMaxRecoverableHp();
	}
	
	public final int getMCriticalHit(L2Character target, L2Skill skill)
	{
		return getStat().getMCriticalHit(target, skill);
	}
	
	public int getMDef(L2Character target, L2Skill skill)
	{
		return getStat().getMDef(target, skill);
	}
	
	public int getMEN()
	{
		return getStat().getMEN();
	}
	
	public double getMReuseRate(L2Skill skill)
	{
		return getStat().getMReuseRate(skill);
	}
	
	public float getMovementSpeedMultiplier()
	{
		return getStat().getMovementSpeedMultiplier();
	}
	
	public int getPAtk(L2Character target)
	{
		return getStat().getPAtk(target);
	}
	
	public double getPAtkAnimals(L2Character target)
	{
		return getStat().getPAtkAnimals(target);
	}
	
	public double getPAtkDragons(L2Character target)
	{
		return getStat().getPAtkDragons(target);
	}
	
	public double getPAtkInsects(L2Character target)
	{
		return getStat().getPAtkInsects(target);
	}
	
	public double getPAtkMonsters(L2Character target)
	{
		return getStat().getPAtkMonsters(target);
	}
	
	public double getPAtkPlants(L2Character target)
	{
		return getStat().getPAtkPlants(target);
	}
	
	public double getPAtkGiants(L2Character target)
	{
		return getStat().getPAtkGiants(target);
	}
	
	public double getPAtkMagicCreatures(L2Character target)
	{
		return getStat().getPAtkMagicCreatures(target);
	}
	
	public double getPDefAnimals(L2Character target)
	{
		return getStat().getPDefAnimals(target);
	}
	
	public double getPDefDragons(L2Character target)
	{
		return getStat().getPDefDragons(target);
	}
	
	public double getPDefInsects(L2Character target)
	{
		return getStat().getPDefInsects(target);
	}
	
	public double getPDefMonsters(L2Character target)
	{
		return getStat().getPDefMonsters(target);
	}
	
	public double getPDefPlants(L2Character target)
	{
		return getStat().getPDefPlants(target);
	}
	
	public double getPDefGiants(L2Character target)
	{
		return getStat().getPDefGiants(target);
	}
	
	public double getPDefMagicCreatures(L2Character target)
	{
		return getStat().getPDefMagicCreatures(target);
	}
	
	public int getPAtkSpd()
	{
		return getStat().getPAtkSpd();
	}
	
	public int getPDef(L2Character target)
	{
		return getStat().getPDef(target);
	}
	
	public final int getPhysicalAttackRange()
	{
		return getStat().getPhysicalAttackRange();
	}
	
	public int getRunSpeed()
	{
		return getStat().getRunSpeed();
	}
	
	public int getSwimRunSpeed()
	{
		return getStat().getSwimRunSpeed();
	}
	
	public final int getShldDef()
	{
		return getStat().getShldDef();
	}
	
	public int getSTR()
	{
		return getStat().getSTR();
	}
	
	public final int getWalkSpeed()
	{
		return getStat().getWalkSpeed();
	}
	
	public final int getSwimWalkSpeed()
	{
		return getStat().getSwimWalkSpeed();
	}
	
	public int getWIT()
	{
		return getStat().getWIT();
	}
	
	// Status - NEED TO REMOVE ONCE L2CHARTATUS IS COMPLETE
	public void addStatusListener(L2Character object)
	{
		getStatus().addStatusListener(object);
	}
	
	public void reduceCurrentHp(double i, L2Character attacker, L2Skill skill)
	{
		reduceCurrentHp(i, attacker, true, false, skill);
	}
	
	public void reduceCurrentHpByDOT(double i, L2Character attacker, L2Skill skill)
	{
		reduceCurrentHp(i, attacker, !skill.isToggle(), true, skill);
	}
	
	public void reduceCurrentHp(double i, L2Character attacker, boolean awake, boolean isDOT, L2Skill skill)
	{
		if (Config.L2JMOD_CHAMPION_ENABLE && isChampion() && (Config.L2JMOD_CHAMPION_HP != 0))
		{
			getStatus().reduceHp(i / Config.L2JMOD_CHAMPION_HP, attacker, awake, isDOT, false);
		}
		else
		{
			getStatus().reduceHp(i, attacker, awake, isDOT, false);
		}
	}
	
	public void reduceCurrentMp(double i)
	{
		getStatus().reduceMp(i);
	}
	
	public void removeStatusListener(L2Character object)
	{
		getStatus().removeStatusListener(object);
	}
	
	protected void stopHpMpRegeneration()
	{
		getStatus().stopHpMpRegeneration();
	}
	
	public final double getCurrentCp()
	{
		return getStatus().getCurrentCp();
	}
	
	public final void setCurrentCp(Double newCp)
	{
		setCurrentCp((double) newCp);
	}
	
	public final void setCurrentCp(double newCp)
	{
		getStatus().setCurrentCp(newCp);
	}
	
	public final double getCurrentHp()
	{
		return getStatus().getCurrentHp();
	}
	
	public final void setCurrentHp(double newHp)
	{
		getStatus().setCurrentHp(newHp);
	}
	
	public final void setCurrentHpMp(double newHp, double newMp)
	{
		getStatus().setCurrentHpMp(newHp, newMp);
	}
	
	public final double getCurrentMp()
	{
		return getStatus().getCurrentMp();
	}
	
	public final void setCurrentMp(Double newMp)
	{
		setCurrentMp((double) newMp);
	}
	
	public final void setCurrentMp(double newMp)
	{
		getStatus().setCurrentMp(newMp);
	}
	
	/**
	 * @return the max weight that the L2Character can load.
	 */
	public int getMaxLoad()
	{
		if (isPlayer() || isPet())
		{
			// Weight Limit = (CON Modifier*69000) * Skills
			// Source http://l2p.bravehost.com/weightlimit.html (May 2007)
			double baseLoad = Math.floor(BaseStats.CON.calcBonus(this) * 69000 * Config.ALT_WEIGHT_LIMIT);
			return (int) calcStat(Stats.WEIGHT_LIMIT, baseLoad, this, null);
		}
		return 0;
	}
	
	public int getBonusWeightPenalty()
	{
		if (isPlayer() || isPet())
		{
			return (int) calcStat(Stats.WEIGHT_PENALTY, 1, this, null);
		}
		return 0;
	}
	
	/**
	 * @return the current weight of the L2Character.
	 */
	public int getCurrentLoad()
	{
		if (isPlayer() || isPet())
		{
			return getInventory().getTotalWeight();
		}
		return 0;
	}
	
	public boolean isChampion()
	{
		return false;
	}
	
	/**
	 * Check player max buff count
	 * @return max buff count
	 */
	public int getMaxBuffCount()
	{
		final L2Effect effect = getFirstPassiveEffect(L2EffectType.ENLARGE_ABNORMAL_SLOT);
		return Config.BUFFS_MAX_AMOUNT + (effect == null ? 0 : (int) effect.calc());
	}
	
	/**
	 * Send system message about damage.
	 * @param target
	 * @param damage
	 * @param mcrit
	 * @param pcrit
	 * @param miss
	 */
	public void sendDamageMessage(L2Character target, int damage, boolean mcrit, boolean pcrit, boolean miss)
	{
		
	}
	
	public FusionSkill getFusionSkill()
	{
		return _fusionSkill;
	}
	
	public void setFusionSkill(FusionSkill fb)
	{
		_fusionSkill = fb;
	}
	
	public byte getAttackElement()
	{
		return getStat().getAttackElement();
	}
	
	public int getAttackElementValue(byte attackAttribute)
	{
		return getStat().getAttackElementValue(attackAttribute);
	}
	
	public int getDefenseElementValue(byte defenseAttribute)
	{
		return getStat().getDefenseElementValue(defenseAttribute);
	}
	
	public final void startPhysicalAttackMuted()
	{
		abortAttack();
	}
	
	public void disableCoreAI(boolean val)
	{
		_AIdisabled = val;
	}
	
	public boolean isCoreAIDisabled()
	{
		return _AIdisabled;
	}
	
	/**
	 * @return true
	 */
	public boolean giveRaidCurse()
	{
		return true;
	}
	
	/**
	 * Check if target is affected with special buff
	 * @see CharEffectList#isAffected(EffectFlag)
	 * @param flag int
	 * @return boolean
	 */
	public boolean isAffected(EffectFlag flag)
	{
		return _effects.isAffected(flag);
	}
	
	public void broadcastSocialAction(int id)
	{
		broadcastPacket(new SocialAction(getObjectId(), id));
	}
	
	public Team getTeam()
	{
		return _team;
	}
	
	public void setTeam(Team team)
	{
		_team = team;
	}
	
	public void addOverrideCond(PcCondOverride... excs)
	{
		for (PcCondOverride exc : excs)
		{
			_exceptions |= exc.getMask();
		}
	}
	
	public void removeOverridedCond(PcCondOverride... excs)
	{
		for (PcCondOverride exc : excs)
		{
			_exceptions &= ~exc.getMask();
		}
	}
	
	public boolean canOverrideCond(PcCondOverride excs)
	{
		return (_exceptions & excs.getMask()) == excs.getMask();
	}
	
	public void setOverrideCond(long masks)
	{
		_exceptions = masks;
	}
	
	public void setLethalable(boolean val)
	{
		_lethalable = val;
	}
	
	public boolean isLethalable()
	{
		return _lethalable;
	}
	
	public Map<Integer, OptionsSkillHolder> getTriggerSkills()
	{
		if (_triggerSkills == null)
		{
			synchronized (this)
			{
				if (_triggerSkills == null)
				{
					_triggerSkills = new FastMap<Integer, OptionsSkillHolder>().shared();
				}
			}
		}
		return _triggerSkills;
	}
	
	public void addTriggerSkill(OptionsSkillHolder holder)
	{
		getTriggerSkills().put(holder.getSkillId(), holder);
	}
	
	public void removeTriggerSkill(OptionsSkillHolder holder)
	{
		getTriggerSkills().remove(holder.getSkillId());
	}
	
	public void makeTriggerCast(L2Skill skill, L2Character target)
	{
		try
		{
			if (skill.checkCondition(this, target, false))
			{
				if (skill.triggersChanceSkill()) // skill will trigger another skill, but only if its not chance skill
				{
					skill = SkillData.getInstance().getInfo(skill.getTriggeredChanceId(), skill.getTriggeredChanceLevel());
					if ((skill == null) || (skill.getSkillType() == L2SkillType.NOTDONE))
					{
						return;
					}
					// We change skill to new one, we should verify conditions for new one
					if (!skill.checkCondition(this, target, false))
					{
						return;
					}
				}
				
				if (isSkillDisabled(skill))
				{
					return;
				}
				
				if (skill.getReuseDelay() > 0)
				{
					disableSkill(skill, skill.getReuseDelay());
				}
				
				final L2Object[] targets = skill.getTargetList(this, false, target);
				
				if (targets.length == 0)
				{
					return;
				}
				
				final L2Character firstTarget = (L2Character) targets[0];
				
				if (Config.ALT_VALIDATE_TRIGGER_SKILLS && isPlayable() && (firstTarget != null) && firstTarget.isPlayable())
				{
					final L2PcInstance player = getActingPlayer();
					if (!player.checkPvpSkill(firstTarget, skill, isSummon()))
					{
						return;
					}
				}
				
				final ISkillHandler handler = SkillHandler.getInstance().getHandler(skill.getSkillType());
				
				broadcastPacket(new MagicSkillLaunched(this, skill.getDisplayId(), skill.getLevel(), targets));
				broadcastPacket(new MagicSkillUse(this, firstTarget, skill.getDisplayId(), skill.getLevel(), 0, 0));
				// Launch the magic skill and calculate its effects
				// TODO: once core will support all possible effects, use effects (not handler)
				if (handler != null)
				{
					handler.useSkill(this, skill, targets);
				}
				else
				{
					skill.useSkill(this, targets);
				}
			}
		}
		catch (Exception e)
		{
			_log.warn(String.valueOf(e));
		}
	}
	
	/**
	 * Dummy method overriden in {@link L2PcInstance}
	 * @return {@code true} if player is on event, {@code false} otherwise.
	 */
	public boolean isOnEvent()
	{
		return false;
	}
	
	public int getClanId()
	{
		return 0;
	}
	
	public int getAllyId()
	{
		return 0;
	}
	
	/**
	 * Notifies to listeners that current character received damage.
	 * @param damage
	 * @param attacker
	 * @param skill
	 * @param critical
	 * @param damageOverTime
	 */
	public void notifyDamageReceived(double damage, L2Character attacker, L2Skill skill, boolean critical, boolean damageOverTime)
	{
		getEvents().onDamageReceived(damage, attacker, skill, critical, damageOverTime);
		attacker.getEvents().onDamageDealt(damage, this, skill, critical, damageOverTime);
	}
	
	public final boolean isInCategory(CategoryType type)
	{
		return CategoryData.getInstance().isInCategory(type, getId());
	}
	
	@Override
	public boolean isCharacter()
	{
		return true;
	}
	
	// User panel system start
	private boolean _ssAnimation = false;
	
	public boolean isSsAnimationBlocked()
	{
		return _ssAnimation;
	}
	
	public void setSsAnimation(boolean ssAnimation)
	{
		_ssAnimation = ssAnimation;
	}
	
	public void addInvulAgainst(SkillHolder holder)
	{
		final InvulSkillHolder invulHolder = getInvulAgainstSkills().get(holder.getSkillId());
		if (invulHolder != null)
		{
			invulHolder.increaseInstances();
			return;
		}
		getInvulAgainstSkills().put(holder.getSkillId(), new InvulSkillHolder(holder));
	}
	
	public void removeInvulAgainst(SkillHolder holder)
	{
		final InvulSkillHolder invulHolder = getInvulAgainstSkills().get(holder.getSkillId());
		if ((invulHolder != null) && (invulHolder.decreaseInstances() < 1))
		{
			getInvulAgainstSkills().remove(holder.getSkillId());
		}
	}
	
	public boolean isInvulAgainst(int skillId, int skillLvl)
	{
		if (_invulAgainst != null)
		{
			final SkillHolder holder = getInvulAgainstSkills().get(skillId);
			return ((holder != null) && ((holder.getSkillLvl() < 1) || (holder.getSkillLvl() == skillLvl)));
		}
		return false;
	}
	
	private Map<Integer, InvulSkillHolder> getInvulAgainstSkills()
	{
		if (_invulAgainst == null)
		{
			synchronized (this)
			{
				if (_invulAgainst == null)
				{
					return _invulAgainst = new ConcurrentHashMap<>();
				}
			}
		}
		return _invulAgainst;
	}
	
	public int getMinShopDistanceNPC()
	{
		return 0;
	}
	
	public int getMinShopDistancePlayer()
	{
		return 0;
	}
	
	public void setProtectedPlayer(boolean prot)
	{
		_protected = prot;
	}
	
	public boolean isProtected()
	{
		return _protected;
	}
	
	// ============================================== //
	// Premium Engine By L][Reunion Team //
	// ============================================== //
	private boolean _premiumService = false;
	
	public void setPremiumService(boolean premiumService)
	{
		_premiumService = premiumService;
	}
	
	public boolean isPremium()
	{
		return _premiumService;
	}
}
