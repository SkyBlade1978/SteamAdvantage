package com.mcmoddev.steamadvantage.compat;

import com.mcmoddev.steamadvantage.SteamAdvantage;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.Loader;
import org.apache.logging.log4j.Level;

import java.lang.reflect.Method;

public final class BaseMetalsCompat {

	private static final String MODID = "basemetals";
	private static final String[] CRUSHER_REGISTRY_CLASSES = {
			"cyano.basemetals.registry.CrusherRecipeRegistry",
			"com.mcmoddev.lib.registry.CrusherRecipeRegistry"
	};

	private static boolean registryResolved = false;
	private static Class<?> crusherRegistryClass = null;
	private static boolean missingRegistryLogged = false;

	private BaseMetalsCompat() {
		throw new IllegalAccessError("Not an instantiable class");
	}

	public static boolean isLoaded() {
		return Loader.isModLoaded(MODID);
	}

	public static void addCrusherRecipe(Item input, ItemStack output) {
		if (!isLoaded()) return;
		invokeCrusherRecipe(new Class<?>[]{Item.class, ItemStack.class}, new Object[]{input, output});
	}

	public static void addCrusherRecipe(Block input, ItemStack output) {
		if (!isLoaded()) return;
		invokeCrusherRecipe(new Class<?>[]{Block.class, ItemStack.class}, new Object[]{input, output});
	}

	public static boolean hasCrusherRecipe(ItemStack input) {
		return getCrusherRecipeOutput(input) != null;
	}

	public static ItemStack getCrusherRecipeOutput(ItemStack input) {
		if (!isLoaded() || input == null) return null;
		return getRecipeOutput(getCrusherRecipe(input, ItemStack.class));
	}

	public static ItemStack getCrusherRecipeOutput(IBlockState input) {
		if (!isLoaded() || input == null) return null;
		ItemStack output = getRecipeOutput(getCrusherRecipe(input, IBlockState.class));
		if (output != null) return output;
		return getCrusherRecipeOutput(new ItemStack(input.getBlock(), 1, input.getBlock().getMetaFromState(input)));
	}

	private static Class<?> getCrusherRegistryClass() {
		if (!isLoaded()) return null;
		if (registryResolved) return crusherRegistryClass;

		registryResolved = true;
		for (String className : CRUSHER_REGISTRY_CLASSES) {
			try {
				crusherRegistryClass = Class.forName(className);
				return crusherRegistryClass;
			} catch (ClassNotFoundException e) {
				// Try the next known BaseMetals package.
			}
		}

		if (!missingRegistryLogged) {
			FMLLog.warning("%s: BaseMetals is loaded, but no known crusher recipe registry was found", SteamAdvantage.MODID);
			missingRegistryLogged = true;
		}
		return null;
	}

	private static void invokeCrusherRecipe(Class<?>[] parameterTypes, Object[] args) {
		Class<?> registry = getCrusherRegistryClass();
		if (registry == null) return;

		try {
			Method method = registry.getMethod("addNewCrusherRecipe", parameterTypes);
			method.invoke(null, args);
		} catch (ReflectiveOperationException | RuntimeException e) {
			FMLLog.log(Level.WARN, e, "%s: failed to add BaseMetals crusher recipe through %s", SteamAdvantage.MODID, registry.getName());
		}
	}

	private static Object getCrusherRecipe(Object input, Class<?> inputType) {
		Class<?> registry = getCrusherRegistryClass();
		if (registry == null) return null;

		try {
			Object instance = registry.getMethod("getInstance").invoke(null);
			Method method = registry.getMethod("getRecipeForInputItem", inputType);
			return method.invoke(instance, input);
		} catch (NoSuchMethodException e) {
			return null;
		} catch (ReflectiveOperationException | RuntimeException e) {
			FMLLog.log(Level.WARN, e, "%s: failed to look up BaseMetals crusher recipe through %s", SteamAdvantage.MODID, registry.getName());
			return null;
		}
	}

	private static ItemStack getRecipeOutput(Object recipe) {
		if (recipe == null) return null;

		try {
			Method method = recipe.getClass().getMethod("getOutput");
			Object output = method.invoke(recipe);
			return output instanceof ItemStack ? ((ItemStack) output).copy() : null;
		} catch (ReflectiveOperationException | RuntimeException e) {
			FMLLog.log(Level.WARN, e, "%s: failed to read BaseMetals crusher recipe output", SteamAdvantage.MODID);
			return null;
		}
	}
}
