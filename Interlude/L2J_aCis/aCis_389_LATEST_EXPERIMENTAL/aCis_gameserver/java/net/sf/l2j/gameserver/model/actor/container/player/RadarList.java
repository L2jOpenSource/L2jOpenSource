package net.sf.l2j.gameserver.model.actor.container.player;

import java.util.ArrayList;
import java.util.List;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.location.RadarMarker;
import net.sf.l2j.gameserver.network.serverpackets.RadarControl;

public final class RadarList
{
	private final Player _player;
	private final List<RadarMarker> _markers = new ArrayList<>();
	
	public RadarList(Player player)
	{
		_player = player;
	}
	
	/**
	 * Add a {@link RadarMarker} to this {@link RadarList}.
	 * @param x : The X position.
	 * @param y : The Y position.
	 * @param z : The Z position.
	 */
	public void addMarker(int x, int y, int z)
	{
		_markers.add(new RadarMarker(x, y, z));
		
		_player.sendPacket(new RadarControl(2, 2, x, y, z));
		_player.sendPacket(new RadarControl(0, 1, x, y, z));
	}
	
	/**
	 * Remove a {@link RadarMarker} from this {@link RadarList}.
	 * @param x : The X position.
	 * @param y : The Y position.
	 * @param z : The Z position.
	 */
	public void removeMarker(int x, int y, int z)
	{
		_markers.remove(new RadarMarker(x, y, z));
		
		_player.sendPacket(new RadarControl(1, 1, x, y, z));
	}
	
	/**
	 * Load all existing {@link RadarMarker}s to this {@link RadarList}.
	 */
	public void loadMarkers()
	{
		_player.sendPacket(new RadarControl(2, 2, _player.getPosition()));
		
		for (RadarMarker marker : _markers)
			_player.sendPacket(new RadarControl(0, 1, marker));
	}
	
	/**
	 * Remove all existing {@link RadarMarker}s from this {@link RadarList}.
	 */
	public void removeAllMarkers()
	{
		for (RadarMarker marker : _markers)
			_player.sendPacket(new RadarControl(2, 2, marker));
		
		_markers.clear();
	}
}