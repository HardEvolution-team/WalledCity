package com.electrifiedded.walledcitymod;

import com.electrifiedded.archaiclib.legacy.world.LegacyBlockIds;
import com.electrifiedded.archaiclib.legacy.world.LegacyWorld;
import com.electrifiedded.archaiclib.world.ChunkStagedGen;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

/**
 * World access used by the generators. Inside a {@link ChunkStagedGen} job every read/write goes to the staged
 * session (no chunk is ever loaded or generated for a structure; each chunk writes its part when it generates).
 */
public final class GenAccess {
    private GenAccess() {}

    private static ChunkStagedGen.Session s() { return ChunkStagedGen.current(); }

    public static IBlockState state(World w, int x, int y, int z) {
        ChunkStagedGen.Session s = s();
        BlockPos p = new BlockPos(x, y, z);
        return s != null ? s.getBlockState(p) : w.getBlockState(p);
    }

    public static Block getBlock(World w, int x, int y, int z) { return state(w, x, y, z).getBlock(); }

    public static int getBlockMetadata(World w, int x, int y, int z) {
        IBlockState st = state(w, x, y, z);
        return st.getBlock().getMetaFromState(st);
    }

    public static boolean isAirBlock(World w, int x, int y, int z) {
        ChunkStagedGen.Session s = s();
        if (s == null) return LegacyWorld.isAirBlock(w, x, y, z);
        IBlockState st = s.getBlockState(new BlockPos(x, y, z));
        return st.getMaterial() == net.minecraft.block.material.Material.AIR;
    }

    public static boolean setBlock(World w, int x, int y, int z, Block b) { return setBlock(w, x, y, z, b, 0, 3); }

    public static boolean setBlock(World w, int x, int y, int z, Block b, int meta, int flags) {
        ChunkStagedGen.Session s = s();
        if (s != null) return s.setBlockState(new BlockPos(x, y, z), LegacyBlockIds.state(b, meta));
        return w.setBlockState(new BlockPos(x, y, z), LegacyBlockIds.state(b, meta), flags | 2);
    }

    public static boolean setBlockToAir(World w, int x, int y, int z) { return setBlock(w, x, y, z, Blocks.AIR, 0, 3); }

    public static boolean setBlockMetadataWithNotify(World w, int x, int y, int z, int meta, int flags) {
        return setBlock(w, x, y, z, getBlock(w, x, y, z), meta, flags);
    }

    public static TileEntity getTileEntity(World w, int x, int y, int z) {
        ChunkStagedGen.Session s = s();
        BlockPos p = new BlockPos(x, y, z);
        return s != null ? s.getTileEntity(p) : w.getTileEntity(p);
    }

    /** Inside a job everything is "readable" (raw terrain for chunks that do not exist yet). */
    public static boolean blockExists(World w, int x, int y, int z) {
        return s() != null || LegacyWorld.blockExists(w, x, y, z);
    }

    public static int heightValue(World w, int x, int z) {
        ChunkStagedGen.Session s = s();
        return s != null ? s.getHeight(x, z) : w.getChunk(x >> 4, z >> 4).getHeightValue(x & 15, z & 15);
    }

    public static int getHeight(World w, int x, int z) {
        ChunkStagedGen.Session s = s();
        return s != null ? s.getHeight(x, z) : w.getHeight(x, z);
    }

    public static boolean canSeeSky(World w, BlockPos p) {
        ChunkStagedGen.Session s = s();
        return s != null ? s.canSeeSky(p) : w.canSeeSky(p);
    }

    public static int getLightFor(World w, EnumSkyBlock t, BlockPos p) {
        ChunkStagedGen.Session s = s();
        return s != null ? s.getLightFor(t, p) : w.getLightFor(t, p);
    }

    public static Biome biome(World w, int x, int z) {
        ChunkStagedGen.Session s = s();
        return s != null ? s.getBiome(x, z) : w.getBiome(new BlockPos(x, 0, z));
    }

    /** Server-thread work (entities, villages, chat) after the structure's blocks are committed. */
    public static void onMain(Runnable r) { ChunkStagedGen.onMain(r); }

    /** Torch placement check that never touches the World off-thread (vanilla canPlaceBlockAt loads chunks). */
    public static boolean canPlaceTorch(World w, int x, int y, int z) {
        if (s() == null) return Blocks.TORCH.canPlaceBlockAt(w, new BlockPos(x, y, z));
        if (state(w, x, y - 1, z).isTopSolid()) return true;
        int[][] d = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        for (int[] o : d) {
            IBlockState st = state(w, x + o[0], y, z + o[1]);
            if (st.isFullCube() && st.isOpaqueCube()) return true;
        }
        return false;
    }

    public static boolean staged() { return s() != null; }
}
