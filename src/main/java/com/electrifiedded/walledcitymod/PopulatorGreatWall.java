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
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;
import net.minecraft.world.World;

@Mod(modid = "greatwallmod", name = "Great Wall Mod", version = Reference.VERSION, dependencies = "required-after:archaiclib@[1.36.0,);after:biomesoplenty", acceptableRemoteVersions = "*")
public class PopulatorGreatWall extends BuildingExplorationHandler {
   @Instance("greatwallmod")
   public static PopulatorGreatWall instance;
   public float CurveBias = 0.5F;
   public int LengthBiasNorm = 200;
   public int BacktrackLength = 9;
   public ArrayList<TemplateWall> wallStyles = null;
   public int[] placedCoords = null;
   public World placedWorld = null;

   @EventHandler
   public void preInit(FMLPreInitializationEvent event) {
      com.electrifiedded.archaiclib.world.ChunkStagedGen.register();
      this.logger = event.getModLog();
      BuildingExplorationHandler.installDefaultConfigs();
      this.settingsFileName = "GreatWallSettings.txt";
      this.templateFolderName = "greatwall";
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

   @Override
   public final void loadDataFiles() {
      try {
         this.initializeLogging("Loading options and templates for the Great Wall Mod.");
         this.getGlobalOptions();
         File stylesDirectory = new File(CONFIG_DIRECTORY, this.templateFolderName);
         this.wallStyles = TemplateWall.loadWallStylesFromDir(stylesDirectory, this);
         this.finalizeLoading(true, "wall");
      } catch (Exception var5) {
         this.errFlag = true;
         this.logOrPrint("There was a problem loading the great wall mod: " + var5.getMessage(), "SEVERE");
         this.lw.println("There was a problem loading the great wall mod: " + var5.getMessage());
         var5.printStackTrace();
      } finally {
         if (this.lw != null) {
            this.lw.close();
         }
      }

      if (this.GlobalFrequency < 1.0E-6) {
         this.errFlag = true;
      }

      this.dataFilesLoaded = true;
   }

   @Override
   public final void generate(World world, Random random, int i, int k) {
      if (random.nextFloat() < this.GlobalFrequency) {
         new WorldGenGreatWall(this, world, random, i, k, this.TriesPerChunk, this.GlobalFrequency).run();
      }
   }

   @Override
   public void loadGlobalOptions(BufferedReader br) {
      try {
         for (String read = br.readLine(); read != null; read = br.readLine()) {
            this.readGlobalOptions(this.lw, read);
            if (read.startsWith("CurveBias")) {
               this.CurveBias = readFloatParam(this.lw, this.CurveBias, ":", read);
            }

            if (read.startsWith("LengthBiasNorm")) {
               this.LengthBiasNorm = readIntParam(this.lw, this.LengthBiasNorm, ":", read);
            }

            if (read.startsWith("BacktrackLength")) {
               this.BacktrackLength = readIntParam(this.lw, this.BacktrackLength, ":", read);
            }

            this.readChestItemsList(this.lw, read, br);
         }

         if (this.TriesPerChunk > 100) {
            this.TriesPerChunk = 100;
         }

         if (this.CurveBias < 0.0) {
            this.CurveBias = 0.0F;
         }

         if (this.CurveBias > 1.0) {
            this.CurveBias = 1.0F;
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

   @Override
   public void writeGlobalOptions(PrintWriter pw) {
      this.printGlobalOptions(pw, true);
      pw.println();
      pw.println("<-BacktrackLength - length of backtracking for wall planning if a dead end is hit->");
      pw.println("<-CurveBias - strength of the bias towards curvier walls. Value should be between 0.0 and 1.0.->");
      pw.println("<-LengthBiasNorm - wall length at which there is no penalty for generation>");
      pw.println("BacktrackLength:" + this.BacktrackLength);
      pw.println("CurveBias:" + this.CurveBias);
      pw.println("LengthBiasNorm:" + this.LengthBiasNorm);
      pw.println();
      this.printDefaultChestItems(pw);
      if (pw != null) {
         pw.close();
      }
   }

   @Override
   public String toString() {
      return "GreatWallMod";
   }

   @EventHandler
   public void modsLoaded(FMLPostInitializationEvent event) {
      if (!this.dataFilesLoaded) {
         this.loadDataFiles();
      }

      if (!this.errFlag) {
         GameRegistry.registerWorldGenerator(this, 1);
      }
   }
}
