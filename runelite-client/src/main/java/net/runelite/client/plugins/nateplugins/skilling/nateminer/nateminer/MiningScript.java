package net.runelite.client.plugins.nateplugins.skilling.nateminer.nateminer;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;

import java.util.concurrent.TimeUnit;


public class MiningScript extends Script {

    public static double version = 1.3;

    public boolean run(MiningConfig config) {
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            if (!super.run()) return;
            if (!Microbot.isLoggedIn()) return;
            try {
                if (Microbot.isMoving() || Microbot.isAnimating() || Microbot.pauseAllScripts) return;


                if (Rs2Inventory.isFull()) {
                    if (config.hasPickaxeInventory()) {
                        Rs2Inventory.dropAll(x -> !x.name.toLowerCase().contains("pickaxe"));
                    } else {
                        Rs2Inventory.dropAll();
                    }
                    return;
                }
                //List<GameObject> rocks = Rs2GameObject.getGameObjectsWithinDistance(1);
                //rocks.stream().filter(x -> x.getId() == 11364 || x.getId() == 11365).findFirst().ifPresent(Rs2GameObject::interact);
                //Rs2GameObject.interact(rocks.get(0));
                boolean result = Rs2GameObject.interact(config.ORE().getName());
                if (result) {
                    Rs2Player.waitForAnimation();
                }

            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 100, TimeUnit.MILLISECONDS);
        return true;
    }
}
