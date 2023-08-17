package net.runelite.client.plugins.jrPlugins.autoVorkath;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("AutoVorkath")
public interface AutoVorkathConfig extends Config {

    @ConfigItem(
            keyName = "gear",
            name = "Gear Setup",
            description = "Enter the name of the gear setup you want to use",
            position = 0
    )
    default String GEAR() { return "AutoVorkath"; }

    @ConfigItem(
            keyName = "crossbow",
            name = "Crossbow",
            description = "Choose your crossbow",
            position = 1
    )
    default CROSSBOW CROSSBOW() {
        return CROSSBOW.ARMADYL_CROSSBOW;
    }

    @ConfigItem(
            keyName = "slayersStaff",
            name = "Slayers Staff",
            description = "Choose your slayers staff",
            position = 2
    )
    default STAFF SLAYERSTAFF() {
        return STAFF.SLAYER_STAFF;
    }

    @ConfigItem(
            keyName = "teleport",
            name = "Teleport",
            description = "Choose your teleport",
            position = 3
    )
    default TELEPORT TELEPORT() {
        return TELEPORT.CONSTRUCT_CAPE_T;
    }

    @ConfigItem(
            keyName = "AutoBank",
            name = "Automatically bank",
            description = "Automatically banks",
            position = 4
    )
    default boolean AUTOBANK() {
        return false;
    }

    @ConfigItem(
            keyName = "portalNexus",
            name = "Use Portal Nexus",
            description = "Use portal nexus to bank",
            position = 5
    )
    default boolean PORTALNEXUS() {
        return false;
    }

    @ConfigItem(
            keyName = "rigour",
            name = "Rigour",
            description = "Activate Rigour?",
            position = 6
    )
    default boolean ACTIVATERIGOUR() { return true; }

    @ConfigItem(
            keyName = "rangePotion",
            name = "Ranging Potion",
            description = "What Ranging potion to use?",
            position = 7
    )
    default RANGE_POTION RANGEPOTION() { return RANGE_POTION.DIVINE_RANGING_POTION; }
    @ConfigItem(
            keyName = "Min Health",
            name = "Min Health",
            description = "Treshold to eat food",
            position = 8
    )
    default int MINHEALTH() { return 55; }
    @ConfigItem(
            keyName = "Min Prayer",
            name = "Min Prayer",
            description = "Treshold to drink prayer potion",
            position = 9
    )
    default int MINPRAY() { return 20; }
}
