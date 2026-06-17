package com.mcmoddev.steamadvantage.init;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.datafix.IFixableData;
import net.minecraftforge.common.util.ModFixs;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import com.mcmoddev.steamadvantage.SteamAdvantage;
import com.mcmoddev.steamadvantage.blocks.DrillBitTileEntity;
import com.mcmoddev.steamadvantage.machines.*;

public class Entities {

	private static boolean initDone = false;
	private static boolean dataFixersRegistered = false;

	public static void registerDataFixers(){
		if(dataFixersRegistered) return;
		ModFixs fixes = FMLCommonHandler.instance().getDataFixer().init(SteamAdvantage.MODID, 1);
		fixes.registerFix(FixTypes.BLOCK_ENTITY, new LegacyTileEntityIdFix(SteamAdvantage.MODID, 1));
		dataFixersRegistered = true;
	}

	public static void init(){
		if(initDone) return;
		
		Blocks.init();
		

		registerTileEntity(CoalBoilerTileEntity.class, "steam_boiler_coal");
		registerTileEntity(ElectricBoilerTileEntity.class, "steam_boiler_electric");
		registerTileEntity(GeothermalBoilerTileEntity.class, "steam_boiler_geothermal");
		registerTileEntity(SteamTankTileEntity.class, "steam_tank");
		registerTileEntity(BlastFurnaceTileEntity.class, "steam_furnace");
		registerTileEntity(RockCrusherTileEntity.class, "steam_crusher");
		registerTileEntity(SteamDrillTileEntity.class, "steam_drill");
		registerTileEntity(SteamElevatorTileEntity.class, "steam_elevator");
		registerTileEntity(DrillBitTileEntity.class, "drillbit");
		registerTileEntity(SteamStillTileEntity.class, "steam_still");
		registerTileEntity(SteamPumpTileEntity.class, "steam_pump");
		registerTileEntity(OilBoilerTileEntity.class, "steam_boiler_oil");
		
		
		initDone = true;
	}

	private static void registerTileEntity(Class<? extends TileEntity> tileEntityClass, String path){
		GameRegistry.registerTileEntity(tileEntityClass, new ResourceLocation(SteamAdvantage.MODID, path));
	}

	private static final class LegacyTileEntityIdFix implements IFixableData {
		private final String modid;
		private final int version;

		private LegacyTileEntityIdFix(String modid, int version) {
			this.modid = modid;
			this.version = version;
		}

		@Override
		public int getFixVersion() {
			return version;
		}

		@Override
		public NBTTagCompound fixTagCompound(NBTTagCompound compound) {
			if (compound.hasKey("id", 8)) {
				String id = compound.getString("id");
				String legacyPrefix = modid + ".";
				String legacyMinecraftPrefix = "minecraft:" + legacyPrefix;
				if (id.startsWith(legacyMinecraftPrefix)) {
					compound.setString("id", modid + ":" + id.substring(legacyMinecraftPrefix.length()));
				} else if (id.startsWith(legacyPrefix)) {
					compound.setString("id", modid + ":" + id.substring(legacyPrefix.length()));
				}
			}
			return compound;
		}
	}
	
	@SideOnly(Side.CLIENT)
	public static void registerRenderers(){
		ClientRegistry.bindTileEntitySpecialRenderer(DrillBitTileEntity.class, new com.mcmoddev.steamadvantage.graphics.DrillBitRenderer());
	}
}
