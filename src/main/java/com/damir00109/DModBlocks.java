package com.damir00109;

import de.maxhenkel.voicechat.api.audiochannel.LocationalAudioChannel;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.block.WireOrientation;
import net.minecraft.item.ItemPlacementContext;
import org.jetbrains.annotations.Nullable;

public class DModBlocks {

    public static final IntProperty POWER = IntProperty.of("power", 0, 15);
    public static final EnumProperty<Direction> FACING = EnumProperty.of("facing", Direction.class);
    public static final BooleanProperty LISTEN = BooleanProperty.of("listen");
    public static final BooleanProperty ACTIVE = BooleanProperty.of("active");

    public static final Block RADIO = registerBlock("radio", new RadioBlock(Block.Settings.create()
            .mapColor(MapColor.STONE_GRAY)
            .strength(1.5f)
            .pistonBehavior(PistonBehavior.BLOCK)
            .registryKey(RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(VanillaDamir00109.MOD_ID, "radio")))
    ));

    private static Block registerBlock(String name, Block block) {
        Identifier id = Identifier.of(VanillaDamir00109.MOD_ID, name);
        Registry.register(Registries.BLOCK, id, block);
        Registry.register(Registries.ITEM, id, new BlockItem(block, new Item.Settings()
                .registryKey(RegistryKey.of(RegistryKeys.ITEM, id))
        ));
        return block;
    }

    public static void registerModBlocks() {
        VanillaDamir00109.LOGGER.info("Registering Mod Blocks for " + VanillaDamir00109.MOD_ID);
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register(entries -> {
            entries.add(RADIO);
        });
    }

    public static class RadioBlock extends Block {
        private LocationalAudioChannel channel;
        public boolean listen_sate = true;

        public RadioBlock(Settings settings) {
            super(settings);
            this.setDefaultState(this.stateManager.getDefaultState()
                    .with(POWER, 0)
                    .with(FACING, Direction.EAST)
                    .with(LISTEN, true)
                    .with(ACTIVE, false));
        }

        @Override
        protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
            builder.add(POWER, FACING, LISTEN, ACTIVE);
        }

        @Override
        public BlockState getPlacementState(ItemPlacementContext ctx) {
            return this.getDefaultState()
                    .with(FACING, ctx.getHorizontalPlayerFacing().getOpposite())
                    .with(POWER, ctx.getWorld().getReceivedRedstonePower(ctx.getBlockPos()))
                    .with(LISTEN, true)
                    .with(ACTIVE, false);
        }

        @Override
        public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
            if (!world.isClient) {
                int power = world.getReceivedRedstonePower(pos);
                boolean newActive = !this.listen_sate;
                this.listen_sate = newActive;
                world.setBlockState(pos, state.with(LISTEN, newActive), 1);

                VanillaDamir00109.LOGGER.info("Block clocked");
            }

            return ActionResult.SUCCESS;
        }

        @Override
        public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, @Nullable WireOrientation wireOrientation, boolean notify) {
            if (!world.isClient) {
                BlockPos abovePos = pos.up();
                BlockState aboveState = world.getBlockState(abovePos);
                boolean hasRodAbove = aboveState.isOf(Blocks.LIGHTNING_ROD);
                boolean hasAdjacentRod = false;

                // Check horizontal directions for adjacent Lightning Rods
                for (Direction dir : Direction.Type.HORIZONTAL) {
                    BlockPos sidePos = pos.offset(dir);
                    if (world.getBlockState(sidePos).isOf(Blocks.LIGHTNING_ROD)) {
                        hasAdjacentRod = true;
                        break;
                    }
                }

                int power = world.getReceivedRedstonePower(pos);
                boolean newListen = this.listen_sate;
                boolean newActive = false;

                if (hasRodAbove && !hasAdjacentRod) { // if has 2 rod above
                    newActive = true;
                    // Optional: Notify players | Modify: Notify in logger
                    VanillaDamir00109.LOGGER.info("Lightning Rod detected on Radio!");
                }

                if (!newListen && newActive) {
                    this.channel = VanillaDamir00109.getChannelByNum(power-1);
                }

                if (state.get(POWER) != power || state.get(ACTIVE) != newActive) {
                    world.setBlockState(pos, state.with(POWER, power).with(ACTIVE, newActive), 2);
                }
            }
        }
    }
}