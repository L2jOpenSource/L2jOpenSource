/*
 * Copyright (C) 2004-2014 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.model.instancezone;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javolution.util.FastList;
import l2r.gameserver.instancemanager.InstanceManager;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.entity.Instance;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.SystemMessage;

/**
 * Basic instance zone data transfer object.
 * @author Zoey76
 */
public class InstanceWorld
{
	private int _instanceId;
	private int _templateId = -1;
	private final List<Integer> _allowed = new FastList<>();
	private final AtomicInteger _status = new AtomicInteger();
	public int tag = -1;
	
	public List<Integer> getAllowed()
	{
		return _allowed;
	}
	
	public void removeAllowed(int id)
	{
		_allowed.remove(_allowed.indexOf(Integer.valueOf(id)));
	}
	
	public void addAllowed(int id)
	{
		_allowed.add(id);
	}
	
	public boolean isAllowed(int id)
	{
		return _allowed.contains(id);
	}
	
	public int getInstanceId()
	{
		return _instanceId;
	}
	
	public void setInstanceId(int instanceId)
	{
		_instanceId = instanceId;
	}
	
	public int getTemplateId()
	{
		return _templateId;
	}
	
	public void setTemplateId(int templateId)
	{
		_templateId = templateId;
	}
	
	public int getStatus()
	{
		return _status.get();
	}
	
	public boolean isStatus(int status)
	{
		return _status.get() == status;
	}
	
	public void setStatus(int status)
	{
		_status.set(status);
	}
	
	public void incStatus()
	{
		_status.incrementAndGet();
	}
	
	/**
	 * @param killer
	 * @param victim
	 */
	public void onDeath(L2Character killer, L2Character victim)
	{
		if ((victim != null) && victim.isPlayer())
		{
			final Instance instance = InstanceManager.getInstance().getInstance(getInstanceId());
			if (instance != null)
			{
				final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_WILL_BE_EXPELLED_IN_S1);
				sm.addInt(instance.getEjectTime() / 60 / 1000);
				victim.getActingPlayer().sendPacket(sm);
				instance.addEjectDeadTask(victim.getActingPlayer());
			}
		}
	}
}
