package com.electrifiedded.walledcitymod;

import java.util.Random;
import net.minecraft.world.World;

public class WorldGenGreatWall extends WorldGeneratorThread {
   public WorldGenGreatWall(PopulatorGreatWall gw, World world, Random random, int chunkI, int chunkK, int triesPerChunk, double chunkTryProb) {
      super(gw, world, random, chunkI, chunkK, triesPerChunk, chunkTryProb);
   }

   @Override
   public boolean generate(int i0, int j0, int k0) {
      TemplateWall ws = TemplateWall.pickBiomeWeightedWallStyle(
         ((PopulatorGreatWall)this.master).wallStyles, this.world, i0, k0, this.world.rand, false
      );
      if (ws == null) {
         return false;
      } else {
         BuildingDoubleWall dw = new BuildingDoubleWall(10 * (this.random.nextInt(9000) + 1000), this, ws, this.random.nextInt(4), 1, new int[]{i0, j0, k0});
         if (!dw.plan()) {
            return false;
         } else {
            if (((PopulatorGreatWall)this.master).CurveBias > 0.01) {
               double curviness = 0.0;

               for (int m = 1; m < dw.wall1.bLength; m++) {
                  curviness += (dw.wall1.xArray[m] == dw.wall1.xArray[m - 1] ? 0 : 1) + (dw.wall1.zArray[m] == dw.wall1.zArray[m - 1] ? 0 : 1);
               }

               for (int m = 1; m < dw.wall2.bLength; m++) {
                  curviness += (dw.wall2.xArray[m] == dw.wall2.xArray[m - 1] ? 0 : 1) + (dw.wall2.zArray[m] == dw.wall2.zArray[m - 1] ? 0 : 1);
               }

               curviness /= 2 * (dw.wall1.bLength + dw.wall2.bLength - 1);
               double p = 1.0 / (1.0 + Math.exp(-30.0 * (curviness - ((PopulatorGreatWall)this.master).CurveBias / 5.0)));
               if (this.random.nextFloat() > p && curviness != 0.0) {
                  this.master
                     .logOrPrint("Rejected great wall, curviness=" + curviness + ", length=" + (dw.wall1.bLength + dw.wall1.bLength - 1) + ", P=" + p, "INFO");
                  return false;
               }
            }

            dw.build(-1);
            dw.buildTowers(true, true, ws.MakeGatehouseTowers, false, false);
            return true;
         }
      }
   }
}
