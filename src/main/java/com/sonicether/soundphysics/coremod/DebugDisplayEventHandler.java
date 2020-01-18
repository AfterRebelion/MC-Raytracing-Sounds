package com.sonicether.soundphysics.coremod;

import com.sonicether.soundphysics.Config;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ListIterator;

public class DebugDisplayEventHandler {
	@SubscribeEvent
	public static void onDebugOverlay(RenderGameOverlayEvent.Text event) {
		if (ASMTools.mc != null && ASMTools.mc.gameSettings.showDebugInfo && Config.dynamicEnvironementEvalutaion.get() && Config.debugInfoShow.get()) {
			event.getLeft().add("");
			event.getLeft().add("[SoundPhysics] " + ProcThread.source_list.size() + " Sources");
			event.getLeft().add("[SoundPhysics] Source list :");
			synchronized (ProcThread.source_list) {
				ListIterator<Source> iter = ProcThread.source_list.listIterator();
				while (iter.hasNext()) {
					Source s = iter.next();
					Vec3d tmp = new Vec3d(s.posX, s.posY, s.posZ);
					event.getLeft().add(String.valueOf(s.sourceID) + "-" + s.category.toString() + "-" + s.name + "-" + tmp.toString());
					/*int buffq = AL10.alGetSourcei(s.sourceID, AL10.AL_BUFFERS_QUEUED);
					int buffp = AL10.alGetSourcei(s.sourceID, AL10.AL_BUFFERS_PROCESSED);
					int sampoff = AL10.alGetSourcei(s.sourceID, AL11.AL_SAMPLE_OFFSET);
					int byteoff = AL10.alGetSourcei(s.sourceID, AL11.AL_BYTE_OFFSET);
					String k = "";
					if (sampoff!=0) {
						//k = String.valueOf(sampoff)+"/"+String.valueOf((byteoff/sampoff)*size)+" ";
						k = String.valueOf((float)sampoff/(float)s.frequency)+"/"+String.valueOf((float)((byteoff/sampoff)*s.size)/(float)s.frequency)+" ";
					} else {
						k = "0/? ";
					}
					event.getLeft().add(k+String.valueOf(buffp)+"/"+String.valueOf(buffq)+" "+String.valueOf(s.bufferID));
					event.getLeft().add("----");*/
				}
			}
		}
	}
}
