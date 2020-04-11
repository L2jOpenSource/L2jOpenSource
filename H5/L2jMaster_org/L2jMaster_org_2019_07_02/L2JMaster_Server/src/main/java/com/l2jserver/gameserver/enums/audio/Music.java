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
package com.l2jserver.gameserver.enums.audio;

import com.l2jserver.gameserver.network.serverpackets.PlaySound;

/**
 * @author Zealar
 */
public enum Music implements IAudio
{
	B03_D_10000("B03_D", 10000),
	B03_F("B03_F"),
	B04_S01("B04_S01"),
	B06_F_2000("B06_F", 2000),
	B06_S01_2000("B06_S01", 2000),
	B07_S01_2000("B07_S01", 2000),
	BS01_A_10000("BS01_A", 10000),
	BS01_A_7000("BS01_A", 7000),
	BS01_D_10000("BS01_D", 10000),
	BS01_D_6000("BS01_D", 6000),
	BS02_A_10000("BS02_A", 10000),
	BS02_A_6000("BS02_A", 6000),
	BS02_D_10000("BS02_D", 10000),
	BS02_D_7000("BS02_D", 7000),
	BS03_A_10000("BS03_A", 10000),
	BS04_A_6000("BS04_A", 6000),
	BS04_A_3000("BS04_A", 3000),
	BS05_D_5000("BS05_D", 5000),
	BS05_D_6000("BS05_D", 6000),
	BS06_A_5000("BS06_A", 5000),
	BS07_A_10000("BS07_A", 10000),
	BS07_D_10000("BS07_D", 10000),
	BS08_A_10000("BS08_A", 10000),
	EV_01_10000("EV_01", 10000),
	EV_02_10000("EV_02", 10000),
	EV_03_200("EV_03", 200),
	EV_04_200("EV_04", 200),
	HB01_10000("HB01", 10000),
	NS01_F_5000("ns01_f", 5000),
	NS22_F_5000("ns22_f", 5000),
	RM01_A_4000("Rm01_A", 4000),
	RM01_A_8000("Rm01_A", 8000),
	RM01_S_4000("RM01_S", 4000),
	SSQ_Dawn_01("SSQ_Dawn_01"),
	SSQ_Dusk_01("SSQ_Dusk_01"),
	SSQ_Neutral_01("SSQ_Neutral_01"),
	TP01_F_3000("TP01_F", 3000),
	TP02_F_3000("TP02_F", 3000),
	TP03_F_3000("TP03_F", 3000),
	TP04_F_3000("TP04_F", 3000),
	TP05_F("TP05_F"),
	TP05_F_5000("TP05_F", 5000),
	SF_S_01("SF_S_01"),
	NS22_F("NS22_F"),
	S_RACE("S_Race"),
	SF_P_01("SF_P_01"),
	SIEGE_VICTORY("Siege_Victory");
	
	private final PlaySound _playSound;
	
	Music(String soundName)
	{
		_playSound = PlaySound.createMusic(soundName, 0);
	}
	
	Music(String soundName, int delay)
	{
		_playSound = PlaySound.createMusic(soundName, delay);
	}
	
	@Override
	public String getSoundName()
	{
		return _playSound.getSoundName();
	}
	
	@Override
	public PlaySound getPacket()
	{
		return _playSound;
	}
}