package com.sonicether.soundphysics.coremod;

import com.sonicether.soundphysics.Config;
import com.sonicether.soundphysics.SoundPhysics;
import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

public class ProcThread extends Thread {
	protected static volatile boolean thread_alive;
	protected static volatile boolean thread_signal_death;
	protected static volatile List<Source> source_list;
	protected static ProcThread proc_thread;

	public static synchronized void setupThread() {
		if (source_list == null) source_list = Collections.synchronizedList(new ArrayList<Source>());
		else source_list.clear();

		/*if (proc_thread != null) {
			thread_signal_death = false;
			thread_alive = false;
			while (!thread_signal_death);
		}*/
		if (proc_thread == null) {
			proc_thread = new ProcThread();
			thread_alive = true;
			proc_thread.start();
		}
	}

	@Override
	public synchronized void run() {
		while (thread_alive) {
			while (!Config.dynamicEnvironementEvalutaion.get()) {
				try {
					Thread.sleep(1000);
				} catch (Exception e) {
					SoundPhysics.logError(String.valueOf(e));
				}
			}
			synchronized (source_list) {
				//log("Updating env " + String.valueOf(source_list.size()));
				ListIterator<Source> iter = source_list.listIterator();
				while (iter.hasNext()) {
					Source source = iter.next();
					//log("Updating sound '" + source.name + "' SourceID:" + String.valueOf(source.sourceID));
					//boolean pl = sndHandler.isSoundPlaying(source.sound);
					//FloatBuffer pos = BufferUtils.createFloatBuffer(3);
					//AL10.alGetSource(source.sourceID,AL10.AL_POSITION,pos);
					//To try ^
					int state = AL10.alGetSourcei(source.sourceID, AL10.AL_SOURCE_STATE);
					//int byteoff = AL10.alGetSourcei(source.sourceID, AL11.AL_BYTE_OFFSET);
					//boolean finished = source.size == byteoff;
					if (state == AL10.AL_PLAYING) {
						FloatBuffer pos = BufferUtils.createFloatBuffer(3);
						AL10.alGetSourcef(source.sourceID, AL10.AL_POSITION, pos);
						source.posX = pos.get(0);
						source.posY = pos.get(1);
						source.posZ = pos.get(2);
						ASMTools.evaluateEnvironment(source.sourceID, source.posX, source.posY, source.posZ, source.category, source.name);
					} else /*if (state == AL10.AL_STOPPED)*/ {
						iter.remove();
					}
				}
			}
			try {
				Thread.sleep(1000 / Config.dynamicEnvironementEvalutaionFrequency.get());
			} catch (Exception e) {
				SoundPhysics.logError(String.valueOf(e));
			}
		}
		thread_signal_death = true;
	}
}
