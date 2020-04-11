/*
 * Copyright (C) 2004-2015 L2J Server
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
package l2r.gameserver.model.effects;

/**
 * Effect types.
 * @author nBd
 */
public enum L2EffectType
{
	ACTION_BLOCK,
	BLOCK_BUFF, // confirmed
	BLOCK_DAMAGE, // confirmed
	BLOCK_DEBUFF, // confirmed
	BUFF, // confirmed
	CANCEL,
	CHAT_BLOCK, // confirmed
	CONSUME_BODY,
	CPHEAL, // confirmed
	CPHEAL_OVER_TIME,
	DEBUFF, // confirmed
	DISPEL, // confirmed
	DISPEL_BY_SLOT, // confirmed
	DMG_OVER_TIME, // confirmed
	DMG_OVER_TIME_PERCENT, // confirmed
	DEATH_LINK, // confirmed
	FAKE_DEATH, // confirmed
	FEAR, // confirmed
	FISHING, // confirmed
	FISHING_START, // confirmed
	HATE, // confirmed
	HEAL, // confirmed
	HEAL_OVER_TIME,
	HEAL_PERCENT,
	HIDE,
	INVINCIBLE,
	MANA_HEAL_OVER_TIME,
	MAGICAL_ATTACK_MP, // confirmed
	MANAHEAL_BY_LEVEL, // confirmed
	MANAHEAL_PERCENT, // confirmed
	MUTE, // confirmed
	NOBLESSE_BLESSING, // confirmed
	NONE, // confirmed
	PARALYZE, // confirmed
	PHYSICAL_ATTACK, // confirmed
	PHYSICAL_ATTACK_HP_LINK, // confirmed
	REBALANCE_HP, // confirmed
	REFUEL_AIRSHIP, // confirmed
	RELAXING, // confirmed
	RESURRECTION, // confirmed
	RESURRECTION_SPECIAL, // confirmed
	ROOT, // confirmed
	SIGNET_EFFECT,
	SIGNET_GROUND,
	SLEEP, // confirmed
	SPOIL,
	SWEEP,
	STUN, // confirmed
	SINGLE_TARGET, // confirmed
	SUMMON_PET, // confirmed
	SUMMON_NPC, // confirmed
	TRANSFORMATION,
	TELEPORT_TO_TARGET, // confirmed
	STEAL_ABNORMAL,
	PASSIVE,
	REMOVE_TARGET
}