package com.electrifiedded.walledcitymod;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.Tuple;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class BlockAndMeta extends Tuple {
   public BlockAndMeta(Block block, int meta) {
      super(block, meta);
   }

   public Block get() {
      return Block.class.cast(this.getFirst());
   }

   public int getMeta() {
      return Integer.class.cast(this.getSecond());
   }

   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      } else {
         return obj == this ? true : obj instanceof BlockAndMeta && this.getMeta() == ((BlockAndMeta)obj).getMeta() && this.get() == ((BlockAndMeta)obj).get();
      }
   }

   public int hashCode() {
      return new HashCodeBuilder().append(this.getMeta()).append(this.get()).toHashCode();
   }

   public Block toStair() {
      if (this.get() == Blocks.COBBLESTONE || this.get() == Blocks.MOSSY_COBBLESTONE) {
         return Blocks.STONE_STAIRS;
      } else if (this.get() == Blocks.NETHER_BRICK) {
         return Blocks.NETHER_BRICK_STAIRS;
      } else if (this.get() == Blocks.STONEBRICK || this.get() == Blocks.STONE) {
         return Blocks.STONE_BRICK_STAIRS;
      } else if (this.get() == Blocks.BRICK_BLOCK) {
         return Blocks.BRICK_STAIRS;
      } else if (this.get() == Blocks.SANDSTONE) {
         return Blocks.SANDSTONE_STAIRS;
      } else if (this.get() == Blocks.QUARTZ_BLOCK) {
         return Blocks.QUARTZ_STAIRS;
      } else {
         if (this.get() == Blocks.PLANKS) {
            int tempdata = this.getMeta();
            switch (tempdata) {
               case 0:
                  return Blocks.OAK_STAIRS;
               case 1:
                  return Blocks.SPRUCE_STAIRS;
               case 2:
                  return Blocks.BIRCH_STAIRS;
               case 3:
                  return Blocks.JUNGLE_STAIRS;
               case 4:
                  return Blocks.ACACIA_STAIRS;
               case 5:
                  return Blocks.DARK_OAK_STAIRS;
            }
         }

         return this.get();
      }
   }

   public BlockAndMeta toStep() {
      if (!BlockProperties.get(this.get()).isArtificial) {
         return this;
      } else if (this.get() == Blocks.SANDSTONE) {
         return new BlockAndMeta(Blocks.STONE_SLAB, 1);
      } else if (this.get() == Blocks.PLANKS) {
         return new BlockAndMeta(Blocks.STONE_SLAB, 2);
      } else if (this.get() == Blocks.COBBLESTONE) {
         return new BlockAndMeta(Blocks.STONE_SLAB, 3);
      } else if (this.get() == Blocks.BRICK_BLOCK) {
         return new BlockAndMeta(Blocks.STONE_SLAB, 4);
      } else if (this.get() == Blocks.STONEBRICK) {
         return new BlockAndMeta(Blocks.STONE_SLAB, 5);
      } else if (this.get() == Blocks.NETHER_BRICK) {
         return new BlockAndMeta(Blocks.STONE_SLAB, 6);
      } else if (this.get() == Blocks.QUARTZ_BLOCK) {
         return new BlockAndMeta(Blocks.STONE_SLAB, 7);
      } else if (this.get() == Blocks.STONE_SLAB || this.get() == Blocks.DOUBLE_STONE_SLAB) {
         return new BlockAndMeta(Blocks.STONE_SLAB, this.getMeta());
      } else {
         return this.get() != Blocks.DOUBLE_WOODEN_SLAB && this.get() != Blocks.WOODEN_SLAB
            ? new BlockAndMeta(this.get(), 0)
            : new BlockAndMeta(Blocks.WOODEN_SLAB, this.getMeta());
      }
   }

   public BlockAndMeta stairToSolid() {
      Block block = this.get();
      int meta = 0;
      if (block == Blocks.STONE_STAIRS) {
         block = Blocks.COBBLESTONE;
      } else if (block == Blocks.OAK_STAIRS) {
         block = Blocks.PLANKS;
      } else if (block == Blocks.SPRUCE_STAIRS) {
         block = Blocks.PLANKS;
         meta = 1;
      } else if (block == Blocks.BIRCH_STAIRS) {
         block = Blocks.PLANKS;
         meta = 2;
      } else if (block == Blocks.JUNGLE_STAIRS) {
         block = Blocks.PLANKS;
         meta = 3;
      } else if (block == Blocks.ACACIA_STAIRS) {
         block = Blocks.PLANKS;
         meta = 4;
      } else if (block == Blocks.DARK_OAK_STAIRS) {
         block = Blocks.PLANKS;
         meta = 5;
      } else if (block == Blocks.BRICK_STAIRS) {
         block = Blocks.BRICK_BLOCK;
      } else if (block == Blocks.STONE_BRICK_STAIRS) {
         block = Blocks.STONEBRICK;
      } else if (block == Blocks.NETHER_BRICK_STAIRS) {
         block = Blocks.NETHER_BRICK;
      } else if (block == Blocks.SANDSTONE_STAIRS) {
         block = Blocks.SANDSTONE;
      } else if (block == Blocks.QUARTZ_STAIRS) {
         block = Blocks.QUARTZ_BLOCK;
      }

      return new BlockAndMeta(block, meta);
   }
}
