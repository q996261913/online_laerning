package com.mooc.util;

import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;

public class FileUtils {

   private static final String UPLOAD_DIRECTORY = "style\\video";
   private static final String UPLOADImage_DIRECTORY = "style\\image\\courses";

   public  static  boolean uploadFile(MultipartFile ogg, MultipartFile jpg, HttpServletRequest request, int maxId){
       String uploadOggPath = request.getServletContext().getRealPath("./")  + UPLOAD_DIRECTORY;
       String uploadImagePath = request.getServletContext().getRealPath("./") + UPLOADImage_DIRECTORY;
       //视频路径
       String oggFilePath=uploadOggPath+ File.separator+maxId+".ogg";
       //图片路径
       String ImageFilePath=uploadImagePath+File.separator+maxId+".jpg";
        //视频文件对象
       File oggFile=new File(oggFilePath);
       File imageFile=new File(ImageFilePath);
       try {
           ogg.transferTo(oggFile);
           jpg.transferTo(imageFile);
       } catch (IOException e) {
           return  false;
       }

       return  true;
   }

    public static boolean removeFile(HttpServletRequest request, int courseid) {
        String uploadOggPath = request.getServletContext().getRealPath("./")  + UPLOAD_DIRECTORY;
        String uploadImagePath = request.getServletContext().getRealPath("./") + UPLOADImage_DIRECTORY;
        //视频路径
        String oggFilePath=uploadOggPath+ File.separator+courseid+".ogg";
        //图片路径
        String ImageFilePath=uploadImagePath+File.separator+courseid+".jpg";
        //视频文件对象
        File oggFile=new File(oggFilePath);
        File imageFile=new File(ImageFilePath);
        if(!oggFile.exists()&&!imageFile.exists()){
            return false;
        }
        else{
            oggFile.delete();
            imageFile.delete();
            return true;
        }
    }
}
