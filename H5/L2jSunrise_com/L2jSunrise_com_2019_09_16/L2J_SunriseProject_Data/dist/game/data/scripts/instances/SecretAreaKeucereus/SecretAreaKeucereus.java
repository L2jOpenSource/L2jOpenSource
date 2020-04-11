package instances.SecretAreaKeucereus;

import l2r.gameserver.enums.CtrlIntention;
import l2r.gameserver.instancemanager.InstanceManager;
import l2r.gameserver.instancemanager.QuestManager;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.instancezone.InstanceWorld;
import l2r.gameserver.model.quest.Quest;
import l2r.gameserver.model.quest.State;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.SystemMessage;

public class SecretAreaKeucereus extends Quest
{
	private class KSAWorld extends InstanceWorld
	{
		public KSAWorld()
		{
			// InstanceManager.getInstance().super();
		}
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("enter"))
		{
			enterInstance(player, "SecretAreaKeucereus.xml");
		}
		else if (event.equalsIgnoreCase("enter_118"))
		{
			enterInstance118(player, "SecretAreaKeucereus.xml");
		}
		else if (event.equalsIgnoreCase("exit"))
		{
			player.teleToLocation(-184997, 242818, 1578);
			player.setInstanceId(0);
		}
		return "";
	}
	
	public SecretAreaKeucereus()
	{
		super(-1, SecretAreaKeucereus.class.getSimpleName(), "instances");
		addTalkId(32566);
		addTalkId(32567);
	}
	
	private void enterInstance(L2PcInstance player, String template)
	{
		// check for existing instances for this player
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		// existing instance
		if (world != null)
		{
			if (!(world instanceof KSAWorld))
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_ENTERED_ANOTHER_INSTANT_ZONE_THEREFORE_YOU_CANNOT_ENTER_CORRESPONDING_DUNGEON));
				return;
			}
			teleportPlayer(player, (KSAWorld) world);
			return;
		}
		// New instance
		if (checkCond(player))
		{
			world = new KSAWorld();
			world.setInstanceId(InstanceManager.getInstance().createDynamicInstance(template));
			world.setTemplateId(127);
			world.setStatus(0);
			
			InstanceManager.getInstance().addWorld(world);
			_log.info("SecretAreaKeucereus started " + template + " Instance: " + world.getInstanceId() + " created by player: " + player.getName());
			teleportPlayer(player, (KSAWorld) world);
		}
	}
	
	private void enterInstance118(L2PcInstance player, String template)
	{
		// check for existing instances for this player
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		// existing instance
		if (world != null)
		{
			if (!(world instanceof KSAWorld))
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_ENTERED_ANOTHER_INSTANT_ZONE_THEREFORE_YOU_CANNOT_ENTER_CORRESPONDING_DUNGEON));
				return;
			}
			teleportPlayer(player, (KSAWorld) world);
			return;
		}
		// New instance
		if (checkCond118(player))
		{
			world = new KSAWorld();
			world.setInstanceId(InstanceManager.getInstance().createDynamicInstance(template));
			world.setTemplateId(118);
			world.setStatus(0);
			
			InstanceManager.getInstance().addWorld(world);
			_log.info("SecretAreaKeucereus started " + template + " Instance: " + world.getInstanceId() + " created by player: " + player.getName());
			teleportPlayer(player, (KSAWorld) world);
		}
	}
	
	private void teleportPlayer(L2PcInstance player, KSAWorld world)
	{
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		player.teleToLocation(-23530, -8963, -5413);
		player.setInstanceId(world.getInstanceId());
		if (player.getSummon() != null)
		{
			player.getSummon().getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			player.getSummon().setInstanceId(world.getInstanceId());
			player.getSummon().teleToLocation(-23530, -8963, -5413);
		}
	}
	
	private boolean checkCond(L2PcInstance player)
	{
		if (QuestManager.getInstance().getQuest(10270) != null)
		{
			if ((player.getQuestState(QuestManager.getInstance().getQuest(10270).getName()).getState() == State.STARTED) && (player.getQuestState(QuestManager.getInstance().getQuest(10270).getName()).getInt("cond") == 4))
			{
				return true;
			}
		}
		return false;
	}
	
	private boolean checkCond118(L2PcInstance player)
	{
		if (QuestManager.getInstance().getQuest(10272) != null)
		{
			if ((player.getQuestState(QuestManager.getInstance().getQuest(10272).getName()).getState() == State.STARTED) && (player.getQuestState(QuestManager.getInstance().getQuest(10272).getName()).getInt("cond") == 3))
			{
				return true;
			}
		}
		return false;
	}
}