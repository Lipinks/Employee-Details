import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;
import org.apache.struts.upload.FormFile;

public class AddAction extends DispatchAction {
	
	public ActionForward add(ActionMapping mapping, ActionForm form,
                             HttpServletRequest request, HttpServletResponse response) {
        EmployeeDetailsForm employeeForm = (EmployeeDetailsForm) form;
        
		
        String employeeId = employeeForm.getEmployeeId();
        String displayName = employeeForm.getDisplayName();
        String description = employeeForm.getDescription();
        String telephone = employeeForm.getTelephone();
        String email = employeeForm.getEmail();
        String street = employeeForm.getStreet();
		FormFile photo = employeeForm.getPhoto();
		
		//*********SAVING THE SELECTED FILE IN A TEMPORARY DIRECTORY**********
		
		try {
            // Define the directory to save the uploaded file
            String uploadDirectory = "C:/Program Files/Apache Software Foundation/Tomcat 9.0/webapps/myApp/photos";
            File uploadDir = new File(uploadDirectory);

            // Save the uploaded file
            String filePath = uploadDirectory + "/" + photo.getFileName();
            InputStream inputStream = photo.getInputStream();
            FileOutputStream outputStream = new FileOutputStream(filePath);

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();
            inputStream.close();

        } catch (Exception e) {}
		
		//***********************************************************

        AddHandler addHandler = new AddHandler();
		
		String photo_path="C:/Program Files/Apache Software Foundation/Tomcat 9.0/webapps/myApp/photos/"+photo.getFileName();
        String status =addHandler.uploadEmployeeDetails(employeeId, displayName, description, telephone, email, street, photo_path);
		
		if ("Success".equals(status)) {
            request.setAttribute("status", "success");
        } else {
            request.setAttribute("status", "fail");
        }
        return mapping.findForward("view");
    }
	
	public ActionForward view(ActionMapping mapping, ActionForm form,
                                HttpServletRequest request, HttpServletResponse response) {
		return mapping.findForward("success");
    }
}