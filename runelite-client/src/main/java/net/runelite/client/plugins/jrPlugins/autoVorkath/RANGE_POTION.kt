package net.runelite.client.plugins.jrPlugins.autoVorkath

import lombok.Getter
import lombok.RequiredArgsConstructor

@Getter
@RequiredArgsConstructor
enum class RANGE_POTION(val potionName: String) {
    DIVINE_RANGING_POTION("Divine ranging Potion"),
    DIVINE_BASTION_POTION("Divine bastion Potion");

    override fun toString(): String { return potionName }
}
