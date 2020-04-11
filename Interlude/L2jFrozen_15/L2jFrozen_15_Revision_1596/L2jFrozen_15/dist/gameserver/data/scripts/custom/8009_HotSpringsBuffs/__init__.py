import sys
from com.l2jfrozen.gameserver.model.quest import State
from com.l2jfrozen.gameserver.model.quest import QuestState
from com.l2jfrozen.gameserver.model.quest.jython import QuestJython as JQuest
from com.l2jfrozen.gameserver.datatables import SkillTable
from com.l2jfrozen.gameserver.model import L2Effect
from com.l2jfrozen.util.random import Rnd
	
qn = "8009_HotSpringsBuffs"
	
#print "HotSpringsBuffs"
	
HSMOBS = [21316, 21321, 21314, 21319]
	
class Quest (JQuest) :
	
 def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)
        
 def onAttack (self,npc,player,damage,isPet):
    npcId = npc.getNpcId()
    if npcId in HSMOBS:
      if (Rnd.get(2) == 1):
        if (Rnd.get(2) == 1):
          if player.getFirstEffect(int(4552)):
            holera = player.getFirstEffect(int(4552)).getLevel()
            if (Rnd.get(100) < 30):
              if holera < 10:
                newholera = int(holera + 1)
                npc.setTarget(player)
                npc.doCast(SkillTable.getInstance().getInfo(4552,newholera))
          else:
            npc.setTarget(player)
            npc.doCast(SkillTable.getInstance().getInfo(4552,1))
        else:
          if player.getFirstEffect(int(4554)):
            malaria = player.getFirstEffect(int(4554)).getLevel()
            if (Rnd.get(100) < 15):
              if malaria < 10:
                newmalaria = int(malaria + 1)
                npc.setTarget(player)
                npc.doCast(SkillTable.getInstance().getInfo(4554,newmalaria))
          else:
            npc.setTarget(player)
            npc.doCast(SkillTable.getInstance().getInfo(4554,1))
      
    return 
        
QUEST       = Quest(8009,qn,"custom")

for i in HSMOBS: 
  QUEST.addAttackId(i)