package com.electrifiedded.walledcitymod;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

public class BuildingUndergroundEntranceway extends Building {
   private static final int PASSAGE_HEIGHT = 6;
   private static final int PASSAGE_WIDTH = 4;
   private static final int SUPPORT_INTERVAL = 6;
   private final TemplateWall ws;
   public BuildingWall street;
   private final Block stairsID;

   public BuildingUndergroundEntranceway(int ID_, WorldGeneratorThread wgt_, TemplateWall ws_, int dir_, int[] sourcePt) {
      super(ID_, wgt_, ws_.TowerRule, dir_, 1, false, new int[]{4, 6, 0}, sourcePt);
      this.ws = ws_;
      this.stairsID = this.ws.rules[this.ws.template[0][0][this.ws.WWidth / 2]].primaryBlock.toStair();
   }

   public boolean build() {
      while (this.bLength < 255 - this.j0) {
         if (BlockProperties.get(this.getBlockIdLocal(0, this.bLength + 6, this.bLength)).isWater) {
            return false;
         }

         if (this.j0 + this.bLength > 53 && this.isArtificialWallBlock(0, this.bLength + 6, this.bLength)) {
            return false;
         }

         if (this.j0 + this.bLength > 63
            && (
               BlockProperties.get(this.getBlockIdLocal(0, this.bLength, this.bLength)).isWallable
                  || BlockProperties.get(this.getBlockIdLocal(3, this.bLength, this.bLength)).isWallable
            )) {
            this.bLength--;
            break;
         }

         this.bLength++;
      }

      BuildingTower tower = new BuildingTower(0, this, true, 4, this.bDir, this.bHand, false, 10, 7, 10, this.getIJKPt(-3, this.bLength, this.bLength - 7));
      tower.build(0, 0, true);

      for (int z = 0; z < this.bLength; z++) {
         for (int x = -1; x <= 4; x++) {
            for (int z1 = 1; z1 <= 6; z1++) {
               if (x != -1 && x != 4 && z1 != 6) {
                  this.setBlockLocal(x, z + z1, z, Blocks.AIR);
               } else if (BlockProperties.get(this.getBlockIdLocal(x, z + z1, z)).isFlowing) {
                  this.setBlockLocal(x, z + z1, z, Blocks.STONE);
               }
            }
         }

         for (int x = 0; x < 4; x++) {
            this.setBlockLocal(x, z, z, this.random.nextInt(100) < this.bRule.chance ? this.stairsID : Blocks.AIR, STAIRS_DIR_TO_META[0]);
            this.buildDown(x, z - 1, z, TemplateRule.STONE_RULE, 20, 0, 3);
         }

         this.flushDelayed();
         if (z % 6 == 0 && z <= this.bLength - 6) {
            for (int z1x = -1; z1x < 6; z1x++) {
               this.setBlockLocal(0, z + z1x, z, this.bRule);
               this.setBlockLocal(3, z + z1x, z, this.bRule);
            }

            for (int x = 0; x < 4; x++) {
               this.setBlockLocal(x, z + 6 - 1, z, this.bRule);
            }
         }

         if (z % 6 == 3 && z <= this.bLength - 6) {
            if (this.random.nextInt(2) == 0) {
               this.setBlockLocal(0, z + 6 - 3, z, EAST_FACE_TORCH_BLOCK);
            }

            if (this.random.nextInt(2) == 0) {
               this.setBlockLocal(3, z + 6 - 3, z, WEST_FACE_TORCH_BLOCK);
            }
         }
      }

      for (int y = this.bLength; y >= this.bLength - 6 - 4; y--) {
         this.buildDown(-1, y >= this.bLength - 6 - 1 ? this.bLength - 1 : y + 6, y, this.bRule, 20, 7, 3);
         this.buildDown(4, y >= this.bLength - 6 - 1 ? this.bLength - 1 : y + 6, y, this.bRule, 20, 7, 3);
         if (y < this.bLength - 6) {
            for (int x = 0; x < 4; x++) {
               this.setBlockLocal(x, y + 6, y, this.bRule);
            }
         }
      }

      this.flushDelayed();
      this.street = new BuildingWall(
         this.bID, this.wgt, this.ws, flipDir(this.bDir), -this.bHand, this.ws.MaxL, true, this.getIJKPt((4 - this.ws.WWidth) / 2, 0, -1)
      );
      this.street.plan(1, 0, 5, true);
      return true;
   }

   @Override
   protected BlockAndMeta getDelayedStair(Block blc, int... block) {
      return new BlockAndMeta(blc, block[3]);
   }
}
