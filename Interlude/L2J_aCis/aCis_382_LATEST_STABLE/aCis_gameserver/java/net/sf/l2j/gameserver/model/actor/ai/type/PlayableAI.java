package net.sf.l2j.gameserver.model.actor.ai.type;

import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.SystemMessageId;

public abstract class PlayableAI extends CreatureAI
{
	public PlayableAI(Playable playable)
	{
		super(playable);
	}
	
	@Override
	protected void onIntentionAttack(Creature target)
	{
		if (target instanceof Playable)
		{
			final Player targetPlayer = target.getActingPlayer();
			final Player actorPlayer = _actor.getActingPlayer();
			
			if (!target.isInsideZone(ZoneId.PVP))
			{
				if (targetPlayer.getProtectionBlessing() && (actorPlayer.getLevel() - targetPlayer.getLevel()) >= 10 && actorPlayer.getKarma() > 0)
				{
					// If attacker have karma, level >= 10 and target have Newbie Protection Buff
					actorPlayer.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
					clientActionFailed();
					return;
				}
				
				if (actorPlayer.getProtectionBlessing() && (targetPlayer.getLevel() - actorPlayer.getLevel()) >= 10 && targetPlayer.getKarma() > 0)
				{
					// If target have karma, level >= 10 and actor have Newbie Protection Buff
					actorPlayer.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
					clientActionFailed();
					return;
				}
			}
			
			if (targetPlayer.isCursedWeaponEquipped() && actorPlayer.getLevel() <= 20)
			{
				actorPlayer.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
				clientActionFailed();
				return;
			}
			
			if (actorPlayer.isCursedWeaponEquipped() && targetPlayer.getLevel() <= 20)
			{
				actorPlayer.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
				clientActionFailed();
				return;
			}
		}
		super.onIntentionAttack(target);
	}
	
	@Override
	protected void onIntentionCast(L2Skill skill, WorldObject target)
	{
		if (target instanceof Playable && skill.isOffensive())
		{
			final Player targetPlayer = target.getActingPlayer();
			final Player actorPlayer = _actor.getActingPlayer();
			
			if (!target.isInsideZone(ZoneId.PVP))
			{
				if (targetPlayer.getProtectionBlessing() && (actorPlayer.getLevel() - targetPlayer.getLevel()) >= 10 && actorPlayer.getKarma() > 0)
				{
					// If attacker have karma, level >= 10 and target have Newbie Protection Buff
					actorPlayer.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
					clientActionFailed();
					_actor.setIsCastingNow(false);
					return;
				}
				
				if (actorPlayer.getProtectionBlessing() && (targetPlayer.getLevel() - actorPlayer.getLevel()) >= 10 && targetPlayer.getKarma() > 0)
				{
					// If target have karma, level >= 10 and actor have Newbie Protection Buff
					actorPlayer.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
					clientActionFailed();
					_actor.setIsCastingNow(false);
					return;
				}
			}
			
			if (targetPlayer.isCursedWeaponEquipped() && actorPlayer.getLevel() <= 20)
			{
				actorPlayer.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
				clientActionFailed();
				_actor.setIsCastingNow(false);
				return;
			}
			
			if (actorPlayer.isCursedWeaponEquipped() && targetPlayer.getLevel() <= 20)
			{
				actorPlayer.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
				clientActionFailed();
				_actor.setIsCastingNow(false);
				return;
			}
		}
		super.onIntentionCast(skill, target);
	}
}