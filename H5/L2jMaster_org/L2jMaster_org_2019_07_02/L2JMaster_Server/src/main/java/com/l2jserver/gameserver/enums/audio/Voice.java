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
public enum Voice implements IAudio
{
	TUTORIAL_VOICE_001A_2000("tutorial_voice_001a", 2000),
	TUTORIAL_VOICE_001B_2000("tutorial_voice_001b", 2000),
	TUTORIAL_VOICE_001C_2000("tutorial_voice_001c", 2000),
	TUTORIAL_VOICE_001D_2000("tutorial_voice_001d", 2000),
	TUTORIAL_VOICE_001E_2000("tutorial_voice_001e", 2000),
	TUTORIAL_VOICE_001F_2000("tutorial_voice_001f", 2000),
	TUTORIAL_VOICE_001G_2000("tutorial_voice_001g", 2000),
	TUTORIAL_VOICE_001H_2000("tutorial_voice_001h", 2000),
	TUTORIAL_VOICE_001I_2000("tutorial_voice_001i", 2000),
	TUTORIAL_VOICE_001K_2000("tutorial_voice_001k", 2000),
	TUTORIAL_VOICE_002_1000("tutorial_voice_002", 1000),
	TUTORIAL_VOICE_003_2000("tutorial_voice_003", 2000),
	TUTORIAL_VOICE_004_5000("tutorial_voice_004", 5000),
	TUTORIAL_VOICE_005_5000("tutorial_voice_005", 5000),
	TUTORIAL_VOICE_006_1000("tutorial_voice_006", 1000),
	TUTORIAL_VOICE_006_3500("tutorial_voice_006", 3500),
	TUTORIAL_VOICE_007_3500("tutorial_voice_007", 3500),
	TUTORIAL_VOICE_008_1000("tutorial_voice_008", 1000),
	TUTORIAL_VOICE_009A("tutorial_voice_009a"),
	TUTORIAL_VOICE_009B("tutorial_voice_009b"),
	TUTORIAL_VOICE_009C("tutorial_voice_009c"),
	TUTORIAL_VOICE_010A("tutorial_voice_010a"),
	TUTORIAL_VOICE_010B("tutorial_voice_010b"),
	TUTORIAL_VOICE_010C("tutorial_voice_010c"),
	TUTORIAL_VOICE_010D("tutorial_voice_010d"),
	TUTORIAL_VOICE_010E("tutorial_voice_010e"),
	TUTORIAL_VOICE_010F("tutorial_voice_010f"),
	TUTORIAL_VOICE_010G("tutorial_voice_010g"),
	TUTORIAL_VOICE_011_1000("tutorial_voice_011", 1000),
	TUTORIAL_VOICE_012_1000("tutorial_voice_012", 1000),
	TUTORIAL_VOICE_013_1000("tutorial_voice_013", 1000),
	TUTORIAL_VOICE_014_1000("tutorial_voice_014", 1000),
	TUTORIAL_VOICE_016_1000("tutorial_voice_016", 1000),
	TUTORIAL_VOICE_017_1000("tutorial_voice_017", 1000),
	TUTORIAL_VOICE_018_1000("tutorial_voice_018", 1000),
	TUTORIAL_VOICE_019_1000("tutorial_voice_019", 1000),
	TUTORIAL_VOICE_020_1000("tutorial_voice_020", 1000),
	TUTORIAL_VOICE_021_1000("tutorial_voice_021", 1000),
	TUTORIAL_VOICE_023_1000("tutorial_voice_023", 1000),
	TUTORIAL_VOICE_024_1000("tutorial_voice_024", 1000),
	TUTORIAL_VOICE_025_1000("tutorial_voice_025", 1000),
	TUTORIAL_VOICE_026_1000("tutorial_voice_026", 1000),
	TUTORIAL_VOICE_027_1000("tutorial_voice_027", 1000),
	TUTORIAL_VOICE_028_1000("tutorial_voice_028", 1000),
	TUTORIAL_VOICE_030_1000("tutorial_voice_030", 1000);
	
	private final PlaySound _playSound;
	
	private Voice(String soundName)
	{
		_playSound = PlaySound.createVoice(soundName, 0);
	}
	
	private Voice(String soundName, int delay)
	{
		_playSound = PlaySound.createVoice(soundName, delay);
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