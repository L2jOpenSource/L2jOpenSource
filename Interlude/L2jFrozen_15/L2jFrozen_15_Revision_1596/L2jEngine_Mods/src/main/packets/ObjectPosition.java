package main.packets;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.l2jfrozen.gameserver.network.serverpackets.L2GameServerPacket;
import com.l2jfrozen.util.Point3D;

import main.holders.objects.NpcHolder;

/**
 * OriginalPacket -> PartyMemberPosition
 * @author fissban
 */
public class ObjectPosition extends L2GameServerPacket
{
	private final Map<Integer, Point3D> locations = new HashMap<>();
	
	public ObjectPosition(List<NpcHolder> chests)
	{
		locations.clear();
		
		chests.stream().filter(c -> c.getInstance() != null).forEach(c -> locations.put(c.getObjectId(), c.getInstance().getPosition().getWorldPosition()));
	}
	
	@Override
	public void writeImpl()
	{
		writeC(0xa7);
		writeD(locations.size());
		for (Map.Entry<Integer, Point3D> entry : locations.entrySet())
		{
			Point3D loc = entry.getValue();
			writeD(entry.getKey());
			writeD(loc.getX());
			writeD(loc.getY());
			writeD(loc.getZ());
		}
	}
	
	@Override
	public String getType()
	{
		// TODO Auto-generated method stub
		return null;
	}
	
}
