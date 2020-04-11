package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.Boat;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.location.Location;

public class MoveToLocationInVehicle extends L2GameServerPacket
{
	private final int _objectId;
	private final int _boatId;
	private final int _tX;
	private final int _tY;
	private final int _tZ;
	private final int _oX;
	private final int _oY;
	private final int _oZ;
	
	public MoveToLocationInVehicle(Player player, Boat boat, Location tLoc, Location oLoc)
	{
		this(player, boat, tLoc.getX(), tLoc.getY(), tLoc.getZ(), oLoc.getX(), oLoc.getY(), oLoc.getZ());
	}
	
	public MoveToLocationInVehicle(Player player, Boat boat, int tX, int tY, int tZ, int oX, int oY, int oZ)
	{
		_objectId = player.getObjectId();
		_boatId = boat.getObjectId();
		_tX = tX;
		_tY = tY;
		_tZ = tZ;
		_oX = oX;
		_oY = oY;
		_oZ = oZ;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0x71);
		writeD(_objectId);
		writeD(_boatId);
		writeD(_tX);
		writeD(_tY);
		writeD(_tZ);
		writeD(_oX);
		writeD(_oY);
		writeD(_oZ);
	}
}