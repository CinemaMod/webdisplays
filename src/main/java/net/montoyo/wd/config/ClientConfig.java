package net.montoyo.wd.config;

import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.montoyo.wd.WebDisplays;
import net.montoyo.wd.config.annoconfg.AnnoCFG;
import net.montoyo.wd.config.annoconfg.annotation.format.*;
import net.montoyo.wd.config.annoconfg.annotation.value.Default;
import net.montoyo.wd.config.annoconfg.annotation.value.DoubleRange;
import net.montoyo.wd.config.annoconfg.annotation.value.IntRange;

@Config(type = ModConfig.Type.CLIENT)
public class ClientConfig {
	@SuppressWarnings("unused")
	private static final AnnoCFG CFG = new AnnoCFG(FMLJavaModLoadingContext.get().getModEventBus(), ClientConfig.class);
	
	public static void init() {
		// loads the class
	}
	
	@Name("load_distance")
	@Comment("How far (in blocks) you can be before a screen starts rendering")
	@Translation("config.webdisplays.load_distance")
	@DoubleRange(minV = 0, maxV = Double.MAX_VALUE)
	@Default(valueD = 30)
	public static double loadDistance = 30.0;
	
	@Name("unload_distance")
	@Comment("How far you can be before a screen stops rendering")
	@Translation("config.webdisplays.unload_distance")
	@DoubleRange(minV = 0, maxV = Double.MAX_VALUE)
	@Default(valueD = 32)
	public static double unloadDistance = 32.0;
	
	@Name("pad_resolution")
	@Comment({
			"The resolution that minePads should use",
			"Smaller values produce lower qualities, higher values produce higher qualities",
			"Due to how web browsers work however, the larger this value is, the smaller text is",
			"Also, higher values will invariably lag more",
			"A good goto value for this would be the height of your monitor, in pixels",
			"A standard monitor is (at least currently) 1080"
	})
	@Translation("config.webdisplays.pad_res")
	@IntRange(minV = 0, maxV = Integer.MAX_VALUE)
	@Default(valueI = 720)
	public static int padResolution = 720;
	
	@Comment({
			"AutoVolume makes audio fade off based on distance",
			"Currently, this seems to not work"
	})
	@CFGSegment("auto_volume")
	public static class AutoVolumeControl {
		@Name("enabled")
		@Comment("Whether or not auto volume should be enabled")
		@Translation("config.webdisplays.auto_vol")
		@Default(valueBoolean = true)
		public static boolean enableAutoVolume = true;
		
		@Name("youtube_volume")
		@Comment("How loud youtube should be by default")
		@Translation("config.webdisplays.yt_vol")
		@DoubleRange(minV = 0, maxV = 100)
		@Default(valueD = 100)
		public static double ytVolume = 100.0;
		
		@Name("dist0")
		@Comment("Distance after which you can't hear anything (in blocks)")
		@Translation("config.webdisplays.d0")
		@DoubleRange(minV = 0, maxV = Double.MAX_VALUE)
		@Default(valueD = 30)
		public static double dist0 = 30.0;
		
		@Name("dist100")
		@Comment("Distance after which the sound starts dropping (in blocks)")
		@Translation("config.webdisplays.d100")
		@DoubleRange(minV = 0, maxV = Double.MAX_VALUE)
		@Default(valueD = 10)
		public static double dist100 = 10.0;
	}
	
	@SuppressWarnings("unused")
	public static void postLoad() {
		if (unloadDistance < loadDistance + 2.0)
			unloadDistance = loadDistance + 2.0;
		
		if (AutoVolumeControl.dist0 < AutoVolumeControl.dist100 + 0.1)
			AutoVolumeControl.dist0 = AutoVolumeControl.dist100 + 0.1;
		
		// cache pad resolution
		WebDisplays.INSTANCE.padResY = padResolution;
		WebDisplays.INSTANCE.padResX = WebDisplays.INSTANCE.padResY * WebDisplays.PAD_RATIO;
		
		// cache unload/load distances
		WebDisplays.INSTANCE.unloadDistance2 = unloadDistance * unloadDistance;
		WebDisplays.INSTANCE.loadDistance2 = loadDistance * loadDistance;
		
		WebDisplays.INSTANCE.ytVolume = (float) AutoVolumeControl.ytVolume;
		WebDisplays.INSTANCE.avDist100 = (float) AutoVolumeControl.dist100;
		WebDisplays.INSTANCE.avDist0 = (float) AutoVolumeControl.dist0;
	}
}
