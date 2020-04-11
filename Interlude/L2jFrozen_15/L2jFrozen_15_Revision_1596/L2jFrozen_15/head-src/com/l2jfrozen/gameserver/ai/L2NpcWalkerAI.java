package com.l2jfrozen.gameserver.ai;

import java.util.List;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.csv.NpcWalkerRoutesTable;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2NpcWalkerNode;
import com.l2jfrozen.gameserver.model.actor.instance.L2NpcWalkerInstance;
import com.l2jfrozen.gameserver.model.actor.position.L2CharPosition;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;

public class L2NpcWalkerAI extends L2CharacterAI implements Runnable
{
	private static final int DEFAULT_MOVE_DELAY = 0;
	
	private long nextMoveTime;
	
	private boolean walkingToNextPoint = false;
	
	/**
	 * home points for xyz
	 */
	int homeX, homeY, homeZ;
	
	/**
	 * route of the current npc
	 */
	private final List<L2NpcWalkerNode> route = NpcWalkerRoutesTable.getInstance().getRouteForNpc(getActor().getNpcId());
	
	/**
	 * current node
	 */
	private int currentPos;
	
	/**
	 * Constructor of L2CharacterAI.<BR>
	 * <BR>
	 * @param accessor The AI accessor of the L2Character
	 */
	public L2NpcWalkerAI(final L2Character.AIAccessor accessor)
	{
		super(accessor);
		// Do we really need 2 minutes delay before start?
		// no we dont... :)
		ThreadPoolManager.getInstance().scheduleAiAtFixedRate(this, 0, 1000);
	}
	
	@Override
	public void run()
	{
		onEvtThink();
	}
	
	@Override
	protected void onEvtThink()
	{
		if (!Config.ALLOW_NPC_WALKERS)
		{
			return;
		}
		
		if (isWalkingToNextPoint())
		{
			checkArrived();
			return;
		}
		
		if (nextMoveTime < System.currentTimeMillis())
		{
			walkToLocation();
		}
	}
	
	/**
	 * If npc can't walk to it's target then just teleport to next point
	 * @param blocked_at_pos ignoring it
	 */
	@Override
	protected void onEvtArrivedBlocked(final L2CharPosition blocked_at_pos)
	{
		LOGGER.warn("NpcWalker ID: " + getActor().getNpcId() + ": Blocked at rote position [" + currentPos + "], coords: " + blocked_at_pos.x + ", " + blocked_at_pos.y + ", " + blocked_at_pos.z + ". Teleporting to next point");
		
		if (route.size() <= currentPos)
		{
			return;
		}
		
		final int destinationX = route.get(currentPos).getMoveX();
		final int destinationY = route.get(currentPos).getMoveY();
		final int destinationZ = route.get(currentPos).getMoveZ();
		
		getActor().teleToLocation(destinationX, destinationY, destinationZ, false);
		super.onEvtArrivedBlocked(blocked_at_pos);
	}
	
	private void checkArrived()
	{
		if (route.size() <= currentPos)
		{
			return;
		}
		
		final int destinationX = route.get(currentPos).getMoveX();
		final int destinationY = route.get(currentPos).getMoveY();
		final int destinationZ = route.get(currentPos).getMoveZ();
		
		if (getActor().getX() == destinationX && getActor().getY() == destinationY && getActor().getZ() == destinationZ)
		{
			String chat = route.get(currentPos).getChatText();
			
			if (chat != null && !chat.equals("NULL"))
			{
				try
				{
					getActor().broadcastChat(chat);
				}
				catch (final ArrayIndexOutOfBoundsException e)
				{
					if (Config.ENABLE_ALL_EXCEPTIONS)
					{
						e.printStackTrace();
					}
					
					LOGGER.info("L2NpcWalkerInstance: Error, " + e);
				}
			}
			chat = null;
			
			// time in millis
			long delay = route.get(currentPos).getDelay() * 1000;
			
			// sleeps between each move
			if (delay < 0)
			{
				delay = DEFAULT_MOVE_DELAY;
				if (Config.DEVELOPER)
				{
					LOGGER.warn("Wrong Delay Set in Npc Walker Functions = " + delay + " secs, using default delay: " + DEFAULT_MOVE_DELAY + " secs instead.");
				}
			}
			
			nextMoveTime = System.currentTimeMillis() + delay;
			setWalkingToNextPoint(false);
		}
	}
	
	private void walkToLocation()
	{
		if (currentPos < route.size() - 1)
		{
			currentPos++;
		}
		else
		{
			currentPos = 0;
		}
		
		if (route.size() <= currentPos)
		{
			return;
		}
		
		final boolean moveType = route.get(currentPos).getRunning();
		
		/**
		 * false - walking true - Running
		 */
		if (moveType)
		{
			getActor().setRunning();
		}
		else
		{
			getActor().setWalking();
		}
		
		// now we define destination
		final int destinationX = route.get(currentPos).getMoveX();
		final int destinationY = route.get(currentPos).getMoveY();
		final int destinationZ = route.get(currentPos).getMoveZ();
		
		// notify AI of MOVE_TO
		setWalkingToNextPoint(true);
		
		setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(destinationX, destinationY, destinationZ, 0));
	}
	
	@Override
	public L2NpcWalkerInstance getActor()
	{
		return (L2NpcWalkerInstance) super.getActor();
	}
	
	public int getHomeX()
	{
		return homeX;
	}
	
	public int getHomeY()
	{
		return homeY;
	}
	
	public int getHomeZ()
	{
		return homeZ;
	}
	
	public void setHomeX(final int homeX)
	{
		this.homeX = homeX;
	}
	
	public void setHomeY(final int homeY)
	{
		this.homeY = homeY;
	}
	
	public void setHomeZ(final int homeZ)
	{
		this.homeZ = homeZ;
	}
	
	public boolean isWalkingToNextPoint()
	{
		return walkingToNextPoint;
	}
	
	public void setWalkingToNextPoint(final boolean value)
	{
		walkingToNextPoint = value;
	}
}
