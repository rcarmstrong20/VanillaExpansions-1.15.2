package rcarmstrong20.vanilla_expansions;

import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.block.BeetrootBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropsBlock;
import net.minecraft.block.NetherWartBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.passive.RabbitEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.properties.SlabType;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import rcarmstrong20.vanilla_expansions.block.VeDoubleSlabBlock;
import rcarmstrong20.vanilla_expansions.client.renderer.particle.VeDripParticle;
import rcarmstrong20.vanilla_expansions.client.renderer.particle.VeUndervoidParticle;
import rcarmstrong20.vanilla_expansions.core.VeBlocks;
import rcarmstrong20.vanilla_expansions.core.VeParticleTypes;
import rcarmstrong20.vanilla_expansions.proxy.ClientProxy;
import rcarmstrong20.vanilla_expansions.proxy.CommonProxy;

@Mod("ve")
public class VanillaExpansions
{
	public static Object modInstance;
	public static final String MOD_ID = "ve";
	public static final Logger LOGGER = LogManager.getLogger(VanillaExpansions.MOD_ID);
	public static final VeItemGroup VE_GROUP = new VeItemGroup(VanillaExpansions.MOD_ID);
	public static final CommonProxy PROXY = DistExecutor.runForDist(() -> ClientProxy::new, () -> CommonProxy::new);
	
	public VanillaExpansions()
	{
		modInstance = this;
		
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientRegistries);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onRegisterParticle);
		
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	/*
	 * Called on the mod's setup.
	 */
	private void setup(final FMLCommonSetupEvent event)
	{
		LOGGER.info("setup method registered");
		PROXY.onSetupCommon();
	}
	
	/*
	 * Called exclusively on the client.
	 */
	private void clientRegistries(final FMLClientSetupEvent event)
	{
		LOGGER.info("client method registered");
		PROXY.onSetupClient();
	}
	
	/*
	 * This takes care of registering the particle factories if they are not registered under the particle factory event there will be a bug.
	 */
	@OnlyIn(Dist.CLIENT)
	private void onRegisterParticle(ParticleFactoryRegisterEvent event)
	{
		Minecraft.getInstance().particles.registerFactory(VeParticleTypes.DRIPPING_VOID, VeDripParticle.VeDrippingVoidFactory::new);
		Minecraft.getInstance().particles.registerFactory(VeParticleTypes.FALLING_VOID, VeDripParticle.VeFallingVoidFactory::new);
		Minecraft.getInstance().particles.registerFactory(VeParticleTypes.LANDING_VOID, VeDripParticle.VeLandingVoidFactory::new);
		Minecraft.getInstance().particles.registerFactory(VeParticleTypes.UNDERVOID, VeUndervoidParticle.Factory::new);
	}
	
	/**
	 * Controls crop harvesting with right click behavior
	 */
	@SubscribeEvent
	public void onRightClickCrop(final RightClickBlock event)
	{
		//General variables
		BlockPos pos = event.getPos();
		World world = event.getWorld();
		Random random = new Random();
		
		//Block and items
		BlockState worldState = event.getWorld().getBlockState(pos);
		Item item = event.getItemStack().getItem();
		
		//Crop age properties
		IntegerProperty cropsAge = CropsBlock.AGE;
		IntegerProperty netherWartAge = NetherWartBlock.AGE;
		IntegerProperty beetrootAge = BeetrootBlock.BEETROOT_AGE;
		
		if(!event.getWorld().isRemote)
		{
			//If the block your clicking is a crop and your not using bone meal return true
			if(worldState.getBlock() instanceof CropsBlock && item != Items.BONE_MEAL)
			{
				if(worldState.getBlock() instanceof BeetrootBlock)
				{
					//When the beet root crop is fully grown and clicked then harvest it.
					if(worldState.get(beetrootAge) == beetrootAge.getAllowedValues().size() - 1)
					{
						harvestCrop(worldState, world, pos, random, beetrootAge);
						event.setResult(Result.ALLOW);
						event.setCanceled(true);
					}
				}
				//If its not a beet root and a crop then it must be a normal 7 stage crop and if it's fully grown harvest it.
				else if(worldState.get(cropsAge) == 7)
				{
					harvestCrop(worldState, world, pos, random, cropsAge);
					event.setResult(Result.ALLOW);
					event.setCanceled(true);
				}
			}
			//If its not a crop it might be nether wart and if so check if it's fully grown and if so harvest it.
			else if(worldState.getBlock() instanceof NetherWartBlock)
			{
				if(worldState.get(netherWartAge) == 3)
				{
					harvestCrop(worldState, world, pos, random, netherWartAge);
					event.setResult(Result.ALLOW);
					event.setCanceled(true);
				}
			}
			else
			{
				event.setResult(Result.DEFAULT);
			}
		}
	}
	
	/*
	 * Resets the crop back to age 0, spawns the crops drops, and plays the grass breaking sound.
	 */
	private static void harvestCrop(BlockState state, World world, BlockPos pos, Random random, IntegerProperty age)
	{
		world.setBlockState(pos, state.with(age, 0), 2);
		Block.spawnDrops(state, world, pos);
		world.playSound(null, pos, SoundEvents.BLOCK_GRASS_BREAK, SoundCategory.BLOCKS, 1.0F + random.nextFloat(), random.nextFloat() * 0.7F + 0.3F);
	}
	
	/*
	 * Controls custom slab behavior
	 */
	@SubscribeEvent
	public void onRightClickSlab(final PlayerInteractEvent.RightClickBlock event)
	{
		BlockPos pos = event.getPos();
		World world = event.getWorld();
		Direction direction = event.getFace();
		
		BlockState worldState = event.getWorld().getBlockState(pos);
		BlockState worldStateUp = event.getWorld().getBlockState(pos.up());
		Item item = event.getItemStack().getItem();
		Block itemBlock = Block.getBlockFromItem(item.getItem());
		
		if(worldState.getBlock() instanceof SlabBlock && itemBlock instanceof SlabBlock)
		{
			if(worldState.getMaterial() == itemBlock.getDefaultState().getMaterial())
			{
				SlabType slabtype = worldState.get(SlabBlock.TYPE);
				
				if(slabtype != SlabType.DOUBLE && worldState.getBlock() != itemBlock)
				{
					if (slabtype == SlabType.BOTTOM && direction == Direction.UP || direction.getAxis().isHorizontal())
					{
						System.out.print("Placed top on bottom");
						VeDoubleSlabBlock.fillInventory(itemBlock, worldState.getBlock());
					}
					else if (slabtype == SlabType.TOP && direction == Direction.DOWN || direction.getAxis().isHorizontal())
					{
						System.out.print("Placed bottom under top");
						VeDoubleSlabBlock.fillInventory(worldState.getBlock(), itemBlock);
					}
					world.setBlockState(pos, VeBlocks.double_slab.getDefaultState());
					event.setResult(Result.ALLOW);
					event.setCanceled(true);
				}
			}
		}
		else if(worldStateUp.getBlock() instanceof SlabBlock && itemBlock instanceof SlabBlock)
		{
			if(worldStateUp.getMaterial() == itemBlock.getDefaultState().getMaterial())
			{
				SlabType slabtypeUp = worldStateUp.get(SlabBlock.TYPE);
				
				if(slabtypeUp != SlabType.DOUBLE && worldState.getBlock() != itemBlock)
				{
					if (slabtypeUp == SlabType.TOP && direction == Direction.UP)
					{
						System.out.print("Placed bottom on top from bottom");
						VeDoubleSlabBlock.fillInventory(itemBlock, worldStateUp.getBlock());
						world.setBlockState(pos.up(), VeBlocks.double_slab.getDefaultState());
						event.setResult(Result.ALLOW);
						event.setCanceled(true);
					}
				}
			}
		}
	}
	
	/**
	 * Individually tracks the naming behavior for each white or killer rabbit entity, then either sets the rabbit type to 99 or 1.
	 */
	@SubscribeEvent
	public void onNameBunnyEntity(final PlayerInteractEvent.EntityInteractSpecific event)
	{
		if(event.getTarget() instanceof RabbitEntity)
		{
			RabbitEntity rabbit = (RabbitEntity) event.getTarget();
			ItemStack itemStack = event.getItemStack();
			
			if(rabbit.getRabbitType() == 1 || rabbit.getRabbitType() == 99)
			{
				if(itemStack.getItem() == Items.NAME_TAG && itemStack.hasDisplayName())
				{
					if("The Killer Bunny".equals(itemStack.getDisplayName().getUnformattedComponentText()))
					{
						rabbit.setRabbitType(99);
						rabbit.getRabbitType();
						rabbit.tick();
					}
					else
					{
						rabbit.setRabbitType(1);
						rabbit.getRabbitType();
						rabbit.tick();
					}
				}
			}
		}
	}
}
