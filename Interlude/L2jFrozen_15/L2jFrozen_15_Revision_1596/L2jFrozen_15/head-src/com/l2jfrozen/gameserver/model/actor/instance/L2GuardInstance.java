package com.l2jfrozen.gameserver.model.actor.instance;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.ai.CtrlIntention;
import com.l2jfrozen.gameserver.ai.L2AttackableAI;
import com.l2jfrozen.gameserver.model.L2Attackable;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.L2WorldRegion;
import com.l2jfrozen.gameserver.model.actor.knownlist.GuardKnownList;
import com.l2jfrozen.gameserver.model.actor.position.L2CharPosition;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.MyTargetSelected;
import com.l2jfrozen.gameserver.network.serverpackets.SocialAction;
import com.l2jfrozen.gameserver.network.serverpackets.ValidateLocation;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;
import com.l2jfrozen.util.random.Rnd;

/**
 * This class manages all Guards in the world. It inherits all methods from L2Attackable and adds some more such as tracking PK and aggressive L2MonsterInstance.<BR>
 * <BR>
 * @version $Revision: 1.11.2.1.2.7 $ $Date: 2005/04/06 16:13:40 $
 */
public final class L2GuardInstance extends L2Attackable
{
	private static Logger LOGGER = Logger.getLogger(L2GuardInstance.class);
	
	private int homeX;
	private int homeY;
	private int homeZ;
	
	/** The Constant RETURN_INTERVAL. */
	private static final int RETURN_INTERVAL = 60000;
	
	/**
	 * The Class ReturnTask.
	 */
	public class ReturnTask implements Runnable
	{
		@Override
		public void run()
		{
			if (getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE)
			{
				returnHome();
			}
		}
	}
	
	/**
	 * Constructor of L2GuardInstance (use L2Character and L2NpcInstance constructor).<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Call the L2Character constructor to set the template of the L2GuardInstance (copy skills from template to object and link calculators to NPC_STD_CALCULATOR)</li>
	 * <li>Set the name of the L2GuardInstance</li>
	 * <li>Create a RandomAnimation Task that will be launched after the calculated delay if the server allow it</li><BR>
	 * <BR>
	 * @param objectId Identifier of the object to initialized
	 * @param template the template
	 */
	public L2GuardInstance(final int objectId, final L2NpcTemplate template)
	{
		super(objectId, template);
		getKnownList(); // init knownlist
		
		ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new ReturnTask(), RETURN_INTERVAL, RETURN_INTERVAL + Rnd.nextInt(60000));
	}
	
	@Override
	public final GuardKnownList getKnownList()
	{
		if (super.getKnownList() == null || !(super.getKnownList() instanceof GuardKnownList))
		{
			setKnownList(new GuardKnownList(this));
		}
		return (GuardKnownList) super.getKnownList();
	}
	
	/**
	 * Return True if the attacker is a L2MonsterInstance.<BR>
	 * <BR>
	 * @param  attacker the attacker
	 * @return          true, if is auto attackable
	 */
	@Override
	public boolean isAutoAttackable(final L2Character attacker)
	{
		return attacker instanceof L2MonsterInstance;
	}
	
	/**
	 * Set home location of the L2GuardInstance.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * Guard will always try to return to this location after it has killed all PK's in range
	 */
	public void getHomeLocation()
	{
		homeX = getX();
		homeY = getY();
		homeZ = getZ();
		
		if (Config.DEBUG)
		{
			LOGGER.debug(getObjectId() + ": Home location set to" + " X:" + homeX + " Y:" + homeY + " Z:" + homeZ);
		}
	}
	
	/**
	 * Gets the home x.
	 * @return the home x
	 */
	public int getHomeX()
	{
		return homeX;
	}
	
	/**
	 * Notify the L2GuardInstance to return to its home location (AI_INTENTION_MOVE_TO) and clear its aggroList.<BR>
	 * <BR>
	 */
	public void returnHome()
	{
		if (!isInsideRadius(homeX, homeY, 150, false))
		{
			if (Config.DEBUG)
			{
				LOGGER.debug(getObjectId() + ": moving hometo" + " X:" + homeX + " Y:" + homeY + " Z:" + homeZ);
			}
			
			clearAggroList();
			
			getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(homeX, homeY, homeZ, 0));
		}
	}
	
	/**
	 * Set the home location of its L2GuardInstance.<BR>
	 * <BR>
	 */
	@Override
	public void onSpawn()
	{
		super.onSpawn();
		homeX = getX();
		homeY = getY();
		homeZ = getZ();
		
		if (Config.DEBUG)
		{
			LOGGER.debug(getObjectId() + ": Home location set to" + " X:" + homeX + " Y:" + homeY + " Z:" + homeZ);
		}
		
		// check the region where this mob is, do not activate the AI if region is inactive.
		L2WorldRegion region = L2World.getInstance().getRegion(getX(), getY());
		if (region != null && !region.isActive())
		{
			((L2AttackableAI) getAI()).stopAITask();
		}
		region = null;
	}
	
	/**
	 * Return the pathfile of the selected HTML file in function of the L2GuardInstance Identifier and of the page number.<BR>
	 * <BR>
	 * <B><U> Format of the pathfile </U> :</B><BR>
	 * <BR>
	 * <li>if page number = 0 : <B>data/html/guard/12006.htm</B> (npcId-page number)</li>
	 * <li>if page number > 0 : <B>data/html/guard/12006-1.htm</B> (npcId-page number)</li><BR>
	 * <BR>
	 * @param  npcId The Identifier of the L2NpcInstance whose text must be display
	 * @param  val   The number of the page to display
	 * @return       the html path
	 */
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
		return "data/html/guard/" + pom + ".htm";
	}
	
	/**
	 * Manage actions when a player click on the L2GuardInstance.<BR>
	 * <BR>
	 * <B><U> Actions on first click on the L2GuardInstance (Select it)</U> :</B><BR>
	 * <BR>
	 * <li>Set the L2GuardInstance as target of the L2PcInstance player (if necessary)</li>
	 * <li>Send a Server->Client packet MyTargetSelected to the L2PcInstance player (display the select window)</li>
	 * <li>Set the L2PcInstance Intention to AI_INTENTION_IDLE</li>
	 * <li>Send a Server->Client packet ValidateLocation to correct the L2GuardInstance position and heading on the client</li><BR>
	 * <BR>
	 * <B><U> Actions on second click on the L2GuardInstance (Attack it/Interact with it)</U> :</B><BR>
	 * <BR>
	 * <li>If L2PcInstance is in the aggroList of the L2GuardInstance, set the L2PcInstance Intention to AI_INTENTION_ATTACK</li>
	 * <li>If L2PcInstance is NOT in the aggroList of the L2GuardInstance, set the L2PcInstance Intention to AI_INTENTION_INTERACT (after a distance verification) and show message</li><BR>
	 * <BR>
	 * <B><U> Example of use </U> :</B><BR>
	 * <BR>
	 * <li>Client packet : Action, AttackRequest</li><BR>
	 * <BR>
	 * @param player The L2PcInstance that start an action on the L2GuardInstance
	 */
	@Override
	public void onAction(final L2PcInstance player)
	{
		if (!canTarget(player))
		{
			return;
		}
		
		// Check if the L2PcInstance already target the L2GuardInstance
		if (getObjectId() != player.getTargetId())
		{
			if (Config.DEBUG)
			{
				LOGGER.debug(player.getObjectId() + ": Targetted guard " + getObjectId());
			}
			
			// Set the target of the L2PcInstance player
			player.setTarget(this);
			
			// Send a Server->Client packet MyTargetSelected to the L2PcInstance player
			// The color to display in the select window is White
			MyTargetSelected my = new MyTargetSelected(getObjectId(), 0);
			player.sendPacket(my);
			my = null;
			
			// Send a Server->Client packet ValidateLocation to correct the L2NpcInstance position and heading on the client
			player.sendPacket(new ValidateLocation(this));
		}
		else
		{
			// Check if the L2PcInstance is in the aggroList of the L2GuardInstance
			if (containsTarget(player))
			{
				if (Config.DEBUG)
				{
					LOGGER.debug(player.getObjectId() + ": Attacked guard " + getObjectId());
				}
				
				// Set the L2PcInstance Intention to AI_INTENTION_ATTACK
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
			}
			else
			{
				// Calculate the distance between the L2PcInstance and the L2NpcInstance
				if (!canInteract(player))
				{
					// Set the L2PcInstance Intention to AI_INTENTION_INTERACT
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
				}
				else
				{
					// Send a Server->Client packet SocialAction to the all L2PcInstance on the knownPlayer of the L2NpcInstance
					// to display a social action of the L2GuardInstance on their client
					SocialAction sa = new SocialAction(getObjectId(), Rnd.nextInt(8));
					broadcastPacket(sa);
					sa = null;
					
					// Open a chat window on client with the text of the L2GuardInstance
					showChatWindow(player, 0);
				}
			}
		}
		// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
}
