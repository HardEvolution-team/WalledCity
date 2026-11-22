package com.electrifiedded.walledcitymod;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraftforge.fml.common.Loader;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderEnd;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.storage.ISaveHandler;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

public abstract class BuildingExplorationHandler implements IWorldGenerator {
   protected static final String VERSION = "0.1.6";
   protected static final int MAX_TRIES_PER_CHUNK = 100;
   public static final File CONFIG_DIRECTORY = new File(Loader.instance().getConfigDir(), "generatormods");
   protected static final File LOG = new File(new File(getMinecraftBaseDir(), "logs"), "generatormods_log.txt");
   protected String settingsFileName;
   protected String templateFolderName;
   public Logger logger;
   public PrintWriter lw = null;
   public float GlobalFrequency = 0.025F;
   public int TriesPerChunk = 1;
   protected HashMap<String, Integer> chestTries = new HashMap<>();
   protected HashMap<String, Object[][]> chestItems = new HashMap<>();
   protected boolean errFlag = false;
   protected boolean dataFilesLoaded = false;
   protected boolean logActivated = false;
   private List<Integer> AllowedDimensions = new ArrayList<>();
   private List<World> currentWorld = new ArrayList<>();
   public static String[] BIOME_NAMES = new String[256 + 1];

   public void generate(Random random, int chunkX, int chunkZ, World world, net.minecraft.world.gen.IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
      if (world.getWorldInfo().isMapFeaturesEnabled()
         && !(world.provider instanceof WorldProviderEnd)
         && this.AllowedDimensions.contains(world.provider.getDimension())) {
         // planned off-thread; each chunk writes its own part when it generates (no cascading, no lag)
         final Random rnd = new Random(random.nextLong());
         com.electrifiedded.archaiclib.world.ChunkStagedGen.submit(world, () -> this.generateSurface(world, rnd, chunkX, chunkZ));
      }
   }

   public abstract void generate(World var1, Random var2, int var3, int var4);

   public void generateSurface(World world, Random random, int i, int k) {
      if (!this.errFlag) {
         this.updateWorldExplored(world);
         this.generate(world, random, i * 16, k * 16);
      }
   }

   public abstract void loadDataFiles();

   public abstract void loadGlobalOptions(BufferedReader var1);

   public void logOrPrint(String str, String lvl) {
      if (this.logActivated) {
         this.logger.log(Level.toLevel(lvl), str);
      }
   }

   public void readChestItemsList(PrintWriter lw, String line, BufferedReader br) throws IOException {
      if (line.startsWith("CHEST_")) {
         String chestType = line.substring(6);
         this.chestTries.put(chestType, readIntParam(lw, 1, ":", br.readLine()));
         ArrayList<String> lines = new ArrayList<>();

         for (String var14 = br.readLine(); var14 != null && var14.length() != 0; var14 = br.readLine()) {
            lines.add(var14);
         }

         Object[][] tempArray = new Object[6][lines.size()];

         for (int n = 0; n < lines.size(); n++) {
            String[] intStrs = lines.get(n).trim().split(",");

            try {
               tempArray[0][n] = n;
               String[] idAndMeta = intStrs[0].split("-");

               Object temp;
               try {
                  int i = Integer.parseInt(idAndMeta[0]);
                  temp = Compat.itemOrBlock(i);
                  if (temp == null) {
                     temp = Compat.itemOrBlock(i);
                  }
               } catch (Exception var12) {
                  temp = Compat.itemOrBlock(idAndMeta[0]);
                  if (temp == null) {
                     temp = Compat.itemOrBlock(idAndMeta[0]);
                  }
               }

               if (temp != null) {
                  tempArray[1][n] = temp;
                  tempArray[2][n] = idAndMeta.length > 1 ? Integer.parseInt(idAndMeta[1]) : 0;

                  for (int m = 1; m < 4; m++) {
                     tempArray[m + 2][n] = Integer.parseInt(intStrs[m]);
                  }

                  if (Integer.class.cast(tempArray[4][n]) < 0) {
                     tempArray[4][n] = 0;
                  }

                  if (Integer.class.cast(tempArray[5][n]) < Integer.class.cast(tempArray[4][n])) {
                     tempArray[5][n] = tempArray[4][n];
                  }

                  if (Integer.class.cast(tempArray[5][n]) > 64) {
                     tempArray[5][n] = 64;
                  }
               }
            } catch (Exception var13) {
               lw.println("Error parsing Settings file: " + var13.toString());
               lw.println("Line:" + lines.get(n));
            }
         }

         this.chestItems.put(chestType, tempArray);
      }
   }

   public TemplateRule readRuleIdOrRule(String splitString, String read, TemplateRule[] rules) throws Exception {
      String postSplit = read.split(splitString, 2)[1].trim();

      try {
         int ruleId = Integer.parseInt(postSplit);
         return rules[ruleId];
      } catch (NumberFormatException var6) {
         return new TemplateRule(postSplit, false);
      } catch (Exception var7) {
         throw new Exception("Error reading block rule for variable: " + var7.toString() + ". Line:" + read);
      }
   }

   public void updateWorldExplored(World world) {
      if (this.isNewWorld(world)) {
         this.logOrPrint("Starting to survey " + world.provider.getDimensionType().getName() + " for generation...", "INFO");
      }
   }

   public abstract void writeGlobalOptions(PrintWriter var1);

   protected void copyDefaultChestItems() {
      for (int i = 0; i < Building.CHEST_TYPE_LABELS.length; i++) {
         this.chestTries.put(Building.CHEST_TYPE_LABELS[i], Building.DEFAULT_CHEST_TRIES[i]);
      }

      Object[][][] chestItems = new Object[Building.DEFAULT_CHEST_ITEMS.length][][];

      for (int l = 0; l < Building.DEFAULT_CHEST_ITEMS.length; l++) {
         chestItems[l] = new Object[6][Building.DEFAULT_CHEST_ITEMS[l].length];

         for (int m = 0; m < Building.DEFAULT_CHEST_ITEMS[l].length; m++) {
            for (int n = 0; n < 6; n++) {
               chestItems[l][n][m] = Building.DEFAULT_CHEST_ITEMS[l][m][n];
            }
         }

         this.chestItems.put(Building.CHEST_TYPE_LABELS[l], chestItems[l]);
      }
   }

   protected void finalizeLoading(boolean hasTemplate, String structure) {
      if (hasTemplate) {
         this.lw.println("\nTemplate loading complete.");
      }

      this.lw
         .println(
            "Probability of "
               + structure
               + " generation attempt per chunk explored is "
               + this.GlobalFrequency
               + ", with "
               + this.TriesPerChunk
               + " tries per chunk."
         );
   }

   protected final void getGlobalOptions() {
      File settingsFile = new File(CONFIG_DIRECTORY, this.settingsFileName);
      if (settingsFile.exists()) {
         this.lw.println("Getting global options for " + this.toString() + " ...");

         try {
            this.loadGlobalOptions(new BufferedReader(new FileReader(settingsFile)));
         } catch (FileNotFoundException var4) {
         }
      } else {
         this.copyDefaultChestItems();

         try {
            this.writeGlobalOptions(new PrintWriter(new BufferedWriter(new FileWriter(settingsFile))));
         } catch (IOException var3) {
            var3.printStackTrace();
         }
      }
   }

   protected void initializeLogging(String message) throws IOException {
      if (LOG.length() > 8350L) {
         LOG.delete();
      }

      this.lw = new PrintWriter(new BufferedWriter(new FileWriter(LOG, LOG.canWrite())));
      this.logOrPrint(message, "INFO");
      if (BIOME_NAMES[1] == null || BIOME_NAMES[1].equals("")) {
         for (int i = 0; i < BIOME_NAMES.length - 1; i++) {
            if (Biome.getBiome(i) != null) {
               BIOME_NAMES[i + 1] = com.electrifiedded.archaiclib.legacy.world.LegacyBiomes.getName(Biome.getBiome(i));
            }
         }
      }
   }

   protected boolean isNewWorld(World world) {
      if (this.currentWorld.isEmpty()) {
         this.currentWorld.add(world);
         return true;
      } else if (this.currentWorld.contains(world)) {
         return false;
      } else {
         File newdir = getWorldSaveDir(world);

         for (World w : this.currentWorld) {
            File olddir = getWorldSaveDir(w);
            if (newdir != null && olddir != null && olddir.compareTo(newdir) != 0) {
               this.currentWorld.add(world);
               return true;
            }
         }

         return false;
      }
   }

   protected void printDefaultChestItems(PrintWriter pw) {
      pw.println();
      pw.println("<-Chest contents->");
      pw.println("<-Tries is the number of selections that will be made for this chest type.->");
      pw.println("<-Format for items is <item name>,<selection weight>,<min stack size>,<max stack size> ->");
      pw.println("<-So e.g. minecraft:arrow,2,1,12 means a stack of between 1 and 12 arrows, with a selection weight of 2.->");

      for (int l = 0; l < Building.CHEST_TYPE_LABELS.length; l++) {
         pw.println("CHEST_" + Building.CHEST_TYPE_LABELS[l]);
         pw.println("Tries:" + Building.DEFAULT_CHEST_TRIES[l]);

         for (int m = 0; m < Building.DEFAULT_CHEST_ITEMS[l].length; m++) {
            try {
               String txt = Compat.name(Building.DEFAULT_CHEST_ITEMS[l][m][1]);
               if (txt == null) {
                  txt = Compat.name(Building.DEFAULT_CHEST_ITEMS[l][m][1]);
               }

               if (txt != null) {
                  pw.print(txt);
                  pw.print("-" + Building.DEFAULT_CHEST_ITEMS[l][m][2]);
                  pw.print("," + Building.DEFAULT_CHEST_ITEMS[l][m][3]);
                  pw.print("," + Building.DEFAULT_CHEST_ITEMS[l][m][4]);
                  pw.println("," + Building.DEFAULT_CHEST_ITEMS[l][m][5]);
               }
            } catch (Exception var5) {
               var5.printStackTrace();
            }
         }

         pw.println();
      }
   }

   protected void printGlobalOptions(PrintWriter pw, boolean frequency) {
      pw.println("<-README: This file should be in the config/generatormods folder->");
      pw.println();
      if (frequency) {
         pw.println("<-GlobalFrequency controls how likely structures are to appear. Should be between 0.0 and 1.0. Lower to make less common->");
         pw.println("GlobalFrequency:" + this.GlobalFrequency);
      }

      pw.println("<-TriesPerChunk allows multiple attempts per chunk. Only change from 1 if you want very dense generation!->");
      pw.println("TriesPerChunk:" + this.TriesPerChunk);
      pw.println("<-AllowedDimensions allows structures in corresponding dimension, by dimension ID. Default is Nether(-1) and OverWorld(0)->");
      pw.println(
         "AllowedDimensions:"
            + (this.AllowedDimensions.isEmpty() ? "-1,0" : Arrays.toString(this.AllowedDimensions.toArray()).replace("[", "").replace("]", "").trim())
      );
      pw.println("<-LogActivated controls information stored into forge logs. Set to true if you want to report an issue with complete forge logs.->");
      pw.println("LogActivated:" + this.logActivated);
   }

   protected void readGlobalOptions(PrintWriter lw, String read) {
      if (read.startsWith("GlobalFrequency")) {
         this.GlobalFrequency = readFloatParam(lw, this.GlobalFrequency, ":", read);
      }

      if (read.startsWith("TriesPerChunk")) {
         this.TriesPerChunk = readIntParam(lw, this.TriesPerChunk, ":", read);
      }

      if (read.startsWith("AllowedDimensions")) {
         this.AllowedDimensions = Arrays.asList(readIntList(lw, new Integer[]{-1, 0}, ":", read));
      }

      if (read.startsWith("LogActivated")) {
         this.logActivated = readBooleanParam(lw, this.logActivated, ":", read);
      }
   }

   public static ArrayList<byte[][]> readAutomataList(PrintWriter lw, String splitString, String read) {
      ArrayList<byte[][]> rules = new ArrayList<>();
      String[] ruleStrs = read.split(splitString)[1].split(",");

      for (String ruleStr : ruleStrs) {
         byte[][] rule = BuildingCellularAutomaton.parseCARule(ruleStr.trim(), lw);
         if (rule != null) {
            rules.add(rule);
         }
      }

      return rules.size() == 0 ? null : rules;
   }

   public static boolean readBooleanParam(PrintWriter lw, boolean defaultVal, String splitString, String read) {
      try {
         defaultVal = Boolean.parseBoolean(read.split(splitString)[1].trim());
      } catch (NullPointerException var5) {
         lw.println("Error parsing boolean: " + var5.toString());
         lw.println("Using default " + defaultVal + ". Line:" + read);
      }

      return defaultVal;
   }

   public static float readFloatParam(PrintWriter lw, float defaultVal, String splitString, String read) {
      try {
         defaultVal = Float.parseFloat(read.split(splitString)[1].trim());
      } catch (Exception var5) {
         lw.println("Error parsing double: " + var5.toString());
         lw.println("Using default " + defaultVal + ". Line:" + read);
      }

      return defaultVal;
   }

   public static Integer[] readIntList(PrintWriter lw, Integer[] defaultVals, String splitString, String read) {
      try {
         String[] check = read.split(splitString)[1].split(",");
         Integer[] newVals = new Integer[check.length];

         for (int i = 0; i < check.length; i++) {
            newVals[i] = Integer.parseInt(check[i].trim());
         }

         return newVals;
      } catch (Exception var7) {
         lw.println("Error parsing intlist input: " + var7.toString());
         lw.println("Using default. Line:" + read);
         return defaultVals;
      }
   }

   public static int readIntParam(PrintWriter lw, int defaultVal, String splitString, String read) {
      try {
         defaultVal = Integer.parseInt(read.split(splitString)[1].trim());
      } catch (NumberFormatException var5) {
         lw.println("Error parsing int: " + var5.toString());
         lw.println("Using default " + defaultVal + ". Line:" + read);
      }

      return defaultVal;
   }

   public static int[] readNamedCheckList(PrintWriter lw, int[] defaultVals, String splitString, String read, String[] names, String allStr) {
      if (defaultVals == null || names.length != defaultVals.length) {
         defaultVals = new int[names.length];
      }

      try {
         int[] newVals = new int[names.length];

         for (int i = 0; i < newVals.length; i++) {
            newVals[i] = 0;
         }

         if (read.split(splitString)[1].trim().equalsIgnoreCase(allStr)) {
            for (int i = 0; i < newVals.length; i++) {
               newVals[i] = 1;
            }
         } else {
            for (String check : read.split(splitString)[1].split(",")) {
               boolean found = false;

               for (int i = 0; i < names.length; i++) {
                  if (names[i] != null && names[i].replaceAll("\\s", "").trim().equalsIgnoreCase(check.replaceAll("\\s", "").trim())) {
                     found = true;
                     newVals[i]++;
                  }
               }

               if (!found) {
                  lw.println("Warning, named checklist item not found:" + check + ". Line:" + read);
               }
            }
         }

         return newVals;
      } catch (Exception var13) {
         lw.println("Error parsing checklist input: " + var13.toString());
         lw.println("Using default. Line:" + read);
         return defaultVals;
      }
   }

   protected static File getWorldSaveDir(World world) {
      ISaveHandler worldSaver = world.getSaveHandler();
      return worldSaver.getChunkLoader(world.provider) instanceof AnvilChunkLoader
         ? ((AnvilChunkLoader)worldSaver.getChunkLoader(world.provider)).chunkSaveLocation
         : null;
   }

   private static File getMinecraftBaseDir() {
      return Loader.instance().getConfigDir().getAbsoluteFile().getParentFile();
   }

   private static boolean defaultsInstalled = false;

   /** Copies the bundled default settings/templates to config/generatormods when that folder does not exist yet. */
   public static synchronized void installDefaultConfigs() {
      if (defaultsInstalled) return;
      defaultsInstalled = true;
      if (CONFIG_DIRECTORY.exists()) return;
      try (java.io.InputStream idx = BuildingExplorationHandler.class.getResourceAsStream("/assets/walledcitymod/defaults/index.txt")) {
         if (idx == null) return;
         BufferedReader r = new BufferedReader(new java.io.InputStreamReader(idx, java.nio.charset.StandardCharsets.UTF_8));
         for (String line = r.readLine(); line != null; line = r.readLine()) {
            line = line.trim();
            if (line.isEmpty()) continue;
            File out = new File(CONFIG_DIRECTORY, line);
            out.getParentFile().mkdirs();
            try (java.io.InputStream in = BuildingExplorationHandler.class.getResourceAsStream("/assets/walledcitymod/defaults/" + line)) {
               if (in != null) java.nio.file.Files.copy(in, out.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
         }
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   static {
      BIOME_NAMES[0] = "Underground";
   }
}
