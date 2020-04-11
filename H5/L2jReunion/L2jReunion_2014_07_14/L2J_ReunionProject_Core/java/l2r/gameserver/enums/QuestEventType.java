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
package l2r.gameserver.enums;

/**
 * @author unknown
 */
public enum QuestEventType
{
	ON_FIRST_TALK(false), // control the first dialog shown by NPCs when they are clicked (some quests must override the default npc action)
	QUEST_START(true), // onTalk action from start npcs
	ON_TALK(true), // onTalk action from npcs participating in a quest
	ON_ATTACK(true), // onAttack action triggered when a mob gets attacked by someone
	ON_KILL(true), // onKill action triggered when a mob gets killed.
	ON_SPAWN(true), // onSpawn action triggered when an NPC is spawned or respawned.
	ON_SKILL_SEE(true), // NPC or Mob saw a person casting a skill (regardless what the target is).
	ON_FACTION_CALL(true), // NPC or Mob saw a person casting a skill (regardless what the target is).
	ON_AGGRO_RANGE_ENTER(true), // a person came within the Npc/Mob's range
	ON_SPELL_FINISHED(true), // on spell finished action when npc finish casting skill
	ON_SKILL_LEARN(false), // control the AcquireSkill dialog from quest script
	ON_ENTER_ZONE(true), // on zone enter
	ON_EXIT_ZONE(true), // on zone exit
	ON_TRAP_ACTION(true), // on zone exit
	ON_ITEM_USE(true),
	ON_EVENT_RECEIVED(true), // onEventReceived action, triggered when NPC receiving an event, sent by other NPC
	ON_MOVE_FINISHED(true), // onMoveFinished action, triggered when NPC stops after moving
	ON_NODE_ARRIVED(true), // onNodeArrived action, triggered when NPC, controlled by Walking Manager, arrives to next node
	ON_SEE_CREATURE(true), // onSeeCreature action, triggered when NPC's known list include the character
	ON_ROUTE_FINISHED(true), // onRouteFinished action, triggered when NPC, controlled by Walking Manager, arrives to last node
	ON_NPC_HATE(true),
	ON_SUMMON(true),
	ON_CAN_SEE_ME(false);
	
	// control whether this event type is allowed for the same npc template in multiple quests
	// or if the npc must be registered in at most one quest for the specified event
	private boolean _allowMultipleRegistration;
	
	private QuestEventType(boolean allowMultipleRegistration)
	{
		_allowMultipleRegistration = allowMultipleRegistration;
	}
	
	public boolean isMultipleRegistrationAllowed()
	{
		return _allowMultipleRegistration;
	}
}
