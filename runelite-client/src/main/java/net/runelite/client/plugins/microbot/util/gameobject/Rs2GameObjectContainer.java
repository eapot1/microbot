package net.runelite.client.plugins.microbot.util.gameobject;

import lombok.Getter;
import net.runelite.api.GameObject;
import net.runelite.api.ObjectComposition;

public class Rs2GameObjectContainer {
    @Getter
    ObjectComposition objectComposition;
    @Getter
    GameObject gameObject;


    public Rs2GameObjectContainer(GameObject gameObject, ObjectComposition objectComposition) {
        this.objectComposition = objectComposition;
        this.gameObject = gameObject;
    }
}
