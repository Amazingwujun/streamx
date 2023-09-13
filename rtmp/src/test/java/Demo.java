import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author Jun
 * @since 1.0.0
 */
public class Demo {

    public static void main(String[] args) {
        String videoFilePath = "E:\\Project\\streamx\\flv.flv";
        long videoPts = 0;
        long audioPts = 0;

        try (FileInputStream fis = new FileInputStream(videoFilePath)) {
            byte[] buffer = new byte[4096];
            ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);

            while (fis.read(buffer) != -1) {
                byte videoFlags = byteBuffer.get(0);
                byte audioFlags = byteBuffer.get(1);

                // 计算视频帧的PTS和DTS
                long videoPtsDelta = ((buffer[2] & 0xff) << 16) | ((buffer[3] & 0xff) << 8) | (buffer[4] & 0xff);
                videoPts += videoPtsDelta;
                long videoDts = videoPts;

                // 计算音频帧的PTS和DTS
                long audioPtsDelta = ((buffer[5] & 0xff) << 16) | ((buffer[6] & 0xff) << 8) | (buffer[7] & 0xff);
                audioPts += audioPtsDelta;
                long audioDts = audioPts;

                System.out.println("Video PTS: " + videoPts + ", DTS: " + videoDts);
                System.out.println("Audio PTS: " + audioPts + ", DTS: " + audioDts);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
