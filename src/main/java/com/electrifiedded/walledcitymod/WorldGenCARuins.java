package com.electrifiedded.walledcitymod;

import java.util.Random;
import net.minecraft.world.World;

public class WorldGenCARuins extends WorldGeneratorThread {
   private byte[][] caRule = (byte[][])null;
   private int[][] caRulesWeightsAndIndex;
   private final int MinHeight;
   private final int MaxHeight;
   private final float GlobalFrequency;
   private final float SymmetricSeedDensity;
   private final int ContainerWidth;
   private final int ContainerLength;
   private int[] seedTypeWeights;
   private static final int[] SEED_TYPE_CODES = new int[]{0, 1, 2, 3};
   private final int MinHeightBeforeOscillation;
   private final boolean SmoothWithStairs;
   private final boolean MakeFloors;
   private TemplateRule[] blockRules;

   public WorldGenCARuins(PopulatorCARuins ca, World world, Random random, int chunkI, int chunkK, int triesPerChunk, double chunkTryProb) {
      super(ca, world, random, chunkI, chunkK, triesPerChunk, chunkTryProb);
      this.caRulesWeightsAndIndex = ca.caRulesWeightsAndIndex;
      this.MinHeight = ca.MinHeight;
      this.MaxHeight = ca.MaxHeight;
      this.GlobalFrequency = ca.GlobalFrequency;
      this.SymmetricSeedDensity = ca.SymmetricSeedDensity;
      this.ContainerWidth = ca.ContainerWidth;
      this.ContainerLength = ca.ContainerLength;
      this.seedTypeWeights = ca.seedTypeWeights;
      this.MinHeightBeforeOscillation = ca.MinHeightBeforeOscillation;
      this.SmoothWithStairs = ca.SmoothWithStairs;
      this.MakeFloors = ca.MakeFloors;
      this.blockRules = ca.blockRules;
   }

   @Override
   public boolean generate(int i0, int j0, int k0) {
      int th = this.MinHeight + this.random.nextInt(this.MaxHeight - this.MinHeight + 1);
      if (this.caRule == null) {
         this.caRule = ((PopulatorCARuins)this.master)
            .caRules
            .get(Building.pickWeightedOption(this.world.rand, this.caRulesWeightsAndIndex[0], this.caRulesWeightsAndIndex[1]));
      }

      if (this.caRule == null) {
         return false;
      } else {
         int seedCode = Building.pickWeightedOption(this.world.rand, this.seedTypeWeights, SEED_TYPE_CODES);
         byte[][] seed = seedCode != 0 && (this.caRule[0][0] != 0 || this.caRule[0][1] != 0 || this.caRule[0][2] != 0 || this.caRule[0][3] != 0)
            ? (
               seedCode == 1
                  ? BuildingCellularAutomaton.makeLinearSeed(this.ContainerWidth, this.world.rand)
                  : (
                     seedCode == 2
                        ? BuildingCellularAutomaton.makeCircularSeed(Math.min(this.ContainerWidth, this.ContainerLength), this.world.rand)
                        : BuildingCellularAutomaton.makeCruciformSeed(Math.min(this.ContainerWidth, this.ContainerLength), this.world.rand)
                  )
            )
            : BuildingCellularAutomaton.makeSymmetricSeed(
               Math.min(this.ContainerWidth, this.ContainerLength), this.SymmetricSeedDensity, this.world.rand
            );
         TemplateRule blockRule = this.blockRules[net.minecraft.world.biome.Biome.getIdForBiome(Compat.biome(this.world, i0, k0)) + 1];
         BuildingCellularAutomaton bca = new BuildingCellularAutomaton(
            this, blockRule, this.random.nextInt(4), 1, false, this.ContainerWidth, th, this.ContainerLength, seed, this.caRule, null, new int[]{i0, j0, k0}
         );
         if (bca.plan(true, this.MinHeightBeforeOscillation) && bca.queryCanBuild(0, true)) {
            bca.build(this.SmoothWithStairs, this.MakeFloors);
            if (this.GlobalFrequency < 0.05 && this.random.nextInt(2) != 0) {
               for (int tries = 0; tries < 10; tries++) {
                  int[] pt = new int[]{
                     i0 + (2 * this.random.nextInt(2) - 1) * (this.ContainerWidth + this.random.nextInt(this.ContainerWidth)),
                     0,
                     k0 + (2 * this.random.nextInt(2) - 1) * (this.ContainerWidth + this.random.nextInt(this.ContainerWidth))
                  };
                  pt[1] = Building.findSurfaceJ(this.world, pt[0], pt[2], 255, true, 3) + 1;
                  if (this.generate(pt[0], pt[1], pt[2])) {
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
}
