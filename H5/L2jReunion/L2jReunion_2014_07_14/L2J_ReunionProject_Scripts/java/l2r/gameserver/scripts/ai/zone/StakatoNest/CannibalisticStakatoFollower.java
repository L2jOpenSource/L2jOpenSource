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
package l2r.gameserver.scripts.ai.zone.StakatoNest;

import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2MonsterInstance;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.serverpackets.MagicSkillUse;
import l2r.gameserver.scripts.ai.npc.AbstractNpcAI;
import l2r.util.Rnd;

public class CannibalisticStakatoFollower extends AbstractNpcAI
{
	private static final int CANNIBALISTIC_LEADER = 22625;
	
	public CannibalisticStakatoFollower(int questId, String name, String descr)
	{
		super(name, descr);
		
		addAttackId(CANNIBALISTIC_LEADER);
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance player, int damage, boolean isPet)
	{
		if (npc.getMaxHp() * 0.3D > npc.getCurrentHp())
		{
			if (Rnd.get(100) <= 25)
			{
				L2Npc minion = getLeaderMinion(npc);
				if ((minion != null) && (!minion.isDead()))
				{
					npc.broadcastPacket(new MagicSkillUse(npc, minion, 4485, 1, 3000, 0));
					ThreadPoolManager.getInstance().scheduleGeneral(new eatTask(npc, minion), 3000L);
				}
			}
		}
		return super.onAttack(npc, player, damage, isPet);
	}
	
	public L2Npc getLeaderMinion(L2Npc leader)
	{
		if (((L2MonsterInstance) leader).getMinionList().getSpawnedMinions().size() > 0)
		{
			return ((L2MonsterInstance) leader).getMinionList().getSpawnedMinions().get(0);
		}
		return null;
	}
	
	private class eatTask implements Runnable
	{
		private L2Npc _npc;
		private L2Npc _minion;
		
		public eatTask(L2Npc npc, L2Npc minion)
		{
			this._npc = npc;
			this._minion = minion;
		}
		
		@Override
		public void run()
		{
			if (this._minion == null)
			{
				return;
			}
			double hpToSacrifice = this._minion.getCurrentHp();
			this._npc.setCurrentHp(this._npc.getCurrentHp() + hpToSacrifice);
			this._npc.broadcastPacket(new MagicSkillUse(this._npc, this._minion, 4484, 1, 1000, 0));
			this._minion.doDie(this._minion);
		}
	}
	
	public static void main(String[] args)
	{
		new CannibalisticStakatoFollower(-1, CannibalisticStakatoFollower.class.getSimpleName(), "ai");
	}
}
