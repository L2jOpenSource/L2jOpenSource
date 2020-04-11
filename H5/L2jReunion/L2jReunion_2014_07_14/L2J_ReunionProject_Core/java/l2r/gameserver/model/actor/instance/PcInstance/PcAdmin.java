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
package l2r.gameserver.model.actor.instance.PcInstance;

import l2r.gameserver.enums.IllegalActionPunishmentType;
import l2r.gameserver.instancemanager.PunishmentManager;
import l2r.gameserver.model.L2World;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.punishment.PunishmentAffect;
import l2r.gameserver.model.punishment.PunishmentTask;
import l2r.gameserver.model.punishment.PunishmentType;
import l2r.gameserver.network.clientpackets.Say2;
import l2r.gameserver.network.serverpackets.CreatureSay;
import l2r.gameserver.util.Util;
import gr.reunion.configsEngine.SecuritySystemConfigs;

public class PcAdmin extends PcExtension
{
	private boolean _safeadmin = false;
	private String _adminConfirmCmd = null;
	private boolean _inCameraMode = false;
	
	public PcAdmin(L2PcInstance activeChar)
	{
		super(activeChar);
	}
	
	public void setIsSafeAdmin(boolean b)
	{
		_safeadmin = b;
	}
	
	public boolean isSafeAdmin()
	{
		return _safeadmin;
	}
	
	public boolean canUseAdminCommand()
	{
		if (SecuritySystemConfigs.ENABLE_ADMIN_SECURITY_SYSTEM && !getPlayer().getPcAdmin().isSafeAdmin())
		{
			_log.warn("Character " + getPlayer().getName() + "(" + getPlayer().getObjectId() + ") tried to use an admin command.");
			punishUnSafeAdmin();
			return false;
		}
		return true;
	}
	
	public void punishUnSafeAdmin()
	{
		if (getPlayer() != null)
		{
			getPlayer().setAccessLevel(0);
			
			switch (SecuritySystemConfigs.SAFE_ADMIN_PUNISH)
			{
				case 1: // Broadcast only warning
					for (L2PcInstance allgms : L2World.getInstance().getAllGMs())
					{
						allgms.sendPacket(new CreatureSay(0, Say2.SHOUT, "Security System", "WARN: Unsafe Admin: " + getPlayer().getName() + "(" + getPlayer().getObjectId() + ") has been logged in."));
					}
					break;
				case 2: // Kick player
					Util.handleIllegalPlayerAction(getPlayer(), "Player: " + getPlayer().getName() + " is not allowed to be a GM and he will be Punished..", IllegalActionPunishmentType.KICK);
					break;
				case 3: // Kick and ban
					PunishmentManager.getInstance().startPunishment(new PunishmentTask(getPlayer().getAccountName(), PunishmentAffect.ACCOUNT, PunishmentType.BAN, System.currentTimeMillis(), "", getClass().getSimpleName()));
					break;
			}
		}
	}
	
	public String getAdminConfirmCmd()
	{
		return _adminConfirmCmd;
	}
	
	public void setAdminConfirmCmd(String adminConfirmCmd)
	{
		_adminConfirmCmd = adminConfirmCmd;
	}
	
	public void setCameraMode(boolean val)
	{
		_inCameraMode = val;
	}
	
	public boolean inCameraMode()
	{
		return _inCameraMode;
	}
}
