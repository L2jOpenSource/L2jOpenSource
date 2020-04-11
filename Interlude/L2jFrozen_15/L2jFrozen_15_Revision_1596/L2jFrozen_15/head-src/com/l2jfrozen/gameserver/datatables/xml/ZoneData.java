package com.l2jfrozen.gameserver.datatables.xml;

import java.io.File;
import java.lang.reflect.Constructor;
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
import com.l2jfrozen.gameserver.managers.FishingZoneManager;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.L2WorldRegion;
import com.l2jfrozen.gameserver.model.zone.L2ZoneType;
import com.l2jfrozen.gameserver.model.zone.shape.ZoneCuboid;
import com.l2jfrozen.gameserver.model.zone.shape.ZoneCylinder;
import com.l2jfrozen.gameserver.model.zone.shape.ZoneNPoly;
import com.l2jfrozen.gameserver.model.zone.type.L2FishingZone;
import com.l2jfrozen.gameserver.model.zone.type.L2WaterZone;
import com.l2jfrozen.util.database.DatabaseUtils;
import com.l2jfrozen.util.database.L2DatabaseFactory;

/**
 * @author programmos
 * @author scoria dev
 * @author ReynalDev
 */
public class ZoneData
{
	private static final Logger LOGGER = Logger.getLogger(ZoneData.class);
	private static final String SELECT_X_Y_ZONE_VERTICES_ID = "SELECT x,y FROM zone_vertices WHERE id=? ORDER BY 'order' ASC";
	private static Map<Integer, L2ZoneType> zones = new HashMap<>();
	
	private static ZoneData instance;
	
	public static final ZoneData getInstance()
	{
		if (instance == null)
		{
			instance = new ZoneData();
		}
		
		return instance;
	}
	
	public ZoneData()
	{
		load();
	}
	
	public void reload()
	{
		synchronized (instance)
		{
			instance = null;
			instance = new ZoneData();
		}
	}
	
	private void load()
	{
		// Get the world regions
		final L2WorldRegion[][] worldRegions = L2World.getInstance().getAllWorldRegions();
		
		// Load the zone xml
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);
			
			File file = new File(Config.DATAPACK_ROOT + "/data/xml/zone.xml");
			if (!file.exists())
			{
				if (Config.DEBUG)
				{
					LOGGER.info("The zone.xml file is missing.");
				}
			}
			else
			{
				
				Document doc = factory.newDocumentBuilder().parse(file);
				factory = null;
				file = null;
				
				for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
				{
					if ("list".equalsIgnoreCase(n.getNodeName()))
					{
						for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
						{
							if ("zone".equalsIgnoreCase(d.getNodeName()))
							{
								NamedNodeMap attrs = d.getAttributes();
								
								int zoneId = 0;
								
								zoneId = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
								
								String zoneName = "No zone name";
								if (attrs.getNamedItem("name") != null)
								{
									zoneName = attrs.getNamedItem("name").getNodeValue();
								}
								
								int minZ = Integer.parseInt(attrs.getNamedItem("minZ").getNodeValue());
								int maxZ = Integer.parseInt(attrs.getNamedItem("maxZ").getNodeValue());
								
								String zoneType = attrs.getNamedItem("type").getNodeValue();
								String zoneShape = attrs.getNamedItem("shape").getNodeValue();
								
								// Create the zone
								Class<?> newZone = null;
								Constructor<?> zoneConstructor = null;
								L2ZoneType temp = null;
								
								try
								{
									newZone = Class.forName("com.l2jfrozen.gameserver.model.zone.type.L2" + zoneType);
									zoneConstructor = newZone.getConstructor(int.class);
									temp = (L2ZoneType) zoneConstructor.newInstance(zoneId);
								}
								catch (Exception e)
								{
									LOGGER.error("ZoneData.load: No such zone type: " + zoneType + " for zone with ID: " + zoneId);
									continue;
								}
								
								temp.setZoneName(zoneName);
								
								zoneType = null;
								
								// get the zone shape from file if any
								
								int[][] coords = null;
								int[] point;
								List<int[]> rs = new ArrayList<>();
								
								// loading from XML first
								for (Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling())
								{
									if ("node".equalsIgnoreCase(cd.getNodeName()))
									{
										attrs = cd.getAttributes();
										point = new int[2];
										point[0] = Integer.parseInt(attrs.getNamedItem("X").getNodeValue());
										point[1] = Integer.parseInt(attrs.getNamedItem("Y").getNodeValue());
										rs.add(point);
									}
								}
								
								coords = rs.toArray(new int[rs.size()][]);
								
								if (coords == null || coords.length == 0) // check on database
								{
									// Get the zone shape from sql or from file if not defined into sql
									try
									{
										// Set the correct query
										PreparedStatement statement = con.prepareStatement(SELECT_X_Y_ZONE_VERTICES_ID);
										
										statement.setInt(1, zoneId);
										ResultSet rset = statement.executeQuery();
										
										// Create this zone. Parsing for cuboids is a bit different than for other polygons
										// cuboids need exactly 2 points to be defined. Other polygons need at least 3 (one per vertex)
										switch (zoneShape)
										{
											case "Cuboid":
												final int[] x =
												{
													0,
													0
												};
												final int[] y =
												{
													0,
													0
												};
												boolean successfulLoad = true;
												
												for (int i = 0; i < 2; i++)
												{
													if (rset.next())
													{
														x[i] = rset.getInt("x");
														y[i] = rset.getInt("y");
													}
													else
													{
														LOGGER.warn("ZoneData: Missing cuboid vertex in sql data for zone: " + zoneId);
														DatabaseUtils.close(statement);
														DatabaseUtils.close(rset);
														successfulLoad = false;
														break;
													}
												}
												
												if (successfulLoad)
												{
													temp.setZoneShape(new ZoneCuboid(x[0], x[1], y[0], y[1], minZ, maxZ));
												}
												else
												{
													continue;
												}
												break;
											case "NPoly":
												List<Integer> fl_x = new ArrayList<>();
												List<Integer> fl_y = new ArrayList<>();
												
												// Load the rest
												while (rset.next())
												{
													fl_x.add(rset.getInt("x"));
													fl_y.add(rset.getInt("y"));
												}
												
												// An nPoly needs to have at least 3 vertices
												if (fl_x.size() == fl_y.size() && fl_x.size() > 2)
												{
													// Create arrays
													final int[] aX = new int[fl_x.size()];
													final int[] aY = new int[fl_y.size()];
													
													// This runs only at server startup so dont complain :>
													for (int i = 0; i < fl_x.size(); i++)
													{
														aX[i] = fl_x.get(i);
														aY[i] = fl_y.get(i);
													}
													
													// Create the zone
													temp.setZoneShape(new ZoneNPoly(aX, aY, minZ, maxZ));
												}
												else
												{
													LOGGER.warn("ZoneData: Bad sql data for zone: " + zoneId);
													DatabaseUtils.close(statement);
													DatabaseUtils.close(rset);
													continue;
												}
												
												fl_x = null;
												break;
											default:
												LOGGER.warn("ZoneData: Unknown shape: " + zoneShape);
												DatabaseUtils.close(statement);
												DatabaseUtils.close(rset);
												continue;
										}
										
										DatabaseUtils.close(statement);
										DatabaseUtils.close(rset);
										statement = null;
										rset = null;
									}
									catch (final Exception e)
									{
										if (Config.ENABLE_ALL_EXCEPTIONS)
										{
											e.printStackTrace();
										}
										
										LOGGER.warn("ZoneData: Failed to load zone coordinates: " + e);
									}
									
								}
								else
								{ // use file one
									
									// Create this zone. Parsing for cuboids is a
									// bit different than for other polygons
									// cuboids need exactly 2 points to be defined.
									// Other polygons need at least 3 (one per
									// vertex)
									if (zoneShape.equalsIgnoreCase("Cuboid"))
									{
										if (coords.length == 2)
										{
											temp.setZoneShape(new ZoneCuboid(coords[0][0], coords[1][0], coords[0][1], coords[1][1], minZ, maxZ));
										}
										else
										{
											LOGGER.warn("ZoneData: Missing cuboid vertex in sql data for zone: " + zoneId);
											continue;
										}
									}
									else if (zoneShape.equalsIgnoreCase("NPoly"))
									{
										// nPoly needs to have at least 3 vertices
										if (coords.length > 2)
										{
											final int[] aX = new int[coords.length];
											final int[] aY = new int[coords.length];
											for (int i = 0; i < coords.length; i++)
											{
												aX[i] = coords[i][0];
												aY[i] = coords[i][1];
											}
											temp.setZoneShape(new ZoneNPoly(aX, aY, minZ, maxZ));
										}
										else
										{
											LOGGER.warn("ZoneData: Bad data for zone: " + zoneId);
											continue;
										}
									}
									else if (zoneShape.equalsIgnoreCase("Cylinder"))
									{
										// A Cylinder zone requires a center point
										// at x,y and a radius
										attrs = d.getAttributes();
										final int zoneRad = Integer.parseInt(attrs.getNamedItem("rad").getNodeValue());
										if (coords.length == 1 && zoneRad > 0)
										{
											temp.setZoneShape(new ZoneCylinder(coords[0][0], coords[0][1], minZ, maxZ, zoneRad));
										}
										else
										{
											LOGGER.warn("ZoneData: Bad data for zone: " + zoneId);
											continue;
										}
									}
									else
									{
										LOGGER.warn("ZoneData: Unknown shape: " + zoneShape);
										continue;
									}
									
								}
								
								zoneShape = null;
								
								// Check for aditional parameters
								for (Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling())
								{
									if ("stat".equalsIgnoreCase(cd.getNodeName()))
									{
										attrs = cd.getAttributes();
										String name = attrs.getNamedItem("name").getNodeValue();
										String val = attrs.getNamedItem("val").getNodeValue();
										
										temp.setParameter(name, val);
									}
									if ("spawn".equalsIgnoreCase(cd.getNodeName()))
									{
										temp.setSpawnLocs(cd);
									}
								}
								
								// Skip checks for fishing zones & add to fishing zone manager
								if (temp instanceof L2FishingZone)
								{
									FishingZoneManager.getInstance().addFishingZone((L2FishingZone) temp);
									continue;
								}
								
								if (temp instanceof L2WaterZone)
								{
									FishingZoneManager.getInstance().addWaterZone((L2WaterZone) temp);
								}
								
								// Register the zone into any world region it intersects with...
								// currently 11136 test for each zone :>
								int ax, ay, bx, by;
								
								for (int x = 0; x < worldRegions.length; x++)
								{
									for (int y = 0; y < worldRegions[x].length; y++)
									{
										ax = x - L2World.OFFSET_X << L2World.SHIFT_BY;
										bx = x + 1 - L2World.OFFSET_X << L2World.SHIFT_BY;
										ay = y - L2World.OFFSET_Y << L2World.SHIFT_BY;
										by = y + 1 - L2World.OFFSET_Y << L2World.SHIFT_BY;
										
										if (temp.getZoneShape().intersectsRectangle(ax, bx, ay, by))
										{
											if (Config.DEBUG)
											{
												LOGGER.info("Zone (" + zoneId + ") added to: " + x + " " + y);
											}
											worldRegions[x][y].addZone(temp);
										}
									}
								}
								
								// check if ID already exist in the map
								
								if (zones.containsKey(zoneId))
								{
									LOGGER.warn("Zone ID " + zoneId + " already exists and was replaced");
								}
								
								zones.put(zoneId, temp);
							}
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.error("ZoneData.load: Error while loading zones ", e);
		}
		
		LOGGER.info("ZoneData: loaded " + zones.size() + " zones.");
	}
	
	public L2ZoneType getZoneById(int zoneId)
	{
		return zones.get(zoneId);
	}
	
	public Map<Integer, L2ZoneType> getAllZones()
	{
		return zones;
	}
	
	public L2ZoneType getZoneByCoordinates(int x, int y, int z)
	{
		for (L2ZoneType zone : zones.values())
		{
			if (zone.isInsideZone(x, y, z))
			{
				return zone;
			}
		}
		
		return null;
	}
}
