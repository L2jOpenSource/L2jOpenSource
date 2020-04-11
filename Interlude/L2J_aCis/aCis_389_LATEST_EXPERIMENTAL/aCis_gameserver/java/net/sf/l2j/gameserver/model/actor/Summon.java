package net.sf.l2j.gameserver.model.actor;

import java.util.List;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.data.xml.ItemData;
import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.enums.TeamType;
import net.sf.l2j.gameserver.enums.actors.NpcSkillType;
import net.sf.l2j.gameserver.enums.items.ActionType;
import net.sf.l2j.gameserver.enums.items.ShotType;
import net.sf.l2j.gameserver.enums.skills.SkillTargetType;
import net.sf.l2j.gameserver.geoengine.GeoEngine;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.handler.ItemHandler;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.ai.type.CreatureAI;
import net.sf.l2j.gameserver.model.actor.ai.type.SummonAI;
import net.sf.l2j.gameserver.model.actor.cast.SummonCast;
import net.sf.l2j.gameserver.model.actor.instance.Door;
import net.sf.l2j.gameserver.model.actor.instance.Pet;
import net.sf.l2j.gameserver.model.actor.instance.Servitor;
import net.sf.l2j.gameserver.model.actor.stat.SummonStat;
import net.sf.l2j.gameserver.model.actor.status.SummonStatus;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.group.Party;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.item.kind.Weapon;
import net.sf.l2j.gameserver.model.itemcontainer.PetInventory;
import net.sf.l2j.gameserver.model.olympiad.OlympiadGameManager;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.AbstractNpcInfo.SummonInfo;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.L2GameServerPacket;
import net.sf.l2j.gameserver.network.serverpackets.MoveToPawn;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.PetDelete;
import net.sf.l2j.gameserver.network.serverpackets.PetInfo;
import net.sf.l2j.gameserver.network.serverpackets.PetItemList;
import net.sf.l2j.gameserver.network.serverpackets.PetStatusShow;
import net.sf.l2j.gameserver.network.serverpackets.PetStatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.RelationChanged;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.L2Skill;

public abstract class Summon extends Playable
{
	private Player _owner;
	
	private boolean _follow = true;
	private boolean _previousFollowStatus = true;
	
	private int _shotsMask = 0;
	
	public Summon(int objectId, NpcTemplate template, Player owner)
	{
		super(objectId, template);
		
		// Calculate passive skills stats.
		for (L2Skill skill : template.getSkills(NpcSkillType.PASSIVE))
			addStatFuncs(skill.getStatFuncs(this));
		
		// Set the magical circle animation.
		setShowSummonAnimation(true);
		
		// Set the Player owner.
		_owner = owner;
	}
	
	public abstract int getSummonType();
	
	@Override
	public void initCharStat()
	{
		setStat(new SummonStat(this));
	}
	
	@Override
	public SummonStat getStat()
	{
		return (SummonStat) super.getStat();
	}
	
	@Override
	public void initCharStatus()
	{
		setStatus(new SummonStatus(this));
	}
	
	@Override
	public SummonStatus getStatus()
	{
		return (SummonStatus) super.getStatus();
	}
	
	@Override
	public CreatureAI getAI()
	{
		CreatureAI ai = _ai;
		if (ai == null)
		{
			synchronized (this)
			{
				ai = _ai;
				if (ai == null)
					_ai = ai = new SummonAI(this);
			}
		}
		return ai;
	}
	
	@Override
	public NpcTemplate getTemplate()
	{
		return (NpcTemplate) super.getTemplate();
	}
	
	@Override
	public void setWalkOrRun(boolean value)
	{
		super.setWalkOrRun(value);
		
		broadcastStatusUpdate();
	}
	
	@Override
	public void updateAbnormalEffect()
	{
		for (Player player : getKnownType(Player.class))
			player.sendPacket(new SummonInfo(this, player, 1));
	}
	
	@Override
	public void setCast()
	{
		_cast = new SummonCast(this);
	}
	
	/**
	 * @return Returns the mountable.
	 */
	public boolean isMountable()
	{
		return false;
	}
	
	@Override
	public void onAction(Player player)
	{
		// Set the target of the player
		if (player.getTarget() != this)
			player.setTarget(this);
		else if (player == _owner)
		{
			// Calculate the distance between the Player and the Npc.
			if (!canInteract(player))
			{
				// Notify the Player AI with INTERACT
				player.getAI().tryTo(IntentionType.INTERACT, this, false);
			}
			else
			{
				// Stop moving if we're already in interact range.
				if (player.isMoving() || player.isInCombat())
					player.getAI().tryTo(IntentionType.IDLE, null, null);
				
				// Rotate the player to face the instance
				player.sendPacket(new MoveToPawn(player, this, Npc.INTERACTION_DISTANCE));
				
				player.sendPacket(new PetStatusShow(this));
				
				// Send ActionFailed to the player in order to avoid he stucks
				player.sendPacket(ActionFailed.STATIC_PACKET);
			}
		}
		else
		{
			if (isAutoAttackable(player))
			{
				if (GeoEngine.getInstance().canSeeTarget(player, this))
				{
					player.getAI().tryTo(IntentionType.ATTACK, this, false);
					player.onActionRequest();
				}
			}
			else
			{
				// Rotate the player to face the instance
				player.sendPacket(new MoveToPawn(player, this, Npc.INTERACTION_DISTANCE));
				
				// Send ActionFailed to the player in order to avoid he stucks
				player.sendPacket(ActionFailed.STATIC_PACKET);
				
				if (GeoEngine.getInstance().canSeeTarget(player, this))
					player.getAI().tryTo(IntentionType.FOLLOW, this, false);
			}
		}
	}
	
	@Override
	public void onActionShift(Player player)
	{
		if (player.isGM())
		{
			final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			html.setFile("data/html/admin/petinfo.htm");
			html.replace("%name%", getName() == null ? "N/A" : getName());
			html.replace("%level%", getLevel());
			html.replace("%exp%", getStat().getExp());
			html.replace("%owner%", " <a action=\"bypass -h admin_character_info " + getActingPlayer().getName() + "\">" + getActingPlayer().getName() + "</a>");
			html.replace("%class%", getClass().getSimpleName());
			html.replace("%ai%", hasAI() ? getAI().getCurrentIntention().getType().name() : "NULL");
			html.replace("%hp%", (int) getStatus().getCurrentHp() + "/" + getStat().getMaxHp());
			html.replace("%mp%", (int) getStatus().getCurrentMp() + "/" + getStat().getMaxMp());
			html.replace("%karma%", getKarma());
			html.replace("%undead%", isUndead() ? "yes" : "no");
			
			if (this instanceof Pet)
			{
				html.replace("%inv%", " <a action=\"bypass admin_show_pet_inv " + getActingPlayer().getObjectId() + "\">view</a>");
				html.replace("%food%", ((Pet) this).getCurrentFed() + "/" + ((Pet) this).getPetData().getMaxMeal());
				html.replace("%load%", ((Pet) this).getInventory().getTotalWeight() + "/" + ((Pet) this).getMaxLoad());
			}
			else
			{
				html.replace("%inv%", "none");
				html.replace("%food%", "N/A");
				html.replace("%load%", "N/A");
			}
			
			player.sendPacket(html);
		}
		super.onActionShift(player);
	}
	
	@Override
	public final int getKarma()
	{
		return (getOwner() != null) ? getOwner().getKarma() : 0;
	}
	
	@Override
	public final byte getPvpFlag()
	{
		return (getOwner() != null) ? getOwner().getPvpFlag() : 0;
	}
	
	public final TeamType getTeam()
	{
		return (getOwner() != null) ? getOwner().getTeam() : TeamType.NONE;
	}
	
	public final Player getOwner()
	{
		return _owner;
	}
	
	public final int getNpcId()
	{
		return getTemplate().getNpcId();
	}
	
	public int getMaxLoad()
	{
		return 0;
	}
	
	public int getSoulShotsPerHit()
	{
		return getTemplate().getSsCount();
	}
	
	public int getSpiritShotsPerHit()
	{
		return getTemplate().getSpsCount();
	}
	
	public void followOwner()
	{
		setFollowStatus(true);
	}
	
	@Override
	public boolean doDie(Creature killer)
	{
		if (!super.doDie(killer))
			return false;
		
		// Disable beastshots
		for (int itemId : getOwner().getAutoSoulShot())
		{
			switch (ItemData.getInstance().getTemplate(itemId).getDefaultAction())
			{
				case summon_soulshot:
				case summon_spiritshot:
					getOwner().disableAutoShot(itemId);
					break;
			}
		}
		return true;
	}
	
	@Override
	public void onDecay()
	{
		if (_owner.getSummon() != this)
			return;
		
		deleteMe(_owner);
	}
	
	@Override
	public void broadcastStatusUpdate()
	{
		super.broadcastStatusUpdate();
		updateAndBroadcastStatus(1);
	}
	
	public void deleteMe(Player owner)
	{
		owner.setSummon(null);
		owner.sendPacket(new PetDelete(getSummonType(), getObjectId()));
		
		decayMe();
		
		super.deleteMe();
	}
	
	public void unSummon(Player owner)
	{
		if (isVisible() && !isDead())
		{
			// Abort attack, cast and move.
			abortAll(true);
			
			stopHpMpRegeneration();
			stopAllEffects();
			store();
			
			owner.setSummon(null);
			owner.sendPacket(new PetDelete(getSummonType(), getObjectId()));
			
			decayMe();
			
			super.deleteMe();
			
			// Disable beastshots
			for (int itemId : owner.getAutoSoulShot())
			{
				switch (ItemData.getInstance().getTemplate(itemId).getDefaultAction())
				{
					case summon_soulshot:
					case summon_spiritshot:
						owner.disableAutoShot(itemId);
						break;
				}
			}
		}
	}
	
	public int getAttackRange()
	{
		return 36;
	}
	
	public void setFollowStatus(boolean state)
	{
		_follow = state;
		
		if (_follow)
			getAI().tryTo(IntentionType.FOLLOW, getOwner(), false);
		else
			getAI().tryTo(IntentionType.IDLE, null, null);
	}
	
	public boolean getFollowStatus()
	{
		return _follow;
	}
	
	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		return _owner.isAutoAttackable(attacker);
	}
	
	public int getControlItemId()
	{
		return 0;
	}
	
	public Weapon getActiveWeapon()
	{
		return null;
	}
	
	@Override
	public PetInventory getInventory()
	{
		return null;
	}
	
	public void store()
	{
	}
	
	@Override
	public ItemInstance getActiveWeaponInstance()
	{
		return null;
	}
	
	@Override
	public Weapon getActiveWeaponItem()
	{
		return null;
	}
	
	@Override
	public ItemInstance getSecondaryWeaponInstance()
	{
		return null;
	}
	
	@Override
	public Weapon getSecondaryWeaponItem()
	{
		return null;
	}
	
	/**
	 * Return True if the L2Summon is invulnerable or if the summoner is in spawn protection.<BR>
	 * <BR>
	 */
	@Override
	public boolean isInvul()
	{
		return super.isInvul() || getOwner().isSpawnProtected();
	}
	
	/**
	 * Return the Party of its owner, or null.
	 */
	@Override
	public Party getParty()
	{
		return (_owner == null) ? null : _owner.getParty();
	}
	
	/**
	 * Return True if the Summon owner has a Party in progress.
	 */
	@Override
	public boolean isInParty()
	{
		return (_owner == null) ? false : _owner.getParty() != null;
	}
	
	@Override
	public void setIsImmobilized(boolean value)
	{
		super.setIsImmobilized(value);
		
		if (value)
		{
			_previousFollowStatus = getFollowStatus();
			// if immobilized, disable follow mode
			if (_previousFollowStatus)
				setFollowStatus(false);
		}
		else
		{
			// if not more immobilized, restore follow mode
			setFollowStatus(_previousFollowStatus);
		}
	}
	
	public void setOwner(Player newOwner)
	{
		_owner = newOwner;
	}
	
	@Override
	public void sendDamageMessage(Creature target, int damage, boolean mcrit, boolean pcrit, boolean miss)
	{
		if (miss || getOwner() == null)
			return;
		
		// Prevents the double spam of system messages, if the target is the owning player.
		if (target.getObjectId() != getOwner().getObjectId())
		{
			if (pcrit || mcrit)
				if (this instanceof Servitor)
					sendPacket(SystemMessageId.CRITICAL_HIT_BY_SUMMONED_MOB);
				else
					sendPacket(SystemMessageId.CRITICAL_HIT_BY_PET);
				
			final SystemMessage sm;
			
			if (target.isInvul())
			{
				if (target.isParalyzed())
					sm = SystemMessage.getSystemMessage(SystemMessageId.OPPONENT_PETRIFIED);
				else
					sm = SystemMessage.getSystemMessage(SystemMessageId.ATTACK_WAS_BLOCKED);
			}
			else
				sm = SystemMessage.getSystemMessage(SystemMessageId.PET_HIT_FOR_S1_DAMAGE).addNumber(damage);
			
			sendPacket(sm);
			
			if (getOwner().isInOlympiadMode() && target instanceof Player && ((Player) target).isInOlympiadMode() && ((Player) target).getOlympiadGameId() == getOwner().getOlympiadGameId())
			{
				OlympiadGameManager.getInstance().notifyCompetitorDamage(getOwner(), damage);
			}
		}
	}
	
	@Override
	public void reduceCurrentHp(double damage, Creature attacker, L2Skill skill)
	{
		super.reduceCurrentHp(damage, attacker, skill);
	}
	
	@Override
	public boolean isOutOfControl()
	{
		return super.isOutOfControl() || isBetrayed();
	}
	
	@Override
	public boolean isInCombat()
	{
		return getOwner() != null ? getOwner().isInCombat() : false;
	}
	
	@Override
	public Player getActingPlayer()
	{
		return getOwner();
	}
	
	@Override
	public String toString()
	{
		return super.toString() + "(" + getNpcId() + ") Owner: " + getOwner();
	}
	
	@Override
	public void sendPacket(L2GameServerPacket mov)
	{
		if (getOwner() != null)
			getOwner().sendPacket(mov);
	}
	
	@Override
	public void sendPacket(SystemMessageId id)
	{
		if (getOwner() != null)
			getOwner().sendPacket(id);
	}
	
	public int getWeapon()
	{
		return 0;
	}
	
	public int getArmor()
	{
		return 0;
	}
	
	public void updateAndBroadcastStatusAndInfos(int val)
	{
		sendPacket(new PetInfo(this, val));
		
		// The PetInfo packet wipes the PartySpelled (list of active spells' icons). Re-add them
		updateEffectIcons(true);
		
		updateAndBroadcastStatus(val);
	}
	
	public void sendPetInfosToOwner()
	{
		sendPacket(new PetInfo(this, 2));
		
		// The PetInfo packet wipes the PartySpelled (list of active spells' icons). Re-add them
		updateEffectIcons(true);
	}
	
	public void updateAndBroadcastStatus(int val)
	{
		sendPacket(new PetStatusUpdate(this));
		
		if (isVisible())
		{
			for (Player player : getKnownType(Player.class))
			{
				if (player == getOwner())
					continue;
				
				player.sendPacket(new SummonInfo(this, player, val));
			}
		}
	}
	
	@Override
	public void onSpawn()
	{
		super.onSpawn();
		
		// Need it only for "crests on summons" custom.
		if (Config.SHOW_SUMMON_CREST)
			sendPacket(new SummonInfo(this, getOwner(), 0));
		
		sendPacket(new RelationChanged(this, getOwner().getRelation(getOwner()), false));
		broadcastRelationsChanges();
	}
	
	@Override
	public void broadcastRelationsChanges()
	{
		for (Player player : getOwner().getKnownType(Player.class))
			player.sendPacket(new RelationChanged(this, getOwner().getRelation(player), isAutoAttackable(player)));
	}
	
	@Override
	public void sendInfo(Player player)
	{
		// Check if the Player is the owner of the Pet
		if (player == getOwner())
		{
			player.sendPacket(new PetInfo(this, 0));
			
			// The PetInfo packet wipes the PartySpelled (list of active spells' icons). Re-add them
			updateEffectIcons(true);
			
			if (this instanceof Pet)
				player.sendPacket(new PetItemList(this));
		}
		else
			player.sendPacket(new SummonInfo(this, player, 0));
	}
	
	@Override
	public boolean isChargedShot(ShotType type)
	{
		return (_shotsMask & type.getMask()) == type.getMask();
	}
	
	@Override
	public void setChargedShot(ShotType type, boolean charged)
	{
		if (charged)
			_shotsMask |= type.getMask();
		else
			_shotsMask &= ~type.getMask();
	}
	
	@Override
	public void rechargeShots(boolean physical, boolean magic)
	{
		if (getOwner().getAutoSoulShot() == null || getOwner().getAutoSoulShot().isEmpty())
			return;
		
		for (int itemId : getOwner().getAutoSoulShot())
		{
			ItemInstance item = getOwner().getInventory().getItemByItemId(itemId);
			if (item != null)
			{
				if (magic && item.getItem().getDefaultAction() == ActionType.summon_spiritshot)
				{
					final IItemHandler handler = ItemHandler.getInstance().getHandler(item.getEtcItem());
					if (handler != null)
						handler.useItem(getOwner(), item, false);
				}
				
				if (physical && item.getItem().getDefaultAction() == ActionType.summon_soulshot)
				{
					final IItemHandler handler = ItemHandler.getInstance().getHandler(item.getEtcItem());
					if (handler != null)
						handler.useItem(getOwner(), item, false);
				}
			}
			else
				getOwner().removeAutoSoulShot(itemId);
		}
	}
	
	@Override
	public int getSkillLevel(int skillId)
	{
		for (List<L2Skill> list : getTemplate().getSkills().values())
		{
			for (L2Skill skill : list)
				if (skill.getId() == skillId)
					return skill.getLevel();
		}
		return 0;
	}
	
	@Override
	public L2Skill getSkill(int skillId)
	{
		for (List<L2Skill> list : getTemplate().getSkills().values())
		{
			for (L2Skill skill : list)
				if (skill.getId() == skillId)
					return skill;
		}
		return null;
	}
	
	public boolean checkUseMagicConditions(L2Skill skill, boolean forceUse, boolean dontMove)
	{
		// Check if the player is dead or out of control.
		if (isDead() || isOutOfControl())
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		// ************************************* Check Target *******************************************
		
		// Get the target for the skill
		WorldObject target = null;
		
		switch (skill.getTargetType())
		{
			// OWNER_PET should be cast even if no target has been found
			case OWNER_PET:
				target = getOwner();
				break;
			// PARTY, AURA, SELF should be cast even if no target has been found
			case PARTY:
			case AURA:
			case FRONT_AURA:
			case BEHIND_AURA:
			case AURA_UNDEAD:
			case SELF:
			case CORPSE_ALLY:
				target = this;
				break;
			default:
				// Get the first target of the list
				target = skill.getFirstOfTargetList(this);
				break;
		}
		
		// Check the validity of the target
		if (target == null)
		{
			sendPacket(SystemMessageId.TARGET_CANT_FOUND);
			return false;
		}
		
		// ************************************* Check Consumables *******************************************
		
		// Check if the summon has enough MP
		if (getCurrentMp() < getStat().getMpConsume(skill) + getStat().getMpInitialConsume(skill))
		{
			// Send a System Message to the caster
			sendPacket(SystemMessageId.NOT_ENOUGH_MP);
			return false;
		}
		
		// Check if the summon has enough HP
		if (getCurrentHp() <= skill.getHpConsume())
		{
			// Send a System Message to the caster
			sendPacket(SystemMessageId.NOT_ENOUGH_HP);
			return false;
		}
		
		// ************************************* Check Summon State *******************************************
		
		// Check if this is offensive magic skill
		if (skill.isOffensive())
		{
			if (isInsidePeaceZone(target))
			{
				// If summon or target is in a peace zone, send a system message TARGET_IN_PEACEZONE
				sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IN_PEACEZONE));
				return false;
			}
			
			if (getOwner() != null && getOwner().isInOlympiadMode() && !getOwner().isOlympiadStart())
			{
				// if Player is in Olympia and the match isn't already start, send ActionFailed
				sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}
			
			// Check if the target is attackable
			if (target instanceof Door)
			{
				if (!((Door) target).isAutoAttackable(getOwner()))
					return false;
			}
			else
			{
				if (!target.isAttackable() && getOwner() != null && !getOwner().getAccessLevel().allowPeaceAttack())
					return false;
				
				// Check if a Forced ATTACK is in progress on non-attackable target
				if (!target.isAutoAttackable(this) && !forceUse && skill.getTargetType() != SkillTargetType.AURA && skill.getTargetType() != SkillTargetType.FRONT_AURA && skill.getTargetType() != SkillTargetType.BEHIND_AURA && skill.getTargetType() != SkillTargetType.AURA_UNDEAD && skill.getTargetType() != SkillTargetType.CLAN && skill.getTargetType() != SkillTargetType.ALLY && skill.getTargetType() != SkillTargetType.PARTY && skill.getTargetType() != SkillTargetType.SELF)
					return false;
			}
		}
		
		return true;
	}
}