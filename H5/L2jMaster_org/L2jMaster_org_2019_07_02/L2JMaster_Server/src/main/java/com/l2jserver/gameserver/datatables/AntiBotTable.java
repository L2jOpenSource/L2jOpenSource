/*
 * Copyright (C) 2004-2019 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jserver.gameserver.datatables;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2jserver.gameserver.ThreadPoolManager;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.network.serverpackets.PledgeImage;
import com.l2jserver.util.Rnd;

/**
 * @author fissban
 */
public class AntiBotTable
{
	public static Logger _log = Logger.getLogger(AntiBotTable.class.getName());
	
	public static Map<Integer, antiBotData> _imageAntiBotOri = new HashMap<>();
	public static Map<Integer, antiBotData> _imageAntiBotClient = new HashMap<>();
	
	// @formatter:off
	public final static int[] img_antibot_id =
	{
		7000, 7001, 7002, 7003, 7004, 7005, 7006, 7007, 7008, 7009
	};
	// @formatter:on
	
	/**
	 * Cargamos las imagenes a la memoria
	 */
	public void loadImage()
	{
		LoadImgAntiBot();
		_log.log(Level.INFO, "loading " + _imageAntiBotOri.size() + " images of AntiBot");
	}
	
	/**
	 * Leemos y cargamos las imagenes a la memoria
	 */
	private void LoadImgAntiBot()
	{
		_imageAntiBotOri.clear();
		int cont = 0;
		
		for (int imgId : img_antibot_id)
		{
			File image = new File("data/images/antibot/" + imgId + ".dds");
			_imageAntiBotOri.put(cont, new antiBotData(cont, ConverterImgBytes(image)));
			cont++;
		}
		
		// iniciamos la encriptacion
		ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new startEncriptCaptcha(), 100, 600000); // 10 min
	}
	
	/**
	 * Usado para enviar la imagen al char.
	 * @param player
	 * @param imgId
	 */
	public void sendImage(L2PcInstance player, int imgId)
	{
		PledgeImage packet = null;
		
		// Antibot
		if ((imgId >= 50000) && (imgId <= 800000))
		{
			for (Entry<Integer, antiBotData> entrySet : _imageAntiBotClient.entrySet())
			{
				antiBotData imgCoding = entrySet.getValue();
				
				if (imgId == imgCoding.getCodificacion())
				{
					packet = new PledgeImage(imgId, imgCoding.getImagen());
				}
			}
		}
		
		player.sendPacket(packet);
	}
	
	/**
	 * Sistema de encritpacion de imagenes para el sistema de AntiBot
	 */
	public static class startEncriptCaptcha implements Runnable
	{
		public startEncriptCaptcha()
		{
		}
		
		@Override
		public void run()
		{
			_imageAntiBotClient.clear();
			
			for (Entry<Integer, antiBotData> entrySet : _imageAntiBotOri.entrySet())
			{
				entrySet.getValue().getImagen();
				_imageAntiBotClient.put(entrySet.getKey(), new antiBotData(Rnd.get(50000, 800000), entrySet.getValue().getImagen()));
			}
		}
	}
	
	/**
	 * @param pos
	 * @return segun el valor el pos, devuelve el ID correspondiente al numero "pos" ingresado
	 */
	public int getAntiBotClientID(int pos)
	{
		int returnCoding = 0;
		
		for (Entry<Integer, antiBotData> entrySet : _imageAntiBotClient.entrySet())
		{
			int numeroImage = entrySet.getKey().intValue(); // numero
			
			if (pos == numeroImage)
			{
				antiBotData imgCoding = entrySet.getValue();
				returnCoding = imgCoding.getCodificacion();// codificacion
			}
			
			// Nunca deberia pasar!
			if (pos > 9)
			{
				_log.log(Level.SEVERE, "error in getAntiBotClientID...number dont exist");
			}
		}
		return returnCoding;
	}
	
	public static class antiBotData
	{
		int _codificacion;
		byte[] _data;
		
		public antiBotData(int codificacion, byte[] data)
		{
			_codificacion = codificacion;
			_data = data;
		}
		
		public int getCodificacion()
		{
			return _codificacion;
		}
		
		public byte[] getImagen()
		{
			return _data;
		}
	}
	
	/**
	 * Tomamos una imagen DDS y la convertimos en byte[]
	 * @param imagen a convertir (File)
	 * @return imagen en byte[]
	 */
	private static byte[] ConverterImgBytes(File imagen)
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		
		byte[] buffer = new byte[1024];
		try (FileInputStream fis = new FileInputStream(imagen))
		{
			for (int readNum; (readNum = fis.read(buffer)) != -1;)
			{
				bos.write(buffer, 0, readNum);
			}
		}
		catch (IOException e)
		{
			_log.log(Level.SEVERE, "Error when converter image to byte[]");
			
		}
		
		return bos.toByteArray();
	}
	
	public static AntiBotTable getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final AntiBotTable _instance = new AntiBotTable();
	}
}