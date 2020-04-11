package net.sf.l2j.gameserver.model.actor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.enums.AiEventType;
import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.enums.skills.EffectFlag;
import net.sf.l2j.gameserver.enums.skills.EffectType;
import net.sf.l2j.gameserver.geoengine.GeoEngine;
import net.sf.l2j.gameserver.model.actor.stat.PlayableStat;
import net.sf.l2j.gameserver.model.actor.status.PlayableStatus;
import net.sf.l2j.gameserver.model.actor.template.CreatureTemplate;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.item.kind.EtcItem;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.ExUseSharedGroupItem;
import net.sf.l2j.gameserver.network.serverpackets.Revive;
import net.sf.l2j.gameserver.scripting.QuestState;
import net.sf.l2j.gameserver.skills.AbstractEffect;
import net.sf.l2j.gameserver.skills.L2Skill;

/**
 * This class represents all {@link Playable} actors in the world : {@link Player}s and their different {@link Summon} types.
 */
public abstract class Playable extends Creature
{
	private final Map<Integer, Long> _disabledItems = new ConcurrentHashMap<>();
	
	public Playable(int objectId, CreatureTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void initCharStat()
	{
		setStat(new PlayableStat(this));
	}
	
	@Override
	public PlayableStat getStat()
	{
		return (PlayableStat) super.getStat();
	}
	
	@Override
	public void initCharStatus()
	{
		setStatus(new PlayableStatus(this));
	}
	
	@Override
	public PlayableStatus getStatus()
	{
		return (PlayableStatus) super.getStatus();
	}
	
	@Override
	public void onActionShift(Player player)
	{
		if (player.getTarget() != this)
			player.setTarget(this);
		else
		{
			if (isAutoAttackable(player) && isIn3DRadius(player, player.getPhysicalAttackRange()) && GeoEngine.getInstance().canSeeTarget(player, this))
				player.getAI().tryTo(IntentionType.ATTACK, this, true);
			else
				player.sendPacket(ActionFailed.STATIC_PACKET);
		}
	}
	
	@Override
	public boolean doDie(Creature killer)
	{
		// killing is only possible one time
		synchronized (this)
		{
			if (isDead())
				return false;
			
			// now reset currentHp to zero
			setCurrentHp(0);
			
			setIsDead(true);
		}
		
		// Set target to null and cancel Attack or Cast
		setTarget(null);
		
		// Stop movement
		getMove().stop();
		
		// Stop HP/MP/CP Regeneration task
		getStatus().stopHpMpRegeneration();
		
		// Stop all active skills effects in progress
		if (isPhoenixBlessed())
		{
			// remove Lucky Charm if player has SoulOfThePhoenix/Salvation buff
			if (getCharmOfLuck())
				stopCharmOfLuck(null);
			if (isNoblesseBlessed())
				stopNoblesseBlessing(null);
		}
		// Same thing if the Character isn't a Noblesse Blessed L2Playable
		else if (isNoblesseBlessed())
		{
			stopNoblesseBlessing(null);
			
			// remove Lucky Charm if player have Nobless blessing buff
			if (getCharmOfLuck())
				stopCharmOfLuck(null);
		}
		else
			stopAllEffectsExceptThoseThatLastThroughDeath();
		
		// Send the Server->Client packet StatusUpdate with current HP and MP to all other Player to inform
		broadcastStatusUpdate();
		
		// Notify Creature AI
		getAI().notifyEvent(AiEventType.DEAD, null, null);
		
		// Notify Quest of L2Playable's death
		final Player actingPlayer = getActingPlayer();
		for (final QuestState qs : actingPlayer.getNotifyQuestOfDeath())
			qs.getQuest().notifyDeath((killer == null ? this : killer), actingPlayer);
		
		if (killer != null)
		{
			final Player player = killer.getActingPlayer();
			if (player != null)
				player.onKillUpdatePvPKarma(this);
		}
		
		return true;
	}
	
	@Override
	public void doRevive()
	{
		if (!isDead() || isTeleporting())
			return;
		
		setIsDead(false);
		
		if (isPhoenixBlessed())
		{
			stopPhoenixBlessing(null);
			
			getStatus().setCurrentHp(getMaxHp());
			getStatus().setCurrentMp(getMaxMp());
		}
		else
			getStatus().setCurrentHp(getMaxHp() * Config.RESPAWN_RESTORE_HP);
		
		// Start broadcast status
		broadcastPacket(new Revive(this));
	}
	
	public boolean checkIfPvP(Playable target)
	{
		if (target == null || target == this)
			return false;
		
		final Player player = getActingPlayer();
		if (player == null || player.getKarma() != 0)
			return false;
		
		final Player targetPlayer = target.getActingPlayer();
		if (targetPlayer == null || targetPlayer == this)
			return false;
		
		if (targetPlayer.getKarma() != 0 || targetPlayer.getPvpFlag() == 0)
			return false;
		
		return true;
	}
	
	/**
	 * Return True.
	 */
	@Override
	public boolean isAttackable()
	{
		return true;
	}
	
	/**
	 * <B><U> Overridden in </U> :</B>
	 * <ul>
	 * <li>L2Summon</li>
	 * <li>Player</li>
	 * </ul>
	 * @param id The system message to send to player.
	 */
	public void sendPacket(SystemMessageId id)
	{
		// default implementation
	}
	
	// Support for Noblesse Blessing skill, where buffs are retained after resurrect
	public final boolean isNoblesseBlessed()
	{
		return _effects.isAffected(EffectFlag.NOBLESS_BLESSING);
	}
	
	public final void stopNoblesseBlessing(AbstractEffect effect)
	{
		if (effect == null)
			stopEffects(EffectType.NOBLESSE_BLESSING);
		else
			removeEffect(effect);
		updateAbnormalEffect();
	}
	
	// Support for Soul of the Phoenix and Salvation skills
	public final boolean isPhoenixBlessed()
	{
		return _effects.isAffected(EffectFlag.PHOENIX_BLESSING);
	}
	
	public final void stopPhoenixBlessing(AbstractEffect effect)
	{
		if (effect == null)
			stopEffects(EffectType.PHOENIX_BLESSING);
		else
			removeEffect(effect);
		
		updateAbnormalEffect();
	}
	
	/**
	 * @return True if the Silent Moving mode is active.
	 */
	public boolean isSilentMoving()
	{
		return _effects.isAffected(EffectFlag.SILENT_MOVE);
	}
	
	// for Newbie Protection Blessing skill, keeps you safe from an attack by a chaotic character >= 10 levels apart from you
	public final boolean getProtectionBlessing()
	{
		return _effects.isAffected(EffectFlag.PROTECTION_BLESSING);
	}
	
	public void stopProtectionBlessing(AbstractEffect effect)
	{
		if (effect == null)
			stopEffects(EffectType.PROTECTION_BLESSING);
		else
			removeEffect(effect);
		
		updateAbnormalEffect();
	}
	
	// Charm of Luck - During a Raid/Boss war, decreased chance for death penalty
	public final boolean getCharmOfLuck()
	{
		return _effects.isAffected(EffectFlag.CHARM_OF_LUCK);
	}
	
	public final void stopCharmOfLuck(AbstractEffect effect)
	{
		if (effect == null)
			stopEffects(EffectType.CHARM_OF_LUCK);
		else
			removeEffect(effect);
		
		updateAbnormalEffect();
	}
	
	@Override
	public void updateEffectIcons(boolean partyOnly)
	{
		_effects.updateEffectIcons(partyOnly);
	}
	
	/**
	 * This method allows to easily send relations. Overridden in L2Summon and Player.
	 */
	public void broadcastRelationsChanges()
	{
	}
	
	@Override
	public boolean isInArena()
	{
		return isInsideZone(ZoneId.PVP) && !isInsideZone(ZoneId.SIEGE);
	}
	
	public boolean continueAttackingPlayable()
	{
		return isInsideZone(ZoneId.PVP) || isInsideZone(ZoneId.SIEGE);
	}
	
	public void addItemSkillTimeStamp(L2Skill itemSkill, ItemInstance itemInstance)
	{
		final EtcItem etcItem = itemInstance.getEtcItem();
		final int reuseDelay = Math.max(itemSkill.getReuseDelay(), etcItem.getReuseDelay());
		
		addTimeStamp(itemSkill, reuseDelay);
		if (reuseDelay != 0)
			disableSkill(itemSkill, reuseDelay);
		
		final int group = etcItem.getSharedReuseGroup();
		if (group >= 0)
			sendPacket(new ExUseSharedGroupItem(etcItem.getItemId(), group, reuseDelay, reuseDelay));
	}
	
	public abstract int getKarma();
	
	public abstract byte getPvpFlag();
	
	/**
	 * Disable this ItemInstance id for the duration of the delay in milliseconds.
	 * @param item
	 * @param delay (seconds * 1000)
	 */
	public void disableItem(ItemInstance item, long delay)
	{
		if (item == null)
			return;
		
		_disabledItems.put(item.getObjectId(), System.currentTimeMillis() + delay);
	}
	
	/**
	 * Check if an item is disabled. All skills disabled are identified by their reuse objectIds in <B>_disabledItems</B>.
	 * @param item The ItemInstance to check
	 * @return true if the item is currently disabled.
	 */
	public boolean isItemDisabled(ItemInstance item)
	{
		if (_disabledItems.isEmpty())
			return false;
		
		if (item == null || isAllSkillsDisabled())
			return true;
		
		final int hashCode = item.getObjectId();
		
		final Long timeStamp = _disabledItems.get(hashCode);
		if (timeStamp == null)
			return false;
		
		if (timeStamp < System.currentTimeMillis())
		{
			_disabledItems.remove(hashCode);
			return false;
		}
		
		return true;
	}
}