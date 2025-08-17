# mineFind

Flood-fill utilities for Bukkit/Spigot/Paper plugins.
Find contiguous blocks of the same `Material` around an origin, with 4-, 6-, or 26-way connectivity.
Includes helpers to sort found blocks by distance to a player.

## Requirements

* Java 21+
* Bukkit/Spigot/Paper

## Installation

Add the library as a dependency.

```kotlin
dependencies {
    implementation("com.github.MenelBOT:MineFind:1.0.0")
}
```

```groovy
dependencies {
    implementation 'com.github.MenelBOT:MineFind:1.0.0'
}
```

```xml
<dependency>
  <groupId>com.github.MenelBOT</groupId>
  <artifactId>MineFind</artifactId>
  <version>1.0.0</version>
  <scope>shadow</scope>
</dependency>
```

## Quick start

```java
import io.github.menelbot.mineFind.FindModes;
import io.github.menelbot.mineFind.Finder;
import org.bukkit.block.Block;

import java.util.List;

public class Demo {
  // Find up to 128 connected blocks matching origin type (6-way), then do something.
  public void run(Block origin) {
    List<Block> cluster = Finder.find(
            FindModes.SIX_SIDED,
            origin,
            b -> true,   // permission check predicate
            128          // hard cap to bound work
    );

    cluster.forEach(b -> b.getWorld().spawnParticle(
            org.bukkit.Particle.VILLAGER_HAPPY,
            b.getLocation().add(0.5, 0.5, 0.5), 1)
    );
  }
}
```

### `enum FindModes`

* `FOUR_SIDED` — 2D connectivity on X/Z (N, S, E, W).
* `SIX_SIDED` — 3D orthogonal connectivity (±X, ±Y, ±Z).
* `TWENTY_SIX_SIDED` — 3D including diagonals (all neighbors in a 3×3×3 cube minus center).

You can either call one of the `Finder` find functions, or use the generic `Finder.find` function
and pass it a member of `FindModes`.

**`Finder.find` Parameters**

* `mode` — connectivity rule.
* `origin` — starting block. Its `Material` defines the target type.
* `permissionCheck` — predicate called for every candidate block. Return `true` to include, `false` to skip. Use it for region/claim checks or custom filters.
* `cap` — maximum number of blocks to return. Required to bound runtime.

**Returns**

A list including `origin` first. Empty if `origin == null`, `cap <= 0`, or `permissionCheck` fails for `origin`.

### Mode-specific finders

* `find4Neighbor(Block origin, Predicate<Block> permissionCheck, int cap) -> List<Block>`
* `find6Neighbor(Block origin, Predicate<Block> permissionCheck, int cap) -> List<Block>`
* `find26(Block origin, Predicate<Block> permissionCheck, int cap) -> List<Block>`

Breadth-first search. Stops when queue is exhausted or `cap` is reached.

### Sorting helpers

* `sortBlocksByDistance3D(Set<Block> blocks, Player player) -> ArrayDeque<Block>`
  Ascending by squared 3D distance to player eye position center-adjusted to block center.

* `sortBlocksByHorizontalDistance(Set<Block> blocks, Player player) -> ArrayDeque<Block>`
  Ascending by squared horizontal (XZ) distance to block center.

## Usage patterns

### Basic vein miner

```java
List<Block> vein = Finder.find(
    FindModes.SIX_SIDED,
    clickedBlock,
    b -> b.breakNaturally(), // or run your real permission check and return true/false
    256
);
```

### With a protection check

```java
Predicate<Block> canEdit = b -> myRegionApi.canPlayerModify(player, b.getLocation());
List<Block> regionSafe = Finder.find(FindModes.TWENTY_SIX_SIDED, origin, canEdit, 200);
```

### Process nearest first

```java
List<Block> list = Finder.find(FindModes.FOUR_SIDED, someOriginBlock, () -> true, 512);
ArrayDeque<Block> work = Finder.sortBlocksByHorizontalDistance(set, player);

while (!work.isEmpty()) {
    Block b = work.poll();
    // handle b
}
```

## Behavior notes

* Target `Material` is locked to `origin.getType()`. Mixed types are not traversed.
* BFS ensures stable expansion and tends to prefer nearer blocks, but not strictly by metric distance. Use the sorters if strict ordering is needed.
* `cap` caps both traversal and output size. Choose conservatively to protect the server tick.
* `permissionCheck` is evaluated on `origin` and all neighbors. If it returns `false` the block is skipped.
* Uses `Block#getRelative(...)` and `BlockFace` offsets. No chunk loads are forced explicitly; Bukkit may load chunks when accessing neighbors.
* Threading: interact with Bukkit world objects on the main server thread.

## Complexity

* Time: `O(min(N, cap))`, where `N` is the reachable component size under the chosen connectivity and predicate.
* Memory: `O(min(N, cap))` for queue, and output.

## Edge cases

* `origin == null` → returns `Collections.emptyList()`.
* `cap <= 0` → returns empty.
* `permissionCheck.test(origin) == false` → returns empty.
* If `origin` changes type during traversal, comparisons use the original type captured before the search.

## License

**Menel Permissive Non-Resale License (MPNRL-1.0)**
Copyright (c) 2025 Menel

You may use, modify, and distribute this library freely, **including in commercial plugins**,
**provided** that you do **not** sell or license this library itself for a fee, except as a
dependency of a larger work which substantially extends its functionality.

Plain English:

* ✅ Free plugins: fully allowed
* ✅ Paid plugins using this as a dependency: allowed
* ❌ Selling this library directly: not allowed
* ❌ Premium wrappers with minimal changes: not allowed

Full license text: [LICENSE](./LICENSE)


---
