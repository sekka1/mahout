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

<h2>EDIT ME NOW!</h2>

<form:form method="post" action="/vk/customer/update" commandName="customer">
	<form:input type="hidden" path="custId" value="${customer.custId}" />
	<table>
	<tr>
		<td><form:label path="name"><spring:message code="label.name"/></form:label></td>
		<td><form:input path="name" value="${customer.name}" /></td> 
	</tr>
	<tr>
		<td><form:label path="age"><spring:message code="label.age"/></form:label></td>
		<td><form:input path="age" value="${customer.age}" /></td>
	</tr>
	<tr>
		<td colspan="2">
			<input type="submit" value="update"/>
		</td>
	</tr>
</table>	
</form:form>

</body>
</html>
