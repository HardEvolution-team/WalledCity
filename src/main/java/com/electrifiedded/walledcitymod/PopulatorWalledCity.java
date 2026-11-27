package com.electrifiedded.walledcitymod;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.village.Village;
import net.minecraft.village.VillageDoorInfo;
import net.minecraft.world.World;

@Mod(
   modid = "walledcitymod",
   name = "Walled City Generator",
   version = Reference.VERSION,
   dependencies = "required-after:archaiclib@[1.40.0,);after:biomesoplenty",
   acceptableRemoteVersions = "*"
)
public class PopulatorWalledCity extends BuildingExplorationHandler {
   @Instance("walledcitymod")
   public static PopulatorWalledCity instance;
   public static final int MIN_CITY_LENGTH = 40;
   static final int MAX_FOG_HEIGHT = 27;
   public static final int CITY_TYPE_UNDERGROUND = 1;
   private static final String CITY_FILE_SAVE = "WalledCities.txt";
   private static final String STREET_TEMPLATES_FOLDER_NAME = "streets";
   public float UndergroundGlobalFrequency = 0.015F;
   public int MinCitySeparation = 500;
   public int UndergroundMinCitySeparation = 500;
   public boolean CityBuiltMessage = false;
   public int BacktrackLength = 9;
   public boolean RejectOnPreexistingArtifacts = true;
   public List<TemplateWall> cityStyles = null;
   public List<TemplateWall> undergroundCityStyles = new ArrayList<>();
   public Map<World, List<int[]>> cityLocations;
   public Map<Integer, List<VillageDoorInfo>> cityDoors;
   public LinkedList<int[]> citiesBuiltMessages = new LinkedList<>();
   private Map<World, File> cityFiles;

   public void addCityToVillages(World world, int id) {
      if (GenAccess.staged()) {
         GenAccess.onMain(() -> this.addCityToVillages(world, id));
         return;
      }
      if (world != null && world.provider.getDimension() != 1 && world.getVillageCollection() != null) {
         Village city = new Village(world);
         if (this.cityDoors.containsKey(id)) {
            for (VillageDoorInfo door : this.cityDoors.get(id)) {
               if (door != null) {
                  city.addVillageDoorInfo(door);
               }
            }

            world.getVillageCollection().getVillageList().add(city);
            this.cityDoors.remove(id);
         }
      }
   }

   public void chatBuildingCity(String chatString, String logString) {
      if (GenAccess.staged()) {
         GenAccess.onMain(() -> this.chatBuildingCity(chatString, logString));
         return;
      }
      if (logString != null) {
         this.logOrPrint(logString, "FINEST");
      }

      if (this.CityBuiltMessage) {
         List<?> playerList = net.minecraftforge.fml.common.FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers();
         if (playerList != null) {
            for (int index = 0; index < playerList.size(); index++) {
               EntityPlayerMP player = (EntityPlayerMP)playerList.get(index);
               player.sendMessage(new TextComponentString(chatString));
            }
         }
      }
   }

   public void chatCityBuilt(int[] args) {
      if (GenAccess.staged()) {
         GenAccess.onMain(() -> this.chatCityBuilt(args));
         return;
      }
      if (this.CityBuiltMessage) {
         List<?> playerList = net.minecraftforge.fml.common.FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers();
         if (playerList == null) {
            this.citiesBuiltMessages.add(args);
         } else {
            for (int index = 0; index < playerList.size(); index++) {
               EntityPlayerMP player = (EntityPlayerMP)playerList.get(index);
               int dI = args[0] - (int)player.posX;
               int dK = args[2] - (int)player.posZ;
               if (dI * dI + dK * dK < args[4] * args[4]) {
                  String dirStr = "nearby";
               }

               String dirStr = "to the ";
               if (Math.abs(dI) > 2 * Math.abs(dK)) {
                  dirStr = dirStr + (dI > 0 ? "east" : "west");
               } else if (Math.abs(dK) > 2 * Math.abs(dI)) {
                  dirStr = dirStr + (dK > 0 ? "south" : "north");
               } else {
                  dirStr = dirStr + (dI > 0 ? (dK > 0 ? "southeast" : "northeast") : (dK > 0 ? "southwest" : "northwest"));
               }

               player.sendMessage(new TextComponentString("** Built city " + dirStr + " (" + args[0] + "," + args[1] + "," + args[2] + ")! **"));
            }
         }
      }
   }

   public boolean cityIsSeparated(World world, int i, int k, int cityType) {
      if (this.cityLocations.containsKey(world)) {
         for (int[] location : this.cityLocations.get(world)) {
            if (location[2] == cityType
               && Math.abs(location[0] - i) + Math.abs(location[1] - k) < (cityType == 1 ? this.UndergroundMinCitySeparation : this.MinCitySeparation)) {
               return false;
            }
         }
      }

      return true;
   }

   @Override
   public final void generate(World world, Random random, int i, int k) {
      if (this.CityBuiltMessage) {
         GenAccess.onMain(() -> {
            synchronized (this.citiesBuiltMessages) {
               while (this.citiesBuiltMessages.size() > 0) {
                  this.chatCityBuilt(this.citiesBuiltMessages.remove());
               }
            }
         });
      }

      if (this.cityStyles.size() > 0 && this.cityIsSeparated(world, i, k, world.provider.getDimension()) && random.nextFloat() < this.GlobalFrequency) {
         new WorldGenWalledCity(this, world, random, i, k, this.TriesPerChunk, this.GlobalFrequency).run();
      }

      if (this.undergroundCityStyles.size() > 0 && this.cityIsSeparated(world, i, k, 1) && random.nextFloat() < this.UndergroundGlobalFrequency) {
         WorldGeneratorThread wgt = new WorldGenUndergroundCity(this, world, random, i, k, 1, this.UndergroundGlobalFrequency);
         int maxSpawnHeight = Building.findSurfaceJ(world, i, k, 255, false, -1) - 15 - 5;
         int minSpawnHeight = 34;
         if (minSpawnHeight <= maxSpawnHeight) {
            wgt.setSpawnHeight(minSpawnHeight, maxSpawnHeight, false);
         }

         wgt.run();
      }
   }

   @Override
   public final void loadDataFiles() {
      try {
         this.initializeLogging("Loading options and templates for the Walled City Generator.");
         this.getGlobalOptions();
         File stylesDirectory = new File(CONFIG_DIRECTORY, this.templateFolderName);
         this.cityStyles = TemplateWall.loadWallStylesFromDir(stylesDirectory, this);
         TemplateWall.loadStreets(this.cityStyles, new File(stylesDirectory, "streets"), this);

         for (int m = 0; m < this.cityStyles.size(); m++) {
            if (this.cityStyles.get(m).underground) {
               TemplateWall uws = this.cityStyles.remove(m);
               uws.streets.add(uws);
               this.undergroundCityStyles.add(uws);
               m--;
            }
         }

         this.finalizeLoading(true, "city");
      } catch (Exception var7) {
         this.errFlag = true;
         this.lw.println("There was a problem loading the walled city mod: " + var7.getMessage());
         this.logOrPrint("There was a problem loading the walled city mod: " + var7.getMessage(), "SEVERE");
         var7.printStackTrace();
      } finally {
         if (this.lw != null) {
            this.lw.close();
         }
      }

      if (this.GlobalFrequency < 1.0E-6 && this.UndergroundGlobalFrequency < 1.0E-6) {
         this.errFlag = true;
      }

      this.dataFilesLoaded = true;
   }

   @Override
   public void loadGlobalOptions(BufferedReader br) {
      try {
         for (String read = br.readLine(); read != null; read = br.readLine()) {
            this.readGlobalOptions(this.lw, read);
            if (read.startsWith("UndergroundGlobalFrequency")) {
               this.UndergroundGlobalFrequency = readFloatParam(this.lw, this.UndergroundGlobalFrequency, ":", read);
            }

            if (read.startsWith("MinCitySeparation")) {
               this.MinCitySeparation = readIntParam(this.lw, this.MinCitySeparation, ":", read);
            }

            if (read.startsWith("MinUndergroundCitySeparation")) {
               this.UndergroundMinCitySeparation = readIntParam(this.lw, this.UndergroundMinCitySeparation, ":", read);
            }

            if (read.startsWith("BacktrackLength")) {
               this.BacktrackLength = readIntParam(this.lw, this.BacktrackLength, ":", read);
            }

            if (read.startsWith("CityBuiltMessage")) {
               this.CityBuiltMessage = readBooleanParam(this.lw, this.CityBuiltMessage, ":", read);
            }

            if (read.startsWith("RejectOnPreexistingArtifacts")) {
               this.RejectOnPreexistingArtifacts = readBooleanParam(this.lw, this.RejectOnPreexistingArtifacts, ":", read);
            }

            this.readChestItemsList(this.lw, read, br);
         }

         if (this.TriesPerChunk > 100) {
            this.TriesPerChunk = 100;
         }
      } catch (IOException var11) {
         this.lw.println(var11.getMessage());
      } finally {
         try {
            if (br != null) {
               br.close();
            }
         } catch (IOException var10) {
         }
      }
   }

   @EventHandler
   public void modsLoaded(FMLPostInitializationEvent event) {
      if (!this.dataFilesLoaded) {
         this.loadDataFiles();
      }

      if (!this.errFlag) {
         GameRegistry.registerWorldGenerator(this, 0);
      }
   }

   @EventHandler
   public void preInit(FMLPreInitializationEvent event) {
      com.electrifiedded.archaiclib.world.ChunkStagedGen.register();
      this.cityFiles = new HashMap<>();
      this.cityLocations = new HashMap<>();
      this.cityDoors = new HashMap<>();
      this.logger = event.getModLog();
      BuildingExplorationHandler.installDefaultConfigs();
      this.settingsFileName = "WalledCitySettings.txt";
      this.templateFolderName = "walledcity";
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

   public void saveCityLocations(World world) {
      PrintWriter pw = null;

      try {
         pw = new PrintWriter(new BufferedWriter(new FileWriter(this.cityFiles.get(world), true)));
         BufferedReader br = new BufferedReader(new FileReader(this.cityFiles.get(world)));
         if (br.readLine() == null) {
            pw.println("City locations in " + world.provider.getDimensionType().getName() + " of : " + world.getWorldInfo().getWorldName());
         }

         br.close();
         int[] location = this.cityLocations.get(world).get(this.cityLocations.get(world).size() - 1);
         pw.println(
            new StringBuilder(Integer.toString(location[0]))
               .append(",")
               .append(Integer.toString(location[1]))
               .append(",")
               .append(Integer.toString(location[2]))
         );
      } catch (IOException var8) {
         this.logOrPrint(var8.getMessage(), "WARNING");
      } finally {
         if (pw != null) {
            pw.close();
         }
      }
   }

   @Override
   public String toString() {
      return "WalledCityMod";
   }

   @Override
   public void updateWorldExplored(World world) {
      super.updateWorldExplored(world);
      File cityFile = new File(getWorldSaveDir(world), world.provider.getDimensionType().getName() + "WalledCities.txt");
      if (this.cityFiles.isEmpty() || !this.cityFiles.containsKey(world)) {
         this.cityFiles.put(world, cityFile);
      }

      try {
         if (!cityFile.createNewFile() && !this.cityLocations.containsKey(world)) {
            this.cityLocations.put(world, getCityLocs(cityFile));
         }
      } catch (IOException var4) {
         this.logOrPrint(var4.getMessage(), "WARNING");
      }
   }

   @Override
   public void writeGlobalOptions(PrintWriter pw) {
      this.printGlobalOptions(pw, false);
      pw.println(
         "<-GlobalFrequency/UndergroundGlobalFrequency controls how likely aboveground/belowground cities are to appear. Should be between 0.0 and 1.0. Lower to make less common->"
      );
      pw.println("GlobalFrequency:" + this.GlobalFrequency);
      pw.println("UndergroundGlobalFrequency:" + this.UndergroundGlobalFrequency);
      pw.println("<-MinCitySeparation/UndergroundMinCitySeparation define a minimum allowable separation between city spawns.->");
      pw.println("MinCitySeparation:" + this.MinCitySeparation);
      pw.println("MinUndergroundCitySeparation:" + this.UndergroundMinCitySeparation);
      pw.println();
      pw.println("<-BacktrackLength - length of backtracking for wall planning if a dead end is hit->");
      pw.println("BacktrackLength:" + this.BacktrackLength);
      pw.println("<-CityBuiltMessage controls whether players receive message when a city is building. Set to true to receive message.->");
      pw.println("CityBuiltMessage:" + this.CityBuiltMessage);
      pw.println(
         "<-RejectOnPreexistingArtifacts determines whether the planner rejects city sites that contain preexiting man-made blocks. Set to true to do this check.->"
      );
      pw.println("RejectOnPreexistingArtifacts:" + this.RejectOnPreexistingArtifacts);
      pw.println();
      this.printDefaultChestItems(pw);
      if (pw != null) {
         pw.close();
      }
   }

   public static List<int[]> getCityLocs(File city) {
      List<int[]> cityLocs = new ArrayList<>();
      BufferedReader br = null;

      try {
         br = new BufferedReader(new FileReader(city));

         for (String read = br.readLine(); read != null; read = br.readLine()) {
            String[] split = read.split(",");
            if (split.length == 3) {
               cityLocs.add(new int[]{Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2])});
            }
         }
      } catch (IOException var13) {
         System.err.println(var13.getMessage());
      } finally {
         try {
            if (br != null) {
               br.close();
            }
         } catch (IOException var12) {
         }
      }

      return cityLocs;
   }
}
