package com.electrifiedded.walledcitymod;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockMobSpawner;
import net.minecraft.init.Blocks;

public class TemplateRule {
   public static final int FIXED_FOR_BUILDING = 5;
   public static final TemplateRule RULE_NOT_PROVIDED = null;
   public static final String BLOCK_NOT_REGISTERED_ERROR_PREFIX = "Error reading rule: BlockID ";
   public static final String SPECIAL_AIR = "PRESERVE";
   public static final String SPECIAL_STAIR = "WALL_STAIR";
   public static final String SPECIAL_PAINT = "PAINTING";
   public static final TemplateRule AIR_RULE = new TemplateRule(Blocks.AIR, 0, null);
   public static final TemplateRule STONE_RULE = new TemplateRule(Blocks.STONE, 0, null);
   private Block[] blockIDs;
   private int[] blockMDs;
   private String[] extraData;
   public int chance = 100;
   public int condition = 0;
   public BlockAndMeta primaryBlock = null;
   private BlockAndMeta fixedRuleChosen = null;

   public TemplateRule(String rule, boolean checkMetaValue) throws Exception {
      String[] items = rule.split(",");
      int numblocks = items.length - 2;
      if (numblocks < 1) {
         throw new Exception("Error reading rule: No blockIDs specified for rule!");
      } else {
         this.condition = Integer.parseInt(items[0].trim());
         this.chance = Integer.parseInt(items[1].trim());
         this.blockIDs = new Block[numblocks];
         this.blockMDs = new int[numblocks];
         this.extraData = new String[numblocks];

         for (int i = 0; i < numblocks; i++) {
            String[] data = items[i + 2].trim().split("-", 2);
            if (data[0].equals("PRESERVE")) {
               this.blockIDs[i] = Building.PRESERVE_BLOCK.get();
               this.blockMDs[i] = Building.PRESERVE_BLOCK.getMeta();
               this.extraData[i] = data[0];
            } else if (!data[0].equals("WALL_STAIR") && !data[0].equals("PAINTING")) {
               Block temp;
               try {
                  temp = Compat.block(Integer.parseInt(data[0]));
               } catch (Exception var11) {
                  temp = Compat.block(data[0]);
               }

               if (temp == null) {
                  throw new Exception("Error reading rule: BlockID " + data[0] + " unknown!");
               }

               this.blockIDs[i] = temp;
               if (data.length > 1) {
                  String[] txt = data[1].split("-", 2);

                  try {
                     this.blockMDs[i] = Integer.parseInt(txt[0]);
                  } catch (Exception var10) {
                     this.blockMDs[i] = 0;
                  }

                  if (txt.length > 1 && this.isSpecial(temp)) {
                     this.extraData[i] = txt[1];
                  }
               } else {
                  this.blockMDs[i] = 0;
               }
            } else {
               this.blockIDs[i] = Blocks.AIR;
               int x = 0;
               if (data.length > 1) {
                  try {
                     x = Integer.parseInt(data[1]);
                  } catch (Exception var12) {
                  }
               }

               if (data[0].startsWith("WALL_STAIR".substring(0, 1))) {
                  this.blockMDs[i] = -x;
               } else {
                  this.blockMDs[i] = 3 + x;
               }

               this.extraData[i] = data[0];
            }

            if (checkMetaValue && !(this.blockIDs[i] instanceof BlockAir)) {
               String checkStr = Building.metaValueCheck(this.blockIDs[i], this.blockMDs[i]);
               if (checkStr != null) {
                  throw new Exception("Error reading rule: " + rule + "\nBad meta value " + this.blockMDs[i] + ". " + checkStr);
               }
            }
         }

         this.primaryBlock = this.getPrimaryBlock();
      }
   }

   public TemplateRule(Block block, int meta, String extra) {
      this.blockIDs = new Block[]{block};
      this.blockMDs = new int[]{meta};
      if (extra != null) {
         this.extraData = new String[]{extra};
      }

      this.primaryBlock = this.getPrimaryBlock();
   }

   public TemplateRule(Block block, int meta, int chance_) {
      this(block, meta, null, chance_);
   }

   public TemplateRule(Block block, int meta, String extra, int chance_) {
      this(block, meta, extra);
      this.chance = chance_;
   }

   public TemplateRule(Block[] blockIDs_, int[] blockMDs_, int chance_) {
      this.blockIDs = blockIDs_;
      this.blockMDs = blockMDs_;
      this.chance = chance_;
      this.primaryBlock = this.getPrimaryBlock();
   }

   public TemplateRule(Block[] blockIDs_, int[] blockMDs_, String[] extra, int chance_) {
      this.blockIDs = blockIDs_;
      this.blockMDs = blockMDs_;
      this.extraData = extra;
      this.chance = chance_;
      this.primaryBlock = this.getPrimaryBlock();
   }

   public void setFixedRule(Random random) {
      if (this.condition == 5) {
         int m = random.nextInt(this.blockIDs.length);
         if (this.extraData != null && this.extraData[m] != null && !this.extraData[m].equals("")) {
            this.fixedRuleChosen = new BlockExtended(this.blockIDs[m], this.blockMDs[m], this.extraData[m]);
         } else {
            this.fixedRuleChosen = new BlockAndMeta(this.blockIDs[m], this.blockMDs[m]);
         }
      } else {
         this.fixedRuleChosen = null;
      }
   }

   public TemplateRule getFixedRule(Random random) {
      if (this.condition != 5) {
         return this;
      } else {
         int m = random.nextInt(this.blockIDs.length);
         return this.extraData != null && this.extraData[m] != null && !this.extraData[m].equals("")
            ? new TemplateRule(this.blockIDs[m], this.blockMDs[m], this.extraData[m], this.chance)
            : new TemplateRule(this.blockIDs[m], this.blockMDs[m], this.chance);
      }
   }

   public BlockAndMeta getBlockOrHole(Random random) {
      if (this.chance < 100 && random.nextInt(100) >= this.chance) {
         return Building.HOLE_BLOCK_LIGHTING;
      } else if (this.fixedRuleChosen != null) {
         return this.fixedRuleChosen;
      } else {
         int m = random.nextInt(this.blockIDs.length);
         return (BlockAndMeta)(this.extraData != null && this.extraData[m] != null && !this.extraData[m].equals("")
            ? new BlockExtended(this.blockIDs[m], this.blockMDs[m], this.extraData[m])
            : new BlockAndMeta(this.blockIDs[m], this.blockMDs[m]));
      }
   }

   public boolean isPreserveRule() {
      for (int i = 0; i < this.blockIDs.length; i++) {
         if (this.blockIDs[i] != Building.PRESERVE_BLOCK.get()) {
            return false;
         }

         if (this.blockMDs[i] != Building.PRESERVE_BLOCK.getMeta()) {
            return false;
         }

         if (this.extraData == null || this.extraData[i] == null || !this.extraData[i].equals("PRESERVE")) {
            return false;
         }
      }

      return true;
   }

   public boolean hasUndeadSpawner() {
      if (this.extraData != null) {
         for (int i = 0; i < this.blockIDs.length; i++) {
            if (this.blockIDs[i] instanceof BlockMobSpawner) {
               String txt = this.extraData[i];
               if (txt != null && (txt.equals("Zombie") || txt.equals("Skeleton") || txt.equals("Creeper") || txt.equals("EASY") || txt.equals("UPRIGHT"))) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   public BlockAndMeta getNonAirBlock(Random random) {
      int m = random.nextInt(this.blockIDs.length);
      return (BlockAndMeta)(this.extraData != null && this.extraData[m] != null && !this.extraData[m].equals("")
         ? new BlockExtended(this.blockIDs[m], this.blockMDs[m], this.extraData[m])
         : new BlockAndMeta(this.blockIDs[m], this.blockMDs[m]));
   }

   @Override
   public String toString() {
      String str = this.condition + "," + this.chance;

      for (int m = 0; m < this.blockIDs.length; m++) {
         str = str + "," + Compat.name(this.blockIDs[m]) + "-" + this.blockMDs[m];
         if (this.extraData != null && this.extraData[m] != null && !this.extraData[m].equals("")) {
            str = str + "-" + this.extraData[m];
         }
      }

      return str;
   }

   private BlockAndMeta getPrimaryBlock() {
      int[] hist = new int[this.blockIDs.length];

      for (int l = 0; l < hist.length; l++) {
         for (int m = 0; m < hist.length; m++) {
            if (this.blockIDs[l] == this.blockIDs[m]) {
               hist[l]++;
            }
         }
      }

      int maxFreq = 0;
      int pos = 0;

      for (int l = 0; l < hist.length; l++) {
         if (hist[l] > maxFreq) {
            maxFreq = hist[l];
            pos = l;
         }
      }

      return (BlockAndMeta)(this.extraData != null && this.extraData[pos] != null && !this.extraData[pos].equals("")
         ? new BlockExtended(this.blockIDs[pos], this.blockMDs[pos], this.extraData[pos])
         : new BlockAndMeta(this.blockIDs[pos], this.blockMDs[pos]));
   }

   public boolean isSpecial(Block block) {
      return block instanceof BlockAir || block instanceof BlockMobSpawner || block instanceof BlockChest;
   }
}
