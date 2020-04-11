package com.l2jfrozen.gameserver.model.entity.olympiad;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.entity.olympiad.Olympiad.COMP_TYPE;
import com.l2jfrozen.util.L2FastList;
import com.l2jfrozen.util.random.Rnd;

/**
 * @author GodKratos
 */
class OlympiadManager implements Runnable
{
	protected static final Logger LOGGER = Logger.getLogger(OlympiadManager.class);
	private final Map<Integer, OlympiadGame> olympiadInstances;
	
	protected static final OlympiadStadium[] STADIUMS =
	{
		new OlympiadStadium(-20814, -21189, -3030),
		new OlympiadStadium(-120324, -225077, -3331),
		new OlympiadStadium(-102495, -209023, -3331),
		new OlympiadStadium(-120156, -207378, -3331),
		new OlympiadStadium(-87628, -225021, -3331),
		new OlympiadStadium(-81705, -213209, -3331),
		new OlympiadStadium(-87593, -207339, -3331),
		new OlympiadStadium(-93709, -218304, -3331),
		new OlympiadStadium(-77157, -218608, -3331),
		new OlympiadStadium(-69682, -209027, -3331),
		new OlympiadStadium(-76887, -201256, -3331),
		new OlympiadStadium(-109985, -218701, -3331),
		new OlympiadStadium(-126367, -218228, -3331),
		new OlympiadStadium(-109629, -201292, -3331),
		new OlympiadStadium(-87523, -240169, -3331),
		new OlympiadStadium(-81748, -245950, -3331),
		new OlympiadStadium(-77123, -251473, -3331),
		new OlympiadStadium(-69778, -241801, -3331),
		new OlympiadStadium(-76754, -234014, -3331),
		new OlympiadStadium(-93742, -251032, -3331),
		new OlympiadStadium(-87466, -257752, -3331),
		new OlympiadStadium(-114413, -213241, -3331)
	};
	
	public OlympiadManager()
	{
		olympiadInstances = new HashMap<>();
	}
	
	public static OlympiadManager getInstance()
	{
		return SingletonHolder.instance;
	}
	
	@Override
	public synchronized void run()
	{
		if (Olympiad.getInstance().isOlympiadEnd())
		{
			return;
		}
		
		Map<Integer, OlympiadGameTask> gamesQueue = new HashMap<>();
		
		while (Olympiad.getInstance().inCompPeriod())
		{
			if (Olympiad.getNobleCount() == 0)
			{
				try
				{
					wait(60000);
				}
				catch (final InterruptedException ex)
				{
					// return;
				}
				continue;
			}
			
			int gamesQueueSize = 0;
			
			final L2FastList<Integer> readyClasses = Olympiad.hasEnoughRegisteredClassed();
			final boolean readyNonClassed = Olympiad.hasEnoughRegisteredNonClassed();
			if (readyClasses != null || readyNonClassed)
			{
				// set up the games queue
				for (int i = 0; i < STADIUMS.length; i++)
				{
					if (!existNextOpponents(Olympiad.getRegisteredNonClassBased()) && !existNextOpponents(getRandomClassList(Olympiad.getRegisteredClassBased(), readyClasses)))
					{
						break;
					}
					if (STADIUMS[i].isFreeToUse())
					{
						if (i < STADIUMS.length / 2)
						{
							if (readyNonClassed && existNextOpponents(Olympiad.getRegisteredNonClassBased()))
							{
								try
								{
									olympiadInstances.put(i, new OlympiadGame(i, COMP_TYPE.NON_CLASSED, nextOpponents(Olympiad.getRegisteredNonClassBased())));
									gamesQueue.put(i, new OlympiadGameTask(olympiadInstances.get(i)));
									STADIUMS[i].setStadiaBusy();
								}
								catch (final Exception ex)
								{
									if (Config.DEBUG)
									{
										LOGGER.warn("Olympiad Manager: Stadium - " + i + " assignment, an error has been occurred:", ex);
									}
									
									if (olympiadInstances.get(i) != null)
									{
										for (final L2PcInstance player : olympiadInstances.get(i).getPlayers())
										{
											player.sendMessage("Your olympiad registration was canceled due to an error");
											player.setIsInOlympiadMode(false);
											player.setIsInOlympiadFight(false);
											player.setOlympiadSide(-1);
											player.setOlympiadGameId(-1);
										}
										olympiadInstances.remove(i);
									}
									if (gamesQueue.get(i) != null)
									{
										gamesQueue.remove(i);
									}
									STADIUMS[i].setStadiaFree();
									
									// try to reuse this stadia next time
									i--;
								}
							}
							
							else if (readyClasses != null && existNextOpponents(getRandomClassList(Olympiad.getRegisteredClassBased(), readyClasses)))
							{
								try
								{
									olympiadInstances.put(i, new OlympiadGame(i, COMP_TYPE.CLASSED, nextOpponents(getRandomClassList(Olympiad.getRegisteredClassBased(), readyClasses))));
									gamesQueue.put(i, new OlympiadGameTask(olympiadInstances.get(i)));
									STADIUMS[i].setStadiaBusy();
								}
								catch (final Exception ex)
								{
									if (Config.DEBUG)
									{
										LOGGER.warn("Olympiad Manager: Stadium - " + i + " assignment, an error has been occurred:", ex);
									}
									
									if (olympiadInstances.get(i) != null)
									{
										for (final L2PcInstance player : olympiadInstances.get(i).getPlayers())
										{
											player.sendMessage("Your olympiad registration was canceled due to an error");
											player.setIsInOlympiadMode(false);
											player.setIsInOlympiadFight(false);
											player.setOlympiadSide(-1);
											player.setOlympiadGameId(-1);
										}
										olympiadInstances.remove(i);
									}
									if (gamesQueue.get(i) != null)
									{
										gamesQueue.remove(i);
									}
									STADIUMS[i].setStadiaFree();
									
									// try to reuse this stadia next time
									i--;
								}
							}
						}
						else
						{
							if (readyClasses != null && existNextOpponents(getRandomClassList(Olympiad.getRegisteredClassBased(), readyClasses)))
							{
								try
								{
									olympiadInstances.put(i, new OlympiadGame(i, COMP_TYPE.CLASSED, nextOpponents(getRandomClassList(Olympiad.getRegisteredClassBased(), readyClasses))));
									gamesQueue.put(i, new OlympiadGameTask(olympiadInstances.get(i)));
									STADIUMS[i].setStadiaBusy();
								}
								catch (final Exception ex)
								{
									if (Config.DEBUG)
									{
										LOGGER.warn("Olympiad Manager: Stadium - " + i + " assignment, an error has been occurred:", ex);
									}
									
									if (olympiadInstances.get(i) != null)
									{
										for (final L2PcInstance player : olympiadInstances.get(i).getPlayers())
										{
											player.sendMessage("Your olympiad registration was canceled due to an error");
											player.setIsInOlympiadMode(false);
											player.setIsInOlympiadFight(false);
											player.setOlympiadSide(-1);
											player.setOlympiadGameId(-1);
										}
										olympiadInstances.remove(i);
									}
									if (gamesQueue.get(i) != null)
									{
										gamesQueue.remove(i);
									}
									STADIUMS[i].setStadiaFree();
									
									// try to reuse this stadia next time
									i--;
								}
							}
							else if (readyNonClassed && existNextOpponents(Olympiad.getRegisteredNonClassBased()))
							{
								try
								{
									olympiadInstances.put(i, new OlympiadGame(i, COMP_TYPE.NON_CLASSED, nextOpponents(Olympiad.getRegisteredNonClassBased())));
									gamesQueue.put(i, new OlympiadGameTask(olympiadInstances.get(i)));
									STADIUMS[i].setStadiaBusy();
								}
								catch (final Exception ex)
								{
									if (Config.DEBUG)
									{
										LOGGER.warn("Olympiad Manager: Stadium - " + i + " assignment, an error has been occurred:", ex);
									}
									
									if (olympiadInstances.get(i) != null)
									{
										for (final L2PcInstance player : olympiadInstances.get(i).getPlayers())
										{
											player.sendMessage("Your olympiad registration was canceled due to an error");
											player.setIsInOlympiadMode(false);
											player.setIsInOlympiadFight(false);
											player.setOlympiadSide(-1);
											player.setOlympiadGameId(-1);
										}
										olympiadInstances.remove(i);
									}
									if (gamesQueue.get(i) != null)
									{
										gamesQueue.remove(i);
									}
									STADIUMS[i].setStadiaFree();
									
									// try to reuse this stadia next time
									i--;
								}
							}
						}
					}
					else
					{
						if (gamesQueue.get(i) == null || gamesQueue.get(i).isTerminated() || gamesQueue.get(i).game == null)
						{
							try
							{
								olympiadInstances.remove(i);
								gamesQueue.remove(i);
								STADIUMS[i].setStadiaFree();
								i--;
							}
							catch (final Exception e)
							{
								LOGGER.warn("Exception on OlympiadManager.run() ", e);
							}
						}
					}
				}
				
				/*
				 * try { wait(30000); } catch (InterruptedException e) { }
				 */
				
				// Start games
				gamesQueueSize = gamesQueue.size();
				for (int i = 0; i < gamesQueueSize; i++)
				{
					if (gamesQueue.get(i) != null && !gamesQueue.get(i).isTerminated() && !gamesQueue.get(i).isStarted())
					{
						// start new games
						final Thread T = new Thread(gamesQueue.get(i));
						T.start();
					}
					
					// Pause one second between games starting to reduce OlympiadManager shout spam.
					try
					{
						wait(1000);
					}
					catch (final InterruptedException e)
					{
						// return;
					}
				}
			}
			
			// wait 30 sec for !stress the server
			try
			{
				wait(30000);
			}
			catch (final InterruptedException e)
			{
				// return;
			}
		}
		
		// when comp time finish wait for all games terminated before execute
		// the cleanup code
		boolean allGamesTerminated = false;
		// wait for all games terminated
		while (!allGamesTerminated)
		{
			try
			{
				wait(30000);
			}
			catch (final InterruptedException e)
			{
			}
			
			if (gamesQueue.isEmpty())
			{
				allGamesTerminated = true;
			}
			else
			{
				for (final OlympiadGameTask game : gamesQueue.values())
				{
					allGamesTerminated = allGamesTerminated || game.isTerminated();
				}
			}
		}
		// when all games terminated clear all
		gamesQueue.clear();
		olympiadInstances.clear();
		Olympiad.clearRegistered();
		
		OlympiadGame.battleStarted = false;
	}
	
	protected OlympiadGame getOlympiadGame(final int index)
	{
		if (olympiadInstances != null && !olympiadInstances.isEmpty())
		{
			return olympiadInstances.get(index);
		}
		return null;
	}
	
	protected void removeGame(final OlympiadGame game)
	{
		if (olympiadInstances != null && !olympiadInstances.isEmpty())
		{
			for (int i = 0; i < olympiadInstances.size(); i++)
			{
				if (olympiadInstances.get(i) == game)
				{
					olympiadInstances.remove(i);
				}
			}
		}
	}
	
	protected Map<Integer, OlympiadGame> getOlympiadGames()
	{
		return olympiadInstances;
	}
	
	protected L2FastList<L2PcInstance> getRandomClassList(final Map<Integer, L2FastList<L2PcInstance>> list, final L2FastList<Integer> classList)
	{
		if (list == null || classList == null || list.isEmpty() || classList.isEmpty())
		{
			return null;
		}
		
		return list.get(classList.get(Rnd.nextInt(classList.size())));
	}
	
	protected L2FastList<L2PcInstance> nextOpponents(final L2FastList<L2PcInstance> list)
	{
		final L2FastList<L2PcInstance> opponents = new L2FastList<>();
		if (list.isEmpty())
		{
			return opponents;
		}
		final int loopCount = (list.size() / 2);
		
		int first;
		int second;
		
		if (loopCount < 1)
		{
			return opponents;
		}
		
		first = Rnd.nextInt(list.size());
		opponents.add(list.get(first));
		list.remove(first);
		
		second = Rnd.nextInt(list.size());
		opponents.add(list.get(second));
		list.remove(second);
		
		return opponents;
		
	}
	
	protected boolean existNextOpponents(final L2FastList<L2PcInstance> list)
	{
		if (list == null)
		{
			return false;
		}
		if (list.isEmpty())
		{
			return false;
		}
		final int loopCount = list.size() >> 1;
		if (loopCount < 1)
		{
			return false;
		}
		
		return true;
	}
	
	protected Map<Integer, String> getAllTitles()
	{
		Map<Integer, String> titles = new HashMap<>();
		
		for (final OlympiadGame instance : olympiadInstances.values())
		{
			if (instance.gamestarted != true)
			{
				continue;
			}
			
			titles.put(instance.stadiumID, instance.getTitle());
		}
		
		return titles;
	}
	
	private static class SingletonHolder
	{
		protected static final OlympiadManager instance = new OlympiadManager();
	}
}
