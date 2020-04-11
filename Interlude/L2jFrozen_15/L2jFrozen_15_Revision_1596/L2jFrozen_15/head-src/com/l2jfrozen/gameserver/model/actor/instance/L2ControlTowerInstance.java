package com.l2jfrozen.gameserver.model.actor.instance;

import java.util.ArrayList;
import java.util.List;

import com.l2jfrozen.gameserver.ai.CtrlIntention;
import com.l2jfrozen.gameserver.datatables.sql.NpcTable;
import com.l2jfrozen.gameserver.geo.GeoData;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.spawn.L2Spawn;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.MyTargetSelected;
import com.l2jfrozen.gameserver.network.serverpackets.StatusUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.ValidateLocation;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;

public class L2ControlTowerInstance extends L2NpcInstance
{
	private List<L2Spawn> guards;
	
	public L2ControlTowerInstance(final int objectId, final L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public boolean isAttackable()
	{
		// Attackable during siege by attacker only
		return getCastle() != null && getCastle().getCastleId() > 0 && getCastle().getSiege().getIsInProgress();
	}
	
	@Override
	public boolean isAutoAttackable(final L2Character attacker)
	{
		// Attackable during siege by attacker only
		return attacker != null && attacker instanceof L2PcInstance && getCastle() != null && getCastle().getCastleId() > 0 && getCastle().getSiege().getIsInProgress() && getCastle().getSiege().checkIsAttacker(((L2PcInstance) attacker).getClan());
	}
	
	@Override
	public void onForcedAttack(final L2PcInstance player)
	{
		onAction(player);
	}
	
	@Override
	public void onAction(final L2PcInstance player)
	{
		if (!canTarget(player))
		{
			return;
		}
		
		// Check if the L2PcInstance already target the L2NpcInstance
		if (this != player.getTarget())
		{
			// Set the target of the L2PcInstance player
			player.setTarget(this);
			
			// Send a Server->Client packet MyTargetSelected to the L2PcInstance player
			MyTargetSelected my = new MyTargetSelected(getObjectId(), player.getLevel() - getLevel());
			player.sendPacket(my);
			my = null;
			
			// Send a Server->Client packet StatusUpdate of the L2NpcInstance to the L2PcInstance to update its HP bar
			StatusUpdate su = new StatusUpdate(getObjectId());
			su.addAttribute(StatusUpdate.CUR_HP, (int) getStatus().getCurrentHp());
			su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
			player.sendPacket(su);
			su = null;
			
			// Send a Server->Client packet ValidateLocation to correct the L2NpcInstance position and heading on the client
			player.sendPacket(new ValidateLocation(this));
		}
		else
		{
			if (isAutoAttackable(player) && Math.abs(player.getZ() - getZ()) < 100 // Less then max height difference, delete check when geo
				&& GeoData.getInstance().canSeeTarget(player, this))
			{
				// Notify the L2PcInstance AI with AI_INTENTION_INTERACT
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
				
				// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
				player.sendPacket(ActionFailed.STATIC_PACKET);
			}
		}
	}
	
	public void onDeath()
	{
		if (getCastle().getSiege().getIsInProgress())
		{
			getCastle().getSiege().killedCT();
			
			L2NpcTemplate template = NpcTable.getInstance().getTemplate(13003); // Life control tower destroyed
			L2Spawn spawnTemplate;
			
			try
			{
				spawnTemplate = new L2Spawn(template);
				spawnTemplate.setIsCustomSpawn(false);
				spawnTemplate.setLocx(getX());
				spawnTemplate.setLocy(getY());
				spawnTemplate.setLocz(getZ());
				spawnTemplate.doSpawn();
			}
			catch (SecurityException e)
			{
				e.printStackTrace();
			}
			catch (ClassNotFoundException e)
			{
				e.printStackTrace();
			}
			catch (NoSuchMethodException e)
			{
				e.printStackTrace();
			}
			
			if (getGuards() != null && getGuards().size() > 0)
			{
				for (final L2Spawn spawn : getGuards())
				{
					if (spawn == null)
					{
						continue;
					}
					
					spawn.stopRespawn();
				}
			}
		}
	}
	
	public void registerGuard(final L2Spawn guard)
	{
		getGuards().add(guard);
	}
	
	public final List<L2Spawn> getGuards()
	{
		if (guards == null)
		{
			guards = new ArrayList<>();
		}
		return guards;
	}
}
