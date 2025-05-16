package net.kyrptonaught.customportalapi;

import net.kyrptonaught.customportalapi.portal.frame.PortalFrameTester;
import net.kyrptonaught.customportalapi.util.CustomPortalHelper;
import net.kyrptonaught.customportalapi.util.CustomTeleporter;
import net.kyrptonaught.customportalapi.util.PortalLink;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Portal;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CustomPortalBlock extends Block implements Portal {

    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;

    protected static final VoxelShape X_SHAPE = Block.box(0.0D, 0.0D, 6.0D, 16.0D, 16.0D, 10.0D);

    protected static final VoxelShape Z_SHAPE = Block.box(6.0D, 0.0D, 0.0D, 10.0D, 16.0D, 16.0D);

    protected static final VoxelShape Y_SHAPE = Block.box(0.0D, 6.0D, 0.0D, 16.0D, 10.0D, 16.0D);

    public CustomPortalBlock(Properties settings) {
        super(settings);
        this.registerDefaultState(stateDefinition.any().setValue(AXIS, Direction.Axis.X));
    }

    @Override
    public @NotNull VoxelShape getShape(
        BlockState state,
        @NotNull BlockGetter world,
        @NotNull BlockPos pos,
        @NotNull CollisionContext context
    ) {
        return switch (state.getValue(AXIS)) {
            case Z -> Z_SHAPE;
            case Y -> Y_SHAPE;
            default -> X_SHAPE;
        };
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData, Player player) {
        return ItemStack.EMPTY;
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess scheduledTickAccess, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        Block block = getPortalBase((Level) level, pos);
        PortalLink link = CustomPortalApiRegistry.getPortalLinkFromBase(block);
        if (link != null) {
            PortalFrameTester portalFrameTester = link.getFrameTester()
                    .createInstanceOfPortalFrameTester()
                    .init((LevelAccessor) level, pos, CustomPortalHelper.getAxisFrom(state), block);
            if (portalFrameTester.isAlreadyLitPortalFrame()) {
                return super.updateShape(state, level, scheduledTickAccess, pos, direction, neighborPos, neighborState, random);
            }
        }

        return Blocks.AIR.defaultBlockState();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AXIS);
    }

    @Override
    public void animateTick(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, RandomSource random) {
        if (random.nextInt(100) == 0)
            level.playLocalSound(
                pos.getX() + 0.5D,
                pos.getY() + 0.5D,
                pos.getZ() + 0.5D,
                SoundEvents.PORTAL_AMBIENT,
                SoundSource.BLOCKS,
                0.5F,
                random.nextFloat() * 0.4F + 0.8F,
                false
            );

        for (int i = 0; i < 4; ++i) {
            double d = pos.getX() + random.nextDouble();
            double e = pos.getY() + random.nextDouble();
            double f = pos.getZ() + random.nextDouble();
            double g = (random.nextFloat() - 0.5D) * 0.5D;
            double h = (random.nextFloat() - 0.5D) * 0.5D;
            double j = (random.nextFloat() - 0.5D) * 0.5D;
            int k = random.nextInt(2) * 2 - 1;
            if (!level.getBlockState(pos.west()).is(this) && !level.getBlockState(pos.east()).is(this)) {
                d = pos.getX() + 0.5D + 0.25D * k;
                g = random.nextFloat() * 2.0F * k;
            } else {
                f = pos.getZ() + 0.5D + 0.25D * k;
                j = random.nextFloat() * 2.0F * k;
            }
            level.addParticle(
                new BlockParticleOption(
                    ParticleTypes.BLOCK,
                    getPortalBase(level, pos).defaultBlockState()
                ),
                d,
                e,
                f,
                g,
                h,
                j
            );
        }
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier) {
        if (entity.canUsePortal(false)) {
            entity.setAsInsidePortal(this, pos);
        }
    }

    @Override
    public int getPortalTransitionTime(@NotNull ServerLevel world, @NotNull Entity entity) {
        if (entity instanceof Player playerEntity) {
            return Math.max(
                1,
                world.getGameRules()
                    .getInt(
                        playerEntity.getAbilities().invulnerable
                            ? GameRules.RULE_PLAYERS_NETHER_PORTAL_CREATIVE_DELAY
                            : GameRules.RULE_PLAYERS_NETHER_PORTAL_DEFAULT_DELAY
                    )
            );
        } else {
            return 0;
        }
    }

    public Block getPortalBase(Level world, BlockPos pos) {
        return CustomPortalHelper.getPortalBaseDefault(world, pos);
    }

    @Override
    public @Nullable TeleportTransition getPortalDestination(@NotNull ServerLevel world, @NotNull Entity entity, @NotNull BlockPos pos) {
        return CustomTeleporter.createTeleportTarget(world, entity, getPortalBase(world, pos), pos);
    }

    @Override
    public @NotNull Transition getLocalTransition() {
        return Transition.CONFUSION;
    }
}