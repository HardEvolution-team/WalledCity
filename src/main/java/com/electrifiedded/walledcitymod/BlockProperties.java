package com.electrifiedded.walledcitymod;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockButton;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockDynamicLiquid;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.BlockFire;
import net.minecraft.block.BlockFurnace;
import net.minecraft.block.BlockGlowstone;
import net.minecraft.block.BlockGrass;
import net.minecraft.block.BlockGravel;
import net.minecraft.block.BlockHugeMushroom;
import net.minecraft.block.BlockLadder;
import net.minecraft.block.BlockLever;
import net.minecraft.block.BlockLog;
import net.minecraft.block.BlockMelon;
import net.minecraft.block.BlockMycelium;
import net.minecraft.block.BlockNetherrack;
import net.minecraft.block.BlockOre;
import net.minecraft.block.BlockPumpkin;
import net.minecraft.block.BlockRedstoneOre;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.block.BlockSand;
import net.minecraft.block.BlockSign;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.BlockSoulSand;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockTorch;
import net.minecraft.block.BlockVine;
import net.minecraft.block.BlockWeb;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.IShearable;

public class BlockProperties {
   private static final Map<Block, BlockProperties> props = new HashMap<>();
   public final boolean isWater;
   public final boolean isStair;
   public final boolean isDoor;
   public final boolean isTree;
   public final boolean isFlowing;
   public final boolean isWallable;
   public final boolean isOre;
   public final boolean isGround;
   public final boolean isArtificial;
   public final boolean isLight;
   public final boolean isLoaded;
   public final boolean isDelayed;

   public BlockProperties(Block block) {
      this.isWater = block.getDefaultState().getMaterial() == Material.WATER || block == Blocks.ICE;
      this.isStair = block instanceof BlockStairs;
      this.isDoor = block instanceof BlockDoor;
      this.isTree = block instanceof BlockLog || block instanceof IShearable || block instanceof BlockSnow;
      this.isFlowing = this.isWater || block.getDefaultState().getMaterial() == Material.LAVA || block instanceof BlockDynamicLiquid || block instanceof BlockFalling;
      this.isWallable = this.isWater
         || block instanceof BlockAir
         || this.isTree
         || block instanceof BlockWeb
         || block instanceof BlockPumpkin
         || block instanceof BlockMelon
         || block instanceof BlockHugeMushroom
         || block instanceof IPlantable;
      this.isOre = block == Blocks.CLAY || block instanceof BlockRedstoneOre || block instanceof BlockOre;
      this.isGround = block == Blocks.STONE
         || block instanceof BlockDirt
         || block instanceof BlockGrass
         || block instanceof BlockGravel
         || block instanceof BlockSand
         || block instanceof BlockNetherrack
         || block instanceof BlockSoulSand
         || block instanceof BlockMycelium;
      this.isArtificial = !this.isWallable && !this.isOre && !this.isGround;
      this.isLight = block instanceof BlockTorch || block instanceof BlockGlowstone;
      this.isDelayed = this.isStair
         || this.isFlowing
         || this.isLight
         || block == Blocks.AIR
         || block instanceof BlockLever
         || block instanceof BlockSign
         || block instanceof BlockFire
         || block instanceof BlockButton
         || block instanceof BlockVine
         || block instanceof BlockRedstoneWire
         || block instanceof BlockDispenser
         || block instanceof BlockFurnace;
      this.isLoaded = !this.isWallable && !this.isFlowing && !(block instanceof BlockTorch) && !(block instanceof BlockLadder);
      props.put(block, this);
   }

   public static BlockProperties get(Block block) {
      BlockProperties p = props.get(block);
      return p != null ? p : new BlockProperties(block);
   }
}
