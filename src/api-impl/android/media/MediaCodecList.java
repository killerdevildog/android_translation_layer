package android.media;

public class MediaCodecList {

	public MediaCodecList(int kind) {}

	public static int getCodecCount() {
		return 6;
	}

	public static MediaCodecInfo getCodecInfoAt(int index) {
		switch (index) {
			case 0:
				return new MediaCodecInfo("aac", "audio/mp4a-latm");
			case 1:
				return new MediaCodecInfo("h264", "video/avc");
			case 2:
				return new MediaCodecInfo("mp3", "audio/mpeg");
			case 3:
				return new MediaCodecInfo("opus", "audio/opus");
			case 4:
				return new MediaCodecInfo("vp8", "video/x-vnd.on2.vp8");
			case 5:
				return new MediaCodecInfo("vp9", "video/x-vnd.on2.vp9");
			default:
				return null;
		}
	}

	public MediaCodecInfo[] getCodecInfos() {
		MediaCodecInfo[] infos = new MediaCodecInfo[getCodecCount()];
		for (int i = 0; i < infos.length; i++)
			infos[i] = getCodecInfoAt(i);
		return infos;
	}
}
