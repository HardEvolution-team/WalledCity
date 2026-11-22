package com.electrifiedded.walledcitymod;

import com.electrifiedded.archaiclib.legacy.world.LegacyWorld;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

public class BuildingWall extends Building {
   protected static final Block[] STEP_TO_STAIRS = new Block[]{
      Blocks.STONE_BRICK_STAIRS,
      Blocks.SANDSTONE_STAIRS,
      Blocks.OAK_STAIRS,
      Blocks.STONE_STAIRS,
      Blocks.BRICK_STAIRS,
      Blocks.STONE_BRICK_STAIRS,
      Blocks.NETHER_BRICK_STAIRS,
      Blocks.QUARTZ_STAIRS
   };
   public static final boolean DEBUG = false;
   public static final boolean DEBUG_SIGNS = false;
   public static final int SEARCHDOWN = 2;
   public static final int MIN_SEARCHUP = 2;
   public static final int DEFAULT_LOOKAHEAD = 5;
   public static final int MIN_BRANCH_IMPROVEMENT = 15;
   public static final int MAX_BACKTRACK_DEPTH = 2;
   public static final int OVERHEAD_CLEARENCE = 4;
   public static final int OVERHEAD_TREE_CLEARENCE = 8;
   public static final int NO_GATEWAY = -1;
   public static final int NO_MIN_J = -1;
   private static final int MIN_GATEWAY_ROAD_LENGTH = 20;
   public int i1;
   public int j1;
   public int k1;
   public int n0 = 0;
   public int WalkHeight;
   public int maxLength;
   public int[] xArray;
   public int[] zArray;
   public int gatewayStart = -1;
   public int gatewayEnd = -1;
   public TemplateWall ws;
   public boolean target = false;
   public boolean circular = false;
   public int x_targ;
   public int z_targ;
   public int y_targ;
   public int minJ = -1;
   private boolean hitMaxDepth = false;
   public BuildingWall.FailType failCode = BuildingWall.FailType.NOTHING;
   public TemplateTML endBTemplate = null;
   public int endBLength = 0;
   private BlockAndMeta halfStairValue = new BlockAndMeta(Blocks.STONE_SLAB, 2);
   public int roofStyle;
   public TemplateRule towerRule;
   public TemplateRule roofRule;
   public final int Backtrack;

   public BuildingWall(BuildingWall bw, int maxLength_, int i1_, int j1_, int k1_) {
      super(bw.bID, bw.wgt, bw.bRule, bw.bDir, bw.bHand, false, new int[]{bw.bWidth, bw.bHeight, 0}, new int[]{i1_, j1_, k1_});
      this.constructorHelper(bw.ws, maxLength_, i1_, j1_, k1_);
      this.Backtrack = bw.Backtrack;
      this.target = bw.target;
      this.x_targ = bw.x_targ;
      this.z_targ = bw.z_targ;
      this.y_targ = bw.y_targ;
   }

   public BuildingWall(
      int ID_, WorldGeneratorThread wgt_, TemplateWall ws_, int dir_, int axXHand_, int maxLength_, boolean endTowers, int i1_, int j1_, int k1_
   ) {
      super(ID_, wgt_, ws_.rules[ws_.template[0][0][ws_.WWidth / 2]], dir_, axXHand_, false, new int[]{ws_.WWidth, ws_.WHeight, 0}, new int[]{i1_, j1_, k1_});
      this.constructorHelper(ws_, maxLength_, i1_, j1_, k1_);
      this.pickTowers(this.random.nextFloat() < this.ws.CircularProb, endTowers);
      this.Backtrack = this.wgt.backtrackLength;
      if (this.maxLength > 0) {
         this.xArray[0] = 0;
         this.zArray[0] = 0;
      }
   }

   public BuildingWall(int ID_, WorldGeneratorThread wgt_, TemplateWall ws_, int dir_, int axXHand_, int maxLength_, boolean endTowers, int[] sourcePt) {
      super(ID_, wgt_, ws_.rules[ws_.template[0][0][ws_.WWidth / 2]], dir_, axXHand_, false, new int[]{ws_.WWidth, ws_.WHeight, 0}, sourcePt);
      this.constructorHelper(ws_, maxLength_, sourcePt[0], sourcePt[1], sourcePt[2]);
      this.pickTowers(this.random.nextFloat() < this.ws.CircularProb, endTowers);
      this.Backtrack = this.wgt.backtrackLength;
      if (this.maxLength > 0) {
         this.xArray[0] = 0;
         this.zArray[0] = 0;
      }
   }

   public void buildFromTML() {
      if (this.ws == null) {
         System.err.println("Tried to build wall from template but no template was given!");
      } else {
         this.setCursor(0);
         if (this.bLength > 0) {
         }

         int lN = 0;
         int[] base = this.ws.template[0][0];
         if (this.ws.namedLayers.containsKey("base")) {
            base = this.ws.namedLayers.get("base")[this.ws.length - 1];
         }

         int[][] shifted = this.ws.namedLayers.containsKey("shifted") ? this.ws.namedLayers.get("shifted") : this.ws.template[0];
         int[][] shiftedLeft = this.ws.namedLayers.containsKey("shifted_left") ? this.ws.namedLayers.get("shifted_left") : shifted;
         int[][] shiftedRight = this.ws.namedLayers.containsKey("shifted_right") ? this.ws.namedLayers.get("shifted_right") : shifted;
         int[][] shiftedUp = this.ws.namedLayers.containsKey("shifted_up") ? this.ws.namedLayers.get("shifted_up") : shifted;
         int[][] shiftedDown = this.ws.namedLayers.containsKey("shifted_down") ? this.ws.namedLayers.get("shifted_down") : shifted;
         this.setCursor(0);

         while (this.n0 < this.bLength) {
            int[][] layer;
            if (this.n0 == 0) {
               layer = shifted;
            } else if (this.xArray[this.n0 - 1] < this.xArray[this.n0]) {
               layer = shiftedRight;
            } else if (this.xArray[this.n0 - 1] > this.xArray[this.n0]) {
               layer = shiftedLeft;
            } else if (this.zArray[this.n0 - 1] < this.zArray[this.n0]) {
               layer = shiftedUp;
            } else if (this.zArray[this.n0 - 1] > this.zArray[this.n0]) {
               layer = shiftedDown;
            } else if (this.n0 != this.bLength - 1 && this.xArray[this.n0 + 1] == this.xArray[this.n0] && this.zArray[this.n0 + 1] == this.zArray[this.n0]) {
               layer = this.ws.template[lN];
            } else {
               layer = shifted;
            }

            if (layer == this.ws.template[lN]) {
               lN = (lN + 1) % this.ws.height;
            } else {
               lN = 0;
            }

            for (int x1 = 0; x1 < this.bWidth; x1++) {
               boolean keepWallFromAbove = true;

               for (int z1 = this.bHeight + 4 - 1; z1 >= -this.ws.embed; z1--) {
                  boolean wallBlockPresent = this.isWallBlock(x1, z1, 0);
                  BlockAndMeta idAndMeta = z1 < this.bHeight
                     ? this.ws.rules[layer[z1 + this.ws.embed][x1]].getBlockOrHole(this.world.rand)
                     : HOLE_BLOCK_NO_LIGHTING;
                  if (!keepWallFromAbove || !wallBlockPresent || idAndMeta.get() != Blocks.AIR) {
                     keepWallFromAbove = false;
                     if (idAndMeta.get() == Blocks.AIR && idAndMeta instanceof BlockExtended && ((BlockExtended)idAndMeta).info.equals("WALL_STAIR")
                        )
                      {
                        if (!wallBlockPresent && !BlockProperties.get(this.getBlockIdLocal(x1, z1, 0)).isWater) {
                           if (this.n0 > 0 && this.zArray[this.n0 - 1] > this.zArray[this.n0]) {
                              if (this.n0 != 1 && this.zArray[this.n0 - 2] != this.zArray[this.n0 - 1]
                                 || this.n0 != this.bLength - 1 && this.zArray[this.n0] != this.zArray[this.n0 + 1]) {
                                 this.setBlockLocal(x1, z1, 0, STEP_TO_STAIRS[-idAndMeta.getMeta() > 7 ? -idAndMeta.getMeta() - 8 : -idAndMeta.getMeta()], 2);
                              } else {
                                 this.setSpecialBlockLocal(x1, z1, 0, idAndMeta.get(), idAndMeta.getMeta(), ((BlockExtended)idAndMeta).info);
                              }
                           } else if (this.n0 < this.bLength - 1 && this.zArray[this.n0] < this.zArray[this.n0 + 1]) {
                              if (this.n0 != 0 && this.zArray[this.n0 - 1] != this.zArray[this.n0]
                                 || this.n0 != this.bLength - 2 && this.zArray[this.n0 + 1] != this.zArray[this.n0 + 2]) {
                                 this.setBlockLocal(x1, z1, 0, STEP_TO_STAIRS[-idAndMeta.getMeta() > 7 ? -idAndMeta.getMeta() - 8 : -idAndMeta.getMeta()], 3);
                              } else {
                                 this.setSpecialBlockLocal(x1, z1, 0, idAndMeta.get(), idAndMeta.getMeta(), ((BlockExtended)idAndMeta).info);
                              }
                           } else {
                              this.setBlockLocal(x1, z1, 0, idAndMeta.get());
                           }
                        }
                     } else if (z1 < this.WalkHeight
                        || (x1 != 0 || !wallBlockPresent && !this.isWallBlock(-1, this.WalkHeight - 1, 0) && !this.isWallBlock(-1, this.WalkHeight - 2, 0))
                           && (
                              x1 != this.bWidth - 1
                                 || !wallBlockPresent
                                    && !this.isFloor(this.bWidth, this.WalkHeight - 1, 0)
                                    && !this.isWallBlock(this.bWidth, this.WalkHeight - 2, 0)
                           )) {
                        if (idAndMeta.get() == Blocks.AIR && idAndMeta.getMeta() == 0 && z1 < this.bHeight) {
                           this.removeBlockWithLighting(x1, z1, 0);
                        } else {
                           this.setBlockLocal(x1, z1, 0, idAndMeta);
                        }
                     }
                  }
               }
            }

            for (int x1 = 0; x1 < this.bWidth; x1++) {
               this.buildDown(x1, -1 - this.ws.embed, 0, this.ws.rules[base[x1]], this.ws.leveling, Math.min(2, this.ws.embed), 3);
            }

            this.clearTrees();
            this.mergeWallLayer();
            this.setCursor(this.n0 + 1);
         }

         this.flushDelayed();
         this.setCursor(0);
      }
   }

   public BuildingWall[] buildGateway(
      int[] scanWindow,
      int scanStart,
      int gateHeight,
      int gateWidth,
      TemplateWall rs,
      int flankTHand,
      int XMaxLen,
      int[] XTarget,
      int XHand,
      int antiXMaxLen,
      int[] antiXTarget,
      int antiXHand
   ) {
      BuildingWall[] avenues = null;
      if (rs != null) {
         gateWidth = rs.WWidth;
      }

      if (scanStart < scanWindow[0]) {
         scanStart = scanWindow[0];
      }

      if (scanStart > scanWindow[1]) {
         scanStart = scanWindow[1];
      }

      int scanA = scanStart;
      int scanB = scanStart + 1;

      for (boolean aOrB = true; scanA >= scanWindow[0] || scanB <= scanWindow[1]; aOrB = !aOrB) {
         this.setCursor(aOrB ? scanA : scanB);
         if (aOrB) {
            if (scanA < scanWindow[0]) {
               continue;
            }

            scanA -= 3;
         } else {
            if (scanB > scanWindow[1]) {
               continue;
            }

            scanB += 3;
         }

         if (this.n0 - gateWidth - 1 >= 0
            && curvature(this.zArray[this.n0], this.zArray[this.n0 - gateWidth / 2], this.zArray[this.n0 - gateWidth - 1], 1) == 0
            && curvature(this.xArray[this.n0], this.xArray[this.n0 - gateWidth / 2], this.xArray[this.n0 - gateWidth - 1], 0) == 0) {
            int tw = this.ws.pickTWidth(this.circular, this.world.rand);
            int th = this.ws.getTMaxHeight(this.circular);
            if (rs != null) {
               avenues = new BuildingWall[]{
                  new BuildingWall(
                     this.bID,
                     this.wgt,
                     rs,
                     rotDir(this.bDir, this.bHand),
                     XHand,
                     XMaxLen,
                     false,
                     this.getIJKPt(this.bWidth, 0, XHand == -this.bHand ? 1 - gateWidth : 0)
                  ),
                  new BuildingWall(
                     this.bID,
                     this.wgt,
                     rs,
                     rotDir(this.bDir, -this.bHand),
                     antiXHand,
                     antiXMaxLen,
                     false,
                     this.getIJKPt(-1, 0, antiXHand == this.bHand ? 1 - gateWidth : 0)
                  )
               };
               avenues[0].setTarget(XTarget == null ? this.getIJKPt(this.bWidth + tw, 0, XHand == -this.bHand ? 1 - gateWidth : 0) : XTarget);
               avenues[0].plan(1, 0, 5, true);
               if (XTarget == null && avenues[0].bLength >= tw) {
                  avenues[0].target = false;
                  avenues[0].plan(tw + 1, 0, 5, true);
               }

               if (avenues[0].bLength >= 20) {
                  avenues[1].setTarget(antiXTarget == null ? this.getIJKPt(-1 - tw, 0, antiXHand == this.bHand ? 1 - gateWidth : 0) : antiXTarget);
                  avenues[1].plan(1, 0, 5, true);
                  if (antiXTarget == null && avenues[1].bLength >= tw) {
                     avenues[1].target = false;
                     avenues[1].plan(tw + 1, 0, 5, true);
                  }
               }
            }

            if (rs == null || avenues[1].bLength >= 20) {
               if (rs != null) {
                  avenues[0].smooth(10, 10, false);
                  avenues[1].smooth(10, 10, false);
               }

               Block fenceBlock = this.bRule.chance >= 100 && this.bRule.primaryBlock.get() != Blocks.NETHER_BRICK
                  ? Blocks.OAK_FENCE
                  : Blocks.NETHER_BRICK_FENCE;
               int fenceX = flankTHand == 0 ? this.bWidth / 2 : (flankTHand == this.bHand ? this.bWidth - 2 + this.ws.TowerXOffset : 1 - this.ws.TowerXOffset);
               gateHeight = Math.min(gateHeight, this.bHeight - 1);

               for (int y1 = 0; y1 > -gateWidth; y1--) {
                  for (int x1 = 0; x1 < this.bWidth; x1++) {
                     for (int z1 = 0; z1 < gateHeight; z1++) {
                        if (y1 != 0 && y1 != 1 - gateWidth || z1 != gateHeight - 1) {
                           this.setBlockLocal(x1, z1, y1, Blocks.AIR);
                        }
                     }
                  }

                  for (int z1x = gateHeight - 2; z1x < gateHeight; z1x++) {
                     if (this.random.nextInt(100) < this.bRule.chance) {
                        this.setBlockLocal(fenceX, z1x, y1, fenceBlock);
                     }
                  }
               }

               if (flankTHand != -this.bHand) {
                  this.setBlockLocal(-1 - this.ws.TowerXOffset, gateHeight - 2, -gateWidth, WEST_FACE_TORCH_BLOCK);
               }

               if (flankTHand != -this.bHand) {
                  this.setBlockLocal(-1 - this.ws.TowerXOffset, gateHeight - 2, 1, WEST_FACE_TORCH_BLOCK);
               }

               if (flankTHand != this.bHand) {
                  this.setBlockLocal(this.bWidth + this.ws.TowerXOffset, gateHeight - 2, -gateWidth, EAST_FACE_TORCH_BLOCK);
               }

               if (flankTHand != this.bHand) {
                  this.setBlockLocal(this.bWidth + this.ws.TowerXOffset, gateHeight - 2, 1, EAST_FACE_TORCH_BLOCK);
               }

               if (this.n0 + gateWidth + tw > this.bLength) {
                  flankTHand = 0;
               }

               if (flankTHand != 0) {
                  int tnMid1 = this.n0 - gateWidth - tw / 2;
                  int tnMid2 = this.n0 + tw / 2 + 1;
                  int x1 = flankTHand == this.bHand ? this.bWidth - 1 + this.ws.TowerXOffset : -this.ws.TowerXOffset;
                  new BuildingTower(0, this, rotDir(this.bDir, flankTHand), this.bHand, true, tw, th, tw, this.getIJKPtAtN(tnMid1, x1, 0, 0))
                     .build(0, 0, false);
                  new BuildingTower(
                        0,
                        this,
                        rotDir(this.bDir, flankTHand),
                        -this.bHand,
                        true,
                        tw,
                        th + this.zArray[tnMid1] - this.zArray[tnMid2],
                        tw,
                        this.getIJKPtAtN(tnMid2, x1, 0, 0)
                     )
                     .build(0, 0, false);
               }

               this.flushDelayed();
               this.gatewayStart = this.n0 - gateWidth + 1;
               this.gatewayEnd = this.n0;
               if (this.bWidth + 2 * this.ws.TowerXOffset >= 5) {
                  int ngw1 = this.n0 - gateWidth;
                  int ngw2 = this.n0 + 1;
                  int x2 = flankTHand != 0 && flankTHand != this.bHand ? this.bWidth - 2 + this.ws.TowerXOffset : 1 - this.ws.TowerXOffset;

                  for (int n1 = ngw1; n1 > ngw1 - 5; n1--) {
                     if (this.zArray[n1 - 3] == this.zArray[n1] && this.xArray[n1 - 3] == this.xArray[ngw1 + 1]) {
                        new BuildingSpiralStaircase(
                              this.wgt, this.bRule, this.bDir, this.bHand, false, -this.WalkHeight, this.getIJKPtAtN(n1, x2, this.WalkHeight - 2, -3)
                           )
                           .build(1, ngw1 - n1 + 4);
                        this.gatewayStart = n1 - 3;
                        break;
                     }
                  }

                  for (int n1x = ngw2; n1x < ngw2 + 5; n1x++) {
                     if (this.zArray[n1x + 3] == this.zArray[n1x] && this.xArray[n1x + 3] == this.xArray[ngw2 - 1]) {
                        new BuildingSpiralStaircase(
                              this.wgt, this.bRule, flipDir(this.bDir), -this.bHand, false, -this.WalkHeight, this.getIJKPtAtN(n1x, x2, this.WalkHeight - 2, 3)
                           )
                           .build(1, n1x - ngw2 + 5);
                        this.gatewayEnd = n1x + 3;
                        break;
                     }
                  }
               }

               this.gatewayStart = this.gatewayStart - (flankTHand != 0 ? tw + this.ws.BuildingInterval / 2 : 0);
               this.gatewayEnd = this.gatewayEnd + (flankTHand != 0 ? tw + this.ws.BuildingInterval / 2 : 0);
               return avenues;
            }
         }
      }

      return null;
   }

   public String failString() {
      switch (this.failCode) {
         case OBSTRUCTED:
            return "Obstructed.";
         case UNDERWATER:
            return "Underwater.";
         case TOOSTEEPDOWN:
            return "Too Steep Down.";
         case TOOSTEEPUP:
            return "Too Steep Up.";
         case HITWALL:
            return "Hit Wall";
         case CANNOTEXPLORE:
            return "Could not explore";
         case HITTARGET:
            return "Hit Target";
         case MAXLENGTH:
            return "Max length (" + this.maxLength + ") reached.";
         case NOTHING:
         default:
            return "No Fail.";
      }
   }

   public int[] getIJKPtAtN(int n, int x, int z, int y) {
      return n == this.n0
         ? this.getIJKPt(x, z, y)
         : this.getIJKPt(x + this.xArray[n] - this.xArray[this.n0], z + this.zArray[n] - this.zArray[this.n0], y + n - this.n0);
   }

   public void makeBuildings(boolean buildOnL, boolean buildOnR, boolean makeGatehouseTowers, boolean overlapTowers, boolean isAvenue) {
      if (this.ws == null) {
         System.err.println("Tried to build towers but wall style was null!");
      } else if (this.ws.MakeBuildings) {
         makeGatehouseTowers = makeGatehouseTowers && this.ws.makeDefaultTower.weight > 0 && !this.circular;
         int cursorStart = Math.max(this.ws.getTMaxWidth(this.circular) + 3, 2 * this.ws.BuildingInterval / 3);
         this.setCursor(cursorStart);

         for (; this.n0 < this.bLength; this.setCursor(this.n0 + 1)) {
            if (this.gatewayStart != -1 && this.n0 >= this.gatewayStart && this.n0 <= this.gatewayEnd + this.ws.getTMaxWidth(this.circular) + 2) {
               this.setCursor(this.gatewayEnd + this.ws.getTMaxWidth(this.circular) + 2);
               if (this.n0 >= this.bLength) {
                  break;
               }
            }

            int tw = this.ws.pickTWidth(this.circular, this.world.rand);
            int nMid = this.n0 - tw / 2 - 2;
            int nBack = this.n0 - tw - 3;
            int clearSide = -this.bHand * signum(curvature(this.xArray[nBack], this.xArray[nMid], this.xArray[this.n0], 0), 0);
            if (clearSide == 0) {
               if (buildOnL && buildOnR) {
                  clearSide = 2 * this.random.nextInt(2) - 1;
               } else {
                  clearSide = buildOnL ? -1 : 1;
               }
            }

            if (makeGatehouseTowers
               && curvature(this.zArray[nBack], this.zArray[nMid], this.zArray[this.n0], 0) == 0
               && curvature(this.xArray[nBack], this.xArray[nMid], this.xArray[this.n0], 2) == 0) {
               BuildingTower tower = new BuildingTower(
                  this.bID + this.n0,
                  this,
                  flipDir(this.bDir),
                  -this.bHand,
                  true,
                  tw,
                  this.ws.pickTHeight(this.circular, this.world.rand),
                  this.circular ? tw : this.ws.pickTWidth(this.circular, this.world.rand),
                  this.getIJKPtAtN(nMid, this.bWidth / 2, 0, tw / 2)
               );
               if (!tower.isObstructedRoof(-1)) {
                  this.wgt.setLayoutCode(tower.getIJKPt(0, 0, 0), tower.getIJKPt(tw - 1, 0, tw - 1), 4);
                  tower.build(this.xArray[this.n0 - 1] - this.xArray[nMid], this.xArray[nBack + 1] - this.xArray[nMid], false);
                  this.setCursor(this.n0 + this.ws.BuildingInterval - 1);
               }
            } else if (buildOnL && clearSide == -1 || buildOnR && clearSide == 1) {
               TemplateTML template = this.ws
                  .buildings
                  .get(Building.pickWeightedOption(this.world.rand, this.ws.buildingWeights[0], this.ws.buildingWeights[1]));
               int ybuffer = -this.ws.TowerXOffset + (isAvenue ? 0 : 1);
               int[] pt = this.getIJKPtAtN(nMid, clearSide == this.bHand ? this.bWidth - ybuffer : ybuffer - 1, 0, 0);
               if (this.makeBuilding(template, tw, ybuffer, overlapTowers, rotDir(this.bDir, clearSide), pt)) {
                  this.setCursor(this.n0 + this.ws.BuildingInterval - 1);
               }
            }
         }

         this.setCursor(0);
         if (this.endBLength >= 5) {
            int endTN = this.circular ? this.bLength - 2 : this.bLength - 1;
            if (endTN < 0) {
               endTN = 0;
            }

            int[] pt = this.getIJKPtAtN(endTN, this.bWidth / 2, 0, 1);
            this.makeBuilding(this.endBTemplate, this.ws.pickTWidth(this.circular, this.world.rand), 1, overlapTowers, this.bDir, pt);
         }
      }
   }

   public int plan(int startN, int depth, int lookahead, boolean stopAtWall) {
      if (startN >= 1 && startN < this.maxLength) {
         int fails = 0;
         this.setOriginLocal(this.i1, this.j1, this.k1, this.xArray[startN - 1], this.zArray[startN - 1], startN);
         this.bLength = startN;
         int searchUp = 2;
         int obstructionHeight = this.WalkHeight > 4 ? this.WalkHeight + 1 : this.bHeight + 1;

         while (true) {
            int gradx = 0;
            int gradz = 0;
            this.failCode = BuildingWall.FailType.NOTHING;

            for (int x1 = -1; x1 <= this.bWidth; x1++) {
               for (int z1 = -2; z1 <= searchUp; z1++) {
                  Block blockId = this.getBlockIdLocal(x1, z1, 0);
                  if (!BlockProperties.get(blockId).isWallable) {
                     gradz++;
                     gradx += Integer.signum(2 * x1 - this.bWidth + 1);
                  } else if (BlockProperties.get(blockId).isWater) {
                     gradx -= Integer.signum(2 * x1 - this.bWidth + 1);
                  }

                  if ((stopAtWall || z1 < -2) && this.isArtificialWallBlock(x1, z1, 0)) {
                     this.failCode = BuildingWall.FailType.HITWALL;
                  }
               }

               if (BlockProperties.get(this.getBlockIdLocal(x1, this.ws.waterHeight + 1, 0)).isWater) {
                  this.failCode = BuildingWall.FailType.UNDERWATER;
               }

               if (!this.isWallable(x1, obstructionHeight, 0) && this.failCode == BuildingWall.FailType.NOTHING) {
                  this.failCode = BuildingWall.FailType.OBSTRUCTED;
               }
            }

            gradz = (gradz + (this.bWidth + 2) / 2) / (this.bWidth + 2) - 2;
            if (this.failCode == BuildingWall.FailType.HITWALL) {
               gradz = 0;
            }

            if (this.failCode == BuildingWall.FailType.NOTHING && gradz < -1) {
               this.failCode = BuildingWall.FailType.TOOSTEEPDOWN;
            }

            if (this.failCode == BuildingWall.FailType.NOTHING && gradz > 4) {
               this.failCode = BuildingWall.FailType.TOOSTEEPUP;
            }

            gradz = signum(gradz, 0);
            if (this.minJ != -1 && this.zArray[this.bLength - 1] + gradz + this.j1 < this.minJ) {
               gradz = 0;
            }

            if (gradz == 0) {
               int HorizForceThreshold = this.bWidth / 2;
               int bias = this.target ? Integer.signum(this.xArray[this.bLength - 1] - this.x_targ) * 2 * HorizForceThreshold : 0;
               gradx = gradx > HorizForceThreshold + bias ? 1 : (gradx < -HorizForceThreshold + bias ? -1 : 0);
            } else {
               gradx = 0;
            }

            this.setOriginLocal(this.i0, this.j0, this.k0, gradx, gradz, 1);
            this.xArray[this.bLength] = this.xArray[this.bLength - 1] + gradx;
            this.zArray[this.bLength] = this.zArray[this.bLength - 1] + gradz;
            this.bLength++;
            if (this.failCode == BuildingWall.FailType.NOTHING) {
               fails = 0;
            } else {
               fails++;
            }

            if (this.target && this.bLength > this.y_targ) {
               this.failCode = BuildingWall.FailType.HITTARGET;
               break;
            }

            if (this.bLength >= this.maxLength) {
               this.failCode = BuildingWall.FailType.MAXLENGTH;
               break;
            }

            if (this.failCode == BuildingWall.FailType.HITWALL || this.failCode == BuildingWall.FailType.UNDERWATER) {
               this.bLength -= fails;
               break;
            }

            if (fails >= lookahead) {
               this.bLength -= fails;
               if (this.bLength - startN < this.Backtrack || this.bLength - startN < 15 && depth != 0) {
                  break;
               }

               if (depth >= 2) {
                  this.hitMaxDepth = true;
                  break;
               }

               int bestImprovement = 0;
               BuildingWall bestBranch = null;

               for (int zAx = 0; zAx <= 1; zAx++) {
                  for (int d = -1; d <= 1; d++) {
                     if (zAx != 0 || d != 0) {
                        BuildingWall branch = new BuildingWall(this, this.maxLength, this.i1, this.j1, this.k1);

                        for (int m = 0; m < this.Backtrack; m++) {
                           branch.xArray[this.bLength - this.Backtrack + m] = this.xArray[this.bLength - this.Backtrack] + (1 - zAx) * d * m;
                           branch.zArray[this.bLength - this.Backtrack + m] = this.zArray[this.bLength - this.Backtrack] + zAx * d * m;
                        }

                        int improvement = branch.plan(this.bLength, depth + 1, lookahead, stopAtWall);
                        if (improvement > bestImprovement) {
                           bestBranch = branch;
                           bestImprovement = improvement;
                        }
                     }
                  }
               }

               if (bestImprovement + this.bLength > this.maxLength) {
                  bestImprovement = this.maxLength - this.bLength;
               }

               if (bestImprovement > 0) {
                  for (int m = this.bLength - this.Backtrack; m < this.bLength + bestImprovement; m++) {
                     this.xArray[m] = bestBranch.xArray[m];
                     this.zArray[m] = bestBranch.zArray[m];
                     this.failCode = bestBranch.failCode;
                  }

                  this.hitMaxDepth = bestBranch.hitMaxDepth;
                  this.bLength += bestImprovement;
               }

               if (depth != 0 || !this.hitMaxDepth || this.bLength >= this.maxLength) {
                  break;
               }

               this.hitMaxDepth = false;
               fails = 1;
            }
         }

         if (depth == 0) {
            this.bLength = this.bLength - this.endBLength;
            if (this.bLength < startN) {
               this.bLength = startN;
            }
         }

         this.setCursor(0);
         return this.bLength - startN;
      } else {
         System.err.println("Error, bad start length at BuildingWall.plan:" + startN + ".");
         return 0;
      }
   }

   public void printWall() {
   }

   public void printWall(int start) {
   }

   public boolean ptIsToXHand(int[] pt, int buffer) {
      this.setCursor(0);
      if (this.ws.TowerXOffset < 0) {
         buffer -= this.ws.TowerXOffset;
      }

      int ptY = this.getY(pt);
      if (ptY < 0) {
         return this.getX(pt) >= buffer;
      } else {
         return ptY >= this.bLength ? this.getX(pt) >= this.xArray[this.bLength - 1] + buffer : this.getX(pt) >= this.xArray[ptY] + buffer;
      }
   }

   public boolean queryLayout(int layoutCode) {
      for (int n = 0; n < this.bLength; n++) {
         this.setCursor(n);
         if (!this.wgt.layoutIsClear(this.getIJKPt(0, 0, 0), this.getIJKPt(this.bWidth - 1, 0, 0), layoutCode)) {
            this.setCursor(0);
            return false;
         }
      }

      this.setLayoutCode(layoutCode);
      return true;
   }

   public void setCursor(int n) {
      this.n0 = n;
      if (this.n0 >= 0 && (this.n0 < this.bLength || this.bLength == 0)) {
         this.setOriginLocal(this.i1, this.j1, this.k1, this.bLength == 0 ? 0 : this.xArray[this.n0], this.bLength == 0 ? 0 : this.zArray[this.n0], this.n0);
      }
   }

   public void setLayoutCode(int layoutCode) {
      for (int n = 0; n < this.bLength; n++) {
         this.setCursor(n);
         this.wgt.setLayoutCode(this.getIJKPt(0, 0, 0), this.getIJKPt(this.bWidth - 1, 0, 0), layoutCode);
      }

      this.setCursor(0);
   }

   public BuildingWall setMinJ(int minJ_) {
      this.minJ = minJ_;
      return this;
   }

   public boolean setTarget(int[] targ) {
      if (targ[1] > 20 && Math.abs(this.j1 - targ[1]) < Math.max(Math.abs(this.i1 - targ[0]), Math.abs(this.k1 - targ[2]))) {
         this.target = true;
         this.setPrimaryAx(Math.abs(this.i1 - targ[0]) > Math.abs(this.k1 - targ[2]) ? (targ[0] > this.i1 ? 1 : 3) : (targ[2] > this.k1 ? 2 : 0));
         this.setCursor(0);
         this.x_targ = this.getX(targ);
         this.z_targ = this.getZ(targ);
         this.y_targ = this.getY(targ);
      }

      return this.target;
   }

   public BuildingWall setTowers(BuildingWall bw) {
      this.circular = bw.circular;
      this.roofStyle = bw.roofStyle;
      this.towerRule = bw.towerRule;
      this.roofRule = bw.roofRule;
      this.endBLength = bw.endBLength;
      return this;
   }

   public void smooth(int convexWindow, int concaveWindow, boolean flattenEnds) {
      smooth(this.xArray, 0, this.bLength - 1, convexWindow, concaveWindow, flattenEnds);
      smooth(this.zArray, 0, this.bLength - 1, convexWindow, concaveWindow, flattenEnds);
   }

   private void clearTrees() {
      for (int x1 = 0; x1 < this.bWidth; x1++) {
         for (int z1 = this.bHeight + 4; z1 < this.bHeight + 8; z1++) {
            Block block = this.getBlockIdLocal(x1, z1, 0);
            if (BlockProperties.get(block).isTree) {
               this.setBlockLocal(x1, z1, 0, Blocks.AIR);
            }
         }
      }
   }

   private void constructorHelper(TemplateWall ws_, int maxLength_, int i1_, int j1_, int k1_) {
      this.i1 = i1_;
      this.j1 = j1_;
      this.k1 = k1_;
      this.ws = ws_;
      this.WalkHeight = this.ws.WalkHeight;
      this.maxLength = maxLength_;
      this.xArray = new int[this.maxLength];
      this.zArray = new int[this.maxLength];
      this.bLength = 0;
      this.halfStairValue = this.bRule.primaryBlock.toStep();
   }

   private boolean makeBuilding(TemplateTML template, int tw, int ybuffer, boolean overlapTowers, int dir, int[] pt) {
      if (template == this.ws.makeDefaultTower) {
         int maxBL = this.bDir == dir ? this.endBLength : (this.circular ? tw : this.ws.pickTWidth(false, this.world.rand));

         for (int tl = maxBL; tl >= this.ws.getTMinWidth(this.circular); tl--) {
            BuildingTower tower = new BuildingTower(
               this.bID + this.n0, this, dir, 1, true, this.circular ? tl : tw, this.ws.pickTHeight(this.circular, this.world.rand), tl, pt
            );
            if (tower.queryCanBuild(ybuffer, overlapTowers)) {
               tower.build(0, 0, true);
               return true;
            }
         }
      } else if (template == this.ws.makeCARuin) {
         byte[][] caRule = this.ws.CARuinAutomataRules.get(this.random.nextInt(this.ws.CARuinAutomataRules.size()));

         for (int tries = 0; tries < 10; tries++) {
            byte[][] seed = BuildingCellularAutomaton.makeSymmetricSeed(this.ws.CARuinContainerWidth, 0.5F, this.world.rand);
            BuildingCellularAutomaton bca = new BuildingCellularAutomaton(
               this.wgt,
               this.ws.CARuinRule,
               dir,
               1,
               true,
               this.ws.CARuinContainerWidth,
               this.ws.CARuinMinHeight + this.random.nextInt(this.ws.CARuinMaxHeight - this.ws.CARuinMinHeight + 1),
               this.ws.CARuinContainerWidth,
               seed,
               caRule,
               null,
               pt
            );
            if (bca.plan(false, 12) && bca.queryCanBuild(ybuffer, this.ws.CARuinContainerWidth <= 15)) {
               bca.build(true, true);
               return true;
            }
         }

         if (this.bDir == dir && this.ws.makeDefaultTower.weight > 0) {
            return this.makeBuilding(this.ws.makeDefaultTower, tw, ybuffer, overlapTowers, dir, pt);
         }
      } else {
         BuildingTML buildingTML = new BuildingTML(this.bID + this.n0, this.wgt, dir, 1, true, template, pt);
         if (buildingTML.queryCanBuild(ybuffer)) {
            buildingTML.build();
            return true;
         }

         if (this.bDir == dir && this.ws.makeDefaultTower.weight > 0) {
            return this.makeBuilding(this.ws.makeDefaultTower, tw, ybuffer, overlapTowers, dir, pt);
         }
      }

      return false;
   }

   private void mergeWallLayer() {
      if (this.isFloor(-1, this.WalkHeight - 1, 0)) {
         this.setBlockLocal(-1, this.WalkHeight - 1, 0, this.halfStairValue.get(), this.halfStairValue.getMeta());
      }

      if (this.isFloor(this.bWidth, this.WalkHeight - 1, 0)) {
         this.setBlockLocal(this.bWidth, this.WalkHeight - 1, 0, this.halfStairValue.get(), this.halfStairValue.getMeta());
      }

      if (this.isFloor(-1, this.WalkHeight + 1, 0)
         && this.isFloor(-2, this.WalkHeight + 2, 0)
         && this.isFloor(-2, this.WalkHeight + 2, 1)
         && this.isFloor(-2, this.WalkHeight + 2, -1)) {
         this.setBlockLocal(0, this.WalkHeight, 0, this.halfStairValue.get(), this.halfStairValue.getMeta());
      }

      if (this.isFloor(this.bWidth, this.WalkHeight + 1, 0)
         && this.isFloor(this.bWidth + 1, this.WalkHeight + 2, 0)
         && this.isFloor(this.bWidth + 1, this.WalkHeight + 2, 1)
         && this.isFloor(this.bWidth + 1, this.WalkHeight + 2, -1)) {
         this.setBlockLocal(this.bWidth - 1, this.WalkHeight, 0, this.halfStairValue.get(), this.halfStairValue.getMeta());
      }

      int[] pt = this.getIJKPt(-1, this.WalkHeight - 1, 0);
      Block id = GenAccess.getBlock(this.world, pt[0], pt[1], pt[2]);
      int meta = GenAccess.getBlockMetadata(this.world, pt[0], pt[1], pt[2]);
      if (BlockProperties.get(id).isStair && STAIRS_META_TO_DIR[meta < 4 ? meta : meta - 4] == rotDir(this.bDir, -this.bHand)) {
         BlockAndMeta temp = new BlockAndMeta(id, meta).stairToSolid();
         GenAccess.setBlock(this.world, pt[0], pt[1], pt[2], temp.get(), temp.getMeta(), 2);
      }

      pt = this.getIJKPt(this.bWidth, this.WalkHeight - 1, 0);
      id = GenAccess.getBlock(this.world, pt[0], pt[1], pt[2]);
      meta = GenAccess.getBlockMetadata(this.world, pt[0], pt[1], pt[2]);
      if (BlockProperties.get(id).isStair && STAIRS_META_TO_DIR[meta < 4 ? meta : meta - 4] == rotDir(this.bDir, this.bHand)) {
         BlockAndMeta temp = new BlockAndMeta(id, meta).stairToSolid();
         GenAccess.setBlock(this.world, pt[0], pt[1], pt[2], temp.get(), temp.getMeta(), 2);
      }
   }

   private void pickTowers(boolean circular_, boolean endTowers) {
      this.circular = circular_;
      if (this.ws != null) {
         this.roofStyle = this.ws.pickRoofStyle(this.circular, this.world.rand);
         this.towerRule = this.ws.TowerRule.getFixedRule(this.world.rand);
         this.roofRule = this.ws.getRoofRule(this.circular);
         if (this.roofRule != TemplateRule.RULE_NOT_PROVIDED) {
            this.roofRule = this.roofRule.getFixedRule(this.world.rand);
         }

         if (endTowers && this.ws.MakeEndTowers) {
            this.endBTemplate = this.ws
               .buildings
               .get(Building.pickWeightedOption(this.world.rand, this.ws.buildingWeights[0], this.ws.buildingWeights[1]));
            this.endBLength = this.endBTemplate == this.ws.makeDefaultTower
               ? this.ws.pickTWidth(this.circular, this.world.rand) + 1
               : (this.endBTemplate == this.ws.makeCARuin ? this.ws.CARuinContainerWidth : this.endBTemplate.length);
         }
      }
   }

   public static void smooth(int[] arry, int a, int b, int convexWindow, int concaveWindow, boolean flattenEnds) {
      int shorterWinStart = a;
      int longerWinStart = a;
      int shorterWinInitEnd = a + Math.min(concaveWindow, convexWindow);
      int longerWinInitEnd = a + Math.max(concaveWindow, convexWindow);

      for (int winEnd = a + 2; winEnd <= b; winEnd++) {
         if (winEnd >= shorterWinInitEnd) {
            shorterWinStart++;
         }

         if (winEnd >= longerWinInitEnd) {
            longerWinStart++;
         }

         int n = winEnd - 1;
         int leadSlope = arry[winEnd] - arry[n];
         int smoothStart;
         if (leadSlope * (arry[n] - arry[shorterWinStart]) < 0) {
            smoothStart = shorterWinStart;
         } else if (leadSlope * (arry[n] - arry[longerWinStart]) < 0 && leadSlope * (convexWindow - concaveWindow) < 0) {
            smoothStart = longerWinStart;
         } else {
            smoothStart = -1;
         }

         if (smoothStart >= 0) {
            while (true) {
               arry[n] = arry[winEnd];
               n--;
               if (n <= smoothStart || arry[n] == arry[winEnd]) {
                  break;
               }
            }
         }
      }

      if (flattenEnds && b - a >= 2) {
         if (arry[a] != arry[a + 2]) {
            arry[a] = arry[a + 2];
         }

         if (arry[b] != arry[b - 2]) {
            arry[b] = arry[b - 2];
         }

         if (arry[a + 1] != arry[a + 2]) {
            arry[a + 1] = arry[a + 2];
         }

         if (arry[b - 1] != arry[b - 2]) {
            arry[b - 1] = arry[b - 2];
         }
      }
   }

   private static int curvature(int a, int b, int c, int wiggle) {
      int d1 = signum(a - b, wiggle);
      int d2 = signum(c - b, wiggle);
      return d1 * d2 < 0 ? 2 * d2 : signum(d1 + d2, 0);
   }

   public static enum FailType {
      NOTHING,
      OBSTRUCTED,
      UNDERWATER,
      TOOSTEEPDOWN,
      TOOSTEEPUP,
      HITWALL,
      CANNOTEXPLORE,
      HITTARGET,
      MAXLENGTH;
   }
}
