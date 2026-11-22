package com.electrifiedded.walledcitymod;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

/** 1.7 GameData / misc helpers used by the generator code. */
public final class Compat {
    private Compat() {}

    public static Object itemOrBlock(int id) {
        Item it = Item.getItemById(id);
        if (it != null && it != Items.AIR) return it;
        Block b = Block.getBlockById(id);
        return b == Blocks.AIR && id != 0 ? null : b;
    }

    public static Object itemOrBlock(String name) {
        ResourceLocation rl = new ResourceLocation(name);
        if (ForgeRegistries.ITEMS.containsKey(rl)) return ForgeRegistries.ITEMS.getValue(rl);
        if (ForgeRegistries.BLOCKS.containsKey(rl)) return ForgeRegistries.BLOCKS.getValue(rl);
        return null;
    }

    public static Block block(int id) {
        Block b = Block.getBlockById(id);
        return b == Blocks.AIR && id != 0 ? null : b;
    }

    public static Block block(String name) {
        ResourceLocation rl = new ResourceLocation(name);
        return ForgeRegistries.BLOCKS.containsKey(rl) ? ForgeRegistries.BLOCKS.getValue(rl) : null;
    }

    public static String name(Object o) {
        ResourceLocation rl = null;
        if (o instanceof Item) rl = ((Item) o).getRegistryName();
        else if (o instanceof Block) rl = ((Block) o).getRegistryName();
        return rl == null ? null : rl.toString();
    }

    public static String biomeName(Biome b) {
        return b == null ? null : com.electrifiedded.archaiclib.legacy.world.LegacyBiomes.getName(b);
    }

    public static Biome biome(World w, int x, int z) {
        return GenAccess.biome(w, x, z);
    }

    public static boolean waterVaporizes(World w) {
        return w.provider.doesWaterVaporize();
    }

    public static void setSpawner(net.minecraft.tileentity.TileEntityMobSpawner s, String name) {
        net.minecraftforge.fml.common.registry.EntityEntry e = com.electrifiedded.archaiclib.legacy.entity.LegacyEntityList.getEntryByName(name);
        ResourceLocation rl = e != null ? e.getRegistryName() : new ResourceLocation(name.toLowerCase(java.util.Locale.ROOT));
        s.getSpawnerBaseLogic().setEntityId(rl);
    }

    /** ItemStack from a 1.7 (item, count, damage) triple; legacy potion damage values become 1.12 potion types. */
    public static net.minecraft.item.ItemStack stack(Item item, int count, int meta) {
        if (item == Items.POTIONITEM) {
            return com.electrifiedded.archaiclib.legacy.item.LegacyPotions.stack(meta, count);
        }
        return new net.minecraft.item.ItemStack(item, count, meta);
    }
}
