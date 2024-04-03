package net.runelite.client.plugins.microbot.dax.walker_engine;

import net.runelite.api.GameState;
import net.runelite.api.Tile;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.dax.walker_engine.bfs.BFS;
import net.runelite.client.plugins.microbot.dax.walker_engine.interaction_handling.PathObjectHandler;
import net.runelite.client.plugins.microbot.dax.walker_engine.local_pathfinding.PathAnalyzer;
import net.runelite.client.plugins.microbot.dax.walker_engine.local_pathfinding.Reachable;
import net.runelite.client.plugins.microbot.dax.walker_engine.navigation_utils.Charter;
import net.runelite.client.plugins.microbot.dax.walker_engine.navigation_utils.NavigationSpecialCase;
import net.runelite.client.plugins.microbot.dax.walker_engine.navigation_utils.ShipUtils;
import net.runelite.client.plugins.microbot.dax.walker_engine.real_time_collision.CollisionDataCollector;
import net.runelite.client.plugins.microbot.dax.walker_engine.real_time_collision.RealTimeCollisionTile;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static net.runelite.client.plugins.microbot.util.Global.sleep;

public class WalkerEngine {
    private int attemptsForAction;
    private final int failThreshold;
    private boolean navigating;
    private List<Tile> currentPath;

    private WalkerEngine(){
        attemptsForAction = 0;
        failThreshold = 3;
        navigating = false;
        currentPath = null;
    }

    public static WalkerEngine getInstance(){
        return (WalkerEngine) ScriptCache.get().computeIfAbsent("DaxWalker.WalkerEngine", k -> new WalkerEngine());
    }

    public boolean walkPath(List<Tile> path){
        return walkPath(path, null);
    }

    public List<Tile> getCurrentPath() {
        return currentPath;
    }

    /**
     *
     * @param path
     * @param walkingCondition
     * @return
     */
    public boolean walkPath(List<Tile> path, WalkingCondition walkingCondition){
        if (path.size() == 0) {

            System.out.println("Path is empty");
            return false;
        } else {
            System.out.println("Got path: " + path);
        }


        if (!handleTeleports(path)) {
            System.out.println("Failed to handle teleports...");
            return false;
        }


        navigating = true;
        currentPath = path;
        try {
            PathAnalyzer.DestinationDetails destinationDetails;
            resetAttempts();

            while (true) {

                if (Microbot.getClient().getGameState() != GameState.LOGGED_IN){
                    return false;
                }

                if (ShipUtils.isOnShip()) {
                    if (!ShipUtils.crossGangplank()) {
                        System.out.println("Failed to exit ship via gangplank.");
                        failedAttempt();
                    }
                    sleep(50);
                    continue;
                }

                if (isFailedOverThreshhold()) {
                    System.out.println("Too many failed attempts");
                    return false;
                }

                destinationDetails = PathAnalyzer.furthestReachableTile(path);
                if (PathUtils.getFurthestReachableTileInMinimap(path) == null || destinationDetails == null) {
                    System.out.println("Could not grab destination details.");
                    failedAttempt();
                    continue;
                }

                RealTimeCollisionTile currentNode = destinationDetails.getDestination();
                Tile assumedNext = destinationDetails.getAssumed();

                if (destinationDetails.getState() != PathAnalyzer.PathState.FURTHEST_CLICKABLE_TILE) {
                    System.out.println(destinationDetails.toString());
                }

                final RealTimeCollisionTile destination = currentNode;
                if (!Projection.isInMinimap(Projection.tileToMinimap(new Tile(destination.getX(), destination.getY(), destination.getZ())))) {
                    System.out.println("Closest tile in path is not in minimap: " + destination);
                    failedAttempt();
                    continue;
                }

                CustomConditionContainer conditionContainer = new CustomConditionContainer(walkingCondition);
                switch (destinationDetails.getState()) {
                    case DISCONNECTED_PATH:
                        if (currentNode.getTile().distanceTo(Player.getPosition()) > 10){
                            clickMinimap(currentNode);
                            WaitFor.milliseconds(1200, 3400);
                        }
                        NavigationSpecialCase.SpecialLocation specialLocation = NavigationSpecialCase.getLocation(currentNode.getTile()),
                                specialLocationDestination = NavigationSpecialCase.getLocation(assumedNext);
                        if (specialLocation != null && specialLocationDestination != null) {
                            System.out.println("[SPECIAL LOCATION] We are at " + specialLocation + " and our destination is " + specialLocationDestination);
                            if (!NavigationSpecialCase.handle(specialLocationDestination)) {
                                failedAttempt();
                            } else {
                                successfulAttempt();
                            }
                            break;
                        }

                        Charter.LocationProperty
                                locationProperty = Charter.LocationProperty.getLocation(currentNode.getTile()),
                                destinationProperty = Charter.LocationProperty.getLocation(assumedNext);
                        if (locationProperty != null && destinationProperty != null) {
                            System.out.println("Chartering to: " + destinationProperty);
                            if (!Charter.to(destinationProperty)) {
                                failedAttempt();
                            } else {
                                successfulAttempt();
                            }
                            break;
                        }
                        //DO NOT BREAK OUT
                    case OBJECT_BLOCKING:
                        Tile walkingTile = Reachable.getBestWalkableTile(destination.getTile(), new Reachable());
                        if (isDestinationClose(destination) || (walkingTile != null ? AccurateMouse.clickMinimap(walkingTile) : clickMinimap(destination))) {
                            System.out.println("Handling Object...");
                            if (!PathObjectHandler.handle(destinationDetails, path)) {
                                failedAttempt();
                            } else {
                                successfulAttempt();
                            }
                            break;
                        }
                        break;

                    case FURTHEST_CLICKABLE_TILE:
                        if (clickMinimap(currentNode)) {
                            long offsetWalkingTimeout = System.currentTimeMillis() + General.random(2500, 4000);
                            WaitFor.condition(10000, () -> {
                                switch (conditionContainer.trigger()) {
                                    case EXIT_OUT_WALKER_SUCCESS:
                                    case EXIT_OUT_WALKER_FAIL:
                                        return WaitFor.Return.SUCCESS;
                                }

                                PathAnalyzer.DestinationDetails furthestReachable = PathAnalyzer.furthestReachableTile(path);
                                PathFindingNode currentDestination = BFS.bfsClosestToPath(path, RealTimeCollisionTile.get(destination.getX(), destination.getY(), destination.getZ()));
                                if (currentDestination == null) {
                                    System.out.println("Could not walk to closest tile in path.");
                                    failedAttempt();
                                    return WaitFor.Return.FAIL;
                                }
                                int indexCurrentDestination = path.indexOf(currentDestination.getTile());

                                PathFindingNode closestToPlayer = PathAnalyzer.closestTileInPathToPlayer(path);
                                if (closestToPlayer == null) {
                                    System.out.println("Could not detect closest tile to player in path.");
                                    failedAttempt();
                                    return WaitFor.Return.FAIL;
                                }
                                int indexCurrentPosition = path.indexOf(closestToPlayer.getTile());
                                if (furthestReachable == null) {
                                    System.out.println("Furthest reachable is null/");
                                    return WaitFor.Return.FAIL;
                                }
                                int indexNextDestination = path.indexOf(furthestReachable.getDestination().getTile());
                                if (indexNextDestination - indexCurrentDestination > 5 || indexCurrentDestination - indexCurrentPosition < 5) {
                                    return WaitFor.Return.SUCCESS;
                                }
                                if (System.currentTimeMillis() > offsetWalkingTimeout && !Player.isMoving()){
                                    return WaitFor.Return.FAIL;
                                }
                                return WaitFor.milliseconds(100);
                            });
                        }
                        break;

                    case END_OF_PATH:
                        clickMinimap(destinationDetails.getDestination());
                        System.out.println("Reached end of path");
                        return true;
                }

                switch (conditionContainer.getResult()) {
                    case EXIT_OUT_WALKER_SUCCESS:
                        return true;
                    case EXIT_OUT_WALKER_FAIL:
                        return false;
                }

                WaitFor.milliseconds(50, 100);

            }
        } finally {
            navigating = false;
        }
    }

    boolean isNavigating() {
        return navigating;
    }

    boolean isDestinationClose(PathFindingNode pathFindingNode){
        final Tile playerPosition = Player.getPosition();
        return new Tile(pathFindingNode.getX(), pathFindingNode.getY(), pathFindingNode.getZ()).isClickable()
                && playerPosition.distanceTo(new Tile(pathFindingNode.getX(), pathFindingNode.getY(), pathFindingNode.getZ())) <= 12
                && (BFS.isReachable(RealTimeCollisionTile.get(playerPosition.getX(), playerPosition.getY(), playerPosition.getPlane()), RealTimeCollisionTile.get(pathFindingNode.getX(), pathFindingNode.getY(), pathFindingNode.getZ()), 200));
    }

    public boolean clickMinimap(PathFindingNode pathFindingNode){
        final Tile playerPosition = Player.getPosition();
        if (playerPosition.distanceTo(pathFindingNode.getTile()) <= 1){
            return true;
        }
        PathFindingNode randomNearby = BFS.getRandomTileNearby(pathFindingNode);

        if (randomNearby == null){
            System.out.println("Unable to generate randomization.");
            return false;
        }

        System.out.println("Randomize(" + pathFindingNode.getX() + "," + pathFindingNode.getY() + "," + pathFindingNode.getZ() + ") -> (" + randomNearby.getX() + "," + randomNearby.getY() + "," + randomNearby.getZ() + ")");
        return AccurateMouse.clickMinimap(new Tile(randomNearby.getX(), randomNearby.getY(), randomNearby.getZ())) || AccurateMouse.clickMinimap(new Tile(pathFindingNode.getX(), pathFindingNode.getY(), pathFindingNode.getZ()));
    }

    public void hoverMinimap(PathFindingNode pathFindingNode){
        if (pathFindingNode == null){
            return;
        }
        Point point = Projection.tileToMinimap(new Tile(pathFindingNode.getX(), pathFindingNode.getY(), pathFindingNode.getZ()));
        Mouse.move(point);
    }

    private boolean resetAttempts(){
        return successfulAttempt();
    }

    private boolean successfulAttempt(){
        attemptsForAction = 0;
        return true;
    }

    private void failedAttempt(){
        if (Camera.getCameraAngle() < 90) {
            Camera.setCameraAngle(General.random(90, 100));
        }
        if (++attemptsForAction > 1) {
            Camera.setCameraRotation(General.random(0, 360));
        }
        System.out.println("Failed attempt on action.");
        WaitFor.milliseconds(450 * (attemptsForAction + 1), 850 * (attemptsForAction + 1));
        CollisionDataCollector.generateRealTimeCollision();
    }

    private boolean isFailedOverThreshhold(){
        return attemptsForAction >= failThreshold;
    }

    private class CustomConditionContainer {
        private WalkingCondition walkingCondition;
        private WalkingCondition.State result;
        CustomConditionContainer(WalkingCondition walkingCondition){
            this.walkingCondition = walkingCondition;
            this.result = WalkingCondition.State.CONTINUE_WALKER;
        }
        public WalkingCondition.State trigger(){
            result = (walkingCondition != null ? walkingCondition.action() : result);
            return result != null ? result : WalkingCondition.State.CONTINUE_WALKER;
        }
        public WalkingCondition.State getResult() {
            return result;
        }
    }

    @Override
    public String getName() {
        return "Walker Engine";
    }

    private boolean handleTeleports(List<Tile> path) {
        Tile startPosition = path.get(0);
        Tile playerPosition = Player.getPosition();
        if(playerPosition.getPosition().distanceTo(startPosition) < 10) {
            System.out.println("We are less than 10 tiles away from the start tile; skipping teleports.");
            return true;
        }
        PathAnalyzer.DestinationDetails destinationDetails = PathAnalyzer.furthestReachableTile(path);
        if(destinationDetails != null && destinationDetails.getAssumed().isClickable() && destinationDetails.getAssumed().distanceTo(playerPosition) < 20){
            System.out.println("We are already somewhere along the path. Destination details: " + destinationDetails);
            return true;
        }
        if(Banking.isBankScreenOpen())
            Banking.close();
        Teleport targetTeleport = Arrays.stream(Teleport.values()).filter(t ->
                        !DaxWalker.getBlacklist().contains(t) && t.isAtTeleportSpot(startPosition) &&
                                !t.isAtTeleportSpot(playerPosition) && t.getRequirement().satisfies())
                .min(Comparator.comparingInt(Teleport::getMoveCost)).orElse(null);
        if(targetTeleport == null){
            System.out.println("No teleports are necessary for this path.");
            return true;
        }
        System.out.println("Using teleport: " + targetTeleport + " with cost: " + targetTeleport.getMoveCost());
        return targetTeleport.trigger() && (WaitFor.condition(General.random(targetTeleport.getMinWaitTime(), targetTeleport.getMaxWaitTime()),
                () -> startPosition.distanceTo(Player.getPosition()) < 10 ?
                        WaitFor.Return.SUCCESS : WaitFor.Return.IGNORE) == WaitFor.Return.SUCCESS);
    }
}
