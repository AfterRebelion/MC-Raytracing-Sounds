package com.sonicether.soundphysics;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.ListIterator;

//import net.minecraft.launchwrapper.IClassTransformer;

//import net.minecraftforge.fml.Loader;
//import net.minecraftforge.fml.common.ModContainer;


public class CoreModInjector { // implements IClassTransformer {

	public static final Logger logger = LogManager.getLogger(SoundPhysics.modid + "injector");

/*
	public static boolean shouldPatchDS() {
		if (Loader.isModLoaded("dsurround")) {
			Map<String, ModContainer> mods = Loader.instance().getIndexedModList();
			String version[] = mods.get("dsurround").getVersion().split("\\.");
			if (version.length < 2) {
				logError("What the hell, DS's version is not properly formatted ?");
			} else if (version[1].equals("5")) {
				return true;
			}
		}
		return false;
	}
*/

	public byte[] transform(final String obfuscated, final String deobfuscated, byte[] bytes) {
		if (obfuscated.equals("chm$a")) {
			// Inside SoundManager.SoundSystemStarterThread
			InsnList toInject = new InsnList();
			toInject.add(new VarInsnNode(Opcodes.ALOAD, 0));
			toInject.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/sonicether/soundphysics/SoundPhysics", "init",
					"(Lpaulscode/sound/SoundSystem;)V", false));

			// Target method: Constructor
			// It seems like they directly implemented SoundSystem constructor, so this should be changed to
			// be injected there in SoundSystem.
			// More like func_216404_a... maybe? Seems to be the init method.
			bytes = patchMethodInClass(obfuscated, bytes, "<init>", "(Lchm;)V", Opcodes.INVOKESPECIAL,
					AbstractInsnNode.METHOD_INSN, "<init>", null, -1, toInject, false, 0, 0, false, 0, -1);
		} else if (obfuscated.equals("chm")) {
			// Inside SoundEngine
			InsnList toInject = new InsnList();
			toInject.add(new VarInsnNode(Opcodes.ALOAD, 7));
			toInject.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/sonicether/soundphysics/SoundPhysics",
					"setLastSoundCategory", "(Lqg;)V", false));

			// Target method: playSound
			// This method call was removed.
			// it seems setVolume is now only called from SoundHandler
			//
			// .. now that more mappings are avaliable, it seems like there is another setVolume in SoundEngine
			// Used only once inside SoundHandler, in setSoundLevel (Changed names, old name was setVolume)
			bytes = patchMethodInClass(obfuscated, bytes, "c", "(Lcgt;)V", Opcodes.INVOKEVIRTUAL,
					AbstractInsnNode.METHOD_INSN, "setVolume", null, -1, toInject, false, 0, 0, false, 0, -1);

			// ================================================================================================
			toInject = new InsnList();
			toInject.add(new VarInsnNode(Opcodes.ALOAD, 4));
			toInject.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "cgq", "a", "()Lnf;", false));
			toInject.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "nf", "toString", "()Ljava/lang/String;", false));
			toInject.add(new VarInsnNode(Opcodes.ALOAD, 3));
			toInject.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "nf", "toString", "()Ljava/lang/String;", false));
			toInject.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/sonicether/soundphysics/SoundPhysics",
					"setLastSoundName", "(Ljava/lang/String;Ljava/lang/String;)V", false));

			// Target method: playSound
			// This method call was removed.
			// it seems setVolume is now only called from SoundHandler
			//
			// .. now that more mappings are avaliable, it seems like there is another setVolume in SoundEngine
			// Used only once inside SoundHandler, in setSoundLevel (Changed names, old name was setVolume)
			bytes = patchMethodInClass(obfuscated, bytes, "c", "(Lcgt;)V", Opcodes.INVOKEVIRTUAL,
					AbstractInsnNode.METHOD_INSN, "setVolume", null, -1, toInject, false, 0, 0, false, 0, -1);

			// ================================================================================================
			toInject = new InsnList();
			toInject.add(new FieldInsnNode(Opcodes.GETSTATIC, "com/sonicether/soundphysics/SoundPhysics",
					"globalVolumeMultiplier", "F"));
			toInject.add(new InsnNode(Opcodes.FMUL));

			// Target method: playSound, target invocation getClampedVolume
			// This still exist in the same way it did before
			bytes = patchMethodInClass(obfuscated, bytes, "c", "(Lcgt;)V", Opcodes.INVOKESPECIAL,
					AbstractInsnNode.METHOD_INSN, "e", "(Lcgt;)F", -1, toInject, false, 0, 0, false, 0, -1);

			// ================================================================================================
			/*toInject = new InsnList();
			toInject.add(new VarInsnNode(Opcodes.ALOAD, 1));
			toInject.add(new VarInsnNode(Opcodes.FLOAD, 2));
			toInject.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/sonicether/soundphysics/SoundPhysics",
					"onSetListener", "(Lvg;F)V", false));

			// This function has been added by forge so the name isn't obfuscated
			bytes = patchMethodInClass(obfuscated, bytes, "setListener", "(Lvg;F)V", Opcodes.INVOKEVIRTUAL,
					AbstractInsnNode.METHOD_INSN, "setListenerOrientation", null, -1, toInject, false, 0, 0, false, 0, -1);*/
		} else if (obfuscated.equals("paulscode.sound.libraries.SourceLWJGLOpenAL")) {
			// Inside SourceLWJGLOpenAL
			// yeah... this is gone
			InsnList toInject = new InsnList();

			toInject.add(new VarInsnNode(Opcodes.ALOAD, 0));
			toInject.add(new FieldInsnNode(Opcodes.GETFIELD, "paulscode/sound/libraries/SourceLWJGLOpenAL", "position",
					"Lpaulscode/sound/Vector3D;"));
			toInject.add(new FieldInsnNode(Opcodes.GETFIELD, "paulscode/sound/Vector3D", "x", "F"));
			toInject.add(new VarInsnNode(Opcodes.ALOAD, 0));
			toInject.add(new FieldInsnNode(Opcodes.GETFIELD, "paulscode/sound/libraries/SourceLWJGLOpenAL", "position",
					"Lpaulscode/sound/Vector3D;"));
			toInject.add(new FieldInsnNode(Opcodes.GETFIELD, "paulscode/sound/Vector3D", "y", "F"));
			toInject.add(new VarInsnNode(Opcodes.ALOAD, 0));
			toInject.add(new FieldInsnNode(Opcodes.GETFIELD, "paulscode/sound/libraries/SourceLWJGLOpenAL", "position",
					"Lpaulscode/sound/Vector3D;"));
			toInject.add(new FieldInsnNode(Opcodes.GETFIELD, "paulscode/sound/Vector3D", "z", "F"));
			toInject.add(new VarInsnNode(Opcodes.ALOAD, 0));
			toInject.add(new FieldInsnNode(Opcodes.GETFIELD, "paulscode/sound/libraries/SourceLWJGLOpenAL",
					"channelOpenAL", "Lpaulscode/sound/libraries/ChannelLWJGLOpenAL;"));
			toInject.add(new FieldInsnNode(Opcodes.GETFIELD, "paulscode/sound/libraries/ChannelLWJGLOpenAL", "ALSource",
					"Ljava/nio/IntBuffer;"));
			toInject.add(new InsnNode(Opcodes.ICONST_0));
			toInject.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/nio/IntBuffer", "get", "(I)I", false));
			toInject.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/sonicether/soundphysics/SoundPhysics",
					"onPlaySound", "(FFFI)V", false));

			// Target method: play
			bytes = patchMethodInClass(obfuscated, bytes, "play", "(Lpaulscode/sound/Channel;)V", Opcodes.INVOKEVIRTUAL,
					AbstractInsnNode.METHOD_INSN, "play", null, -1, toInject, false, 0, 0, false, 0, -1);
			// Convert stero sounds to mono
		} else if (obfuscated.equals("paulscode.sound.libraries.LibraryLWJGLOpenAL") && Config.autoSteroDownmix.get()) {
			// Inside LibraryLWJGLOpenAL
			// This is also gone
			InsnList toInject = new InsnList();

			toInject.add(new VarInsnNode(Opcodes.ALOAD, 4));
			toInject.add(new VarInsnNode(Opcodes.ALOAD, 1));
			toInject.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "paulscode/sound/FilenameURL", "getFilename", "()Ljava/lang/String;", false));

			toInject.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/sonicether/soundphysics/SoundPhysics",
					"onLoadSound", "(Lpaulscode/sound/SoundBuffer;Ljava/lang/String;)Lpaulscode/sound/SoundBuffer;", false));

			toInject.add(new VarInsnNode(Opcodes.ASTORE, 4));
			//buffer = onLoadSound(SoundPhysics.buffer,filenameURL.getFilename());

			// Target method: loadSound
			bytes = patchMethodInClass(obfuscated, bytes, "loadSound", "(Lpaulscode/sound/FilenameURL;)Z", Opcodes.INVOKEINTERFACE,
					AbstractInsnNode.METHOD_INSN, "cleanup", null, -1, toInject, false, 0, 0, false, 0, -1);

			// ================================================================================================
			toInject = new InsnList();

			toInject.add(new VarInsnNode(Opcodes.ALOAD, 0));
			toInject.add(new VarInsnNode(Opcodes.ALOAD, 1));

			toInject.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/sonicether/soundphysics/SoundPhysics",
					"onLoadSound", "(Lpaulscode/sound/SoundBuffer;Ljava/lang/String;)Lpaulscode/sound/SoundBuffer;", false));

			toInject.add(new VarInsnNode(Opcodes.ASTORE, 0));

			// Target method: loadSound
			bytes = patchMethodInClass(obfuscated, bytes, "loadSound", "(Lpaulscode/sound/SoundBuffer;Ljava/lang/String;)Z", Opcodes.INVOKEVIRTUAL,
					AbstractInsnNode.METHOD_INSN, "getChannels", null, -1, toInject, true, 0, 0, false, -12, -1);
		} else if (obfuscated.equals("paulscode.sound.SoundSystem")) {
			// Inside SoundSystem
			InsnList toInject = new InsnList();

			toInject.add(new FieldInsnNode(Opcodes.GETSTATIC, "com/sonicether/soundphysics/SoundPhysics",
					"attenuationModel", "I"));
			toInject.add(new FieldInsnNode(Opcodes.GETSTATIC, "com/sonicether/soundphysics/SoundPhysics",
					"globalRolloffFactor", "F"));

			// Target method: newSource
			bytes = patchMethodInClass(obfuscated, bytes, "newSource",
					"(ZLjava/lang/String;Ljava/net/URL;Ljava/lang/String;ZFFFIF)V", Opcodes.INVOKESPECIAL,
					AbstractInsnNode.METHOD_INSN, "<init>", null, -1, toInject, true, 2, 0, false, 0, -1);
		} else if (obfuscated.equals("pl")) {
			// Inside PlayerList
			InsnList toInject = new InsnList();

			// Multiply sound distance volume play decision by
			// SoundPhysics.soundDistanceAllowance
			toInject.add(new FieldInsnNode(Opcodes.GETSTATIC, "com/sonicether/soundphysics/SoundPhysics",
					"soundDistanceAllowance", "D"));
			toInject.add(new InsnNode(Opcodes.DMUL));

			// Target method: sendToAllNearExcept
			bytes = patchMethodInClass(obfuscated, bytes, "a", "(Laed;DDDDILht;)V", Opcodes.DCMPG,
					AbstractInsnNode.INSN, "", "", -1, toInject, true, 0, 0, false, 0, -1);
		} else if (obfuscated.equals("vg")) {
			// Inside Entity
			InsnList toInject = new InsnList();

			// Offset entity sound by their eye height
			toInject.add(new VarInsnNode(Opcodes.ALOAD, 1));
			toInject.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/sonicether/soundphysics/SoundPhysics",
					"calculateEntitySoundOffset", "(Lvg;Lqe;)D", false));
			toInject.add(new InsnNode(Opcodes.DADD));
			toInject.add(new VarInsnNode(Opcodes.ALOAD, 0));

			// Target method: playSound
			// Inside target method, target node: Entity/getSoundCategory
			bytes = patchMethodInClass(obfuscated, bytes, "a", "(Lqe;FF)V", Opcodes.INVOKEVIRTUAL,
					AbstractInsnNode.METHOD_INSN, "bK", null, -1, toInject, true, 0, 0, false, -3, -1);
		} /*else

		// Fix for computronics's devices
		if (obfuscated.equals("pl.asie.lib.audio.StreamingAudioPlayer") && Config.computronicsPatching) {
			// Inside StreamingAudioPlayer
			InsnList toInject = new InsnList();

			toInject.add(new VarInsnNode(Opcodes.FLOAD, 2));
			toInject.add(new VarInsnNode(Opcodes.FLOAD, 3));
			toInject.add(new VarInsnNode(Opcodes.FLOAD, 4));
			toInject.add(new VarInsnNode(Opcodes.ALOAD, 8));
			toInject.add(new FieldInsnNode(Opcodes.GETFIELD, "pl/asie/lib/audio/StreamingAudioPlayer$SourceEntry", "src",
					"Ljava/nio/IntBuffer;"));
			toInject.add(new InsnNode(Opcodes.ICONST_0));
			toInject.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/nio/IntBuffer", "get", "(I)I", false));

			toInject.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/sonicether/soundphysics/SoundPhysics",
					"onPlaySoundAL", "(FFFI)V", false));

			// Target method: play
			bytes = patchMethodInClass(obfuscated, bytes, "play", "(Ljava/lang/String;FFFF)V", Opcodes.INVOKESTATIC,
					AbstractInsnNode.METHOD_INSN, "alSourceQueueBuffers", null, -1, toInject, true, 0, 0, false, -5, -1);

			toInject = new InsnList();

			toInject.add(new FieldInsnNode(Opcodes.GETSTATIC, "com/sonicether/soundphysics/SoundPhysics",
					"soundDistanceAllowance", "D"));
			toInject.add(new InsnNode(Opcodes.D2F));
			toInject.add(new InsnNode(Opcodes.FMUL));

			// Target method: setHearing
			bytes = patchMethodInClass(obfuscated, bytes, "setHearing", "(FF)V", Opcodes.FLOAD,
					AbstractInsnNode.VAR_INSN, "", null, 1, toInject, false, 0, 0, false, 0, -1);
		} else

		if (obfuscated.equals("pl.asie.computronics.api.audio.AudioPacket") && Config.computronicsPatching) {
			// Inside AudioPacket
			InsnList toInject = new InsnList();

			toInject.add(new FieldInsnNode(Opcodes.GETSTATIC, "com/sonicether/soundphysics/SoundPhysics",
					"soundDistanceAllowance", "D"));
			toInject.add(new FieldInsnNode(Opcodes.GETSTATIC, "com/sonicether/soundphysics/SoundPhysics",
					"soundDistanceAllowance", "D"));
			toInject.add(new InsnNode(Opcodes.DMUL));
			toInject.add(new InsnNode(Opcodes.D2I));
			toInject.add(new InsnNode(Opcodes.IMUL));

			// Target method: canHearReceiver
			bytes = patchMethodInClass(obfuscated, bytes, "canHearReceiver", "(Lnet/minecraft/entity/player/EntityPlayerMP;Lpl/asie/computronics/api/audio/IAudioReceiver;)Z", Opcodes.IMUL,
					AbstractInsnNode.INSN, "", null, -1, toInject, false, 0, 0, false, 0, -1);
		} else

		if (obfuscated.equals("pl.asie.computronics.tile.TileTapeDrive$1") && Config.computronicsPatching) {
			// Inside TileTapeDrive.internalSpeaker
			InsnList toInject = new InsnList();

			toInject.add(new VarInsnNode(Opcodes.ALOAD, 0));
			toInject.add(new FieldInsnNode(Opcodes.GETFIELD, "pl/asie/computronics/tile/TileTapeDrive$1", "this$0",
					"Lpl/asie/computronics/tile/TileTapeDrive;"));
			toInject.add(new FieldInsnNode(Opcodes.GETSTATIC, "pl/asie/computronics/Computronics",
					"tapeReader", "Lpl/asie/computronics/block/BlockTapeReader;"));
			toInject.add(new FieldInsnNode(Opcodes.GETFIELD, "pl/asie/computronics/block/BlockTapeReader", "rotation",
					"Lpl/asie/lib/block/BlockBase$Rotation;"));
			toInject.add(new FieldInsnNode(Opcodes.GETFIELD, "pl/asie/lib/block/BlockBase$Rotation", "FACING",
					"Lnet/minecraft/block/properties/PropertyDirection;"));
			toInject.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/sonicether/soundphysics/SoundPhysics", "computronicsOffset",
					"(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/tileentity/TileEntity;Lnet/minecraft/block/properties/PropertyDirection;)Lnet/minecraft/util/math/Vec3d;", false));

			// Target method: getSoundPos
			bytes = patchMethodInClass(obfuscated, bytes, "getSoundPos", "()Lnet/minecraft/util/math/Vec3d;", Opcodes.ARETURN,
					AbstractInsnNode.INSN, "", null, -1, toInject, true, 0, 0, false, 0, -1);
		} else

		if (obfuscated.equals("pl.asie.computronics.tile.TileSpeaker") && Config.computronicsPatching) {
			// Inside TileSpeaker
			InsnList toInject = new InsnList();

			toInject.add(new VarInsnNode(Opcodes.ALOAD, 0));
			toInject.add(new FieldInsnNode(Opcodes.GETSTATIC, "pl/asie/computronics/Computronics",
					"speaker", "Lpl/asie/computronics/block/BlockSpeaker;"));
			toInject.add(new FieldInsnNode(Opcodes.GETFIELD, "pl/asie/computronics/block/BlockSpeaker", "rotation",
					"Lpl/asie/lib/block/BlockBase$Rotation;"));
			toInject.add(new FieldInsnNode(Opcodes.GETFIELD, "pl/asie/lib/block/BlockBase$Rotation", "FACING",
					"Lnet/minecraft/block/properties/PropertyDirection;"));
			toInject.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/sonicether/soundphysics/SoundPhysics", "computronicsOffset",
					"(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/tileentity/TileEntity;Lnet/minecraft/block/properties/PropertyDirection;)Lnet/minecraft/util/math/Vec3d;", false));

			// Target method: getSoundPos
			bytes = patchMethodInClass(obfuscated, bytes, "getSoundPos", "()Lnet/minecraft/util/math/Vec3d;", Opcodes.ARETURN,
					AbstractInsnNode.INSN, "", null, -1, toInject, true, 0, 0, false, 0, -1);
		} else

		if (obfuscated.equals("pl.asie.computronics.tile.TileSpeechBox$1") && Config.computronicsPatching) {
			// Inside TileSpeechBox.internalSpeaker
			InsnList toInject = new InsnList();

			toInject.add(new VarInsnNode(Opcodes.ALOAD, 0));
			toInject.add(new FieldInsnNode(Opcodes.GETFIELD, "pl/asie/computronics/tile/TileSpeechBox$1", "this$0",
					"Lpl/asie/computronics/tile/TileSpeechBox;"));
			toInject.add(new FieldInsnNode(Opcodes.GETSTATIC, "pl/asie/computronics/Computronics",
					"speechBox", "Lpl/asie/computronics/block/BlockSpeechBox;"));
			toInject.add(new FieldInsnNode(Opcodes.GETFIELD, "pl/asie/computronics/block/BlockSpeechBox", "rotation",
					"Lpl/asie/lib/block/BlockBase$Rotation;"));
			toInject.add(new FieldInsnNode(Opcodes.GETFIELD, "pl/asie/lib/block/BlockBase$Rotation", "FACING",
					"Lnet/minecraft/block/properties/PropertyDirection;"));
			toInject.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/sonicether/soundphysics/SoundPhysics", "computronicsOffset",
					"(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/tileentity/TileEntity;Lnet/minecraft/block/properties/PropertyDirection;)Lnet/minecraft/util/math/Vec3d;", false));

			// Target method: getSoundPos
			bytes = patchMethodInClass(obfuscated, bytes, "getSoundPos", "()Lnet/minecraft/util/math/Vec3d;", Opcodes.ARETURN,
					AbstractInsnNode.INSN, "", null, -1, toInject, true, 0, 0, false, 0, -1);
		} else

		if ((obfuscated.equals("cam72cam.immersiverailroading.sound.ClientSound") || obfuscated.equals("cam72cam.mod.sound.ClientSound")) && Config.irPatching) {
			// Inside ClientSound
			InsnList toInject = new InsnList();

			final boolean newIR = obfuscated.equals("cam72cam.mod.sound.ClientSound");
			final String classCS = obfuscated.replace(".","/");
			final String playDesc = newIR ? "(Lcam72cam/mod/math/Vec3d;)V" : "(Lnet/minecraft/util/math/Vec3d;)V";
			final String classRes = newIR ? "cam72cam/mod/resource/Identifier" : "nf";

			toInject.add(new FieldInsnNode(Opcodes.GETSTATIC, "qg","i", "Lqg;")); // Ambient sound category
			toInject.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/sonicether/soundphysics/SoundPhysics",
					"setLastSoundCategory", "(Lqg;)V", false));
			toInject.add(new VarInsnNode(Opcodes.ALOAD, 0));
			toInject.add(new FieldInsnNode(Opcodes.GETFIELD, classCS, "oggLocation",
					"L"+classRes+";"));
			toInject.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, classRes, "toString", "()Ljava/lang/String;", false));
			toInject.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/sonicether/soundphysics/SoundPhysics",
					"setLastSoundName", "(Ljava/lang/String;)V", false));

			// Target method: play
			bytes = patchMethodInClass(obfuscated, bytes, "play", playDesc, Opcodes.INVOKEVIRTUAL,
					AbstractInsnNode.METHOD_INSN, "update", null, -1, toInject, false, 0, 0, false, 0, -1);

			toInject = new InsnList();

			toInject.add(new FieldInsnNode(Opcodes.GETSTATIC, "com/sonicether/soundphysics/SoundPhysics",
					"soundDistanceAllowance", "D"));
			toInject.add(new InsnNode(Opcodes.DMUL));

			// Target method: play
			bytes = patchMethodInClass(obfuscated, bytes, "play", playDesc, Opcodes.DCMPG,
					AbstractInsnNode.INSN, "", null, -1, toInject, true, 0, 0, false, 0, -1);

			toInject = new InsnList();

			toInject.add(new FieldInsnNode(Opcodes.GETSTATIC, "com/sonicether/soundphysics/SoundPhysics",
					"globalVolumeMultiplier", "F"));
			toInject.add(new InsnNode(Opcodes.FMUL));

			// Target method: update
			bytes = patchMethodInClass(obfuscated, bytes, "update", "()V", Opcodes.FMUL,
					AbstractInsnNode.INSN, "", null, -1, toInject, true, 0, 0, false, 0, -1);

			toInject = new InsnList();

			toInject.add(new LdcInsnNode(1.75d));
			toInject.add(new VarInsnNode(Opcodes.ALOAD, 0));
			toInject.add(new FieldInsnNode(Opcodes.GETFIELD, "cam72cam/immersiverailroading/sound/ClientSound", "gauge",
					"Lcam72cam/immersiverailroading/library/Gauge;"));
			toInject.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "cam72cam/immersiverailroading/library/Gauge", "scale", "()D", false));
			toInject.add(new InsnNode(Opcodes.DMUL));
			toInject.add(new InsnNode(Opcodes.DADD));

			// Target method: update
			bytes = patchMethodInClass(obfuscated, bytes, "update", "()V", Opcodes.INVOKESPECIAL,
					AbstractInsnNode.METHOD_INSN, "<init>", "(ILjava/lang/String;FFF)V", -1, toInject, true, 0, 0, false, -5, -1);

			if (Config.dopplerEnabled) { // IR has its own doppler shift so we remove that and just give the velocity to OpenAL so that it does it itself.
				toInject = new InsnList();

				toInject.add(new VarInsnNode(Opcodes.ALOAD, 0));
				toInject.add(new FieldInsnNode(Opcodes.GETFIELD, "cam72cam/immersiverailroading/sound/ClientSound", "velocity",
					"Lbhe;"));
				toInject.add(new VarInsnNode(Opcodes.ALOAD, 0));
				toInject.add(new FieldInsnNode(Opcodes.GETFIELD, "cam72cam/immersiverailroading/sound/ClientSound", "id",
					"Ljava/lang/String;"));
				toInject.add(new VarInsnNode(Opcodes.ALOAD, 0));
				toInject.add(new FieldInsnNode(Opcodes.GETFIELD, "cam72cam/immersiverailroading/sound/ClientSound", "currentPitch",
					"F"));
				toInject.add(new VarInsnNode(Opcodes.FLOAD, 2));
				toInject.add(new InsnNode(Opcodes.FDIV));
				toInject.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/sonicether/soundphysics/SoundPhysics",
					"onIRUpdate", "(Lbhe;Ljava/lang/String;F)V", false));

				// Target method: update
				bytes = patchMethodInClass(obfuscated, bytes, "update", "()V", Opcodes.INVOKEVIRTUAL,
					AbstractInsnNode.METHOD_INSN, "CommandQueue", null, -1, toInject, false, 108, 1, true, 0, -1);
			}
		} else

		if (obfuscated.equals("org.orecruncher.dsurround.client.sound.SoundEffect") && Config.dsPatching && shouldPatchDS()) {
			// Inside SoundEffect
			InsnList toInject = new InsnList();

			toInject.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "org/orecruncher/dsurround/client/sound/SoundInstance",
					"noAttenuation", "()Lcgt$a;", false));

			// Target method: createTrackingSound
			bytes = patchMethodInClass(obfuscated, bytes, "createTrackingSound", "(Lnet/minecraft/entity/Entity;Z)Lorg/orecruncher/dsurround/client/sound/SoundInstance;", Opcodes.GETSTATIC,
					AbstractInsnNode.FIELD_INSN, "", null, -1, toInject, true, 0, 0, true, 0, -1);
		} else

		if (obfuscated.equals("org.orecruncher.dsurround.client.sound.ConfigSoundInstance") && Config.dsPatching && shouldPatchDS()) {
			// Inside ConfigSoundInstance
			InsnList toInject = new InsnList();

			toInject.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "org/orecruncher/dsurround/client/sound/SoundInstance",
					"noAttenuation", "()Lcgt$a;", false));

			// Target method: ConfigSoundInstance
			bytes = patchMethodInClass(obfuscated, bytes, "<init>", "(Ljava/lang/String;F)V", Opcodes.GETSTATIC,
					AbstractInsnNode.FIELD_INSN, "", null, -1, toInject, true, 0, 0, true, 0, 1);
		}*/

		//System.out.println("[SP Inject] "+obfuscated+" ("+deobfuscated+")");

		return bytes;
	}

	private static Printer printer = new Textifier();
	private static TraceMethodVisitor mp = new TraceMethodVisitor(printer);

	public static String insnToString(AbstractInsnNode insn) {
		insn.accept(mp);
		StringWriter sw = new StringWriter();
		printer.print(new PrintWriter(sw));
		printer.getText().clear();
		return sw.toString();
	}

	private byte[] patchMethodInClass(String className, final byte[] bytes, final String targetMethod,
									  final String targetMethodSignature, final int targetNodeOpcode, final int targetNodeType,
									  final String targetInvocationMethodName, final String targetInvocationMethodSignature, final int targetVarNodeIndex,
									  final InsnList instructionsToInject, final boolean insertBefore, final int nodesToDeleteBefore,
									  final int nodesToDeleteAfter, final boolean deleteTargetNode, final int targetNodeOffset, final int targetNodeNumber) {
		log("Patching class : " + className);

		final ClassNode classNode = new ClassNode();
		final ClassReader classReader = new ClassReader(bytes);
		classReader.accept(classNode, 0);
		final Iterator<MethodNode> methodIterator = classNode.methods.iterator();

		while (methodIterator.hasNext()) {
			final MethodNode m = methodIterator.next();
			//log("@" + m.name + " " + m.desc);

			if (m.name.equals(targetMethod) && m.desc.equals(targetMethodSignature)) {
				log("Inside target method: " + targetMethod);

				AbstractInsnNode targetNode = null;
				int targetNodeNb = 0;

				final ListIterator<AbstractInsnNode> nodeIterator = m.instructions.iterator();
				while (nodeIterator.hasNext()) {
					AbstractInsnNode currentNode = nodeIterator.next();
					//log(insnToString(currentNode).replace("\n", ""));
					if (currentNode.getOpcode() == targetNodeOpcode) {

						if (targetNodeType == AbstractInsnNode.METHOD_INSN) {
							if (currentNode.getType() == AbstractInsnNode.METHOD_INSN) {
								final MethodInsnNode method = (MethodInsnNode) currentNode;
								if (method.name.equals(targetInvocationMethodName)) {
									if (method.desc.equals(targetInvocationMethodSignature)
											|| targetInvocationMethodSignature == null) {
										log("Found target method invocation for injection: " + targetInvocationMethodName);
										targetNode = currentNode;
										if (targetNodeNumber >= 0 && targetNodeNb == targetNodeNumber) break;
										targetNodeNb++;
									}

								}
							}
						} else if (targetNodeType == AbstractInsnNode.VAR_INSN) {
							if (currentNode.getType() == AbstractInsnNode.VAR_INSN) {
								final VarInsnNode varnode = (VarInsnNode) currentNode;
								if (targetVarNodeIndex < 0 || varnode.var == targetVarNodeIndex) {
									log("Found target var node for injection: " + targetVarNodeIndex);
									targetNode = currentNode;
									if (targetNodeNumber >= 0 && targetNodeNb == targetNodeNumber) break;
									targetNodeNb++;
								}
							}
						} else {
							if (currentNode.getType() == targetNodeType) {
								log("Found target node for injection: " + targetNodeType);
								targetNode = currentNode;
								if (targetNodeNumber >= 0 && targetNodeNb == targetNodeNumber) break;
								targetNodeNb++;
							}
						}

					}
				}

				if (targetNode == null) {
					logError("Target node not found! " + className);
					break;
				}

				// Offset the target node by the supplied offset value
				if (targetNodeOffset > 0) {
					for (int i = 0; i < targetNodeOffset; i++) {
						targetNode = targetNode.getNext();
					}
				} else if (targetNodeOffset < 0) {
					for (int i = 0; i < -targetNodeOffset; i++) {
						targetNode = targetNode.getPrevious();
					}
				}

				// If we've found the target, inject the instructions!
				for (int i = 0; i < nodesToDeleteBefore; i++) {
					final AbstractInsnNode previousNode = targetNode.getPrevious();
					//log("Removing Node " + insnToString(previousNode).replace("\n", ""));
					log("Removing Node " + previousNode.getOpcode());
					m.instructions.remove(previousNode);
				}

				for (int i = 0; i < nodesToDeleteAfter; i++) {
					final AbstractInsnNode nextNode = targetNode.getNext();
					//log("Removing Node " + insnToString(nextNode).replace("\n", ""));
					log("Removing Node " + nextNode.getOpcode());
					m.instructions.remove(nextNode);
				}

				if (insertBefore) {
					m.instructions.insertBefore(targetNode, instructionsToInject);
				} else {
					m.instructions.insert(targetNode, instructionsToInject);
				}

				if (deleteTargetNode) {
					m.instructions.remove(targetNode);
				}

				break;
			}
		}
		log("Class finished : " + className);

		final ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		classNode.accept(writer);
		return writer.toByteArray();
	}

	public static void log(final String message) {
		if (Config.injectorLogging.get()) logger.info(message);
	}

	public static void logError(final String errorMessage) {
		logger.error(errorMessage);
	}
}
