import sys
from com.l2jfrozen.gameserver.model.quest import State
from com.l2jfrozen.gameserver.model.quest import QuestState
from com.l2jfrozen.gameserver.model.quest.jython import QuestJython as JQuest
from com.l2jfrozen.gameserver.datatables import SkillTable
from java.lang import System

EGG = 18344

class AncientEgg(JQuest) :

 def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)

 def onAttack (self,npc,player,damage,isPet):
   player.setTarget(player)
   player.doCast(SkillTable.getInstance().getInfo(5088,1))
   return

QUEST = AncientEgg(-1, "ancientegg", "ai")
CREATED = State('Start', QUEST)
QUEST.setInitialState(CREATED)

QUEST.addAttackId(EGG)
