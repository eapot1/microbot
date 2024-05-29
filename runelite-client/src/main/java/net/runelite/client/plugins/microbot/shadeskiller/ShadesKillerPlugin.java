package net.runelite.client.plugins.microbot.shadeskiller;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.NPC;
import net.runelite.api.NpcID;
import net.runelite.api.events.AnimationChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;


@PluginDescriptor(
        name = PluginDescriptor.Mocrosoft + "ShadesKiller",
        description = "Microbot ShadesKiller plugin",
        tags = {"Shades", "microbot", "Moneymaking"},
        enabledByDefault = false
)
@Slf4j
public class ShadesKillerPlugin extends Plugin {
    @Inject
    private ShadesKillerConfig config;

    @Provides
    ShadesKillerConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(ShadesKillerConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private ShadesKillerOverlay shadesKillerOverlay;

    @Inject
    ShadesKillerScript shadesKillerScript;


    @Override
    protected void startUp() throws AWTException {
        if (overlayManager != null) {
            overlayManager.add(shadesKillerOverlay);
        }
        shadesKillerScript.run(config);
    }

    protected void shutDown() {
        shadesKillerScript.shutdown();
        overlayManager.remove(shadesKillerOverlay);
    }

    @Subscribe
    public void onAnimationChanged(AnimationChanged e) {
        if (!(e.getActor() instanceof NPC)) {
            return;
        }

        final NPC npc = (NPC) e.getActor();
        if (npc.getId() == NpcID.FIYR_SHADE && npc.getAnimation() == 1287) {
            log.info("Shade died");
            shadesKillerScript.shadeDied();
        }

    }
}
