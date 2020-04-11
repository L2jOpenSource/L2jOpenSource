package handler.voicecommands;

import l2f.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2f.gameserver.handler.voicecommands.VoicedCommandHandler;
import l2f.gameserver.model.Player;
import l2f.gameserver.scripts.ScriptFile;
import custom.QuizEvent;

/**
 * @author Ionescu Leontin-Ovidiu
 * @date 07.05.2013
 * @project_name l2jeuropa
 */
public class Quiz implements IVoicedCommandHandler, ScriptFile {
	private static final String[] _voicedCommands = { "quiz", "1", "2", "3" };

	/**
	 * 
	 * @see com.l2jserver.gameserver.handler.IVoicedCommandHandler#getVoicedCommandList()
	 */
	@Override
	public String[] getVoicedCommandList() {
		return _voicedCommands;
	}

	@Override
	public boolean useVoicedCommand(String command, Player activeChar,
			String target) {

		if (command.equalsIgnoreCase("1") && QuizEvent._quizRunning) {
			QuizEvent.setAnswer(activeChar, 1);
		}

		if (command.equalsIgnoreCase("2") && QuizEvent._quizRunning) {
			QuizEvent.setAnswer(activeChar, 2);
		}

		if (command.equalsIgnoreCase("3") && QuizEvent._quizRunning) {
			QuizEvent.setAnswer(activeChar, 3);
		}
		return true;
	}

	@Override
	public void onLoad() {
		System.out.println("Loading Quiz.java");
		VoicedCommandHandler.getInstance().registerVoicedCommandHandler(this);
		new QuizEvent();
	}

	@Override
	public void onReload() {
	}

	@Override
	public void onShutdown() {
		// TODO Auto-generated method stub

	}
}