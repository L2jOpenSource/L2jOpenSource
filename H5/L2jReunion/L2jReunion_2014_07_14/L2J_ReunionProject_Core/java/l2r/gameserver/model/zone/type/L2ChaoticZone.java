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
import l2r.util.Rnd;
import gr.reunion.configsEngine.ChaoticZoneConfigs;

/**
 * @author -=GodFather=-
 */
public class L2ChaoticZone extends L2RespawnZone
{
	public L2ChaoticZone(int id)
	{
		super(id);
	}
	
	@Override
	protected void onEnter(L2Character character)
	{
		if (ChaoticZoneConfigs.ENABLE_CHAOTIC_ZONE && character.isPlayer())
		{
			L2PcInstance activeChar = character.getActingPlayer();
			activeChar.setInsideZone(ZoneIdType.ZONE_CHAOTIC, true);
			activeChar.setInsideZone(ZoneIdType.NO_SUMMON_FRIEND, true);
			activeChar.setInsideZone(ZoneIdType.NO_STORE, true);
			activeChar.setInsideZone(ZoneIdType.NO_BOOKMARK, true);
			activeChar.setInsideZone(ZoneIdType.NO_ITEM_DROP, true);
			activeChar.setPvpFlag(1);
			activeChar.sendMessage("You entered the Chaotic Zone.");
			activeChar.broadcastUserInfo();
		}
	}
	
	@Override
	protected void onExit(L2Character character)
	{
		if (ChaoticZoneConfigs.ENABLE_CHAOTIC_ZONE && character.isPlayer())
		{
			L2PcInstance activeChar = character.getActingPlayer();
			activeChar.setInsideZone(ZoneIdType.ZONE_CHAOTIC, false);
			activeChar.setInsideZone(ZoneIdType.NO_SUMMON_FRIEND, false);
			activeChar.setInsideZone(ZoneIdType.NO_STORE, false);
			activeChar.setInsideZone(ZoneIdType.NO_BOOKMARK, false);
			activeChar.setInsideZone(ZoneIdType.NO_ITEM_DROP, false);
			activeChar.sendMessage("You left the Chaotic Zone.");
			
			if (ChaoticZoneConfigs.ENABLE_CHAOTIC_ZONE_SKILL)
			{
				activeChar.stopSkillEffects(ChaoticZoneConfigs.CHAOTIC_ZONE_SKILL_ID);
			}
			
			activeChar.setPvpFlag(0);
			activeChar.broadcastUserInfo();
		}
	}
	
	@Override
	public void onDieInside(final L2Character character)
	{
		if (ChaoticZoneConfigs.ENABLE_CHAOTIC_ZONE && ChaoticZoneConfigs.ENABLE_CHAOTIC_ZONE_AUTO_REVIVE && character.isPlayer())
		{
			final L2PcInstance activeChar = character.getActingPlayer();
			activeChar.sendMessage("Get ready! You will be revived in " + ChaoticZoneConfigs.CHAOTIC_ZONE_REVIVE_DELAY + " seconds!");
			ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
			{
				@Override
				public void run()
				{
					if (activeChar.isDead())
					{
						activeChar.doRevive();
						int r = Rnd.get(ChaoticZoneConfigs.CHAOTIC_ZONE_AUTO_RES_LOCS_COUNT);
						activeChar.teleToLocation(ChaoticZoneConfigs.xCoords[r], ChaoticZoneConfigs.yCoords[r], ChaoticZoneConfigs.zCoords[r]);
					}
				}
			}, ChaoticZoneConfigs.CHAOTIC_ZONE_REVIVE_DELAY * 1000);
		}
	}
	
	@Override
	public void onReviveInside(L2Character character)
	{
		if (ChaoticZoneConfigs.ENABLE_CHAOTIC_ZONE && character.isPlayer())
		{
			L2PcInstance activeChar = character.getActingPlayer();
			SkillData.getInstance().getInfo(1323, 1).getEffects(activeChar, activeChar);
			activeChar.setCurrentHpMp(activeChar.getMaxHp(), activeChar.getMaxMp());
			activeChar.setCurrentCp(activeChar.getMaxCp());
			
			if (ChaoticZoneConfigs.ENABLE_CHAOTIC_ZONE_SKILL)
			{
				SkillData.getInstance().getInfo(ChaoticZoneConfigs.CHAOTIC_ZONE_SKILL_ID, 1).getEffects(activeChar, activeChar);
			}
		}
	}
}