package net.sf.l2j.gameserver.handler.skillhandlers;

import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.enums.skills.L2SkillType;
import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.Monster;
import net.sf.l2j.gameserver.model.group.Party;
import net.sf.l2j.gameserver.model.manor.Seed;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.PlaySound;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Sow implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.SOW
	};
	
	@Override
	public void useSkill(Creature activeChar, L2Skill skill, WorldObject[] targets)
	{
		if (!(activeChar instanceof Player))
			return;
		
		final WorldObject object = targets[0];
		if (!(object instanceof Monster))
			return;
		
		final Player player = (Player) activeChar;
		final Monster target = (Monster) object;
		
		if (target.isDead() || !target.isSeeded() || target.getSeederId() != activeChar.getObjectId())
			return;
		
		final Seed seed = target.getSeed();
		if (seed == null)
			return;
		
		// Consuming used seed
		if (!activeChar.destroyItemByItemId("Consume", seed.getSeedId(), 1, target, false))
			return;
		
		SystemMessageId smId;
		if (calcSuccess(activeChar, target, seed))
		{
			player.sendPacket(new PlaySound(QuestState.SOUND_ITEMGET));
			target.setSeeded(activeChar.getObjectId());
			smId = SystemMessageId.THE_SEED_WAS_SUCCESSFULLY_SOWN;
		}
		else
			smId = SystemMessageId.THE_SEED_WAS_NOT_SOWN;
		
		final Party party = player.getParty();
		if (party == null)
			player.sendPacket(smId);
		else
			party.broadcastMessage(smId);
		
		target.getAI().setIntention(IntentionType.IDLE);
	}
	
	private static boolean calcSuccess(Creature activeChar, Creature target, Seed seed)
	{
		final int minlevelSeed = seed.getLevel() - 5;
		final int maxlevelSeed = seed.getLevel() + 5;
		
		final int levelPlayer = activeChar.getLevel(); // Attacker Level
		final int levelTarget = target.getLevel(); // target Level
		
		int basicSuccess = (seed.isAlternative()) ? 20 : 90;
		
		// Seed level
		if (levelTarget < minlevelSeed)
			basicSuccess -= 5 * (minlevelSeed - levelTarget);
		
		if (levelTarget > maxlevelSeed)
			basicSuccess -= 5 * (levelTarget - maxlevelSeed);
		
		// 5% decrease in chance if player level is more than +/- 5 levels to _target's_ level
		int diff = (levelPlayer - levelTarget);
		if (diff < 0)
			diff = -diff;
		
		if (diff > 5)
			basicSuccess -= 5 * (diff - 5);
		
		// Chance can't be less than 1%
		if (basicSuccess < 1)
			basicSuccess = 1;
		
		return Rnd.get(99) < basicSuccess;
	}
	
	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}