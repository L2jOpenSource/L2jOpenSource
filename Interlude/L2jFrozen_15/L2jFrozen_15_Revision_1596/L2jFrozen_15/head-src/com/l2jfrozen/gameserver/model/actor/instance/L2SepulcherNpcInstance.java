package com.l2jfrozen.gameserver.model.actor.instance;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.ai.CtrlIntention;
import com.l2jfrozen.gameserver.datatables.csv.DoorTable;
import com.l2jfrozen.gameserver.managers.FourSepulchersManager;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.quest.Quest;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.CreatureSay;
import com.l2jfrozen.gameserver.network.serverpackets.MyTargetSelected;
import com.l2jfrozen.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jfrozen.gameserver.network.serverpackets.SocialAction;
import com.l2jfrozen.gameserver.network.serverpackets.StatusUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.ValidateLocation;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;
import com.l2jfrozen.gameserver.util.Util;
import com.l2jfrozen.util.random.Rnd;

/**
 * @author sandman
 */
public class L2SepulcherNpcInstance extends L2NpcInstance
{
	protected static Map<Integer, Integer> hallGateKeepers = new HashMap<>();
	
	protected Future<?> closeTask = null;
	protected Future<?> spawnNextMysteriousBoxTask = null;
	protected Future<?> spawnMonsterTask = null;
	
	private final static String HTML_FILE_PATH = "data/html/SepulcherNpc/";
	private final static int HALLS_KEY = 7260;
	
	public L2SepulcherNpcInstance(final int objectID, final L2NpcTemplate template)
	{
		super(objectID, template);
		// setShowSummonAnimation(true);
		
		if (closeTask != null)
		{
			closeTask.cancel(true);
		}
		if (spawnNextMysteriousBoxTask != null)
		{
			spawnNextMysteriousBoxTask.cancel(true);
		}
		if (spawnMonsterTask != null)
		{
			spawnMonsterTask.cancel(true);
		}
		closeTask = null;
		spawnNextMysteriousBoxTask = null;
		spawnMonsterTask = null;
	}
	
	@Override
	public void deleteMe()
	{
		if (closeTask != null)
		{
			closeTask.cancel(true);
			closeTask = null;
		}
		if (spawnNextMysteriousBoxTask != null)
		{
			spawnNextMysteriousBoxTask.cancel(true);
			spawnNextMysteriousBoxTask = null;
		}
		if (spawnMonsterTask != null)
		{
			spawnMonsterTask.cancel(true);
			spawnMonsterTask = null;
		}
		super.deleteMe();
	}
	
	@Override
	public void onAction(final L2PcInstance player)
	{
		if (!canTarget(player))
		{
			return;
		}
		
		// Check if the L2PcInstance already target the L2NpcInstance
		if (this != player.getTarget())
		{
			if (Config.DEBUG)
			{
				LOGGER.info("new target selected:" + getObjectId());
			}
			
			// Set the target of the L2PcInstance player
			player.setTarget(this);
			
			// Check if the player is attackable (without a forced attack)
			if (isAutoAttackable(player))
			{
				// Send a Server->Client packet MyTargetSelected to the
				// L2PcInstance player
				// The player.getLevel() - getLevel() permit to display the
				// correct color in the select window
				final MyTargetSelected my = new MyTargetSelected(getObjectId(), player.getLevel() - getLevel());
				player.sendPacket(my);
				
				// Send a Server->Client packet StatusUpdate of the
				// L2NpcInstance to the L2PcInstance to update its HP bar
				final StatusUpdate su = new StatusUpdate(getObjectId());
				su.addAttribute(StatusUpdate.CUR_HP, (int) getStatus().getCurrentHp());
				su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
				player.sendPacket(su);
			}
			else
			{
				// Send a Server->Client packet MyTargetSelected to the
				// L2PcInstance player
				final MyTargetSelected my = new MyTargetSelected(getObjectId(), 0);
				player.sendPacket(my);
			}
			
			// Send a Server->Client packet ValidateLocation to correct the
			// L2NpcInstance position and heading on the client
			player.sendPacket(new ValidateLocation(this));
		}
		else
		{
			// Check if the player is attackable (without a forced attack) and
			// isn't dead
			if (isAutoAttackable(player) && !isAlikeDead())
			{
				// Check the height difference
				if (Math.abs(player.getZ() - getZ()) < 400) // this max heigth
				// difference might
				// need some tweaking
				{
					// Set the L2PcInstance Intention to AI_INTENTION_ATTACK
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
				}
				else
				{
					// Send a Server->Client packet ActionFailed (target is out
					// of attack range) to the L2PcInstance player
					player.sendPacket(ActionFailed.STATIC_PACKET);
				}
			}
			
			if (!isAutoAttackable(player))
			{
				// Calculate the distance between the L2PcInstance and the
				// L2NpcInstance
				if (!canInteract(player))
				{
					// Notify the L2PcInstance AI with AI_INTENTION_INTERACT
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
				}
				else
				{
					// Send a Server->Client packet SocialAction to the all
					// L2PcInstance on the knownPlayer of the L2NpcInstance
					// to display a social action of the L2NpcInstance on their
					// client
					final SocialAction sa = new SocialAction(getObjectId(), Rnd.get(8));
					broadcastPacket(sa);
					
					doAction(player);
				}
			}
			// Send a Server->Client ActionFailed to the L2PcInstance in order
			// to avoid that the client wait another packet
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
	}
	
	private void doAction(L2PcInstance player)
	{
		if (isDead())
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		switch (getNpcId())
		{
			case 31468:
			case 31469:
			case 31470:
			case 31471:
			case 31472:
			case 31473:
			case 31474:
			case 31475:
			case 31476:
			case 31477:
			case 31478:
			case 31479:
			case 31480:
			case 31481:
			case 31482:
			case 31483:
			case 31484:
			case 31485:
			case 31486:
			case 31487:
				setIsInvul(false);
				reduceCurrentHp(getMaxHp() + 1, player);
				if (spawnMonsterTask != null)
				{
					spawnMonsterTask.cancel(true);
				}
				spawnMonsterTask = ThreadPoolManager.getInstance().scheduleEffect(new SpawnMonster(getNpcId()), 3500);
				break;
			
			case 31455:
			case 31456:
			case 31457:
			case 31458:
			case 31459:
			case 31460:
			case 31461:
			case 31462:
			case 31463:
			case 31464:
			case 31465:
			case 31466:
			case 31467:
				setIsInvul(false);
				reduceCurrentHp(getMaxHp() + 1, player);
				if (player.getParty() != null && !player.getParty().isLeader(player))
				{
					player = player.getParty().getLeader();
				}
				player.addItem("Quest", HALLS_KEY, 1, player, true);
				break;
			
			default:
			{
				if (getTemplate().getEventQuests(Quest.QuestEventType.QUEST_START).length > 0)
				{
					player.setLastQuestNpcObject(getObjectId());
				}
				final Quest[] qlst = getTemplate().getEventQuests(Quest.QuestEventType.QUEST_TALK);
				if (qlst.length == 1)
				{
					qlst[0].notifyFirstTalk(this, player);
				}
				else
				{
					showChatWindow(player, 0);
				}
			}
		}
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	@Override
	public String getHtmlPath(final int npcId, final int val)
	{
		String pom = "";
		if (val == 0)
		{
			pom = "" + npcId;
		}
		else
		{
			pom = npcId + "-" + val;
		}
		
		return HTML_FILE_PATH + pom + ".htm";
	}
	
	@Override
	public void showChatWindow(final L2PcInstance player, final int val)
	{
		final String filename = getHtmlPath(getNpcId(), val);
		final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(html);
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	@Override
	public void onBypassFeedback(final L2PcInstance player, final String command)
	{
		if (isBusy())
		{
			final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			html.setFile("data/html/npcbusy.htm");
			html.replace("%busymessage%", getBusyMessage());
			html.replace("%npcname%", getName());
			html.replace("%playername%", player.getName());
			player.sendPacket(html);
		}
		else if (command.startsWith("Chat"))
		{
			int val = 0;
			try
			{
				val = Integer.parseInt(command.substring(5));
			}
			catch (IndexOutOfBoundsException | NumberFormatException ioobe)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					ioobe.printStackTrace();
				}
			}
			showChatWindow(player, val);
		}
		else if (command.startsWith("open_gate"))
		{
			final L2ItemInstance hallsKey = player.getInventory().getItemByItemId(HALLS_KEY);
			if (hallsKey == null)
			{
				showHtmlFile(player, "Gatekeeper-no.htm");
			}
			else if (FourSepulchersManager.getInstance().isAttackTime())
			{
				switch (getNpcId())
				{
					case 31929:
					case 31934:
					case 31939:
					case 31944:
						FourSepulchersManager.getInstance().spawnShadow(getNpcId());
						// break;
					default:
					{
						openNextDoor(getNpcId());
						if (player.getParty() != null)
						{
							for (final L2PcInstance mem : player.getParty().getPartyMembers())
							{
								if (mem.getInventory().getItemByItemId(HALLS_KEY) != null)
								{
									mem.destroyItemByItemId("Quest", HALLS_KEY, mem.getInventory().getItemByItemId(HALLS_KEY).getCount(), mem, true);
								}
							}
						}
						else
						{
							player.destroyItemByItemId("Quest", HALLS_KEY, hallsKey.getCount(), player, true);
						}
					}
				}
			}
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}
	
	public void openNextDoor(final int npcId)
	{
		final int doorId = FourSepulchersManager.getInstance().getHallGateKeepers().get(npcId).intValue();
		final DoorTable doorTable = DoorTable.getInstance();
		doorTable.getDoor(doorId).openMe();
		
		if (closeTask != null)
		{
			closeTask.cancel(true);
		}
		closeTask = ThreadPoolManager.getInstance().scheduleEffect(new CloseNextDoor(doorId), 10000);
		if (spawnNextMysteriousBoxTask != null)
		{
			spawnNextMysteriousBoxTask.cancel(true);
		}
		spawnNextMysteriousBoxTask = ThreadPoolManager.getInstance().scheduleEffect(new SpawnNextMysteriousBox(npcId), 0);
	}
	
	private class CloseNextDoor implements Runnable
	{
		final DoorTable doorTable = DoorTable.getInstance();
		
		private final int doorId;
		
		public CloseNextDoor(final int doorId)
		{
			this.doorId = doorId;
		}
		
		@Override
		public void run()
		{
			try
			{
				doorTable.getDoor(doorId).closeMe();
			}
			catch (final Exception e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					e.printStackTrace();
				}
				
				LOGGER.warn(e.getMessage());
			}
		}
	}
	
	private class SpawnNextMysteriousBox implements Runnable
	{
		private final int npcId;
		
		public SpawnNextMysteriousBox(final int npcId)
		{
			this.npcId = npcId;
		}
		
		@Override
		public void run()
		{
			FourSepulchersManager.getInstance().spawnMysteriousBox(npcId);
		}
	}
	
	private class SpawnMonster implements Runnable
	{
		private final int npcId;
		
		public SpawnMonster(final int npcId)
		{
			this.npcId = npcId;
		}
		
		@Override
		public void run()
		{
			FourSepulchersManager.getInstance().spawnMonster(npcId);
		}
	}
	
	public void sayInShout(final String msg)
	{
		if (msg == null || msg.isEmpty())
		{
			return;// wrong usage
		}
		final Collection<L2PcInstance> knownPlayers = L2World.getInstance().getAllPlayers();
		if (knownPlayers == null || knownPlayers.isEmpty())
		{
			return;
		}
		final CreatureSay sm = new CreatureSay(0, 1, getName(), msg);
		for (final L2PcInstance player : knownPlayers)
		{
			if (player == null)
			{
				continue;
			}
			if (Util.checkIfInRange(15000, player, this, true))
			{
				player.sendPacket(sm);
			}
		}
	}
	
	public void showHtmlFile(final L2PcInstance player, final String file)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile("data/html/SepulcherNpc/" + file);
		html.replace("%npcname%", getName());
		player.sendPacket(html);
	}
}
