package main;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.http.fileupload.DiskFileUpload;
import org.apache.tomcat.util.http.fileupload.FileItem;
import org.apache.tomcat.util.http.fileupload.FileUploadException;

/**
 * Servlet implementation class Selector
 */
public class Selector extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Selector() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
    protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		// String onto1 = request.getParameter("ontology1");

//		String onto1 = request.getParameter("inputO1");
//		String onto2 = request.getParameter("inputO2");
//		String align = request.getParameter("inputAl");

    	//String mode = request.getParameter("inputO1");
		String mode = "";
		String sid = request.getSession().getId();
		String O1 = "";
		String O2 = "";
		String AL = "";

		
		//if (mode.equalsIgnoreCase("url")) {
//			O1 = downloadFromURL(request.getParameter("onto1url"), sid + "O1");

//			O2 = downloadFromURL(request.getParameter("onto2url"), sid + "O2");

//			AL = downloadFromURL(request.getParameter("alignurl"), sid + "AL");
		//} else if (mode.equalsIgnoreCase("file")) {

			// if (onto1.equalsIgnoreCase("file") ||
			// onto2.equalsIgnoreCase("file") ||
			// align.equalsIgnoreCase("file")){
			DiskFileUpload fu = new DiskFileUpload();
			fu.setSizeMax(1000000);
			fu.setSizeThreshold(4096);
			fu.setRepositoryPath(this.getServletContext().getRealPath("")+"/temps");
			
			try {
				List fileItems = fu.parseRequest(request);
				//System.out.println(fileItems.size());
				Iterator i = fileItems.iterator();
				/*int k = 0;
				while (i.hasNext()){
					FileItem fi = (FileItem) i.next();
					uploadFromPC(fi, sid + k);
					k++;
				}*/
			    FileItem fi = (FileItem) i.next();
				// if(O1.isEmpty()){
				if("url".equalsIgnoreCase(fi.getString())){
					fi = (FileItem) i.next();
					O1 = downloadFromURL(fi.getString(), sid + "O1");
					//System.out.println("Obtenido desde el enlace "+fi.getString());
					i.next();
				} else {
					i.next();
					fi = (FileItem) i.next();
					O1 = uploadFromPC(fi, sid + "O1");
					//System.out.println("Obtenido desde PC");
				}
				fi = (FileItem) i.next();
				// }
				// if(O2.isEmpty()){
				if("url".equalsIgnoreCase(fi.getString())){
					fi = (FileItem) i.next();
					O2 = downloadFromURL(fi.getString(), sid + "O2");
					//System.out.println("Obtenido desde el enlace "+fi.getString());
					i.next();
				} else {
					i.next();
					fi = (FileItem) i.next();
					O2 = uploadFromPC(fi, sid + "O2");
					//System.out.println("Obtenido desde PC");
				}
				fi = (FileItem) i.next();
				// }
				// if(AL.isEmpty()){
				if("url".equalsIgnoreCase(fi.getString())){
					fi = (FileItem) i.next();
					AL = downloadFromURL(fi.getString(), sid + "AL");
					//System.out.println("Obtenido desde el enlace "+fi.getString());
					i.next();
				} else {
					i.next();
					fi = (FileItem) i.next();
					AL = uploadFromPC(fi, sid + "AL");
					//System.out.println("Obtenido desde PC");
				}
				// }
				fi = (FileItem) i.next();
				mode = fi.getString();
			} catch (FileUploadException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		//}

		Interpreter.main(O1, O2, AL, mode, sid, this.getServletContext().getRealPath(""));
		// Interpreter.main(O1, O2, AL, mode, sid);

		// response.sendRedirect(request.getContextPath()+"/"+sid+".html");
		response.sendRedirect(request.getContextPath() + "/" + sid + ".html");
	}

	private String uploadFromPC(FileItem filePart, String name) {
		ServletContext context = this.getServletContext();
    	String path = context.getRealPath("");
		String fullpath = path + "/temps/" + name + ".txt";
		try {
			InputStream reader = filePart.getInputStream();
			// FileOutputStream writer = new
			// FileOutputStream("/ontovis/wtpwebapps/ontovis/temps/" + filename);
			FileOutputStream writer = new FileOutputStream(fullpath);
					//"/atomcat6temps/webapps/ontovis/temps/" + fullpath);
			byte[] buffer = new byte[153600];
			int bytesRead = 0;

			while ((bytesRead = reader.read(buffer)) > 0) {
				writer.write(buffer, 0, bytesRead);
				buffer = new byte[153600];
			}

			writer.close();
			reader.close();
			return fullpath;

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}
	}

	private String downloadFromURL(String u, String name) {
		ServletContext context = this.getServletContext();
    	String path = context.getRealPath("");
		String fullpath = path + "/temps/" + name + ".txt";
		try {
			URL url = new URL(u);
			InputStream reader = url.openStream();
			// FileOutputStream writer = new
			// FileOutputStream("/ontovis/wtpwebapps/ontovis/temps/" + filename);
			FileOutputStream writer = new FileOutputStream(fullpath);
					//"/atomcat6temps/webapps/ontovis/temps/" + filename);
			        //"Archivos de programa/Apache Software Foundation/Tomcat 6.0/webapps/ontovis/temps/" + filename);

			byte[] buffer = new byte[153600];
			int bytesRead = 0;

			while ((bytesRead = reader.read(buffer)) > 0) {
				writer.write(buffer, 0, bytesRead);
				buffer = new byte[153600];
			}

			writer.close();
			reader.close();
			return fullpath;

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}
	}

}
