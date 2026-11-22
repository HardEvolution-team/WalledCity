package com.electrifiedded.walledcitymod;

import com.electrifiedded.archaiclib.legacy.world.LegacyWorld;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

public class WorldGenWalledCity extends WorldGeneratorThread {
   private static final int GATE_HEIGHT = 6;
   private static final int JMEAN_DEVIATION_SLOPE = 10;
   private static final int LEVELLING_DEVIATION_SLOPE = 18;
   private static final int MIN_SIDE_LENGTH = 10;
   private static final float MAX_WATER_PERCENTAGE = 0.4F;
   private TemplateWall ows;
   private TemplateWall sws;
   private BuildingWall[] walls;
   private int axXHand;
   private int[] dir = null;
   private int Lmean;
   private int jmean;
   private final int cityType;
   private int[] corner1;
   private int[] corner2;
   private int[] mincorner;
   public int[][] layout;

   public WorldGenWalledCity(PopulatorWalledCity wc, World world, Random random, int chunkI, int chunkK, int triesPerChunk, double chunkTryProb) {
      super(wc, world, random, chunkI, chunkK, triesPerChunk, chunkTryProb);
      this.cityType = world.provider.getDimension();
   }

   @Override
   public boolean generate(int i0, int j0, int k0) {
      this.ows = TemplateWall.pickBiomeWeightedWallStyle(((PopulatorWalledCity)this.master).cityStyles, this.world, i0, k0, this.world.rand, false);
      if (this.ows == null) {
         return false;
      } else {
         this.sws = TemplateWall.pickBiomeWeightedWallStyle(this.ows.streets, this.world, i0, k0, this.world.rand, false);
         if (this.sws == null) {
            return false;
         } else if (!((PopulatorWalledCity)this.master).cityIsSeparated(this.world, i0, k0, this.cityType)) {
            return false;
         } else {
            int ID = (this.random.nextInt(9000) + 1000) * 100;
            int minJ = this.ows.LevelInterior ? 62 : -1;
            this.chooseDirection(i0 >> 4, k0 >> 4);
            if (this.ows.MinL < 40) {
               this.ows.MinL = 40;
            }

            this.walls = new BuildingWall[4];
            this.ows.setFixedRules(this.world.rand);
            this.walls[0] = new BuildingWall(
                  ID, this, this.ows, this.dir[0], this.axXHand, this.ows.MinL + this.random.nextInt(this.ows.MaxL - this.ows.MinL), false, i0, j0, k0
               )
               .setMinJ(minJ);
            this.walls[0].plan(1, 0, 5, true);
            if (this.walls[0].bLength < this.ows.MinL) {
               return false;
            } else {
               this.walls[0].setCursor(this.walls[0].bLength - 1);
               this.walls[1] = new BuildingWall(
                     ID + 1,
                     this,
                     this.ows,
                     this.dir[1],
                     this.axXHand,
                     this.ows.MinL + this.random.nextInt(this.ows.MaxL - this.ows.MinL),
                     false,
                     this.walls[0].getIJKPt(-1 - this.ows.TowerXOffset, 0, 1 + this.ows.TowerXOffset)
                  )
                  .setTowers(this.walls[0])
                  .setMinJ(minJ);
               if (!((PopulatorWalledCity)this.master).cityIsSeparated(this.world, this.walls[1].i1, this.walls[1].k1, this.cityType)) {
                  return false;
               } else {
                  this.walls[1].plan(1, 0, 5, false);
                  if (this.walls[1].bLength < this.ows.MinL) {
                     return false;
                  } else {
                     this.walls[1].setCursor(this.walls[1].bLength - 1);
                     int distToTarget = this.walls[0].bLength + this.walls[1].xArray[this.walls[1].bLength - 1];
                     if (distToTarget < 10) {
                        return false;
                     } else {
                        this.walls[2] = new BuildingWall(
                              ID + 2,
                              this,
                              this.ows,
                              this.dir[2],
                              this.axXHand,
                              distToTarget + 2,
                              false,
                              this.walls[1].getIJKPt(-1 - this.ows.TowerXOffset, 0, 1 + this.ows.TowerXOffset)
                           )
                           .setTowers(this.walls[0])
                           .setMinJ(minJ);
                        if (!((PopulatorWalledCity)this.master).cityIsSeparated(this.world, this.walls[2].i1, this.walls[2].k1, this.cityType)) {
                           return false;
                        } else {
                           this.walls[2].setCursor(0);
                           this.walls[2].setTarget(this.walls[2].getIJKPt(0, 0, distToTarget));
                           this.walls[2].plan(1, 0, 5, false);
                           if (this.walls[2].bLength < this.walls[2].y_targ) {
                              return false;
                           } else {
                              this.walls[2].setCursor(this.walls[2].bLength - 1);
                              distToTarget = this.walls[1].bLength
                                 - this.walls[0].xArray[this.walls[0].bLength - 1]
                                 + this.walls[1].xArray[this.walls[1].bLength - 1];
                              if (distToTarget < 10) {
                                 return false;
                              } else {
                                 this.walls[3] = new BuildingWall(
                                       ID + 3,
                                       this,
                                       this.ows,
                                       this.dir[3],
                                       this.axXHand,
                                       distToTarget + 2,
                                       false,
                                       this.walls[2].getIJKPt(-1 - this.ows.TowerXOffset, 0, 1 + this.ows.TowerXOffset)
                                    )
                                    .setTowers(this.walls[0])
                                    .setMinJ(minJ);
                                 if (!((PopulatorWalledCity)this.master).cityIsSeparated(this.world, this.walls[3].i1, this.walls[3].k1, this.cityType)) {
                                    return false;
                                 } else {
                                    this.walls[0].setCursor(0);
                                    this.walls[3].setCursor(0);
                                    this.walls[3].setTarget(this.walls[0].getIJKPt(-1 - this.ows.TowerXOffset, 0, -1 - this.ows.TowerXOffset));
                                    this.walls[3].plan(1, 0, 5, false);
                                    if (this.walls[3].bLength < this.walls[3].y_targ) {
                                       return false;
                                    } else {
                                       for (BuildingWall w : this.walls) {
                                          w.smooth(this.ows.ConcaveDownSmoothingScale, this.ows.ConcaveUpSmoothingScale, true);
                                       }

                                       int[] xmax = new int[4];

                                       for (int w = 0; w < 4; w++) {
                                          xmax[w] = 0;

                                          for (int n = 0; n < this.walls[w].bLength; n++) {
                                             if (this.walls[w].xArray[n] > xmax[w]) {
                                                xmax[w] = this.walls[w].xArray[n];
                                             }
                                          }
                                       }

                                       for (BuildingWall w : this.walls) {
                                          w.setCursor(0);
                                       }

                                       this.corner1 = this.walls[1]
                                          .getIJKPt(
                                             xmax[1] + this.walls[1].bWidth + 1,
                                             0,
                                             this.walls[0].xArray[this.walls[0].bLength - 1] - xmax[0] - this.walls[0].bWidth - 2
                                          );
                                       this.corner2 = this.walls[3]
                                          .getIJKPt(
                                             xmax[3] + this.walls[3].bWidth + 1,
                                             0,
                                             this.walls[2].xArray[this.walls[2].bLength - 1] - xmax[2] - this.walls[2].bWidth - 2
                                          );
                                       this.mincorner = new int[]{Math.min(this.corner1[0], this.corner2[0]), 0, Math.min(this.corner1[2], this.corner2[2])};
                                       this.Lmean = (this.walls[0].bLength + this.walls[1].bLength + this.walls[2].bLength + this.walls[3].bLength) / 4;
                                       this.jmean = 0;

                                       for (BuildingWall w : this.walls) {
                                          for (int nx = 0; nx < w.bLength; nx++) {
                                             this.jmean = this.jmean + w.zArray[nx] + w.j1;
                                          }
                                       }

                                       this.jmean = this.jmean / (this.Lmean * 4);

                                       for (BuildingWall w : this.walls) {
                                          if (Math.abs(w.j1 - this.jmean) > w.bLength / 10) {
                                             this.master
                                                .logOrPrint(
                                                   "Rejected city " + ID + ", height at corner differed from mean by " + Math.abs(w.j1 - this.jmean) + ".",
                                                   "INFO"
                                                );
                                             return false;
                                          }
                                       }

                                       int cityArea = 0;
                                       int waterArea = 0;
                                       int incI = Building.signum(this.corner2[0] - this.corner1[0], 0);
                                       int incK = Building.signum(this.corner2[2] - this.corner1[2], 0);

                                       for (int i2 = this.corner1[0]; (this.corner2[0] - i2) * incI > 0; i2 += incI) {
                                          for (int k2 = this.corner1[2]; (this.corner2[2] - k2) * incK > 0; k2 += incK) {
                                             boolean enclosed = true;

                                             for (BuildingWall wx : this.walls) {
                                                if (wx.ptIsToXHand(new int[]{i2, 0, k2}, 1)) {
                                                   enclosed = false;
                                                }
                                             }

                                             if (enclosed) {
                                                int j2 = Building.findSurfaceJ(this.world, i2, k2, 255, true, 3);
                                                cityArea++;
                                                if (j2 == -666) {
                                                   waterArea++;
                                                }

                                                if (((PopulatorWalledCity)this.master).RejectOnPreexistingArtifacts
                                                   && this.ows.LevelInterior
                                                   && BlockProperties.get(GenAccess.getBlock(this.world, i2, j2, k2)).isArtificial) {
                                                   this.master
                                                      .logOrPrint(
                                                         "Rejected " + this.ows.name + " city " + ID + ", found previous construction in city zone!", "WARNING"
                                                      );
                                                   return false;
                                                }
                                             }
                                          }
                                       }

                                       if (!this.ows.LevelInterior && (float)waterArea / cityArea > 0.4F) {
                                          this.master
                                             .logOrPrint(
                                                "Rejected "
                                                   + this.ows.name
                                                   + " city "
                                                   + ID
                                                   + ", too much water! City area was "
                                                   + 100.0F * waterArea / cityArea
                                                   + "% water!",
                                                "INFO"
                                             );
                                          return false;
                                       } else {
                                          for (BuildingWall wxx : this.walls) {
                                             if (!((PopulatorWalledCity)this.master).cityIsSeparated(this.world, wxx.i1, wxx.k1, this.cityType)) {
                                                this.master.logOrPrint("Rejected city " + ID + " nearby city was built during planning!", "WARNING");
                                                return false;
                                             }
                                          }

                                          this.walls[0].setCursor(0);
                                          int[] cityCenter = new int[]{
                                             (this.walls[0].i1 + this.walls[1].i1 + this.walls[2].i1 + this.walls[3].i1) / 4,
                                             0,
                                             (this.walls[0].k1 + this.walls[1].k1 + this.walls[2].k1 + this.walls[3].k1) / 4
                                          };
                                          cityCenter[1] = Building.findSurfaceJ(this.world, cityCenter[0], cityCenter[1], 255, false, 3);
                                          ((PopulatorWalledCity)this.master)
                                             .cityLocations
                                             .get(this.world)
                                             .add(new int[]{cityCenter[0], cityCenter[2], this.cityType});
                                          ((PopulatorWalledCity)this.master).saveCityLocations(this.world);
                                          ((PopulatorWalledCity)this.master)
                                             .chatBuildingCity(
                                                "** Building city... **",
                                                "\n***** Building "
                                                   + this.ows.name
                                                   + " city"
                                                   + ", ID="
                                                   + ID
                                                   + " in "
                                                   + com.electrifiedded.archaiclib.legacy.world.LegacyBiomes.getName(Compat.biome(this.world, this.walls[0].i1, this.walls[0].k1))
                                                   + " biome between "
                                                   + this.walls[0].localCoordString(0, 0, 0)
                                                   + " and "
                                                   + this.walls[2].localCoordString(0, 0, 0)
                                                   + " ******\n"
                                             );
                                          if (this.ows.LevelInterior) {
                                             this.levelCity();
                                          }

                                          TemplateWall avenueWS = TemplateWall.pickBiomeWeightedWallStyle(
                                             this.ows.streets, this.world, i0, k0, this.world.rand, false
                                          );
                                          LinkedList<BuildingWall> radialAvenues = new LinkedList<>();
                                          this.layout = new int[Math.abs(this.corner1[0] - this.corner2[0])][Math.abs(this.corner1[2] - this.corner2[2])];

                                          for (int x = 0; x < this.layout.length; x++) {
                                             for (int y = 0; y < this.layout[0].length; y++) {
                                                this.layout[x][y] = 0;
                                             }
                                          }

                                          for (BuildingWall wxxx : this.walls) {
                                             wxxx.setLayoutCode(1);
                                          }

                                          int gateFlankingTowers = 0;

                                          for (BuildingWall wxxx : this.walls) {
                                             wxxx.endBLength = 0;
                                             wxxx.buildFromTML();
                                             int radialAvenueHand = wxxx.bDir != this.dir[0] && wxxx.bDir != this.dir[1] ? 1 : -1;
                                             int startScan = wxxx.getY(cityCenter) + (radialAvenueHand == wxxx.bHand ? avenueWS.WWidth - 1 : 0);
                                             BuildingWall[] avenues = wxxx.buildGateway(
                                                new int[]{wxxx.bLength / 4, 3 * wxxx.bLength / 4},
                                                startScan,
                                                6,
                                                avenueWS.WWidth,
                                                avenueWS,
                                                this.random.nextInt(6) < gateFlankingTowers ? 0 : this.axXHand,
                                                500,
                                                null,
                                                -this.axXHand,
                                                150,
                                                cityCenter,
                                                radialAvenueHand
                                             );
                                             wxxx.makeBuildings(this.axXHand == -1, this.axXHand == 1, true, false, false);
                                             if (wxxx.gatewayStart != -1) {
                                                gateFlankingTowers++;
                                             }

                                             if (avenues != null) {
                                                avenues[0].buildFromTML();
                                                radialAvenues.add(avenues[1]);
                                             } else {
                                                wxxx.setCursor(startScan);
                                                BuildingWall radialAvenue = new BuildingWall(
                                                   0,
                                                   this,
                                                   this.sws,
                                                   Building.rotDir(wxxx.bDir, -this.axXHand),
                                                   radialAvenueHand,
                                                   this.ows.MaxL,
                                                   false,
                                                   wxxx.getSurfaceIJKPt(-1, 0, 255, false, -1)
                                                );
                                                radialAvenue.setTarget(cityCenter);
                                                radialAvenue.plan(1, 0, 5, true);
                                                if (radialAvenue.bLength > 20) {
                                                   radialAvenue.smooth(10, 10, true);
                                                   radialAvenues.add(radialAvenue);
                                                }
                                             }
                                          }

                                          for (BuildingWall wxxx : this.walls) {
                                             wxxx.setCursor(0);
                                          }

                                          if (this.ows.MakeEndTowers) {
                                             for (int wxxx = 0; wxxx < 4; wxxx++) {
                                                if (this.walls[(wxxx + 3) % 4].bLength > 2) {
                                                   int zmean = (
                                                         this.walls[wxxx].zArray[2]
                                                            - this.walls[wxxx].j1
                                                            + this.walls[(wxxx + 3) % 4].zArray[this.walls[(wxxx + 3) % 4].bLength - 3]
                                                            + this.walls[(wxxx + 3) % 4].j1
                                                      )
                                                      / 2;
                                                   int minCornerWidth = this.ows.WWidth + 2 + (this.ows.TowerXOffset < 0 ? 2 * this.ows.TowerXOffset : 0);
                                                   int TWidth = this.ows.getTMaxWidth(this.walls[wxxx].circular) < minCornerWidth
                                                      ? minCornerWidth
                                                      : this.ows.getTMaxWidth(this.walls[wxxx].circular);
                                                   BuildingTower tower = new BuildingTower(
                                                      ID + 10 + wxxx,
                                                      this.walls[wxxx],
                                                      this.dir[(wxxx + 2) % 4],
                                                      -this.axXHand,
                                                      false,
                                                      TWidth,
                                                      this.ows.getTMaxHeight(this.walls[wxxx].circular),
                                                      TWidth,
                                                      this.walls[wxxx].getIJKPt(-2 - (this.ows.TowerXOffset < 0 ? this.ows.TowerXOffset : 0), zmean, 2)
                                                   );
                                                   this.setLayoutCode(tower.getIJKPt(0, 0, 0), tower.getIJKPt(TWidth - 1, 0, TWidth - 1), 4);
                                                   tower.build(0, 0, true);
                                                }
                                             }
                                          }

                                          boolean cityIsDense = this.ows.StreetDensity >= 15;
                                          LinkedList<BuildingDoubleWall> crossAvenues = new LinkedList<>();
                                          int avInterval = cityIsDense ? 60 : (this.Lmean > 110 ? 35 : 20);
                                          int maxStreetCount = this.Lmean * this.ows.StreetDensity / 20;

                                          for (BuildingWall radialAvenue : radialAvenues) {
                                             for (int nx = radialAvenue.bLength - avInterval; nx >= 20; nx -= avInterval) {
                                                radialAvenue.setCursor(nx);
                                                BuildingDoubleWall crossAvenue = new BuildingDoubleWall(
                                                   ID, this, this.sws, Building.rotDir(radialAvenue.bDir, 1), 1, radialAvenue.getIJKPt(0, 0, 0)
                                                );
                                                if (crossAvenue.plan()) {
                                                   crossAvenues.add(crossAvenue);
                                                }
                                             }

                                             radialAvenue.setLayoutCode(2);
                                          }

                                          for (BuildingWall avenue : radialAvenues) {
                                             avenue.buildFromTML();
                                          }

                                          LinkedList<BuildingDoubleWall> plannedStreets = new LinkedList<>();

                                          for (int tries = 0; tries < maxStreetCount; tries++) {
                                             int[] pt = this.randInteriorPoint();
                                             if (pt != null) {
                                                pt[1]++;
                                                this.sws = TemplateWall.pickBiomeWeightedWallStyle(
                                                   this.ows.streets, this.world, i0, k0, this.world.rand, true
                                                );
                                                if (pt[1] != -1) {
                                                   BuildingDoubleWall street = new BuildingDoubleWall(ID + tries, this, this.sws, this.random.nextInt(4), 1, pt);
                                                   if (street.plan()) {
                                                      plannedStreets.add(street);
                                                   }
                                                }
                                             }
                                          }

                                          for (BuildingDoubleWall avenue : crossAvenues) {
                                             avenue.build(2);
                                          }

                                          for (BuildingDoubleWall street : plannedStreets) {
                                             street.build(3);
                                          }

                                          for (BuildingWall avenue : radialAvenues) {
                                             avenue.makeBuildings(true, true, false, cityIsDense, true);
                                          }

                                          for (BuildingDoubleWall avenue : crossAvenues) {
                                             avenue.buildTowers(true, true, false, cityIsDense, true);
                                          }

                                          for (BuildingDoubleWall street : plannedStreets) {
                                             street.buildTowers(true, true, this.sws.MakeGatehouseTowers, cityIsDense, false);
                                          }

                                          ((PopulatorWalledCity)this.master).chatCityBuilt(new int[]{i0, j0, k0, this.cityType, this.Lmean / 2 + 40});
                                          ((PopulatorWalledCity)this.master).addCityToVillages(this.world, ID);
                                          this.layout = (int[][])null;
                                          this.walls = null;
                                          return true;
                                       }
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   @Override
   public boolean isLayoutGenerator() {
      return true;
   }

   @Override
   public boolean layoutIsClear(Building building, boolean[][] templateLayout, int layoutCode) {
      for (int y = 0; y < templateLayout.length; y++) {
         for (int x = 0; x < templateLayout[0].length; x++) {
            if (templateLayout[y][x]) {
               int i = building.getI(x, y);
               int k = building.getK(x, y);
               if (i >= this.mincorner[0]
                  && k >= this.mincorner[2]
                  && i - this.mincorner[0] < this.layout.length
                  && k - this.mincorner[2] < this.layout[0].length
                  && LAYOUT_CODE_OVERRIDE_MATRIX[this.layout[i - this.mincorner[0]][k - this.mincorner[2]]][layoutCode] == 0) {
                  return false;
               }
            }
         }
      }

      return true;
   }

   @Override
   public boolean layoutIsClear(int[] pt1, int[] pt2, int layoutCode) {
      for (int i = Math.min(pt1[0], pt2[0]); i <= Math.max(pt1[0], pt2[0]); i++) {
         for (int k = Math.min(pt1[2], pt2[2]); k <= Math.max(pt1[2], pt2[2]); k++) {
            if (i >= this.mincorner[0]
               && k >= this.mincorner[2]
               && i - this.mincorner[0] < this.layout.length
               && k - this.mincorner[2] < this.layout[0].length
               && LAYOUT_CODE_OVERRIDE_MATRIX[this.layout[i - this.mincorner[0]][k - this.mincorner[2]]][layoutCode] == 0) {
               return false;
            }
         }
      }

      return true;
   }

   @Override
   public void setLayoutCode(Building building, boolean[][] templateLayout, int layoutCode) {
      for (int y = 0; y < templateLayout.length; y++) {
         for (int x = 0; x < templateLayout[0].length; x++) {
            if (templateLayout[y][x]) {
               int i = building.getI(x, y);
               int k = building.getK(x, y);
               if (i >= this.mincorner[0]
                  && k >= this.mincorner[2]
                  && i - this.mincorner[0] < this.layout.length
                  && k - this.mincorner[2] < this.layout[0].length) {
                  this.layout[i - this.mincorner[0]][k - this.mincorner[2]] = layoutCode;
               }
            }
         }
      }
   }

   @Override
   public void setLayoutCode(int[] pt1, int[] pt2, int layoutCode) {
      for (int i = Math.min(pt1[0], pt2[0]); i <= Math.max(pt1[0], pt2[0]); i++) {
         for (int k = Math.min(pt1[2], pt2[2]); k <= Math.max(pt1[2], pt2[2]); k++) {
            if (i >= this.mincorner[0] && k >= this.mincorner[2] && i - this.mincorner[0] < this.layout.length && k - this.mincorner[2] < this.layout[0].length
               )
             {
               this.layout[i - this.mincorner[0]][k - this.mincorner[2]] = layoutCode;
            }
         }
      }
   }

   private void chooseDirection(int chunkI, int chunkK) {
      boolean[] exploredChunk = new boolean[]{
         GenAccess.blockExists(this.world, chunkI << 4, 0, chunkK - 1 << 4),
         GenAccess.blockExists(this.world, chunkI + 1 << 4, 0, chunkK << 4),
         GenAccess.blockExists(this.world, chunkI << 4, 0, chunkK + 1 << 4),
         GenAccess.blockExists(this.world, chunkI - 1 << 4, 0, chunkK << 4)
      };
      this.dir = new int[4];
      int randDir = this.random.nextInt(4);
      this.dir[0] = (randDir + 1) % 4;

      while (this.dir[0] != randDir && !exploredChunk[this.dir[0]]) {
         this.dir[0] = (this.dir[0] + 1) % 4;
      }

      this.axXHand = exploredChunk[(this.dir[0] + 1) % 4] ? -1 : 1;
      this.dir[1] = (this.dir[0] - this.axXHand + 4) % 4;
      this.dir[2] = (this.dir[1] - this.axXHand + 4) % 4;
      this.dir[3] = (this.dir[2] - this.axXHand + 4) % 4;
   }

   private void levelCity() {
      for (BuildingWall w : this.walls) {
         w.setCursor(0);
      }

      int incI = Building.signum(this.corner2[0] - this.corner1[0], 0);
      int incK = Building.signum(this.corner2[2] - this.corner1[2], 0);
      int[] pt = new int[3];
      int jmin = Compat.waterVaporizes(this.world) ? this.jmean : Math.max(this.jmean, 63);

      for (BuildingWall w : this.walls) {
         for (int n = 0; n < w.bLength; n++) {
            if (w.zArray[n] + w.j1 + w.WalkHeight - 1 < jmin && (Compat.waterVaporizes(this.world) || jmin >= 63)) {
               jmin = w.zArray[n] + w.j1 + w.WalkHeight - 1;
            }
         }
      }

      int jmax = Math.max(this.jmean + this.Lmean / 18, jmin);

      for (pt[0] = this.corner1[0]; (this.corner2[0] - pt[0]) * incI > 0; pt[0] += incI) {
         for (pt[2] = this.corner1[2]; (this.corner2[2] - pt[2]) * incK > 0; pt[2] += incK) {
            boolean enclosed = true;

            for (BuildingWall w : this.walls) {
               if (w.ptIsToXHand(pt, 1)) {
                  enclosed = false;
               }
            }

            if (enclosed) {
               pt[1] = Building.findSurfaceJ(this.world, pt[0], pt[2], 255, false, -1);
               Block oldSurfaceBlockId = GenAccess.getBlock(this.world, pt[0], pt[1], pt[2]);
               if (pt[1] > jmax) {
                  while (!GenAccess.isAirBlock(this.world, pt[0], pt[1] + 1, pt[2])) {
                     pt[1]++;
                  }

                  for (pt[1] += 10; pt[1] > jmax; pt[1]--) {
                     if (!GenAccess.isAirBlock(this.world, pt[0], pt[1], pt[2])) {
                        Building.setBlockAndMetaNoLighting(this.world, pt[0], pt[1], pt[2], Blocks.AIR, 0);
                     }
                  }

                  if (!GenAccess.isAirBlock(this.world, pt[0], jmax - 1, pt[2])) {
                     Building.setBlockAndMetaNoLighting(this.world, pt[0], jmax, pt[2], oldSurfaceBlockId, 0);
                  }
               }

               if (pt[1] < jmin) {
                  Building.fillDown(pt, jmin, this.world);
               }
            }
         }
      }

      for (int chunkI = this.corner1[0] >> 4; ((this.corner2[0] >> 4) - chunkI) * incI > 0; chunkI += incI) {
         for (int chunkK = this.corner1[2] >> 4; ((this.corner2[2] >> 4) - chunkK) * incK > 0; chunkK += incK) {
            if (!GenAccess.staged()) this.world.getChunk(chunkI, chunkK).generateSkylightMap();
         }
      }
   }

   private void printLayout(File f) {
      try {
         PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(f)));
         pw.println("  +y   ");
         pw.println("   ^   ");
         pw.println("+x<.>-x");
         pw.println("   v   ");
         pw.println("  -y   ");
         pw.println();

         for (int y = this.layout[0].length - 1; y >= 0; y--) {
            for (int x = this.layout.length - 1; x >= 0; x--) {
               pw.print(LAYOUT_CODE_TO_CHAR[this.layout[x][y]]);
            }

            pw.println();
         }

         pw.close();
      } catch (Exception var5) {
      }
   }

   private int[] randInteriorPoint() {
      int tries = 0;
      int[] pt = new int[3];
      this.master
         .logOrPrint(
            "Finding random interior point for city seeded at corner ("
               + this.walls[0].i1
               + ","
               + this.walls[0].j1
               + ","
               + this.walls[0].k1
               + ")"
               + this.walls[0].IDString(),
            "FINE"
         );

      while (tries < 20) {
         pt[0] = this.mincorner[0] + this.random.nextInt(Math.abs(this.corner1[0] - this.corner2[0]));
         pt[2] = this.mincorner[2] + this.random.nextInt(Math.abs(this.corner1[2] - this.corner2[2]));
         pt[1] = Building.findSurfaceJ(this.world, pt[0], pt[2], 255, true, 3);
         boolean enclosed = true;

         for (BuildingWall w : this.walls) {
            if (w.ptIsToXHand(pt, -this.sws.WWidth)) {
               enclosed = false;
            }
         }

         if (enclosed) {
            return pt;
         }

         tries++;
      }

      this.master.logOrPrint("Could not find point within bounds!", "WARNING");
      return null;
   }
}
