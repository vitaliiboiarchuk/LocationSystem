<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%--
  Created by IntelliJ IDEA.
  User: boyar
  Date: 28/02/2023
  Time: 14:11
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Title</title>
</head>
<body>

<form:form method="post" modelAttribute="user" action="/shareAdmin">

    <form:hidden path="id"/>
    <form:hidden path="enabled"/>
    <form:hidden path="name"/>
    <form:hidden path="username"/>
    <form:hidden path="password"/>
    <form:hidden path="roles"/>

    <h3>Choose location that you want to share</h3>

    <form:checkboxes path="adminLocations" items="${locations}"
                 itemValue="id" itemLabel="name"/>

    <input type="submit" value="Submit"></input>

</form:form>

</body>
</html>
