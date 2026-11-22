package com.electrifiedded.walledcitymod;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

public class BuildingTML extends Building {
   final TemplateTML tmlt;

   public BuildingTML(int ID_, WorldGeneratorThread wgt, int bDir_, int axXHand_, boolean centerAligned_, TemplateTML tmlt_, int[] sourcePt) {
      super(ID_, wgt, null, bDir_, axXHand_, centerAligned_, new int[]{tmlt_.width, tmlt_.height, tmlt_.length}, sourcePt);
      this.tmlt = tmlt_;
      this.j0 = this.j0 - this.tmlt.embed;
   }

   public void build() {
      this.tmlt.setFixedRules(this.world.rand);
      int[][] base = this.tmlt.namedLayers.get("base");

      for (int y = 0; y < this.bLength; y++) {
         for (int x = 0; x < this.bWidth; x++) {
            if (base != null) {
               this.buildDown(x, -1, y, this.tmlt.rules[base[y][x]], this.tmlt.leveling, 0, 0);
            } else {
               fillDown(this.getSurfaceIJKPt(x, y, this.j0 - 1, true, -1), this.j0 - 1, this.world);
            }
         }
      }

      for (int z = this.bHeight; z < this.tmlt.cutIn + this.tmlt.embed; z++) {
         for (int y = 0; y < this.bLength; y++) {
            for (int xx = 0; xx < this.bWidth; xx++) {
               this.setBlockLocal(xx, z, y, Blocks.AIR);
            }
         }
      }

      for (int z = 0; z < this.bHeight; z++) {
         for (int y = 0; y < this.bLength; y++) {
            for (int xx = 0; xx < this.bWidth; xx++) {
               this.setBlockLocal(xx, z, y, this.tmlt.rules[this.tmlt.template[z][y][xx]]);
            }
         }
      }

      this.flushDelayed();
   }

   public boolean queryCanBuild(int ybuffer) {
      if (this.j0 <= 0) {
         return false;
      } else {
         for (int y = 0; y < this.bLength; y++) {
            for (int x = 0; x < this.bWidth; x++) {
               if (this.j0 - this.getSurfaceIJKPt(x, y, this.j0 - 1, true, 3)[1] > this.tmlt.leveling + 1) {
                  return false;
               }
            }
         }

         if (this.tmlt.waterHeight != -666) {
            int waterCheckHeight = this.tmlt.waterHeight + this.tmlt.embed + 1;
            if (BlockProperties.get(this.getBlockIdLocal(0, waterCheckHeight, 0)).isWater
               || BlockProperties.get(this.getBlockIdLocal(0, waterCheckHeight, this.bLength - 1)).isWater
               || BlockProperties.get(this.getBlockIdLocal(this.bWidth - 1, waterCheckHeight, 0)).isWater
               || BlockProperties.get(this.getBlockIdLocal(this.bWidth - 1, waterCheckHeight, this.bLength - 1)).isWater) {
               return false;
            }
         }

         if (this.wgt.isLayoutGenerator()) {
            int layoutCode = this.tmlt.buildOverStreets ? 4 : 5;
            if (!this.wgt.layoutIsClear(this, this.tmlt.templateLayout, layoutCode)) {
               return false;
            }

            this.wgt.setLayoutCode(this, this.tmlt.templateLayout, 5);
         } else if (this.isObstructedFrame(0, ybuffer)) {
            return false;
         }

         return true;
      }
   }

   @Override
   protected BlockAndMeta getDelayedStair(Block blc, int... block) {
      return new BlockAndMeta(blc, block[3]);
   }
}
