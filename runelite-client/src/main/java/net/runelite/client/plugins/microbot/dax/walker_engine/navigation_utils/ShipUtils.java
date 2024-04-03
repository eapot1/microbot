package net.runelite.client.plugins.microbot.dax.walker_engine.navigation_utils;

import dax.walker_engine.WaitFor;
import dax.walker_engine.interaction_handling.InteractionHelper;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.grounditem.Rs2GroundItem;
import org.tribot.api.General;
import org.tribot.api2007.Game;
import org.tribot.api2007.Objects;
import org.tribot.api2007.Player;
import org.tribot.api2007.ext.Filters;
import org.tribot.api2007.types.RSArea;
import org.tribot.api2007.types.RSObject;
import org.tribot.api2007.types.Tile;
import net.runelite.api.Tile;
import net.runelite.api.Player;

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

        }
        return getGangplank() != null
                && Player.getWorldLocation().getPlane() == 1
                && Rs2GameObject.get(10, Filters.Objects.nameEquals("Ship's wheel", "Ship's ladder", "Anchor")).length > 0;
    }

    public static boolean crossGangplank() {
        Rs2GameObject gangplank = getGangplank();
        if (gangplank == null){
            return false;
        }
        if (!gangplank.click("Cross")){
            return false;
        }
        if (WaitFor.condition(1000, () -> Game.getCrosshairState() == 2 ? WaitFor.Return.SUCCESS : WaitFor.Return.IGNORE) != WaitFor.Return.SUCCESS){
            return false;
        }
        return WaitFor.condition(General.random(2500, 3000), () -> !ShipUtils.isOnShip() ? WaitFor.Return.SUCCESS : WaitFor.Return.IGNORE) == WaitFor.Return.SUCCESS;
    }

    private static Rs2GameObject getGangplank(){
        return InteractionHelper.getRSObject(Filters.Objects.nameEquals("Gangplank").combine(Filters.Objects.actionsContains("Cross"), true));
    }

}
