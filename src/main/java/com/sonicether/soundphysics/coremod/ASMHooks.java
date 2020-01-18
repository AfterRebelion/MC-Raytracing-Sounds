package com.sonicether.soundphysics.coremod;

import com.sonicether.soundphysics.Config;
import com.sonicether.soundphysics.SoundPhysics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.AudioStreamBuffer;
import net.minecraft.client.audio.ISound;
import net.minecraft.entity.Entity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;

import javax.sound.sampled.AudioFormat;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static com.sonicether.soundphysics.coremod.ASMTools.clickPattern;
import static com.sonicether.soundphysics.coremod.ASMTools.evaluateEnvironment;
import static com.sonicether.soundphysics.coremod.ASMTools.mc;
import static com.sonicether.soundphysics.coremod.ASMTools.noteBlockPattern;
import static com.sonicether.soundphysics.coremod.ASMTools.rainPattern;
import static com.sonicether.soundphysics.coremod.ASMTools.stepPattern;
import static com.sonicether.soundphysics.coremod.ASMTools.uiPattern;

public class ASMHooks {
	// THESE VARIABLES ARE CONSTANTLY ACCESSED AND USED BY ASM INJECTED CODE! DO
	// NOT REMOVE!
	public static float globalVolumeMultiplier = 4.0f;
	public static float globalReverbMultiplier = (float) (0.7f * Config.globalReverbGain.get());
	public static ISound.AttenuationType attenuationModel = ISound.AttenuationType.LINEAR; // Mojang only ported LINEAR attenuation for their sound system... See ISound.AttenuationType
	public static Double globalRolloffFactor = Config.rolloffFactor.get();
	public static double soundDistanceAllowance = Config.soundDistanceAllowance.get();

	private static SoundCategory lastSoundCategory;
	private static String lastSoundName;

	/**
	 * CALLED BY ASM INJECTED CODE!
	 */
	public static void init() {
		mc = Minecraft.getInstance();
		try {
			/*if (Config.dopplerEnabled) {
				sndSystem.changeDopplerFactor(1.0f);
				AL11.alSpeedOfSound(343.3f); // Should already be 343.3 but just in case
			}*/
			ASMTools.setupEFX();
			ProcThread.setupThread();
		} catch (Throwable e) {
			SoundPhysics.logError("Failed to init EFX or thread");
			SoundPhysics.logError(e.toString());
		}
	}

	/**
	 * CALLED BY ASM INJECTED CODE!
	 */
	public static void setLastSoundCategory(final SoundCategory sc) {
		lastSoundCategory = sc;
	}

	/**
	 * CALLED BY ASM INJECTED CODE!
	 */
	public static void setLastSoundName(final String soundName, final String eventName) {
		lastSoundName = eventName + "|" + soundName.split(":")[1]; // Quick and dirty hack to check the event and sound name
	}

	/**
	 * CALLED BY ASM INJECTED CODE!
	 */
	public static void setLastSoundName(final String soundName) {
		lastSoundName = soundName;
	}

	/**
	 * CALLED BY ASM INJECTED CODE!
	 */
	// For sounds that get played normally
	public static void onPlaySound(final float posX, final float posY, final float posZ, final int sourceID) {
		onPlaySound(posX, posY, posZ, sourceID, lastSoundCategory, lastSoundName);
	}

	/**
	 * CALLED BY ASM INJECTED CODE!
	 */
	// For sounds that get played using OpenAL directly or just not using the minecraft sound system
	public static void onPlaySoundAL(final float posX, final float posY, final float posZ, final int sourceID) {
		onPlaySound(posX, posY, posZ, sourceID, SoundCategory.MASTER, "openal");
	}

	/**
	 * CALLED BY ASM INJECTED CODE!
	 */
	public static void onPlaySound(final float posX, final float posY, final float posZ, final int sourceID, SoundCategory soundCat, String soundName) {
		//log(String.valueOf(posX)+" "+String.valueOf(posY)+" "+String.valueOf(posZ)+" - "+String.valueOf(sourceID)+" - "+soundCat.toString()+" - "+soundName);
		if (Config.noteBlockEnable.get() && soundCat == SoundCategory.RECORDS && noteBlockPattern.matcher(soundName).matches())
			soundCat = SoundCategory.BLOCKS;
		evaluateEnvironment(sourceID, posX, posY, posZ, soundCat, soundName);
		if (!Config.dynamicEnvironementEvalutaion.get()) return;
		if ((mc.player == null || mc.world == null || posY <= 0 || soundCat == SoundCategory.RECORDS
				|| soundCat == SoundCategory.MUSIC) || (Config.skipRainOcclusionTracing.get() && rainPattern.matcher(soundName).matches()))
			return;
		if (clickPattern.matcher(soundName).matches() || uiPattern.matcher(soundName).matches()) return;
		Source tmp = new Source(sourceID, posX, posY, posZ, soundCat, soundName);
		Source.source_check_add(tmp);
	}

	/**
	 * CALLED BY ASM INJECTED CODE!
	 */
	public static AudioStreamBuffer onLoadSound(AudioStreamBuffer buff, String filename) {
		if (buff == null || buff.field_216476_b.getChannels() == 1 || !Config.autoSteroDownmix.get()) return buff;
		if (mc.player == null || mc.world == null || lastSoundCategory == SoundCategory.RECORDS
				| lastSoundCategory == SoundCategory.MUSIC || uiPattern.matcher(filename).matches() || clickPattern.matcher(filename).matches()) {
			if (Config.autoSteroDownmixLogging.get())
				SoundPhysics.log("Not converting sound '" + filename + "'(" + buff.field_216476_b.toString() + ")");
			return buff;
		}
		AudioFormat orignalformat = buff.field_216476_b;
		int bits = orignalformat.getSampleSizeInBits();
		boolean bigendian = orignalformat.isBigEndian();
		AudioFormat monoformat = new AudioFormat(orignalformat.getEncoding(), orignalformat.getSampleRate(), bits,
				1, orignalformat.getFrameSize(), orignalformat.getFrameRate(), bigendian);
		if (Config.autoSteroDownmixLogging.get())
			SoundPhysics.log("Converting sound '" + filename + "'(" + orignalformat.toString() + ") to mono (" + monoformat.toString() + ")");

		ByteBuffer bb = buff.field_216475_a;
		bb.order(bigendian ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
		if (bits == 8) {
			for (int i = 0; i < bb.array().length; i += 2) {
				bb.put(i / 2, (byte) ((bb.get(i) + bb.get(i + 1)) / 2));
			}
		} else if (bits == 16) {
			for (int i = 0; i < bb.array().length; i += 4) {
				bb.putShort((i / 2), (short) ((bb.getShort(i) + bb.getShort(i + 2)) / 2));
			}
		}
		buff.field_216476_b = monoformat;
		// buff.trimData(bb.array().length/2); TODO: trimData is gone. Alternatives?
		return buff;
	}

	/**
	 * CALLED BY ASM INJECTED CODE!
	 */
	public static double calculateEntitySoundOffset(final Entity entity, final SoundEvent sound) {
		if (sound == null) return entity.getEyeHeight();
		if (stepPattern.matcher(sound.getName().getPath()).matches()) {
			return 0;
		}

		return entity.getEyeHeight();
	}

	/**
	 * CALLED BY ASM INJECTED CODE!
	 */
	/*public static Vec3d computronicsOffset(Vec3d or, TileEntity te, DirectionProperty pd) {
		if (!te.hasWorld()) return or;
		Direction ef = te.getWorld().getBlockState(te.getPos()).get(pd);
		Vec3d efv = getNormalFromFacing(ef).scale(0.51);
		return or.add(efv);
	}*/

	/**
	 * CALLED BY ASM INJECTED CODE!
	 */
	/*public static void onSetListener(Entity player, float partial_tick) {
		float motionX = (float)((player.posX - player.prevPosX) * 20.0d);
		float motionY = (float)((player.posY - player.prevPosY) * 20.0d);
		float motionZ = (float)((player.posZ - player.prevPosZ) * 20.0d);
		sndSystem.setListenerVelocity(motionX,motionY,motionZ);
	}*/

	/**
	 * CALLED BY ASM INJECTED CODE!
	 */
	/*public static void onIRUpdate(Vec3d velocity, String source, float pitch) {
		float motionX = (float)(velocity.x * 20.0d);
		float motionY = (float)(velocity.y * 20.0d);
		float motionZ = (float)(velocity.z * 20.0d);
		sndSystem.CommandQueue(new CommandObject(CommandObject.SET_VELOCITY, source, motionX, motionY, motionZ));
		sndSystem.CommandQueue(new CommandObject(CommandObject.SET_PITCH, source, pitch));
	}*/


}
