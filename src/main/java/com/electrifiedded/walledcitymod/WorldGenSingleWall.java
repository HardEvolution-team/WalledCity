package com.electrifiedded.walledcitymod;

import java.util.Random;
import net.minecraft.world.World;

public class WorldGenSingleWall extends WorldGeneratorThread {
   private int[] pt;

   public WorldGenSingleWall(PopulatorGreatWall gw, World world, Random random, int[] pt) {
      super(gw, world, random, pt[0] >> 4, pt[2] >> 4, 0, 0.0);
      this.pt = pt;
   }

   @Override
   public boolean generate(int i0, int j0, int k0) {
      TemplateWall ws = TemplateWall.pickBiomeWeightedWallStyle(
         ((PopulatorGreatWall)this.master).wallStyles, this.world, i0, k0, this.world.rand, false
      );
      BuildingWall wall = new BuildingWall(0, this, ws, 0, 1, ws.MaxL, true, i0, j0, k0);
      wall.setTarget(((PopulatorGreatWall)this.master).placedCoords);
      wall.plan(1, 0, ws.MergeWalls ? ws.WWidth : 5, false);
      if (wall.bLength >= wall.y_targ) {
         wall.smooth(ws.ConcaveDownSmoothingScale, ws.ConcaveUpSmoothingScale, true);
         wall.buildFromTML();
         wall.makeBuildings(true, true, true, false, false);
         ((PopulatorGreatWall)this.master).placedCoords = null;
      }

      return true;
   }
}
