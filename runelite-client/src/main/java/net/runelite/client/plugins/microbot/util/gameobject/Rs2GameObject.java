package net.runelite.client.plugins.microbot.util.gameobject;

import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.camera.Rs2Camera;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Item;
import net.runelite.client.plugins.microbot.util.menu.NewMenuEntry;
import net.runelite.client.plugins.microbot.util.reflection.Rs2Reflection;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;


public class Rs2GameObject {
    public static boolean interact(WorldPoint worldPoint) {
        TileObject gameObject = findObjectByLocation(worldPoint);
        return clickObject(gameObject);
    }

    public static boolean interact(GameObject gameObject) {
        return clickObject(gameObject);
    }

    public static boolean interact(TileObject tileObject) {
        return clickObject(tileObject, null);
    }

    public static boolean interact(TileObject tileObject, String action) {
        return clickObject(tileObject, action);
    }

    public static boolean interact(GameObject gameObject, String action) {
        return clickObject(gameObject, action);
    }

    public static boolean interact(int id) {
        TileObject object = findObjectById(id);
        return clickObject(object);
    }

    public static boolean interact(int id, boolean checkCanReach) {
        TileObject object = findObjectById(id);
        ObjectComposition objectComposition = Microbot.getClientThread().runOnClientThread(() -> Microbot.getClient().getObjectDefinition(id));
        int objectSize = (objectComposition.getSizeX() + objectComposition.getSizeY()) / 2;
        if (object == null) return false;
        if (checkCanReach && Microbot.getWalker().canReach(object.getWorldLocation(), objectSize))
            return clickObject(object);
        if (Microbot.getClient().getLocalPlayer().getWorldLocation().distanceTo2D(object.getWorldLocation()) > 10) {
            Microbot.getWalker().hybridWalkTo(object.getWorldLocation());
        } else {
            Microbot.getWalker().walkTo(object.getWorldLocation());
        }
        return false;
    }


    public static boolean interact(int id, String action) {
        TileObject object = findObjectById(id);
        return clickObject(object, action);
    }

    public static boolean interact(int id, String action, int distance) {
        TileObject object = findObjectByIdAndDistance(id, distance);
        return clickObject(object, action);
    }

    public static boolean interact(String name, String action) {
        TileObject object = findObject(name);
        return clickObject(object, action);
    }

    public static TileObject interactAndGetObject(int id) {
        TileObject object = findObjectById(id);
        clickObject(object);
        return object;
    }

    public static boolean interact(int[] objectIds, String action) {
        for (int objectId : objectIds) {
            if (interact(objectId, action)) return true;
        }
        return false;
    }

    public static boolean interact(String objectName) {
        GameObject object = findObject(objectName, true);
        return clickObject(object);
    }

    public static boolean interactByOptionName(String action) {
        GameObject object = findObjectByOption(action);
        return clickObject(object);
    }

    public static boolean interact(String objectName, boolean exact) {
        GameObject object = findObject(objectName, exact);
        return clickObject(object);
    }

    public static boolean interact(String objectName, String action, boolean exact) {
        GameObject object = findObject(objectName, exact);
        return clickObject(object, action);
    }

    public static GameObject findObject(String objectName) {
        return findObject(objectName, true);
    }

    @Deprecated(since = "Use findObjectById", forRemoval = true)
    public static ObjectComposition findObject(int id) {
        return Microbot.getClientThread().runOnClientThread(() -> Microbot.getClient().getObjectDefinition(id));
    }

    public static boolean exists(int id) {
        return findObjectById(id) != null;
    }

    public static TileObject findObjectById(int id) {

        List<GameObject> gameObjects = getGameObjects();

        if (gameObjects == null) return null;

        for (net.runelite.api.GameObject gameObject : gameObjects) {
            if (gameObject.getId() == id)
                return gameObject;
        }

        List<GroundObject> groundObjects = getGroundObjects();

        for (GroundObject groundObject : groundObjects) {
            if (groundObject.getId() == id)
                return groundObject;
        }

        List<WallObject> wallObjects = getWallObjects();


        for (WallObject wallObject : wallObjects) {
            if (wallObject.getId() == id)
                return wallObject;
        }

        List<DecorativeObject> decorationObjects = getDecorationObjects();


        for (DecorativeObject decorativeObject : decorationObjects) {
            if (decorativeObject.getId() == id)
                return decorativeObject;
        }

        return null;
    }

    public static TileObject findObjectByLocation(WorldPoint worldPoint) {

        List<GameObject> gameObjects = getGameObjects();

        if (gameObjects == null) return null;

        for (net.runelite.api.GameObject gameObject : gameObjects) {
            if (gameObject.getWorldLocation().equals(worldPoint))
                return gameObject;
        }

        List<GroundObject> groundObjects = getGroundObjects();

        for (GroundObject groundObject : groundObjects) {
            if (groundObject.getWorldLocation().equals(worldPoint))
                return groundObject;
        }

        List<WallObject> wallObjects = getWallObjects();


        for (WallObject wallObject : wallObjects) {
            if (wallObject.getWorldLocation().equals(worldPoint))
                return wallObject;
        }

        List<DecorativeObject> decorationObjects = getDecorationObjects();


        for (DecorativeObject decorativeObject : decorationObjects) {
            if (decorativeObject.getWorldLocation().equals(worldPoint))
                return decorativeObject;
        }

        return null;
    }

    public static TileObject findObjectByIdAndDistance(int id, int distance) {

        List<GameObject> gameObjects = getGameObjects();

        gameObjects = gameObjects.stream()
                .filter(x -> Microbot.getClient().getLocalPlayer().getWorldLocation().distanceTo(x.getWorldLocation()) < distance)
                .collect(Collectors.toList());

        for (net.runelite.api.GameObject gameObject : gameObjects) {
            if (gameObject.getId() == id)
                return gameObject;
        }

        List<GroundObject> groundObjects = getGroundObjects();

        groundObjects = groundObjects.stream().filter(x -> Microbot.getClient().getLocalPlayer().getWorldLocation().distanceTo(x.getWorldLocation()) < distance).collect(Collectors.toList());


        for (GroundObject groundObject : groundObjects) {
            if (groundObject.getId() == id)
                return groundObject;
        }

        List<WallObject> wallObjects = getWallObjects();

        wallObjects = wallObjects.stream().filter(x -> Microbot.getClient().getLocalPlayer().getWorldLocation().distanceTo(x.getWorldLocation()) < distance).collect(Collectors.toList());


        for (WallObject wallObject : wallObjects) {
            if (wallObject.getId() == id)
                return wallObject;
        }

        List<DecorativeObject> decorationObjects = getDecorationObjects();

        decorationObjects = decorationObjects.stream().filter(x -> Microbot.getClient().getLocalPlayer().getWorldLocation().distanceTo(x.getWorldLocation()) < distance).collect(Collectors.toList());

        for (DecorativeObject decorativeObject : decorationObjects) {
            if (decorativeObject.getId() == id)
                return decorativeObject;
        }

        return null;
    }

    public static List<DecorativeObject> getDecorationObjects() {
        List<Tile> tiles = getTiles();

        if (tiles.isEmpty()) return new ArrayList<>();

        List<DecorativeObject> tileObjects = new ArrayList<>();
        for (Tile tile : tiles) {
            tileObjects.add(tile.getDecorativeObject());
        }
        return tileObjects.stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingInt(tile -> tile.getWorldLocation().distanceTo(Microbot.getClient().getLocalPlayer().getWorldLocation())))
                .collect(Collectors.toList());
    }

    public static GameObject findObjectById(int id, int x) {

        List<GameObject> gameObjects = getGameObjects();

        if (gameObjects == null) return null;

        for (net.runelite.api.GameObject gameObject : gameObjects) {
            if (gameObject.getId() == id && gameObject.getWorldLocation().getX() == x)
                return gameObject;
        }

        return null;
    }

    public static GameObject findObject(int id, WorldPoint worldPoint) {

        List<GameObject> gameObjects = getGameObjects();

        if (gameObjects == null) return null;

        for (net.runelite.api.GameObject gameObject : gameObjects) {
            if (gameObject.getId() == id && gameObject.getWorldLocation().equals(worldPoint))
                return gameObject;
        }

        return null;
    }

    public static ObjectComposition findObjectComposition(int id) {

        List<GameObject> gameObjects = getGameObjects();

        if (gameObjects == null) return null;

        for (net.runelite.api.GameObject gameObject : gameObjects) {
            if (gameObject.getId() == id) {
                return convertGameObjectToObjectComposition(gameObject);
            }
        }
        return null;
    }


    public static GameObject findObject(String objectName, boolean exact) {
        List<GameObject> gameObjects = getGameObjects();

        if (gameObjects == null) {
            return null;
        }

        for (GameObject gameObject : gameObjects) {
            ObjectComposition objComp = convertGameObjectToObjectComposition(gameObject);

            if (objComp == null) {
                continue;
            }
            String compName = null;

            try {
                compName = !objComp.getName().equals("null") ? objComp.getName() : (objComp.getImpostor() != null ? objComp.getImpostor().getName() : null);
            } catch (Exception e) {
                continue;
            }

            if (compName != null && Microbot.getWalker().canInteract(gameObject.getWorldLocation())) {
                if (!exact && compName.toLowerCase().contains(objectName.toLowerCase())) {
                    return gameObject;
                } else if (exact && compName.equalsIgnoreCase(objectName)) {
                    return gameObject;
                }
            }
        }

        return null;
    }

    public static GameObject findObject(String objectName, boolean exact, int distance) {
        List<GameObject> gameObjects = getGameObjects(distance, Microbot.getClient().getLocalPlayer().getWorldLocation());

        if (gameObjects == null) {
            return null;
        }

        for (GameObject gameObject : gameObjects) {
            ObjectComposition objComp = convertGameObjectToObjectComposition(gameObject);

            if (objComp == null) {
                continue;
            }
            String compName = null;

            try {
                compName = !objComp.getName().equals("null") ? objComp.getName() : (objComp.getImpostor() != null ? objComp.getImpostor().getName() : null);
            } catch (Exception e) {
                continue;
            }

            if (compName != null && Microbot.getWalker().canInteract(gameObject.getWorldLocation())) {
                if (!exact && compName.toLowerCase().contains(objectName.toLowerCase())) {
                    return gameObject;
                } else if (exact && compName.equalsIgnoreCase(objectName)) {
                    return gameObject;
                }
            }
        }

        return null;
    }

    public static GameObject findObject(String objectName, boolean exact, int distance, WorldPoint anchorPoint) {
        List<GameObject> gameObjects = getGameObjects(distance, anchorPoint);

        if (gameObjects == null) {
            return null;
        }

        for (GameObject gameObject : gameObjects) {
            ObjectComposition objComp = convertGameObjectToObjectComposition(gameObject);

            if (objComp == null) {
                continue;
            }
            String compName = null;

            try {
                compName = !objComp.getName().equals("null") ? objComp.getName() : (objComp.getImpostor() != null ? objComp.getImpostor().getName() : null);
            } catch (Exception e) {
                continue;
            }

            if (compName != null && Microbot.getWalker().canInteract(gameObject.getWorldLocation())) {
                if (!exact && compName.toLowerCase().contains(objectName.toLowerCase())) {
                    return gameObject;
                } else if (exact && compName.equalsIgnoreCase(objectName)) {
                    return gameObject;
                }
            }
        }

        return null;
    }

    public static boolean hasAction(ObjectComposition objComp, String action) {
        boolean result = false;

        result = Arrays.stream(objComp.getActions()).anyMatch(x -> x != null && x.equals(action));
        if (!result) {
            try {
                result = Arrays.stream(objComp.getImpostor().getActions()).anyMatch(x -> x != null && x.equalsIgnoreCase(action));
            } catch (Exception ex) {
                //do nothing
            }
        }
        return result;
    }

    public static boolean hasAction(GameObject gameObject, String action) {
        ObjectComposition objComp = convertGameObjectToObjectComposition(gameObject);

        boolean result = Arrays.stream(objComp.getActions()).anyMatch(x -> x != null && x.equals(action));
        if (!result) {
            try {
                result = Arrays.stream(objComp.getImpostor().getActions()).anyMatch(x -> x != null && x.equalsIgnoreCase(action));
            } catch (Exception ex) {
                //do nothing
            }
        }
        return result;
    }

    /**
     * Imposter objects are objects that have their menu action changed but still remain the same object.
     * for example: farming patches
     */
    public static GameObject findObjectByImposter(int id, String action) {
        return findObjectByImposter(id, action, true);
    }

    public static GameObject findObjectByImposter(int id, String optionName, boolean exact) {
        List<GameObject> gameObjects = getGameObjects();

        if (gameObjects == null) return null;

        for (net.runelite.api.GameObject gameObject : gameObjects) {

            if (gameObject.getId() != id) continue;

            ObjectComposition objComp = convertGameObjectToObjectComposition(gameObject);

            if (objComp == null) continue;

            try {
                if (objComp.getImpostor() == null) continue;
                if (exact) {
                    if (Arrays.stream(objComp.getImpostor().getActions()).filter(Objects::nonNull)
                            .anyMatch((action) -> action.equalsIgnoreCase(optionName))) {
                        return gameObject;
                    }
                } else {
                    if (Arrays.stream(objComp.getImpostor().getActions()).filter(Objects::nonNull)
                            .anyMatch((action) -> action.toLowerCase().contains(optionName.toLowerCase()))) {
                        return gameObject;
                    }
                }
            } catch (Exception ex) {
                // do nothing
            }
        }

        return null;
    }

    public static GameObject findObjectByOption(String action) {
        return findObjectByOption(action, true);
    }

    public static GameObject findObjectByOption(String optionName, boolean exact) {
        List<GameObject> gameObjects = getGameObjects();

        if (gameObjects.isEmpty()) return null;

        for (net.runelite.api.GameObject gameObject : gameObjects) {

            ObjectComposition objComp = convertGameObjectToObjectComposition(gameObject);

            if (objComp == null) continue;

            if (exact) {
                if (Arrays.stream(objComp.getActions()).filter(Objects::nonNull).anyMatch((action) -> action.equalsIgnoreCase(optionName))) {
                    return gameObject;
                }
            } else {
                if (Arrays.stream(objComp.getActions()).filter(Objects::nonNull).anyMatch((action) -> action.toLowerCase().contains(optionName.toLowerCase()))) {
                    return gameObject;
                }
            }
        }

        return null;
    }


    public static GameObject findBank() {
        List<GameObject> gameObjects = getGameObjects();

        ArrayList<Integer> possibleBankIds = Rs2Reflection.getObjectByName(new String[]{"bank_booth"}, false);

        for (GameObject gameObject : gameObjects) {
            if (possibleBankIds.stream().noneMatch(x -> x == gameObject.getId())) continue;

            ObjectComposition objectComposition = convertGameObjectToObjectComposition(gameObject);

            if (objectComposition == null) continue;

            if (Arrays.stream(objectComposition.getActions())
                    .noneMatch(action ->
                            action != null && (
                                    action.toLowerCase().contains("bank") ||
                                            action.toLowerCase().contains("collect"))))
                continue;

            return gameObject;
        }

        return null;
    }

    public static GameObject findChest() {
        List<GameObject> gameObjects = getGameObjects();

        ArrayList<Integer> possibleBankIds = Rs2Reflection.getObjectByName(new String[]{"chest"}, false);

        for (GameObject gameObject : gameObjects) {
            if (possibleBankIds.stream().noneMatch(x -> x == gameObject.getId())) continue;

            ObjectComposition objectComposition = convertGameObjectToObjectComposition(gameObject);

            if (objectComposition == null) continue;

            if (Arrays.stream(objectComposition.getActions())
                    .noneMatch(action ->
                            action != null && (
                                    action.toLowerCase().contains("bank") ||
                                            action.toLowerCase().contains("collect"))))
                continue;

            return gameObject;
        }

        return null;
    }

    public static GameObject findBank(String action) {
        return findObjectByOption(action, false);
    }

    public static TileObject findObject(int[] ids) {
        TileObject tileObject = null;
        for (int id :
                ids) {
            tileObject = findObjectById(id);
        }
        return tileObject;
    }

    public static ObjectComposition convertGameObjectToObjectComposition(TileObject tileObject) {
        Player player = Microbot.getClient().getLocalPlayer();
        if (player.getLocalLocation().distanceTo(tileObject.getLocalLocation()) > 2400) return null;
        return Microbot.getClientThread().runOnClientThread(() -> Microbot.getClient().getObjectDefinition(tileObject.getId()));
    }

    public static WallObject findDoor(int id) {
        Scene scene = Microbot.getClient().getScene();
        Tile[][][] tiles = scene.getTiles();

        int z = Microbot.getClient().getPlane();
        for (int x = 0; x < Constants.SCENE_SIZE; ++x) {
            for (int y = 0; y < Constants.SCENE_SIZE; ++y) {
                Tile tile = tiles[z][x][y];

                if (tile == null) {
                    continue;
                }
                WallObject wall = tile.getWallObject();
                if (wall != null && wall.getId() == id)
                    return wall;
            }
        }
        return null;
    }


    public static GameObject getGameObject(LocalPoint localPoint) {
        Scene scene = Microbot.getClient().getScene();
        Tile[][][] tiles = scene.getTiles();

        int z = Microbot.getClient().getPlane();
        Tile tile = tiles[z][localPoint.getSceneX()][localPoint.getSceneY()];

        return Arrays.stream(tile.getGameObjects()).filter(Objects::nonNull).findFirst().orElse(null);
    }

    private static List<Tile> getTiles() {
        return getTiles(Constants.SCENE_SIZE);
    }

    private static List<Tile> getTiles(int sceneSize) {
        List<Tile> validTiles = new ArrayList<>();
        Scene scene = Microbot.getClient().getScene();
        Tile[][][] tiles = scene.getTiles();

        if (tiles == null) return new ArrayList<>();

        int z = Microbot.getClient().getPlane();
        for (int x = 0; x < sceneSize; ++x) {
            for (int y = 0; y < sceneSize; ++y) {
                Tile tile = tiles[z][x][y];

                if (tile == null) {
                    continue;
                }
                validTiles.add(tile);
            }
        }
        return validTiles;
    }

    public static List<GameObject> getGameObjects(int id) {
        List<Tile> tiles = getTiles();

        if (tiles.isEmpty()) return new ArrayList<>();

        List<GameObject> tileObjects = new ArrayList<>();
        for (Tile tile : tiles) {
            for (GameObject tileObject : tile.getGameObjects()) {
                if (tileObject != null
                        && tileObject.getSceneMinLocation().equals(tile.getSceneLocation()) && tileObject.getId() == id)
                    tileObjects.add(tileObject);
            }
        }

        return tileObjects.stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingInt(tile -> tile.getWorldLocation().distanceTo(Microbot.getClient().getLocalPlayer().getWorldLocation())))
                .collect(Collectors.toList());
    }

    public static List<GameObject> getGameObjects(String... objectNames) {
        return getGameObjects(Constants.SCENE_SIZE, objectNames);
    }

    public static List<GameObject> getGameObjects(int sceneSize, String... objectNames) {
        List<Tile> tiles = getTiles(sceneSize);

        if (tiles.isEmpty()) return new ArrayList<>();

        List<GameObject> tileObjects = new ArrayList<>();
        for (Tile tile : tiles) {
            for (GameObject tileObject : tile.getGameObjects()) {
                if (tileObject == null) continue;
                ObjectComposition objectComposition = Microbot.getClient().getObjectDefinition(tileObject.getId());
                if (tileObject.getSceneMinLocation().equals(tile.getSceneLocation())
                        && Arrays.stream(objectNames).anyMatch(obj -> obj.equalsIgnoreCase(objectComposition.getName())))
                    tileObjects.add(tileObject);
            }
        }

        return tileObjects.stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingInt(tile -> tile.getWorldLocation().distanceTo(Microbot.getClient().getLocalPlayer().getWorldLocation())))
                .collect(Collectors.toList());
    }

    public static List<GameObject> getGameObjects() {
        List<Tile> tiles = getTiles();

        if (tiles.isEmpty()) return new ArrayList<>();

        List<GameObject> tileObjects = new ArrayList<>();
        for (Tile tile : tiles) {
            for (GameObject tileObject : tile.getGameObjects()) {
                if (tileObject != null
                        && tileObject.getSceneMinLocation().equals(tile.getSceneLocation()))
                    tileObjects.add(tileObject);
            }
        }

        return tileObjects.stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingInt(tile -> tile.getWorldLocation().distanceTo(Microbot.getClient().getLocalPlayer().getWorldLocation())))
                .collect(Collectors.toList());
    }

    public static List<GameObject> getGameObjects(int distance, WorldPoint anchorPoint) {
        List<Tile> tiles = getTiles();

        if (tiles.isEmpty()) return new ArrayList<>();

        List<GameObject> tileObjects = new ArrayList<>();
        for (Tile tile : tiles) {
            for (GameObject tileObject : tile.getGameObjects()) {
                if (tileObject != null
                        && tileObject.getSceneMinLocation().equals(tile.getSceneLocation())
                        && tileObject.getWorldLocation().distanceTo2D(anchorPoint) <= distance)
                    tileObjects.add(tileObject);
            }
        }

        return tileObjects.stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingInt(tile -> tile.getWorldLocation().distanceTo(Microbot.getClient().getLocalPlayer().getWorldLocation())))
                .collect(Collectors.toList());
    }

    public static List<GroundObject> getGroundObjects() {
        List<Tile> tiles = getTiles();

        if (tiles.isEmpty()) return new ArrayList<>();

        List<GroundObject> tileObjects = new ArrayList<>();
        for (Tile tile : tiles) {
            tileObjects.add(tile.getGroundObject());
        }

        return tileObjects.stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingInt(tile -> tile.getWorldLocation().distanceTo(Microbot.getClient().getLocalPlayer().getWorldLocation())))
                .collect(Collectors.toList());
    }

    public static List<WallObject> getWallObjects() {
        List<Tile> tiles = getTiles();

        if (tiles.isEmpty()) return new ArrayList<>();

        List<WallObject> tileObjects = new ArrayList<>();
        for (Tile tile : tiles) {
            tileObjects.add(tile.getWallObject());
        }
        return tileObjects.stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingInt(tile -> tile.getWorldLocation().distanceTo(Microbot.getClient().getLocalPlayer().getWorldLocation())))
                .collect(Collectors.toList());
    }

    // private methods
    private static boolean clickObject(TileObject object) {
        return clickObject(object, "");
    }

    private static boolean clickObject(TileObject object, String action) {
        if (object == null) return false;
        if (Microbot.getClient().getLocalPlayer().getWorldLocation().distanceTo2D(object.getWorldLocation()) > 17) {
            Microbot.getWalker().walkFastCanvas(object.getWorldLocation());
            return false;
        }
        try {

            int param0 = 0;
            int param1 = 0;
            MenuAction menuAction = MenuAction.WALK;

            ObjectComposition objComp = convertGameObjectToObjectComposition(object);
            if (objComp == null) return false;

            if (object instanceof GameObject) {
                GameObject obj = (GameObject) object;
                if (obj.sizeX() > 1) {
                    param0 = obj.getLocalLocation().getSceneX() - obj.sizeX() / 2;
                } else {
                    param0 = obj.getLocalLocation().getSceneX();
                }

                if (obj.sizeY() > 1) {
                    param1 = obj.getLocalLocation().getSceneY() - obj.sizeY() / 2;
                } else {
                    param1 = obj.getLocalLocation().getSceneY();
                }
            } else {
                // Default objects like walls, groundobjects, decorationobjects etc...
                param0 = object.getLocalLocation().getSceneX();
                param1 = object.getLocalLocation().getSceneY();
            }

            int index = 0;
            if (action != null && !action.isEmpty()) {
                String[] actions;
                if (objComp.getImpostorIds() != null) {
                    actions = objComp.getImpostor().getActions();
                } else {
                    actions = objComp.getActions();
                }

                for (int i = 0; i < actions.length; i++) {
                    if (action.equalsIgnoreCase(actions[i])) {
                        index = i;
                        break;
                    }
                }
            }


            if (Microbot.getClient().isWidgetSelected()) {
                menuAction = MenuAction.WIDGET_TARGET_ON_GAME_OBJECT;
            } else if (index == 0) {
                menuAction = MenuAction.GAME_OBJECT_FIRST_OPTION;
            } else if (index == 1) {
                menuAction = MenuAction.GAME_OBJECT_SECOND_OPTION;
            } else if (index == 2) {
                menuAction = MenuAction.GAME_OBJECT_THIRD_OPTION;
            } else if (index == 3) {
                menuAction = MenuAction.GAME_OBJECT_FOURTH_OPTION;
            } else if (index == 4) {
                menuAction = MenuAction.GAME_OBJECT_FIFTH_OPTION;
            }

            if (!Rs2Camera.isTileOnScreen(object.getLocalLocation())) {
                Rs2Camera.turnTo(object);
            }

            Microbot.doInvoke(new NewMenuEntry(param0, param1, menuAction.getId(), object.getId(), -1, objComp.getName()), new Rectangle(object.getCanvasTilePoly().getBounds()));

            //Rs2Reflection.invokeMenu(param0, param1, menuAction.getId(), object.getId(),-1, "", "", -1, -1);

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

        return true;
    }

    public static boolean hasLineOfSight(TileObject tileObject) {
        if (tileObject == null) return true;
        if (tileObject instanceof GameObject) {
            GameObject gameObject = (GameObject) tileObject;
            WorldPoint worldPoint = WorldPoint.fromScene(Microbot.getClient(), gameObject.getSceneMinLocation().getX(), gameObject.getSceneMinLocation().getY(), gameObject.getPlane());
            return new WorldArea(
                    worldPoint,
                    gameObject.sizeX(),
                    gameObject.sizeY())
                    .hasLineOfSightTo(Microbot.getClient(), Microbot.getClient().getLocalPlayer().getWorldLocation().toWorldArea());
        }
        return true;
    }

//    public static List<GameObject> get(GameObjectPredicate filter) {
//        List<Tile> tiles = getTiles();
//
//        if (tiles.isEmpty()) return new ArrayList<>();
//
//        return tiles.stream().flatMap(x -> Arrays.stream(x.getGameObjects()).filter(filter)).collect(Collectors.toList());
//    }


    /**
     * @param filter
     * @return
     */
    public static List<Rs2GameObjectContainer> get(Predicate<Rs2GameObjectContainer> filter) {
        List<Tile> tiles = getTiles();

        if (tiles.isEmpty()) return new ArrayList<>();

        return tiles.stream().flatMap(x -> Arrays
                .stream(x.getGameObjects())
                .map(g -> Microbot.getClientThread().runOnClientThread(() -> new Rs2GameObjectContainer(g, Microbot.getClient().getObjectDefinition(g.getId()))
                ))
                .filter(o -> o.objectComposition != null && o.objectComposition.getActions() != null && o.objectComposition.getActions().length > 0
                        || o.objectComposition != null && o.objectComposition.getImpostor() != null
                        && o.objectComposition.getImpostor().getActions() != null
                        && o.objectComposition.getImpostor().getActions().length > 0)
                .filter(filter)).collect(Collectors.toList());
    }

    public static boolean interact(ObjectComposition objectComposition) {
        TileObject gameObject = get(objectComposition.getId()
        return clickObject(gameObject);
    }
}
