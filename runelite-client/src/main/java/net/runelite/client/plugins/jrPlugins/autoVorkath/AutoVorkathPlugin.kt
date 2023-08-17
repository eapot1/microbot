package net.runelite.client.plugins.jrPlugins.autoVorkath

import com.google.inject.Provides
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.runelite.api.*
import net.runelite.api.coords.LocalPoint
import net.runelite.api.coords.WorldPoint
import net.runelite.api.widgets.Widget
import net.runelite.client.callback.ClientThread
import net.runelite.client.config.ConfigManager
import net.runelite.client.plugins.Plugin
import net.runelite.client.plugins.PluginDescriptor
import net.runelite.client.plugins.microbot.Microbot
import net.runelite.client.plugins.microbot.Script
import net.runelite.client.plugins.microbot.util.Global.sleep
import net.runelite.client.plugins.microbot.util.Global.sleepUntil
import net.runelite.client.plugins.microbot.util.MicrobotInventorySetup
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject
import net.runelite.client.plugins.microbot.util.inventory.Inventory
import net.runelite.client.plugins.microbot.util.mouse.VirtualMouse
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc
import net.runelite.client.plugins.microbot.util.player.Rs2Player
import net.runelite.client.plugins.microbot.util.poh.Rs2POH
import net.runelite.client.plugins.microbot.util.prayer.Prayer
import net.runelite.client.plugins.microbot.util.prayer.Rs2Prayer
import net.runelite.client.plugins.microbot.util.walker.Walker
import net.runelite.client.ui.overlay.OverlayManager
import javax.inject.Inject


@PluginDescriptor(
    name = "Auto Vorkath",
    description = "JR - Auto vorkath",
    tags = ["vorkath", "microbot", "auto", "auto prayer"],
    enabledByDefault = false
)
class AutoVorkathPlugin : Plugin() {
    @Inject
    private lateinit var client: Client

    @Inject
    private lateinit var clientThread: ClientThread

    @Inject
    private lateinit var config: AutoVorkathConfig

    @Inject
    private lateinit var overlayManager: OverlayManager

    @Inject
    private lateinit var autoVorkathOverlay: AutoVorkathOverlay

    @Provides
    fun getConfig(configManager: ConfigManager): AutoVorkathConfig {
        return configManager.getConfig(AutoVorkathConfig::class.java)
    }

    var botState: State? = null

    private var previousBotState: State? = null
    private var running = false
    private val rangeProjectileId = 1477
    private val magicProjectileId = 393
    private val purpleProjectileId = 1471
    private val blueProjectileId = 1479
    private val whiteProjectileId = 395
    private val redProjectileId = 1481
    private val acidProjectileId = 1483
    private val acidRedProjectileId = 1482

    private lateinit var centerTile: WorldPoint
    private lateinit var rightTile: WorldPoint
    private lateinit var leftTile: WorldPoint

    private var foods: Array<Widget>? = null

    enum class State {
        RANGE,
        ZOMBIFIED_SPAWN,
        RED_BALL,
        EAT,
        PRAYER,
        RANGE_POTION,
        ANTIFIRE_POTION,
        ANTI_VENOM_POTION,
        ACID,
        BANK,
        WALK_TO_VORKATH,
        POKE_VORKATH,
        NONE
    }

    override fun startUp() {
        botState = State.BANK
        previousBotState = State.NONE
        running = if (Microbot.isLoggedIn()) true else false
        overlayManager.add(autoVorkathOverlay)
        GlobalScope.launch {
            run()
            return@launch
        }
    }

    override fun shutDown() {
        println("Done")
        running = false
        botState = null
        previousBotState = null
    }

    private fun run() {
        while (running) {
            val sleepyVorkath = Rs2Npc.getNpc(intArrayOf(NpcID.VORKATH_8058, NpcID.VORKATH_8059))
            val vorkath = Rs2Npc.getNpc(NpcID.VORKATH_8061)
            // Check if player is in Vorkath Area
            if (sleepyVorkath == null && vorkath == null && botState != State.WALK_TO_VORKATH) {
                botState = State.BANK
            }
            if (sleepyVorkath != null) {
                // Check if player needs to drink range potion
                if (!Rs2Player.hasDivineRangedActive()) {
                    botState = State.RANGE_POTION
                }

                // Check if player needs to drink antifire potion
                //Antifire checks for super anti fire & normal anti fire
                if (!Rs2Player.hasAntiFireActive()) {
                    botState = State.ANTIFIRE_POTION
                }

                // Check if player needs to drink antifire potion
                if (!Rs2Player.hasAntiVenomActive()) {
                    botState = State.ANTI_VENOM_POTION
                }

                if (Rs2Player.hasAntiFireActive() && Rs2Player.hasAntiVenomActive() && Rs2Player.hasDivineRangedActive()
                    && client.localPlayer.localLocation.equals(LocalPoint(6208, 6976))
                ) {
                    botState = State.POKE_VORKATH
                }
            } else if (vorkath != null) {
                if (vorkath.isDead) {
                    botState = State.BANK
                } else if (vorkath.isInteracting) {
                    Script.toggleRunEnergy(false)
                    centerTile =
                        WorldPoint(
                            vorkath.worldLocation.x + 3,
                            vorkath.worldLocation.y - 5,
                            vorkath.worldLocation.plane
                        )
                    rightTile = WorldPoint(centerTile.x + 2, centerTile.y - 3, centerTile.plane)
                    leftTile = WorldPoint(centerTile.x - 2, centerTile.y - 3, centerTile.plane)
                    // Check what projectile is coming
                    if (doesProjectileExistById(redProjectileId)) {
                        botState = State.RED_BALL
                    } else if (doesProjectileExistById(acidProjectileId) || doesProjectileExistById(acidRedProjectileId)) {
                        botState = State.ACID
                        //println("Acid")
                    } else if (doesProjectileExistById(rangeProjectileId) || doesProjectileExistById(magicProjectileId) || doesProjectileExistById(
                            purpleProjectileId
                        ) || doesProjectileExistById(blueProjectileId)
                    ) {
                        botState = State.RANGE
                    } else if (doesProjectileExistById(whiteProjectileId) || Rs2Npc.getNpc("Zombified Spawn") != null) {
                        botState = State.ZOMBIFIED_SPAWN
                    } else if (doesProjectileExistById(redProjectileId)) {
                        botState = State.RED_BALL
                    }

                    // Check if player needs to eat
                    if (clientThread.runOnClientThread { client.getBoostedSkillLevel(Skill.HITPOINTS) } < config.MINHEALTH() && botState != State.ACID && botState != State.RED_BALL) {
                        foods = clientThread.runOnClientThread { Inventory.getInventoryFood() }
                        botState = State.EAT
                    }

                    // Check if player needs to drink prayer potion
                    if (clientThread.runOnClientThread { client.getBoostedSkillLevel(Skill.PRAYER) } < config.MINPRAY() && botState != State.ACID && botState != State.RED_BALL) {
                        botState = State.PRAYER
                    }

                    // Check if player needs to drink range potion
                    if (!Rs2Player.hasDivineRangedActive()) {
                        botState = State.RANGE_POTION
                    }

                    // Check if player needs to drink antifire potion
                    //Antifire checks for super anti fire & normal anti fire
                    if (!Rs2Player.hasAntiFireActive()) {
                        botState = State.ANTIFIRE_POTION
                    }

                    // Check if player needs to drink antifire potion
                    if (!Rs2Player.hasAntiVenomActive()) {
                        botState = State.ANTI_VENOM_POTION
                    }
                }
            }

            // Handle bot state
            when (botState) {
                State.RANGE -> if ((clientThread.runOnClientThread { client.getVarbitValue(Varbits.PRAYER_PROTECT_FROM_MISSILES) == 0 }) || previousBotState != State.RANGE) {
                    previousBotState = State.RANGE
                    togglePrayers(true)
                    walkToCenterLocation(isPlayerInCenterLocation())
                }

                State.ZOMBIFIED_SPAWN -> if (previousBotState != State.ZOMBIFIED_SPAWN) {
                    previousBotState = State.ZOMBIFIED_SPAWN
                    togglePrayers(false)
                    Inventory.useItem(config.SLAYERSTAFF().toString())
                    eatAt(75)
                    while (Rs2Npc.getNpc("Zombified Spawn") == null) {
                        sleep(100, 200)
                    }
                    Rs2Npc.attack("Zombified Spawn")
                    sleep(2300, 2500)
                    Inventory.useItem(config.CROSSBOW().toString())
                    eatAt(75)
                    sleep(600, 1000)
                    Rs2Npc.attack("Vorkath")
                }
                // If the player is not walking
                State.RED_BALL -> if (client.localPlayer.idlePoseAnimation == 1 || doesProjectileExistById(
                        redProjectileId
                    )
                ) {
                    previousBotState = State.RED_BALL
                    redBallWalk()
                    sleep(2100, 2200)
                    Rs2Npc.attack("Vorkath")
                }

                State.ACID -> if (doesProjectileExistById(acidProjectileId) || doesProjectileExistById(
                        acidRedProjectileId
                    ) || Rs2GameObject.findObject(ObjectID.ACID_POOL) != null
                ) {
                    previousBotState = State.ACID
                    acidWalk()
                }

                State.EAT -> if (foods?.size!! > 0) {
                    VirtualMouse().click(foods!![0].getBounds())
                    botState = previousBotState
                } else {
                    println("No food found")
                    // Teleport
                    Inventory.useItem(config.TELEPORT().toString())
                    botState = State.BANK
                }

                State.PRAYER -> if (Inventory.findItemContains("prayer") != null) {
                    Inventory.useItemContains("prayer")
                    botState = previousBotState
                } else {
                    println("No prayer potions found")
                    // Teleport
                    Inventory.useItem(config.TELEPORT().toString())
                    botState = State.BANK
                }

                State.RANGE_POTION -> usePotion(config.RANGEPOTION().toString(), useTeleport = false)
                State.ANTIFIRE_POTION -> {
                    usePotion("super antifire")
                    botState = previousBotState
                }

                State.ANTI_VENOM_POTION -> {
                    usePotion("anti-venom")
                    botState = previousBotState
                }

                State.BANK -> {
                    togglePrayers(false)
                    Script.toggleRunEnergy(true)
                    if (config.AUTOBANK()) {
                        var result = autoBank();
                        if (result) {
                            if (Rs2Bank.isOpen()) {
                                bankLoadOut()
                            }
                        }
                    } else {
                        if (Rs2Bank.isOpen()) {
                            bankLoadOut()
                        }
                    }
                }

                State.WALK_TO_VORKATH -> {
                    val relleka = WorldPoint(2640, 3694, 0);
                    val torfiin = Rs2Npc.getNpc(NpcID.TORFINN_10405)
                    val vorkathIsland = WorldPoint(2267, 4026, 0)
                    val isOnVorkathIsland = client.localPlayer.worldLocation.distanceTo(vorkathIsland) < 30
                    if (isOnVorkathIsland) {
                        Rs2GameObject.interact(ObjectID.ICE_CHUNKS_31990)
                        sleepUntil { Rs2Player.isAnimating() }
                    } else if (client.localPlayer.worldLocation.distanceTo(relleka) > 4) {
                        Walker().walkTo(relleka)
                        sleep(600, 2400);
                    }
                    if (torfiin != null && !isOnVorkathIsland) {
                        Rs2Npc.interact(torfiin, "Ungael")
                        sleepUntil { client.localPlayer.worldLocation.distanceTo(vorkathIsland) < 20 }
                    }
                    previousBotState = State.WALK_TO_VORKATH
                }

                State.POKE_VORKATH -> {
                    if (client.localPlayer.localLocation.equals(LocalPoint(6208, 6976))) {
                        Rs2Npc.interact(sleepyVorkath, "Poke")
                        sleepUntil { Microbot.isWalking() }
                        sleepUntil { !Microbot.isWalking() }
                        botState = State.NONE
                    } else {
                        botState = previousBotState
                    }
                }

                State.NONE -> println("TODO")
                else -> botState = State.NONE
            }
        }
    }

    private fun acidWalk() {
        togglePrayers(false)
        var clickedTile: WorldPoint
        var toggle = true
        while (botState == State.ACID && previousBotState == State.ACID && (doesProjectileExistById(acidProjectileId) || doesProjectileExistById(
                acidRedProjectileId
            ))
        ) {
            clickedTile = if (toggle) rightTile else leftTile

            // Check if player's location is equal to the clicked tile location or if it's within one tile of the clicked location.
            val currentPlayerLocation = client.localPlayer.worldLocation

            // Ensure player is at the clickedTile.y before toggling
            if (currentPlayerLocation.y != clickedTile.y) {
                // Walk player to clickedTile.y location
                Walker().walkCanvas(WorldPoint(currentPlayerLocation.x, clickedTile.y, currentPlayerLocation.plane))
                while (client.localPlayer.worldLocation.y != clickedTile.y) {
                    sleep(1)
                }
            } else {
                if (currentPlayerLocation.distanceTo(clickedTile) <= 1) {
                    toggle = !toggle
                    clickedTile = if (toggle) rightTile else leftTile
                }

                Walker().walkCanvas(clickedTile)
                while (client.localPlayer.worldLocation != clickedTile && client.localPlayer.worldLocation.distanceTo(
                        clickedTile
                    ) > 1 && client.localPlayer.worldLocation.y == clickedTile.y && Microbot.isWalking()
                ) {
                    sleep(1)
                }
                toggle = !toggle
            }
        }
    }

    private fun eatAt(health: Int) {
        if (clientThread.runOnClientThread { client.getBoostedSkillLevel(Skill.HITPOINTS) } < health && Rs2Npc.getNpc("Vorkath") != null) {
            foods = clientThread.runOnClientThread { Inventory.getInventoryFood() }
            val food = if (foods?.size!! > 0) foods!![0] else null
            if (food != null) {
                VirtualMouse().click(food.getBounds())
            } else {
                //println("No food found")
                // Teleport
                Inventory.useItem(config.TELEPORT().toString())
            }
        }
    }

    // Check if projectile exists by ID
    private fun doesProjectileExistById(id: Int): Boolean {
        for (projectile in client.projectiles) {
            if (projectile.id == id) {
                //println("Projectile $id found")
                return true
            }
        }
        return false
    }

    // Click 2 tiles west of the player's current location
    private fun redBallWalk() {
        val currentPlayerLocation = client.localPlayer.worldLocation
        val twoTilesEastFromCurrentLocation = WorldPoint(currentPlayerLocation.x + 2, currentPlayerLocation.y, 0)
        Walker().walkCanvas(twoTilesEastFromCurrentLocation)
    }

    // player location is center location
    private fun isPlayerInCenterLocation(): Boolean {
        val currentPlayerLocation = client.localPlayer.worldLocation
        return currentPlayerLocation.x == centerTile.x && currentPlayerLocation.y == centerTile.y
    }

    // walk to center location
    private fun walkToCenterLocation(isPlayerInCenterLocation: Boolean) {
        if (!isPlayerInCenterLocation) {
            Walker().walkCanvas(centerTile)
            sleep(2000, 2100)
            Rs2Npc.attack("Vorkath")
        }
    }

    private fun usePotion(item: String, useTeleport: Boolean = true) {
        if (Inventory.findItemContains(item) != null) {
            Inventory.useItemContains(item)
            sleep(600, 1200);
            botState = previousBotState
        } else {
            println("No $item found")
            //Teleport
            Inventory.useItem(config.TELEPORT().toString())
            botState = State.BANK
        }
    }

    private fun autoBank(): Boolean {
        var hasBankNearby = Rs2Bank.openBank()
        if (!hasBankNearby && Rs2Player.isInHouse()) {
            Rs2POH.usePool()
            Rs2POH.usePortalNexus()
            return false
        } else if (!hasBankNearby && !Rs2Player.isInHouse()) {
            Rs2Bank.walkToBank()
            sleep(600, 2400)
            return false
        }
        return true
    }

    private fun bankLoadOut() {
        Rs2Bank.depositAll()
        MicrobotInventorySetup.loadEquipment(config.GEAR())
        MicrobotInventorySetup.loadInventory(config.GEAR())
        Rs2Bank.closeBank()
        Inventory.useItem(ItemID.RELLEKKA_TELEPORT)
        sleepUntil { client.localPlayer.worldLocation.distanceTo(WorldPoint(2669, 3633, 0)) < 20 }
        botState = State.WALK_TO_VORKATH
    }

    private fun togglePrayers(toggle: Boolean) {
        Rs2Prayer.fastPray(Prayer.PROTECT_RANGE, toggle)
        if (config.ACTIVATERIGOUR()) {
            Rs2Prayer.fastPray(Prayer.RIGOUR, toggle)
        }
    }
}
