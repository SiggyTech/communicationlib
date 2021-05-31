package com.siggytech.utils.communication.util;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Objects;

public class FileUtil {
    private static final String TAG = "FileUtil";

	public static boolean saveFile(String fileName, String folderName, String text) {

	    File file;
	    boolean success = true;

        File filePath = getPath("");

        if (folderName.trim().length() > 0){
            file = new File(filePath.getAbsolutePath() + File.separator + folderName + File.separator + fileName);
        }
        else{
            file = new File(filePath.getAbsolutePath() + File.separator + fileName);
        }

        try{
            writeFile(file, text);
        }
        catch(Exception ex){
            return false;
        }


		return success;
	}
	
	private static void writeFile(File file, String text) throws IOException {
		
		BufferedWriter bwEscritor = new BufferedWriter(new FileWriter(file));
		bwEscritor.write(text);
		bwEscritor.close();
	}


    public static void writeInFile(File file, String text) {
        try {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(file, true));
            buf.append(text);
            buf.newLine();
            buf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	
	public static String readFile(String fileName, String folder){
		 File file;
		 String content = "";

         File filePath = getPath("");

         if (folder.trim().length() > 0){
             file = new File(filePath.getAbsolutePath() + File.separator + folder + File.separator + fileName);
         }
         else{
             file = new File(filePath.getAbsolutePath() + File.separator + fileName);
         }

         try{
             content = readFile(file);
         }
         catch(Exception ex){
             ex.printStackTrace();
         }


		 return content;
	}
	
	private static String readFile(File file) throws IOException {
		
		BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
		StringBuilder stringBuilder = new StringBuilder();
		String sLinea = "";
		
		while ((sLinea = bufferedReader.readLine()) != null){
            stringBuilder.append(sLinea);
		}

        bufferedReader.close();
		return stringBuilder.toString();
	}
	
	public static boolean deleteFile(String folder, String fileName){
		
		File file;
		boolean success = false;

        File path = getPath("");

        if (folder.trim().length() > 0){
            file = new File(path.getAbsolutePath() + File.separator + folder + File.separator + fileName);
        }
        else{
            file = new File(path.getAbsolutePath() + File.separator + fileName);
        }

        if (file.exists()) {
            success = deleteRecursiveFile(file);
        }

		return success;
	}

    private static boolean deleteRecursiveFile(File file) {
	    boolean deleted = false;
	    try{
            if(file.isDirectory()
                    && Objects.requireNonNull(file.listFiles()).length > 0) {
                File[] dirs = file.listFiles();
                assert dirs != null;
                for (File dir : dirs) {
                    if (dir.isDirectory()) {
                        deleted = deleteRecursiveFile(dir);
                    } else deleted = dir.delete();
                }
            }else{
                deleted = file.delete();
            }
        }catch (Exception e){
	        e.printStackTrace();
        }
	    return deleted;
    }

    public static boolean fileExists(String folder, String fileName){
		return getFile(folder,fileName).exists();
	}

    public static File getFile(String folder, String fileName){
        File file;
        File filePath = getPath("");

        if (folder.trim().length() > 0){
            if(folder.endsWith("/")){
                folder = folder.substring(0, folder.length()-1);
            }
            if(folder.startsWith(filePath.getAbsolutePath())){
                file = new File(folder + File.separator + fileName);
            }
            else{
                file = new File(filePath.getAbsolutePath() + File.separator + folder + File.separator + fileName);
            }
        }
        else{
            file = new File(filePath.getAbsolutePath() + File.separator + fileName);
        }

        return file;
    }
	
	public static File getPath(String type){
		File file;

        if("DB".equals(type)){
            file = new File("/data/data/" + Conf.APPLICATION_ID + "/databases");
        }
        else if (Conf.ROOT_FOLDER.equals(type)){
            file = new File("/data/data/"+ Conf.APPLICATION_ID +"/files/"+Conf.ROOT_FOLDER);
        }
        else{
            file = new File("/data/data/"+ Conf.APPLICATION_ID +"/files");
        }

		return file;
	}
	
	public static String createFolder(String folder, String path){
	    String outPath = null;
		File folderFile = null;
		File filePath = getPath("");
	    	
        if(path.endsWith("/")){
            folder = folder.substring(0, folder.length()-1);
        }

        if("".equals(path) || path.startsWith(filePath.getAbsolutePath())){
            folderFile = new File(filePath + File.separator + folder);
        }
        else{
            folderFile = new File(filePath.getAbsolutePath() + File.separator + path + File.separator + folder);
        }

        if (!folderFile.exists()) {
            try{
                folderFile.mkdirs();
            }
            catch (Exception ex){
                ex.printStackTrace();
            }
        }

        if(folderFile.exists()) {
            outPath = folderFile.getAbsolutePath();
        }

		return outPath;
	}

    public static String getImageFileName() {
        File file = getPath(Conf.ROOT_FOLDER);
        if (!file.exists()) {
            file.mkdirs();
        }
        return (file.getAbsolutePath() + "/" + System.currentTimeMillis() + ".jpg");
    }

    public static String readFromFile(String fileName,Context context) {
        String ret = "";
        try {
            InputStream inputStream = context.openFileInput(fileName);

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        } catch (FileNotFoundException e) {
            Log.e(TAG, "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e(TAG, "Can not read file: " + e.toString());
        }

        return ret;
    }

    public static void writeToFile(String fileName,String data,Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(fileName, Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e(TAG, "File write failed: " + e.toString());
        }
    }

    /**
     * Converts a File to Base64
     * @param file file
     * @return base64
     */
    public static String fileToBase64(File file){
        String base64="";
        int size = (int) file.length();
        byte[] bytes = new byte[size];
        try {
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            buf.read(bytes, 0, bytes.length);
            buf.close();

            base64 = toBase64(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return base64;
    }

    /**
     * Converts byte[] to base 64
     * @param byteArray data
     * @return base64
     */
    public static String toBase64(byte[] byteArray){
        return Base64.encodeToString(byteArray,Base64.DEFAULT);
    }

    public static String getFileExt(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }
}