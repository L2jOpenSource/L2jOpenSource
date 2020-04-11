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
package l2r.gameserver.network.serverpackets;

import l2r.gameserver.enums.ZoneIdType;
import l2r.gameserver.model.actor.L2Summon;
import l2r.gameserver.model.actor.instance.L2PetInstance;
import l2r.gameserver.model.actor.instance.L2ServitorInstance;

public class PetInfo extends L2GameServerPacket
{
	private final L2Summon _summon;
	private final int _x, _y, _z, _heading;
	private final boolean _isSummoned;
	private final int _val;
	private final int _mAtkSpd, _pAtkSpd;
	private final int _runSpd, _walkSpd;
	private final int _swimRunSpd;
	private final int _swimWalkSpd;
	private final int _flRunSpd = 0;
	private final int _flWalkSpd = 0;
	private final int _flyRunSpd;
	private final int _flyWalkSpd;
	private final float _moveMultiplier;
	private final int _maxHp, _maxMp;
	private int _maxFed, _curFed;
	
	public PetInfo(L2Summon summon, int val)
	{
		_summon = summon;
		_isSummoned = summon.isShowSummonAnimation();
		_x = summon.getX();
		_y = summon.getY();
		_z = summon.getZ();
		_heading = summon.getHeading();
		_mAtkSpd = summon.getMAtkSpd();
		_pAtkSpd = summon.getPAtkSpd();
		_moveMultiplier = summon.getMovementSpeedMultiplier();
		_runSpd = Math.round(summon.getRunSpeed() / _moveMultiplier);
		_walkSpd = Math.round(summon.getWalkSpeed() / _moveMultiplier);
		_swimRunSpd = Math.round(summon.getSwimRunSpeed() / _moveMultiplier);
		_swimWalkSpd = Math.round(summon.getSwimWalkSpeed() / _moveMultiplier);
		_flyRunSpd = summon.isFlying() ? _runSpd : 0;
		_flyWalkSpd = summon.isFlying() ? _walkSpd : 0;
		_maxHp = summon.getMaxHp();
		_maxMp = summon.getMaxMp();
		_val = val;
		if (summon.isPet())
		{
			final L2PetInstance pet = (L2PetInstance) _summon;
			_curFed = pet.getCurrentFed(); // how fed it is
			_maxFed = pet.getMaxFed(); // max fed it can be
		}
		else if (summon.isServitor())
		{
			final L2ServitorInstance sum = (L2ServitorInstance) _summon;
			_curFed = sum.getTimeRemaining();
			_maxFed = sum.getTotalLifeTime();
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xb2);
		writeD(_summon.getSummonType());
		writeD(_summon.getObjectId());
		writeD(_summon.getTemplate().getIdTemplate() + 1000000);
		writeD(0); // 1=attackable
		
		writeD(_x);
		writeD(_y);
		writeD(_z);
		writeD(_heading);
		writeD(0);
		writeD(_mAtkSpd);
		writeD(_pAtkSpd);
		writeD(_runSpd);
		writeD(_walkSpd);
		writeD(_swimRunSpd);
		writeD(_swimWalkSpd);
		writeD(_flRunSpd);
		writeD(_flWalkSpd);
		writeD(_flyRunSpd);
		writeD(_flyWalkSpd);
		writeF(_moveMultiplier);
		writeF(_summon.getAttackSpeedMultiplier()); // attack speed multiplier
		writeF(_summon.getTemplate().getfCollisionRadius());
		writeF(_summon.getTemplate().getfCollisionHeight());
		writeD(_summon.getWeapon()); // right hand weapon
		writeD(_summon.getArmor()); // body armor
		writeD(0x00); // left hand weapon
		writeC(_summon.getOwner() != null ? 1 : 0); // when pet is dead and player exit game, pet doesn't show master name
		writeC(_summon.isRunning() ? 1 : 0); // running=1 (it is always 1, walking mode is calculated from multiplier)
		writeC(_summon.isInCombat() ? 1 : 0); // attacking 1=true
		writeC(_summon.isAlikeDead() ? 1 : 0); // dead 1=true
		writeC(_isSummoned ? 2 : _val); // 0=teleported 1=default 2=summoned
		writeD(-1); // High Five NPCString ID
		if (_summon.isPet())
		{
			writeS(_summon.getName()); // Pet name.
		}
		else
		{
			writeS(_summon.getTemplate().isServerSideName() ? _summon.getName() : ""); // Summon name.
		}
		writeD(-1); // High Five NPCString ID
		writeS(_summon.getTitle()); // owner name
		writeD(1);
		writeD(_summon.getPvpFlag()); // 0 = white,2= purpleblink, if its greater then karma = purple
		writeD(_summon.getKarma()); // karma
		writeD(_curFed); // how fed it is
		writeD(_maxFed); // max fed it can be
		writeD((int) _summon.getCurrentHp());// current hp
		writeD(_maxHp);// max hp
		writeD((int) _summon.getCurrentMp());// current mp
		writeD(_maxMp);// max mp
		writeD(_summon.getStat().getSp()); // sp
		writeD(_summon.getLevel());// lvl
		writeQ(_summon.getStat().getExp());
		
		if (_summon.getExpForThisLevel() > _summon.getStat().getExp())
		{
			writeQ(_summon.getStat().getExp());// 0% absolute value
		}
		else
		{
			writeQ(_summon.getExpForThisLevel());// 0% absolute value
		}
		
		writeQ(_summon.getExpForNextLevel());// 100% absoulte value
		writeD(_summon.isPet() ? _summon.getInventory().getTotalWeight() : 0);// weight
		writeD(_summon.getMaxLoad());// max weight it can carry
		writeD(_summon.getPAtk(null));// patk
		writeD(_summon.getPDef(null));// pdef
		writeD(_summon.getMAtk(null, null));// matk
		writeD(_summon.getMDef(null, null));// mdef
		writeD(_summon.getAccuracy());// accuracy
		writeD(_summon.getEvasionRate(null));// evasion
		writeD(_summon.getCriticalHit(null, null));// critical
		writeD((int) _summon.getStat().getMoveSpeed());// speed
		writeD(_summon.getPAtkSpd());// atkspeed
		writeD(_summon.getMAtkSpd());// casting speed
		
		writeD(_summon.getAbnormalEffect());// c2 abnormal visual effect... bleed=1; poison=2; poison & bleed=3; flame=4;
		writeH(_summon.isMountable() ? 1 : 0);// c2 ride button
		
		writeC(_summon.isInsideZone(ZoneIdType.WATER) ? 1 : _summon.isFlying() ? 2 : 0); // c2
		
		// Following all added in C4.
		writeH(0); // ??
		writeC(_summon.getTeam().getId());
		writeD(_summon.getSoulShotsPerHit()); // How many soulshots this servitor uses per hit
		writeD(_summon.getSpiritShotsPerHit()); // How many spiritshots this servitor uses per hit
		writeD(_summon.getFormId());// CT1.5 Pet form and skills
		writeD(_summon.getSpecialEffect());
	}
}
