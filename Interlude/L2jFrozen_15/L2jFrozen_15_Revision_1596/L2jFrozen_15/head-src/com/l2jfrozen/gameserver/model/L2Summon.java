package com.l2jfrozen.gameserver.model;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.ai.CtrlIntention;
import com.l2jfrozen.gameserver.ai.L2CharacterAI;
import com.l2jfrozen.gameserver.ai.L2SummonAI;
import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.datatables.xml.ExperienceData;
import com.l2jfrozen.gameserver.geo.GeoData;
import com.l2jfrozen.gameserver.model.L2Skill.SkillTargetType;
import com.l2jfrozen.gameserver.model.L2Skill.SkillType;
import com.l2jfrozen.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2SiegeSummonInstance;
import com.l2jfrozen.gameserver.model.actor.knownlist.SummonKnownList;
import com.l2jfrozen.gameserver.model.actor.stat.SummonStat;
import com.l2jfrozen.gameserver.model.actor.status.SummonStatus;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.MyTargetSelected;
import com.l2jfrozen.gameserver.network.serverpackets.NpcInfo;
import com.l2jfrozen.gameserver.network.serverpackets.PetDelete;
import com.l2jfrozen.gameserver.network.serverpackets.PetStatusShow;
import com.l2jfrozen.gameserver.network.serverpackets.PetStatusUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.StatusUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.taskmanager.DecayTaskManager;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;
import com.l2jfrozen.gameserver.templates.L2Weapon;

public abstract class L2Summon extends L2PlayableInstance
{
	// private static Logger LOGGER = Logger.getLogger(L2Summon.class);
	
	protected int pkKills;
	private L2PcInstance owner;
	private int attackRange = 36; // Melee range
	private boolean follow = true;
	private boolean previousFollowStatus = true;
	private int maxLoad;
	
	private int chargedSoulShot;
	private int chargedSpiritShot;
	
	// TODO: currently, all servitors use 1 shot. However, this value
	// should vary depending on the servitor template (id and level)!
	private final int soulShotsPerHit = 1;
	private final int spiritShotsPerHit = 1;
	protected boolean showSummonAnimation;
	
	public class AIAccessor extends L2Character.AIAccessor
	{
		protected AIAccessor()
		{
			// null
		}
		
		public L2Summon getSummon()
		{
			return L2Summon.this;
		}
		
		public boolean isAutoFollow()
		{
			return getFollowStatus();
		}
		
		public void doPickupItem(final L2Object object)
		{
			L2Summon.this.doPickupItem(object);
		}
	}
	
	public L2Summon(final int objectId, final L2NpcTemplate template, final L2PcInstance owner)
	{
		super(objectId, template);
		getKnownList(); // init knownlist
		getStat(); // init stats
		getStatus(); // init status
		
		showSummonAnimation = true;
		this.owner = owner;
		aiCharacter = new L2SummonAI(new L2Summon.AIAccessor());
		
		setXYZInvisible(owner.getX() + 50, owner.getY() + 100, owner.getZ() + 100);
	}
	
	@Override
	public final SummonKnownList getKnownList()
	{
		if (super.getKnownList() == null || !(super.getKnownList() instanceof SummonKnownList))
		{
			setKnownList(new SummonKnownList(this));
		}
		
		return (SummonKnownList) super.getKnownList();
	}
	
	@Override
	public SummonStat getStat()
	{
		if (super.getStat() == null || !(super.getStat() instanceof SummonStat))
		{
			setStat(new SummonStat(this));
		}
		
		return (SummonStat) super.getStat();
	}
	
	@Override
	public SummonStatus getStatus()
	{
		if (super.getStatus() == null || !(super.getStatus() instanceof SummonStatus))
		{
			setStatus(new SummonStatus(this));
		}
		
		return (SummonStatus) super.getStatus();
	}
	
	@Override
	public L2CharacterAI getAI()
	{
		if (aiCharacter == null)
		{
			synchronized (this)
			{
				if (aiCharacter == null)
				{
					aiCharacter = new L2SummonAI(new L2Summon.AIAccessor());
				}
			}
		}
		
		return aiCharacter;
	}
	
	@Override
	public L2NpcTemplate getTemplate()
	{
		return (L2NpcTemplate) super.getTemplate();
	}
	
	// this defines the action buttons, 1 for Summon, 2 for Pets
	public abstract int getSummonType();
	
	@Override
	public void updateAbnormalEffect()
	{
		for (final L2PcInstance player : getKnownList().getKnownPlayers().values())
		{
			if (player != null)
			{
				player.sendPacket(new NpcInfo(this, player));
			}
		}
	}
	
	/**
	 * @return Returns the mountable.
	 */
	public boolean isMountable()
	{
		return false;
	}
	
	@Override
	public void onAction(final L2PcInstance player)
	{
		if (player == owner && player.getTarget() == this)
		{
			player.sendPacket(new PetStatusShow(this));
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
		else if (player.getTarget() != this)
		{
			if (Config.DEBUG)
			{
				LOGGER.debug("New target selected:" + getObjectId());
			}
			
			player.setTarget(this);
			MyTargetSelected my = new MyTargetSelected(getObjectId(), player.getLevel() - getLevel());
			player.sendPacket(my);
			my = null;
			
			// update status hp&mp
			StatusUpdate su = new StatusUpdate(getObjectId());
			su.addAttribute(StatusUpdate.CUR_HP, (int) getCurrentHp());
			su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
			su.addAttribute(StatusUpdate.CUR_MP, (int) getCurrentMp());
			su.addAttribute(StatusUpdate.MAX_MP, getMaxMp());
			player.sendPacket(su);
			su = null;
		}
		else if (player.getTarget() == this)
		{
			if (isAutoAttackable(player))
			{
				if (Config.GEODATA > 0)
				{
					if (GeoData.getInstance().canSeeTarget(player, this))
					{
						player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
						player.onActionRequest();
					}
				}
				else
				{
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
					player.onActionRequest();
				}
			}
			else
			{
				// This Action Failed packet avoids player getting stuck when clicking three or more times
				player.sendPacket(ActionFailed.STATIC_PACKET);
				
				if (Config.GEODATA > 0)
				{
					if (GeoData.getInstance().canSeeTarget(player, this))
					{
						player.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, this);
					}
				}
				else
				{
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, this);
				}
			}
		}
	}
	
	public long getExpForThisLevel()
	{
		if (getLevel() >= ExperienceData.getInstance().getMaxPetLevel())
		{
			return 0;
		}
		return ExperienceData.getInstance().getExpForLevel(getLevel());
	}
	
	public long getExpForNextLevel()
	{
		if (getLevel() >= ExperienceData.getInstance().getMaxPetLevel() - 1)
		{
			return 0;
		}
		return ExperienceData.getInstance().getExpForLevel(getLevel() + 1);
	}
	
	public final int getKarma()
	{
		return getOwner() != null ? getOwner().getKarma() : 0;
	}
	
	public final byte getPvpFlag()
	{
		return getOwner() != null ? getOwner().getPvpFlag() : 0;
	}
	
	public final L2PcInstance getOwner()
	{
		return owner;
	}
	
	public final int getNpcId()
	{
		return getTemplate().npcId;
	}
	
	@Override
	protected void doAttack(final L2Character target)
	{
		if (getOwner() != null && getOwner() == target && !getOwner().isBetrayed())
		{
			sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
			return;
		}
		if (isInsidePeaceZone(this, target))
		{
			getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			return;
		}
		if (!target.isAttackable())
		{
			if (!(this instanceof L2SiegeSummonInstance))
			{
				getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				return;
			}
		}
		
		super.doAttack(target);
	}
	
	public void setPkKills(final int pkKills)
	{
		this.pkKills = pkKills;
	}
	
	public final int getPkKills()
	{
		return pkKills;
	}
	
	public final int getMaxLoad()
	{
		return maxLoad;
	}
	
	public final int getSoulShotsPerHit()
	{
		return soulShotsPerHit;
	}
	
	public final int getSpiritShotsPerHit()
	{
		return spiritShotsPerHit;
	}
	
	public void setMaxLoad(final int maxLoad)
	{
		this.maxLoad = maxLoad;
	}
	
	public void setChargedSoulShot(final int shotType)
	{
		chargedSoulShot = shotType;
	}
	
	public void setChargedSpiritShot(final int shotType)
	{
		chargedSpiritShot = shotType;
	}
	
	public void followOwner()
	{
		setFollowStatus(true);
	}
	
	@Override
	public boolean doDie(final L2Character killer)
	{
		if (!super.doDie(killer))
		{
			return false;
		}
		
		DecayTaskManager.getInstance().addDecayTask(this);
		return true;
	}
	
	public boolean doDie(final L2Character killer, final boolean decayed)
	{
		if (!super.doDie(killer))
		{
			return false;
		}
		
		if (!decayed)
		{
			DecayTaskManager.getInstance().addDecayTask(this);
		}
		
		return true;
	}
	
	public void stopDecay()
	{
		DecayTaskManager.getInstance().cancelDecayTask(this);
	}
	
	@Override
	public void onDecay()
	{
		deleteMe(owner);
	}
	
	@Override
	public void broadcastStatusUpdate()
	{
		super.broadcastStatusUpdate();
		
		if (getOwner() != null && isVisible())
		{
			getOwner().sendPacket(new PetStatusUpdate(this));
			
		}
	}
	
	public void deleteMe(final L2PcInstance owner)
	{
		getAI().stopFollow();
		owner.sendPacket(new PetDelete(getObjectId(), 2));
		
		// FIXME: I think it should really drop items to ground and only owner can take for a while
		giveAllToOwner();
		decayMe();
		getKnownList().removeAllKnownObjects();
		owner.setPet(null);
	}
	
	public synchronized void unSummon(final L2PcInstance owner)
	{
		/*
		 * if(isVisible() && !isDead()) { getAI().stopFollow(); owner.sendPacket(new PetDelete(getObjectId(), 2)); if(getWorldRegion() != null) { getWorldRegion().removeFromZones(this); } store(); giveAllToOwner(); decayMe(); getKnownList().removeAllKnownObjects(); owner.setPet(null); setTarget(null); }
		 */
		
		if (isVisible() && !isDead())
		{
			stopAllEffects();
			
			getAI().stopFollow();
			owner.sendPacket(new PetDelete(getObjectId(), 2));
			
			store();
			
			giveAllToOwner();
			
			stopAllEffects();
			
			final L2WorldRegion oldRegion = getWorldRegion();
			decayMe();
			if (oldRegion != null)
			{
				oldRegion.removeFromZones(this);
			}
			
			getKnownList().removeAllKnownObjects();
			owner.setPet(null);
			setTarget(null);
		}
	}
	
	public int getAttackRange()
	{
		return attackRange;
	}
	
	public void setAttackRange(int range)
	{
		if (range < 36)
		{
			range = 36;
		}
		attackRange = range;
	}
	
	public void setFollowStatus(final boolean state)
	{
		follow = state;
		
		if (follow)
		{
			getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, getOwner());
		}
		else
		{
			getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, null);
		}
	}
	
	public boolean getFollowStatus()
	{
		return follow;
	}
	
	@Override
	public boolean isAutoAttackable(final L2Character attacker)
	{
		return owner.isAutoAttackable(attacker);
	}
	
	public int getChargedSoulShot()
	{
		return chargedSoulShot;
	}
	
	public int getChargedSpiritShot()
	{
		return chargedSpiritShot;
	}
	
	public int getControlItemId()
	{
		return 0;
	}
	
	public L2Weapon getActiveWeapon()
	{
		return null;
	}
	
	public PetInventory getInventory()
	{
		return null;
	}
	
	protected void doPickupItem(final L2Object object)
	{
		// TODO: Implement?
	}
	
	public void giveAllToOwner()
	{
		// TODO: Implement?
	}
	
	public void store()
	{
		// TODO: Implement?
	}
	
	@Override
	public L2ItemInstance getActiveWeaponInstance()
	{
		return null;
	}
	
	@Override
	public L2Weapon getActiveWeaponItem()
	{
		return null;
	}
	
	@Override
	public L2ItemInstance getSecondaryWeaponInstance()
	{
		return null;
	}
	
	@Override
	public L2Weapon getSecondaryWeaponItem()
	{
		return null;
	}
	
	/**
	 * Return the L2Party object of its L2PcInstance owner or null.<BR>
	 * <BR>
	 */
	@Override
	public L2Party getParty()
	{
		if (owner == null)
		{
			return null;
		}
		return owner.getParty();
	}
	
	/**
	 * Return True if the L2Character has a Party in progress.<BR>
	 * <BR>
	 */
	@Override
	public boolean isInParty()
	{
		if (owner == null)
		{
			return false;
		}
		return owner.getParty() != null;
	}
	
	/**
	 * Check if the active L2Skill can be casted.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Check if the target is correct</li>
	 * <li>Check if the target is in the skill cast range</li>
	 * <li>Check if the summon owns enough HP and MP to cast the skill</li>
	 * <li>Check if all skills are enabled and this skill is enabled</li><BR>
	 * <BR>
	 * <li>Check if the skill is active</li><BR>
	 * <BR>
	 * <li>Notify the AI with AI_INTENTION_CAST and target</li><BR>
	 * <BR>
	 * @param skill    The L2Skill to use
	 * @param forceUse used to force ATTACK on players
	 * @param dontMove used to prevent movement, if not in range
	 */
	public void useMagic(L2Skill skill, final boolean forceUse, final boolean dontMove)
	{
		if (skill == null || isDead())
		{
			return;
		}
		
		// Check if the skill is active
		if (skill.isPassive())
		{
			// just ignore the passive skill request. why does the client send it anyway ??
			return;
		}
		
		// ************************************* Check Casting in Progress *******************************************
		
		// If a skill is currently being used
		if (isCastingNow())
		{
			return;
		}
		
		// Set current pet skill
		getOwner().setCurrentPetSkill(skill, forceUse, dontMove);
		
		// ************************************* Check Target *******************************************
		
		// Get the target for the skill
		L2Object target = null;
		
		switch (skill.getTargetType())
		{
			// OWNER_PET should be cast even if no target has been found
			case TARGET_OWNER_PET:
				target = getOwner();
				break;
			// PARTY, AURA, SELF should be cast even if no target has been found
			case TARGET_PARTY:
			case TARGET_AURA:
			case TARGET_SELF:
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
			if (getOwner() != null)
			{
				getOwner().sendPacket(new SystemMessage(SystemMessageId.TARGET_CANT_FOUND));
			}
			return;
		}
		
		// ************************************* Check skill availability *******************************************
		
		// Check if this skill is enabled (ex : reuse time)
		if (isSkillDisabled(skill) && getOwner() != null)
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.SKILL_NOT_AVAILABLE);
			sm.addString(skill.getName());
			getOwner().sendPacket(sm);
			sm = null;
			return;
		}
		
		// Check if all skills are disabled
		if (isAllSkillsDisabled() && getOwner() != null)
		{
			return;
		}
		
		// ************************************* Check Consumables *******************************************
		
		// Check if the summon has enough MP
		if (getCurrentMp() < getStat().getMpConsume(skill) + getStat().getMpInitialConsume(skill))
		{
			// Send a System Message to the caster
			if (getOwner() != null)
			{
				getOwner().sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_MP));
			}
			
			return;
		}
		
		// Check if the summon has enough HP
		if (getCurrentHp() <= skill.getHpConsume())
		{
			// Send a System Message to the caster
			if (getOwner() != null)
			{
				getOwner().sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_HP));
			}
			
			return;
		}
		
		// ************************************* Check Summon State *******************************************
		
		// Check if this is offensive magic skill
		if (skill.isOffensive())
		{
			if (getOwner() != null && getOwner() == target && !getOwner().isBetrayed())
			{
				sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
				return;
			}
			
			if (isInsidePeaceZone(this, target) && getOwner() != null && !getOwner().getAccessLevel().allowPeaceAttack())
			{
				// If summon or target is in a peace zone, send a system message TARGET_IN_PEACEZONE
				sendPacket(new SystemMessage(SystemMessageId.TARGET_IN_PEACEZONE));
				return;
			}
			
			if (getOwner() != null && getOwner().isInOlympiadMode() && !getOwner().isInOlympiadFight())
			{
				// if L2PcInstance is in Olympia and the match isn't already start, send a Server->Client packet ActionFailed
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			// Check if the target is attackable
			if (target instanceof L2DoorInstance)
			{
				if (!((L2DoorInstance) target).isAttackable(getOwner()))
				{
					return;
				}
			}
			else
			{
				if (!target.isAttackable() && getOwner() != null && getOwner().getAccessLevel().allowPeaceAttack())
				{
					return;
				}
				
				// Check if a Forced ATTACK is in progress on non-attackable target
				if (!target.isAutoAttackable(this) && !forceUse && skill.getTargetType() != SkillTargetType.TARGET_AURA && skill.getTargetType() != SkillTargetType.TARGET_CLAN && skill.getTargetType() != SkillTargetType.TARGET_ALLY && skill.getTargetType() != SkillTargetType.TARGET_PARTY && skill.getTargetType() != SkillTargetType.TARGET_SELF)
				{
					return;
				}
			}
		}
		
		// Notify the AI with AI_INTENTION_CAST and target
		getAI().setIntention(CtrlIntention.AI_INTENTION_CAST, skill, target);
		
		// For SOME reason skills in Summons like Mirage The Unicorn can be used every time, like reuse task is not applied.
		// So for NOW this can help (disable the skill right here)
		disableSkill(skill, skill.getReuseDelay());
	}
	
	@Override
	public void setIsImobilised(final boolean value)
	{
		super.setIsImobilised(value);
		
		if (value)
		{
			previousFollowStatus = getFollowStatus();
			
			// if imobilized temporarly disable follow mode
			if (previousFollowStatus)
			{
				setFollowStatus(false);
			}
		}
		else
		{
			// if not more imobilized restore previous follow mode
			setFollowStatus(previousFollowStatus);
		}
	}
	
	public void setOwner(final L2PcInstance newOwner)
	{
		owner = newOwner;
	}
	
	/**
	 * @return Returns the showSummonAnimation.
	 */
	public boolean isShowSummonAnimation()
	{
		return showSummonAnimation;
	}
	
	/**
	 * @param showSummonAnimation The showSummonAnimation to set.
	 */
	public void setShowSummonAnimation(final boolean showSummonAnimation)
	{
		this.showSummonAnimation = showSummonAnimation;
	}
	
	@Override
	public boolean isInCombat()
	{
		return getOwner() != null ? getOwner().isInCombat() : false;
	}
	
	/**
	 * Servitors' skills automatically change their level based on the servitor's level. Until level 70, the servitor gets 1 lv of skill per 10 levels. After that, it is 1 skill level per 5 servitor levels. If the resulting skill level doesn't exist use the max that does exist!
	 * @see com.l2jfrozen.gameserver.model.L2Character#doCast(com.l2jfrozen.gameserver.model.L2Skill)
	 */
	@Override
	public void doCast(final L2Skill skill)
	{
		final int petLevel = getLevel();
		int skillLevel = petLevel / 10;
		
		if (skill.getSkillType() == SkillType.BUFF)
		{
			if (petLevel > 77)
			{
				skillLevel = (petLevel - 77) + 3; // max buff lvl 11 with pet lvl 85
			}
			else if (petLevel >= 70)
			{
				skillLevel = 3;
			}
			else if (petLevel >= 64)
			{
				skillLevel = 2;
			}
			else
			{
				skillLevel = 1;
			}
		}
		else
		{
			if (petLevel >= 70)
			{
				skillLevel += (petLevel - 65) / 10;
			}
			
			// adjust the level for servitors less than lv 10
			if (skillLevel < 1)
			{
				skillLevel = 1;
			}
		}
		
		L2Skill skillToCast = SkillTable.getInstance().getInfo(skill.getId(), skillLevel);
		
		if (skillToCast != null)
		{
			super.doCast(skillToCast);
		}
		else
		{
			super.doCast(skill);
		}
		
		skillToCast = null;
	}
	
	@Override
	public L2PcInstance getActingPlayer()
	{
		return owner;
	}
}
