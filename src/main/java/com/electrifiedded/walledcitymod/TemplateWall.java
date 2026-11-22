package com.electrifiedded.walledcitymod;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

public class TemplateWall extends TemplateTML {
   public static final String BUILDING_DIRECTORY_NAME = "buildings";
   public static final int[] ALL_BIOMES = null;
   public static final int NO_RULE = -1;
   public static final int MAX_STREET_DENSITY = 20;
   public TemplateTML makeDefaultTower;
   public TemplateTML makeCARuin;
   public int[] Biomes = ALL_BIOMES;
   public boolean underground = false;
   public List<TemplateTML> buildings = null;
   public List<TemplateWall> streets = null;
   public int[][] buildingWeights;
   public int StreetDensity = 6;
   public boolean LevelInterior = true;
   public int WHeight = 7;
   public int WWidth = 5;
   public int WalkHeight = 0;
   public int MinL = 15;
   public int MaxL = 1000;
   public TemplateRule TowerRule = TemplateRule.RULE_NOT_PROVIDED;
   public TemplateRule SpawnerRule = TemplateRule.RULE_NOT_PROVIDED;
   public TemplateRule ChestRule = TemplateRule.RULE_NOT_PROVIDED;
   public TemplateRule CARuinRule = TemplateRule.RULE_NOT_PROVIDED;
   public boolean MakeBuildings = true;
   public boolean MergeWalls = false;
   public boolean MakeEndTowers = true;
   public boolean MakeGatehouseTowers = true;
   public boolean MakeUndergroundEntranceways = true;
   public boolean PopulateFurniture = false;
   public boolean MakeDoors = false;
   public int LateralSmoothingScale = 20;
   public int ConcaveUpSmoothingScale = 20;
   public int ConcaveDownSmoothingScale = 10;
   public int BuildingInterval = 75;
   private int DefaultTowerWeight = 1;
   public int TowerXOffset = 0;
   public float CircularProb = 0.3F;
   private int SqrMinHeight = 11;
   private int SqrMaxHeight = 15;
   private int SqrMinWidth = 7;
   private int SqrMaxWidth = 7;
   private int CircMinHeight = 11;
   private int CircMaxHeight = 15;
   private int CircMinWidth = 7;
   private int CircMaxWidth = 7;
   private int[] SqrRoofStyles = new int[]{4, 1, 1, 1, 1, 0, 0};
   private int[] CircRoofStyles = new int[]{3, 0, 0, 0, 1, 1, 0};
   private TemplateRule SqrRoofRule = null;
   private TemplateRule CircRoofRule = null;
   public int CARuinWeight = 0;
   public int CARuinContainerWidth = 15;
   public int CARuinMinHeight = 20;
   public int CARuinMaxHeight = 35;
   ArrayList<byte[][]> CARuinAutomataRules = null;

   public TemplateWall(File wallFile, HashMap<String, TemplateTML> buildingTemplateMap, BuildingExplorationHandler beh) throws Exception {
      super(wallFile, beh);
      this.readTowerParameters();
      this.buildings = this.loadChildTemplates("building_templates", buildingTemplateMap);
      this.makeCARuin = new TemplateTML(2, this.CARuinWeight);
      this.buildings.add(this.makeCARuin);
      this.makeDefaultTower = new TemplateTML(1, this.DefaultTowerWeight);
      this.buildings.add(this.makeDefaultTower);

      try {
         this.buildingWeights = TemplateTML.buildWeightsAndIndex(this.buildings);
      } catch (Exception var5) {
         if (var5 != ZERO_WEIGHT_EXCEPTION) {
            throw var5;
         }

         this.buildings.get(this.buildings.size() - 1).weight = 1;
         this.buildingWeights = TemplateTML.buildWeightsAndIndex(this.buildings);
      }
   }

   public void readTowerParameters() throws Exception {
      float mobProb = 0.0F;
      float pigZombieProb = 0.0F;
      float endermanProb = 0.0F;
      float caveSpiderProb = 0.0F;
      if (this.extraOptions.containsKey("biomes")) {
         this.Biomes = BuildingExplorationHandler.readNamedCheckList(
            this.lw, this.Biomes, "=", this.extraOptions.get("biomes"), BuildingExplorationHandler.BIOME_NAMES, "ALL"
         );
      }

      if (this.extraOptions.containsKey("street_density")) {
         this.StreetDensity = BuildingExplorationHandler.readIntParam(this.lw, this.StreetDensity, "=", this.extraOptions.get("street_density"));
      }

      if (this.extraOptions.containsKey("level_interior")) {
         this.LevelInterior = BuildingExplorationHandler.readIntParam(this.lw, 1, "=", this.extraOptions.get("level_interior")) == 1;
      }

      if (this.extraOptions.containsKey("walk_height")) {
         this.WalkHeight = BuildingExplorationHandler.readIntParam(this.lw, this.WalkHeight, "=", this.extraOptions.get("walk_height"));
      }

      if (this.extraOptions.containsKey("min_length")) {
         this.MinL = BuildingExplorationHandler.readIntParam(this.lw, this.MinL, "=", this.extraOptions.get("min_length"));
      }

      if (this.extraOptions.containsKey("max_length")) {
         this.MaxL = BuildingExplorationHandler.readIntParam(this.lw, this.MaxL, "=", this.extraOptions.get("max_length"));
      }

      if (this.extraOptions.containsKey("tower_rule")) {
         this.TowerRule = this.explorationHandler.readRuleIdOrRule("=", this.extraOptions.get("tower_rule"), this.rules);
      }

      if (this.extraOptions.containsKey("building_interval")) {
         this.BuildingInterval = BuildingExplorationHandler.readIntParam(this.lw, this.BuildingInterval, "=", this.extraOptions.get("building_interval"));
      }

      if (this.extraOptions.containsKey("make_buildings")) {
         this.MakeBuildings = BuildingExplorationHandler.readIntParam(this.lw, 1, "=", this.extraOptions.get("make_buildings")) == 1;
      }

      if (this.extraOptions.containsKey("make_gatehouse_towers")) {
         this.MakeGatehouseTowers = BuildingExplorationHandler.readIntParam(this.lw, 1, "=", this.extraOptions.get("make_gatehouse_towers")) == 1;
      }

      if (this.extraOptions.containsKey("make_end_towers")) {
         this.MakeEndTowers = BuildingExplorationHandler.readIntParam(this.lw, 1, "=", this.extraOptions.get("make_end_towers")) == 1;
      }

      if (this.extraOptions.containsKey("make_underground_entranceways")) {
         this.MakeUndergroundEntranceways = BuildingExplorationHandler.readIntParam(this.lw, 1, "=", this.extraOptions.get("make_underground_entranceways"))
            == 1;
      }

      if (this.extraOptions.containsKey("merge_walls")) {
         this.MergeWalls = BuildingExplorationHandler.readIntParam(this.lw, 0, "=", this.extraOptions.get("merge_walls")) == 1;
      }

      if (this.extraOptions.containsKey("lateral_smoothing_scale")) {
         this.LateralSmoothingScale = BuildingExplorationHandler.readIntParam(
            this.lw, this.LateralSmoothingScale, "=", this.extraOptions.get("lateral_smoothing_scale")
         );
      }

      if (this.extraOptions.containsKey("concave_up_smoothing_scale")) {
         this.ConcaveUpSmoothingScale = BuildingExplorationHandler.readIntParam(
            this.lw, this.ConcaveUpSmoothingScale, "=", this.extraOptions.get("concave_up_smoothing_scale")
         );
      }

      if (this.extraOptions.containsKey("concave_down_smoothing_scale")) {
         this.ConcaveDownSmoothingScale = BuildingExplorationHandler.readIntParam(
            this.lw, this.ConcaveDownSmoothingScale, "=", this.extraOptions.get("concave_down_smoothing_scale")
         );
      }

      if (this.extraOptions.containsKey("default_tower_weight")) {
         this.DefaultTowerWeight = BuildingExplorationHandler.readIntParam(this.lw, this.DefaultTowerWeight, "=", this.extraOptions.get("default_tower_weight"));
      }

      if (this.extraOptions.containsKey("tower_offset")) {
         this.TowerXOffset = BuildingExplorationHandler.readIntParam(this.lw, this.TowerXOffset, "=", this.extraOptions.get("tower_offset"));
      }

      if (this.extraOptions.containsKey("spawner_rule")) {
         this.SpawnerRule = this.explorationHandler.readRuleIdOrRule("=", this.extraOptions.get("spawner_rule"), this.rules);
      }

      if (this.extraOptions.containsKey("populate_furniture")) {
         this.PopulateFurniture = BuildingExplorationHandler.readFloatParam(this.lw, 0.0F, "=", this.extraOptions.get("populate_furniture")) == 1.0F;
      }

      if (this.extraOptions.containsKey("make_doors")) {
         this.MakeDoors = BuildingExplorationHandler.readFloatParam(this.lw, 0.0F, "=", this.extraOptions.get("make_doors")) == 1.0F;
      }

      if (this.extraOptions.containsKey("circular_probability")) {
         this.CircularProb = BuildingExplorationHandler.readFloatParam(this.lw, this.CircularProb, "=", this.extraOptions.get("circular_probability"));
      }

      if (this.extraOptions.containsKey("chest_rule")) {
         this.ChestRule = this.explorationHandler.readRuleIdOrRule("=", this.extraOptions.get("chest_rule"), this.rules);
      }

      if (this.extraOptions.containsKey("square_min_height")) {
         this.SqrMinHeight = BuildingExplorationHandler.readIntParam(this.lw, this.SqrMinHeight, "=", this.extraOptions.get("square_min_height"));
      }

      if (this.extraOptions.containsKey("square_max_height")) {
         this.SqrMaxHeight = BuildingExplorationHandler.readIntParam(this.lw, this.SqrMaxHeight, "=", this.extraOptions.get("square_max_height"));
      }

      if (this.extraOptions.containsKey("square_min_width")) {
         this.SqrMinWidth = BuildingExplorationHandler.readIntParam(this.lw, this.SqrMinWidth, "=", this.extraOptions.get("square_min_width"));
      }

      if (this.extraOptions.containsKey("square_max_width")) {
         this.SqrMaxWidth = BuildingExplorationHandler.readIntParam(this.lw, this.SqrMaxWidth, "=", this.extraOptions.get("square_max_width"));
      }

      if (this.extraOptions.containsKey("square_roof_styles")) {
         this.SqrRoofStyles = BuildingExplorationHandler.readNamedCheckList(
            this.lw, this.SqrRoofStyles, "=", this.extraOptions.get("square_roof_styles"), BuildingTower.ROOFSTYLE_NAMES, ""
         );
      }

      if (this.extraOptions.containsKey("square_roof_rule")) {
         this.SqrRoofRule = this.explorationHandler.readRuleIdOrRule("=", this.extraOptions.get("square_roof_rule"), this.rules);
      }

      if (this.extraOptions.containsKey("circular_tower_min_height")) {
         this.CircMinHeight = BuildingExplorationHandler.readIntParam(this.lw, this.CircMinHeight, "=", this.extraOptions.get("circular_tower_min_height"));
      }

      if (this.extraOptions.containsKey("circular_tower_max_height")) {
         this.CircMaxHeight = BuildingExplorationHandler.readIntParam(this.lw, this.CircMaxHeight, "=", this.extraOptions.get("circular_tower_max_height"));
      }

      if (this.extraOptions.containsKey("circular_tower_min_width")) {
         this.CircMinWidth = BuildingExplorationHandler.readIntParam(this.lw, this.CircMinWidth, "=", this.extraOptions.get("circular_tower_min_width"));
      }

      if (this.extraOptions.containsKey("circular_tower_max_width")) {
         this.CircMaxWidth = BuildingExplorationHandler.readIntParam(this.lw, this.CircMaxWidth, "=", this.extraOptions.get("circular_tower_max_width"));
      }

      if (this.extraOptions.containsKey("circular_tower_roof_styles")) {
         this.CircRoofStyles = BuildingExplorationHandler.readNamedCheckList(
            this.lw, this.CircRoofStyles, "=", this.extraOptions.get("circular_tower_roof_styles"), BuildingTower.ROOFSTYLE_NAMES, ""
         );
      }

      if (this.extraOptions.containsKey("circular_tower_roof_rule")) {
         this.CircRoofRule = this.explorationHandler.readRuleIdOrRule("=", this.extraOptions.get("circular_tower_roof_rule"), this.rules);
      }

      if (this.extraOptions.containsKey("mob_probability")) {
         mobProb = BuildingExplorationHandler.readFloatParam(this.lw, mobProb, "=", this.extraOptions.get("mob_probability"));
      }

      if (this.extraOptions.containsKey("pig_zombie_probability")) {
         pigZombieProb = BuildingExplorationHandler.readFloatParam(this.lw, pigZombieProb, "=", this.extraOptions.get("pig_zombie_probability"));
      }

      if (this.extraOptions.containsKey("enderman_probability")) {
         endermanProb = BuildingExplorationHandler.readFloatParam(this.lw, endermanProb, "=", this.extraOptions.get("enderman_probability"));
      }

      if (this.extraOptions.containsKey("cave_spider_probability")) {
         caveSpiderProb = BuildingExplorationHandler.readFloatParam(this.lw, caveSpiderProb, "=", this.extraOptions.get("cave_spider_probability"));
      }

      if (this.extraOptions.containsKey("ca_ruin_rule")) {
         this.CARuinRule = this.explorationHandler.readRuleIdOrRule("=", this.extraOptions.get("ca_ruin_rule"), this.rules);
      }

      if (this.extraOptions.containsKey("ca_ruin_weight")) {
         this.CARuinWeight = BuildingExplorationHandler.readIntParam(this.lw, this.CARuinWeight, "=", this.extraOptions.get("ca_ruin_weight"));
      }

      if (this.extraOptions.containsKey("ca_ruin_min_height")) {
         this.CARuinMinHeight = BuildingExplorationHandler.readIntParam(this.lw, this.CARuinMinHeight, "=", this.extraOptions.get("ca_ruin_min_height"));
      }

      if (this.extraOptions.containsKey("ca_ruin_max_height")) {
         this.CARuinMaxHeight = BuildingExplorationHandler.readIntParam(this.lw, this.CARuinMaxHeight, "=", this.extraOptions.get("ca_ruin_max_height"));
      }

      if (this.extraOptions.containsKey("ca_ruin_max_width")) {
         this.CARuinContainerWidth = BuildingExplorationHandler.readIntParam(
            this.lw, this.CARuinContainerWidth, "=", this.extraOptions.get("ca_ruin_max_width")
         );
      }

      if (this.extraOptions.containsKey("ca_ruin_automata_rules")) {
         this.CARuinAutomataRules = BuildingExplorationHandler.readAutomataList(this.lw, "=", this.extraOptions.get("ca_ruin_automata_rules"));
      }

      if (this.MaxL <= this.MinL) {
         this.MaxL = this.MinL + 1;
      }

      if (this.StreetDensity < 0) {
         this.StreetDensity = 0;
      }

      if (this.StreetDensity > 20) {
         this.StreetDensity = 20;
      }

      this.WWidth = this.width;
      this.WHeight = this.length - this.embed;
      this.WalkHeight = this.WalkHeight - this.embed;
      if (!this.readInWaterHeight && this.waterHeight >= this.WalkHeight) {
         this.waterHeight = this.WalkHeight - 1;
      }

      if (this.DefaultTowerWeight < 0) {
         this.DefaultTowerWeight = 0;
      }

      if (this.SqrMinWidth < 5) {
         this.SqrMinWidth = 5;
      }

      if (this.SqrMaxWidth < this.SqrMinWidth) {
         this.SqrMaxWidth = this.SqrMinWidth;
      }

      if (this.SqrMinHeight < this.WalkHeight + 4) {
         this.SqrMinHeight = this.WalkHeight + 4;
      }

      if (this.SqrMaxHeight < this.SqrMinHeight) {
         this.SqrMaxHeight = this.SqrMinHeight;
      }

      if (this.BuildingInterval < this.SqrMinWidth) {
         this.BuildingInterval = this.SqrMinWidth + 1;
      }

      if (this.CircMinWidth < 5) {
         this.CircMinWidth = 5;
      }

      if (this.CircMaxWidth < this.CircMinWidth) {
         this.CircMaxWidth = this.CircMinWidth;
      }

      if (this.CircMaxWidth >= Building.CIRCLE_CRENEL.length) {
         this.CircMaxWidth = Building.CIRCLE_CRENEL.length - 1;
      }

      if (this.CircMinWidth >= Building.CIRCLE_CRENEL.length) {
         this.CircMinWidth = Building.CIRCLE_CRENEL.length - 1;
      }

      if (this.CircMinHeight < this.WalkHeight + 4) {
         this.CircMinHeight = this.WalkHeight + 4;
      }

      if (this.CircMaxHeight < this.CircMinHeight) {
         this.CircMaxHeight = this.CircMinHeight;
      }

      if (this.CARuinAutomataRules == null || this.CARuinAutomataRules.size() == 0) {
         this.CARuinWeight = 0;
      }

      if (this.CARuinWeight > 0 && this.CARuinRule == TemplateRule.RULE_NOT_PROVIDED) {
         this.CARuinRule = this.TowerRule;
      }

      if (this.CARuinMaxHeight < this.CARuinMinHeight) {
         this.CARuinMaxHeight = this.CARuinMinHeight;
      }

      if (this.TowerRule == null) {
         throw new Exception("No valid rule provided for tower block!");
      } else {
         if (this.SpawnerRule == TemplateRule.RULE_NOT_PROVIDED) {
            if (mobProb > 0.0F) {
               this.SpawnerRule = new TemplateRule(Blocks.MOB_SPAWNER, 0, "UPRIGHT", (int)(mobProb * 100.0F));
            } else if (pigZombieProb > 0.0F) {
               this.SpawnerRule = new TemplateRule(Blocks.MOB_SPAWNER, 0, "PigZombie", (int)(pigZombieProb * 100.0F));
            } else if (endermanProb > 0.0F) {
               this.SpawnerRule = new TemplateRule(Blocks.MOB_SPAWNER, 0, "Enderman", (int)(endermanProb * 100.0F));
            } else if (caveSpiderProb > 0.0F) {
               this.SpawnerRule = new TemplateRule(Blocks.MOB_SPAWNER, 0, "CaveSpider", (int)(caveSpiderProb * 100.0F));
            }
         }

         if (this.Biomes != ALL_BIOMES && this.Biomes[0] > 0) {
            this.underground = true;
            this.Biomes = ALL_BIOMES;
         }
      }
   }

   public ArrayList<TemplateTML> loadChildTemplates(String listVarString, HashMap<String, TemplateTML> childTemplateMap) {
      ArrayList<TemplateTML> childTemplates = new ArrayList<>();
      if (!this.extraOptions.containsKey(listVarString)) {
         return childTemplates;
      } else {
         String[] names = this.extraOptions.get(listVarString).split("=")[1].split(",");
         String templateListStr = this.extraOptions.get(listVarString);
         if (templateListStr == null) {
            return childTemplates;
         } else {
            for (String name : names) {
               name = name.trim();
               if (name.toUpperCase().equals("NONE")) {
                  return new ArrayList<>();
               }

               if (name.toUpperCase().equals("ALL")) {
                  childTemplates.addAll(childTemplateMap.values());
                  break;
               }

               if (childTemplateMap.containsKey(name.trim())) {
                  childTemplates.add(childTemplateMap.get(name.trim()));
               }
            }

            return childTemplates;
         }
      }
   }

   public ArrayList<TemplateWall> loadChildStyles(String listVarString, HashMap<String, TemplateWall> childTemplateMap) {
      ArrayList<TemplateWall> childTemplates = new ArrayList<>();
      if (!this.extraOptions.containsKey(listVarString)) {
         return childTemplates;
      } else {
         String[] names = this.extraOptions.get(listVarString).split("=")[1].split(",");

         for (String name : names) {
            name = name.trim();
            if (name.toUpperCase().equals("NONE")) {
               return new ArrayList<>();
            }

            if (name.toUpperCase().equals("ALL")) {
               childTemplates.addAll(childTemplateMap.values());
               break;
            }

            if (childTemplateMap.containsKey(name.trim())) {
               TemplateWall ws = childTemplateMap.get(name.trim());
               ws.Biomes = ALL_BIOMES;
               childTemplates.add(ws);
            }
         }

         return childTemplates;
      }
   }

   public int pickRoofStyle(boolean circular, Random random) {
      return circular
         ? Building.pickWeightedOption(random, this.CircRoofStyles, BuildingTower.ROOF_STYLE_IDS)
         : Building.pickWeightedOption(random, this.SqrRoofStyles, BuildingTower.ROOF_STYLE_IDS);
   }

   public int getTMinWidth(boolean circular) {
      return circular ? this.CircMinWidth : this.SqrMinWidth;
   }

   public int getTMaxWidth(boolean circular) {
      return circular ? this.CircMaxWidth : this.SqrMaxWidth;
   }

   public int getTMinHeight(boolean circular) {
      return circular ? this.CircMinHeight : this.SqrMinHeight;
   }

   public int getTMaxHeight(boolean circular) {
      return circular ? this.CircMaxHeight : this.SqrMaxHeight;
   }

   public TemplateRule getRoofRule(boolean circular) {
      return circular ? this.CircRoofRule : this.SqrRoofRule;
   }

   public int pickTWidth(boolean circular, Random random) {
      return circular
         ? this.CircMinWidth + random.nextInt(this.CircMaxWidth - this.CircMinWidth + 1)
         : this.SqrMinWidth + random.nextInt(this.SqrMaxWidth - this.SqrMinWidth + 1);
   }

   public int pickTHeight(boolean circular, Random random) {
      return circular
         ? this.CircMinHeight + random.nextInt(this.CircMaxHeight - this.CircMinHeight + 1)
         : this.SqrMinHeight + random.nextInt(this.SqrMaxHeight - this.SqrMinHeight + 1);
   }

   public static ArrayList<TemplateTML> loadTemplatesFromDir(File tmlDirectory, BuildingExplorationHandler explorationHandler) {
      ArrayList<TemplateTML> templates = new ArrayList<>();

      for (File f : tmlDirectory.listFiles()) {
         if (getFileType(f.getName()).equals("tml")) {
            try {
               TemplateTML t = new TemplateTML(f, explorationHandler).buildLayout();
               templates.add(t);
            } catch (Exception var8) {
               if (var8 == TemplateTML.ZERO_WEIGHT_EXCEPTION) {
                  explorationHandler.lw.println("Did not load " + f.getName() + ", weight was zero.");
               } else {
                  explorationHandler.lw.println("\nThere was a problem loading the .tml file " + f.getName());
                  if (!var8.getMessage().startsWith("Error reading rule: BlockID ")) {
                     var8.printStackTrace(explorationHandler.lw);
                     explorationHandler.lw.println();
                  } else {
                     explorationHandler.lw.println(var8.getMessage());
                  }
               }
            }
         }
      }

      return templates;
   }

   public static ArrayList<TemplateWall> loadWallStylesFromDir(File stylesDirectory, BuildingExplorationHandler explorationHandler) throws Exception {
      if (!stylesDirectory.exists()) {
         throw new Exception("Could not find directory /" + stylesDirectory.getName() + " in the config folder " + stylesDirectory.getParent() + "!");
      } else {
         explorationHandler.lw.println("\nLoading building subfolder in " + stylesDirectory + "\\" + "buildings" + "...");
         HashMap<String, TemplateTML> buildingTemplates = new HashMap<>();
         Iterator<TemplateTML> itr = null;

         try {
            itr = loadTemplatesFromDir(new File(stylesDirectory, "buildings"), explorationHandler).iterator();
         } catch (NullPointerException var10) {
            explorationHandler.lw.println("No buildings folder for " + stylesDirectory.getName() + var10.toString());
         }

         if (itr != null) {
            while (itr.hasNext()) {
               TemplateTML t = itr.next();
               buildingTemplates.put(t.name, t);
            }
         }

         explorationHandler.lw.println("\nLoading wall styles from directory " + stylesDirectory + "...");
         ArrayList<TemplateWall> styles = new ArrayList<>();

         for (File f : stylesDirectory.listFiles()) {
            if (getFileType(f.getName()).equals("tml")) {
               try {
                  TemplateWall ws = new TemplateWall(f, buildingTemplates, explorationHandler);
                  styles.add(ws);
               } catch (Exception var11) {
                  if (var11 == TemplateTML.ZERO_WEIGHT_EXCEPTION) {
                     explorationHandler.lw.println("Did not load " + f.getName() + ", weight was zero.");
                  } else {
                     explorationHandler.lw.println("\nError loading wall style " + f.getName());
                     if (!var11.getMessage().startsWith("Error reading rule: BlockID ")) {
                        var11.printStackTrace(explorationHandler.lw);
                        explorationHandler.lw.println();
                     } else {
                        explorationHandler.lw.println(var11.getMessage());
                     }
                  }
               }
            }
         }

         explorationHandler.lw.flush();
         if (styles.size() == 0) {
            throw new Exception("Did not find any valid wall styles!");
         } else {
            return styles;
         }
      }
   }

   public static TemplateWall pickBiomeWeightedWallStyle(List<TemplateWall> styles, World world, int i, int k, Random random, boolean ignoreBiomes) {
      int biome = Biome.getIdForBiome(Compat.biome(world, i, k)) + 1;
      if ((biome < 0 || biome > 256) && !ignoreBiomes) {
         return null;
      } else {
         int sum = 0;

         for (TemplateWall ws : styles) {
            if (ignoreBiomes || ws.Biomes == ALL_BIOMES || ws.Biomes[biome] > 0) {
               sum += ws.weight;
            }
         }

         if (sum <= 0) {
            return null;
         } else {
            int s = random.nextInt(sum);
            sum = 0;

            for (TemplateWall wsx : styles) {
               if (ignoreBiomes || wsx.Biomes == ALL_BIOMES || wsx.Biomes[biome] > 0) {
                  sum += wsx.weight;
               }

               if (sum > s) {
                  return wsx;
               }
            }

            return null;
         }
      }
   }

   public static void loadStreets(List<TemplateWall> cityStyles, File streetsDirectory, BuildingExplorationHandler explorationHandler) throws Exception {
      HashMap<String, TemplateWall> streetTemplateMap = new HashMap<>();

      try {
         explorationHandler.lw.println("\nLoading streets subfolder in " + streetsDirectory + "...");

         for (TemplateWall cs : loadWallStylesFromDir(streetsDirectory, explorationHandler)) {
            streetTemplateMap.put(cs.name, cs);
         }
      } catch (Exception var6) {
         explorationHandler.lw.println("No street folder for " + streetsDirectory.getName() + var6.toString());
      }

      explorationHandler.lw.println();
      Iterator<TemplateWall> var7 = cityStyles.iterator();

      while (var7.hasNext()) {
         TemplateWall cs = (TemplateWall)var7.next();
         cs.streets = cs.loadChildStyles("street_templates", streetTemplateMap);
         if (cs.streets.size() == 0 && !cs.underground) {
            var7.remove();
            explorationHandler.lw.println("No valid street styles for " + cs.name + ". Disabling this city style.");
         }
      }

      if (cityStyles.size() == 0) {
         throw new Exception("Did not find any valid city styles that had street styles!");
      }
   }

   private static String getFileType(String s) {
      int mid = s.lastIndexOf(".");
      return s.substring(mid + 1, s.length());
   }
}
