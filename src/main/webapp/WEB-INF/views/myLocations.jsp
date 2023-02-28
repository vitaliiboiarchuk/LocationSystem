<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
  Created by IntelliJ IDEA.
  User: boyar
  Date: 28/02/2023
  Time: 12:25
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Title</title>
</head>
<body>

<table class="table">
    <thead>
    <tr>
        <th scope="col">Name</th>
        <th scope="col">Address</th>
    </tr>
    </thead>
    <tbody>
    <c:forEach items="${locations}" var="location">
        <tr>
            <td>${location.name}</td>
            <td>${location.address}</td>
            <td>
                <a href="<c:url value="/showMyLocation/${location.id}/"/>">Show</a>
            </td>
        </tr>
    </c:forEach>
    </tbody>
</table>

</body>
</html>
