<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Title</title>
</head>
<body>
<h3>Friends on location</h3>
<table class="table">
    <thead>
    <tr>
        <th scope="col">Name</th>
        <th scope="col">Email</th>
    </tr>
    </thead>
    <tbody>
    <c:if test="${showOwner}">
        <tr>
            <td>${owner.name}</td>
            <td>${owner.username}</td>
            <td>(Owner)</td>
        </tr>
    </c:if>
    <c:forEach items="${adminAccess}" var="user">
        <tr>
            <td>${user.name}</td>
            <td>${user.username}</td>
            <td>(Admin access)</td>
            <c:if test="${showOwnerActions}">
                <td><a href="<c:url value="/location/${locationId}/${user.id}/"/>">Change access</a></td>
            </c:if>
        </tr>
    </c:forEach>
    <c:forEach items="${readAccess}" var="user">
        <tr>
            <td>${user.name}</td>
            <td>${user.username}</td>
            <td>(Read access)</td>
            <c:if test="${showOwnerActions}">
                <td><a href="<c:url value="/location/${locationId}/${user.id}/"/>">Change access</a></td>
            </c:if>
        </tr>
    </c:forEach>
    </tbody>
</table>
</body>
</html>
