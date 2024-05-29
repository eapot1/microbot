package net.runelite.client.plugins.microbot.herblore;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("example")
public interface HerbloreConfig extends Config {

    @ConfigSection(
            name = "Item Settings",
            description = "Set Items",
            position = 0,
            closedByDefault = false
    )
    String itemSection = "itemSection";

    @ConfigItem(
            keyName = "Herb name",
            name = "First Item",
            description = "",
            position = 0,
            section = itemSection
    )

    default String firstItemIdentifier() {
        return "grimy guam leaf";
    }
}
