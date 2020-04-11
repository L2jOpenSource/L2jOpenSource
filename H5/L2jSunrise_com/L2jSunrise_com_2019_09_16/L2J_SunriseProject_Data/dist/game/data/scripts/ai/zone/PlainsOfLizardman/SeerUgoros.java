package ai.zone.PlainsOfLizardman;

import java.util.concurrent.ScheduledFuture;

import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.data.xml.impl.SkillData;
import l2r.gameserver.enums.CtrlIntention;
import l2r.gameserver.instancemanager.MapRegionManager;
import l2r.gameserver.instancemanager.ZoneManager;
import l2r.gameserver.model.L2MapRegion;
import l2r.gameserver.model.L2World;
import l2r.gameserver.model.actor.L2Attackable;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.quest.QuestState;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.model.zone.L2ZoneType;
import l2r.gameserver.network.serverpackets.NpcSay;
import l2r.gameserver.util.Util;
import l2r.util.Rnd;

import ai.npc.AbstractNpcAI;
import quests.Q00288_HandleWithCare.Q00288_HandleWithCare;
import quests.Q00423_TakeYourBestShot.Q00423_TakeYourBestShot;

/**
 * @author vGodFather
 */
public class SeerUgoros extends AbstractNpcAI
{
	// Boss attack task
	ScheduledFuture<?> _thinkTask = null;
	// Item
	private static final int _ugoros_pass = 15496;
	private static final int _mid_scale = 15498;
	private static final int _high_scale = 15497;
	// Zone ID
	private static final int _ugoros_zone = 20706;
	// NPC ID
	private static final int _seer_ugoros = 18863;
	private static final int _batracos = 32740;
	private static final int _weed_id = 18867;
	// Ugoros
	static L2Npc _ugoros = null;
	// Weed
	static L2Npc _weed = null;
	// State
	static boolean _weed_attack = false;
	// Killer
	private static boolean _weed_killed_by_player = false;
	private static boolean _killed_one_weed = false;
	// Player
	static L2PcInstance _player = null;
	// State
	private static final byte ALIVE = 0;
	private static final byte FIGHTING = 1;
	private static final byte DEAD = 2;
	// State
	static byte STATE = DEAD;
	// Skill
	private static final L2Skill _ugoros_skill = SkillData.getInstance().getInfo(6426, 1);
	
	public SeerUgoros()
	{
		super(SeerUgoros.class.getSimpleName(), "ai/zone/PlainsOfLizardman");
		
		addStartNpc(_batracos);
		addTalkId(_batracos);
		addKillId(_seer_ugoros);
		addAttackId(_weed_id);
		
		startQuestTimer("ugoros_respawn", 60000, null, null);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("ugoros_respawn") && (_ugoros == null))
		{
			_ugoros = addSpawn(_seer_ugoros, 96804, 85604, -3720, 34360, false, 0);
			
			broadcastInRegion(_ugoros, "Listen, oh Tantas! I have returned! The Prophet Yugoros of the Black Abyss is with me, so do not be afraid!");
			
			STATE = ALIVE;
			
			startQuestTimer("ugoros_shout", 120000, null, null);
		}
		else if (event.equalsIgnoreCase("ugoros_shout"))
		{
			if (STATE == FIGHTING)
			{
				L2ZoneType _zone = ZoneManager.getInstance().getZoneById(_ugoros_zone);
				if (_player == null)
				{
					STATE = ALIVE;
				}
				else if (!_zone.isCharacterInZone(_player))
				{
					STATE = ALIVE;
					_player = null;
				}
			}
			else if (STATE == ALIVE)
			{
				broadcastInRegion(_ugoros, "Listen, oh Tantas! The Black Abyss is famished! Find some fresh offerings!");
			}
			startQuestTimer("ugoros_shout", 120000, null, null);
		}
		else if (event.equalsIgnoreCase("ugoros_attack"))
		{
			if (_player != null)
			{
				changeAttackTarget(_player);
				
				broadcastInRegion(_ugoros, "Welcome, " + _player.getName() + "! Let us see if you have broght a worthy offering for the Black Abyss!");
				
				if (_thinkTask != null)
				{
					_thinkTask.cancel(true);
				}
				
				_thinkTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new ThinkTask(), 1000, 3000);
			}
		}
		else if (event.equalsIgnoreCase("weed_check"))
		{
			if ((_weed_attack == true) && (_ugoros != null) && (_weed != null))
			{
				if (_weed.isDead() && !_weed_killed_by_player)
				{
					_killed_one_weed = true;
					_weed = null;
					_weed_attack = false;
					_ugoros.getStatus().setCurrentHp(_ugoros.getStatus().getCurrentHp() + (_ugoros.getMaxHp() * 0.2));
					_ugoros.broadcastPacket(new NpcSay(_ugoros.getObjectId(), 0, _ugoros.getId(), "What a formidable foe! But i have the Abyss Weed given to me by the Black Abyss! Let me see..."));
				}
				else
				{
					startQuestTimer("weed_check", 2000, null, null);
				}
			}
			else
			{
				_weed = null;
				_weed_attack = false;
			}
		}
		else if (event.equalsIgnoreCase("ugoros_expel"))
		{
			if (_player != null)
			{
				_player.teleToLocation(94701, 83053, -3580);
				_player = null;
			}
		}
		else if (event.equalsIgnoreCase("teleportInside"))
		{
			if ((player != null) && (STATE == ALIVE))
			{
				if (player.getInventory().getItemByItemId(_ugoros_pass) != null)
				{
					STATE = FIGHTING;
					
					_player = player;
					_killed_one_weed = false;
					
					player.teleToLocation(95984, 85692, -3720);
					player.destroyItemByItemId("SeerUgoros", _ugoros_pass, 1, npc, true);
					
					startQuestTimer("ugoros_attack", 2000, null, null);
					
					QuestState st = player.getQuestState(Q00288_HandleWithCare.class.getSimpleName());
					if (st != null)
					{
						st.set("drop", "1");
					}
				}
				else
				{
					QuestState st = player.getQuestState(Q00423_TakeYourBestShot.class.getSimpleName());
					if (st == null)
					{
						return "<html><body>Gatekeeper Batracos:<br>You look too inexperienced to make a journey to see Tanta Seer Ugoros. If you can convince Chief Investigator Johnny that you should go, then I will let you pass. Johnny has been everywhere and done everything. He may not be of my people but he has my respect, and anyone who has his will in turn have mine as well.<br></body></html>";
					}
					return "<html><body>Gatekeeper Batracos:<br>Tanta Seer Ugoros is hard to find. You'll just have to keep looking.<br></body></html>";
				}
			}
			else
			{
				return "<html><body>Gatekeeper Batracos:<br>Tanta Seer Ugoros is hard to find. You'll just have to keep looking.<br></body></html>";
			}
		}
		else if (event.equalsIgnoreCase("teleport_back"))
		{
			if (player != null)
			{
				player.teleToLocation(94701, 83053, -3580);
				_player = null;
			}
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		if (npc.isDead())
		{
			return null;
		}
		
		switch (npc.getId())
		{
			case _weed_id:
				if ((_ugoros != null) && (_weed != null) && npc.equals(_weed))
				{
					// Reset weed
					_weed = null;
					// Reset attack state
					_weed_attack = false;
					// Set it
					_weed_killed_by_player = true;
					// Complain
					_ugoros.broadcastPacket(new NpcSay(_ugoros.getObjectId(), 0, _ugoros.getId(), "No! How dare you to stop me from using the Abyss Weed... Do you know what you have done?!"));
					// Cancel current think-task
					if (_thinkTask != null)
					{
						_thinkTask.cancel(true);
					}
					// Re-setup task to re-think attack again
					_thinkTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new ThinkTask(), 500, 3000);
				}
				
				npc.doDie(attacker);
				break;
		}
		
		return super.onAttack(npc, attacker, damage, isPet);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		if (npc.getId() == _seer_ugoros)
		{
			if (_thinkTask != null)
			{
				_thinkTask.cancel(true);
				_thinkTask = null;
			}
			
			STATE = DEAD;
			
			broadcastInRegion(_ugoros, "Ah... How could I lose... Oh, Black Abyss, receive me...");
			
			_ugoros = null;
			
			addSpawn(_batracos, 96782, 85918, -3720, 34360, false, 50000);
			
			startQuestTimer("ugoros_expel", 50000, null, null);
			startQuestTimer("ugoros_respawn", 60000, null, null);
			
			QuestState st = player.getQuestState(Q00288_HandleWithCare.class.getSimpleName());
			if ((st != null) && (st.getInt("cond") == 1) && (st.getInt("drop") == 1))
			{
				if (_killed_one_weed)
				{
					player.addItem("SeerUgoros", _mid_scale, 1, npc, true);
					st.set("cond", "2");
				}
				else
				{
					player.addItem("SeerUgoros", _high_scale, 1, npc, true);
					st.set("cond", "3");
				}
				st.unset("drop");
			}
		}
		return null;
	}
	
	private void broadcastInRegion(L2Npc npc, String _text)
	{
		if (npc == null)
		{
			return;
		}
		NpcSay cs = new NpcSay(npc.getObjectId(), 1, npc.getId(), _text);
		L2MapRegion region = MapRegionManager.getInstance().getMapRegion(npc.getX(), npc.getY());
		for (L2PcInstance player : L2World.getInstance().getPlayers())
		{
			if (region == MapRegionManager.getInstance().getMapRegion(player.getX(), player.getY()))
			{
				if (Util.checkIfInRange(6000, npc, player, false))
				{
					player.sendPacket(cs);
				}
			}
		}
	}
	
	private class ThinkTask implements Runnable
	{
		protected ThinkTask()
		{
		}
		
		@Override
		public void run()
		{
			L2ZoneType _zone = ZoneManager.getInstance().getZoneById(_ugoros_zone);
			
			if ((STATE == FIGHTING) && (_player != null) && _zone.isCharacterInZone(_player) && !_player.isDead())
			{
				if (_weed_attack && (_weed != null))
				{
					// Dummy, just wait
				}
				else if (Rnd.get(10) < 6)
				{
					_weed = null;
					
					for (L2Character _char : _ugoros.getKnownList().getKnownCharactersInRadius(2000))
					{
						if ((_char instanceof L2Attackable) && !_char.isDead() && (((L2Attackable) _char).getId() == _weed_id))
						{
							_weed_attack = true;
							_weed = (L2Attackable) _char;
							changeAttackTarget(_weed);
							startQuestTimer("weed_check", 1000, null, null);
							break;
						}
					}
					if (_weed == null)
					{
						changeAttackTarget(_player);
					}
				}
				else
				{
					changeAttackTarget(_player);
				}
			}
			else
			{
				STATE = ALIVE;
				
				_player = null;
				
				if (_thinkTask != null)
				{
					_thinkTask.cancel(true);
					_thinkTask = null;
				}
			}
		}
	}
	
	protected void changeAttackTarget(L2Character _attack)
	{
		final L2Attackable ugoros = ((L2Attackable) _ugoros);
		if (ugoros == null)
		{
			return;
		}
		
		ugoros.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		ugoros.clearAggroList();
		ugoros.setTarget(_attack);
		
		if (_attack instanceof L2Attackable)
		{
			_weed_killed_by_player = false;
			
			ugoros.disableSkill(_ugoros_skill, 100000);
			
			ugoros.setIsRunning(true);
			ugoros.addDamageHate(_attack, 0, Integer.MAX_VALUE);
		}
		else
		{
			ugoros.enableSkill(_ugoros_skill);
			
			ugoros.addDamageHate(_attack, 0, 99);
			ugoros.setIsRunning(false);
		}
		ugoros.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, _attack);
	}
}
