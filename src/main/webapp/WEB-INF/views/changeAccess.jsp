<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
  Created by IntelliJ IDEA.
  User: boyar
  Date: 01/03/2023
  Time: 11:17
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Title</title>
</head>
<body>

<form:form method="post" modelAttribute="user" action="/changeAccess">

    <form:hidden path="id"/>
    <form:hidden path="enabled"/>
    <form:hidden path="name"/>
    <form:hidden path="username"/>
    <form:hidden path="password"/>
    <form:hidden path="roles"/>
    <c:if test="${showAdminLocations}">
        <h3>Do you want to change Read Only Access to Admin Access?</h3>
    <form:input path="adminLocations" type="hidden" value="${location.id.toString()}"/>
    </c:if>
    <c:if test="${showReadOnlyLocations}">
        <h3>Do you want to change Admin Access to Read Only Access?</h3>
        <form:input path="readOnlyLocations" type="hidden" value="${location.id.toString()}"/>
    </c:if>

    <button type="submit">Yes</button>

</form:form>


</body>
</html>
