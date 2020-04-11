/*
 * Copyright (C) L2J Sunrise
 * This file is part of L2J Sunrise.
 */
package l2r.gameserver.model.actor;

import java.util.Collection;

import l2r.gameserver.enums.InstanceType;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.actor.templates.L2CharTemplate;
import l2r.gameserver.model.actor.templates.L2NpcTemplate;
import l2r.gameserver.model.items.L2Weapon;
import l2r.gameserver.model.items.instance.L2ItemInstance;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.CharInfo;
import l2r.gameserver.network.serverpackets.L2GameServerPacket;
import l2r.gameserver.taskmanager.DecayTaskManager;

public abstract class L2Decoy extends L2Character
{
	private final L2PcInstance _owner;
	
	/**
	 * Creates an abstract decoy.
	 * @param template the decoy template
	 * @param owner the owner
	 */
	public L2Decoy(L2CharTemplate template, L2PcInstance owner)
	{
		super(template);
		setInstanceType(InstanceType.L2Decoy);
		_owner = owner;
		setXYZInvisible(owner.getX(), owner.getY(), owner.getZ());
		setIsInvul(false);
	}
	
	@Override
	public void onSpawn()
	{
		super.onSpawn();
		sendPacket(new CharInfo(this));
	}
	
	@Override
	public void updateAbnormalEffect()
	{
		Collection<L2PcInstance> plrs = getKnownList().getKnownPlayers().values();
		
		for (L2PcInstance player : plrs)
		{
			if (player != null)
			{
				sendInfo(player);
			}
		}
	}
	
	public void stopDecay()
	{
		DecayTaskManager.getInstance().cancel(this);
	}
	
	@Override
	public void onDecay()
	{
		deleteMe(_owner);
	}
	
	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return _owner.isAutoAttackable(attacker);
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
	
	@Override
	public final int getId()
	{
		return getTemplate().getId();
	}
	
	@Override
	public int getLevel()
	{
		return getTemplate().getLevel();
	}
	
	public void deleteMe(L2PcInstance owner)
	{
		decayMe();
		getKnownList().removeAllKnownObjects();
		owner.setDecoy(null);
	}
	
	public synchronized void unSummon(L2PcInstance owner)
	{
		if (isVisible() && !isDead())
		{
			owner.setDecoy(null);
			decayMe();
			getKnownList().removeAllKnownObjects();
		}
	}
	
	public final L2PcInstance getOwner()
	{
		return _owner;
	}
	
	@Override
	public L2PcInstance getActingPlayer()
	{
		return _owner;
	}
	
	@Override
	public L2NpcTemplate getTemplate()
	{
		return (L2NpcTemplate) super.getTemplate();
	}
	
	@Override
	public void sendInfo(L2PcInstance activeChar)
	{
		activeChar.sendPacket(new CharInfo(this));
	}
	
	@Override
	public void sendPacket(L2GameServerPacket mov)
	{
		if (getOwner() != null)
		{
			getOwner().sendPacket(mov);
		}
	}
	
	@Override
	public void sendPacket(SystemMessageId id)
	{
		if (getOwner() != null)
		{
			getOwner().sendPacket(id);
		}
	}
}
