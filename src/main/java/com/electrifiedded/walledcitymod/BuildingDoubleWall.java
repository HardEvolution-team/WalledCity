package com.electrifiedded.walledcitymod;

public class BuildingDoubleWall extends Building {
   public BuildingWall wall1;
   public BuildingWall wall2;
   public final TemplateWall ws;

   public BuildingDoubleWall(int ID_, WorldGeneratorThread wgt_, TemplateWall ws_, int dir_, int axXHand_, int[] sourcePt) {
      super(ID_, wgt_, ws_.TowerRule, dir_, axXHand_, false, new int[]{ws_.WWidth, ws_.WHeight, 0}, sourcePt);
      this.ws = ws_;
   }

   public boolean plan() {
      this.wall1 = new BuildingWall(this.bID, this.wgt, this.ws, this.bDir, 1, this.ws.MaxL / 2, true, this.getIJKPt(0, 0, 0));
      this.wall2 = new BuildingWall(this.bID, this.wgt, this.ws, flipDir(this.bDir), -1, this.ws.MaxL / 2, true, this.getIJKPt(0, 0, -1)).setTowers(this.wall1);
      int a = this.wall1.plan(1, 0, this.ws.MergeWalls ? this.ws.WWidth : 5, !this.ws.MergeWalls) + 1;
      int b = this.wall2.plan(1, 0, this.ws.MergeWalls ? this.ws.WWidth : 5, !this.ws.MergeWalls) + 1;
      if (b + a - 1 < this.ws.MinL) {
         return false;
      } else {
         this.wall1.printWall();
         this.wall2.printWall();
         int[] tempx = new int[a + b];
         int[] tempz = new int[a + b];

         for (int m = 0; m < b; m++) {
            tempx[m] = this.wall2.xArray[b - m - 1];
            tempz[m] = this.wall2.zArray[b - m - 1];
         }

         for (int m = 0; m < a; m++) {
            tempx[m + b] = this.wall1.xArray[m];
            tempz[m + b] = this.wall1.zArray[m];
         }

         BuildingWall.smooth(tempx, 0, a + b - 1, this.ws.LateralSmoothingScale, this.ws.LateralSmoothingScale, true);
         BuildingWall.smooth(tempz, 0, a + b - 1, this.ws.ConcaveDownSmoothingScale, this.ws.ConcaveUpSmoothingScale, true);

         for (int m = 0; m < b; m++) {
            this.wall2.xArray[b - m - 1] = tempx[m];
            this.wall2.zArray[b - m - 1] = tempz[m];
         }

         for (int m = 0; m < a; m++) {
            this.wall1.xArray[m] = tempx[m + b];
            this.wall1.zArray[m] = tempz[m + b];
         }

         return true;
      }
   }

   public void build(int layoutCode) {
      this.ws.setFixedRules(this.world.rand);
      if (layoutCode != -1) {
         this.wall1.setLayoutCode(layoutCode);
         this.wall2.setLayoutCode(layoutCode);
      }

      this.wall1.buildFromTML();
      this.wall2.buildFromTML();
   }

   public void buildTowers(boolean lSideTowers, boolean rSideTowers, boolean gatehouseTowers, boolean overlapTowers, boolean isAvenue) {
      this.wall1.makeBuildings(lSideTowers, rSideTowers, gatehouseTowers, overlapTowers, isAvenue);
      this.wall2.makeBuildings(lSideTowers, rSideTowers, gatehouseTowers, overlapTowers, isAvenue);
   }
}
