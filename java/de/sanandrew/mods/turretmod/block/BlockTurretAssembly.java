/**
 * ****************************************************************************************************************
 * Authors:   SanAndreasP
 * Copyright: SanAndreasP
 * License:   Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
 * http://creativecommons.org/licenses/by-nc-sa/4.0/
 * *****************************************************************************************************************
 */
package de.sanandrew.mods.turretmod.block;

import cofh.api.item.IToolHammer;
import de.sanandrew.mods.turretmod.tileentity.TileEntityTurretAssembly;
import de.sanandrew.mods.turretmod.util.EnumGui;
import de.sanandrew.mods.turretmod.util.TmrCreativeTabs;
import de.sanandrew.mods.turretmod.util.TmrUtils;
import de.sanandrew.mods.turretmod.util.TurretModRebirth;
import net.darkhax.bookshelf.lib.util.ItemStackUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockTurretAssembly
        extends Block
{
    public static final PropertyDirection FACING = BlockHorizontal.FACING;

    protected BlockTurretAssembly() {
        super(Material.ROCK);
        this.setCreativeTab(TmrCreativeTabs.TURRETS);
        this.setHardness(4.25F);
        this.blockSoundType = SoundType.STONE;
        this.setUnlocalizedName(TurretModRebirth.ID + ":turret_assembly");
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
        if( !world.isRemote ) {
            ItemStack held = player.getHeldItemMainhand();
            if( ItemStackUtils.isValidStack(held) && held.getItem() instanceof IToolHammer && !player.isSneaking() ) {
//                int meta = (world.getBlockMetadata(x, y, z) + 1) & 3;
//                world.setBlockMetadataWithNotify(x, y, z, meta, 2);
            } else {
                TurretModRebirth.proxy.openGui(player, EnumGui.GUI_TASSEMBLY_MAN, pos.getX(), pos.getY(), pos.getZ());
            }
        }

        return true;
    }

    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
        this.setDefaultFacing(worldIn, pos, state);
    }

    private void setDefaultFacing(World world, BlockPos pos, IBlockState state) {
        if( !world.isRemote ) {
            IBlockState northState = world.getBlockState(pos.north());
            IBlockState southState = world.getBlockState(pos.south());
            IBlockState westState = world.getBlockState(pos.west());
            IBlockState eastState = world.getBlockState(pos.east());
            EnumFacing facing = state.getValue(FACING);

            if( facing == EnumFacing.NORTH && northState.isFullBlock() && !southState.isFullBlock() ) {
                facing = EnumFacing.SOUTH;
            } else if( facing == EnumFacing.SOUTH && southState.isFullBlock() && !northState.isFullBlock() ) {
                facing = EnumFacing.NORTH;
            } else if( facing == EnumFacing.WEST && westState.isFullBlock() && !eastState.isFullBlock() ) {
                facing = EnumFacing.EAST;
            } else if( facing == EnumFacing.EAST && eastState.isFullBlock() && !westState.isFullBlock() ) {
                facing = EnumFacing.WEST;
            }

            world.setBlockState(pos, state.withProperty(FACING, facing), 2);
        }
    }

    @Override
    public IBlockState onBlockPlaced(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        world.setBlockState(pos, state.withProperty(FACING, placer.getHorizontalFacing().getOpposite()), 2);

        if( stack.hasDisplayName() ) {
            TileEntity tileentity = world.getTileEntity(pos);

            if( tileentity instanceof TileEntityTurretAssembly ) {
                ((TileEntityTurretAssembly)tileentity).setCustomName(stack.getDisplayName());
            }
        }
//        int dir = MathHelper.floor_double((placer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
//
//        world.setBlockMetadataWithNotify(x, y, z, dir, 2);
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        TileEntityTurretAssembly assembly = (TileEntityTurretAssembly) world.getTileEntity(pos);

        if( assembly != null ) {
            for( int i = 0; i < assembly.getSizeInventory(); i++ ) {
                ItemStack stack = assembly.getStackInSlot(i);

                if( ItemStackUtils.isValidStack(stack) ) {
                    float xOff = TmrUtils.RNG.nextFloat() * 0.8F + 0.1F;
                    float yOff = TmrUtils.RNG.nextFloat() * 0.8F + 0.1F;
                    float zOff = TmrUtils.RNG.nextFloat() * 0.8F + 0.1F;

                    EntityItem entityitem = new EntityItem(world, (pos.getX() + xOff), (pos.getY() + yOff), (pos.getZ() + zOff), stack.copy());

                    float motionSpeed = 0.05F;
                    entityitem.motionX = ((float)TmrUtils.RNG.nextGaussian() * motionSpeed);
                    entityitem.motionY = ((float)TmrUtils.RNG.nextGaussian() * motionSpeed + 0.2F);
                    entityitem.motionZ = ((float)TmrUtils.RNG.nextGaussian() * motionSpeed);
                    world.spawnEntityInWorld(entityitem);
                }
            }

            world.updateComparatorOutputLevel(pos, this);
        }

        super.breakBlock(world, pos, state);
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    //    @Override
//    public boolean hasTileEntity(int metadata) {
//        return true;
//    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileEntityTurretAssembly();
    }


//    @Override
//    public TileEntity createTileEntity(World world, int metadata) {
//        return new TileEntityTurretAssembly();
//    }

    @Override
    @Deprecated
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.INVISIBLE;
    }


//    @Override
//    public int getRenderType() {
//        return -1;
//    }

    @Override
    @Deprecated
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }


//    @Override
//    public boolean isOpaqueCube() {
//        return false;
//    }

    @Override
    @Deprecated
    public boolean isNormalCube(IBlockState state) {
        return false;
    }


//    @Override
//    public boolean renderAsNormalBlock() {
//        return false;
//    }

//    @Override
//    public void registerBlockIcons(IIconRegister iconRegister) {
//        this.blockIcon = Blocks.anvil.getIcon(0, 0);
//    }

    @Override
    @Deprecated
    public boolean hasComparatorInputOverride(IBlockState state) {
        return true;
    }

    @Override
    public int getComparatorInputOverride(IBlockState blockState, World world, BlockPos pos) {
        return Container.calcRedstoneFromInventory((IInventory) world.getTileEntity(pos));
    }

    /**
     * Convert the given metadata into a BlockState for this Block
     */
    public IBlockState getStateFromMeta(int meta)
    {
        EnumFacing enumfacing = EnumFacing.getFront(meta);

        if (enumfacing.getAxis() == EnumFacing.Axis.Y)
        {
            enumfacing = EnumFacing.NORTH;
        }

        return this.getDefaultState().withProperty(FACING, enumfacing);
    }

    /**
     * Convert the BlockState into the correct metadata value
     */
    public int getMetaFromState(IBlockState state)
    {
        return ((EnumFacing)state.getValue(FACING)).getIndex();
    }

    /**
     * Returns the blockstate with the given rotation from the passed blockstate. If inapplicable, returns the passed
     * blockstate.
     */
    public IBlockState withRotation(IBlockState state, Rotation rot)
    {
        return state.withProperty(FACING, rot.rotate((EnumFacing)state.getValue(FACING)));
    }

    /**
     * Returns the blockstate with the given mirror of the passed blockstate. If inapplicable, returns the passed
     * blockstate.
     */
    public IBlockState withMirror(IBlockState state, Mirror mirrorIn)
    {
        return state.withRotation(mirrorIn.toRotation((EnumFacing)state.getValue(FACING)));
    }

    public int getDirection(int meta) {
        return meta & 3;
    }
}