package net.runelite.client.plugins.microbot.herblore;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.bankjs.BanksBankStander.CurrentStatus;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Item;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;

import java.util.concurrent.TimeUnit;


public class HerbloreScript extends Script {
    public static double version = 1.0;

    public boolean run(HerbloreConfig config) {
        Microbot.enableAutoRunOn = false;
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            if (!super.run()) return;
            try {

                long startTime = System.currentTimeMillis();
                fetchItems(config);

                if (!Rs2Bank.isOpen() && Rs2Inventory.hasItem(config.firstItemIdentifier())) {
                    for(Rs2Item item : Rs2Inventory.all(i -> i.name.toLowerCase().contains(config.firstItemIdentifier()))) {
                        Microbot.getMouse().click(Rs2Inventory.itemBounds(item));
                        //Rs2Inventory.interact(item, "clean");
                        sleep(20,40);
                    }
                }


                long endTime = System.currentTimeMillis();
                long totalTime = endTime - startTime;
                System.out.println("Total time for loop " + totalTime);

            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
        return true;
    }

    private boolean fetchItems(HerbloreConfig config) {
        if (!hasItems(config)) {
            if (!Rs2Bank.isOpen()) {
                // Open Bank
                Rs2Bank.useBank();
            }
            sleepUntil(Rs2Bank::isOpen);
            Rs2Bank.depositAll();
            sleepUntil(Rs2Inventory::isEmpty);

            if (Rs2Bank.hasItem(config.firstItemIdentifier())) {
                Rs2Bank.withdrawItemAll(config.firstItemIdentifier());
                sleepUntil(() -> Rs2Inventory.hasItem(config.firstItemIdentifier()));
                sleepUntil(Rs2Bank::closeBank);
            } else {
                return false;
            }
        }

        return true;
    }

    private boolean hasItems(HerbloreConfig config) {
        return Rs2Inventory.hasItem(config.firstItemIdentifier());
    }

    @Override
    public void shutdown() {
        super.shutdown();
    }
}
