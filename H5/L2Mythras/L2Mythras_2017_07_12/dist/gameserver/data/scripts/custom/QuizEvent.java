package custom;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import javolution.util.FastMap;
import l2f.commons.util.Rnd;
import l2f.gameserver.Announcements;
import l2f.gameserver.Config;
import l2f.gameserver.ThreadPoolManager;
import l2f.gameserver.model.Player;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * @author Grivesky
 * @date 07.05.2015
 * @project_name l2mythras
 */
public class QuizEvent
{
	public static boolean _quizRunning;
	private static String _question;
	private static String _answer1;
	private static String _answer2;
	private static String _answer3;
	private static int _rightanswer;
	private static FastMap<Player, Integer> _players;
	private static int _status;
	private static int announced;
	private static AutoEventTask _task;
	private static String[][] _questions;
	private static int i = 0;
	private static final int STATUS_NOT_IN_PROGRESS = 0;
	private static final int STATUS_ASK = 1;
	private static final int STATUS_ANSWER = 2;
	private static final int STATUS_END = 3;

	// ----------------------------------------------------------------------------
	// ------------------------------ CONFIG
	// --------------------------------------
	// ----------------------------------------------------------------------------

	// Number of questions per event
	private static int _questionNumber = 3;

	// The Item ID of the reward
	private static int _rewardID = 9627;

	// The ammount of the reward
	private static int _rewardCount = 1;

	// Wait for the first event after the server start (in seconds) 1200
	private static int _initWait = 1800;

	// Time for answer the question (in seconds)
	private static int _answerTime = 10;

	// Time between two event (in seconds) 7200
	private static int _betweenTime = 7200;

	public QuizEvent()
	{
		_status = STATUS_NOT_IN_PROGRESS;
		_task = new AutoEventTask();
		announced = 0;
		_quizRunning = false;
		_question = "";
		_answer1 = "";
		_answer2 = "";
		_answer3 = "";
		_rightanswer = 0;
		_players = new FastMap<Player, Integer>(100);
		_questions = new String[93][];
		includeQuestions();
		ThreadPoolManager.getInstance().schedule(_task, _initWait * 1000);

	}

	private void includeQuestions()
	{

		File questionFile = new File(Config.DATAPACK_ROOT, "data/scripts/custom/QuizEvent.xml");
		Document doc = null;
		try
		{
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setIgnoringComments(true);
			dbf.setValidating(false);
			DocumentBuilder db = dbf.newDocumentBuilder();
			doc = db.parse(questionFile);

			for (Node root = doc.getFirstChild(); root != null; root = root.getNextSibling())
			{
				if ("list".equalsIgnoreCase(root.getNodeName()))
				{

					for (Node child = root.getFirstChild(); child != null; child = child.getNextSibling())
					{

						if ("question".equalsIgnoreCase(child.getNodeName()))
						{
							int id, correct;
							String ask, answer1, answer2, answer3;
							NamedNodeMap attrs = child.getAttributes();

							id = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
							correct = Integer.parseInt(attrs.getNamedItem("correct").getNodeValue());
							ask = attrs.getNamedItem("ask").getNodeValue();
							answer1 = attrs.getNamedItem("answer1").getNodeValue();
							answer2 = attrs.getNamedItem("answer2").getNodeValue();
							answer3 = attrs.getNamedItem("answer3").getNodeValue();

							_questions[id] = new String[]
							{ ask, answer1, answer2, answer3, "" + correct };
							i++;

						}
					}
				}
			}
		} catch (Exception e)
		{

		}
	}

	private class AutoEventTask implements Runnable
	{
		@Override
		public void run()
		{
			switch (_status)
			{
				case STATUS_NOT_IN_PROGRESS:
					announceStart();
					break;
				case STATUS_ASK:
					if (announced < _questionNumber)
					{
						announceQuestion();
					} else
					{
						_status = STATUS_END;
						ThreadPoolManager.getInstance().schedule(_task, 3000);
					}
					break;
				case STATUS_ANSWER:
					announceCorrect();
					break;
				case STATUS_END:
					endEvent();
					break;
				default:
					break;

			}
		}
	}

	// Get a random question from the quiz_event table
	private static void selectQuestion()
	{
		int id = Rnd.get(i) + 1;
		_question = _questions[id][0];
		_answer1 = _questions[id][1];
		_answer2 = _questions[id][2];
		_answer3 = _questions[id][3];
		_rightanswer = Integer.parseInt("" + _questions[id][4]);
	}

	// Announce the question
	private static void announceQuestion()
	{
		selectQuestion();
		Announcements.getInstance().announceToAll("-----------------");
		Announcements.getInstance().announceToAll("Question: " + _question);
		Announcements.getInstance().announceToAll("-----------------");
		Announcements.getInstance().announceToAll("1: " + _answer1);
		Announcements.getInstance().announceToAll("2: " + _answer2);
		Announcements.getInstance().announceToAll("3: " + _answer3);
		Announcements.getInstance().announceToAll("-----------------");

		_status = STATUS_ANSWER;
		ThreadPoolManager.getInstance().schedule(_task, _answerTime * 1000);
	}

	// Announce the correct answer
	private static void announceCorrect()
	{
		Announcements.getInstance().announceToAll("-----------------");
		Announcements.getInstance().announceToAll("The correct answer was: " + _rightanswer);
		Announcements.getInstance().announceToAll("-----------------");
		announced++;
		giveReward();
		_status = STATUS_ASK;
		ThreadPoolManager.getInstance().schedule(_task, 5000);
	}

	private static void announceStart()
	{
		_quizRunning = true;
		_players.clear();
		Announcements.getInstance().announceToAll("Quiz Event begins! " + _questionNumber + " questions. " + _answerTime + " secs for answer each. ");
		Announcements.getInstance().announceToAll("Type . and the number of the correct answer to the chat. (Like: .1)");
		Announcements.getInstance().announceToAll("Get Ready! L2Mythras is ready to reward you!");

		_status = STATUS_ASK;
		ThreadPoolManager.getInstance().schedule(_task, 5000);
	}

	// Add a player and its answer
	public static void setAnswer(Player player, int answer)
	{
		if (_players.containsKey(player))
			player.sendMessage("You already choosen an aswer!: " + _players.get(player));
		else
			_players.put(player, answer);
	}

	private static void endEvent()
	{
		_quizRunning = false;
		Announcements.getInstance().announceToAll("The Quiz Event is over! Play with us! We <3 L2Mythras");
		announced = 0;
		_status = STATUS_NOT_IN_PROGRESS;
		ThreadPoolManager.getInstance().schedule(_task, _betweenTime * 1000);
	}

	private static void giveReward()
	{
		for (Player p : _players.keySet())
		{
			if (_players.get(p) == _rightanswer)
			{
				p.sendMessage("Your answer was correct! L2Mythras Reward you with 1 GCM!");
				// p.getInventory().addItem(_rewardID, _rewardCount);
				p.getInventory().addItem(_rewardID, _rewardCount, null);
			} else
			{
				p.sendMessage("Your answer was not correct!");
			}

		}
		_players.clear();
	}
}