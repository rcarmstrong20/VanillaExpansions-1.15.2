package rcarmstrong20.vanilla_expansions.core;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.gen.feature.BigMushroomFeatureConfig;
import net.minecraft.world.gen.feature.BigRedMushroomFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.feature.structure.IStructurePieceType;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import rcarmstrong20.vanilla_expansions.VanillaExpansions;
import rcarmstrong20.vanilla_expansions.gen.feature.structure.BirchForestCabinPiece;
import rcarmstrong20.vanilla_expansions.gen.feature.structure.BirchForestCabinStructure;
import rcarmstrong20.vanilla_expansions.gen.feature.structure.ForestCabinPiece;
import rcarmstrong20.vanilla_expansions.gen.feature.structure.ForestCabinStructure;
import rcarmstrong20.vanilla_expansions.gen.feature.structure.TaigaCabinPiece;
import rcarmstrong20.vanilla_expansions.gen.feature.structure.TaigaCabinStructure;

/**
 * Author: rcarmstrong20
 */
@Mod.EventBusSubscriber(modid = VanillaExpansions.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class VeFeature
{
	private static final List<Feature<?>> FEATURES = new ArrayList<>();
	private static final List<IStructurePieceType> STRUCTURE_PIECES = new ArrayList<>();
	private static final List<Structure<?>> STRUCTURES = new ArrayList<>();
	
	public static final IStructurePieceType TAIGA_CABIN_PIECE = register(VanillaExpansions.location("taiga_cabin"), TaigaCabinPiece::new);
	public static final IStructurePieceType FOREST_CABIN_PIECE = register(VanillaExpansions.location("forest_cabin"), ForestCabinPiece::new);
	public static final IStructurePieceType BIRCH_FOREST_CABIN_PIECE = register(VanillaExpansions.location("forest_cabin"), BirchForestCabinPiece::new);
	
	public static final Feature<BigMushroomFeatureConfig> HUGE_PURPLE_MUSHROOM = register(VanillaExpansions.location("huge_purple_mushroom"), new BigRedMushroomFeature(BigMushroomFeatureConfig::deserialize));
	public static final Structure<NoFeatureConfig> TAIGA_CABIN = register(VanillaExpansions.location("taiga_cabin"), new TaigaCabinStructure(NoFeatureConfig::deserialize));
	public static final Structure<NoFeatureConfig> FOREST_CABIN = register(VanillaExpansions.location("forest_cabin"), new ForestCabinStructure(NoFeatureConfig::deserialize));
	public static final Structure<NoFeatureConfig> BIRCH_FOREST_CABIN = register(VanillaExpansions.location("birch_forest_cabin"), new BirchForestCabinStructure(NoFeatureConfig::deserialize));
	
	/**
	 * Set the registry name for the features and add them to the registry list.
	 */
	private static <C extends IFeatureConfig> Feature<C> register(ResourceLocation name, Feature<C> feature)
	{
		feature.setRegistryName(name);
		FEATURES.add(feature);
		return feature;
	}
	
	/**
	 * Set the registry name for the structures and add them to the registry list.
	 */
	private static <C extends IFeatureConfig> Structure<C> register(ResourceLocation name, Structure<C> structure)
	{
		structure.setRegistryName(name);
		STRUCTURES.add(structure);
		return structure;
	}
	
	/**
	 * Create registers for the structure pieces and add them to the registry list.
	 */
	private static IStructurePieceType register(ResourceLocation name, IStructurePieceType structurePieceType)
	{
		IStructurePieceType pieceRegistry = Registry.register(Registry.STRUCTURE_PIECE, name, structurePieceType);
		STRUCTURE_PIECES.add(pieceRegistry);
		return pieceRegistry;
	}
	
	/*
	 * Register the Features to the game
	 */
	@SubscribeEvent
	public static void registerFeaturesAndStructures(final RegistryEvent.Register<Feature<?>> event)
	{
		FEATURES.forEach(feature -> event.getRegistry().register(feature));
		FEATURES.clear();
		
		STRUCTURE_PIECES.forEach(structurePiece -> STRUCTURE_PIECES.iterator());
		STRUCTURE_PIECES.clear();
		
		STRUCTURES.forEach(structure -> event.getRegistry().register(structure));
		STRUCTURES.clear();
		
		VanillaExpansions.LOGGER.info("Features registered.");
	}
}