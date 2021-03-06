/* ******************************************************************************************************************
 * Authors:   SanAndreasP
 * Copyright: SanAndreasP
 * License:   Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
 *                http://creativecommons.org/licenses/by-nc-sa/4.0/
 *******************************************************************************************************************/
package de.sanandrew.mods.turretmod.block;

import de.sanandrew.mods.sanlib.lib.util.InventoryUtils;
import de.sanandrew.mods.turretmod.tileentity.TurretCrateEntity;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ContainerBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@SuppressWarnings("deprecation")
public class TurretCrateBlock
        extends ContainerBlock
{
    TurretCrateBlock() {
        super(Properties.of(Material.METAL, MaterialColor.QUARTZ).strength(4.25F).sound(SoundType.METAL)
                        .requiresCorrectToolForDrops().noOcclusion());
    }

    @Override
    public void onRemove(@Nonnull BlockState state, World level, @Nonnull BlockPos pos, @Nonnull BlockState newState, boolean isMoving) {
        TurretCrateEntity crate = (TurretCrateEntity) level.getBlockEntity(pos);

        if( crate != null ) {
            InventoryUtils.dropBlockItems(crate.getInventory(), level, pos);
        }

        level.updateNeighbourForOutputSignal(pos, this);
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Nonnull
    @Override
    public BlockRenderType getRenderShape(@Nonnull BlockState p_149645_1_) {
        return BlockRenderType.MODEL;
    }

    //TODO: reimplement Crate GUI
    @Nonnull
    @Override
    public ActionResultType use(@Nonnull BlockState state, @Nonnull World level, @Nonnull BlockPos pos, @Nonnull PlayerEntity player,
                                @Nonnull Hand hand, @Nonnull BlockRayTraceResult hit)
    {
        if( player instanceof ServerPlayerEntity ) {
            TileEntity tileentity = level.getBlockEntity(pos);
            if( tileentity instanceof TurretCrateEntity ) {
                NetworkHooks.openGui((ServerPlayerEntity) player, (TurretCrateEntity) tileentity, pos);
            }

            return ActionResultType.CONSUME;
        }

        return ActionResultType.SUCCESS;
    }

    @Override
    public void setPlacedBy(@Nonnull World level, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if( stack.hasCustomHoverName() ) {
            TileEntity tileentity = level.getBlockEntity(pos);

            if( tileentity instanceof TurretCrateEntity ) {
                ((TurretCrateEntity) tileentity).setCustomName(stack.getDisplayName());
            }
        }
    }

    @Nullable
    @Override
    public TileEntity newBlockEntity(@Nonnull IBlockReader level) {
        return new TurretCrateEntity();
    }

    @Override
    public boolean hasAnalogOutputSignal(@Nonnull BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(@Nonnull BlockState state, World level, @Nonnull BlockPos pos) {
        return Container.getRedstoneSignalFromContainer((IInventory) level.getBlockEntity(pos));
    }
}
