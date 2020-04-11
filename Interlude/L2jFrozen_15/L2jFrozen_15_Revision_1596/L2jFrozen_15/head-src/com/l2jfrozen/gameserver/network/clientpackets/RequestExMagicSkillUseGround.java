package com.l2jfrozen.gameserver.network.clientpackets;

import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.util.Util;
import com.l2jfrozen.util.Point3D;

/**
 * Fromat:(ch) dddddc
 */
public final class RequestExMagicSkillUseGround extends L2GameClientPacket
{
	private int x;
	private int y;
	private int z;
	private int skillId;
	private boolean ctrlPressed;
	private boolean shiftPressed;
	
	@Override
	protected void readImpl()
	{
		x = readD();
		y = readD();
		z = readD();
		skillId = readD();
		ctrlPressed = readD() != 0;
		shiftPressed = readC() != 0;
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		// Get the level of the used skill
		final int level = activeChar.getSkillLevel(skillId);
		if (level <= 0)
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Get the L2Skill template corresponding to the skillID received from the client
		final L2Skill skill = SkillTable.getInstance().getInfo(skillId, level);
		
		if (skill != null)
		{
			activeChar.setCurrentSkillWorldPosition(new Point3D(x, y, z));
			
			// normally magicskilluse packet turns char client side but for these skills, it doesn't (even with correct target)
			activeChar.setHeading(Util.calculateHeadingFrom(activeChar.getX(), activeChar.getY(), x, y));
			
			// TODO: Send a valide position and broadcast the new heading.
			// Putting a simple Validelocation chars can go up of wall spamming on position and clicking on a SIGNET
			// activeChar.broadcastPacket(new ValidateLocation(activeChar));
			
			activeChar.useMagic(skill, ctrlPressed, shiftPressed);
		}
		else
		{
			sendPacket(ActionFailed.STATIC_PACKET);
		}
	}
	
	@Override
	public String getType()
	{
		return "[C] D0:2F RequestExMagicSkillUseGround";
	}
}
