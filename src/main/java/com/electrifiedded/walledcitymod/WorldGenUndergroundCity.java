package com.electrifiedded.walledcitymod;

import com.electrifiedded.archaiclib.legacy.world.LegacyWorld;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class WorldGenUndergroundCity extends WorldGeneratorThread {
   private static final float P_CHILDREN = 0.8F;
   private static final int MAX_CHILDREN = 3;
   public static final int MIN_DIAM = 11;
   public static final int MAX_DIAM = 30;
   private static final int DIAM_INCREMENT = 3;
   private static final int Z_SHIFT = 12;
   private static final float HORIZ_SHIFT_SIGMA = 0.2F;
   private static final float THETA_SHIFT_SIGMA = 5.0F;
   private List<int[]> hollows = new ArrayList<>();
   private List<BuildingDoubleWall> streets = new ArrayList<>();
   private double cavernMass = 0.0;
   private double cavernMass_i = 0.0;
   private double cavernMass_k = 0.0;
   TemplateWall pws;

   public WorldGenUndergroundCity(PopulatorWalledCity wc, World world, Random random, int chunkI, int chunkK, int triesPerChunk, double chunkTryProb) {
      super(wc, world, random, chunkI, chunkK, triesPerChunk, chunkTryProb);
   }

   @Override
   public boolean generate(int i0, int j0, int k0) {
      this.pws = TemplateWall.pickBiomeWeightedWallStyle(
         ((PopulatorWalledCity)this.master).undergroundCityStyles, this.world, i0, k0, this.world.rand, true
      );
      if (this.pws == null) {
         return false;
      } else if (!((PopulatorWalledCity)this.master).cityIsSeparated(this.world, i0, k0, 1)) {
         return false;
      } else {
         this.hollow(i0, j0, k0, 30);
         if (this.hollows.size() == 0) {
            return false;
         } else {
            ((PopulatorWalledCity)this.master).cityLocations.get(this.world).add(new int[]{i0, k0, 1});
            ((PopulatorWalledCity)this.master).saveCityLocations(this.world);
            this.master
               .logOrPrint(
                  "\n***** Building " + this.pws.name + " city with " + this.hollows.size() + " hollows at (" + i0 + "," + j0 + "," + k0 + "). ******\n",
                  "FINER"
               );
            List<BuildingUndergroundEntranceway> entranceways = this.buildEntranceways();
            this.fillHollows();

            for (BuildingUndergroundEntranceway entranceway : entranceways) {
               if (entranceway.street.bLength > 1) {
                  entranceway.street.buildFromTML();
                  entranceway.street.makeBuildings(true, true, false, false, false);
               }
            }

            for (BuildingDoubleWall street : this.streets) {
               street.buildTowers(true, true, false, this.pws.StreetDensity > 10, false);
            }

            return true;
         }
      }
   }

   private List<BuildingUndergroundEntranceway> buildEntranceways() {
      if (!this.pws.MakeUndergroundEntranceways) {
         return new ArrayList<>();
      } else {
         int[] center = new int[]{(int)(this.cavernMass_i / this.cavernMass), 256, (int)(this.cavernMass_k / this.cavernMass)};
         int[] pole = new int[]{center[0] + 100, center[1], center[2]};
         List<BuildingUndergroundEntranceway> entranceways = new ArrayList<>();

         for (int attempts = 0; attempts < Math.min(20, this.hollows.size()); attempts++) {
            int[] hollow = this.getFarthestHollowFromPt(pole);
            int diam = Building.SPHERE_SHAPE[hollow[3]][hollow[3] / 3];
            int axDir = Math.abs(center[0] - hollow[0]) > Math.abs(center[2] - hollow[2]) ? (hollow[0] > center[0] ? 2 : 0) : (hollow[2] > center[2] ? 3 : 1);
            int[] pt = new int[]{
               hollow[0] + (Math.abs(axDir) == 1 ? hollow[3] / 2 : (axDir == 2 ? (hollow[3] + diam) / 2 : (hollow[3] - diam) / 2 + 1)),
               hollow[1] - hollow[3] / 3,
               hollow[2] + (Math.abs(axDir) == 2 ? hollow[3] / 2 : (axDir == 3 ? (hollow[3] + diam) / 2 : (hollow[3] - diam) / 2 + 1))
            };
            boolean separated = true;

            for (BuildingUndergroundEntranceway entranceway : entranceways) {
               if (Building.distance(entranceway.getIJKPt(0, 0, 0), pt) < 400) {
                  separated = false;
               }
            }

            BuildingUndergroundEntranceway entrancewayx = new BuildingUndergroundEntranceway(attempts, this, this.pws, axDir, pt);
            if (separated && entrancewayx.build()) {
               entranceways.add(entrancewayx);
               ((PopulatorWalledCity)this.master)
                  .chatBuildingCity("Built an underground entranceway at (" + hollow[0] + "," + hollow[1] + "," + hollow[2] + ").", null);
            }

            pole[0] = center[0] + (center[2] - hollow[2]) / 2;
            pole[2] = center[2] + (hollow[0] - center[0]) / 2;
            if (entranceways.size() >= 4) {
               break;
            }
         }

         return entranceways;
      }
   }

   private void fillHollows() {
      int successes = 0;

      for (int tries = 0; tries < this.pws.StreetDensity * 4; tries++) {
         int[] hollow = this.hollows.get(this.random.nextInt(this.hollows.size()));
         int[] pt = new int[]{this.random.nextInt(hollow[3]), 0, this.random.nextInt(hollow[3])};
         if (Building.CIRCLE_SHAPE[hollow[3]][pt[0]][pt[2]] == 0) {
            pt[0] += hollow[0];
            pt[2] += hollow[2];
            pt[1] = Building.findSurfaceJ(this.world, pt[0], pt[2], hollow[1] - (hollow[3] + 1) / 2, false, -1) + 1;
            TemplateWall sws = TemplateWall.pickBiomeWeightedWallStyle(this.pws.streets, this.world, pt[0], pt[2], this.world.rand, true);
            sws.MergeWalls = true;
            BuildingDoubleWall street = new BuildingDoubleWall(tries, this, sws, this.random.nextInt(4), 1, pt);
            if (street.plan()) {
               street.build(-1);
               this.streets.add(street);
               successes++;
            }

            if (successes > Math.min(this.hollows.size() * this.pws.StreetDensity, 4 * this.pws.StreetDensity)) {
               break;
            }
         }
      }
   }

   private int[] getFarthestHollowFromPt(int[] pt) {
      int[] farthestHollow = null;
      int maxDist = -1;

      for (int[] h : this.hollows) {
         int dist = Building.distance(pt, h);
         if (dist > maxDist) {
            maxDist = dist;
            farthestHollow = h;
         }
      }

      return farthestHollow;
   }

   private boolean hollow(int i, int j, int k, int diam) {
      if (diam < 11) {
         return false;
      } else if (j - diam / 2 >= 10 && j + diam / 2 <= Building.findSurfaceJ(this.world, i + diam / 2, k + diam / 2, 255, false, -1) - 3) {
         this.hollows.add(new int[]{i, j, k, diam, 0});
         if (diam == 30) {
            ((PopulatorWalledCity)this.master).chatBuildingCity("** Building underground city... **", null);
         }

         for (int z1 = 0; z1 < (diam + 1) / 2; z1++) {
            int top_diam = Building.SPHERE_SHAPE[diam][z1];
            int offset = (diam - top_diam) / 2;

            for (int y1 = 0; y1 < top_diam; y1++) {
               for (int x1 = 0; x1 < top_diam; x1++) {
                  if (Building.CIRCLE_SHAPE[top_diam][x1][y1] >= 0) {
                     Building.setBlockAndMetaNoLighting(this.world, i + offset + x1, j + z1, k + offset + y1, Blocks.AIR, 0);
                  }
               }
            }

            for (int y1 = 0; y1 < top_diam; y1++) {
               for (int x1x = 0; x1x < top_diam; x1x++) {
                  if (Building.CIRCLE_SHAPE[top_diam][x1x][y1] >= 0) {
                     for (int z2 = z1 + 1; z2 <= z1 + 3; z2++) {
                        if (BlockProperties.get(GenAccess.getBlock(this.world, i + offset + x1x, j + z2, k + offset + y1)).isFlowing) {
                           GenAccess.setBlock(this.world, i + offset + x1x, j + z2, k + offset + y1, Blocks.STONE, 0, 2);
                        }
                     }
                  }
               }
            }

            int bottom_diam = Building.SPHERE_SHAPE[diam][2 * z1 / 3];
            offset = (diam - bottom_diam) / 2;
            if (z1 > 0) {
               for (int y1 = 0; y1 < bottom_diam; y1++) {
                  for (int x1xx = 0; x1xx < bottom_diam; x1xx++) {
                     if (Building.CIRCLE_SHAPE[bottom_diam][x1xx][y1] >= 0) {
                        Building.setBlockAndMetaNoLighting(this.world, i + offset + x1xx, j - z1, k + offset + y1, Blocks.AIR, 0);
                     }
                  }
               }

               for (int y1 = 0; y1 < bottom_diam; y1++) {
                  for (int x1xxx = 0; x1xxx < bottom_diam; x1xxx++) {
                     if (Building.CIRCLE_SHAPE[bottom_diam][x1xxx][y1] >= 0) {
                        Block blockId = GenAccess.getBlock(this.world, i + offset + x1xxx, j - z1 - 1, k + offset + y1);
                        if (BlockProperties.get(blockId).isOre && blockId != Blocks.COAL_ORE) {
                           GenAccess.setBlock(this.world, i + offset + x1xxx, j - z1 - 1, k + offset + y1, Blocks.STONE, 0, 2);
                        }
                     }
                  }
               }
            }
         }

         int hollowMass = diam * diam * diam;
         this.cavernMass += hollowMass;
         this.cavernMass_i += hollowMass * i;
         this.cavernMass_k += hollowMass * k;
         int successes = 0;

         for (int tries = 0; tries < (diam >= 24 ? 10 : 3); tries++) {
            if (this.random.nextFloat() < 0.8F) {
               float theta;
               if (diam >= 24) {
                  theta = this.random.nextFloat() * 6.283185F;
               } else {
                  theta = (float)Math.atan((this.cavernMass * i - this.cavernMass_i) / (this.cavernMass * k - this.cavernMass_k))
                     + 5.0F * this.random.nextFloat() * (this.random.nextFloat() - 0.5F);
               }

               float rshift = Building.SPHERE_SHAPE[diam][diam / 3] + diam * (0.1F - 0.2F * this.random.nextFloat());
               if (this.hollow(
                  i + (int)(MathHelper.sin(theta) * rshift),
                  j + this.random.nextInt(this.random.nextInt(12) + 1) - 3,
                  k + (int)(MathHelper.cos(theta) * rshift),
                  diam - 3
               )) {
                  successes++;
               }

               if (successes >= 3) {
                  break;
               }
            }
         }

         return true;
      } else {
         return false;
      }
   }
}
