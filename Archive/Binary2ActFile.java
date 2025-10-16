import java.io.*;
import java.util.Arrays;

public class Binary2ActFile
{
    public static final String OUTPUT_FILE = "IMG_test_cases.txt";
    public static final String BINARY_FILE = "_processed_test.bin";

    public static void write2File(String inputFile, FileWriter out, int width, int height) throws Exception
    {
        InputStream inputStream = new FileInputStream(inputFile);
        for (int i = 0; i < width; i++)
        {
            for (int j = 0; j < height; j++)
            {
                double val = inputStream.read() / 255.0;
                out.write(Double.toString(val) + " ");
            }
        }
        inputStream.close();
    }
    public static void main(String[] args) throws Exception
    {
        FileWriter out = new FileWriter(OUTPUT_FILE);
        
        String[] imageProcessList = Image2GrayBin.listFiles(BINARY_FILE);
        Arrays.sort(imageProcessList);
        /* 
        String[] imageProcessListOrg = new String[imageProcessList.length];
        int finger = 1;
        int caseNum = 2;
        for(int i=0; i<25; i++)
        {
            if(imageProcessList[i].equals(finger + "_" + caseNum + BINARY_FILE))
            {
                imageProcessListOrg[i] = imageProcessList[i];
            }
        }
        */

        String fileName;

        String[][] output = {{"0", "0", "0", "0", "1"},
                             {"0", "0", "0", "1", "0"},
                             {"0", "0", "1", "0", "0"},
                             {"0", "1", "0", "0", "0"},
                             {"1", "0", "0", "0", "0"}};
                             
        for (String fileNameExt : imageProcessList)
        {
            write2File(Image2GrayBin.IMG_DIR + fileNameExt, out, 100, 100);
            System.out.println(fileNameExt);
            for (int i = 0; i < 5; i++)
            {
                out.write(output[Character.getNumericValue(fileNameExt.charAt(0)) - 1][i] + " ");
            }
            out.write("\n");
        }
        
        out.close();
    }
}