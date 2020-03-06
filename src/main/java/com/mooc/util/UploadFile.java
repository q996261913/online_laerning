//package com.mooc.util;
//
//import java.io.File;
//import java.io.IOException;
//import java.io.PrintWriter;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//
//import org.apache.commons.fileupload.FileItem;
//import org.apache.commons.fileupload.disk.DiskFileItemFactory;
//import org.apache.commons.fileupload.servlet.ServletFileUpload;
//
//import com.mooc.entity.Course;
//import org.springframework.web.multipart.MultipartFile;
//
///**
// * 文件上传包装类
// * jpg文件与其他文件分开存放
// *
// */
//public class UploadFile {
//	// 上传文件存储目录
//		private static final String UPLOAD_DIRECTORY = "style\\video";
//		//上传图片存放位置
//		private static final String UPLOADImage_DIRECTORY = "style\\image\\courses";
//
//		// 上传配置
//		private static final int MEMORY_THRESHOLD   = 1024 * 1024 * 300;  // 3MB
//		private static final int MAX_FILE_SIZE      = 1024 * 1024 * 5000; // 500MB
//		private static final int MAX_REQUEST_SIZE   = 1024 * 1024 * 5000; // 500MB
//		/**
//		 * 方法uploadFile("保存的文件名",HttpServletRequest,HttpServletResponse)
//		 * @param refilename
//		 * @param request
//		 * @param response
//		 * @return
//		 * @return
//		 */
//		public static Object uploadFile(String refilename,HttpServletRequest request){
//			System.out.println("哈哈哈欢迎上传文件");
//			/*//程序状态
//			boolean isok = true;*/
//			// 验证表单是否是enctype=multipart/form-data
//	        if (!ServletFileUpload.isMultipartContent(request)) {
//	            // 如果不是则停止
//	        	System.out.println("Error: 表单必须包含 enctype=multipart/form-data");
//	            return null;
//	        }
//
//	        // 配置上传参数
//	        DiskFileItemFactory factory = new DiskFileItemFactory();
//	        // 设置内存临界值 - 超过后将产生临时文件并存储于临时目录中
//	        factory.setSizeThreshold(MEMORY_THRESHOLD);
//	        // 设置临时存储目录
//	        factory.setRepository(new File(System.getProperty("java.io.tmpdir")));
//
//	        ServletFileUpload upload = new ServletFileUpload(factory);
//
//	        // 设置最大文件上传值
//	        upload.setFileSizeMax(MAX_FILE_SIZE);
//
//	        // 设置最大请求值 (包含文件和表单数据)
//	        upload.setSizeMax(MAX_REQUEST_SIZE);
//
//	        // 中文处理
//	        upload.setHeaderEncoding("UTF-8");
//
//	        // 构造临时路径来存储上传的文件
//	        // 这个路径相对当前应用的目录
//	        String uploadPath = request.getServletContext().getRealPath("./")  + UPLOAD_DIRECTORY;
//	        String uploadImagePath = request.getServletContext().getRealPath("./") + UPLOADImage_DIRECTORY;
//
//			System.out.println("uploadpath"+uploadImagePath);
//			System.out.println("uploadImagePath"+uploadImagePath);
//
//	        // 如果目录不存在则创建
//	        File uploadDir = new File(uploadPath);
//	        File uploadImageDir = new File(uploadImagePath);
//	        if (!uploadDir.exists()) {
//	            uploadDir.mkdir();
//	        }
//	        if (!uploadImageDir.exists()) {
//	        	uploadImageDir.mkdir();
//	        }
//	        //map集合
//	        Map<String,String> pmap = new HashMap<>();
//	        Course course = new Course();
//
//	        try {
//	            // 解析请求的内容提取文件数据
//	            @SuppressWarnings("unchecked")
//	            List<FileItem> formItems = upload.parseRequest(request);
//				System.out.println("解析请求成功");
//
//				System.out.println("formItems"+formItems);
//				System.out.println(formItems.size());
//				if (formItems != null && formItems.size() > 0) {
//					System.out.println("判断表单数据成功");
//	                // 迭代表单数据
//	                for (FileItem item : formItems) {
//						System.out.println("遍历for循环");
//						System.out.println(item.getFieldName());
//						System.out.println(item.getString("utf-8"));
//	                    // 处理在表单中的字段
//	                	if(item.isFormField()) {
//	                		//放入map
//	                		pmap.put(item.getFieldName(), item.getString("utf-8"));
//	                	}
//
//	                }
//
//					System.out.println("表单处理结束");
//	                //遍历map,如果没有课程id字段
//	                if(pmap.get("id")==null||pmap.get("id").equals("")) {
//	                	//文件名直接在原基础+1
//						System.out.println("没有id传入");
//	                	refilename = String.valueOf(Integer.parseInt(refilename)+1);
//
//	                }else {
//	                	//否则直接命名为id,覆盖掉原来的文件
//	                	refilename = pmap.get("id");
//	                	/*System.out.println(pmap.get("id"));*/
//	                }
//	                for (FileItem item : formItems) {
//	                    // 处理不在表单中的字段(文件数据)
//	                    if (!item.isFormField()) {
//							System.out.println("处理文件域");
//	                        String fileName = new File(item.getName()).getName();
//	                        //获取文件后缀名
//	                        String fileExtName = fileName.substring(fileName.lastIndexOf(".")+1);
//	                        //如果是视频文件,封装文件路径
//	                        String filePath = uploadPath + File.separator + refilename+"."+fileExtName;
//							System.out.println("视频文件路径"+filePath);
//	                        //如果是图片文件,封装图片路径
//	                        if(fileExtName.equals("jpg")) {
//	                        	  filePath = uploadImagePath + File.separator + refilename+"."+fileExtName;
//								System.out.println("图片文件路径"+filePath);
//	                        }
//	                        File storeFile = new File(filePath);
//	                        // 在控制台输出文件的上传路径
//	                        System.out.println(filePath);
//	                        // 保存文件到硬盘
//	                        item.write(storeFile);
//	                        System.out.println("文件上传成功!");
//	                        /*request.setAttribute("message",
//	                            "文件上传成功!");*/
//
//	                    }
//	                }
//	            }
//	        } catch (Exception ex) {
//	        	System.out.println( "错误信息: " + ex.getMessage());
//	           /* request.setAttribute("message",
//	                    "错误信息: " + ex.getMessage());*/
//	        }
//	        if(pmap.get("id")!=null&&!pmap.get("id").equals("")) {
//	        course.setId(Integer.parseInt(pmap.get("id")));
//	        }
//	        course.setName(pmap.get("name"));
//	        course.setContext(pmap.get("context"));
//	        course.setType(pmap.get("type"));
//	        course.setPrice("1");
//	        return course;
//		}
//
//		public static void uploadFile(){
//
//		}
//}
