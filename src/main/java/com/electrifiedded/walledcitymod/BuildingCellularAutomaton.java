package com.electrifiedded.walledcitymod;

import com.electrifiedded.archaiclib.legacy.world.LegacyWorld;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.EnumSkyBlock;

public class BuildingCellularAutomaton extends Building {
   private static final byte DEAD = 0;
   private static final byte ALIVE = 1;
   private final float MEAN_SIDE_LENGTH_PER_POPULATE = 15.0F;
   private static final int HOLE_FLOOR_BUFFER = 2;
   private static final int UNREACHED = -1;
   private static final int SYMMETRIC_SEED_MIN_WIDTH = 4;
   private static final int CIRCULAR_SEED_MIN_WIDTH = 4;
   public static final TemplateRule DEFAULT_MEDIUM_LIGHT_NARROW_SPAWNER_RULE = new TemplateRule(
      new Block[]{
         Blocks.MOB_SPAWNER, Blocks.MOB_SPAWNER, Blocks.MOB_SPAWNER, Blocks.MOB_SPAWNER, Blocks.MOB_SPAWNER, Blocks.MOB_SPAWNER
      },
      new int[]{0, 0, 0, 0, 0, 0},
      new String[]{"Blaze", "Blaze", "Blaze", "Silverfish", "Silverfish", "LavaSlime"},
      100
   );
   public static final TemplateRule DEFAULT_MEDIUM_LIGHT_WIDE_SPAWNER_RULE = new TemplateRule(
      new Block[]{
         Blocks.MOB_SPAWNER, Blocks.MOB_SPAWNER, Blocks.MOB_SPAWNER, Blocks.MOB_SPAWNER, Blocks.MOB_SPAWNER, Blocks.MOB_SPAWNER
      },
      new int[]{0, 0, 0, 0, 0, 0},
      new String[]{"Blaze", "Silverfish", "Silverfish", "CaveSpider", "CaveSpider", "Spider"},
      100
   );
   public static final TemplateRule DEFAULT_LOW_LIGHT_SPAWNER_RULE = new TemplateRule(
      new Block[]{Blocks.MOB_SPAWNER, Blocks.MOB_SPAWNER, Blocks.MOB_SPAWNER, Blocks.MOB_SPAWNER, Blocks.MOB_SPAWNER},
      new int[]{0, 0, 0, 0, 0},
      new String[]{"UPRIGHT", "UPRIGHT", "Silverfish", "LavaSlime", "CaveSpider"},
      100
   );
   private byte[][][] layers = (byte[][][])null;
   public byte[][] seed = (byte[][])null;
   private byte[][] caRule = (byte[][])null;
   private final TemplateRule lowLightSpawnerRule;
   private final TemplateRule mediumLightNarrowSpawnerRule;
   private final TemplateRule mediumLightWideSpawnerRule;
   int[][] fBB;
   int zGround = 0;

   public BuildingCellularAutomaton(
      WorldGeneratorThread wgt_,
      TemplateRule bRule_,
      int bDir_,
      int axXHand_,
      boolean centerAligned_,
      int width,
      int height,
      int length,
      byte[][] seed_,
      byte[][] caRule_,
      TemplateRule[] spawnerRules,
      int[] sourcePt
   ) {
      super(0, wgt_, bRule_, bDir_, axXHand_, centerAligned_, new int[]{width, height, length}, sourcePt);
      this.seed = seed_;
      if ((this.bWidth - this.seed.length) % 2 != 0) {
         this.bWidth++;
      }

      if ((this.bLength - this.seed[0].length) % 2 != 0) {
         this.bLength++;
      }

      this.caRule = caRule_;
      this.mediumLightNarrowSpawnerRule = spawnerRules != null ? spawnerRules[0] : DEFAULT_MEDIUM_LIGHT_NARROW_SPAWNER_RULE;
      this.mediumLightWideSpawnerRule = spawnerRules != null ? spawnerRules[1] : DEFAULT_MEDIUM_LIGHT_WIDE_SPAWNER_RULE;
      this.lowLightSpawnerRule = spawnerRules != null ? spawnerRules[2] : DEFAULT_LOW_LIGHT_SPAWNER_RULE;
   }

   public void build(boolean SmoothWithStairs, boolean makeFloors) {
      Block stairsBlock = SmoothWithStairs ? this.bRule.primaryBlock.toStair() : Blocks.AIR;
      TemplateRule[] stairs = new TemplateRule[]{
         new TemplateRule(stairsBlock, STAIRS_DIR_TO_META[0], this.bRule.chance),
         new TemplateRule(stairsBlock, STAIRS_DIR_TO_META[1], this.bRule.chance),
         new TemplateRule(stairsBlock, STAIRS_DIR_TO_META[2], this.bRule.chance),
         new TemplateRule(stairsBlock, STAIRS_DIR_TO_META[3], this.bRule.chance)
      };
      int[] floorBlockCounts = new int[this.bHeight];
      ArrayList<ArrayList<int[]>> floorBlocks = new ArrayList<>();

      for (int m = 0; m < this.bHeight; m++) {
         floorBlockCounts[m] = 0;
         floorBlocks.add(new ArrayList<>());
      }

      int centerX = (this.fBB[0][0] + this.fBB[1][0]) / 2;
      int[][] holeLimits = new int[this.bLength][2];

      for (int y = 0; y < this.bLength; y++) {
         holeLimits[y][0] = centerX;
         holeLimits[y][1] = centerX + 1;
      }

      for (int z = this.bHeight - 1; z >= 0; z--) {
         for (int x = 0; x < this.bWidth; x++) {
            for (int y = 0; y < this.bLength; y++) {
               if (this.layers[z][x][y] == 1) {
                  this.setBlockLocal(x, z, y, this.bRule);
               } else if (z > 0 && this.layers[z - 1][x][y] == 1) {
                  if (z < this.bHeight - 5 && this.fBB[0][z] <= x && x <= this.fBB[1][z] && this.fBB[2][z] <= y && y <= this.fBB[3][z]) {
                     if (makeFloors) {
                        floorBlocks.get(z).add(new int[]{x, y});
                        if (x - 2 < holeLimits[y][0]) {
                           holeLimits[y][0] = Math.max(this.fBB[0][z], x - 2);
                        }

                        if (x + 2 > holeLimits[y][1]) {
                           holeLimits[y][1] = Math.min(this.fBB[1][z], x + 2);
                        }

                        floorBlockCounts[z]++;
                     }
                  } else if (stairsBlock != Blocks.AIR && (z == this.bHeight - 1 || this.layers[z + 1][x][y] != 1)) {
                     if (y + 1 >= this.bLength
                        || this.layers[z][x][y + 1] != 1
                        || y - 1 >= 0
                           && (
                              this.layers[z][x][y - 1] == 1
                                 || x + 1 != this.bWidth && this.layers[z][x + 1][y] != 1 && this.layers[z][x + 1][y - 1] == 1
                                 || x - 1 >= 0 && this.layers[z][x - 1][y] != 1 && this.layers[z][x - 1][y - 1] == 1
                                 || this.random.nextInt(100) >= this.bRule.chance
                           )) {
                        if (y - 1 < 0
                           || this.layers[z][x][y - 1] != 1
                           || y + 1 != this.bLength
                              && (
                                 this.layers[z][x][y + 1] == 1
                                    || x + 1 != this.bWidth && this.layers[z][x + 1][y] != 1 && this.layers[z][x + 1][y + 1] == 1
                                    || x - 1 >= 0 && this.layers[z][x - 1][y] != 1 && this.layers[z][x - 1][y + 1] == 1
                                    || this.random.nextInt(100) >= this.bRule.chance
                              )) {
                           if (x + 1 >= this.bWidth
                              || this.layers[z][x + 1][y] != 1
                              || x - 1 >= 0
                                 && (
                                    this.layers[z][x - 1][y] == 1
                                       || y + 1 != this.bLength && this.layers[z][x][y + 1] != 1 && this.layers[z][x - 1][y + 1] == 1
                                       || y - 1 >= 0 && this.layers[z][x][y - 1] != 1 && this.layers[z][x - 1][y - 1] == 1
                                       || this.random.nextInt(100) >= this.bRule.chance
                                 )) {
                              if (x - 1 >= 0
                                 && this.layers[z][x - 1][y] == 1
                                 && (
                                    x + 1 == this.bWidth
                                       || this.layers[z][x + 1][y] != 1
                                          && (y + 1 == this.bLength || this.layers[z][x][y + 1] == 1 || this.layers[z][x + 1][y + 1] != 1)
                                          && (y - 1 < 0 || this.layers[z][x][y - 1] == 1 || this.layers[z][x + 1][y - 1] != 1)
                                          && this.random.nextInt(100) < this.bRule.chance
                                 )) {
                                 this.setBlockLocal(x, z, y, stairs[3]);
                              }
                           } else {
                              this.setBlockLocal(x, z, y, stairs[1]);
                           }
                        } else {
                           this.setBlockLocal(x, z, y, stairs[2]);
                        }
                     } else {
                        this.setBlockLocal(x, z, y, stairs[0]);
                     }
                  }
               }
            }
         }

         for (int yx = 0; yx < this.bLength; yx++) {
            for (int x = holeLimits[yx][0] + 1; x <= holeLimits[yx][1] - 1; x++) {
               if (this.layers[z][x][yx] != 1 && !BlockProperties.get(this.getBlockIdLocal(x, z, yx)).isArtificial) {
                  this.setBlockLocal(x, z, yx, Blocks.AIR);
               }
            }
         }

         if (z % 2 == 0) {
            for (int yx = 0; yx < this.bLength; yx++) {
               holeLimits[yx][0] = holeLimits[yx][0] < centerX ? holeLimits[yx][0] + 1 : centerX;
               holeLimits[yx][1] = holeLimits[yx][1] > centerX + 1 ? holeLimits[yx][1] - 1 : centerX + 1;
            }
         }
      }

      this.flushDelayed();
      if (makeFloors) {
         this.buildFloors(floorBlockCounts, floorBlocks);
      }
   }

   public void buildFloors(int[] floorBlockCounts, ArrayList<ArrayList<int[]>> floorBlocks) {
      ArrayList<int[]> floors = new ArrayList<>();

      while (true) {
         int maxFloorBlocks = floorBlockCounts[1];
         int maxFloor = 1;

         for (int floor = 2; floor < this.bHeight - 5; floor++) {
            if (floorBlockCounts[floor - 1] + floorBlockCounts[floor] + floorBlockCounts[floor + 1] > maxFloorBlocks) {
               maxFloorBlocks = floorBlockCounts[floor - 1] + floorBlockCounts[floor] + floorBlockCounts[floor + 1];
               maxFloor = floor;
            }
         }

         if (maxFloorBlocks <= 20) {
            Collections.sort(floors, new Comparator() {
               @Override
               public int compare(Object o1, Object o2) {
                  int a = ((int[])o1)[0];
                  int b = ((int[])o2)[0];
                  return a < b ? -1 : (a == b ? 0 : 1);
               }
            });

            for (int m = floors.size() - 1; m >= 0; m--) {
               if (m > 0) {
                  this.populateLadderOrStairway(floors.get(m - 1)[0], floors.get(m)[0], true);
               }

               do {
                  this.populateFloor(floors.get(m)[0], floors.get(m)[1]);
               } while (this.random.nextFloat() < 1.0F - 15.0F / MathHelper.sqrt(floors.get(m)[1]));
            }

            return;
         }

         boolean[][] layout = new boolean[this.bWidth][this.bLength];

         for (int x = 0; x < this.bWidth; x++) {
            for (int y = 0; y < this.bLength; y++) {
               layout[x][y] = false;
            }
         }

         for (int[] pt : floorBlocks.get(maxFloor - 1)) {
            this.makeFloorCrossAt(pt[0], maxFloor, pt[1], layout);
         }

         for (int[] pt : floorBlocks.get(maxFloor)) {
            this.makeFloorCrossAt(pt[0], maxFloor, pt[1], layout);
         }

         for (int[] pt : floorBlocks.get(maxFloor + 1)) {
            this.makeFloorCrossAt(pt[0], maxFloor, pt[1], layout);
         }

         floors.add(new int[]{maxFloor, maxFloorBlocks});
         if (maxFloor - 3 >= 0) {
            floorBlockCounts[maxFloor - 3] = 0;
         }

         if (maxFloor - 2 >= 0) {
            floorBlockCounts[maxFloor - 2] = 0;
         }

         floorBlockCounts[maxFloor - 1] = 0;
         floorBlockCounts[maxFloor] = 0;
         floorBlockCounts[maxFloor + 1] = 0;
         if (maxFloor + 2 < this.bHeight) {
            floorBlockCounts[maxFloor + 2] = 0;
         }

         if (maxFloor + 3 < this.bHeight) {
            floorBlockCounts[maxFloor + 3] = 0;
         }

         this.flushDelayed();
      }
   }

   public boolean plan(boolean bury, int MinHeightBeforeOscillation) {
      this.layers = new byte[this.bHeight][this.bWidth][this.bLength];

      for (int z = 0; z < this.bHeight; z++) {
         for (int x = 0; x < this.bWidth; x++) {
            for (int y = 0; y < this.bLength; y++) {
               this.layers[z][x][y] = 0;
            }
         }
      }

      int[][] BB = new int[4][this.bHeight];
      BB[0][0] = (this.bWidth - this.seed.length) / 2;
      BB[1][0] = (this.bWidth - this.seed.length) / 2 + this.seed.length - 1;
      BB[2][0] = (this.bLength - this.seed[0].length) / 2;
      BB[3][0] = (this.bLength - this.seed[0].length) / 2 + this.seed[0].length - 1;

      for (int x = 0; x < this.seed.length; x++) {
         for (int y = 0; y < this.seed[0].length; y++) {
            this.layers[0][BB[0][0] + x][BB[2][0] + y] = this.seed[x][y];
         }
      }

      int crystallizationHeight = -1;

      for (int z = 1; z < this.bHeight; z++) {
         boolean layerIsAlive = false;
         boolean layerIsFixed = crystallizationHeight == -1 && z >= 1;
         boolean layerIsPeriod2 = crystallizationHeight == -1 && z >= 2;
         boolean layerIsPeriod3 = crystallizationHeight == -1 && z >= 3;
         BB[0][z] = this.bWidth / 2;
         BB[1][z] = this.bWidth / 2;
         BB[2][z] = this.bLength / 2;
         BB[3][z] = this.bLength / 2;

         for (int x = Math.max(0, BB[0][z - 1] - 1); x <= Math.min(this.bWidth - 1, BB[1][z - 1] + 1); x++) {
            for (int y = Math.max(0, BB[2][z - 1] - 1); y <= Math.min(this.bLength - 1, BB[3][z - 1] + 1); y++) {
               int neighbors = 0;

               for (int x1 = Math.max(x - 1, 0); x1 <= Math.min(x + 1, this.bWidth - 1); x1++) {
                  for (int y1 = Math.max(y - 1, 0); y1 <= Math.min(y + 1, this.bLength - 1); y1++) {
                     if (x1 != x || y1 != y) {
                        neighbors += this.layers[z - 1][x1][y1];
                     }
                  }
               }

               this.layers[z][x][y] = this.caRule[this.layers[z - 1][x][y]][neighbors];
               if (this.layers[z][x][y] == 1) {
                  if (x < BB[0][z]) {
                     BB[0][z] = x;
                  }

                  if (x > BB[1][z]) {
                     BB[1][z] = x;
                  }

                  if (y < BB[2][z]) {
                     BB[2][z] = y;
                  }

                  if (y > BB[3][z]) {
                     BB[3][z] = y;
                  }

                  layerIsAlive = true;
               }

               if (this.layers[z][x][y] != this.layers[z - 1][x][y]) {
                  layerIsFixed = false;
               }

               if (z >= 2 && this.layers[z][x][y] != this.layers[z - 2][x][y]) {
                  layerIsPeriod2 = false;
               }

               if (z >= 3 && this.layers[z][x][y] != this.layers[z - 3][x][y]) {
                  layerIsPeriod3 = false;
               }
            }
         }

         if (!layerIsAlive) {
            if (z <= MinHeightBeforeOscillation) {
               return false;
            }

            this.bHeight = z;
            break;
         }

         if (layerIsFixed) {
            if (z - 1 <= MinHeightBeforeOscillation) {
               return false;
            }

            crystallizationHeight = z - 1;
         }

         if (layerIsPeriod2) {
            if (z - 2 <= MinHeightBeforeOscillation) {
               return false;
            }

            crystallizationHeight = z - 2;
         }

         if (layerIsPeriod3) {
            if (z - 3 <= MinHeightBeforeOscillation) {
               return false;
            }

            crystallizationHeight = z - 3;
         }

         if (crystallizationHeight > -1 && z > 2 * crystallizationHeight) {
            this.bHeight = z;
            break;
         }
      }

      int topLayerCount = 0;
      int secondLayerCount = 0;

      for (int x = 0; x < this.bWidth; x++) {
         for (int y = 0; y < this.bLength; y++) {
            topLayerCount += this.layers[0][x][y];
            secondLayerCount += this.layers[1][x][y];
         }
      }

      if (2 * topLayerCount >= 3 * secondLayerCount) {
         for (int x = 0; x < this.bWidth; x++) {
            for (int y = 0; y < this.bLength; y++) {
               if (this.layers[0][x][y] == 1 && this.layers[1][x][y] == 0) {
                  this.layers[0][x][y] = 0;
               }
            }
         }
      }

      int minX = minOrMax(BB[0], true);
      int maxX = minOrMax(BB[1], false);
      int minY = minOrMax(BB[2], true);
      int maxY = minOrMax(BB[3], false);
      this.bWidth = maxX - minX + 1;
      this.bLength = maxY - minY + 1;
      if (!this.shiftBuidlingJDown(15)) {
         return false;
      } else {
         boolean hitWater = false;
         if (this.caRule[0][2] != 1) {
            int[] heights = new int[]{
               findSurfaceJ(this.world, this.getI(this.bWidth - 1, 0), this.getK(this.bWidth - 1, 0), this.j0 + 10, false, 0),
               findSurfaceJ(this.world, this.getI(0, this.bLength - 1), this.getK(0, this.bLength - 1), this.j0 + 10, false, 0),
               findSurfaceJ(this.world, this.getI(this.bWidth - 1, this.bLength - 1), this.getK(this.bWidth - 1, this.bLength - 1), this.j0 + 10, false, 0),
               findSurfaceJ(this.world, this.getI(this.bWidth / 2, this.bLength / 2), this.getK(this.bWidth / 2, this.bLength / 2), this.j0 + 10, false, 0)
            };

            for (int height : heights) {
               hitWater |= height == -666;
            }
         }

         if (this.j0 + this.bHeight > 254) {
            this.j0 = 255 - this.bHeight - 1;
         }

         if (bury && !hitWater) {
            this.zGround = this.caRule[0][2] == 1
               ? Math.max(0, this.bHeight - this.bWidth / 3 - this.random.nextInt(this.bWidth))
               : this.random.nextInt(3 * this.bHeight / 4);
            if (this.j0 - this.zGround < 5) {
               this.zGround = this.j0 - 5;
            }

            this.j0 = this.j0 - this.zGround;
         }

         byte[][][] layers2 = new byte[this.bHeight][this.bWidth][this.bLength];
         this.fBB = new int[4][this.bHeight];

         for (int z = 0; z < this.bHeight; z++) {
            int lZ = this.bHeight - z - 1;

            for (int x = 0; x < this.bWidth; x++) {
               for (int yx = 0; yx < this.bLength; yx++) {
                  layers2[z][x][yx] = this.layers[lZ][x + minX][yx + minY];
               }
            }

            this.fBB[0][z] = BB[0][lZ] - minX + (BB[1][lZ] - BB[0][lZ]) / 4;
            this.fBB[1][z] = BB[1][lZ] - minX - (BB[1][lZ] - BB[0][lZ]) / 4;
            this.fBB[2][z] = BB[2][lZ] - minY + (BB[3][lZ] - BB[2][lZ]) / 4;
            this.fBB[3][z] = BB[3][lZ] - minY - (BB[3][lZ] - BB[2][lZ]) / 4;
         }

         this.layers = layers2;
         return true;
      }
   }

   public boolean queryCanBuild(int ybuffer, boolean nonLayoutFrameCheck) {
      int layoutCode = this.bWidth * this.bLength > 120 ? 4 : 5;
      if (this.wgt.isLayoutGenerator()) {
         if (!this.wgt.layoutIsClear(this.getIJKPt(0, 0, ybuffer), this.getIJKPt(this.bWidth - 1, 0, this.bLength - 1), layoutCode)) {
            return false;
         }

         this.wgt.setLayoutCode(this.getIJKPt(0, 0, ybuffer), this.getIJKPt(this.bWidth - 1, 0, this.bLength - 1), layoutCode);
      } else if (nonLayoutFrameCheck && this.isObstructedFrame(0, ybuffer)) {
         return false;
      }

      return true;
   }

   public boolean shiftBuidlingJDown(int maxShift) {
      int[] heights = new int[]{
         findSurfaceJ(this.world, this.getI(this.bWidth - 1, 0), this.getK(this.bWidth - 1, 0), this.j0 + 10, false, -1),
         findSurfaceJ(this.world, this.getI(0, this.bLength - 1), this.getK(0, this.bLength - 1), this.j0 + 10, false, -1),
         findSurfaceJ(this.world, this.getI(this.bWidth - 1, this.bLength - 1), this.getK(this.bWidth - 1, this.bLength - 1), this.j0 + 10, false, -1),
         findSurfaceJ(this.world, this.getI(this.bWidth / 2, this.bLength / 2), this.getK(this.bWidth / 2, this.bLength / 2), this.j0 + 10, false, -1)
      };
      int minHeight = minOrMax(heights, true);
      if (minOrMax(heights, false) - minHeight > maxShift) {
         return false;
      } else {
         this.j0 = minHeight;
         return true;
      }
   }

   private void makeFloorAt(int x, int z, int y, boolean[][] layout) {
      if (!layout[x][y]) {
         if (BlockProperties.get(this.getBlockIdLocal(x, z, y)).isArtificial && BlockProperties.get(this.getBlockIdLocal(x, z + 1, y)).isArtificial) {
            if (!BlockProperties.get(this.getBlockIdLocal(x, z + 2, y)).isArtificial) {
               this.setBlockLocal(x, z + 2, y, this.bRule);
            }
         } else {
            if (!BlockProperties.get(this.getBlockIdLocal(x, z - 1, y)).isArtificial) {
               BlockAndMeta idAndMeta = this.bRule.getNonAirBlock(this.world.rand);
               this.setBlockWithLightingLocal(x, z - 1, y, idAndMeta, true);
            }

            this.removeBlockWithLighting(x, z, y);
            this.removeBlockWithLighting(x, z + 1, y);
            layout[x][y] = true;
         }
      }
   }

   private void makeFloorCrossAt(int x, int z, int y, boolean[][] layout) {
      this.makeFloorAt(x, z, y, layout);
      if (x - 1 >= this.fBB[0][z]) {
         this.makeFloorAt(x - 1, z, y, layout);
      }

      if (x + 1 <= this.fBB[1][z]) {
         this.makeFloorAt(x + 1, z, y, layout);
      }

      if (y - 1 >= this.fBB[2][z]) {
         this.makeFloorAt(x, z, y - 1, layout);
      }

      if (y + 1 <= this.fBB[2][z]) {
         this.makeFloorAt(x, z, y + 1, layout);
      }
   }

   private String pickCAChestType(int z) {
      if (Math.abs(this.zGround - z) > this.random.nextInt(1 + z > this.zGround ? this.bHeight - this.zGround : this.zGround)
         && (z > this.zGround ? this.bHeight - this.zGround : this.zGround) > 20) {
         return this.random.nextBoolean() ? "MEDIUM" : "HARD";
      } else {
         return this.random.nextBoolean() ? "EASY" : "MEDIUM";
      }
   }

   private void populateFloor(int z, int floorBlocks) {
      TemplateRule spawnerSelection = null;
      int fWidth = this.fBB[1][z] - this.fBB[0][z];
      int fLength = this.fBB[3][z] - this.fBB[2][z];
      if (fWidth > 0 && fLength > 0) {
         if (this.random.nextInt(100) < 70) {
            for (int tries = 0; tries < 8; tries++) {
               int x = this.random.nextInt(fWidth) + this.fBB[0][z];
               int y = this.random.nextInt(fLength) + this.fBB[2][z];
               if (this.isFloor(x, z, y)) {
                  int[] pt = this.getIJKPt(x, z, y);
                  int lightVal = GenAccess.getLightFor(this.world, EnumSkyBlock.SKY, LegacyWorld.pos(pt[0], pt[1], pt[2]));
                  if (lightVal >= 5 || lightVal == 0 && this.j0 + z > 63) {
                     if (lightVal < 10) {
                        spawnerSelection = floorBlocks > 70 ? this.mediumLightWideSpawnerRule : this.mediumLightNarrowSpawnerRule;
                     }
                  } else {
                     spawnerSelection = this.lowLightSpawnerRule;
                  }

                  if (spawnerSelection != null) {
                     this.setBlockLocal(x, z, y, spawnerSelection);
                     break;
                  }
               }
            }
         }

         if (this.random.nextInt(100) < (spawnerSelection == null ? 20 : 70)) {
            for (int triesx = 0; triesx < 8; triesx++) {
               int x = this.random.nextInt(fWidth) + this.fBB[0][z];
               int y = this.random.nextInt(fLength) + this.fBB[2][z];
               if (this.isFloor(x, z, y)) {
                  this.setBlockLocal(x, z - 1, y, new BlockExtended(Blocks.CHEST, 0, this.pickCAChestType(z)));
                  this.setBlockLocal(x, z - 2, y, this.bRule);
                  if (this.random.nextBoolean()) {
                     break;
                  }
               }
            }
         }

         int s = this.random.nextInt(1 + this.random.nextInt(5)) - 2;

         for (int triesxx = 0; triesxx < s; triesxx++) {
            int x = this.random.nextInt(fWidth) + this.fBB[0][z];
            int y = this.random.nextInt(fLength) + this.fBB[2][z];
            if (this.isFloor(x, z, y)) {
               this.setBlockLocal(x, z, y, Blocks.STONE_PRESSURE_PLATE);
               this.setBlockLocal(x, z - 1, y, Blocks.TNT);
               this.setBlockLocal(x, z - 2, y, this.bRule);
            }
         }

         s = spawnerSelection == null ? this.random.nextInt(1 + this.random.nextInt(7)) - 2 : 0;

         for (int triesxxx = 0; triesxxx < s; triesxxx++) {
            int x = this.random.nextInt(fWidth) + this.fBB[0][z];
            int y = this.random.nextInt(fLength) + this.fBB[2][z];
            if (this.isFloor(x, z, y)) {
               int var26 = 1;
               if (this.isFloor(x, z, 1)) {
                  for (int x1 = x - 1; x1 <= x + 1; x1++) {
                     for (int y1 = var26 - 1; y1 <= var26 + 2; y1++) {
                        for (int z1 = z - 3; z1 <= z - 2; z1++) {
                           this.setBlockLocal(x1, z1, y1, this.bRule);
                        }
                     }
                  }

                  this.setBlockLocal(x, z, var26, Blocks.STONE_PRESSURE_PLATE);
                  this.setBlockLocal(x, z - 2, var26, Blocks.TNT);
                  this.setBlockLocal(x, z - 2, var26 + 1, Blocks.TNT);
               }
            }
         }

         if (spawnerSelection == null || this.random.nextBoolean()) {
            for (int triesxxxx = 0; triesxxxx < 10; triesxxxx++) {
               int x = this.random.nextInt(fWidth) + this.fBB[0][z];
               int y = this.random.nextInt(fLength) + this.fBB[2][z];
               BuildingDispenserTrap bdt = new BuildingDispenserTrap(this.wgt, this.bRule, this.random.nextInt(4), 1, this.getIJKPt(x, z, y));
               if (bdt.queryCanBuild(2)) {
                  bdt.build(this.random.nextBoolean() ? 0 : 1, true);
                  break;
               }
            }
         }
      }
   }

   private void populateLadderOrStairway(int z1, int z2, boolean buildStairs) {
      int fWidth = this.fBB[1][z2] - this.fBB[0][z2];
      int fLength = this.fBB[3][z2] - this.fBB[2][z2];
      if (fWidth > 0 && fLength > 0) {
         for (int tries = 0; tries < 8; tries++) {
            int x = this.random.nextInt(fWidth) + this.fBB[0][z2];
            int y = this.random.nextInt(fLength) + this.fBB[2][z2];
            if (this.isFloor(x, z2, y)) {
               int dir = this.random.nextInt(4);
               BuildingSpiralStaircase bss;
               if (buildStairs
                  && (bss = new BuildingSpiralStaircase(this.wgt, this.bRule, dir, 1, false, z1 - z2 + 1, this.getIJKPt(x, z2 - 1, y))).bottomIsFloor()) {
                  bss.build(0, 0);
               } else if (this.isFloor(x, z1, y)) {
                  for (int z = z1; z < z2; z++) {
                     this.setBlockLocal(x + DIR_TO_X[dir], z, y + DIR_TO_Y[dir], this.bRule);
                     this.setBlockLocal(x, z, y, Blocks.LADDER, LADDER_DIR_TO_META[flipDir(dir)]);
                  }
               }

               return;
            }
         }
      }
   }

   public static byte[][] makeCircularSeed(int maxWidth, Random random) {
      int diam = Math.min(random.nextInt(random.nextInt(random.nextInt(Math.max(1, maxWidth - 4)) + 1) + 1) + 4, 40);
      byte[][] seed = new byte[diam][diam];

      for (int x = 0; x < diam; x++) {
         for (int y = 0; y < diam; y++) {
            seed[x][y] = (byte)(CIRCLE_SHAPE[diam][x][y] == 1 ? 1 : 0);
         }
      }

      return seed;
   }

   public static byte[][] makeCruciformSeed(int maxWidth, Random random) {
      if (maxWidth <= 1) {
         return new byte[][]{{1}};
      } else {
         int width = 2 * (random.nextInt(random.nextInt(maxWidth / 2) + 1) + 1) + 1;
         int length = 2 * (random.nextInt(random.nextInt(maxWidth / 2) + 1) + 1) + 1;
         byte[][] seed = new byte[width][length];

         for (int x = 0; x < width; x++) {
            for (int y = 0; y < length; y++) {
               seed[x][y] = (byte)(x != width / 2 && y != length / 2 ? 0 : 1);
            }
         }

         return seed;
      }
   }

   public static byte[][] makeLinearSeed(int maxWidth, Random random) {
      if (maxWidth <= 1) {
         return new byte[][]{{1}};
      } else {
         int width = random.nextInt(random.nextInt(maxWidth - 1) + 1) + 2;
         byte[][] seed = new byte[width][1];

         for (int x = 0; x < width; x++) {
            seed[x][0] = 1;
         }

         return seed;
      }
   }

   public static byte[][] makeSymmetricSeed(int maxWidth, float seedDensity, Random random) {
      maxWidth = random.nextInt(random.nextInt(Math.max(1, maxWidth - 4)) + 1) + 1;
      int width = random.nextInt(random.nextInt(maxWidth) + 1) + 4;
      int length = random.nextInt(random.nextInt(maxWidth) + 1) + 4;
      byte[][] seed = new byte[width][length];
      int diam = Math.min(Math.max(width, length), 40);

      for (int x = 0; x < (width + 1) / 2; x++) {
         for (int y = 0; y < (length + 1) / 2; y++) {
            seed[x][y] = (byte)(Building.CIRCLE_SHAPE[diam][x][y] >= 0 && random.nextFloat() < seedDensity ? 1 : 0);
            seed[width - x - 1][y] = seed[x][y];
            seed[x][length - y - 1] = seed[x][y];
            seed[width - x - 1][length - y - 1] = seed[x][y];
         }
      }

      return seed;
   }

   public static byte[][] parseCARule(String str, PrintWriter lw) {
      try {
         byte[][] rule = new byte[][]{{0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}};
         String birthStr = str.split("/")[0].trim();
         String surviveStr = str.split("/")[1].trim();

         for (int n = 1; n < birthStr.length(); n++) {
            int digit = Integer.parseInt(birthStr.substring(n, n + 1));
            rule[0][digit] = 1;
         }

         for (int n = 1; n < surviveStr.length(); n++) {
            int digit = Integer.parseInt(surviveStr.substring(n, n + 1));
            rule[1][digit] = 1;
         }

         return rule;
      } catch (Exception var7) {
         if (lw != null) {
            lw.println("Error parsing automaton rule " + str + ": " + var7.getMessage());
         }

         return (byte[][])null;
      }
   }

   public static String ruleToString(byte[][] rule) {
      StringBuilder sb = new StringBuilder(30);
      sb.append("B");

      for (int n = 0; n < 9; n++) {
         if (rule[0][n] == 1) {
            sb.append(n);
         }
      }

      sb.append("S");

      for (int nx = 0; nx < 9; nx++) {
         if (rule[1][nx] == 1) {
            sb.append(nx);
         }
      }

      return sb.toString();
   }
}
