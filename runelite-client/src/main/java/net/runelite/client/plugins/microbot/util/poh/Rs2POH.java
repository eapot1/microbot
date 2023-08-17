package net.runelite.client.plugins.microbot.util.poh;

import net.runelite.api.TileObject;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;

import static net.runelite.client.plugins.microbot.util.Global.sleepUntil;
import static net.runelite.client.plugins.poh.PohIcons.POOLS;
import static net.runelite.client.plugins.poh.PohIcons.PORTALNEXUS;

public class Rs2POH {
    public static void usePortalNexus() {
        TileObject portalNexus = Rs2GameObject.findObject(PORTALNEXUS.getIds());
        if (portalNexus == null) return;
        Rs2GameObject.interact(portalNexus);
        sleepUntil(() -> !Rs2Player.isInHouse());
    }

    public static void usePool() {
        TileObject pool = Rs2GameObject.findObject(POOLS.getIds());
        if (pool == null) return;
        Rs2GameObject.interact(pool);
        sleepUntil(() -> Rs2Player.isAnimating());
    }
}
