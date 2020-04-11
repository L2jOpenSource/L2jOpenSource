/*
 * Copyright (C) 2004-2019 L2J Server
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
package com.l2jserver.gameserver.model.events;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.script.ScriptException;

import com.l2jserver.Config;
import com.l2jserver.gameserver.GameTimeController;
import com.l2jserver.gameserver.ai.CtrlIntention;
import com.l2jserver.gameserver.data.xml.impl.DoorData;
import com.l2jserver.gameserver.data.xml.impl.NpcData;
import com.l2jserver.gameserver.datatables.ItemTable;
import com.l2jserver.gameserver.enums.audio.IAudio;
import com.l2jserver.gameserver.enums.audio.Sound;
import com.l2jserver.gameserver.instancemanager.CastleManager;
import com.l2jserver.gameserver.instancemanager.FortManager;
import com.l2jserver.gameserver.instancemanager.InstanceManager;
import com.l2jserver.gameserver.instancemanager.ZoneManager;
import com.l2jserver.gameserver.model.L2Spawn;
import com.l2jserver.gameserver.model.Location;
import com.l2jserver.gameserver.model.actor.L2Attackable;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.L2Playable;
import com.l2jserver.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jserver.gameserver.model.actor.instance.L2MonsterInstance;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.actor.instance.L2TrapInstance;
import com.l2jserver.gameserver.model.actor.templates.L2NpcTemplate;
import com.l2jserver.gameserver.model.drops.GeneralDropItem;
import com.l2jserver.gameserver.model.drops.GroupedGeneralDropItem;
import com.l2jserver.gameserver.model.drops.IDropItem;
import com.l2jserver.gameserver.model.entity.Castle;
import com.l2jserver.gameserver.model.entity.Fort;
import com.l2jserver.gameserver.model.entity.Instance;
import com.l2jserver.gameserver.model.events.annotations.Id;
import com.l2jserver.gameserver.model.events.annotations.Ids;
import com.l2jserver.gameserver.model.events.annotations.NpcLevelRange;
import com.l2jserver.gameserver.model.events.annotations.NpcLevelRanges;
import com.l2jserver.gameserver.model.events.annotations.Priority;
import com.l2jserver.gameserver.model.events.annotations.Range;
import com.l2jserver.gameserver.model.events.annotations.Ranges;
import com.l2jserver.gameserver.model.events.annotations.RegisterEvent;
import com.l2jserver.gameserver.model.events.annotations.RegisterType;
import com.l2jserver.gameserver.model.events.impl.IBaseEvent;
import com.l2jserver.gameserver.model.events.impl.character.OnCreatureKill;
import com.l2jserver.gameserver.model.events.impl.character.OnCreatureZoneEnter;
import com.l2jserver.gameserver.model.events.impl.character.OnCreatureZoneExit;
import com.l2jserver.gameserver.model.events.impl.character.npc.OnNpcCanBeSeen;
import com.l2jserver.gameserver.model.events.impl.character.npc.OnNpcCreatureSee;
import com.l2jserver.gameserver.model.events.impl.character.npc.OnNpcEventReceived;
import com.l2jserver.gameserver.model.events.impl.character.npc.OnNpcFirstTalk;
import com.l2jserver.gameserver.model.events.impl.character.npc.OnNpcMoveFinished;
import com.l2jserver.gameserver.model.events.impl.character.npc.OnNpcMoveNodeArrived;
import com.l2jserver.gameserver.model.events.impl.character.npc.OnNpcMoveRouteFinished;
import com.l2jserver.gameserver.model.events.impl.character.npc.OnNpcSkillFinished;
import com.l2jserver.gameserver.model.events.impl.character.npc.OnNpcSkillSee;
import com.l2jserver.gameserver.model.events.impl.character.npc.OnNpcSpawn;
import com.l2jserver.gameserver.model.events.impl.character.npc.OnNpcTeleport;
import com.l2jserver.gameserver.model.events.impl.character.npc.attackable.OnAttackableAggroRangeEnter;
import com.l2jserver.gameserver.model.events.impl.character.npc.attackable.OnAttackableAttack;
import com.l2jserver.gameserver.model.events.impl.character.npc.attackable.OnAttackableFactionCall;
import com.l2jserver.gameserver.model.events.impl.character.npc.attackable.OnAttackableHate;
import com.l2jserver.gameserver.model.events.impl.character.npc.attackable.OnAttackableKill;
import com.l2jserver.gameserver.model.events.impl.character.player.OnPlayerLogin;
import com.l2jserver.gameserver.model.events.impl.character.player.OnPlayerLogout;
import com.l2jserver.gameserver.model.events.impl.character.player.OnPlayerProfessionCancel;
import com.l2jserver.gameserver.model.events.impl.character.player.OnPlayerProfessionChange;
import com.l2jserver.gameserver.model.events.impl.character.player.OnPlayerSkillLearn;
import com.l2jserver.gameserver.model.events.impl.character.player.OnPlayerSummonSpawn;
import com.l2jserver.gameserver.model.events.impl.character.player.OnPlayerSummonTalk;
import com.l2jserver.gameserver.model.events.impl.character.player.OnPlayerTutorialClientEvent;
import com.l2jserver.gameserver.model.events.impl.character.player.OnPlayerTutorialCmd;
import com.l2jserver.gameserver.model.events.impl.character.player.OnPlayerTutorialEvent;
import com.l2jserver.gameserver.model.events.impl.character.player.OnPlayerTutorialQuestionMark;
import com.l2jserver.gameserver.model.events.impl.character.trap.OnTrapAction;
import com.l2jserver.gameserver.model.events.impl.item.OnItemBypassEvent;
import com.l2jserver.gameserver.model.events.impl.item.OnItemTalk;
import com.l2jserver.gameserver.model.events.impl.olympiad.OnOlympiadMatchResult;
import com.l2jserver.gameserver.model.events.impl.sieges.castle.OnCastleSiegeFinish;
import com.l2jserver.gameserver.model.events.impl.sieges.castle.OnCastleSiegeOwnerChange;
import com.l2jserver.gameserver.model.events.impl.sieges.castle.OnCastleSiegeStart;
import com.l2jserver.gameserver.model.events.listeners.AbstractEventListener;
import com.l2jserver.gameserver.model.events.listeners.AnnotationEventListener;
import com.l2jserver.gameserver.model.events.listeners.ConsumerEventListener;
import com.l2jserver.gameserver.model.events.listeners.DummyEventListener;
import com.l2jserver.gameserver.model.events.listeners.FunctionEventListener;
import com.l2jserver.gameserver.model.events.listeners.RunnableEventListener;
import com.l2jserver.gameserver.model.events.returns.AbstractEventReturn;
import com.l2jserver.gameserver.model.events.returns.TerminateReturn;
import com.l2jserver.gameserver.model.holders.ItemHolder;
import com.l2jserver.gameserver.model.holders.SkillHolder;
import com.l2jserver.gameserver.model.interfaces.INamable;
import com.l2jserver.gameserver.model.interfaces.IPositionable;
import com.l2jserver.gameserver.model.itemcontainer.Inventory;
import com.l2jserver.gameserver.model.itemcontainer.PcInventory;
import com.l2jserver.gameserver.model.items.L2EtcItem;
import com.l2jserver.gameserver.model.items.L2Item;
import com.l2jserver.gameserver.model.items.instance.L2ItemInstance;
import com.l2jserver.gameserver.model.olympiad.Olympiad;
import com.l2jserver.gameserver.model.skills.Skill;
import com.l2jserver.gameserver.model.stats.Stats;
import com.l2jserver.gameserver.model.zone.L2ZoneType;
import com.l2jserver.gameserver.network.NpcStringId;
import com.l2jserver.gameserver.network.SystemMessageId;
import com.l2jserver.gameserver.network.serverpackets.ExShowScreenMessage;
import com.l2jserver.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jserver.gameserver.network.serverpackets.SpecialCamera;
import com.l2jserver.gameserver.network.serverpackets.StatusUpdate;
import com.l2jserver.gameserver.network.serverpackets.SystemMessage;
import com.l2jserver.gameserver.scripting.L2ScriptEngineManager;
import com.l2jserver.gameserver.scripting.ScriptManager;
import com.l2jserver.gameserver.util.MinionList;
import com.l2jserver.util.Rnd;
import com.l2jserver.util.Util;

/**
 * Abstract script.
 * @author KenM, UnAfraid, Zoey76
 */
public abstract class AbstractScript implements INamable
{
	public static final Logger _log = Logger.getLogger(AbstractScript.class.getName());
	private final Map<ListenerRegisterType, Set<Integer>> _registeredIds = new ConcurrentHashMap<>();
	private final List<AbstractEventListener> _listeners = new CopyOnWriteArrayList<>();
	private final File _scriptFile;
	private boolean _isActive;
	
	public AbstractScript()
	{
		_scriptFile = L2ScriptEngineManager.getInstance().getCurrentLoadingScript();
		initializeAnnotationListeners();
	}
	
	private void initializeAnnotationListeners()
	{
		final List<Integer> ids = new ArrayList<>();
		for (Method method : getClass().getMethods())
		{
			if (method.isAnnotationPresent(RegisterEvent.class) && method.isAnnotationPresent(RegisterType.class))
			{
				final RegisterEvent listener = method.getAnnotation(RegisterEvent.class);
				final RegisterType regType = method.getAnnotation(RegisterType.class);
				
				final ListenerRegisterType type = regType.value();
				final EventType eventType = listener.value();
				if (method.getParameterCount() != 1)
				{
					_log.log(Level.WARNING, getClass().getSimpleName() + ": Non properly defined annotation listener on method: " + method.getName() + " expected parameter count is 1 but found: " + method.getParameterCount());
					continue;
				}
				else if (!eventType.isEventClass(method.getParameterTypes()[0]))
				{
					_log.log(Level.WARNING, getClass().getSimpleName() + ": Non properly defined annotation listener on method: " + method.getName() + " expected parameter to be type of: " + eventType.getEventClass().getSimpleName() + " but found: " + method.getParameterTypes()[0].getSimpleName());
					continue;
				}
				else if (!eventType.isReturnClass(method.getReturnType()))
				{
					_log.log(Level.WARNING, getClass().getSimpleName() + ": Non properly defined annotation listener on method: " + method.getName() + " expected return type to be one of: " + Arrays.toString(eventType.getReturnClasses()) + " but found: " + method.getReturnType().getSimpleName());
					continue;
				}
				
				int priority = 0;
				
				// Clear the list
				ids.clear();
				
				// Scan for possible Id filters
				for (Annotation annotation : method.getAnnotations())
				{
					if (annotation instanceof Id)
					{
						final Id npc = (Id) annotation;
						for (int id : npc.value())
						{
							ids.add(id);
						}
					}
					else if (annotation instanceof Ids)
					{
						final Ids npcs = (Ids) annotation;
						for (Id npc : npcs.value())
						{
							for (int id : npc.value())
							{
								ids.add(id);
							}
						}
					}
					else if (annotation instanceof Range)
					{
						final Range range = (Range) annotation;
						if (range.from() > range.to())
						{
							_log.log(Level.WARNING, getClass().getSimpleName() + ": Wrong " + annotation.getClass().getSimpleName() + " from is higher then to!");
							continue;
						}
						
						for (int id = range.from(); id <= range.to(); id++)
						{
							ids.add(id);
						}
					}
					else if (annotation instanceof Ranges)
					{
						final Ranges ranges = (Ranges) annotation;
						for (Range range : ranges.value())
						{
							if (range.from() > range.to())
							{
								_log.log(Level.WARNING, getClass().getSimpleName() + ": Wrong " + annotation.getClass().getSimpleName() + " from is higher then to!");
								continue;
							}
							
							for (int id = range.from(); id <= range.to(); id++)
							{
								ids.add(id);
							}
						}
					}
					else if (annotation instanceof NpcLevelRange)
					{
						final NpcLevelRange range = (NpcLevelRange) annotation;
						if (range.from() > range.to())
						{
							_log.log(Level.WARNING, getClass().getSimpleName() + ": Wrong " + annotation.getClass().getSimpleName() + " from is higher then to!");
							continue;
						}
						else if (type != ListenerRegisterType.NPC)
						{
							_log.log(Level.WARNING, getClass().getSimpleName() + ": ListenerRegisterType " + type + " for " + annotation.getClass().getSimpleName() + " NPC is expected!");
							continue;
						}
						
						for (int level = range.from(); level <= range.to(); level++)
						{
							final List<L2NpcTemplate> templates = NpcData.getInstance().getAllOfLevel(level);
							templates.forEach(template -> ids.add(template.getId()));
						}
						
					}
					else if (annotation instanceof NpcLevelRanges)
					{
						final NpcLevelRanges ranges = (NpcLevelRanges) annotation;
						for (NpcLevelRange range : ranges.value())
						{
							if (range.from() > range.to())
							{
								_log.log(Level.WARNING, getClass().getSimpleName() + ": Wrong " + annotation.getClass().getSimpleName() + " from is higher then to!");
								continue;
							}
							else if (type != ListenerRegisterType.NPC)
							{
								_log.log(Level.WARNING, getClass().getSimpleName() + ": ListenerRegisterType " + type + " for " + annotation.getClass().getSimpleName() + " NPC is expected!");
								continue;
							}
							
							for (int level = range.from(); level <= range.to(); level++)
							{
								final List<L2NpcTemplate> templates = NpcData.getInstance().getAllOfLevel(level);
								templates.forEach(template -> ids.add(template.getId()));
							}
						}
					}
					else if (annotation instanceof Priority)
					{
						final Priority p = (Priority) annotation;
						priority = p.value();
					}
				}
				
				if (!ids.isEmpty())
				{
					_registeredIds.putIfAbsent(type, ConcurrentHashMap.newKeySet(ids.size()));
					
					_registeredIds.get(type).addAll(ids);
				}
				
				registerAnnotation(method, eventType, type, priority, ids);
			}
		}
	}
	
	public void setActive(boolean status)
	{
		_isActive = status;
	}
	
	public boolean isActive()
	{
		return _isActive;
	}
	
	public File getScriptFile()
	{
		return _scriptFile;
	}
	
	public boolean reload()
	{
		try
		{
			L2ScriptEngineManager.getInstance().executeScript(getScriptFile());
			return true;
		}
		catch (ScriptException e)
		{
			return false;
		}
	}
	
	/**
	 * Unloads all listeners registered by this class.
	 * @return {@code true}
	 */
	public boolean unload()
	{
		_listeners.forEach(AbstractEventListener::unregisterMe);
		_listeners.clear();
		return true;
	}
	
	public abstract ScriptManager<?> getManager();
	
	// ---------------------------------------------------------------------------------------------------------------------------
	
	/**
	 * Provides delayed (Depending on {@link com.l2jserver.gameserver.model.actor.L2Attackable#getOnKillDelay()}) callback operation when L2Attackable dies from a player.
	 * @param callback
	 * @param npcIds
	 * @return
	 */
	protected final List<AbstractEventListener> setAttackableKillId(Consumer<OnAttackableKill> callback, int... npcIds)
	{
		return registerConsumer(callback, EventType.ON_ATTACKABLE_KILL, ListenerRegisterType.NPC, npcIds);
	}
	
	/**
	 * Provides delayed (Depending on {@link com.l2jserver.gameserver.model.actor.L2Attackable#getOnKillDelay()}) callback operation when L2Attackable dies from a player.
	 * @param callback
	 * @param npcIds
	 * @return
	 */
	protected final List<AbstractEventListener> setAttackableKillId(Consumer<OnAttackableKill> callback, Collection<Integer> npcIds)
	{
		return registerConsumer(callback, EventType.ON_ATTACKABLE_KILL, ListenerRegisterType.NPC, npcIds);
	}
	
	// ---------------------------------------------------------------------------------------------------------------------------
	
	/**
	 * Provides instant callback operation when L2Attackable dies from a player with return type.
	 * @param callback
	 * @param npcIds
	 * @return
	 */
	protected final List<AbstractEventListener> addCreatureKillId(Function<OnCreatureKill, ? extends AbstractEventReturn> callback, int... npcIds)
	{
		return registerFunction(callback, EventType.ON_CREATURE_KILL, ListenerRegisterType.NPC, npcIds);
	}
	
	/**
	 * Provides instant callback operation when L2Attackable dies from a player.
	 * @param callback
	 * @param npcIds
	 * @return
	 */
	protected final List<AbstractEventListener> setCreatureKillId(Consumer<OnCreatureKill> callback, int... npcIds)
	{
		return registerConsumer(callback, EventType.ON_CREATURE_KILL, ListenerRegisterType.NPC, npcIds);
	}
	
	/**
	 * Provides instant callback operation when {@link L2Attackable} dies from a {@link L2PcInstance}.
	 * @param callback
	 * @param npcIds
	 * @return
	 */
	protected final List<AbstractEventListener> setCreatureKillId(Consumer<OnCreatureKill> callback, Collection<Integer> npcIds)
	{
		return registerConsumer(callback, EventType.ON_CREATURE_KILL, ListenerRegisterType.NPC, npcIds);
	}
	
	// ---------------------------------------------------------------------------------------------------------------------------
	
	/**
	 * Provides instant callback operation when {@link L2PcInstance} talk to {@link L2Npc} for first time.
	 * @param callback
	 * @param npcIds
	 * @return
	 */
	protected final List<AbstractEventListener> setNpcFirstTalkId(Consumer<OnNpcFirstTalk> callback, int... npcIds)
	{
		return registerConsumer(callback, EventType.ON_NPC_FIRST_TALK, ListenerRegisterType.NPC, npcIds);
	}
	
	/**
	 * Provides instant callback operation when {@link L2PcInstance} talk to {@link L2Npc} for first time.
	 * @param callback
	 * @param npcIds
	 * @return
	 */
	protected final List<AbstractEventListener> setNpcFirstTalkId(Consumer<OnNpcFirstTalk> callback, Collection<Integer> npcIds)
	{
		return registerConsumer(callback, EventType.ON_NPC_FIRST_TALK, ListenerRegisterType.NPC, npcIds);
	}
	
	// ---------------------------------------------------------------------------------------------------------------------------
	
	protected final List<AbstractEventListener> setPlayerTutorialEvent(Consumer<OnPlayerTutorialEvent> callback)
	{
		return registerConsumer(callback, EventType.ON_PLAYER_TUTORIAL_EVENT, ListenerRegisterType.GLOBAL);
	}
	
	protected final List<AbstractEventListener> setPlayerTutorialClientEvent(Consumer<OnPlayerTutorialClientEvent> callback)
	{
		return registerConsumer(callback, EventType.ON_PLAYER_TUTORIAL_CLIENT_EVENT, ListenerRegisterType.GLOBAL);
	}
	
	protected final List<AbstractEventListener> setPlayerTutorialQuestionMark(Consumer<OnPlayerTutorialQuestionMark> callback)
	{
		return registerConsumer(callback, EventType.ON_PLAYER_TUTORIAL_QUESTION_MARK, ListenerRegisterType.GLOBAL);
	}
	
	protected final List<AbstractEventListener> setPlayerTutorialCmd(Consumer<OnPlayerTutorialCmd> callback)
	{
		return registerConsumer(callback, EventType.ON_PLAYER_TUTORIAL_CMD, ListenerRegisterType.GLOBAL);
	}
	
	// ---------------------------------------------------------------------------------------------------------------------------
	
	/**
	 * Provides instant callback operation when {@link L2PcInstance} talk to {@link L2Npc}.
	 * @param npcIds
	 * @return
	 */
	protected final List<AbstractEventListener> setNpcTalkId(Collection<Integer> npcIds)
	{
		return registerDummy(EventType.ON_NPC_TALK, ListenerRegisterType.NPC, npcIds);
	}
	
	/**
	 * Provides instant callback operation when {@link L2PcInstance} talk to {@link L2Npc}.
	 * @param npcIds
	 * @return
	 */
	protected final List<AbstractEventListener> setNpcTalkId(int... npcIds)
	{
		return registerDummy(EventType.ON_NPC_TALK, ListenerRegisterType.NPC, npcIds);
	}
	
	// ---------------------------------------------------------------------------------------------------------------------------
	
	/**
	 * Provides instant callback operation when teleport {@link L2Npc}.
	 * @param callback
	 * @param npcIds
	 * @return
	 */
	protected final List<AbstractEventListener> setNpcTeleportId(Consumer<OnNpcTeleport> callback, Collection<Integer> npcIds)
	{
		return registerConsumer(callback, EventType.ON_NPC_TELEPORT, ListenerRegisterType.NPC, npcIds);
	}
	
	/**
	 * Provides instant callback operation when teleport {@link L2Npc}.
	 * @param callback
	 * @param npcIds
	 * @return
	 */
	protected final List<AbstractEventListener> setNpcTeleportId(Consumer<OnNpcTeleport> callback, int... npcIds)
	{
		return registerConsumer(callback, EventType.ON_NPC_TELEPORT, ListenerRegisterType.NPC, npcIds);
	}
	
	// ---------------------------------------------------------------------------------------------------------------------------
	
	/**
	 * Provides instant callback operation when {@link L2PcInstance} talk to {@link L2Npc} and must receive quest state.
	 * @param npcIds
	 * @return
	 */
	protected final List<AbstractEventListener> setNpcQuestStartId(int... npcIds)
	{
		return registerDummy(EventType.ON_NPC_QUEST_START, ListenerRegisterType.NPC, npcIds);
	}
	
	/**
	 * Provides instant callback operation when {@link L2PcInstance} talk to {@link L2Npc} and must receive quest state.
	 * @param npcIds
	 * @return
	 */
	protected final List<AbstractEventListener> setNpcQuestStartId(Collection<Integer> npcIds)
	{
		return registerDummy(EventType.ON_NPC_QUEST_START, ListenerRegisterType.NPC, npcIds);
	}
	
	// ---------------------------------------------------------------------------------------------------------------------------
	
	/**
	 * Provides instant callback operation when L2Npc sees skill from a player.
	 * @param callback
	 * @param npcIds
	 * @return
	 */
	protected final List<AbstractEventListener> setNpcSkillSeeId(Consumer<OnNpcSkillSee> callback, int... npcIds)
	{
		return registerConsumer(callback, EventType.ON_NPC_SKILL_SEE, ListenerRegisterType.NPC, npcIds);
	}
	
	/**
	 * Provides instant callback operation when L2Npc sees skill from a player.
	 * @param callback
	 * @param npcIds
	 * @return
	 */
	protected final List<AbstractEventListener> setNpcSkillSeeId(Consumer<OnNpcSkillSee> callback, Collection<Integer> npcIds)
	{
		return registerConsumer(callback, EventType.ON_NPC_SKILL_SEE, ListenerRegisterType.NPC, npcIds);
	}
	
	// ---------------------------------------------------------------------------------------------------------------------------
	
	/**
	 * Provides instant callback operation when L2Npc casts skill on a player.
	 * @param callback
	 * @param npcIds
	 * @return
	 */
	protected final List<AbstractEventListener> setNpcSkillFinishedId(Consumer<OnNpcSkillFinished> callback, int... npcIds)
	{
		return registerConsumer(callback, EventType.ON_NPC_SKILL_FINISHED, ListenerRegisterType.NPC, npcIds);
	}
	
	/**
	 * Provides instant callback operation when L2Npc casts skill on a player.
	 * @param callback
	 * @param npcIds
	 * @return
	 */
	protected final List<AbstractEventListener> setNpcSkillFinishedId(Consumer<OnNpcSkillFinished> callback, Collection<Integer> npcIds)
	{
		return registerConsumer(callback, EventType.ON_NPC_SKILL_FINISHED, ListenerRegisterType.NPC, npcIds);
	}
	
	// ---------------------------------------------------------------------------------------------------------------------------
	
	/**
	 * Provides instant callback operation when L2Npc is spawned.
	 * @param callback
	 * @param npcIds
	 * @return
	 */
	protected final List<AbstractEventListener> setNpcSpawnId(Consumer<OnNpcSpawn> callback, int... npcIds)
	{
		return registerConsumer(callback, EventType.ON_NPC_SPAWN, ListenerRegisterType.NPC, npcIds);
	}
	
	/**
	 * Provides instant callback operation when L2Npc is spawned.
	 * @param callback
	 * @param npcIds
	 * @return
	 */
	protected final List<AbstractEventListener> setNpcSpawnId(Consumer<OnNpcSpawn> callback, Collection<Integer> npcIds)
	{
		return registerConsumer(callback, EventType.ON_NPC_SPAWN, ListenerRegisterType.NPC, npcIds);
	}
	
	// ---------------------------------------------------------------------------------------------------------------------------
	
	/**
	 * Provides instant callback operation when {@link L2Npc} receives event from another {@link L2Npc}
	 * @param callback
	 * @param npcIds
	 * @return
	 */
	protected final List<AbstractEventListener> setNpcEventReceivedId(Consumer<OnNpcEventReceived> callback, int... npcIds)
	{
		return registerConsumer(callback, EventType.ON_NPC_EVENT_RECEIVED, ListenerRegisterType.NPC, npcIds);
	}
	
	/**
	 * Provides instant callback operation when {@link L2Npc} receives event from another {@link L2Npc}
	 * @param callback
	 * @param npcIds
	 * @return
	 */
	protected final List<AbstractEventListener> setNpcEventReceivedId(Consumer<OnNpcEventReceived> callback, Collection<Integer> npcIds)
	{
		return registerConsumer(callback, EventType.ON_NPC_EVENT_RECEIVED, ListenerRegisterType.NPC, npcIds);
	}
	
	// ---------------------------------------------------------------------------------------------------------------------------
	
	/**
	 * Provides instant callback operation when {@link L2Npc} finishes to move.
	 * @param callback
	 * @param npcIds
	 * @return
	 */
	protected final List<AbstractEventListener> setNpcMoveFinishedId(Consumer<OnNpcMoveFinished> callback, int... npcIds)
	{
		return registerConsumer(callback, EventType.ON_NPC_MOVE_FINISHED, ListenerRegisterType.NPC, npcIds);
	}
	
	/**
	 * Provides instant callback operation when {@link L2Npc} finishes to move.
	 * @param callback
	 * @param npcIds
	 * @return
	 */
	protected final List<AbstractEventListener> setNpcMoveFinishedId(Consumer<OnNpcMoveFinished> callback, Collection<Integer> npcIds)
	{
		return registerConsumer(callback, EventType.ON_NPC_MOVE_FINISHED, ListenerRegisterType.NPC, npcIds);
	}
	
	// ---------------------------------------------------------------------------------------------------------------------------
	
	/**
	 * Provides instant callback operation when {@link L2Npc} arrive to node of its route
	 * @param callback
	 * @param npcIds
	 * @return
	 */
	protected final List<AbstractEventListener> setNpcMoveNodeArrivedId(Consumer<OnNpcMoveNodeArrived> callback, int... npcIds)
	{
		return registerConsumer(callback, EventType.ON_NPC_MOVE_NODE_ARRIVED, ListenerRegisterType.NPC, npcIds);
	}
	
	/**
	 * Provides instant callback operation when {@link L2Npc} arrive to node of its route
	 * @param callback
	 * @param npcIds
	 * @return
	 */
	protected final List<AbstractEventListener> setNpcMoveNodeArrivedId(Consumer<OnNpcMoveNodeArrived> callback, Collection<Integer> npcIds)
	{
		return registerConsumer(callback, EventType.ON_NPC_MOVE_NODE_ARRIVED, ListenerRegisterType.NPC, npcIds);
	}
	
	// ---------------------------------------------------------------------------------------------------------------------------
	
	/**
	 * Provides instant callback operation when {@link L2Npc} finishes to move on its route.
	 * @param callback
	 * @param npcIds
	 * @return
	 */
	protected final List<AbstractEventListener> setNpcMoveRouteFinishedId(Consumer<OnNpcMoveRouteFinished> callback, int... npcIds)
	{
		return registerConsumer(callback, EventType.ON_NPC_MOVE_ROUTE_FINISHED, ListenerRegisterType.NPC, npcIds);
	}
	
	/**
	 * Provides instant callback operation when {@link L2Npc} finishes to move on its route.
	 * @param callback
	 * @param npcIds
	 * @return
	 */
	protected final List<AbstractEventListener> setNpcMoveRouteFinishedId(Consumer<OnNpcMoveRouteFinished> callback, Collection<Integer> npcIds)
	{
		return registerConsumer(callback, EventType.ON_NPC_MOVE_ROUTE_FINISHED, ListenerRegisterType.NPC, npcIds);
	}
	
	// ---------------------------------------------------------------------------------------------------------------------------
	
	/**
	 * Provides instant callback operation when {@link L2Npc} is about to hate and start attacking a creature.
	 * @param callback
	 * @param npcIds
	 * @return
	 */
	protected final List<AbstractEventListener> setNpcHateId(Consumer<OnAttackableHate> callback, int... npcIds)
	{
		return registerConsumer(callback, EventType.ON_NPC_HATE, ListenerRegisterType.NPC, npcIds);
	}
	
	/**
	 * Provides instant callback operation when {@link L2Npc} is about to hate and start attacking a creature.
	 * @param callback
	 * @param npcIds
	 * @return
	 */
	protected final List<AbstractEventListener> setNpcHateId(Consumer<OnAttackableHate> callback, Collection<Integer> npcIds)
	{
		return registerConsumer(callback, EventType.ON_NPC_HATE, ListenerRegisterType.NPC, npcIds);
	}
	
	/**
	 * Provides instant callback operation when {@link L2Npc} is about to hate and start attacking a creature.
	 * @param callback
	 * @param npcIds
	 * @return
	 */
	protected final List<AbstractEventListener> addNpcHateId(Function<OnAttackableHate, TerminateReturn> callback, int... npcIds)
	{
		return registerFunction(callback, EventType.ON_NPC_HATE, ListenerRegisterType.NPC, npcIds);
	}
	
	/**
	 * Provides instant callback operation when {@link L2Npc} is about to hate and start attacking a creature.
	 * @param callback
	 * @param npcIds
	 * @return
	 */
	protected final List<AbstractEventListener> addNpcHateId(Function<OnAttackableHate, TerminateReturn> callback, Collection<Integer> npcIds)
	{
		return registerFunction(callback, EventType.ON_NPC_HATE, ListenerRegisterType.NPC, npcIds);
	}
	
	// ---------------------------------------------------------------------------------------------------------------------------
	
	/**
	 * Provides instant callback operation when {@link L2Npc} is about to hate and start attacking a creature.
	 * @param callback
	 * @param npcIds
	 * @return
	 */
	protected final List<AbstractEventListener> setNpcCanBeSeenId(Consumer<OnNpcCanBeSeen> callback, int... npcIds)
	{
		return registerConsumer(callback, EventType.ON_NPC_CAN_BE_SEEN, ListenerRegisterType.NPC, npcIds);
	}
	
	/**
	 * Provides instant callback operation when {@link L2Npc} is about to hate and start attacking a creature.
	 * @param callback
	 * @param npcIds
	 * @return
	 */
	protected final List<AbstractEventListener> setNpcCanBeSeenId(Consumer<OnNpcCanBeSeen> callback, Collection<Integer> npcIds)
	{
		return registerConsumer(callback, EventType.ON_NPC_CAN_BE_SEEN, ListenerRegisterType.NPC, npcIds);
	}
	
	/**
	 * Provides instant callback operation when {@link L2Npc} is about to hate and start attacking a creature.
	 * @param callback
	 * @param npcIds
	 * @return
	 */
	protected final List<AbstractEventListener> setNpcCanBeSeenId(Function<OnNpcCanBeSeen, TerminateReturn> callback, int... npcIds)
	{
		return registerFunction(callback, EventType.ON_NPC_CAN_BE_SEEN, ListenerRegisterType.NPC, npcIds);
	}
	
	/**
	 * Provides instant callback operation when {@link L2Npc} is about to hate and start attacking a creature.
	 * @param callback
	 * @param npcIds
	 * @return
	 */
	protected final List<AbstractEventListener> setNpcCanBeSeenId(Function<OnNpcCanBeSeen, TerminateReturn> callback, Collection<Integer> npcIds)
	{
		return registerFunction(callback, EventType.ON_NPC_CAN_BE_SEEN, ListenerRegisterType.NPC, npcIds);
	}
	
	// ---------------------------------------------------------------------------------------------------------------------------
	
	/**
	 * Provides instant callback operation when {@link L2Npc} sees another creature.
	 * @param callback
	 * @param npcIds
	 * @return
	 */
	protected final List<AbstractEventListener> setNpcCreatureSeeId(Consumer<OnNpcCreatureSee> callback, int... npcIds)
	{
		return registerConsumer(callback, EventType.ON_NPC_CREATURE_SEE, ListenerRegisterType.NPC, npcIds);
	}
	
	/**
	 * Provides instant callback operation when {@link L2Npc} sees another creature.
	 * @param callback
	 * @param npcIds
	 * @return
	 */
	protected final List<AbstractEventListener> setNpcCreatureSeeId(Consumer<OnNpcCreatureSee> callback, Collection<Integer> npcIds)
	{
		return registerConsumer(callback, EventType.ON_NPC_CREATURE_SEE, ListenerRegisterType.NPC, npcIds);
	}
	
	// ---------------------------------------------------------------------------------------------------------------------------
	
	/**
	 * Provides instant callback operation when L2Attackable is under attack to other clan mates.
	 * @param callback
	 * @param npcIds
	 * @return
	 */
	protected final List<AbstractEventListener> setAttackableFactionIdId(Consumer<OnAttackableFactionCall> callback, int... npcIds)
	{
		return registerConsumer(callback, EventType.ON_ATTACKABLE_FACTION_CALL, ListenerRegisterType.NPC, npcIds);
	}
	
	/**
	 * Provides instant callback operation when L2Attackable is under attack to other clan mates.
	 * @param callback
	 * @param npcIds
	 * @return
	 */
	protected final List<AbstractEventListener> setAttackableFactionIdId(Consumer<OnAttackableFactionCall> callback, Collection<Integer> npcIds)
	{
		return registerConsumer(callback, EventType.ON_ATTACKABLE_FACTION_CALL, ListenerRegisterType.NPC, npcIds);
	}
	
	// ---------------------------------------------------------------------------------------------------------------------------
	
	/**
	 * Provides instant callback operation when L2Attackable is attacked from a player.
	 * @param callback
	 * @param npcIds
	 * @return
	 */
	protected final List<AbstractEventListener> setAttackableAttackId(Consumer<OnAttackableAttack> callback, int... npcIds)
	{
		return registerConsumer(callback, EventType.ON_ATTACKABLE_ATTACK, ListenerRegisterType.NPC, npcIds);
	}
	
	/**
	 * Provides instant callback operation when L2Attackable is attacked from a player.
	 * @param callback
	 * @param npcIds
	 * @return
	 */
	protected final List<AbstractEventListener> setAttackableAttackId(Consumer<OnAttackableAttack> callback, Collection<Integer> npcIds)
	{
		return registerConsumer(callback, EventType.ON_ATTACKABLE_ATTACK, ListenerRegisterType.NPC, npcIds);
	}
	
	// ---------------------------------------------------------------------------------------------------------------------------
	
	/**
	 * Provides instant callback operation when {@link L2PcInstance} enters in {@link L2Attackable}'s aggressive range.
	 * @param callback
	 * @param npcIds
	 * @return
	 */
	protected final List<AbstractEventListener> setAttackableAggroRangeEnterId(Consumer<OnAttackableAggroRangeEnter> callback, int... npcIds)
	{
		return registerConsumer(callback, EventType.ON_ATTACKABLE_AGGRO_RANGE_ENTER, ListenerRegisterType.NPC, npcIds);
	}
	
	/**
	 * Provides instant callback operation when {@link L2PcInstance} enters in {@link L2Attackable}'s aggressive range.
	 * @param callback
	 * @param npcIds
	 * @return
	 */
	protected final List<AbstractEventListener> setAttackableAggroRangeEnterId(Consumer<OnAttackableAggroRangeEnter> callback, Collection<Integer> npcIds)
	{
		return registerConsumer(callback, EventType.ON_ATTACKABLE_AGGRO_RANGE_ENTER, ListenerRegisterType.NPC, npcIds);
	}
	
	// ---------------------------------------------------------------------------------------------------------------------------
	
	/**
	 * Provides instant callback operation when {@link L2PcInstance} learn's a {@link Skill}.
	 * @param callback
	 * @param npcIds
	 * @return
	 */
	protected final List<AbstractEventListener> setPlayerSkillLearnId(Consumer<OnPlayerSkillLearn> callback, int... npcIds)
	{
		return registerConsumer(callback, EventType.ON_PLAYER_SKILL_LEARN, ListenerRegisterType.NPC, npcIds);
	}
	
	/**
	 * Provides instant callback operation when {@link L2PcInstance} learn's a {@link Skill}.
	 * @param callback
	 * @param npcIds
	 * @return
	 */
	protected final List<AbstractEventListener> setPlayerSkillLearnId(Consumer<OnPlayerSkillLearn> callback, Collection<Integer> npcIds)
	{
		return registerConsumer(callback, EventType.ON_PLAYER_SKILL_LEARN, ListenerRegisterType.NPC, npcIds);
	}
	
	// ---------------------------------------------------------------------------------------------------------------------------
	
	/**
	 * Provides instant callback operation when {@link L2PcInstance} summons a servitor or a pet
	 * @param callback
	 * @param npcIds
	 * @return
	 */
	protected final List<AbstractEventListener> setPlayerSummonSpawnId(Consumer<OnPlayerSummonSpawn> callback, int... npcIds)
	{
		return registerConsumer(callback, EventType.ON_PLAYER_SUMMON_SPAWN, ListenerRegisterType.NPC, npcIds);
	}
	
	/**
	 * Provides instant callback operation when {@link L2PcInstance} summons a servitor or a pet
	 * @param callback
	 * @param npcIds
	 * @return
	 */
	protected final List<AbstractEventListener> setPlayerSummonSpawnId(Consumer<OnPlayerSummonSpawn> callback, Collection<Integer> npcIds)
	{
		return registerConsumer(callback, EventType.ON_PLAYER_SUMMON_SPAWN, ListenerRegisterType.NPC, npcIds);
	}
	
	// ---------------------------------------------------------------------------------------------------------------------------
	
	/**
	 * Provides instant callback operation when {@link L2PcInstance} talk with a servitor or a pet
	 * @param callback
	 * @param npcIds
	 * @return
	 */
	protected final List<AbstractEventListener> setPlayerSummonTalkId(Consumer<OnPlayerSummonTalk> callback, int... npcIds)
	{
		return registerConsumer(callback, EventType.ON_PLAYER_SUMMON_TALK, ListenerRegisterType.NPC, npcIds);
	}
	
	/**
	 * Provides instant callback operation when {@link L2PcInstance} talk with a servitor or a pet
	 * @param callback
	 * @param npcIds
	 * @return
	 */
	protected final List<AbstractEventListener> setPlayerSummonTalkId(Consumer<OnPlayerSummonSpawn> callback, Collection<Integer> npcIds)
	{
		return registerConsumer(callback, EventType.ON_PLAYER_SUMMON_TALK, ListenerRegisterType.NPC, npcIds);
	}
	
	// ---------------------------------------------------------------------------------------------------------------------------
	
	/**
	 * Provides instant callback operation when {@link L2PcInstance} summons a servitor or a pet
	 * @param callback
	 * @return
	 */
	protected final List<AbstractEventListener> setPlayerLoginId(Consumer<OnPlayerLogin> callback)
	{
		return registerConsumer(callback, EventType.ON_PLAYER_LOGIN, ListenerRegisterType.GLOBAL);
	}
	
	// ---------------------------------------------------------------------------------------------------------------------------
	
	/**
	 * Provides instant callback operation when {@link L2PcInstance} summons a servitor or a pet
	 * @param callback
	 * @return
	 */
	protected final List<AbstractEventListener> setPlayerLogoutId(Consumer<OnPlayerLogout> callback)
	{
		return registerConsumer(callback, EventType.ON_PLAYER_LOGOUT, ListenerRegisterType.GLOBAL);
	}
	
	// ---------------------------------------------------------------------------------------------------------------------------
	
	/**
	 * Provides instant callback operation when {@link com.l2jserver.gameserver.model.actor.L2Character} Enters on a {@link L2ZoneType}.
	 * @param callback
	 * @param npcIds
	 * @return
	 */
	protected final List<AbstractEventListener> setCreatureZoneEnterId(Consumer<OnCreatureZoneEnter> callback, int... npcIds)
	{
		return registerConsumer(callback, EventType.ON_CREATURE_ZONE_ENTER, ListenerRegisterType.ZONE, npcIds);
	}
	
	/**
	 * Provides instant callback operation when {@link com.l2jserver.gameserver.model.actor.L2Character} Enters on a {@link L2ZoneType}.
	 * @param callback
	 * @param npcIds
	 * @return
	 */
	protected final List<AbstractEventListener> setCreatureZoneEnterId(Consumer<OnCreatureZoneEnter> callback, Collection<Integer> npcIds)
	{
		return registerConsumer(callback, EventType.ON_CREATURE_ZONE_ENTER, ListenerRegisterType.ZONE, npcIds);
	}
	
	// ---------------------------------------------------------------------------------------------------------------------------
	
	/**
	 * Provides instant callback operation when {@link com.l2jserver.gameserver.model.actor.L2Character} Exits on a {@link L2ZoneType}.
	 * @param callback
	 * @param npcIds
	 * @return
	 */
	protected final List<AbstractEventListener> setCreatureZoneExitId(Consumer<OnCreatureZoneExit> callback, int... npcIds)
	{
		return registerConsumer(callback, EventType.ON_CREATURE_ZONE_EXIT, ListenerRegisterType.ZONE, npcIds);
	}
	
	/**
	 * Provides instant callback operation when {@link com.l2jserver.gameserver.model.actor.L2Character} Exits on a {@link L2ZoneType}.
	 * @param callback
	 * @param npcIds
	 * @return
	 */
	protected final List<AbstractEventListener> setCreatureZoneExitId(Consumer<OnCreatureZoneExit> callback, Collection<Integer> npcIds)
	{
		return registerConsumer(callback, EventType.ON_CREATURE_ZONE_EXIT, ListenerRegisterType.ZONE, npcIds);
	}
	
	// ---------------------------------------------------------------------------------------------------------------------------
	
	/**
	 * Provides instant callback operation when {@link com.l2jserver.gameserver.model.actor.instance.L2TrapInstance} acts.
	 * @param callback
	 * @param npcIds
	 * @return
	 */
	protected final List<AbstractEventListener> setTrapActionId(Consumer<OnTrapAction> callback, int... npcIds)
	{
		return registerConsumer(callback, EventType.ON_TRAP_ACTION, ListenerRegisterType.NPC, npcIds);
	}
	
	/**
	 * Provides instant callback operation when {@link com.l2jserver.gameserver.model.actor.instance.L2TrapInstance} acts.
	 * @param callback
	 * @param npcIds
	 * @return
	 */
	protected final List<AbstractEventListener> setTrapActionId(Consumer<OnTrapAction> callback, Collection<Integer> npcIds)
	{
		return registerConsumer(callback, EventType.ON_TRAP_ACTION, ListenerRegisterType.NPC, npcIds);
	}
	
	// ---------------------------------------------------------------------------------------------------------------------------
	
	/**
	 * Provides instant callback operation when {@link L2Item} receives an event from {@link L2PcInstance}.
	 * @param callback
	 * @param npcIds
	 * @return
	 */
	protected final List<AbstractEventListener> setItemBypassEvenId(Consumer<OnItemBypassEvent> callback, int... npcIds)
	{
		return registerConsumer(callback, EventType.ON_ITEM_BYPASS_EVENT, ListenerRegisterType.ITEM, npcIds);
	}
	
	/**
	 * Provides instant callback operation when {@link L2Item} receives an event from {@link L2PcInstance}.
	 * @param callback
	 * @param npcIds
	 * @return
	 */
	protected final List<AbstractEventListener> setItemBypassEvenId(Consumer<OnItemBypassEvent> callback, Collection<Integer> npcIds)
	{
		return registerConsumer(callback, EventType.ON_ITEM_BYPASS_EVENT, ListenerRegisterType.ITEM, npcIds);
	}
	
	// ---------------------------------------------------------------------------------------------------------------------------
	
	/**
	 * Provides instant callback operation when {@link L2PcInstance} talk to {@link L2Item}.
	 * @param callback
	 * @param npcIds
	 * @return
	 */
	protected final List<AbstractEventListener> setItemTalkId(Consumer<OnItemTalk> callback, int... npcIds)
	{
		return registerConsumer(callback, EventType.ON_ITEM_TALK, ListenerRegisterType.ITEM, npcIds);
	}
	
	/**
	 * Provides instant callback operation when {@link L2PcInstance} talk to {@link L2Item}.
	 * @param callback
	 * @param npcIds
	 * @return
	 */
	protected final List<AbstractEventListener> setItemTalkId(Consumer<OnItemTalk> callback, Collection<Integer> npcIds)
	{
		return registerConsumer(callback, EventType.ON_ITEM_TALK, ListenerRegisterType.ITEM, npcIds);
	}
	
	// ---------------------------------------------------------------------------------------------------------------------------
	
	/**
	 * Provides instant callback operation when Olympiad match finishes.
	 * @param callback
	 * @return
	 */
	protected final List<AbstractEventListener> setOlympiadMatchResult(Consumer<OnOlympiadMatchResult> callback)
	{
		return registerConsumer(callback, EventType.ON_OLYMPIAD_MATCH_RESULT, ListenerRegisterType.OLYMPIAD);
	}
	
	// ---------------------------------------------------------------------------------------------------------------------------
	
	/**
	 * Provides instant callback operation when castle siege begins
	 * @param callback
	 * @param castleIds
	 * @return
	 */
	protected final List<AbstractEventListener> setCastleSiegeStartId(Consumer<OnCastleSiegeStart> callback, int... castleIds)
	{
		return registerConsumer(callback, EventType.ON_CASTLE_SIEGE_START, ListenerRegisterType.CASTLE, castleIds);
	}
	
	/**
	 * Provides instant callback operation when castle siege begins
	 * @param callback
	 * @param castleIds
	 * @return
	 */
	protected final List<AbstractEventListener> setCastleSiegeStartId(Consumer<OnCastleSiegeStart> callback, Collection<Integer> castleIds)
	{
		return registerConsumer(callback, EventType.ON_CASTLE_SIEGE_START, ListenerRegisterType.CASTLE, castleIds);
	}
	
	// ---------------------------------------------------------------------------------------------------------------------------
	
	/**
	 * Provides instant callback operation when Castle owner has changed during a siege
	 * @param callback
	 * @param castleIds
	 * @return
	 */
	protected final List<AbstractEventListener> setCastleSiegeOwnerChangeId(Consumer<OnCastleSiegeOwnerChange> callback, int... castleIds)
	{
		return registerConsumer(callback, EventType.ON_CASTLE_SIEGE_OWNER_CHANGE, ListenerRegisterType.CASTLE, castleIds);
	}
	
	/**
	 * Provides instant callback operation when Castle owner has changed during a siege
	 * @param callback
	 * @param castleIds
	 * @return
	 */
	protected final List<AbstractEventListener> setCastleSiegeOwnerChangeId(Consumer<OnCastleSiegeOwnerChange> callback, Collection<Integer> castleIds)
	{
		return registerConsumer(callback, EventType.ON_CASTLE_SIEGE_OWNER_CHANGE, ListenerRegisterType.CASTLE, castleIds);
	}
	
	// ---------------------------------------------------------------------------------------------------------------------------
	
	/**
	 * Provides instant callback operation when castle siege ends
	 * @param callback
	 * @param castleIds
	 * @return
	 */
	protected final List<AbstractEventListener> setCastleSiegeFinishId(Consumer<OnCastleSiegeFinish> callback, int... castleIds)
	{
		return registerConsumer(callback, EventType.ON_CASTLE_SIEGE_FINISH, ListenerRegisterType.CASTLE, castleIds);
	}
	
	/**
	 * Provides instant callback operation when castle siege ends
	 * @param callback
	 * @param castleIds
	 * @return
	 */
	protected final List<AbstractEventListener> setCastleSiegeFinishId(Consumer<OnCastleSiegeFinish> callback, Collection<Integer> castleIds)
	{
		return registerConsumer(callback, EventType.ON_CASTLE_SIEGE_FINISH, ListenerRegisterType.CASTLE, castleIds);
	}
	
	// ---------------------------------------------------------------------------------------------------------------------------
	
	/**
	 * Provides instant callback operation when player's profession has change
	 * @param callback
	 * @return
	 */
	protected final List<AbstractEventListener> setPlayerProfessionChangeId(Consumer<OnPlayerProfessionChange> callback)
	{
		return registerConsumer(callback, EventType.ON_PLAYER_PROFESSION_CHANGE, ListenerRegisterType.GLOBAL);
	}
	
	/**
	 * Provides instant callback operation when player's cancel profession
	 * @param callback
	 * @return
	 */
	protected final List<AbstractEventListener> setPlayerProfessionCancelId(Consumer<OnPlayerProfessionCancel> callback)
	{
		return registerConsumer(callback, EventType.ON_PLAYER_PROFESSION_CANCEL, ListenerRegisterType.GLOBAL);
	}
	
	// --------------------------------------------------------------------------------------------------
	// --------------------------------Default listener register methods---------------------------------
	// --------------------------------------------------------------------------------------------------
	
	/**
	 * Method that registers Function type of listeners (Listeners that need parameters but doesn't return objects)
	 * @param callback
	 * @param type
	 * @param registerType
	 * @param npcIds
	 * @return
	 */
	protected final List<AbstractEventListener> registerConsumer(Consumer<? extends IBaseEvent> callback, EventType type, ListenerRegisterType registerType, int... npcIds)
	{
		return registerListener((container) -> new ConsumerEventListener(container, type, callback, this), registerType, npcIds);
	}
	
	/**
	 * Method that registers Function type of listeners (Listeners that need parameters but doesn't return objects)
	 * @param callback
	 * @param type
	 * @param registerType
	 * @param npcIds
	 * @return
	 */
	protected final List<AbstractEventListener> registerConsumer(Consumer<? extends IBaseEvent> callback, EventType type, ListenerRegisterType registerType, Collection<Integer> npcIds)
	{
		return registerListener((container) -> new ConsumerEventListener(container, type, callback, this), registerType, npcIds);
	}
	
	/**
	 * Method that registers Function type of listeners (Listeners that need parameters and return objects)
	 * @param callback
	 * @param type
	 * @param registerType
	 * @param npcIds
	 * @return
	 */
	protected final List<AbstractEventListener> registerFunction(Function<? extends IBaseEvent, ? extends AbstractEventReturn> callback, EventType type, ListenerRegisterType registerType, int... npcIds)
	{
		return registerListener((container) -> new FunctionEventListener(container, type, callback, this), registerType, npcIds);
	}
	
	/**
	 * Method that registers Function type of listeners (Listeners that need parameters and return objects)
	 * @param callback
	 * @param type
	 * @param registerType
	 * @param npcIds
	 * @return
	 */
	protected final List<AbstractEventListener> registerFunction(Function<? extends IBaseEvent, ? extends AbstractEventReturn> callback, EventType type, ListenerRegisterType registerType, Collection<Integer> npcIds)
	{
		return registerListener((container) -> new FunctionEventListener(container, type, callback, this), registerType, npcIds);
	}
	
	/**
	 * Method that registers runnable type of listeners (Listeners that doesn't needs parameters or return objects)
	 * @param callback
	 * @param type
	 * @param registerType
	 * @param npcIds
	 * @return
	 */
	protected final List<AbstractEventListener> registerRunnable(Runnable callback, EventType type, ListenerRegisterType registerType, int... npcIds)
	{
		return registerListener((container) -> new RunnableEventListener(container, type, callback, this), registerType, npcIds);
	}
	
	/**
	 * Method that registers runnable type of listeners (Listeners that doesn't needs parameters or return objects)
	 * @param callback
	 * @param type
	 * @param registerType
	 * @param npcIds
	 * @return
	 */
	protected final List<AbstractEventListener> registerRunnable(Runnable callback, EventType type, ListenerRegisterType registerType, Collection<Integer> npcIds)
	{
		return registerListener((container) -> new RunnableEventListener(container, type, callback, this), registerType, npcIds);
	}
	
	/**
	 * Method that registers runnable type of listeners (Listeners that doesn't needs parameters or return objects)
	 * @param callback
	 * @param type
	 * @param registerType
	 * @param priority
	 * @param npcIds
	 * @return
	 */
	protected final List<AbstractEventListener> registerAnnotation(Method callback, EventType type, ListenerRegisterType registerType, int priority, int... npcIds)
	{
		return registerListener((container) -> new AnnotationEventListener(container, type, callback, this, priority), registerType, npcIds);
	}
	
	/**
	 * Method that registers runnable type of listeners (Listeners that doesn't needs parameters or return objects)
	 * @param callback
	 * @param type
	 * @param registerType
	 * @param priority
	 * @param npcIds
	 * @return
	 */
	protected final List<AbstractEventListener> registerAnnotation(Method callback, EventType type, ListenerRegisterType registerType, int priority, Collection<Integer> npcIds)
	{
		return registerListener((container) -> new AnnotationEventListener(container, type, callback, this, priority), registerType, npcIds);
	}
	
	/**
	 * Method that registers dummy type of listeners (Listeners doesn't gets notification but just used to check if their type present or not)
	 * @param type
	 * @param registerType
	 * @param npcIds
	 * @return
	 */
	protected final List<AbstractEventListener> registerDummy(EventType type, ListenerRegisterType registerType, int... npcIds)
	{
		return registerListener((container) -> new DummyEventListener(container, type, this), registerType, npcIds);
	}
	
	/**
	 * Method that registers dummy type of listeners (Listeners doesn't gets notification but just used to check if their type present or not)
	 * @param type
	 * @param registerType
	 * @param npcIds
	 * @return
	 */
	protected final List<AbstractEventListener> registerDummy(EventType type, ListenerRegisterType registerType, Collection<Integer> npcIds)
	{
		return registerListener((container) -> new DummyEventListener(container, type, this), registerType, npcIds);
	}
	
	// --------------------------------------------------------------------------------------------------
	// --------------------------------------Register methods--------------------------------------------
	// --------------------------------------------------------------------------------------------------
	
	/**
	 * Generic listener register method
	 * @param action
	 * @param registerType
	 * @param ids
	 * @return
	 */
	protected final List<AbstractEventListener> registerListener(Function<ListenersContainer, AbstractEventListener> action, ListenerRegisterType registerType, int... ids)
	{
		final List<AbstractEventListener> listeners = new ArrayList<>(ids.length > 0 ? ids.length : 1);
		if (ids.length > 0)
		{
			for (int id : ids)
			{
				switch (registerType)
				{
					case NPC:
					{
						final L2NpcTemplate template = NpcData.getInstance().getTemplate(id);
						if (template != null)
						{
							listeners.add(template.addListener(action.apply(template)));
						}
						break;
					}
					case ZONE:
					{
						final L2ZoneType template = ZoneManager.getInstance().getZoneById(id);
						if (template != null)
						{
							listeners.add(template.addListener(action.apply(template)));
						}
						break;
					}
					case ITEM:
					{
						final L2Item template = ItemTable.getInstance().getTemplate(id);
						if (template != null)
						{
							listeners.add(template.addListener(action.apply(template)));
						}
						break;
					}
					case CASTLE:
					{
						final Castle template = CastleManager.getInstance().getCastleById(id);
						if (template != null)
						{
							listeners.add(template.addListener(action.apply(template)));
						}
						break;
					}
					case FORTRESS:
					{
						final Fort template = FortManager.getInstance().getFortById(id);
						if (template != null)
						{
							listeners.add(template.addListener(action.apply(template)));
						}
						break;
					}
					default:
					{
						_log.log(Level.WARNING, getClass().getSimpleName() + ": Unhandled register type: " + registerType);
					}
				}
				
				_registeredIds.putIfAbsent(registerType, ConcurrentHashMap.newKeySet(1));
				_registeredIds.get(registerType).add(id);
			}
		}
		else
		{
			switch (registerType)
			{
				case OLYMPIAD:
				{
					final Olympiad template = Olympiad.getInstance();
					listeners.add(template.addListener(action.apply(template)));
					break;
				}
				case GLOBAL: // Global Listener
				{
					final ListenersContainer template = Containers.Global();
					listeners.add(template.addListener(action.apply(template)));
					break;
				}
				case GLOBAL_NPCS: // Global Npcs Listener
				{
					final ListenersContainer template = Containers.Npcs();
					listeners.add(template.addListener(action.apply(template)));
					break;
				}
				case GLOBAL_MONSTERS: // Global Monsters Listener
				{
					final ListenersContainer template = Containers.Monsters();
					listeners.add(template.addListener(action.apply(template)));
					break;
				}
				case GLOBAL_PLAYERS: // Global Players Listener
				{
					final ListenersContainer template = Containers.Players();
					listeners.add(template.addListener(action.apply(template)));
					break;
				}
			}
		}
		
		_listeners.addAll(listeners);
		return listeners;
	}
	
	/**
	 * Generic listener register method
	 * @param action
	 * @param registerType
	 * @param ids
	 * @return
	 */
	protected final List<AbstractEventListener> registerListener(Function<ListenersContainer, AbstractEventListener> action, ListenerRegisterType registerType, Collection<Integer> ids)
	{
		final List<AbstractEventListener> listeners = new ArrayList<>(!ids.isEmpty() ? ids.size() : 1);
		if (!ids.isEmpty())
		{
			for (int id : ids)
			{
				switch (registerType)
				{
					case NPC:
					{
						final L2NpcTemplate template = NpcData.getInstance().getTemplate(id);
						if (template != null)
						{
							listeners.add(template.addListener(action.apply(template)));
						}
						break;
					}
					case ZONE:
					{
						final L2ZoneType template = ZoneManager.getInstance().getZoneById(id);
						if (template != null)
						{
							listeners.add(template.addListener(action.apply(template)));
						}
						break;
					}
					case ITEM:
					{
						final L2Item template = ItemTable.getInstance().getTemplate(id);
						if (template != null)
						{
							listeners.add(template.addListener(action.apply(template)));
						}
						break;
					}
					case CASTLE:
					{
						final Castle template = CastleManager.getInstance().getCastleById(id);
						if (template != null)
						{
							listeners.add(template.addListener(action.apply(template)));
						}
						break;
					}
					case FORTRESS:
					{
						final Fort template = FortManager.getInstance().getFortById(id);
						if (template != null)
						{
							listeners.add(template.addListener(action.apply(template)));
						}
						break;
					}
					default:
					{
						_log.log(Level.WARNING, getClass().getSimpleName() + ": Unhandled register type: " + registerType);
					}
				}
			}
			
			_registeredIds.putIfAbsent(registerType, ConcurrentHashMap.newKeySet(ids.size()));
			_registeredIds.get(registerType).addAll(ids);
		}
		else
		{
			switch (registerType)
			{
				case OLYMPIAD:
				{
					final Olympiad template = Olympiad.getInstance();
					listeners.add(template.addListener(action.apply(template)));
					break;
				}
				case GLOBAL: // Global Listener
				{
					final ListenersContainer template = Containers.Global();
					listeners.add(template.addListener(action.apply(template)));
					break;
				}
				case GLOBAL_NPCS: // Global Npcs Listener
				{
					final ListenersContainer template = Containers.Npcs();
					listeners.add(template.addListener(action.apply(template)));
					break;
				}
				case GLOBAL_MONSTERS: // Global Monsters Listener
				{
					final ListenersContainer template = Containers.Monsters();
					listeners.add(template.addListener(action.apply(template)));
					break;
				}
				case GLOBAL_PLAYERS: // Global Players Listener
				{
					final ListenersContainer template = Containers.Players();
					listeners.add(template.addListener(action.apply(template)));
					break;
				}
			}
		}
		_listeners.addAll(listeners);
		return listeners;
	}
	
	public Set<Integer> getRegisteredIds(ListenerRegisterType type)
	{
		return _registeredIds.containsKey(type) ? _registeredIds.get(type) : Collections.emptySet();
	}
	
	public List<AbstractEventListener> getListeners()
	{
		return _listeners;
	}
	
	/**
	 * -------------------------------------------------------------------------------------------------------
	 */
	
	/**
	 * Show an on screen message to the player.
	 * @param player the player to display the message to
	 * @param text the message to display
	 * @param time the duration of the message in milliseconds
	 */
	public static void showOnScreenMsg(L2PcInstance player, String text, int time)
	{
		player.sendPacket(new ExShowScreenMessage(text, time));
	}
	
	/**
	 * Show an on screen message to the player.
	 * @param player the player to display the message to
	 * @param npcString the NPC string to display
	 * @param position the position of the message on the screen
	 * @param time the duration of the message in milliseconds
	 * @param params values of parameters to replace in the NPC String (like S1, C1 etc.)
	 */
	public static void showOnScreenMsg(L2PcInstance player, NpcStringId npcString, int position, int time, String... params)
	{
		player.sendPacket(new ExShowScreenMessage(npcString, position, time, params));
	}
	
	/**
	 * Show an on screen message to the player.
	 * @param player the player to display the message to
	 * @param systemMsg the system message to display
	 * @param position the position of the message on the screen
	 * @param time the duration of the message in milliseconds
	 * @param params values of parameters to replace in the system message (like S1, C1 etc.)
	 */
	public static void showOnScreenMsg(L2PcInstance player, SystemMessageId systemMsg, int position, int time, String... params)
	{
		player.sendPacket(new ExShowScreenMessage(systemMsg, position, time, params));
	}
	
	/**
	 * Add a temporary spawn of the specified NPC.
	 * @param npcId the ID of the NPC to spawn
	 * @param pos the object containing the spawn location coordinates
	 * @return the {@link L2Npc} object of the newly spawned NPC or {@code null} if the NPC doesn't exist
	 * @see #addSpawn(int, IPositionable, boolean, long, boolean, int)
	 * @see #addSpawn(int, int, int, int, int, boolean, long, boolean, int)
	 */
	public static L2Npc addSpawn(int npcId, IPositionable pos)
	{
		return addSpawn(npcId, pos.getX(), pos.getY(), pos.getZ(), pos.getHeading(), false, 0, false, 0);
	}
	
	/**
	 * Add a temporary spawn of the specified NPC.
	 * @param summoner the NPC that requires this spawn
	 * @param npcId the ID of the NPC to spawn
	 * @param pos the object containing the spawn location coordinates
	 * @param randomOffset if {@code true}, adds +/- 50~100 to X/Y coordinates of the spawn location
	 * @param despawnDelay time in milliseconds till the NPC is despawned (0 - only despawned on server shutdown)
	 * @return the {@link L2Npc} object of the newly spawned NPC, {@code null} if the NPC doesn't exist
	 */
	public static L2Npc addSpawn(L2Npc summoner, int npcId, IPositionable pos, boolean randomOffset, long despawnDelay)
	{
		return addSpawn(summoner, npcId, pos.getX(), pos.getY(), pos.getZ(), pos.getHeading(), randomOffset, despawnDelay, false, 0);
	}
	
	/**
	 * Add a temporary spawn of the specified NPC.
	 * @param npcId the ID of the NPC to spawn
	 * @param pos the object containing the spawn location coordinates
	 * @param isSummonSpawn if {@code true}, displays a summon animation on NPC spawn
	 * @return the {@link L2Npc} object of the newly spawned NPC or {@code null} if the NPC doesn't exist
	 * @see #addSpawn(int, IPositionable, boolean, long, boolean, int)
	 * @see #addSpawn(int, int, int, int, int, boolean, long, boolean, int)
	 */
	public static L2Npc addSpawn(int npcId, IPositionable pos, boolean isSummonSpawn)
	{
		return addSpawn(npcId, pos.getX(), pos.getY(), pos.getZ(), pos.getHeading(), false, 0, isSummonSpawn, 0);
	}
	
	/**
	 * Add a temporary spawn of the specified NPC.
	 * @param npcId the ID of the NPC to spawn
	 * @param pos the object containing the spawn location coordinates
	 * @param randomOffset if {@code true}, adds +/- 50~100 to X/Y coordinates of the spawn location
	 * @param despawnDelay time in milliseconds till the NPC is despawned (0 - only despawned on server shutdown)
	 * @return the {@link L2Npc} object of the newly spawned NPC or {@code null} if the NPC doesn't exist
	 * @see #addSpawn(int, IPositionable, boolean, long, boolean, int)
	 * @see #addSpawn(int, int, int, int, int, boolean, long, boolean, int)
	 */
	public static L2Npc addSpawn(int npcId, IPositionable pos, boolean randomOffset, long despawnDelay)
	{
		return addSpawn(npcId, pos.getX(), pos.getY(), pos.getZ(), pos.getHeading(), randomOffset, despawnDelay, false, 0);
	}
	
	/**
	 * Add a temporary spawn of the specified NPC.
	 * @param npcId the ID of the NPC to spawn
	 * @param pos the object containing the spawn location coordinates
	 * @param randomOffset if {@code true}, adds +/- 50~100 to X/Y coordinates of the spawn location
	 * @param despawnDelay time in milliseconds till the NPC is despawned (0 - only despawned on server shutdown)
	 * @param isSummonSpawn if {@code true}, displays a summon animation on NPC spawn
	 * @return the {@link L2Npc} object of the newly spawned NPC or {@code null} if the NPC doesn't exist
	 * @see #addSpawn(int, IPositionable, boolean, long, boolean, int)
	 * @see #addSpawn(int, int, int, int, int, boolean, long, boolean, int)
	 */
	public static L2Npc addSpawn(int npcId, IPositionable pos, boolean randomOffset, long despawnDelay, boolean isSummonSpawn)
	{
		return addSpawn(npcId, pos.getX(), pos.getY(), pos.getZ(), pos.getHeading(), randomOffset, despawnDelay, isSummonSpawn, 0);
	}
	
	/**
	 * Add a temporary spawn of the specified NPC.
	 * @param npcId the ID of the NPC to spawn
	 * @param pos the object containing the spawn location coordinates
	 * @param randomOffset if {@code true}, adds +/- 50~100 to X/Y coordinates of the spawn location
	 * @param despawnDelay time in milliseconds till the NPC is despawned (0 - only despawned on server shutdown)
	 * @param isSummonSpawn if {@code true}, displays a summon animation on NPC spawn
	 * @param instanceId the ID of the instance to spawn the NPC in (0 - the open world)
	 * @return the {@link L2Npc} object of the newly spawned NPC or {@code null} if the NPC doesn't exist
	 * @see #addSpawn(int, IPositionable)
	 * @see #addSpawn(int, IPositionable, boolean)
	 * @see #addSpawn(int, IPositionable, boolean, long)
	 * @see #addSpawn(int, IPositionable, boolean, long, boolean)
	 * @see #addSpawn(int, int, int, int, int, boolean, long, boolean, int)
	 */
	public static L2Npc addSpawn(int npcId, IPositionable pos, boolean randomOffset, long despawnDelay, boolean isSummonSpawn, int instanceId)
	{
		return addSpawn(npcId, pos.getX(), pos.getY(), pos.getZ(), pos.getHeading(), randomOffset, despawnDelay, isSummonSpawn, instanceId);
	}
	
	/**
	 * Add a temporary spawn of the specified NPC.
	 * @param npcId the ID of the NPC to spawn
	 * @param x the X coordinate of the spawn location
	 * @param y the Y coordinate of the spawn location
	 * @param z the Z coordinate (height) of the spawn location
	 * @param heading the heading of the NPC
	 * @param randomOffset if {@code true}, adds +/- 50~100 to X/Y coordinates of the spawn location
	 * @param despawnDelay time in milliseconds till the NPC is despawned (0 - only despawned on server shutdown)
	 * @return the {@link L2Npc} object of the newly spawned NPC or {@code null} if the NPC doesn't exist
	 * @see #addSpawn(int, IPositionable, boolean, long, boolean, int)
	 * @see #addSpawn(int, int, int, int, int, boolean, long, boolean, int)
	 */
	public static L2Npc addSpawn(int npcId, int x, int y, int z, int heading, boolean randomOffset, long despawnDelay)
	{
		return addSpawn(npcId, x, y, z, heading, randomOffset, despawnDelay, false, 0);
	}
	
	/**
	 * Add a temporary spawn of the specified NPC.
	 * @param npcId the ID of the NPC to spawn
	 * @param x the X coordinate of the spawn location
	 * @param y the Y coordinate of the spawn location
	 * @param z the Z coordinate (height) of the spawn location
	 * @param heading the heading of the NPC
	 * @param randomOffset if {@code true}, adds +/- 50~100 to X/Y coordinates of the spawn location
	 * @param despawnDelay time in milliseconds till the NPC is despawned (0 - only despawned on server shutdown)
	 * @param isSummonSpawn if {@code true}, displays a summon animation on NPC spawn
	 * @return the {@link L2Npc} object of the newly spawned NPC or {@code null} if the NPC doesn't exist
	 * @see #addSpawn(int, IPositionable, boolean, long, boolean, int)
	 * @see #addSpawn(int, int, int, int, int, boolean, long, boolean, int)
	 */
	public static L2Npc addSpawn(int npcId, int x, int y, int z, int heading, boolean randomOffset, long despawnDelay, boolean isSummonSpawn)
	{
		return addSpawn(npcId, x, y, z, heading, randomOffset, despawnDelay, isSummonSpawn, 0);
	}
	
	/**
	 * Add a temporary spawn of the specified NPC.
	 * @param npcId the ID of the NPC to spawn
	 * @param x the X coordinate of the spawn location
	 * @param y the Y coordinate of the spawn location
	 * @param z the Z coordinate (height) of the spawn location
	 * @param heading the heading of the NPC
	 * @param randomOffset if {@code true}, adds +/- 50~100 to X/Y coordinates of the spawn location
	 * @param despawnDelay time in milliseconds till the NPC is despawned (0 - only despawned on server shutdown)
	 * @param isSummonSpawn if {@code true}, displays a summon animation on NPC spawn
	 * @param instanceId the ID of the instance to spawn the NPC in (0 - the open world)
	 * @return the {@link L2Npc} object of the newly spawned NPC or {@code null} if the NPC doesn't exist
	 * @see #addSpawn(int, IPositionable, boolean, long, boolean, int)
	 * @see #addSpawn(int, int, int, int, int, boolean, long)
	 * @see #addSpawn(int, int, int, int, int, boolean, long, boolean)
	 */
	public static L2Npc addSpawn(int npcId, int x, int y, int z, int heading, boolean randomOffset, long despawnDelay, boolean isSummonSpawn, int instanceId)
	{
		return addSpawn(null, npcId, x, y, z, heading, randomOffset, despawnDelay, isSummonSpawn, instanceId);
	}
	
	/**
	 * Add a temporary spawn of the specified NPC.
	 * @param summoner the NPC that requires this spawn
	 * @param npcId the ID of the NPC to spawn
	 * @param x the X coordinate of the spawn location
	 * @param y the Y coordinate of the spawn location
	 * @param z the Z coordinate (height) of the spawn location
	 * @param heading the heading of the NPC
	 * @param randomOffset if {@code true}, adds +/- 50~100 to X/Y coordinates of the spawn location
	 * @param despawnDelay time in milliseconds till the NPC is despawned (0 - only despawned on server shutdown)
	 * @param isSummonSpawn if {@code true}, displays a summon animation on NPC spawn
	 * @param instanceId the ID of the instance to spawn the NPC in (0 - the open world)
	 * @return the {@link L2Npc} object of the newly spawned NPC or {@code null} if the NPC doesn't exist
	 * @see #addSpawn(int, IPositionable, boolean, long, boolean, int)
	 * @see #addSpawn(int, int, int, int, int, boolean, long)
	 * @see #addSpawn(int, int, int, int, int, boolean, long, boolean)
	 */
	public static L2Npc addSpawn(L2Npc summoner, int npcId, int x, int y, int z, int heading, boolean randomOffset, long despawnDelay, boolean isSummonSpawn, int instanceId)
	{
		try
		{
			if ((x == 0) && (y == 0))
			{
				_log.log(Level.SEVERE, "addSpawn(): invalid spawn coordinates for NPC #" + npcId + "!");
				return null;
			}
			
			if (randomOffset)
			{
				int offset = Rnd.get(50, 100);
				if (Rnd.nextBoolean())
				{
					offset *= -1;
				}
				x += offset;
				
				offset = Rnd.get(50, 100);
				if (Rnd.nextBoolean())
				{
					offset *= -1;
				}
				y += offset;
			}
			
			final L2Spawn spawn = new L2Spawn(npcId);
			spawn.setInstanceId(instanceId);
			spawn.setHeading(heading);
			spawn.setX(x);
			spawn.setY(y);
			spawn.setZ(z);
			spawn.stopRespawn();
			
			final L2Npc npc = spawn.spawnOne(isSummonSpawn);
			if (despawnDelay > 0)
			{
				npc.scheduleDespawn(despawnDelay);
			}
			
			if (summoner != null)
			{
				summoner.addSummonedNpc(npc);
			}
			return npc;
		}
		catch (Exception e)
		{
			_log.warning("Could not spawn NPC #" + npcId + "; error: " + e.getMessage());
		}
		
		return null;
	}
	
	/**
	 * @param trapId
	 * @param x
	 * @param y
	 * @param z
	 * @param heading
	 * @param skill
	 * @param instanceId
	 * @return
	 */
	public L2TrapInstance addTrap(int trapId, int x, int y, int z, int heading, Skill skill, int instanceId)
	{
		final L2NpcTemplate npcTemplate = NpcData.getInstance().getTemplate(trapId);
		L2TrapInstance trap = new L2TrapInstance(npcTemplate, instanceId, -1);
		trap.setCurrentHp(trap.getMaxHp());
		trap.setCurrentMp(trap.getMaxMp());
		trap.setIsInvul(true);
		trap.setHeading(heading);
		trap.spawnMe(x, y, z);
		return trap;
	}
	
	/**
	 * @param master
	 * @param minionId
	 * @return
	 */
	public L2Npc addMinion(L2MonsterInstance master, int minionId)
	{
		return MinionList.spawnMinion(master, minionId);
	}
	
	/**
	 * Get the amount of an item in player's inventory.
	 * @param player the player whose inventory to check
	 * @param itemId the ID of the item whose amount to get
	 * @return the amount of the specified item in player's inventory
	 */
	public static long getQuestItemsCount(L2PcInstance player, int itemId)
	{
		return player.getInventory().getInventoryItemCount(itemId, -1);
	}
	
	/**
	 * Get the total amount of all specified items in player's inventory.
	 * @param player the player whose inventory to check
	 * @param itemIds a list of IDs of items whose amount to get
	 * @return the summary amount of all listed items in player's inventory
	 */
	public long getQuestItemsCount(L2PcInstance player, int... itemIds)
	{
		long count = 0;
		for (L2ItemInstance item : player.getInventory().getItems())
		{
			if (item == null)
			{
				continue;
			}
			
			for (int itemId : itemIds)
			{
				if (item.getId() == itemId)
				{
					if ((count + item.getCount()) > Long.MAX_VALUE)
					{
						return Long.MAX_VALUE;
					}
					count += item.getCount();
				}
			}
		}
		return count;
	}
	
	/**
	 * Check if the player has the specified item in his inventory.
	 * @param player the player whose inventory to check for the specified item
	 * @param item the {@link ItemHolder} object containing the ID and count of the item to check
	 * @return {@code true} if the player has the required count of the item
	 */
	protected static boolean hasItem(L2PcInstance player, ItemHolder item)
	{
		return hasItem(player, item, true);
	}
	
	/**
	 * Check if the player has the required count of the specified item in his inventory.
	 * @param player the player whose inventory to check for the specified item
	 * @param item the {@link ItemHolder} object containing the ID and count of the item to check
	 * @param checkCount if {@code true}, check if each item is at least of the count specified in the ItemHolder,<br>
	 *            otherwise check only if the player has the item at all
	 * @return {@code true} if the player has the item
	 */
	protected static boolean hasItem(L2PcInstance player, ItemHolder item, boolean checkCount)
	{
		if (item == null)
		{
			return false;
		}
		if (checkCount)
		{
			return (getQuestItemsCount(player, item.getId()) >= item.getCount());
		}
		return hasQuestItems(player, item.getId());
	}
	
	/**
	 * Check if the player has all the specified items in his inventory and, if necessary, if their count is also as required.
	 * @param player the player whose inventory to check for the specified item
	 * @param checkCount if {@code true}, check if each item is at least of the count specified in the ItemHolder,<br>
	 *            otherwise check only if the player has the item at all
	 * @param itemList a list of {@link ItemHolder} objects containing the IDs of the items to check
	 * @return {@code true} if the player has all the items from the list
	 */
	protected static boolean hasAllItems(L2PcInstance player, boolean checkCount, ItemHolder... itemList)
	{
		if ((itemList == null) || (itemList.length == 0))
		{
			return false;
		}
		for (ItemHolder item : itemList)
		{
			if (!hasItem(player, item, checkCount))
			{
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Check for an item in player's inventory.
	 * @param player the player whose inventory to check for quest items
	 * @param itemId the ID of the item to check for
	 * @return {@code true} if the item exists in player's inventory, {@code false} otherwise
	 */
	public static boolean hasQuestItems(L2PcInstance player, int itemId)
	{
		return (player.getInventory().getItemByItemId(itemId) != null);
	}
	
	/**
	 * Check for multiple items in player's inventory.
	 * @param player the player whose inventory to check for quest items
	 * @param itemIds a list of item IDs to check for
	 * @return {@code true} if all items exist in player's inventory, {@code false} otherwise
	 */
	public static boolean hasQuestItems(L2PcInstance player, int... itemIds)
	{
		if ((itemIds == null) || (itemIds.length == 0))
		{
			return false;
		}
		final PcInventory inv = player.getInventory();
		for (int itemId : itemIds)
		{
			if (inv.getItemByItemId(itemId) == null)
			{
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Check for multiple items in player's inventory.
	 * @param player the player whose inventory to check for quest items
	 * @param itemIds a list of item IDs to check for
	 * @return {@code true} if at least one items exist in player's inventory, {@code false} otherwise
	 */
	public boolean hasAtLeastOneQuestItem(L2PcInstance player, int... itemIds)
	{
		final PcInventory inv = player.getInventory();
		for (int itemId : itemIds)
		{
			if (inv.getItemByItemId(itemId) != null)
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Get the enchantment level of an item in player's inventory.
	 * @param player the player whose item to check
	 * @param itemId the ID of the item whose enchantment level to get
	 * @return the enchantment level of the item or 0 if the item was not found
	 */
	public static int getEnchantLevel(L2PcInstance player, int itemId)
	{
		final L2ItemInstance enchantedItem = player.getInventory().getItemByItemId(itemId);
		if (enchantedItem == null)
		{
			return 0;
		}
		return enchantedItem.getEnchantLevel();
	}
	
	/**
	 * Give Adena to the player.
	 * @param player the player to whom to give the Adena
	 * @param count the amount of Adena to give
	 * @param applyRates if {@code true} quest rates will be applied to the amount
	 */
	public void giveAdena(L2PcInstance player, long count, boolean applyRates)
	{
		if (applyRates)
		{
			rewardItems(player, Inventory.ADENA_ID, count);
		}
		else
		{
			giveItems(player, Inventory.ADENA_ID, count);
		}
	}
	
	/**
	 * Give a reward to player using multipliers.
	 * @param player the player to whom to give the item
	 * @param holder
	 */
	public static void rewardItems(L2PcInstance player, ItemHolder holder)
	{
		rewardItems(player, holder.getId(), holder.getCount());
	}
	
	/**
	 * Give a reward to player using multipliers.
	 * @param player the player to whom to give the item
	 * @param itemId the ID of the item to give
	 * @param count the amount of items to give
	 */
	public static void rewardItems(L2PcInstance player, int itemId, long count)
	{
		if (count <= 0)
		{
			return;
		}
		
		final L2Item item = ItemTable.getInstance().getTemplate(itemId);
		if (item == null)
		{
			return;
		}
		
		try
		{
			if (itemId == Inventory.ADENA_ID)
			{
				count *= Config.RATE_QUEST_REWARD_ADENA;
			}
			else if (Config.RATE_QUEST_REWARD_USE_MULTIPLIERS)
			{
				if (item instanceof L2EtcItem)
				{
					switch (((L2EtcItem) item).getItemType())
					{
						case POTION:
							count *= Config.RATE_QUEST_REWARD_POTION;
							break;
						case SCRL_ENCHANT_WP:
						case SCRL_ENCHANT_AM:
						case SCROLL:
							count *= Config.RATE_QUEST_REWARD_SCROLL;
							break;
						case RECIPE:
							count *= Config.RATE_QUEST_REWARD_RECIPE;
							break;
						case MATERIAL:
							count *= Config.RATE_QUEST_REWARD_MATERIAL;
							break;
						default:
							count *= Config.RATE_QUEST_REWARD;
					}
				}
			}
			else
			{
				count *= Config.RATE_QUEST_REWARD;
			}
		}
		catch (Exception e)
		{
			count = Long.MAX_VALUE;
		}
		
		// Add items to player's inventory
		final L2ItemInstance itemInstance = player.getInventory().addItem("Quest", itemId, count, player, player.getTarget());
		if (itemInstance == null)
		{
			return;
		}
		
		sendItemGetMessage(player, itemInstance, count);
	}
	
	/**
	 * Send the system message and the status update packets to the player.
	 * @param player the player that has got the item
	 * @param item the item obtain by the player
	 * @param count the item count
	 */
	private static void sendItemGetMessage(L2PcInstance player, L2ItemInstance item, long count)
	{
		// If item for reward is gold, send message of gold reward to client
		if (item.getId() == Inventory.ADENA_ID)
		{
			SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S1_ADENA);
			smsg.addLong(count);
			player.sendPacket(smsg);
		}
		// Otherwise, send message of object reward to client
		else
		{
			if (count > 1)
			{
				SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S);
				smsg.addItemName(item);
				smsg.addLong(count);
				player.sendPacket(smsg);
			}
			else
			{
				SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1);
				smsg.addItemName(item);
				player.sendPacket(smsg);
			}
		}
		// send packets
		StatusUpdate su = new StatusUpdate(player);
		su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
		player.sendPacket(su);
	}
	
	/**
	 * Give item/reward to the player
	 * @param player
	 * @param itemId
	 * @param count
	 */
	public static void giveItems(L2PcInstance player, int itemId, long count)
	{
		giveItems(player, itemId, count, 0);
	}
	
	/**
	 * Give item/reward to the player
	 * @param player
	 * @param holder
	 */
	protected static void giveItems(L2PcInstance player, ItemHolder holder)
	{
		giveItems(player, holder.getId(), holder.getCount());
	}
	
	/**
	 * @param player
	 * @param itemId
	 * @param count
	 * @param enchantlevel
	 */
	public static void giveItems(L2PcInstance player, int itemId, long count, int enchantlevel)
	{
		if (count <= 0)
		{
			return;
		}
		
		// Add items to player's inventory
		final L2ItemInstance item = player.getInventory().addItem("Quest", itemId, count, player, player.getTarget());
		if (item == null)
		{
			return;
		}
		
		// set enchant level for item if that item is not adena
		if ((enchantlevel > 0) && (itemId != Inventory.ADENA_ID))
		{
			item.setEnchantLevel(enchantlevel);
		}
		
		sendItemGetMessage(player, item, count);
	}
	
	/**
	 * @param player
	 * @param itemId
	 * @param count
	 * @param attributeId
	 * @param attributeLevel
	 */
	public static void giveItems(L2PcInstance player, int itemId, long count, byte attributeId, int attributeLevel)
	{
		if (count <= 0)
		{
			return;
		}
		
		// Add items to player's inventory
		final L2ItemInstance item = player.getInventory().addItem("Quest", itemId, count, player, player.getTarget());
		if (item == null)
		{
			return;
		}
		
		// set enchant level for item if that item is not adena
		if ((attributeId >= 0) && (attributeLevel > 0))
		{
			item.setElementAttr(attributeId, attributeLevel);
			if (item.isEquipped())
			{
				item.updateElementAttrBonus(player);
			}
			
			InventoryUpdate iu = new InventoryUpdate();
			iu.addModifiedItem(item);
			player.sendPacket(iu);
		}
		
		sendItemGetMessage(player, item, count);
	}
	
	/**
	 * Give the specified player a set amount of items if he is lucky enough.<br>
	 * Not recommended to use this for non-stacking items.
	 * @param player the player to give the item(s) to
	 * @param itemId the ID of the item to give
	 * @param amountToGive the amount of items to give
	 * @param limit the maximum amount of items the player can have. Won't give more if this limit is reached. 0 - no limit.
	 * @param dropChance the drop chance as a decimal digit from 0 to 1
	 * @param playSound if true, plays ItemSound.quest_itemget when items are given and ItemSound.quest_middle when the limit is reached
	 * @return {@code true} if limit > 0 and the limit was reached or if limit <= 0 and items were given; {@code false} in all other cases
	 */
	public static boolean giveItemRandomly(L2PcInstance player, int itemId, long amountToGive, long limit, double dropChance, boolean playSound)
	{
		return giveItemRandomly(player, null, itemId, amountToGive, amountToGive, limit, dropChance, playSound);
	}
	
	/**
	 * Give the specified player a set amount of items if he is lucky enough.<br>
	 * Not recommended to use this for non-stacking items.
	 * @param player the player to give the item(s) to
	 * @param npc the NPC that "dropped" the item (can be null)
	 * @param itemId the ID of the item to give
	 * @param amountToGive the amount of items to give
	 * @param limit the maximum amount of items the player can have. Won't give more if this limit is reached. 0 - no limit.
	 * @param dropChance the drop chance as a decimal digit from 0 to 1
	 * @param playSound if true, plays ItemSound.quest_itemget when items are given and ItemSound.quest_middle when the limit is reached
	 * @return {@code true} if limit > 0 and the limit was reached or if limit <= 0 and items were given; {@code false} in all other cases
	 */
	public static boolean giveItemRandomly(L2PcInstance player, L2Npc npc, int itemId, long amountToGive, long limit, double dropChance, boolean playSound)
	{
		return giveItemRandomly(player, npc, itemId, amountToGive, amountToGive, limit, dropChance, playSound);
	}
	
	/**
	 * Give the specified player a random amount of items if he is lucky enough.<br>
	 * Not recommended to use this for non-stacking items.
	 * @param player the player to give the item(s) to
	 * @param npc the NPC that "dropped" the item (can be null)
	 * @param itemId the ID of the item to give
	 * @param minAmount the minimum amount of items to give
	 * @param maxAmount the maximum amount of items to give (will give a random amount between min/maxAmount multiplied by quest rates)
	 * @param limit the maximum amount of items the player can have. Won't give more if this limit is reached. 0 - no limit.
	 * @param dropChance the drop chance as a decimal digit from 0 to 1
	 * @param playSound if true, plays ItemSound.quest_itemget when items are given and ItemSound.quest_middle when the limit is reached
	 * @return {@code true} if limit > 0 and the limit was reached or if limit <= 0 and items were given; {@code false} in all other cases
	 */
	public static boolean giveItemRandomly(L2PcInstance player, L2Npc npc, int itemId, long minAmount, long maxAmount, long limit, double dropChance, boolean playSound)
	{
		final long currentCount = getQuestItemsCount(player, itemId);
		
		if ((limit > 0) && (currentCount >= limit))
		{
			return true;
		}
		
		minAmount *= Config.RATE_QUEST_DROP;
		maxAmount *= Config.RATE_QUEST_DROP;
		dropChance *= Config.RATE_QUEST_DROP; // TODO separate configs for rate and amount
		if ((npc != null) && Config.L2JMOD_CHAMPION_ENABLE && npc.isChampion())
		{
			if ((itemId == Inventory.ADENA_ID) || (itemId == Inventory.ANCIENT_ADENA_ID))
			{
				dropChance *= Config.L2JMOD_CHAMPION_ADENAS_REWARDS_CHANCE;
				minAmount *= Config.L2JMOD_CHAMPION_ADENAS_REWARDS_AMOUNT;
				maxAmount *= Config.L2JMOD_CHAMPION_ADENAS_REWARDS_AMOUNT;
			}
			else
			{
				dropChance *= Config.L2JMOD_CHAMPION_REWARDS_CHANCE;
				minAmount *= Config.L2JMOD_CHAMPION_REWARDS_AMOUNT;
				maxAmount *= Config.L2JMOD_CHAMPION_REWARDS_AMOUNT;
			}
		}
		
		long amountToGive = ((minAmount == maxAmount) ? minAmount : Rnd.get(minAmount, maxAmount));
		final double random = Rnd.nextDouble();
		// Inventory slot check (almost useless for non-stacking items)
		if ((dropChance >= random) && (amountToGive > 0) && player.getInventory().validateCapacityByItemId(itemId))
		{
			if ((limit > 0) && ((currentCount + amountToGive) > limit))
			{
				amountToGive = limit - currentCount;
			}
			
			// Give the item to player
			L2ItemInstance item = player.addItem("Quest", itemId, amountToGive, npc, true);
			if (item != null)
			{
				// limit reached (if there is no limit, this block doesn't execute)
				if ((currentCount + amountToGive) == limit)
				{
					if (playSound)
					{
						playSound(player, Sound.ITEMSOUND_QUEST_MIDDLE);
					}
					return true;
				}
				
				if (playSound)
				{
					playSound(player, Sound.ITEMSOUND_QUEST_ITEMGET);
				}
				// if there is no limit, return true every time an item is given
				if (limit <= 0)
				{
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Gives an item to the player
	 * @param player
	 * @param item
	 * @param victim the character that "dropped" the item
	 * @return <code>true</code> if at least one item was given, <code>false</code> otherwise
	 */
	protected static boolean giveItems(L2PcInstance player, IDropItem item, L2Character victim)
	{
		List<ItemHolder> items = item.calculateDrops(victim, player);
		if ((items == null) || items.isEmpty())
		{
			return false;
		}
		giveItems(player, items);
		return true;
	}
	
	/**
	 * Gives an item to the player
	 * @param player
	 * @param items
	 */
	protected static void giveItems(L2PcInstance player, List<ItemHolder> items)
	{
		for (ItemHolder item : items)
		{
			giveItems(player, item);
		}
		
	}
	
	/**
	 * Gives an item to the player
	 * @param player
	 * @param item
	 * @param limit the maximum amount of items the player can have. Won't give more if this limit is reached.
	 * @return <code>true</code> if at least one item was given to the player, <code>false</code> otherwise
	 */
	protected static boolean giveItems(L2PcInstance player, ItemHolder item, long limit)
	{
		long maxToGive = limit - player.getInventory().getInventoryItemCount(item.getId(), -1);
		if (maxToGive <= 0)
		{
			return false;
		}
		giveItems(player, item.getId(), Math.min(maxToGive, item.getCount()));
		return true;
	}
	
	protected static boolean giveItems(L2PcInstance player, ItemHolder item, long limit, boolean playSound)
	{
		boolean drop = giveItems(player, item, limit);
		if (drop && playSound)
		{
			playSound(player, Sound.ITEMSOUND_QUEST_ITEMGET);
		}
		return drop;
	}
	
	/**
	 * @param player
	 * @param items
	 * @param limit the maximum amount of items the player can have. Won't give more if this limit is reached.
	 * @return <code>true</code> if at least one item was given to the player, <code>false</code> otherwise
	 */
	protected static boolean giveItems(L2PcInstance player, List<ItemHolder> items, long limit)
	{
		boolean b = false;
		for (ItemHolder item : items)
		{
			b |= giveItems(player, item, limit);
		}
		return b;
	}
	
	protected static boolean giveItems(L2PcInstance player, List<ItemHolder> items, long limit, boolean playSound)
	{
		boolean drop = giveItems(player, items, limit);
		if (drop && playSound)
		{
			playSound(player, Sound.ITEMSOUND_QUEST_ITEMGET);
		}
		return drop;
	}
	
	/**
	 * @param player
	 * @param items
	 * @param limit the maximum amount of items the player can have. Won't give more if this limit is reached. If a no limit for an itemId is specified, item will always be given
	 * @return <code>true</code> if at least one item was given to the player, <code>false</code> otherwise
	 */
	protected static boolean giveItems(L2PcInstance player, List<ItemHolder> items, Map<Integer, Long> limit)
	{
		return giveItems(player, items, Util.mapToFunction(limit));
	}
	
	/**
	 * @param player
	 * @param items
	 * @param limit the maximum amount of items the player can have. Won't give more if this limit is reached. If a no limit for an itemId is specified, item will always be given
	 * @return <code>true</code> if at least one item was given to the player, <code>false</code> otherwise
	 */
	protected static boolean giveItems(L2PcInstance player, List<ItemHolder> items, Function<Integer, Long> limit)
	{
		boolean b = false;
		for (ItemHolder item : items)
		{
			if (limit != null)
			{
				Long longLimit = limit.apply(item.getId());
				// null -> no limit specified for that item id. This trick is to avoid limit.apply() be called twice (once for the null check)
				if (longLimit != null)
				{
					b |= giveItems(player, item, longLimit);
					continue; // the item is given, continue with next
				}
			}
			// da BIG else
			// no limit specified here (either limit or limit.apply(item.getId()) is null)
			b = true;
			giveItems(player, item);
			
		}
		return b;
	}
	
	protected static boolean giveItems(L2PcInstance player, List<ItemHolder> items, Function<Integer, Long> limit, boolean playSound)
	{
		boolean drop = giveItems(player, items, limit);
		if (drop && playSound)
		{
			playSound(player, Sound.ITEMSOUND_QUEST_ITEMGET);
		}
		return drop;
	}
	
	protected static boolean giveItems(L2PcInstance player, List<ItemHolder> items, Map<Integer, Long> limit, boolean playSound)
	{
		return giveItems(player, items, Util.mapToFunction(limit), playSound);
	}
	
	/**
	 * @param player
	 * @param item
	 * @param victim the character that "dropped" the item
	 * @param limit the maximum amount of items the player can have. Won't give more if this limit is reached.
	 * @return <code>true</code> if at least one item was given to the player, <code>false</code> otherwise
	 */
	protected static boolean giveItems(L2PcInstance player, IDropItem item, L2Character victim, int limit)
	{
		return giveItems(player, item.calculateDrops(victim, player), limit);
	}
	
	protected static boolean giveItems(L2PcInstance player, IDropItem item, L2Character victim, int limit, boolean playSound)
	{
		boolean drop = giveItems(player, item, victim, limit);
		if (drop && playSound)
		{
			playSound(player, Sound.ITEMSOUND_QUEST_ITEMGET);
		}
		return drop;
	}
	
	/**
	 * @param player
	 * @param item
	 * @param victim the character that "dropped" the item
	 * @param limit the maximum amount of items the player can have. Won't give more if this limit is reached. If a no limit for an itemId is specified, item will always be given
	 * @return <code>true</code> if at least one item was given to the player, <code>false</code> otherwise
	 */
	protected static boolean giveItems(L2PcInstance player, IDropItem item, L2Character victim, Map<Integer, Long> limit)
	{
		return giveItems(player, item.calculateDrops(victim, player), limit);
	}
	
	/**
	 * @param player
	 * @param item
	 * @param victim the character that "dropped" the item
	 * @param limit the maximum amount of items the player can have. Won't give more if this limit is reached. If a no limit for an itemId is specified, item will always be given
	 * @return <code>true</code> if at least one item was given to the player, <code>false</code> otherwise
	 */
	protected static boolean giveItems(L2PcInstance player, IDropItem item, L2Character victim, Function<Integer, Long> limit)
	{
		return giveItems(player, item.calculateDrops(victim, player), limit);
	}
	
	protected static boolean giveItems(L2PcInstance player, IDropItem item, L2Character victim, Map<Integer, Long> limit, boolean playSound)
	{
		return giveItems(player, item, victim, Util.mapToFunction(limit), playSound);
	}
	
	protected static boolean giveItems(L2PcInstance player, IDropItem item, L2Character victim, Function<Integer, Long> limit, boolean playSound)
	{
		boolean drop = giveItems(player, item, victim, limit);
		if (drop && playSound)
		{
			playSound(player, Sound.ITEMSOUND_QUEST_ITEMGET);
		}
		return drop;
	}
	
	/**
	 * Distributes items to players equally
	 * @param players the players to whom the items will be distributed
	 * @param items the items to distribute
	 * @param limit the limit what single player can have of each item
	 * @param playSound if to play sound if a player gets at least one item
	 * @return the counts of each items given to each player
	 */
	protected static Map<L2PcInstance, Map<Integer, Long>> distributeItems(Collection<L2PcInstance> players, Collection<ItemHolder> items, Function<Integer, Long> limit, boolean playSound)
	{
		Map<L2PcInstance, Map<Integer, Long>> rewardedCounts = calculateDistribution(players, items, limit);
		// now give the calculated items to the players
		giveItems(rewardedCounts, playSound);
		return rewardedCounts;
	}
	
	/**
	 * Distributes items to players equally
	 * @param players the players to whom the items will be distributed
	 * @param items the items to distribute
	 * @param limit the limit what single player can have of each item
	 * @param playSound if to play sound if a player gets at least one item
	 * @return the counts of each items given to each player
	 */
	protected static Map<L2PcInstance, Map<Integer, Long>> distributeItems(Collection<L2PcInstance> players, Collection<ItemHolder> items, Map<Integer, Long> limit, boolean playSound)
	{
		return distributeItems(players, items, Util.mapToFunction(limit), playSound);
	}
	
	/**
	 * Distributes items to players equally
	 * @param players the players to whom the items will be distributed
	 * @param items the items to distribute
	 * @param limit the limit what single player can have of each item
	 * @param playSound if to play sound if a player gets at least one item
	 * @return the counts of each items given to each player
	 */
	protected static Map<L2PcInstance, Map<Integer, Long>> distributeItems(Collection<L2PcInstance> players, Collection<ItemHolder> items, long limit, boolean playSound)
	{
		return distributeItems(players, items, t -> limit, playSound);
	}
	
	/**
	 * Distributes items to players equally
	 * @param players the players to whom the items will be distributed
	 * @param item the items to distribute
	 * @param limit the limit what single player can have of each item
	 * @param playSound if to play sound if a player gets at least one item
	 * @return the counts of each items given to each player
	 */
	protected static Map<L2PcInstance, Long> distributeItems(Collection<L2PcInstance> players, ItemHolder item, long limit, boolean playSound)
	{
		Map<L2PcInstance, Map<Integer, Long>> distribution = distributeItems(players, Collections.singletonList(item), limit, playSound);
		Map<L2PcInstance, Long> returnMap = new HashMap<>();
		for (Entry<L2PcInstance, Map<Integer, Long>> entry : distribution.entrySet())
		{
			for (Entry<Integer, Long> entry2 : entry.getValue().entrySet())
			{
				returnMap.put(entry.getKey(), entry2.getValue());
			}
		}
		return returnMap;
	}
	
	/**
	 * Distributes items to players equally
	 * @param players the players to whom the items will be distributed
	 * @param items the items to distribute
	 * @param killer the one who "kills" the victim
	 * @param victim the character that "dropped" the item
	 * @param limit the limit what single player can have of each item
	 * @param playSound if to play sound if a player gets at least one item
	 * @return the counts of each items given to each player
	 */
	protected static Map<L2PcInstance, Map<Integer, Long>> distributeItems(Collection<L2PcInstance> players, IDropItem items, L2Character killer, L2Character victim, Function<Integer, Long> limit, boolean playSound)
	{
		return distributeItems(players, items.calculateDrops(victim, killer), limit, playSound);
	}
	
	/**
	 * Distributes items to players equally
	 * @param players the players to whom the items will be distributed
	 * @param items the items to distribute
	 * @param killer the one who "kills" the victim
	 * @param victim the character that "dropped" the item
	 * @param limit the limit what single player can have of each item
	 * @param playSound if to play sound if a player gets at least one item
	 * @return the counts of each items given to each player
	 */
	protected static Map<L2PcInstance, Map<Integer, Long>> distributeItems(Collection<L2PcInstance> players, IDropItem items, L2Character killer, L2Character victim, Map<Integer, Long> limit, boolean playSound)
	{
		return distributeItems(players, items.calculateDrops(victim, killer), limit, playSound);
	}
	
	/**
	 * Distributes items to players equally
	 * @param players the players to whom the items will be distributed
	 * @param items the items to distribute
	 * @param killer the one who "kills" the victim
	 * @param victim the character that "dropped" the item
	 * @param limit the limit what single player can have of each item
	 * @param playSound if to play sound if a player gets at least one item
	 * @return the counts of each items given to each player
	 */
	protected static Map<L2PcInstance, Map<Integer, Long>> distributeItems(Collection<L2PcInstance> players, IDropItem items, L2Character killer, L2Character victim, long limit, boolean playSound)
	{
		return distributeItems(players, items.calculateDrops(victim, killer), limit, playSound);
	}
	
	/**
	 * Distributes items to players equally
	 * @param players the players to whom the items will be distributed
	 * @param items the items to distribute
	 * @param killer the one who "kills" the victim
	 * @param victim the character that "dropped" the item
	 * @param limit the limit what single player can have of each item
	 * @param playSound if to play sound if a player gets at least one item
	 * @param smartDrop true if to not calculate a drop, which can't be given to any player 'cause of limits
	 * @return the counts of each items given to each player
	 */
	protected static Map<L2PcInstance, Map<Integer, Long>> distributeItems(Collection<L2PcInstance> players, final GroupedGeneralDropItem items, L2Character killer, L2Character victim, Function<Integer, Long> limit, boolean playSound, boolean smartDrop)
	{
		GroupedGeneralDropItem toDrop;
		if (smartDrop)
		{
			toDrop = new GroupedGeneralDropItem(items.getChance(), items.getDropCalculationStrategy(), items.getKillerChanceModifierStrategy(), items.getPreciseStrategy());
			List<GeneralDropItem> dropItems = new LinkedList<>(items.getItems());
			ITEM_LOOP:
			for (Iterator<GeneralDropItem> it = dropItems.iterator(); it.hasNext();)
			{
				GeneralDropItem item = it.next();
				for (L2PcInstance player : players)
				{
					int itemId = item.getItemId();
					if (player.getInventory().getInventoryItemCount(itemId, -1, true) < avoidNull(limit, itemId))
					{
						// we can give this item to this player
						continue ITEM_LOOP;
					}
				}
				// there's nobody to give this item to
				it.remove();
			}
			toDrop.setItems(dropItems);
			toDrop = toDrop.normalizeMe(victim, killer);
		}
		else
		{
			toDrop = items;
		}
		return distributeItems(players, toDrop, killer, victim, limit, playSound);
	}
	
	/**
	 * Distributes items to players equally
	 * @param players the players to whom the items will be distributed
	 * @param items the items to distribute
	 * @param killer the one who "kills" the victim
	 * @param victim the character that "dropped" the item
	 * @param limit the limit what single player can have of each item
	 * @param playSound if to play sound if a player gets at least one item
	 * @param smartDrop true if to not calculate a drop, which can't be given to any player
	 * @return the counts of each items given to each player
	 */
	protected static Map<L2PcInstance, Map<Integer, Long>> distributeItems(Collection<L2PcInstance> players, final GroupedGeneralDropItem items, L2Character killer, L2Character victim, Map<Integer, Long> limit, boolean playSound, boolean smartDrop)
	{
		return distributeItems(players, items, killer, victim, Util.mapToFunction(limit), playSound, smartDrop);
	}
	
	/**
	 * Distributes items to players equally
	 * @param players the players to whom the items will be distributed
	 * @param items the items to distribute
	 * @param killer the one who "kills" the victim
	 * @param victim the character that "dropped" the item
	 * @param limit the limit what single player can have of each item
	 * @param playSound if to play sound if a player gets at least one item
	 * @param smartDrop true if to not calculate a drop, which can't be given to any player
	 * @return the counts of each items given to each player
	 */
	protected static Map<L2PcInstance, Map<Integer, Long>> distributeItems(Collection<L2PcInstance> players, final GroupedGeneralDropItem items, L2Character killer, L2Character victim, long limit, boolean playSound, boolean smartDrop)
	{
		return distributeItems(players, items, killer, victim, t -> limit, playSound, smartDrop);
	}
	
	/**
	 * @param players
	 * @param items
	 * @param limit
	 * @return
	 */
	private static Map<L2PcInstance, Map<Integer, Long>> calculateDistribution(Collection<L2PcInstance> players, Collection<ItemHolder> items, Function<Integer, Long> limit)
	{
		Map<L2PcInstance, Map<Integer, Long>> rewardedCounts = new HashMap<>();
		for (L2PcInstance player : players)
		{
			rewardedCounts.put(player, new HashMap<Integer, Long>());
		}
		NEXT_ITEM:
		for (ItemHolder item : items)
		{
			long equaldist = item.getCount() / players.size();
			long randomdist = item.getCount() % players.size();
			List<L2PcInstance> toDist = new ArrayList<>(players);
			do // this must happen at least once in order to get away already full players (and then equaldist can become nonzero)
			{
				for (Iterator<L2PcInstance> it = toDist.iterator(); it.hasNext();)
				{
					L2PcInstance player = it.next();
					if (!rewardedCounts.get(player).containsKey(item.getId()))
					{
						rewardedCounts.get(player).put(item.getId(), 0L);
					}
					long maxGive = avoidNull(limit, item.getId()) - player.getInventory().getInventoryItemCount(item.getId(), -1, true) - rewardedCounts.get(player).get(item.getId());
					long toGive = equaldist;
					if (equaldist >= maxGive)
					{
						toGive = maxGive;
						randomdist += (equaldist - maxGive); // overflown items are available to next players
						it.remove(); // this player is already full
					}
					rewardedCounts.get(player).put(item.getId(), rewardedCounts.get(player).get(item.getId()) + toGive);
				}
				if (toDist.isEmpty())
				{
					// there's no one to give items anymore, all players will be full when we give the items
					continue NEXT_ITEM;
				}
				equaldist = randomdist / toDist.size(); // the rest of items may be allowed to be equally distributed between remaining players
				randomdist %= toDist.size();
			}
			while (equaldist > 0);
			while (randomdist > 0)
			{
				if (toDist.isEmpty())
				{
					// we don't have any player left
					continue NEXT_ITEM;
				}
				L2PcInstance player = toDist.get(getRandom(toDist.size()));
				// avoid null return
				long maxGive = avoidNull(limit, item.getId()) - limit.apply(item.getId()) - player.getInventory().getInventoryItemCount(item.getId(), -1, true) - rewardedCounts.get(player).get(item.getId());
				if (maxGive > 0)
				{
					// we can add an item to player
					// so we add one item to player
					rewardedCounts.get(player).put(item.getId(), rewardedCounts.get(player).get(item.getId()) + 1);
					randomdist--;
				}
				toDist.remove(player); // Either way this player isn't allowable for next random award
			}
		}
		return rewardedCounts;
	}
	
	/**
	 * This function is for avoidance null returns in function limits
	 * @param <T> the type of function arg
	 * @param function the function
	 * @param arg the argument
	 * @return {@link Long#MAX_VALUE} if function.apply(arg) is null, function.apply(arg) otherwise
	 */
	private static <T> long avoidNull(Function<T, Long> function, T arg)
	{
		Long longLimit = function.apply(arg);
		return longLimit == null ? Long.MAX_VALUE : longLimit;
	}
	
	/**
	 * Distributes items to players
	 * @param rewardedCounts A scheme of distribution items (the structure is: Map<player Map<itemId, count>>)
	 * @param playSound if to play sound if a player gets at least one item
	 */
	private static void giveItems(Map<L2PcInstance, Map<Integer, Long>> rewardedCounts, boolean playSound)
	{
		for (Entry<L2PcInstance, Map<Integer, Long>> entry : rewardedCounts.entrySet())
		{
			L2PcInstance player = entry.getKey();
			boolean playPlayerSound = false;
			for (Entry<Integer, Long> item : entry.getValue().entrySet())
			{
				if (item.getValue() >= 0)
				{
					playPlayerSound = true;
					giveItems(player, item.getKey(), item.getValue());
				}
			}
			if (playSound && playPlayerSound)
			{
				playSound(player, Sound.ITEMSOUND_QUEST_ITEMGET);
			}
		}
	}
	
	/**
	 * Take an amount of a specified item from player's inventory.
	 * @param player the player whose item to take
	 * @param itemId the ID of the item to take
	 * @param amount the amount to take
	 * @return {@code true} if any items were taken, {@code false} otherwise
	 */
	public static boolean takeItems(L2PcInstance player, int itemId, long amount)
	{
		final List<L2ItemInstance> items = player.getInventory().getItemsByItemId(itemId);
		if (amount < 0)
		{
			items.forEach(i -> takeItem(player, i, i.getCount()));
		}
		else
		{
			long currentCount = 0;
			for (L2ItemInstance i : items)
			{
				long toDelete = i.getCount();
				if ((currentCount + toDelete) > amount)
				{
					toDelete = amount - currentCount;
				}
				takeItem(player, i, toDelete);
				currentCount += toDelete;
			}
		}
		return true;
	}
	
	private static boolean takeItem(L2PcInstance player, L2ItemInstance item, long toDelete)
	{
		if (item.isEquipped())
		{
			final L2ItemInstance[] unequiped = player.getInventory().unEquipItemInBodySlotAndRecord(item.getItem().getBodyPart());
			final InventoryUpdate iu = new InventoryUpdate();
			for (L2ItemInstance itm : unequiped)
			{
				iu.addModifiedItem(itm);
			}
			player.sendPacket(iu);
			player.broadcastUserInfo();
		}
		return player.destroyItemByItemId("Quest", item.getId(), toDelete, player, true);
	}
	
	/**
	 * Take a set amount of a specified item from player's inventory.
	 * @param player the player whose item to take
	 * @param holder the {@link ItemHolder} object containing the ID and count of the item to take
	 * @return {@code true} if the item was taken, {@code false} otherwise
	 */
	protected static boolean takeItem(L2PcInstance player, ItemHolder holder)
	{
		if (holder == null)
		{
			return false;
		}
		return takeItems(player, holder.getId(), holder.getCount());
	}
	
	/**
	 * Take a set amount of all specified items from player's inventory.
	 * @param player the player whose items to take
	 * @param itemList the list of {@link ItemHolder} objects containing the IDs and counts of the items to take
	 * @return {@code true} if all items were taken, {@code false} otherwise
	 */
	protected static boolean takeAllItems(L2PcInstance player, ItemHolder... itemList)
	{
		if ((itemList == null) || (itemList.length == 0))
		{
			return false;
		}
		// first check if the player has all items to avoid taking half the items from the list
		if (!hasAllItems(player, true, itemList))
		{
			return false;
		}
		for (ItemHolder item : itemList)
		{
			// this should never be false, but just in case
			if (!takeItem(player, item))
			{
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Take an amount of all specified items from player's inventory.
	 * @param player the player whose items to take
	 * @param amount the amount to take of each item
	 * @param itemIds a list or an array of IDs of the items to take
	 * @return {@code true} if all items were taken, {@code false} otherwise
	 */
	public static boolean takeItems(L2PcInstance player, int amount, int... itemIds)
	{
		boolean check = true;
		if (itemIds != null)
		{
			for (int item : itemIds)
			{
				check &= takeItems(player, item, amount);
			}
		}
		return check;
	}
	
	/**
	 * Send a packet in order to play a sound to the player.
	 * @param player the player whom to send the packet
	 * @param sound the {@link IAudio} object of the sound to play
	 */
	public static void playSound(L2PcInstance player, IAudio sound)
	{
		player.sendPacket(sound.getPacket());
	}
	
	/**
	 * Add EXP and SP as quest reward.
	 * @param player the player whom to reward with the EXP/SP
	 * @param exp the amount of EXP to give to the player
	 * @param sp the amount of SP to give to the player
	 */
	public static void addExpAndSp(L2PcInstance player, long exp, int sp)
	{
		player.addExpAndSp((long) player.calcStat(Stats.EXPSP_RATE, exp * Config.RATE_QUEST_REWARD_XP, null, null), (int) player.calcStat(Stats.EXPSP_RATE, sp * Config.RATE_QUEST_REWARD_SP, null, null));
	}
	
	/**
	 * Get a random integer from 0 (inclusive) to {@code max} (exclusive).<br>
	 * Use this method instead of importing {@link com.l2jserver.util.Rnd} utility.
	 * @param max the maximum value for randomization
	 * @return a random integer number from 0 to {@code max - 1}
	 */
	public static int getRandom(int max)
	{
		return Rnd.get(max);
	}
	
	/**
	 * Get a random integer from {@code min} (inclusive) to {@code max} (inclusive).<br>
	 * Use this method instead of importing {@link com.l2jserver.util.Rnd} utility.
	 * @param min the minimum value for randomization
	 * @param max the maximum value for randomization
	 * @return a random integer number from {@code min} to {@code max}
	 */
	public static int getRandom(int min, int max)
	{
		return Rnd.get(min, max);
	}
	
	/**
	 * Get a random boolean.<br>
	 * Use this method instead of importing {@link com.l2jserver.util.Rnd} utility.
	 * @return {@code true} or {@code false} randomly
	 */
	public static boolean getRandomBoolean()
	{
		return Rnd.nextBoolean();
	}
	
	/**
	 * Get the ID of the item equipped in the specified inventory slot of the player.
	 * @param player the player whose inventory to check
	 * @param slot the location in the player's inventory to check
	 * @return the ID of the item equipped in the specified inventory slot or 0 if the slot is empty or item is {@code null}.
	 */
	public static int getItemEquipped(L2PcInstance player, int slot)
	{
		return player.getInventory().getPaperdollItemId(slot);
	}
	
	/**
	 * @return the number of ticks from the {@link com.l2jserver.gameserver.GameTimeController}.
	 */
	public static int getGameTicks()
	{
		return GameTimeController.getInstance().getGameTicks();
	}
	
	/**
	 * Execute a procedure for each player depending on the parameters.
	 * @param player the player on which the procedure will be executed
	 * @param npc the related NPC
	 * @param isSummon {@code true} if the event that called this method was originated by the player's summon, {@code false} otherwise
	 * @param includeParty if {@code true}, #actionForEachPlayer(L2PcInstance, L2Npc, boolean) will be called with the player's party members
	 * @param includeCommandChannel if {@code true}, {@link #actionForEachPlayer(L2PcInstance, L2Npc, boolean)} will be called with the player's command channel members
	 * @see #actionForEachPlayer(L2PcInstance, L2Npc, boolean)
	 */
	public final void executeForEachPlayer(L2PcInstance player, final L2Npc npc, final boolean isSummon, boolean includeParty, boolean includeCommandChannel)
	{
		if ((includeParty || includeCommandChannel) && player.isInParty())
		{
			if (includeCommandChannel && player.getParty().isInCommandChannel())
			{
				player.getParty().getCommandChannel().forEachMember(member ->
				{
					actionForEachPlayer(member, npc, isSummon);
					return true;
				});
			}
			else if (includeParty)
			{
				player.getParty().forEachMember(member ->
				{
					actionForEachPlayer(member, npc, isSummon);
					return true;
				});
			}
		}
		else
		{
			actionForEachPlayer(player, npc, isSummon);
		}
	}
	
	/**
	 * Overridable method called from {@link #executeForEachPlayer(L2PcInstance, L2Npc, boolean, boolean, boolean)}
	 * @param player the player on which the action will be run
	 * @param npc the NPC related to this action
	 * @param isSummon {@code true} if the event that called this method was originated by the player's summon
	 */
	public void actionForEachPlayer(L2PcInstance player, L2Npc npc, boolean isSummon)
	{
		// To be overridden in quest scripts.
	}
	
	/**
	 * Open a door if it is present on the instance and its not open.
	 * @param doorId the ID of the door to open
	 * @param instanceId the ID of the instance the door is in (0 if the door is not not inside an instance)
	 */
	public void openDoor(int doorId, int instanceId)
	{
		final L2DoorInstance door = getDoor(doorId, instanceId);
		if (door == null)
		{
			_log.log(Level.WARNING, getClass().getSimpleName() + ": called openDoor(" + doorId + ", " + instanceId + "); but door wasnt found!", new NullPointerException());
		}
		else if (!door.getOpen())
		{
			door.openMe();
		}
	}
	
	/**
	 * Close a door if it is present in a specified the instance and its open.
	 * @param doorId the ID of the door to close
	 * @param instanceId the ID of the instance the door is in (0 if the door is not not inside an instance)
	 */
	public void closeDoor(int doorId, int instanceId)
	{
		final L2DoorInstance door = getDoor(doorId, instanceId);
		if (door == null)
		{
			_log.log(Level.WARNING, getClass().getSimpleName() + ": called closeDoor(" + doorId + ", " + instanceId + "); but door wasnt found!", new NullPointerException());
		}
		else if (door.getOpen())
		{
			door.closeMe();
		}
	}
	
	/**
	 * Retrieve a door from an instance or the real world.
	 * @param doorId the ID of the door to get
	 * @param instanceId the ID of the instance the door is in (0 if the door is not not inside an instance)
	 * @return the found door or {@code null} if no door with that ID and instance ID was found
	 */
	public L2DoorInstance getDoor(int doorId, int instanceId)
	{
		L2DoorInstance door = null;
		if (instanceId <= 0)
		{
			door = DoorData.getInstance().getDoor(doorId);
		}
		else
		{
			final Instance inst = InstanceManager.getInstance().getInstance(instanceId);
			if (inst != null)
			{
				door = inst.getDoor(doorId);
			}
		}
		return door;
	}
	
	/**
	 * Teleport a player into/out of an instance.
	 * @param player the player to teleport
	 * @param loc the {@link Location} object containing the destination coordinates
	 * @param instanceId the ID of the instance to teleport the player to (0 to teleport out of an instance)
	 */
	public void teleportPlayer(L2PcInstance player, Location loc, int instanceId)
	{
		teleportPlayer(player, loc, instanceId, true);
	}
	
	/**
	 * Teleport a player into/out of an instance.
	 * @param player the player to teleport
	 * @param loc the {@link Location} object containing the destination coordinates
	 * @param instanceId the ID of the instance to teleport the player to (0 to teleport out of an instance)
	 * @param allowRandomOffset if {@code true}, will randomize the teleport coordinates by +/-Config.MAX_OFFSET_ON_TELEPORT
	 */
	public void teleportPlayer(L2PcInstance player, Location loc, int instanceId, boolean allowRandomOffset)
	{
		player.teleToLocation(loc, instanceId, allowRandomOffset ? Config.MAX_OFFSET_ON_TELEPORT : 0);
	}
	
	/**
	 * Monster is running and attacking the playable.
	 * @param npc the NPC that performs the attack
	 * @param creature the target of the attack
	 */
	protected void addAttackDesire(L2Npc npc, L2Character creature)
	{
		addAttackDesire(npc, creature, 999);
	}
	
	/**
	 * Monster is running and attacking the target.
	 * @param npc the NPC that performs the attack
	 * @param creature the target of the attack
	 * @param desire the desire to perform the attack
	 */
	protected void addAttackDesire(L2Npc npc, L2Character creature, long desire)
	{
		if (npc instanceof L2Attackable)
		{
			((L2Attackable) npc).addDamageHate(creature, 0, desire);
		}
		npc.setIsRunning(true);
		npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, creature);
	}
	
	/**
	 * Adds desire to move to the given NPC.
	 * @param npc the NPC
	 * @param loc the location
	 * @param desire the desire
	 */
	protected void addMoveToDesire(L2Npc npc, Location loc, int desire)
	{
		npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, loc);
	}
	
	/**
	 * Instantly cast a skill upon the given target.
	 * @param npc the caster NPC
	 * @param target the target of the cast
	 * @param skill the skill to cast
	 */
	protected void castSkill(L2Npc npc, L2Playable target, SkillHolder skill)
	{
		npc.setTarget(target);
		npc.doCast(skill.getSkill());
	}
	
	/**
	 * Instantly cast a skill upon the given target.
	 * @param npc the caster NPC
	 * @param target the target of the cast
	 * @param skill the skill to cast
	 */
	protected void castSkill(L2Npc npc, L2Playable target, Skill skill)
	{
		npc.setTarget(target);
		npc.doCast(skill);
	}
	
	/**
	 * Adds the desire to cast a skill to the given NPC.
	 * @param npc the NPC whom cast the skill
	 * @param target the skill target
	 * @param skill the skill to cast
	 * @param desire the desire to cast the skill
	 */
	protected void addSkillCastDesire(L2Npc npc, L2Character target, SkillHolder skill, long desire)
	{
		addSkillCastDesire(npc, target, skill.getSkill(), desire);
	}
	
	/**
	 * Adds the desire to cast a skill to the given NPC.
	 * @param npc the NPC whom cast the skill
	 * @param target the skill target
	 * @param skill the skill to cast
	 * @param desire the desire to cast the skill
	 */
	protected void addSkillCastDesire(L2Npc npc, L2Character target, Skill skill, long desire)
	{
		if (npc instanceof L2Attackable)
		{
			((L2Attackable) npc).addDamageHate(target, 0, desire);
		}
		npc.setTarget(target);
		npc.getAI().setIntention(CtrlIntention.AI_INTENTION_CAST, skill, target);
	}
	
	/**
	 * Sends the special camera packet to the player.
	 * @param player the player
	 * @param creature the watched creature
	 * @param force
	 * @param angle1
	 * @param angle2
	 * @param time
	 * @param range
	 * @param duration
	 * @param relYaw
	 * @param relPitch
	 * @param isWide
	 * @param relAngle
	 */
	public static final void specialCamera(L2PcInstance player, L2Character creature, int force, int angle1, int angle2, int time, int range, int duration, int relYaw, int relPitch, int isWide, int relAngle)
	{
		player.sendPacket(new SpecialCamera(creature, force, angle1, angle2, time, range, duration, relYaw, relPitch, isWide, relAngle));
	}
	
	/**
	 * Sends the special camera packet to the player.
	 * @param player
	 * @param creature
	 * @param force
	 * @param angle1
	 * @param angle2
	 * @param time
	 * @param duration
	 * @param relYaw
	 * @param relPitch
	 * @param isWide
	 * @param relAngle
	 */
	public static final void specialCameraEx(L2PcInstance player, L2Character creature, int force, int angle1, int angle2, int time, int duration, int relYaw, int relPitch, int isWide, int relAngle)
	{
		player.sendPacket(new SpecialCamera(creature, player, force, angle1, angle2, time, duration, relYaw, relPitch, isWide, relAngle));
	}
	
	/**
	 * Sends the special camera packet to the player.
	 * @param player
	 * @param creature
	 * @param force
	 * @param angle1
	 * @param angle2
	 * @param time
	 * @param range
	 * @param duration
	 * @param relYaw
	 * @param relPitch
	 * @param isWide
	 * @param relAngle
	 * @param unk
	 */
	public static final void specialCamera3(L2PcInstance player, L2Character creature, int force, int angle1, int angle2, int time, int range, int duration, int relYaw, int relPitch, int isWide, int relAngle, int unk)
	{
		player.sendPacket(new SpecialCamera(creature, force, angle1, angle2, time, range, duration, relYaw, relPitch, isWide, relAngle, unk));
	}
	
	/**
	 * @param player
	 * @param x
	 * @param y
	 * @param z
	 */
	public static void addRadar(L2PcInstance player, int x, int y, int z)
	{
		player.getRadar().addMarker(x, y, z);
	}
	
	/**
	 * @param player
	 * @param x
	 * @param y
	 * @param z
	 */
	public void removeRadar(L2PcInstance player, int x, int y, int z)
	{
		player.getRadar().removeMarker(x, y, z);
	}
	
	/**
	 * @param player
	 */
	public void clearRadar(L2PcInstance player)
	{
		player.getRadar().removeAllMarkers();
	}
}