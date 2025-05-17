package net.kyrptonaught.customportalapi;

import net.kyrptonaught.customportalapi.portal.frame.PortalFrameTester;
import net.kyrptonaught.customportalapi.util.CustomPortalHelper;
import net.kyrptonaught.customportalapi.util.CustomTeleporter;
import net.kyrptonaught.customportalapi.util.PortalLink;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
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
import org.jetbrains.annotations.Nullable;

public class CustomPortalBlock extends Block implements Portal {

    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;
    protected static final VoxelShape X_SHAPE = Block.box(0.0D, 0.0D, 6.0D, 16.0D, 16.0D, 10.0D);
    protected static final VoxelShape Z_SHAPE = Block.box(6.0D, 0.0D, 0.0D, 10.0D, 16.0D, 16.0D);
    protected static final VoxelShape Y_SHAPE = Block.box(0.0D, 6.0D, 0.0D, 16.0D, 10.0D, 16.0D);

    public CustomPortalBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(AXIS, Direction.Axis.X));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context) {
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
        Block portalBase = getPortalBase((Level) level, pos);
        PortalLink link = CustomPortalsMod.getPortalLinkFromBase(portalBase);
        if (link != null) {
            PortalFrameTester portalFrameTester = link.getFrameTester()
                    .init((LevelAccessor) level, pos, CustomPortalHelper.getAxisFrom(state), portalBase);
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
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        Block portalBase = getPortalBase(level, pos);
        PortalLink link = CustomPortalsMod.getPortalLinkFromBase(portalBase);

        if (link == null) {
            return;
        }

        if (random.nextInt(100) == 0) {
            SoundEvent event = BuiltInRegistries.SOUND_EVENT.getValue(link.ambientSoundLocation);
            if (event != null) {
                level.playLocalSound(
                        pos.getX() + 0.5D,
                        pos.getY() + 0.5D,
                        pos.getZ() + 0.5D,
                        event,
                        SoundSource.BLOCKS,
                        link.ambientSoundVolume.apply(level),
                        link.ambientSoundPitch.apply(level),
                        false
                );
            }
        }

        for (int i = 0; i < 4; i++) {
            double dX = pos.getX() + random.nextDouble();
            double dY = pos.getY() + random.nextDouble();
            double dZ = pos.getZ() + random.nextDouble();
            double sX = (random.nextFloat() - 0.5d) * 0.5d;
            double sY = (random.nextFloat() - 0.5d) * 0.5d;
            double sZ = (random.nextFloat() - 0.5d) * 0.5d;
            int mod = random.nextInt(2) * 2 - 1;
            if (!level.getBlockState(pos.west()).is(this) && !level.getBlockState(pos.east()).is(this)) {
                dX = pos.getX() + 0.5f + 0.25f * mod;
                sX = random.nextFloat() * 2.0f * mod;
            } else {
                dZ = pos.getZ() + 0.5f + 0.25f * mod;
                sZ = random.nextFloat() * 2.0f * mod;
            }

            level.addParticle(link.portalParticle.apply(level, pos), dX, dY, dZ, sX, sY, sZ);
        }
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier) {
        if (entity.canUsePortal(false)) {
            entity.setAsInsidePortal(this, pos);
        }
    }

    @Override
    public int getPortalTransitionTime(ServerLevel level, Entity entity) {
        if (entity instanceof Player playerEntity) {
            return Math.max(1, level.getGameRules().getInt(playerEntity.getAbilities().invulnerable
                            ? GameRules.RULE_PLAYERS_NETHER_PORTAL_CREATIVE_DELAY
                            : GameRules.RULE_PLAYERS_NETHER_PORTAL_DEFAULT_DELAY));
        }

        return 0;
    }

    public Block getPortalBase(Level level, BlockPos pos) {
        return CustomPortalHelper.getPortalBaseDefault(level, pos);
    }

    @Override
    @Nullable
    public TeleportTransition getPortalDestination(ServerLevel level, Entity entity, BlockPos pos) {
        return CustomTeleporter.createTeleportTarget(level, entity, getPortalBase(level, pos), pos);
    }

    @Override
    public Transition getLocalTransition() {
        return Transition.CONFUSION;
    }
}