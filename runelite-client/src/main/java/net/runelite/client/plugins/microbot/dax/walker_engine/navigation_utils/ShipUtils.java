package net.runelite.client.plugins.microbot.dax.walker_engine.navigation_utils;

import net.runelite.api.Player;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObjectContainer;

import java.util.Arrays;

import static net.runelite.client.plugins.microbot.util.Global.sleepUntil;

public class ShipUtils {

    private static final WorldPoint[] SPECIAL_CASES = new WorldPoint[]{new WorldPoint(2663, 2676, 1)};

    private static Player Player = Microbot.getClient().getLocalPlayer();

    public static boolean isOnShip() {
        WorldPoint playerPos = Player.getWorldLocation();
        for (WorldPoint specialCase : SPECIAL_CASES){
            if (new WorldArea(specialCase, 5, 5).contains(playerPos)){
                return true;
            }
        }
        if (getGangplank() == null) return false;
        if (Player.getWorldLocation().getPlane() != 1) return false;

        return !Rs2GameObject.getGameObjects("Ship's wheel", "Ship's ladder", "Anchor").isEmpty();
    }

    public static boolean crossGangplank() {
        Rs2GameObjectContainer gangplank = getGangplank();
        if (gangplank == null){
            return false;
        }
        if (!Rs2GameObject.interact(gangplank.getGameObject(), "Cross")) {
            return false;
        }

        sleepUntil(() -> !ShipUtils.isOnShip());

        return !ShipUtils.isOnShip();
    }

    private static Rs2GameObjectContainer getGangplank(){
        return Rs2GameObject.get(x -> Arrays.stream(x.getObjectComposition().getActions()).anyMatch(o -> o.equalsIgnoreCase("gangplank") && o.contains("Cross"))).stream().findFirst().orElse(null);
    }

}
