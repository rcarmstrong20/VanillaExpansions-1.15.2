package rcarmstrong20.vanilla_expansions.item.crafting;

import com.google.gson.JsonObject;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistryEntry;
import rcarmstrong20.vanilla_expansions.core.VeBlocks;
import rcarmstrong20.vanilla_expansions.core.VeRecipeSerializers;
import rcarmstrong20.vanilla_expansions.core.VeRecipeTypes;

/**
 *
 * @author Ryan
 *
 */
public class VeEaselRecipe implements IRecipe<IInventory>
{
    protected final Ingredient ingredient;
    protected final ItemStack result;
    private final IRecipeType<?> type;
    private final IRecipeSerializer<?> serializer;
    protected final ResourceLocation id;
    protected final String group;

    public VeEaselRecipe(ResourceLocation id, String group, Ingredient ingredient, ItemStack result)
    {
        this.type = VeRecipeTypes.easel;
        this.serializer = VeRecipeSerializers.easel;
        this.id = id;
        this.group = group;
        this.ingredient = ingredient;
        this.result = result;
    }

    @Override
    public boolean matches(IInventory inv, World worldIn)
    {
        return this.ingredient.test(inv.getStackInSlot(0)) && this.ingredient.test(inv.getStackInSlot(1))
                && this.ingredient.test(inv.getStackInSlot(2));
    }

    @Override
    public ItemStack getIcon()
    {
        return new ItemStack(VeBlocks.easel);
    }

    @Override
    public ItemStack getCraftingResult(IInventory inv)
    {
        return this.result.copy();
    }

    @Override
    public ItemStack getRecipeOutput()
    {
        return this.result;
    }

    @Override
    public NonNullList<Ingredient> getIngredients()
    {
        NonNullList<Ingredient> list = NonNullList.create();
        list.add(this.ingredient);
        return list;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean canFit(int width, int height)
    {
        return true;
    }

    @Override
    public boolean isDynamic()
    {
        return true;
    }

    @Override
    public ResourceLocation getId()
    {
        return this.id;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public String getGroup()
    {
        return this.group;
    }

    @Override
    public IRecipeSerializer<?> getSerializer()
    {
        return this.serializer;
    }

    @Override
    public IRecipeType<?> getType()
    {
        return this.type;
    }

    public static class Serializer<T extends VeEaselRecipe> extends ForgeRegistryEntry<IRecipeSerializer<?>>
            implements IRecipeSerializer<T>
    {
        final IRecipeFactory<T> factory;

        public Serializer(IRecipeFactory<T> factory)
        {
            this.factory = factory;
        }

        @Override
        public T read(ResourceLocation recipeId, JsonObject json)
        {
            String s = JSONUtils.getString(json, "group", "");
            Ingredient ingredients;
            if (JSONUtils.isJsonArray(json, "ingredients"))
            {
                ingredients = Ingredient.deserialize(JSONUtils.getJsonArray(json, "ingredients"));
            }
            else
            {
                ingredients = Ingredient.deserialize(JSONUtils.getJsonObject(json, "ingredients"));
            }

            String s1 = JSONUtils.getString(json, "result");
            int i = JSONUtils.getInt(json, "count");
            @SuppressWarnings("deprecation")
            ItemStack itemstack = new ItemStack(Registry.ITEM.getOrDefault(new ResourceLocation(s1)), i);
            return this.factory.create(recipeId, s, ingredients, itemstack);
        }

        @Override
        public T read(ResourceLocation recipeId, PacketBuffer buffer)
        {
            String s = buffer.readString(32767);
            Ingredient ingredients = Ingredient.read(buffer);
            ItemStack itemstack = buffer.readItemStack();
            return this.factory.create(recipeId, s, ingredients, itemstack);
        }

        @Override
        public void write(PacketBuffer buffer, T recipe)
        {
            buffer.writeString(recipe.group);
            recipe.ingredient.write(buffer);
            buffer.writeItemStack(recipe.result);
        }

        public interface IRecipeFactory<T extends VeEaselRecipe>
        {
            T create(ResourceLocation id, String group, Ingredient ingredient, ItemStack result);
        }
    }
}
