package com.l2jfrozen.gameserver.model.entity.event;

import java.util.Random;
import java.util.concurrent.ScheduledFuture;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.sql.ItemTable;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.entity.Announcements;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.clientpackets.Say2;
import com.l2jfrozen.gameserver.network.serverpackets.CreatureSay;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;

/**
 * @author Darki699
 */
public class ChristmasPresents
{
	private static final Logger LOGGER = Logger.getLogger(ChristmasPresents.class);
	protected Random rand = new Random();
	
	// Santa Trainees
	// NPC Ids: 31863 , 31864
	
	// The message task sent at fixed rate
	private ScheduledFuture<?> xMasTask = null;
	// Interval of Christmas actions, value in minutes
	private long interval = 120; // 120 minutes = 2 hours
	private boolean running = false;
	
	private String[] message =
	{
		"Ho Ho Ho... Merry Christmas!",
		"God is Love...",
		"Christmas is all about love...",
		"Christmas is thus about God and Love...",
		"Love is the key to peace among all Lineage creature kind..",
		"Love is the key to peace and happiness within all creation...",
		"Love needs to be practiced - Love needs to flow - Love needs to make happy...",
		"Love starts with your partner, children and family and expands to all world.",
		"God bless all kind.",
		"God bless Lineage.",
		"Forgive all.",
		"Ask for forgiveness even from all the \"past away\" ones.",
		"Give love in many different ways to your family members, relatives, neighbors and \"foreigners\".",
		"Enhance the feeling within yourself of being a member of a far larger family than your physical family",
		"MOST important - Christmas is a feast of BLISS given to YOU from God and all beloved ones back home in God !!",
		"Open yourself for all divine bliss, forgiveness and divine help that is offered TO YOU by many others AND GOD.",
		"Take it easy. Relax these coming days.",
		"Every day is Christmas day - it is UP TO YOU to create the proper inner attitude and opening for love toward others AND from others within YOUR SELF !",
		"Peace and Silence. Reduced activities. More time for your most direct families. If possible NO other dates or travel may help you most to actually RECEIVE all offered bliss.",
		"What ever is offered to you from God either enters YOUR heart and soul or is LOST for GOOD !!! or at least until another such day - next year Christmas or so !!",
		"Divine bliss and love NEVER can be stored and received later.",
		"There is year round a huge quantity of love and bliss available from God and your Guru and other loving souls, but Christmas days are an extended period FOR ALL PLANET",
		"Please open your heart and accept all love and bliss - For your benefit as well as for the benefit of all your beloved ones.",
		"Beloved children of God",
		"Beyond Christmas days and beyond Christmas season - The Christmas love lives on, the Christmas bliss goes on, the Christmas feeling expands.",
		"The holy spirit of Christmas is the holy spirit of God and God's love for all days.",
		"When the Christmas spirit lives on and on...",
		"When the power of love created during the pre-Christmas days is kept alive and growing.",
		"Peace among all mankind is growing as well =)",
		"The holy gift of love is an eternal gift of love put into your heart like a seed.",
		"Dozens of millions of humans worldwide are changing in their hearts during weeks of pre-Christmas time and find their peak power of love on Christmas nights and Christmas days.",
		"What is special during these days, to give all of you this very special power of love, the power to forgive, the power to make others happy, power to join the loved one on his or her path of loving life.",
		"It only is your now decision that makes the difference !",
		"It only is your now focus in life that makes all the changes. It is your shift from purely worldly matters toward the power of love from God that dwells within all of us that gave you the power to change your own behavior from your normal year long behavior.",
		"The decision of love, peace and happiness is the right one.",
		"Whatever you focus on is filling your mind and subsequently filling your heart.",
		"No one else but you have change your focus these past Christmas days and the days of love you may have experienced in earlier Christmas seasons.",
		"God's love is always present.",
		"God's Love has always been in same power and purity and quantity available to all of you.",
		"Expand the spirit of Christmas love and Christmas joy to span all year of your life...",
		"Do all year long what is creating this special Christmas feeling of love joy and happiness.",
		"Expand the true Christmas feeling, expand the love you have ever given at your maximum power of love days ... ",
		"Expand the power of love over more and more days.",
		"Re-focus on what has brought your love to its peak power and refocus on those objects and actions in your focus of mind and actions.",
		"Remember the people and surrounding you had when feeling most happy, most loved, most magic",
		"People of true loving spirit - who all was present, recall their names, recall the one who may have had the greatest impact in love those hours of magic moments of love...",
		"The decoration of your surrounding - Decoration may help to focus on love - Or lack of decoration may make you drift away into darkness or business - away from love...",
		"Love songs, songs full of living joy - any of the thousands of true touching love songs and happy songs do contribute to the establishment of an inner attitude perceptible of love.",
		"Songs can fine tune and open our heart for love from God and our loved ones.",
		"Your power of will and focus of mind can keep Christmas Love and Christmas joy alive beyond Christmas season for eternity",
		"Enjoy your love for ever!",
		"Christmas can be every day - As soon as you truly love every day =)",
		"Christmas is when you love all and are loved by all.",
		"Christmas is when you are truly happy by creating true happiness in others with love from the bottom of your heart.",
		"Secret in God's creation is that no single person can truly love without ignition of his love.",
		"You need another person to love and to receive love, a person to truly fall in love to ignite your own divine fire of love. ",
		"God created many and all are made of love and all are made to love...",
		"The miracle of love only works if you want to become a fully loving member of the family of divine love.",
		"Once you have started to fall in love with the one God created for you - your entire eternal life will be a permanent fire of miracles of love ... Eternally !",
		"May all have a happy time on Christmas each year. Merry Christmas!",
		"Christmas day is a time for love. It is a time for showing our affection to our loved ones. It is all about love.",
		"Have a wonderful Christmas. May god bless our family. I love you all.",
		"Wish all living creatures a Happy X-mas and a Happy New Year! By the way I would like us to share a warm fellowship in all places.",
		"Just as animals need peace of mind, poeple and also trees need peace of mind. This is why I say, all creatures are waiting upon the Lord for their salvation. May God bless you all creatures in the whole world.",
		"Merry Xmas!",
		"May the grace of Our Mighty Father be with you all during this eve of Christmas. Have a blessed Christmas and a happy New Year.",
		"Merry Christmas my children. May this new year give all of the things you rightly deserve. And may peace finally be yours.",
		"I wish everybody a Merry Christmas! May the Holy Spirit be with you all the time.",
		"May you have the best of Christmas this year and all your dreams come true.",
		"May the miracle of Christmas fill your heart with warmth and love. Merry Christmas!"
	};
	
	private String[] sender =
	{
		"Santa Claus",
		"Papai Noel",
		"Shengdan Laoren",
		"Santa",
		"Viejo Pascuero",
		"Sinter Klaas",
		"Father Christmas",
		"Saint Nicholas",
		"Joulupukki",
		"Pere Noel",
		"Saint Nikolaus",
		"Kanakaloka",
		"De Kerstman",
		"Winter grandfather",
		"Babbo Natale",
		"Hoteiosho",
		"Kaledu Senelis",
		"Black Peter",
		"Kerstman",
		"Julenissen",
		"Swiety Mikolaj",
		"Ded Moroz",
		"Julenisse",
		"El Nino Jesus",
		"Jultomten",
		"Reindeer Dasher",
		"Reindeer Dancer",
		"Christmas Spirit",
		"Reindeer Prancer",
		"Reindeer Vixen",
		"Reindeer Comet",
		"Reindeer Cupid",
		"Reindeer Donner",
		"Reindeer Donder",
		"Reindeer Dunder",
		"Reindeer Blitzen",
		"Reindeer Bliksem",
		"Reindeer Blixem",
		"Reindeer Rudolf",
		"Christmas Elf"
	};
	
	// Presents List:
	protected int[] presents =
	{
		5560,
		5560,
		5560,
		5560,
		5560, /* x-mas tree */
		5560,
		5560,
		5560,
		5560,
		5560,
		5561,
		5561,
		5561,
		5561,
		5561, /* special x-mas tree */
		5562,
		5562,
		5562,
		5562, /* 1st Carol */
		5563,
		5563,
		5563,
		5563, /* 2nd Carol */
		5564,
		5564,
		5564,
		5564, /* 3rd Carol */
		5565,
		5565,
		5565,
		5565, /* 4th Carol */
		5566,
		5566,
		5566,
		5566, /* 5th Carol */
		5583,
		5583, /* 6th Carol */
		5584,
		5584, /* 7th Carol */
		5585,
		5585, /* 8th Carol */
		5586,
		5586, /* 9th Carol */
		5587,
		5587, /* 10th Carol */
		6403,
		6403,
		6403,
		6403, /* Star Shard */
		6403,
		6403,
		6403,
		6403,
		6406,
		6406,
		6406,
		6406, /* FireWorks */
		6407, /* Large FireWorks */
		6407,
		5555, /* Token of Love */
		7836, /* Santa Hat #1 */
		9138, /* Santa Hat #2 */
		8936, /* Santa's Antlers Hat */
		6394, /* Red Party Mask */
		5808 /* Black Party Mask */
	};
	
	public ChristmasPresents()
	{
	}
	
	/**
	 * @return an instance of <b>this</b> InstanceManager.
	 */
	public static ChristmasPresents getInstance()
	{
		return SingletonHolder.instance;
	}
	
	/**
	 * Start sending Christmas messages and Christmas presents to ONLINE players.
	 * @param activeChar
	 */
	public void init(L2PcInstance activeChar)
	{
		if (running)
		{
			activeChar.sendMessage("Christmast event already in progress");
			return;
		}
		
		Announcements.getInstance().announceToAll("Christmas Event has begun, have a Merry Christmas");
		LOGGER.info("ChristmasManager: Init ChristmasManager was started successfully, have a festive holiday.");
		
		// Tasks:
		startTask();
		
		running = true;
	}
	
	/**
	 * ends Christmas event
	 * @param activeChar
	 */
	public void end(L2PcInstance activeChar)
	{
		if (!running)
		{
			activeChar.sendMessage("Christmas event is not in progress");
			return;
		}
		
		Announcements.getInstance().announceToAll("Christmas Event has ended... Hope you enjoyed the festivities.");
		LOGGER.info("ChristmasManager:Terminated ChristmasManager.");
		
		endTask();
	}
	
	/**
	 * Returns a random name of the X-Mas message sender, sent to players
	 * @return String of the message sender's name
	 */
	
	private String getRandomSender()
	{
		return sender[rand.nextInt(sender.length)];
	}
	
	/**
	 * Returns a random X-Mas message String
	 * @return String containing the random message.
	 */
	private String getRandomXMasMessage()
	{
		return message[rand.nextInt(message.length)];
	}
	
	/**
	 * Returns a random X-Mas present
	 * @return item id.
	 */
	protected int getSantaRandomPresent()
	{
		return presents[rand.nextInt(presents.length)];
	}
	
	/**
	 * Starts X-Mas Santa messaged presents sent to all players, and initialize the thread.
	 */
	private void startTask()
	{
		xMasTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(() ->
		{
			try
			{
				for (L2PcInstance pc : L2World.getInstance().getAllPlayers())
				{
					if (pc == null)
					{
						continue;
					}
					else if (!pc.isOnline())
					{
						continue;
					}
					else if (pc.getInventoryLimit() <= pc.getInventory().getSize())
					{
						pc.sendMessage("Santa wanted to give you a Present but your inventory was full :(");
						continue;
					}
					
					int itemId = getSantaRandomPresent();
					
					pc.addItem("Christmas Event", itemId, 1, pc, false);
					String itemName = ItemTable.getInstance().getTemplate(itemId).getName();
					
					CreatureSay cs = new CreatureSay(0, Say2.PARTYROOM_COMMANDER, getRandomSender(), getRandomXMasMessage());
					pc.sendPacket(cs);
					
					SystemMessage sm = new SystemMessage(SystemMessageId.EARNED_ITEM);
					sm.addString(itemName + " from santa's present bag...");
					pc.broadcastPacket(sm);
				}
			}
			catch (final Throwable t)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					t.printStackTrace();
				}
			}
		}, 1000, interval * 60000);
	}
	
	private void endTask()
	{
		if (xMasTask != null)
		{
			xMasTask.cancel(true);
			xMasTask = null;
		}
	}
	
	private static class SingletonHolder
	{
		protected static final ChristmasPresents instance = new ChristmasPresents();
	}
}
