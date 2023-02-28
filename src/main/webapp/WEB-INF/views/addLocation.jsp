<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%--
  Created by IntelliJ IDEA.
  User: boyar
  Date: 28/02/2023
  Time: 11:45
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Title</title>
</head>
<body>

<form:form method="post" modelAttribute="location">

    <form:input path="user" type="hidden" value="${user.id.toString()}"/>

    Name:
    <form:input path="name" type="text"/>
    <form:errors path="name" class="alert alert-danger"/>

    Address:
    <form:input path="address" type="text"/>
    <form:errors path="address" class="alert alert-danger"/>

    <button class="btn btn-primary w-100" type="submit">Add Location</button>


</form:form>
</body>
</html>
