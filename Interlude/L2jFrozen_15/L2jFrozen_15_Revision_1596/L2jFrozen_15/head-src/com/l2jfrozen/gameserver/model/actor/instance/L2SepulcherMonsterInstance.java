package com.l2jfrozen.gameserver.model.actor.instance;

import java.util.concurrent.Future;

import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.managers.FourSepulchersManager;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.quest.QuestState;
import com.l2jfrozen.gameserver.network.serverpackets.CreatureSay;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;

/**
 * @author sandman
 */
public class L2SepulcherMonsterInstance extends L2MonsterInstance
{
	public int mysteriousBoxId = 0;
	
	protected Future<?> victimSpawnKeyBoxTask = null;
	protected Future<?> victimShout = null;
	protected Future<?> changeImmortalTask = null;
	protected Future<?> onDeadEventTask = null;
	
	public L2SepulcherMonsterInstance(final int objectId, final L2NpcTemplate template)
	{
		super(objectId, template);
		// setShowSummonAnimation(true);
	}
	
	@Override
	public void onSpawn()
	{
		super.onSpawn();
		// setShowSummonAnimation(false);
		switch (getNpcId())
		{
			case 18150:
			case 18151:
			case 18152:
			case 18153:
			case 18154:
			case 18155:
			case 18156:
			case 18157:
				if (victimSpawnKeyBoxTask != null)
				{
					victimSpawnKeyBoxTask.cancel(true);
				}
				victimSpawnKeyBoxTask = ThreadPoolManager.getInstance().scheduleEffect(new VictimSpawnKeyBox(this), 300000);
				if (victimShout != null)
				{
					victimShout.cancel(true);
				}
				victimShout = ThreadPoolManager.getInstance().scheduleEffect(new VictimShout(this), 5000);
				break;
			case 18196:
			case 18197:
			case 18198:
			case 18199:
			case 18200:
			case 18201:
			case 18202:
			case 18203:
			case 18204:
			case 18205:
			case 18206:
			case 18207:
			case 18208:
			case 18209:
			case 18210:
			case 18211:
				break;
			
			case 18231:
			case 18232:
			case 18233:
			case 18234:
			case 18235:
			case 18236:
			case 18237:
			case 18238:
			case 18239:
			case 18240:
			case 18241:
			case 18242:
			case 18243:
				if (changeImmortalTask != null)
				{
					changeImmortalTask.cancel(true);
				}
				changeImmortalTask = ThreadPoolManager.getInstance().scheduleEffect(new ChangeImmortal(this), 1600);
				
				break;
			case 18256:
				break;
		}
	}
	
	@Override
	public boolean doDie(final L2Character killer)
	{
		if (!super.doDie(killer))
		{
			return false;
		}
		
		switch (getNpcId())
		{
			case 18120:
			case 18121:
			case 18122:
			case 18123:
			case 18124:
			case 18125:
			case 18126:
			case 18127:
			case 18128:
			case 18129:
			case 18130:
			case 18131:
			case 18149:
			case 18158:
			case 18159:
			case 18160:
			case 18161:
			case 18162:
			case 18163:
			case 18164:
			case 18165:
			case 18183:
			case 18184:
			case 18212:
			case 18213:
			case 18214:
			case 18215:
			case 18216:
			case 18217:
			case 18218:
			case 18219:
				if (onDeadEventTask != null)
				{
					onDeadEventTask.cancel(true);
				}
				onDeadEventTask = ThreadPoolManager.getInstance().scheduleEffect(new OnDeadEvent(this), 3500);
				break;
			
			case 18150:
			case 18151:
			case 18152:
			case 18153:
			case 18154:
			case 18155:
			case 18156:
			case 18157:
				if (victimSpawnKeyBoxTask != null)
				{
					victimSpawnKeyBoxTask.cancel(true);
					victimSpawnKeyBoxTask = null;
				}
				if (victimShout != null)
				{
					victimShout.cancel(true);
					victimShout = null;
				}
				if (onDeadEventTask != null)
				{
					onDeadEventTask.cancel(true);
				}
				onDeadEventTask = ThreadPoolManager.getInstance().scheduleEffect(new OnDeadEvent(this), 3500);
				break;
			
			case 18141:
			case 18142:
			case 18143:
			case 18144:
			case 18145:
			case 18146:
			case 18147:
			case 18148:
				if (FourSepulchersManager.getInstance().isViscountMobsAnnihilated(mysteriousBoxId))
				{
					if (onDeadEventTask != null)
					{
						onDeadEventTask.cancel(true);
					}
					onDeadEventTask = ThreadPoolManager.getInstance().scheduleEffect(new OnDeadEvent(this), 3500);
				}
				break;
			
			case 18220:
			case 18221:
			case 18222:
			case 18223:
			case 18224:
			case 18225:
			case 18226:
			case 18227:
			case 18228:
			case 18229:
			case 18230:
			case 18231:
			case 18232:
			case 18233:
			case 18234:
			case 18235:
			case 18236:
			case 18237:
			case 18238:
			case 18239:
			case 18240:
				if (FourSepulchersManager.getInstance().isDukeMobsAnnihilated(mysteriousBoxId))
				{
					if (onDeadEventTask != null)
					{
						onDeadEventTask.cancel(true);
					}
					onDeadEventTask = ThreadPoolManager.getInstance().scheduleEffect(new OnDeadEvent(this), 3500);
				}
				break;
			
			case 25339:
			case 25342:
			case 25346:
			case 25349:
				giveCup((L2PcInstance) killer);
				if (onDeadEventTask != null)
				{
					onDeadEventTask.cancel(true);
				}
				onDeadEventTask = ThreadPoolManager.getInstance().scheduleEffect(new OnDeadEvent(this), 8500);
				break;
		}
		return true;
	}
	
	@Override
	public void deleteMe()
	{
		if (victimSpawnKeyBoxTask != null)
		{
			victimSpawnKeyBoxTask.cancel(true);
			victimSpawnKeyBoxTask = null;
		}
		if (onDeadEventTask != null)
		{
			onDeadEventTask.cancel(true);
			onDeadEventTask = null;
		}
		
		super.deleteMe();
	}
	
	@Override
	public boolean isRaid()
	{
		switch (getNpcId())
		{
			case 25339:
			case 25342:
			case 25346:
			case 25349:
				return true;
			default:
				return false;
		}
	}
	
	private void giveCup(final L2PcInstance player)
	{
		final String questId = "620_FourGoblets";
		int cupId = 0;
		final int oldBrooch = 7262;
		
		switch (getNpcId())
		{
			case 25339:
				cupId = 7256;
				break;
			case 25342:
				cupId = 7257;
				break;
			case 25346:
				cupId = 7258;
				break;
			case 25349:
				cupId = 7259;
				break;
		}
		
		if (player.getParty() != null)
		{
			for (final L2PcInstance mem : player.getParty().getPartyMembers())
			{
				final QuestState qs = mem.getQuestState(questId);
				if (qs != null && (qs.isStarted() || qs.isCompleted()) && mem.getInventory().getItemByItemId(oldBrooch) == null)
				{
					mem.addItem("Quest", cupId, 1, mem, true);
				}
			}
		}
		else
		{
			final QuestState qs = player.getQuestState(questId);
			if (qs != null && (qs.isStarted() || qs.isCompleted()) && player.getInventory().getItemByItemId(oldBrooch) == null)
			{
				player.addItem("Quest", cupId, 1, player, true);
			}
		}
	}
	
	private class VictimShout implements Runnable
	{
		private final L2SepulcherMonsterInstance activeChar;
		
		public VictimShout(final L2SepulcherMonsterInstance activeChar)
		{
			this.activeChar = activeChar;
		}
		
		@Override
		public void run()
		{
			if (activeChar.isDead())
			{
				return;
			}
			
			if (!activeChar.isVisible())
			{
				return;
			}
			
			broadcastPacket(new CreatureSay(getObjectId(), 0, getName(), "forgive me!!"));
		}
	}
	
	private class VictimSpawnKeyBox implements Runnable
	{
		private final L2SepulcherMonsterInstance activeChar;
		
		public VictimSpawnKeyBox(final L2SepulcherMonsterInstance activeChar)
		{
			this.activeChar = activeChar;
		}
		
		@Override
		public void run()
		{
			if (activeChar.isDead())
			{
				return;
			}
			
			if (!activeChar.isVisible())
			{
				return;
			}
			
			FourSepulchersManager.getInstance().spawnKeyBox(activeChar);
			broadcastPacket(new CreatureSay(getObjectId(), 0, getName(), "Many thanks for rescue me."));
		}
	}
	
	private class OnDeadEvent implements Runnable
	{
		L2SepulcherMonsterInstance activeChar;
		
		public OnDeadEvent(final L2SepulcherMonsterInstance activeChar)
		{
			this.activeChar = activeChar;
		}
		
		@Override
		public void run()
		{
			switch (activeChar.getNpcId())
			{
				case 18120:
				case 18121:
				case 18122:
				case 18123:
				case 18124:
				case 18125:
				case 18126:
				case 18127:
				case 18128:
				case 18129:
				case 18130:
				case 18131:
				case 18149:
				case 18158:
				case 18159:
				case 18160:
				case 18161:
				case 18162:
				case 18163:
				case 18164:
				case 18165:
				case 18183:
				case 18184:
				case 18212:
				case 18213:
				case 18214:
				case 18215:
				case 18216:
				case 18217:
				case 18218:
				case 18219:
					FourSepulchersManager.getInstance().spawnKeyBox(activeChar);
					break;
				
				case 18150:
				case 18151:
				case 18152:
				case 18153:
				case 18154:
				case 18155:
				case 18156:
				case 18157:
					FourSepulchersManager.getInstance().spawnExecutionerOfHalisha(activeChar);
					break;
				
				case 18141:
				case 18142:
				case 18143:
				case 18144:
				case 18145:
				case 18146:
				case 18147:
				case 18148:
					FourSepulchersManager.getInstance().spawnMonster(activeChar.mysteriousBoxId);
					break;
				
				case 18220:
				case 18221:
				case 18222:
				case 18223:
				case 18224:
				case 18225:
				case 18226:
				case 18227:
				case 18228:
				case 18229:
				case 18230:
				case 18231:
				case 18232:
				case 18233:
				case 18234:
				case 18235:
				case 18236:
				case 18237:
				case 18238:
				case 18239:
				case 18240:
					FourSepulchersManager.getInstance().spawnArchonOfHalisha(activeChar.mysteriousBoxId);
					break;
				
				case 25339:
				case 25342:
				case 25346:
				case 25349:
					FourSepulchersManager.getInstance().spawnEmperorsGraveNpc(activeChar.mysteriousBoxId);
					break;
			}
		}
	}
	
	private class ChangeImmortal implements Runnable
	{
		L2SepulcherMonsterInstance activeChar;
		
		public ChangeImmortal(final L2SepulcherMonsterInstance mob)
		{
			activeChar = mob;
		}
		
		@Override
		public void run()
		{
			final L2Skill fp = SkillTable.getInstance().getInfo(4616, 1); // Invulnerable by petrification
			fp.getEffects(activeChar, activeChar, false, false, false);
		}
	}
	
	@Override
	public boolean isAutoAttackable(final L2Character attacker)
	{
		return true;
	}
}
