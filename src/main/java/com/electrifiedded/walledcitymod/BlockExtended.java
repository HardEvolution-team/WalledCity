package com.electrifiedded.walledcitymod;

import net.minecraft.block.Block;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class BlockExtended extends BlockAndMeta {
   public final String info;

   public BlockExtended(Block block, int meta, String extra) {
      super(block, meta);
      this.info = extra;
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
         return obj instanceof BlockExtended ? this.info.equals(((BlockExtended)obj).info) : false;
      }
   }

   @Override
   public int hashCode() {
      return new HashCodeBuilder().append(this.getMeta()).append(this.get()).append(this.info).toHashCode();
   }
}
