package com.electrifiedded.walledcitymod;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class TemplateTML {
   public static final String NO_WATER_CHECK_STR = "NO_WATER_CHECK";
   public static final int NO_WATER_CHECK = -666;
   public static final int TML_CODE = 0;
   public static final int DEFAULT_TOWER_CODE = 1;
   public static final int CA_RUIN_CODE = 2;
   public static final int MAX_NO_BUILDOVER_LAYOUT_AREA = 120;
   public static final Exception ZERO_WEIGHT_EXCEPTION = new Exception("weight=0");
   public TemplateRule[] rules = null;
   public int[][][] template = (int[][][])null;
   public boolean[][] templateLayout = (boolean[][])null;
   public boolean buildOverStreets = false;
   public HashMap<String, int[][]> namedLayers = null;
   public HashMap<String, String> extraOptions = null;
   public BuildingExplorationHandler explorationHandler = null;
   public String name = "";
   protected boolean readInWaterHeight = false;
   public int templateTypeCode = 0;
   public int height = 0;
   public int length = 0;
   public int width = 0;
   public int weight = 1;
   public int embed = 1;
   public int leveling = 4;
   public int cutIn = 0;
   public int waterHeight = 3;
   PrintWriter lw;

   public TemplateTML(File file, BuildingExplorationHandler beh) throws Exception {
      this.explorationHandler = beh;
      this.name = file.getName();
      this.lw = beh.lw;
      ArrayList<String> lines = new ArrayList<>();
      BufferedReader br = new BufferedReader(new FileReader(file));

      try {
         for (String read = br.readLine(); read != null; read = br.readLine()) {
            lines.add(read);
         }

         br.close();
      } catch (IOException var6) {
      }

      this.parseFile(lines);
      this.lw.println("Successfully loaded template " + this.name + " with weight " + this.weight + ".");
   }

   public TemplateTML(int code, int weight_) {
      this.templateTypeCode = code;
      this.weight = weight_;
   }

   public void setFixedRules(Random random) {
      for (TemplateRule rule : this.rules) {
         rule.setFixedRule(random);
      }
   }

   private void parseFile(ArrayList<String> lines) throws Exception {
      this.namedLayers = new HashMap<>();
      ArrayList<int[][]> layers = new ArrayList<>();
      ArrayList<TemplateRule> rulesArrayList = new ArrayList<>();
      this.extraOptions = new HashMap<>();
      rulesArrayList.add(TemplateRule.AIR_RULE);
      Iterator<String> itr = lines.iterator();
      int layerN = 0;

      while (itr.hasNext()) {
         String[] splitStr = itr.next().split("#");
         String line = splitStr.length > 0 ? splitStr[0].trim() : "";
         if (line.startsWith("layer")) {
            String[] layerData = line.split(":");
            if (layerData.length == 1) {
               layers.add(new int[this.length][this.width]);

               try {
                  this.parseLayer(itr, layers.get(layers.size() - 1), true);
               } catch (Exception var11) {
                  throw new Exception("Error reading layer " + layerN + ": " + var11.toString());
               }

               layerN++;
            } else if (layerData.length == 2) {
               this.namedLayers.put(layerData[1], new int[this.length][this.width]);

               try {
                  this.parseLayer(itr, this.namedLayers.get(layerData[1]), false);
               } catch (Exception var10) {
                  throw new Exception("Error reading layer \"" + layerData[1] + "\": " + var10.toString());
               }
            }
         } else if (line.startsWith("rule")) {
            String[] parts = line.split("=");
            rulesArrayList.add(new TemplateRule(parts[1], true));
         } else if (line.startsWith("dimensions")) {
            Integer[] dim = BuildingExplorationHandler.readIntList(this.lw, null, "=", line);
            if (dim == null || dim.length != 3) {
               throw new Exception("Bad dimension input!" + line);
            }

            this.height = dim[0];
            this.length = dim[1];
            this.width = dim[2];
         } else if (line.startsWith("weight")) {
            this.weight = BuildingExplorationHandler.readIntParam(this.lw, this.weight, "=", line);
            if (this.weight <= 0) {
               throw ZERO_WEIGHT_EXCEPTION;
            }
         } else if (line.startsWith("embed_into_distance")) {
            this.embed = BuildingExplorationHandler.readIntParam(this.lw, this.embed, "=", line);
         } else if (line.startsWith("max_cut_in")) {
            this.cutIn = BuildingExplorationHandler.readIntParam(this.lw, this.cutIn, "=", line);
         } else if (line.startsWith("max_leveling")) {
            this.leveling = BuildingExplorationHandler.readIntParam(this.lw, this.leveling, "=", line);
         } else if (line.startsWith("water_height")) {
            this.readInWaterHeight = true;
            if (line.contains("NO_WATER_CHECK")) {
               this.waterHeight = -666;
            } else {
               this.waterHeight = BuildingExplorationHandler.readIntParam(this.lw, this.waterHeight, "=", line);
            }
         } else if (line.length() > 0) {
            String[] spl = line.split("=");
            if (spl.length == 2 && !spl[0].equals("") && !spl[1].equals("")) {
               this.extraOptions.put(spl[0], line);
            }
         }
      }

      this.waterHeight = this.waterHeight - this.embed;
      if (layers.size() == 0) {
         throw new Exception("No layers provided!");
      } else {
         if (layers.size() != this.height) {
            this.lw.println("\nWarning, number of layers provided " + layers.size() + " did not equal height=" + this.height + ".");
            this.height = layers.size();
         }

         this.template = new int[this.height][this.length][this.width];
         this.template = layers.toArray(this.template);
         this.rules = new TemplateRule[rulesArrayList.size()];
         this.rules = rulesArrayList.toArray(this.rules);

         for (int z = 0; z < this.height; z++) {
            for (int x = 0; x < this.length; x++) {
               for (int y = 0; y < this.width; y++) {
                  if (this.template[z][x][y] >= this.rules.length || this.template[z][x][y] < 0) {
                     throw new Exception("No rule provided for rule at (" + z + "," + x + "," + y + "): " + this.template[z][x][y] + " in template!");
                  }
               }
            }
         }
      }
   }

   private void parseLayer(Iterator<String> itr, int[][] layer, boolean isFixedLength) throws Exception {
      int lengthN = this.length - 1;

      for (String line = itr.next(); itr.hasNext() && !line.startsWith("endlayer"); line = itr.next()) {
         if (lengthN < 0) {
            throw new Exception("# of lines in layer was > length=" + this.length + " specified in dimensions.");
         }

         if (line.charAt(0) != '#') {
            String[] rowdata = line.split(",");
            if (rowdata.length != this.width) {
               throw new Exception("\nlength of line \"" + line + "\" did not match width=" + this.width + " specified in dimensions.");
            }

            for (int y = 0; y < this.width; y++) {
               layer[lengthN][y] = Integer.parseInt(rowdata[y]);
            }
         }

         lengthN--;
      }

      if (lengthN >= 0 && isFixedLength) {
         throw new Exception("# of lines in layer was  < length=" + this.length + " specified in dimensions.");
      }
   }

   public TemplateTML buildLayout() {
      int layoutLayer = this.embed >= 0 && this.embed < this.height ? this.embed : 0;
      int layoutArea = 0;
      this.templateLayout = new boolean[this.length][this.width];

      for (int y = 0; y < this.length; y++) {
         for (int x = 0; x < this.width; x++) {
            this.templateLayout[y][x] = !this.rules[this.template[layoutLayer][y][x]].isPreserveRule();
            if (this.templateLayout[y][x]) {
               layoutArea++;
            }
         }
      }

      this.buildOverStreets = layoutArea > 120;
      return this;
   }

   public static int[][] buildWeightsAndIndex(List<TemplateTML> buildings) throws Exception {
      int[][] weightsAndIndex = new int[2][buildings.size()];
      int sum = 0;
      Iterator<TemplateTML> itr = buildings.iterator();

      for (int m = 0; itr.hasNext(); weightsAndIndex[1][m] = m++) {
         TemplateTML temp = itr.next();
         weightsAndIndex[0][m] = temp.weight;
         sum += temp.weight;
      }

      if (sum == 0) {
         throw ZERO_WEIGHT_EXCEPTION;
      } else {
         return weightsAndIndex;
      }
   }

   public void printTemplate() {
      this.explorationHandler.logOrPrint("TEMPLATE - " + this.name, "CONFIG");

      for (int z = 0; z < this.height; z++) {
         for (int y = 0; y < this.length; y++) {
            for (int x = 0; x < this.width; x++) {
               this.explorationHandler.logOrPrint(this.template[z][x][this.length - y - 1] + ",", "CONFIG");
            }
         }

         this.explorationHandler.logOrPrint("endlayer\n", "CONFIG");
      }
   }
}
