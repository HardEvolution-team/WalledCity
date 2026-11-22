package com.electrifiedded.walledcitymod;

import com.electrifiedded.archaiclib.legacy.world.LegacyWorld;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityDispenser;

public class BuildingDispenserTrap extends Building {
   public static final int ARROW_MISSILE = 0;
   public static final int DAMAGE_POTION_MISSILE = 1;
   private static BlockAndMeta[] CODE_TO_BLOCK = new BlockAndMeta[]{
      PRESERVE_BLOCK,
      null,
      new BlockAndMeta(Blocks.AIR, 0),
      new BlockAndMeta(Blocks.REDSTONE_WIRE, 0),
      new BlockAndMeta(Blocks.REDSTONE_TORCH, BUTTON_DIR_TO_META[0]),
      new BlockAndMeta(Blocks.UNLIT_REDSTONE_TORCH, BUTTON_DIR_TO_META[2]),
      new BlockAndMeta(Blocks.REDSTONE_TORCH, 5)
   };
   private static int[][][] MECHANISM = new int[][][]{
      {{0, 0, 0}, {0, 0, 0}, {0, 1, 0}, {0, 0, 0}},
      {{0, 0, 0}, {1, 1, 1}, {1, 4, 1}, {1, 1, 1}},
      {{0, 0, 0}, {1, 1, 1}, {1, 1, 1}, {1, 5, 1}},
      {{0, 1, 0}, {1, 1, 1}, {1, 3, 1}, {1, 1, 1}},
      {{0, 1, 0}, {1, 6, 1}, {1, 0, 1}, {1, 2, 1}},
      {{0, 1, 0}, {1, 1, 1}, {1, 1, 1}, {1, 1, 1}}
   };

   public BuildingDispenserTrap(WorldGeneratorThread wgt_, TemplateRule bRule_, int bDir_, int plateSeparation, int[] sourcePt) {
      super(0, wgt_, bRule_, bDir_, 1, true, new int[]{3, 6, plateSeparation}, sourcePt);
   }

   public void build(int missileType, boolean multipleTriggers) {
      if (this.bLength < 0) {
         this.bLength = 0;
      }

      for (int x = 0; x < MECHANISM[0][0].length; x++) {
         for (int y = 0; y < MECHANISM[0].length; y++) {
            for (int z = 0; z < MECHANISM.length; z++) {
               if (MECHANISM[z][3 - y][x] == 1) {
                  this.setBlockLocal(x, z - 3, y + this.bLength, this.bRule);
               } else {
                  this.setBlockLocal(x, z - 3, y + this.bLength, CODE_TO_BLOCK[MECHANISM[z][3 - y][x]]);
               }
            }
         }
      }

      for (int y = 0; y < this.bLength; y++) {
         this.setBlockLocal(1, -3, y, this.bRule);
         this.setBlockLocal(1, -2, y, Blocks.REDSTONE_WIRE);
         this.setBlockLocal(0, -2, y, this.bRule);
         this.setBlockLocal(2, -2, y, this.bRule);
         this.setBlockLocal(1, -1, y, this.bRule);
         this.setBlockLocal(1, 0, y, multipleTriggers && this.random.nextBoolean() ? Blocks.STONE_PRESSURE_PLATE : Blocks.AIR);
         this.setBlockLocal(1, 1, y, Blocks.AIR);
      }

      this.setBlockLocal(1, 0, 0, Blocks.STONE_PRESSURE_PLATE);
      this.flushDelayed();
      ItemStack itemstack = missileType == 0
         ? new ItemStack(Items.ARROW, 30 + this.random.nextInt(10), 0)
         : Compat.stack(Items.POTIONITEM, 30 + this.random.nextInt(10), 16396);
      this.setItemDispenser(1, 1, this.bLength + 1, 2, itemstack);
   }

   public boolean queryCanBuild(int minLength) {
      if (!this.isFloor(1, 0, 0)) {
         return false;
      } else {
         for (int y = 1; y < 9; y++) {
            if (!this.isFloor(1, 0, y)) {
               if (!this.isWallBlock(1, -1, y)) {
                  return false;
               }

               if (y < minLength) {
                  return false;
               }

               if (!this.isWallable(1, 1, y)) {
                  this.bLength = y;
                  return true;
               }

               return false;
            }
         }

         return false;
      }
   }

   private void setItemDispenser(int x, int z, int y, int metaDir, ItemStack itemstack) {
      int[] pt = this.getIJKPt(x, z, y);
      GenAccess.setBlock(this.world, pt[0], pt[1], pt[2], Blocks.DISPENSER, 0, 2);
      GenAccess.setBlockMetadataWithNotify(this.world, pt[0], pt[1], pt[2], LADDER_DIR_TO_META[this.orientDirToBDir(metaDir)], 3);

      try {
         TileEntityDispenser tileentitychest = (TileEntityDispenser)GenAccess.getTileEntity(this.world, pt[0], pt[1], pt[2]);
         if (itemstack != null && tileentitychest != null) {
            tileentitychest.setInventorySlotContents(this.random.nextInt(tileentitychest.getSizeInventory()), itemstack);
         }
      } catch (Exception var8) {
         System.err.println("Error filling dispenser: " + var8.toString());
         var8.printStackTrace();
      }
   }
}
