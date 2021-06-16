package com.sonicether.soundphysics.soundsystem;

import com.sonicether.soundphysics.Config;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.Heightmap;
import net.minecraftforge.client.event.sound.SoundEvent;

import java.util.regex.Pattern;

public class Utils {
	private static final Pattern stepPattern = Pattern.compile(".*step.*");

	public static Vector3d offsetSound(final SoundEvent.SoundSourceEvent soundSourceEvent, final Vector3d playerPos) {
		ISound sourceSound = soundSourceEvent.getSound();
		Minecraft mc = Minecraft.getInstance();

		double offsetX = 0.0;
		double offsetY = 0.0;
		double offsetZ = 0.0;
		// 0.867 > square root of 0.5^2 * 3
		final double offsetTowardsPlayer = 0.867;

		double tempNormX;
		double tempNormY;
		double tempNormZ;

		final SoundCategory category = sourceSound.getSource();

		if (sourceSound.getY() % 1.0 < 0.001 || stepPattern.matcher(sourceSound.toString()).matches()) {
			offsetY = 0.225;
		}

		if (mc.level != null && (category == SoundCategory.BLOCKS ||
				!mc.level.isEmptyBlock(new BlockPos(sourceSound.getX(), sourceSound.getY(), sourceSound.getZ())))) {
			// The ray will probably hit the block that it's emitting from
			// before escaping. Offset the ray start position towards the player by the
			// diagonal half length of a cube

			tempNormX = playerPos.x() - sourceSound.getX();
			tempNormY = playerPos.y() - sourceSound.getY();
			tempNormZ = playerPos.z() - sourceSound.getZ();
			final double length = Math.sqrt(tempNormX * tempNormX + tempNormY * tempNormY + tempNormZ * tempNormZ);
			tempNormX /= length;
			tempNormY /= length;
			tempNormZ /= length;

			offsetX += tempNormX * offsetTowardsPlayer;
			offsetY += tempNormY * offsetTowardsPlayer;
			offsetZ += tempNormZ * offsetTowardsPlayer;
		}

		return new Vector3d(sourceSound.getX() + offsetX, sourceSound.getY() + offsetY, sourceSound.getZ() + offsetZ);
	}

	/** Copy of {@link net.minecraft.world.World#isRainingAt} */
	public static boolean isSnowingAt(BlockPos position, boolean check_rain) {
		Minecraft mc = Minecraft.getInstance();
		assert mc.level != null;
		if (check_rain && !mc.level.isRaining()) {
			return false;
		} else if (!mc.level.canSeeSky(position)) {
			return false;
		} else if (mc.level.getHeightmapPos(Heightmap.Type.MOTION_BLOCKING, position).getY() > position.getY()) {
			return false;
		} else {
			return mc.level.getBiome(position).getPrecipitation() == Biome.RainType.SNOW;
		}
	}

	public static float getBlockReflectivity(final BlockPos blockPos) {
		Minecraft mc = Minecraft.getInstance();
		assert mc.level != null;
		final BlockState blockState = mc.level.getBlockState(blockPos);
		final SoundType soundType = blockState.getSoundType();

		float reflectivity = 0.5f;

		if (soundType == SoundType.STONE) {
			reflectivity = Config.stoneReflectivity.get().floatValue();
		} else if (soundType == SoundType.WOOD) {
			reflectivity = Config.woodReflectivity.get().floatValue();
		} else if (soundType == SoundType.GRAVEL) {
			reflectivity = Config.groundReflectivity.get().floatValue();
		} else if (soundType == SoundType.GRASS) {
			reflectivity = Config.plantReflectivity.get().floatValue();
		} else if (soundType == SoundType.METAL) {
			reflectivity = Config.metalReflectivity.get().floatValue();
		} else if (soundType == SoundType.GLASS) {
			reflectivity = Config.glassReflectivity.get().floatValue();
		} else if (soundType == SoundType.WOOL) {
			reflectivity = Config.clothReflectivity.get().floatValue();
		} else if (soundType == SoundType.SAND) {
			reflectivity = Config.sandReflectivity.get().floatValue();
		} else if (soundType == SoundType.SNOW) {
			reflectivity = Config.snowReflectivity.get().floatValue();
		} else if (soundType == SoundType.LADDER) {
			reflectivity = Config.woodReflectivity.get().floatValue();
		} else if (soundType == SoundType.ANVIL) {
			reflectivity = Config.metalReflectivity.get().floatValue();
		}

		reflectivity *= Config.globalBlockReflectance.get().floatValue();

		return reflectivity;
	}

	public static Vector3d reflect(final Vector3d dir, final Vector3d normal) {
		final double dot2 = dir.dot(normal) * 2;

		final double x = dir.x() - dot2 * normal.x();
		final double y = dir.y() - dot2 * normal.y();
		final double z = dir.z() - dot2 * normal.z();

		return new Vector3d(x, y, z);
	}

	private Utils() {
		throw new IllegalStateException("Utility class");
	}
}
