package com.electrifiedded.walledcitymod;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

public class BuildingTower extends Building {
   public static final int FLOOR_HAUNTED_CHANCE = 50;
   public static final int HAUNTED_CHEST_CHANCE = 60;
   public static final int TOWER_UNIV_MIN_WIDTH = 5;
   public static final int TOWER_LEVELING = 12;
   public static final String[] ROOFSTYLE_NAMES = new String[]{"Crenel", "Steep", "Steep Trim", "Shallow", "Dome", "Cone", "Two Sided"};
   public static final int[] ROOF_STYLE_IDS = new int[ROOFSTYLE_NAMES.length];
   public static final int SURFACE_PORTAL_ODDS = 20;
   public static final int NETHER_PORTAL_ODDS = 10;
   public static final int BOOKSHELF_ODDS = 3;
   public static final int BED_ODDS = 5;
   public static final int CAULDRON_ODDS = 8;
   public static final int BREWING_STAND_ODDS = 8;
   public static final int ENCHANTMENT_TABLE_ODDS = 12;
   public static final int ROOF_CRENEL = 0;
   public static final int ROOF_STEEP = 1;
   public static final int ROOF_TRIM = 2;
   public static final int ROOF_SHALLOW = 3;
   public static final int ROOF_DOME = 4;
   public static final int ROOF_CONE = 5;
   public static final int ROOF_TWO_SIDED = 6;
   public static final int NORTH_FACE_DOOR_META = 3;
   public static final int EAST_FACE_DOOR_META = 0;
   public static final int SOUTH_FACE_DOOR_META = 1;
   public static final int WEST_FACE_DOOR_META = 2;
   public int baseHeight;
   public int roofStyle;
   public int minHorizDim;
   public final boolean PopulateFurniture;
   public final boolean MakeDoors;
   public final boolean circular;
   private BlockAndMeta[][][] buffer;
   private int[][] circle_shape;
   private TemplateRule roofRule;
   private TemplateRule SpawnerRule;
   private TemplateRule ChestRule;

   public void build(int doorOffset1, int doorOffset2, boolean hanging) {
      boolean undeadTower = false;
      boolean ghastTower = false;
      if (this.SpawnerRule != TemplateRule.RULE_NOT_PROVIDED) {
         if (this.SpawnerRule.hasUndeadSpawner()) {
            undeadTower = true;
         }

         ghastTower = this.roofStyle == 0 && this.SpawnerRule.getBlockOrHole(this.world.rand).equals(GHAST_SPAWNER);
         if (ghastTower || this.random.nextInt(100) > this.SpawnerRule.chance) {
            undeadTower = false;
         }
      }

      if (undeadTower && this.bHeight - this.baseHeight < 9) {
         this.bHeight = this.baseHeight + 9;
      }

      if (this.baseHeight < 0) {
         this.baseHeight = 0;
      }

      this.buffer = new BlockAndMeta[this.bWidth + 2][this.bHeight + this.minHorizDim + 3][this.bLength + 2];

      for (int x1 = 0; x1 < this.bWidth + 2; x1++) {
         for (int y1 = 0; y1 < this.bLength + 2; y1++) {
            for (int z1 = 0; z1 < this.bHeight + this.minHorizDim + 3; z1++) {
               this.buffer[x1][z1][y1] = PRESERVE_BLOCK;
            }
         }
      }

      for (int x1 = 0; x1 < this.bWidth; x1++) {
         for (int y1 = 0; y1 < this.bLength; y1++) {
            if (!this.circular || this.circle_shape[x1][y1] >= 0) {
               for (int z1 = this.baseHeight - 1; z1 < this.bHeight; z1++) {
                  if ((!this.circular || this.circle_shape[x1][y1] != 1)
                     && (this.circular || x1 != 0 && x1 != this.bWidth - 1 && y1 != 0 && y1 != this.bLength - 1)) {
                     this.buffer[x1 + 1][z1 + 1][y1 + 1] = HOLE_BLOCK_NO_LIGHTING;
                  } else {
                     this.buffer[x1 + 1][z1 + 1][y1 + 1] = this.bRule.getBlockOrHole(this.world.rand);
                  }
               }

               for (int z1x = -1; z1x < this.baseHeight - 1; z1x++) {
                  this.buffer[x1 + 1][z1x + 1][y1 + 1] = this.bRule.getBlockOrHole(this.world.rand);
               }

               this.buildDown(x1, -2, y1, this.bRule, 12, 2, (this.bLength - y1 - 1) / 2);
            }
         }
      }

      int sideWindowY = this.bLength / 2;

      for (int z1x = this.baseHeight - 1; z1x < this.bHeight - 3; z1x += 4) {
         int winDoorX1 = Math.min(Math.max(this.bWidth / 2 + (z1x == this.baseHeight - 1 ? doorOffset1 : 0), 1), this.bWidth - 2);
         int winDoorX2 = Math.min(Math.max(this.bWidth / 2 + (z1x == this.baseHeight - 1 ? doorOffset2 : 0), 1), this.bWidth - 2);
         int torchX1 = winDoorX1 + (winDoorX1 == this.bWidth - 2 ? -1 : 1);
         int torchX2 = winDoorX2 + (winDoorX2 == this.bWidth - 2 ? -1 : 1);
         boolean floorHasUndeadSpawner = undeadTower && z1x > this.baseHeight - 1 && this.random.nextInt(100) < 50;
         if (z1x == this.baseHeight - 1 || !floorHasUndeadSpawner) {
            int winH = z1x > this.baseHeight - 1 ? 2 : 3;
            this.buildWindowOrDoor(0, z1x + 2, sideWindowY, -1, 0, winH);
            this.buildWindowOrDoor(this.bWidth - 1, z1x + 2, sideWindowY, 1, 0, winH);
            this.buildWindowOrDoor(winDoorX1, z1x + 2, 0, 0, -1, winH);
            this.buildWindowOrDoor(winDoorX2, z1x + 2, this.bLength - 1, 0, 1, winH);
         }

         for (int y1x = 1; y1x < this.bLength - 1; y1x++) {
            for (int x1 = 1; x1 < this.bWidth - 1; x1++) {
               if (!this.circular || this.circle_shape[x1][y1x] == 0) {
                  this.buffer[x1 + 1][z1x + 1][y1x + 1] = this.bRule.primaryBlock.get() == Blocks.LOG
                     ? new BlockAndMeta(Blocks.PLANKS, 0)
                     : this.bRule.getBlockOrHole(this.world.rand);
               }
            }
         }

         if (!undeadTower && this.bRule.chance == 100) {
            this.buffer[torchX1 + 1][z1x + 3 + 1][1 + (this.circular && this.bLength == 6 ? 1 : 0) + 1] = NORTH_FACE_TORCH_BLOCK;
            this.buffer[torchX2 + 1][z1x + 3 + 1][this.bLength - 2 - (this.circular && this.bLength == 6 ? 1 : 0) + 1] = SOUTH_FACE_TORCH_BLOCK;
         }

         if (floorHasUndeadSpawner && z1x == this.baseHeight + 4 && this.bRule.chance == 100) {
            for (int x1x = this.bWidth / 2 - 1; x1x <= this.bWidth / 2 + 1; x1x++) {
               for (int y1x = 1; y1x < Math.min(3, sideWindowY - 1); y1x++) {
                  if (!this.circular || this.circle_shape[x1x][y1x] == 0) {
                     this.buffer[x1x + 1][z1x + 1][y1x + 1] = HOLE_BLOCK_LIGHTING;
                  }
               }

               for (int y1xx = this.bLength - Math.min(3, sideWindowY - 1); y1xx < this.bLength - 1; y1xx++) {
                  if (!this.circular || this.circle_shape[x1x][y1xx] == 0) {
                     this.buffer[x1x + 1][z1x + 1][y1xx + 1] = HOLE_BLOCK_LIGHTING;
                  }
               }
            }
         } else if (this.SpawnerRule != TemplateRule.RULE_NOT_PROVIDED && this.random.nextInt(100) < this.SpawnerRule.chance && !ghastTower) {
            BlockAndMeta spawnerBlock = this.SpawnerRule.getNonAirBlock(this.world.rand);
            if (!spawnerBlock.equals(GHAST_SPAWNER)) {
               this.buffer[this.bWidth / 2 + 1][z1x + 1 + 1][sideWindowY + 1] = spawnerBlock;
            }
         }

         if (this.ChestRule != TemplateRule.RULE_NOT_PROVIDED && this.random.nextInt(100) < this.ChestRule.chance) {
            this.buffer[this.bWidth - 2 + 1][z1x + 1 + 1][sideWindowY - 1 + 1] = this.ChestRule.getNonAirBlock(this.world.rand);
         } else if (floorHasUndeadSpawner && this.random.nextInt(100) < 60) {
            this.buffer[this.bWidth - 2 + 1][z1x + 1 + 1][sideWindowY - 1 + 1] = z1x < 15 ? TOWER_CHEST_BLOCK : HARD_CHEST_BLOCK;
         }

         if (z1x == this.baseHeight - 1) {
            z1x++;
         }
      }

      int topFloorHeight = (this.bHeight - this.baseHeight - 4) / 4 * 4 + this.baseHeight + 1;
      int ladderHeight = this.roofStyle == 0 ? this.bHeight : (this.bHeight - this.baseHeight < 8 ? 0 : topFloorHeight);

      for (int z1x = this.baseHeight; z1x < ladderHeight; z1x++) {
         this.buffer[2][z1x + 1][sideWindowY - 1 + 1] = EAST_FACE_LADDER_BLOCK;
      }

      this.buildRoof();
      if (undeadTower && this.roofStyle == 0) {
         this.buffer[2][this.bHeight > this.baseHeight + 12 ? this.baseHeight + 9 : this.bHeight + 1][sideWindowY - 1 + 1] = this.bRule
            .getBlockOrHole(this.world.rand);
      }

      int zLim = this.bRule.chance >= 100 ? this.buffer[0].length : this.propagateCollapse(this.bRule.chance);

      for (int x1x = 1; x1x < this.buffer.length - 1; x1x++) {
         for (int y1xxx = 1; y1xxx < this.buffer[0][0].length - 1; y1xxx++) {
            if (!this.circular || this.circle_shape[x1x - 1][y1xxx - 1] >= 0) {
               for (int z1x = 0; z1x < Math.min(this.bHeight, zLim); z1x++) {
                  this.setBlockLocal(x1x - 1, z1x - 1, y1xxx - 1, this.buffer[x1x][z1x][y1xxx]);
               }
            }
         }
      }

      for (int x1x = 0; x1x < this.buffer.length; x1x++) {
         for (int y1xxxx = 0; y1xxxx < this.buffer[0][0].length; y1xxxx++) {
            for (int z1x = this.bHeight; z1x < zLim; z1x++) {
               this.setBlockLocal(x1x - 1, z1x - 1, y1xxxx - 1, this.buffer[x1x][z1x][y1xxxx]);
            }
         }
      }

      for (int x1x = 1; x1x < this.bWidth - 1; x1x++) {
         if (this.isStairBlock(x1x, this.baseHeight, -1) && this.getBlockIdLocal(x1x, this.baseHeight, -2) == this.bRule.primaryBlock.get()) {
            this.setBlockLocal(x1x, this.baseHeight, -1, this.bRule.primaryBlock);
         }

         if (this.isStairBlock(x1x, this.baseHeight, this.bLength)
            && this.getBlockIdLocal(x1x, this.baseHeight, this.bLength + 1) == this.bRule.primaryBlock.get()) {
            this.setBlockLocal(x1x, this.baseHeight, this.bLength, this.bRule.primaryBlock);
         }
      }

      if (this.PopulateFurniture) {
         for (int z1x = this.baseHeight; z1x < this.bHeight - 2; z1x += 4) {
            for (int m = 0; m < this.bLength * this.bWidth / 25; m++) {
               if (!undeadTower && this.random.nextInt(5) == 0) {
                  this.populateBeds(z1x);
               }

               if (this.bHeight - this.baseHeight > 8 && this.random.nextInt(3) == 0) {
                  this.populateBookshelves(z1x);
               }

               if (this.random.nextInt(8) == 0) {
                  this.populateFurnitureColumn(z1x, new BlockAndMeta[]{new BlockAndMeta(Blocks.CAULDRON, this.random.nextInt(4))});
               }

               if (z1x > 12 && this.random.nextInt(8) == 0) {
                  this.populateFurnitureColumn(
                     z1x, new BlockAndMeta[]{this.bRule.primaryBlock, new BlockAndMeta(Blocks.BREWING_STAND, this.random.nextInt(2) + 1)}
                  );
               }

               if (z1x > 20 && this.random.nextInt(12) == 0) {
                  this.populateFurnitureColumn(z1x, new BlockAndMeta[]{new BlockAndMeta(Blocks.ENCHANTING_TABLE, 0)});
               }
            }

            if (z1x == this.baseHeight) {
               z1x++;
            }
         }
      }

      if (ghastTower) {
         this.populateGhastSpawner(this.bHeight + 1);
      } else if (this.roofStyle == 0 && this.bHeight > 22) {
         this.populatePortal(this.bHeight + 1);
      }

      this.flushDelayed();
   }

   public void buildXYRotated(int p, int q, int r, BlockAndMeta block, boolean rotated) {
      if (rotated) {
         this.buffer[r][q][p] = block;
      } else {
         this.buffer[p][q][r] = block;
      }
   }

   public boolean isObstructedRoof(int ybuffer) {
      int rBuffer = this.roofStyle == 0 ? 1 : (this.roofStyle != 4 && this.roofStyle != 5 ? -1 : 0);
      int rHeight = this.roofStyle == 0 ? 2 : this.minHorizDim / 2;
      return this.isObstructedSolid(
         new int[]{rBuffer, this.bHeight, Math.max(rBuffer, ybuffer)}, new int[]{this.bWidth - 1 - rBuffer, this.bHeight + rHeight, this.bLength - 1 - rBuffer}
      );
   }

   public int propagateCollapse(int buildChance) {
      int xLim = this.buffer.length;
      int zLim = this.buffer[0].length;
      int yLim = this.buffer[0][0].length;
      int[][][] support = new int[xLim][zLim][yLim];

      for (int x = 0; x < xLim; x++) {
         for (int z = 1; z < zLim; z++) {
            for (int y = 0; y < yLim; y++) {
               support[x][z][y] = 0;
            }
         }
      }

      for (int x = 0; x < xLim; x++) {
         for (int y = 0; y < yLim; y++) {
            if (this.buffer[x][0][y].get() != Blocks.AIR) {
               support[x][0][y] = 2;
            }
         }
      }

      for (int z = 1; z < zLim; z++) {
         boolean levelCollapsed = true;

         for (int x = 0; x < xLim; x++) {
            for (int yx = 0; yx < yLim; yx++) {
               if (this.buffer[x][z][yx].get() != Blocks.AIR
                  && BlockProperties.get(this.buffer[x][z - 1][yx].get()).isLoaded
                  && support[x][z - 1][yx] > 0) {
                  support[x][z][yx] = 2;
                  levelCollapsed = false;
               }
            }
         }

         if (levelCollapsed) {
            return z;
         }

         for (int m = 0; m < 4; m++) {
            for (int x = 0; x < xLim; x++) {
               for (int yxx = 0; yxx < yLim; yxx++) {
                  if (this.buffer[x][z][yxx].get() != Blocks.AIR && support[x][z][yxx] == 0) {
                     int neighbors = 0;
                     if (x < xLim - 1 && BlockProperties.get(this.buffer[x + 1][z][yxx].get()).isLoaded) {
                        neighbors += support[x + 1][z][yxx];
                     }

                     if (x > 0 && BlockProperties.get(this.buffer[x - 1][z][yxx].get()).isLoaded) {
                        neighbors += support[x - 1][z][yxx];
                     }

                     if (yxx < yLim - 1 && BlockProperties.get(this.buffer[x][z][yxx + 1].get()).isLoaded) {
                        neighbors += support[x][z][yxx + 1];
                     }

                     if (yxx > 0 && BlockProperties.get(this.buffer[x][z][yxx - 1].get()).isLoaded) {
                        neighbors += support[x][z][yxx - 1];
                     }

                     if (neighbors > this.random.nextInt(4)) {
                        support[x][z][yxx] = 1;
                     }
                  }
               }
            }
         }

         for (int x = 0; x < xLim; x++) {
            for (int yxxx = 0; yxxx < yLim; yxxx++) {
               if (support[x][z][yxxx] == 0 && !this.buffer[x][z][yxxx].equals(Building.PRESERVE_BLOCK)) {
                  this.buffer[x][z][yxxx] = HOLE_BLOCK_LIGHTING;
               }
            }
         }
      }

      return zLim;
   }

   public boolean queryCanBuild(int ybuffer, boolean overlapTowers) {
      int rooftopJ = this.j0 + this.bHeight + (this.roofStyle == 5 ? this.minHorizDim : this.minHorizDim / 2) + 2;
      if (rooftopJ > 255) {
         this.bHeight -= rooftopJ - 255;
      }

      if (this.bHeight < this.baseHeight + 4) {
         return false;
      } else if (this.isObstructedRoof(ybuffer)) {
         return false;
      } else {
         if (this.wgt.isLayoutGenerator()) {
            int[] pt1 = this.getIJKPt(overlapTowers ? this.bWidth / 4 : 0, 0, overlapTowers ? this.bLength / 4 : ybuffer);
            int[] pt2 = this.getIJKPt(overlapTowers ? 3 * this.bWidth / 4 - 1 : this.bWidth - 1, 0, overlapTowers ? 3 * this.bLength / 4 - 1 : this.bLength - 1);
            if (!this.wgt.layoutIsClear(pt1, pt2, 4)) {
               return false;
            }

            this.wgt.setLayoutCode(pt1, pt2, 4);
         } else if (!overlapTowers && this.isObstructedFrame(3, ybuffer)) {
            return false;
         }

         return true;
      }
   }

   private void buildRoof() {
      if (this.roofRule == TemplateRule.RULE_NOT_PROVIDED) {
         this.roofRule = this.roofStyle != 1 && this.roofStyle != 3 && this.roofStyle != 2 && this.roofStyle != 6
            ? this.bRule
            : new TemplateRule(Blocks.PLANKS, 0, "");
      }

      int stepMeta = this.roofRule.primaryBlock.toStep().getMeta();
      TemplateRule stepRule = new TemplateRule(Blocks.STONE_SLAB, stepMeta, this.roofRule.chance);
      TemplateRule doubleStepRule = stepMeta == 2
         ? new TemplateRule(Blocks.PLANKS, 0, this.roofRule.chance)
         : new TemplateRule(Blocks.DOUBLE_STONE_SLAB, stepMeta, this.roofRule.chance);
      TemplateRule trimRule = this.roofStyle == 2
         ? new TemplateRule(this.bRule.primaryBlock.get() == Blocks.COBBLESTONE ? Blocks.LOG : Blocks.COBBLESTONE, 0, this.roofRule.chance)
         : stepRule;
      Block roof = this.roofRule.primaryBlock.toStair();
      TemplateRule northStairsRule = new TemplateRule(roof, STAIRS_DIR_TO_META[0], this.roofRule.chance);
      TemplateRule southStairsRule = new TemplateRule(roof, STAIRS_DIR_TO_META[2], this.roofRule.chance);
      TemplateRule eastStairsRule = new TemplateRule(roof, STAIRS_DIR_TO_META[1], this.roofRule.chance);
      TemplateRule westStairsRule = new TemplateRule(roof, STAIRS_DIR_TO_META[3], this.roofRule.chance);
      if (this.roofStyle == 0) {
         if (this.circular) {
            for (int y1 = 0; y1 < this.bLength; y1++) {
               for (int x1 = 0; x1 < this.bWidth; x1++) {
                  if (this.circle_shape[x1][y1] >= 0) {
                     this.buffer[x1 + 1][this.bHeight + 1][y1 + 1] = this.bRule.getBlockOrHole(this.world.rand);
                  }

                  if (CIRCLE_CRENEL[this.minHorizDim][x1][y1] == 1) {
                     this.buffer[x1 + 1][this.bHeight + 1 + 1][y1 + 1] = this.bRule.getBlockOrHole(this.world.rand);
                  }
               }
            }
         } else {
            for (int y1 = 0; y1 < this.bLength; y1++) {
               for (int x1 = 0; x1 < this.bWidth; x1++) {
                  this.buffer[x1 + 1][this.bHeight + 1][y1 + 1] = this.bRule.getBlockOrHole(this.world.rand);
               }
            }

            for (int m = 0; m < this.bWidth; m += 2) {
               if (this.getBlockIdLocal(m, this.bHeight, -1) != this.bRule.primaryBlock.get()
                  && this.getBlockIdLocal(m, this.bHeight - 1, -1) != this.bRule.primaryBlock.get()) {
                  this.buffer[m + 1][this.bHeight + 1 + 1][1] = (m + 1) % 2 == 0 ? HOLE_BLOCK_LIGHTING : this.bRule.getBlockOrHole(this.world.rand);
               }

               if (this.getBlockIdLocal(m, this.bHeight, this.bLength) != this.bRule.primaryBlock.get()
                  && this.getBlockIdLocal(m, this.bHeight - 1, this.bLength) != this.bRule.primaryBlock.get()) {
                  this.buffer[m + 1][this.bHeight + 1 + 1][this.bLength - 1 + 1] = (m + 1) % 2 == 0
                     ? HOLE_BLOCK_LIGHTING
                     : this.bRule.getBlockOrHole(this.world.rand);
               }
            }

            for (int m = 0; m < this.bLength; m += 2) {
               if (this.getBlockIdLocal(-1, this.bHeight, m) != this.bRule.primaryBlock.get()
                  && this.getBlockIdLocal(-1, this.bHeight - 1, m) != this.bRule.primaryBlock.get()) {
                  this.buffer[1][this.bHeight + 1 + 1][m + 1] = (m + 1) % 2 == 0 ? HOLE_BLOCK_LIGHTING : this.bRule.getBlockOrHole(this.world.rand);
               }

               if (this.getBlockIdLocal(this.bWidth, this.bHeight, m) != this.bRule.primaryBlock.get()
                  && this.getBlockIdLocal(this.bWidth, this.bHeight - 1, m) != this.bRule.primaryBlock.get()) {
                  this.buffer[this.bWidth - 1 + 1][this.bHeight + 1 + 1][m + 1] = (m + 1) % 2 == 0
                     ? HOLE_BLOCK_LIGHTING
                     : this.bRule.getBlockOrHole(this.world.rand);
               }
            }

            for (int y1 = 1; y1 < this.bLength - 1; y1++) {
               for (int x1 = 1; x1 < this.bWidth - 1; x1++) {
                  if (this.isWallBlock(x1, this.bHeight, y1)
                     && !this.isWallBlock(x1 + 1, this.bHeight, y1)
                     && !this.isWallBlock(x1 - 1, this.bHeight, y1)
                     && !this.isWallBlock(x1, this.bHeight, y1 + 1)
                     && !this.isWallBlock(x1, this.bHeight, y1 - 1)) {
                     this.buffer[x1 + 1][this.bHeight + 1 - 1 + 1][y1 + 1] = HOLE_BLOCK_LIGHTING;
                  }
               }
            }
         }

         this.buffer[2][this.bHeight + 1][this.bLength / 2] = EAST_FACE_LADDER_BLOCK;
         this.buffer[2][this.bHeight + 2][this.bLength / 2] = new BlockAndMeta(Blocks.TRAPDOOR, 3);
      } else if (this.roofStyle == 1 || this.roofStyle == 2 || this.roofStyle == 3 && (this.bWidth < 6 || this.bLength < 6)) {
         for (int m = 0; m < (this.minHorizDim + 1) / 2; m++) {
            for (int x1x = m; x1x < this.bWidth - m; x1x++) {
               for (int y1 = m; y1 < this.bLength - m; y1++) {
                  this.buffer[x1x + 1][this.bHeight + m + 1][y1 + 1] = HOLE_BLOCK_LIGHTING;
                  if (m == (this.bWidth + 1) / 2 - 1) {
                     this.buffer[x1x + 1][this.bHeight + m + 1 + 1][y1 + 1] = trimRule.getBlockOrHole(this.world.rand);
                  }
               }

               this.buffer[x1x + 1][this.bHeight + m + 1][m - 1 + 1] = northStairsRule.getBlockOrHole(this.world.rand);
               this.buffer[x1x + 1][this.bHeight + m + 1][this.bLength - m + 1] = southStairsRule.getBlockOrHole(this.world.rand);
               this.buffer[x1x + 1][this.bHeight + m + 1][m + 1] = doubleStepRule.getBlockOrHole(this.world.rand);
               this.buffer[x1x + 1][this.bHeight + m + 1][this.bLength - m - 1 + 1] = doubleStepRule.getBlockOrHole(this.world.rand);
            }

            for (int y1x = m; y1x < this.bLength - m; y1x++) {
               this.buffer[m - 1 + 1][this.bHeight + m + 1][y1x + 1] = eastStairsRule.getBlockOrHole(this.world.rand);
               this.buffer[this.bWidth - m + 1][this.bHeight + m + 1][y1x + 1] = westStairsRule.getBlockOrHole(this.world.rand);
               this.buffer[m + 1][this.bHeight + m + 1][y1x + 1] = doubleStepRule.getBlockOrHole(this.world.rand);
               this.buffer[this.bWidth - m - 1 + 1][this.bHeight + m + 1][y1x + 1] = doubleStepRule.getBlockOrHole(this.world.rand);
            }

            this.buffer[m - 1 + 1][this.bHeight + m + 1][m - 1 + 1] = trimRule.getBlockOrHole(this.world.rand);
            this.buffer[m - 1 + 1][this.bHeight + m + 1][this.bLength - m + 1] = trimRule.getBlockOrHole(this.world.rand);
            this.buffer[this.bWidth - m + 1][this.bHeight + m + 1][m - 1 + 1] = trimRule.getBlockOrHole(this.world.rand);
            this.buffer[this.bWidth - m + 1][this.bHeight + m + 1][this.bLength - m + 1] = trimRule.getBlockOrHole(this.world.rand);
         }
      } else if (this.roofStyle == 3) {
         for (int z12 = -1; z12 < (this.minHorizDim + 1) / 2; z12++) {
            int z1 = (z12 + 1) / 2;
            if ((z12 + 1) % 2 == 0) {
               for (int y1x = z12 + 1; y1x < this.bLength - 2 * z1; y1x++) {
                  this.buffer[z12 + 1][z1 + this.bHeight + 1][y1x + 1] = stepRule.getBlockOrHole(this.world.rand);
                  this.buffer[this.bWidth - z12 - 1 + 1][z1 + this.bHeight + 1][y1x + 1] = stepRule.getBlockOrHole(this.world.rand);
               }

               for (int x1x = z12 + 1; x1x < this.bWidth - 2 * z1; x1x++) {
                  this.buffer[x1x + 1][z1 + this.bHeight + 1][z12 + 1] = stepRule.getBlockOrHole(this.world.rand);
                  this.buffer[x1x + 1][z1 + this.bHeight + 1][this.bLength - z12 - 1 + 1] = stepRule.getBlockOrHole(this.world.rand);
               }

               this.buffer[z12 + 1][z1 + this.bHeight + 1][z12 + 1] = doubleStepRule.getBlockOrHole(this.world.rand);
               this.buffer[z12 + 1][z1 + this.bHeight + 1][this.bLength - z12 - 1 + 1] = doubleStepRule.getBlockOrHole(this.world.rand);
               this.buffer[this.bWidth - z12 - 1 + 1][z1 + this.bHeight + 1][z12 + 1] = doubleStepRule.getBlockOrHole(this.world.rand);
               this.buffer[this.bWidth - z12 - 1 + 1][z1 + this.bHeight + 1][this.bLength - z12 - 1 + 1] = doubleStepRule.getBlockOrHole(
                  this.world.rand
               );
            } else {
               for (int y1x = z12; y1x < this.bLength - z12; y1x++) {
                  for (int x1x = z12; x1x < this.bWidth - z12; x1x++) {
                     this.buffer[x1x + 1][z1 + this.bHeight + 1][y1x + 1] = doubleStepRule.getBlockOrHole(this.world.rand);
                  }
               }

               for (int y1x = z12 + 2; y1x < this.bLength - z12 - 2; y1x++) {
                  for (int x1x = z12 + 2; x1x < this.bWidth - z12 - 2; x1x++) {
                     this.buffer[x1x + 1][z1 + this.bHeight + 1][y1x + 1] = HOLE_BLOCK_LIGHTING;
                  }
               }

               this.buffer[z12 + 1][z1 + this.bHeight + 1 + 1][z12 + 1] = stepRule.getBlockOrHole(this.world.rand);
               this.buffer[z12 + 1][z1 + this.bHeight + 1 + 1][this.bLength - z12 - 1 + 1] = stepRule.getBlockOrHole(this.world.rand);
               this.buffer[this.bWidth - z12 - 1 + 1][z1 + this.bHeight + 1 + 1][z12 + 1] = stepRule.getBlockOrHole(this.world.rand);
               this.buffer[this.bWidth - z12 - 1 + 1][z1 + this.bHeight + 1 + 1][this.bLength - z12 - 1 + 1] = stepRule.getBlockOrHole(this.world.rand);
            }
         }
      } else if (this.roofStyle == 4) {
         for (int z1 = 0; z1 < (this.minHorizDim + 1) / 2; z1++) {
            int diam = SPHERE_SHAPE[this.minHorizDim][z1];

            for (int y1x = 0; y1x < diam; y1x++) {
               for (int x1x = 0; x1x < diam; x1x++) {
                  if (CIRCLE_SHAPE[diam][x1x][y1x] >= 0) {
                     this.buffer[x1x + (this.bWidth - diam) / 2 + 1][this.bHeight + z1 + 1 + 1][y1x + (this.bLength - diam) / 2 + 1] = CIRCLE_SHAPE[diam][x1x][y1x]
                              != 1
                           && z1 < (this.minHorizDim + 1) / 2 - 2
                        ? HOLE_BLOCK_LIGHTING
                        : this.roofRule.getBlockOrHole(this.world.rand);
                  }

                  if (z1 < (this.minHorizDim - 1) / 2) {
                     int nextDiam = SPHERE_SHAPE[this.minHorizDim][z1 + 1];
                     int x2 = x1x - (diam - nextDiam) / 2;
                     int y2 = y1x - (diam - nextDiam) / 2;
                     if (CIRCLE_SHAPE[diam][x1x][y1x] == 0 && (x2 < 0 || y2 < 0 || x2 >= nextDiam || y2 >= nextDiam || CIRCLE_SHAPE[nextDiam][x2][y2] != 0)) {
                        this.buffer[x1x + (this.bWidth - diam) / 2 + 1][this.bHeight + z1 + 1 + 1][y1x + (this.bLength - diam) / 2 + 1] = this.roofRule
                           .getBlockOrHole(this.world.rand);
                     }
                  }
               }
            }
         }
      } else if (this.roofStyle == 5) {
         int prevDiam = 0;

         for (int z1 = 0; z1 < this.minHorizDim + 1; z1++) {
            int diam = this.minHorizDim % 2 == 0 ? 2 * ((this.minHorizDim - z1 + 1) / 2) : 2 * ((this.minHorizDim - z1) / 2) + 1;

            for (int y1x = 0; y1x < diam; y1x++) {
               for (int x1x = 0; x1x < diam; x1x++) {
                  if (CIRCLE_SHAPE[diam][x1x][y1x] >= 0) {
                     this.buffer[x1x + (this.bWidth - diam) / 2 + 1][this.bHeight + z1 + 1 + 1][y1x + (this.bLength - diam) / 2 + 1] = CIRCLE_SHAPE[diam][x1x][y1x]
                           == 1
                        ? this.roofRule.getBlockOrHole(this.world.rand)
                        : HOLE_BLOCK_LIGHTING;
                  }

                  if (z1 > 0 && CIRCLE_SHAPE[diam][x1x][y1x] != 0 && CIRCLE_SHAPE[prevDiam][x1x + (prevDiam - diam) / 2][y1x + (prevDiam - diam) / 2] == 0) {
                     this.buffer[x1x + (this.bWidth - diam) / 2 + 1][this.bHeight + z1 + 1][y1x + (this.bLength - diam) / 2 + 1] = this.roofRule
                        .getBlockOrHole(this.world.rand);
                  }
               }
            }

            prevDiam = diam;
         }
      } else if (this.roofStyle == 6) {
         boolean rot = this.bLength > this.minHorizDim;
         int minAxLen = rot ? this.bWidth : this.bLength;
         int maxAxLen = rot ? this.bLength : this.bWidth;
         BlockAndMeta forwardsStairsRule = rot
            ? eastStairsRule.getBlockOrHole(this.world.rand)
            : northStairsRule.getBlockOrHole(this.world.rand);
         BlockAndMeta backwardsStirRule = rot
            ? westStairsRule.getBlockOrHole(this.world.rand)
            : southStairsRule.getBlockOrHole(this.world.rand);

         for (int m = 0; m <= this.minHorizDim / 2; m++) {
            for (int p = 0; p < maxAxLen; p++) {
               for (int r = m + 1; r < minAxLen - m - 1; r++) {
                  this.buildXYRotated(p + 1, this.bHeight + m + 1, r + 1, HOLE_BLOCK_LIGHTING, rot);
               }

               this.buildXYRotated(p + 1, this.bHeight + m + 1, m - 1 + 1, forwardsStairsRule, rot);
               this.buildXYRotated(p + 1, this.bHeight + m + 1, minAxLen - m + 1, backwardsStirRule, rot);
               if (this.minHorizDim % 2 != 0 || m != this.minHorizDim / 2) {
                  this.buildXYRotated(p + 1, this.bHeight + m + 1, m + 1, doubleStepRule.getBlockOrHole(this.world.rand), rot);
                  this.buildXYRotated(p + 1, this.bHeight + m + 1, minAxLen - m - 1 + 1, doubleStepRule.getBlockOrHole(this.world.rand), rot);
               }
            }

            for (int r = m; r < minAxLen - m; r++) {
               if (this.minHorizDim % 2 != 1 || m != this.minHorizDim / 2) {
                  this.buildXYRotated(
                     1,
                     this.bHeight + m + 1,
                     r + 1,
                     this.circular && this.circle_shape[0][r] < 0
                        ? doubleStepRule.getBlockOrHole(this.world.rand)
                        : this.bRule.getBlockOrHole(this.world.rand),
                     rot
                  );
                  this.buildXYRotated(
                     maxAxLen - 1 + 1,
                     this.bHeight + m + 1,
                     r + 1,
                     this.circular && this.circle_shape[this.bLength - 1][r] < 0
                        ? doubleStepRule.getBlockOrHole(this.world.rand)
                        : this.bRule.getBlockOrHole(this.world.rand),
                     rot
                  );
               }
            }
         }
      }

      if (this.circular && (this.roofStyle == 1 || this.roofStyle == 2 || this.roofStyle == 3 || this.roofStyle == 6)) {
         for (int x1x = 0; x1x < this.minHorizDim; x1x++) {
            for (int y1x = 0; y1x < this.minHorizDim; y1x++) {
               if (this.circle_shape[x1x][y1x] < 0) {
                  this.buffer[x1x + 1][this.bHeight + 1][y1x + 1] = doubleStepRule.getBlockOrHole(this.world.rand);
               }

               if (this.circle_shape[x1x][y1x] == 1) {
                  for (int z1 = this.bHeight; z1 < this.bHeight + this.minHorizDim && this.buffer[x1x + 1][z1 + 1][y1x + 1] == HOLE_BLOCK_LIGHTING; z1++) {
                     this.buffer[x1x + 1][z1 + 1][y1x + 1] = this.bRule.getBlockOrHole(this.world.rand);
                  }
               }
            }
         }
      }

      if (this.roofStyle == 4 || this.roofStyle == 5) {
         int xBuff = (this.bWidth - this.minHorizDim) / 2;
         int yBuff = (this.bLength - this.minHorizDim) / 2;

         for (int x1x = 0; x1x < this.minHorizDim; x1x++) {
            for (int y1x = 0; y1x < this.minHorizDim; y1x++) {
               if (this.circle_shape[x1x][y1x] >= 0) {
                  this.buffer[x1x + xBuff + 1][this.bHeight + 1][y1x + yBuff + 1] = this.circle_shape[x1x][y1x] == 0
                     ? HOLE_BLOCK_LIGHTING
                     : this.bRule.getBlockOrHole(this.world.rand);
               } else if (!this.circular) {
                  this.buffer[x1x + xBuff + 1][this.bHeight + 1][y1x + yBuff + 1] = this.bRule.getBlockOrHole(this.world.rand);
               }
            }
         }

         if (!this.circular) {
            for (int y1xx = 0; y1xx < this.minHorizDim; y1xx++) {
               for (int x1x = 0; x1x < xBuff; x1x++) {
                  this.buffer[x1x + 1][this.bHeight + 1][y1xx + 1] = this.bRule.getBlockOrHole(this.world.rand);
               }

               for (int x1x = xBuff + this.minHorizDim; x1x < this.bWidth; x1x++) {
                  this.buffer[x1x + 1][this.bHeight + 1][y1xx + 1] = this.bRule.getBlockOrHole(this.world.rand);
               }
            }

            for (int x1x = 0; x1x < this.minHorizDim; x1x++) {
               for (int y1xx = 0; y1xx < yBuff; y1xx++) {
                  this.buffer[x1x + 1][this.bHeight + 1][y1xx + 1] = this.bRule.getBlockOrHole(this.world.rand);
               }

               for (int y1xx = yBuff + this.minHorizDim; y1xx < this.bLength; y1xx++) {
                  this.buffer[x1x + 1][this.bHeight + 1][y1xx + 1] = this.bRule.getBlockOrHole(this.world.rand);
               }
            }
         }
      }
   }

   private void populateBookshelves(int z) {
      int x1 = this.random.nextInt(this.bWidth - 2) + 1;
      int y1 = this.random.nextInt(this.bLength - 2) + 1;
      int dir = this.random.nextInt(4);
      int xinc = DIR_TO_X[dir];

      for (int yinc = DIR_TO_Y[dir]; x1 >= 1 && x1 < this.bWidth - 1 && y1 >= 1 && y1 < this.bLength - 1 && this.isFloor(x1, z, y1); y1 += yinc) {
         if (BlockProperties.get(this.getBlockIdLocal(x1 + xinc, z, y1 + yinc)).isArtificial
            && BlockProperties.get(this.getBlockIdLocal(x1 + xinc, z - 1, y1 + yinc)).isArtificial
            && this.getBlockIdLocal(x1 + xinc, z - 1, y1 + yinc) != Blocks.LADDER) {
            for (int m = 0; m < 2; m++) {
               for (int z1 = z;
                  z1 < z + 1 + this.random.nextInt(3)
                     && this.getBlockIdLocal(x1, z1, y1) == Blocks.AIR
                     && this.isWallBlock(x1 + xinc, z1, y1 + yinc);
                  z1++
               ) {
                  this.setBlockLocal(x1, z1, y1, Blocks.BOOKSHELF);
               }

               x1 += DIR_TO_X[(dir + 1) % 4];
               y1 += DIR_TO_Y[(dir + 1) % 4];
               if (!this.isFloor(x1, z, y1)) {
                  break;
               }
            }

            return;
         }

         x1 += xinc;
      }
   }

   private boolean populatePortal(int z) {
      if (Compat.waterVaporizes(this.world)) {
         if (this.random.nextInt(10) != 0) {
            return false;
         }
      } else if (this.random.nextInt(20) != 0) {
         return false;
      }

      boolean hasSupport = false;

      for (int y1 = this.bLength / 2 - 2; y1 < this.bLength / 2 + 2; y1++) {
         if (this.getBlockIdLocal(this.bWidth / 2, z, y1) != Blocks.AIR) {
            return false;
         }

         if (this.getBlockIdLocal(this.bWidth / 2, z - 1, y1) != Blocks.AIR) {
            hasSupport = true;
         }
      }

      if (!hasSupport) {
         return false;
      } else {
         for (int y1 = this.bLength / 2 - 2; y1 < this.bLength / 2 + 2; y1++) {
            this.setBlockLocal(this.bWidth / 2, z, y1, Blocks.OBSIDIAN);
            this.setBlockLocal(this.bWidth / 2, z + 4, y1, Blocks.OBSIDIAN);
         }

         for (int z1 = z + 1; z1 < z + 4; z1++) {
            this.setBlockLocal(this.bWidth / 2, z1, this.bLength / 2 - 2, Blocks.OBSIDIAN);
            this.setBlockLocal(this.bWidth / 2, z1, this.bLength / 2 - 1, Blocks.PORTAL);
            this.setBlockLocal(this.bWidth / 2, z1, this.bLength / 2, Blocks.PORTAL);
            this.setBlockLocal(this.bWidth / 2, z1, this.bLength / 2 + 1, Blocks.OBSIDIAN);
         }

         return true;
      }
   }

   public BuildingTower(
      int ID_,
      Building parent,
      boolean circular_,
      int roofStyle_,
      int dir_,
      int axXHand_,
      boolean centerAligned_,
      int TWidth_,
      int THeight_,
      int TLength_,
      int[] sourcePt
   ) {
      super(ID_, parent.wgt, parent.bRule, dir_, axXHand_, centerAligned_, new int[]{TWidth_, THeight_, TLength_}, sourcePt);
      this.baseHeight = 0;
      this.roofStyle = roofStyle_;
      this.minHorizDim = Math.min(this.bWidth, this.bLength);
      this.circle_shape = CIRCLE_SHAPE[this.minHorizDim];
      this.circular = circular_;
      this.ChestRule = TemplateRule.RULE_NOT_PROVIDED;
      this.roofRule = this.bRule;
      this.SpawnerRule = TemplateRule.RULE_NOT_PROVIDED;
      this.PopulateFurniture = false;
      this.MakeDoors = false;
      if (this.circular) {
         this.bLength = this.bWidth = this.minHorizDim;
      }
   }

   public BuildingTower(int ID_, BuildingWall wall, int dir_, int axXHand_, boolean centerAligned_, int TWidth_, int THeight_, int TLength_, int[] sourcePt) {
      super(ID_, wall.wgt, wall.towerRule, dir_, axXHand_, centerAligned_, new int[]{TWidth_, THeight_, TLength_}, sourcePt);
      this.baseHeight = wall.WalkHeight;
      this.roofStyle = wall.roofStyle;
      this.minHorizDim = Math.min(this.bWidth, this.bLength);
      this.circle_shape = CIRCLE_SHAPE[this.minHorizDim];
      this.circular = wall.circular;
      this.ChestRule = wall.ws.ChestRule;
      this.roofRule = wall.roofRule;
      this.SpawnerRule = wall.ws.SpawnerRule;
      this.PopulateFurniture = wall.ws.PopulateFurniture;
      this.MakeDoors = wall.ws.MakeDoors;
      if (this.circular) {
         this.bLength = this.bWidth = this.minHorizDim;
      }
   }

   private void buildWindowOrDoor(int x, int z, int y, int xFace, int yFace, int height) {
      boolean buildWoodDoor = false;
      if (this.isFloor(x + xFace, z - 1, y + yFace) || this.isFloor(x + xFace, z - 2, y + yFace)) {
         z--;
         if (this.MakeDoors) {
            buildWoodDoor = true;
         }
      }

      if (BlockProperties.get(this.getBlockIdLocal(x, z + height - 2, y + yFace)).isWallable) {
         if (buildWoodDoor) {
            int metadata = xFace == 0 ? (yFace > 0 ? 1 : 3) : (xFace > 0 ? 2 : 0);
            this.buffer[x + 1][z + 1][y + 1] = new BlockAndMeta(Blocks.OAK_DOOR, metadata);
            this.buffer[x + 1][z + 1 + 1][y + 1] = new BlockAndMeta(Blocks.OAK_DOOR, this.random.nextBoolean() ? 8 : 9);
            if (this.isFloor(x + xFace, z - 1, y + yFace)
               && x + xFace + 1 >= 0
               && x + xFace + 1 < this.buffer.length
               && y + yFace + 1 >= 0
               && y + yFace + 1 < this.buffer[0][0].length) {
               this.buffer[x + xFace + 1][z - 1 + 1][y + yFace + 1] = this.bRule.primaryBlock.toStep();
            }
         } else {
            for (int z1 = z; z1 < z + height; z1++) {
               this.buffer[x + 1][z1 + 1][y + 1] = HOLE_BLOCK_LIGHTING;
            }
         }

         if (this.isFloor(x + xFace, z + 2, y + yFace)
            && this.isWallBlock(x + xFace, z, y + yFace)
            && x + xFace + 1 >= 0
            && x + xFace + 1 < this.buffer.length
            && y + yFace + 1 >= 0
            && y + yFace + 1 < this.buffer[0][0].length) {
            this.buffer[x + xFace + 1][z + 1 + 1][y + yFace + 1] = HOLE_BLOCK_LIGHTING;
         }
      }
   }

   private void populateBeds(int z) {
      int dir = this.random.nextInt(4);
      int x1 = this.random.nextInt(this.bWidth - 2) + 1;
      int y1 = this.random.nextInt(this.bLength - 2) + 1;
      int x2 = x1 + DIR_TO_X[dir];
      int y2 = y1 + DIR_TO_Y[dir];
      if (this.isFloor(x1, z, y1) && this.hasNoDoorway(x1, z, y1) && this.isFloor(x2, z, y2) && this.hasNoDoorway(x2, z, y2)) {
         this.setBlockLocal(x1, z, y1, Blocks.BED, dir + 8);
         this.setBlockLocal(x2, z, y2, Blocks.BED, dir);
      }
   }

   private void populateFurnitureColumn(int z, BlockAndMeta[] block) {
      int x1 = this.random.nextInt(this.bWidth - 2) + 1;
      int y1 = this.random.nextInt(this.bLength - 2) + 1;
      if (this.isFloor(x1, z, y1) && this.hasNoDoorway(x1, z, y1)) {
         for (int z1 = 0; z1 < block.length; z1++) {
            this.setBlockLocal(x1, z + z1, y1, block[z1]);
         }
      }
   }

   private boolean populateGhastSpawner(int z) {
      for (int tries = 0; tries < 5; tries++) {
         int x1 = this.random.nextInt(this.bWidth - 2) + 1;
         int y1 = this.random.nextInt(this.bLength - 2) + 1;
         if (this.isFloor(x1, z, y1)) {
            this.setBlockLocal(x1, z, y1, GHAST_SPAWNER);
            return true;
         }
      }

      return false;
   }

   static {
      int m = 0;

      while (m < ROOFSTYLE_NAMES.length) {
         ROOF_STYLE_IDS[m] = m++;
      }
   }
}
