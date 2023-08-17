package net.runelite.client.plugins.microbot.util.player;

import lombok.Setter;
import net.runelite.api.VarPlayer;
import net.runelite.api.Varbits;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;

import static net.runelite.api.ObjectID.PORTAL_4525;


public class Rs2Player {

    private static int antiFireTimer = -1;
    private static int superAntiFireTimer = -1;
    private static int divineRangedTimer = -1;
    private static int divineBastionTimer = -1;
    @Setter
    private static int antiVenomTimer = -1;
    private static int divineSuperStrengthTimer = -1;
    private static int divineSuperAttackTimer = -1;
    private static int divineSuperDefenceTimer = -1;
    private static int divineSuperCombatTimer = -1;
    private static int antiPoisonTimer = -1;
    private static int overloadTimer = -1;
    static int VENOM_VALUE_CUTOFF = -38;

    public static boolean hasAntiFireActive() {
        return antiFireTimer > 0 || hasSuperAntiFireActive();
    }

    public static boolean hasSuperAntiFireActive() {
        return superAntiFireTimer > 0;
    }

    public static boolean hasDivineRangedActive() {
        return divineRangedTimer > 0 || hasDivineBastionActive();
    }

    public static boolean hasDivineBastionActive() {
        return divineBastionTimer > 0;
    }

    public static boolean hasAntiVenomActive() {
        return antiVenomTimer < VENOM_VALUE_CUTOFF;
    }
    public static boolean hasDivineSuperStrengthActive() {
        return divineSuperStrengthTimer > 0;
    }
    public static boolean hasDivineSuperAttackActive() {
        return divineSuperAttackTimer > 0;
    }
    public static boolean hasDivineSuperDefenceActive() {
        return divineSuperDefenceTimer > 0;
    }
    public static boolean hasDivineSuperCombatActive() {
        return divineSuperCombatTimer > 0;
    }
    public static boolean hasAntiPoisonActive() {
        return antiPoisonTimer > 0 || hasAntiVenomActive();
    }
    public static boolean hasOverloadActive() {
        return overloadTimer > 0;
    }

    public static boolean isInHouse() {
        return Rs2GameObject.findObjectById(PORTAL_4525) != null;
    }

    public static boolean isAnimating() {
        return Microbot.getClientThread().runOnClientThread(() -> Microbot.getClient().getLocalPlayer().getAnimation() != -1);
    }


    public static void handlePotionTimers(VarbitChanged event) {
        if (event.getVarbitId() == Varbits.ANTIFIRE) {
            antiFireTimer = event.getValue();
        }
        if (event.getVarbitId() == Varbits.SUPER_ANTIFIRE) {
            superAntiFireTimer = event.getValue();
        }
        if (event.getVarbitId() == Varbits.DIVINE_RANGING) {
            divineRangedTimer = event.getValue();
        }
        if (event.getVarbitId() == Varbits.DIVINE_BASTION) {
            divineBastionTimer = event.getValue();
        }
        if (event.getVarpId() == VarPlayer.POISON) {
            if (event.getValue() >= VENOM_VALUE_CUTOFF) {
                antiVenomTimer = 0;
                return;
            }
            if (event.getValue() > 0 && event.getValue() < VENOM_VALUE_CUTOFF) {
                antiPoisonTimer = event.getValue();
            }
            antiVenomTimer = event.getValue();
        }
        if (event.getVarpId() == Varbits.DIVINE_SUPER_STRENGTH) {
            divineSuperStrengthTimer = event.getValue();
        }
        if (event.getVarpId() == Varbits.DIVINE_SUPER_ATTACK) {
            divineSuperAttackTimer = event.getValue();
        }
        if (event.getVarpId() == Varbits.DIVINE_SUPER_DEFENCE) {
            divineSuperDefenceTimer = event.getValue();
        }
        if (event.getVarpId() == Varbits.DIVINE_SUPER_COMBAT) {
            divineSuperCombatTimer = event.getValue();
        }
        if (event.getVarpId() == Varbits.NMZ_OVERLOAD_REFRESHES_REMAINING) {
            overloadTimer = event.getValue();
        }
    }
}
