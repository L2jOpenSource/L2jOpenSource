package net.sf.l2j.gameserver.model.actor.ai.type;

import java.util.List;
import java.util.concurrent.Future;

import net.sf.l2j.commons.concurrent.ThreadPool;
import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.enums.actors.NpcSkillType;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.TamedBeast;
import net.sf.l2j.gameserver.model.holder.SkillUseHolder;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.skills.L2Skill;

public class TamedBeastAI extends AttackableAI
{
	private static final int MAX_DISTANCE_FROM_HOME = 13000;
	private static final int TASK_INTERVAL = 5000;
	
	// Messages used every minute by the tamed beast when he automatically eats food.
	protected static final String[] FOOD_CHAT =
	{
		"Refills! Yeah!",
		"I am such a gluttonous beast, it is embarrassing! Ha ha.",
		"Your cooperative feeling has been getting better and better.",
		"I will help you!",
		"The weather is really good. Wanna go for a picnic?",
		"I really like you! This is tasty...",
		"If you do not have to leave this place, then I can help you.",
		"What can I help you with?",
		"I am not here only for food!",
		"Yam, yam, yam, yam, yam!"
	};
	
	private int _step;
	private Future<?> _aiTask = null;
	
	public TamedBeastAI(TamedBeast tamedBeast)
	{
		super(tamedBeast);
		
		// Create an AI task (schedule onEvtThink every second).
		if (_aiTask == null)
			_aiTask = ThreadPool.scheduleAtFixedRate(this, 1000, TASK_INTERVAL);
	}
	
	@Override
	public void run()
	{
		final Player owner = getOwner();
		
		// Check if the owner is no longer around. If so, despawn.
		if (owner == null || !owner.isOnline())
		{
			getActor().deleteMe();
			return;
		}
		
		// Happens every 60s.
		if (++_step > 12)
		{
			// Verify first if the tamed beast is still in the good range. If not, delete it.
			if (!getActor().isIn2DRadius(52335, -83086, MAX_DISTANCE_FROM_HOME))
			{
				getActor().deleteMe();
				return;
			}
			
			// Destroy the food from owner's inventory ; if none is found, delete the pet.
			if (!owner.destroyItemByItemId("BeastMob", getActor().getFoodId(), 1, getActor(), true))
			{
				getActor().deleteMe();
				return;
			}
			
			getActor().broadcastPacket(new SocialAction(getActor(), 2));
			getActor().broadcastNpcSay(Rnd.get(FOOD_CHAT));
			
			_step = 0;
		}
		
		// If the owner is dead or if the tamed beast is currently casting a spell,do nothing.
		if (owner.isDead())
			return;
		
		int totalBuffsOnOwner = 0;
		int i = 0;
		L2Skill buffToGive = null;
		
		final List<L2Skill> skills = getActor().getTemplate().getSkills(NpcSkillType.BUFF);
		final int rand = Rnd.get(skills.size());
		
		// Retrieve the random buff, and check how much tamed beast buffs the player has.
		for (final L2Skill skill : skills)
		{
			if (i == rand)
				buffToGive = skill;
			
			i++;
			
			if (owner.getFirstEffect(skill) != null)
				totalBuffsOnOwner++;
		}
		
		// If the owner has less than 2 buffs, cast the chosen buff.
		if (totalBuffsOnOwner < 2 && owner.getFirstEffect(buffToGive) == null)
			tryTo(IntentionType.CAST, new SkillUseHolder(buffToGive, owner, false, false), null);
		else
			tryTo(IntentionType.FOLLOW, owner, false);
	}
	
	@Override
	protected void onOwnerAttacked(Creature attacker)
	{
		// Check if the owner is no longer around. If so, despawn.
		if (getOwner() == null || !getOwner().isOnline())
		{
			getActor().deleteMe();
			return;
		}
		
		// If the owner is dead or if the tamed beast is currently casting a spell,do nothing.
		if (getOwner().isDead())
			return;
		
		final int proba = Rnd.get(3);
		
		// Heal, 33% luck.
		if (proba == 0)
		{
			// Happen only when owner's HPs < 50%
			final float HPRatio = ((float) getOwner().getCurrentHp()) / getOwner().getMaxHp();
			if (HPRatio < 0.5)
			{
				for (final L2Skill skill : getActor().getTemplate().getSkills(NpcSkillType.HEAL))
				{
					switch (skill.getSkillType())
					{
						case HEAL:
						case HOT:
						case BALANCE_LIFE:
						case HEAL_PERCENT:
						case HEAL_STATIC:
							tryTo(IntentionType.CAST, new SkillUseHolder(skill, getOwner(), false, false), null);
							return;
					}
				}
			}
		}
		// Debuff, 33% luck.
		else if (proba == 1)
		{
			for (final L2Skill skill : getActor().getTemplate().getSkills(NpcSkillType.DEBUFF))
			{
				// if the skill is a debuff, check if the attacker has it already
				if (attacker.getFirstEffect(skill) == null)
				{
					tryTo(IntentionType.CAST, new SkillUseHolder(skill, attacker, false, false), null);
					return;
				}
			}
		}
		// Recharge, 33% luck.
		else if (proba == 2)
		{
			// Happen only when owner's MPs < 50%
			final float MPRatio = ((float) getOwner().getCurrentMp()) / getOwner().getMaxMp();
			if (MPRatio < 0.5)
			{
				for (final L2Skill skill : getActor().getTemplate().getSkills(NpcSkillType.HEAL))
				{
					switch (skill.getSkillType())
					{
						case MANARECHARGE:
						case MANAHEAL_PERCENT:
							tryTo(IntentionType.CAST, new SkillUseHolder(skill, getOwner(), false, false), null);
							return;
					}
				}
			}
		}
	}
	
	@Override
	protected void onEvtFinishedCasting(Boolean success)
	{
		if (success)
		{
			if (_nextIntention.isBlank())
				changeCurrentIntention(IntentionType.FOLLOW, getOwner(), false);
			else
				changeCurrentIntention(_nextIntention);
		}
		else
			setNextIntention(IntentionType.FOLLOW, getOwner(), false);
	}
	
	@Override
	public TamedBeast getActor()
	{
		return (TamedBeast) _actor;
	}
	
	private Player getOwner()
	{
		return getActor().getOwner();
	}
	
}
