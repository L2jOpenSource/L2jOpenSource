package com.l2jfrozen.gameserver.geo.geoeditorcon;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

public class GeoEditorThread extends Thread
{
	protected static final Logger LOGGER = Logger.getLogger(GeoEditorThread.class);
	
	private boolean working = false;
	
	private int mode = 0; // 0 - don't send coords, 1 - send each
	
	// validateposition from client, 2 - send in
	// intervals of sendDelay ms.
	private int sendDelay = 1000; // default - once in second
	
	private final Socket geSocket;
	
	private OutputStream out;
	
	private final List<L2PcInstance> gms;
	
	public GeoEditorThread(final Socket ge)
	{
		geSocket = ge;
		working = true;
		gms = new ArrayList<>();
	}
	
	@Override
	public void interrupt()
	{
		try
		{
			geSocket.close();
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
		}
		super.interrupt();
	}
	
	@Override
	public void run()
	{
		try
		{
			out = geSocket.getOutputStream();
			int timer = 0;
			
			while (working)
			{
				if (!isConnected())
				{
					working = false;
				}
				
				if (mode == 2 && timer > sendDelay)
				{
					for (final L2PcInstance gm : gms)
					{
						if (gm.isOnline())
						{
							sendGmPosition(gm);
						}
						else
						{
							gms.remove(gm);
						}
					}
					timer = 0;
				}
				
				try
				{
					sleep(100);
					if (mode == 2)
					{
						timer += 100;
					}
				}
				catch (final Exception e)
				{
					if (Config.ENABLE_ALL_EXCEPTIONS)
					{
						e.printStackTrace();
					}
					
				}
			}
		}
		catch (final SocketException e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			LOGGER.warn("GeoEditor disconnected. ", e);
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			LOGGER.error(e.getMessage(), e);
		}
		finally
		{
			try
			{
				geSocket.close();
			}
			catch (final Exception e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					e.printStackTrace();
				}
				
			}
			working = false;
		}
	}
	
	public void sendGmPosition(final int gx, final int gy, final short z)
	{
		if (!isConnected())
		{
			return;
		}
		try
		{
			synchronized (out)
			{
				writeC(0x0b); // length 11 bytes!
				writeC(0x01); // Cmd = save cell;
				writeD(gx); // Global coord X;
				writeD(gy); // Global coord Y;
				writeH(z); // Coord Z;
				out.flush();
			}
		}
		catch (final SocketException e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			LOGGER.warn("GeoEditor disconnected. ", e);
			working = false;
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			LOGGER.error(e.getMessage(), e);
			try
			{
				geSocket.close();
			}
			catch (final Exception ex)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					e.printStackTrace();
				}
				
			}
			working = false;
		}
	}
	
	public void sendGmPosition(final L2PcInstance gm)
	{
		sendGmPosition(gm.getX(), gm.getY(), (short) gm.getZ());
	}
	
	public void sendPing()
	{
		if (!isConnected())
		{
			return;
		}
		try
		{
			synchronized (out)
			{
				writeC(0x01); // length 1 byte!
				writeC(0x02); // Cmd = ping (dummy packet for connection test);
				out.flush();
			}
		}
		catch (final SocketException e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			LOGGER.warn("GeoEditor disconnected. ", e);
			working = false;
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			LOGGER.error(e.getMessage(), e);
			try
			{
				geSocket.close();
			}
			catch (final Exception ex)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					ex.printStackTrace();
				}
				
			}
			working = false;
		}
	}
	
	private void writeD(final int value) throws IOException
	{
		out.write(value & 0xff);
		out.write(value >> 8 & 0xff);
		out.write(value >> 16 & 0xff);
		out.write(value >> 24 & 0xff);
	}
	
	private void writeH(final int value) throws IOException
	{
		out.write(value & 0xff);
		out.write(value >> 8 & 0xff);
	}
	
	private void writeC(final int value) throws IOException
	{
		out.write(value & 0xff);
	}
	
	public void setMode(final int value)
	{
		mode = value;
	}
	
	public void setTimer(final int value)
	{
		if (value < 500)
		{
			sendDelay = 500; // maximum - 2 times per second!
		}
		else if (value > 60000)
		{
			sendDelay = 60000; // Minimum - 1 time per minute.
		}
		else
		{
			sendDelay = value;
		}
	}
	
	public void addGM(final L2PcInstance gm)
	{
		if (!gms.contains(gm))
		{
			gms.add(gm);
		}
	}
	
	public void removeGM(final L2PcInstance gm)
	{
		if (gms.contains(gm))
		{
			gms.remove(gm);
		}
	}
	
	public boolean isSend(final L2PcInstance gm)
	{
		return mode == 1 && gms.contains(gm);
	}
	
	private boolean isConnected()
	{
		return geSocket.isConnected() && !geSocket.isClosed();
	}
	
	public boolean isWorking()
	{
		sendPing();
		return working;
	}
	
	public int getMode()
	{
		return mode;
	}
}