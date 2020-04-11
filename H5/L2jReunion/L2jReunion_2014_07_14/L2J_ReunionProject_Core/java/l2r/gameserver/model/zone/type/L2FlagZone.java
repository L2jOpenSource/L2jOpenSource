/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.model.zone.type;

import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.datatables.xml.SkillData;
import l2r.gameserver.enums.ZoneIdType;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.zone.L2ZoneType;
import l2r.gameserver.network.serverpackets.MagicSkillUse;
import l2r.util.Rnd;
import gr.reunion.configsEngine.FlagZoneConfigs;

/**
 * @author -=GodFather=-
 */
public class L2FlagZone extends L2ZoneType
{
	public L2FlagZone(int id)
	{
		super(id);
	}
	
	@Override
	protected void onEnter(L2Character character)
	{
		if (FlagZoneConfigs.ENABLE_FLAG_ZONE && character.isPlayer())
		{
			L2PcInstance activeChar = character.getActingPlayer();
			activeChar.setInsideZone(ZoneIdType.FLAG, true);
			activeChar.setInsideZone(ZoneIdType.NO_SUMMON_FRIEND, true);
			activeChar.setInsideZone(ZoneIdType.NO_STORE, true);
			activeChar.setInsideZone(ZoneIdType.NO_BOOKMARK, true);
			activeChar.setInsideZone(ZoneIdType.NO_ITEM_DROP, true);
			
			SkillData.getInstance().getInfo(1323, 1).getEffects(activeChar, activeChar);
			
			if (FlagZoneConfigs.AUTO_FLAG_ON_ENTER)
			{
				activeChar.setPvpFlag(1);
			}
			if (FlagZoneConfigs.ENABLE_ANTIFEED_PROTECTION)
			{
				activeChar.startAntifeedProtection(true, true);
			}
			
			activeChar.broadcastUserInfo();
		}
	}
	
	@Override
	protected void onExit(final L2Character character)
	{
		if (FlagZoneConfigs.ENABLE_FLAG_ZONE && character.isPlayer())
		{
			L2PcInstance activeChar = character.getActingPlayer();
			activeChar.setInsideZone(ZoneIdType.FLAG, false);
			activeChar.setInsideZone(ZoneIdType.NO_SUMMON_FRIEND, false);
			activeChar.setInsideZone(ZoneIdType.NO_STORE, false);
			activeChar.setInsideZone(ZoneIdType.NO_BOOKMARK, false);
			activeChar.setInsideZone(ZoneIdType.NO_ITEM_DROP, false);
			
			if (FlagZoneConfigs.AUTO_FLAG_ON_ENTER)
			{
				activeChar.setPvpFlag(0);
			}
			if (FlagZoneConfigs.ENABLE_ANTIFEED_PROTECTION)
			{
				activeChar.startAntifeedProtection(false, true);
			}
			
			activeChar.broadcastUserInfo();
		}
	}
	
	@Override
	public void onDieInside(final L2Character character)
	{
		if (FlagZoneConfigs.ENABLE_FLAG_ZONE && FlagZoneConfigs.ENABLE_FLAG_ZONE_AUTO_REVIVE && character.isPlayer())
		{
			final L2PcInstance activeChar = character.getActingPlayer();
			if (FlagZoneConfigs.SHOW_DIE_ANIMATION)
			{
				final MagicSkillUse msu = new MagicSkillUse(activeChar, activeChar, 23096, 1, 1, 1);
				activeChar.broadcastPacket(msu);
			}
			
			if (FlagZoneConfigs.ENABLE_FLAG_ZONE_AUTO_REVIVE)
			{
				activeChar.sendMessage("Get ready! You will be revived in " + FlagZoneConfigs.FLAG_ZONE_REVIVE_DELAY + " seconds!");
				ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
				{
					@Override
					public void run()
					{
						if (activeChar.isDead())
						{
							activeChar.doRevive();
							int r = Rnd.get(FlagZoneConfigs.FLAG_ZONE_AUTO_RES_LOCS_COUNT);
							activeChar.teleToLocation(FlagZoneConfigs.xCoords[r], FlagZoneConfigs.yCoords[r], FlagZoneConfigs.zCoords[r]);
						}
					}
				}, FlagZoneConfigs.FLAG_ZONE_REVIVE_DELAY * 1000);
			}
		}
	}
	
	@Override
	public void onReviveInside(L2Character character)
	{
		if (FlagZoneConfigs.ENABLE_FLAG_ZONE && character.isPlayer())
		{
			L2PcInstance activeChar = character.getActingPlayer();
			SkillData.getInstance().getInfo(1323, 1).getEffects(activeChar, activeChar);
			activeChar.setCurrentHpMp(activeChar.getMaxHp(), activeChar.getMaxMp());
			activeChar.setCurrentCp(activeChar.getMaxCp());
		}
	}
}