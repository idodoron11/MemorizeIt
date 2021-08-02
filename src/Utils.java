import java.io.File;

public class Utils {

    public static String getExtension(File f) {
        String ext = "";
        String s = f.getName();
        int i = s.lastIndexOf('.');
        int p = Math.max(f.getName().lastIndexOf('/'), f.getName().lastIndexOf('\\'));

        if (i > p && i > 0 && i < s.length() - 1) {
            ext = s.substring(i+1).toLowerCase();
        }
        return ext;
    }

}
