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
package com.l2jserver.gameserver.network.clientpackets;

import com.l2jserver.Config;
import com.l2jserver.gameserver.ai.CtrlIntention;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.effects.L2EffectType;
import com.l2jserver.gameserver.model.skills.Skill;
import com.l2jserver.gameserver.model.skills.targets.L2TargetType;
import com.l2jserver.gameserver.network.SystemMessageId;
import com.l2jserver.gameserver.network.serverpackets.ActionFailed;

public final class RequestMagicSkillUse extends L2GameClientPacket
{
	private static final String _C__39_REQUESTMAGICSKILLUSE = "[C] 39 RequestMagicSkillUse";
	
	private int _magicId;
	private boolean _ctrlPressed;
	private boolean _shiftPressed;
	
	@Override
	protected void readImpl()
	{
		_magicId = readD(); // Identifier of the used skill
		_ctrlPressed = readD() != 0; // True if it's a ForceAttack : Ctrl pressed
		_shiftPressed = readC() != 0; // True if Shift pressed
	}
	
	@Override
	protected void runImpl()
	{
		// Get the current L2PcInstance of the player
		final L2PcInstance activeChar = getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		if (activeChar.isDead())
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (activeChar.isFakeDeath() && (_magicId == 60))
		{
			activeChar.stopEffects(L2EffectType.FAKE_DEATH);
			return;
		}
		
		if (activeChar.isFakeDeath())
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Abnormal effects(ex : Stun, Sleep...)
		if (activeChar.isOutOfControl() || activeChar.isParalyzed() || activeChar.isStunned() || activeChar.isSleeping())
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Get the level of the used skill
		Skill skill = activeChar.getKnownSkill(_magicId);
		if (skill == null)
		{
			// Player doesn't know this skill, maybe it's the display Id.
			skill = activeChar.getCustomSkill(_magicId);
			if (skill == null)
			{
				skill = activeChar.getTransformSkill(_magicId);
				if (skill == null)
				{
					activeChar.sendPacket(ActionFailed.STATIC_PACKET);
					_log.warning("Skill Id " + _magicId + " not found in player : " + activeChar);
					return;
				}
			}
		}
		
		// Avoid Use of Skills in AirShip.
		if (activeChar.isPlayable() && activeChar.isInAirShip())
		{
			activeChar.sendPacket(SystemMessageId.ACTION_PROHIBITED_WHILE_MOUNTED_OR_ON_AN_AIRSHIP);
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if ((activeChar.isTransformed() || activeChar.isInStance()) && !activeChar.hasTransformSkill(skill.getId()))
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// If Alternate rule Karma punishment is set to true, forbid skill Return to player with Karma
		if (!Config.ALT_GAME_KARMA_PLAYER_CAN_TELEPORT && (activeChar.getKarma() > 0) && skill.hasEffectType(L2EffectType.TELEPORT))
		{
			return;
		}
		
		// players mounted on pets cannot use any toggle skills
		if (skill.isToggle() && activeChar.isMounted())
		{
			return;
		}
		
		// Stop if use self-buff (except if on AirShip or Boat).
		if ((skill.isContinuous() && !skill.isDebuff() && (skill.getTargetType() == L2TargetType.SELF)) && (!activeChar.isInAirShip() || !activeChar.isInBoat()))
		{
			activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, activeChar.getLocation());
		}
		
		activeChar.useMagic(skill, _ctrlPressed, _shiftPressed);
	}
	
	@Override
	public String getType()
	{
		return _C__39_REQUESTMAGICSKILLUSE;
	}
}
