/*
 * ****************************************************************************************************************
 * Authors:   SanAndreasP
 * Copyright: SanAndreasP
 * License:   Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
 * http://creativecommons.org/licenses/by-nc-sa/4.0/
 * *****************************************************************************************************************
 */
package de.sanandrew.mods.turretmod.block;

import de.sanandrew.mods.sanlib.lib.util.ItemStackUtils;
import de.sanandrew.mods.turretmod.tileentity.electrolyte.ElectrolyteGeneratorEntity;
import net.minecraft.block.Block;
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
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

@SuppressWarnings("deprecation")
public class ElectrolyteGeneratorBlock
        extends ContainerBlock
{
    private static final VoxelShape MAIN_SEL_BB = Block.box(0, 0, 0, 16, 32, 16);
    private static final VoxelShape UPPER_SEL_BB = Block.box(0, -16, 0, 16, 16, 16);

    private static final BooleanProperty TILE_HOLDER = BooleanProperty.create("tile_main");

    ElectrolyteGeneratorBlock() {
        super(Properties.of(Material.METAL, MaterialColor.COLOR_BROWN).strength(4.25F).sound(SoundType.STONE)
                        .requiresCorrectToolForDrops().noOcclusion());

        this.registerDefaultState(this.getStateDefinition().any().setValue(TILE_HOLDER, true));
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(TILE_HOLDER);
    }

    @Override
    public void onPlace(@Nonnull BlockState state, @Nonnull World level, @Nonnull BlockPos pos, @Nonnull BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);

        if( state.getValue(TILE_HOLDER) ) {
            level.setBlock(pos.above(), this.stateDefinition.any().setValue(TILE_HOLDER, false), 3);
        }
    }

    @Override
    public void onRemove(BlockState state, @Nonnull World level, @Nonnull BlockPos pos, @Nonnull BlockState newState, boolean isMoving) {
        if( state.getValue(TILE_HOLDER) ) {
            ElectrolyteGeneratorEntity electrolyteGen = (ElectrolyteGeneratorEntity) level.getBlockEntity(pos);

            if( electrolyteGen != null ) {
                Optional<IItemHandler> loHandler = electrolyteGen.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, Direction.DOWN).resolve();
                if( loHandler.isPresent() ) {
                    IItemHandler handler = loHandler.get();
                    for( int i = 0, max = handler.getSlots(); i < max; i++ ) {
                        ItemStackUtils.dropBlockItem(handler.getStackInSlot(i), level, pos);
                    }
                }

                level.updateNeighbourForOutputSignal(pos, this);
            }

            BlockPos upBlock = pos.above();
            if( level.getBlockState(upBlock).getBlock() == this ) {
                level.levelEvent(2001, upBlock, getId(level.getBlockState(upBlock)));
                level.setBlock(upBlock, Blocks.AIR.defaultBlockState(), 35);
            }
        } else {
            BlockPos downBlock = pos.below();
            if( level.getBlockState(downBlock).getBlock() == this ) {
                level.levelEvent(2001, downBlock, getId(level.getBlockState(downBlock)));
                level.setBlock(downBlock, Blocks.AIR.defaultBlockState(), 35);
            }
        }

        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public void playerWillDestroy(World level, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull PlayerEntity player) {
        if( !level.isClientSide && player.isCreative() ) {
            if( !state.getValue(TILE_HOLDER) ) {
                BlockPos blockpos = pos.below();
                BlockState blockstate = level.getBlockState(blockpos);
                if( blockstate.getBlock() == state.getBlock() && blockstate.getValue(TILE_HOLDER) ) {
                    level.setBlock(blockpos, Blocks.AIR.defaultBlockState(), 35);
                    level.levelEvent(player, 2001, blockpos, Block.getId(blockstate));
                }
            }
        }

        super.playerWillDestroy(level, pos, state, player);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        BlockPos blockpos = context.getClickedPos();
        World world = context.getLevel();
        if( blockpos.getY() < world.getHeight() - 1 && world.getBlockState(blockpos.above()).canBeReplaced(context) ) {
            return this.defaultBlockState().setValue(TILE_HOLDER, true);
        } else {
            return null;
        }
    }

    @Nonnull
    @Override
    public ActionResultType use(@Nonnull BlockState state, @Nonnull World level, @Nonnull BlockPos pos, @Nonnull PlayerEntity player,
                                @Nonnull Hand hand, @Nonnull BlockRayTraceResult hit)
    {
        if( player instanceof ServerPlayerEntity ) {
            if( state.getValue(TILE_HOLDER) ) {
                TileEntity tileentity = level.getBlockEntity(pos);
                if( tileentity instanceof ElectrolyteGeneratorEntity ) {
                    NetworkHooks.openGui((ServerPlayerEntity) player, (INamedContainerProvider) tileentity, b -> b.writeBlockPos(pos));
                }
            } else {
                return this.use(level.getBlockState(pos.below()), level, pos.below(), player, hand, hit);
            }

            return ActionResultType.CONSUME;
        }

        return ActionResultType.SUCCESS;
    }

    @Override
    public void setPlacedBy(World level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        level.setBlock(pos.above(), state.setValue(TILE_HOLDER, false), 3);

        if( stack.hasCustomHoverName() && state.getValue(TILE_HOLDER) ) {
            TileEntity te = level.getBlockEntity(pos);
            assert te != null;
            ((ElectrolyteGeneratorEntity) te).setCustomName(stack.getDisplayName());
        }
    }

    @Nonnull
    @Override
    public BlockRenderType getRenderShape(@Nonnull BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return state.getValue(TILE_HOLDER);
    }

    @Nullable
    @Override
    public TileEntity newBlockEntity(@Nonnull IBlockReader level) {
        return new ElectrolyteGeneratorEntity();
    }

    @Override
    public boolean hasAnalogOutputSignal(@Nonnull BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, @Nonnull World worldIn, @Nonnull BlockPos pos) {
        return blockState.getValue(TILE_HOLDER)
               ? Container.getRedstoneSignalFromBlockEntity(worldIn.getBlockEntity(pos))
               : this.getAnalogOutputSignal(worldIn.getBlockState(pos.below()), worldIn, pos.below());
    }

    @Nonnull
    @Override
    public VoxelShape getShape(BlockState state, @Nonnull IBlockReader worldIn, @Nonnull BlockPos pos, @Nonnull ISelectionContext context) {
        return state.getValue(TILE_HOLDER) ? MAIN_SEL_BB : UPPER_SEL_BB;
    }
}
