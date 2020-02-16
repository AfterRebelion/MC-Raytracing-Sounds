package com.sonicether.soundphysics.soundsystem;

import com.sonicether.soundphysics.Config;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.Heightmap;
import net.minecraftforge.client.event.sound.SoundEvent;

import java.util.regex.Pattern;

public class Utils {
	private static final Pattern stepPattern = Pattern.compile(".*step.*");

	public static Vec3d offsetSound(final SoundEvent.SoundSourceEvent soundSourceEvent, final Vec3d playerPos) {
		ISound sourceSound = soundSourceEvent.getSound();
		Minecraft mc = Minecraft.getInstance();

		double offsetX = 0.0;
		double offsetY = 0.0;
		double offsetZ = 0.0;
		double offsetTowardsPlayer;

		double tempNormX;
		double tempNormY;
		double tempNormZ;

		final SoundCategory category = sourceSound.getCategory();

		if (sourceSound.getY() % 1.0 < 0.001 || stepPattern.matcher(sourceSound.toString()).matches()) {
			offsetY = 0.225;
		}

		if (mc.world != null && (category == SoundCategory.BLOCKS ||
				!mc.world.isAirBlock(new BlockPos(sourceSound.getX(), sourceSound.getY(), sourceSound.getZ())))) {
			// The ray will probably hit the block that it's emitting from
			// before
			// escaping. Offset the ray start position towards the player by the
			// diagonal half length of a cube

			tempNormX = playerPos.getX() - sourceSound.getX();
			tempNormY = playerPos.getY() - sourceSound.getY();
			tempNormZ = playerPos.getZ() - sourceSound.getZ();
			final double length = Math.sqrt(tempNormX * tempNormX + tempNormY * tempNormY + tempNormZ * tempNormZ);
			tempNormX /= length;
			tempNormY /= length;
			tempNormZ /= length;
			// 0.867 > square root of 0.5^2 * 3
			offsetTowardsPlayer = 0.867;
			offsetX += tempNormX * offsetTowardsPlayer;
			offsetY += tempNormY * offsetTowardsPlayer;
			offsetZ += tempNormZ * offsetTowardsPlayer;
		}

		return new Vec3d(sourceSound.getX() + offsetX, sourceSound.getY() + offsetY, sourceSound.getZ() + offsetZ);
	}

	/** Copy of {@link net.minecraft.world.World#isRainingAt} */
	public static boolean isSnowingAt(BlockPos position, boolean check_rain) {
		Minecraft mc = Minecraft.getInstance();
		assert mc.world != null;
		if (check_rain && !mc.world.isRaining()) {
			return false;
		} else if (!mc.world.canSeeSky(position)) {
			return false;
		} else if (mc.world.getHeight(Heightmap.Type.MOTION_BLOCKING, position).getY() > position.getY()) {
			return false;
		} else {
			return mc.world.getBiome(position).getPrecipitation() == Biome.RainType.SNOW;
		}
	}

	public static float getBlockReflectivity(final BlockPos blockPos) {
		Minecraft mc = Minecraft.getInstance();
		final BlockState blockState = mc.world.getBlockState(blockPos);
		final SoundType soundType = blockState.getBlock().getSoundType(blockState);

		float reflectivity = 0.5f;

		if (soundType == SoundType.STONE) {
			reflectivity = (float) (Config.stoneReflectivity.get() * 1.0f);
		} else if (soundType == SoundType.WOOD) {
			reflectivity = (float) (Config.woodReflectivity.get() * 1.0f);
		} else if (soundType == SoundType.GROUND) {
			reflectivity = (float) (Config.groundReflectivity.get() * 1.0f);
		} else if (soundType == SoundType.PLANT) {
			reflectivity = (float) (Config.plantReflectivity.get() * 1.0f);
		} else if (soundType == SoundType.METAL) {
			reflectivity = (float) (Config.metalReflectivity.get() * 1.0f);
		} else if (soundType == SoundType.GLASS) {
			reflectivity = (float) (Config.glassReflectivity.get() * 1.0f);
		} else if (soundType == SoundType.CLOTH) {
			reflectivity = (float) (Config.clothReflectivity.get() * 1.0f);
		} else if (soundType == SoundType.SAND) {
			reflectivity = (float) (Config.sandReflectivity.get() * 1.0f);
		} else if (soundType == SoundType.SNOW) {
			reflectivity = (float) (Config.snowReflectivity.get() * 1.0f);
		} else if (soundType == SoundType.LADDER) {
			reflectivity = (float) (Config.woodReflectivity.get() * 1.0f);
		} else if (soundType == SoundType.ANVIL) {
			reflectivity = (float) (Config.metalReflectivity.get() * 1.0f);
		}

		reflectivity *= Config.globalBlockReflectance.get();

		return reflectivity;
	}

	public static Vec3d reflect(final Vec3d dir, final Vec3d normal) {
		final double dot2 = dir.dotProduct(normal) * 2;

		final double x = dir.getX() - dot2 * normal.getX();
		final double y = dir.getY() - dot2 * normal.getY();
		final double z = dir.getZ() - dot2 * normal.getZ();

		return new Vec3d(x, y, z);
	}
}
