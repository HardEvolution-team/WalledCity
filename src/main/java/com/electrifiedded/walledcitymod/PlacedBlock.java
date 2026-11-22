package com.electrifiedded.walledcitymod;

import net.minecraft.block.Block;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class PlacedBlock extends BlockAndMeta {
   public final int x;
   public final int y;
   public final int z;

   public PlacedBlock(Block block, int[] data) {
      super(block, data[3]);
      this.x = data[0];
      this.y = data[1];
      this.z = data[2];
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      } else if (obj == this) {
         return true;
      } else if (!super.equals(obj)) {
         return false;
      } else {
         return !(obj instanceof PlacedBlock) ? false : this.x == ((PlacedBlock)obj).x && this.y == ((PlacedBlock)obj).y && this.z == ((PlacedBlock)obj).z;
      }
   }

   @Override
   public int hashCode() {
      return new HashCodeBuilder().append(this.getMeta()).append(this.get()).append(this.x).append(this.y).append(this.z).toHashCode();
   }
}
