package com.electrifiedded.walledcitymod;

import com.electrifiedded.archaiclib.legacy.world.LegacyWorld;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import net.minecraft.command.CommandBase;
import net.minecraft.server.MinecraftServer;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;

public class CommandScan extends CommandBase {
   public final String[] modes = new String[]{"wall", "building"};

   public boolean checkPermission(MinecraftServer server, ICommandSender commandSender) {
      return commandSender instanceof EntityPlayer;
   }

   public String getName() {
      return "scantml";
   }

   public String getUsage(ICommandSender var1) {
      return "/" + this.getName() + " <templatetype:wall/building> <templatename> <x0,x1> <y0,y1> <z0,z1>";
   }

   public void execute(MinecraftServer server, ICommandSender var1, String[] command) throws net.minecraft.command.CommandException {
      if (command.length == 5) {
         int a = parseInt(command[2].split(",")[0]);
         int b = parseInt(command[2].split(",")[1]);
         int maxX = Math.max(a, b);
         int minX = Math.min(a, b);
         a = parseInt(command[3].split(",")[0]);
         b = parseInt(command[3].split(",")[1]);
         int maxY = Math.max(a, b);
         int minY = Math.min(a, b);
         a = parseInt(command[4].split(",")[0]);
         b = parseInt(command[4].split(",")[1]);
         int maxZ = Math.max(a, b);
         int minZ = Math.min(a, b);
         File template = new File(BuildingExplorationHandler.CONFIG_DIRECTORY, command[1] + ".tml");

         try {
            if (template.createNewFile()) {
               List<BlockAndMeta> blocks = new ArrayList<>();
               blocks.add(new BlockAndMeta(Blocks.AIR, 0));
               HashMap<Integer, List<Integer>> layers = new HashMap<>();
               List<String> rules = new ArrayList<>();
               HashSet<String> biomes = new HashSet<>();
               int SIZE_2 = maxZ - minZ + 1;
               int SIZE_0;
               int SIZE_1;
               if (command[0].equals(this.modes[1])) {
                  SIZE_0 = maxY - minY + 1;
                  SIZE_1 = maxX - minX + 1;

                  for (int y = minY; y <= maxY; y++) {
                     List<Integer> temp = new ArrayList<>(SIZE_1 * SIZE_2);

                     for (int x = minX; x <= maxX; x++) {
                        for (int z = minZ; z <= maxZ; z++) {
                           BlockAndMeta blc = new BlockAndMeta(LegacyWorld.getBlock(var1.getEntityWorld(), x, y, z), LegacyWorld.getBlockMetadata(var1.getEntityWorld(), x, y, z));
                           if (!blocks.contains(blc)) {
                              rules.add("rule" + blocks.size() + "=0,100," + Compat.name(blc.get()) + "-" + blc.getMeta());
                              blocks.add(blc);
                           }

                           temp.add(blocks.indexOf(blc));
                           if (y == minY) {
                              biomes.add(com.electrifiedded.archaiclib.legacy.world.LegacyBiomes.getName(Compat.biome(var1.getEntityWorld(), x, z)));
                           }
                        }
                     }

                     layers.put(y - minY, temp);
                  }
               } else {
                  if (!command[0].equals(this.modes[0])) {
                     throw new WrongUsageException(this.getUsage(var1), new Object[0]);
                  }

                  SIZE_0 = maxX - minX + 1;
                  SIZE_1 = maxY - minY + 1;

                  for (int x = minX; x <= maxX; x++) {
                     List<Integer> temp = new ArrayList<>(SIZE_1 * SIZE_2);

                     for (int z = minZ; z <= maxZ; z++) {
                        biomes.add(com.electrifiedded.archaiclib.legacy.world.LegacyBiomes.getName(Compat.biome(var1.getEntityWorld(), x, z)));

                        for (int y = minY; y <= maxY; y++) {
                           BlockAndMeta blcx = new BlockAndMeta(LegacyWorld.getBlock(var1.getEntityWorld(), x, y, z), LegacyWorld.getBlockMetadata(var1.getEntityWorld(), x, y, z));
                           if (!blocks.contains(blcx)) {
                              rules.add("rule" + blocks.size() + "=0,100," + Compat.name(blcx.get()) + "-" + blcx.getMeta());
                              blocks.add(blcx);
                           }

                           temp.add(blocks.indexOf(blcx));
                        }
                     }

                     layers.put(x - minX, temp);
                  }
               }

               PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(template)));
               writer.println("# Author: " + var1.getName());
               writer.println("# Generated by in-game template scanner");
               writer.println("weight=1");
               writer.println("embed_into_distance=0");
               writer.println("max_leveling=2");
               writer.println("dimensions=" + SIZE_0 + "," + SIZE_1 + "," + SIZE_2);
               StringBuffer buf = new StringBuffer("biomes=");

               for (String biome : biomes) {
                  buf.append(biome).append(",");
               }

               buf.deleteCharAt(buf.length() - 1);
               writer.println(buf.toString());
               if (command[0].equals(this.modes[0])) {
                  writer.println("building_templates=NONE");
                  writer.println("min_length=100");
                  writer.println("building_interval=45");
                  writer.println("max_length=300");
                  writer.println("make_buildings=1");
                  writer.println("make_end_towers=1");
                  writer.println("tower_rule=1");
                  writer.println("default_tower_weight=1");
                  writer.println("spawner_rule=0,10,52-0-Pig");
                  writer.println("spawner_count=1");
                  writer.println("circular_probability=0.25");
                  writer.println("square_min_height=10");
                  writer.println("square_max_height=10");
                  writer.println("square_min_width=7");
                  writer.println("square_max_width=7");
                  writer.println("square_roof_styles=Crenel,Steep");
                  writer.println("circular_tower_min_height=8");
                  writer.println("circular_tower_max_height=15");
                  writer.println("circular_tower_min_width=10");
                  writer.println("circular_tower_max_width=10");
                  writer.println("circular_tower_roof_styles=Crenel,Dome");
               }

               for (String rule : rules) {
                  writer.println(rule);
               }

               for (int index : layers.keySet()) {
                  writer.println("layer");

                  for (int j = 0; j < SIZE_1; j++) {
                     buf = new StringBuffer();

                     for (int i = 0; i < SIZE_2; i++) {
                        int id = layers.get(index).get(i + j * SIZE_2);
                        buf.append(id);
                        if (i < SIZE_2 - 1) {
                           buf.append(",");
                        }
                     }

                     writer.println(buf.toString());
                  }

                  writer.println("endlayer");
               }

               writer.close();
               notifyCommandListener(var1, this, "Template scanner used by " + var1.getName(), new Object[]{var1.getName(), command});
            }
         } catch (IOException var26) {
         }
      } else {
         throw new WrongUsageException(this.getUsage(var1), new Object[0]);
      }
   }

   public List<String> getTabCompletions(MinecraftServer server, ICommandSender var1, String[] var2, net.minecraft.util.math.BlockPos pos) {
      return var2.length == 1 ? getListOfStringsMatchingLastWord(var2, this.modes) : (var2.length == 2 ? getListOfStringsMatchingLastWord(var2, "TemplateDefault") : null);
   }

}
