package com.l2jfrozen.gameserver.model;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.Future;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.controllers.GameTimeController;
import com.l2jfrozen.gameserver.datatables.sql.ItemTable;
import com.l2jfrozen.gameserver.managers.DuelManager;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PetInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2SummonInstance;
import com.l2jfrozen.gameserver.model.entity.DimensionalRift;
import com.l2jfrozen.gameserver.model.entity.sevensigns.SevenSignsFestival;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.CreatureSay;
import com.l2jfrozen.gameserver.network.serverpackets.ExCloseMPCC;
import com.l2jfrozen.gameserver.network.serverpackets.ExOpenMPCC;
import com.l2jfrozen.gameserver.network.serverpackets.L2GameServerPacket;
import com.l2jfrozen.gameserver.network.serverpackets.PartyMemberPosition;
import com.l2jfrozen.gameserver.network.serverpackets.PartySmallWindowAdd;
import com.l2jfrozen.gameserver.network.serverpackets.PartySmallWindowAll;
import com.l2jfrozen.gameserver.network.serverpackets.PartySmallWindowDelete;
import com.l2jfrozen.gameserver.network.serverpackets.PartySmallWindowDeleteAll;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.skills.Stats;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;
import com.l2jfrozen.gameserver.util.Util;
import com.l2jfrozen.util.random.Rnd;

/**
 * This class ...
 * @author  nuocnam
 * @version $Revision: 1.6.2.2.2.6 $ $Date: 2005/04/11 19:12:16 $
 */
public class L2Party
{
	private static final double[] BONUS_EXP_SP =
	{
		1,
		1.30,
		1.39,
		1.50,
		1.54,
		1.58,
		1.63,
		1.67,
		1.71
	};
	
	// private static final Logger LOGGER = Logger.getLogger(L2Party.class);
	
	private static final int PARTY_POSITION_BROADCAST = 10000;
	
	public static final int ITEM_LOOTER = 0;
	public static final int ITEM_RANDOM = 1;
	public static final int ITEM_RANDOM_SPOIL = 2;
	public static final int ITEM_ORDER = 3;
	public static final int ITEM_ORDER_SPOIL = 4;
	
	private final List<L2PcInstance> members;
	private boolean pendingInvitation = false;
	private long pendingInviteTimeout;
	private int partyLvl = 0;
	private int itemDistribution = 0;
	private int itemLastLoot = 0;
	
	private L2CommandChannel commandChannel = null;
	private DimensionalRift dimensionalRift;
	
	private Future<?> positionBroadcastTask = null;
	protected PartyMemberPosition positionPacket;
	
	/**
	 * constructor ensures party has always one member - leader
	 * @param leader
	 * @param itemDistribution
	 */
	public L2Party(final L2PcInstance leader, final int itemDistribution)
	{
		members = new ArrayList<>();
		this.itemDistribution = itemDistribution;
		getPartyMembers().add(leader);
		partyLvl = leader.getLevel();
	}
	
	/**
	 * returns number of party members
	 * @return
	 */
	public int getMemberCount()
	{
		return getPartyMembers().size();
	}
	
	/**
	 * Check if another player can start invitation process
	 * @return boolean if party waits for invitation respond
	 */
	public boolean getPendingInvitation()
	{
		return pendingInvitation;
	}
	
	/**
	 * set invitation process flag and store time for expiration happens when: player join party or player decline to join
	 * @param val
	 */
	public void setPendingInvitation(final boolean val)
	{
		pendingInvitation = val;
		pendingInviteTimeout = GameTimeController.getGameTicks() + L2PcInstance.REQUEST_TIMEOUT * GameTimeController.TICKS_PER_SECOND;
	}
	
	/**
	 * Check if player invitation is expired
	 * @return boolean if time is expired
	 * @see    com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance#isRequestExpired()
	 */
	public boolean isInvitationRequestExpired()
	{
		return !(pendingInviteTimeout > GameTimeController.getGameTicks());
	}
	
	public List<L2PcInstance> getPartyMembers()
	{
		return members;
	}
	
	/**
	 * get random member from party
	 * @param  ItemId
	 * @param  target
	 * @return
	 */
	private L2PcInstance getCheckedRandomMember(int ItemId, L2Character target)
	{
		List<L2PcInstance> availableMembers = new ArrayList<>();
		
		for (L2PcInstance member : getPartyMembers())
		{
			if (member.getInventory().validateCapacityByItemId(ItemId) && Util.checkIfInRange(Config.ALT_PARTY_RANGE2, target, member, true))
			{
				availableMembers.add(member);
			}
		}
		
		if (!availableMembers.isEmpty())
		{
			return availableMembers.get(Rnd.get(availableMembers.size()));
		}
		
		return null;
	}
	
	/**
	 * get next item looter
	 * @param  ItemId
	 * @param  target
	 * @return
	 */
	private L2PcInstance getCheckedNextLooter(final int ItemId, final L2Character target)
	{
		for (int i = 0; i < getMemberCount(); i++)
		{
			if (++itemLastLoot >= getMemberCount())
			{
				itemLastLoot = 0;
			}
			
			L2PcInstance member;
			try
			{
				member = getPartyMembers().get(itemLastLoot);
				if (member.getInventory().validateCapacityByItemId(ItemId) && Util.checkIfInRange(Config.ALT_PARTY_RANGE2, target, member, true))
				{
					return member;
				}
			}
			catch (final Exception e)
			{
				// continue, take another member if this just logged off
			}
			member = null;
		}
		
		return null;
	}
	
	/**
	 * get next item looter
	 * @param  player
	 * @param  ItemId
	 * @param  spoil
	 * @param  target
	 * @return
	 */
	private L2PcInstance getActualLooter(final L2PcInstance player, final int ItemId, final boolean spoil, final L2Character target)
	{
		L2PcInstance looter = player;
		
		switch (itemDistribution)
		{
			case ITEM_RANDOM:
				if (!spoil)
				{
					looter = getCheckedRandomMember(ItemId, target);
				}
				break;
			case ITEM_RANDOM_SPOIL:
				looter = getCheckedRandomMember(ItemId, target);
				break;
			case ITEM_ORDER:
				if (!spoil)
				{
					looter = getCheckedNextLooter(ItemId, target);
				}
				break;
			case ITEM_ORDER_SPOIL:
				looter = getCheckedNextLooter(ItemId, target);
				break;
		}
		
		if (looter == null)
		{
			looter = player;
		}
		
		return looter;
	}
	
	/**
	 * true if player is party leader
	 * @param  player
	 * @return
	 */
	public boolean isLeader(final L2PcInstance player)
	{
		return getLeader().equals(player);
	}
	
	/**
	 * Returns the Object ID for the party leader to be used as a unique identifier of this party
	 * @return int
	 */
	public int getPartyLeaderOID()
	{
		return getLeader().getObjectId();
	}
	
	/**
	 * Broadcasts packet to every party member
	 * @param msg
	 */
	public void broadcastToPartyMembers(final L2GameServerPacket msg)
	{
		for (final L2PcInstance member : getPartyMembers())
		{
			if (member != null)
			{
				member.sendPacket(msg);
			}
		}
	}
	
	public void broadcastToPartyMembersNewLeader()
	{
		for (final L2PcInstance member : getPartyMembers())
		{
			if (member != null)
			{
				member.sendPacket(new PartySmallWindowDeleteAll());
				member.sendPacket(new PartySmallWindowAll(member, this));
				member.broadcastUserInfo();
			}
		}
	}
	
	public void broadcastCSToPartyMembers(final CreatureSay msg, final L2PcInstance broadcaster)
	{
		for (final L2PcInstance member : getPartyMembers())
		{
			if (member == null || broadcaster == null)
			{
				continue;
			}
			
			final boolean blocked = member.getBlockList().isInBlockList(broadcaster.getName());
			
			if (!blocked)
			{
				member.sendPacket(msg);
			}
		}
	}
	
	/**
	 * Send a Server->Client packet to all other L2PcInstance of the Party.
	 * @param player
	 * @param msg
	 */
	public void broadcastToPartyMembers(final L2PcInstance player, final L2GameServerPacket msg)
	{
		for (final L2PcInstance member : getPartyMembers())
		{
			if (member != null && !member.equals(player))
			{
				member.sendPacket(msg);
			}
		}
	}
	
	/**
	 * adds new member to party
	 * @param player
	 */
	public synchronized void addPartyMember(final L2PcInstance player)
	{
		if (getPartyMembers().contains(player))
		{
			return;
		}
		
		// sends new member party window for all members
		player.sendPacket(new PartySmallWindowAll(player, this));
		
		SystemMessage msg = new SystemMessage(SystemMessageId.YOU_JOINED_S1_PARTY);
		msg.addString(getLeader().getName());
		player.sendPacket(msg);
		msg = null;
		
		msg = new SystemMessage(SystemMessageId.S1_JOINED_PARTY);
		msg.addString(player.getName());
		broadcastToPartyMembers(msg);
		broadcastToPartyMembers(new PartySmallWindowAdd(player, this));
		msg = null;
		
		// add player to party, adjust party level
		getPartyMembers().add(player);
		if (player.getLevel() > partyLvl)
		{
			partyLvl = player.getLevel();
		}
		
		// update partySpelled
		for (final L2PcInstance member : getPartyMembers())
		{
			if (member != null)
			{
				member.updateEffectIcons(true); // update party icons only
				member.broadcastUserInfo();
			}
		}
		
		if (isInDimensionalRift())
		{
			dimensionalRift.partyMemberInvited();
		}
		
		// open the CCInformationwindow
		if (isInCommandChannel())
		{
			player.sendPacket(new ExOpenMPCC());
		}
		
		// activate position task
		if (positionBroadcastTask == null)
		{
			positionBroadcastTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new PositionBroadcast(), PARTY_POSITION_BROADCAST / 2, PARTY_POSITION_BROADCAST);
		}
		
	}
	
	/**
	 * Remove player from party Overloaded method that takes player's name as parameter
	 * @param name
	 */
	public void removePartyMember(final String name)
	{
		final L2PcInstance player = getPlayerByName(name);
		
		if (player != null)
		{
			removePartyMember(player);
		}
	}
	
	/**
	 * Remove player from party
	 * @param player
	 */
	public void removePartyMember(final L2PcInstance player)
	{
		removePartyMember(player, true);
	}
	
	public synchronized void removePartyMember(final L2PcInstance player, final boolean sendMessage)
	{
		if (getPartyMembers().contains(player))
		{
			final boolean isLeader = isLeader(player);
			getPartyMembers().remove(player);
			recalculatePartyLevel();
			
			if (player.isFestivalParticipant())
			{
				SevenSignsFestival.getInstance().updateParticipants(player, this);
			}
			
			if (player.isInDuel())
			{
				DuelManager.getInstance().onRemoveFromParty(player);
			}
			
			if (sendMessage)
			{
				player.sendPacket(new SystemMessage(SystemMessageId.YOU_LEFT_PARTY));
				SystemMessage msg = new SystemMessage(SystemMessageId.S1_LEFT_PARTY);
				msg.addString(player.getName());
				broadcastToPartyMembers(msg);
				msg = null;
			}
			
			player.sendPacket(new PartySmallWindowDeleteAll());
			player.setParty(null);
			
			broadcastToPartyMembers(new PartySmallWindowDelete(player));
			
			if (isInDimensionalRift())
			{
				dimensionalRift.partyMemberExited(player);
			}
			
			// Close the CCInfoWindow
			if (isInCommandChannel())
			{
				player.sendPacket(new ExCloseMPCC());
			}
			
			if (isLeader && getPartyMembers().size() > 1)
			{
				SystemMessage msg = new SystemMessage(SystemMessageId.S1_HAS_BECOME_A_PARTY_LEADER);
				msg.addString(getLeader().getName());
				broadcastToPartyMembers(msg);
				msg = null;
				broadcastToPartyMembersNewLeader();
			}
			
			if (getPartyMembers().size() == 1)
			{
				if (isInCommandChannel())
				{
					// delete the whole command channel when the party who opened the channel is disbanded
					if (getCommandChannel().getChannelLeader().equals(getLeader()))
					{
						getCommandChannel().disbandChannel();
					}
					else
					{
						getCommandChannel().removeParty(this);
					}
				}
				
				final L2PcInstance leader = getLeader();
				if (leader != null)
				{
					leader.setParty(null);
					if (leader.isInDuel())
					{
						DuelManager.getInstance().onRemoveFromParty(leader);
					}
				}
				
				if (positionBroadcastTask != null)
				{
					positionBroadcastTask.cancel(false);
					positionBroadcastTask = null;
				}
				members.clear();
			}
		}
	}
	
	/**
	 * Change party leader (used for string arguments)
	 * @param name
	 */
	
	public void changePartyLeader(final String name)
	{
		final L2PcInstance player = getPlayerByName(name);
		
		if (player != null && !player.isInDuel())
		{
			if (getPartyMembers().contains(player))
			{
				if (isLeader(player))
				{
					player.sendPacket(new SystemMessage(SystemMessageId.YOU_CANNOT_TRANSFER_RIGHTS_TO_YOURSELF));
				}
				else
				{
					// Swap party members
					L2PcInstance temp;
					final int p1 = getPartyMembers().indexOf(player);
					temp = getLeader();
					getPartyMembers().set(0, getPartyMembers().get(p1));
					getPartyMembers().set(p1, temp);
					
					SystemMessage msg = new SystemMessage(SystemMessageId.S1_HAS_BECOME_A_PARTY_LEADER);
					msg.addString(getLeader().getName());
					broadcastToPartyMembers(msg);
					broadcastToPartyMembersNewLeader();
					
					if (isInCommandChannel() && temp.equals(commandChannel.getChannelLeader()))
					{
						commandChannel.setChannelLeader(getLeader());
						msg = new SystemMessage(SystemMessageId.COMMAND_CHANNEL_LEADER_NOW_S1);
						msg.addString(commandChannel.getChannelLeader().getName());
						commandChannel.broadcastToChannelMembers(msg);
					}
					
					if (player.isInPartyMatchRoom())
					{
						final PartyMatchRoom room = PartyMatchRoomList.getInstance().getPlayerRoom(player);
						room.changeLeader(player);
					}
				}
			}
			else
			{
				player.sendPacket(new SystemMessage(SystemMessageId.YOU_CAN_TRANSFER_RIGHTS_ONLY_TO_ANOTHER_PARTY_MEMBER));
			}
		}
	}
	
	/**
	 * finds a player in the party by name
	 * @param  name
	 * @return
	 */
	private L2PcInstance getPlayerByName(final String name)
	{
		for (final L2PcInstance member : getPartyMembers())
		{
			if (member.getName().equalsIgnoreCase(name))
			{
				return member;
			}
		}
		return null;
	}
	
	/**
	 * distribute item(s) to party members
	 * @param player
	 * @param item
	 */
	public void distributeItem(final L2PcInstance player, final L2ItemInstance item)
	{
		if (item.getItemId() == 57)
		{
			distributeAdena(player, item.getCount(), player);
			ItemTable.getInstance().destroyItem("Party", item, player, null);
			return;
		}
		
		L2PcInstance target = getActualLooter(player, item.getItemId(), false, player);
		target.addItem("Party", item, player, true);
		
		// Send messages to other party members about reward
		if (item.getCount() > 1)
		{
			SystemMessage msg = new SystemMessage(SystemMessageId.S1_PICKED_UP_S2_S3);
			msg.addString(target.getName());
			msg.addItemName(item.getItemId());
			msg.addNumber(item.getCount());
			broadcastToPartyMembers(target, msg);
			msg = null;
		}
		else
		{
			SystemMessage msg = new SystemMessage(SystemMessageId.S1_PICKED_UP_S2);
			msg.addString(target.getName());
			msg.addItemName(item.getItemId());
			broadcastToPartyMembers(target, msg);
			msg = null;
		}
		
		target = null;
	}
	
	/**
	 * distribute item(s) to party members
	 * @param player
	 * @param item
	 * @param spoil
	 * @param target
	 */
	public void distributeItem(final L2PcInstance player, final L2Attackable.RewardItem item, final boolean spoil, final L2Attackable target)
	{
		if (item == null)
		{
			return;
		}
		
		if (item.getItemId() == 57)
		{
			distributeAdena(player, item.getCount(), target);
			return;
		}
		
		L2PcInstance looter = getActualLooter(player, item.getItemId(), spoil, target);
		
		looter.addItem(spoil ? "Sweep" : "Party", item.getItemId(), item.getCount(), player, true);
		
		// Send messages to other aprty members about reward
		if (item.getCount() > 1)
		{
			SystemMessage msg = spoil ? new SystemMessage(SystemMessageId.S1_SWEEPED_UP_S2_S3) : new SystemMessage(SystemMessageId.S1_PICKED_UP_S2_S3);
			msg.addString(looter.getName());
			msg.addItemName(item.getItemId());
			msg.addNumber(item.getCount());
			broadcastToPartyMembers(looter, msg);
			msg = null;
		}
		else
		{
			SystemMessage msg = spoil ? new SystemMessage(SystemMessageId.S1_SWEEPED_UP_S2) : new SystemMessage(SystemMessageId.S1_PICKED_UP_S2);
			msg.addString(looter.getName());
			msg.addItemName(item.getItemId());
			broadcastToPartyMembers(looter, msg);
			msg = null;
		}
		
		looter = null;
	}
	
	/**
	 * distribute adena to party members
	 * @param player
	 * @param adena
	 * @param target
	 */
	public void distributeAdena(final L2PcInstance player, final int adena, final L2Character target)
	{
		// Get all the party members
		final List<L2PcInstance> membersList = getPartyMembers();
		
		// Check the number of party members that must be rewarded
		// (The party member must be in range to receive its reward)
		List<L2PcInstance> ToReward = new ArrayList<>();
		
		for (final L2PcInstance member : membersList)
		{
			if (!Util.checkIfInRange(Config.ALT_PARTY_RANGE2, target, member, true))
			{
				continue;
			}
			
			ToReward.add(member);
		}
		
		// Avoid null exceptions, if any
		if (ToReward.isEmpty())
		{
			return;
		}
		
		// Now we can actually distribute the adena reward
		// (Total adena split by the number of party members that are in range and must be rewarded)
		final int count = adena / ToReward.size();
		
		for (L2PcInstance member : ToReward)
		{
			if (member.isVIP())
			{
				member.addAdena("Party", (int) (count * Config.VIP_ADENA_RATE), player, true);
			}
			else
			{
				member.addAdena("Party", count, player, true);
			}
		}
	}
	
	/**
	 * Distribute Experience and SP rewards to L2PcInstance Party members in the known area of the last attacker.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Get the L2PcInstance owner of the L2SummonInstance (if necessary)</li>
	 * <li>Calculate the Experience and SP reward distribution rate</li>
	 * <li>Add Experience and SP to the L2PcInstance</li><BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T GIVE rewards to L2PetInstance</B></FONT><BR>
	 * <BR>
	 * Exception are L2PetInstances that leech from the owner's XP; they get the exp indirectly, via the owner's exp gain<BR>
	 * @param xpReward        The Experience reward to distribute
	 * @param spReward        The SP reward to distribute
	 * @param rewardedMembers The list of L2PcInstance to reward
	 * @param topLvl
	 */
	public void distributeXpAndSp(long xpReward, int spReward, final List<L2PlayableInstance> rewardedMembers, final int topLvl)
	{
		L2SummonInstance summon = null;
		List<L2PlayableInstance> validMembers = getValidMembers(rewardedMembers, topLvl);
		
		float penalty;
		double sqLevel;
		double preCalculation;
		
		xpReward *= getExpBonus(validMembers.size());
		spReward *= getSpBonus(validMembers.size());
		
		double sqLevelSum = 0;
		
		for (final L2PlayableInstance character : validMembers)
		{
			sqLevelSum += character.getLevel() * character.getLevel();
		}
		
		// Go through the L2PcInstances and L2PetInstances (not L2SummonInstances) that must be rewarded
		synchronized (rewardedMembers)
		{
			for (final L2Character member : rewardedMembers)
			{
				if (member.isDead())
				{
					continue;
				}
				
				penalty = 0;
				
				// The L2SummonInstance penalty
				if (member.getPet() instanceof L2SummonInstance)
				{
					summon = (L2SummonInstance) member.getPet();
					penalty = summon.getExpPenalty();
				}
				
				// Pets that leech xp from the owner (like babypets) do not get rewarded directly
				if (member instanceof L2PetInstance)
				{
					if (((L2PetInstance) member).getPetData().getOwnerExpTaken() > 0)
					{
						continue;
					}
					// TODO: This is a temporary fix while correct pet xp in party is figured out
					penalty = (float) 0.85;
				}
				
				// Calculate and add the EXP and SP reward to the member
				if (validMembers.contains(member))
				{
					sqLevel = member.getLevel() * member.getLevel();
					preCalculation = sqLevel / sqLevelSum * (1 - penalty);
					
					// Add the XP/SP points to the requested party member
					if (!member.isDead())
					{
						member.addExpAndSp(Math.round(member.calcStat(Stats.EXPSP_RATE, xpReward * preCalculation, null, null)), (int) member.calcStat(Stats.EXPSP_RATE, spReward * preCalculation, null, null));
					}
				}
				else
				{
					member.addExpAndSp(0, 0);
				}
			}
		}
		
		summon = null;
		validMembers = null;
	}
	
	/**
	 * Calculates and gives final XP and SP rewards to the party member.<BR>
	 * This method takes in consideration number of members, members' levels, rewarder's level and bonus modifier for the actual party.<BR>
	 * <BR>
	 * @param member   is the L2Character to be rewarded
	 * @param xpReward is the total amount of XP to be "splited" and given to the member
	 * @param spReward is the total amount of SP to be "splited" and given to the member
	 * @param penalty  is the penalty that must be applied to the XP rewards of the requested member
	 */
	
	/**
	 * refresh party level
	 */
	public void recalculatePartyLevel()
	{
		int newLevel = 0;
		
		for (final L2PcInstance member : getPartyMembers())
		{
			if (member == null)
			{
				getPartyMembers().remove(member);
				continue;
			}
			
			if (member.getLevel() > newLevel)
			{
				newLevel = member.getLevel();
			}
		}
		
		partyLvl = newLevel;
	}
	
	private List<L2PlayableInstance> getValidMembers(final List<L2PlayableInstance> members, final int topLvl)
	{
		final List<L2PlayableInstance> validMembers = new ArrayList<>();
		
		// Fixed LevelDiff cutoff point
		if (Config.PARTY_XP_CUTOFF_METHOD.equalsIgnoreCase("level"))
		{
			for (final L2PlayableInstance member : members)
			{
				if (topLvl - member.getLevel() <= Config.PARTY_XP_CUTOFF_LEVEL)
				{
					validMembers.add(member);
				}
			}
		}
		// Fixed MinPercentage cutoff point
		else if (Config.PARTY_XP_CUTOFF_METHOD.equalsIgnoreCase("percentage"))
		{
			int sqLevelSum = 0;
			
			for (final L2PlayableInstance member : members)
			{
				sqLevelSum += member.getLevel() * member.getLevel();
			}
			
			for (final L2PlayableInstance member : members)
			{
				final int sqLevel = member.getLevel() * member.getLevel();
				
				if (sqLevel * 100 >= sqLevelSum * Config.PARTY_XP_CUTOFF_PERCENT)
				{
					validMembers.add(member);
				}
			}
		}
		// Automatic cutoff method
		else if (Config.PARTY_XP_CUTOFF_METHOD.equalsIgnoreCase("auto"))
		{
			int sqLevelSum = 0;
			
			for (final L2PlayableInstance member : members)
			{
				sqLevelSum += member.getLevel() * member.getLevel();
			}
			
			int i = members.size() - 1;
			
			if (i < 1)
			{
				return members;
			}
			
			if (i >= BONUS_EXP_SP.length)
			{
				i = BONUS_EXP_SP.length - 1;
			}
			
			for (final L2PlayableInstance member : members)
			{
				final int sqLevel = member.getLevel() * member.getLevel();
				
				if (sqLevel >= sqLevelSum * (1 - 1 / (1 + BONUS_EXP_SP[i] - BONUS_EXP_SP[i - 1])))
				{
					validMembers.add(member);
				}
			}
		}
		return validMembers;
	}
	
	private double getBaseExpSpBonus(final int membersCount)
	{
		int i = membersCount - 1;
		
		if (i < 1)
		{
			return 1;
		}
		
		if (i >= BONUS_EXP_SP.length)
		{
			i = BONUS_EXP_SP.length - 1;
		}
		
		return BONUS_EXP_SP[i];
	}
	
	private double getExpBonus(final int membersCount)
	{
		if (membersCount < 2)
		{
			// not is a valid party
			return getBaseExpSpBonus(membersCount);
		}
		return getBaseExpSpBonus(membersCount) * Config.RATE_PARTY_XP;
	}
	
	private double getSpBonus(final int membersCount)
	{
		if (membersCount < 2)
		{
			// not is a valid party
			return getBaseExpSpBonus(membersCount);
		}
		return getBaseExpSpBonus(membersCount) * Config.RATE_PARTY_SP;
	}
	
	public int getLevel()
	{
		return partyLvl;
	}
	
	public int getLootDistribution()
	{
		return itemDistribution;
	}
	
	public boolean isInCommandChannel()
	{
		return commandChannel != null;
	}
	
	public L2CommandChannel getCommandChannel()
	{
		return commandChannel;
	}
	
	public void setCommandChannel(final L2CommandChannel channel)
	{
		commandChannel = channel;
	}
	
	public boolean isInDimensionalRift()
	{
		return dimensionalRift != null;
	}
	
	public void setDimensionalRift(final DimensionalRift dr)
	{
		dimensionalRift = dr;
	}
	
	public DimensionalRift getDimensionalRift()
	{
		return dimensionalRift;
	}
	
	public L2PcInstance getLeader()
	{
		try
		{
			return members.get(0);
		}
		catch (final NoSuchElementException e)
		{
			return null;
		}
	}
	
	protected class PositionBroadcast implements Runnable
	{
		@Override
		public void run()
		{
			if (positionPacket == null)
			{
				positionPacket = new PartyMemberPosition(L2Party.this);
			}
			else
			{
				positionPacket.reuse(L2Party.this);
			}
			
			broadcastToPartyMembers(positionPacket);
		}
	}
}
