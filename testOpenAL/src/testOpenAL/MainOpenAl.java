package testOpenAL;

import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.openal.EXTEfx;

import org.apache.commons.io.*;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Scanner;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

public class MainOpenAl{
	
	private float[] fperson = {0f,0f,0f};
	private float[] up = {0f,-20f,0f};
	private float[] down = {0f,20f,0f};
	private float[] right = {20f,0f,0f};
	private float[] left = {-20f,0f,0f};
	float dynamic_pitch = 1f;
	float dynamic_gain = 1f;
	
	public MainOpenAl() {
		
	}
	
	public long[] selectSound(String s, float [] vect) throws Exception{
		long device = ALC10.alcOpenDevice((ByteBuffer)null);
		ALCCapabilities deviceCaps = ALC.createCapabilities(device);
		IntBuffer contextAttribList = BufferUtils.createIntBuffer(16);

		contextAttribList.put(ALC10.ALC_REFRESH);
        contextAttribList.put(60);
        contextAttribList.put(ALC10.ALC_SYNC);
        contextAttribList.put(ALC10.ALC_FALSE);
        contextAttribList.put(EXTEfx.ALC_MAX_AUXILIARY_SENDS);
        contextAttribList.put(2);
        contextAttribList.put(0);
        contextAttribList.flip();
        
        long newContext = ALC10.alcCreateContext(device, contextAttribList);
        
        if(!ALC10.alcMakeContextCurrent(newContext)) {
            throw new Exception("Failed to make context current");
        }
        
        AL.createCapabilities(deviceCaps);
        
        AL10.alListener3f(AL10.AL_VELOCITY, 0f, 0f, 0f);
        AL10.alListener3f(AL10.AL_ORIENTATION, 0f, 0f, 0f);
        IntBuffer buffer = BufferUtils.createIntBuffer(1);
        AL10.alGenBuffers(buffer);
        
        long time = createBufferData(buffer.get(0),s);
        int source = AL10.alGenSources();
        
        AL10.alSourcei(source, AL10.AL_BUFFER, buffer.get(0));
        AL10.alSource3f(source, AL10.AL_POSITION, vect[0],vect[1],vect[2]);
        AL10.alSource3f(source, AL10.AL_VELOCITY, 0f, 0f, 0f);
        
        AL10.alSourcef(source, AL10.AL_PITCH, dynamic_pitch);
        AL10.alSourcef(source, AL10.AL_GAIN, dynamic_gain);
        AL10.alSourcei(source, AL10.AL_LOOPING, AL10.AL_FALSE);
        
        long [] sol = {time,(long)source};
        return sol;
        
        
    }
	
	private long createBufferData(int p, String s) throws UnsupportedAudioFileException, IOException, URISyntaxException {

        AudioInputStream stream = null;
        stream = AudioSystem.getAudioInputStream(MainOpenAl.class.getResource(s));
        
        AudioFormat format = stream.getFormat();
        if(format.isBigEndian()) throw new UnsupportedAudioFileException("Can't handle Big Endian formats yet");
        
        int openALFormat = -1;
        switch(format.getChannels()) {
        case 1:
        	System.out.println("MONO, "+s);
            switch(format.getSampleSizeInBits()) {
                case 8:
                    openALFormat = AL10.AL_FORMAT_MONO8;
                    break;
                case 16:
                    openALFormat = AL10.AL_FORMAT_MONO16;
                    break;
            }
            break;
        case 2:
        	System.out.println("STEREO, "+s);
            switch(format.getSampleSizeInBits()) {
                case 8:
                    openALFormat = AL10.AL_FORMAT_STEREO8;
                    break;
                case 16:
                    openALFormat = AL10.AL_FORMAT_STEREO16;
                    break;
            }
            break;
    }
           
        byte[] b = IOUtils.toByteArray(stream);
        ByteBuffer data = BufferUtils.createByteBuffer(b.length).put(b);
        data.flip();
        
        AL10.alBufferData(p, openALFormat, data, (int)format.getSampleRate());
        
        return (long)(1000f * stream.getFrameLength() / format.getFrameRate());
    }
	
	
	public long [] play(MainOpenAl controler, String sound_lib_item, float [] vect) throws Exception {
		long[] st = controler.selectSound(sound_lib_item, vect);
		int source = (int)st[1];
		AL10.alSourcePlay(source);
		return st;
	}
	
	public void stop(MainOpenAl controler, int source) {
		AL10.alSourceStop(source); 
        AL10.alDeleteSources(source);
		long ccontext = ALC10.alcGetCurrentContext();
        long cdevice = ALC10.alcGetContextsDevice(ccontext);
        ALC10.alcDestroyContext(ccontext);
        ALC10.alcCloseDevice(cdevice);
	}
	
	public char get_command() throws IOException {
		@SuppressWarnings("resource")
		Scanner reader = new Scanner(System.in);
		return reader.next().charAt(0);
	}
	
	public void game_over(MainOpenAl controler) throws Exception {
		long [] st = controler.play(controler,"/game_over.wav",controler.fperson);
		System.out.println("GAME OVER");
		System.out.println("¡El propietario de la casa te ha descubierto!");
		Thread.sleep(st[0]);
		st = controler.play(controler,"/game_over.wav",controler.fperson);
		System.out.println("¿Desea volver al inicio? (S:Si/N:No)");
		char cmd = get_command();
		switch(cmd) {
			case 'S':
				generate_main_scene(controler);
			break;
		}
	}
	
	public void generate_second_scene(MainOpenAl controler) throws Exception {
		char c = '.';
		long [] st = controler.play(controler,"/wake_up.wav",controler.fperson);
		Thread.sleep((long)(st[0]*0.70));
		st = controler.play(controler,"/walk_in_house.wav",controler.fperson);
		Thread.sleep((long)(st[0]*0.70));
		boolean flag = false;
		while(!flag) {
			st = controler.play(controler,"/cough.wav",controler.right);
			System.out.println("!!!!!!!!!!");
			Thread.sleep(1000);
			System.out.println("¡Parece que alguien se encuentra en una habitación cercana!");
			Thread.sleep(1000);
			System.out.println("¡Lo mejor es desviarse!");
			Thread.sleep(1000);
			System.out.println("¿Cual será tu movimiento? "+"\n" + "(D: Ir a la derecha."+"\n" + "I: Ir a la izquierda "+"\n" + "Q: Quedarse quieto para escuchar de donde proviene el sonido)");
			c = controler.get_command();
			flag = (c != 'Q');
			controler.stop(controler, (int)st[1]);
		}
		switch(c) {
		case 'D':
			controler.game_over(controler);
		break;
		case 'I':
			controler.generate_third_scene(controler);
		}
	}
	
	private void generate_third_scene(MainOpenAl controler) throws Exception {
		char c = '.';
		long [] st = controler.play(controler,"/walk_in_house.wav",controler.fperson);
		Thread.sleep((long)(st[0]*0.70));
		System.out.println("¡Vamos! ¡La salida está justo frente a ti!");
		Thread.sleep(1500);
		st = controler.play(controler,"/knocking_door.wav",controler.fperson);
		Thread.sleep(1000);
		System.out.println("!!!!!!!!!!");
		Thread.sleep(1000);
		System.out.println("¡Parece que alguien está llamando a la puerta!");
		Thread.sleep(1000);
		System.out.println("¡Rápido, Escóndete! (E: Subir las escaleras al 2do piso" + "\n" + "P: Esconderte tras el pasillo derecho");
		c = controler.get_command();
		switch(c) {
		case 'E':
			generate_fourth_scene(controler);
			break;
		case 'P':
			st = controler.play(controler, "/walk_in_house.wav", controler.fperson);
			Thread.sleep(st[0]);
			controler.game_over(controler);
		}
		controler.stop(controler, (int)st[1]);
	}

	private void generate_fourth_scene(MainOpenAl controler) throws Exception {
		char c = '.';
		long [] st = controler.play(controler,"/walk_up_stairs.wav",controler.fperson);
		Thread.sleep((long)(st[0]*0.70));
		System.out.println("!!!!!!!!!!");
		st = controler.play(controler,"/other_room_voices.wav",controler.down);
		Thread.sleep(3000);
		st = controler.play(controler,"/piano_play.wav",controler.up);
		Thread.sleep(3000);
		st = controler.play(controler,"/lala.wav",controler.left);
		Thread.sleep(3000);
		System.out.println("¡Hay demasiadas personas en la propiedad!");
		Thread.sleep(1000);
		System.out.println("Es ahora o nunca ¿Por donde quieres salir?");
		Thread.sleep(1000);
		System.out.println("V: Saltar por la ventana" + "\n" + "E: Bajar las escaleras y salir por la puerta principal" + "\n" + "T: Subir al tercer piso para seguir explorando salidas");
		c = controler.get_command();
		switch(c) {
		case 'V':
			System.out.println("¡AND.. THE WINNNER IS!");
			controler.play(controler, "/winner.wav", controler.fperson);
			break;
		default:
			controler.game_over(controler);
			break;
		}
		controler.stop(controler, (int)st[1]);
	}

	public void generate_main_scene(MainOpenAl controler) throws Exception {
		boolean end_scene_check = false;
		while(!end_scene_check) {
		long [] src = controler.play(controler,"/alarm_clock.wav", controler.fperson);
		System.out.println("!!!!!!!!!!");
		Thread.sleep(1000);
		System.out.println("La alarma ha sonado!");
		Thread.sleep(1000);
		System.out.println("¡Debes escapar de la casa antes de que el dueño te encuentre!");
		Thread.sleep(1000);
		System.out.println("¿Cual será tu movimiento? (L: Levantarse y caminar hacia el pasillo, Q: Quedarse en cama)");
		char c = controler.get_command();
		end_scene_check = c=='L';
		controler.stop(controler, (int)src[1]);
		}
		controler.generate_second_scene(controler);
	}
	
	public void testScene(int e, MainOpenAl controler) throws Exception {
		switch(e) {
		case 1:
			controler.generate_main_scene(controler);
			break;
		case 2:
			controler.generate_second_scene(controler);
			break;
		}
	}
	
	public static void main(String[] args) throws Exception {
		MainOpenAl controler = new MainOpenAl();
        controler.generate_main_scene(controler);
		//controler.testScene(2, controler);
	}

}
