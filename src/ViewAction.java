import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import org.json.JSONArray;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

public class ViewAction extends Action {

    public ActionForward execute(ActionMapping mapping, ActionForm form,
                                 HttpServletRequest request, HttpServletResponse response)
            throws Exception{
				
        ViewHandler vh = new ViewHandler();
		JSONArray employeeArray = vh.getEmployeeDetailsFromDB();
        
        request.setAttribute("employeeData", employeeArray);
		
		String employeeAdded = request.getParameter("status");
		
		if ("success".equals(employeeAdded) || "fail".equals(employeeAdded)) {
			request.setAttribute("employeeAdded", employeeAdded);
		}

		return mapping.findForward("success");
    }
}