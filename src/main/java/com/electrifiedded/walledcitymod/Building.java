package com.electrifiedded.walledcitymod;

import com.electrifiedded.archaiclib.legacy.world.LegacyWorld;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockMobSpawner;
import net.minecraft.block.BlockVine;
import net.minecraft.entity.item.EntityPainting;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.village.VillageDoorInfo;
import net.minecraft.world.World;

public class Building {
   public static final int HIT_WATER = -666;
   public static final String EASY_CHEST = "EASY";
   public static final String MEDIUM_CHEST = "MEDIUM";
   public static final String HARD_CHEST = "HARD";
   public static final String TOWER_CHEST = "TOWER";
   public static final int DIR_NORTH = 0;
   public static final int DIR_EAST = 1;
   public static final int DIR_SOUTH = 2;
   public static final int DIR_WEST = 3;
   public static final int ROT_R = 1;
   public static final int R_HAND = 1;
   public static final int L_HAND = -1;
   public static final int SEA_LEVEL = 63;
   public static final int WORLD_MAX_Y = 255;
   protected final World world;
   protected final Random random;
   protected TemplateRule bRule;
   public int bWidth;
   public int bHeight;
   public int bLength;
   public final int bID;
   private final LinkedList<PlacedBlock> delayedBuildQueue;
   protected final WorldGeneratorThread wgt;
   protected boolean centerAligned;
   protected int i0;
   protected int j0;
   protected int k0;
   private int xI;
   private int yI;
   private int xK;
   private int yK;
   protected int bHand;
   protected int bDir;
   public static final int IGNORE_WATER = -1;
   public static final int PAINTING_BLOCK_OFFSET = 3;
   public static final String[] SPAWNERS = new String[]{
      "Zombie",
      "Skeleton",
      "Spider",
      "Creeper",
      "PigZombie",
      "Ghast",
      "Enderman",
      "CaveSpider",
      "Blaze",
      "Slime",
      "LavaSlime",
      "Villager",
      "SnowMan",
      "MushroomCow",
      "Sheep",
      "Cow",
      "Chicken",
      "Squid",
      "Wolf",
      "Giant",
      "Silverfish",
      "EnderDragon",
      "Ozelot",
      "VillagerGolem",
      "WitherBoss",
      "Bat",
      "Witch"
   };
   public static final int[] BED_META_TO_DIR = new int[]{2, 3, 0, 1};
   public static final int[] STAIRS_META_TO_DIR = new int[]{1, 3, 2, 0};
   public static final int[] LADDER_META_TO_DIR = new int[]{0, 2, 3, 1};
   public static final int[] TRAPDOOR_META_TO_DIR = new int[]{2, 0, 1, 3};
   public static final int[] VINES_META_TO_DIR = new int[]{0, 2, 3, 0, 0, 0, 0, 0, 1};
   public static final int[] DOOR_META_TO_DIR = new int[]{3, 0, 1, 2};
   public static final int[] BED_DIR_TO_META = new int[]{2, 3, 0, 1};
   public static final int[] BUTTON_DIR_TO_META = new int[]{4, 1, 3, 2};
   public static final int[] STAIRS_DIR_TO_META = new int[]{3, 0, 2, 1};
   public static final int[] LADDER_DIR_TO_META = new int[]{2, 5, 3, 4};
   public static final int[] TRAPDOOR_DIR_TO_META = new int[]{1, 2, 0, 3};
   public static final int[] VINES_DIR_TO_META = new int[]{4, 8, 1, 2};
   public static final int[] DOOR_DIR_TO_META = new int[]{3, 0, 1, 2};
   public static final int[] PAINTING_DIR_TO_FACEDIR = new int[]{0, 3, 2, 1};
   public static final int[] DIR_TO_I = new int[]{0, 1, 0, -1};
   public static final int[] DIR_TO_K = new int[]{-1, 0, 1, 0};
   public static final int[] DIR_TO_X = new int[]{0, 1, 0, -1};
   public static final int[] DIR_TO_Y = new int[]{1, 0, -1, 0};
   public static final BlockAndMeta WEST_FACE_TORCH_BLOCK = new BlockAndMeta(Blocks.TORCH, BUTTON_DIR_TO_META[3]);
   public static final BlockAndMeta EAST_FACE_TORCH_BLOCK = new BlockAndMeta(Blocks.TORCH, BUTTON_DIR_TO_META[1]);
   public static final BlockAndMeta NORTH_FACE_TORCH_BLOCK = new BlockAndMeta(Blocks.TORCH, BUTTON_DIR_TO_META[0]);
   public static final BlockAndMeta SOUTH_FACE_TORCH_BLOCK = new BlockAndMeta(Blocks.TORCH, BUTTON_DIR_TO_META[2]);
   public static final BlockAndMeta EAST_FACE_LADDER_BLOCK = new BlockAndMeta(Blocks.LADDER, LADDER_DIR_TO_META[1]);
   public static final BlockAndMeta HOLE_BLOCK_LIGHTING = new BlockAndMeta(Blocks.AIR, 0);
   public static final BlockAndMeta HOLE_BLOCK_NO_LIGHTING = new BlockAndMeta(Blocks.AIR, 1);
   public static final BlockAndMeta PRESERVE_BLOCK = new BlockExtended(Blocks.AIR, 0, "PRESERVE");
   public static final BlockAndMeta TOWER_CHEST_BLOCK = new BlockExtended(Blocks.CHEST, 0, "TOWER");
   public static final BlockAndMeta HARD_CHEST_BLOCK = new BlockExtended(Blocks.CHEST, 0, "HARD");
   public static final BlockAndMeta GHAST_SPAWNER = new BlockExtended(Blocks.MOB_SPAWNER, 0, "Ghast");
   public static final int MAX_SPHERE_DIAM = 40;
   public static final int[][] SPHERE_SHAPE = new int[41][];
   public static final int[][][] CIRCLE_SHAPE = new int[41][][];
   public static final int[][][] CIRCLE_CRENEL = new int[41][][];
   private static final int LIGHTING_INVERSE_DENSITY = 10;
   private static final boolean[] randLightingHash;
   public static final String[] CHEST_TYPE_LABELS;
   public static final int[] DEFAULT_CHEST_TRIES;
   public static final Object[][][] DEFAULT_CHEST_ITEMS;

   public Building(int ID_, WorldGeneratorThread wgt_, TemplateRule buildingRule_, int dir_, int axXHand_, boolean centerAligned_, int[] dim, int[] alignPt) {
      this.bID = ID_;
      this.wgt = wgt_;
      this.world = this.wgt.world;
      this.bRule = buildingRule_;
      if (this.bRule == null) {
         this.bRule = TemplateRule.STONE_RULE;
      }

      this.bWidth = dim[0];
      this.bHeight = dim[1];
      this.bLength = dim[2];
      this.random = this.wgt.random;
      this.bHand = axXHand_;
      this.centerAligned = centerAligned_;
      this.setPrimaryAx(dir_);
      if (alignPt != null && alignPt.length == 3) {
         if (this.centerAligned) {
            this.setOrigin(alignPt[0] - this.xI * this.bWidth / 2, alignPt[1], alignPt[2] - this.xK * this.bWidth / 2);
         } else {
            this.setOrigin(alignPt[0], alignPt[1], alignPt[2]);
         }
      }

      this.delayedBuildQueue = new LinkedList<>();
   }

   public final int getI(int x, int y) {
      return this.i0 + this.yI * y + this.xI * x;
   }

   public final int[] getIJKPt(int x, int z, int y) {
      return new int[]{this.i0 + this.yI * y + this.xI * x, this.j0 + z, this.k0 + this.yK * y + this.xK * x};
   }

   public final int getJ(int z) {
      return this.j0 + z;
   }

   public final int getK(int x, int y) {
      return this.k0 + this.yK * y + this.xK * x;
   }

   public final int[] getSurfaceIJKPt(int x, int y, int j, boolean wallIsSurface, int waterSurfaceBuffer) {
      int[] pt = this.getIJKPt(x, 0, y);
      pt[1] = findSurfaceJ(this.world, pt[0], pt[2], j, wallIsSurface, waterSurfaceBuffer);
      return pt;
   }

   public final int getX(int[] pt) {
      return this.xI * (pt[0] - this.i0) + this.xK * (pt[2] - this.k0);
   }

   public final int getY(int[] pt) {
      return this.yI * (pt[0] - this.i0) + this.yK * (pt[2] - this.k0);
   }

   public final int getZ(int[] pt) {
      return pt[1] - this.j0;
   }

   public final String localCoordString(int x, int z, int y) {
      int[] pt = this.getIJKPt(x, z, y);
      return "(" + pt[0] + "," + pt[1] + "," + pt[2] + ")";
   }

   public int orientDirToBDir(int dir) {
      return this.bHand < 0 && dir % 2 == 1 ? this.bDir + dir + 2 & 3 : this.bDir + dir & 3;
   }

   public void setPainting(int[] pt, int metadata) {
      int dir = this.orientDirToBDir(LADDER_META_TO_DIR[metadata]);
      pt[0] -= DIR_TO_I[dir];
      pt[2] -= DIR_TO_K[dir];
      if (dir == 0) {
         pt[2]++;
      } else if (dir == 2) {
         pt[2]--;
      } else if (dir == 3) {
         pt[0]++;
      } else {
         pt[0]--;
      }

      final net.minecraft.util.math.BlockPos ppos = LegacyWorld.pos(pt[0], pt[1], pt[2]);
      final net.minecraft.util.EnumFacing pface = net.minecraft.util.EnumFacing.byHorizontalIndex(PAINTING_DIR_TO_FACEDIR[dir]);
      final World pw = this.world;
      GenAccess.onMain(() -> {
         // only where the wall already exists; never loads a chunk
         if (pw.isRemote || !pw.isAreaLoaded(ppos, 2)) return;
         EntityPainting entitypainting = new EntityPainting(pw, ppos, pface);
         if (entitypainting.onValidSurface()) {
            pw.spawnEntity(entitypainting);
         }
      });
   }

   public void setPrimaryAx(int dir_) {
      this.bDir = dir_;
      switch (this.bDir) {
         case 0:
            this.xI = this.bHand;
            this.yI = 0;
            this.xK = 0;
            this.yK = -1;
            break;
         case 1:
            this.xI = 0;
            this.yI = 1;
            this.xK = this.bHand;
            this.yK = 0;
            break;
         case 2:
            this.xI = -this.bHand;
            this.yI = 0;
            this.xK = 0;
            this.yK = 1;
            break;
         case 3:
            this.xI = 0;
            this.yI = -1;
            this.xK = -this.bHand;
            this.yK = 0;
      }
   }

   public void setSignOrPost(int x2, int z2, int y2, boolean post, int sDir, String[] lines) {
      int[] pt = this.getIJKPt(x2, z2, y2);
      GenAccess.setBlock(this.world, pt[0], pt[1], pt[2], post ? Blocks.STANDING_SIGN : Blocks.WALL_SIGN, sDir, 2);
      TileEntitySign tileentitysign = (TileEntitySign)GenAccess.getTileEntity(this.world, pt[0], pt[1], pt[2]);
      if (tileentitysign != null) {
         for (int n = 0; n < Math.min(lines.length, 4); n++) {
            tileentitysign.signText[n] = new net.minecraft.util.text.TextComponentString(lines[n] == null ? "" : lines[n]);
         }
      }
   }

   public void buildDown(int x, int z, int y, TemplateRule buildRule, int maxDepth, int foundationDepth, int reserveDepth) {
      int stopZ = z;

      while (stopZ > z - maxDepth && this.isWallable(x, stopZ, y)) {
         stopZ--;
      }

      if (stopZ == z - maxDepth && this.isWallable(x, z - maxDepth, y)) {
         stopZ = z - reserveDepth;
      } else {
         stopZ -= foundationDepth;
      }

      for (int z1 = z; z1 > stopZ; z1--) {
         this.setBlockWithLightingLocal(x, z1, y, buildRule, false);
      }
   }

   protected final Block getBlockIdLocal(int x, int z, int y) {
      return GenAccess.getBlock(this.world, this.i0 + this.yI * y + this.xI * x, this.j0 + z, this.k0 + this.yK * y + this.xK * x);
   }

   protected final int getBlockMetadataLocal(int x, int z, int y) {
      return GenAccess.getBlockMetadata(this.world, this.i0 + this.yI * y + this.xI * x, this.j0 + z, this.k0 + this.yK * y + this.xK * x);
   }

   protected final String IDString() {
      String str = "ID=" + this.bID + " axes(Y,X)=";
      switch (this.bDir) {
         case 0:
            return str + "(N," + (this.bHand > 0 ? "E)" : "W)");
         case 1:
            return str + "(E," + (this.bHand > 0 ? "S)" : "N)");
         case 2:
            return str + "(S," + (this.bHand > 0 ? "W)" : "E)");
         case 3:
            return str + "(W," + (this.bHand > 0 ? "N)" : "S)");
         default:
            return "Error - bad dir value for ID=" + this.bID;
      }
   }

   protected final boolean isArtificialWallBlock(int x, int z, int y) {
      Block blockId = this.getBlockIdLocal(x, z, y);
      return BlockProperties.get(blockId).isArtificial
         && (
            blockId != Blocks.SANDSTONE
               || this.getBlockIdLocal(x, z + 1, y) != Blocks.SAND && this.getBlockIdLocal(x, z + 2, y) != Blocks.SAND
         );
   }

   protected final boolean isDoorway(int x, int z, int y) {
      return this.isFloor(x, z, y)
         && (this.isWallBlock(x + 1, z, y) && this.isWallBlock(x - 1, z, y) || this.isWallBlock(x, z, y + 1) && this.isWallBlock(x - 1, z, y - 1));
   }

   protected final boolean hasNoDoorway(int x, int z, int y) {
      return !this.isDoorway(x - 1, z, y) && !this.isDoorway(x + 1, z, y) && !this.isDoorway(x, z, y - 1) && !this.isDoorway(x - 1, z, y + 1);
   }

   protected boolean isObstructedFrame(int zstart, int ybuffer) {
      for (int z1 = zstart; z1 < this.bHeight; z1++) {
         for (int x1 = 0; x1 < this.bWidth; x1++) {
            if (this.isArtificialWallBlock(x1, z1, this.bLength - 1)) {
               return true;
            }
         }

         for (int y1 = ybuffer; y1 < this.bLength - 1; y1++) {
            if (this.isArtificialWallBlock(0, z1, y1)) {
               return true;
            }

            if (this.isArtificialWallBlock(this.bWidth - 1, z1, y1)) {
               return true;
            }
         }
      }

      return false;
   }

   protected boolean isObstructedSolid(int[] pt1, int[] pt2) {
      for (int x1 = pt1[0]; x1 <= pt2[0]; x1++) {
         for (int z1 = pt1[1]; z1 <= pt2[1]; z1++) {
            for (int y1 = pt1[2]; y1 <= pt2[2]; y1++) {
               if (!this.isWallable(x1, z1, y1)) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   protected final boolean isFloor(int x, int z, int y) {
      Block blkId1 = this.getBlockIdLocal(x, z, y);
      Block blkId2 = this.getBlockIdLocal(x, z - 1, y);
      return blkId1 == Blocks.AIR && BlockProperties.get(blkId2).isArtificial && blkId2 != Blocks.LADDER;
   }

   protected final boolean isStairBlock(int x, int z, int y) {
      Block blkId = this.getBlockIdLocal(x, z, y);
      return blkId == Blocks.STONE_SLAB || BlockProperties.get(blkId).isStair;
   }

   protected final boolean isWallable(int x, int z, int y) {
      return BlockProperties.get(GenAccess.getBlock(this.world, this.i0 + this.yI * y + this.xI * x, this.j0 + z, this.k0 + this.yK * y + this.xK * x)).isWallable;
   }

   protected final boolean isWallableIJK(int[] pt) {
      return pt != null && BlockProperties.get(GenAccess.getBlock(this.world, pt[0], pt[1], pt[2])).isWallable;
   }

   protected final boolean isWallBlock(int x, int z, int y) {
      return BlockProperties.get(GenAccess.getBlock(this.world, this.i0 + this.yI * y + this.xI * x, this.j0 + z, this.k0 + this.yK * y + this.xK * x)).isArtificial;
   }

   public final void flushDelayed() {
      while (this.delayedBuildQueue.size() > 0) {
         PlacedBlock block = this.delayedBuildQueue.poll();
         this.setDelayed(block.get(), block.x, block.y, block.z, block.getMeta());
      }
   }

   protected void setDelayed(Block blc, int... block) {
      if (BlockProperties.get(blc).isStair) {
         BlockAndMeta temp = this.getDelayedStair(blc, block);
         blc = temp.get();
         block[3] = temp.getMeta();
      } else if (blc instanceof BlockVine) {
         if (block[3] == 0 && !isSolidBlock(GenAccess.getBlock(this.world, block[0], block[1] + 1, block[2]))) {
            block[3] = 1;
         }

         if (block[3] != 0) {
            int dir = VINES_META_TO_DIR[block[3]];

            while (!isSolidBlock(GenAccess.getBlock(this.world, block[0] + DIR_TO_I[dir], block[1], block[2] + DIR_TO_K[dir]))) {
               dir = (dir + 1) % 4;
               if (dir == VINES_META_TO_DIR[block[3]]) {
                  if (!isSolidBlock(GenAccess.getBlock(this.world, block[0], block[1] + 1, block[2]))) {
                     return;
                  }

                  dir = -1;
                  break;
               }
            }

            block[4] = dir == -1 ? 0 : VINES_DIR_TO_META[dir];
         }
      }

      if (blc == Blocks.AIR && block[3] >= 3) {
         this.setPainting(block, block[3] - 3);
      } else if (blc == Blocks.TORCH) {
         if (Blocks.TORCH.canPlaceBlockAt(this.world, LegacyWorld.pos(block[0], block[1], block[2]))) {
            GenAccess.setBlock(this.world, block[0], block[1], block[2], blc, block[3], 3);
         }
      } else if (blc == Blocks.GLOWSTONE) {
         GenAccess.setBlock(this.world, block[0], block[1], block[2], blc, block[3], 3);
      } else if (blc != null) {
         if (randLightingHash[block[0] & 7 | block[1] & 56 | block[2] & 448]) {
            GenAccess.setBlock(this.world, block[0], block[1], block[2], blc, block[3], 3);
         } else {
            setBlockAndMetaNoLighting(this.world, block[0], block[1], block[2], blc, block[3]);
         }
      }
   }

   protected BlockAndMeta getDelayedStair(Block blc, int... block) {
      int dirX = block[0] - DIR_TO_I[STAIRS_META_TO_DIR[block[3] % 4]];
      int dirZ = block[2] - DIR_TO_K[STAIRS_META_TO_DIR[block[3] % 4]];
      if (GenAccess.getHeight(this.world, dirX, dirZ) > block[1]) {
         Block adjId = GenAccess.getBlock(this.world, dirX, block[1], dirZ);
         Block aboveID = GenAccess.getBlock(this.world, block[0], block[1] + 1, block[2]);
         if (BlockProperties.get(aboveID).isGround && BlockProperties.get(adjId).isGround) {
            return new BlockAndMeta(blc, block[3]).stairToSolid();
         }

         if (!BlockProperties.get(adjId).isWallable || !BlockProperties.get(aboveID).isWallable) {
            return new BlockAndMeta(null, 0);
         }
      }

      return new BlockAndMeta(blc, block[3]);
   }

   protected final void recenterFromOldWidth(int oldWidth) {
      this.i0 = this.i0 + this.xI * (oldWidth - this.bWidth) / 2;
      this.k0 = this.k0 + this.xK * (oldWidth - this.bWidth) / 2;
   }

   protected final void setBlockLocal(int x, int z, int y, Block blockID) {
      this.setBlockLocal(x, z, y, blockID, 0);
   }

   protected final void setBlockLocal(int x, int z, int y, Block blockID, int metadata) {
      int[] pt = this.getIJKPt(x, z, y);
      if (blockID != Blocks.AIR || !GenAccess.isAirBlock(this.world, pt[0], pt[1], pt[2])) {
         if (!(blockID instanceof BlockChest)) {
            this.emptyIfChest(pt);
         }

         if (BlockProperties.get(blockID).isDelayed) {
            this.delayedBuildQueue.offer(new PlacedBlock(blockID, new int[]{pt[0], pt[1], pt[2], this.rotateMetadata(blockID, metadata)}));
         } else if (randLightingHash[x & 7 | y & 56 | z & 448]) {
            GenAccess.setBlock(this.world, pt[0], pt[1], pt[2], blockID, this.rotateMetadata(blockID, metadata), 2);
         } else {
            setBlockAndMetaNoLighting(this.world, pt[0], pt[1], pt[2], blockID, this.rotateMetadata(blockID, metadata));
         }

         if (BlockProperties.get(blockID).isDoor) {
            this.addDoorToNewListIfAppropriate(pt[0], pt[1], pt[2]);
         }
      }
   }

   protected final void setBlockLocal(int x, int z, int y, BlockAndMeta block) {
      if (block instanceof BlockExtended) {
         this.setSpecialBlockLocal(x, z, y, block.get(), block.getMeta(), ((BlockExtended)block).info);
      } else {
         this.setBlockLocal(x, z, y, block.get(), block.getMeta());
      }
   }

   protected final void setBlockLocal(int x, int z, int y, TemplateRule rule) {
      this.setBlockLocal(x, z, y, rule.getBlockOrHole(this.random));
   }

   protected final void setBlockWithLightingLocal(int x, int z, int y, TemplateRule rule, boolean lighting) {
      this.setBlockWithLightingLocal(x, z, y, rule.getBlockOrHole(this.random), lighting);
   }

   protected final void removeBlockWithLighting(int x, int z, int y) {
      this.setBlockWithLightingLocal(x, z, y, TemplateRule.AIR_RULE, true);
   }

   protected final void setBlockWithLightingLocal(int x, int z, int y, BlockAndMeta block, boolean lighting) {
      if (block instanceof BlockExtended) {
         this.setSpecialBlockLocal(x, z, y, block.get(), block.getMeta(), ((BlockExtended)block).info);
      } else {
         this.setBlockWithLightingLocal(x, z, y, block.get(), block.getMeta(), lighting);
      }
   }

   protected final void setBlockWithLightingLocal(int x, int z, int y, Block blockID, int metadata, boolean lighting) {
      int[] pt = this.getIJKPt(x, z, y);
      if (blockID != Blocks.AIR || !GenAccess.isAirBlock(this.world, pt[0], pt[1], pt[2])) {
         if (!(blockID instanceof BlockChest)) {
            this.emptyIfChest(pt);
         }

         if (BlockProperties.get(blockID).isDelayed) {
            this.delayedBuildQueue.offer(new PlacedBlock(blockID, new int[]{pt[0], pt[1], pt[2], this.rotateMetadata(blockID, metadata)}));
         } else if (lighting) {
            GenAccess.setBlock(this.world, pt[0], pt[1], pt[2], blockID, this.rotateMetadata(blockID, metadata), 3);
         } else {
            setBlockAndMetaNoLighting(this.world, pt[0], pt[1], pt[2], blockID, this.rotateMetadata(blockID, metadata));
         }

         if (BlockProperties.get(blockID).isDoor) {
            this.addDoorToNewListIfAppropriate(pt[0], pt[1], pt[2]);
         }
      }
   }

   protected final void setOrigin(int i0_, int j0_, int k0_) {
      this.i0 = i0_;
      this.j0 = j0_;
      this.k0 = k0_;
   }

   protected final void setOriginLocal(int i1, int j1, int k1, int x, int z, int y) {
      this.i0 = i1 + this.yI * y + this.xI * x;
      this.j0 = j1 + z;
      this.k0 = k1 + this.yK * y + this.xK * x;
   }

   protected final void setSpecialBlockLocal(int x, int z, int y, Block blockID, int metadata, String extra) {
      if (!extra.equals("PRESERVE")) {
         int[] pt = this.getIJKPt(x, z, y);
         if (blockID instanceof BlockAir) {
            if (extra.equals("WALL_STAIR") && metadata <= 0) {
               GenAccess.setBlock(this.world, pt[0], pt[1], pt[2], Blocks.STONE_SLAB, this.rotateMetadata(Blocks.STONE_SLAB, -metadata), 2);
               return;
            }

            if (extra.equals("PAINTING") && metadata >= 3) {
               this.delayedBuildQueue.offer(new PlacedBlock(blockID, new int[]{pt[0], pt[1], pt[2], metadata}));
               return;
            }

            Block presentBlock = GenAccess.getBlock(this.world, pt[0], pt[1], pt[2]);
            if (!GenAccess.isAirBlock(this.world, pt[0], pt[1], pt[2])
               && !BlockProperties.get(presentBlock).isWater
               && !BlockProperties.get(GenAccess.getBlock(this.world, pt[0] - 1, pt[1], pt[2])).isWater
               && !BlockProperties.get(GenAccess.getBlock(this.world, pt[0], pt[1], pt[2] - 1)).isWater
               && !BlockProperties.get(GenAccess.getBlock(this.world, pt[0] + 1, pt[1], pt[2])).isWater
               && !BlockProperties.get(GenAccess.getBlock(this.world, pt[0], pt[1], pt[2] + 1)).isWater
               && !BlockProperties.get(GenAccess.getBlock(this.world, pt[0], pt[1] + 1, pt[2])).isWater) {
               GenAccess.setBlockToAir(this.world, pt[0], pt[1], pt[2]);
            }
         } else if (blockID instanceof BlockMobSpawner) {
            this.setMobSpawner(pt, blockID, metadata, extra);
         } else if (blockID instanceof BlockChest) {
            this.setLootChest(pt, blockID, metadata, extra);
         } else {
            GenAccess.setBlock(this.world, pt[0], pt[1], pt[2], blockID, metadata, 2);
         }
      }
   }

   private void addDoorToNewListIfAppropriate(int par1, int par2, int par3) {
      if (this.wgt instanceof WorldGenWalledCity) {
         int id = this.getKnownBuilding();
         if (id == -1) {
            ((PopulatorWalledCity)this.wgt.master).cityDoors.put(this.bID, new ArrayList<>());
            id = this.bID;
         }

         int l = (GenAccess.getBlockMetadata(this.world, par1, par2, par3) & 3);
         if (l != 0 && l != 2) {
            int i1 = 0;

            for (int j1 = -5; j1 < 0; j1++) {
               if (GenAccess.canSeeSky(this.world, LegacyWorld.pos(par1, par2, par3 + j1))) {
                  i1--;
               }
            }

            for (int j1x = 1; j1x <= 5; j1x++) {
               if (GenAccess.canSeeSky(this.world, LegacyWorld.pos(par1, par2, par3 + j1x))) {
                  i1++;
               }
            }

            if (i1 != 0) {
               ((PopulatorWalledCity)this.wgt.master).cityDoors.get(id).add(new VillageDoorInfo(LegacyWorld.pos(par1, par2, par3), 0, i1 > 0 ? -2 : 2, 0));
            }
         } else {
            int i1 = 0;

            for (int j1xx = -5; j1xx < 0; j1xx++) {
               if (GenAccess.canSeeSky(this.world, LegacyWorld.pos(par1 + j1xx, par2, par3))) {
                  i1--;
               }
            }

            for (int j1xxx = 1; j1xxx <= 5; j1xxx++) {
               if (GenAccess.canSeeSky(this.world, LegacyWorld.pos(par1 + j1xxx, par2, par3))) {
                  i1++;
               }
            }

            if (i1 != 0) {
               ((PopulatorWalledCity)this.wgt.master).cityDoors.get(id).add(new VillageDoorInfo(LegacyWorld.pos(par1, par2, par3), i1 > 0 ? -2 : 2, 0, 0));
            }
         }
      }
   }

   private void emptyIfChest(int[] pt) {
      if (pt != null && GenAccess.getBlock(this.world, pt[0], pt[1], pt[2]) instanceof BlockChest) {
         TileEntityChest tileentitychest = (TileEntityChest)GenAccess.getTileEntity(this.world, pt[0], pt[1], pt[2]);

         for (int m = 0; m < tileentitychest.getSizeInventory(); m++) {
            tileentitychest.setInventorySlotContents(m, ItemStack.EMPTY);
         }
      }
   }

   private ItemStack getChestItemstack(String chestType) {
      if (chestType.equals("TOWER") && this.random.nextInt(4) == 0) {
         return new ItemStack(this.bRule.primaryBlock.get(), this.random.nextInt(10), this.bRule.primaryBlock.getMeta());
      } else {
         Object[][] itempool = this.wgt.chestItems.get(chestType);
         int idx = pickWeightedOption(this.world.rand, Arrays.asList(itempool[3]), Arrays.asList(itempool[0]));
         Object obj = itempool[1][idx];
         if (obj == null) {
            return null;
         } else {
            if (obj instanceof Block) {
               if (obj == Blocks.AIR) {
                  return null;
               }

               obj = Item.getItemFromBlock((Block)obj);
            }

            return Compat.stack(
               (Item)obj,
               Integer.class.cast(itempool[4][idx]) + this.random.nextInt(Integer.class.cast(itempool[5][idx]) - Integer.class.cast(itempool[4][idx]) + 1),
               Integer.class.cast(itempool[2][idx])
            );
         }
      }
   }

   private int getKnownBuilding() {
      Set<?> keys = ((PopulatorWalledCity)this.wgt.master).cityDoors.keySet();

      for (int id = this.bID - 3; id < this.bID + 4; id++) {
         if (keys.contains(id)) {
            return id;
         }
      }

      return -1;
   }

   private int rotateMetadata(Block blockID, int metadata) {
      int tempdata = 0;
      if (BlockProperties.get(blockID).isStair) {
         if (metadata >= 4) {
            tempdata += 4;
            metadata -= 4;
         }

         return STAIRS_DIR_TO_META[this.orientDirToBDir(STAIRS_META_TO_DIR[metadata])] + tempdata;
      } else if (BlockProperties.get(blockID).isDoor) {
         if (metadata >= 8) {
            return metadata;
         } else {
            if (metadata >= 4) {
               tempdata += 4;
            }

            return DOOR_DIR_TO_META[this.orientDirToBDir(DOOR_META_TO_DIR[metadata % 4])] + tempdata;
         }
      } else if (blockID != Blocks.LEVER && blockID != Blocks.STONE_BUTTON && blockID != Blocks.WOODEN_BUTTON) {
         if (blockID != Blocks.TORCH && blockID != Blocks.REDSTONE_TORCH && blockID != Blocks.UNLIT_REDSTONE_TORCH) {
            if (blockID != Blocks.LADDER
               && blockID != Blocks.DISPENSER
               && blockID != Blocks.FURNACE
               && blockID != Blocks.LIT_FURNACE
               && blockID != Blocks.WALL_SIGN
               && blockID != Blocks.PISTON
               && blockID != Blocks.STICKY_PISTON
               && blockID != Blocks.CHEST
               && blockID != Blocks.HOPPER
               && blockID != Blocks.DROPPER) {
               if (blockID != Blocks.RAIL
                  && blockID != Blocks.GOLDEN_RAIL
                  && blockID != Blocks.DETECTOR_RAIL
                  && blockID != Blocks.ACTIVATOR_RAIL) {
                  if (blockID == Blocks.BED
                     || blockID == Blocks.OAK_FENCE_GATE
                     || blockID == Blocks.TRIPWIRE_HOOK
                     || blockID == Blocks.PUMPKIN
                     || blockID == Blocks.LIT_PUMPKIN
                     || blockID == Blocks.POWERED_REPEATER
                     || blockID == Blocks.UNPOWERED_REPEATER) {
                     while (metadata >= 4) {
                        tempdata += 4;
                        metadata -= 4;
                     }

                     if (blockID == Blocks.TRAPDOOR) {
                        return TRAPDOOR_DIR_TO_META[this.orientDirToBDir(TRAPDOOR_META_TO_DIR[metadata])] + tempdata;
                     }

                     return BED_DIR_TO_META[this.orientDirToBDir(BED_META_TO_DIR[metadata])] + tempdata;
                  }

                  if (blockID == Blocks.VINE) {
                     if (metadata == 0) {
                        return 0;
                     }

                     if (metadata != 1 && metadata != 2 && metadata != 4 && metadata != 8) {
                        return 1;
                     }

                     return VINES_DIR_TO_META[(this.bDir + VINES_META_TO_DIR[metadata]) % 4];
                  }

                  if (blockID == Blocks.STANDING_SIGN) {
                     switch (this.bDir) {
                        case 0:
                           if (metadata == 0) {
                              return this.bHand == 1 ? 0 : 8;
                           }

                           if (metadata == 1) {
                              return this.bHand == 1 ? 1 : 7;
                           }

                           if (metadata == 2) {
                              return this.bHand == 1 ? 2 : 6;
                           }

                           if (metadata == 3) {
                              return this.bHand == 1 ? 3 : 5;
                           }

                           if (metadata == 4) {
                              return 4;
                           }

                           if (metadata == 5) {
                              return this.bHand == 1 ? 5 : 3;
                           }

                           if (metadata == 6) {
                              return this.bHand == 1 ? 6 : 2;
                           }

                           if (metadata == 7) {
                              return this.bHand == 1 ? 7 : 1;
                           }

                           if (metadata == 8) {
                              return this.bHand == 1 ? 8 : 0;
                           }

                           if (metadata == 9) {
                              return this.bHand == 1 ? 9 : 15;
                           }

                           if (metadata == 10) {
                              return this.bHand == 1 ? 10 : 14;
                           }

                           if (metadata == 11) {
                              return this.bHand == 1 ? 11 : 13;
                           }

                           if (metadata == 12) {
                              return 12;
                           }

                           if (metadata == 13) {
                              return this.bHand == 1 ? 13 : 11;
                           }

                           if (metadata == 14) {
                              return this.bHand == 1 ? 14 : 10;
                           }

                           if (metadata == 15) {
                              return this.bHand == 1 ? 15 : 9;
                           }
                        case 1:
                           if (metadata == 0) {
                              return this.bHand == 1 ? 4 : 12;
                           }

                           if (metadata == 1) {
                              return this.bHand == 1 ? 5 : 11;
                           }

                           if (metadata == 2) {
                              return this.bHand == 1 ? 6 : 10;
                           }

                           if (metadata == 3) {
                              return this.bHand == 1 ? 7 : 9;
                           }

                           if (metadata == 4) {
                              return 8;
                           }

                           if (metadata == 5) {
                              return this.bHand == 1 ? 9 : 7;
                           }

                           if (metadata == 6) {
                              return this.bHand == 1 ? 10 : 6;
                           }

                           if (metadata == 7) {
                              return this.bHand == 1 ? 11 : 5;
                           }

                           if (metadata == 8) {
                              return this.bHand == 1 ? 12 : 4;
                           }

                           if (metadata == 9) {
                              return this.bHand == 1 ? 13 : 3;
                           }

                           if (metadata == 10) {
                              return this.bHand == 1 ? 14 : 2;
                           }

                           if (metadata == 11) {
                              return this.bHand == 1 ? 15 : 1;
                           }

                           if (metadata == 12) {
                              return 0;
                           }

                           if (metadata == 13) {
                              return this.bHand == 1 ? 1 : 15;
                           }

                           if (metadata == 14) {
                              return this.bHand == 1 ? 2 : 14;
                           }

                           if (metadata == 15) {
                              return this.bHand == 1 ? 3 : 13;
                           }
                        case 2:
                           if (metadata == 0) {
                              return this.bHand == 1 ? 8 : 0;
                           }

                           if (metadata == 1) {
                              return this.bHand == 1 ? 9 : 15;
                           }

                           if (metadata == 2) {
                              return this.bHand == 1 ? 10 : 14;
                           }

                           if (metadata == 3) {
                              return this.bHand == 1 ? 11 : 13;
                           }

                           if (metadata == 4) {
                              return 12;
                           }

                           if (metadata == 5) {
                              return this.bHand == 1 ? 13 : 11;
                           }

                           if (metadata == 6) {
                              return this.bHand == 1 ? 14 : 10;
                           }

                           if (metadata == 7) {
                              return this.bHand == 1 ? 15 : 9;
                           }

                           if (metadata == 8) {
                              return this.bHand == 1 ? 0 : 8;
                           }

                           if (metadata == 9) {
                              return this.bHand == 1 ? 1 : 7;
                           }

                           if (metadata == 10) {
                              return this.bHand == 1 ? 2 : 6;
                           }

                           if (metadata == 11) {
                              return this.bHand == 1 ? 3 : 5;
                           }

                           if (metadata == 12) {
                              return 4;
                           }

                           if (metadata == 13) {
                              return this.bHand == 1 ? 5 : 3;
                           }

                           if (metadata == 14) {
                              return this.bHand == 1 ? 6 : 2;
                           }

                           if (metadata == 15) {
                              return this.bHand == 1 ? 7 : 1;
                           }
                        case 3:
                           if (metadata == 0) {
                              return this.bHand == 1 ? 12 : 4;
                           }

                           if (metadata == 1) {
                              return this.bHand == 1 ? 13 : 3;
                           }

                           if (metadata == 2) {
                              return this.bHand == 1 ? 14 : 2;
                           }

                           if (metadata == 3) {
                              return this.bHand == 1 ? 15 : 1;
                           }

                           if (metadata == 4) {
                              return 0;
                           }

                           if (metadata == 5) {
                              return this.bHand == 1 ? 1 : 15;
                           }

                           if (metadata == 6) {
                              return this.bHand == 1 ? 2 : 14;
                           }

                           if (metadata == 7) {
                              return this.bHand == 1 ? 3 : 13;
                           }

                           if (metadata == 8) {
                              return this.bHand == 1 ? 4 : 12;
                           }

                           if (metadata == 9) {
                              return this.bHand == 1 ? 5 : 11;
                           }

                           if (metadata == 10) {
                              return this.bHand == 1 ? 6 : 10;
                           }

                           if (metadata == 11) {
                              return this.bHand == 1 ? 7 : 9;
                           }

                           if (metadata == 12) {
                              return 8;
                           }

                           if (metadata == 13) {
                              return this.bHand == 1 ? 9 : 7;
                           }

                           if (metadata == 14) {
                              return this.bHand == 1 ? 10 : 6;
                           }

                           if (metadata == 15) {
                              return this.bHand == 1 ? 11 : 5;
                           }
                     }
                  }
               } else {
                  switch (this.bDir) {
                     case 0:
                        if (metadata == 0) {
                           return 0;
                        }

                        if (metadata == 1) {
                           return 1;
                        }

                        if (metadata == 2) {
                           return 2;
                        }

                        if (metadata == 3) {
                           return 3;
                        }

                        if (metadata == 4) {
                           return this.bHand == 1 ? 4 : 5;
                        }

                        if (metadata == 5) {
                           return this.bHand == 1 ? 5 : 4;
                        }

                        if (metadata == 6) {
                           return this.bHand == 1 ? 6 : 9;
                        }

                        if (metadata == 7) {
                           return this.bHand == 1 ? 7 : 8;
                        }

                        if (metadata == 8) {
                           return this.bHand == 1 ? 8 : 7;
                        }

                        if (metadata == 9) {
                           return this.bHand == 1 ? 9 : 6;
                        }
                     case 1:
                        if (metadata == 0) {
                           return 1;
                        }

                        if (metadata == 1) {
                           return 0;
                        }

                        if (metadata == 2) {
                           return 5;
                        }

                        if (metadata == 3) {
                           return 4;
                        }

                        if (metadata == 4) {
                           return this.bHand == 1 ? 2 : 3;
                        }

                        if (metadata == 5) {
                           return this.bHand == 1 ? 3 : 2;
                        }

                        if (metadata == 6) {
                           return this.bHand == 1 ? 7 : 6;
                        }

                        if (metadata == 7) {
                           return this.bHand == 1 ? 8 : 9;
                        }

                        if (metadata == 8) {
                           return this.bHand == 1 ? 9 : 8;
                        }

                        if (metadata == 9) {
                           return this.bHand == 1 ? 6 : 7;
                        }
                     case 2:
                        if (metadata == 0) {
                           return 0;
                        }

                        if (metadata == 1) {
                           return 1;
                        }

                        if (metadata == 2) {
                           return 3;
                        }

                        if (metadata == 3) {
                           return 2;
                        }

                        if (metadata == 4) {
                           return this.bHand == 1 ? 5 : 4;
                        }

                        if (metadata == 5) {
                           return this.bHand == 1 ? 4 : 5;
                        }

                        if (metadata == 6) {
                           return this.bHand == 1 ? 8 : 7;
                        }

                        if (metadata == 7) {
                           return this.bHand == 1 ? 9 : 6;
                        }

                        if (metadata == 8) {
                           return this.bHand == 1 ? 6 : 9;
                        }

                        if (metadata == 9) {
                           return this.bHand == 1 ? 7 : 8;
                        }
                     case 3:
                        if (metadata == 0) {
                           return 1;
                        }

                        if (metadata == 1) {
                           return 0;
                        }

                        if (metadata == 2) {
                           return 4;
                        }

                        if (metadata == 3) {
                           return 5;
                        }

                        if (metadata == 4) {
                           return this.bHand == 1 ? 3 : 2;
                        }

                        if (metadata == 5) {
                           return this.bHand == 1 ? 2 : 3;
                        }

                        if (metadata == 6) {
                           return this.bHand == 1 ? 9 : 8;
                        }

                        if (metadata == 7) {
                           return this.bHand == 1 ? 6 : 7;
                        }

                        if (metadata == 8) {
                           return this.bHand == 1 ? 7 : 6;
                        }

                        if (metadata == 9) {
                           return this.bHand == 1 ? 8 : 9;
                        }
                  }
               }

               return metadata + tempdata;
            } else {
               if ((blockID == Blocks.PISTON || blockID == Blocks.STICKY_PISTON) && metadata - 8 >= 0) {
                  tempdata += 8;
                  metadata -= 8;
               }

               return metadata <= 1 ? metadata + tempdata : LADDER_DIR_TO_META[this.orientDirToBDir(LADDER_META_TO_DIR[metadata - 2])] + tempdata;
            }
         } else {
            return metadata != 0 && metadata < 5 ? BUTTON_DIR_TO_META[this.orientDirToBDir(STAIRS_META_TO_DIR[metadata - 1])] : metadata;
         }
      } else {
         if (metadata - 8 > 0) {
            tempdata += 8;
            metadata -= 8;
         }

         return metadata != 0 && (blockID != Blocks.LEVER || metadata < 5)
            ? BUTTON_DIR_TO_META[this.orientDirToBDir(STAIRS_META_TO_DIR[metadata - 1])] + tempdata
            : metadata + tempdata;
      }
   }

   private void setLootChest(int[] pt, Block chestBlock, int meta, String chestType) {
      if (GenAccess.setBlock(this.world, pt[0], pt[1], pt[2], chestBlock, meta, 2)) {
         TileEntityChest chest = (TileEntityChest)GenAccess.getTileEntity(this.world, pt[0], pt[1], pt[2]);
         if (this.wgt.chestTries != null && this.wgt.chestTries.containsKey(chestType)) {
            for (int m = 0; m < this.wgt.chestTries.get(chestType); m++) {
               if (this.random.nextBoolean()) {
                  ItemStack itemstack = this.getChestItemstack(chestType);
                  if (itemstack != null && chest != null) {
                     chest.setInventorySlotContents(this.random.nextInt(chest.getSizeInventory()), itemstack);
                  }
               }
            }
         }
      }
   }

   private void setMobSpawner(int[] pt, Block spawner, int metadata, String info) {
      if (GenAccess.setBlock(this.world, pt[0], pt[1], pt[2], spawner, metadata, 2)) {
         TileEntityMobSpawner tileentitymobspawner = (TileEntityMobSpawner)GenAccess.getTileEntity(this.world, pt[0], pt[1], pt[2]);
         if (tileentitymobspawner != null) {
            if (info.equals("UPRIGHT")) {
               if (this.random.nextInt(3) == 0) {
                  this.setMobSpawner(tileentitymobspawner, 1, 3);
               } else {
                  this.setMobSpawner(tileentitymobspawner, 2, 0);
               }
            } else if (info.equals("EASY")) {
               this.setMobSpawner(tileentitymobspawner, 2, 0);
            } else if (info.equals("MEDIUM")) {
               this.setMobSpawner(tileentitymobspawner, 3, 0);
            } else if (info.equals("HARD")) {
               this.setMobSpawner(tileentitymobspawner, 4, 0);
            } else {
               Compat.setSpawner(tileentitymobspawner, info);
            }
         }
      }
   }

   private void setMobSpawner(TileEntityMobSpawner spawner, int nTypes, int offset) {
      String mob = "Pig";
      int n = this.random.nextInt(nTypes) + offset;
      if (n < SPAWNERS.length) {
         mob = SPAWNERS[n];
      }

      Compat.setSpawner(spawner, mob);
   }

   public static int distance(int[] pt1, int[] pt2) {
      return (int)Math.sqrt((pt1[0] - pt2[0]) * (pt1[0] - pt2[0]) + (pt1[1] - pt2[1]) * (pt1[1] - pt2[1]) + (pt1[2] - pt2[2]) * (pt1[2] - pt2[2]));
   }

   public static int findSurfaceJ(World world, int i, int k, int jinit, boolean wallIsSurface, int waterSurfaceBuffer) {
      if (Compat.waterVaporizes(world)) {
         if (i % 2 == 1 ^ k % 2 == 1) {
            for (int j = 127; j > -1; j--) {
               if (GenAccess.isAirBlock(world, i, j, k)) {
                  while (j > -1) {
                     if (!BlockProperties.get(GenAccess.getBlock(world, i, j, k)).isWallable) {
                        return j;
                     }

                     j--;
                  }
               }
            }
         } else {
            for (int jx = 0; jx <= 127; jx++) {
               if (GenAccess.isAirBlock(world, i, jx, k)) {
                  return jx;
               }
            }
         }

         return -1;
      } else {
         int minecraftHeight = GenAccess.heightValue(world, i, k);
         if (minecraftHeight < jinit) {
            jinit = minecraftHeight;
         }

         for (int jxx = jinit; jxx >= 0; jxx--) {
            Block blockId = GenAccess.getBlock(world, i, jxx, k);
            if (!BlockProperties.get(blockId).isWallable && (wallIsSurface || !BlockProperties.get(blockId).isArtificial)) {
               return jxx;
            }

            if (waterSurfaceBuffer != -1 && BlockProperties.get(blockId).isWater) {
               return BlockProperties.get(GenAccess.getBlock(world, i, jxx - waterSurfaceBuffer, k)).isWater ? -666 : jxx;
            }
         }

         return -1;
      }
   }

   public static int flipDir(int dir) {
      return (dir + 2) % 4;
   }

   public static String metaValueCheck(Block blockID, int metadata) {
      if (metadata >= 0 && metadata < 16) {
         String fail = blockID.getTranslationKey() + " meta value should be between";
         if (BlockProperties.get(blockID).isStair) {
            return metadata < 8 ? null : fail + " 0 and 7";
         } else if (blockID == Blocks.RAIL) {
            return metadata < 10 ? null : fail + " 0 and 9";
         } else if (blockID != Blocks.STONE_BUTTON && blockID != Blocks.WOODEN_BUTTON) {
            if (blockID == Blocks.LADDER
               || blockID == Blocks.DISPENSER
               || blockID == Blocks.FURNACE
               || blockID == Blocks.LIT_FURNACE
               || blockID == Blocks.WALL_SIGN
               || blockID == Blocks.PISTON
               || blockID == Blocks.STICKY_PISTON
               || blockID == Blocks.CHEST
               || blockID == Blocks.HOPPER
               || blockID == Blocks.DROPPER
               || blockID == Blocks.GOLDEN_RAIL
               || blockID == Blocks.DETECTOR_RAIL
               || blockID == Blocks.ACTIVATOR_RAIL) {
               return metadata % 8 < 6 ? null : fail + " 0 and 5 or 8 and 13";
            } else if (blockID != Blocks.PUMPKIN && blockID != Blocks.LIT_PUMPKIN) {
               if (blockID == Blocks.OAK_FENCE_GATE) {
                  return metadata < 8 ? null : fail + " 0 and 7";
               } else if (blockID == Blocks.WOODEN_SLAB || blockID == Blocks.BED) {
                  return metadata % 8 < 4 ? null : fail + " 0 and 3 or 8 and 11";
               } else if (blockID != Blocks.TORCH && blockID != Blocks.REDSTONE_TORCH && blockID != Blocks.UNLIT_REDSTONE_TORCH) {
                  return null;
               } else {
                  return metadata > 0 && metadata < 7 ? null : fail + " 1 and 6";
               }
            } else {
               return metadata < 5 ? null : fail + " 0 and 4";
            }
         } else {
            return metadata % 8 > 0 && metadata % 8 < 5 ? null : fail + " 1 and 4 or 9 and 12";
         }
      } else {
         return "All Minecraft meta values should be between 0 and 15";
      }
   }

   public static int pickWeightedOption(Random random, List<Object> weights, List<Object> options) {
      int[] w = new int[weights.size()];
      int[] o = new int[options.size()];

      for (int i = 0; i < w.length; i++) {
         w[i] = (Integer)weights.get(i);
      }

      for (int i = 0; i < o.length; i++) {
         o[i] = (Integer)options.get(i);
      }

      return pickWeightedOption(random, w, o);
   }

   public static int pickWeightedOption(Random random, int[] weights, int[] options) {
      int sum = 0;

      for (int n = 0; n < weights.length; n++) {
         sum += weights[n];
      }

      if (sum <= 0) {
         System.err.println("Error selecting options, weightsum not positive!");
         return options[0];
      } else {
         int s = random.nextInt(sum);
         sum = 0;

         for (int var7 = 0; var7 < weights.length; var7++) {
            sum += weights[var7];
            if (sum > s) {
               return options[var7];
            }
         }

         return options[options.length - 1];
      }
   }

   public static int rotDir(int dir, int rotation) {
      return (dir + rotation + 4) % 4;
   }

   public static void setBlockAndMetaNoLighting(World world, int i, int j, int k, Block blockId, int meta) {
      if (i >= -30000000 && k >= -30000000 && i < 30000000 && k < 30000000 && j >= 0 && j <= 255) {
         GenAccess.setBlock(world, i, j, k, blockId, meta, 2);
      }
   }

   protected static void fillDown(int[] lowPt, int jtop, World world) {
      while (BlockProperties.get(GenAccess.getBlock(world, lowPt[0], lowPt[1], lowPt[2])).isArtificial) {
         lowPt[1]--;
      }

      Block oldSurfaceBlockId = GenAccess.getBlock(world, lowPt[0], lowPt[1], lowPt[2]);
      if (BlockProperties.get(oldSurfaceBlockId).isOre) {
         oldSurfaceBlockId = Blocks.STONE;
      }

      if (oldSurfaceBlockId == Blocks.DIRT || lowPt[1] <= 63 && oldSurfaceBlockId == Blocks.SAND) {
         oldSurfaceBlockId = Blocks.GRASS;
      }

      if (oldSurfaceBlockId == Blocks.AIR) {
         oldSurfaceBlockId = (Block)(Compat.waterVaporizes(world) ? Blocks.NETHERRACK : Blocks.GRASS);
      }

      for (Block fillBlockId = oldSurfaceBlockId == Blocks.GRASS ? Blocks.DIRT : oldSurfaceBlockId; lowPt[1] <= jtop; lowPt[1]++) {
         setBlockAndMetaNoLighting(world, lowPt[0], lowPt[1], lowPt[2], lowPt[1] == jtop ? oldSurfaceBlockId : fillBlockId, 0);
      }
   }

   protected static int minOrMax(int[] a, boolean isMin) {
      if (isMin) {
         int min = Integer.MAX_VALUE;

         for (int i : a) {
            min = Math.min(min, i);
         }

         return min;
      } else {
         int max = Integer.MIN_VALUE;

         for (int i : a) {
            max = Math.max(max, i);
         }

         return max;
      }
   }

   protected static int signum(int n, int wiggle) {
      if (n <= wiggle && -n <= wiggle) {
         return 0;
      } else {
         return n < 0 ? -1 : 1;
      }
   }

   private static void circleShape(int diam) {
      float rad = diam / 2.0F;
      float[][] shape_density = new float[diam][diam];

      for (int x = 0; x < diam; x++) {
         for (int y = 0; y < diam; y++) {
            shape_density[y][x] = ((x + 0.5F - rad) * (x + 0.5F - rad) + (y + 0.5F - rad) * (y + 0.5F - rad)) / (rad * rad);
         }
      }

      int[] xheight = new int[diam];

      for (int y = 0; y < diam; y++) {
         int x = 0;

         while (shape_density[y][x] > 1.0F) {
            x++;
         }

         xheight[y] = x;
      }

      CIRCLE_SHAPE[diam] = new int[diam][diam];
      CIRCLE_CRENEL[diam] = new int[diam][diam];
      SPHERE_SHAPE[diam] = new int[(diam + 1) / 2];
      int crenel_adj = 0;

      for (int x = 0; x < diam; x++) {
         for (int y = 0; y < diam; y++) {
            CIRCLE_SHAPE[diam][y][x] = 0;
            CIRCLE_CRENEL[diam][y][x] = 0;
         }
      }

      for (int y = 0; y < diam; y++) {
         int nextHeight;
         if (y != 0 && y != diam - 1) {
            nextHeight = xheight[y < diam / 2 ? y - 1 : y + 1] + (xheight[y] == xheight[y < diam / 2 ? y - 1 : y + 1] ? 1 : 0);
         } else {
            nextHeight = diam / 2 + 1;
         }

         if (y > 0 && xheight[y] == xheight[y - 1]) {
            crenel_adj++;
         }

         int x;
         for (x = 0; x < xheight[y]; x++) {
            CIRCLE_SHAPE[diam][y][x] = -1;
            CIRCLE_SHAPE[diam][y][diam - x - 1] = -1;
            CIRCLE_CRENEL[diam][y][x] = -1;
            CIRCLE_CRENEL[diam][y][diam - x - 1] = -1;
         }

         while (x < nextHeight) {
            CIRCLE_SHAPE[diam][y][x] = 1;
            CIRCLE_SHAPE[diam][y][diam - x - 1] = 1;
            CIRCLE_CRENEL[diam][y][x] = (x + crenel_adj) % 2;
            CIRCLE_CRENEL[diam][y][diam - x - 1] = (x + crenel_adj + diam + 1) % 2;
            x++;
         }
      }

      for (int y = diam / 2; y < diam; y++) {
         SPHERE_SHAPE[diam][y - diam / 2] = 2 * (diam / 2 - xheight[y]) + (diam % 2 == 0 ? 0 : 1);
      }
   }

   private static boolean isSolidBlock(Block blockID) {
      return blockID.getDefaultState().getMaterial().isSolid();
   }

   static {
      for (int diam = 1; diam <= 40; diam++) {
         circleShape(diam);
      }

      CIRCLE_SHAPE[6] = new int[][]{
         {-1, -1, 1, 1, -1, -1}, {-1, 1, 0, 0, 1, -1}, {1, 0, 0, 0, 0, 1}, {1, 0, 0, 0, 0, 1}, {-1, 1, 0, 0, 1, -1}, {-1, -1, 1, 1, -1, -1}
      };
      CIRCLE_CRENEL[6] = new int[][]{
         {-1, -1, 1, 0, -1, -1}, {-1, 0, 0, 0, 1, -1}, {1, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 1}, {-1, 1, 0, 0, 0, -1}, {-1, -1, 0, 1, -1, -1}
      };
      randLightingHash = new boolean[512];
      Random rand = new Random();

      for (int m = 0; m < randLightingHash.length; m++) {
         randLightingHash[m] = rand.nextInt(10) == 0;
      }

      CHEST_TYPE_LABELS = new String[]{"EASY", "MEDIUM", "HARD", "TOWER"};
      DEFAULT_CHEST_TRIES = new int[]{4, 6, 6, 6};
      DEFAULT_CHEST_ITEMS = new Object[][][]{
         {
               {0, Items.ARROW, 0, 2, 1, 12},
               {1, Items.IRON_SWORD, 0, 2, 1, 1},
               {2, Items.LEATHER_LEGGINGS, 0, 1, 1, 1},
               {3, Items.IRON_SHOVEL, 0, 1, 1, 1},
               {4, Items.STRING, 0, 1, 1, 1},
               {5, Items.IRON_PICKAXE, 0, 2, 1, 1},
               {6, Items.LEATHER_BOOTS, 0, 1, 1, 1},
               {7, Items.BUCKET, 0, 1, 1, 1},
               {8, Items.LEATHER_HELMET, 0, 1, 1, 1},
               {9, Items.WHEAT_SEEDS, 0, 1, 10, 15},
               {10, Items.GOLD_NUGGET, 0, 2, 3, 8},
               {11, Items.POTIONITEM, 5, 2, 1, 1},
               {12, Items.POTIONITEM, 4, 1, 1, 1}
         },
         {
               {0, Items.GOLDEN_SWORD, 0, 2, 1, 1},
               {1, Items.MILK_BUCKET, 0, 2, 1, 1},
               {2, Blocks.WEB, 0, 1, 8, 16},
               {3, Items.GOLDEN_SHOVEL, 0, 1, 1, 1},
               {4, Items.GOLDEN_HOE, 0, 1, 0, 1},
               {5, Items.CLOCK, 0, 1, 1, 1},
               {6, Items.IRON_AXE, 0, 3, 1, 1},
               {7, Items.MAP, 0, 1, 1, 1},
               {8, Items.APPLE, 0, 2, 2, 3},
               {9, Items.COMPASS, 0, 1, 1, 1},
               {10, Items.IRON_INGOT, 0, 1, 5, 8},
               {11, Items.SLIME_BALL, 0, 1, 1, 3},
               {12, Blocks.OBSIDIAN, 0, 1, 1, 4},
               {13, Items.BREAD, 0, 2, 8, 15},
               {14, Items.POTIONITEM, 2, 1, 1, 1},
               {15, Items.POTIONITEM, 37, 3, 1, 1},
               {16, Items.POTIONITEM, 34, 1, 1, 1},
               {17, Items.POTIONITEM, 9, 1, 1, 1}
         },
         {
               {0, Blocks.STICKY_PISTON, 0, 2, 6, 12},
               {1, Blocks.WEB, 0, 1, 8, 24},
               {2, Items.COOKIE, 0, 2, 8, 18},
               {3, Items.DIAMOND_AXE, 0, 1, 1, 1},
               {4, Items.MINECART, 0, 1, 12, 24},
               {5, Items.REDSTONE, 0, 2, 12, 24},
               {6, Items.LAVA_BUCKET, 0, 2, 1, 1},
               {7, Items.ENDER_PEARL, 0, 1, 1, 1},
               {8, Blocks.MOB_SPAWNER, 0, 1, 2, 4},
               {9, Items.RECORD_13, 0, 1, 1, 1},
               {10, Items.GOLDEN_APPLE, 0, 1, 4, 8},
               {11, Blocks.TNT, 0, 2, 8, 20},
               {12, Items.DIAMOND, 0, 2, 1, 4},
               {13, Items.GOLD_INGOT, 0, 2, 30, 64},
               {14, Items.POTIONITEM, 37, 3, 1, 1},
               {15, Items.POTIONITEM, 49, 2, 1, 1},
               {16, Items.POTIONITEM, 3, 2, 1, 1}
         },
         {
               {0, Items.ARROW, 0, 1, 1, 12},
               {1, Items.FISH, 0, 2, 1, 1},
               {2, Items.GOLDEN_HELMET, 0, 1, 1, 1},
               {3, Blocks.WEB, 0, 1, 1, 12},
               {4, Items.IRON_INGOT, 0, 1, 2, 3},
               {5, Items.STONE_SWORD, 0, 1, 1, 1},
               {6, Items.IRON_AXE, 0, 1, 1, 1},
               {7, Items.EGG, 0, 2, 8, 16},
               {8, Items.SADDLE, 0, 1, 1, 1},
               {9, Items.WHEAT, 0, 2, 3, 6},
               {10, Items.GUNPOWDER, 0, 1, 2, 4},
               {11, Items.LEATHER_CHESTPLATE, 0, 1, 1, 1},
               {12, Blocks.PUMPKIN, 0, 1, 1, 5},
               {13, Items.GOLD_NUGGET, 0, 2, 1, 3}
         }
      };
   }
}
