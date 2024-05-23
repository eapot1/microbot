package net.runelite.client.plugins.microbot.enchantbracelet;

import net.runelite.api.Point;
import net.runelite.api.widgets.Widget;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.globval.enums.InterfaceTab;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Item;
import net.runelite.client.plugins.microbot.util.tabs.Rs2Tab;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;

import java.util.concurrent.TimeUnit;

import static net.runelite.client.plugins.microbot.util.Global.sleep;
import static net.runelite.client.plugins.microbot.util.Global.sleepUntil;
import static net.runelite.client.plugins.microbot.util.camera.Rs2Camera.isTileOnScreen;


public class ExampleScript extends Script {
    public static double version = 1.0;

    public boolean run(ExampleConfig config) {
        Microbot.enableAutoRunOn = false;
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            if (!super.run()) return;
            try {

                //check if dragonstone bracelet is in inventory
                if (!Rs2Inventory.hasItem("Dragonstone bracelet")) {
                    fetchItems();
                    return;
                }

                enchantItems(Rs2Inventory.get("Dragonstone bracelet"));

            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 800, TimeUnit.MILLISECONDS);
        return true;
    }

    private boolean fetchItems() {
        if (!Rs2Bank.isOpen()) {
            // Open Bank
            Rs2Bank.useBank();
        }
        sleepUntil(() -> Rs2Bank.isOpen());
        Rs2Bank.depositAllExcept(564);
        sleepUntil(() -> !Rs2Inventory.hasItem("combat bracelet"));

        if (Rs2Bank.hasItem("dragonstone bracelet")) {
            Rs2Bank.withdrawItemAll("dragonstone bracelet");
        }

        sleepUntil(() -> Rs2Inventory.hasItem("dragonstone bracelet"));

        Rs2Bank.closeBank();
        sleepUntil(() -> !Rs2Bank.isOpen());

        return true;
    }

    private void enchantItems(Rs2Item item) {
        if(Rs2Tab.getCurrentTab() != InterfaceTab.MAGIC) {
            sleepUntil(() -> {
                Rs2Tab.switchToMagicTab();
                sleep(50, 150);
                return Rs2Tab.getCurrentTab() == InterfaceTab.MAGIC;
            });
        }
        //Widget enchantWidget = Rs2Widget.getWidget(MagicAction.ENCHANT_DRAGONSTONE_JEWELLERY.getWidgetId());
        Widget enchantWidget = Rs2Widget.findWidget("Lvl-5 Enchant");

        Widget mainEnchantWidget = Rs2Widget.findWidget("Jewellery enchantments");
        if(Rs2Widget.isHidden(enchantWidget.getId())) {
            Point jpoint = new Point((int) mainEnchantWidget.getBounds().getCenterX(), (int) mainEnchantWidget.getBounds().getCenterY());
            Microbot.getMouse().click(jpoint);
            sleep(300, 600);
        }

        Point point = new Point((int) enchantWidget.getBounds().getCenterX(), (int) enchantWidget.getBounds().getCenterY());
        sleepUntil(() -> Microbot.getClientThread().runOnClientThread(() -> Rs2Tab.getCurrentTab() == InterfaceTab.MAGIC), 5000);
        sleep(100, 300);
        Microbot.getMouse().click(point);
        sleepUntil(() -> Microbot.getClientThread().runOnClientThread(() -> Rs2Tab.getCurrentTab() == InterfaceTab.INVENTORY), 5000);
        sleep(100, 300);
        if (item == null) {
            Microbot.status = "enchanting x: " + point.getX() + " y: " + point.getY();
            Microbot.getMouse().click(point);
        } else {
            Microbot.status = "enchanting " + item.name;
            Rs2Inventory.interact(item, "cast");
        }
        sleepUntil(() -> Microbot.getClientThread().runOnClientThread(() -> Rs2Tab.getCurrentTab() == InterfaceTab.MAGIC), 5000);
    }

    @Override
    public void shutdown() {
        super.shutdown();
    }
}
