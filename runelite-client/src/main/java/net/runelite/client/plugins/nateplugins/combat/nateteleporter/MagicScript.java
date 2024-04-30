package net.runelite.client.plugins.nateplugins.combat.nateteleporter;

import net.runelite.api.ItemID;
import net.runelite.api.Skill;
import net.runelite.api.widgets.Widget;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2Cannon;
import net.runelite.client.plugins.microbot.globval.enums.InterfaceTab;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Item;
import net.runelite.client.plugins.microbot.util.magic.Rs2Magic;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.tabs.Rs2Tab;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.nateplugins.combat.nateteleporter.enums.SPELLS;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;


public class MagicScript extends Script {

    public static double version = 1.8;


    public boolean run(MagicConfig config) {
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            if (!super.run()) return;
            if (!Microbot.isLoggedIn()) return;


            try {
                if (!config.alchemy() && (Microbot.isMoving() || Microbot.isAnimating() || Microbot.pauseAllScripts))
                    return;

                if (config.alchemy()) {

                    Rs2Item item = getNextItem(config.AlchItem());
                    if (item == null) {
                        Microbot.showMessage("Item: " + config.AlchItem() + " not found in your inventory.");
                        shutdown();
                        return;
                    }


                    Rs2Magic.alch(item);
                    sleepUntil(() -> Rs2Tab.getCurrentTab() == InterfaceTab.MAGIC);
                }

                Rs2Player.eatAt(50);

                if (config.cannon()) {
                    if (Rs2Cannon.repair()) return;
                    if (Rs2Cannon.refill(5)) return;
                    if (Microbot.getClient().getLocalPlayer().getWorldLocation().equals(initialPlayerLocation)) return;
                    Rs2Walker.walkFastCanvas(initialPlayerLocation);
                }

                if (config.MODE() == SPELLS.NONE)
                    return;

                Widget widget = Microbot.getClient().getWidget(config.MODE().getSpell().getSpell().getWidgetId());

                if (config.MODE().getSpell().getLevel() > Microbot.getClient().getBoostedSkillLevel(Skill.MAGIC)) {
                    Microbot.showMessage("Level not high enough!");
                    shutdown();
                } else if (widget != null && widget.getSpriteId() != config.MODE().getSpell().getSpell().getSprite()) {
                    Microbot.showMessage("Missing required items!");
                    shutdown();
                } else {
                    Rs2Magic.cast(config.MODE().getSpell().getSpell());
                    sleep(100, 300);
                    if (!config.MODE().toString().contains("teleport")) {
                        net.runelite.api.NPC npc = (net.runelite.api.NPC) Microbot.getClient().getLocalPlayer().getInteracting();
                        if (npc != null && npc.getName().equals(config.NPC()))
                            Rs2Npc.interact(npc, "");
                        else
                            Rs2Npc.interact(config.NPC(), "");
                    }
                }
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 500, TimeUnit.MILLISECONDS);
        return true;
    }

    //Blood essence,med helm,warhammer,platelegs,chainbody,halberd,kite,full helm,battleaxe,mystic air,longsword,Rune sword,dagger,Combat bracelet,Onyx dragon bolts (e),battlestaff,Onyx bolts (e),d'hide body,platebody,Phoenix necklace,d'hide chaps,d'hide vambraces,Diamond bracelet,2h sword

    private Rs2Item getNextItem(String itemList) {
        return Microbot.getClientThread().runOnClientThread(() -> {
            // get inventory items that are in itemList
            String[] items = itemList.split(",");
            //make array lowercase
            for (int i = 0; i < items.length; i++) {
                items[i] = items[i].toLowerCase();
            }
            //check if name is in the list, name can be a substring of the item name
            // sorted by HA value
            var rsItems = Rs2Inventory.all(item -> Arrays.stream(items).anyMatch(item.name.toLowerCase()::contains));


            rsItems.sort((a, b) -> Microbot.getItemManager().getItemComposition(b.id).getHaPrice() - Microbot.getItemManager().getItemComposition(a.id).getHaPrice());

            // log rsItems
            for (Rs2Item rs2Item : rsItems) {
                System.out.println(rs2Item.name);
            }

            if (!rsItems.isEmpty())
                return rsItems.get(0);
            else
                return null;

        });

//        for (String item : items) {
//            Rs2Item rs2Item = Rs2Inventory.get(item);
//            if (rs2Item != null) {
//                return rs2Item;
//            }
//        }
//
//        return null;
    }

    private int getHaPrice(int itemId) {

        return Microbot.getClientThread().runOnClientThread(() -> Microbot.getItemManager().getItemComposition(itemId).getHaPrice());
    }

    @Override
    public void shutdown() {
        initialPlayerLocation = null;
        super.shutdown();
    }
}
