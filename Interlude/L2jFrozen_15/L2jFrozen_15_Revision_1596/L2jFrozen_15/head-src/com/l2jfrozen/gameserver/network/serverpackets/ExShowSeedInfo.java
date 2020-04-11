package com.l2jfrozen.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

import com.l2jfrozen.gameserver.managers.CastleManorManager.SeedProduction;
import com.l2jfrozen.gameserver.model.L2Manor;

/**
 * format(packet 0xFE) ch ddd [dddddcdcd] c - id h - sub id d - manor id d d - size [ d - seed id d - left to buy d - started amount d - sell price d - seed level c d - reward 1 id c d - reward 2 id ]
 * @author l3x
 */
public class ExShowSeedInfo extends L2GameServerPacket
{
	private List<SeedProduction> seedProductions;
	private final int manorId;
	
	public ExShowSeedInfo(final int manorId, final List<SeedProduction> seeds)
	{
		this.manorId = manorId;
		seedProductions = seeds;
		if (seedProductions == null)
		{
			seedProductions = new ArrayList<>();
		}
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE); // Id
		writeH(0x1C); // SubId
		writeC(0);
		writeD(manorId); // Manor ID
		writeD(0);
		writeD(seedProductions.size());
		for (final SeedProduction seed : seedProductions)
		{
			writeD(seed.getId()); // Seed id
			writeD(seed.getCanProduce()); // Left to buy
			writeD(seed.getStartProduce()); // Started amount
			writeD(seed.getPrice()); // Sell Price
			writeD(L2Manor.getInstance().getSeedLevel(seed.getId())); // Seed Level
			writeC(1); // reward 1 Type
			writeD(L2Manor.getInstance().getRewardItemBySeed(seed.getId(), 1)); // Reward 1 Type Item Id
			writeC(1); // reward 2 Type
			writeD(L2Manor.getInstance().getRewardItemBySeed(seed.getId(), 2)); // Reward 2 Type Item Id
		}
	}
	
	@Override
	public String getType()
	{
		return "[S] FE:1C ExShowSeedInfo";
	}
}
