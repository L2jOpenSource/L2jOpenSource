package main.holders.objects;

import com.l2jfrozen.gameserver.model.L2Summon;

/**
 * @author fissban
 */
public class SummonHolder extends CharacterHolder
{
	private PlayerHolder owner = null;
	
	public SummonHolder(L2Summon summon)
	{
		super(summon);
	}
	
	@Override
	public L2Summon getInstance()
	{
		return (L2Summon) super.getInstance();
	}
	
	@Override
	public PlayerHolder getActingPlayer()
	{
		return owner;
	}
	
	public PlayerHolder getOwner()
	{
		return owner;
	}
	
	public void setOwner(PlayerHolder player)
	{
		owner = player;
	}
}
