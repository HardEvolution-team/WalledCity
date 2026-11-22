package com.electrifiedded.walledcitymod;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

public class BuildingSpiralStaircase extends Building {
   public BuildingSpiralStaircase(WorldGeneratorThread wgt_, TemplateRule bRule_, int bDir_, int axXHand_, boolean centerAligned_, int height, int[] sourcePt) {
      super(0, wgt_, bRule_, bDir_, axXHand_, centerAligned_, new int[]{3, height, 3}, sourcePt);
   }

   public boolean bottomIsFloor() {
      int x = calcBottomX(this.bHeight);
      int y = calcBottomY(this.bHeight);
      int btDir = rotDir(0, -this.bHeight - 2);
      return this.isFloor(x + DIR_TO_X[btDir], this.bHeight, y + DIR_TO_Y[btDir]);
   }

   public void build(int extraTopStairs, int yP) {
      Block stairsBlockId = this.bRule.primaryBlock.toStair();
      int sDir = 2;
      this.setBlockLocal(1, 0, 1, this.bRule);
      if (yP == 1 && yP == 2) {
         yP = 0;
      }

      int jB0 = this.getSurfaceIJKPt(0, yP, this.j0 + this.bHeight + 2, true, 0)[1] + 1;
      int jB2 = this.getSurfaceIJKPt(2, yP, this.j0 + this.bHeight + 2, true, 0)[1] + 1;
      int pYInc = Integer.signum(yP);

      for (int n = 0; n <= extraTopStairs; n++) {
         this.buildStairwaySegment(0, n, -n, 3, stairsBlockId, sDir);
      }

      int x = 0;
      int y = 1;
      this.setBlockLocal(x, 2, y, Blocks.AIR);

      for (int z = -1; z >= this.bHeight; z--) {
         this.buildStairwaySegment(x, z, y, 2, stairsBlockId, sDir);
         this.setBlockLocal(1, z, 1, this.bRule);
         x -= DIR_TO_X[sDir];
         y -= DIR_TO_Y[sDir];
         if (z == this.bHeight + 1) {
            this.buildStairwaySegment(x, --z, y, 3, stairsBlockId, sDir);
            this.setBlockLocal(1, z, 1, this.bRule);
            x -= DIR_TO_X[sDir];
            y -= DIR_TO_Y[sDir];
         }

         this.buildHallwaySegment(x, z, y, 3);
         if (yP != 0) {
            int zP = (x == 0 ? jB0 : jB2) - this.j0;
            if (y == pYInc + 1 && Math.abs(y - yP) > z - zP
               || y == pYInc + 1 && Math.abs(y - yP) >= z - zP && DIR_TO_Y[sDir] != 0
               || y != pYInc + 1 && Math.abs(y - yP) > z - zP && DIR_TO_Y[rotDir(sDir, 1)] != 0) {
               if (DIR_TO_Y[sDir] != 0) {
                  this.setBlockLocal(x, z - 1, y, stairsBlockId, STAIRS_DIR_TO_META[sDir]);
                  z--;
               }

               for (int y1 = y + pYInc; y1 != yP; y1 += pYInc) {
                  if (z - zP > 0) {
                     this.buildStairwaySegment(x, --z, y1, 3, stairsBlockId, rotDir(1, pYInc));
                  } else if (y1 == pYInc + 1 && !this.isWallBlock(x, z, y1 - pYInc)) {
                     this.buildHallwaySegment(x, z, y1, 2);
                  } else {
                     this.buildHallwaySegment(x, z, y1, 3);
                  }
               }
               break;
            }
         }

         sDir = rotDir(sDir, 1);
         x -= DIR_TO_X[sDir];
         y -= DIR_TO_Y[sDir];
      }

      this.flushDelayed();
   }

   private void buildHallwaySegment(int x, int z, int y, int height) {
      this.setBlockLocal(x, z - 1, y, this.bRule);

      for (int z1 = z; z1 < z + height; z1++) {
         this.setBlockLocal(x, z1, y, Blocks.AIR);
      }
   }

   private void buildStairwaySegment(int x, int z, int y, int height, Block stairsBlockId, int sDir) {
      this.setBlockLocal(x, z - 1, y, this.bRule);
      this.setBlockLocal(x, z, y, stairsBlockId, STAIRS_DIR_TO_META[sDir]);

      for (int z1 = z + 1; z1 <= z + height; z1++) {
         this.setBlockLocal(x, z1, y, Blocks.AIR);
      }
   }

   public static int calcBottomX(int height) {
      return height == 1 ? 0 : 2 * ((-height - 1) / 2 % 2);
   }

   public static int calcBottomY(int height) {
      return height == 1 ? 1 : 2 * (-height / 2 % 2);
   }

   @Override
   protected BlockAndMeta getDelayedStair(Block blc, int... block) {
      return new BlockAndMeta(blc, block[3]);
   }
}
