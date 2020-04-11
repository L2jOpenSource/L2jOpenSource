package com.l2jfrozen.gameserver.network.clientpackets;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.csv.MapRegionTable;
import com.l2jfrozen.gameserver.geo.GeoData;
import com.l2jfrozen.gameserver.geo.GeoEngine;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.serverpackets.CharMoveToLocation;
import com.l2jfrozen.gameserver.network.serverpackets.ValidateLocation;
import com.l2jfrozen.gameserver.network.serverpackets.ValidateLocationInVehicle;

public final class ValidatePosition extends L2GameClientPacket
{
	private static Logger LOGGER = Logger.getLogger(ValidatePosition.class);
	
	private int x;
	private int y;
	private int z;
	private int heading;
	@SuppressWarnings("unused")
	private int data;
	
	@Override
	protected void readImpl()
	{
		x = readD();
		y = readD();
		z = readD();
		heading = readD();
		data = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null || activeChar.isTeleporting())
		{
			return;
		}
		
		if (x == 0 && y == 0 && activeChar.getX() != 0)
		{
			return;
		}
		
		if (activeChar.getX() == 0 && activeChar.getY() == 0)
		{
			activeChar.teleToLocation(MapRegionTable.TeleportWhereType.Town);
			clientToServer(activeChar);
		}
		
		final int realX = activeChar.getX();
		final int realY = activeChar.getY();
		final int realZ = activeChar.getZ();
		
		final double dx = x - realX;
		final double dy = y - realY;
		final double dz = z - realZ;
		final double diffSq = dx * dx + dy * dy;
		
		int finalZ = z;
		if (Math.abs(dz) <= 200)
		{
			finalZ = realZ;
		}
		
		final int geoZ = GeoData.getInstance().getHeight(realX, realY, finalZ);
		
		if (Config.DEBUG)
		{
			
			final int realHeading = activeChar.getHeading();
			LOGGER.info("client pos: " + x + " " + y + " " + z + " head " + heading);
			LOGGER.info("server pos: " + realX + " " + realY + " " + realZ + " head " + realHeading);
			LOGGER.info("finalZ" + finalZ + " geoZ: " + geoZ + " destZ: " + activeChar.getZdestination());
			
		}
		
		// COORD Client<-->Server synchronization
		switch (Config.COORD_SYNCHRONIZE)
		{
			
			case 1:
			{ // full synchronization Client --> Server
				// only * using this option it is difficult
				// for players to bypass obstacles
				
				if (!activeChar.isMoving() || !activeChar.validateMovementHeading(heading)) // Heading changed on client = possible obstacle
				{
					// character is not moving, take coordinates from client
					if (diffSq < 2500)
					{ // 50*50 - attack won't work fluently if even small differences are corrected
						activeChar.getPosition().setXYZ(realX, realY, finalZ);
						
					}
					else
					{
						activeChar.getPosition().setXYZ(x, y, finalZ);
					}
				}
				else
				{
					activeChar.getPosition().setXYZ(realX, realY, finalZ);
					
				}
				
				activeChar.setHeading(heading);
				
			}
				break;
			case 2:
			{ // full synchronization Server --> Client (bounces for validation)
				
				if (Config.GEODATA > 0 && (diffSq > 250000 || Math.abs(dz) > 200))
				{
					if (Math.abs(dz) > 200)
					{
						
						if (Math.abs(finalZ - activeChar.getClientZ()) < 800)
						{
							activeChar.getPosition().setXYZ(realX, realY, finalZ);
						}
						
					}
					else
					{
						if (!activeChar.isMoving())
						{
							
							if (activeChar.isInBoat())
							{
								sendPacket(new ValidateLocationInVehicle(activeChar));
							}
							else
							{
								sendPacket(new ValidateLocation(activeChar));
							}
							
						}
						else if (diffSq > activeChar.getStat().getMoveSpeed())
						{
							activeChar.broadcastPacket(new CharMoveToLocation(activeChar));
						}
						
						finalZ = activeChar.getPosition().getZ();
					}
					
				}
				
			}
				break;
			case -1:
			{ // just (client-->server) Z coordination
				
				if (Math.abs(dz) > 200)
				{
					
					if (Math.abs(z - activeChar.getClientZ()) < 800)
					{
						activeChar.getPosition().setXYZ(realX, realY, finalZ);
					}
					
				}
				else
				{
					finalZ = realZ;
				}
				
			}
				break;
			default:
			case 0:
			{ // no synchronization at all
				// the server has the correct information
				finalZ = realZ;
			}
				break;
			
		}
		
		// EXPERIMENTAL fix when players cross the floor
		int deltaZ = activeChar.getZ() - z;
		if (deltaZ > 1024)
		{
			int zLocation = GeoEngine.getInstance().getHeight(activeChar.getX(), activeChar.getY(), activeChar.getZ());
			activeChar.teleToLocation(activeChar.getX(), activeChar.getY(), zLocation);
			LOGGER.info("Player " + activeChar.getName() + " has fallen more than 1024 units, returned to last position (" + activeChar.getX() + ", " + activeChar.getY() + ", " + zLocation + ")");
			return;
		}
		
		// check water
		if (Config.ALLOW_WATER)
		{
			activeChar.checkWaterState();
		}
		
		// check falling if previous client Z is less then
		if (Config.FALL_DAMAGE)
		{
			activeChar.isFalling(finalZ);
		}
		
		activeChar.setClientX(x);
		activeChar.setClientY(y);
		activeChar.setClientZ(z);
		activeChar.setClientHeading(heading);
		
	}
	
	private void clientToServer(final L2PcInstance player)
	{
		x = player.getX();
		y = player.getY();
		z = player.getZ();
	}
	
	public boolean equal(final ValidatePosition pos)
	{
		return x == pos.x && y == pos.y && z == pos.z && heading == pos.heading;
	}
	
	@Override
	public String getType()
	{
		return "[C] 48 ValidatePosition";
	}
}
