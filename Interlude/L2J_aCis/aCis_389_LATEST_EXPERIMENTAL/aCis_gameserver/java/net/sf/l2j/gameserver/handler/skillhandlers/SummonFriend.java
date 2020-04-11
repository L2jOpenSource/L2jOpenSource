package net.sf.l2j.gameserver.handler.skillhandlers;

import net.sf.l2j.commons.math.MathUtil;

import net.sf.l2j.gameserver.enums.skills.SkillType;
import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ConfirmDlg;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.L2Skill;

public class SummonFriend implements ISkillHandler
{
	private static final SkillType[] SKILL_IDS =
	{
		SkillType.SUMMON_FRIEND
	};
	
	@Override
	public void useSkill(Creature activeChar, L2Skill skill, WorldObject[] targets)
	{
		if (!(activeChar instanceof Player))
			return;
		
		final Player player = (Player) activeChar;
		
		// Check player status.
		if (!player.checkSummonerStatus())
			return;
		
		for (WorldObject obj : targets)
		{
			// The target must be a player.
			if (!(obj instanceof Player))
				continue;
			
			// Can't summon yourself.
			final Player target = ((Player) obj);
			if (activeChar == target)
				continue;
			
			// Check target status.
			if (!player.checkSummonTargetStatus(target))
				continue;
			
			// Check target distance.
			if (MathUtil.checkIfInRange(50, activeChar, target, false))
				continue;
			
			// Check target teleport request status.
			if (!target.teleportRequest(player, skill))
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_ALREADY_SUMMONED).addCharName(target));
				continue;
			}
			
			// Send a request for Summon Friend skill.
			if (skill.getId() == 1403)
			{
				final ConfirmDlg confirm = new ConfirmDlg(SystemMessageId.S1_WISHES_TO_SUMMON_YOU_FROM_S2_DO_YOU_ACCEPT.getId());
				confirm.addCharName(player);
				confirm.addZoneName(activeChar.getPosition());
				confirm.addTime(30000);
				confirm.addRequesterId(player.getObjectId());
				target.sendPacket(confirm);
			}
			else
			{
				target.teleportToFriend(player, skill);
				target.teleportRequest(null, null);
			}
		}
	}
	
	@Override
	public SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}