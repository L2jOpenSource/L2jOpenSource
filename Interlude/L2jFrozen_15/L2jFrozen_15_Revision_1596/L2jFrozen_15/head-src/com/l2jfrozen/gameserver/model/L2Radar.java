package com.l2jfrozen.gameserver.model;

import java.util.Vector;

import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.serverpackets.RadarControl;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;

public final class L2Radar
{
	private final L2PcInstance player;
	private final Vector<RadarMarker> markers;
	
	public L2Radar(final L2PcInstance player)
	{
		this.player = player;
		markers = new Vector<>();
	}
	
	// Add a marker to player's radar
	public void addMarker(final int x, final int y, final int z)
	{
		RadarMarker newMarker = new RadarMarker(x, y, z);
		
		markers.add(newMarker);
		player.sendPacket(new RadarControl(0, 1, x, y, z));
		
		newMarker = null;
	}
	
	// Remove a marker from player's radar
	public void removeMarker(final int x, final int y, final int z)
	{
		RadarMarker newMarker = new RadarMarker(x, y, z);
		
		markers.remove(newMarker);
		player.sendPacket(new RadarControl(1, 1, x, y, z));
		
		newMarker = null;
	}
	
	public void removeAllMarkers()
	{
		// TODO: Need method to remove all markers from radar at once
		for (final RadarMarker tempMarker : markers)
		{
			player.sendPacket(new RadarControl(1, tempMarker.type, tempMarker.x, tempMarker.y, tempMarker.z));
		}
		
		markers.removeAllElements();
	}
	
	public void loadMarkers()
	{
		// TODO: Need method to re-send radar markers after load/teleport/death
		// etc.
	}
	
	private static class RadarMarker
	{
		// Simple class to model radar points.
		public int type, x, y, z;
		
		@SuppressWarnings("unused")
		public RadarMarker(final int type, final int x, final int y, final int z)
		{
			this.type = type;
			this.x = x;
			this.y = y;
			this.z = z;
		}
		
		public RadarMarker(final int x, final int y, final int z)
		{
			type = 1;
			this.x = x;
			this.y = y;
			this.z = z;
		}
		
		@Override
		public boolean equals(final Object obj)
		{
			try
			{
				RadarMarker temp = (RadarMarker) obj;
				
				if (temp.x == x && temp.y == y && temp.z == z && temp.type == type)
				{
					return true;
				}
				
				temp = null;
				
				return false;
			}
			catch (final Exception e)
			{
				return false;
			}
		}
	}
	
	public class RadarOnPlayer implements Runnable
	{
		private final L2PcInstance myTarget, me;
		
		public RadarOnPlayer(final L2PcInstance target, final L2PcInstance me)
		{
			this.me = me;
			myTarget = target;
		}
		
		@Override
		public void run()
		{
			try
			{
				if (me == null || !me.isOnline())
				{
					return;
				}
				me.sendPacket(new RadarControl(1, 1, me.getX(), me.getY(), me.getZ()));
				if (myTarget == null || !myTarget.isOnline() || !myTarget.haveFlagCTF)
				{
					return;
				}
				me.sendPacket(new RadarControl(0, 1, myTarget.getX(), myTarget.getY(), myTarget.getZ()));
				ThreadPoolManager.getInstance().scheduleGeneral(new RadarOnPlayer(myTarget, me), 15000);
			}
			catch (final Throwable t)
			{
			}
		}
	}
}
