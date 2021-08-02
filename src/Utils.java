import java.io.File;
import javax.swing.filechooser.FileFilter;

public class Utils {

    public static FileFilter csvFileFilter = new FileFilter() {
        @Override
        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }
            String extension = Utils.getExtension(f);
            return extension.equals("csv");
        }

        @Override
        public String getDescription() {
            return "comma-separated CSV file";
        }
    };

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
