package net.sf.l2j.gameserver.scripting.scripts.ai;

import java.util.ArrayList;
import java.util.List;

import net.sf.l2j.commons.math.MathUtil;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.commons.util.ArraysUtil;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.data.SkillTable.FrequentSkill;
import net.sf.l2j.gameserver.data.xml.NpcData;
import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.enums.ScriptEventType;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Attackable;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.Monster;
import net.sf.l2j.gameserver.model.actor.instance.RiftInvader;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUse;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.skills.L2Skill;

public class L2AttackableAIScript extends Quest
{
	private static final String ACTOR_INSTANCE_PACKAGE = "net.sf.l2j.gameserver.model.actor.instance.";
	
	/**
	 * Implicit constructor for generic AI script.<br>
	 * It is used by default for all {@link Attackable} instances.
	 */
	public L2AttackableAIScript()
	{
		super(-1, "ai");
		
		registerNpcs();
	}
	
	/**
	 * A superclass constructor for all inherited AI scripts.<br>
	 * Inherited AI provides special behavior for particular {@link Attackable} instances.
	 * @param descr : The path/package of the AI script.
	 */
	protected L2AttackableAIScript(String descr)
	{
		super(-1, descr);
		
		registerNpcs();
	}
	
	/**
	 * Registers this AI script to the {@link Attackable}'s {@link NpcTemplate} for various {@link ScriptEventType} events.<br>
	 * All inherited AI scripts must override this method and register only to related {@link NpcTemplate}s + {@link ScriptEventType}s.<br>
	 * Every overridden {@link ScriptEventType} replaces default {@link L2AttackableAIScript} with the new AI script.
	 */
	protected void registerNpcs()
	{
		// register all mobs here...
		for (final NpcTemplate template : NpcData.getInstance().getAllNpcs())
		{
			try
			{
				if (Attackable.class.isAssignableFrom(Class.forName(ACTOR_INSTANCE_PACKAGE + template.getType())))
				{
					template.addQuestEvent(ScriptEventType.ON_ATTACK, this);
					template.addQuestEvent(ScriptEventType.ON_KILL, this);
					template.addQuestEvent(ScriptEventType.ON_SPAWN, this);
					template.addQuestEvent(ScriptEventType.ON_SKILL_SEE, this);
					template.addQuestEvent(ScriptEventType.ON_FACTION_CALL, this);
					template.addQuestEvent(ScriptEventType.ON_AGGRO, this);
				}
			}
			catch (final ClassNotFoundException e)
			{
				LOGGER.error("An unknown template type {} has been found on {}.", e, template.getType(), toString());
			}
		}
	}
	
	@Override
	public String onSkillSee(Npc npc, Player caster, L2Skill skill, WorldObject[] targets, boolean isPet)
	{
		if (caster == null)
			return null;
		
		if (!(npc instanceof Attackable))
			return null;
		
		final Attackable attackable = (Attackable) npc;
		int skillAggroPoints = skill.getAggroPoints();
		
		// Do not hate if the skill is a solo target skill, and if the target is player summon OR if the target is the npc and the skill was a positive effect.
		if (targets.length == 1 && ((caster.getSummon() != null && ArraysUtil.contains(targets, caster.getSummon())) || (!skill.isOffensive() && !skill.isDebuff() && ArraysUtil.contains(targets, npc))))
			skillAggroPoints = 0;
		
		if (skillAggroPoints > 0 && attackable.hasAI() && attackable.getAI().getCurrentIntention().getType() == IntentionType.ATTACK)
		{
			final WorldObject npcTarget = attackable.getTarget();
			for (final WorldObject skillTarget : targets)
			{
				if (npcTarget == skillTarget || npc == skillTarget)
				{
					final Creature originalCaster = isPet ? caster.getSummon() : caster;
					attackable.addDamageHate(originalCaster, 0, (skillAggroPoints * 150) / (attackable.getLevel() + 7));
				}
			}
		}
		return null;
	}
	
	@Override
	public String onFactionCall(Npc npc, Npc caller, Player attacker, boolean isPet)
	{
		if (attacker == null)
			return null;
		
		if (caller instanceof RiftInvader && attacker.isInParty() && attacker.getParty().isInDimensionalRift() && !attacker.getParty().getDimensionalRift().isInCurrentRoomZone(npc))
			return null;
		
		final Attackable attackable = (Attackable) npc;
		final Creature originalAttackTarget = (isPet ? attacker.getSummon() : attacker);
		
		// Add the target to the actor _aggroList or update hate if already present
		attackable.addDamageHate(originalAttackTarget, 0, 1);
		
		// Set the actor AI Intention to ATTACK
		if (attackable.getAI().getCurrentIntention().getType() != IntentionType.ATTACK)
		{
			// Set the Creature movement type to run and send Server->Client packet ChangeMoveType to all others Player
			attackable.forceRunStance();
			
			attackable.getAI().tryTo(IntentionType.ATTACK, originalAttackTarget, null);
		}
		return null;
	}
	
	@Override
	public String onAggro(Npc npc, Player player, boolean isPet)
	{
		if (player == null)
			return null;
		
		((Attackable) npc).addDamageHate(isPet ? player.getSummon() : player, 0, 1);
		return null;
	}
	
	@Override
	public String onAttack(Npc npc, Creature attacker, int damage, L2Skill skill)
	{
		((Attackable) npc).addDamageHate(attacker, damage, (damage * 100) / (npc.getLevel() + 7));
		return null;
	}
	
	@Override
	public String onKill(Npc npc, Creature killer)
	{
		if (npc instanceof Monster)
		{
			final Monster monster = (Monster) npc;
			final Monster master = monster.getMaster();
			
			if (master != null)
				master.getMinionList().onMinionDie(monster, (master.isRaidBoss()) ? Config.RAID_MINION_RESPAWN_TIMER : (master.getSpawn().getRespawnDelay() * 1000 / 2));
			
			if (monster.hasMinions())
				monster.getMinionList().onMasterDie();
		}
		return null;
	}
	
	/**
	 * This method selects a random player.<br>
	 * Player can't be dead and isn't an hidden GM aswell.
	 * @param npc to check.
	 * @return the random player.
	 */
	public static Player getRandomPlayer(Npc npc)
	{
		final List<Player> result = new ArrayList<>();
		
		for (final Player player : npc.getKnownType(Player.class))
		{
			if (player.isDead())
				continue;
			
			if (player.isGM() && !player.getAppearance().isVisible())
				continue;
			
			result.add(player);
		}
		
		return (result.isEmpty()) ? null : Rnd.get(result);
	}
	
	/**
	 * Return the number of players in a defined radius.<br>
	 * Dead players aren't counted, invisible ones is the boolean parameter.
	 * @param range : the radius.
	 * @param npc : the object to make the test on.
	 * @param invisible : true counts invisible characters.
	 * @return the number of targets found.
	 */
	public static int getPlayersCountInRadius(int range, Creature npc, boolean invisible)
	{
		int count = 0;
		for (final Player player : npc.getKnownTypeInRadius(Player.class, range))
		{
			if (player.isDead())
				continue;
			
			if (!invisible && !player.getAppearance().isVisible())
				continue;
			
			count++;
		}
		return count;
	}
	
	/**
	 * Under that barbarian name, return the number of players in front, back and sides of the npc.<br>
	 * Dead players aren't counted, invisible ones is the boolean parameter.
	 * @param range : the radius.
	 * @param npc : the object to make the test on.
	 * @param invisible : true counts invisible characters.
	 * @return an array composed of front, back and side targets number.
	 */
	public static int[] getPlayersCountInPositions(int range, Creature npc, boolean invisible)
	{
		int frontCount = 0;
		int backCount = 0;
		int sideCount = 0;
		
		for (final Player player : npc.getKnownType(Player.class))
		{
			if (player.isDead())
				continue;
			
			if (!invisible && !player.getAppearance().isVisible())
				continue;
			
			if (!MathUtil.checkIfInRange(range, npc, player, true))
				continue;
			
			if (player.isInFrontOf(npc))
				frontCount++;
			else if (player.isBehind(npc))
				backCount++;
			else
				sideCount++;
		}
		
		final int[] array =
		{
			frontCount,
			backCount,
			sideCount
		};
		return array;
	}
	
	/**
	 * Set an {@link Attackable} intention to ATTACK, and attacks the chosen {@link Creature}.
	 * @param npc : The Attackable who is attacking the target.
	 * @param victim : The Creature to attack.
	 * @param aggro : The aggro to add, 999 if not given.
	 */
	protected static void attack(Attackable npc, Creature victim, int aggro)
	{
		npc.forceRunStance();
		npc.addDamageHate(victim, 0, (aggro <= 0) ? 999 : aggro);
		npc.getAI().tryTo(IntentionType.ATTACK, victim, null);
	}
	
	protected static void attack(Attackable npc, Creature victim)
	{
		attack(npc, victim, 0);
	}
	
	/**
	 * Test and cast curses once a {@link Creature} attacks a {@link Npc}.<br>
	 * <br>
	 * <font color=red>BEWARE : no checks are made based on Playable. You have to add it on the caller method.</font>
	 * @param npc : The NPC who casts the skill.
	 * @param attacker : The Creature to test.
	 * @param npcId : The npcId who calls Anti Strider debuff (only bosses, normally).
	 * @return true if the curse must counter the leftover behavior.
	 */
	protected static boolean testCursesOnAttack(Npc npc, Creature attacker, int npcId)
	{
		if (Config.RAID_DISABLE_CURSE)
			return false;
		
		// Petrification curse.
		if (attacker.getLevel() - npc.getLevel() > 8)
		{
			final L2Skill curse = FrequentSkill.RAID_CURSE2.getSkill();
			if (attacker.getFirstEffect(curse) == null)
			{
				npc.broadcastPacket(new MagicSkillUse(npc, attacker, curse.getId(), curse.getLevel(), 300, 0));
				curse.getEffects(npc, attacker);
				
				((Attackable) npc).stopHating(attacker);
				return true;
			}
		}
		
		// Antistrider slow curse.
		if (npc.getNpcId() == npcId && attacker instanceof Player && ((Player) attacker).isMounted())
		{
			final L2Skill curse = FrequentSkill.RAID_ANTI_STRIDER_SLOW.getSkill();
			if (attacker.getFirstEffect(curse) == null)
			{
				npc.broadcastPacket(new MagicSkillUse(npc, attacker, curse.getId(), curse.getLevel(), 300, 0));
				curse.getEffects(npc, attacker);
			}
		}
		return false;
	}
	
	/**
	 * Similar to its mother class, but the Anti Strider Slow debuff is known to be casted by the actual npc.
	 * @see #testCursesOnAttack(Npc npc, Creature attacker, int npcId)
	 * @param npc : The NPC who casts the skill.
	 * @param attacker : The Creature to test.
	 * @return true if the curse must counter the leftover behavior.
	 */
	protected static boolean testCursesOnAttack(Npc npc, Creature attacker)
	{
		return testCursesOnAttack(npc, attacker, npc.getNpcId());
	}
	
	/**
	 * Enforced testCursesOnAttack with third parameter set to -1. We only test RAID_CURSE2, not RAID_ANTI_STRIDER_SLOW.
	 * @see #testCursesOnAttack(Npc npc, Creature attacker, int npcId)
	 * @param npc : The NPC who casts the skill.
	 * @param attacker : The Creature to test.
	 * @return true if the curse must counter the leftover behavior.
	 */
	protected static boolean testCursesOnAggro(Npc npc, Creature attacker)
	{
		return testCursesOnAttack(npc, attacker, -1);
	}
}