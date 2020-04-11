import sys
from com.l2jfrozen.gameserver.ai import CtrlIntention
from com.l2jfrozen.gameserver.datatables.csv import DoorTable
from com.l2jfrozen.gameserver.managers import CastleManager
from com.l2jfrozen.gameserver.managers import GrandBossManager
from com.l2jfrozen.gameserver.model.actor.position import L2CharPosition
from com.l2jfrozen.gameserver.model.quest import QuestState
from com.l2jfrozen.gameserver.model.quest import State
from com.l2jfrozen.gameserver.model.quest.jython import QuestJython as JQuest
from com.l2jfrozen.gameserver.network.serverpackets import CreatureSay
from com.l2jfrozen.gameserver.network.serverpackets import SocialAction
from com.l2jfrozen.gameserver.network.serverpackets import SpecialCamera
from com.l2jfrozen.util.random import Rnd
from java.lang import System

Benom = 29054
BenomTeleport = 13101

BenomSpeak = [ "You should have finished me when you had the chance!!!", "I will crush all of you!!!", "I am not finished here, come face me!!!", "You cowards!!! I will torture each and everyone of you!!!" ]

WalkTimes = [ 18000,17000,4500,16000,22000,14000,10500,14000,9500,12500,20500,14500,17000,20000,22000,11000,11000,20000,8000,5500,20000,18000,25000,28000,25000,25000,25000,25000,10000,24000,7000,12000,20000 ]

# Benom status tracking
LIVE = 0
DEAD = 1

benomWalkRoutes = {
  0:  [ 12565, -49739, -547 ],
  1:  [ 11242, -49689, -33 ],
  2:  [ 10751, -49702, 83 ],
  3:  [ 10824, -50808, 316 ],
  4:  [ 9084,  -50786, 972 ],
  5:  [ 9095,  -49787, 1252 ],
  6:  [ 8371,  -49711, 1252 ],
  7:  [ 8423,  -48545, 1252 ],
  8:  [ 9105,  -48474, 1252 ],
  9:  [ 9085,  -47488, 972 ],
  10: [ 10858, -47527, 316 ],
  11: [ 10842, -48626, 75 ],
  12: [ 12171, -48464, -547 ],
  13: [ 13565, -49145, -535 ],
  14: [ 15653, -49159, -1059 ],
  15: [ 15423, -48402, -839 ],
  16: [ 15066, -47438, -419 ],
  17: [ 13990, -46843, -292 ],
  18: [ 13685, -47371, -163 ],
  19: [ 13384, -47470, -163 ],
  20: [ 14609, -48608, 346 ],
  21: [ 13878, -47449, 747 ],
  22: [ 12894, -49109, 980 ],
  23: [ 10135, -49150, 996 ],
  24: [ 12894, -49109, 980 ],
  25: [ 13738, -50894, 747 ],
  26: [ 14579, -49698, 347 ],
  27: [ 12896, -51135, -166 ],
  28: [ 12971, -52046, -292, ],
  29: [ 15140, -50781, -442, ],
  30: [ 15328, -50406, -603 ],
  31: [ 15594, -49192, -1059 ],
  32: [ 13175, -49153, -537 ],
}

class benom (JQuest):

  def __init__(self, id, name, descr) :
    JQuest.__init__(self, id, name, descr)
    self.Teleport = []
    self.Benomm = []
    castleOwner = CastleManager.getInstance().getCastleById(8).getOwnerId()
    siegeDate = CastleManager.getInstance().getCastleById(8).getSiegeDate().getTimeInMillis()
    benomTeleporterSpawn = (siegeDate - System.currentTimeMillis()) - 86400000
    benomRaidRoomSpawn = (siegeDate - System.currentTimeMillis()) - 86400000
    benomRaidSiegeSpawn = (siegeDate - System.currentTimeMillis())
    if benomTeleporterSpawn < 0 :
      benomTeleporterSpawn = 1
    if benomRaidSiegeSpawn < 0 :
      benomRaidSiegeSpawn = 1
    self.BenomWalkRouteStep = 0
    self.BenomIsSpawned = 0
    if castleOwner > 0 :
      if benomTeleporterSpawn >= 1 :
        self.startQuestTimer("BenomTeleSpawn", benomTeleporterSpawn, None, None)
      if (siegeDate - System.currentTimeMillis()) > 0 :
        self.startQuestTimer("BenomRaidRoomSpawn", benomRaidRoomSpawn, None, None)
      self.startQuestTimer("BenomRaidSiegeSpawn", benomRaidSiegeSpawn, None, None)
    self.Benom = Benom

  def onTalk(self, npc, player) :
    npcId = npc.getNpcId()
    castleOwner = CastleManager.getInstance().getCastleById(8).getOwnerId()
    clanId = player.getClanId()
    if castleOwner and clanId :
      if castleOwner == clanId :
        X = 12558 + (Rnd.get(200) - 100)
        Y = -49279 + (Rnd.get(200) - 100)
        player.teleToLocation(X, Y, -3007)
        return
      else :
        htmltext = "<html><body>Benom's Avatar:<br>Your clan does not own this castle. Only members of this Castle's owning clan can challenge Benom.</body></html>"
    else :
      htmltext = "<html><body>Benom's Avatar:<br>Your clan does not own this castle. Only members of this Castle's owning clan can challenge Benom.</body></html>"
    return htmltext

  def onAdvEvent(self, event, npc, player) :
    status = GrandBossManager.getInstance().getBossStatus(Benom)
    if event == "BenomTeleSpawn" :
      self.Teleport = self.addSpawn(BenomTeleport, 11013, -49629, -547, 13400, False, 0)
    elif event == "BenomRaidRoomSpawn" :
      if self.BenomIsSpawned == 0 and status == 0 :
        self.Benomm = self.addSpawn(Benom, 12047, -49211, -3009, 0, False, 0)
        self.BenomIsSpawned = 1
    elif event == "BenomRaidSiegeSpawn" :
      if status == 0 :
        if self.BenomIsSpawned == 0 :
          self.Benomm = self.addSpawn(Benom, 11025, -49152, -537, 0, False, 0)
          self.BenomIsSpawned = 1
        elif self.BenomIsSpawned == 1 :
          self.Benomm.teleToLocation(11025, -49152, -537)
        self.startQuestTimer("BenomSpawnEffect", 100, None, None)
        self.startQuestTimer("BenomBossDespawn", 5400000, None, None)
        self.Teleport.deleteMe()
    elif event == "BenomSpawnEffect" :
      self.Benomm.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE)
      self.Benomm.broadcastPacket(SpecialCamera(self.Benomm.getObjectId(), 200, 0, 150, 0, 5000))
      self.Benomm.broadcastPacket(SocialAction(self.Benomm.getObjectId(), 3))
      self.startQuestTimer("BenomWalk", 5000, self.Benomm, None)
      self.BenomWalkRouteStep = 0
    elif event == "Attacking" :
      NumPlayers = []
      for player in npc.getKnownList().getKnownPlayers().values() :
        NumPlayers.append(player)
      if len(NumPlayers) > 0 :
        target = NumPlayers[Rnd.get(len(NumPlayers))]
        npc.addDamageHate(target, 0, 999)
        npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target)
        self.startQuestTimer("Attacking", 2000, npc, player)
      elif len(NumPlayers) == 0 :
          self.startQuestTimer("BenomWalkFinish", 2000, npc, None)
    elif event == "BenomWalkFinish" :
      if npc.getCastle().getSiege().getIsInProgress() :
        self.cancelQuestTimer("Attacking", npc, player)
        X = benomWalkRoutes[self.BenomWalkRouteStep][0]
        Y = benomWalkRoutes[self.BenomWalkRouteStep][1]
        Z = benomWalkRoutes[self.BenomWalkRouteStep][2]
        npc.teleToLocation(X, Y, Z)
        npc.setWalking()
        self.BenomWalkRouteStep = 0
        self.startQuestTimer("BenomWalk", 2200, npc, None)
    elif event == "BenomWalk" :
      if self.BenomWalkRouteStep == 33 :
        self.BenomWalkRouteStep = 0
        self.startQuestTimer("BenomWalk", 100, npc, None)
      else :
        self.startQuestTimer("Talk", 100, npc, None)
        if self.BenomWalkRouteStep == 14 :
          self.startQuestTimer("DoorOpen", 15000, None, None)
          self.startQuestTimer("DoorClose", 23000, None, None)
        if self.BenomWalkRouteStep == 32 :
          self.startQuestTimer("DoorOpen", 500, None, None)
          self.startQuestTimer("DoorClose", 4000, None, None)
        Time = WalkTimes[self.BenomWalkRouteStep]
        npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE)
        X = benomWalkRoutes[self.BenomWalkRouteStep][0]
        Y = benomWalkRoutes[self.BenomWalkRouteStep][1]
        Z = benomWalkRoutes[self.BenomWalkRouteStep][2]
        npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, L2CharPosition(X, Y, Z, 0))
        self.BenomWalkRouteStep = int(self.BenomWalkRouteStep) + 1
        self.startQuestTimer("BenomWalk", Time, npc, None)
    elif event == "DoorOpen" :
      DoorTable.getInstance().getDoor(20160005).openMe()
    elif event == "DoorClose" :
      DoorTable.getInstance().getDoor(20160005).closeMe()
    elif event == "Talk" :
      if Rnd.get(100) < 40 :
        npc.broadcastPacket(CreatureSay(npc.getObjectId(), 0, "Benom", BenomSpeak[Rnd.get(4)]))
    elif event == "BenomBossDespawn" :
      GrandBossManager.getInstance().setBossStatus(Benom,LIVE)
      self.BenomIsSpawned = 0
      self.Benomm.deleteMe()
    return

  def onAttack (self,npc,player,damage,isPet) :
    self.cancelQuestTimer("BenomWalk", npc, None)
    self.cancelQuestTimer("BenomWalkFinish", npc, None)
    self.startQuestTimer("Attacking", 100, npc, player)
    return

  def onKill(self, npc, player, isPet) :
    GrandBossManager.getInstance().setBossStatus(Benom,DEAD)
    self.cancelQuestTimer("BenomWalk", npc, None)
    self.cancelQuestTimer("BenomWalkFinish", npc, None)
    self.cancelQuestTimer("BenomBossDespawn", npc, None)
    self.cancelQuestTimer("Talk", npc, None)
    self.cancelQuestTimer("Attacking", npc, None)
    return

QUEST = benom(-1, "benom", "ai")
CREATED = State('Start', QUEST)

# Quest initialization
QUEST.setInitialState(CREATED)

QUEST.addStartNpc(BenomTeleport)
QUEST.addTalkId(BenomTeleport)
QUEST.addAttackId(Benom)
QUEST.addKillId(Benom)