<%@ page import="org.json.JSONArray, org.json.JSONObject" %>
<%@ page import="org.json.JSONArray" %>
<%@ page import="org.json.JSONObject" %>

<html>
<head>
	<link rel="stylesheet" type="text/css" href="view_style.css">
</head>
<body>
    <h2>EMPLOYEE LIST</h2>
	<% if ("success".equals(request.getAttribute("status")) || "fail".equals(request.getAttribute("status"))) 
		{
			if ("success".equals(request.getAttribute("status"))) 
			{
				out.println("<p>Employee added successfully.</p>");
			} 
			else{
				out.println("<p>There is some issues while adding the details to Azure AD. Please try again.</p>");
			}
		}
	%>
    <table border="1">
      <tr class="row">
        <th><strong>EmployeeId</strong></th>
        <th><strong>Employee Name</strong></th>
        <th><strong>Description</strong></th>
        <th><strong>Telephone</strong></th>
        <th><strong>Email</strong></th>
        <th><strong>Street</strong></th>
      </tr>
    <%
    JSONArray jsonArray = (JSONArray) request.getAttribute("employeeData");
    if(jsonArray.length()==0)
    {
      out.println("<p><strong>NO DETAILS FOUND</strong></p>");
    }
    else
    {
      for (int i = 0; i<jsonArray.length(); i++) 
      {
          JSONObject employee = jsonArray.getJSONObject(i);
      %>
          <tr class="row">
              <td> <%= employee.get("employeeId") %></td>
              <td> <%= employee.get("displayNameName") %></td>
              <td> <%= employee.get("description") %></td>
              <td> <%= employee.get("mobilePhone") %></td>
              <td> <%= employee.get("userPrincipalName") %></td>
              <td> <%= employee.get("streetAddress") %></td>
          </tr>
    <%
      }
    }
    %>
    </table>
	
	<%
		out.println("<button><a href='add.do?method=view'>Add Employee Details</a></button>"); 
	%>
</body>
</html>