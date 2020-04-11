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
package l2r.gameserver.network.clientpackets;

import l2r.Config;
import l2r.gameserver.enums.CtrlIntention;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.model.skills.L2SkillType;
import l2r.gameserver.model.skills.targets.L2TargetType;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.ActionFailed;
import gr.reunion.interf.ReunionEvents;

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
		
		// Get the level of the used skill
		L2Skill skill = activeChar.getKnownSkill(_magicId);
		if (skill == null)
		{
			// Player doesn't know this skill, maybe it's the display Id.
			skill = activeChar.getCustomSkill(_magicId);
			if (skill == null)
			{
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				if (Config.DEBUG)
				{
					_log.warn("Skill Id " + _magicId + " not found in player!");
				}
				return;
			}
		}
		
		// Avoid Use of Skills in AirShip.
		if (activeChar.isPlayable() && activeChar.isInAirShip())
		{
			activeChar.sendPacket(SystemMessageId.ACTION_PROHIBITED_WHILE_MOUNTED_OR_ON_AN_AIRSHIP);
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		boolean allow = true;
		if (activeChar.isTransformed() || activeChar.isInStance())
		{
			if (ReunionEvents.isInEvent(activeChar))
			{
				int allowSkill = ReunionEvents.allowTransformationSkill(activeChar, skill);
				
				if (allowSkill == -1)
				{
					allow = false;
				}
				else if (allowSkill == 0)
				{
					if (!activeChar.hasTransformSkill(skill.getId()))
					{
						allow = false;
					}
				}
			}
			else if (!activeChar.hasTransformSkill(skill.getId()))
			{
				allow = false;
			}
		}
		
		if (!allow)
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (activeChar.isDebug())
		{
			_log.info("Debugging: " + activeChar.getName());
			_log.info("Skill:" + skill.getName() + " level:" + skill.getLevel() + " passive:" + skill.isPassive());
			_log.info("Range:" + skill.getCastRange() + " targettype:" + skill.getTargetType() + " power:" + skill.getPower());
			_log.info("Reusedelay:" + skill.getReuseDelay() + " hittime:" + skill.getHitTime());
		}
		
		// If Alternate rule Karma punishment is set to true, forbid skill Return to player with Karma
		if ((skill.getSkillType() == L2SkillType.RECALL) && !Config.ALT_GAME_KARMA_PLAYER_CAN_TELEPORT && (activeChar.getKarma() > 0))
		{
			return;
		}
		
		// players mounted on pets cannot use any toggle skills
		if (skill.isToggle() && activeChar.isMounted())
		{
			return;
		}
		
		// Stop if use self-buff (except if on AirShip or Boat).
		if (((skill.getSkillType() == L2SkillType.BUFF) && (skill.getTargetType() == L2TargetType.SELF)) && (!activeChar.isInAirShip() || !activeChar.isInBoat()))
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
