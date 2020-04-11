package com.l2jfrozen.gameserver.network.serverpackets;

import java.util.Map;

import com.l2jfrozen.gameserver.model.entity.Hero;
import com.l2jfrozen.gameserver.model.entity.olympiad.Olympiad;
import com.l2jfrozen.gameserver.templates.StatsSet;

/**
 * Format: (ch) d [SdSdSdd] d: size [ S: hero name d: hero class ID S: hero clan name d: hero clan crest id S: hero ally name d: hero Ally id d: count ]
 * @author -Wooden- Format from KenM Re-written by godson
 */
public class ExHeroList extends L2GameServerPacket
{
	private final Map<Integer, StatsSet> heroList;
	
	public ExHeroList()
	{
		heroList = Hero.getInstance().getHeroes();
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x23);
		writeD(heroList.size());
		
		for (final Integer heroId : heroList.keySet())
		{
			final StatsSet hero = heroList.get(heroId);
			writeS(hero.getString(Olympiad.CHAR_NAME));
			writeD(hero.getInteger(Olympiad.CLASS_ID));
			writeS(hero.getString(Hero.CLAN_NAME, ""));
			writeD(hero.getInteger(Hero.CLAN_CREST, 0));
			writeS(hero.getString(Hero.ALLY_NAME, ""));
			writeD(hero.getInteger(Hero.ALLY_CREST, 0));
			writeD(hero.getInteger(Hero.COUNT));
		}
		
	}
	
	@Override
	public String getType()
	{
		return "[S] FE:23 ExHeroList";
	}
	
}