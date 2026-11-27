package com.electrifiedded.walledcitymod;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

@Mod(
   modid = "caruins",
   name = "Cellular Automata Generator",
   version = Reference.VERSION,
   dependencies = "required-after:archaiclib@[1.40.0,);after:biomesoplenty",
   acceptableRemoteVersions = "*"
)
public class PopulatorCARuins extends BuildingExplorationHandler {
   @Instance("caruins")
   public static PopulatorCARuins instance;
   private static final String AUTOMATA_RULES_STRING = "AUTOMATA RULES";
   private static final TemplateRule DEFAULT_TEMPLATE = new TemplateRule(
      new Block[]{Blocks.COBBLESTONE, Blocks.MOSSY_COBBLESTONE, Blocks.MOSSY_COBBLESTONE}, new int[]{0, 0, 0}, 100
   );
   private static TemplateRule[] DEFAULT_BLOCK_RULES = new TemplateRule[BIOME_NAMES.length];
   private static String[] BLOCK_RULE_NAMES = new String[DEFAULT_BLOCK_RULES.length];
   private static final String[] SPAWNER_RULE_NAMES = new String[]{
      "MediumLightNarrowFloorSpawnerRule", "MediumLightWideFloorSpawnerRule", "LowLightSpawnerRule"
   };
   public static final String[][] DEFAULT_CA_RULES = new String[][]{
      {"B3/S23", "5", "Life - good for weird temples"},
      {"B36/S013468", "3", "pillars and hands"},
      {"B367/S02347", "2", "towers with interiors and chasms"},
      {"B34/S2356", "3", "towers with hetrogenous shapes"},
      {"B368/S245", "8", "Morley - good hanging bits"},
      {"B36/S125", "4", "2x2 - pillar & arch temple/tower/statue"},
      {"B36/S23", "4", "High Life - space invaders, hanging arms."},
      {"B3568/S148", "4", "fuzzy stilts"},
      {"B3/S1245", "8", "complex"},
      {"B3567/S13468", "5", "fat fuzzy"},
      {"B356/S16", "5", "fuzzy with spurs"},
      {"B3468/S123", "3", "towers with arches"},
      {"B35678/S015678", "2", "checkerboard"},
      {"B35678/S0156", "15", "spermatazoa"},
      {"B26/S12368", "1", "mayan pyramid"},
      {"B248/S45", "1", "gaudi pyramid"},
      {"B2457/S013458", "1", "complex interior pyramid"},
      {"B45/S2345", "6", "45-rule - square towers"}
   };
   public static final String[] SEED_TYPE_STRINGS = new String[]{"SymmetricSeedWeight", "LinearSeedWeight", "CircularSeedWeight", "CruciformSeedWeight"};
   public int[] seedTypeWeights = new int[]{8, 2, 2, 1};
   public float SymmetricSeedDensity = 0.5F;
   public int MinHeight = 20;
   public int MaxHeight = 70;
   public int ContainerWidth = 40;
   public int ContainerLength = 40;
   public int MinHeightBeforeOscillation = 12;
   public boolean SmoothWithStairs = true;
   public boolean MakeFloors = true;
   public TemplateRule[] blockRules = new TemplateRule[DEFAULT_BLOCK_RULES.length];
   public TemplateRule[] spawnerRules = new TemplateRule[]{
      BuildingCellularAutomaton.DEFAULT_MEDIUM_LIGHT_NARROW_SPAWNER_RULE,
      BuildingCellularAutomaton.DEFAULT_MEDIUM_LIGHT_WIDE_SPAWNER_RULE,
      BuildingCellularAutomaton.DEFAULT_LOW_LIGHT_SPAWNER_RULE
   };
   ArrayList<byte[][]> caRules = null;
   int[][] caRulesWeightsAndIndex = (int[][])null;

   @EventHandler
   public void preInit(FMLPreInitializationEvent event) {
      com.electrifiedded.archaiclib.world.ChunkStagedGen.register();
      this.logger = event.getModLog();
      BuildingExplorationHandler.installDefaultConfigs();
      this.settingsFileName = "CARuinsSettings.txt";
      if (event.getSourceFile().getName().endsWith(".jar") && event.getSide().isClient()) {
         try {
            Class.forName("mods.mud.ModUpdateDetector")
               .getDeclaredMethod("registerMod", ModContainer.class, String.class, String.class)
               .invoke(
                  null,
                  FMLCommonHandler.instance().findContainerFor(this),
                  "https://raw.github.com/GotoLink/Generatormods/master/update.xml",
                  "https://raw.github.com/GotoLink/Generatormods/master/changelog.md"
               );
         } catch (Throwable var3) {
         }
      }
   }

   @EventHandler
   public void serverStarting(FMLServerStartingEvent event) {
      event.registerServerCommand(new CommandBuild());
      event.registerServerCommand(new CommandScan());
   }

   @Override
   public final void loadDataFiles() {
      try {
         this.initializeLogging("Loading options for the Cellular Automata Generator.");

         for (int i = 0; i < BIOME_NAMES.length; i++) {
            if (BIOME_NAMES[i] != null) {
               if (i > 21) {
                  DEFAULT_BLOCK_RULES[i] = DEFAULT_TEMPLATE;
               }

               BLOCK_RULE_NAMES[i] = BIOME_NAMES[i].replaceAll("\\s", "") + "BlockRule";
            }
         }

         this.caRules = new ArrayList<>();
         this.getGlobalOptions();
         this.finalizeLoading(false, "ruin");
      } catch (Exception var5) {
         this.errFlag = true;
         this.logOrPrint("There was a problem loading the Cellular Automata Generator: " + var5.getMessage(), "SEVERE");
         this.lw.println("There was a problem loading the Cellular Automata Generator: " + var5.getMessage());
         var5.printStackTrace();
      } finally {
         if (this.lw != null) {
            this.lw.close();
         }
      }

      if (this.GlobalFrequency < 1.0E-6 || this.caRules == null || this.caRules.size() == 0) {
         this.errFlag = true;
      }

      this.dataFilesLoaded = true;
   }

   @Override
   public void loadGlobalOptions(BufferedReader br) {
      ArrayList<Integer> caRuleWeights = new ArrayList<>();

      try {
         for (String read = br.readLine(); read != null; read = br.readLine()) {
            this.readGlobalOptions(this.lw, read);
            if (read.startsWith("MinHeight")) {
               this.MinHeight = readIntParam(this.lw, this.MinHeight, ":", read);
            }

            if (read.startsWith("MaxHeight")) {
               this.MaxHeight = readIntParam(this.lw, this.MaxHeight, ":", read);
            }

            if (read.startsWith("MinHeightBeforeOscillation")) {
               this.MinHeightBeforeOscillation = readIntParam(this.lw, this.MinHeightBeforeOscillation, ":", read);
            }

            if (read.startsWith("SmoothWithStairs")) {
               this.SmoothWithStairs = readBooleanParam(this.lw, this.SmoothWithStairs, ":", read);
            }

            if (read.startsWith("MakeFloors")) {
               this.MakeFloors = readBooleanParam(this.lw, this.MakeFloors, ":", read);
            }

            if (read.startsWith("ContainerWidth")) {
               this.ContainerWidth = readIntParam(this.lw, this.ContainerWidth, ":", read);
            }

            if (read.startsWith("ContainerLength")) {
               this.ContainerLength = readIntParam(this.lw, this.ContainerLength, ":", read);
            }

            this.readChestItemsList(this.lw, read, br);
            if (read.startsWith("SymmetricSeedDensity")) {
               this.SymmetricSeedDensity = readFloatParam(this.lw, this.SymmetricSeedDensity, ":", read);
            }

            for (int m = 0; m < SEED_TYPE_STRINGS.length; m++) {
               if (read.startsWith(SEED_TYPE_STRINGS[m])) {
                  this.seedTypeWeights[m] = readIntParam(this.lw, this.seedTypeWeights[m], ":", read);
               }
            }

            for (int mx = 0; mx < this.spawnerRules.length; mx++) {
               if (read.startsWith(SPAWNER_RULE_NAMES[mx])) {
                  try {
                     this.spawnerRules[mx] = this.readRuleIdOrRule(":", read, null);
                  } catch (Exception var17) {
                     this.spawnerRules[mx] = BuildingCellularAutomaton.DEFAULT_MEDIUM_LIGHT_NARROW_SPAWNER_RULE;
                     this.lw.println(var17.getMessage());
                  }
               }
            }

            for (int mxx = 0; mxx < DEFAULT_BLOCK_RULES.length; mxx++) {
               if (BIOME_NAMES[mxx] != null && read.startsWith(BLOCK_RULE_NAMES[mxx])) {
                  try {
                     this.blockRules[mxx] = this.readRuleIdOrRule(":", read, null);
                  } catch (Exception var16) {
                     this.blockRules[mxx] = DEFAULT_BLOCK_RULES[mxx];
                     this.lw.println(var16.getMessage());
                  }
               }
            }

            if (read.startsWith("AUTOMATA RULES")) {
               for (String var20 = br.readLine(); var20 != null; var20 = br.readLine()) {
                  if (var20.startsWith("B") || var20.startsWith("b")) {
                     String[] splitStr = var20.split(",");
                     this.caRules.add(BuildingCellularAutomaton.parseCARule(splitStr[0], this.lw));
                     caRuleWeights.add(readIntParam(this.lw, 1, "=", splitStr[1].trim()));
                  }
               }
               break;
            }
         }

         if (this.TriesPerChunk > 100) {
            this.TriesPerChunk = 100;
         }
      } catch (IOException var18) {
         this.lw.println(var18.getMessage());
      } finally {
         try {
            if (br != null) {
               br.close();
            }
         } catch (IOException var15) {
         }
      }

      this.setRulesWeightAndIndex(caRuleWeights);
   }

   private void setRulesWeightAndIndex(ArrayList<Integer> caRuleWeights) {
      this.caRulesWeightsAndIndex = new int[2][caRuleWeights.size()];

      for (int m = 0; m < caRuleWeights.size(); this.caRulesWeightsAndIndex[1][m] = m++) {
         this.caRulesWeightsAndIndex[0][m] = caRuleWeights.get(m);
      }
   }

   @Override
   public void writeGlobalOptions(PrintWriter pw) {
      ArrayList<Integer> caRuleWeights = new ArrayList<>();
      this.printGlobalOptions(pw, true);
      pw.println();
      pw.println("<-MinHeight and MaxHeight are the minimum and maximum allowed height of the structures->");
      pw.println("<-MinHeightBeforeOscillation - Any structures that form oscillators before MaxOscillatorCullStep will be culled.->");
      pw.println("<-Smooth with stairs - If set to true, will smooth out ruins by placing extra stair blocks.->");
      pw.println("<-ContainerWidth and ContainerLength are the dimensions of the bounding rectangle.->");
      pw.println("MinHeight:" + this.MinHeight);
      pw.println("MaxHeight:" + this.MaxHeight);
      pw.println("MinHeightBeforeOscillation:" + this.MinHeightBeforeOscillation);
      pw.println("SmoothWithStairs:" + this.SmoothWithStairs);
      pw.println("MakeFloors:" + this.MakeFloors);
      pw.println("ContainerWidth:" + this.ContainerWidth);
      pw.println("ContainerLength:" + this.ContainerLength);
      pw.println();
      this.printDefaultChestItems(pw);
      pw.println();
      pw.println("<-Seed type weights are the relative likelihood weights that different seeds will be used. Weights are nonnegative integers.->");
      pw.println("<-SymmetricSeedDensity is the density (out of 1.0) of live blocks in the symmetric seed.->");
      pw.println("SymmetricSeedDensity:" + this.SymmetricSeedDensity);

      for (int m = 0; m < SEED_TYPE_STRINGS.length; m++) {
         pw.println(SEED_TYPE_STRINGS[m] + ":" + this.seedTypeWeights[m]);
      }

      pw.println();
      pw.println("<-These spawner rule variables control what spawners will be used depending on the light level and floor width.->");

      for (int m = 0; m < this.spawnerRules.length; m++) {
         pw.println(SPAWNER_RULE_NAMES[m] + ":" + this.spawnerRules[m]);
      }

      pw.println();
      pw.println("<-BlockRule is the template rule that controls what blocks the structure will be made out of.->");
      pw.println("<-Default is BiomeNameBlockRule:" + DEFAULT_TEMPLATE.toString() + "->");
      pw.println(
         "<-Which translates into: (special condition) then,(100%=complete)ruin in either normal(1 out of 3 chance) or mossy cobblestone(2 out of 3) in said biome->"
      );
      pw.println("<-Metadatas are supported, use blockname-blockmetadata syntax->");

      for (int m = 0; m < DEFAULT_BLOCK_RULES.length; m++) {
         if (BLOCK_RULE_NAMES[m] != null) {
            pw.println(BLOCK_RULE_NAMES[m] + ":" + DEFAULT_BLOCK_RULES[m]);
         }
      }

      pw.println();
      pw.println("<-An automata rule should be in the form B<neighbor digits>/S<neighbor digits>, where B stands for \"birth\" and S stands->");
      pw.println("<-   for \"survive\". <neighbor digits> are the subset the digits from 0 to 8 on which the rule will birth or survive.->");
      pw.println("<-   For example, the Game of Life has the rule code B3/S23.->");
      pw.println("<-Rule weights are the relative likelihood weights that different rules will be used. Weights are nonnegative integers.->");
      pw.println("AUTOMATA RULES");

      try {
         for (String[] defaultRule : DEFAULT_CA_RULES) {
            pw.println(defaultRule[0] + ", weight=" + defaultRule[1] + (defaultRule[2].length() > 0 ? ",  <-" + defaultRule[2] + "->" : ""));
            this.caRules.add(BuildingCellularAutomaton.parseCARule(defaultRule[0], this.lw));
            caRuleWeights.add(Integer.parseInt(defaultRule[1]));
         }
      } catch (NumberFormatException var10) {
         this.lw.println(var10.getMessage());
      } finally {
         if (pw != null) {
            pw.close();
         }
      }

      this.setRulesWeightAndIndex(caRuleWeights);
   }

   @Override
   public final void generate(World world, Random random, int i, int k) {
      if (random.nextFloat() < this.GlobalFrequency) {
         new WorldGenCARuins(this, world, random, i, k, this.TriesPerChunk, this.GlobalFrequency).run();
      }
   }

   @Override
   public String toString() {
      return "CARuins";
   }

   @EventHandler
   public void modsLoaded(FMLPostInitializationEvent event) {
      if (!this.dataFilesLoaded) {
         this.loadDataFiles();
      }

      if (!this.errFlag) {
         GameRegistry.registerWorldGenerator(this, 2);
      }
   }

   static {
      DEFAULT_BLOCK_RULES[0] = DEFAULT_TEMPLATE;
      DEFAULT_BLOCK_RULES[1] = DEFAULT_TEMPLATE;
      DEFAULT_BLOCK_RULES[2] = new TemplateRule(new Block[]{Blocks.STONE, Blocks.STONEBRICK, Blocks.STONEBRICK}, new int[]{0, 1, 2}, 100);
      DEFAULT_BLOCK_RULES[3] = new TemplateRule(Blocks.SANDSTONE, 0, 100);
      DEFAULT_BLOCK_RULES[4] = new TemplateRule(new Block[]{Blocks.STONE, Blocks.STONEBRICK, Blocks.STONEBRICK}, new int[]{0, 0, 2}, 100);
      DEFAULT_BLOCK_RULES[5] = DEFAULT_TEMPLATE;
      DEFAULT_BLOCK_RULES[6] = DEFAULT_TEMPLATE;
      DEFAULT_BLOCK_RULES[7] = DEFAULT_TEMPLATE;
      DEFAULT_BLOCK_RULES[8] = DEFAULT_TEMPLATE;
      DEFAULT_BLOCK_RULES[9] = new TemplateRule(Blocks.NETHER_BRICK, 0, 100);
      DEFAULT_BLOCK_RULES[10] = new TemplateRule(Blocks.END_STONE, 0, 100);
      DEFAULT_BLOCK_RULES[11] = new TemplateRule(new Block[]{Blocks.ICE, Blocks.SNOW, Blocks.STONEBRICK}, new int[]{0, 0, 2}, 100);
      DEFAULT_BLOCK_RULES[12] = new TemplateRule(new Block[]{Blocks.ICE, Blocks.SNOW, Blocks.STONEBRICK}, new int[]{0, 0, 2}, 100);
      DEFAULT_BLOCK_RULES[13] = new TemplateRule(new Block[]{Blocks.SNOW, Blocks.STONEBRICK, Blocks.STONEBRICK}, new int[]{0, 2, 2}, 100);
      DEFAULT_BLOCK_RULES[14] = new TemplateRule(new Block[]{Blocks.SNOW, Blocks.STONEBRICK, Blocks.STONEBRICK}, new int[]{0, 2, 2}, 100);
      DEFAULT_BLOCK_RULES[15] = DEFAULT_TEMPLATE;
      DEFAULT_BLOCK_RULES[16] = DEFAULT_TEMPLATE;
      DEFAULT_BLOCK_RULES[17] = DEFAULT_TEMPLATE;
      DEFAULT_BLOCK_RULES[18] = new TemplateRule(Blocks.SANDSTONE, 0, 100);
      DEFAULT_BLOCK_RULES[19] = DEFAULT_TEMPLATE;
      DEFAULT_BLOCK_RULES[20] = DEFAULT_TEMPLATE;
      DEFAULT_BLOCK_RULES[21] = new TemplateRule(new Block[]{Blocks.STONE, Blocks.STONEBRICK, Blocks.STONEBRICK}, new int[]{0, 0, 2}, 100);
   }
}
