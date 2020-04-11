package com.l2jfrozen.gameserver.managers;

import java.awt.Polygon;
import java.awt.Shape;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.sql.NpcTable;
import com.l2jfrozen.gameserver.datatables.sql.SpawnTable;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.entity.DimensionalRift;
import com.l2jfrozen.gameserver.model.spawn.L2Spawn;
import com.l2jfrozen.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;
import com.l2jfrozen.gameserver.util.Util;
import com.l2jfrozen.util.database.L2DatabaseFactory;
import com.l2jfrozen.util.random.Rnd;

/**
 * Thanks to L2Fortress and balancer.ru - kombat
 */
public class DimensionalRiftManager
{
	
	protected static final Logger LOGGER = Logger.getLogger(DimensionalRiftManager.class);
	private static final String SELECT_DIMENSIONAL_RIFT_ROOMS = "SELECT type,room_id,xMin,xMax,yMin,yMax,zMin,zMax,xT,yT,zT,boss FROM dimensional_rift";
	
	private static DimensionalRiftManager instance;
	private final Map<Byte, Map<Byte, DimensionalRiftRoom>> rooms = new HashMap<>();
	private final short DIMENSIONAL_FRAGMENT_ITEM_ID = 7079;
	private final static int MAX_PARTY_PER_AREA = 3;
	
	public static DimensionalRiftManager getInstance()
	{
		if (instance == null)
		{
			instance = new DimensionalRiftManager();
		}
		
		return instance;
	}
	
	private DimensionalRiftManager()
	{
		loadRooms();
		loadSpawns();
	}
	
	public DimensionalRiftRoom getRoom(final byte type, final byte room)
	{
		return rooms.get(type) == null ? null : rooms.get(type).get(room);
	}
	
	public boolean isAreaAvailable(final byte area)
	{
		final Map<Byte, DimensionalRiftRoom> tmap = rooms.get(area);
		if (tmap == null)
		{
			return false;
		}
		int used = 0;
		for (final DimensionalRiftRoom room : tmap.values())
		{
			if (room.isUsed())
			{
				used++;
			}
		}
		return used <= MAX_PARTY_PER_AREA;
	}
	
	public boolean isRoomAvailable(final byte area, final byte room)
	{
		if (rooms.get(area) == null || rooms.get(area).get(room) == null)
		{
			return false;
		}
		return !rooms.get(area).get(room).isUsed();
	}
	
	private void loadRooms()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement s = con.prepareStatement(SELECT_DIMENSIONAL_RIFT_ROOMS);
			ResultSet rs = s.executeQuery();)
		{
			while (rs.next())
			{
				// 0 waiting room, 1 recruit, 2 soldier, 3 officer, 4 captain , 5 commander, 6 hero
				byte type = rs.getByte("type");
				byte room_id = rs.getByte("room_id");
				
				// coords related
				int xMin = rs.getInt("xMin");
				int xMax = rs.getInt("xMax");
				int yMin = rs.getInt("yMin");
				int yMax = rs.getInt("yMax");
				int z1 = rs.getInt("zMin");
				int z2 = rs.getInt("zMax");
				int xT = rs.getInt("xT");
				int yT = rs.getInt("yT");
				int zT = rs.getInt("zT");
				boolean isBossRoom = rs.getByte("boss") > 0;
				
				if (!rooms.containsKey(type))
				{
					rooms.put(type, new HashMap<Byte, DimensionalRiftRoom>());
				}
				
				rooms.get(type).put(room_id, new DimensionalRiftRoom(type, room_id, xMin, xMax, yMin, yMax, z1, z2, xT, yT, zT, isBossRoom));
			}
		}
		catch (Exception e)
		{
			LOGGER.error("DimensionalRiftManager.loadRooms : Can't load Dimension Rift zones", e);
		}
		
		int typeSize = rooms.keySet().size();
		int roomSize = 0;
		
		for (final Byte b : rooms.keySet())
		{
			roomSize += rooms.get(b).keySet().size();
		}
		
		LOGGER.info("DimensionalRiftManager: Loaded " + typeSize + " room types with " + roomSize + " rooms.");
	}
	
	public void loadSpawns()
	{
		int countGood = 0;
		try
		{
			final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);
			
			final File file = new File(Config.DATAPACK_ROOT + "/data/xml/dimensionalRift.xml");
			if (!file.exists())
			{
				throw new IOException();
			}
			
			final Document doc = factory.newDocumentBuilder().parse(file);
			
			NamedNodeMap attrs;
			byte type, roomId;
			int mobId, x, y, z, delay, count;
			L2Spawn spawnDat;
			L2NpcTemplate template;
			
			for (Node rift = doc.getFirstChild(); rift != null; rift = rift.getNextSibling())
			{
				if ("rift".equalsIgnoreCase(rift.getNodeName()))
				{
					for (Node area = rift.getFirstChild(); area != null; area = area.getNextSibling())
					{
						if ("area".equalsIgnoreCase(area.getNodeName()))
						{
							attrs = area.getAttributes();
							type = Byte.parseByte(attrs.getNamedItem("type").getNodeValue());
							
							for (Node room = area.getFirstChild(); room != null; room = room.getNextSibling())
							{
								if ("room".equalsIgnoreCase(room.getNodeName()))
								{
									attrs = room.getAttributes();
									roomId = Byte.parseByte(attrs.getNamedItem("id").getNodeValue());
									
									for (Node spawn = room.getFirstChild(); spawn != null; spawn = spawn.getNextSibling())
									{
										if ("spawn".equalsIgnoreCase(spawn.getNodeName()))
										{
											attrs = spawn.getAttributes();
											mobId = Integer.parseInt(attrs.getNamedItem("mobId").getNodeValue());
											delay = Integer.parseInt(attrs.getNamedItem("delay").getNodeValue());
											count = Integer.parseInt(attrs.getNamedItem("count").getNodeValue());
											
											template = NpcTable.getInstance().getTemplate(mobId);
											if (template == null)
											{
												LOGGER.warn("Template " + mobId + " not found!");
											}
											if (!rooms.containsKey(type))
											{
												LOGGER.warn("Type " + type + " not found!");
											}
											else if (!rooms.get(type).containsKey(roomId))
											{
												LOGGER.warn("Room " + roomId + " in Type " + type + " not found!");
											}
											
											for (int i = 0; i < count; i++)
											{
												DimensionalRiftRoom riftRoom = rooms.get(type).get(roomId);
												x = riftRoom.getRandomX();
												y = riftRoom.getRandomY();
												z = riftRoom.getTeleportCoords()[2];
												riftRoom = null;
												
												if (template != null && rooms.containsKey(type) && rooms.get(type).containsKey(roomId))
												{
													spawnDat = new L2Spawn(template);
													spawnDat.setAmount(1);
													spawnDat.setLocx(x);
													spawnDat.setLocy(y);
													spawnDat.setLocz(z);
													spawnDat.setHeading(-1);
													spawnDat.setRespawnDelay(delay);
													SpawnTable.getInstance().addNewSpawn(spawnDat, false);
													rooms.get(type).get(roomId).getSpawns().add(spawnDat);
													countGood++;
												}
											}
										}
									}
								}
							}
							attrs = null;
						}
					}
				}
			}
			spawnDat = null;
			template = null;
		}
		catch (Exception e)
		{
			LOGGER.error("DimensionalRiftManager.loadSpawns : Error on loading dimensional rift spawns: " + e);
		}
		LOGGER.info("DimensionalRiftManager: Loaded " + countGood + " dimensional rift spawns.");
	}
	
	public void reload()
	{
		for (final Byte b : rooms.keySet())
		{
			for (final byte i : rooms.get(b).keySet())
			{
				rooms.get(b).get(i).getSpawns().clear();
			}
			rooms.get(b).clear();
		}
		rooms.clear();
		loadRooms();
		loadSpawns();
	}
	
	public boolean checkIfInRiftZone(final int x, final int y, final int z, final boolean ignorePeaceZone)
	{
		if (ignorePeaceZone)
		{
			return rooms.get((byte) 0).get((byte) 1).checkIfInZone(x, y, z);
		}
		return rooms.get((byte) 0).get((byte) 1).checkIfInZone(x, y, z) && !rooms.get((byte) 0).get((byte) 0).checkIfInZone(x, y, z);
	}
	
	public boolean checkIfInPeaceZone(final int x, final int y, final int z)
	{
		return rooms.get((byte) 0).get((byte) 0).checkIfInZone(x, y, z);
	}
	
	public void teleportToWaitingRoom(final L2PcInstance player)
	{
		final int[] coords = getRoom((byte) 0, (byte) 0).getTeleportCoords();
		player.teleToLocation(coords[0], coords[1], coords[2]);
	}
	
	public void start(final L2PcInstance player, final byte type, final L2NpcInstance npc)
	{
		boolean canPass = true;
		if (!player.isInParty())
		{
			showHtmlFile(player, "data/html/seven_signs/rift/NoParty.htm", npc);
			return;
		}
		
		if (player.getParty().getPartyLeaderOID() != player.getObjectId())
		{
			showHtmlFile(player, "data/html/seven_signs/rift/NotPartyLeader.htm", npc);
			return;
		}
		
		if (player.getParty().isInDimensionalRift())
		{
			handleCheat(player, npc);
			return;
		}
		
		if (!isAreaAvailable(type))
		{
			player.sendMessage("This rift area is full. Try later.");
			return;
		}
		
		if (player.getParty().getMemberCount() < Config.RIFT_MIN_PARTY_SIZE)
		{
			final NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
			html.setFile("data/html/seven_signs/rift/SmallParty.htm");
			html.replace("%npc_name%", npc.getName());
			html.replace("%count%", String.valueOf(Config.RIFT_MIN_PARTY_SIZE));
			player.sendPacket(html);
			return;
		}
		
		for (final L2PcInstance p : player.getParty().getPartyMembers())
		{
			if (!checkIfInPeaceZone(p.getX(), p.getY(), p.getZ()))
			{
				canPass = false;
			}
		}
		
		if (!canPass)
		{
			showHtmlFile(player, "data/html/seven_signs/rift/NotInWaitingRoom.htm", npc);
			return;
		}
		
		L2ItemInstance i;
		for (final L2PcInstance p : player.getParty().getPartyMembers())
		{
			i = p.getInventory().getItemByItemId(DIMENSIONAL_FRAGMENT_ITEM_ID);
			
			if (i == null)
			{
				canPass = false;
				break;
			}
			
			if (i.getCount() > 0)
			{
				if (i.getCount() < getNeededItems(type))
				{
					canPass = false;
				}
			}
		}
		
		if (!canPass)
		{
			NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
			html.setFile("data/html/seven_signs/rift/NoFragments.htm");
			html.replace("%npc_name%", npc.getName());
			html.replace("%count%", String.valueOf(getNeededItems(type)));
			player.sendPacket(html);
			html = null;
			return;
		}
		
		for (final L2PcInstance p : player.getParty().getPartyMembers())
		{
			i = p.getInventory().getItemByItemId(DIMENSIONAL_FRAGMENT_ITEM_ID);
			p.destroyItem("RiftEntrance", i.getObjectId(), getNeededItems(type), null, false);
		}
		
		i = null;
		
		byte room;
		do
		{
			room = (byte) Rnd.get(1, 9);
		}
		while (!isRoomAvailable(type, room));
		
		new DimensionalRift(player.getParty(), type, room);
	}
	
	public void killRift(final DimensionalRift d)
	{
		if (d.getTeleportTimerTask() != null)
		{
			d.getTeleportTimerTask().cancel();
		}
		d.setTeleportTimerTask(null);
		
		if (d.getTeleportTimer() != null)
		{
			d.getTeleportTimer().cancel();
		}
		d.setTeleportTimer(null);
		
		if (d.getSpawnTimerTask() != null)
		{
			d.getSpawnTimerTask().cancel();
		}
		d.setSpawnTimerTask(null);
		
		if (d.getSpawnTimer() != null)
		{
			d.getSpawnTimer().cancel();
		}
		d.setSpawnTimer(null);
	}
	
	public class DimensionalRiftRoom
	{
		protected final byte type;
		protected final byte room;
		private final int xMin;
		private final int xMax;
		private final int yMin;
		private final int yMax;
		private final int zMin;
		private final int zMax;
		private final int[] teleportCoords;
		private final Shape s;
		private final boolean isBossRoom;
		private final List<L2Spawn> roomSpawns;
		protected final List<L2NpcInstance> roomMobs;
		private boolean isUsed = false;
		
		public DimensionalRiftRoom(final byte type, final byte room, final int xMin, final int xMax, final int yMin, final int yMax, final int zMin, final int zMax, final int xT, final int yT, final int zT, final boolean isBossRoom)
		{
			this.type = type;
			this.room = room;
			this.xMin = xMin + 128;
			this.xMax = xMax - 128;
			this.yMin = yMin + 128;
			this.yMax = yMax - 128;
			this.zMin = zMin;
			this.zMax = zMax;
			teleportCoords = new int[]
			{
				xT,
				yT,
				zT
			};
			this.isBossRoom = isBossRoom;
			roomSpawns = new ArrayList<>();
			roomMobs = new ArrayList<>();
			s = new Polygon(new int[]
			{
				xMin,
				xMax,
				xMax,
				xMin
			}, new int[]
			{
				yMin,
				yMin,
				yMax,
				yMax
			}, 4);
		}
		
		public int getRandomX()
		{
			return Rnd.get(xMin, xMax);
		}
		
		public int getRandomY()
		{
			return Rnd.get(yMin, yMax);
		}
		
		public int[] getTeleportCoords()
		{
			return teleportCoords;
		}
		
		public boolean checkIfInZone(final int x, final int y, final int z)
		{
			return s.contains(x, y) && z >= zMin && z <= zMax;
		}
		
		public boolean isBossRoom()
		{
			return isBossRoom;
		}
		
		public List<L2Spawn> getSpawns()
		{
			return roomSpawns;
		}
		
		public void spawn()
		{
			for (final L2Spawn spawn : roomSpawns)
			{
				spawn.doSpawn();
				if (spawn.getNpcid() < 25333 && spawn.getNpcid() > 25338)
				{
					spawn.startRespawn();
				}
			}
		}
		
		public void unspawn()
		{
			for (final L2Spawn spawn : roomSpawns)
			{
				spawn.stopRespawn();
				if (spawn.getLastSpawn() != null)
				{
					spawn.getLastSpawn().deleteMe();
				}
			}
			isUsed = false;
		}
		
		public void setUsed()
		{
			isUsed = true;
		}
		
		public boolean isUsed()
		{
			return isUsed;
		}
	}
	
	private int getNeededItems(final byte type)
	{
		switch (type)
		{
			case 1:
				return Config.RIFT_ENTER_COST_RECRUIT;
			case 2:
				return Config.RIFT_ENTER_COST_SOLDIER;
			case 3:
				return Config.RIFT_ENTER_COST_OFFICER;
			case 4:
				return Config.RIFT_ENTER_COST_CAPTAIN;
			case 5:
				return Config.RIFT_ENTER_COST_COMMANDER;
			case 6:
				return Config.RIFT_ENTER_COST_HERO;
			default:
				return 999999;
		}
	}
	
	public void showHtmlFile(final L2PcInstance player, final String file, final L2NpcInstance npc)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
		html.setFile(file);
		html.replace("%npc_name%", npc.getName());
		player.sendPacket(html);
		html = null;
	}
	
	public void handleCheat(final L2PcInstance player, final L2NpcInstance npc)
	{
		showHtmlFile(player, "data/html/seven_signs/rift/Cheater.htm", npc);
		if (!player.isGM())
		{
			LOGGER.warn("Player " + player.getName() + "(" + player.getObjectId() + ") was cheating in dimension rift area!");
			Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " tried to cheat in dimensional rift.", Config.DEFAULT_PUNISH);
		}
	}
}
