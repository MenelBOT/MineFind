package io.menelbot.github.mineFind;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;
import java.util.function.Predicate;

@SuppressWarnings("unused")
public class Finder {
    private static final Vector[] OFF4 = new Vector[] {
            new Vector(1, 0, 0),   // East
            new Vector(-1, 0, 0),  // West
            new Vector(0, 0, 1),   // South
            new Vector(0, 0, -1)   // North
    };

    private static final BlockFace[] SIX_FACES = {
            BlockFace.NORTH, BlockFace.SOUTH,
            BlockFace.EAST,  BlockFace.WEST,
            BlockFace.UP,    BlockFace.DOWN
    };

    private static final int[][] OFF26 = {
            {-1, -1, -1}, {-1, -1, 0}, {-1, -1, 1},
            {-1,  0, -1}, {-1,  0, 0}, {-1,  0, 1},
            {-1,  1, -1}, {-1,  1, 0}, {-1,  1, 1},

            {0, -1, -1}, {0, -1, 0}, {0, -1, 1},
            {0,  0, -1},             {0,  0, 1},
            {0,  1, -1}, {0,  1, 0}, {0,  1, 1},

            {1, -1, -1}, {1, -1, 0}, {1, -1, 1},
            {1,  0, -1}, {1,  0, 0}, {1,  0, 1},
            {1,  1, -1}, {1,  1, 0}, {1,  1, 1}
    };

    @ApiStatus.AvailableSince("1.0.0")
    public static List<Block> find(FindModes mode, Block origin, Predicate<Block> permissionCheck, int cap) {
        return switch(mode) {
            case FOUR_SIDED -> find4Neighbor(origin, permissionCheck, cap);
            case SIX_SIDED -> find6Neighbor(origin, permissionCheck, cap);
            case TWENTY_SIX_SIDED -> find26(origin, permissionCheck, cap);
        };
    }

    @ApiStatus.AvailableSince("1.0.0")
    public static List<Block> find4Neighbor(Block origin, Predicate<Block> permissionCheck, int cap) {
        if (origin == null
                || cap <= 0
                || !permissionCheck.test(origin)
        ) return Collections.emptyList();

        Material target = origin.getType();

        Queue<Block> q = new ArrayDeque<>();
        Set<Block> visited = new HashSet<>();
        List<Block> out = new ArrayList<>(Math.min(cap, 32));

        q.add(origin);
        visited.add(origin);
        out.add(origin);

        while(!q.isEmpty() && visited.size() < cap) {
            Block cur = q.poll();

            for (Vector dir : OFF4) {
                Block nb = cur.getRelative(dir.getBlockX(), 0, dir.getBlockZ());
                if (visited.contains(nb)
                        || nb.getType() != target
                        || !permissionCheck.test(nb)
                ) continue;

                visited.add(nb);
                q.add(nb);
                out.add(nb);

                if (out.size() >= cap) break;
            }
        }

        return out;
    }

    @ApiStatus.AvailableSince("1.0.0")
    public static List<Block> find6Neighbor(Block origin, Predicate<Block> permissionCheck, int cap) {
        if (origin == null
            || cap <= 0
            || !permissionCheck.test(origin)
        ) return Collections.emptyList();

        Material target = origin.getType();

        Queue<Block> q = new ArrayDeque<>();
        Set<Block> visited = new HashSet<>();
        List<Block> out = new ArrayList<>(Math.min(cap, 64));

        q.add(origin);
        visited.add(origin);
        out.add(origin);

        while (!q.isEmpty() && out.size() < cap) {
            Block cur = q.poll();

            for (BlockFace f : SIX_FACES) {
                Block nb = cur.getRelative(f);
                if (visited.contains(nb)
                    || nb.getType() != target
                    || !permissionCheck.test(nb)
                ) continue;

                visited.add(nb);
                q.add(nb);
                out.add(nb);

                if (out.size() >= cap) break;
            }
        }

        return out;
    }

    @ApiStatus.AvailableSince("1.0.0")
    public static List<Block> find26(Block origin, Predicate<Block> permissionCheck, int cap) {
        if (origin == null
                || cap <= 0
                || !permissionCheck.test(origin)
        ) return Collections.emptyList();

        Material target = origin.getType();

        Queue<Block> q = new ArrayDeque<>();
        Set<Block> visited = new HashSet<>();
        List<Block> out = new ArrayList<>(Math.min(cap, 64));

        q.add(origin);
        visited.add(origin);
        out.add(origin);

        while (!q.isEmpty() && out.size() < cap) {
            Block cur = q.poll();

            for (int[] d : OFF26) {
                Block nb = cur.getRelative(d[0], d[1], d[2]);
                if (visited.contains(nb)
                    || nb.getType() != target
                    || !permissionCheck.test(nb)) continue;

                visited.add(nb);
                q.add(nb);
                out.add(nb);

                if (out.size() >= cap) break;
            }
        }

        return out;
    }

    @ApiStatus.AvailableSince("1.0.0")
    public static ArrayDeque<Block> sortBlocksByDistance3D(List<Block> blocks, Player player) {
        final double px = player.getLocation().getX();
        final double py = player.getLocation().getY();
        final double pz = player.getLocation().getZ();

        List<Block> sorted = blocks.stream()
                .sorted(Comparator.comparingDouble(b -> {
                    double dx = (b.getX() + 0.5) - px;
                    double dy = (b.getY() + 0.5) - py;
                    double dz = (b.getZ() + 0.5) - pz;
                    return dx*dx + dy*dy + dz*dz;
                }))
                .toList();

        return new ArrayDeque<>(sorted);
    }

    @ApiStatus.AvailableSince("1.0.0")
    public static ArrayDeque<Block> sortBlocksByHorizontalDistance(List<Block> blocks, Player player) {
        final double playerX = player.getLocation().getX();
        final double playerZ = player.getLocation().getZ();

        List<Block> sortedBlocks = blocks.stream()
                .sorted(Comparator.comparingDouble(block -> {
                    double dx = block.getX() + 0.5 - playerX;
                    double dz = block.getZ() + 0.5 - playerZ;
                    return dx * dx + dz * dz;
                }))
                .toList();
        return new ArrayDeque<>(sortedBlocks);
    }
}
