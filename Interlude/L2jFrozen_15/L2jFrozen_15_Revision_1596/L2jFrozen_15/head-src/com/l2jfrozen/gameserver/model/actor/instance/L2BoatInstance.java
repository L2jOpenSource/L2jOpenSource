package com.l2jfrozen.gameserver.model.actor.instance;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.controllers.GameTimeController;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.actor.knownlist.BoatKnownList;
import com.l2jfrozen.gameserver.model.actor.position.L2CharPosition;
import com.l2jfrozen.gameserver.network.clientpackets.Say2;
import com.l2jfrozen.gameserver.network.serverpackets.CreatureSay;
import com.l2jfrozen.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.OnVehicleCheckLocation;
import com.l2jfrozen.gameserver.network.serverpackets.PlaySound;
import com.l2jfrozen.gameserver.network.serverpackets.VehicleDeparture;
import com.l2jfrozen.gameserver.network.serverpackets.VehicleInfo;
import com.l2jfrozen.gameserver.templates.L2CharTemplate;
import com.l2jfrozen.gameserver.templates.L2Weapon;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;

/**
 * @author eX1steam, l2jfrozen
 */
public class L2BoatInstance extends L2Character
{
	protected static final Logger LOGGER = Logger.getLogger(L2BoatInstance.class);
	
	public float boatSpeed;
	
	private class L2BoatTrajet
	{
		private Map<Integer, L2BoatPoint> path;
		
		public int idWaypoint1;
		public int idWTicket1;
		public int ntx1;
		public int nty1;
		public int ntz1;
		public int max;
		public String boatName;
		public String npc1;
		public String sysmess10_1;
		public String sysmess5_1;
		public String sysmess1_1;
		public String sysmessb_1;
		public String sysmess0_1;
		
		protected class L2BoatPoint
		{
			public int speed1;
			public int speed2;
			public int x;
			public int y;
			public int z;
			public int time;
		}
		
		/**
		 * @param pIdWaypoint1
		 * @param pIdWTicket1
		 * @param pNtx1
		 * @param pNty1
		 * @param pNtz1
		 * @param pNpc1
		 * @param pSysmess10_1
		 * @param pSysmess5_1
		 * @param pSysmess1_1
		 * @param pSysmess0_1
		 * @param pSysmessb_1
		 * @param pBoatname
		 */
		public L2BoatTrajet(final int pIdWaypoint1, final int pIdWTicket1, final int pNtx1, final int pNty1, final int pNtz1, final String pNpc1, final String pSysmess10_1, final String pSysmess5_1, final String pSysmess1_1, final String pSysmess0_1, final String pSysmessb_1, final String pBoatname)
		{
			idWaypoint1 = pIdWaypoint1;
			idWTicket1 = pIdWTicket1;
			ntx1 = pNtx1;
			nty1 = pNty1;
			ntz1 = pNtz1;
			npc1 = pNpc1;
			sysmess10_1 = pSysmess10_1;
			sysmess5_1 = pSysmess5_1;
			sysmess1_1 = pSysmess1_1;
			sysmessb_1 = pSysmessb_1;
			sysmess0_1 = pSysmess0_1;
			boatName = pBoatname;
			loadBoatPath();
		}
		
		/**
		 * @param line
		 */
		public void parseLine(final String line)
		{
			// L2BoatPath bp = new L2BoatPath();
			path = new HashMap<>();
			StringTokenizer st = new StringTokenizer(line, ";");
			Integer.parseInt(st.nextToken());
			max = Integer.parseInt(st.nextToken());
			for (int i = 0; i < max; i++)
			{
				final L2BoatPoint bp = new L2BoatPoint();
				bp.speed1 = Integer.parseInt(st.nextToken());
				bp.speed2 = Integer.parseInt(st.nextToken());
				bp.x = Integer.parseInt(st.nextToken());
				bp.y = Integer.parseInt(st.nextToken());
				bp.z = Integer.parseInt(st.nextToken());
				bp.time = Integer.parseInt(st.nextToken());
				path.put(i, bp);
			}
			st = null;
			return;
		}
		
		protected void loadBoatPath()
		{
			File boatpath = new File(Config.DATAPACK_ROOT, "data/csv/boatpath.csv");
			
			try (FileReader reader = new FileReader(boatpath);
				BufferedReader buff = new BufferedReader(reader);
				LineNumberReader lnr = new LineNumberReader(buff))
			{
				boolean token = false;
				String line = null;
				
				while ((line = lnr.readLine()) != null)
				{
					if (line.trim().length() == 0 || !line.startsWith(idWaypoint1 + ";"))
					{
						continue;
					}
					
					parseLine(line);
					token = true;
					break;
				}
				
				if (!token)
				{
					LOGGER.warn("No path for boat " + boatName + " !!!");
				}
			}
			catch (FileNotFoundException e)
			{
				LOGGER.error("L2BoatInstance.loadBoathPath : boatpath.csv file is missing in gameserver/data/csv/ folder", e);
			}
			catch (Exception e)
			{
				LOGGER.error("L2BoatInstance.loadBoathPath : Error while creating boat table ", e);
			}
		}
		
		public int state(final int state, final L2BoatInstance boat)
		{
			if (state < max)
			{
				final L2BoatPoint bp = path.get(state);
				final double dx = boat.getX() - bp.x;
				final double dy = boat.getY() - bp.y;
				final double distance = Math.sqrt(dx * dx + dy * dy);
				final double cos = dx / distance;
				final double sin = dy / distance;
				
				boat.getPosition().setHeading((int) (Math.atan2(-sin, -cos) * 10430.378350470452724949566316381) + 32768);
				
				boat.vd = new VehicleDeparture(boat, bp.speed1, bp.speed2, bp.x, bp.y, bp.z);
				// boat.getTemplate().baseRunSpd = bp.speed1;
				boatSpeed = bp.speed1;
				boat.moveToLocation(bp.x, bp.y, bp.z, (float) bp.speed1);
				Collection<L2PcInstance> knownPlayers = boat.getKnownList().getKnownPlayers().values();
				if (knownPlayers == null || knownPlayers.isEmpty())
				{
					return bp.time;
				}
				for (final L2PcInstance player : knownPlayers)
				{
					player.sendPacket(boat.vd);
				}
				knownPlayers = null;
				
				if (bp.time == 0)
				{
					bp.time = 1;
				}
				
				return bp.time;
			}
			return 0;
		}
	}
	
	private final String name;
	protected L2BoatTrajet t1;
	protected L2BoatTrajet t2;
	protected int cycle = 0;
	protected VehicleDeparture vd = null;
	private Map<Integer, L2PcInstance> inboat;
	
	public L2BoatInstance(final int objectId, final L2CharTemplate template, final String name)
	{
		super(objectId, template);
		super.setKnownList(new BoatKnownList(this));
		/*
		 * super.setStat(new DoorStat(new L2DoorInstance[] {this})); super.setStatus(new DoorStatus(new L2DoorInstance[] {this}));
		 */
		this.name = name;
	}
	
	/**
	 * @param x
	 * @param y
	 * @param z
	 * @param speed
	 */
	public void moveToLocation(final int x, final int y, final int z, final float speed)
	{
		final int curX = getX();
		final int curY = getY();
		// final int curZ = getZ();
		
		// Calculate distance (dx,dy) between current position and destination
		final int dx = x - curX;
		final int dy = y - curY;
		final double distance = Math.sqrt(dx * dx + dy * dy);
		
		/*
		 * if(Config.DEBUG) { logBoat.fine("distance to target:" + distance); }
		 */
		
		// Define movement angles needed
		// ^
		// | X (x,y)
		// | /
		// | /distance
		// | /
		// |/ angle
		// X ---------->
		// (curx,cury)
		
		final double cos = dx / distance;
		final double sin = dy / distance;
		// Create and Init a MoveData object
		MoveData m = new MoveData();
		
		// Caclulate the Nb of ticks between the current position and the destination
		// int ticksToMove = (int) (GameTimeController.TICKS_PER_SECOND * distance / speed);
		
		// Calculate and set the heading of the L2Character
		getPosition().setHeading((int) (Math.atan2(-sin, -cos) * 10430.378350470452724949566316381) + 32768);
		
		/*
		 * if(Config.DEBUG) { logBoat.fine("dist:" + distance + "speed:" + speed + " ttt:" + ticksToMove + " heading:" + getPosition().getHeading()); }
		 */
		
		m.xDestination = x;
		m.yDestination = y;
		m.zDestination = z; // this is what was requested from client
		m.heading = 0;
		m.onGeodataPathIndex = -1; // Initialize not on geodata path
		m.moveStartTime = GameTimeController.getGameTicks();
		
		/*
		 * if(Config.DEBUG) { logBoat.fine("time to target:" + ticksToMove); }
		 */
		
		// Set the L2Character move object to MoveData object
		playerMove = m;
		
		// Add the L2Character to movingObjects of the GameTimeController
		// The GameTimeController manage objects movement
		GameTimeController.getInstance().registerMovingObject(this);
		m = null;
	}
	
	class BoatCaptain implements Runnable
	{
		private final int state;
		private final L2BoatInstance boat;
		
		/**
		 * @param i
		 * @param instance
		 */
		public BoatCaptain(final int i, final L2BoatInstance instance)
		{
			state = i;
			boat = instance;
		}
		
		@Override
		public void run()
		{
			BoatCaptain bc;
			switch (state)
			{
				case 1:
					boat.say(5);
					bc = new BoatCaptain(2, boat);
					ThreadPoolManager.getInstance().scheduleGeneral(bc, 240000);
					break;
				case 2:
					boat.say(1);
					bc = new BoatCaptain(3, boat);
					ThreadPoolManager.getInstance().scheduleGeneral(bc, 40000);
					break;
				case 3:
					boat.say(0);
					bc = new BoatCaptain(4, boat);
					ThreadPoolManager.getInstance().scheduleGeneral(bc, 20000);
					break;
				case 4:
					boat.say(-1);
					boat.begin();
					break;
			}
		}
	}
	
	class Boatrun implements Runnable
	{
		private int state;
		private final L2BoatInstance boat;
		
		/**
		 * @param i
		 * @param instance
		 */
		public Boatrun(final int i, final L2BoatInstance instance)
		{
			state = i;
			boat = instance;
		}
		
		@Override
		public void run()
		{
			if (!inCycle)
			{
				return;
			}
			
			boat.vd = null;
			boat.needOnVehicleCheckLocation = false;
			
			if (boat.cycle == 1)
			{
				final int time = boat.t1.state(state, boat);
				if (time > 0)
				{
					state++;
					Boatrun bc = new Boatrun(state, boat);
					ThreadPoolManager.getInstance().scheduleGeneral(bc, time);
					bc = null;
				}
				else if (time == 0)
				{
					boat.cycle = 2;
					boat.say(10);
					BoatCaptain bc = new BoatCaptain(1, boat);
					ThreadPoolManager.getInstance().scheduleGeneral(bc, 300000);
					bc = null;
				}
				else
				{
					boat.needOnVehicleCheckLocation = true;
					state++;
					boat.runstate = state;
				}
			}
			else if (boat.cycle == 2)
			{
				final int time = boat.t2.state(state, boat);
				if (time > 0)
				{
					state++;
					Boatrun bc = new Boatrun(state, boat);
					ThreadPoolManager.getInstance().scheduleGeneral(bc, time);
					bc = null;
				}
				else if (time == 0)
				{
					boat.cycle = 1;
					boat.say(10);
					BoatCaptain bc = new BoatCaptain(1, boat);
					ThreadPoolManager.getInstance().scheduleGeneral(bc, 300000);
					bc = null;
				}
				else
				{
					boat.needOnVehicleCheckLocation = true;
					state++;
					boat.runstate = state;
				}
			}
		}
	}
	
	public int runstate = 0;
	
	/**
	 *
	 */
	public void evtArrived()
	{
		
		if (runstate != 0)
		{
			// runstate++;
			Boatrun bc = new Boatrun(runstate, this);
			ThreadPoolManager.getInstance().scheduleGeneral(bc, 10);
			runstate = 0;
			bc = null;
		}
	}
	
	/**
	 * @param activeChar
	 */
	public void sendVehicleDeparture(final L2PcInstance activeChar)
	{
		if (vd != null)
		{
			activeChar.sendPacket(vd);
		}
	}
	
	public VehicleDeparture getVehicleDeparture()
	{
		return vd;
	}
	
	public void beginCycle()
	{
		say(10);
		BoatCaptain bc = new BoatCaptain(1, this);
		ThreadPoolManager.getInstance().scheduleGeneral(bc, 300000);
		bc = null;
	}
	
	private int lastx = -1;
	private int lasty = -1;
	protected boolean needOnVehicleCheckLocation = false;
	protected boolean inCycle = true;
	private int id;
	
	public void updatePeopleInTheBoat(final int x, final int y, final int z)
	{
		
		if (inboat != null)
		{
			boolean check = false;
			if (lastx == -1 || lasty == -1)
			{
				check = true;
				lastx = x;
				lasty = y;
			}
			else if ((x - lastx) * (x - lastx) + (y - lasty) * (y - lasty) > 2250000) // 1500 * 1500 = 2250000
			{
				check = true;
				lastx = x;
				lasty = y;
			}
			for (int i = 0; i < inboat.size(); i++)
			{
				final L2PcInstance player = inboat.get(i);
				if (player != null && player.isInBoat())
				{
					if (player.getBoat() == this)
					{
						// player.getKnownList().addKnownObject(this);
						player.getPosition().setXYZ(x, y, z);
						player.revalidateZone(false);
					}
				}
				
				if (check && needOnVehicleCheckLocation && (player != null))
				{
					player.sendPacket(new OnVehicleCheckLocation(this, x, y, z));
				}
			}
		}
		
	}
	
	public void begin()
	{
		if (!inCycle)
		{
			return;
		}
		
		if (cycle == 1)
		{
			Collection<L2PcInstance> knownPlayers = getKnownList().getKnownPlayers().values();
			if (knownPlayers != null && !knownPlayers.isEmpty())
			{
				inboat = new HashMap<>();
				int i = 0;
				for (final L2PcInstance player : knownPlayers)
				{
					if (player.isInBoat() && player.getBoat() == this)
					{
						L2ItemInstance it;
						it = player.getInventory().getItemByItemId(t1.idWTicket1);
						if (it != null && it.getCount() >= 1)
						{
							player.getInventory().destroyItem("Boat", it.getObjectId(), 1, player, this);
							final InventoryUpdate iu = new InventoryUpdate();
							iu.addModifiedItem(it);
							player.sendPacket(iu);
							inboat.put(i, player);
							i++;
						}
						else if (it == null && t1.idWTicket1 == 0)
						{
							inboat.put(i, player);
							i++;
						}
						else
						{
							player.teleToLocation(t1.ntx1, t1.nty1, t1.ntz1, false);
						}
					}
				}
				knownPlayers = null;
			}
			Boatrun bc = new Boatrun(0, this);
			ThreadPoolManager.getInstance().scheduleGeneral(bc, 0);
			bc = null;
		}
		else if (cycle == 2)
		{
			Collection<L2PcInstance> knownPlayers = getKnownList().getKnownPlayers().values();
			if (knownPlayers != null && !knownPlayers.isEmpty())
			{
				inboat = new HashMap<>();
				int i = 0;
				for (final L2PcInstance player : knownPlayers)
				{
					if (player.isInBoat() && player.getBoat() == this)
					{
						L2ItemInstance it;
						it = player.getInventory().getItemByItemId(t2.idWTicket1);
						if (it != null && it.getCount() >= 1)
						{
							player.getInventory().destroyItem("Boat", it.getObjectId(), 1, player, this);
							final InventoryUpdate iu = new InventoryUpdate();
							iu.addModifiedItem(it);
							player.sendPacket(iu);
							inboat.put(i, player);
							i++;
						}
						else if (it == null && t2.idWTicket1 == 0)
						{
							inboat.put(i, player);
							i++;
						}
						else
						{
							player.teleToLocation(t2.ntx1, t2.nty1, t2.ntz1, false);
						}
					}
				}
				knownPlayers = null;
			}
			Boatrun bc = new Boatrun(0, this);
			ThreadPoolManager.getInstance().scheduleGeneral(bc, 0);
			bc = null;
		}
	}
	
	/**
	 * @param i
	 */
	public void say(final int i)
	{
		
		Collection<L2PcInstance> knownPlayers = getKnownList().getKnownPlayers().values();
		CreatureSay sm;
		PlaySound ps;
		switch (i)
		{
			case 10:
				if (cycle == 1)
				{
					sm = new CreatureSay(0, Say2.SHOUT, t1.npc1, t1.sysmess10_1);
				}
				else
				{
					sm = new CreatureSay(0, Say2.SHOUT, t2.npc1, t2.sysmess10_1);
				}
				ps = new PlaySound(0, "itemsound.ship_arrival_departure", 1, getObjectId(), getX(), getY(), getZ());
				if (knownPlayers == null || knownPlayers.isEmpty())
				{
					return;
				}
				for (final L2PcInstance player : knownPlayers)
				{
					player.sendPacket(sm);
					player.sendPacket(ps);
				}
				break;
			case 5:
				if (cycle == 1)
				{
					sm = new CreatureSay(0, Say2.SHOUT, t1.npc1, t1.sysmess5_1);
				}
				else
				{
					sm = new CreatureSay(0, Say2.SHOUT, t2.npc1, t2.sysmess5_1);
				}
				ps = new PlaySound(0, "itemsound.ship_5min", 1, getObjectId(), getX(), getY(), getZ());
				if (knownPlayers == null || knownPlayers.isEmpty())
				{
					return;
				}
				for (final L2PcInstance player : knownPlayers)
				{
					player.sendPacket(sm);
					player.sendPacket(ps);
				}
				break;
			case 1:
				
				if (cycle == 1)
				{
					sm = new CreatureSay(0, Say2.SHOUT, t1.npc1, t1.sysmess1_1);
				}
				else
				{
					sm = new CreatureSay(0, Say2.SHOUT, t2.npc1, t2.sysmess1_1);
				}
				ps = new PlaySound(0, "itemsound.ship_1min", 1, getObjectId(), getX(), getY(), getZ());
				if (knownPlayers == null || knownPlayers.isEmpty())
				{
					return;
				}
				for (final L2PcInstance player : knownPlayers)
				{
					player.sendPacket(sm);
					player.sendPacket(ps);
				}
				break;
			case 0:
				
				if (cycle == 1)
				{
					sm = new CreatureSay(0, Say2.SHOUT, t1.npc1, t1.sysmess0_1);
				}
				else
				{
					sm = new CreatureSay(0, Say2.SHOUT, t2.npc1, t2.sysmess0_1);
				}
				if (knownPlayers == null || knownPlayers.isEmpty())
				{
					return;
				}
				for (final L2PcInstance player : knownPlayers)
				{
					player.sendPacket(sm);
					// player.sendPacket(ps);
				}
				break;
			case -1:
				if (cycle == 1)
				{
					sm = new CreatureSay(0, Say2.SHOUT, t1.npc1, t1.sysmessb_1);
				}
				else
				{
					sm = new CreatureSay(0, Say2.SHOUT, t2.npc1, t2.sysmessb_1);
				}
				ps = new PlaySound(0, "itemsound.ship_arrival_departure", 1, getObjectId(), getX(), getY(), getZ());
				for (final L2PcInstance player : knownPlayers)
				{
					player.sendPacket(sm);
					player.sendPacket(ps);
				}
				break;
		}
		knownPlayers = null;
		sm = null;
		ps = null;
	}
	
	//
	/**
	 *
	 */
	public void spawn()
	{
		Collection<L2PcInstance> knownPlayers = getKnownList().getKnownPlayers().values();
		cycle = 1;
		beginCycle();
		if (knownPlayers == null || knownPlayers.isEmpty())
		{
			return;
		}
		final VehicleInfo vi = new VehicleInfo(this);
		for (final L2PcInstance player : knownPlayers)
		{
			player.sendPacket(vi);
		}
		knownPlayers = null;
	}
	
	/**
	 * @param idWaypoint1
	 * @param idWTicket1
	 * @param ntx1
	 * @param nty1
	 * @param ntz1
	 * @param idnpc1
	 * @param sysmess10_1
	 * @param sysmess5_1
	 * @param sysmess1_1
	 * @param sysmess0_1
	 * @param sysmessb_1
	 */
	public void setTrajet1(final int idWaypoint1, final int idWTicket1, final int ntx1, final int nty1, final int ntz1, final String idnpc1, final String sysmess10_1, final String sysmess5_1, final String sysmess1_1, final String sysmess0_1, final String sysmessb_1)
	{
		t1 = new L2BoatTrajet(idWaypoint1, idWTicket1, ntx1, nty1, ntz1, idnpc1, sysmess10_1, sysmess5_1, sysmess1_1, sysmess0_1, sysmessb_1, name);
	}
	
	public void setTrajet2(final int idWaypoint1, final int idWTicket1, final int ntx1, final int nty1, final int ntz1, final String idnpc1, final String sysmess10_1, final String sysmess5_1, final String sysmess1_1, final String sysmess0_1, final String sysmessb_1)
	{
		t2 = new L2BoatTrajet(idWaypoint1, idWTicket1, ntx1, nty1, ntz1, idnpc1, sysmess10_1, sysmess5_1, sysmess1_1, sysmess0_1, sysmessb_1, name);
	}
	
	@Override
	public void updateAbnormalEffect()
	{
	}
	
	@Override
	public L2ItemInstance getActiveWeaponInstance()
	{
		return null;
	}
	
	@Override
	public L2Weapon getActiveWeaponItem()
	{
		return null;
	}
	
	@Override
	public L2ItemInstance getSecondaryWeaponInstance()
	{
		return null;
	}
	
	@Override
	public L2Weapon getSecondaryWeaponItem()
	{
		return null;
	}
	
	@Override
	public int getLevel()
	{
		return 0;
	}
	
	@Override
	public boolean isAutoAttackable(final L2Character attacker)
	{
		return false;
	}
	
	public boolean isInCycle()
	{
		return inCycle;
	}
	
	public void stopCycle()
	{
		inCycle = false;
		stopMove(new L2CharPosition(getX(), getY(), getZ(), getPosition().getHeading()));
	}
	
	public void startCycle()
	{
		inCycle = true;
		cycle = 1;
		beginCycle();
	}
	
	public void reloadPath()
	{
		t1.loadBoatPath();
		t2.loadBoatPath();
		cycle = 0;
		stopCycle();
		startCycle();
	}
	
	public String getBoatName()
	{
		return name;
	}
	
	public int getSizeInside()
	{
		return inboat == null ? 0 : inboat.size();
	}
	
	public int getCycle()
	{
		return cycle;
	}
	
	/**
	 * @return
	 */
	public int getId()
	{
		return id;
	}
	
	/**
	 * @param id
	 */
	public void setId(final int id)
	{
		this.id = id;
	}
}
