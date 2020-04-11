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

import java.util.List;

import javolution.util.FastList;
import l2r.gameserver.model.actor.L2Playable;

/**
 * @author Luca Baldi
 */
public final class RelationChanged extends L2GameServerPacket
{
	public static final int RELATION_PARTY1 = 0x00001; // party member
	public static final int RELATION_PARTY2 = 0x00002; // party member
	public static final int RELATION_PARTY3 = 0x00004; // party member
	public static final int RELATION_PARTY4 = 0x00008; // party member (for information, see L2PcInstance.getRelation())
	public static final int RELATION_PARTYLEADER = 0x00010; // true if is party leader
	public static final int RELATION_HAS_PARTY = 0x00020; // true if is in party
	public static final int RELATION_CLAN_MEMBER = 0x00040; // true if is in clan
	public static final int RELATION_LEADER = 0x00080; // true if is clan leader
	public static final int RELATION_CLAN_MATE = 0x00100; // true if is in same clan
	public static final int RELATION_INSIEGE = 0x00200; // true if in siege
	public static final int RELATION_ATTACKER = 0x00400; // true when attacker
	public static final int RELATION_ALLY = 0x00800; // blue siege icon, cannot have if red
	public static final int RELATION_ENEMY = 0x01000; // true when red icon, doesn't matter with blue
	public static final int RELATION_MUTUAL_WAR = 0x04000; // double fist
	public static final int RELATION_1SIDED_WAR = 0x08000; // single fist
	public static final int RELATION_ALLY_MEMBER = 0x10000; // clan is in alliance
	public static final int RELATION_TERRITORY_WAR = 0x80000; // show Territory War icon
	
	protected static class Relation
	{
		int _objId, _relation, _autoAttackable, _karma, _pvpFlag;
	}
	
	private Relation _singled;
	private List<Relation> _multi;
	
	public RelationChanged(L2Playable activeChar, int relation, boolean autoattackable)
	{
		_singled = new Relation();
		_singled._objId = activeChar.getObjectId();
		_singled._relation = relation;
		_singled._autoAttackable = autoattackable ? 1 : 0;
		_singled._karma = activeChar.getKarma();
		_singled._pvpFlag = activeChar.getPvpFlag();
		_invisible = activeChar.isInvisible();
	}
	
	public RelationChanged()
	{
		_multi = FastList.newInstance();
	}
	
	public void addRelation(L2Playable activeChar, int relation, boolean autoattackable)
	{
		if (activeChar.isInvisible())
		{
			throw new IllegalArgumentException("Cannot add insivisble character to multi relation packet");
		}
		Relation r = new Relation();
		r._objId = activeChar.getObjectId();
		r._relation = relation;
		r._autoAttackable = autoattackable ? 1 : 0;
		r._karma = activeChar.getKarma();
		r._pvpFlag = activeChar.getPvpFlag();
		_multi.add(r);
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xce);
		if (_multi == null)
		{
			writeD(1);
			writeRelation(_singled);
		}
		else
		{
			writeD(_multi.size());
			for (Relation r : _multi)
			{
				writeRelation(r);
			}
			FastList.recycle((FastList<?>) _multi);
		}
	}
	
	private void writeRelation(Relation relation)
	{
		writeD(relation._objId);
		writeD(relation._relation);
		writeD(relation._autoAttackable);
		writeD(relation._karma);
		writeD(relation._pvpFlag);
	}
}
