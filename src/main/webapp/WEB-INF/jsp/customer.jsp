<%@taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<html>
<head>
	<title>Visikard Application</title>
	<style type="text/css">
		body {
			font-family: sans-serif;
		}
		.data, .data td {
			border-collapse: collapse;
			width: 100%;
			border: 1px solid #aaa;
			margin: 2px;
			padding: 2px;
		}
		.data th {
			font-weight: bold;
			background-color: #5C82FF;
			color: white;
		}
	</style>
</head>
<body>

<h2>Contact Manager</h2>

<form:form method="post" action="/vk/customer/add" commandName="customer">

	<table>
	<tr>
		<td><form:label path="name"><spring:message code="label.name"/></form:label></td>
		<td><form:input path="name" value="" /></td> 
	</tr>
	<tr>
		<td><form:label path="age"><spring:message code="label.age"/></form:label></td>
		<td><form:input path="age" value="" /></td>
	</tr>
	<tr>
		<td colspan="2">
			<input type="submit" value="<spring:message code="label.addcontact"/>"/>
		</td>
	</tr>
</table>	
</form:form>

<h3>Contacts</h3>
<c:if  test="${!empty customerList}">
<table class="data">
<tr>
	<th>Name</th>
	<th>Age</th>
	<th>&nbsp;</th>
</tr>
<c:forEach items="${customerList}" var="customer">
	<tr>
		<td>${customer.name}</td>
		<td>${customer.age}</td>
		<td><a href="edit/${customer.custId}">edit</a></td>
		<td><a href="delete/${customer.custId}">delete</a></td>
	</tr>
</c:forEach>
</table>
</c:if>

</body>
</html>
