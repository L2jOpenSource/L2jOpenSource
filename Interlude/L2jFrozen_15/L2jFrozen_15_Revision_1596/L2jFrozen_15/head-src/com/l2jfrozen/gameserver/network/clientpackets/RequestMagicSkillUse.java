package com.l2jfrozen.gameserver.network.clientpackets;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.L2Skill.SkillType;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;

/**
 * This class ...
 * @version $Revision: 1.7.2.1.2.3 $ $Date: 2005/03/27 15:29:30 $
 */
public final class RequestMagicSkillUse extends L2GameClientPacket
{
	private static Logger LOGGER = Logger.getLogger(RequestMagicSkillUse.class);
	
	private int magicId;
	private boolean ctrlPressed;
	private boolean shiftPressed;
	
	@Override
	protected void readImpl()
	{
		magicId = readD(); // Identifier of the used skill
		ctrlPressed = readD() != 0; // True if it's a ForceAttack : Ctrl pressed
		shiftPressed = readC() != 0; // True if Shift pressed
	}
	
	@Override
	protected void runImpl()
	{
		// Get the current L2PcInstance of the player
		final L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
		{
			return;
		}
		
		// Get the level of the used skill
		final int level = activeChar.getSkillLevel(magicId);
		if (level <= 0)
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (activeChar.isOutOfControl())
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Get the L2Skill template corresponding to the skillID received from the client
		final L2Skill skill = SkillTable.getInstance().getInfo(magicId, level);
		
		// Check the validity of the skill
		if (skill != null)
		{
			
			// LOGGER.fine(" [FINE] skill:"+skill.getName() + " level:"+skill.getLevel() + " passive:"+skill.isPassive());
			// LOGGER.fine(" [FINE] range:"+skill.getCastRange()+" targettype:"+skill.getTargetType()+" optype:"+skill.getOperateType()+" power:"+skill.getPower());
			// LOGGER.fine(" [FINE] reusedelay:"+skill.getReuseDelay()+" hittime:"+skill.getHitTime());
			// LOGGER.fine(" [FINE] currentState:"+activeChar.getCurrentState()); //for debug
			
			// If Alternate rule Karma punishment is set to true, forbid skill Return to player with Karma
			if (skill.getSkillType() == SkillType.RECALL && !Config.ALT_GAME_KARMA_PLAYER_CAN_TELEPORT && activeChar.getKarma() > 0)
			{
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			// players mounted on pets cannot use any toggle skills
			if (skill.isToggle() && activeChar.isMounted())
			{
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			activeChar.useMagic(skill, ctrlPressed, shiftPressed);
		}
		else
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			LOGGER.warn("No skill found with id " + magicId + " and level " + level + " !!");
		}
	}
	
	@Override
	public String getType()
	{
		return "[C] 2F RequestMagicSkillUse";
	}
}