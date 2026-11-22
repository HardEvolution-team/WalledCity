package com.electrifiedded.walledcitymod;

import java.util.List;
import java.util.Random;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

public class CommandBuild extends CommandBase {
   public int getRequiredPermissionLevel() {
      return 2;
   }

   public String getName() {
      return "build";
   }

   public String getUsage(ICommandSender commandSender) {
      return "/" + this.getName() + " <ruin:wall:city:undcity> <dimensionID,default:0> <x> <z>";
   }

   public List<String> getAliases() {
      return java.util.Collections.emptyList();
   }

   public void execute(MinecraftServer server, ICommandSender var1, String[] coordinate) throws net.minecraft.command.CommandException {
      if (!server.isCallingFromMinecraftThread()) {
         server.addScheduledTask(() -> {
            try {
               this.execute(server, var1, coordinate);
            } catch (net.minecraft.command.CommandException e) {
               var1.sendMessage(new net.minecraft.util.text.TextComponentString(e.getMessage()));
            }
         });
         return;
      }

      if (coordinate.length != 4 && coordinate.length != 3) {
         throw new WrongUsageException(this.getUsage(var1), new Object[0]);
      } else {
         notifyCommandListener(var1, this, "/build command used by " + var1.getName(), new Object[]{var1.getName(), coordinate});
         int posX = parseInt(coordinate[coordinate.length - 2]);
         int posZ = parseInt(coordinate[coordinate.length - 1]);
         World world = server.worlds[coordinate.length == 3 ? 0 : Integer.parseInt(coordinate[1])];
         if ("ruin".equalsIgnoreCase(coordinate[0])) {
            com.electrifiedded.archaiclib.world.ChunkStagedGen.submit(world, () -> new WorldGenCARuins(PopulatorCARuins.instance, world, new Random(), posX, posZ, 1, 1.0).run());
         } else if ("wall".equalsIgnoreCase(coordinate[0])) {
            com.electrifiedded.archaiclib.world.ChunkStagedGen.submit(world, () -> new WorldGenGreatWall(PopulatorGreatWall.instance, world, new Random(), posX, posZ, 1, 1.0).run());
         } else if ("city".equalsIgnoreCase(coordinate[0])) {
            com.electrifiedded.archaiclib.world.ChunkStagedGen.submit(world, () -> new WorldGenWalledCity(PopulatorWalledCity.instance, world, new Random(), posX, posZ, 1, 1.0).run());
         } else if ("undcity".equalsIgnoreCase(coordinate[0])) {
            com.electrifiedded.archaiclib.world.ChunkStagedGen.submit(world, () -> {
            PopulatorWalledCity city = PopulatorWalledCity.instance;
            WorldGeneratorThread wgt = new WorldGenUndergroundCity(city, world, new Random(), posX, posZ, 1, 1.0);
            int maxSpawnHeight = Building.findSurfaceJ(world, posX, posZ, 255, false, -1) - 15 - 5;
            int minSpawnHeight = 34;
            if (minSpawnHeight <= maxSpawnHeight) {
               wgt.setSpawnHeight(minSpawnHeight, maxSpawnHeight, false);
            }

            wgt.run();
            });
         }
      }
   }

   public List<String> getTabCompletions(MinecraftServer server, ICommandSender var1, String[] var2, net.minecraft.util.math.BlockPos pos) {
      return var2.length == 1 ? getListOfStringsMatchingLastWord(var2, "ruin", "wall", "city", "undcity") : java.util.Collections.emptyList();
   }

}
