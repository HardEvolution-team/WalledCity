package com.electrifiedded.walledcitymod;

import java.util.HashMap;
import java.util.Random;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

public abstract class WorldGeneratorThread {
   public static final int LAYOUT_CODE_NOCODE = -1;
   public static final int LAYOUT_CODE_EMPTY = 0;
   public static final int LAYOUT_CODE_WALL = 1;
   public static final int LAYOUT_CODE_AVENUE = 2;
   public static final int LAYOUT_CODE_STREET = 3;
   public static final int LAYOUT_CODE_TOWER = 4;
   public static final int LAYOUT_CODE_TEMPLATE = 5;
   protected static final int[][] LAYOUT_CODE_OVERRIDE_MATRIX = new int[][]{
      {0, 1, 1, 1, 1, 1}, {0, 0, 0, 0, 0, 0}, {0, 0, 1, 1, 0, 0}, {0, 0, 1, 1, 1, 0}, {0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0}
   };
   public static final char[] LAYOUT_CODE_TO_CHAR = new char[]{' ', '#', '=', '-', '@', '&'};
   public final BuildingExplorationHandler master;
   public final World world;
   public final Random random;
   public final int chunkI;
   public final int chunkK;
   public final int triesPerChunk;
   public final double chunkTryProb;
   private int min_spawn_height = 0;
   private int max_spawn_height = 127;
   public boolean spawn_surface = true;
   HashMap<String, Integer> chestTries = null;
   HashMap<String, Object[][]> chestItems = null;
   public int backtrackLength = 9;

   public WorldGeneratorThread(BuildingExplorationHandler master, World world, Random random, int chunkI, int chunkK, int TriesPerChunk, double ChunkTryProb) {
      this.master = master;
      this.chestTries = master.chestTries;
      this.chestItems = master.chestItems;
      this.world = world;
      this.random = random;
      this.chunkI = chunkI;
      this.chunkK = chunkK;
      this.triesPerChunk = TriesPerChunk;
      this.chunkTryProb = ChunkTryProb;
      this.max_spawn_height = 255;
   }

   public abstract boolean generate(int var1, int var2, int var3);

   public boolean isLayoutGenerator() {
      return false;
   }

   public boolean layoutIsClear(int[] pt1, int[] pt2, int layoutCode) {
      return true;
   }

   public boolean layoutIsClear(Building building, boolean[][] templateLayout, int layoutCode) {
      return true;
   }

   public void setLayoutCode(int[] pt1, int[] pt2, int layoutCode) {
   }

   public void setLayoutCode(Building building, boolean[][] templateLayout, int layoutCode) {
   }

   public void run() {
      boolean success = false;
      int tries = 0;
      int j0 = 0;

      do {
         if (tries == 0 || this.random.nextDouble() < this.chunkTryProb) {
            int i0 = this.chunkI + this.random.nextInt(16);
            int k0 = this.chunkK + this.random.nextInt(16);
            if (this.spawn_surface) {
               j0 = Building.findSurfaceJ(this.world, i0, k0, 255, true, 3) + 1;
            } else {
               j0 = this.min_spawn_height + this.random.nextInt(this.max_spawn_height - this.min_spawn_height + 1);
            }

            if (j0 > 0 && Compat.biome(this.world, i0, k0) != net.minecraft.init.Biomes.OCEAN) {
               success = this.generate(i0, j0, k0);
            }
         }

         tries++;
      } while (!success && tries < this.triesPerChunk && j0 != -666);
   }

   public void setSpawnHeight(int min_spawn_height_, int max_spawn_height_, boolean spawn_surface_) {
      this.min_spawn_height = min_spawn_height_;
      this.max_spawn_height = max_spawn_height_;
      this.spawn_surface = spawn_surface_;
   }
}
