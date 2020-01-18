function initializeCoreMod() {
	return {
		/*'SoundEngine_setLastSoundCategory': {
			'target': {
				'type': 'METHOD',
				'class': 'net.minecraft.client.audio.SoundEngine',
				'methodName': 'func_148611_c', // play
				'methodDesc': '(Lnet/minecraft/client/audio/ISound;)V'
			},
			'transformer': function(methodNode) {
				var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
				var Opcodes = Java.type('org.objectweb.asm.Opcodes');
				var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');

				var newInstructions = ASMAPI.listOf(
					new VarInsnNode(Opcodes.ALOAD, 7),
					ASMAPI.buildMethodCall(
						"com/sonicether/soundphysics/coremod/ASMHooks", 
						"setLastSoundCategory",
						"(Lnet/minecraft/util/SoundCategory;)V", 
						ASMAPI.MethodType.STATIC)
				);

				var isTransformed = ASMAPI.insertInsnList(
					methodNode,
					ASMAPI.MethodType.STATIC,
					"net.minecraft.client.audio.SoundEngine",
					"setVolume",
					"(Lnet/minecraft/client/audio/ISound;)V",
					newInstructions,
					ASMAPI.InsertMode.INSERT_AFTER
				)

				if (!isTransformed) {
					ASMAPI.log("WARN", "Could not find target for: " + methodNode.name + methodNode.desc);
				}

				return methodNode;
			}
		},*/
		/*'SoundEngine_setLastSoundName': {
			'target': {
				'type': 'METHOD',
				'class': 'net.minecraft.client.audio.SoundEngine',
				'methodName': 'func_148611_c', // play
				'methodDesc': '(Lnet/minecraft/client/audio/ISound;)V'
			},
			'transformer': function(methodNode) {
				var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
				var Opcodes = Java.type('org.objectweb.asm.Opcodes');
				var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');

				var newInstructions = ASMAPI.listOf(
					new VarInsnNode(Opcodes.ALOAD, 4),
					ASMAPI.buildMethodCall(
						"net.minecraft.client.audio.Sound", 
						"func_188719_a", // getSoundLocation
						"()Lnet/minecraft/util/ResourceLocation;", 
						ASMAPI.MethodType.VIRTUAL),
					ASMAPI.buildMethodCall(
						"net/minecraft/util/ResourceLocation", 
						"toString",
						"()Ljava/lang/String;", 
						ASMAPI.MethodType.VIRTUAL),
					new VarInsnNode(Opcodes.ALOAD, 3),
					ASMAPI.buildMethodCall(
						"net/minecraft/util/ResourceLocation", 
						"toString",
						"()Ljava/lang/String;", 
						ASMAPI.MethodType.VIRTUAL),
					ASMAPI.buildMethodCall(
						"com/sonicether/soundphysics/coremod/ASMHooks", 
						"setLastSoundName",
						"(Ljava/lang/String;Ljava/lang/String;)V", 
					ASMAPI.MethodType.STATIC)
				);

				var isTransformed = ASMAPI.insertInsnList(
					methodNode,
					ASMAPI.MethodType.STATIC,
					"net.minecraft.client.audio.SoundEngine",
					"setVolume",
					"(Lnet/minecraft/client/audio/ISound;)V",
					newInstructions,
					ASMAPI.InsertMode.INSERT_AFTER
				)

				if (!isTransformed) {
					ASMAPI.log("WARN", "Could not find target for: " + methodNode.name + methodNode.desc);
				}

				return methodNode;
			}
		},*/
		'SoundEngine_globalVolumeMultiplier': {
			'target': {
				'type': 'METHOD',
				'class': 'net.minecraft.client.audio.SoundEngine',
				'methodName': 'func_148611_c', // play
				'methodDesc': '(Lnet/minecraft/client/audio/ISound;)V'
			},
			'transformer': function (methodNode) {
				var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
				var Opcodes = Java.type('org.objectweb.asm.Opcodes');
				var FieldInsnNode = Java.type('org.objectweb.asm.tree.FieldInsnNode');
				var InsnNode = Java.type('org.objectweb.asm.tree.InsnNode');

				var newInstructions = ASMAPI.listOf(
					new FieldInsnNode(
						Opcodes.GETSTATIC,
						"com/sonicether/soundphysics/coremod/ASMHooks",
						"globalVolumeMultiplier",
						"F"),
					new InsnNode(Opcodes.FMUL)
				);

				var isTransformed = ASMAPI.insertInsnList(
					methodNode,
					ASMAPI.MethodType.SPECIAL,
					"net/minecraft/client/audio/SoundEngine",
					ASMAPI.mapMethod("func_188770_e"), // getClampedVolume
					"(Lnet/minecraft/client/audio/ISound;)F",
					newInstructions,
					ASMAPI.InsertMode.INSERT_AFTER
				)

				if (!isTransformed) {
					ASMAPI.log("WARN", "Could not find target for: " + methodNode.name + methodNode.desc);
				}

				return methodNode;
			}
		}
	}
}
