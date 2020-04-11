package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.enums.AiEventType;
import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.enums.skills.L2SkillType;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.ai.NextAction;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;

public final class RequestMagicSkillUse extends L2GameClientPacket
{
	private int _skillId;
	protected boolean _ctrlPressed;
	protected boolean _shiftPressed;
	
	@Override
	protected void readImpl()
	{
		_skillId = readD();
		_ctrlPressed = readD() != 0;
		_shiftPressed = readC() != 0;
	}
	
	@Override
	protected void runImpl()
	{
		// Get the current player
		final Player player = getClient().getPlayer();
		if (player == null)
			return;
		
		// Get the L2Skill template corresponding to the skillID received from the client
		final L2Skill skill = player.getSkill(_skillId);
		if (skill == null)
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// If Alternate rule Karma punishment is set to true, forbid skill Return to player with Karma
		if (skill.getSkillType() == L2SkillType.RECALL && !Config.KARMA_PLAYER_CAN_TELEPORT && player.getKarma() > 0)
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// players mounted on pets cannot use any toggle skills
		if (skill.isToggle() && player.isMounted())
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.isOutOfControl())
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.isAttackingNow())
			player.getAI().setNextAction(new NextAction(AiEventType.READY_TO_ACT, IntentionType.CAST, () -> player.useMagic(skill, _ctrlPressed, _shiftPressed)));
		else
			player.useMagic(skill, _ctrlPressed, _shiftPressed);
	}
}