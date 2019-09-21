function initializeCoreMod() {
	return {
		'SoundSystem_init': {
			'target': {
				'type': 'METHOD',
				'class': 'net.minecraft.client.audio.SoundSystem',
				'methodName': '<init>', // Constructor
				'methodDesc': '()V'
			},
			'transformer': function (methodNode) {
				var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
				var Opcodes = Java.type('org.objectweb.asm.Opcodes');
				var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
				var InsnList = Java.type('org.objectweb.asm.tree.InsnList');

				var newInstructions = new InsnList();
				newInstructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
				newInstructions.add(ASMAPI.buildMethodCall(
					"com/sonicether/soundphysics/AsmHooks",
					"init",
					"()V",
					ASMAPI.MethodType.STATIC
				));

				var isTransformed = ASMAPI.insertInsnList(
					methodNode,
					ASMAPI.MethodType.SPECIAL,
					"java/lang/Object",
					"<init>", // constructor
					"()V",
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
