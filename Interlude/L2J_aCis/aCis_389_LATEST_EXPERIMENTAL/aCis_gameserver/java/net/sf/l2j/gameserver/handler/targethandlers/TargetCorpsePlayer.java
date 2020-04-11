package net.sf.l2j.gameserver.handler.targethandlers;

import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.enums.skills.SkillTargetType;
import net.sf.l2j.gameserver.enums.skills.SkillType;
import net.sf.l2j.gameserver.handler.ITargetHandler;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.Pet;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.L2Skill;

public class TargetCorpsePlayer implements ITargetHandler
{
	@Override
	public WorldObject[] getTargetList(L2Skill skill, Creature caster, Creature target, boolean onlyFirst)
	{
		if (!(caster instanceof Player))
			return EMPTY_TARGET_ARRAY;
		
		if (target != null && target.isDead())
		{
			final Player targetPlayer;
			if (target instanceof Player)
				targetPlayer = (Player) target;
			else
				targetPlayer = null;
			
			final Pet targetPet;
			if (target instanceof Pet)
				targetPet = (Pet) target;
			else
				targetPet = null;
			
			if (targetPlayer != null || targetPet != null)
			{
				boolean condGood = true;
				
				if (skill.getSkillType() == SkillType.RESURRECT)
				{
					final Player player = (Player) caster;
					
					if (targetPlayer != null)
					{
						// Check if the target isn't in a active siege zone.
						if (targetPlayer.isInsideZone(ZoneId.SIEGE) && !targetPlayer.isInSiege())
						{
							condGood = false;
							caster.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_BE_RESURRECTED_DURING_SIEGE));
						}
						
						// Check if the target is in a festival.
						if (targetPlayer.isFestivalParticipant())
						{
							condGood = false;
							caster.sendMessage("You may not resurrect participants in a festival.");
						}
						
						if (targetPlayer.isReviveRequested())
						{
							if (targetPlayer.isRevivingPet())
								player.sendPacket(SystemMessageId.MASTER_CANNOT_RES);
							else
								player.sendPacket(SystemMessageId.RES_HAS_ALREADY_BEEN_PROPOSED);
							
							condGood = false;
						}
					}
					else if (targetPet != null)
					{
						if (targetPet.getOwner() != player)
						{
							if (targetPet.getOwner().isReviveRequested())
							{
								if (targetPet.getOwner().isRevivingPet())
									player.sendPacket(SystemMessageId.RES_HAS_ALREADY_BEEN_PROPOSED);
								else
									player.sendPacket(SystemMessageId.CANNOT_RES_PET2);
								
								condGood = false;
							}
						}
					}
				}
				
				if (condGood)
					return new Creature[]
					{
						target
					};
			}
		}
		caster.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
		return EMPTY_TARGET_ARRAY;
	}
	
	@Override
	public SkillTargetType getTargetType()
	{
		return SkillTargetType.CORPSE_PLAYER;
	}
}