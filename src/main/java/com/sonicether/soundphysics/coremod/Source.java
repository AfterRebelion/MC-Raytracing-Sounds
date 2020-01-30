package com.sonicether.soundphysics.coremod;

import net.minecraft.util.SoundCategory;
import org.lwjgl.openal.AL10;

import java.util.ListIterator;

import static com.sonicether.soundphysics.coremod.ProcThread.source_list;

/**
 *  Hybrid of {@link net.minecraft.client.audio.SimpleSound} and {@link net.minecraft.client.audio.SoundSource}
 *
 */
public class Source {
	public int sourceID;
	public float posX;
	public float posY;
	public float posZ;
	public SoundCategory category;
	public String name;
	public int frequency;
	public int size;
	public int bufferID;

	public Source(int sid, float px, float py, float pz, SoundCategory cat, String n) {
		this.sourceID = sid;
		this.posX = px;
		this.posY = py;
		this.posZ = pz;
		this.category = cat;
		this.name = n;
		bufferID = AL10.alGetSourcei(sid, AL10.AL_BUFFER);
		frequency = AL10.alGetBufferi(bufferID, AL10.AL_FREQUENCY);
		size = AL10.alGetBufferi(bufferID, AL10.AL_SIZE);
	}

	public static void source_check_add(Source s) {
		synchronized (source_list) {
			ListIterator<Source> iter = source_list.listIterator();
			while (iter.hasNext()) {
				Source sn = iter.next();
				if (sn.sourceID == s.sourceID) {
					sn.posX = s.posX;
					sn.posY = s.posY;
					sn.posZ = s.posZ;
					sn.category = s.category;
					sn.name = s.name;
					return;
				}
			}
			source_list.add(s);
		}
	}
}
