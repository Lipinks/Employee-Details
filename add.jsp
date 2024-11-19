<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<html>
<head>
  <link rel="stylesheet" type="text/css" href="add_style.css">
</head>
<body>
  <div class="main">
    <h2>ADD EMPLOYEE DETAILS</h2>
    <form action="add.do?method=add" method="post" enctype="multipart/form-data">

      <div class="form-field">
        <label for="employeeId">Employee ID</label>
        <input type="text" name="employeeId" id="employeeId" placeholder="Enter ID"/>
      </div>

      <div class="form-field">
        <label for="displayName">Display Name</label>
        <input type="text" name="displayName" id="displayName" placeholder="Enter Display Name"/>
      </div>

      <div class="form-field">
        <label for="description">Description</label>
        <input type="text" name="description" id="description" placeholder="Enter Description"/>
      </div>

      <div class="form-field">
        <label for="telephone">Telephone</label>
        <input type="text" name="telephone" id="telephone" placeholder="Enter Phone Number"/>
      </div>

      <div class="form-field">
        <label for="email">Email</label>
        <input type="text" name="email" id="email" placeholder="Enter E-mail Address"/>
      </div>

      <div class="form-field">
        <label for="street">Street</label>
        <input type="text" name="street" id="street" placeholder="Enter Street Name"/>
      </div>

      <div class="form-field">
        <label for="photo">Choose Photo</label>
        <input type="file" name="photo" id="photo" class="photo_upload"/>
      </div>
      <div>
        <button type="submit" value="Submit" class="submit-button">UPLOAD</button>
      </div>
    </form>
  </div>
</body>
</html>